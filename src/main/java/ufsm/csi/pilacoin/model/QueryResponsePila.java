package ufsm.csi.pilacoin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponsePila {
    @Id
    private Long id;
    private Date dataCriacao;
    
    @Column(columnDefinition = "TEXT")
    private String chaveCriador;
    private String nomeCriador;
    private String status;
    
    @Column(columnDefinition = "TEXT")
    private String noncePila;
    
    @Column(columnDefinition = "TEXT")
    private String nonce;
    
    @OneToMany(cascade = CascadeType.PERSIST)
    private List<Transaction> transacoes;
}