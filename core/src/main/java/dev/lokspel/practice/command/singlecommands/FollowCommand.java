package dev.lokspel.practice.command.singlecommands;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
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

public class FollowCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return false;
    }

    var followConfig = PracticeConfig.language().getCommand().getFollow();

    if (!player.hasPermission("ap.follow")) {
      Common.sendMmMessage(player, followConfig.getNoPermission());
      return false;
    }

    if (args.length != 1) {
      Common.sendMmMessage(player, followConfig.getCommandHelp().replace("%label%", label));
      return false;
    }

    Player target = Bukkit.getPlayer(args[0]);
    if (target == null || !target.isOnline()) {
      Common.sendMmMessage(player, followConfig.getTargetOffline().replace("%target%", args[0]));
      return false;
    }

    if (target.equals(player)) {
      Common.sendMmMessage(player, followConfig.getCantSelf());
      return false;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return false;
    }

    if (target.equals(profile.getFollowTarget())) {
      profile.setFollowTarget(null);

      if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
        Match match = MatchManager.getInstance().getLiveMatchBySpectator(player);
        if (match != null) {
          match.removeSpectator(player);
        }
      }

      Common.sendMmMessage(player, followConfig.getStopped().replace("%target%", target.getName()));
      return true;
    }

    if (!profile.getStatus().equals(ProfileStatus.LOBBY)) {
      Common.sendMmMessage(player, followConfig.getCantFollow());
      return false;
    }

    if (profile.isStaffMode()) {
      Common.sendMmMessage(player, followConfig.getInStaffMode());
      return false;
    }

    if (profile.isParty()) {
      Common.sendMmMessage(player, followConfig.getInParty());
      return false;
    }

    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
    if (targetProfile == null) {
      Common.sendMmMessage(player, followConfig.getTargetOffline().replace("%target%", args[0]));
      return false;
    }

    if (!targetProfile.isAllowSpectate() && !player.hasPermission("ap.bypass.spectate")) {
      Common.sendMmMessage(
          player, followConfig.getNotAllowed().replace("%target%", target.getName()));
      return false;
    }

    profile.setFollowTarget(target);
    Common.sendMmMessage(player, followConfig.getStarted().replace("%target%", target.getName()));

    if (targetProfile.getStatus().equals(ProfileStatus.MATCH)) {
      Match match = MatchManager.getInstance().getLiveMatchByPlayer(target);
      if (match != null
          && (match.isAllowSpectators() || player.hasPermission("ap.bypass.spectate"))) {
        match.addSpectator(player, target, true, true);
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
    if (!player.hasPermission("ap.follow")) {
      return arguments;
    }

    if (args.length == 1) {
      for (Player online : Bukkit.getOnlinePlayers()) {
        if (player != online) {
          arguments.add(online.getName());
        }
      }

      StringUtil.copyPartialMatches(args[0], arguments, completion);
    }

    Collections.sort(completion);
    return completion;
  }
}
