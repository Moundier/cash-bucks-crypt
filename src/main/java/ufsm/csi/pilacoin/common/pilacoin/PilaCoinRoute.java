package ufsm.csi.pilacoin.common.pilacoin;

import static ufsm.csi.pilacoin.config.Config.CONST_NAME;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ufsm.csi.pilacoin.model.QueryRequest;
import ufsm.csi.pilacoin.model.QueryResponsePila;
import ufsm.csi.pilacoin.model.QueryType;
import ufsm.csi.pilacoin.model.StatusPila;
import ufsm.csi.pilacoin.services.HashChallengeService;
import ufsm.csi.pilacoin.services.RabbitService;


@RestController
@CrossOrigin("*")
@RequestMapping("/pilacoin")
@RequiredArgsConstructor
public class PilaCoinRoute {

    // private final HashChallengeService hashChallengeService;
    private final PilaCoinService pilaCoinService;
    private final RabbitService rabbitService;

    @GetMapping("/getAll")
    public ResponseEntity<?> getPilas() {
        return ResponseEntity.of(Optional.ofNullable(this.pilaCoinService.getPilaCoins()));
    }

    // TODO: Implement from DifficultyService

    // @GetMapping("/startMining")
    // public ResponseEntity<?> startMining() {
    //     this.hashChallengeService.startMining();
    //     return ResponseEntity.ok().build();
    // }

    // @GetMapping("/stopMining")
    // public ResponseEntity<?> stopMining() {
    //     this.hashChallengeService.stopMining();
    //     return ResponseEntity.ok().build();
    // }

    @GetMapping("/paginationAndSort/{offset}/{pageSize}/{field}")
    public ResponseEntity<Page<PilaCoin>> getPilaCoinsWithPaginationAndSort(@PathVariable int offset, @PathVariable int pageSize, @PathVariable String field) {
        Page<PilaCoin> pilaCoins = this.pilaCoinService.findPilaCoinsWithPaginationAndSorting(offset, pageSize, field);
        return ResponseEntity.ok(pilaCoins);
    }
    @PostMapping("/query")
    public void query() {
        QueryRequest queryRequest = QueryRequest.builder()
                .idQuery(2)
                .tipoQuery(QueryType.PILA)
                .usuarioMinerador(CONST_NAME)
                .nomeUsuario(CONST_NAME)
                .status(StatusPila.VALIDO)
                .build();

        this.rabbitService.sendQuery(queryRequest);
    }

    @DeleteMapping("/deleteAllQueryResponsePilas")
    public ResponseEntity<?> deleteAllQueryResponsePilas() {
       this.pilaCoinService.deleteAllQueryResponsePilas();
       return ResponseEntity.ok().build();
    }

    @GetMapping("/queryResponsePilas/{offset}/{pageSize}/{field}")
    public ResponseEntity<Page<QueryResponsePila>> getQueryResponsePilas(@PathVariable int offset, @PathVariable int pageSize, @PathVariable String field) {
       return ResponseEntity.ok(this.pilaCoinService.getQueryResponsePilas(offset, pageSize, field));
    }
}
