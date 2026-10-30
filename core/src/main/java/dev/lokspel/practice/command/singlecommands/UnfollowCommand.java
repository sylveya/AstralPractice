package dev.lokspel.practice.command.singlecommands;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.util.Common;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class UnfollowCommand implements CommandExecutor, TabCompleter {

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

    Profile profile = ProfileManager.getInstance().getProfile(player);
    Player target = profile != null ? profile.getFollowTarget() : null;
    if (target == null) {
      Common.sendMmMessage(player, followConfig.getNotFollowing());
      return false;
    }

    String targetName = target.getName();
    profile.setFollowTarget(null);

    if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
      Match match = MatchManager.getInstance().getLiveMatchBySpectator(player);
      if (match != null) {
        match.removeSpectator(player);
      }
    }

    Common.sendMmMessage(player, followConfig.getStopped().replace("%target%", targetName));
    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String alias, String[] args) {
    return Collections.emptyList();
  }
}
