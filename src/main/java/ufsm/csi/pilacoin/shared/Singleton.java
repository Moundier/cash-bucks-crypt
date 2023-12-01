package ufsm.csi.pilacoin.shared;

import lombok.Data;
import lombok.SneakyThrows;

import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static ufsm.csi.pilacoin.config.Config.*;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

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

  @SneakyThrows
  private Singleton() {
    FileInputStream fosPublic = new FileInputStream("/home/casanova/Desktop/virtual-bucks/src/main/resources/keys/public_key.der");
    FileInputStream fosPrivate = new FileInputStream("/home/casanova/Desktop/virtual-bucks/src/main/resources/keys/private_key.der");
    byte[] encodedPublic = new byte[fosPublic.available()];
    byte[] encodedPrivate = new byte[fosPrivate.available()];
    fosPublic.read(encodedPublic);
    fosPrivate.read(encodedPrivate);
    fosPublic.close();
    fosPrivate.close();

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublic);
    PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivate);
    this.publicKey = keyFactory.generatePublic(publicKeySpec);
    this.privateKey = keyFactory.generatePrivate(privateKeySpec);
  }

  public static Singleton getInstance() {
    return GetSingleton.INSTANCE;
  }

  private static class GetSingleton {
    private static final Singleton INSTANCE = new Singleton();
  }

  // Utilitary
  
  @SneakyThrows
  public byte[] generateSignature(String str) {
    Cipher encryptCipher = Cipher.getInstance("RSA");
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    encryptCipher.init(Cipher.ENCRYPT_MODE, this.getPrivateKey());
    byte[] hash = md.digest(str.getBytes(StandardCharsets.UTF_8));
    return encryptCipher.doFinal(hash);
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