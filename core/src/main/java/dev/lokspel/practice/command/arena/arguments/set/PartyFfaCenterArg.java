package dev.lokspel.practice.command.arena.arguments.set;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class PartyFfaCenterArg {

  private PartyFfaCenterArg() {}

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
              .getPartyffacenter()
              .getCommandHelp()
              .replace("%label%", label));
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
              .getPartyffacenter()
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
              .getPartyffacenter()
              .getArenaEnabled()
              .replace("%arena%", arena.getName()));
      return;
    }

    if (arena.isBuild() && !arena.getCopies().isEmpty()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPartyffacenter()
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
              .getPartyffacenter()
              .getNoRegion());
      return;
    }

    Location center = player.getLocation().clone();
    if (!arena.getCuboid().contains(center)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPartyffacenter()
              .getPosOutsideRegion());
      return;
    }

    arena.setPartyFfaCenter(center);
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
            .getPartyffacenter()
            .getSet()
            .replace("%arena%", arena.getName()));
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.setup") || args.length != 3) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[2], ArenaManager.getInstance().getNormalArenaNames(), new ArrayList<>());
  }
}
