package ufsm.csi.pilacoin.common.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ufsm.csi.pilacoin.model.QueryRequest;
import ufsm.csi.pilacoin.model.QueryType;
import ufsm.csi.pilacoin.services.RabbitService;

import static ufsm.csi.pilacoin.config.Config.CONST_NAME;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserRoute {

  private final UserService userService;
  private final RabbitService rabbitService;

  @GetMapping("/list")
  public ResponseEntity<List<User>> getUsers() {
     return ResponseEntity.ok(this.userService.find());
  }

  @PostMapping("/update")
  public void update() {
      
    QueryRequest query = QueryRequest.builder()
      .idQuery(1)
      .nomeUsuario(CONST_NAME)
      .tipoQuery(QueryType.USUARIOS)
      .build();

    this.rabbitService.sendQuery(query);
  }
}
