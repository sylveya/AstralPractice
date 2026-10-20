package dev.lokspel.practice.command.practice.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.nametag.LobbyNametagUtil;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.format.NameFormatUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class NametagArg {

  private NametagArg() {}

  private static String joinArgs(String[] args, int start) {
    StringBuilder builder = new StringBuilder();
    for (int i = start; i < args.length; i++) {
      if (i != start) {
        builder.append(" ");
      }
      builder.append(args[i]);
    }
    return builder.toString();
  }

  public static void run(Player player, String label, String[] args) {
    if (!PracticeConfig.settings().getPlayer().getLobbyNametag().isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getNametag()
              .getNametagDisabled());
      return;
    }

    if (args.length >= 4
        && (args[1].equalsIgnoreCase("prefix")
            || args[1].equalsIgnoreCase("suffix")
            || args[1].equalsIgnoreCase("name"))) {
      if (!player.hasPermission("ap.practice.nametag.set")) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
        return;
      }

      Player target = Bukkit.getPlayer(args[2]);
      if (target == null) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getTargetOffline()
                .replace("%target%", args[2]));
        return;
      }

      if (player != target && target.hasPermission("ap.bypass.nametag")) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getTargetBypass()
                .replace("%target%", target.getName()));
        return;
      }

      Profile targetProfile = ProfileManager.getInstance().getProfile(target);
      if (args[1].equalsIgnoreCase("prefix")) {
        String prefix = joinArgs(args, 3);

        targetProfile.setPrefix(NameFormatUtil.parseConfiguredComponent(prefix));
        LobbyNametagUtil.setLobbyNametag(target, targetProfile);

        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getPrefixSet()
                .replace("%target%", target.getName())
                .replace("%prefix%", Common.mmToNormal(prefix)));

        Common.sendMmMessage(
            target,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getPlayerPrefixSet()
                .replace("%player%", player.getName())
                .replace("%prefix%", Common.mmToNormal(prefix)));
      } else if (args[1].equalsIgnoreCase("suffix")) {
        String suffix = joinArgs(args, 3);

        targetProfile.setSuffix(NameFormatUtil.parseConfiguredComponent(suffix));
        LobbyNametagUtil.setLobbyNametag(target, targetProfile);

        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getSuffixSet()
                .replace("%target%", target.getName())
                .replace("%suffix%", Common.mmToNormal(suffix)));

        Common.sendMmMessage(
            target,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getPlayerSuffixSet()
                .replace("%player%", player.getName())
                .replace("%suffix%", Common.mmToNormal(suffix)));
      } else if (args[1].equalsIgnoreCase("name")) {
        String nameTemplate = joinArgs(args, 3);

        if (player != target && !player.hasPermission("ap.practice.nametag.name.others")) {
          Common.sendMmMessage(
              player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
          return;
        }

        targetProfile.setNameTemplate(NameFormatUtil.normalizeTemplate(nameTemplate));
        LobbyNametagUtil.setLobbyNametag(target, targetProfile);

        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getNameSet()
                .replace("%target%", target.getName())
                .replace("%name%", Common.mmToNormal(nameTemplate)));

        Common.sendMmMessage(
            target,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getPlayerNameSet()
                .replace("%player%", player.getName())
                .replace("%name%", Common.mmToNormal(nameTemplate)));
      }
    } else if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
      if (!player.hasPermission("ap.practice.nametag.reset")) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
        return;
      }

      Player target = Bukkit.getPlayer(args[2]);
      if (target == null) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getTargetOffline()
                .replace("%target%", args[2]));
        return;
      }
      Profile targetProfile = ProfileManager.getInstance().getProfile(target);

      if (player != target && target.hasPermission("ap.bypass.nametag")) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getTargetBypass()
                .replace("%target%", target.getName()));
        return;
      }

      targetProfile.setPrefix(null);
      targetProfile.setSuffix(null);
      targetProfile.setNameTemplate(null);

      LobbyNametagUtil.setLobbyNametag(target, targetProfile);

      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getNametag()
              .getReloaded()
              .replace("%target%", target.getName()));
    } else {
      if (player.hasPermission("ap.practice.nametag.set")
          || player.hasPermission("ap.practice.nametag.reset")) {
        for (String line :
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getCommandHelp()) {
          Common.sendMmMessage(player, line.replace("%label%", label));
        }
      } else {
        Common.sendMmMessage(
            player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
      }
    }
  }

  public static void run(String label, String[] args) {
    if (!PracticeConfig.settings().getPlayer().getLobbyNametag().isEnabled()) {
      Common.sendConsoleMmMessage(
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getNametag()
              .getNametagDisabled());
      return;
    }

    if (args.length >= 4
        && (args[1].equalsIgnoreCase("prefix")
            || args[1].equalsIgnoreCase("suffix")
            || args[1].equalsIgnoreCase("name"))) {
      Player target = Bukkit.getPlayer(args[2]);
      if (target == null) {
        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getTargetOffline()
                .replace("%target%", args[2]));
        return;
      }

      Profile targetProfile = ProfileManager.getInstance().getProfile(target);
      if (args[1].equalsIgnoreCase("prefix")) {
        String prefix = joinArgs(args, 3);

        targetProfile.setPrefix(NameFormatUtil.parseConfiguredComponent(prefix));
        LobbyNametagUtil.setLobbyNametag(target, targetProfile);

        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getPrefixSet()
                .replace("%target%", target.getName())
                .replace("%prefix%", Common.mmToNormal(prefix)));

        Common.sendMmMessage(
            target,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getPlayerPrefixSet()
                .replace("%player%", PracticeConfig.language().getConsoleName())
                .replace("%prefix%", Common.mmToNormal(prefix)));
      } else if (args[1].equalsIgnoreCase("suffix")) {
        String suffix = joinArgs(args, 3);

        targetProfile.setSuffix(NameFormatUtil.parseConfiguredComponent(suffix));
        LobbyNametagUtil.setLobbyNametag(target, targetProfile);

        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getSuffixSet()
                .replace("%target%", target.getName())
                .replace("%suffix%", Common.mmToNormal(suffix)));

        Common.sendMmMessage(
            target,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getPlayerSuffixSet()
                .replace("%player%", PracticeConfig.language().getConsoleName())
                .replace("%suffix%", Common.mmToNormal(suffix)));
      } else if (args[1].equalsIgnoreCase("name")) {
        String nameTemplate = joinArgs(args, 3);

        targetProfile.setNameTemplate(NameFormatUtil.normalizeTemplate(nameTemplate));
        LobbyNametagUtil.setLobbyNametag(target, targetProfile);

        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getNameSet()
                .replace("%target%", target.getName())
                .replace("%name%", Common.mmToNormal(nameTemplate)));

        Common.sendMmMessage(
            target,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getPlayerNameSet()
                .replace("%player%", PracticeConfig.language().getConsoleName())
                .replace("%name%", Common.mmToNormal(nameTemplate)));
      }
    } else if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
      Player target = Bukkit.getPlayer(args[2]);
      if (target == null) {
        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getNametag()
                .getTargetOffline()
                .replace("%target%", args[2]));
        return;
      }
      Profile targetProfile = ProfileManager.getInstance().getProfile(target);

      targetProfile.setPrefix(null);
      targetProfile.setSuffix(null);
      targetProfile.setNameTemplate(null);

      LobbyNametagUtil.setLobbyNametag(target, targetProfile);

      Common.sendConsoleMmMessage(
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getNametag()
              .getReloaded()
              .replace("%target%", target.getName()));
    } else {
      for (String line :
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getNametag()
              .getCommandHelp()) {
        Common.sendConsoleMmMessage(line.replace("%label%", label));
      }
    }
  }

  public static List<String> tabComplete(Player player, String[] args) {
    List<String> arguments = new ArrayList<>();

    if (args.length == 2) {
      if (player.hasPermission("ap.practice.nametag.reset")) {
        arguments.add("reset");
      }
      if (player.hasPermission("ap.practice.nametag.set")) {
        arguments.add("prefix");
        arguments.add("suffix");
        arguments.add("name");
      }

      return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
    } else if (args.length == 3) {
      if ((args[1].equalsIgnoreCase("prefix")
              || args[1].equalsIgnoreCase("suffix")
              || args[1].equalsIgnoreCase("name"))
          && player.hasPermission("ap.practice.nametag.set")) {
        if (args[1].equalsIgnoreCase("name")
            && !player.hasPermission("ap.practice.nametag.name.others")) {
          arguments.add(player.getName());
        } else {
          for (Player online : Bukkit.getOnlinePlayers()) {
            arguments.add(online.getName());
          }
        }
      } else if (args[1].equalsIgnoreCase("reset")
          && player.hasPermission("ap.practice.nametag.reset")) {
        for (Player online : Bukkit.getOnlinePlayers()) {
          arguments.add(online.getName());
        }
      }

      return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
    }

    return arguments;
  }
}
