package dev.lokspel.practice.command.arena.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class StopArg {

  private StopArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.arena.stop")) {
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
              .getStop()
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
              .getStop()
              .getArenaNotExists()
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
              .getStop()
              .getArenaEnabled()
              .replace("%arena%", arena.getName()));
      return;
    }

    List<Match> matches = MatchManager.getInstance().getLiveMatchesByArena(arena);
    if (matches.isEmpty()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getStop()
              .getNoMatches()
              .replace("%arena%", arena.getName()));
      return;
    }

    for (Match match : matches) {
      match.sendMessage(
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getStop()
              .getMatchEndMessage()
              .replace("%player%", player.getName()),
          true);
      match.endMatch();
    }
    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getArena()
            .getArguments()
            .getStop()
            .getPlayerEnded());
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.setup") || args.length != 2) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[1], ArenaManager.getInstance().getArenaNames(), new ArrayList<>());
  }
}
