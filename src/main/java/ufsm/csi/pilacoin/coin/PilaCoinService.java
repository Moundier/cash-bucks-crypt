package ufsm.csi.pilacoin.coin;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import ufsm.csi.pilacoin.model.PilaTransfer;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Service
public class PilaCoinService {
    private final PilaCoinRepository pilaCoinRepository;

    public PilaCoinService(PilaCoinRepository pilaCoinRepository) {
        this.pilaCoinRepository = pilaCoinRepository;
    }

    @Transactional
    public void transferPila(PilaCoin pilaCoin, String target_username, String target_user_key) {
        PilaTransfer pilaTransfer = PilaTransfer.builder()
                .nomeUsuarioDestino(target_username)
                .noncePila(pilaCoin.getNonce())
                .chaveUsuarioDestino(target_user_key.getBytes(StandardCharsets.UTF_8))
                .dataTransacao(new Date(System.currentTimeMillis()))
                .build();

        System.out.println(pilaTransfer);
    }

    public void save(PilaCoin pilaCoin) {
        pilaCoin.setId(0l);
        this.pilaCoinRepository.save(pilaCoin);
    }

    public List<PilaCoin> getPilaCoins() {
        return this.pilaCoinRepository.findAll();
    }
}
