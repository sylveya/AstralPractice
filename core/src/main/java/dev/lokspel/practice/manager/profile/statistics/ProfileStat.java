package dev.lokspel.practice.manager.profile.statistics;

import dev.lokspel.api.event.EloChangeEvent;
import dev.lokspel.api.interfaces.Match;
import dev.lokspel.practice.manager.division.Division;
import dev.lokspel.practice.manager.division.DivisionManager;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileData;
import dev.lokspel.practice.manager.profile.ProfileFile;
import dev.lokspel.practice.util.NumberUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ProfileStat {

  private final Profile profile;
  private final ProfileFile profileFile;

  @Getter private final Map<NormalLadder, LadderStats> ladderStats = new HashMap<>();
  @Getter private final FfaStats ffaStats = new FfaStats();

  @Getter @Setter private int winStreak = 0;
  @Getter @Setter private int bestWinStreak = 0;

  @Getter @Setter private int loseStreak = 0;
  @Getter @Setter private int bestLoseStreak = 0;

  @Getter @Setter private Division division;
  @Getter @Setter private int experience = 0;

  public ProfileStat(Profile profile) {
    this.profile = profile;
    this.profileFile = profile.getFile();
  }

  public LadderStats getLadderStat(NormalLadder ladder) {
    this.createLadderStat(ladder);
    return ladderStats.get(ladder);
  }

  public void createLadderStat(NormalLadder ladder) {
    if (!ladderStats.containsKey(ladder)) {
      ladderStats.put(ladder, new LadderStats());
    }
  }

  public void increaseWinStreak(NormalLadder ladder, boolean ranked) {
    LadderStats ladderStat = getLadderStat(ladder);
    ladderStat.increaseWinStreak(ranked);

    this.winStreak++;
    this.loseStreak = 0;

    if (this.winStreak > this.bestWinStreak) {
      this.bestWinStreak = this.winStreak;
    }
  }

  public void increaseLoseStreak(NormalLadder ladder, boolean ranked) {
    LadderStats ladderStat = getLadderStat(ladder);
    ladderStat.increaseLoseStreak(ranked);

    this.loseStreak++;
    this.winStreak = 0;

    if (this.loseStreak > this.bestLoseStreak) {
      this.bestLoseStreak = this.loseStreak;
    }
  }

  public void setElo(NormalLadder ladder, int newElo) {
    LadderStats ladderStat = getLadderStat(ladder);
    int oldElo = ladderStat.getElo();
    if (oldElo == newElo) {
      return;
    }

    ladderStat.setElo(newElo);
    callEloChangeEvent(ladder.getName(), oldElo, newElo, null);
  }

  public void changeElo(NormalLadder ladder, int delta, Match match) {
    LadderStats ladderStat = getLadderStat(ladder);
    int oldElo = ladderStat.getElo();

    if (delta > 0) {
      ladderStat.increaseElo(delta);
    } else if (delta < 0) {
      ladderStat.decreaseElo(-delta);
    }

    int newElo = ladderStat.getElo();
    if (oldElo == newElo) {
      return;
    }

    callEloChangeEvent(ladder.getName(), oldElo, newElo, match);
  }

  private void callEloChangeEvent(String ladderName, int oldElo, int newElo, Match match) {
    Player online = profile.getPlayer() != null ? profile.getPlayer().getPlayer() : null;
    if (online != null) {
      Bukkit.getPluginManager()
          .callEvent(new EloChangeEvent(online, ladderName, oldElo, newElo, match));
    }
  }

  /**
   * Writes the statistics into the profile file's model; values of 0 are left out of the file.
   *
   * @param save whether to write the file afterwards
   */
  public void setData(boolean save) {
    ProfileData data = profileFile.getProfileData();
    data.setExperience(nonZero(experience));
    data.setWinStreak(nonZero(winStreak));
    data.setBestWinStreak(nonZero(bestWinStreak));
    data.setLoseStreak(nonZero(loseStreak));
    data.setBestLoseStreak(nonZero(bestLoseStreak));

    if (data.getStats() == null) {
      data.setStats(new ProfileData.Stats());
    }
    if (data.getStats().getLadderStats() == null) {
      data.getStats().setLadderStats(new LinkedHashMap<>());
    }
    Map<String, LadderStatsData> stored = data.getStats().getLadderStats();
    for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
      LadderStatsData ladderData =
          stored.computeIfAbsent(ladder.getName().toLowerCase(), name -> new LadderStatsData());
      getLadderStat(ladder).setData(ladderData, ladder.isRanked());
    }

    if (data.getStats().getFfaStats() == null) {
      data.getStats().setFfaStats(new FfaStatsData());
    }
    ffaStats.setData(data.getStats().getFfaStats());

    if (save) {
      profileFile.saveFile();
    }
  }

  private static Integer nonZero(int value) {
    return value != 0 ? value : null;
  }

  /** Reads the statistics from the profile file's model. */
  public void getData() {
    ProfileData data = profileFile.getProfileData();
    setExperience(valueOf(data.getExperience()));
    setWinStreak(valueOf(data.getWinStreak()));
    setBestWinStreak(valueOf(data.getBestWinStreak()));
    setLoseStreak(valueOf(data.getLoseStreak()));
    setBestLoseStreak(valueOf(data.getBestLoseStreak()));

    Map<String, LadderStatsData> stored =
        data.getStats() != null ? data.getStats().getLadderStats() : null;
    for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
      LadderStats ladderStat = new LadderStats();
      ladderStat.getData(
          stored != null ? stored.get(ladder.getName().toLowerCase()) : null, ladder.isRanked());
      ladderStats.put(ladder, ladderStat);
    }

    FfaStatsData ffaData = data.getStats() != null ? data.getStats().getFfaStats() : null;
    ffaStats.getData(ffaData);
  }

  private static int valueOf(Integer value) {
    return value != null ? value : 0;
  }

  public void loadDefaultStats(NormalLadder ladder) {
    LadderStats ladderStat = getLadderStat(ladder);
    ladderStat.reset();

    if (ladder.isRanked()) {
      this.setDivision(DivisionManager.getInstance().getDivision(profile));
    }
  }

  public double getLadderRatio(NormalLadder ladder, boolean ranked) {
    LadderStats ladderStat = getLadderStat(ladder);

    if ((ranked && !ladder.isRanked()) || (!ranked && !ladder.isUnranked())) {
      return 0;
    }

    int w = ranked ? ladderStat.getRankedWins() : ladderStat.getUnRankedWins();
    int l = ranked ? ladderStat.getRankedLosses() : ladderStat.getUnRankedLosses();

    if (l == 0) {
      return w;
    }
    return NumberUtil.roundDouble((double) w / l);
  }

  public double getOverallRatio(NormalLadder ladder) {
    LadderStats ladderStat = getLadderStat(ladder);

    int w = 0;
    if (ladder.isUnranked()) {
      w += ladderStat.getUnRankedWins();
    }
    if (ladder.isRanked()) {
      w += ladderStat.getRankedWins();
    }

    int l = 0;
    if (ladder.isUnranked()) {
      l += ladderStat.getUnRankedLosses();
    }
    if (ladder.isRanked()) {
      l += ladderStat.getRankedLosses();
    }

    if (l == 0) {
      return w;
    }
    return NumberUtil.roundDouble((double) w / l);
  }

  public int getGlobalElo() {
    int returnElo = 0;
    int count = 0;

    for (NormalLadder ladder : ladderStats.keySet()) {
      LadderStats ladderStat = getLadderStat(ladder);

      if (ladder.isEnabled() && ladder.isRanked()) {
        returnElo += ladderStat.getElo();
        count++;
      }
    }

    if (count == 0) {
      return 0;
    }
    return (returnElo / count);
  }

  public int getWins(boolean ranked) {
    int wins = 0;
    if (ranked) {
      for (NormalLadder ladder : ladderStats.keySet()) {
        LadderStats ladderStat = getLadderStat(ladder);

        if (ladder.isEnabled() && ladder.isRanked()) {
          wins += ladderStat.getRankedWins();
        }
      }
    } else {
      for (NormalLadder ladder : ladderStats.keySet()) {
        LadderStats ladderStat = getLadderStat(ladder);

        if (ladder.isEnabled()) {
          wins += ladderStat.getUnRankedWins();
        }
      }
    }
    return wins;
  }

  public int getLosses(boolean ranked) {
    int losses = 0;
    if (ranked) {
      for (NormalLadder ladder : ladderStats.keySet()) {
        LadderStats ladderStat = getLadderStat(ladder);

        if (ladder.isEnabled() && ladder.isRanked()) {
          losses += ladderStat.getRankedLosses();
        }
      }
    } else {
      for (NormalLadder ladder : ladderStats.keySet()) {
        LadderStats ladderStat = getLadderStat(ladder);

        if (ladder.isEnabled()) {
          losses += ladderStat.getUnRankedLosses();
        }
      }
    }
    return losses;
  }

  public double getRatio(boolean ranked) {
    int w = getWins(ranked);
    int l = getLosses(ranked);

    if (l == 0) {
      return w;
    }
    return NumberUtil.roundDouble((double) w / l);
  }

  public int getGlobalWins() {
    return getWins(false) + getWins(true);
  }

  public int getGlobalLosses() {
    return getLosses(false) + getLosses(true);
  }

  public double getGlobalRatio() {
    int w = getGlobalWins();
    int l = getGlobalLosses();

    if (l == 0) {
      return w;
    }
    return NumberUtil.roundDouble((double) w / l);
  }

  public double getWinrate(boolean ranked) {
    int total = getWins(ranked) + getLosses(ranked);
    if (total == 0) {
      return 0;
    }
    return NumberUtil.roundDouble((double) getWins(ranked) * 100 / total);
  }

  public double getGlobalWinrate() {
    int total = getGlobalWins() + getGlobalLosses();
    if (total == 0) {
      return 0;
    }
    return NumberUtil.roundDouble((double) getGlobalWins() * 100 / total);
  }

  public int getKills() {
    int kills = 0;
    for (NormalLadder ladder : ladderStats.keySet()) {
      LadderStats ladderStat = getLadderStat(ladder);

      if (ladder.isEnabled()) {
        kills += ladderStat.getKills();
      }
    }
    return kills;
  }

  public int getDeaths() {
    int deaths = 0;
    for (NormalLadder ladder : ladderStats.keySet()) {
      LadderStats ladderStat = getLadderStat(ladder);

      if (ladder.isEnabled()) {
        deaths += ladderStat.getDeaths();
      }
    }
    return deaths;
  }

  public double getGlobalKdr() {
    int k = getKills();
    int d = getDeaths();

    if (d == 0) {
      return k;
    }
    return NumberUtil.roundDouble((double) k / d);
  }
}
