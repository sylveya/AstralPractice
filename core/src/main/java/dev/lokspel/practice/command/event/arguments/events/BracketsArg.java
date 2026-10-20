package dev.lokspel.practice.command.event.arguments.events;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.enums.EventType;
import dev.lokspel.practice.manager.fight.event.events.duel.brackets.BracketsData;
import dev.lokspel.practice.manager.fight.event.util.EventUtil;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.kit.KitData;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class BracketsArg {

  private BracketsArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.setup")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getEvent().getNoPermission());
      return;
    }

    // Checking if the event is live, if it is, it will send a message to the player and return.
    if (EventManager.getInstance().isEventLive(EventType.BRACKETS)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getEvent()
              .getArguments()
              .getBrackets()
              .getEventLive());
      return;
    }

    BracketsData bracketsData =
        (BracketsData) EventManager.getInstance().getEventData().get(EventType.BRACKETS);
    // Checking if the player is trying to enable or disable the arena.
    if (args.length == 2 && args[1].equalsIgnoreCase("enable")) {
      if (!bracketsData.isEnabled()) {
        EventUtil.changeStatus(bracketsData, player);
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getEvent()
                .getArguments()
                .getBrackets()
                .getEventAlreadyEnabled());
      }

      return;
    } else if (args.length == 2 && args[1].equalsIgnoreCase("disable")) {
      if (bracketsData.isEnabled()) {
        EventUtil.changeStatus(bracketsData, player);
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getEvent()
                .getArguments()
                .getBrackets()
                .getEventAlreadyDisabled());
      }

      return;
    } else if (args.length == 2 && args[1].equalsIgnoreCase("help")) {
      sendHelpMsg(player, label);

      return;
    }

    // Checking if the arena is enabled, if it is, it will send a message to the player and return.
    if (bracketsData.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getEvent()
              .getArguments()
              .getBrackets()
              .getCantEdit());
      return;
    }

    // Setting the kit for the event.
    if (args.length == 2 && args[1].equalsIgnoreCase("setkit")) {
      KitData kitData = bracketsData.getKitData();
      kitData.setKitData(player, true);

      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getEvent()
              .getArguments()
              .getBrackets()
              .getKitSet());
    } else {
      sendHelpMsg(player, label);
    }
  }

  private static void sendHelpMsg(Player player, String label) {
    for (String line :
        PracticeConfig.language()
            .getCommand()
            .getEvent()
            .getArguments()
            .getBrackets()
            .getCommandHelp()) {
      Common.sendMmMessage(player, line.replace("%label%", label));
    }
  }

  public static List<String> tabComplete(Player player, String[] args) {
    List<String> arguments = new ArrayList<>();

    if (!player.hasPermission("ap.setup")) {
      return arguments;
    }

    if (args.length == 2) {
      arguments.add("setkit");
      arguments.add("enable");
      arguments.add("disable");

      return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
    }

    return arguments;
  }
}
