package dev.lokspel.practice.command.singlecommands;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.guis.queue.customkit.ChooseQueueTypeGui;
import dev.lokspel.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.queue.CustomKitQueueManager;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class CustomQueueCommand implements CommandExecutor, TabCompleter {

  private static final List<String> SUBCOMMANDS = List.of("join", "host", "leave", "open");

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return true;
    }

    if (!player.hasPermission("ap.playerkit.queue.use")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getQueues().getCustom().getNoPermission());
      return true;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null
        || profile.getStatus() != ProfileStatus.LOBBY
        || profile.isStaffMode()
        || profile.isParty()) {
      Common.sendMmMessage(player, PracticeConfig.language().getCantUseCommand());
      return true;
    }

    if (profile.getFollowTarget() != null) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getFollow().getCantQueue());
      return true;
    }

    if (args.length == 0 || args[0].equalsIgnoreCase("open") || args[0].equalsIgnoreCase("gui")) {
      openQueueGui(player);
      return true;
    }

    String subcommand = args[0].toLowerCase(Locale.ROOT);
    CustomKitQueueManager queueManager = CustomKitQueueManager.getInstance();

    switch (subcommand) {
      case "join", "search" -> {
        if (queueManager.getJoinSearch(player) != null) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language().getCommand().getQueues().getCustom().getAlreadySearching());
          return true;
        }

        if (queueManager.getHostedQueue(player) != null) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language().getCommand().getQueues().getCustom().getAlreadyHosting());
          return true;
        }

        queueManager.startJoinSearch(player);
        return true;
      }
      case "host" -> {
        if (!player.hasPermission("ap.playerkit.queue.host")) {
          Common.sendMmMessage(
              player, PracticeConfig.language().getQueues().getCustom().getNoPermission());
          return true;
        }

        if (args.length < 2) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language().getCommand().getQueues().getCustom().getHostUsage());
          return true;
        }

        if (queueManager.getHostedQueue(player) != null) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language().getCommand().getQueues().getCustom().getAlreadyHosting());
          return true;
        }

        String kitName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        CustomLadder customLadder = findHostableKit(profile, kitName);
        if (customLadder == null) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language()
                  .getCommand()
                  .getQueues()
                  .getCustom()
                  .getKitNotFound()
                  .replace("%kit%", kitName));
          return true;
        }

        queueManager.hostQueue(player, customLadder);
        return true;
      }
      case "leave", "cancel" -> {
        boolean cancelledSearch = queueManager.cancelJoinSearch(player, true, true);
        boolean cancelledHost = queueManager.cancelHostedQueue(player, true, true);
        if (!cancelledSearch && !cancelledHost) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language().getCommand().getQueues().getCustom().getNotInCustomQueue());
        }
        return true;
      }
      default -> {
        Common.sendMmMessage(
            player, PracticeConfig.language().getCommand().getQueues().getCustom().getHelp());
        return true;
      }
    }
  }

  private void openQueueGui(Player player) {
    Gui gui = GuiManager.getInstance().searchGui(GuiType.Queue_CustomKitChooseType);
    if (gui instanceof ChooseQueueTypeGui chooseQueueTypeGui) {
      chooseQueueTypeGui.openFor(player, GuiType.Queue_Unranked);
    } else if (gui != null) {
      gui.open(player);
    }
  }

  private CustomLadder findHostableKit(Profile profile, String inputName) {
    String normalizedInput = normalizeKitName(inputName);
    for (CustomLadder customLadder : CustomKitQueueManager.getInstance().getHostableKits(profile)) {
      String displayName = normalizeKitName(customLadder.getDisplayName());
      String internalName = normalizeKitName(customLadder.getName());
      if (normalizedInput.equals(displayName) || normalizedInput.equals(internalName)) {
        return customLadder;
      }
    }
    return null;
  }

  private String normalizeKitName(String input) {
    if (input == null) {
      return "";
    }

    String stripped = Common.stripLegacyColor(input).replaceAll("<[^>]+>", "");
    return stripped.replace("_", "").replace(" ", "").toLowerCase(Locale.ROOT);
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player) || !player.hasPermission("ap.playerkit.queue.use")) {
      return Collections.emptyList();
    }

    List<String> completion = new ArrayList<>();
    if (args.length == 1) {
      StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, completion);
      Collections.sort(completion);
      return completion;
    }

    if (args.length >= 2
        && args[0].equalsIgnoreCase("host")
        && player.hasPermission("ap.playerkit.queue.host")) {
      Profile profile = ProfileManager.getInstance().getProfile(player);
      if (profile == null) {
        return Collections.emptyList();
      }

      List<String> hostableKits = new ArrayList<>();
      for (CustomLadder customLadder :
          CustomKitQueueManager.getInstance().getHostableKits(profile)) {
        hostableKits.add(customLadder.getDisplayName().replace(' ', '_'));
      }

      StringUtil.copyPartialMatches(args[args.length - 1], hostableKits, completion);
      Collections.sort(completion);
      return completion;
    }

    return Collections.emptyList();
  }
}
