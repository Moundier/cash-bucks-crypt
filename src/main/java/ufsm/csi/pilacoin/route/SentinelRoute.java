package ufsm.csi.pilacoin.route;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ufsm.csi.pilacoin.services.MiningService;

@RestController
@CrossOrigin("*")
public class SentinelRoute {

    // private final MiningService miningService;

    public SentinelRoute(MiningService miningService) {
        // this.miningService = miningService;
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/startMining")
    public ResponseEntity<?> startMining() {
        // this.difficultyService.startMining();
       return ResponseEntity.ok().build();
    }

    @GetMapping("/stop")
    public void closeApplication() {
        System.exit(0);
    }
}
