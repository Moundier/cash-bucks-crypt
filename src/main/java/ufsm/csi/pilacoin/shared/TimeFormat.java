package ufsm.csi.pilacoin.shared;

import ufsm.csi.pilacoin.common.Colors;

import static ufsm.csi.pilacoin.common.Colors.*;

public class TimeFormat {

    public static String reduplicator(String surround, String message) {
        String surrounds = surround.repeat(message.length());
        return surrounds + "\n" + message + "\n" + surrounds;
    }

    public static String blockFoundMessage(int count, String json) {
        return String.format(
            BLACK_BACKGROUND + "Block found in " +
            WHITE_BOLD_BRIGHT + "%,d" + " tries" +
            ANSI_RESET + "\n", count) + json;
    }

    public static String threadName(Thread thread) {
        return Colors.YELLOW_BOLD_BRIGHT + thread.getName() + Colors.ANSI_RESET  + ": ";
    }

    public static String timeFormat(Long hours, Long minutes, Long seconds) {
        StringBuilder message = new StringBuilder();
    
        appendTimeUnit(message, hours, "hour", "hours");
        appendTimeUnit(message, minutes, "minute", "minutes");
        appendTimeUnit(message, seconds, "second", "seconds");
    
        return message.length() == 0 ? "0 seconds" : message.toString();
    }
    
    private static void appendTimeUnit(StringBuilder message, Long value, String singularUnit, String pluralUnit) {
        if (value != null && value > 0) {
            if (message.length() > 0) {
                message.append(" and ");
            }
            message.append(value).append(" ").append(value == 1 ? singularUnit : pluralUnit);
        }
    }
}