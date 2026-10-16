package dev.lokspel.practice.command.staff.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.interfaces.Event;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.spectator.SpectatorManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.interfaces.Spectatable;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class StopArg {
  private StopArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.staffmode.stop")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getStaff().getNoPermission());
      return;
    }

    if (args.length != 2) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getStaff()
              .getArguments()
              .getStop()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    Player target = Bukkit.getPlayer(args[1]);
    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
    if (target == null || targetProfile == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getStaff()
              .getArguments()
              .getStop()
              .getTargetOffline()
              .replace("%target%", args[1]));
      return;
    }

    if (player != target && target.hasPermission("ap.bypass.stop")) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getStaff()
              .getArguments()
              .getStop()
              .getTargetBypass()
              .replace("%target%", target.getName()));
      return;
    }

    ProfileStatus profileStatus = targetProfile.getStatus();
    switch (profileStatus) {
      case MATCH:
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(target);
        if (match == null) {
          return;
        }

        match.removePlayer(target, false);

        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getStop()
                .getMatch()
                .getPlayer()
                .replace("%target%", target.getName()));
        target.sendMessage(
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getStop()
                .getMatch()
                .getTarget()
                .replace("%player%", player.getName()));
        match.sendMessage(
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getStop()
                .getMatch()
                .getMatch()
                .replace("%target%", target.getName()),
            true);
        break;
      case EVENT:
        Event event = EventManager.getInstance().getEventByPlayer(target);
        if (event == null) {
          return;
        }

        event.removePlayer(target, false);

        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getStop()
                .getEvent()
                .getPlayer()
                .replace("%target%", target.getName()));
        target.sendMessage(
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getStop()
                .getEvent()
                .getTarget()
                .replace("%player%", player.getName()));
        event.sendMessage(
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getStop()
                .getEvent()
                .getEvent()
                .replace("%target%", target.getName()),
            true);
        break;
      case SPECTATE:
        Spectatable spectatable =
            SpectatorManager.getInstance().getSpectators().get(target.getUniqueId());
        if (spectatable != null) {
          spectatable.removeSpectator(target);

          Common.sendMmMessage(
              player,
              PracticeConfig.language()
                  .getCommand()
                  .getStaff()
                  .getArguments()
                  .getStop()
                  .getSpectate()
                  .getPlayer()
                  .replace("%target%", target.getName()));
          Common.sendMmMessage(
              target,
              PracticeConfig.language()
                  .getCommand()
                  .getStaff()
                  .getArguments()
                  .getStop()
                  .getSpectate()
                  .getTarget()
                  .replace("%player%", player.getName()));
        }
        break;

      default:
        break;
    }
  }

  public static void run(String label, String[] args) {
    if (args.length != 2) {
      Common.sendConsoleMmMessage(
          PracticeConfig.language()
              .getCommand()
              .getStaff()
              .getArguments()
              .getStop()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    Player target = Bukkit.getPlayer(args[1]);
    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
    if (target == null || targetProfile == null) {
      Common.sendConsoleMmMessage(
          PracticeConfig.language()
              .getCommand()
              .getStaff()
              .getArguments()
              .getStop()
              .getTargetOffline()
              .replace("%target%", args[1]));
      return;
    }

    ProfileStatus profileStatus = targetProfile.getStatus();
    switch (profileStatus) {
      case MATCH:
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(target);
        if (match == null) {
          return;
        }

        match.removePlayer(target, false);

        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getStop()
                .getMatch()
                .getPlayer()
                .replace("%target%", target.getName()));
        target.sendMessage(
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getStop()
                .getMatch()
                .getTarget()
                .replace("%player%", PracticeConfig.language().getConsoleName()));
        match.sendMessage(
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getStop()
                .getMatch()
                .getMatch()
                .replace("%target%", target.getName()),
            true);
        break;
      case EVENT:
        Event event = EventManager.getInstance().getEventByPlayer(target);
        if (event == null) {
          return;
        }

        event.removePlayer(target, false);

        Common.sendConsoleMmMessage(
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getStop()
                .getEvent()
                .getPlayer()
                .replace("%target%", target.getName()));
        target.sendMessage(
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getStop()
                .getEvent()
                .getTarget()
                .replace("%player%", PracticeConfig.language().getConsoleName()));
        event.sendMessage(
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getStop()
                .getEvent()
                .getEvent()
                .replace("%target%", target.getName()),
            true);
        break;
      case SPECTATE:
        Spectatable spectatable =
            SpectatorManager.getInstance().getSpectators().get(target.getUniqueId());
        if (spectatable != null) {
          spectatable.removeSpectator(target);

          Common.sendConsoleMmMessage(
              PracticeConfig.language()
                  .getCommand()
                  .getStaff()
                  .getArguments()
                  .getStop()
                  .getSpectate()
                  .getPlayer()
                  .replace("%target%", target.getName()));
          Common.sendMmMessage(
              target,
              PracticeConfig.language()
                  .getCommand()
                  .getStaff()
                  .getArguments()
                  .getStop()
                  .getSpectate()
                  .getTarget()
                  .replace("%player%", PracticeConfig.language().getConsoleName()));
        }
        break;

      default:
        break;
    }
  }

  public static List<String> tabComplete(Player player, String[] args) {
    List<String> arguments = new ArrayList<>();

    if (!player.hasPermission("ap.staffmode.stop")) {
      return arguments;
    }

    if (args.length == 2) {
      for (Player online : Bukkit.getOnlinePlayers()) {
        arguments.add(online.getName());
      }

      return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
    }

    return arguments;
  }
}
