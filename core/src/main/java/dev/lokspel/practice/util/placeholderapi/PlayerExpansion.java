package dev.lokspel.practice.util.placeholderapi;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.division.DivisionManager;
import dev.lokspel.practice.manager.fight.ffa.FfaManager;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.enums.TeamEnum;
import dev.lokspel.practice.manager.fight.match.interfaces.Team;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.fight.util.stats.Statistic;
import dev.lokspel.practice.util.LobbyNametagUtil;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.leaderboard.Leaderboard;
import dev.lokspel.practice.manager.leaderboard.LeaderboardManager;
import dev.lokspel.practice.manager.leaderboard.types.LbMainType;
import dev.lokspel.practice.manager.leaderboard.types.LbSecondaryType;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.group.Group;
import dev.lokspel.practice.manager.profile.statistics.FfaStats;
import dev.lokspel.practice.manager.queue.QueueManager;
import java.util.ArrayList;
import java.util.Map;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerExpansion extends PlaceholderExpansion implements Relational {

  private final String identifier;

  public PlayerExpansion(String identifier) {
    this.identifier = identifier;
  }

  @Override
  public @NotNull String getIdentifier() {
    return identifier;
  }

  @Override
  public @NotNull String getAuthor() {
    return "lokspel";
  }

  @Override
  public @NotNull String getVersion() {
    return "1.0.1";
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public String onRequest(OfflinePlayer player, @NotNull String params) {
    if ("ping".equalsIgnoreCase(params)) {
      Player onlinePlayer = player.getPlayer();
      return onlinePlayer != null ? String.valueOf(PlayerUtil.getPing(onlinePlayer)) : "N/A";
    }

    if ("nametag_color".equalsIgnoreCase(params)) {
      return resolveNametagColor(player);
    }

    final Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return null;
    }

    String[] input = params.split("_");
    if (input.length == 0) {
      return null;
    }

    switch (input[0]) {
      case "group":
        if (input.length == 1) {
          return null;
        }

        Group group = profile.getGroup();
        if (group == null) {
          return null;
        }

        switch (input[1]) {
          case "name":
            if (input.length == 2) {
              return group.getDisplayName();
            }
            break;
          case "prefix":
            if (input.length == 2) {
              return AstralPractice.getMiniMessage().serialize(group.getPrefix());
            }
            break;
          case "suffix":
            if (input.length == 2) {
              return AstralPractice.getMiniMessage().serialize(group.getSuffix());
            }
            break;
          case "limit":
            if (input.length == 2) {
              return null;
            }

            switch (input[2]) {
              case "u":
                return String.valueOf(group.getUnrankedLimit());
              case "r":
                return String.valueOf(group.getRankedLimit());

              default:
                break;
            }
            break;

          default:
            break;
        }
        break;
      case "in":
        if (input.length == 1) {
          return null;
        }

        switch (input[1]) {
          case "queue":
            if (input.length == 2) {
              return String.valueOf(QueueManager.getInstance().getQueues().size());
            } else {
              if (input.length != 3) {
                return null;
              }

              NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
              if (ladder == null) {
                return null;
              }

              return String.valueOf(QueueManager.getInstance().getQueueSize(ladder));
            }
          case "fight":
            if (input.length == 2) {
              return String.valueOf(MatchManager.getInstance().getPlayerInMatchSize());
            } else {
              if (input.length != 3) {
                return null;
              }

              NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
              if (ladder == null) {
                return null;
              }

              return String.valueOf(MatchManager.getInstance().getPlayerInMatchSize(ladder));
            }

          default:
            break;
        }
        break;
      case "division":
        if (input.length == 1) {
          return null;
        }
        if (profile.getStats().getDivision() == null) {
          return null;
        }

        switch (input[1]) {
          // division_weight
          case "weight":
            return String.valueOf(
                DivisionManager.getInstance().getDivisionWeight(profile.getStats().getDivision()));
          // division_short
          case "short":
            return profile.getStats().getDivision().getShortName();
          // division_full
          case "full":
            return profile.getStats().getDivision().getFullName();

          default:
            break;
        }
        break;
      case "kills":
        if (input.length != 2) {
          break;
        }

        // kills_global
        if (input[1].equals("global")) {
          return String.valueOf(profile.getStats().getKills());
        }
        break;
      case "deaths":
        if (input.length != 2) {
          break;
        }

        // deaths_global
        if (input[1].equals("global")) {
          return String.valueOf(profile.getStats().getDeaths());
        }
        break;
      case "wins":
        if (input.length == 1) {
          return null;
        }

        switch (input[1]) {
          case "ranked":
            if (input.length == 2) {
              // wins_ranked
              return String.valueOf(profile.getStats().getWins(true));
            }
            break;
          case "unranked":
            if (input.length == 2) {
              // wins_unranked
              return String.valueOf(profile.getStats().getWins(false));
            }
            break;
          case "global":
            if (input.length == 2) {
              // wins_global
              return String.valueOf(profile.getStats().getGlobalWins());
            } else if (input.length == 3) {
              switch (input[2]) {
                // wins_global_u
                case "u":
                  return String.valueOf(profile.getStats().getWins(false));
                // wins_global_r
                case "r":
                  return String.valueOf(profile.getStats().getWins(true));

                default:
                  break;
              }
            }
            break;
          case "ladder":
            if (input.length != 4) {
              return null;
            }

            NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
            if (ladder == null) {
              return null;
            }

            switch (input[3]) {
              // wins_ladder_<ladder>_u
              case "u":
                return String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedWins());
              // wins_ladder_<ladder>_r
              case "r":
                if (ladder.isRanked()) {
                  return String.valueOf(profile.getStats().getLadderStat(ladder).getRankedWins());
                }
                break;

              default:
                break;
            }
            break;

          default:
            break;
        }
        break;
      case "losses":
        if (input.length == 1) {
          return null;
        }

        switch (input[1]) {
          case "ranked":
            if (input.length == 2) {
              // losses_ranked
              return String.valueOf(profile.getStats().getLosses(true));
            }
            break;
          case "unranked":
            if (input.length == 2) {
              // losses_unranked
              return String.valueOf(profile.getStats().getLosses(false));
            }
            break;
          case "global":
            if (input.length == 2) {
              // losses_global
              return String.valueOf(profile.getStats().getGlobalLosses());
            } else if (input.length == 3) {
              switch (input[2]) {
                // losses_global_u
                case "u":
                  return String.valueOf(profile.getStats().getLosses(false));
                // losses_global_r
                case "r":
                  return String.valueOf(profile.getStats().getLosses(true));

                default:
                  break;
              }
            }
            break;
          case "ladder":
            if (input.length != 4) {
              return null;
            }

            NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
            if (ladder == null) {
              return null;
            }

            switch (input[3]) {
              // losses_ladder_<ladder>_u
              case "u":
                return String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedLosses());
              // losses_ladder_<ladder>_r
              case "r":
                if (ladder.isRanked()) {
                  return String.valueOf(profile.getStats().getLadderStat(ladder).getRankedLosses());
                }
                break;

              default:
                break;
            }
            break;

          default:
            break;
        }
        break;
      case "kdr":
        if (input.length != 2) {
          break;
        }

        // kdr_global
        if (input[1].equals("global")) {
          return String.valueOf(profile.getStats().getGlobalKdr());
        }
        break;
      case "winrate":
        if (input.length != 2) {
          break;
        }

        switch (input[1]) {
          // winrate_global
          case "global":
            return String.valueOf(profile.getStats().getGlobalWinrate());
          // winrate_ranked
          case "ranked":
            return String.valueOf(profile.getStats().getWinrate(true));
          // winrate_unranked
          case "unranked":
            return String.valueOf(profile.getStats().getWinrate(false));

          default:
            break;
        }
        break;
      case "elo":
        if (input.length == 1) {
          return null;
        }

        switch (input[1]) {
          // elo_global
          case "global":
            if (input.length == 2) {
              return String.valueOf(profile.getStats().getGlobalElo());
            }
            break;
          // elo_ladder_<ladder>
          case "ladder":
            if (input.length != 3) {
              return null;
            }

            NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
            if (ladder == null || !ladder.isRanked()) {
              return null;
            }

            return String.valueOf(profile.getStats().getLadderStat(ladder).getElo());

          default:
            break;
        }
        break;
      case "lb":
        if (input.length == 1) {
          return null;
        }

        Leaderboard leaderboard;
        Map<OfflinePlayer, Integer> list;
        OfflinePlayer lbPlayer;
        int rank;

        switch (input[1]) {
          case "global":
            if (input.length != 5) {
              return null;
            }

            try {
              rank = Integer.parseInt(input[3]);
              if (rank < 1 || rank > 10) {
                return null;
              }
            } catch (NumberFormatException e) {
              return null;
            }

            leaderboard =
                switch (input[2]) {
                  case "wins" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.GLOBAL, LbSecondaryType.WIN, null);
                  case "elo" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.GLOBAL, LbSecondaryType.ELO, null);
                  default -> null;
                };

            if (leaderboard == null) {
              return null;
            }
            list = leaderboard.getList();
            if (list.size() < rank) {
              return null;
            }

            lbPlayer = new ArrayList<>(list.keySet()).get(rank - 1);

            switch (input[4]) {
              // lb_global_wins_<1-10>_k || lb_global_elo_<1-10>_k
              case "k":
                return lbPlayer.getName();
              // lb_global_wins_<1-10>_v || lb_global_elo_<1-10>_v
              case "v":
                return String.valueOf(list.get(lbPlayer));

              default:
                break;
            }
            break;
          case "ffa", "ffa_global":
            String statType;
            String rankStr;
            String kvStr;

            if (input[1].equals("ffa") && input.length == 6 && input[2].equals("global")) {
              // %ap_lb_ffa_global_<type>_<rank>_<k|v>%
              statType = input[3];
              rankStr = input[4];
              kvStr = input[5];
            } else if (input.length == 5) {
              // %ap_lb_ffa_<type>_<rank>_<k|v>% or %ap_lb_ffa_global_<type>_<rank>_<k|v>%
              statType = input[2];
              rankStr = input[3];
              kvStr = input[4];
            } else {
              return null;
            }

            try {
              rank = Integer.parseInt(rankStr);
              if (rank < 1 || rank > 10) {
                return null;
              }
            } catch (NumberFormatException e) {
              return null;
            }

            leaderboard =
                switch (statType) {
                  case "kills" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.FFA_GLOBAL, LbSecondaryType.KILLS, null);
                  case "deaths" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.FFA_GLOBAL, LbSecondaryType.DEATHS, null);
                  case "streak", "best_streak", "killstreak", "kill_streak" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.FFA_GLOBAL, LbSecondaryType.BEST_WIN_STREAK, null);
                  default -> null;
                };

            if (leaderboard == null) {
              return null;
            }
            list = leaderboard.getList();
            if (list.size() < rank) {
              return null;
            }

            lbPlayer = new ArrayList<>(list.keySet()).get(rank - 1);

            switch (kvStr) {
              case "k":
                return lbPlayer.getName();
              case "v":
                return String.valueOf(list.get(lbPlayer));

              default:
                break;
            }
            break;
          case "ladder":
            if (input.length != 6) {
              return null;
            }

            try {
              rank = Integer.parseInt(input[4]);
              if (rank < 1 || rank > 10) {
                return null;
              }
            } catch (NumberFormatException e) {
              return null;
            }

            NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
            if (ladder == null) {
              return null;
            }

            leaderboard =
                switch (input[3]) {
                  case "wins" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.LADDER, LbSecondaryType.WIN, ladder);
                  case "elo" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.LADDER, LbSecondaryType.ELO, ladder);
                  default -> null;
                };

            if (leaderboard == null) {
              return null;
            }
            list = leaderboard.getList();
            if (list.size() < rank) {
              return null;
            }

            lbPlayer = new ArrayList<>(list.keySet()).get(rank - 1);

            switch (input[5]) {
              // lb_ladder_<ladder>_wins_<1-10>_k || lb_ladder_<ladder>_elo_<1-10>_k
              case "k":
                return lbPlayer.getName();
              // lb_ladder_<ladder>_wins_<1-10>_v || lb_ladder_<ladder>_elo_<1-10>_v
              case "v":
                return String.valueOf(list.get(lbPlayer));

              default:
                break;
            }
            break;

          default:
            break;
        }
        break;
      case "ffa":
        if (input.length == 1) {
          return null;
        }

        Statistic statistic = getFfaStatistic(player);
        if (statistic != null) {
          switch (input[1]) {
            // ffa_killstreak
            case "killstreak":
              if (input.length == 2) {
                return String.valueOf(statistic.getKillStreak());
              }
              break;
            // ffa_best_killstreak
            case "best":
              if (input.length == 3 && input[2].equals("killstreak")) {
                return String.valueOf(statistic.getBestKillStreak());
              }
              break;
            // ffa_session_...
            case "session":
              if (input.length == 3) {
                switch (input[2]) {
                  case "kills" -> {
                    return String.valueOf(statistic.getKills());
                  }
                  case "deaths" -> {
                    return String.valueOf(statistic.getDeaths());
                  }
                  case "streak" -> {
                    return String.valueOf(statistic.getKillStreak());
                  }
                  default -> {}
                }
              } else if (input.length == 4
                  && input[2].equals("best")
                  && input[3].equals("streak")) {
                return String.valueOf(statistic.getBestKillStreak());
              }
              break;

            default:
              break;
          }
        }

        Profile ffaProfile =
            player != null ? ProfileManager.getInstance().getProfile(player) : null;
        if (ffaProfile != null) {
          FfaStats ffaStats = ffaProfile.getStats().getFfaStats();
          switch (input[1]) {
            // ffa_global_... or ffa_lifetime_...
            case "global", "lifetime":
              if (input.length == 3) {
                switch (input[2]) {
                  case "kills" -> {
                    return String.valueOf(ffaStats.getKills());
                  }
                  case "deaths" -> {
                    return String.valueOf(ffaStats.getDeaths());
                  }
                  case "kdr" -> {
                    return String.valueOf(ffaStats.getKdr());
                  }
                  case "streak", "best_streak" -> {
                    return String.valueOf(ffaStats.getBestKillStreak());
                  }
                  default -> {}
                }
              }
              break;
            // ffa_kdr
            case "kdr":
              if (input.length == 2) {
                return String.valueOf(ffaStats.getKdr());
              }
              break;

            default:
              break;
          }
        }

        if (input.length != 3) {
          return null;
        }

        FfaArena ffaArena = ArenaManager.getInstance().getFfaArena(input[1]);
        if (ffaArena == null) {
          return null;
        }

        Ffa ffa = ffaArena.getFfa();
        if (ffa != null && ffa.isOpen()) {
          switch (input[2]) {
            case "players":
              return String.valueOf(ffa.getPlayers().size());
            case "spectators":
              return String.valueOf(ffa.getSpectators().size());

            default:
              break;
          }
        }
        break;

      default:
        break;
    }

    return null;
  }

  @Override
  public String onPlaceholderRequest(Player one, Player two, String params) {
    if (!"nametag_color".equalsIgnoreCase(params) || two == null) {
      return null;
    }
    return resolveNametagColor(two);
  }

  private String resolveNametagColor(OfflinePlayer offlinePlayer) {
    if (offlinePlayer == null) {
      return null;
    }

    Player onlinePlayer = offlinePlayer.getPlayer();
    if (onlinePlayer != null) {
      Match match = MatchManager.getInstance().getLiveMatchByPlayer(onlinePlayer);
      if (match instanceof Team teamMatch) {
        TeamEnum team = teamMatch.getTeam(onlinePlayer);
        if (team != null && team != TeamEnum.FFA) {
          return team.getColorMm();
        }
      }
    }

    Profile profile = ProfileManager.getInstance().getProfile(offlinePlayer);
    if (profile == null) {
      return null;
    }

    String playerName = offlinePlayer.getName() == null ? "" : offlinePlayer.getName();
    NamedTextColor lobbyColor =
        LobbyNametagUtil.getLobbyNametag(profile, playerName, onlinePlayer).getScoreboardNameColor();
    return "<" + lobbyColor.toString().toLowerCase() + ">";
  }

  private Statistic getFfaStatistic(OfflinePlayer offlinePlayer) {
    if (offlinePlayer == null) {
      return null;
    }
    Player player = offlinePlayer.getPlayer();
    if (player == null) {
      return null;
    }
    Ffa ffa = FfaManager.getInstance().getFfaByPlayer(player);
    if (ffa == null) {
      return null;
    }
    return ffa.getStatistics().get(player);
  }
}
