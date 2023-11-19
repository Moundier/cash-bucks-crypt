package ufsm.csi.pilacoin.services;

import ufsm.csi.pilacoin.common.Colors;
import ufsm.csi.pilacoin.shared.TimeFormat;

public class TimeMetrics {

  private Long miningStartTime;
  private Long miningEndTime;
  private Long miningPeriod;
  private Long miningPeriodHours;
  private Long miningPeriodMinutes;
  private Long miningPeriodSeconds;

  public TimeMetrics(Long timerStart) {

    this.miningStartTime = timerStart;
    this.miningEndTime = System.currentTimeMillis();
    this.miningPeriod = this.miningEndTime - this.miningStartTime;
    this.miningPeriodSeconds = (this.miningPeriod / 1000) % 60;
    this.miningPeriodMinutes = (this.miningPeriod / (60 * 1000)) % 60;
    this.miningPeriodHours = this.miningPeriod / (60 * 60 * 1000);

    this.getMetrics(); // Shutdown by printing out in terminal
  }

  private void getMetrics() {
    System.out.println(
        Colors.ANSI_PURPLE + "\nMining Time: " + Colors.ANSI_RESET
            + Colors.WHITE_BOLD_BRIGHT
            + TimeFormat.timeFormat(this.miningPeriodHours, this.miningPeriodMinutes, this.miningPeriodSeconds)
            + Colors.ANSI_RESET);
  }

}
