package dev.lokspel.practice.command.ladder.arguments;

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
        case "effect" -> EffectArg.run(player, label, args);
        case "icon" -> IconArg.run(player, label, args);
        case "inventory", "inv" -> InventoryArg.run(player, label, args);
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
      arguments.add("effect");
      arguments.add("icon");
      arguments.add("inventory");

      StringUtil.copyPartialMatches(args[1], arguments, completion);
    } else if (args.length > 2) {
      completion =
          switch (args[1]) {
            case "effect" -> EffectArg.tabComplete(player, args);
            case "icon" -> IconArg.tabComplete(player, args);
            case "inventory", "inv" -> InventoryArg.tabComplete(player, args);
            default -> completion;
          };
    }

    Collections.sort(completion);
    return completion;
  }
}
