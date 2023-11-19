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
import ufsm.csi.pilacoin.coin.PilaCoin;
import ufsm.csi.pilacoin.coin.PilaCoinService;
import ufsm.csi.pilacoin.common.Constants;
import ufsm.csi.pilacoin.model.Difficulty;
import ufsm.csi.pilacoin.common.Colors;
import ufsm.csi.pilacoin.shared.TimeFormat;
import ufsm.csi.pilacoin.shared.Singleton;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Data
@Service
public class MiningService implements Runnable, TypeCommon, TypeGenericStrategy {

    /* Difficulty & Miner */

    // MINING RELATED ATTRIBUTES AND METHODS

    private BigInteger difficulty;
    private final Singleton singleton;
    private final RabbitService rabbitService;
    private final PilaCoinService pilaCoinService;

    public MiningService(RabbitService rabbitService, PilaCoinService pilaCoinService, Singleton singleton) {
        this.rabbitService = rabbitService;
        this.pilaCoinService = pilaCoinService;
        this.singleton = singleton;
    }

    @Override
    @SneakyThrows
    public void run() {
        BigInteger hash;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String json = "";
        PilaCoin pilaCoin = PilaCoin.builder()
            .chaveCriador(this.singleton.getPublicKey().toString().getBytes(StandardCharsets.UTF_8))
            .nomeCriador(Constants.CONST_NAME)
            .build();
        int count = 0;
        Random random = new Random();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        while (true) {
            byte[] byteArray = new byte[256 / 8];
            random.nextBytes(byteArray);
            pilaCoin.setNonce(new BigInteger(md.digest(byteArray)).abs().toString());
            pilaCoin.setDataCriacao(new Date(System.currentTimeMillis()));
            json = ow.writeValueAsString(pilaCoin);
            hash = new BigInteger(md.digest(json.getBytes(StandardCharsets.UTF_8))).abs();
            count++;
            if (hash.compareTo(this.difficulty) < 0) {
                this.rabbitService.send("pila-minerado", json);
                this.singleton.updatePilaCoinsFoundPerDifficulty(this.difficulty);
                this.singleton.updatePilaCoinsFoundPerThread(Thread.currentThread().getName());
                System.out.printf(TimeFormat.threadName(Thread.currentThread())
                        + Colors.BLACK_BACKGROUND + "Pilacoin found in " + Colors.WHITE_BOLD_BRIGHT + "%,d" + " tries"
                        + Colors.ANSI_RESET + "\n", count);
                System.out.println(json);
                this.pilaCoinService.save(pilaCoin);
                count = 0;
            }
        }
    }

    @Override
    public <T> void update(T obj) {
        if (obj instanceof BigInteger difficulty) {
            this.difficulty = difficulty;
        }
    }

    // DIFFICULTY RELATED ATTRIBUTES AND METHODS

    private boolean firstPilaSent = false;
    private boolean isFirstDifficulty = true;
    private boolean threadsAlreadyStarted = false;
    private BigInteger prevDifficulty;
    private BigInteger currentDifficulty;
    private List<TypeGenericStrategy> observers = new ArrayList<>();

    private Long timerStart;

    private final Thread shutdownThread = new Thread(() -> {
        if (timerStart != null) {
            new TimeMetrics(timerStart);
        }
    });

    {
        Runtime.getRuntime().addShutdownHook(shutdownThread); // ANONYMOUS GOURMET
    }

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
            this.startMiningThreads(Constants.PROCESSORS);
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
            MiningService miningService = new MiningService(this.rabbitService, pilaCoinService, singleton);
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