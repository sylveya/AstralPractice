package dev.lokspel.practice.command.ladder;

import dev.lokspel.practice.command.ladder.arguments.CloneArg;
import dev.lokspel.practice.command.ladder.arguments.CreateArg;
import dev.lokspel.practice.command.ladder.arguments.DeleteArg;
import dev.lokspel.practice.command.ladder.arguments.FreezeArg;
import dev.lokspel.practice.command.ladder.arguments.HelpArg;
import dev.lokspel.practice.command.ladder.arguments.InfoArg;
import dev.lokspel.practice.command.ladder.arguments.LoadArg;
import dev.lokspel.practice.command.ladder.arguments.SetArg;
import dev.lokspel.practice.command.ladder.arguments.StopArg;
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

public class LadderCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return false;
    }

    if (args.length > 0) {
      switch (args[0]) {
        case "info":
          InfoArg.run(player, label, args);
          break;
        case "create":
          CreateArg.run(player, label, args);
          break;
        case "load":
          LoadArg.run(player, label, args);
          break;
        case "clone":
          CloneArg.run(player, label, args);
          break;
        case "delete":
          DeleteArg.run(player, label, args);
          break;
        case "set":
          SetArg.run(player, label, args);
          break;
        case "freeze":
          FreezeArg.run(player, label, args);
          break;
        case "stop":
          StopArg.run(player, label, args);
          break;
        default:
          HelpArg.run(player, label);
          break;
      }
    } else {
      HelpArg.run(player, label);
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
      if (player.hasPermission("ap.setup")) {
        arguments.add("info");
        arguments.add("create");
        arguments.add("load");
        arguments.add("clone");
        arguments.add("delete");
        arguments.add("set");
      }

      if (player.hasPermission("ap.ladder.stop")) {
        arguments.add("stop");
      }
      if (player.hasPermission("ap.ladder.freeze")) {
        arguments.add("freeze");
      }

      StringUtil.copyPartialMatches(args[0], arguments, completion);
    } else {
      switch (args[0]) {
        case "delete":
          completion = DeleteArg.tabComplete(player, args);
          break;
        case "freeze":
          completion = FreezeArg.tabComplete(player, args);
          break;
        case "stop":
          completion = StopArg.tabComplete(player, args);
          break;
        case "info":
          completion = InfoArg.tabComplete(player, args);
          break;
        case "load":
          completion = LoadArg.tabComplete(player, args);
          break;
        case "clone":
          completion = CloneArg.tabComplete(player, args);
          break;
        case "set":
          completion = SetArg.tabComplete(player, args);
          break;

        default:
          break;
      }
    }

    Collections.sort(completion);
    return completion;
  }
}
