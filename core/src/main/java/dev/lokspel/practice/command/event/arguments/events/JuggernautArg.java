package dev.lokspel.practice.command.event.arguments.events;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.enums.EventType;
import dev.lokspel.practice.manager.fight.event.events.onevsall.juggernaut.JuggernautData;
import dev.lokspel.practice.manager.fight.event.util.EventUtil;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.kit.KitData;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class JuggernautArg {

  private JuggernautArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.setup")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getEvent().getNoPermission());
      return;
    }

    String label2 = args[0];
    JuggernautData juggernautData =
        (JuggernautData) EventManager.getInstance().getEventData().get(EventType.JUGGERNAUT);

    // Checking if the event is live, if it is, it will send a message to the player and return.
    if (EventManager.getInstance().isEventLive(EventType.JUGGERNAUT)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getEvent()
              .getArguments()
              .getJuggernaut()
              .getEventLive());
      return;
    }

    // Checking if the player is trying to enable or disable the arena.
    if (args.length == 2 && args[1].equalsIgnoreCase("enable")) {
      if (!juggernautData.isEnabled()) {
        EventUtil.changeStatus(juggernautData, player);
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getEvent()
                .getArguments()
                .getJuggernaut()
                .getEventAlreadyEnabled());
      }

      return;
    } else if (args.length == 2 && args[1].equalsIgnoreCase("disable")) {
      if (juggernautData.isEnabled()) {
        EventUtil.changeStatus(juggernautData, player);
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getEvent()
                .getArguments()
                .getJuggernaut()
                .getEventAlreadyDisabled());
      }

      return;
    } else if (args.length == 2 && args[1].equalsIgnoreCase("help")) {
      sendHelpMsg(player, label, label2);

      return;
    }

    // Checking if the arena is enabled, if it is, it will send a message to the player and return.
    if (juggernautData.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getEvent()
              .getArguments()
              .getJuggernaut()
              .getCantEdit());
      return;
    }

    // This is checking if the player is trying to scan the arena for spawn and spectator positions.
    if (args.length == 3 && args[1].equalsIgnoreCase("setkit")) {
      if (args[2].equalsIgnoreCase("juggernaut")) {
        KitData kitData = juggernautData.getJuggernautKitData();
        kitData.setKitData(player, true);

        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getEvent()
                .getArguments()
                .getJuggernaut()
                .getJuggernautKitSet());
      } else if (args[2].equalsIgnoreCase("players")) {
        KitData kitData = juggernautData.getPlayerKitData();
        kitData.setKitData(player, true);

        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getEvent()
                .getArguments()
                .getJuggernaut()
                .getPlayersKitSet());
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getEvent()
                .getArguments()
                .getJuggernaut()
                .getKitCommandHelp()
                .replace("%label%", label)
                .replace("%label2%", label2));
      }
    } else {
      sendHelpMsg(player, label, label2);
    }
  }

  private static void sendHelpMsg(Player player, String label, String label2) {
    for (String line :
        PracticeConfig.language()
            .getCommand()
            .getEvent()
            .getArguments()
            .getJuggernaut()
            .getCommandHelp()) {
      Common.sendMmMessage(player, line.replace("%label%", label).replace("%label2%", label2));
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

    if (args.length == 3 && args[1].equalsIgnoreCase("setkit")) {
      arguments.add("juggernaut");
      arguments.add("players");

      return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
    }

    return arguments;
  }
}
