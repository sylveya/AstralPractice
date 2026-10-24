package dev.lokspel.practice.command.event;

import dev.lokspel.practice.command.event.arguments.HelpArg;
import dev.lokspel.practice.command.event.arguments.HostArg;
import dev.lokspel.practice.command.event.arguments.JoinArg;
import dev.lokspel.practice.command.event.arguments.StopArg;
import dev.lokspel.practice.command.event.arguments.events.BracketsArg;
import dev.lokspel.practice.command.event.arguments.events.JuggernautArg;
import dev.lokspel.practice.command.event.arguments.events.LmsArg;
import dev.lokspel.practice.command.event.arguments.events.OitcArg;
import dev.lokspel.practice.command.event.arguments.events.SpleggArg;
import dev.lokspel.practice.command.event.arguments.events.SumoArg;
import dev.lokspel.practice.command.event.arguments.events.TntTagArg;
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

public class EventCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return false;
    }

    if (args.length >= 1) {
      switch (args[0]) {
        case "host" -> HostArg.run(player, label, args);
        case "join" -> JoinArg.run(player, label, args);
        case "stop" -> StopArg.run(player, label, args);
        case "lms" -> LmsArg.run(player, label, args);
        case "oitc" -> OitcArg.run(player, label, args);
        case "tnttag" -> TntTagArg.run(player, label, args);
        case "brackets" -> BracketsArg.run(player, label, args);
        case "sumo" -> SumoArg.run(player, label, args);
        case "splegg" -> SpleggArg.run(player, label, args);
        case "jn", "juggernaut" -> JuggernautArg.run(player, label, args);
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
      if (player.hasPermission("ap.event.host")) {
        arguments.add("host");
      }
      if (player.hasPermission("ap.event.join")) {
        arguments.add("join");
      }
      if (player.hasPermission("ap.event.stop.collecting")
          || player.hasPermission("ap.event.stop.live")) {
        arguments.add("stop");
      }
      if (player.hasPermission("ap.setup")) {
        arguments.add("brackets");
        arguments.add("juggernaut");
        arguments.add("lms");
        arguments.add("oitc");
        arguments.add("splegg");
        arguments.add("sumo");
        arguments.add("tnttag");
      }

      StringUtil.copyPartialMatches(args[0], arguments, completion);
    } else {
      completion =
          switch (args[0]) {
            case "stop" -> StopArg.tabComplete(player, args);
            case "brackets" -> BracketsArg.tabComplete(player, args);
            case "jn", "juggernaut" -> JuggernautArg.tabComplete(player, args);
            case "lms" -> LmsArg.tabComplete(player, args);
            case "oitc" -> OitcArg.tabComplete(player, args);
            case "splegg" -> SpleggArg.tabComplete(player, args);
            case "sumo" -> SumoArg.tabComplete(player, args);
            case "tnttag" -> TntTagArg.tabComplete(player, args);
            default -> completion;
          };
    }

    Collections.sort(completion);
    return completion;
  }
}
