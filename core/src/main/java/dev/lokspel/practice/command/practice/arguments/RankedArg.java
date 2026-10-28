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

public final class RankedArg {

  private RankedArg() {}

  public static void run(Player player, String label, String[] args) {
    if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
      if (!player.hasPermission("ap.practice.ranked.default")) {
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
                .getRanked()
                .getTargetNotFound()
                .replace("%target%", args[2]));
        return;
      }

      if (player != target.getPlayer()
          && target.getPlayer().isOp()
          && PracticeConfig.settings().getAdminSettings().isOpBypassRankedChange()) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getRanked()
                .getTargetOp());
        return;
      }

      final Group group = target.getGroup();
      if (group != null) {
        target.setRankedLeft(group.getRankedLimit());

        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getRanked()
                .getResetDailyLimit()
                .replace("%target%", target.getPlayer().getName())
                .replace("%dailyRankedLimit%", StringUtil.formatLimit(group.getRankedLimit())));
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getRanked()
                .getNoGroup()
                .replace("%target%", target.getPlayer().getName()));
      }
    } else if (args.length == 4 && args[1].equalsIgnoreCase("add")) {
      if (!player.hasPermission("ap.practice.ranked.add")) {
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
                .getRanked()
                .getTargetNotFound()
                .replace("%target%", args[2]));
        return;
      }

      if (player != target.getPlayer()
          && target.getPlayer().isOp()
          && PracticeConfig.settings().getAdminSettings().isOpBypassRankedChange()) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getRanked()
                .getTargetOp());
        return;
      }

      final int extraRanked = Integer.parseInt(args[3]);
      if (extraRanked <= 0) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getRanked()
                .getInvalidNumber());
        return;
      }

      if (target.getRankedLeft() >= 0) {
        target.setRankedLeft(target.getRankedLeft() + extraRanked);
      }

      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getRanked()
              .getAddExtraRanked()
              .replace("%extraRanked%", String.valueOf(extraRanked))
              .replace("%target%", target.getPlayer().getName())
              .replace("%newRanked%", StringUtil.formatLimit(target.getRankedLeft())));
    } else if (args.length >= 3 && args[1].equalsIgnoreCase("ban")) {
      if (!player.hasPermission("ap.practice.ranked.ban")) {
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
                .getRanked()
                .getTargetNotFound()
                .replace("%target%", args[2]));
        return;
      }

      if (target.getPlayer().isOp()) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getRanked()
                .getCantBanPlayer()
                .replace("%target%", target.getPlayer().getName()));
        return;
      }

      String reason = null;
      if (args.length > 3) {
        StringBuilder reasonBuilder = new StringBuilder();
        for (int i = 3; i < args.length; i++) {
          reasonBuilder.append(args[i]);
          if (i + 1 != args.length) {
            reasonBuilder.append(" ");
          }
        }
        reason = reasonBuilder.toString();
      }

      if (target.getRankedBan().ban(ProfileManager.getInstance().getProfile(player), reason)) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getBan()
                .getBannedFromRanked()
                .replace("%target%", target.getPlayer().getName())
                .replace(
                    "%reason%",
                    reason == null
                        ? PracticeConfig.language()
                            .getCommand()
                            .getPractice()
                            .getArguments()
                            .getBan()
                            .getNoReason()
                        : reason));

        if (target.getPlayer().isOnline()) {
          Common.sendMmMessage(
              target.getPlayer().getPlayer(),
              PracticeConfig.language()
                  .getCommand()
                  .getPractice()
                  .getArguments()
                  .getBan()
                  .getBannedFromRankedPlayer()
                  .replace("%banner%", player.getName())
                  .replace(
                      "%reason%",
                      reason == null
                          ? PracticeConfig.language()
                              .getCommand()
                              .getPractice()
                              .getArguments()
                              .getBan()
                              .getNoReason()
                          : reason));
        }
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getBan()
                .getPlayerAlreadyBanned()
                .replace("%target%", target.getPlayer().getName()));
      }
    } else if (args.length == 3 && args[1].equalsIgnoreCase("unban")) {
      if (!player.hasPermission("ap.practice.ranked.unban")) {
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
                .getRanked()
                .getTargetNotFound()
                .replace("%target%", args[2]));
        return;
      }

      if (target.getRankedBan().unban()) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnban()
                .getUnbannedFromRanked()
                .replace("%target%", target.getPlayer().getName()));
        if (target.getPlayer().isOnline()) {
          Common.sendMmMessage(
              target.getPlayer().getPlayer(),
              PracticeConfig.language()
                  .getCommand()
                  .getPractice()
                  .getArguments()
                  .getUnban()
                  .getUnbannedFromRankedPlayer()
                  .replace("%unbanner%", player.getName()));
        }
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getUnban()
                .getPlayerNotBanned()
                .replace("%target%", target.getPlayer().getName()));
      }
    } else {
      if (player.hasPermission("ap.practice.ranked.default")
          || player.hasPermission("ap.practice.ranked.add")) {
        for (String line :
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getRanked()
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
                .getRanked()
                .getTargetNotFound()
                .replace("%target%", args[2]));
        return;
      }

      final Group group = target.getGroup();
      if (group != null) {
        target.setRankedLeft(group.getRankedLimit());

        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getRanked()
                .getResetDailyLimit()
                .replace("%target%", target.getPlayer().getName())
                .replace("%dailyRankedLimit%", StringUtil.formatLimit(group.getRankedLimit())));
      } else {
        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getRanked()
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
                .getRanked()
                .getTargetNotFound()
                .replace("%target%", args[2]));
        return;
      }

      final int extraRanked = Integer.parseInt(args[3]);
      if (extraRanked <= 0) {
        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getRanked()
                .getInvalidNumber());
        return;
      }

      if (target.getRankedLeft() >= 0) {
        target.setRankedLeft(target.getRankedLeft() + extraRanked);
      }

      Common.sendConsoleMmMessage(
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getRanked()
              .getAddExtraRanked()
              .replace("%extraRanked%", String.valueOf(extraRanked))
              .replace("%target%", target.getPlayer().getName())
              .replace("%newRanked%", StringUtil.formatLimit(target.getRankedLeft())));
    } else {
      for (String line :
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getRanked()
              .getCommandHelp()) {
        Common.sendConsoleMmMessage(line.replace("%label%", label));
      }
    }
  }

  public static List<String> tabComplete(Player player, String[] args) {
    List<String> arguments = new ArrayList<>();

    if (args.length == 2) {
      if (player.hasPermission("ap.practice.ranked.default")) {
        arguments.add("reset");
      }
      if (player.hasPermission("ap.practice.ranked.add")) {
        arguments.add("add");
      }
      if (player.hasPermission("ap.practice.ranked.ban")) {
        arguments.add("ban");
      }
      if (player.hasPermission("ap.practice.ranked.unban")) {
        arguments.add("unban");
      }

      return org.bukkit.util.StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
    } else if (args.length == 3) {
      if (player.hasPermission("ap.practice.ranked.default")
          || player.hasPermission("ap.practice.ranked.add")
          || player.hasPermission("ap.practice.ranked.ban")
          || player.hasPermission("ap.practice.ranked.unban")) {
        for (Player online : Bukkit.getOnlinePlayers()) {
          arguments.add(online.getName());
        }
      }

      return org.bukkit.util.StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
    }

    return arguments;
  }
}
