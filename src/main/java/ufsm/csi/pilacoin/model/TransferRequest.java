package ufsm.csi.pilacoin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ufsm.csi.pilacoin.common.pilacoin.PilaCoin;
import ufsm.csi.pilacoin.common.user.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private User user;
    private PilaCoin pilaCoin;
}