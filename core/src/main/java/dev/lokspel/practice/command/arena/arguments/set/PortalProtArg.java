package dev.lokspel.practice.command.arena.arguments.set;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class PortalProtArg {

  private PortalProtArg() {}

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
              .getPortalProtection()
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
              .getPortalProtection()
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
              .getPortalProtection()
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
              .getPortalProtection()
              .getNotBuild()
              .replace("%arena%", arena.getName()));
      return;
    }

    try {
      final int portalProtection = Integer.parseInt(args[3]);

      if (portalProtection < 0 || portalProtection > 20) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getPortalProtection()
                .getNotInteger());
        return;
      }

      arena.setPortalProtectionValue(portalProtection);
      arena.setPortalProtection(true);

      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPortalProtection()
              .getSetPortalprot()
              .replace("%arena%", arena.getName())
              .replace("%portalProt%", String.valueOf(portalProtection)));
    } catch (NumberFormatException e) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPortalProtection()
              .getNotInteger());
    }
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.setup") || args.length != 3) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[2], ArenaManager.getInstance().getNormalArenaNames(), new ArrayList<>());
  }
}
