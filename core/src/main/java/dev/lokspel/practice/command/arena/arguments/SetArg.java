package dev.lokspel.practice.command.arena.arguments;

import dev.lokspel.practice.command.arena.arguments.set.IconArg;
import dev.lokspel.practice.command.arena.arguments.set.PartyFfaCenterArg;
import dev.lokspel.practice.command.arena.arguments.set.PortalProtArg;
import dev.lokspel.practice.command.arena.arguments.set.SideBuildLimitArg;
import dev.lokspel.practice.command.arena.arguments.set.SpectatorArg;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class SetArg {

  private SetArg() {}

  public static void run(Player player, String label, String[] args) {
    if (args.length > 1) {
      switch (args[1]) {
        case "icon" -> IconArg.run(player, label, args);
        case "portalprot" -> PortalProtArg.run(player, label, args);
        case "sidebuildlimit" -> SideBuildLimitArg.run(player, label, args);
        case "partyffacenter" -> PartyFfaCenterArg.run(player, label, args);
        case "spectator", "spectatorpos" -> SpectatorArg.run(player, label, args);
        default -> {}
      }
    } else {
      HelpArg.runSetCommand(player, label);
    }
  }

  public static List<String> tabComplete(Player player, String[] args) {
    List<String> arguments = new ArrayList<>();
    List<String> completion = new ArrayList<>();
    if (!player.hasPermission("ap.setup")) {
      return arguments;
    }

    if (args.length == 2) {
      arguments.add("icon");
      arguments.add("portalprot");
      arguments.add("sidebuildlimit");
      arguments.add("partyffacenter");
      arguments.add("spectator");

      StringUtil.copyPartialMatches(args[1], arguments, completion);
    } else if (args.length > 2) {
      completion =
          switch (args[1]) {
            case "icon" -> IconArg.tabComplete(player, args);
            case "portalprot" -> PortalProtArg.tabComplete(player, args);
            case "sidebuildlimit" -> SideBuildLimitArg.tabComplete(player, args);
            case "partyffacenter" -> PartyFfaCenterArg.tabComplete(player, args);
            case "spectator", "spectatorpos" -> SpectatorArg.tabComplete(player, args);
            default -> completion;
          };
    }

    Collections.sort(completion);
    return completion;
  }
}
