package dev.lokspel.practice.command.singlecommands;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.gui.guis.leaderboard.LbSelectorGui;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.server.ServerManager;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class StatisticsCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return false;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (!player.hasPermission("ap.admin")) {
      switch (profile.getStatus()) {
        case MATCH, FFA, EVENT -> {
          Common.sendMmMessage(
              player, PracticeConfig.language().getCommand().getStatistics().getCantUse());
          return false;
        }
        default -> {}
      }
    }

    if (args.length == 0) {
      if (!player.hasPermission("ap.statistics")) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getCommand().getStatistics().getNoPermission());
        return false;
      }

      new LbSelectorGui(player, profile).open(player);
    } else if (args.length == 1) {
      if (!player.hasPermission("ap.statistics.other")) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getCommand().getStatistics().getNoPermission());
        return false;
      }

      Profile target =
          ProfileManager.getInstance()
              .getProfile(ServerManager.getInstance().resolvePlayer(args[0]));
      if (target == null) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getStatistics()
                .getTargetNotExists()
                .replace("%target%", args[0]));
        return false;
      }

      new LbSelectorGui(player, target).open(player);
    } else {
      for (String line : PracticeConfig.language().getCommand().getStatistics().getCommandHelp()) {
        Common.sendMmMessage(player, line.replace("%label%", label));
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

    if (!player.hasPermission("ap.statistics.other")) {
      return arguments;
    }

    if (args.length == 1) {
      for (Player online : Bukkit.getOnlinePlayers()) {
        Profile onlineProfile = ProfileManager.getInstance().getProfile(online);
        if (player != online && onlineProfile.isHideFromPlayers()) {
          continue;
        }

        arguments.add(online.getName());
      }

      StringUtil.copyPartialMatches(args[0], arguments, completion);
    }

    Collections.sort(completion);
    return completion;
  }
}
