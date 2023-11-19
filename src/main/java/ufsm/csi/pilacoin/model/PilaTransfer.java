package ufsm.csi.pilacoin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder(alphabetic = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PilaTransfer {
    public byte[] chaveUsuarioOrigem;
    public byte[] chaveUsuarioDestino;
    public String nomeUsuarioOrigem;
    public String nomeUsuarioDestino;
    public byte[] assinatura;
    public String noncePila;
    public Date dataTransacao;
    public Long id;
    public String status;
}
