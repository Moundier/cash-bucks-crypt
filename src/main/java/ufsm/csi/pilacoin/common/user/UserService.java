package ufsm.csi.pilacoin.common.user;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepo userRepo;

  public void save(List<User> user) {
    this.userRepo.saveAll(user);
  }

  public List<User> find() {
    return this.userRepo.findAll();
  }
}
