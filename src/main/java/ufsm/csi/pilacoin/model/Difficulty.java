package ufsm.csi.pilacoin.model;

import lombok.Data;

import java.util.Date;

@Data
public class Difficulty {
    String dificuldade;
    Date inicio;
    Date validadeFinal;
}
