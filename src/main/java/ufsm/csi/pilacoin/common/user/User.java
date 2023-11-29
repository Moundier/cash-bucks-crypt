package ufsm.csi.pilacoin.common.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "table_user")
@Entity
public class User {
  
  @Id
  private Long id;
  private byte[] chavePublica;
  private String nome;
}
