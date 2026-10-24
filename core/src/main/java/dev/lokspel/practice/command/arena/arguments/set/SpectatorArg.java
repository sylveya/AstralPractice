package dev.lokspel.practice.command.arena.arguments.set;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.arena.setup.SpawnMarkerManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class SpectatorArg {

  private SpectatorArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.setup")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getArena().getNoPermission());
      return;
    }

    if (args.length != 3) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getSpectator()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    DisplayArena arena = ArenaManager.getInstance().getArena(args[2]);
    if (arena == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getSpectator()
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
              .getSpectator()
              .getArenaEnabled()
              .replace("%arena%", arena.getName()));
      return;
    }

    if (arena instanceof Arena normalArena
        && normalArena.isBuild()
        && !normalArena.getCopies().isEmpty()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getSpectator()
              .getCantEdit()
              .replace("%arena%", arena.getName()));
      return;
    }

    if (arena.getCuboid() == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getSpectator()
              .getNoRegion());
      return;
    }

    Location loc = player.getLocation().clone();
    if (!arena.getCuboid().contains(loc)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getSpectator()
              .getPosOutsideRegion());
      return;
    }

    if (arena.isDeadZone() && loc.getY() <= arena.getDeadZoneValue()) {
      Common.sendMmMessage(player, "<red>Spectator position cannot be at or below the dead zone!");
      return;
    }

    arena.setSpectatorPosition(loc);
    arena.setData();

    if (ArenaGuiSetupManager.getInstance().getArenaSetupGuis().containsKey(arena)) {
      ArenaGuiSetupManager.getInstance()
          .getArenaSetupGuis()
          .get(arena)
          .get(GuiType.Arena_Main)
          .update();
    }
    SpawnMarkerManager.getInstance().updateMarkers(arena);

    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getArena()
            .getArguments()
            .getSpectator()
            .getSet()
            .replace("%arena%", arena.getName()));
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.setup") || args.length != 3) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[2], ArenaManager.getInstance().getArenaNames(), new ArrayList<>());
  }
}
