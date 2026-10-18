package dev.lokspel.practice.command.singlecommands;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.util.LobbyNametagUtil;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.NameFormatUtil;
import dev.lokspel.practice.util.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class NickCommand implements CommandExecutor, TabCompleter {

  private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER =
      PlainTextComponentSerializer.plainText();
  private static final String DIFFERENT_NAME_PERMISSION = "ap.nick.different-name";

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

  private static void applyNameTemplate(Player target, String nameTemplate) {
    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
    targetProfile.setNameTemplate(NameFormatUtil.normalizeTemplate(nameTemplate));
    LobbyNametagUtil.setLobbyNametag(target, targetProfile);
  }

  private static boolean isTemplateKeepingRealName(Player player, String rawTemplate) {
    return getPlainName(rawTemplate, player.getName()).equals(player.getName());
  }

  private static String renderNamePreview(Player target, String rawTemplate) {
    String normalizedTemplate = NameFormatUtil.normalizeTemplate(rawTemplate);
    return AstralPractice.getMiniMessage()
        .serialize(NameFormatUtil.renderTemplate(normalizedTemplate, target.getName()));
  }

  private static String getPlainName(String rawTemplate, String playerName) {
    String normalizedTemplate = NameFormatUtil.normalizeTemplate(rawTemplate);
    return PLAIN_TEXT_SERIALIZER
        .serialize(NameFormatUtil.renderTemplate(normalizedTemplate, playerName))
        .trim();
  }

  private static boolean isValidNickname(String rawTemplate, String playerName) {
    String plainName = getPlainName(rawTemplate, playerName);
    return Pattern.compile(
            PracticeConfig.settings().getPlayer().getNickname().getValidationPattern())
        .matcher(plainName)
        .matches();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return false;
    }

    if (!PracticeConfig.settings().getPlayer().getLobbyNametag().isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getNametag()
              .getNametagDisabled());
      return false;
    }

    if (!player.hasPermission("ap.nick.use")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
      return false;
    }

    if (args.length == 0) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getNick().getHelp().replace("%label%", label));
      return false;
    }

    if (args[0].equalsIgnoreCase("reset")) {
      if (args.length == 1) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        profile.setNameTemplate(null);
        LobbyNametagUtil.setLobbyNametag(player, profile);
        Common.sendMmMessage(
            player, PracticeConfig.language().getCommand().getNick().getResetSelf());
        return true;
      }

      if (!player.hasPermission("ap.nick.reset.others")) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
        return false;
      }

      Player target = Bukkit.getPlayer(args[1]);
      if (target == null) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getNick()
                .getTargetOffline()
                .replace("%target%", args[1]));
        return false;
      }

      Profile targetProfile = ProfileManager.getInstance().getProfile(target);
      targetProfile.setNameTemplate(null);
      LobbyNametagUtil.setLobbyNametag(target, targetProfile);

      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getNick()
              .getResetOther()
              .replace("%target%", target.getName()));
      if (!target.equals(player)) {
        Common.sendMmMessage(
            target,
            PracticeConfig.language()
                .getCommand()
                .getNick()
                .getResetedBy()
                .replace("%player%", player.getName()));
      }
      return true;
    }

    Player target = player;
    int templateStartIndex = 0;

    if (args.length >= 2 && player.hasPermission("ap.nick.others")) {
      Player selectedTarget = Bukkit.getPlayer(args[0]);
      if (selectedTarget != null) {
        target = selectedTarget;
        templateStartIndex = 1;
      }
    }

    String nameTemplate = joinArgs(args, templateStartIndex).trim();
    if (nameTemplate.isEmpty()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getNick().getHelp().replace("%label%", label));
      return false;
    }

    if (StringUtil.containsBlockedFormatting(nameTemplate)) {
      String msg = PracticeConfig.language().getCommand().getNick().getBlockedFormatting();
      if (msg == null || msg.isEmpty()) {
        msg = PracticeConfig.language().getCommand().getNick().getInvalidName();
      }
      Common.sendMmMessage(player, msg);
      return false;
    }

    if (!player.hasPermission(DIFFERENT_NAME_PERMISSION)
        && !isTemplateKeepingRealName(target, nameTemplate)) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getNick().getDifferentNameNoPerm());
      return false;
    }

    if (!isValidNickname(nameTemplate, target.getName())) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getNick().getInvalidName());
      return false;
    }

    applyNameTemplate(target, nameTemplate);
    String formattedNamePreview = renderNamePreview(target, nameTemplate);

    if (target.equals(player)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getNick()
              .getSetSelf()
              .replace("%name%", formattedNamePreview));
    } else {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getNick()
              .getSetOther()
              .replace("%target%", target.getName())
              .replace("%name%", formattedNamePreview));
      Common.sendMmMessage(
          target,
          PracticeConfig.language()
              .getCommand()
              .getNick()
              .getSetBy()
              .replace("%player%", player.getName())
              .replace("%name%", formattedNamePreview));
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
    if (!player.hasPermission("ap.nick.use")) {
      return arguments;
    }

    if (args.length == 1) {
      arguments.add("reset");

      if (player.hasPermission("ap.nick.others") || player.hasPermission("ap.nick.reset.others")) {
        for (Player online : Bukkit.getOnlinePlayers()) {
          arguments.add(online.getName());
        }
      }
      org.bukkit.util.StringUtil.copyPartialMatches(args[0], arguments, completion);
    } else if (args.length == 2
        && args[0].equalsIgnoreCase("reset")
        && player.hasPermission("ap.nick.reset.others")) {
      for (Player online : Bukkit.getOnlinePlayers()) {
        arguments.add(online.getName());
      }
      org.bukkit.util.StringUtil.copyPartialMatches(args[1], arguments, completion);
    }

    Collections.sort(completion);
    return completion;
  }
}
