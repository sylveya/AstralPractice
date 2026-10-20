package dev.lokspel.practice.command.privatemessage;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.format.ChatFormatUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class MessageCommand extends PrivateMessageCommand {

  private static final Map<UUID, UUID> latestMessage = new ConcurrentHashMap<>();

  public static void setLatestMessage(Player p1, Player p2) {
    if (p1 == null || p2 == null) {
      return;
    }
    latestMessage.put(p1.getUniqueId(), p2.getUniqueId());
    latestMessage.put(p2.getUniqueId(), p1.getUniqueId());
  }

  public static Player getLatestMessageTarget(Player player) {
    if (player == null) {
      return null;
    }
    UUID targetUuid = latestMessage.get(player.getUniqueId());
    if (targetUuid == null) {
      return null;
    }
    return Bukkit.getPlayer(targetUuid);
  }

  public static boolean hasLatestMessage(Player player) {
    if (player == null) {
      return false;
    }
    return latestMessage.containsKey(player.getUniqueId());
  }

  public static void clearPlayer(Player player) {
    if (player == null) {
      return;
    }
    clearPlayer(player.getUniqueId());
  }

  public static void clearPlayer(UUID uuid) {
    if (uuid == null) {
      return;
    }
    latestMessage.remove(uuid);
    latestMessage.values().removeIf(targetUuid -> targetUuid.equals(uuid));
  }

  public MessageCommand() {
    super("message", new String[] {"m", "msg", "tell", "whisper", "w"});
  }

  @Override
  public void executeCommand(CommandSender sender, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (args.length < 2) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPrivateMessage()
              .getMessage()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    if (!profile.isPrivateMessages()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getPrivateMessage().getMessage().getCantSend());
      return;
    }

    Player target = Bukkit.getPlayer(args[0]);
    if (target == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPrivateMessage()
              .getMessage()
              .getTargetOffline()
              .replace("%target%", args[0]));
      return;
    }

    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
    if (player != target
        && targetProfile.isHideFromPlayers()
        && !player.hasPermission("ap.staffmode.see")) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPrivateMessage()
              .getMessage()
              .getTargetHide()
              .replace("%target%", target.getName()));
      return;
    }

    if (!targetProfile.isPrivateMessages() && !player.hasPermission("ap.bypass.privatemessage")) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPrivateMessage()
              .getMessage()
              .getCantSend2()
              .replace("%target%", target.getName()));
      return;
    }

    String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

    String senderFormat =
        PracticeConfig.language()
            .getCommand()
            .getPrivateMessage()
            .getMessage()
            .getSenderMessage()
            .replace("%sender%", player.getName())
            .replace("%receiver%", target.getName());
    String receiverFormat =
        PracticeConfig.language()
            .getCommand()
            .getPrivateMessage()
            .getMessage()
            .getReceiverMessage()
            .replace("%sender%", player.getName())
            .replace("%receiver%", target.getName());

    Common.send(player, ChatFormatUtil.formatChatMessage(player, senderFormat, message));
    if (!player.equals(target)) {
      Common.send(target, ChatFormatUtil.formatChatMessage(target, receiverFormat, message));
    }

    setLatestMessage(target, player);
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, String[] args) {
    List<String> results = new ArrayList<>();
    if (!(sender instanceof Player player)) {
      return results;
    }

    if (args.length == 1) {
      List<String> onlineNames = new ArrayList<>();

      for (Player online : Bukkit.getOnlinePlayers()) {
        Profile onlineProfile = ProfileManager.getInstance().getProfile(online);
        if (player != online
            && (!onlineProfile.isHideFromPlayers() || player.hasPermission("ap.staffmode.see"))
            && (onlineProfile.isPrivateMessages()
                || player.hasPermission("ap.bypass.privatemessage"))) {
          onlineNames.add(online.getName());
        }
      }

      return StringUtil.copyPartialMatches(args[0], onlineNames, new ArrayList<>());
    }

    return results;
  }
}
