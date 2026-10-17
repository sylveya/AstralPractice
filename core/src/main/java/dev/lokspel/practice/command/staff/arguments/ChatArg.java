package dev.lokspel.practice.command.staff.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.playerutil.PlayerUtil;
import org.bukkit.entity.Player;

public final class ChatArg {

  private ChatArg() {}

  public static void run(Player player, String[] args) {
    if (!player.hasPermission("ap.staffmode.chat")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getStaff().getNoPermission());
      return;
    }

    if (!PracticeConfig.settings().getChat().getStaffChat().isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getStaff().getArguments().getChat().getDisabled());
      return;
    }

    if (args.length == 1) {
      Profile profile = ProfileManager.getInstance().getProfile(player);
      profile.setStaffChat(!profile.isStaffChat());

      if (profile.isStaffChat()) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getChat()
                .getChatEnabled());
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getStaff()
                .getArguments()
                .getChat()
                .getChatDisabled());
      }
    } else {
      String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
      PlayerUtil.sendStaffMessage(player, message);
    }
  }

  public static void run(String[] args) {
    if (!PracticeConfig.settings().getChat().getStaffChat().isEnabled()) {
      Common.sendConsoleMmMessage(
          PracticeConfig.language().getCommand().getStaff().getArguments().getChat().getDisabled());
      return;
    }

    if (args.length == 1) {
      Common.sendConsoleMmMessage("<red>Use: /staff chat <message>");
    } else {
      String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
      PlayerUtil.sendStaffMessage(null, message);
    }
  }
}
