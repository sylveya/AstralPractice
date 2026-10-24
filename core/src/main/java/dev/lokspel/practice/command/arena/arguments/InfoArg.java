package dev.lokspel.practice.command.arena.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;

public final class InfoArg {

  private InfoArg() {}

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
              .getInfo()
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
              .getInfo()
              .getNotExists()
              .replace("%arena%", args[1]));
      return;
    }

    var infoConfig = PracticeConfig.language().getCommand().getArena().getArguments().getInfo();
    var statusNames = infoConfig.getStatusNames();
    List<String> ladderNames = ArenaUtil.getLadderNames(arena);

    String iconStatus = arena.getIcon() != null ? statusNames.getSet() : statusNames.getNotSet();
    String laddersStr =
        ladderNames.isEmpty() ? StringUtil.cc("<red>NULL") : String.join(", ", ladderNames);
    String status = arena.isEnabled() ? statusNames.getEnabled() : statusNames.getDisabled();
    String corner1 = ArenaUtil.convertLocation(arena.getCorner1());
    String corner2 = ArenaUtil.convertLocation(arena.getCorner2());
    String pos1 = ArenaUtil.convertLocation(arena.getPosition1());
    String pos2 = ArenaUtil.convertLocation(arena.getPosition2());
    String partyFfaCenter = ArenaUtil.convertLocation(arena.getPartyFfaCenter());
    String specPos = ArenaUtil.convertLocation(arena.getSpectatorPosition());

    for (String line : infoConfig.getArenaInfo()) {
      Common.sendMmMessage(
          player,
          line.replace("%arena%", arena.getName())
              .replace("%type%", arena.getType().getName())
              .replace("%icon%", iconStatus)
              .replace("%displayName%", arena.getDisplayName())
              .replace("%ladders%", laddersStr)
              .replace("%corner1%", corner1)
              .replace("%corner2%", corner2)
              .replace("%position1%", pos1)
              .replace("%position2%", pos2)
              .replace("%partyFfaCenter%", partyFfaCenter)
              .replace("%spectatorPosition%", specPos)
              .replace("%status%", status));
    }
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.setup") || args.length != 2) {
      return List.of();
    }

    return org.bukkit.util.StringUtil.copyPartialMatches(
        args[1], ArenaManager.getInstance().getArenaNames(), new ArrayList<>());
  }
}
