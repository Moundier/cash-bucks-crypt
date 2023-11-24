package ufsm.csi.pilacoin.common.pilacoin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping("/pilacoin")
public class PilaCoinRoute {

    // private final PilaCoinService pilaCoinService;

    public PilaCoinRoute(PilaCoinService pilaCoinService) {
        // this.pilaCoinService = pilaCoinService;
    }

    @GetMapping
    public ResponseEntity<?> getPilas() {
        return null;
    }
}
