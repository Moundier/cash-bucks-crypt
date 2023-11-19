package ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(NON_NULL)
public class Block {
    private Long numeroBloco;
    private BigInteger nonce;
    private BigInteger nonceBlocoAnterior;
    private byte[] chaveUsuarioMinerador;
    private String nomeUsuarioMinerador;
    private List<Transaction> transacoes;
}
