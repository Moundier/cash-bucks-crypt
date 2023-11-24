package ufsm.csi.pilacoin.common.pilacoin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "pilacoin")
@JsonPropertyOrder({ "dataCriacao", "chaveCriador", "nomeCriador", "nonce" })
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PilaCoin implements Cloneable {
   
   @Id
   @JsonIgnore(value = true)
   @Column(name = "pilacoin_id")
   @SequenceGenerator(name = "pilacoin_seq", sequenceName = "pilacoin_pilacoin_id_seq", allocationSize = 1)
   @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pilacoin_seq")
   private Long id;
   private Date dataCriacao;
   private byte[] chaveCriador;
   private String nomeCriador;
   private String status;
   private String nonce;

   @Override
   public PilaCoin clone() {
      try {
         return (PilaCoin) super.clone();
      } catch (CloneNotSupportedException e) {
         throw new AssertionError();
      }
   }
}
