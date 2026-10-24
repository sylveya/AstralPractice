package dev.lokspel.practice.command.arena.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class DisableArg {

  private DisableArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.setup")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getArena().getNoPermission());
      return;
    }

    if (args.length != 2) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getDisable()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    DisplayArena arena = ArenaManager.getInstance().getArena(args[1]);
    if (arena == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getDisable()
              .getNotExists()
              .replace("%arena%", args[1]));
      return;
    }

    if (!arena.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getDisable()
              .getAlreadyDisabled()
              .replace("%arena%", arena.getName()));
      return;
    }

    if (ArenaUtil.changeStatus(player, arena)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getDisable()
              .getArenaDisabled()
              .replace("%arena%", arena.getName()));
    }
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.setup") || args.length != 2) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[1],
        ArenaManager.getInstance().getArenaNames(DisplayArena::isEnabled),
        new ArrayList<>());
  }
}
