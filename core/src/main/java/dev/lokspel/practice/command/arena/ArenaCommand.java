package dev.lokspel.practice.command.arena;

import dev.lokspel.practice.command.arena.arguments.CreateArg;
import dev.lokspel.practice.command.arena.arguments.DeleteArg;
import dev.lokspel.practice.command.arena.arguments.DisableArg;
import dev.lokspel.practice.command.arena.arguments.EnableArg;
import dev.lokspel.practice.command.arena.arguments.FreezeArg;
import dev.lokspel.practice.command.arena.arguments.HelpArg;
import dev.lokspel.practice.command.arena.arguments.InfoArg;
import dev.lokspel.practice.command.arena.arguments.SetArg;
import dev.lokspel.practice.command.arena.arguments.SetupArg;
import dev.lokspel.practice.command.arena.arguments.StopArg;
import dev.lokspel.practice.command.arena.arguments.TeleportArg;
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

public class ArenaCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return false;
    }

    if (args.length > 0) {
      switch (args[0]) {
        case "info" -> InfoArg.run(player, label, args);
        case "create" -> CreateArg.run(player, label, args);
        case "setup" -> SetupArg.run(player, label, args);
        case "delete" -> DeleteArg.run(player, label, args);
        case "set" -> SetArg.run(player, label, args);
        case "teleport" -> TeleportArg.run(player, label, args);
        case "freeze" -> FreezeArg.run(player, label, args);
        case "enable" -> EnableArg.run(player, label, args);
        case "disable" -> DisableArg.run(player, label, args);
        case "stop" -> StopArg.run(player, label, args);
        default -> HelpArg.run(player, label);
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
        arguments.add("create");
        arguments.add("setup");
        arguments.add("delete");
        arguments.add("info");
        arguments.add("set");
        arguments.add("teleport");
        arguments.add("enable");
        arguments.add("disable");
      }

      if (player.hasPermission("ap.arena.stop")) {
        arguments.add("stop");
      }
      if (player.hasPermission("ap.arena.freeze")) {
        arguments.add("freeze");
      }

      StringUtil.copyPartialMatches(args[0], arguments, completion);
    } else {
      completion =
          switch (args[0]) {
            case "delete" -> DeleteArg.tabComplete(player, args);
            case "freeze" -> FreezeArg.tabComplete(player, args);
            case "enable" -> EnableArg.tabComplete(player, args);
            case "setup" -> SetupArg.tabComplete(player, args);
            case "disable" -> DisableArg.tabComplete(player, args);
            case "info" -> InfoArg.tabComplete(player, args);
            case "set" -> SetArg.tabComplete(player, args);
            case "stop" -> StopArg.tabComplete(player, args);
            case "teleport" -> TeleportArg.tabComplete(player, args);
            default -> completion;
          };
    }

    Collections.sort(completion);
    return completion;
  }
}
