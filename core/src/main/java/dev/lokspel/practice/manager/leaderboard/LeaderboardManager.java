package dev.lokspel.practice.manager.leaderboard;

import dev.lokspel.api.event.FfaRemovePlayerEvent;
import dev.lokspel.api.event.match.MatchEndEvent;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.type.duel.Duel;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.leaderboard.hologram.HologramManager;
import dev.lokspel.practice.manager.leaderboard.types.LbMainType;
import dev.lokspel.practice.manager.leaderboard.types.LbSecondaryType;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.statistics.LadderStats;
import dev.lokspel.practice.util.StartUpCallback;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Getter
public class LeaderboardManager implements Listener {

  private static LeaderboardManager instance;

  public static LeaderboardManager getInstance() {
    if (instance == null) {
      instance = new LeaderboardManager();
    }
    return instance;
  }

  private final List<Leaderboard> leaderboards = new ArrayList<>();

  private LeaderboardManager() {
    Bukkit.getPluginManager().registerEvents(this, AstralPractice.getInstance());
  }

  public Leaderboard searchLb(
      final LbMainType mainType, final LbSecondaryType secondaryType, final Ladder ladder) {
    for (Leaderboard lb : new ArrayList<>(leaderboards)) {
      if (lb.getMainType().equals(mainType) && lb.getSecondaryType().equals(secondaryType)) {
        if (ladder == null && lb.getLadder() == null) {
          return lb;
        } else if (ladder != null && lb.getLadder() != null && ladder == lb.getLadder()) {
          return lb;
        }
      }
    }
    return null;
  }

  public void createAllLb(final StartUpCallback startUpCallback) {
    for (LbMainType lbMainType : LbMainType.values()) {
      for (LbSecondaryType lbSecondaryType : LbSecondaryType.values()) {
        if (lbMainType.equals(LbMainType.LADDER)) {
          for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            if (ladder.isEnabled()) {
              updateLb(lbMainType, lbSecondaryType, ladder);
            }
          }
        } else if (lbMainType.equals(LbMainType.FFA_GLOBAL)) {
          if (lbSecondaryType == LbSecondaryType.KILLS
              || lbSecondaryType == LbSecondaryType.DEATHS
              || lbSecondaryType == LbSecondaryType.BEST_WIN_STREAK) {
            updateLb(lbMainType, lbSecondaryType, null);
          }
        } else {
          updateLb(lbMainType, lbSecondaryType, null);
        }
      }
    }

    Bukkit.getScheduler().runTask(AstralPractice.getInstance(), startUpCallback::onLoadingDone);
  }

  public void removeLadder(NormalLadder ladder) {
    HologramManager.getInstance().removeLadder(ladder);
    leaderboards.removeIf(
        leaderboard -> leaderboard.getLadder() != null && leaderboard.getLadder() == ladder);
  }

  public interface LeaderboardCallback {
    void onLeaderboardBuildDone(Map<OfflinePlayer, Integer> list);
  }

  public void updateLb(
      final LbMainType mainType, final LbSecondaryType secondaryType, final NormalLadder ladder) {
    if (!AstralPractice.getInstance().isEnabled()) {
      return;
    }

    createLb(
        mainType,
        secondaryType,
        ladder,
        list -> {
          if (list == null) {
            return;
          }

          Leaderboard leaderboard = searchLb(mainType, secondaryType, ladder);
          if (leaderboard != null) {
            leaderboard.setList(list);
          } else {
            leaderboard = new Leaderboard(mainType, secondaryType, ladder, list);
            leaderboards.add(leaderboard);
          }

          // NOTE: We do NOT trigger immediate hologram updates here anymore.
          // The HologramRunnable handles periodic updates on its own timer.
          // Triggering updates here was causing:
          // 1. Race conditions with multiple leaderboard updates happening simultaneously
          // 2. Excessive calls to getNextLeaderboard() which was rotating dynamic hologram ladders
          // 3. Holograms flickering/disappearing due to overlapping update cycles
          //
          // Holograms will show updated data on their next scheduled update cycle,
          // which is typically within a few seconds (configurable).
        });
  }

  public void createLb(
      final LbMainType mainType,
      final LbSecondaryType secondaryType,
      final NormalLadder ladder,
      final LeaderboardCallback callback) {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            AstralPractice.getInstance(),
            () -> {
              ToIntFunction<Profile> extractor = getStatExtractor(mainType, secondaryType, ladder);
              if (extractor == null) {
                Bukkit.getScheduler()
                    .runTask(
                        AstralPractice.getInstance(), () -> callback.onLeaderboardBuildDone(null));
                return;
              }

              HashMap<OfflinePlayer, Integer> unsorted = new HashMap<>();
              for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                unsorted.put(profile.getPlayer(), extractor.applyAsInt(profile));
              }

              Bukkit.getScheduler()
                  .runTask(
                      AstralPractice.getInstance(),
                      () -> callback.onLeaderboardBuildDone(sortByValue(unsorted)));
            });
  }

  private ToIntFunction<Profile> getStatExtractor(
      LbMainType mainType, LbSecondaryType secondaryType, NormalLadder ladder) {
    if (mainType == LbMainType.GLOBAL) {
      return switch (secondaryType) {
        case ELO -> profile -> profile.getStats().getGlobalElo();
        case WIN -> profile -> profile.getStats().getGlobalWins();
        case KILLS -> profile -> profile.getStats().getKills();
        case DEATHS -> profile -> profile.getStats().getDeaths();
        case WIN_STREAK -> profile -> profile.getStats().getWinStreak();
        case LOSE_STREAK -> profile -> profile.getStats().getLoseStreak();
        case BEST_WIN_STREAK -> profile -> profile.getStats().getBestWinStreak();
        case BEST_LOSE_STREAK -> profile -> profile.getStats().getBestLoseStreak();
      };
    }

    if (mainType == LbMainType.LADDER && ladder != null) {
      return switch (secondaryType) {
        case ELO ->
            ladder.isRanked() ? profile -> profile.getStats().getLadderStat(ladder).getElo() : null;
        case WIN ->
            profile -> {
              LadderStats stat = profile.getStats().getLadderStat(ladder);
              return stat.getUnRankedWins() + stat.getRankedWins();
            };
        case KILLS -> profile -> profile.getStats().getLadderStat(ladder).getKills();
        case DEATHS -> profile -> profile.getStats().getLadderStat(ladder).getDeaths();
        case WIN_STREAK ->
            profile -> {
              LadderStats stat = profile.getStats().getLadderStat(ladder);
              return stat.getUnRankedWinStreak() + stat.getRankedWinStreak();
            };
        case LOSE_STREAK ->
            profile -> {
              LadderStats stat = profile.getStats().getLadderStat(ladder);
              return stat.getUnRankedLoseStreak() + stat.getRankedLoseStreak();
            };
        case BEST_WIN_STREAK ->
            profile -> {
              LadderStats stat = profile.getStats().getLadderStat(ladder);
              return stat.getUnRankedBestWinStreak() + stat.getRankedBestWinStreak();
            };
        case BEST_LOSE_STREAK ->
            profile -> {
              LadderStats stat = profile.getStats().getLadderStat(ladder);
              return stat.getUnRankedBestLoseStreak() + stat.getRankedBestLoseStreak();
            };
      };
    }

    if (mainType == LbMainType.FFA_GLOBAL) {
      return switch (secondaryType) {
        case KILLS -> profile -> profile.getStats().getFfaStats().getKills();
        case DEATHS -> profile -> profile.getStats().getFfaStats().getDeaths();
        case BEST_WIN_STREAK -> profile -> profile.getStats().getFfaStats().getBestKillStreak();
        default -> null;
      };
    }

    return null;
  }

  /**
   * It takes a HashMap of OfflinePlayers and Integers, sorts it by the Integer value, and returns a
   * LinkedHashMap of OfflinePlayers and Integers.
   *
   * @param map The HashMap you want to sort.
   * @return A LinkedHashMap with the keys sorted by value in descending order.
   */
  public static Map<OfflinePlayer, Integer> sortByValue(Map<OfflinePlayer, Integer> map) {
    if (map.isEmpty()) {
      return map;
    }

    LinkedHashMap<OfflinePlayer, Integer> reverseSortedMap = new LinkedHashMap<>();

    map.entrySet().stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));

    return reverseSortedMap;
  }

  @EventHandler
  public void onMatchEnd(MatchEndEvent e) {
    Match match = (Match) e.getMatch();

    if (match instanceof Duel duel && match.getLadder() instanceof NormalLadder ladder) {
      for (LbMainType lbMainType : LbMainType.values()) {
        for (LbSecondaryType lbSecondaryType : LbSecondaryType.values()) {
          if (!duel.isRanked() && lbSecondaryType.isRankedRelated()) {
            continue;
          }

          switch (lbMainType) {
            case GLOBAL:
              updateLb(lbMainType, lbSecondaryType, null);
              break;
            case LADDER:
              updateLb(lbMainType, lbSecondaryType, ladder);
              break;

            default:
              break;
          }
        }
      }
    }

    if (AstralPractice.getInstance().isEnabled()) {
      Bukkit.getScheduler()
          .runTaskLaterAsynchronously(
              AstralPractice.getInstance(),
              () -> {
                GuiManager.getInstance().searchGui(GuiType.Queue_Unranked).update();
                GuiManager.getInstance().searchGui(GuiType.Queue_Ranked).update();
              },
              20L);
    }
  }

  @EventHandler
  public void onFfaRemovePlayer(FfaRemovePlayerEvent e) {
    updateLb(LbMainType.GLOBAL, LbSecondaryType.KILLS, null);
    updateLb(LbMainType.GLOBAL, LbSecondaryType.DEATHS, null);

    updateLb(LbMainType.FFA_GLOBAL, LbSecondaryType.KILLS, null);
    updateLb(LbMainType.FFA_GLOBAL, LbSecondaryType.DEATHS, null);
    updateLb(LbMainType.FFA_GLOBAL, LbSecondaryType.BEST_WIN_STREAK, null);

    // It's important to call this before the player gets removed from the FFA
    final Ffa ffa = (Ffa) e.getFfa();
    updateLb(LbMainType.LADDER, LbSecondaryType.KILLS, ffa.getPlayers().get(e.getPlayer()));
    updateLb(LbMainType.LADDER, LbSecondaryType.DEATHS, ffa.getPlayers().get(e.getPlayer()));
  }
}
