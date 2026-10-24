package dev.lokspel.practice.command.staff.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.interfaces.Event;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.runnable.round.RoundEndRunnable;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.Nullable;

public final class ForceEndArg {
  private ForceEndArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.staffmode.forceend")) {
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
              .getForceEnd()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (!profile.isStaffMode()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getStaff()
              .getArguments()
              .getForceEnd()
              .getOnlyInStaffmode());
      return;
    }

    Player target = Bukkit.getPlayer(args[1]);
    if (target == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getStaff()
              .getArguments()
              .getForceEnd()
              .getTargetOffline()
              .replace("%target%", args[1]));
      return;
    }

    forceEndTarget(player, target);
  }

  public static void run(String label, String[] args) {
    if (args.length != 2) {
      Common.sendConsoleMmMessage(
          PracticeConfig.language()
              .getCommand()
              .getStaff()
              .getArguments()
              .getForceEnd()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    Player target = Bukkit.getPlayer(args[1]);
    if (target == null) {
      Common.sendConsoleMmMessage(
          PracticeConfig.language()
              .getCommand()
              .getStaff()
              .getArguments()
              .getForceEnd()
              .getTargetOffline()
              .replace("%target%", args[1]));
      return;
    }

    forceEndTarget(null, target);
  }

  private static void forceEndTarget(@Nullable Player player, Player target) {
    var forceEndCfg =
        PracticeConfig.language().getCommand().getStaff().getArguments().getForceEnd();
    Profile targetProfile = ProfileManager.getInstance().getProfile(target);

    if (targetProfile.getStatus() == ProfileStatus.MATCH) {
      Match match = MatchManager.getInstance().getLiveMatchByPlayer(target);
      RoundEndRunnable roundEndRunnable = match.getCurrentRound().getRoundEndRunnable();

      if (roundEndRunnable != null && roundEndRunnable.isRunning() && roundEndRunnable.isEnded()) {
        sendMessage(player, forceEndCfg.getMatch().getEnded());
        return;
      }

      if (player != null) {
        for (Player matchPlayer : match.getPlayers()) {
          if (player != matchPlayer && matchPlayer.hasPermission("ap.bypass.forceend")) {
            Common.sendMmMessage(player, forceEndCfg.getMatch().getCantEnd());
            return;
          }
        }
      }

      String senderName =
          player != null ? player.getName() : PracticeConfig.language().getConsoleName();
      match.sendMessage(
          forceEndCfg.getMatch().getMatchEndMsg().replace("%player%", senderName), false);
      sendMessage(
          player, forceEndCfg.getMatch().getPlayerEndMsg().replace("%target%", target.getName()));
      match.endMatch();

    } else if (targetProfile.getStatus() == ProfileStatus.EVENT) {
      Event event = EventManager.getInstance().getEventByPlayer(target);

      if (event.getEndRunnable() != null && event.getEndRunnable().isRunning()) {
        sendMessage(player, forceEndCfg.getEvent().getEnded());
        return;
      }

      if (player != null) {
        for (Player matchPlayer : event.getPlayers()) {
          if (player != matchPlayer && matchPlayer.hasPermission("ap.bypass.forceend")) {
            Common.sendMmMessage(player, forceEndCfg.getEvent().getCantEnd());
            return;
          }
        }
        event.forceEnd(player);
      } else {
        Common.sendConsoleMmMessage(forceEndCfg.getEvent().getPlayerEndMsg());
        event.sendMessage(
            forceEndCfg
                .getEvent()
                .getMatchEndMsg()
                .replace("%player%", PracticeConfig.language().getConsoleName()),
            true);
        event.forceEnd(null);
      }
    } else {
      sendMessage(player, forceEndCfg.getPlayerInactive());
    }
  }

  private static void sendMessage(@Nullable Player player, String message) {
    if (player != null) {
      Common.sendMmMessage(player, message);
    } else {
      Common.sendConsoleMmMessage(message);
    }
  }

  public static List<String> tabComplete(Player player, String[] args) {
    List<String> arguments = new ArrayList<>();
    if (!player.hasPermission("ap.staffmode.forceend")) {
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
