package ufsm.csi.pilacoin.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import ufsm.csi.pilacoin.blueprint.TypeGenericStrategy;
import ufsm.csi.pilacoin.common.pilacoin.PilaCoin;
import ufsm.csi.pilacoin.common.pilacoin.PilaCoinService;
import ufsm.csi.pilacoin.common.user.User;
import ufsm.csi.pilacoin.common.user.UserService;
import ufsm.csi.pilacoin.model.PilaValidado;
import ufsm.csi.pilacoin.model.QueryRequest;
import ufsm.csi.pilacoin.model.QueryResponse;
import ufsm.csi.pilacoin.model.QueryResponsePila;
import ufsm.csi.pilacoin.shared.Singleton;

import javax.crypto.Cipher;

import static ufsm.csi.pilacoin.config.Config.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Service
public class RabbitService implements TypeGenericStrategy {

  @Autowired
  private RabbitTemplate rabbitTemplate;
  private final ObjectReader objectReader = new ObjectMapper().reader();
  private final ObjectWriter objectWriter = new ObjectMapper().writer();
  private final Singleton singleton;
  private BigInteger difficulty;

  // TODO: Service Layer
  private final UserService userService;
  private final PilaCoinService pilaCoinService;

  public RabbitService(Singleton singleton, UserService userService, PilaCoinService pilaCoinService) {
    this.singleton = singleton;
    this.userService = userService;
    this.pilaCoinService = pilaCoinService;
  }

  public void send(String topic, String object) {
    this.rabbitTemplate.convertAndSend(topic, object);
  }

  @SneakyThrows
  @RabbitListener(queues={"pila-minerado"})
  public void validatePila(@Payload String pilaCoinStr) {

    if (pilaCoinStr.isEmpty())
      return;

    MessageDigest md = MessageDigest.getInstance("SHA-256");
    BigInteger hash = new BigInteger(md.digest(pilaCoinStr.getBytes(StandardCharsets.UTF_8))).abs();
    PilaCoin coin = this.objectReader.readValue(pilaCoinStr, PilaCoin.class);

    if (this.difficulty == null || coin.getNomeCriador().equals(CONST_NAME) || hash.compareTo(this.difficulty) >= 0) {
      this.send("pila-minerado", pilaCoinStr);
      return;
    }

    Cipher encryptCipher = Cipher.getInstance("RSA");
    encryptCipher.init(Cipher.ENCRYPT_MODE, this.singleton.getPrivateKey());
    byte[] hashByteArr = hash.toString().getBytes(StandardCharsets.UTF_8);

    PilaValidado pilaValidado = PilaValidado.builder()
      .nomeValidador(CONST_NAME)
      .chavePublicaValidador(this.singleton.getPublicKey().toString().getBytes(StandardCharsets.UTF_8))
      .assinaturaPilaCoin(encryptCipher.doFinal(hashByteArr))
      .pilaCoin(coin)
      .build();

    System.out.println(WHITE_BOLD + pilaValidado.getPilaCoin().getNomeCriador() + "'s " + CYAN + "Pila valid!" + RESET);
    String json = this.objectWriter.writeValueAsString(pilaValidado);
    this.send("pila-validado", json);
  }

  @SneakyThrows
  @RabbitListener(queues={"casanova-query"})
  public void query(@Payload String query) {
    QueryResponse queryResponse = this.objectReader.readValue(query, QueryResponse.class);
    System.out.println(queryResponse);
    if (queryResponse.getIdQuery() == 1) {
      List<User> users = queryResponse.getUsuariosResult();
      this.userService.save(users);
    } else if (queryResponse.getIdQuery() == 2) {
      List<QueryResponsePila> pilaCoins = queryResponse.getPilasResult();
      this.pilaCoinService.saveAllQueryResponsePilas(pilaCoins);
    }
  }

  @SneakyThrows
  public void sendQuery(QueryRequest query) {
    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    String queryJson = ow.writeValueAsString(query);
    this.send("query", queryJson);
  }

  /*
   * @RabbitListener(queues = {"casa_nova"})
   * public void rabbitResponse(@Payload Message message) {
   * String responseMessage = new String(message.getBody());
   * String outputColor = responseMessage.contains("erro") ? Colors.ANSI_RED :
   * Colors.ANSI_GREEN;
   * System.out.println(outputColor + responseMessage + Colors.ANSI_RESET);
   * }
   */

  /*
   * @RabbitListener(queues = {"casa_nova-bloco-validado"})
   * public void rabbitBlockResponse(@Payload Message message) {
   * String responseMessage = new String(message.getBody());
   * String outputColor = responseMessage.contains("erro") ? Colors.ANSI_RED :
   * Colors.ANSI_GREEN;
   * System.out.println(outputColor + responseMessage + Colors.ANSI_RESET);
   * }
   * 
   * @RabbitListener(queues = {"casa_nova-pila-validado"})
   * public void rabbitPilaResponse(@Payload Message message) {
   * String responseMessage = new String(message.getBody());
   * String outputColor = responseMessage.contains("erro") ? Colors.ANSI_RED :
   * Colors.ANSI_GREEN;
   * System.out.println(outputColor + responseMessage + Colors.ANSI_RESET);
   * }
   */

  @Override
  public <T> void change(T type) {
    if (type instanceof BigInteger difficulty) {
      this.difficulty = difficulty;
    }
  }
}
