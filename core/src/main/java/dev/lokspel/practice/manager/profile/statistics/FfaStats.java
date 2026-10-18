package dev.lokspel.practice.manager.profile.statistics;

import dev.lokspel.practice.util.NumberUtil;
import lombok.Getter;
import lombok.Setter;

/** Runtime in-memory statistics for Free-For-All (FFA). */
@Getter
@Setter
public class FfaStats {

  private int kills;
  private int deaths;
  private int bestKillStreak;

  public void increaseKills() {
    kills++;
  }

  public void increaseDeaths() {
    deaths++;
  }

  public void updateBestKillStreak(int streak) {
    if (streak > bestKillStreak) {
      bestKillStreak = streak;
    }
  }

  public double getKdr() {
    if (deaths == 0) {
      return kills;
    }
    return NumberUtil.roundDouble((double) kills / deaths);
  }

  public void setData(FfaStatsData data) {
    data.setKills(kills != 0 ? kills : null);
    data.setDeaths(deaths != 0 ? deaths : null);
    data.setBestKillStreak(bestKillStreak != 0 ? bestKillStreak : null);
  }

  public void getData(FfaStatsData data) {
    if (data != null) {
      kills = valueOf(data.getKills());
      deaths = valueOf(data.getDeaths());
      bestKillStreak = valueOf(data.getBestKillStreak());
    } else {
      kills = 0;
      deaths = 0;
      bestKillStreak = 0;
    }
  }

  private static int valueOf(Integer value) {
    return value != null ? value : 0;
  }
}
