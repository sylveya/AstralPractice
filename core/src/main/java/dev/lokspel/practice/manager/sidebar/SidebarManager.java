package dev.lokspel.practice.manager.sidebar;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.sidebar.adapter.PracticeAdapter;
import dev.lokspel.practice.manager.sidebar.adapter.SidebarAdapter;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public class SidebarManager implements Listener {

  private static SidebarManager instance;

  public static SidebarManager getInstance() {
    if (instance == null) {
      instance = new SidebarManager();
    }
    return instance;
  }

  @Getter private ScoreboardLibrary scoreboardLibrary;

  private final Map<UUID, PracticeSidebar> boards = new ConcurrentHashMap<>();
  @Getter private final SidebarAdapter sidebarAdapter;
  private BukkitTask updateTask;

  private SidebarManager() {
    this.sidebarAdapter = new PracticeAdapter();

    Bukkit.getServer().getPluginManager().registerEvents(this, AstralPractice.getInstance());
  }

  public void load() {
    if (!isSidebarGloballyEnabled()) {
      return;
    }

    ensureScoreboardLibrary();
    startUpdateTask();
  }

  public boolean isSidebarGloballyEnabled() {
    return PracticeConfig.settings().getSidebar().isEnabled();
  }

  private void ensureScoreboardLibrary() {
    if (scoreboardLibrary != null) {
      return;
    }

    try {
      scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(AstralPractice.getInstance());
    } catch (NoPacketAdapterAvailableException e) {
      // If no packet adapter was found, you can fallback to the no-op implementation:
      scoreboardLibrary = new NoopScoreboardLibrary();
      Common.sendConsoleMmMessage("<red>No scoreboard packet adapter available!");
    }
  }

  private void startUpdateTask() {
    if (!isSidebarGloballyEnabled()) {
      stopUpdateTask();
      return;
    }

    if (updateTask != null) {
      updateTask.cancel();
    }

    updateTask =
        Bukkit.getScheduler()
            .runTaskTimerAsynchronously(
                AstralPractice.getInstance(),
                () -> {
                  for (PracticeSidebar practiceSidebar : boards.values()) {
                    if (practiceSidebar == null
                        || practiceSidebar.getSidebar() == null
                        || practiceSidebar.getSidebar().closed()) {
                      continue;
                    }
                    practiceSidebar.update();
                  }
                },
                20L,
                PracticeConfig.settings().getSidebar().getUpdateTime());
  }

  private void stopUpdateTask() {
    if (updateTask != null) {
      updateTask.cancel();
      updateTask = null;
    }
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent e) {
    Player player = e.getPlayer();

    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> {
              if (!isSidebarGloballyEnabled()) {
                return;
              }

              // Don't call profile.load() here — the profile is already loaded,
              // and reloading from disk would overwrite an unsaved toggle.
              Profile profile = ProfileManager.getInstance().getProfile(player);
              if (profile == null) {
                return;
              }

              if (profile.isSidebar()) {
                loadSidebar(player);
              }
            },
            10L);
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    unLoadSidebar(e.getPlayer().getUniqueId());
  }

  public void loadSidebar(Player player) {
    if (!isSidebarGloballyEnabled() || player == null) {
      return;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null || !profile.isSidebar()) {
      return;
    }

    UUID uuid = player.getUniqueId();
    // If sidebar already exists, ensure it's still open before returning
    PracticeSidebar existing = boards.get(uuid);
    if (existing != null) {
      if (existing.getSidebar() != null && !existing.getSidebar().closed()) {
        return;
      }
      // If the sidebar was closed, remove it so we can recreate it
      boards.remove(uuid);
    }

    ensureScoreboardLibrary();

    boards.put(uuid, new PracticeSidebar(this, scoreboardLibrary.createSidebar(), player));
  }

  public void unLoadSidebar(Player player) {
    if (player != null) {
      unLoadSidebar(player.getUniqueId());
    }
  }

  public void unLoadSidebar(UUID uuid) {
    if (uuid == null) {
      return;
    }
    PracticeSidebar sidebar = boards.remove(uuid);
    if (sidebar != null && sidebar.getSidebar() != null) {
      sidebar.getSidebar().close();
    }
  }

  /**
   * Immediately updates a specific player's scoreboard. This is useful for real-time updates (e.g.,
   * hit counter).
   *
   * @param player the player whose scoreboard should be updated
   */
  public void updatePlayerSidebar(Player player) {
    if (!isSidebarGloballyEnabled() || player == null) {
      return;
    }

    PracticeSidebar sidebar = boards.get(player.getUniqueId());
    if (sidebar != null && sidebar.getSidebar() != null && !sidebar.getSidebar().closed()) {
      sidebar.update();
    }
  }

  /**
   * Immediately updates multiple players' scoreboards. This is useful for real-time updates
   * involving multiple players.
   *
   * @param players the players whose scoreboards should be updated
   */
  public void updatePlayersSidebar(Player... players) {
    for (Player player : players) {
      updatePlayerSidebar(player);
    }
  }

  public void reloadSidebarConfig() {
    Bukkit.getScheduler()
        .runTask(
            AstralPractice.getInstance(),
            () -> {
              if (!isSidebarGloballyEnabled()) {
                for (UUID uuid : new ArrayList<>(boards.keySet())) {
                  unLoadSidebar(uuid);
                }
                stopUpdateTask();
                return;
              }

              ensureScoreboardLibrary();
              startUpdateTask();

              for (Player player : Bukkit.getOnlinePlayers()) {
                Profile profile = ProfileManager.getInstance().getProfile(player);
                if (profile == null) {
                  continue;
                }

                if (profile.isSidebar()) {
                  loadSidebar(player);
                } else {
                  unLoadSidebar(player);
                }
              }

              for (UUID uuid : new ArrayList<>(boards.keySet())) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                  updatePlayerSidebar(player);
                }
              }
            });
  }

  public void close() {
    stopUpdateTask();

    for (UUID uuid : new ArrayList<>(boards.keySet())) {
      unLoadSidebar(uuid);
    }

    try {
      if (scoreboardLibrary != null) {
        scoreboardLibrary.close();
      }
    } catch (IllegalStateException ignored) {
      // This best-effort operation may fail without interrupting its caller.
    }
  }
}
