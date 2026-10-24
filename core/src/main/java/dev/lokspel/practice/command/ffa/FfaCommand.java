package dev.lokspel.practice.command.ffa;

import dev.lokspel.practice.command.ffa.arguments.HelpArg;
import dev.lokspel.practice.command.ffa.arguments.JoinArg;
import dev.lokspel.practice.command.ffa.arguments.KitArg;
import dev.lokspel.practice.command.ffa.arguments.LeaveArg;
import dev.lokspel.practice.command.ffa.arguments.ListArg;
import dev.lokspel.practice.command.ffa.arguments.SpectateArg;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class FfaCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return false;
    }

    if (args.length > 0) {
      switch (args[0]) {
        case "join" -> JoinArg.run(player, label, args);
        case "leave" -> LeaveArg.run(player);
        case "kit" -> KitArg.run(player);
        case "spec", "spectate" -> SpectateArg.run(player, label, args);
        case "list" -> ListArg.run(player);
        default -> HelpArg.run(player, label);
      }
    } else {
      JoinArg.run(player, label, args);
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
      arguments.add("join");
      arguments.add("leave");
      arguments.add("kit");
      arguments.add("list");

      StringUtil.copyPartialMatches(args[0], arguments, completion);
    } else {
      if (args[0].equalsIgnoreCase("join")) {
        completion = JoinArg.tabComplete(player, args);
      } else if (args[0].equalsIgnoreCase("spectate")) {
        completion = SpectateArg.tabComplete(player, args);
      }
    }

    Collections.sort(completion);
    return completion;
  }
}
