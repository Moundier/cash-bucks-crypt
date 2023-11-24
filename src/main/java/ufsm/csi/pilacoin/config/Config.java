package ufsm.csi.pilacoin.config;

public class Config {
 
    // System Requirements
    public static final int PROCESSORS = getRuntime(); 
    public static final String CONST_NAME = "Casanova";

    public static int getRuntime() {
        return Runtime.getRuntime().availableProcessors() * 3; 
    }
    
    // Foreground Colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\033[0;91m";
    public static final String GREEN = "\033[0;92m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Reset Foreground Color
    public static final String RESET = "\u001B[0m";

    // Bold Foreground Colors
    public static final String YELLOW_BOLD = "\033[1;93m";
    public static final String WHITE_BOLD = "\033[1;97m";
    public static final String PURPLE_BOLD = "\033[1;95m";

    // Background Colors
    public static final String BLACK_BG = "\033[40m";
}
