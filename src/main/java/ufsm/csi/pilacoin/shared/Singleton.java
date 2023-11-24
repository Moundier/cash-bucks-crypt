package ufsm.csi.pilacoin.shared;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import static ufsm.csi.pilacoin.config.Config.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Data
@Component
public class Singleton implements SmartLifecycle {

  private PublicKey publicKey;
  private PrivateKey privateKey;
  private boolean isRunning = false;
  private final Object lock = new Object();

  // BILL PUGH'S SINGLETON 

  private Singleton() {
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(1024);
      this.publicKey = keyPairGenerator.generateKeyPair().getPublic();
      this.privateKey = keyPairGenerator.generateKeyPair().getPrivate();
    } catch (NoSuchAlgorithmException e) {
      System.err.println("Error: exception at Singleton.java");
    }
  }

  public static Singleton getInstance() {
    return GetSingleton.INSTANCE;
  }

  private static class GetSingleton {
    private static final Singleton INSTANCE = new Singleton();
  }

  // STATISTICAL METHODS

  @Autowired
  public SimpMessagingTemplate template;
  private Map<BigInteger, Integer> pilaCoinsFoundPerDifficulty = new HashMap<BigInteger, Integer>();
  private Map<String, Integer> pilaCoinsFoundPerThread = new HashMap<String, Integer>();


  // Getters Setters Mergers
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

  private void printStatisticalHistory() {
    System.out.println("\n");
    System.out.println(YELLOW_BOLD + "Mining Data" + RESET);
    System.out.println(CYAN + TimeFormat.sequence("-", "Pilacoins found per difficulty") + RESET);

    sortKeyPair(pilaCoinsFoundPerDifficulty, BigInteger.class);
    System.out.println(CYAN + TimeFormat.sequence("-", "Pilacoins found per Thread") + RESET);
    sortKeyPair(pilaCoinsFoundPerThread, String.class);

    System.out.println("\n");
  }

  // (COINS/BLOCKS) PER THREAD
  private static <K> void sortKeyPair(Map<K, Integer> map, Class<K> key) {
    var pairs = new ArrayList<>(map.entrySet());
    pairs.sort((K, V) -> K.getValue().compareTo(V.getValue()));

    for (var p : pairs) {
      PrintKeyPair(p.getKey(), p.getValue());
    }
  }

  public static <T> void PrintKeyPair(T k, Integer v) {
    System.out.println("| " + WHITE_BOLD + k + ": " + GREEN + v + RESET + " |");
  }

  @Override
  public void start() {
    System.out.println("Spring Process Running");
  }

  @Override
  public void stop() {
    synchronized (this.lock) {
      if (this.isRunning)
        printStatisticalHistory();
    }
  }

  @Override
  public boolean isRunning() {
    return this.isRunning = true;
  }
}