package dev.lokspel.practice.command.staff.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.util.Common;
import org.bukkit.entity.Player;

public final class FollowArg {

  private FollowArg() {}

  public static void run(Player player, String label, String[] args) {
    var followConfig = PracticeConfig.language().getCommand().getStaff().getArguments().getFollow();
    if (!player.hasPermission("ap.staffmode.follow")) {
      Common.sendMmMessage(player, followConfig.getNoPermission());
      return;
    }

    Common.sendMmMessage(player, followConfig.getUnavailable());

    /*
     * if (args.length == 2)
     * {
     * Profile profile = ProfileManager.getInstance().getProfile(player);
     *
     * if (profile.isStaffMode())
     * {
     * Player target = Bukkit.getPlayer(args[1]);
     *
     * if (target != null)
     * {
     *
     * }
     * else
     * Common.sendMMMessage(player, "<red>Player is not online.");
     * }
     * else
     * Common.sendMMMessage(player, "<red>You can only use this command in staff mode.");
     * }
     * else
     * Common.sendMMMessage(player, "<red>/" + label + " follow <yellow><player>"));
     */
  }
}
