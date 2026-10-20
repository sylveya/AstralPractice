package dev.lokspel.practice.manager.fight.event.enums;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.List;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
public enum EventType {
  LMS(
      10,
      PracticeConfig.settings().getEvent().getLms().getName(),
      ItemCreateUtil.createItem("<yellow>Last Man Standing", Material.DIAMOND_SWORD),
      15,
      PracticeConfig.language().getCommand().getEvent().getArguments().getLms().getBroadcast(),
      PracticeConfig.settings().getEvent().getLms().getWinnerCommand(),
      40,
      15 * 60,
      30 * 60,
      15,
      10,
      50),
  OITC(
      11,
      PracticeConfig.settings().getEvent().getOitc().getName(),
      ItemCreateUtil.createItem("<gold>One In The Chamber", Material.BOW),
      15,
      PracticeConfig.language().getCommand().getEvent().getArguments().getOitc().getBroadcast(),
      PracticeConfig.settings().getEvent().getOitc().getWinnerCommand(),
      40,
      15 * 60,
      30 * 60,
      20,
      10,
      30),
  TNTTAG(
      12,
      PracticeConfig.settings().getEvent().getTnttag().getName(),
      ItemCreateUtil.createItem("<red>TNT Tag", Material.TNT),
      15,
      PracticeConfig.language().getCommand().getEvent().getArguments().getTnttag().getBroadcast(),
      PracticeConfig.settings().getEvent().getTnttag().getWinnerCommand(),
      30,
      10 * 60,
      30, // TNT Explode time
      5,
      12,
      20),
  BRACKETS(
      13,
      PracticeConfig.settings().getEvent().getBrackets().getName(),
      ItemCreateUtil.createItem("<green>Brackets", Material.POTION, Short.valueOf("34")),
      15,
      PracticeConfig.language().getCommand().getEvent().getArguments().getBrackets().getBroadcast(),
      PracticeConfig.settings().getEvent().getBrackets().getWinnerCommand(),
      60,
      15 * 60,
      8 * 60,
      5,
      10,
      60),
  SUMO(
      14,
      PracticeConfig.settings().getEvent().getSumo().getName(),
      ItemCreateUtil.createItem("<gold>Sumo", Material.STICK),
      15,
      PracticeConfig.language().getCommand().getEvent().getArguments().getSumo().getBroadcast(),
      PracticeConfig.settings().getEvent().getSumo().getWinnerCommand(),
      45,
      10 * 60,
      3 * 60,
      3,
      20,
      100),
  SPLEGG(
      15,
      PracticeConfig.settings().getEvent().getSplegg().getName(),
      ItemCreateUtil.createItem("<aqua>Splegg", Material.EGG),
      15,
      PracticeConfig.language().getCommand().getEvent().getArguments().getSplegg().getBroadcast(),
      PracticeConfig.settings().getEvent().getSplegg().getWinnerCommand(),
      40,
      12 * 60,
      15 * 60,
      10,
      20,
      50),
  JUGGERNAUT(
      16,
      PracticeConfig.settings().getEvent().getJuggernaut().getName(),
      ItemCreateUtil.createItem("<gold>Juggernaut", Material.GOLDEN_APPLE),
      15,
      PracticeConfig.language()
          .getCommand()
          .getEvent()
          .getArguments()
          .getJuggernaut()
          .getBroadcast(),
      PracticeConfig.settings().getEvent().getJuggernaut().getWinnerCommand(),
      60,
      15 * 60,
      30 * 60,
      15,
      20,
      50);

  private final int guiSlot;
  private final String name;
  private final ItemStack icon;
  private final int broadcastInterval;
  private final String broadcastMsg;
  private final List<String> winnerCmd;
  private final int waitBeforeStart;
  private final int maxQueueTime;
  private final int duration;
  private final int startTime;
  private final int minPlayer;
  private final int maxPlayer;

  EventType(
      int guiSlot,
      String name,
      ItemStack icon,
      int broadcastInterval,
      String broadcastMsg,
      List<String> winnerCmd,
      int waitBeforeStart,
      int maxQueueTime,
      int duration,
      int startTime,
      int minPlayer,
      int maxPlayer) {
    this.guiSlot = guiSlot;
    this.name = name;
    this.icon = icon;
    this.broadcastInterval = broadcastInterval;
    this.broadcastMsg = broadcastMsg;
    this.winnerCmd = winnerCmd;
    this.waitBeforeStart =
        waitBeforeStart; // Once the required player has entered the queue the countdown starts from
    // here. (seconds)
    this.maxQueueTime =
        maxQueueTime; // If the event doesn't start before this time expires, the event stops.
    // (minutes)
    this.duration = duration; // The maximum duration of the event in minutes. (minutes)
    this.startTime = startTime; // The time before the players can start killing each other.
    this.minPlayer = minPlayer; // Minimum players that needs to join for the event to start.
    this.maxPlayer = maxPlayer; // Maximum player that can join the event.
  }
}
