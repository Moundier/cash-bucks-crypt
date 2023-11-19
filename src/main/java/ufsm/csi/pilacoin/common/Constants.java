package ufsm.csi.pilacoin.common;

public class Constants {

    public static final int PROCESSORS = getRuntime(); // 
    public static final String CONST_NAME = "Casanova";

    public static int getRuntime() {
        return Runtime.getRuntime().availableProcessors(); 
    }
}
