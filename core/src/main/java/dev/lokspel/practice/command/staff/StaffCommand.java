package dev.lokspel.practice.command.staff;

import dev.lokspel.practice.command.staff.arguments.ChatArg;
import dev.lokspel.practice.command.staff.arguments.EnableArg;
import dev.lokspel.practice.command.staff.arguments.FollowArg;
import dev.lokspel.practice.command.staff.arguments.ForceEndArg;
import dev.lokspel.practice.command.staff.arguments.HelpArg;
import dev.lokspel.practice.command.staff.arguments.StopArg;
import dev.lokspel.practice.command.staff.arguments.VanishArg;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class StaffCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (sender instanceof Player player) {
      if (args.length > 0) {
        switch (args[0]) {
          case "chat" -> ChatArg.run(player, args);
          case "enable" -> EnableArg.run(player, label, args);
          case "follow" -> FollowArg.run(player, label, args);
          case "forceend" -> ForceEndArg.run(player, label, args);
          case "stop" -> StopArg.run(player, label, args);
          case "vanish" -> VanishArg.run(player, label, args);
          default -> HelpArg.run(player, label);
        }
      } else {
        HelpArg.run(player, label);
      }
    } else if (sender instanceof ConsoleCommandSender) {
      if (args.length > 0) {
        switch (args[0]) {
          case "stop" -> StopArg.run(label, args);
          case "forceend" -> ForceEndArg.run(label, args);
          case "chat" -> ChatArg.run(args);
          default -> HelpArg.run(label);
        }
      } else {
        HelpArg.run(label);
      }
    }
    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String alias, String[] args) {
    List<String> arguments = new ArrayList<>();
    List<String> completion = new ArrayList<>();
    if (!(sender instanceof Player player)) {
      return arguments;
    }

    if (args.length == 1) {
      if (player.hasPermission("ap.staff")) {
        arguments.add("enable");
        arguments.add("vanish");
      }
      if (player.hasPermission("ap.staffmode.forceend")) {
        arguments.add("forceend");
      }
      if (player.hasPermission("ap.staffmode.stop")) {
        arguments.add("stop");
      }

      StringUtil.copyPartialMatches(args[0], arguments, completion);
    } else {
      completion =
          switch (args[0]) {
            case "forceend" -> ForceEndArg.tabComplete(player, args);
            case "stop" -> StopArg.tabComplete(player, args);
            default -> completion;
          };
    }

    Collections.sort(completion);
    return completion;
  }
}
