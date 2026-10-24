package dev.lokspel.practice.command.arena.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class FreezeArg {

  private FreezeArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.arena.freeze")) {
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
              .getFreeze()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    Arena arena = ArenaManager.getInstance().getNormalArena(args[1]);
    if (arena == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getFreeze()
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
              .getFreeze()
              .getArenaFrozen()
              .replace("%arena%", arena.getName()));
      return;
    }

    if (!arena.isFrozen()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getFreeze()
              .getFreezeSuccess()
              .replace("%arena%", arena.getName()));
    } else {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getFreeze()
              .getUnfreezeSuccess()
              .replace("%arena%", arena.getName()));
    }

    arena.setFrozen(!arena.isFrozen());
    GuiManager.getInstance().searchGui(GuiType.Arena_Summary).update();
    ArenaGuiSetupManager.getInstance()
        .getArenaSetupGuis()
        .get(arena)
        .get(GuiType.Arena_Main)
        .update();
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.arena.freeze") || args.length != 2) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[1],
        ArenaManager.getInstance().getNormalArenaNames(Arena::isEnabled),
        new ArrayList<>());
  }
}
