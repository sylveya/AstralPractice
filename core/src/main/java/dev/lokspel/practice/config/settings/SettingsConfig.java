package dev.lokspel.practice.config.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.config.ItemConfig;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;

@Getter
public class SettingsConfig {
  @Comment({"MariaDB database settings.", "MySQL is also supported if preferred."})
  @JsonProperty("MARIADB-DATABASE")
  private MariadbDatabase mariadbDatabase = new MariadbDatabase();

  @Comment(
      "Knockback modifier - it very much depends on the hit delay. You should test what is the"
          + " optimal for you.")
  @JsonProperty("KNOCKBACK")
  private Knockback knockback = new Knockback();

  @Comment("Setup limits")
  @JsonProperty("SETUP")
  private Setup setup = new Setup();

  @Comment("Queue settings")
  @JsonProperty("QUEUE")
  private Queue queue = new Queue();

  @Comment("Admin settings")
  @JsonProperty("ADMIN-SETTINGS")
  private AdminSettings adminSettings = new AdminSettings();

  @Comment({
    "Auto save settings",
    "",
    "It basically doesn't affect the server performance since it happens async."
  })
  @JsonProperty("AUTO-SAVE")
  private AutoSave autoSave = new AutoSave();

  @Comment("Match settings")
  @JsonProperty("MATCH-SETTINGS")
  private MatchSettings matchSettings = new MatchSettings();

  @Comment("FFA settings")
  @JsonProperty("FFA")
  private Ffa ffa = new Ffa();

  @Comment("Spectator settings")
  @JsonProperty("SPECTATOR-SETTINGS")
  private SpectatorSettings spectatorSettings = new SpectatorSettings();

  @Comment("Party settings")
  @JsonProperty("PARTY")
  private Party party = new Party();

  @Comment("Staff mode")
  @JsonProperty("STAFF-MODE")
  private StaffMode staffMode = new StaffMode();

  @Comment("Sounds - You can find the sound list here: https://minecraft.wiki/w/Sound")
  @JsonProperty("SOUNDS")
  private Map<String, Sound> sounds = defaultSounds();

  @Comment("Leaderboard settings")
  @JsonProperty("LEADERBOARD")
  private Leaderboard leaderboard = new Leaderboard();

  @Comment("Sidebar settings")
  @JsonProperty("SIDEBAR")
  private Sidebar sidebar = new Sidebar();

  @Comment("Arena settings")
  @JsonProperty("ARENA")
  private Arena arena = new Arena();

  @Comment("Event settings")
  @JsonProperty("EVENT")
  private Event event = new Event();

  @Comment("Player settings")
  @JsonProperty("PLAYER")
  private Player player = new Player();

  @Comment("Division settings")
  @JsonProperty("DIVISIONS")
  private Divisions divisions = new Divisions();

  @Comment("Chat settings")
  @JsonProperty("CHAT")
  private Chat chat = new Chat();

  @Comment("Update checker")
  @JsonProperty("UPDATE-CHECKER")
  private UpdateChecker updateChecker = new UpdateChecker();

  private static Map<String, Sound> defaultSounds() {
    Map<String, Sound> values = new LinkedHashMap<>();
    values.put("MATCH_START_COUNTDOWN", Sound.matchStartCountdown());
    values.put("MATCH_STARTED", Sound.matchStarted());
    values.put("BED_BREAK", Sound.bedBreak());
    values.put("MATCH_PLAYER_TEMP_DEATH", Sound.matchStarted());
    values.put("MATCH_PLAYER_DEATH", Sound.matchPlayerDeath());
    values.put("TOTEM_SAVE_PLAYER", Sound.totemSavePlayer());
    values.put("EVENT_QUEUE_COUNTDOWN", Sound.matchStartCountdown());
    values.put("EVENT_START_COUNTDOWN", Sound.matchStartCountdown());
    values.put("GUI_CLICK_ADVANCE", Sound.guiClickAdvance());
    values.put("GUI_CLICK_BACK", Sound.guiClickBack());

    return values;
  }

  @Getter
  public static class MariadbDatabase {
    @JsonProperty("ENABLED")
    private boolean enabled = false;

    @Comment(
        "The server saves the player statistics for this minute interval to the mariadb database.")
    @JsonProperty("SAVE-PERIOD")
    private int savePeriod = 5;

    @JsonProperty("CONNECTION")
    private Connection connection = new Connection();

    @Getter
    public static class Connection {
      @JsonProperty("HOST")
      private String host = "localhost";

      @JsonProperty("PORT")
      private int port = 3306;

      @JsonProperty("DATABASE")
      private String database = "astralpractice";

      @JsonProperty("USER")
      private String user = "root";

      @JsonProperty("PASSWORD")
      private String password = "mysecretpassword";

      @Comment("Connections kept open to the database. At least 2 are used.")
      @JsonProperty("POOL-SIZE")
      private int poolSize = 2;
    }
  }

  @Getter
  public static class Knockback {
    @JsonProperty("NORMAL")
    private KnockbackSettings normal = KnockbackSettings.normal();

    @JsonProperty("COMBO")
    private KnockbackSettings combo = KnockbackSettings.combo();
  }

  @Getter
  public static class Setup {
    @Comment("Set to 0 or -1 for unlimited ladders.")
    @JsonProperty("MAX-LADDERS")
    private int maxLadders = 0;

    @Comment("If true, admins can use /arena set icon and /ladder set icon even while enabled.")
    @JsonProperty("ALLOW-ICON-EDIT-WHILE-ENABLED")
    private boolean allowIconEditWhileEnabled = false;

    @Comment("If true, disabling a ladder keeps player custom kits for that ladder.")
    @JsonProperty("PRESERVE-CUSTOM-KITS-ON-LADDER-DISABLE")
    private boolean preserveCustomKitsOnLadderDisable = false;
  }

  @Getter
  public static class Queue {
    @JsonProperty("COMBINED")
    private Combined combined = new Combined();

    @JsonProperty("MULTI")
    private Multi multi = new Multi();

    @JsonProperty("UNRANKED")
    private QueueSettings unranked = QueueSettings.unranked();

    @JsonProperty("RANKED")
    private QueueSettings ranked = QueueSettings.ranked();

    @Getter
    public static class Combined {
      @Comment(
          "If true, lobby uses a single queue item instead of separate ranked and unranked items.")
      @JsonProperty("ENABLED")
      private boolean enabled = false;

      @Comment("Options: UNRANKED, RANKED")
      @JsonProperty("DEFAULT-GUI")
      private String defaultGui = "UNRANKED";

      @JsonProperty("SWITCH-ENABLED")
      private boolean switchEnabled = false;
    }

    @Getter
    public static class Multi {
      @JsonProperty("ENABLED")
      private boolean enabled = true;

      @JsonProperty("PERMISSION")
      private String permission = "ap.queue.multi";

      @Comment("Set to -1 or 0 for unlimited.")
      @JsonProperty("MAX-LADDERS")
      private int maxLadders = 3;

      @JsonProperty("SIDEBAR")
      private Sidebar sidebar = new Sidebar();

      @Getter
      public static class Sidebar {
        @JsonProperty("MULTIPLE-LABEL")
        private String multipleLabel = "Multiple Ladders";

        @JsonProperty("CURRENT-LADDER-FORMAT")
        private String currentLadderFormat = "<white>Current: <gold>%ladder%";
      }
    }
  }

  @Getter
  public static class AdminSettings {
    @Comment("If true, op players ladder elo's cannot be changed by commands.")
    @JsonProperty("OP-BYPASS-ELO-CHANGE")
    private boolean opBypassEloChange = true;

    @Comment("If true, commands cannot change op players' daily unranked limit.")
    @JsonProperty("OP-BYPASS-UNRANKED-CHANGE")
    private boolean opBypassUnrankedChange = true;

    @Comment("If true, commands cannot change op players daily ranked limit.")
    @JsonProperty("OP-BYPASS-RANKED-CHANGE")
    private boolean opBypassRankedChange = true;

    @Comment("If true, commands cannot change op players' experience.")
    @JsonProperty("OP-BYPASS-EXP-CHANGE")
    private boolean opBypassExpChange = true;

    @Comment("If true, op players info gui can only be opened by themselves.")
    @JsonProperty("OP-BYPASS-INFOGUI")
    private boolean opBypassInfogui = true;

    @Comment("If true, they can only reset op players themselves.")
    @JsonProperty("OP-BYPASS-FULL-RESET")
    private boolean opBypassFullReset = true;
  }

  @Getter
  public static class AutoSave {
    @Comment(
        "It is recommended to enable this, as the plugin is only saved during automatic backups and"
            + " when the server is shut down. If it crashes and auto-save is disabled, all progress"
            + " will be lost.")
    @JsonProperty("ENABLED")
    private boolean enabled = true;

    @JsonProperty("ALERT")
    private boolean alert = true;

    @Comment("minutes")
    @JsonProperty("INTERVAL")
    private int interval = 10;
  }

  @Getter
  public static class Ffa {
    @JsonProperty("NAMETAG")
    private Nametag nametag = new Nametag();

    @JsonProperty("ROLLBACK")
    private Rollback rollback = new Rollback();

    @Comment("Default for newly created FFA arenas. If enabled, killers are fully healed on kill.")
    @JsonProperty("HEALTH-RESET-ON-KILL")
    private boolean healthResetOnKill = false;

    @Comment(
        "Default for newly created FFA arenas. If enabled, players see heart indicators below"
            + " names.")
    @JsonProperty("HEALTH-BELOW-NAME")
    private boolean healthBelowName = false;

    @Comment("If true, the player will see the health of the player they hit.")
    @JsonProperty("DISPLAY-ARROW-HIT-HEALTH")
    private boolean displayArrowHitHealth = true;

    @Comment("If true, player can destroy the fixed blocks in the arena that their ladder has.")
    @JsonProperty("ALLOW-DESTROYABLE-BLOCK")
    private boolean allowDestroyableBlock = true;

    @Comment(
        "Default for newly created FFA arenas. If true, players can drop items from their"
            + " inventory.")
    @JsonProperty("ALLOW-DROP-ITEMS")
    private boolean allowDropItems = false;

    @Comment(
        "In seconds. Only applies to players with custom kits. If a player doesn't select a custom"
            + " kit within this time, the default kit is applied (prevents staying invincible"
            + " forever).")
    @JsonProperty("CUSTOM-KIT-SELECTION-TIME")
    private int customKitSelectionTime = 10;

    @JsonProperty("COMBAT-LOG")
    private CombatLog combatLog = new CombatLog();

    @JsonProperty("KILLSTREAK")
    private Killstreak killstreak = new Killstreak();

    @Getter
    public static class Nametag {
      @Comment("If true, a drop shadow is drawn behind FFA players' nametags.")
      @JsonProperty("TEXT-SHADOW")
      private boolean textShadow = false;

      @Comment("Background of FFA players' nametags as an ARGB value, 0 for none.")
      @JsonProperty("BACKGROUND")
      private int background = 0;

      @Comment("Line drawn above FFA players' nametags, supports PlaceholderAPI placeholders.")
      @JsonProperty("TOP-LINE")
      private String topLine = "";

      @Comment("Line drawn below FFA players' nametags, supports PlaceholderAPI placeholders.")
      @JsonProperty("BOTTOM-LINE")
      private String bottomLine = "";
    }

    @Getter
    public static class Rollback {
      @Comment(
          "After the seconds, the arena will be reseted back to its original state (only on build"
              + " arenas).")
      @JsonProperty("SECONDS")
      private int seconds = 300;

      @Comment(
          "If true, players are teleported back to the arena's spawn points once the reset has"
              + " finished.")
      @JsonProperty("TELEPORT-TO-SPAWN")
      private boolean teleportToSpawn = false;
    }

    @Getter
    public static class CombatLog {
      @Comment("If true, combat log is enabled for FFA (tagging players in combat).")
      @JsonProperty("ENABLED")
      private boolean enabled = true;

      @Comment(
          "In seconds. How long a player remains \"in combat\" after dealing or taking damage.")
      @JsonProperty("TAG-DURATION")
      private int tagDuration = 15;

      @Comment("If true, players cannot use /ffa kit while in combat (prevents health restore).")
      @JsonProperty("BLOCK-KIT")
      private boolean blockKit = true;

      @Comment("If true, players cannot use /ffa leave while in combat (prevents escaping).")
      @JsonProperty("BLOCK-LEAVE")
      private boolean blockLeave = true;

      @Comment("If true, disconnecting while in combat counts as a kill for their opponent.")
      @JsonProperty("COUNT-AS-KILL-ON-QUIT")
      private boolean countAsKillOnQuit = true;

      @Comment("If true, shows a countdown action bar while the player is in combat.")
      @JsonProperty("ACTION-BAR")
      private boolean actionBar = true;

      @JsonProperty("ACTION-BAR-MSG")
      private String actionBarMsg = "<red><bold>In Combat: <yellow><bold>%remaining%s";

      @Comment("If true, killing a player clears the killer's \"in combat\" status.")
      @JsonProperty("CLEAR-ON-KILL")
      private boolean clearOnKill = false;
    }

    @Getter
    public static class Killstreak {
      @Comment("If true, reaching a killstreak milestone is announced in the FFA chat.")
      @JsonProperty("ENABLED")
      private boolean enabled = true;

      @Comment(
          "Kill amounts at which a broadcast is sent. After the highest value, REPEAT-EVERY is used"
              + " as the repeat step.")
      @JsonProperty("MILESTONES")
      private List<Integer> milestones = List.of(5, 10, 15, 20, 25, 30, 40, 50);

      @Comment(
          "After the highest milestone, a broadcast is sent every this many kills. Set to 0 to"
              + " disable.")
      @JsonProperty("REPEAT-EVERY")
      private int repeatEvery = 50;
    }
  }

  @Getter
  public static class SpectatorSettings {
    @Comment("If false, player can't use spectator mode, only watch single matches.")
    @JsonProperty("SPECTATOR-MODE")
    private boolean spectatorMode = true;

    @Comment("Turn on/off the spectator menu.")
    @JsonProperty("SPECTATOR-MENU")
    private boolean spectatorMenu = true;

    @Comment("Between vanish toggles this many seconds has to pass.")
    @JsonProperty("VANISH-COOLDOWN")
    private int vanishCooldown = 5;

    @Comment("Between random match searches, this many seconds has to pass.")
    @JsonProperty("RANDOM-MATCH-COOLDOWN")
    private int randomMatchCooldown = 10;

    @Comment("The spectators flying speed. Between 1 and 10")
    @JsonProperty("SPECTATOR-SPEED")
    private double spectatorSpeed = 2.0;
  }

  @Getter
  public static class Party {
    @JsonProperty("SETTINGS")
    private Settings settings = Settings.party();

    @Comment("The party will be advertised at these intervals (seconds).")
    @JsonProperty("PUBLIC-BROADCAST-TIME")
    private int publicBroadcastTime = 20;

    @Comment("This many times the party will broadcast in the chat.")
    @JsonProperty("BROADCAST-TIMES")
    private int broadcastTimes = 5;

    @Comment("After these seconds, the party invite will expire.")
    @JsonProperty("PARTY-INVITE-COOLDOWN")
    private int partyInviteCooldown = 60;

    @Comment("After the seconds the party vs party request will expire.")
    @JsonProperty("REQUEST-EXPIRY")
    private int requestExpiry = 60;

    @Comment("If the party is disbanded, the ongoing match will end.")
    @JsonProperty("END-MATCH-ON-DISBAND")
    private boolean endMatchOnDisband = true;

    @Getter
    public static class Settings {
      @JsonProperty("MAX-PARTY-MEMBERS")
      private MaxPartyMembers maxPartyMembers;

      @JsonProperty("DEFAULT")
      private Default defaultValue;

      static Settings party() {
        Settings value = new Settings();
        value.maxPartyMembers = MaxPartyMembers.party();
        value.defaultValue = Default.party();

        return value;
      }

      @Getter
      public static class MaxPartyMembers {
        @Comment("Maximum party members, if the owner doesn't have permission to change it.")
        @JsonProperty("DEFAULT")
        private int defaultValue;

        static MaxPartyMembers party() {
          MaxPartyMembers value = new MaxPartyMembers();
          value.defaultValue = 6;

          return value;
        }
      }

      @Getter
      public static class Default {
        @JsonProperty("PUBLIC-PARTY")
        private boolean publicParty;

        @JsonProperty("ALL-INVITE")
        private boolean allInvite;

        @JsonProperty("PARTY-CHAT")
        private boolean partyChat;

        @JsonProperty("DUEL-REQUESTS")
        private boolean duelRequests;

        static Default party() {
          Default value = new Default();
          value.publicParty = false;
          value.allInvite = false;
          value.partyChat = true;
          value.duelRequests = true;

          return value;
        }
      }
    }
  }

  @Getter
  public static class StaffMode {
    @Comment("When a staff joins, the server instantly hides them from other players.")
    @JsonProperty("JOIN-HIDE-FROM-PLAYERS")
    private boolean joinHideFromPlayers = false;

    @Comment("Staff mode spectation flying speed.")
    @JsonProperty("STAFF-SPECTATOR-SPEED")
    private double staffSpectatorSpeed = 2.0;
  }

  @Getter
  public static class Sound {
    @JsonProperty("ENABLED")
    private Boolean enabled;

    @JsonProperty("SOUND")
    private String sound;

    @JsonProperty("VOLUME")
    private Double volume;

    @JsonProperty("PITCH")
    private Double pitch;

    static Sound matchStartCountdown() {
      Sound value = new Sound();
      value.enabled = true;
      value.sound = "block.note_block.pling";
      value.volume = 1.0;
      value.pitch = 1.0;

      return value;
    }

    static Sound matchStarted() {
      Sound value = new Sound();
      value.enabled = true;
      value.sound = "entity.player.levelup";
      value.volume = 1.0;
      value.pitch = 1.0;

      return value;
    }

    static Sound bedBreak() {
      Sound value = new Sound();
      value.enabled = true;
      value.sound = "entity.ender_dragon.growl";
      value.volume = 1.0;
      value.pitch = 1.0;

      return value;
    }

    static Sound matchPlayerDeath() {
      Sound value = new Sound();
      value.enabled = true;
      value.sound = "entity.blaze.death";
      value.volume = 1.0;
      value.pitch = 1.0;

      return value;
    }

    static Sound totemSavePlayer() {
      Sound value = new Sound();
      value.enabled = true;
      value.sound = "item.totem.use";
      value.volume = 1.0;
      value.pitch = 1.0;

      return value;
    }

    static Sound guiClickAdvance() {
      Sound value = new Sound();
      value.enabled = true;
      value.sound = "block.stone_button.click_on";
      value.volume = 0.15;
      value.pitch = 0.8;

      return value;
    }

    static Sound guiClickBack() {
      Sound value = new Sound();
      value.enabled = true;
      value.sound = "block.note_block.bass";
      value.volume = 0.12;
      value.pitch = 0.75;

      return value;
    }
  }

  @Getter
  public static class Leaderboard {
    @JsonProperty("HOLOGRAM")
    private Hologram hologram = new Hologram();

    @Getter
    public static class Hologram {
      @Comment("Sec. This type of hologram is switching between ladders.")
      @JsonProperty("DYNAMIC-UPDATE")
      private int dynamicUpdate = 10;

      @Comment("Sec. This type of hologram is always show one ladder or global elo stats.")
      @JsonProperty("STATIC-UPDATE")
      private int staticUpdate = 30;

      @Comment("Also placeholders: %division%, %division_short%, %group%, %ladder_name%")
      @JsonProperty("FORMAT")
      private Format format = new Format();

      @Getter
      public static class Format {
        @JsonProperty("NULL-LINE")
        private String nullLine = "<gold>%number%. <red>N/A";

        @JsonProperty("NOTHING-TO-DISPLAY")
        private String nothingToDisplay = "<red>There is nothing to display.";

        @JsonProperty("ELO")
        private HologramFormat elo = HologramFormat.elo();

        @JsonProperty("WIN")
        private HologramFormat win = HologramFormat.win();

        @JsonProperty("KILLS")
        private HologramFormat kills = HologramFormat.kills();

        @JsonProperty("DEATHS")
        private HologramFormat deaths = HologramFormat.deaths();

        @JsonProperty("WIN_STREAK")
        private HologramFormat winStreak = HologramFormat.winStreak();

        @JsonProperty("BEST_WIN_STREAK")
        private HologramFormat bestWinStreak = HologramFormat.bestWinStreak();

        @JsonProperty("LOSE_STREAK")
        private HologramFormat loseStreak = HologramFormat.loseStreak();

        @JsonProperty("BEST_LOSE_STREAK")
        private HologramFormat bestLoseStreak = HologramFormat.bestLoseStreak();

        @JsonProperty("SETUP-HOLO")
        private SetupHolo setupHolo = new SetupHolo();

        @Getter
        public static class SetupHolo {
          @JsonProperty("TITLE")
          private String title = "<red>This hologram must be set.";

          @JsonProperty("LINE")
          private String line = "<gold>Hologram Name: <yellow>%name%";
        }
      }
    }
  }

  @Getter
  public static class Sidebar {
    @Comment(
        "If false, the sidebar is fully disabled for all players regardless of profile setting.")
    @JsonProperty("ENABLED")
    private boolean enabled = true;

    @Comment("In ticks. 20 ticks = 1 second")
    @JsonProperty("UPDATE-TIME")
    private int updateTime = 20;
  }

  @Getter
  public static class Arena {
    @Comment("If true, the server will load the enabled arena's chunks.")
    @JsonProperty("LOAD-CHUNKS")
    private boolean loadChunks = true;

    @Comment(
        "Recommend: false,  If you copy an arena, the server will teleport you there, so you can"
            + " watch it happening.")
    @JsonProperty("TELEPORT-TO-COPY")
    private boolean teleportToCopy = false;

    @Comment(
        "If true, the server more likely to be slow, but it triples the copy performance. 100x50x65"
            + " arena copy takes 1,5 minutes with fast copy.")
    @JsonProperty("FAST-COPY")
    private FastCopy fastCopy = new FastCopy();

    @Comment(
        "If default icon is enabled it won't be necessary to set an arena icon, but you still can.")
    @JsonProperty("DEFAULT-ICON")
    private DefaultIcon defaultIcon = new DefaultIcon();

    @JsonProperty("WORLDS")
    private Worlds worlds = new Worlds();

    @Getter
    public static class FastCopy {
      @JsonProperty("USE-FAWE")
      private boolean useFawe = false;

      @JsonProperty("ENABLED")
      private boolean enabled = true;

      @Comment("Be careful with this value.")
      @JsonProperty("MULTIPLIER")
      private int multiplier = 10;
    }

    @Getter
    public static class DefaultIcon {
      @JsonProperty("ENABLED")
      private boolean enabled = false;

      @JsonProperty("ICON")
      private ItemConfig icon = ItemConfig.of("FIREWORK_STAR").name("<gold>%arena%");
    }

    @Getter
    public static class Worlds {
      @Comment("Automatically create and manage the default empty \"arenas\" world.")
      @JsonProperty("CREATE-DEFAULT")
      private boolean createDefault = true;

      @Comment("The world where arena copies are created for matches.")
      @JsonProperty("COPY-WORLD")
      private String copyWorld = "arenas_copy";
    }
  }

  @Getter
  public static class Event {
    @Comment(
        "If true, multiple events can happen in the same time. But only one can be in the"
            + " collecting phase.")
    @JsonProperty("MULTIPLE")
    private boolean multiple = false;

    @JsonProperty("AUTO-EVENTS")
    private boolean autoEvents = true;

    @JsonProperty("DUEL")
    private Duel duel = new Duel();

    @Comment("In sec.")
    @JsonProperty("ENDERPEARL-COOLDOWN")
    private int enderpearlCooldown = 10;

    @JsonProperty("BROADCAST")
    private Broadcast broadcast = new Broadcast();

    @Comment("Shows the nearest player.")
    @JsonProperty("PLAYER-TRACKER")
    private ItemConfig playerTracker =
        ItemConfig.of("COMPASS").name("<red>Player Tracker <gray>(Right-Click)");

    @Comment("Please be aware that building is not yet supported for the event.")
    @JsonProperty("LMS")
    private Lms lms = new Lms();

    @JsonProperty("OITC")
    private Oitc oitc = new Oitc();

    @JsonProperty("TNTTAG")
    private Tnttag tnttag = new Tnttag();

    @Comment("Please be aware that building is not yet supported for the event.")
    @JsonProperty("BRACKETS")
    private Brackets brackets = new Brackets();

    @JsonProperty("SUMO")
    private Sumo sumo = new Sumo();

    @Comment("Please be aware that building is not yet supported for the event.")
    @JsonProperty("SPLEGG")
    private Splegg splegg = new Splegg();

    @Comment("Please be aware that building is not yet supported for the event.")
    @JsonProperty("JUGGERNAUT")
    private Juggernaut juggernaut = new Juggernaut();

    @Getter
    public static class Duel {
      @Comment(
          "Seconds to wait after a duel-event elimination before forwarding/next round. (alias:"
              + " event-post-kill-delay)")
      @JsonProperty("POST-KILL-DELAY")
      private int postKillDelay = 3;
    }

    @Getter
    public static class Broadcast {
      @JsonProperty("ENABLE")
      private boolean enable = true;

      @JsonProperty("MESSAGE-TO")
      private MessageTo messageTo = new MessageTo();

      @Getter
      public static class MessageTo {
        @Comment(
            "If true and the player is a member of a party, he will receive the broadcast message.")
        @JsonProperty("IN-PARTY")
        private boolean inParty = true;

        @Comment(
            "If true and the player is in an unranked match, he will receive the broadcast"
                + " message.")
        @JsonProperty("IN-UNRANKED-MATCH")
        private boolean inUnrankedMatch = true;

        @Comment(
            "If true and the player is in a ranked match, he will receive the broadcast message.")
        @JsonProperty("IN-RANKED-MATCH")
        private boolean inRankedMatch = false;

        @Comment(
            "If true and the player is in another event, he will receive the broadcast message.")
        @JsonProperty("IN-EVENT")
        private boolean inEvent = true;
      }
    }

    @Getter
    public static class Lms {
      @JsonProperty("NAME")
      private String name = "LMS";

      @JsonProperty("SETTINGS")
      private Settings settings = new Settings();

      @JsonProperty("WINNER-COMMAND")
      private List<String> winnerCommand = List.of("practice ranked add %player% 3");

      @Comment(
          "At this time of the day an automatic lms event is going to start. You can extend the"
              + " list, and add it to other events as well. If you have any question about this,"
              + " please join the discord server.")
      @JsonProperty("AUTO-EVENTS")
      private List<String> autoEvents = List.of("18:30");

      @Getter
      public static class Settings {
        @Comment("If a player dies, drop his inventory.")
        @JsonProperty("DROP-INVENTORY")
        private boolean dropInventory = true;
      }
    }

    @Getter
    public static class Oitc {
      @JsonProperty("NAME")
      private String name = "OITC";

      @JsonProperty("SETTINGS")
      private Settings settings = new Settings();

      @JsonProperty("WINNER-COMMAND")
      private List<String> winnerCommand =
          List.of("practice ranked add %player% 3", "eco give %player% 10");

      @Comment("At these times of the day an automatic event of this type is going to start.")
      @JsonProperty("AUTO-EVENTS")
      private List<String> autoEvents = List.of();

      @Getter
      public static class Settings {
        @Comment("Every player gets this many lives. When they die, they loose 1 life.")
        @JsonProperty("PLAYER-LIFE")
        private int playerLife = 5;
      }
    }

    @Getter
    public static class Tnttag {
      @JsonProperty("NAME")
      private String name = "TNT Tag";

      @JsonProperty("WINNER-COMMAND")
      private List<String> winnerCommand = List.of("practice ranked add %player% 3");

      @Comment("At these times of the day an automatic event of this type is going to start.")
      @JsonProperty("AUTO-EVENTS")
      private List<String> autoEvents = List.of();

      @JsonProperty("TAGGED-ITEM")
      private ItemConfig taggedItem = ItemConfig.of("TNT").name("<red>You're IT, tag someone!");
    }

    @Getter
    public static class Brackets {
      @JsonProperty("NAME")
      private String name = "Brackets";

      @JsonProperty("WINNER-COMMAND")
      private List<String> winnerCommand = List.of("practice ranked add %player% 3");

      @Comment("At these times of the day an automatic event of this type is going to start.")
      @JsonProperty("AUTO-EVENTS")
      private List<String> autoEvents = List.of();
    }

    @Getter
    public static class Sumo {
      @JsonProperty("NAME")
      private String name = "Sumo";

      @JsonProperty("WINNER-COMMAND")
      private List<String> winnerCommand = List.of("practice ranked add %player% 3");

      @Comment("At these times of the day an automatic event of this type is going to start.")
      @JsonProperty("AUTO-EVENTS")
      private List<String> autoEvents = List.of();
    }

    @Getter
    public static class Splegg {
      @JsonProperty("NAME")
      private String name = "Splegg";

      @JsonProperty("WINNER-COMMAND")
      private List<String> winnerCommand = List.of("practice ranked add %player% 3");

      @Comment("At these times of the day an automatic event of this type is going to start.")
      @JsonProperty("AUTO-EVENTS")
      private List<String> autoEvents = List.of();

      @Comment("Generic entries like WOOL or TERRACOTTA also match colored variants.")
      @JsonProperty("BREAKABLE-MATERIALS")
      private List<String> breakableMaterials = List.of("WOOL");

      @JsonProperty("EGG-LAUNCHER-ITEM")
      private ItemConfig eggLauncherItem =
          ItemConfig.of("IRON_SHOVEL").name("<green>Shoot The Blocks <gray>(Right-Click)");
    }

    @Getter
    public static class Juggernaut {
      @JsonProperty("NAME")
      private String name = "Juggernaut";

      @JsonProperty("WINNER-COMMAND")
      private List<String> winnerCommand = List.of("practice ranked add %player% 3");

      @Comment("At these times of the day an automatic event of this type is going to start.")
      @JsonProperty("AUTO-EVENTS")
      private List<String> autoEvents = List.of();
    }
  }

  @Getter
  public static class Player {
    @JsonProperty("DELETE-INACTIVE-USER")
    private DeleteInactiveUser deleteInactiveUser = new DeleteInactiveUser();

    @Comment("Sec between settings can be changed. It can be bypassed.")
    @JsonProperty("SETTINGS-DELAY")
    private int settingsDelay = 3;

    @JsonProperty("LOBBY-PROTECTION")
    private Map<String, Boolean> lobbyProtection = defaultLobbyProtection();

    @JsonProperty("NICKNAME")
    private Nickname nickname = new Nickname();

    @JsonProperty("LOBBY-NAMETAG")
    private LobbyNametag lobbyNametag = new LobbyNametag();

    @JsonProperty("NAMETAG-MANAGEMENT")
    private NametagManagement nametagManagement = new NametagManagement();

    @Comment("If true, the player will be teleported to the lobby when they join the server.")
    @JsonProperty("JOIN-TELEPORT-LOBBY")
    private boolean joinTeleportLobby = true;

    @Comment("If true, the default Minecraft join message will be shown when a player joins.")
    @JsonProperty("JOIN-MESSAGE")
    private boolean joinMessage = false;

    @JsonProperty("GROUP-CHAT")
    private GroupChat groupChat = new GroupChat();

    @JsonProperty("DEFAULT-SETTINGS")
    private DefaultSettings defaultSettings = new DefaultSettings();

    private static Map<String, Boolean> defaultLobbyProtection() {
      Map<String, Boolean> values = new LinkedHashMap<>();
      values.put("allow-velocity", false);
      values.put("allow-damage", false);
      values.put("allow-inventory-interact", false);
      values.put("allow-item-drop", false);
      values.put("allow-item-pickup", false);
      values.put("allow-hunger", false);
      values.put("allow-block-break", false);
      values.put("allow-block-place", false);
      values.put("allow-lobby-interact", false);

      return values;
    }

    @Getter
    public static class DeleteInactiveUser {
      @JsonProperty("ENABLED")
      private boolean enabled = true;

      @Comment("After this number of days, the inactive player profiles will be deleted.")
      @JsonProperty("DAYS")
      private int days = 30;
    }

    @Getter
    public static class Nickname {
      @Comment("Regex pattern for allowed characters and length in nicknames.")
      @JsonProperty("VALIDATION-PATTERN")
      private String validationPattern = "^[a-zA-Z0-9_]{3,16}$";

      @Comment("Regex pattern for blocked characters or tags in nicknames. Set empty to disable.")
      @JsonProperty("BLOCKED-PATTERN")
      private String blockedPattern = "(?i)</?obf(?:uscated)?[^>]*>|[&§]k";
    }

    @Getter
    public static class LobbyNametag {
      @JsonProperty("ENABLED")
      private boolean enabled = true;

      @Comment("If true, a drop shadow is drawn behind lobby players' nametags.")
      @JsonProperty("TEXT-SHADOW")
      private boolean textShadow = false;

      @Comment("Background of lobby players' nametags as an ARGB value, 0 for none.")
      @JsonProperty("BACKGROUND")
      private int background = 0;

      @Comment("Line drawn above lobby players' nametags, supports PlaceholderAPI placeholders.")
      @JsonProperty("TOP-LINE")
      private String topLine = "";

      @Comment("Line drawn below lobby players' nametags, supports PlaceholderAPI placeholders.")
      @JsonProperty("BOTTOM-LINE")
      private String bottomLine = "";
    }

    @Getter
    public static class NametagManagement {
      @Comment(
          "If false, AstralPractice will not manage nametags (above-head prefix/suffix/color) or"
              + " the tab-list at all.")
      @JsonProperty("ENABLED")
      private boolean enabled = true;
    }

    @Getter
    public static class GroupChat {
      @Comment(
          "If enabled, the player's chat will be formatted by their group. It is only viable if"
              + " \"SERVER-CHAT\" is also enabled.")
      @JsonProperty("ENABLED")
      private boolean enabled = true;
    }

    @Getter
    public static class DefaultSettings {
      @JsonProperty("DUELREQUEST")
      private boolean duelrequest = true;

      @JsonProperty("AUTOQUEUE")
      private boolean autoqueue = false;

      @JsonProperty("SIDEBAR")
      private boolean sidebar = true;

      @JsonProperty("HIDEPLAYERS")
      private boolean hideplayers = false;

      @JsonProperty("PARTYINVITES")
      private boolean partyinvites = true;

      @JsonProperty("ALLOWSPECTATE")
      private boolean allowspectate = true;

      @JsonProperty("PRIVATEMESSAGE")
      private boolean privatemessage = true;

      @JsonProperty("FLYING")
      private boolean flying = false;

      @Comment("Options: SUNRISE, DAY, SUNSET, NIGHT")
      @JsonProperty("WORLD-TIME")
      private String worldTime = "DAY";

      @Comment("Options: NONE, PREFIX, SUFFIX, PREFIX_AND_SUFFIX")
      @JsonProperty("PREFIX-VISIBILITY")
      private String prefixVisibility = "PREFIX_AND_SUFFIX";
    }
  }

  @Getter
  public static class Divisions {
    @JsonProperty("ENABLED")
    private boolean enabled = true;

    @JsonProperty("NEW-DIVISION-MESSAGE")
    private boolean newDivisionMessage = true;

    @Comment("Have to give the config name, not the full name")
    @JsonProperty("MIN-DIVISION-FOR-RANKED")
    private String minDivisionForRanked = "SILVER2";
  }

  @Getter
  public static class Chat {
    @JsonProperty("SERVER-CHAT-ENABLED")
    private boolean serverChatEnabled = true;

    @Comment(
        "If true, active match/FFA players only see messages from fighters in their own ongoing"
            + " fight.")
    @JsonProperty("ISOLATE-MATCH-CHAT")
    private boolean isolateMatchChat = true;

    @Comment(
        "If true, spectators of a fight also see the messages of the fighters they are watching.")
    @JsonProperty("SHOW-MATCH-CHAT-TO-SPECTATORS")
    private boolean showMatchChatToSpectators = true;

    @JsonProperty("PARTY-CHAT")
    private PartyChat partyChat = new PartyChat();

    @JsonProperty("STAFF-CHAT")
    private StaffChat staffChat = new StaffChat();

    @Comment("Private message system /message (/m) and /reply (/re, /r).")
    @JsonProperty("PRIVATE-CHAT-ENABLED")
    private boolean privateChatEnabled = true;

    @Getter
    public static class PartyChat {
      @JsonProperty("ENABLED")
      private boolean enabled = true;

      @Comment(
          "Prefix a message with this symbol to send it to your party instead of the server, like"
              + " @hello world. Set to \"\" or false to disable the shortcut, then only /party chat"
              + " works.")
      @JsonProperty("SHORTCUT")
      private String shortcut = "@";
    }

    @Getter
    public static class StaffChat {
      @JsonProperty("ENABLED")
      private boolean enabled = true;

      @Comment(
          "If true, you can use send message like #hello world instead of enabling staff chat each"
              + " time.")
      @JsonProperty("SHORTCUT")
      private boolean shortcut = true;
    }
  }

  @Getter
  public static class UpdateChecker {
    @Comment(
        "Checks the repository VERSION file on startup and tells you when a newer release is out.")
    @JsonProperty("ENABLED")
    private boolean enabled = true;
  }
}
