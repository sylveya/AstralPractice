package dev.lokspel.practice.manager.queue;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.division.Division;
import dev.lokspel.practice.manager.division.DivisionManager;
import dev.lokspel.practice.manager.fight.match.enums.WeightClass;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@Getter
public class QueueManager implements Listener {

  private static final String MULTI_QUEUE_PERMISSION_DEFAULT = "ap.queue.multi";

  private static QueueManager instance;

  public static QueueManager getInstance() {
    if (instance == null) {
      if (AstralPractice.getInstance() == null || !AstralPractice.getInstance().isEnabled()) {
        return null;
      }
      instance = new QueueManager();
    }
    return instance;
  }

  private final List<Queue> queues = new CopyOnWriteArrayList<>();

  private QueueManager() {
    Bukkit.getPluginManager().registerEvents(this, AstralPractice.getInstance());
  }

  public Queue getQueue(final Player queuePlayer) {
    return queues.stream()
        .filter(queue -> queue.getPlayer() == queuePlayer)
        .findFirst()
        .orElse(null);
  }

  public int getQueueSize(final Ladder ladder, final boolean ranked) {
    if (!(ladder instanceof NormalLadder normalLadder)) {
      return 0;
    }
    return (int)
        queues.stream()
            .filter(queue -> queue.isRanked() == ranked && queue.isQueued(normalLadder))
            .count();
  }

  public int getQueueSize(final Ladder ladder) {
    if (!(ladder instanceof NormalLadder normalLadder)) {
      return 0;
    }
    return (int) queues.stream().filter(queue -> queue.isQueued(normalLadder)).count();
  }

  public void createUnrankedQueue(Player player, NormalLadder ladder) {
    createUnrankedQueue(player, ladder, false);
  }

  public void createUnrankedQueue(Player player, NormalLadder ladder, boolean keepInventoryOpen) {
    if (ladder.isFrozen()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getQueues()
              .getUnranked()
              .getLadderFrozen()
              .replace("%ladder%", ladder.getDisplayName()));
      return;
    }

    if (!ladder.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getQueues()
              .getUnranked()
              .getLadderDisabled()
              .replace("%ladder%", ladder.getDisplayName()));
      return;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile.getUnrankedLeft() == 0 && !player.hasPermission("ap.bypass.unranked.limit")) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getQueues().getUnranked().getNoUnrankedLeft());
      if (!keepInventoryOpen) {
        player.closeInventory();
      }
      return;
    }

    toggleQueue(player, ladder, false, keepInventoryOpen);
  }

  public void createRankedQueue(Player player, NormalLadder ladder) {
    createRankedQueue(player, ladder, false);
  }

  public void createRankedQueue(Player player, NormalLadder ladder, boolean keepInventoryOpen) {
    if (ladder.isFrozen()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getQueues()
              .getRanked()
              .getLadderFrozen()
              .replace("%ladder%", ladder.getDisplayName()));
      return;
    }

    if (!ladder.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getQueues()
              .getRanked()
              .getLadderDisabled()
              .replace("%ladder%", ladder.getDisplayName()));
      return;
    }

    if (PlayerUtil.getPing(player)
        > PracticeConfig.settings().getQueue().getRanked().getMaxPing()) {
      Common.sendMmMessage(player, PracticeConfig.language().getQueues().getRanked().getHighPing());
      if (!keepInventoryOpen) {
        player.closeInventory();
      }
      return;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (!player.hasPermission("ap.bypass.ranked.requirements")) {
      Division requirement = DivisionManager.getInstance().getMinimumForRanked();
      if (requirement != null && !DivisionManager.getInstance().meetsMinimumForRanked(profile)) {
        DivisionManager.getInstance().sendRankedProgressMessage(player, profile, requirement);

        if (!keepInventoryOpen) {
          player.closeInventory();
        }
        return;
      }
    }

    if (profile.getRankedLeft() == 0 && !player.hasPermission("ap.bypass.ranked.limit")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getQueues().getRanked().getNoRankedLeft());
      if (!keepInventoryOpen) {
        player.closeInventory();
      }
      return;
    }

    if (profile.getRankedBan().isBanned()) {
      String bannerName = "Console";
      if (profile.getRankedBan().getBanner() != null
          && profile.getRankedBan().getBanner().getPlayer() != null) {
        bannerName = profile.getRankedBan().getBanner().getPlayer().getName();
      }

      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getQueues()
              .getRanked()
              .getBanned()
              .replace("%banner%", bannerName != null ? bannerName : "")
              .replace(
                  "%reason%",
                  profile.getRankedBan().getReason() == null
                      ? PracticeConfig.language().getQueues().getRanked().getNoReason()
                      : profile.getRankedBan().getReason()));

      if (!keepInventoryOpen) {
        player.closeInventory();
      }
      return;
    }

    toggleQueue(player, ladder, true, keepInventoryOpen);
  }

  public void endAllQueuesForPlayer(Player player, boolean foundMatch, String message) {
    Queue queue = getQueue(player);
    if (queue != null) {
      queue.endQueue(foundMatch, message);
    }
  }

  /** Cancels all regular and custom-kit queues for a player and restores their lobby state. */
  public void leaveQueue(Player player) {
    endAllQueuesForPlayer(player, false, null);
    if (!CustomKitQueueManager.getInstance().cancelJoinSearch(player, true, true)) {
      CustomKitQueueManager.getInstance().cancelHostedQueue(player, true, true);
    }
  }

  public boolean isMultiQueueAllowed(Player player) {
    if (!PracticeConfig.settings().getQueue().getMulti().isEnabled()) {
      return false;
    }

    String permission = getMultiQueuePermissionNode();
    return player.hasPermission(permission);
  }

  private String getMultiQueuePermissionNode() {
    String permission = PracticeConfig.settings().getQueue().getMulti().getPermission();
    return permission == null || permission.isEmpty() ? MULTI_QUEUE_PERMISSION_DEFAULT : permission;
  }

  private int getMultiQueueLimit() {
    return PracticeConfig.settings().getQueue().getMulti().getMaxLadders();
  }

  private void toggleQueue(
      Player player, NormalLadder ladder, boolean ranked, boolean keepInventoryOpen) {
    Queue existingQueue = getQueue(player);
    boolean allowMultiQueue = isMultiQueueAllowed(player);
    boolean preserveOpenInventory = keepInventoryOpen && allowMultiQueue;

    if (existingQueue != null && existingQueue.isRanked() != ranked) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getQueues().getMulti().getCantMixTypes());
      return;
    }

    if (existingQueue == null) {
      if (!preserveOpenInventory) {
        player.closeInventory();
      }

      Queue queue = new Queue(player, ladder, ranked, preserveOpenInventory);
      queue.startQueue();
      return;
    }

    if (!allowMultiQueue) {
      if (existingQueue.isQueued(ladder)) {
        endAllQueuesForPlayer(
            player,
            false,
            PracticeConfig.language()
                .getQueues()
                .getQueueStopped()
                .replace(
                    "%weightClass%",
                    ranked ? WeightClass.RANKED.getMmName() : WeightClass.UNRANKED.getMmName())
                .replace("%ladder%", ladder.getDisplayName()));
        return;
      }

      endAllQueuesForPlayer(player, false, null);

      player.closeInventory();

      Queue queue = new Queue(player, ladder, ranked, false);
      queue.startQueue();
      return;
    }

    if (existingQueue.isQueued(ladder)) {
      existingQueue.removeLadder(ladder);

      if (existingQueue.getQueuedLadders().isEmpty()) {
        endAllQueuesForPlayer(
            player,
            false,
            PracticeConfig.language()
                .getQueues()
                .getQueueStopped()
                .replace(
                    "%weightClass%",
                    ranked ? WeightClass.RANKED.getMmName() : WeightClass.UNRANKED.getMmName())
                .replace("%ladder%", ladder.getDisplayName()));
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getQueues()
                .getQueueStopped()
                .replace(
                    "%weightClass%",
                    ranked ? WeightClass.RANKED.getMmName() : WeightClass.UNRANKED.getMmName())
                .replace("%ladder%", ladder.getDisplayName()));
      }
      return;
    }

    int limit = getMultiQueueLimit();
    if (limit > 0 && existingQueue.getQueuedLadders().size() >= limit) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getQueues()
              .getMulti()
              .getMaxLadders()
              .replace("%max%", String.valueOf(limit)));
      return;
    }

    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getQueues()
            .getQueueStart()
            .replace(
                "%weightClass%",
                ranked ? WeightClass.RANKED.getMmName() : WeightClass.UNRANKED.getMmName())
            .replace("%ladder%", ladder.getDisplayName()));

    existingQueue.addLadder(ladder);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent e) {
    Player player = e.getPlayer();
    endAllQueuesForPlayer(player, false, null);
  }
}
