package dev.lokspel.practice.command.practice;

import dev.lokspel.practice.command.practice.arguments.ArenasArg;
import dev.lokspel.practice.command.practice.arguments.EloArg;
import dev.lokspel.practice.command.practice.arguments.Exp;
import dev.lokspel.practice.command.practice.arguments.GoldenHeadArg;
import dev.lokspel.practice.command.practice.arguments.HelpArg;
import dev.lokspel.practice.command.practice.arguments.HologramArg;
import dev.lokspel.practice.command.practice.arguments.InfoArg;
import dev.lokspel.practice.command.practice.arguments.LobbyArg;
import dev.lokspel.practice.command.practice.arguments.NametagArg;
import dev.lokspel.practice.command.practice.arguments.PresetArg;
import dev.lokspel.practice.command.practice.arguments.RankedArg;
import dev.lokspel.practice.command.practice.arguments.ReloadArg;
import dev.lokspel.practice.command.practice.arguments.RenameArg;
import dev.lokspel.practice.command.practice.arguments.ResetArg;
import dev.lokspel.practice.command.practice.arguments.TeleportArg;
import dev.lokspel.practice.command.practice.arguments.UnrankedArg;
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

public class PracticeCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (sender instanceof Player player) {
      if (args.length > 0) {
        switch (args[0]) {
          case "arenas" -> ArenasArg.run(player);
          case "lobby" -> LobbyArg.run(player, label, args);
          case "rename" -> RenameArg.run(player, label, args);
          case "preset" -> PresetArg.run(player, label, args);
          case "info" -> InfoArg.run(player, label, args);
          case "goldenhead" -> GoldenHeadArg.run(player, label, args);
          case "elo" -> EloArg.run(player, label, args);
          case "ranked" -> RankedArg.run(player, label, args);
          case "unranked" -> UnrankedArg.run(player, label, args);
          case "exp" -> Exp.run(player, label, args);
          case "reset" -> ResetArg.run(player, label, args);
          case "nametag" -> NametagArg.run(player, label, args);
          case "teleport" -> TeleportArg.run(player, label, args);
          case "hologram" -> HologramArg.run(player, label, args);
          case "reload" -> ReloadArg.run(player, label, args);
          default -> HelpArg.run(player, label);
        }
      } else {
        HelpArg.run(player, label);
      }
    } else if (sender instanceof ConsoleCommandSender) {
      if (args.length > 0) {
        switch (args[0]) {
          case "elo" -> EloArg.run(label, args);
          case "ranked" -> RankedArg.run(label, args);
          case "unranked" -> UnrankedArg.run(label, args);
          case "exp" -> Exp.run(label, args);
          case "reset" -> ResetArg.run(label, args);
          case "nametag" -> NametagArg.run(label, args);
          case "reload" -> ReloadArg.run(label, args);
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
      if (player.hasPermission("ap.practice.arenas")) {
        arguments.add("arenas");
      }
      if (player.hasPermission("ap.practice.lobby")) {
        arguments.add("lobby");
      }
      if (player.hasPermission("ap.practice.rename")) {
        arguments.add("rename");
      }
      if (player.hasPermission("ap.practice.preset")) {
        arguments.add("preset");
      }
      if (player.hasPermission("ap.practice.info")) {
        arguments.add("info");
      }
      if (player.hasPermission("ap.setup")) {
        arguments.add("goldenhead");
      }
      if (player.hasPermission("ap.practice.elo.default")
          || player.hasPermission("ap.practice.elo.specific")) {
        arguments.add("elo");
      }
      if (player.hasPermission("ap.practice.ranked.default")
          || player.hasPermission("ap.practice.ranked.add")) {
        arguments.add("ranked");
      }
      if (player.hasPermission("ap.practice.unranked.default")
          || player.hasPermission("ap.practice.unranked.add")) {
        arguments.add("unranked");
      }
      if (player.hasPermission("ap.practice.exp")) {
        arguments.add("exp");
      }
      if (player.hasPermission("ap.practice.reset")) {
        arguments.add("reset");
      }
      if (player.hasPermission("ap.practice.nametag.set")
          || player.hasPermission("ap.practice.nametag.reset")) {
        arguments.add("nametag");
      }
      if (player.hasPermission("ap.setup")) {
        arguments.add("teleport");
      }
      if (player.hasPermission("ap.setup")) {
        arguments.add("hologram");
      }
      if (player.hasPermission("ap.practice.reload")) {
        arguments.add("reload");
      }

      StringUtil.copyPartialMatches(args[0], arguments, completion);
    } else {
      completion =
          switch (args[0]) {
            case "lobby" -> LobbyArg.tabComplete(player, args);
            case "preset" -> PresetArg.tabComplete(player, args);
            case "info" -> InfoArg.tabComplete(player, args);
            case "elo" -> EloArg.tabComplete(player, args);
            case "ranked" -> RankedArg.tabComplete(player, args);
            case "unranked" -> UnrankedArg.tabComplete(player, args);
            case "exp" -> Exp.tabComplete(player, args);
            case "reset" -> ResetArg.tabComplete(player, args);
            case "nametag" -> NametagArg.tabComplete(player, args);
            case "teleport" -> TeleportArg.tabComplete(player, args);
            case "hologram" -> HologramArg.tabComplete(player, args);
            default -> completion;
          };
    }

    Collections.sort(completion);
    return completion;
  }
}
