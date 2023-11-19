package ufsm.csi.pilacoin.services;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.SneakyThrows;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import ufsm.csi.pilacoin.blueprint.TypeCommon;
import ufsm.csi.pilacoin.blueprint.TypeGenericStrategy;
import ufsm.csi.pilacoin.coin.PilaCoin;
import ufsm.csi.pilacoin.coin.PilaCoinService;
import ufsm.csi.pilacoin.model.Difficulty;
import ufsm.csi.pilacoin.common.Colors;
import ufsm.csi.pilacoin.shared.TimeFormat;
import ufsm.csi.pilacoin.shared.Singleton;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static ufsm.csi.pilacoin.common.Constants.*;

@Data
@Service
public class HashChallengeService implements Runnable, TypeCommon, TypeGenericStrategy {

  /* MINING_SERVICE and DIFFICULTY_SERVICE */

  // MINING TIED FIELDS AND METHODS

  private BigInteger difficulty;
  private final Singleton singleton;
  private final RabbitService rabbitService;
  private final PilaCoinService pilaCoinService;

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

    for (int count = 0; ; count++) {
      byte[] byteArray = new byte[(256 / 8)];
      random.nextBytes(byteArray); // Inserting random bytes 
      pilaCoin.setNonce(new BigInteger(md.digest(byteArray)).abs().toString());
      pilaCoin.setDataCriacao(new Date(System.currentTimeMillis()));
      String json = new ObjectMapper().writer()
        .withDefaultPrettyPrinter()
        .writeValueAsString(pilaCoin);

      BigInteger hash = new BigInteger(md.digest(json.getBytes(StandardCharsets.UTF_8))).abs();

      if (hash.compareTo(this.difficulty) < 0) {
        // Reset count after finding a Pilacoin
        handleMinedPilaCoin(json, pilaCoin, count);
        count = -1; 
      }
    }
  }

  // UTILS FOR THE ABOVE
  private PilaCoin createPilaCoin() {
    return PilaCoin.builder()
        .chaveCriador(this.singleton.getPublicKey().toString().getBytes(StandardCharsets.UTF_8))
        .nomeCriador(CONST_NAME)
        .build();
  }

  private void handleMinedPilaCoin(String json, PilaCoin pilaCoin, int count) {
    this.rabbitService.send("pila-minerado", json);
    this.singleton.updatePilaCoinsFoundPerDifficulty(this.difficulty);
    this.singleton.updatePilaCoinsFoundPerThread(Thread.currentThread().getName());
    logPilaCoinInfo(count, json);
    this.pilaCoinService.save(pilaCoin);
  }

  private void logPilaCoinInfo(int count, String json) {
    String threadInfo = TimeFormat.threadName(Thread.currentThread());
    System.out.printf("%s%sPilacoin found in %,d tries%s\n", threadInfo,
        Colors.BLACK_BACKGROUND, count, Colors.ANSI_RESET);
    System.out.println(json);
  }

  // IMPLEMENTATION

  @Override
  public <T> void update(T obj) {
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
  private List<TypeGenericStrategy> observers = new ArrayList<>();

  private Long timerStart;

  private Runnable shutdown = () -> {
    if (timerStart != null) {
      new TimeMetrics(timerStart);
    }
  };

  private final Thread shutdownThread = new Thread(shutdown);

  { Runtime.getRuntime().addShutdownHook(shutdownThread); } // ANONYMOUS GOURMET 

  @SneakyThrows
  @RabbitListener(queues = { "${queue.dificuldade}" })
  public void getDifficulty(@Payload String difJson) {

    Difficulty difficulty = new ObjectMapper().readValue(difJson, Difficulty.class);
    this.setCurrentDifficulty(new BigInteger(difficulty.getDificuldade(), 16));

    if (!currentDifficulty.equals(this.prevDifficulty) && !this.isFirstDifficulty) {
      logMessage("Difficulty Changed: " + this.currentDifficulty, Colors.ANSI_CYAN);
    }

    if (this.isFirstDifficulty) {
      logMessage("Difficulty Received: " + this.currentDifficulty, Colors.ANSI_YELLOW);
      this.isFirstDifficulty = false;
    }

    this.prevDifficulty = this.currentDifficulty;
    if (!this.threadsAlreadyStarted) {
      this.startMiningThreads(PROCESSORS);
      this.threadsAlreadyStarted = true;
    }
  }

  private void logMessage(String message, String color) {
    System.out.println(color + TimeFormat.reduplicator("-", message) + Colors.ANSI_RESET);
  }

  public void setCurrentDifficulty(BigInteger difficulty) {
    this.currentDifficulty = difficulty;
    if (observers.size() != 0) {
      for (TypeGenericStrategy observer : observers) {
        observer.update(this.currentDifficulty);
      }
    }
  }

  public void startMiningThreads(int threads) {

    if (!firstPilaSent) {
      this.rabbitService.send("pila-minerado", "");
      this.firstPilaSent = true;
    }

    this.timerStart = System.currentTimeMillis(); // Moved the initialization here

    for (int i = 0; i < threads; i++) {
      HashChallengeService miningService = new HashChallengeService(this.rabbitService, pilaCoinService, singleton);
      this.subscribe(miningService);
      miningService.update(this.currentDifficulty);

      new Thread(miningService).start();
    }
  }

  @Override
  public <T> void subscribe(TypeGenericStrategy obj) {
    this.observers.add(obj);
  }

  @Override
  public <T> T unsubscribe(TypeGenericStrategy objs) {
    throw new UnsupportedOperationException("(DifficultyService.java) Unimplemented method 'unsubscribe'");
  }
}