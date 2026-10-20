package dev.lokspel.practice.manager.fight.event.events.onevsall.juggernaut;

import dev.lokspel.api.event.event.EventEndEvent;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.event.enums.EventStatus;
import dev.lokspel.practice.manager.fight.event.events.ffa.interfaces.FfaEvent;
import dev.lokspel.practice.manager.server.ServerManager;
import dev.lokspel.practice.util.kit.KitData;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public class Juggernaut extends FfaEvent {

  private Player juggernaut;

  public Juggernaut(Object starter, JuggernautData eventData) {
    super(
        starter,
        eventData,
        () -> PracticeConfig.language().getCommand().getEvent().getArguments().getJuggernaut());
  }

  @Override
  protected void customCustomStart() {
    this.juggernaut = this.players.get(random.nextInt(this.players.size()));
    this.startPlayerCount = this.startPlayerCount - 1;
    this.sendMessage(
        PracticeConfig.language()
            .getCommand()
            .getEvent()
            .getArguments()
            .getJuggernaut()
            .getJuggernautAnnounce()
            .replace("%player%", juggernaut.getName()),
        false);
  }

  @Override
  protected void loadInventory(Player player) {
    KitData kitData;
    if (player.equals(juggernaut)) {
      kitData = this.getEventData().getJuggernautKitData();
    } else {
      kitData = this.getEventData().getPlayerKitData();
    }
    kitData.loadKitData(player, true);
  }

  @Override
  public void killPlayer(Player player, boolean teleport) {
    if (!this.players.contains(player)) {
      return;
    }

    if (this.juggernaut != player) {
      this.sendMessage(
          PracticeConfig.language()
              .getCommand()
              .getEvent()
              .getArguments()
              .getJuggernaut()
              .getPlayerDied()
              .replace("%player%", player.getName())
              .replace("%startPlayerCount%", String.valueOf(this.getStartPlayerCount()))
              .replace("%playerCount%", String.valueOf(this.players.size() - 2)),
          true);
    } else {
      this.sendMessage(
          PracticeConfig.language()
              .getCommand()
              .getEvent()
              .getArguments()
              .getJuggernaut()
              .getJuggernautDied()
              .replace("%player%", juggernaut.getName()),
          true);
    }

    this.players.remove(player);
    if (player != null && player.isOnline()) {
      addSpectator(player, null, teleport, false);
    }

    if (this.juggernaut != player) {
      if (players.size() == 1) {
        this.winner = juggernaut;
        this.endEvent();
      }
    } else {
      this.winner =
          players.stream().findAny().isPresent() ? players.stream().findAny().get() : null;
      this.endEvent();
    }
  }

  @Override
  public void endEvent() {
    if (this.status.equals(EventStatus.END)) {
      return;
    }

    EventEndEvent event = new EventEndEvent(this);
    Bukkit.getPluginManager().callEvent(event);

    this.cancelAllRunnable();
    this.status = EventStatus.END;

    if (AstralPractice.getInstance().isEnabled()) {
      this.getEndRunnable().begin();
    } else {
      this.getEndRunnable().end();
    }

    if (this.winner != null) {
      if (this.winner == juggernaut) {
        this.sendMessage(
            PracticeConfig.language()
                .getCommand()
                .getEvent()
                .getArguments()
                .getJuggernaut()
                .getJuggernautWon()
                .replace("%player%", winner.getName()),
            true);

        for (String cmd : eventData.getType().getWinnerCmd()) {
          ServerManager.runConsoleCommand(cmd.replace("%player%", juggernaut.getName()));
        }
      } else {
        this.sendMessage(
            PracticeConfig.language()
                .getCommand()
                .getEvent()
                .getArguments()
                .getJuggernaut()
                .getAttackersWon(),
            true);

        for (Player player : players) {
          if (player != juggernaut) {
            for (String cmd : eventData.getType().getWinnerCmd()) {
              ServerManager.runConsoleCommand(cmd.replace("%player%", player.getName()));
            }
          }
        }
      }
    } else {
      this.sendMessage(
          PracticeConfig.language()
              .getCommand()
              .getEvent()
              .getArguments()
              .getJuggernaut()
              .getNoWinner(),
          true);
    }
  }

  @Override
  public JuggernautData getEventData() {
    return (JuggernautData) eventData;
  }
}
