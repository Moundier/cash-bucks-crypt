package ufsm.csi.pilacoin;

import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import lombok.SneakyThrows;

public class Generator {

  private static final String destination = "/home/casanova/Desktop/virtual-bucks/src/main/resources/keys/";

  @SneakyThrows
  public static void main(String[] args) {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(1024);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();
    storeToFile(destination + "public_key.der", keyPair.getPublic().getEncoded());
    storeToFile(destination + "private_key.der", keyPair.getPrivate().getEncoded());
  }

  @SneakyThrows
  private static void storeToFile(String file, byte[] bytes) {
    FileOutputStream fos = new FileOutputStream(file);
    fos.write(bytes);
    fos.close();
    System.out.println("Key stored in file: " + file);
  }
}
