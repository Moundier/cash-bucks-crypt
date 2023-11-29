package ufsm.csi.pilacoin.common.pilacoin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PilaCoinRepo extends JpaRepository<PilaCoin, Long> {

}
