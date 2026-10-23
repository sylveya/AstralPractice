package dev.lokspel.practice.manager.division;

import dev.lokspel.api.event.match.MatchEndEvent;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.division.DivisionConfig;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.statistics.LadderStats;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Getter
public class DivisionManager implements Listener {

  private final boolean countByWins;
  private final boolean countByElo;

  private static DivisionManager instance;

  public static DivisionManager getInstance() {
    if (instance == null) {
      instance = new DivisionManager();
    }
    return instance;
  }

  private final boolean divisionsEnabled = PracticeConfig.settings().getDivisions().isEnabled();
  private final boolean newDivisionMessageEnabled =
      PracticeConfig.settings().getDivisions().isNewDivisionMessage();
  private Division minimumForRanked;

  private final List<Division> divisions = new ArrayList<>();

  private DivisionManager() {
    boolean countByWins = PracticeConfig.divisions().getRequire().isWins();
    boolean countByElo = PracticeConfig.divisions().getRequire().isElo();
    if (!countByWins && !countByElo) {
      countByWins = true;
    }
    this.countByWins = countByWins;
    this.countByElo = countByElo;

    Bukkit.getPluginManager().registerEvents(this, AstralPractice.getInstance());
  }

  public void reloadRanks() {
    divisions.clear();
    this.getData();

    this.setDivisions();
  }

  public void setDivisions() {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            AstralPractice.getInstance(),
            () -> {
              for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                profile.getStats().setDivision(this.getDivision(profile));
              }
            });
  }

  public Division getDivision(final Profile profile) {
    if (profile == null) {
      return null;
    }
    return calculateDivision(
        profile.getStats().getExperience(),
        profile.getStats().getGlobalWins(),
        profile.getStats().getGlobalElo());
  }

  public Division getLadderDivision(final Profile profile, final NormalLadder ladder) {
    if (profile == null || ladder == null) {
      return null;
    }
    LadderStats stats = profile.getStats().getLadderStat(ladder);
    if (stats == null) {
      return null;
    }
    int wins = stats.getRankedWins() + stats.getUnRankedWins();
    int elo = stats.getElo();
    return calculateDivision(profile.getStats().getExperience(), wins, elo);
  }

  public Division getBestDivision(final Profile profile) {
    if (profile == null) {
      return null;
    }
    Division best = null;
    int bestWeight = -1;
    for (NormalLadder ladder : profile.getStats().getLadderStats().keySet()) {
      Division division = getLadderDivision(profile, ladder);
      int weight = getDivisionWeight(division);
      if (weight > bestWeight) {
        bestWeight = weight;
        best = division;
      }
    }
    return best != null ? best : getDivision(profile);
  }

  private Division calculateDivision(final int experience, final int wins, final int elo) {
    for (int i = 0; i < this.divisions.size(); i++) {
      Division division = this.divisions.get(i);
      Division nextDivision;
      try {
        nextDivision = this.divisions.get(i + 1);
      } catch (IndexOutOfBoundsException e) {
        nextDivision = null;
      }

      boolean meetsExperience = division.getExperience() <= experience;
      boolean meetsWins = !countByWins || division.getWin() <= wins;
      boolean meetsElo = !countByElo || division.getElo() <= elo;

      if (meetsExperience && meetsWins && meetsElo) {
        if (nextDivision == null) {
          return division;
        } else {
          boolean nextExperience = nextDivision.getExperience() > experience;
          boolean nextWins = countByWins && nextDivision.getWin() > wins;
          boolean nextElo = countByElo && nextDivision.getElo() > elo;

          if (nextExperience || nextWins || nextElo) {
            return division;
          }
        }
      }
    }

    return null;
  }

  public Division getNextDivision(final Profile profile) {
    return profile != null ? getNextDivision(profile.getStats().getDivision()) : null;
  }

  public Division getNextDivision(final Division division) {
    if (division == null) {
      return null;
    }

    int index = this.divisions.indexOf(division);
    if (index == -1 || index + 1 >= this.divisions.size()) {
      return null;
    }
    return this.divisions.get(index + 1);
  }

  public Division getPreviousDivision(final Division division) {
    if (division == null) {
      return null;
    }

    int index = this.divisions.indexOf(division);
    if (index <= 0) {
      return null;
    }
    return this.divisions.get(index - 1);
  }

  public int getDivisionWeight(final Division division) {
    if (division == null) {
      return 0;
    }

    if (division.getWeight() != -1) {
      return division.getWeight();
    }

    int index = this.divisions.indexOf(division);
    if (index == -1) {
      return 0;
    }

    return index + 1;
  }

  public boolean meetsMinimumForRanked(final Profile profile) {
    if (profile == null || this.minimumForRanked == null) {
      return true;
    }

    // Check against the minimum division's REQUIREMENTS, NOT ranked ELO
    // (ranked elo is only achievable by playing ranked, which is locked behind this check)
    int experience = profile.getStats().getExperience();
    int wins = profile.getStats().getGlobalWins();

    // Check experience requirement
    if (experience < this.minimumForRanked.getExperience()) {
      return false;
    }

    // Check wins requirement (only if wins are counted)
    return !this.countByWins || wins >= this.minimumForRanked.getWin();

    // NOTE: We deliberately do NOT check ELO here, because:
    // - Global ELO only comes from ranked matches
    // - Ranked is locked behind this check
    // - This would create an infinite loop
    // Players can unlock ranked with experience/wins, then earn ranked elo
  }

  public void sendRankedProgressMessage(
      final Player player, final Profile profile, final Division requirement) {
    int currentExp = profile.getStats().getExperience();
    int requiredExp = requirement.getExperience();
    final int currentWins = profile.getStats().getGlobalWins();
    final int requiredWins = requirement.getWin();

    Common.sendMmMessage(player, "");
    Common.sendMmMessage(
        player, PracticeConfig.language().getCommand().getQueues().getRanked().getProgressHeader());

    // Experience progress
    int expProgress = Math.min(100, (int) ((double) currentExp / requiredExp * 100));
    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getQueues()
            .getRanked()
            .getProgressExp()
            .replace("%current%", String.valueOf(currentExp))
            .replace("%required%", String.valueOf(requiredExp))
            .replace("%percent%", String.valueOf(expProgress)));

    // Wins progress (if wins are counted)
    if (this.countByWins) {
      int winsProgress = Math.min(100, (int) ((double) currentWins / requiredWins * 100));
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getQueues()
              .getRanked()
              .getProgressWins()
              .replace("%current%", String.valueOf(currentWins))
              .replace("%required%", String.valueOf(requiredWins))
              .replace("%percent%", String.valueOf(winsProgress)));
    }

    Common.sendMmMessage(player, "");
  }

  /** Builds the divisions from {@link PracticeConfig#divisions()}. */
  public void getData() {
    int count = 0;
    for (Map.Entry<String, DivisionConfig.Division> entry :
        PracticeConfig.divisions().getDivisions().entrySet()) {
      if (count == 28) {
        break;
      }

      Division division = new Division(entry.getKey(), entry.getValue());
      if (!division.isValid()) {
        continue;
      }

      divisions.add(division);

      count++;
    }

    divisions.sort(new DivisionComparator());
    this.minimumForRanked =
        getDivisionByConfigName(PracticeConfig.settings().getDivisions().getMinDivisionForRanked());
  }

  private Division getDivisionByConfigName(final String divisionConfigName) {
    for (Division division : this.divisions) {
      if (division.getConfigName().equalsIgnoreCase(divisionConfigName)) {
        return division;
      }
    }
    return null;
  }

  @EventHandler
  public void onMatchEnd(MatchEndEvent e) {
    Match match = (Match) e.getMatch();
    if (!(match.getLadder() instanceof NormalLadder)) {
      return;
    }

    int expReceived = match.getDuration();

    for (Player player : match.getPlayers()) {
      Profile profile = ProfileManager.getInstance().getProfile(player);
      profile.getStats().setExperience(profile.getStats().getExperience() + expReceived);

      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getMatch()
              .getExpReceived()
              .replace("%exp%", String.valueOf(expReceived)));

      Division oldDivision = profile.getStats().getDivision();
      Division newDivision = this.getDivision(profile);

      if (oldDivision != newDivision) {
        profile.getStats().setDivision(newDivision);

        if (this.newDivisionMessageEnabled) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language()
                  .getMatch()
                  .getReachedNewDivision()
                  .replace("%newDivision_fullName%", newDivision.getFullName())
                  .replace("%newDivision_shortName%", newDivision.getShortName())
                  .replace("%oldDivision_fullName%", oldDivision.getFullName())
                  .replace("%oldDivision_shortName%", oldDivision.getShortName()));

          if (newDivision == this.minimumForRanked) {
            Common.sendMmMessage(
                player,
                PracticeConfig.language()
                    .getMatch()
                    .getDuel()
                    .getRankedRequirementReached()
                    .replace("%newDivision_fullName%", newDivision.getFullName())
                    .replace("%newDivision_shortName%", newDivision.getShortName()));
          }
        }
      }
    }
  }
}
