package dev.lokspel.practice.command.privatemessage;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReplyCommand extends PrivateMessageCommand {

  public ReplyCommand() {
    super("reply", new String[] {"r", "re"});
  }

  @Override
  public void executeCommand(CommandSender sender, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return;
    }

    if (args.length < 1) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPrivateMessage()
              .getReply()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    if (!MessageCommand.hasLatestMessage(player)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getPrivateMessage().getReply().getNoMessage());
      return;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (!profile.isPrivateMessages()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getPrivateMessage().getReply().getCantReply());
      return;
    }

    Player target = MessageCommand.getLatestMessageTarget(player);
    if (target == null || !target.isOnline()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPrivateMessage()
              .getReply()
              .getPlayerLeft()
              .replace("%target%", target != null ? target.getName() : ""));
      return;
    }

    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
    if (!targetProfile.isPrivateMessages() && !player.hasPermission("ap.bypass.privatemessage")) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPrivateMessage()
              .getReply()
              .getCantReply2()
              .replace("%target%", target.getName()));
      return;
    }

    StringBuilder message = new StringBuilder();
    for (String arg : args) {
      message.append(arg).append(" ");
    }

    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getPrivateMessage()
            .getReply()
            .getSenderMessage()
            .replace("%sender%", player.getName())
            .replace("%receiver%", target.getName())
            .replace("%message%", message.toString()));
    Common.sendMmMessage(
        target,
        PracticeConfig.language()
            .getCommand()
            .getPrivateMessage()
            .getReply()
            .getReceiverMessage()
            .replace("%sender%", player.getName())
            .replace("%receiver%", target.getName())
            .replace("%message%", message.toString()));

    MessageCommand.setLatestMessage(target, player);
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, String[] args) {
    return null;
  }
}
