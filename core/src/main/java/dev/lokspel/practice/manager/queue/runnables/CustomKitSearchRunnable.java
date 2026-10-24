package dev.lokspel.practice.manager.queue.runnables;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.queue.CustomKitQueueManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.TimeUtil;
import dev.lokspel.practice.util.actionbar.ActionBarPriority;
import dev.lokspel.practice.util.interfaces.Runnable;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CustomKitSearchRunnable extends Runnable {

  private static final int MAX_JOIN_SEARCH_TIME =
      PracticeConfig.settings().getQueue().getUnranked().getMaxQueueTime();
  private static final String ACTION_BAR_ID = "queue";

  @Getter private final UUID playerUuid;
  @Getter private final SearchMode mode;
  private final String hostedKitName;
  @Getter private final long startedAtMs;

  public CustomKitSearchRunnable(UUID playerUuid, SearchMode mode, String hostedKitName) {
    super(0, 20L, false);
    this.playerUuid = playerUuid;
    this.mode = mode;
    this.hostedKitName = hostedKitName;
    this.startedAtMs = System.currentTimeMillis();
  }

  @Override
  public void run() {
    Player player = Bukkit.getPlayer(playerUuid);
    if (player == null || !player.isOnline()) {
      CustomKitQueueManager.getInstance().getJoinSearches().remove(playerUuid);
      cancel();
      return;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null || profile.getStatus() != ProfileStatus.QUEUE) {
      CustomKitQueueManager.getInstance().cancelJoinSearch(player, false, false);
      return;
    }

    if (mode == SearchMode.JOIN_OTHERS
        && CustomKitQueueManager.getInstance().tryJoinAnyHostedQueue(player)) {
      return;
    }

    if (mode == SearchMode.HOST
        && CustomKitQueueManager.getInstance().tryMatchHostedQueue(player)) {
      return;
    }

    if (seconds >= MAX_JOIN_SEARCH_TIME) {
      if (mode == SearchMode.HOST) {
        CustomKitQueueManager.getInstance().cancelHostedQueue(player, false, true);
      }

      CustomKitQueueManager.getInstance().cancelJoinSearch(player, false, true);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getQueues()
              .getNoMatchInTime()
              .replace("%maxTime%", String.valueOf(MAX_JOIN_SEARCH_TIME / 60)));
      return;
    }

    String searchingActionbar =
        mode == SearchMode.HOST
            ? PracticeConfig.language().getQueues().getCustom().getHostingActionbar()
            : PracticeConfig.language().getQueues().getCustom().getSearchingActionbar();

    if (searchingActionbar != null && !searchingActionbar.isBlank()) {
      profile
          .getActionBar()
          .setMessage(
              ACTION_BAR_ID,
              searchingActionbar
                  .replace("%elapsed%", TimeUtil.formatDuration(seconds * 1000L))
                  .replace("%kit%", hostedKitName == null ? "-" : hostedKitName),
              -1,
              ActionBarPriority.NORMAL);
    } else {
      profile.getActionBar().removeMessage(ACTION_BAR_ID);
    }

    seconds++;
  }

  public long getElapsedSeconds() {
    return (System.currentTimeMillis() - startedAtMs) / 1000L;
  }

  public enum SearchMode {
    HOST,
    JOIN_OTHERS
  }
}
