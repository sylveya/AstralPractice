package dev.lokspel.practice.manager.server;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.util.ArenaWorldUtil;
import dev.lokspel.practice.manager.backend.BackendManager;
import dev.lokspel.practice.manager.division.DivisionManager;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.ffa.FfaManager;
import dev.lokspel.practice.manager.fight.match.util.EloMode;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.guis.leaderboard.LbSelectorGui;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.manager.gui.setup.hologram.HologramSetupManager;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.nametag.NametagManager;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.playerkit.PlayerKitManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticPresets;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.queue.QueueManager;
import dev.lokspel.practice.manager.sidebar.SidebarManager;
import dev.lokspel.practice.manager.spectator.SpectatorManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.fight.CombatLogUtil;
import dev.lokspel.practice.util.items.GoldenHead;
import dev.lokspel.practice.util.playerutil.PlayerUtil;
import dev.lokspel.practice.util.startup.StartUpTypes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ServerManager implements Listener {

  private final AstralPractice astralPractice;

  private static ServerManager instance;

  public static ServerManager getInstance() {
    if (instance == null) {
      instance = new ServerManager();
    }
    return instance;
  }

  @Getter private static Location lobby = null;
  @Getter private final Map<UUID, WorldEnum> inWorld = new HashMap<>();

  @Getter
  private final Map<String, OfflinePlayer> offlinePlayers =
      new HashMap<>(); // All the player that has ever been on the server is here.

  @Getter private final List<Player> onlineStaffs = new ArrayList<>();

  @Getter private final GoldenHead goldenHead;

  @Getter private final AutoSaveRunnable autoSaveRunnable = new AutoSaveRunnable();
  @Getter private final MariadbSaveRunnable mariadbSaveRunnable = new MariadbSaveRunnable();

  @Getter
  private final InactiveProfileRunnable inactiveProfileRunnable = new InactiveProfileRunnable();

  @Getter private final ProfileLimitRunnable profileLimitRunnable = new ProfileLimitRunnable();

  private ServerManager() {
    this.astralPractice = AstralPractice.getInstance();
    Bukkit.getPluginManager().registerEvents(this, AstralPractice.getInstance());

    this.goldenHead = new GoldenHead();

    start();
  }

  public void start() {
    if (PracticeConfig.settings().getAutoSave().isEnabled()) {
      autoSaveRunnable.begin();
    }
    if (PracticeConfig.settings().getPlayer().getDeleteInactiveUser().isEnabled()) {
      BukkitRunnable runnable =
          new BukkitRunnable() {
            @Override
            public void run() {
              if (AstralPractice.getInstance()
                  .getStartUpProgress()
                  .get(StartUpTypes.PROFILE_LOADING)) {
                getInactiveProfileRunnable().begin();
                this.cancel();
              }
            }
          };
      runnable.runTaskTimer(AstralPractice.getInstance(), 0, 20L * 5);
    }
    if (PracticeConfig.settings().getRanked().getLimit().isEnabled()) {
      profileLimitRunnable.begin();
    }
    if (PracticeConfig.settings().getMariadbDatabase().isEnabled()) {
      mariadbSaveRunnable.begin();
    }

    loadOfflinePlayers();
  }

  public void loadOfflinePlayers() {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            astralPractice,
            () -> {
              for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                offlinePlayers.put(offlinePlayer.getName(), offlinePlayer);
              }
            });
  }

  public void loadLobby() {
    try {
      // Reload backend to ensure data is current now that worlds are loaded.
      BackendManager.reload();

      LobbyData lobbyData = BackendManager.data().getLobby();
      if (lobbyData != null && lobbyData.getSerializedType() != null) {
        // Old format: Location was stored as a ConfigurationSerializable ("==" key).
        // Read it, then immediately migrate to the new primitive-field format so
        // future async saves never attempt to serialize a Location object again.
        World world = lobbyData.getWorld() != null ? Bukkit.getWorld(lobbyData.getWorld()) : null;
        if (world != null) {
          lobby = toLocation(world, lobbyData);
          writeLobbyPrimitives(lobby);
          BackendManager.save();
          Common.sendConsoleMmMessage(
              "<yellow>Migrated lobby location to primitive-field format in backend.yml.");
        }
      } else if (lobbyData != null) {
        // New format: plain fields — world, x, y, z, yaw, pitch.
        String worldName = lobbyData.getWorld();
        if (worldName != null) {
          lobby = toLocation(Bukkit.getWorld(worldName), lobbyData);
        }
      }
    } catch (Exception e) {
      Common.sendConsoleMmMessage("<red>Lobby cannot be found.");
    }
  }

  /**
   * Writes the lobby location to the backend data as individual primitive fields (world name +
   * x/y/z/yaw/pitch) so that no {@link Location} is ever stored in it. Serializing a {@link
   * Location} during async saves can throw a NullPointerException if its world {@code
   * WeakReference} has been cleared.
   */
  private void writeLobbyPrimitives(Location location) {
    // Replaces any existing entry (could be a Location object or an old section)
    LobbyData lobbyData = new LobbyData();
    lobbyData.setWorld(location.getWorld().getName());
    lobbyData.setPosX(location.getX());
    lobbyData.setPosY(location.getY());
    lobbyData.setPosZ(location.getZ());
    lobbyData.setYaw((double) location.getYaw());
    lobbyData.setPitch((double) location.getPitch());
    BackendManager.data().setLobby(lobbyData);
  }

  private static Location toLocation(World world, LobbyData lobbyData) {
    return new Location(
        world,
        orZero(lobbyData.getPosX()),
        orZero(lobbyData.getPosY()),
        orZero(lobbyData.getPosZ()),
        (float) orZero(lobbyData.getYaw()),
        (float) orZero(lobbyData.getPitch()));
  }

  private static double orZero(Double value) {
    return value != null ? value : 0;
  }

  public void setLobby(Player player, Location newLobby) {
    lobby = newLobby;
    newLobby
        .getWorld()
        .setSpawnLocation(newLobby.getBlockX(), newLobby.getBlockY(), newLobby.getBlockZ());
    InventoryManager.getInstance().setLobbyInventory(player, true);

    writeLobbyPrimitives(newLobby);
    BackendManager.save();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onTeleport(PlayerTeleportEvent e) {
    Player player = e.getPlayer();
    UUID uuid = player.getUniqueId();
    Profile profile = ProfileManager.getInstance().getProfile(player);

    WorldEnum from = inWorld.get(uuid);
    World to = e.getTo().getWorld();

    inWorld.remove(uuid);

    if (lobby != null && to.equals(lobby.getWorld())) {
      if (from != null && from.equals(WorldEnum.OTHER)) {
        InventoryManager.getInstance().setLobbyInventory(player, true);
        if (profile.isSidebar()) {
          SidebarManager.getInstance().loadSidebar(player);
        }

        final Profile profile1 = profile;
        Bukkit.getScheduler()
            .runTaskLater(
                AstralPractice.getInstance(),
                () -> {
                  PlayerUtil.setPlayerWorldTime(player);

                  if (PracticeConfig.settings().getStaffMode().isJoinHideFromPlayers()
                      && player.hasPermission("ap.staffmode")) {
                    profile1.setHideFromPlayers(true);
                  }
                },
                10L);
      }

      inWorld.put(uuid, WorldEnum.LOBBY);
    } else if (ArenaWorldUtil.isArenaOrEventWorld(to)) {
      inWorld.put(uuid, WorldEnum.ARENA);
    } else {
      if (from != null && !from.equals(WorldEnum.OTHER)) {
        if (profile.getStatus().equals(ProfileStatus.LOBBY)) {
          ProfileManager.getInstance().getProfile(player).setStatus(ProfileStatus.OFFLINE);
          SidebarManager.getInstance().unLoadSidebar(player);
        } else if (profile.getStatus().equals(ProfileStatus.QUEUE)) {
          QueueManager.getInstance().endAllQueuesForPlayer(player, true, null);
          profile.setStatus(ProfileStatus.OFFLINE);
          SidebarManager.getInstance().unLoadSidebar(player);
        }
      }

      inWorld.put(uuid, WorldEnum.OTHER);
    }
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent e) {
    Player player = e.getPlayer();

    if (ServerManager.getLobby() == null) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getSetServerLobby());
      if (player.isOp()) {
        Common.sendMmMessage(player, PracticeConfig.language().getSetServerLobby());
      }
    }

    // Add player to the offline players list if they aren't in it.
    if (!offlinePlayers.containsKey(player.getName())) {
      offlinePlayers.put(player.getName(), player);
    }

    if (lobby == null) {
      inWorld.put(player.getUniqueId(), WorldEnum.OTHER);
    } else {
      inWorld.put(player.getUniqueId(), WorldEnum.LOBBY);
    }
  }

  public boolean reloadFiles() {
    // A broken configuration file keeps its previous values; the rest is reloaded regardless.
    boolean configurationReloaded = true;
    try {
      PracticeConfig.reload();
    } catch (RuntimeException e) {
      Common.sendConsoleMmMessage(
          "<red>Failed to reload practice files: " + PracticeConfig.describeFailure(e));
      configurationReloaded = false;
    }

    try {
      EloMode.validateConfig();
      CosmeticPresets.load();
      InventoryManager.getInstance().reload();
      DivisionManager.getInstance().reloadRanks();
      BackendManager.reload();
      CombatLogUtil.getInstance().reload();
      loadLobby();
      SidebarManager.getInstance().reloadSidebarConfig();
      goldenHead.reload();
      NametagManager.getInstance().reload();
      // A failed configuration reload keeps the GUIs built from the previous values.
      if (configurationReloaded) {
        reloadGuis();
      }

      return configurationReloaded;
    } catch (Exception e) {
      Common.sendConsoleMmMessage(
          "<red>Failed to reload practice files: " + PracticeConfig.describeFailure(e));
      return false;
    }
  }

  /**
   * Re-creates the GUIs that live across opens, so they show the reloaded files. Open GUIs are
   * closed for their viewers. Skipped while the plugin is still starting, the GUIs are created from
   * the reloaded files once their data has loaded.
   */
  private void reloadGuis() {
    if (!AstralPractice.isFullyLoaded()) {
      return;
    }

    // Runs first: it needs to know which kit editors are open.
    PlayerKitManager.getInstance().reloadGuis();
    GuiManager.getInstance().reload();
    LbSelectorGui.resetSharedGuis();
    LadderManager.getInstance().reloadGuis();
    ArenaGuiSetupManager.getInstance().reloadGuis();
    HologramSetupManager.getInstance().reloadGuis();
    EventManager.getInstance().reloadGuis();
    FfaManager.getInstance().reloadGuis();
    PartyManager.getInstance().reloadGuis();
    SpectatorManager.getInstance().reloadGuis();
    ProfileManager.getInstance().reloadSettingsGuis();
  }

  public void onPlayerQuit(Player player) {
    if (player == null) {
      return;
    }

    inWorld.remove(player.getUniqueId());
    onlineStaffs.remove(player);
  }

  public WorldEnum getWorld(Player player) {
    return player == null ? null : inWorld.get(player.getUniqueId());
  }

  public WorldEnum getWorld(UUID uuid) {
    return uuid == null ? null : inWorld.get(uuid);
  }

  public boolean isInWorld(Player player, WorldEnum worldEnum) {
    return getWorld(player) == worldEnum;
  }

  public void alertPlayers(String permission, String message) {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            AstralPractice.getInstance(),
            () -> {
              for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission(permission)) {
                  Common.sendMmMessage(player, message);
                }
              }
            });
  }

  /**
   * Resolves a player by name. First checks currently online players (exact match), then falls back
   * to the offlinePlayers map. This ensures that online players are always found even if the
   * offlinePlayers map is in an inconsistent state due to the async loading in {@link
   * #loadOfflinePlayers()}.
   *
   * @param name the player name to look up
   * @return the matching OfflinePlayer, or {@code null} if not found
   */
  public OfflinePlayer resolvePlayer(String name) {
    // First try online players — this is always reliable
    Player online = Bukkit.getPlayerExact(name);
    if (online != null) {
      return online;
    }

    // Fall back to the offline map
    return offlinePlayers.get(name);
  }

  public static void runConsoleCommand(String command) {
    if (!AstralPractice.getInstance().isEnabled()) {
      return;
    }
    ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
    Bukkit.dispatchCommand(console, command);
  }
}
