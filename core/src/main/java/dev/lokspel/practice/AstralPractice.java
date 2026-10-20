package dev.lokspel.practice;

import com.github.retrooper.packetevents.PacketEvents;
import dev.faststats.ErrorTracker;
import dev.faststats.bukkit.BukkitContext;
import dev.lokspel.api.AstralPracticeApi;
import dev.lokspel.api.AstralPracticeApiImpl;
import dev.lokspel.api.event.AstralPracticeReadyEvent;
import dev.lokspel.practice.api.EventApiImpl;
import dev.lokspel.practice.api.FfaApiImpl;
import dev.lokspel.practice.api.LadderApiImpl;
import dev.lokspel.practice.api.MatchApiImpl;
import dev.lokspel.practice.api.PartyApiImpl;
import dev.lokspel.practice.api.PlayerApiImpl;
import dev.lokspel.practice.api.QueueApiImpl;
import dev.lokspel.practice.api.StatsApiImpl;
import dev.lokspel.practice.command.arena.ArenaCommand;
import dev.lokspel.practice.command.event.EventCommand;
import dev.lokspel.practice.command.ffa.FfaCommand;
import dev.lokspel.practice.command.ladder.LadderCommand;
import dev.lokspel.practice.command.party.PartyCommand;
import dev.lokspel.practice.command.practice.PracticeCommand;
import dev.lokspel.practice.command.privatemessage.MessageCommand;
import dev.lokspel.practice.command.privatemessage.ReplyCommand;
import dev.lokspel.practice.command.singlecommands.AcceptCommand;
import dev.lokspel.practice.command.singlecommands.CopyKitCommand;
import dev.lokspel.practice.command.singlecommands.CosmeticsCommand;
import dev.lokspel.practice.command.singlecommands.CustomQueueCommand;
import dev.lokspel.practice.command.singlecommands.DivisionsCommand;
import dev.lokspel.practice.command.singlecommands.DuelCommand;
import dev.lokspel.practice.command.singlecommands.EditorCommand;
import dev.lokspel.practice.command.singlecommands.IgnoreQueueCommand;
import dev.lokspel.practice.command.singlecommands.LeaveCommand;
import dev.lokspel.practice.command.singlecommands.MatchHistoryCommand;
import dev.lokspel.practice.command.singlecommands.MatchStatsCommand;
import dev.lokspel.practice.command.singlecommands.NickCommand;
import dev.lokspel.practice.command.singlecommands.PreviewCommand;
import dev.lokspel.practice.command.singlecommands.RankedCommand;
import dev.lokspel.practice.command.singlecommands.SettingsCommand;
import dev.lokspel.practice.command.singlecommands.SetupCommand;
import dev.lokspel.practice.command.singlecommands.SpectateCommand;
import dev.lokspel.practice.command.singlecommands.StatisticsCommand;
import dev.lokspel.practice.command.singlecommands.UnrankedCommand;
import dev.lokspel.practice.command.staff.StaffCommand;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.listener.AsyncPlayerPreLogin;
import dev.lokspel.practice.listener.EntityDamage;
import dev.lokspel.practice.listener.ItemConsume;
import dev.lokspel.practice.listener.PlayerChatListener;
import dev.lokspel.practice.listener.PlayerCommandPreprocess;
import dev.lokspel.practice.listener.PlayerInteract;
import dev.lokspel.practice.listener.PlayerJoin;
import dev.lokspel.practice.listener.PlayerQuit;
import dev.lokspel.practice.listener.ProjectileLaunch;
import dev.lokspel.practice.listener.StatisticListener;
import dev.lokspel.practice.listener.WeatherChange;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.listener.ArenaCopyUtilListener;
import dev.lokspel.practice.manager.arena.listener.ArenaListener;
import dev.lokspel.practice.manager.arena.setup.SpawnMarkerManager;
import dev.lokspel.practice.manager.arena.util.ArenaWorldUtil;
import dev.lokspel.practice.manager.backend.BackendManager;
import dev.lokspel.practice.manager.backend.database.Database;
import dev.lokspel.practice.manager.division.DivisionManager;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.setup.EventSpawnMarkerManager;
import dev.lokspel.practice.manager.fight.ffa.FfaListener;
import dev.lokspel.practice.manager.fight.ffa.FfaManager;
import dev.lokspel.practice.manager.fight.listener.BuildListener;
import dev.lokspel.practice.manager.fight.listener.CreeperListener;
import dev.lokspel.practice.manager.fight.listener.FireworkRocketCooldownListener;
import dev.lokspel.practice.manager.fight.listener.ProjectileCooldownListener;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.util.EloMode;
import dev.lokspel.practice.manager.fight.util.EntityHider;
import dev.lokspel.practice.manager.fight.util.EntityHiderListener;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.leaderboard.LeaderboardManager;
import dev.lokspel.practice.manager.leaderboard.hologram.HologramManager;
import dev.lokspel.practice.manager.leaderboard.hologram.HologramProtectionListener;
import dev.lokspel.practice.manager.matchhistory.MatchHistoryManager;
import dev.lokspel.practice.manager.nametag.NametagManager;
import dev.lokspel.practice.manager.playerkit.PlayerKitManager;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticPresets;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsCategoryManager;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.lokspel.practice.manager.server.ServerManager;
import dev.lokspel.practice.manager.sidebar.SidebarManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.SoftDependUtil;
import dev.lokspel.practice.util.StartUpTypes;
import dev.lokspel.practice.util.StartUpUtil;
import dev.lokspel.practice.util.UpdateChecker;
import dev.lokspel.practice.util.VersionChecker;
import dev.lokspel.practice.util.minimessage.MiniMessageUtil;
import dev.lokspel.practice.util.placeholderapi.PlayerExpansion;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import java.io.File;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import me.whereareiam.configura.exception.ConfigException;
import me.whereareiam.strata.exception.MigrationException;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class AstralPractice extends JavaPlugin {

  private static final List<String> DEFAULT_LADDERS =
      List.of(
          "archer",
          "axe",
          "battlerush",
          "bedwars",
          "boxing",
          "bridges",
          "builduhc",
          "cart",
          "creeper",
          "crystal",
          "diamondpotion",
          "fireball",
          "gapple",
          "mace",
          "mlgrush",
          "netheritepotion",
          "pearlfight",
          "sg",
          "skywars",
          "soup",
          "spearelytra",
          "spearmace",
          "spleef",
          "sumo",
          "sword",
          "tntsumo");

  private boolean configurationLoaded;

  @Getter
  private final Map<StartUpTypes, Boolean> startUpProgress = new EnumMap<>(StartUpTypes.class);

  @Getter private static AstralPractice instance;
  @Getter private static MiniMessage miniMessage = MiniMessageUtil.createMiniMessage();
  @Getter private static EntityHider entityHider;
  @Getter private static ArenaCopyUtilListener arenaCopyUtilListener;

  /**
   * The Mariadb backend, or {@code null} when it is disabled or unreachable. Every caller must
   * treat it as optional; the plugin stores profiles on disk regardless.
   */
  @Getter private static Database database;

  @Getter private static volatile boolean fullyLoaded = false;

  // BStats
  private Metrics metrics;

  public static final ErrorTracker ERROR_TRACKER = ErrorTracker.contextAware();
  private final BukkitContext faststatsMetrics =
      new BukkitContext.Factory(this, "a515f033e0639d1407b9bd6a926c2efb")
          .errorTrackerService(ERROR_TRACKER)
          .metrics(factory -> factory.create())
          .create();

  @Override
  public void onLoad() {
    instance = this;

    PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
    PacketEvents.getAPI().load();
  }

  @Override
  public void onEnable() {
    instance = this;

    miniMessage = MiniMessageUtil.createMiniMessage();
    entityHider = new EntityHider(this, EntityHider.Policy.BLACKLIST);
    arenaCopyUtilListener = new ArenaCopyUtilListener();

    PacketEvents.getAPI().init();
    metrics = new Metrics(this, 34622);
    faststatsMetrics.ready();

    if (VersionChecker.getBukkitVersion() == null) {
      Common.sendConsoleMmMessage("<red>Unsupported server version! Please use 1.21.x - 26.3");
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }

    try {
      PracticeConfig.load(getDataFolder().toPath());
      configurationLoaded = true;
    } catch (ConfigException | MigrationException e) {
      Common.sendConsoleMmMessage(
          "<red>Couldn't load the configuration files: " + PracticeConfig.describeFailure(e));
      Bukkit.getPluginManager().disablePlugin(this);
      return;
    }
    saveDefaultLadders();
    CosmeticPresets.load();

    EloMode.validateConfig();
    BackendManager.createFile(this);
    initializeDatabase();
    MatchHistoryManager.getInstance(); // eagerly initialise singleton
    DivisionManager.getInstance().getData();
    ArenaWorldUtil.createArenaWorld();
    CosmeticsPermissionManager.registerAllPermissions();

    this.registerApi();
    StartUpUtil.loadStartUpProgressMap();

    this.registerCommands(Bukkit.getServer());
    this.registerListeners(Bukkit.getPluginManager());

    ServerManager.getInstance().loadLobby();
    InventoryManager.getInstance().loadInventories();
    PlayerKitManager.getInstance().load();
    NametagManager.getInstance()
        .initialize(); // Initialize after all plugins loaded to detect TAB conflicts

    LadderManager.getInstance()
        .loadLadders(
            () -> {
              LadderManager.getInstance().getLadders().sort(Comparator.comparing(Ladder::getName));
              startUpProgress.replace(StartUpTypes.LADDER_LOADING, true);

              ArenaManager.getInstance()
                  .loadArenas(
                      () -> {
                        ArenaGuiSetupManager.getInstance().loadGuis();
                        startUpProgress.replace(StartUpTypes.ARENA_LOADING, true);

                        // Clean up any orphaned marker armor stands from previous server sessions
                        org.bukkit.World arenasWorld = ArenaWorldUtil.getArenasWorld();
                        if (arenasWorld != null) {
                          SpawnMarkerManager.getInstance().cleanupOrphanedMarkers(arenasWorld);
                        }
                      });

              ProfileManager.getInstance()
                  .loadProfiles(
                      () -> {
                        ProfileManager.getInstance().loadAllProfileInformations();
                        startUpProgress.replace(StartUpTypes.PROFILE_LOADING, true);

                        LeaderboardManager.getInstance()
                            .createAllLb(
                                () -> {
                                  startUpProgress.replace(StartUpTypes.LEADERBOARD_LOADING, true);
                                  LadderManager.getInstance().loadGuis();

                                  HologramManager.getInstance().loadHolograms();
                                  HologramProtectionListener.register();
                                  startUpProgress.replace(StartUpTypes.HOLOGRAM_LOADING, true);

                                  SidebarManager.getInstance().load();
                                  startUpProgress.replace(StartUpTypes.SIDEBAR_LOADING, true);

                                  this.loadPlaceholderApi();

                                  // Mark plugin as fully loaded
                                  fullyLoaded = true;
                                  Bukkit.getPluginManager()
                                      .callEvent(new AstralPracticeReadyEvent());

                                  // Check for updates asynchronously and log to console
                                  UpdateChecker.checkAsync(AstralPractice.this);
                                });
                      });
            });

    EventManager.getInstance()
        .loadEventData(
            () -> {
              EventManager.getInstance().loadGuis();
              startUpProgress.replace(StartUpTypes.EVENT_LOADING, true);
            });

    FfaManager.getInstance();
    EntityHiderListener.getInstance();
  }

  @Override
  public void onDisable() {
    PacketEvents.getAPI().terminate();
    if (!configurationLoaded) {
      return;
    }

    // Shutdown nametag manager and unregister packet blocker
    NametagManager.getInstance().shutdown();

    // Clear all spawn markers to prevent them persisting after server restart
    SpawnMarkerManager.getInstance().clearAllMarkers();

    // Clear all event spawn markers
    EventSpawnMarkerManager.getInstance().clearAllMarkers();

    MatchManager.getInstance().endMatches();
    FfaManager.getInstance().endFfas();
    HologramManager.getInstance()
        .saveAndDespawnHolograms(); // Use saveAndDespawn for shutdown to clean up armor stands
    EventManager.getInstance().endEvents();
    EventManager.getInstance().saveEventData();
    ArenaManager.getInstance().saveArenas();
    ProfileManager.getInstance().saveProfiles();
    // The plugin is going down, so wait for the final database flush instead of
    // handing it to an executor that is about to be shut down.
    ProfileManager.getInstance().saveProfilesToDatabase().join();
    LadderManager.getInstance().saveLadders();
    SidebarManager.getInstance().close();
    InventoryManager.getInstance().setData();
    CosmeticsCategoryManager.getInstance().clear();
    AstralPracticeApiImpl.setInstance(null);
    if (metrics != null) {
      metrics.shutdown();
    }
    faststatsMetrics.shutdown();
    if (database != null) {
      database.close();
    }
    BackendManager.save();
  }

  private void registerApi() {
    AstralPracticeApi api =
        new AstralPracticeApiImpl(
            new PlayerApiImpl(),
            new StatsApiImpl(),
            new MatchApiImpl(),
            new PartyApiImpl(),
            new QueueApiImpl(),
            new LadderApiImpl(),
            new FfaApiImpl(),
            new EventApiImpl(),
            CosmeticsCategoryManager.getInstance());
    AstralPracticeApiImpl.setInstance(api);
    Bukkit.getServicesManager()
        .register(AstralPracticeApi.class, api, this, ServicePriority.Normal);
  }

  /** On 26.2 and newer, a server without a ladders folder starts with the bundled ladders. */
  private void saveDefaultLadders() {
    File ladderFolder = new File(getDataFolder(), "ladders");
    if (!VersionChecker.isAtLeast(VersionChecker.BukkitVersion.v_26_2_R1)
        || ladderFolder.exists()) {
      return;
    }

    for (String ladder : DEFAULT_LADDERS) {
      saveResource("ladders/" + ladder + ".yml", false);
    }
  }

  /**
   * Opens the configured database.
   *
   * <p>A database that cannot be reached is not fatal: the plugin keeps running on its file
   * storage, so the failure is reported and the plugin starts without one.
   */
  private static void initializeDatabase() {
    if (!PracticeConfig.settings().getMariadbDatabase().isEnabled()) {
      return;
    }

    try {
      database =
          Database.forMariaDb(
              PracticeConfig.settings().getMariadbDatabase().getConnection().getHost(),
              PracticeConfig.settings().getMariadbDatabase().getConnection().getPort(),
              PracticeConfig.settings().getMariadbDatabase().getConnection().getDatabase(),
              PracticeConfig.settings().getMariadbDatabase().getConnection().getUser(),
              PracticeConfig.settings().getMariadbDatabase().getConnection().getPassword(),
              Math.max(
                  2, PracticeConfig.settings().getMariadbDatabase().getConnection().getPoolSize()));
    } catch (Exception e) {
      Common.sendConsoleMmMessage(
          "<red>Error during database initialization, continuing without it: "
              + (e.getCause() != null ? e.getCause() : e).getMessage());
    }
  }

  /** It registers all the commands that the plugin uses. */
  private void registerCommands(Server server) {
    registerCommand(server, "accept", new AcceptCommand());
    registerCommand(server, "arena", new ArenaCommand());
    registerCommand(server, "duel", new DuelCommand());
    registerCommand(server, "event", new EventCommand());
    registerCommand(server, "ladder", new LadderCommand());
    registerCommand(server, "matchinv", new MatchStatsCommand());
    registerCommand(server, "matchhistory", new MatchHistoryCommand());
    registerCommand(server, "party", new PartyCommand());
    registerCommand(server, "practice", new PracticeCommand());
    registerCommand(server, "preview", new PreviewCommand());
    registerCommand(server, "divisions", new DivisionsCommand());
    registerCommand(server, "settings", new SettingsCommand());
    registerCommand(server, "setup", new SetupCommand());
    registerCommand(server, "spectate", new SpectateCommand());
    registerCommand(server, "staff", new StaffCommand());
    registerCommand(server, "statistics", new StatisticsCommand());
    registerCommand(server, "unranked", new UnrankedCommand());
    registerCommand(server, "ranked", new RankedCommand());
    registerCommand(server, "editor", new EditorCommand());
    registerCommand(server, "copykit", new CopyKitCommand());
    registerCommand(server, "ffa", new FfaCommand());
    registerCommand(server, "ignorequeue", new IgnoreQueueCommand());
    registerCommand(server, "cosmetics", new CosmeticsCommand());
    registerCommand(server, "customqueue", new CustomQueueCommand());
    registerCommand(server, "nick", new NickCommand());

    if (PracticeConfig.settings().getChat().isPrivateChatEnabled()) {
      new MessageCommand();
      new ReplyCommand();
    }

    if (PracticeConfig.settings().getMatchSettings().getLeaveCommand().isEnabled()) {
      new LeaveCommand();
    }
  }

  private void registerCommand(Server server, String name, CommandExecutor executor) {
    PluginCommand command = server.getPluginCommand(name);
    if (command == null) {
      return;
    }

    command.setExecutor(executor);
    if (executor instanceof TabCompleter tabCompleter) {
      command.setTabCompleter(tabCompleter);
    }
  }

  private void loadPlaceholderApi() {
    if (SoftDependUtil.isPAPI_ENABLED) {
      new PlayerExpansion("ap").register();
    }
  }

  /** It registers all the events that are used in the plugin. */
  private void registerListeners(PluginManager pm) {

    pm.registerEvents(new AsyncPlayerPreLogin(), this);
    pm.registerEvents(new PlayerJoin(), this);
    pm.registerEvents(new PlayerQuit(), this);
    pm.registerEvents(new PlayerInteract(), this);
    pm.registerEvents(new WeatherChange(), this);
    pm.registerEvents(new ItemConsume(), this);
    pm.registerEvents(new ProjectileLaunch(), this);
    pm.registerEvents(new PlayerCommandPreprocess(), this);
    pm.registerEvents(new EntityDamage(), this);
    pm.registerEvents(new ArenaListener(), this);
    pm.registerEvents(new StatisticListener(), this);
    pm.registerEvents(arenaCopyUtilListener, this);
    pm.registerEvents(new BuildListener(), this);
    pm.registerEvents(new FfaListener(), this);
    pm.registerEvents(new ProjectileCooldownListener(), this);
    pm.registerEvents(new FireworkRocketCooldownListener(), this);
    pm.registerEvents(new PlayerChatListener(), this);
    pm.registerEvents(new CreeperListener(), this);
  }
}
