package dev.lokspel.practice.manager.queue;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.settings.QueueSettings;
import dev.lokspel.practice.event.ProfileStatusChangeEvent;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.fight.match.type.duel.Duel;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.lokspel.practice.manager.ladder.util.LadderUtil;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.queue.runnables.CustomKitSearchRunnable;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class CustomKitQueueManager implements Listener {

  private static final String ACTION_BAR_ID = "queue";

  private static CustomKitQueueManager instance;

  public static CustomKitQueueManager getInstance() {
    if (instance == null) {
      if (AstralPractice.getInstance() == null || !AstralPractice.getInstance().isEnabled()) {
        return null;
      }
      instance = new CustomKitQueueManager();
    }
    return instance;
  }

  private CustomKitQueueManager() {
    Bukkit.getPluginManager().registerEvents(this, AstralPractice.getInstance());
  }

  @Getter private final Map<UUID, HostedCustomKitQueue> hostedQueues = new LinkedHashMap<>();
  @Getter private final Map<UUID, CustomKitSearchRunnable> joinSearches = new LinkedHashMap<>();

  public boolean isCustomKitQueueEnabled() {
    QueueSettings.CustomKit customKit =
        PracticeConfig.settings().getQueue().getUnranked().getCustomKit();
    Boolean enabled = customKit != null ? customKit.getEnabled() : null;

    // Keep feature enabled by default to avoid breaking old configs that don't have the key yet.
    return enabled == null || enabled;
  }

  public List<CustomLadder> getHostableKits(Profile profile) {
    List<CustomLadder> hostable = new ArrayList<>();
    if (profile == null) {
      return hostable;
    }

    for (CustomLadder customLadder : profile.getCustomLadders()) {
      if (customLadder != null && customLadder.isEnabled()) {
        hostable.add(customLadder);
      }
    }

    return hostable;
  }

  public HostedCustomKitQueue getHostedQueue(Player host) {
    if (host == null) {
      return null;
    }

    return hostedQueues.get(host.getUniqueId());
  }

  public CustomKitSearchRunnable getJoinSearch(Player joiner) {
    if (joiner == null) {
      return null;
    }

    return joinSearches.get(joiner.getUniqueId());
  }

  public boolean hostQueue(Player host, CustomLadder customLadder) {
    if (host == null || customLadder == null) {
      return false;
    }

    if (!isCustomKitQueueEnabled()) {
      Common.sendMmMessage(host, PracticeConfig.language().getCantUseCommand());
      return false;
    }

    Profile profile = ProfileManager.getInstance().getProfile(host);
    if (profile == null || profile.getStatus() != ProfileStatus.LOBBY) {
      Common.sendMmMessage(host, PracticeConfig.language().getCantUseCommand());
      return false;
    }

    if (!customLadder.isEnabled()) {
      Common.sendMmMessage(
          host, PracticeConfig.language().getQueues().getCustom().getKitNotReady());
      return false;
    }

    if (customLadder.getAvailableArenas().isEmpty()) {
      Common.sendMmMessage(host, PracticeConfig.language().getQueues().getNoAvailableArena());
      return false;
    }

    hostedQueues.put(
        host.getUniqueId(),
        new HostedCustomKitQueue(host.getUniqueId(), customLadder, System.currentTimeMillis()));

    InventoryManager.getInstance().setMatchQueueInventory(host);

    CustomKitSearchRunnable hostSearch = joinSearches.get(host.getUniqueId());
    if (hostSearch != null) {
      hostSearch.cancel();
    }

    hostSearch =
        new CustomKitSearchRunnable(
            host.getUniqueId(),
            CustomKitSearchRunnable.SearchMode.HOST,
            customLadder.getDisplayName());
    joinSearches.put(host.getUniqueId(), hostSearch);
    hostSearch.begin();

    Common.sendMmMessage(
        host,
        PracticeConfig.language()
            .getQueues()
            .getCustom()
            .getHosted()
            .replace("%kit%", customLadder.getDisplayName()));

    tryMatchHostedQueue(host);
    return true;
  }

  public boolean startJoinSearch(Player joiner) {
    if (joiner == null) {
      return false;
    }

    if (!isCustomKitQueueEnabled()) {
      Common.sendMmMessage(joiner, PracticeConfig.language().getCantUseCommand());
      return false;
    }

    Profile profile = ProfileManager.getInstance().getProfile(joiner);
    if (profile == null || profile.getStatus() != ProfileStatus.LOBBY) {
      Common.sendMmMessage(joiner, PracticeConfig.language().getCantUseCommand());
      return false;
    }

    if (joinSearches.containsKey(joiner.getUniqueId())) {
      return true;
    }

    InventoryManager.getInstance().setMatchQueueInventory(joiner);

    CustomKitSearchRunnable search =
        new CustomKitSearchRunnable(
            joiner.getUniqueId(), CustomKitSearchRunnable.SearchMode.JOIN_OTHERS, null);
    joinSearches.put(joiner.getUniqueId(), search);
    search.begin();

    Common.sendMmMessage(
        joiner, PracticeConfig.language().getQueues().getCustom().getWaitingForHost());
    return true;
  }

  public boolean cancelHostedQueue(Player host, boolean sendMessage, boolean setLobbyInventory) {
    if (host == null) {
      return false;
    }

    HostedCustomKitQueue removed = hostedQueues.remove(host.getUniqueId());
    if (removed == null) {
      return false;
    }

    if (host.isOnline()) {
      cancelJoinSearch(host, false, false);

      if (setLobbyInventory) {
        InventoryManager.getInstance().setLobbyInventory(host, false);
      }

      clearQueueActionBar(host);

      if (sendMessage) {
        Common.sendMmMessage(
            host, PracticeConfig.language().getQueues().getCustom().getCancelled());
      }
    }

    return true;
  }

  public boolean cancelJoinSearch(Player joiner, boolean sendMessage, boolean setLobbyInventory) {
    if (joiner == null) {
      return false;
    }

    CustomKitSearchRunnable search = joinSearches.remove(joiner.getUniqueId());
    if (search == null) {
      return false;
    }

    search.cancel();

    if (joiner.isOnline()) {
      if (setLobbyInventory) {
        InventoryManager.getInstance().setLobbyInventory(joiner, false);
      }

      clearQueueActionBar(joiner);

      if (sendMessage) {
        Common.sendMmMessage(
            joiner, PracticeConfig.language().getQueues().getCustom().getSearchCancelled());
      }
    }

    return true;
  }

  public boolean tryMatchHostedQueue(Player host) {
    HostedCustomKitQueue hostedQueue = hostedQueues.get(host.getUniqueId());
    if (hostedQueue == null) {
      return false;
    }

    for (CustomKitSearchRunnable search : new ArrayList<>(joinSearches.values())) {
      if (search.getMode() != CustomKitSearchRunnable.SearchMode.JOIN_OTHERS) {
        continue;
      }

      Player joiner = Bukkit.getPlayer(search.getPlayerUuid());
      if (joiner == null || !joiner.isOnline()) {
        continue;
      }

      Profile joinerProfile = ProfileManager.getInstance().getProfile(joiner);
      if (joinerProfile == null || joinerProfile.getStatus() != ProfileStatus.QUEUE) {
        cancelJoinSearch(joiner, false, false);
        continue;
      }

      if (startHostedMatch(host, joiner, hostedQueue)) {
        return true;
      }
    }

    return false;
  }

  public boolean tryJoinAnyHostedQueue(Player joiner) {
    for (HostedCustomKitQueue hostedQueue : new ArrayList<>(hostedQueues.values())) {
      Player host = Bukkit.getPlayer(hostedQueue.hostUuid());
      if (!isJoinable(host, joiner, hostedQueue)) {
        continue;
      }

      if (startHostedMatch(host, joiner, hostedQueue)) {
        return true;
      }
    }

    return false;
  }

  private boolean startHostedMatch(Player host, Player joiner, HostedCustomKitQueue selectedQueue) {
    if (selectedQueue == null || host == null || joiner == null) {
      return false;
    }

    CustomLadder hostedKit = selectedQueue.customLadder();
    Arena arena = LadderUtil.getAvailableArena(hostedKit);
    if (arena == null) {
      hostedQueues.remove(host.getUniqueId());
      Common.sendMmMessage(host, PracticeConfig.language().getQueues().getNoAvailableArena());
      InventoryManager.getInstance().setLobbyInventory(host, false);
      return false;
    }

    hostedQueues.remove(host.getUniqueId());
    cancelJoinSearch(host, false, false);
    cancelJoinSearch(joiner, false, false);

    clearQueueActionBar(host);
    clearQueueActionBar(joiner);

    host.closeInventory();
    joiner.closeInventory();

    Common.sendMmMessage(
        host,
        PracticeConfig.language()
            .getQueues()
            .getCustom()
            .getMatchFound()
            .replace("%player%", joiner.getName()));
    Common.sendMmMessage(
        joiner,
        PracticeConfig.language()
            .getQueues()
            .getCustom()
            .getJoined()
            .replace("%player%", host.getName())
            .replace("%kit%", hostedKit.getDisplayName()));

    Duel duel = new Duel(hostedKit, arena, List.of(host, joiner), false, hostedKit.getRounds());
    if (!duel.startMatch()) {
      InventoryManager.getInstance().setLobbyInventory(host, false);
      InventoryManager.getInstance().setLobbyInventory(joiner, false);
      return false;
    }
    return true;
  }

  private boolean isJoinable(Player host, Player joiner, HostedCustomKitQueue hostedQueue) {
    if (host == null || !host.isOnline()) {
      if (hostedQueue != null) {
        hostedQueues.remove(hostedQueue.hostUuid());
      }
      return false;
    }

    if (host.equals(joiner)) {
      return false;
    }

    Profile hostProfile = ProfileManager.getInstance().getProfile(host);
    if (hostProfile == null || hostProfile.getStatus() != ProfileStatus.QUEUE) {
      hostedQueues.remove(hostedQueue.hostUuid());
      return false;
    }

    Profile joinerProfile = ProfileManager.getInstance().getProfile(joiner);
    if (joinerProfile != null
        && (hostProfile.getIgnoredPlayers().contains(joinerProfile)
            || joinerProfile.getIgnoredPlayers().contains(hostProfile))) {
      return false;
    }

    if (hostedQueue.customLadder() == null || !hostedQueue.customLadder().isEnabled()) {
      hostedQueues.remove(hostedQueue.hostUuid());
      InventoryManager.getInstance().setLobbyInventory(host, false);
      Common.sendMmMessage(
          host, PracticeConfig.language().getQueues().getCustom().getKitNotReady());
      return false;
    }

    return true;
  }

  private void clearQueueActionBar(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile != null) {
      profile.getActionBar().removeMessage(ACTION_BAR_ID);
    }
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent e) {
    cancelHostedQueue(e.getPlayer(), false, false);
    cancelJoinSearch(e.getPlayer(), false, false);
  }

  @EventHandler
  public void onProfileStatusChange(ProfileStatusChangeEvent e) {
    if (e.getOldStatus() != ProfileStatus.QUEUE) {
      return;
    }

    if (e.getNewStatus() == ProfileStatus.QUEUE) {
      return;
    }

    Player player = e.getProfile().getPlayer().getPlayer();
    if (player == null) {
      return;
    }

    cancelHostedQueue(player, false, false);
    cancelJoinSearch(player, false, false);
  }

  public record HostedCustomKitQueue(UUID hostUuid, CustomLadder customLadder, long startedAtMs) {

    public long getElapsedSeconds() {
      return (System.currentTimeMillis() - startedAtMs) / 1000L;
    }
  }
}
