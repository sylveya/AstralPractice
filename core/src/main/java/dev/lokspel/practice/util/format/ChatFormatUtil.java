package dev.lokspel.practice.util.format;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.group.Group;
import dev.lokspel.practice.util.PapiUtil;
import dev.lokspel.practice.util.StringUtil;
import dev.lokspel.practice.util.startup.SoftDependUtil;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ChatFormatUtil {

  public static final String MESSAGE_PLACEHOLDER = "<ap-message>";

  private ChatFormatUtil() {}

  public static String buildPartyChatMessage(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);

    String playerName =
        profile != null
            ? AstralPractice.getMiniMessage()
                .serialize(NameFormatUtil.resolveFullName(profile, player.getName()))
            : player.getName();

    return StringUtil.normalizeSpaces(
        PracticeConfig.language()
            .getGeneralChat()
            .getPartyChat()
            .replace("%%player%%", playerName)
            .replace("%player%", playerName));
  }

  public static String buildStaffChatMessage(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);

    String playerName =
        profile != null
            ? AstralPractice.getMiniMessage()
                .serialize(NameFormatUtil.resolveFullName(profile, player.getName()))
            : player.getName();

    return StringUtil.normalizeSpaces(
        PracticeConfig.language()
            .getGeneralChat()
            .getStaffChat()
            .replace("%%player%%", playerName)
            .replace("%player%", playerName));
  }

  public static String buildServerChatMessage(Profile profile, Player player) {
    String format;

    if (PracticeConfig.settings().getPlayer().getGroupChat().isEnabled()) {
      Group group = profile.getGroup();

      if (group != null && group.getChatFormat() != null) {
        format = group.getChatFormat();
      } else {
        format = PracticeConfig.language().getGeneralChat().getServerChat();
      }
    } else {
      format = PracticeConfig.language().getGeneralChat().getServerChat();
    }

    String decoratedPlayer =
        AstralPractice.getMiniMessage()
            .serialize(NameFormatUtil.resolveFullName(profile, player.getName()));

    return StringUtil.normalizeSpaces(
        format.replace("%%player%%", decoratedPlayer).replace("%player%", decoratedPlayer));
  }

  public static Component formatChatMessage(
      CommandSender viewer, String formatString, Component message) {
    if (formatString == null) {
      return message != null ? message : Component.empty();
    }

    String format =
        formatString
            .replace("%%message%%", MESSAGE_PLACEHOLDER)
            .replace("%message%", MESSAGE_PLACEHOLDER);

    Component component;
    if (SoftDependUtil.isPAPI_ENABLED && viewer instanceof Player viewerPlayer) {
      component = PapiUtil.format(viewerPlayer, format);
    } else {
      component =
          AstralPractice.getMiniMessage().deserialize(StringUtil.legacyToMiniMessage(format));
    }

    return component.replaceText(
        TextReplacementConfig.builder()
            .matchLiteral(MESSAGE_PLACEHOLDER)
            .replacement(message != null ? message : Component.empty())
            .build());
  }

  public static Component formatChatMessage(
      CommandSender viewer, String formatString, String rawMessage) {
    return formatChatMessage(
        viewer, formatString, Component.text(rawMessage != null ? rawMessage : ""));
  }

  public static List<Player> getStaffRecipients() {
    List<Player> staff = new ArrayList<>();

    for (Player online : Bukkit.getOnlinePlayers()) {
      if (online.hasPermission("ap.staffmode.chat")) {
        staff.add(online);
      }
    }

    return staff;
  }
}
