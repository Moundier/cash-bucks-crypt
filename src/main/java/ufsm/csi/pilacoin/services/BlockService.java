package ufsm.csi.pilacoin.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.SneakyThrows;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import ufsm.csi.pilacoin.common.Colors;
import ufsm.csi.pilacoin.common.Constants;
import ufsm.csi.pilacoin.blueprint.TypeCommon;
import ufsm.csi.pilacoin.blueprint.TypeGenericStrategy;
import ufsm.csi.pilacoin.model.Block;
import ufsm.csi.pilacoin.model.BlocoValidado;
import ufsm.csi.pilacoin.shared.TimeFormat;
import ufsm.csi.pilacoin.shared.Singleton;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Service
public class BlockService implements Runnable, TypeCommon, TypeGenericStrategy {

    private final RabbitService rabbitService;

    private Block currentBlock;
    private List<TypeGenericStrategy> observers = new ArrayList<>();

    // Specific
    private boolean miningThreadsStarted = false;
    private final Singleton sharedResources;
    private final ObjectReader reader = new ObjectMapper().reader();
    private final ObjectWriter writer = new ObjectMapper().writer();

    // Newer
    private final MiningService miningService;

    public BlockService(
            RabbitService rabbitService,
            Singleton sharedResources,
            MiningService miningService) {
        this.miningService = miningService;
        this.rabbitService = rabbitService;
        this.sharedResources = sharedResources;
    }

    public void startBlockMiningThreads(int threads) {
        IntStream.range(0, threads)
                .mapToObj(i -> new BlockService(this.rabbitService, Singleton.getInstance(), this.miningService))
                .peek(miningService -> {
                    this.observers.add(miningService);
                    this.miningService.subscribe(miningService);
                    miningService.update(this.currentBlock);
                    miningService.update(this.miningService.getCurrentDifficulty());
                })
                .forEach(miningService -> new Thread(miningService).start());
    }

    @SneakyThrows
    @RabbitListener(queues = { "descobre-bloco" })
    public void findBlocks(@Payload String blockStr) {
        this.currentBlock = this.reader.readValue(blockStr, Block.class);
        if (!this.miningThreadsStarted) {
            this.startBlockMiningThreads(Constants.PROCESSORS);
            this.miningThreadsStarted = true;
        }
    }

    @SneakyThrows
    @RabbitListener(queues = { "bloco-minerado" })
    public void validateBlock(@Payload String blockStr) {

        // LISTEN TO MINED BLOCKS AND VALIDATE
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        BigInteger hash = new BigInteger(md.digest(blockStr.getBytes(StandardCharsets.UTF_8))).abs();
        Block block = this.reader.readValue(blockStr, Block.class);

        BigInteger difficulty = this.miningService.getCurrentDifficulty();

        if (difficulty == null || hash.compareTo(difficulty) >= 0) {
            this.rabbitService.send("bloco-minerado", blockStr);
            return;
        }

        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, this.sharedResources.getPrivateKey());
        byte[] hashByteArr = hash.toString().getBytes(StandardCharsets.UTF_8);

        BlocoValidado blocoValidado = BlocoValidado.builder()
            .nomeValidador(Constants.CONST_NAME)
            .bloco(block)
            .assinaturaBloco(encryptCipher.doFinal(hashByteArr))
            .chavePublicaValidador(this.sharedResources.getPublicKey().toString().getBytes(StandardCharsets.UTF_8))
            .build();

        String json = this.writer.writeValueAsString(blocoValidado);
        this.rabbitService.send("bloco-validado", json);
    }

    // IMPLEMENTATIONS

    @Override
    public <T> void subscribe(TypeGenericStrategy obj) {
        this.observers.add(obj);
    }

    @Override
    public <T> T unsubscribe(TypeGenericStrategy objs) {
        this.observers.remove(objs);
        throw new UnsupportedOperationException("(BlockService.java) Unimplemented method 'unsubscribe'");
    }

    // BLOCKMINING SERVICE RELATED

    private Block block;
    private BigInteger difficulty;
    private final ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();

    @Override
    @SneakyThrows
    public void run() {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        this.block.setChaveUsuarioMinerador(
                this.sharedResources.getPublicKey().toString().getBytes(StandardCharsets.UTF_8));

        for (int count = 1; true; count++) {
            byte[] byteArray = new byte[256 / 8];
            new Random().nextBytes(byteArray);

            this.block.setNonce(new BigInteger(md.digest(byteArray)).abs());

            String json = objectWriter.writeValueAsString(this.block);
            BigInteger hash = new BigInteger(md.digest(json.getBytes(StandardCharsets.UTF_8))).abs();

            if (this.difficulty != null && hash.compareTo(this.difficulty) < 0) {
                printBlockFoundMessage(count, json);
                this.rabbitService.send("bloco-minerado", json);
            }
        }
    }

    private void printBlockFoundMessage(int count, String json) {
        System.out.printf(
            TimeFormat.threadName(Thread.currentThread()) +
                Colors.BLACK_BACKGROUND + "Block found in " +
                Colors.WHITE_BOLD_BRIGHT + "%,d" + " tries" +
                Colors.ANSI_RESET + "\n",
            count);
        System.out.println(json);
    }

    @Override
    public <T> void update(T obj) {

        if (obj instanceof Block block)
            this.block = block;

        if (obj instanceof BigInteger difficulty)
            this.difficulty = difficulty;
    }
}
