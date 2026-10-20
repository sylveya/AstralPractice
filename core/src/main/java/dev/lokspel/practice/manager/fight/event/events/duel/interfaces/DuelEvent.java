package dev.lokspel.practice.manager.fight.event.events.duel.interfaces;

import dev.lokspel.api.event.event.EventEndEvent;
import dev.lokspel.api.event.spectate.start.EventSpectateStartEvent;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.language.EventMessages;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.enums.EventStatus;
import dev.lokspel.practice.manager.fight.event.interfaces.Event;
import dev.lokspel.practice.manager.fight.event.runnables.DurationRunnable;
import dev.lokspel.practice.manager.fight.event.runnables.StartRunnable;
import dev.lokspel.practice.manager.fight.event.util.EventUtil;
import dev.lokspel.practice.manager.server.ServerManager;
import dev.lokspel.practice.manager.server.sound.SoundEffect;
import dev.lokspel.practice.manager.server.sound.SoundManager;
import dev.lokspel.practice.manager.server.sound.SoundType;
import dev.lokspel.practice.util.entityhider.PlayerHider;
import dev.lokspel.practice.util.playerutil.PlayerUtil;
import dev.lokspel.practice.util.startup.PermanentConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
public abstract class DuelEvent extends Event {

  private static final Random random = new Random();

  private final Supplier<EventMessages> messages;
  private final List<DuelFight> fights = new ArrayList<>();
  private final Map<Player, Player> duelList = new HashMap<>();
  private final Map<Player, Player> spectating = new HashMap<>();
  @Setter private int round;

  private StartRunnable startRunnable;
  private DurationRunnable durationRunnable;

  public DuelEvent(Object starter, DuelEventData eventData, Supplier<EventMessages> messages) {
    super(starter, eventData);
    this.messages = messages;
  }

  /** Returns this event's language section, resolved on each call so reloads apply. */
  public EventMessages getMessages() {
    return this.messages.get();
  }

  @Override
  protected void customStart() {
    this.startNextRound();
  }

  protected void startNextRound() {
    if (this.round != 0) {
      this.fightChange.rollback(
          PermanentConfig.EVENT_ROLLBACK_MAX_CHECKS, PermanentConfig.EVENT_ROLLBACK_MAX_CHANGES);
    }

    this.round++;

    this.getNextFights(
        leftPlayer -> {
          sendMessage(
              getMessages().getRoundDrawn().replace("%round%", String.valueOf(round)), true);

          for (DuelFight fight : fights) {
            Player player1 = fight.getPlayers().get(0);
            Player player2 = fight.getPlayers().get(1);

            for (Player eventPlayer : players) {
              if (player1 != eventPlayer && player2 != eventPlayer) {
                PlayerHider.getInstance().hidePlayer(player1, eventPlayer);
                PlayerHider.getInstance().hidePlayer(player2, eventPlayer);
              }

              if (eventPlayer != leftPlayer) {
                PlayerUtil.setFightPlayer(eventPlayer);
              }
            }

            PlayerHider.getInstance().showPlayer(player1, player2);
            PlayerHider.getInstance().showPlayer(player2, player1);

            this.teleport(player1, this.getEventData().getLocation1());
            this.teleport(player2, this.getEventData().getLocation2());
          }

          Set<Player> spectatorsToRefresh = new LinkedHashSet<>(this.getSpectators());
          if (leftPlayer != null) {
            this.sendMessage(
                getMessages().getPlayerOutOfRound().replace("%player%", leftPlayer.getName()),
                true);
            spectatorsToRefresh.add(leftPlayer);
          }

          for (Player spectator : spectatorsToRefresh) {
            Player target = this.spectating.get(spectator);
            if (target != null && !this.isInFight(target)) {
              target = null;
            }

            if (target == null) {
              target = this.getRandomFightPlayer();
            }

            this.addSpectator(spectator, target, true, false);
          }

          // Set the status to start, countdown until the match is live.
          this.status = EventStatus.START;
          this.getStartRunnable().begin();
        });
  }

  public abstract void teleport(final Player player, final Location location);

  @Override
  public void handleStartRunnable(StartRunnable startRunnable) {
    int seconds = startRunnable.getSeconds();

    if (seconds == 0) {
      this.sendEventStartFightTitle();
      startRunnable.cancel();

      this.getDurationRunnable().begin();
      this.status = EventStatus.LIVE;
    } else {
      int titleCountdownFrom =
          PracticeConfig.settings().getMatchSettings().getTitle().getStartCountdownFrom();
      if (seconds <= titleCountdownFrom) {
        this.sendEventStartCountdownTitle(seconds);
      }

      if (seconds <= 5) {
        this.sendMessage(
            getMessages()
                .getRoundStarting()
                .replace("%seconds%", String.valueOf(seconds))
                .replace(
                    "%secondName%",
                    (seconds == 1
                        ? PracticeConfig.language().getSecondName().getOneSecond()
                        : PracticeConfig.language().getSecondName().getUnderOneSecond())),
            true);

        SoundEffect sound = SoundManager.getInstance().getSound(SoundType.EVENT_START_COUNTDOWN);
        if (sound != null) {
          sound.play(this.getPlayers());
        }
      }

      startRunnable.decreaseTime();
    }
  }

  private void sendEventStartCountdownTitle(int seconds) {
    String countdownTitle =
        PracticeConfig.language()
            .getMatch()
            .getStartTitles()
            .getCountdown()
            .replace("%remaining%", String.valueOf(seconds));
    for (Player recipient : this.getTitleRecipients()) {
      EventManager.getInstance().sendRawEventTitle(recipient, countdownTitle, "");
    }
  }

  private void sendEventStartFightTitle() {
    String fightTitle = PracticeConfig.language().getMatch().getStartTitles().getFight();
    for (Player recipient : this.getTitleRecipients()) {
      EventManager.getInstance().sendRawEventTitle(recipient, fightTitle, "");
    }
  }

  private List<Player> getTitleRecipients() {
    Set<Player> recipients = new LinkedHashSet<>();
    recipients.addAll(this.players);
    recipients.addAll(this.getSpectators());
    return new ArrayList<>(recipients);
  }

  @Override
  public void handleDurationRunnable(DurationRunnable durationRunnable) {
    int seconds = durationRunnable.getSeconds();

    if (seconds == 0) {
      durationRunnable.cancel();

      if (!this.status.equals(EventStatus.END)) {
        List<DuelFight> fights = new ArrayList<>(this.fights);
        for (DuelFight fight : fights) {
          fight.endFight(null);
        }
      }
    } else {
      if (seconds <= 5 || seconds == 30 || seconds == 60) {
        sendMessage(
            getMessages()
                .getRoundEnding()
                .replace("%seconds%", String.valueOf(seconds))
                .replace(
                    "%secondName%",
                    (seconds == 1
                        ? PracticeConfig.language().getSecondName().getOneSecond()
                        : PracticeConfig.language().getSecondName().getUnderOneSecond())),
            true);
      }

      durationRunnable.decreaseTime();
    }
  }

  @Override
  public void killPlayer(Player player, boolean teleport) {
    DuelFight duelFight = this.getFight(player);
    if (duelFight != null) {
      duelFight.endFight(player);
    }
  }

  @Override
  public boolean checkIfEnd() {
    if (!this.fights.isEmpty()) {
      return false;
    }

    if (players.size() == 1) {
      this.winner = players.getFirst();
      return true;
    }

    return players.isEmpty();
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

      this.sendMessage(
          getMessages().getWonEvent().replace("%winner%", this.winner.getName()), true);

      for (String cmd : this.eventData.getType().getWinnerCmd()) {
        ServerManager.runConsoleCommand(cmd.replace("%player%", this.winner.getName()));
      }
    } else {
      this.sendMessage(getMessages().getNoWinner(), true);
    }
  }

  @Override
  public DuelEventData getEventData() {
    return (DuelEventData) eventData;
  }

  public boolean isInFight(Player player) {
    for (DuelFight fight : fights) {
      if (fight.getPlayers().contains(player)) {
        return true;
      }
    }
    return false;
  }

  public DuelFight getFight(Player player) {
    for (DuelFight fight : fights) {
      if (fight.getPlayers().contains(player)) {
        return fight;
      }
    }
    return null;
  }

  public Player getRandomFightPlayer() {
    for (DuelFight fight : this.fights) {
      if (!fight.getPlayers().isEmpty()) {
        return fight.getPlayers().getFirst();
      }
    }
    return null;
  }

  private void getNextFights(NextFightCallback callback) {
    fights.clear();
    duelList.clear();
    Collections.shuffle(players);

    Bukkit.getScheduler()
        .runTaskAsynchronously(
            AstralPractice.getInstance(),
            () -> {
              for (Player player : players) {
                if (!isInFight(player)) {
                  List<Player> notInFight = new ArrayList<>();
                  for (Player enemy : players) {
                    if (player != enemy && !isInFight(enemy)) {
                      notInFight.add(enemy);
                    }
                  }

                  if (notInFight.isEmpty()) {
                    Bukkit.getScheduler()
                        .runTask(AstralPractice.getInstance(), () -> callback.onNextFight(player));
                    return;
                  }

                  Player enemy = notInFight.get(random.nextInt(notInFight.size()));

                  List<Player> fightPlayers = new ArrayList<>();
                  fightPlayers.add(player);
                  fightPlayers.add(enemy);

                  fights.add(new DuelFight(this, fightPlayers));

                  duelList.put(player, enemy);
                  duelList.put(enemy, player);
                }
              }

              Bukkit.getScheduler()
                  .runTask(AstralPractice.getInstance(), () -> callback.onNextFight(null));
            });
  }

  @Override
  public void addSpectator(Player spectator, Player target, boolean teleport, boolean message) {
    if (!players.contains(spectator)) {
      EventSpectateStartEvent event = new EventSpectateStartEvent(spectator, this);
      Bukkit.getPluginManager().callEvent(event);
    }

    Player resolvedTarget = target;
    if (resolvedTarget != null && !this.isInFight(resolvedTarget)) {
      resolvedTarget = null;
    }

    if (resolvedTarget == null) {
      resolvedTarget = this.getRandomFightPlayer();
    }

    DuelFight fight = resolvedTarget != null ? this.getFight(resolvedTarget) : null;

    Player previousTarget = this.spectating.get(spectator);
    if (previousTarget != null) {
      DuelFight previousFight = this.getFight(previousTarget);
      if (previousFight != null) {
        previousFight.getSpectators().remove(spectator);
      }
    }

    if (fight == null) {
      this.spectating.remove(spectator);

      if (teleport) {
        spectator.teleport(eventData.getCuboid().getCenter());
      }

      if (players.contains(spectator)) {
        PlayerUtil.clearPlayer(spectator, true, true, false);
      } else {
        this.addSpectator(spectator);
        EventUtil.setEventSpectatorInventory(spectator);
      }

      return;
    }

    this.spectating.put(spectator, resolvedTarget);

    // Teleport the player to the spectator location.
    spectator.teleport(resolvedTarget);

    if (players.contains(spectator)) {
      PlayerUtil.clearPlayer(spectator, true, true, false);
    } else {
      this.addSpectator(spectator);
      fight.getSpectators().add(spectator);

      EventUtil.setEventSpectatorInventory(spectator);

      if (message && !this.status.equals(EventStatus.END)) {
        sendMessage(
            getMessages().getStartedSpectating().replace("%spectator%", spectator.getName()), true);
      }
    }

    for (Player eventPlayer : players) {
      PlayerHider.getInstance().hidePlayer(eventPlayer, spectator);

      if (fight.getPlayers().contains(eventPlayer)) {
        PlayerHider.getInstance().showPlayer(spectator, eventPlayer);
      } else {
        PlayerHider.getInstance().hidePlayer(spectator, eventPlayer);
      }
    }
  }

  @Override
  public void removeSpectator(Player player) {
    Player previousTarget = this.spectating.remove(player);
    if (previousTarget != null) {
      DuelFight previousFight = this.getFight(previousTarget);
      if (previousFight != null) {
        previousFight.getSpectators().remove(player);
      }
    }

    super.removeSpectator(player);
  }

  @Override
  public StartRunnable getStartRunnable() {
    if (this.startRunnable == null) {
      this.startRunnable = new StartRunnable(this);
    } else if (!this.startRunnable.isRunning() && this.startRunnable.isHasRun()) {
      this.startRunnable = new StartRunnable(this);
    }

    return this.startRunnable;
  }

  @Override
  public DurationRunnable getDurationRunnable() {
    if (this.durationRunnable == null) {
      this.durationRunnable = new DurationRunnable(this);
    } else if (!this.durationRunnable.isRunning() && this.durationRunnable.isHasRun()) {
      this.durationRunnable = new DurationRunnable(this);
    }

    return this.durationRunnable;
  }
}
