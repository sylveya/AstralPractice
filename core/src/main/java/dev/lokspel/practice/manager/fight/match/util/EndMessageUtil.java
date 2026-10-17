package dev.lokspel.practice.manager.fight.match.util;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.language.MatchTypeMessages;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.enums.TeamEnum;
import dev.lokspel.practice.manager.fight.match.type.duel.Duel;
import dev.lokspel.practice.manager.fight.match.type.partyffa.PartyFfa;
import dev.lokspel.practice.manager.fight.match.type.playersvsplayers.partysplit.PartySplit;
import dev.lokspel.practice.manager.fight.match.type.playersvsplayers.partyvsparty.PartyVsParty;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

public enum EndMessageUtil {
  ;

  public static List<String> getEndMessage(Duel duel, List<String> rankedExtension) {
    Player winner = duel.getMatchWinner();
    Player loser = duel.getOppositePlayer(winner);

    List<String> message = new ArrayList<>();

    for (String line : PracticeConfig.language().getMatch().getDuel().getMatchEnd().getMessage()) {
      if (line.contains("%spectatorExtension%")) {
        List<String> spectatorNames = new ArrayList<>();
        for (Player spectator : duel.getSpectators()) {
          Profile spectatorProfile = ProfileManager.getInstance().getProfile(spectator);
          if (!spectatorProfile.isHideFromPlayers()) {
            spectatorNames.add(spectator.getName());
          }
        }

        if (!spectatorNames.isEmpty()) {
          for (String line2 :
              PracticeConfig.language()
                  .getMatch()
                  .getDuel()
                  .getMatchEnd()
                  .getSpectatorExtension()) {
            message.add(
                line2
                    .replace("%size%", String.valueOf(spectatorNames.size()))
                    .replace(
                        "%spectators%",
                        spectatorNames.toString().replace("[", "").replace("]", "")));
          }
        }
      } else if (line.contains("%rankedExtension%")) {
        if (duel.isRanked()) {
          message.addAll(rankedExtension);
        }
      } else {
        message.add(
            line.replace("%matchId%", duel.getId())
                .replace("%winner%", winner.getName())
                .replace("%winner_uuid%", winner.getUniqueId().toString())
                .replace("%loser%", loser.getName())
                .replace("%loser_uuid%", loser.getUniqueId().toString()));
      }
    }

    return message;
  }

  public static List<String> getEndMessage(PartyFfa partyFfa, List<Player> losers) {
    Player winner = partyFfa.getMatchWinner();
    List<String> message = new ArrayList<>();

    StringBuilder losersBuilder = new StringBuilder();
    String separator =
        PracticeConfig.language().getMatch().getPartyFfa().getMatchEnd().getSeparatorFormat();
    for (int i = 0; i < losers.size(); i++) {
      Player loser = losers.get(i);
      if (i > 0) {
        losersBuilder.append(separator);
      }

      if (partyFfa.getPlayers().contains(loser)) {
        losersBuilder.append(
            PracticeConfig.language()
                .getMatch()
                .getPartyFfa()
                .getMatchEnd()
                .getLoserPlayerFormat()
                .replace("%matchId%", partyFfa.getId())
                .replace("%player%", loser.getName())
                .replace("%player_uuid%", loser.getUniqueId().toString()));
      } else {
        losersBuilder.append(
            PracticeConfig.language()
                .getMatch()
                .getPartyFfa()
                .getMatchEnd()
                .getLeftPlayerFormat()
                .replace("%player%", loser.getName()));
      }
    }
    String losersString = losersBuilder.toString();

    for (String line :
        PracticeConfig.language().getMatch().getPartyFfa().getMatchEnd().getMessage()) {
      if (line.contains("%spectatorExtension%")) {
        List<String> spectators = new ArrayList<>();
        for (Player spectator : partyFfa.getSpectators()) {
          Profile spectatorProfile = ProfileManager.getInstance().getProfile(spectator);
          if (!spectatorProfile.isHideFromPlayers()) {
            spectators.add(spectator.getName());
          }
        }

        if (!spectators.isEmpty()) {
          for (String line2 :
              PracticeConfig.language()
                  .getMatch()
                  .getPartyFfa()
                  .getMatchEnd()
                  .getSpectatorExtension()) {
            message.add(
                line2
                    .replace("%size%", String.valueOf(spectators.size()))
                    .replace(
                        "%spectators%", spectators.toString().replace("[", "").replace("]", "")));
          }
        }
      } else {
        message.add(
            line.replace("%matchId%", partyFfa.getId())
                .replace("%losers%", losersString)
                .replace("%winner%", winner.getName())
                .replace("%winner_uuid%", winner.getUniqueId().toString()));
      }
    }

    return message;
  }

  public static List<String> getEndMessage(
      PartySplit partySplit, TeamEnum winnerTeam, List<Player> winners, List<Player> losers) {
    return getPlayersVsPlayersEndMessage(
        partySplit,
        winnerTeam,
        winners,
        losers,
        PracticeConfig.language().getMatch().getPartySplit().getMatchEnd());
  }

  public static List<String> getEndMessage(
      PartyVsParty partyVsParty, TeamEnum winnerTeam, List<Player> winners, List<Player> losers) {
    return getPlayersVsPlayersEndMessage(
        partyVsParty,
        winnerTeam,
        winners,
        losers,
        PracticeConfig.language().getMatch().getPartyVsParty().getMatchEnd());
  }

  private static List<String> getPlayersVsPlayersEndMessage(
      Match match,
      TeamEnum winnerTeam,
      List<Player> winners,
      List<Player> losers,
      MatchTypeMessages.MatchEnd matchEnd) {
    List<String> message = new ArrayList<>();
    TeamEnum loserTeam = TeamUtil.getOppositeTeam(winnerTeam);

    String winnersString =
        formatTeamPlayers(
            match, winners, matchEnd.getWinnerPlayerFormat(), matchEnd.getLeftPlayerFormat());
    String losersString =
        formatTeamPlayers(
            match, losers, matchEnd.getLoserPlayerFormat(), matchEnd.getLeftPlayerFormat());

    for (String line : matchEnd.getMessage()) {
      if (line.contains("%spectatorExtension%")) {
        List<String> spectators = new ArrayList<>();
        for (Player spectator : match.getSpectators()) {
          Profile spectatorProfile = ProfileManager.getInstance().getProfile(spectator);
          if (spectatorProfile != null && !spectatorProfile.isHideFromPlayers()) {
            spectators.add(spectator.getName());
          }
        }

        if (!spectators.isEmpty()) {
          for (String line2 : matchEnd.getSpectatorExtension()) {
            message.add(
                line2
                    .replace("%size%", String.valueOf(spectators.size()))
                    .replace(
                        "%spectators%", spectators.toString().replace("[", "").replace("]", "")));
          }
        }
      } else {
        message.add(
            line.replace("%matchId%", match.getId())
                .replace("%winnerTeam%", winnerTeam.getNameMm())
                .replace("%winners%", winnersString)
                .replace("%loserTeam%", loserTeam.getNameMm())
                .replace("%losers%", losersString));
      }
    }

    return message;
  }

  private static String formatTeamPlayers(
      Match match, List<Player> players, String playerFormat, String leftFormat) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < players.size(); i++) {
      Player player = players.get(i);
      if (i > 0) {
        builder.append(", ");
      }
      if (match.getPlayers().contains(player)) {
        builder.append(
            playerFormat
                .replace("%matchId%", match.getId())
                .replace("%player%", player.getName())
                .replace("%player_uuid%", player.getUniqueId().toString()));
      } else if (leftFormat != null && !leftFormat.isEmpty()) {
        builder.append(leftFormat.replace("%player%", player.getName()));
      } else {
        builder.append(player.getName());
      }
    }
    return builder.toString();
  }
}
