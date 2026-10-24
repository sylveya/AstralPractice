package dev.lokspel.practice.command.ladder.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class StopArg {

  private StopArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.ladder.stop")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getLadder().getNoPermission());
      return;
    }

    if (args.length != 2) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getStop()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    Ladder ladder = LadderManager.getInstance().getLadder(args[1]);
    if (ladder == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getStop()
              .getNotExists()
              .replace("%ladder%", args[1]));
      return;
    }

    if (!ladder.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getStop()
              .getLadderDisabled()
              .replace("%ladder%", ladder.getDisplayName()));
      return;
    }

    List<Match> matches = MatchManager.getInstance().getLiveMatchesByLadder(ladder);
    if (matches.isEmpty()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getStop()
              .getNoMatches()
              .replace("%ladder%", ladder.getDisplayName()));
      return;
    }

    for (Match match : matches) {
      match.sendMessage(
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getStop()
              .getMatchEndMessage()
              .replace("%player%", player.getName()),
          true);
      match.endMatch();
    }
    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getLadder()
            .getArguments()
            .getStop()
            .getPlayerEnded()
            .replace("%ladder%", ladder.getDisplayName()));
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.setup") || args.length != 2) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[1], LadderManager.getInstance().getLadderNames(Ladder::isEnabled), new ArrayList<>());
  }
}
