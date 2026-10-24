package dev.lokspel.practice.manager.queue;

import dev.lokspel.api.event.queue.QueueEndEvent;
import dev.lokspel.api.event.queue.QueueStartEvent;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.division.Division;
import dev.lokspel.practice.manager.fight.match.enums.WeightClass;
import dev.lokspel.practice.manager.fight.match.type.duel.Duel;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.util.LadderUtil;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.queue.runnables.RankedSearchRunnable;
import dev.lokspel.practice.manager.queue.runnables.SearchRunnable;
import dev.lokspel.practice.manager.queue.runnables.UnrankedSearchRunnable;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.TimeUtil;
import dev.lokspel.practice.util.interfaces.Runnable;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.DeathProtection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Queue extends Runnable implements dev.lokspel.api.interfaces.Queue {

  private static final int UNRANKED_MAX_QUEUE_TIME =
      PracticeConfig.settings().getQueue().getUnranked().getMaxQueueTime();
  private static final int RANKED_MAX_QUEUE_TIME =
      PracticeConfig.settings().getQueue().getRanked().getMaxQueueTime();

  private final QueueManager queueManager = QueueManager.getInstance();

  @Getter @Setter private Player player;
  @Getter private final Profile profile;
  @Getter private NormalLadder ladder;
  private final Set<NormalLadder> queuedLadders = new LinkedHashSet<>();
  @Getter @Setter private boolean ranked;
  @Getter @Setter private int range;
  @Getter private SearchRunnable searchRunnable;

  @Getter @Setter // Just for testing purposes
  private int duration;

  private final boolean preserveOpenMenuOnStart;

  public Queue(Player player, NormalLadder ladder, boolean ranked) {
    this(player, ladder, ranked, false);
  }

  public Queue(
      Player player, NormalLadder ladder, boolean ranked, boolean preserveOpenMenuOnStart) {
    super(0, 20L, false);
    this.player = player;
    this.profile = ProfileManager.getInstance().getProfile(player);
    this.ladder = ladder;
    this.queuedLadders.add(ladder);
    this.ranked = ranked;
    this.preserveOpenMenuOnStart = preserveOpenMenuOnStart;
  }

  public void addLadder(NormalLadder normalLadder) {
    if (normalLadder == null) {
      return;
    }

    this.ladder = normalLadder;
    this.queuedLadders.add(normalLadder);

    if (this.searchRunnable == null && !this.ranked) {
      searchInstantMatch();
    }
  }

  public void removeLadder(NormalLadder normalLadder) {
    if (normalLadder == null) {
      return;
    }

    boolean removed = this.queuedLadders.remove(normalLadder);
    if (removed) {
      this.ladder = this.queuedLadders.stream().findFirst().orElse(null);
    }
  }

  public boolean isQueued(NormalLadder normalLadder) {
    return this.queuedLadders.contains(normalLadder);
  }

  public List<NormalLadder> getQueuedLadders() {
    return List.copyOf(this.queuedLadders);
  }

  public String getCyclingSidebarLadder() {
    if (queuedLadders.isEmpty()) {
      return "Unknown";
    }

    if (queuedLadders.size() == 1) {
      return queuedLadders.iterator().next().getDisplayName();
    }

    List<NormalLadder> ladders = new ArrayList<>(queuedLadders);
    int index = (seconds / 2) % ladders.size();
    return ladders.get(index).getDisplayName();
  }

  public void startQueue() {
    // Call QueueStartEvent
    QueueStartEvent queueStartEvent = new QueueStartEvent(this);
    Bukkit.getPluginManager().callEvent(queueStartEvent);
    if (queueStartEvent.isCancelled()) {
      return;
    }

    // Add the queue to the queueManager
    this.queueManager.getQueues().add(this);

    // Begins the duration timer
    this.begin();

    // Set the player's inventory to the match queue inventory
    InventoryManager.getInstance().setMatchQueueInventory(player, !preserveOpenMenuOnStart);

    String ladderDisplay = this.ladder != null ? this.ladder.getDisplayName() : "Unknown";

    // Send the player a message that they have started queueing
    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getQueues()
            .getQueueStart()
            .replace(
                "%weightClass%",
                (ranked ? WeightClass.RANKED.getMmName() : WeightClass.UNRANKED.getMmName()))
            .replace("%ladder%", ladderDisplay));

    // Start the queue based on the queue type
    if (this.ranked) {
      startRankedQueue();
    } else {
      startUnrankedQueue();
    }
  }

  public void startRankedQueue() {
    this.searchRunnable = new RankedSearchRunnable(this);
    this.searchRunnable.begin();

    GuiManager.getInstance().searchGui(GuiType.Queue_Ranked).update();
  }

  public void startUnrankedQueue() {
    Division division = profile.getStats().getDivision();

    if (division == null
        || !PracticeConfig.settings().getQueue().getUnranked().getDivisionSearch().getEnabled()) {
      if (searchInstantMatch()) {
        return;
      }
    } else {
      this.searchRunnable = new UnrankedSearchRunnable(this);
      this.searchRunnable.begin();
    }

    GuiManager.getInstance().searchGui(GuiType.Queue_Unranked).update();
  }

  /**
   * Searches for a compatible unranked opponent across active queues and starts a match if found.
   *
   * @return true if a match was found and started
   */
  public boolean searchInstantMatch() {
    if (this.ranked) {
      return false;
    }

    List<NormalLadder> ladders = new ArrayList<>(this.queuedLadders);
    Collections.shuffle(ladders);

    for (Queue other : queueManager.getQueues()) {
      if (other == this || other.getPlayer() == this.player || other.isRanked()) {
        continue;
      }

      if (this.profile.getIgnoredPlayers().contains(other.getProfile())
          || other.getProfile().getIgnoredPlayers().contains(this.profile)) {
        continue;
      }

      for (NormalLadder normalLadder : ladders) {
        if (!other.isQueued(normalLadder)) {
          continue;
        }

        this.cancel();
        this.startMatch(other, normalLadder);
        return true;
      }
    }
    return false;
  }

  public void startMatch(Queue queue, NormalLadder matchedLadder) {
    if (matchedLadder == null) {
      return;
    }

    this.ladder = matchedLadder;
    queue.ladder = matchedLadder;

    // Check if the ladder is frozen or disabled
    if (matchedLadder.getAvailableArenas().isEmpty()) {
      queue.endQueue(false, PracticeConfig.language().getQueues().getNoAvailableArena());
      this.endQueue(false, PracticeConfig.language().getQueues().getNoAvailableArena());
      return;
    }

    // Get an available arena
    Arena arena = LadderUtil.getAvailableArena(matchedLadder);
    if (arena == null) {
      queue.endQueue(false, PracticeConfig.language().getQueues().getNoAvailableArena());
      this.endQueue(false, PracticeConfig.language().getQueues().getNoAvailableArena());
      return;
    }

    String stopMessage =
        PracticeConfig.language()
            .getQueues()
            .getQueueStopped()
            .replace(
                "%weightClass%",
                (ranked ? WeightClass.RANKED.getMmName() : WeightClass.UNRANKED.getMmName()))
            .replace("%ladder%", matchedLadder.getDisplayName());

    queueManager.endAllQueuesForPlayer(queue.getPlayer(), true, stopMessage);
    queueManager.endAllQueuesForPlayer(this.player, true, stopMessage);

    boolean matchFoundAnimation =
        PracticeConfig.settings().getMatchSettings().getMatchFound().isIconAnimation();

    if (matchFoundAnimation) {
      ItemStack icon = matchedLadder.getIcon();
      icon.setData(DataComponentTypes.DEATH_PROTECTION, DeathProtection.deathProtection());

      for (Player matchPlayer : List.of(player, queue.getPlayer())) {
        if (!matchPlayer.isOnline()) {
          continue;
        }

        PlayerInventory inventory = matchPlayer.getInventory();
        final ItemStack previous = inventory.getItemInMainHand();

        inventory.setItemInMainHand(icon.clone());
        matchPlayer.updateInventory();
        matchPlayer.sendEntityEffect(EntityEffect.PROTECTED_FROM_DEATH, matchPlayer);
        inventory.setItemInMainHand(previous);
      }
    }

    Duel duel =
        new Duel(
            matchedLadder,
            arena,
            Arrays.asList(player, queue.getPlayer()),
            ranked,
            matchedLadder.getRounds());
    duel.setQueuedMatch(true);

    if (matchFoundAnimation) {
      Bukkit.getScheduler()
          .runTaskLater(
              AstralPractice.getInstance(),
              () -> {
                if (player.isOnline() && queue.getPlayer().isOnline()) {
                  if (!duel.startMatch()) {
                    returnToLobbyIfOnline(player);
                    returnToLobbyIfOnline(queue.getPlayer());
                  }
                } else {
                  returnToLobbyIfOnline(player);
                  returnToLobbyIfOnline(queue.getPlayer());
                }
              },
              PracticeConfig.settings().getMatchSettings().getMatchFound().getIconAnimationDelay());
    } else {
      if (!duel.startMatch()) {
        returnToLobbyIfOnline(player);
        returnToLobbyIfOnline(queue.getPlayer());
      }
    }
  }

  public void startMatch(Queue queue) {
    this.startMatch(queue, ladder);
  }

  private static void returnToLobbyIfOnline(Player matchPlayer) {
    if (matchPlayer != null && matchPlayer.isOnline()) {
      InventoryManager.getInstance().setLobbyInventory(matchPlayer, false);
    }
  }

  public void endQueue(boolean foundMatch, final String message) {
    // Call QueueEndEvent
    QueueEndEvent queueEndEvent = new QueueEndEvent(this);
    Bukkit.getPluginManager().callEvent(queueEndEvent);

    // Remove the queue from the queueManager
    this.queueManager.getQueues().remove(this);

    // Cancel the timers
    if (this.searchRunnable != null) {
      this.searchRunnable.cancel();
    }

    this.cancel();

    // Set the player's inventory to the lobby inventory
    if (!foundMatch && player.isOnline()) {
      InventoryManager.getInstance().setLobbyInventory(player, false);
    }

    // Send the player a message that they have stopped queueing
    if (message != null) {
      Common.sendMmMessage(player, message);
    }

    // Update the GUIs
    if (ranked) {
      GuiManager.getInstance().searchGui(GuiType.Queue_Ranked).update();
    } else {
      GuiManager.getInstance().searchGui(GuiType.Queue_Unranked).update();
    }
  }

  @Override
  public void run() {

    if (queuedLadders.isEmpty()) {
      this.endQueue(false, null);
      return;
    }

    for (NormalLadder normalLadder : new ArrayList<>(queuedLadders)) {
      if (!normalLadder.isFrozen() && normalLadder.isEnabled()) {
        continue;
      }

      this.removeLadder(normalLadder);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getQueues()
              .getLadderFrozen()
              .replace("%ladder%", normalLadder.getDisplayName()));
    }

    if (queuedLadders.isEmpty()) {
      this.endQueue(false, null);
      return;
    }

    int maxQueueTime;
    if (ranked) {
      maxQueueTime = RANKED_MAX_QUEUE_TIME;
    } else {
      maxQueueTime = UNRANKED_MAX_QUEUE_TIME;
    }

    if (seconds >= maxQueueTime) {
      this.endQueue(
          false,
          PracticeConfig.language()
              .getQueues()
              .getNoMatchInTime()
              .replace("%maxTime%", String.valueOf(maxQueueTime / 60)));
      return;
    }

    seconds++;

    if (this.searchRunnable == null && !this.ranked) {
      searchInstantMatch();
    }
  }

  public String getFormattedDuration() {
    return TimeUtil.formatDuration(seconds * 1000L);
  }

  @Override
  public String getLadderName() {
    return this.ladder != null ? this.ladder.getName() : null;
  }
}
