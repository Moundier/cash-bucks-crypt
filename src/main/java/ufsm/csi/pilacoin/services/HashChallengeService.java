package ufsm.csi.pilacoin.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import lombok.Data;
import lombok.SneakyThrows;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import ufsm.csi.pilacoin.blueprint.TypeCommon;
import ufsm.csi.pilacoin.blueprint.TypeGenericStrategy;
import ufsm.csi.pilacoin.component.pilacoin.PilaCoin;
import ufsm.csi.pilacoin.component.pilacoin.PilaCoinService;
import ufsm.csi.pilacoin.model.Difficulty;
import ufsm.csi.pilacoin.shared.TimeFormat;
import ufsm.csi.pilacoin.shared.Singleton;

import static ufsm.csi.pilacoin.config.Config.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Data
@Service
public class HashChallengeService implements Runnable, TypeCommon, TypeGenericStrategy {

  /* MINING_SERVICE and DIFFICULTY_SERVICE */

  // MINING TIED FIELDS AND METHODS

  private BigInteger difficulty;
  private final Singleton singleton;
  private final RabbitService rabbitService;
  private final PilaCoinService pilaCoinService;

  private volatile boolean shouldStop = false;

  public HashChallengeService(RabbitService rabbitService, PilaCoinService pilaCoinService, Singleton singleton) {
    this.rabbitService = rabbitService;
    this.pilaCoinService = pilaCoinService;
    this.singleton = singleton;
  }

  @Override
  @SneakyThrows
  public void run() {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    PilaCoin pilaCoin = createPilaCoin();
    Random random = new SecureRandom();
    ObjectWriter objectMapper = new ObjectMapper().writer().withDefaultPrettyPrinter();
    StringBuilder jsonBuilder = new StringBuilder();

    byte[] byteArray = new byte[(256 / 8)];
    byte[] nonceDigest;

    for (int count = 0; !this.shouldStop; count++) {
      random.nextBytes(byteArray); // Inserting random bytes

      // Reuse MessageDigest and ObjectMapper instances
      nonceDigest = md.digest(byteArray);
      pilaCoin.setNonce(new BigInteger(nonceDigest).abs().toString());
      pilaCoin.setDataCriacao(new Date(System.currentTimeMillis()));

      jsonBuilder.setLength(0); // StringBuilder resets on iteration
      jsonBuilder.append(objectMapper.writeValueAsString(pilaCoin));

      // Avoid unnecessary String conversion for hashing
      BigInteger hash = new BigInteger(md.digest(jsonBuilder.toString().getBytes())).abs();

      if (hash.compareTo(this.difficulty) < 0) {
        // Reset count after finding a Pilacoin
        String json = jsonBuilder.toString(); // Reuse the JSON string
        this.rabbitService.send("pila-minerado", json);
        this.singleton.updatePilaCoinsFoundPerDifficulty(this.difficulty);
        this.singleton.updatePilaCoinsFoundPerThread(Thread.currentThread().getName());
        logPilaCoinInfo(count, json);
        this.pilaCoinService.save(pilaCoin);
        count = 0;
      }
    }

    Thread.currentThread().interrupt();
  }

  public void shouldStop() {
    this.shouldStop = true;
  }

  // UTILS FOR THE ABOVE
  private PilaCoin createPilaCoin() {
    return PilaCoin.builder()
        .chaveCriador(this.singleton.getPublicKey().toString().getBytes(StandardCharsets.UTF_8))
        .nomeCriador(CONST_NAME)
        .build();
  }

  private void logPilaCoinInfo(int count, String json) {
    String threadInfo = TimeFormat.threadName(Thread.currentThread());
    System.out.printf("%s%sPilacoin found in %,d tries%s\n", threadInfo, BLACK_BG, count, RESET);
    System.out.println(json);
  }

  // IMPLEMENTATION

  @Override
  public <T> void change(T obj) {
    if (obj instanceof BigInteger difficulty) {
      this.difficulty = difficulty;
    }
  }

  // DIFFICULTY TIED FIELDS AND METHODS
  private boolean firstPilaSent = false;
  private boolean isFirstDifficulty = true;
  private boolean threadsAlreadyStarted = false;
  private BigInteger prevDifficulty;
  private BigInteger currentDifficulty;
  private List<TypeGenericStrategy> listeners = new ArrayList<>();

  private Long timerStart;

  private Runnable shutdown = () -> {
    if (timerStart != null) {
      new TimeMetrics(timerStart);
    }
  };

  private final Thread shutdownThread = new Thread(shutdown);

  {
    // ANONYMOUS GOURMET
    Runtime.getRuntime().addShutdownHook(shutdownThread);
  } 

  @SneakyThrows
  @RabbitListener(queues = { "${queue.dificuldade}" })
  public void getDifficulty(@Payload String difJson) {

    Difficulty difficulty = new ObjectMapper().readValue(difJson, Difficulty.class);
    this.setCurrentDifficulty(new BigInteger(difficulty.getDificuldade(), 16));

    if (!currentDifficulty.equals(this.prevDifficulty) && !this.isFirstDifficulty) {
      logMessage("Difficulty Changed: " + this.currentDifficulty, CYAN);
    }

    if (this.isFirstDifficulty) {
      logMessage("Difficulty Received: " + this.currentDifficulty, YELLOW);
      this.isFirstDifficulty = false;
    }

    this.prevDifficulty = this.currentDifficulty;
    if (!this.threadsAlreadyStarted) {
      this.startMiningThreads(PROCESSORS);
      this.threadsAlreadyStarted = true;
    }
  }

  private void logMessage(String message, String color) {
    System.out.println(color + TimeFormat.sequence("-", message) + RESET);
  }

  public void setCurrentDifficulty(BigInteger difficulty) {
    this.currentDifficulty = difficulty;
    if (listeners.size() != 0) {
      for (TypeGenericStrategy observer : listeners) {
        observer.change(this.currentDifficulty);
      }
    }
  }

  public void startMiningThreads(int threads) {

    if (!firstPilaSent) {
      this.rabbitService.send("pila-minerado", "");
      this.firstPilaSent = true;
    }

    this.timerStart = System.currentTimeMillis(); // Moved the initialization here

    HashChallengeService miningService = new HashChallengeService(
      this.rabbitService, 
      this.pilaCoinService, 
      this.singleton
    );

    this.hold(miningService);
    miningService.change(this.currentDifficulty);

    for (int i = 0; i < threads; i++) {
      new Thread(miningService).start();
    }
  }

  @Override
  public <T> void hold(TypeGenericStrategy obj) {
    this.listeners.add(obj);
  }

  @Override
  public <T> T release(TypeGenericStrategy objs) {
    throw new UnsupportedOperationException("(DifficultyService.java) Unimplemented method 'unsubscribe'");
  }
}