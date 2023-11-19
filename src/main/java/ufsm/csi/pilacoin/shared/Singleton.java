package ufsm.csi.pilacoin.shared;

import lombok.Data;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import ufsm.csi.pilacoin.common.Colors;

@Data
@Component
public class Singleton implements SmartLifecycle {

    // SINGLETON ATTENDS UNIQUE KEYPAIR AND STATISTICAL RESULTS

    private boolean isRunning = false;
    private final Object lock = new Object();
    private PrivateKey privateKey;
    private PublicKey publicKey;

    @SneakyThrows
    private Singleton() {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024);
        this.publicKey = keyPairGenerator.generateKeyPair().getPublic();
        this.privateKey = keyPairGenerator.generateKeyPair().getPrivate();
    }

    // BILL PUGH'S ALGORITHM IMPLEMENTATION

    public static Singleton getInstance() {
        // return GetSingleton.INSTANCE; ERROR
        return null;
    }

    private static class GetSingleton {
        // private static final Singleton INSTANCE = new Singleton(); ERROR
    }

    // STATISTICAL METHODS

    @Autowired
    public SimpMessagingTemplate template;
    private Map<BigInteger, Integer> pilaCoinsFoundPerDifficulty = new HashMap<BigInteger, Integer>();
    private Map<String, Integer> pilaCoinsFoundPerThread = new HashMap<String, Integer>();

    public synchronized Map<BigInteger, Integer> getPilaCoinsFoundPerDifficulty() {
        return pilaCoinsFoundPerDifficulty;
    }

    public synchronized void setPilaCoinsFoundPerDifficulty(Map<BigInteger, Integer> pilaCoinsFoundPerDifficulty) {
        this.pilaCoinsFoundPerDifficulty = pilaCoinsFoundPerDifficulty;
    }

    public synchronized Map<String, Integer> getPilaCoinsFoundPerThread() {
        return pilaCoinsFoundPerThread;
    }

    public synchronized void setPilaCoinsFoundPerThread(Map<String, Integer> pilaCoinsFoundPerThread) {
        this.pilaCoinsFoundPerThread = pilaCoinsFoundPerThread;
    }

    public synchronized void updatePilaCoinsFoundPerDifficulty(BigInteger difficulty) {
        pilaCoinsFoundPerDifficulty.merge(difficulty, 1, (arg0, arg1) -> arg0 + arg1);
        this.template.convertAndSend("/topic/pilacoins_found_per_difficulty", this.pilaCoinsFoundPerDifficulty);
    }

    public synchronized void updatePilaCoinsFoundPerThread(String threadName) {
        pilaCoinsFoundPerThread.merge(threadName, 1, (arg0, arg1) -> arg0 + arg1);
        this.template.convertAndSend("/topic/pilacoins_found_per_thread", this.pilaCoinsFoundPerThread);
    }

    private void concludeStatistical() {
        System.out.println("\n");
        System.out.println(Colors.YELLOW_BOLD_BRIGHT + "Mining Data" + Colors.ANSI_RESET);
        System.out.println(Colors.ANSI_CYAN + TimeFormat.reduplicator("-","Pilacoins found per difficulty") + Colors.ANSI_RESET);
        pilaCoinsFoundPerDifficulty.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .forEach(entry -> {
                    BigInteger k = entry.getKey();
                    Integer v = entry.getValue();
                    System.out.println("| " + Colors.WHITE_BOLD_BRIGHT + k + ": " + Colors.ANSI_GREEN + v + Colors.ANSI_RESET + " |");
                });
        System.out.println(Colors.ANSI_CYAN + TimeFormat.reduplicator("-","Pilacoins found per Thread") + Colors.ANSI_RESET);
        pilaCoinsFoundPerThread.entrySet().stream()
                .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                .forEach(entry -> {
                    String k = entry.getKey();
                    Integer v = entry.getValue();
                    System.out.println("| " + Colors.WHITE_BOLD_BRIGHT + k + ": " + Colors.ANSI_GREEN + v + Colors.ANSI_RESET + " |");
                });
        System.out.println("\n");
    }

    @Override
    public void start() {
        System.out.println("Spring Process Running");
    }

    @Override
    public void stop() {
        synchronized (this.lock) {
            if (this.isRunning) {
                concludeStatistical();
            }
        }
    }

    @Override
    public boolean isRunning() {
        this.isRunning = true;
        return isRunning;
    }

}
