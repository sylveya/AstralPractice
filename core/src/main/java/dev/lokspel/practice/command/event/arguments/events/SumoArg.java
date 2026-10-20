package dev.lokspel.practice.command.event.arguments.events;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.enums.EventType;
import dev.lokspel.practice.manager.fight.event.events.duel.sumo.SumoData;
import dev.lokspel.practice.manager.fight.event.util.EventUtil;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.kit.KitData;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class SumoArg {

  private SumoArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.setup")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getEvent().getNoPermission());
      return;
    }

    SumoData sumoData = (SumoData) EventManager.getInstance().getEventData().get(EventType.SUMO);

    // Checking if the event is live, if it is, it will send a message to the player and return.
    if (EventManager.getInstance().isEventLive(EventType.SUMO)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getEvent()
              .getArguments()
              .getSumo()
              .getEventLive());
      return;
    }

    // Checking if the player is trying to enable or disable the arena.
    if (args.length == 2 && args[1].equalsIgnoreCase("enable")) {
      if (!sumoData.isEnabled()) {
        EventUtil.changeStatus(sumoData, player);
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getEvent()
                .getArguments()
                .getSumo()
                .getEventAlreadyEnabled());
      }
      return;
    } else if (args.length == 2 && args[1].equalsIgnoreCase("disable")) {
      if (sumoData.isEnabled()) {
        EventUtil.changeStatus(sumoData, player);
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getEvent()
                .getArguments()
                .getSumo()
                .getEventAlreadyDisabled());
      }
      return;
    } else if (args.length == 2 && args[1].equalsIgnoreCase("help")) {
      sendHelpMsg(player, label);

      return;
    }

    // Checking if the arena is enabled, if it is, it will send a message to the player and return.
    if (sumoData.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getEvent().getArguments().getSumo().getCantEdit());
      return;
    }

    // Setting the kit for the event.
    if (args.length == 2 && args[1].equalsIgnoreCase("setkit")) {
      KitData kitData = sumoData.getKitData();
      kitData.setKitData(player, true);

      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getEvent().getArguments().getSumo().getKitSet());
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
            .getSumo()
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
      arguments.add("enable");
      arguments.add("disable");
      arguments.add("setkit");

      return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
    }

    return arguments;
  }
}
