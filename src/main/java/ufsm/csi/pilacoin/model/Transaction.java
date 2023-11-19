package ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

import java.util.Date;

@Data
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {
    private Long id;
    private String status;
    private String chaveUsuarioOrigem;
    private String chaveUsuarioDestino;
    private String assinatura;
    private String noncePila;
    private Date dataTransacao;
}
