package dev.lokspel.practice.command.ladder.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class FreezeArg {

  private FreezeArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.ladder.freeze")) {
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
              .getFreeze()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    NormalLadder ladder = LadderManager.getInstance().getLadder(args[1]);
    if (ladder == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getFreeze()
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
              .getFreeze()
              .getLadderDisabled()
              .replace("%ladder%", ladder.getDisplayName()));
      return;
    }

    if (!ladder.isFrozen()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getFreeze()
              .getFreezeSuccess()
              .replace("%ladder%", ladder.getDisplayName()));
    } else {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getFreeze()
              .getUnfreezeSuccess()
              .replace("%ladder%", ladder.getDisplayName()));
    }

    ladder.setFrozen(!ladder.isFrozen());
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.arena.freeze") || args.length != 2) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[1],
        LadderManager.getInstance().getLadderNames(ladder -> ladder.isEnabled()),
        new ArrayList<>());
  }
}
