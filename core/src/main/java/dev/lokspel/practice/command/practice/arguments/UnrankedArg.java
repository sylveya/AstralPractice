package dev.lokspel.practice.command.practice.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.group.Group;
import dev.lokspel.practice.manager.server.ServerManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class UnrankedArg {

  private UnrankedArg() {}

  public static void run(Player player, String label, String[] args) {
    if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
      if (!player.hasPermission("ap.practice.unranked.default")) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
        return;
      }

      final Profile target =
          ProfileManager.getInstance()
              .getProfile(ServerManager.getInstance().resolvePlayer(args[2]));
      if (target == null) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnranked()
                .getTargetOffline()
                .replace("%target%", args[2]));
        return;
      }

      if (player != target.getPlayer()
          && target.getPlayer().isOp()
          && PracticeConfig.settings().getAdminSettings().isOpBypassUnrankedChange()) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnranked()
                .getTargetOp());
        return;
      }

      final Group group = target.getGroup();
      if (group != null) {
        target.setUnrankedLeft(group.getUnrankedLimit());

        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnranked()
                .getResetDailyLimit()
                .replace("%target%", target.getPlayer().getName())
                .replace("%dailyUnrankedLimit%", StringUtil.formatLimit(group.getUnrankedLimit())));
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnranked()
                .getNoGroup()
                .replace("%target%", target.getPlayer().getName()));
      }
    } else if (args.length == 4 && args[1].equalsIgnoreCase("add")) {
      if (!player.hasPermission("ap.practice.unranked.add")) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
        return;
      }

      final Profile target =
          ProfileManager.getInstance()
              .getProfile(ServerManager.getInstance().resolvePlayer(args[2]));
      if (target == null) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnranked()
                .getTargetOffline());
        return;
      }

      if (player != target.getPlayer()
          && target.getPlayer().isOp()
          && PracticeConfig.settings().getAdminSettings().isOpBypassUnrankedChange()) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnranked()
                .getTargetOp());
        return;
      }

      final int extraUnranked = Integer.parseInt(args[3]);
      if (extraUnranked <= 0) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnranked()
                .getInvalidNumber());
        return;
      }

      if (target.getUnrankedLeft() >= 0) {
        target.setUnrankedLeft(target.getUnrankedLeft() + extraUnranked);
      }

      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getUnranked()
              .getAddExtraUnranked()
              .replace("%extraUnranked%", String.valueOf(extraUnranked))
              .replace("%target%", target.getPlayer().getName())
              .replace("%newUnranked%", StringUtil.formatLimit(target.getUnrankedLeft())));
    } else {
      if (player.hasPermission("ap.practice.unranked.default")
          || player.hasPermission("ap.practice.unranked.add")) {
        for (String line :
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnranked()
                .getCommandHelp()) {
          Common.sendMmMessage(player, line.replace("%label%", label));
        }
      }
    }
  }

  public static void run(String label, String[] args) {
    if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
      final Profile target =
          ProfileManager.getInstance()
              .getProfile(ServerManager.getInstance().resolvePlayer(args[2]));
      if (target == null) {
        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnranked()
                .getTargetOffline());
        return;
      }

      final Group group = target.getGroup();
      if (group != null) {
        target.setUnrankedLeft(group.getUnrankedLimit());

        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnranked()
                .getResetDailyLimit()
                .replace("%target%", target.getPlayer().getName())
                .replace("%dailyUnrankedLimit%", StringUtil.formatLimit(group.getUnrankedLimit())));
      } else {
        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnranked()
                .getNoGroup()
                .replace("%target%", target.getPlayer().getName()));
      }
    } else if (args.length == 4 && args[1].equalsIgnoreCase("add")) {
      final Profile target =
          ProfileManager.getInstance()
              .getProfile(ServerManager.getInstance().resolvePlayer(args[2]));
      if (target == null) {
        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnranked()
                .getTargetOffline());
        return;
      }

      final int extraUnranked = Integer.parseInt(args[3]);
      if (extraUnranked <= 0) {
        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnranked()
                .getInvalidNumber());
        return;
      }

      if (target.getUnrankedLeft() >= 0) {
        target.setUnrankedLeft(target.getUnrankedLeft() + extraUnranked);
      }

      Common.sendConsoleMmMessage(
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getUnranked()
              .getAddExtraUnranked()
              .replace("%extraUnranked%", String.valueOf(extraUnranked))
              .replace("%target%", target.getPlayer().getName())
              .replace("%newUnranked%", StringUtil.formatLimit(target.getUnrankedLeft())));
    } else {
      for (String line :
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getUnranked()
              .getCommandHelp()) {
        Common.sendConsoleMmMessage(line.replace("%label%", label));
      }
    }
  }

  public static List<String> tabComplete(Player player, String[] args) {
    List<String> arguments = new ArrayList<>();

    if (args.length == 2) {
      if (player.hasPermission("ap.practice.unranked.default")) {
        arguments.add("reset");
      }
      if (player.hasPermission("ap.practice.unranked.add")) {
        arguments.add("add");
      }

      return org.bukkit.util.StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
    } else if (args.length == 3) {
      if (player.hasPermission("ap.practice.unranked.default")
          || player.hasPermission("ap.practice.unranked.add")) {
        for (Player online : Bukkit.getOnlinePlayers()) {
          arguments.add(online.getName());
        }
      }
    }

    return arguments;
  }
}
