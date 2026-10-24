package dev.lokspel.practice.util.placeholderapi;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.division.Division;
import dev.lokspel.practice.manager.division.DivisionManager;
import dev.lokspel.practice.manager.fight.ffa.FfaManager;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.enums.TeamEnum;
import dev.lokspel.practice.manager.fight.match.interfaces.Team;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.fight.util.stats.Statistic;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.leaderboard.Leaderboard;
import dev.lokspel.practice.manager.leaderboard.LeaderboardManager;
import dev.lokspel.practice.manager.leaderboard.types.LbMainType;
import dev.lokspel.practice.manager.leaderboard.types.LbSecondaryType;
import dev.lokspel.practice.manager.nametag.LobbyNametagUtil;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.group.Group;
import dev.lokspel.practice.manager.profile.statistics.FfaStats;
import dev.lokspel.practice.manager.queue.QueueManager;
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

        return switch (input[1]) {
          case "name" -> input.length == 2 ? group.getDisplayName() : null;
          case "prefix" ->
              input.length == 2
                  ? AstralPractice.getMiniMessage().serialize(group.getPrefix())
                  : null;
          case "suffix" ->
              input.length == 2
                  ? AstralPractice.getMiniMessage().serialize(group.getSuffix())
                  : null;
          case "limit" -> {
            if (input.length != 3) {
              yield null;
            }
            yield switch (input[2]) {
              case "u" -> String.valueOf(group.getUnrankedLimit());
              case "r" -> String.valueOf(group.getRankedLimit());
              default -> null;
            };
          }
          default -> null;
        };
      case "in":
        if (input.length == 1) {
          return null;
        }

        switch (input[1]) {
          case "queue":
            if (input.length == 2) {
              return String.valueOf(QueueManager.getInstance().getQueues().size());
            } else if (input.length == 3) {
              NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
              return ladder != null
                  ? String.valueOf(QueueManager.getInstance().getQueueSize(ladder))
                  : null;
            }
            return null;
          case "fight":
            if (input.length == 2) {
              return String.valueOf(MatchManager.getInstance().getPlayerInMatchSize());
            } else if (input.length == 3) {
              NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
              return ladder != null
                  ? String.valueOf(MatchManager.getInstance().getPlayerInMatchSize(ladder))
                  : null;
            }
            return null;
          default:
            return null;
        }
      case "division":
        if (input.length == 1) {
          return null;
        }

        switch (input[1]) {
          case "weight", "short", "full":
            return formatDivision(profile.getStats().getDivision(), input[1]);
          case "ladder":
            if (input.length != 4) {
              return null;
            }
            NormalLadder ladderByKeyword = LadderManager.getInstance().getLadder(input[2]);
            return ladderByKeyword != null
                ? formatDivision(
                    DivisionManager.getInstance().getLadderDivision(profile, ladderByKeyword),
                    input[3])
                : null;
          default:
            if (input.length == 3) {
              NormalLadder directLadder = LadderManager.getInstance().getLadder(input[1]);
              return directLadder != null
                  ? formatDivision(
                      DivisionManager.getInstance().getLadderDivision(profile, directLadder),
                      input[2])
                  : null;
            }
            return null;
        }
      case "match":
        if (input.length == 1 || !"division".equals(input[1])) {
          return null;
        }

        Player onlinePlayer = player != null ? player.getPlayer() : null;
        if (onlinePlayer == null) {
          return null;
        }
        Match liveMatch = MatchManager.getInstance().getLiveMatchByPlayer(onlinePlayer);
        if (liveMatch == null || !(liveMatch.getLadder() instanceof NormalLadder matchLadder)) {
          return null;
        }
        Division matchDiv = DivisionManager.getInstance().getLadderDivision(profile, matchLadder);
        if (matchDiv == null) {
          return null;
        }

        return input.length == 2 ? matchDiv.getFullName() : formatDivision(matchDiv, input[2]);
      case "best":
        if (input.length == 1 || !"division".equals(input[1])) {
          return null;
        }

        Division bestDiv = DivisionManager.getInstance().getBestDivision(profile);
        if (bestDiv == null) {
          return null;
        }

        return input.length == 2 ? bestDiv.getFullName() : formatDivision(bestDiv, input[2]);
      case "kills":
        return (input.length == 2 && "global".equals(input[1]))
            ? String.valueOf(profile.getStats().getKills())
            : null;
      case "deaths":
        return (input.length == 2 && "global".equals(input[1]))
            ? String.valueOf(profile.getStats().getDeaths())
            : null;
      case "wins":
        if (input.length == 1) {
          return null;
        }

        switch (input[1]) {
          case "ranked":
            return input.length == 2 ? String.valueOf(profile.getStats().getWins(true)) : null;
          case "unranked":
            return input.length == 2 ? String.valueOf(profile.getStats().getWins(false)) : null;
          case "global":
            if (input.length == 2) {
              return String.valueOf(profile.getStats().getGlobalWins());
            } else if (input.length == 3) {
              return switch (input[2]) {
                case "u" -> String.valueOf(profile.getStats().getWins(false));
                case "r" -> String.valueOf(profile.getStats().getWins(true));
                default -> null;
              };
            }
            return null;
          case "ladder":
            if (input.length != 4) {
              return null;
            }
            NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
            if (ladder == null) {
              return null;
            }
            return switch (input[3]) {
              case "u" ->
                  String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedWins());
              case "r" ->
                  ladder.isRanked()
                      ? String.valueOf(profile.getStats().getLadderStat(ladder).getRankedWins())
                      : null;
              default -> null;
            };
          default:
            return null;
        }
      case "losses":
        if (input.length == 1) {
          return null;
        }

        switch (input[1]) {
          case "ranked":
            return input.length == 2 ? String.valueOf(profile.getStats().getLosses(true)) : null;
          case "unranked":
            return input.length == 2 ? String.valueOf(profile.getStats().getLosses(false)) : null;
          case "global":
            if (input.length == 2) {
              return String.valueOf(profile.getStats().getGlobalLosses());
            } else if (input.length == 3) {
              return switch (input[2]) {
                case "u" -> String.valueOf(profile.getStats().getLosses(false));
                case "r" -> String.valueOf(profile.getStats().getLosses(true));
                default -> null;
              };
            }
            return null;
          case "ladder":
            if (input.length != 4) {
              return null;
            }
            NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
            if (ladder == null) {
              return null;
            }
            return switch (input[3]) {
              case "u" ->
                  String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedLosses());
              case "r" ->
                  ladder.isRanked()
                      ? String.valueOf(profile.getStats().getLadderStat(ladder).getRankedLosses())
                      : null;
              default -> null;
            };
          default:
            return null;
        }
      case "kdr":
        return (input.length == 2 && "global".equals(input[1]))
            ? String.valueOf(profile.getStats().getGlobalKdr())
            : null;
      case "winrate":
        if (input.length != 2) {
          return null;
        }
        return switch (input[1]) {
          case "global" -> String.valueOf(profile.getStats().getGlobalWinrate());
          case "ranked" -> String.valueOf(profile.getStats().getWinrate(true));
          case "unranked" -> String.valueOf(profile.getStats().getWinrate(false));
          default -> null;
        };
      case "elo":
        if (input.length == 1) {
          return null;
        }
        switch (input[1]) {
          case "global":
            return input.length == 2 ? String.valueOf(profile.getStats().getGlobalElo()) : null;
          case "ladder":
            if (input.length != 3) {
              return null;
            }
            NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
            return (ladder != null && ladder.isRanked())
                ? String.valueOf(profile.getStats().getLadderStat(ladder).getElo())
                : null;
          default:
            return null;
        }
      case "lb":
        if (input.length == 1) {
          return null;
        }

        switch (input[1]) {
          case "global":
            if (input.length != 5) {
              return null;
            }
            int globalRank = parseRank(input[3]);
            if (globalRank < 0) {
              return null;
            }
            Leaderboard globalLb =
                switch (input[2]) {
                  case "wins" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.GLOBAL, LbSecondaryType.WIN, null);
                  case "elo" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.GLOBAL, LbSecondaryType.ELO, null);
                  default -> null;
                };
            return getLbValue(globalLb, globalRank, input[4]);

          case "ffa", "ffa_global":
            String statType;
            String rankStr;
            String kvStr;
            if (input[1].equals("ffa") && input.length == 6 && input[2].equals("global")) {
              statType = input[3];
              rankStr = input[4];
              kvStr = input[5];
            } else if (input.length == 5) {
              statType = input[2];
              rankStr = input[3];
              kvStr = input[4];
            } else {
              return null;
            }
            int ffaRank = parseRank(rankStr);
            if (ffaRank < 0) {
              return null;
            }
            Leaderboard ffaLb =
                switch (statType) {
                  case "kills" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.FFA_GLOBAL, LbSecondaryType.KILLS, null);
                  case "deaths" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.FFA_GLOBAL, LbSecondaryType.DEATHS, null);
                  case "streak", "killstreak" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.FFA_GLOBAL, LbSecondaryType.BEST_WIN_STREAK, null);
                  default -> null;
                };
            return getLbValue(ffaLb, ffaRank, kvStr);

          case "ladder":
            if (input.length != 6) {
              return null;
            }
            int ladderRank = parseRank(input[4]);
            if (ladderRank < 0) {
              return null;
            }
            NormalLadder ladder = LadderManager.getInstance().getLadder(input[2]);
            if (ladder == null) {
              return null;
            }
            Leaderboard ladderLb =
                switch (input[3]) {
                  case "wins" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.LADDER, LbSecondaryType.WIN, ladder);
                  case "elo" ->
                      LeaderboardManager.getInstance()
                          .searchLb(LbMainType.LADDER, LbSecondaryType.ELO, ladder);
                  default -> null;
                };
            return getLbValue(ladderLb, ladderRank, input[5]);

          default:
            return null;
        }
      case "ffa":
        if (input.length == 1) {
          return null;
        }

        Statistic statistic = getFfaStatistic(player);
        if (statistic != null) {
          switch (input[1]) {
            case "killstreak":
              if (input.length == 2) {
                return String.valueOf(statistic.getKillStreak());
              }
              break;
            case "best":
              if (input.length == 3 && input[2].equals("killstreak")) {
                return String.valueOf(statistic.getBestKillStreak());
              }
              break;
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
                  case "streak" -> {
                    return String.valueOf(ffaStats.getBestKillStreak());
                  }
                  default -> {}
                }
              } else if (input.length == 4
                  && input[2].equals("best")
                  && input[3].equals("streak")) {
                return String.valueOf(ffaStats.getBestKillStreak());
              }
              break;
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
          return switch (input[2]) {
            case "players" -> String.valueOf(ffa.getPlayers().size());
            case "spectators" -> String.valueOf(ffa.getSpectators().size());
            case "ladder" -> ffa.getLadderDisplayName();
            default -> null;
          };
        }
        return null;

      default:
        return null;
    }
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
        LobbyNametagUtil.getLobbyNametag(profile, playerName, onlinePlayer).scoreboardNameColor();
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

  private String formatDivision(Division division, String type) {
    if (division == null) {
      return null;
    }
    return switch (type) {
      case "weight" -> String.valueOf(DivisionManager.getInstance().getDivisionWeight(division));
      case "short" -> division.getShortName();
      case "full" -> division.getFullName();
      default -> null;
    };
  }

  private int parseRank(String str) {
    try {
      int rank = Integer.parseInt(str);
      return (rank >= 1 && rank <= 10) ? rank : -1;
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private String getLbValue(Leaderboard leaderboard, int rank, String kv) {
    if (leaderboard == null
        || leaderboard.getList() == null
        || leaderboard.getList().size() < rank) {
      return null;
    }
    OfflinePlayer lbPlayer =
        leaderboard.getList().keySet().stream().skip(rank - 1).findFirst().orElse(null);
    if (lbPlayer == null) {
      return null;
    }
    return switch (kv) {
      case "k" -> lbPlayer.getName();
      case "v" -> String.valueOf(leaderboard.getList().get(lbPlayer));
      default -> null;
    };
  }
}
