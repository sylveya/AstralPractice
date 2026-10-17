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

    List<String> ladderNames = ArenaUtil.getLadderNames(arena);
    for (String line :
        PracticeConfig.language().getCommand().getArena().getArguments().getInfo().getArenaInfo()) {
      Common.sendMmMessage(
          player,
          line.replace("%arena%", arena.getName())
              .replace("%type%", arena.getType().getName())
              .replace(
                  "%icon%",
                  arena.getIcon() != null
                      ? PracticeConfig.language()
                          .getCommand()
                          .getArena()
                          .getArguments()
                          .getInfo()
                          .getStatusNames()
                          .getSet()
                      : PracticeConfig.language()
                          .getCommand()
                          .getArena()
                          .getArguments()
                          .getInfo()
                          .getStatusNames()
                          .getNotSet())
              .replace("%displayName%", arena.getDisplayName())
              .replace(
                  "%ladders%",
                  (ladderNames.isEmpty()
                      ? StringUtil.cc("<red>NULL")
                      : ladderNames.toString().replace("]", "").replace("[", "")))
              .replace("%corner1%", ArenaUtil.convertLocation(arena.getCorner1()))
              .replace("%corner2%", ArenaUtil.convertLocation(arena.getCorner2()))
              .replace("%position1%", ArenaUtil.convertLocation(arena.getPosition1()))
              .replace("%position2%", ArenaUtil.convertLocation(arena.getPosition2()))
              .replace("%partyFfaCenter%", ArenaUtil.convertLocation(arena.getPartyFfaCenter()))
              .replace(
                  "%spectatorPosition%", ArenaUtil.convertLocation(arena.getSpectatorPosition()))
              .replace(
                  "%status%",
                  arena.isEnabled()
                      ? PracticeConfig.language()
                          .getCommand()
                          .getArena()
                          .getArguments()
                          .getInfo()
                          .getStatusNames()
                          .getEnabled()
                      : PracticeConfig.language()
                          .getCommand()
                          .getArena()
                          .getArguments()
                          .getInfo()
                          .getStatusNames()
                          .getDisabled()));
    }
  }

  public static List<String> tabComplete(Player player, String[] args) {
    List<String> arguments = new ArrayList<>();
    if (!player.hasPermission("ap.setup")) {
      return arguments;
    }

    if (args.length == 2) {
      for (DisplayArena arena : ArenaManager.getInstance().getArenaList()) {
        arguments.add(arena.getName());
      }

      return org.bukkit.util.StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
    }

    return arguments;
  }
}
