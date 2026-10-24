package dev.lokspel.practice.command.arena.arguments.set;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class SideBuildLimitArg {

  private SideBuildLimitArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.setup")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getArena().getNoPermission());
      return;
    }

    if (args.length != 4) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getSidebuildlimit()
              .getCommandHelp()
              .replace("%label%", label)
              .replace("%label2%", args[1]));
      return;
    }

    Arena arena = ArenaManager.getInstance().getNormalArena(args[2]);
    if (arena == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getSidebuildlimit()
              .getArenaNotExists()
              .replace("%arena%", args[2]));
      return;
    }

    if (arena.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getSidebuildlimit()
              .getArenaEnabled()
              .replace("%arena%", arena.getName()));
      return;
    }

    if (!arena.isBuild()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getSidebuildlimit()
              .getNotBuild()
              .replace("%arena%", arena.getName()));
      return;
    }

    int sideBuildLimit = Integer.parseInt(args[3]);
    if (sideBuildLimit < 0 || sideBuildLimit > 10) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getSidebuildlimit()
              .getInvalidNumber()
              .replace("%arena%", arena.getName()));
      return;
    }

    arena.setSideBuildLimit(sideBuildLimit);
    ArenaGuiSetupManager.getInstance()
        .getArenaSetupGuis()
        .get(arena)
        .get(GuiType.Arena_Main)
        .update();

    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getArena()
            .getArguments()
            .getSidebuildlimit()
            .getSetSidebuildlimit()
            .replace("%arena%", arena.getName())
            .replace("%sideBuildLimit%", args[3]));
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.setup") || args.length != 3) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[2], ArenaManager.getInstance().getNormalArenaNames(), new ArrayList<>());
  }
}
