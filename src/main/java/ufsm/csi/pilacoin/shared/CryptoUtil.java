package ufsm.csi.pilacoin.shared;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.PrivateKey;

import javax.crypto.Cipher;
import lombok.SneakyThrows;

public class CryptoUtil {

  @SneakyThrows
  public static BigInteger hash(String json) {
    MessageDigest md = MessageDigest.getInstance("SHA-256");

    return new BigInteger(md.digest(json.getBytes(StandardCharsets.UTF_8))).abs();
  }

  @SneakyThrows
  public static boolean compareHash(String json, BigInteger hash) {
    BigInteger calculateHash = CryptoUtil.hash(json);

    return calculateHash.compareTo(hash) < 0;
  }

  @SneakyThrows
  public static byte[] sign(String json, PrivateKey privateKey) {
    Cipher encrypt = Cipher.getInstance("RSA");
    encrypt.init(Cipher.ENCRYPT_MODE, privateKey);
    byte[] hashByteArray = hash(json).toString().getBytes(StandardCharsets.UTF_8);
    
    return encrypt.doFinal(hashByteArray);
  }
  
}
