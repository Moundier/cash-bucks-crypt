package ufsm.csi.pilacoin.common.pilacoin;

import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface PilaCoinRepo extends Repository<PilaCoin, Long> {
    void save(PilaCoin pilaCoin);
    List<PilaCoin> findAll();
}
