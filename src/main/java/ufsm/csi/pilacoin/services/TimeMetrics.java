package ufsm.csi.pilacoin.services;

import static ufsm.csi.pilacoin.config.Config.PURPLE;
import static ufsm.csi.pilacoin.config.Config.RESET;
import static ufsm.csi.pilacoin.config.Config.WHITE_BOLD;

import ufsm.csi.pilacoin.shared.TimeFormat;

public class TimeMetrics {

  private Long start;
  private Long finish;
  private Long totalPeriod;
  private Long periodHours;
  private Long periodMinutes;
  private Long periodSeconds;

  public TimeMetrics(Long timerStart) {

    this.start = timerStart;
    this.finish = System.currentTimeMillis();
    this.totalPeriod = (this.finish - this.start);
    this.periodSeconds = (this.totalPeriod / 1000) % 60;
    this.periodMinutes = (this.totalPeriod / (60 * 1000)) % 60;
    this.periodHours = this.totalPeriod / (60 * 60 * 1000);

    this.getMetrics(); // Shutdown by printing out in terminal
  }

  private void getMetrics() {
    System.out.print(PURPLE + "\nMining Time: " + RESET);
    System.out.print(WHITE_BOLD);
    System.out.print(TimeFormat.timeFormat(this.periodHours, this.periodMinutes, this.periodSeconds));
    System.out.print(RESET);
  }
}
