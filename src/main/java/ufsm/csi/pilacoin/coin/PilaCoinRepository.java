package ufsm.csi.pilacoin.coin;

import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface PilaCoinRepository extends Repository<PilaCoin, Long> {
    void save(PilaCoin pilaCoin);
    List<PilaCoin> findAll();
}
