package dev.lokspel.practice.command.ladder.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class LoadArg {

  private LoadArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.setup")) {
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
              .getLoad()
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
              .getLoad()
              .getNotExists()
              .replace("%ladder%", args[1]));
      return;
    }

    if (!ladder.getKitData().isSet()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getLoad()
              .getNotSet()
              .replace("%ladder%", ladder.getDisplayName()));
      return;
    }

    ladder.getKitData().loadKitData(player, true);

    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getLadder()
            .getArguments()
            .getLoad()
            .getLoadSuccess()
            .replace("%ladder%", ladder.getDisplayName()));
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.setup") || args.length != 2) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[1], LadderManager.getInstance().getLadderNames(), new ArrayList<>());
  }
}
