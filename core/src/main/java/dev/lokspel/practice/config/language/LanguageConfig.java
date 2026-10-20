package dev.lokspel.practice.config.language;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;

@Getter
public class LanguageConfig {
  @JsonProperty("CONSOLE-NAME")
  private String consoleName = "<red>Console";

  @JsonProperty("CANT-USE-CONSOLE")
  private String cantUseConsole = "<red>You can't use this command from the console.";

  @JsonProperty("RELOAD-DISABLED")
  private String reloadDisabled = "<red>Reload is disabled. Please completely restart the server.";

  @JsonProperty("STARTUP-NOT-READY")
  private String startupNotReady = "<red>AstralPractice has not yet loaded all the necessary data.";

  @JsonProperty("PLUGIN-LOADING-MESSAGE")
  private String pluginLoadingMessage = "<red>The server is still loading, please wait...";

  @JsonProperty("DUPLICATE-USERNAME-KICK")
  private String duplicateUsernameKick =
      "<red>A player with your username is already logged in on this server!";

  @JsonProperty("CANT-READ-DATABASE-FILE")
  private String cantReadDatabaseFile = "<red>Could not read database setup file";

  @JsonProperty("CANT-USE-COMMAND")
  private String cantUseCommand = "<red>You can't use this command right now.";

  @JsonProperty("WAIT-FOR-COOLDOWN")
  private String waitForCooldown =
      "<red>You have to wait <yellow>%seconds% %secondName% <red>before using this again.";

  @JsonProperty("AUTO-SAVE")
  private AutoSave autoSave = new AutoSave();

  @JsonProperty("GAMES-RESET")
  private String gamesReset = "%unranked% <yellow>and %ranked% <yellow>have been reset.";

  @JsonProperty("SET-SERVER-LOBBY")
  private String setServerLobby =
      "<red>You should set the server lobby. Until you do, most of the plugin features won't work.";

  @JsonProperty("SECOND-NAME")
  private SecondName secondName = new SecondName();

  @JsonProperty("GENERAL-CHAT")
  private GeneralChat generalChat = new GeneralChat();

  @JsonProperty("COMMAND")
  private Command command = new Command();

  @JsonProperty("ARENA")
  private Arena arena = new Arena();

  @JsonProperty("LADDER")
  private Ladder ladder = new Ladder();

  @JsonProperty("DUEL-ROUND-SELECTOR")
  private DuelRoundSelector duelRoundSelector = new DuelRoundSelector();

  @JsonProperty("EVENT")
  private Event event = new Event();

  @JsonProperty("PARTY")
  private Party party = new Party();

  @JsonProperty("SPECTATE")
  private Spectate spectate = new Spectate();

  @JsonProperty("LEADERBOARD")
  private Leaderboard leaderboard = new Leaderboard();

  @JsonProperty("PROFILE")
  private Profile profile = new Profile();

  @JsonProperty("CUSTOM-PLAYER-KIT")
  private CustomPlayerKit customPlayerKit = new CustomPlayerKit();

  @JsonProperty("QUEUES")
  private Queues queues = new Queues();

  @JsonProperty("MATCH")
  private Match match = new Match();

  @JsonProperty("FFA")
  private Ffa ffa = new Ffa();

  @JsonProperty("FIGHT")
  private Fight fight = new Fight();

  @Getter
  public static class AutoSave {
    @JsonProperty("STARTED")
    private String started = "<gold>AstralPractice auto save has <green>started<yellow>.";

    @JsonProperty("ENDED")
    private String ended = "<gold>AstralPractice auto save has <green>ended<yellow>.";
  }

  @Getter
  public static class SecondName {
    @JsonProperty("1SEC")
    private String oneSecond = "second";

    @JsonProperty("1<SEC")
    private String underOneSecond = "seconds";
  }

  @Getter
  public static class GeneralChat {
    @Comment("Group chat overwrites this.")
    @JsonProperty("SERVER-CHAT")
    private String serverChat =
        "%%division%% <dark_gray>| <reset><gray>%%player%%: <white>%%message%%";

    @JsonProperty("PARTY-CHAT")
    private String partyChat =
        "<dark_gray>[<dark_aqua><bold>Party<dark_gray>] <reset><aqua>%%player%%<white>:"
            + " <gray>%%message%%";

    @JsonProperty("STAFF-CHAT")
    private String staffChat =
        "<dark_gray>[<yellow>StaffChat<dark_gray>] <reset><gray>%%player%%: <white>%%message%%";
  }

  @Getter
  public static class Arena {
    @JsonProperty("DELETE-LAST-COPY")
    private String deleteLastCopy =
        "<yellow>Deleting last copy for arena <red>%arena%<yellow>. This may take a while..";

    @JsonProperty("LAST-COPY-DELETED")
    private String lastCopyDeleted =
        "<yellow>Last copy for arena <red>%arena% <yellow>has been successfully deleted.";

    @JsonProperty("GENERATE-COPY")
    private String generateCopy =
        "<yellow>Generating copy for arena <red>%arena%<yellow>. This may take a while..";

    @JsonProperty("ACTION-BAR-MSG")
    private String actionBarMsg =
        "<yellow>Generating copy for <gold>%arena%<yellow>. Progress: <green>%progress_bar%"
            + " <reset><white>(%progress_percent%%)";

    @JsonProperty("ERROR-DURING-COPY-GENERATE")
    private String errorDuringCopyGenerate =
        "<red>There was an error happened, while generating the arena copy for"
            + " <yellow>%arena%<red>.";

    @JsonProperty("COPY-GENERATED")
    private String copyGenerated =
        "<green>New copy for arena <yellow>%arena% <green>has been successfully generated.";

    @JsonProperty("LOCATION-CONVERSION")
    private LocationConversion locationConversion = new LocationConversion();

    @JsonProperty("STATUS-CHANGE")
    private StatusChange statusChange = new StatusChange();

    @JsonProperty("NO-AVAILABLE-LOCATION")
    private String noAvailableLocation = "<red>Arena has no available location.";

    @JsonProperty("CREATE")
    private Create create = new Create();

    @JsonProperty("CANT-EDIT-COPIES")
    private String cantEditCopies = "<red>You can't edit anything in the arena copy world!";

    @JsonProperty("CANT-EDIT-ARENA-WITH-COPIES")
    private String cantEditArenaWithCopies =
        "<red>You can't edit arenas that have copies. (%arena%<red>)";

    @JsonProperty("CANT-EDIT-ARENA-WITH-MATCH")
    private String cantEditArenaWithMatch =
        "<red>You cannot edit an arena that is currently hosting a match.";

    @JsonProperty("ARENA-REGEN-FAILED-CHEST")
    private String arenaRegenFailedChest = "<red>Arena regen failed to save chest contents.";

    @JsonProperty("SELECTOR")
    private Selector selector = new Selector();

    @JsonProperty("ARENA-TYPES")
    private ArenaTypes arenaTypes = new ArenaTypes();

    @Getter
    public static class LocationConversion {
      @JsonProperty("CONVERSION")
      private String conversion = "%x%, %y%, %z%";

      @JsonProperty("LOCATION-NULL")
      private String locationNull = "<red>NULL";

      @JsonProperty("LOCATION-NOT-REQUIRED")
      private String locationNotRequired = "<dark_gray>Not Required";
    }

    @Getter
    public static class StatusChange {
      @JsonProperty("NO-ICON")
      private String noIcon = "<red>You have to set the arenas icon before enabling.";

      @JsonProperty("NO-CUBOID")
      private String noCuboid = "<red>You have to set both corners before enabling the arena.";

      @JsonProperty("NO-SPAWN-POSITIONS")
      private String noSpawnPositions =
          "<red>You have to set the spawn positions before enabling the arena.";

      @JsonProperty("NO-FFA-POSITIONS")
      private String noFfaPositions =
          "<red>You have to add FFA spawn positions before enabling the arena.";

      @JsonProperty("NO-LADDER-ASSIGNED")
      private String noLadderAssigned =
          "<red>At least one ladder must be assigned to the arena before enabling the arena.";

      @JsonProperty("NO-BED")
      private String noBed = "<red>You must set the two bed locations before enabling the arena.";

      @JsonProperty("NO-PORTAL")
      private String noPortal =
          "<red>You must set the two portal locations before enabling the arena.";

      @JsonProperty("CANT-DISABLE1")
      private String cantDisable1 =
          "<red>You can't disable an arena that currently has a match in it.";

      @JsonProperty("CANT-DISABLE2")
      private String cantDisable2 = "<red>You can't disable an arena while it is copying.";
    }

    @Getter
    public static class Create {
      @JsonProperty("ARENA-CREATED")
      private String arenaCreated = "<yellow>%arena% <green>arena has been created.";

      @JsonProperty("NO-NAME")
      private String noName = "<red>Please type the name in the second line.";

      @JsonProperty("ONLY-ALPHANUMERIC")
      private String onlyAlphanumeric = "<red>You can only use alphanumeric characters.";

      @JsonProperty("NO-ACCENTS")
      private String noAccents = "<red>You cannot use accents in the name.";

      @JsonProperty("FEW-CHAR")
      private String fewChar = "<red>The name must have at least 4 letters.";

      @JsonProperty("MANY-CHAR")
      private String manyChar = "<red>The name cannot be longer than 16 letters.";

      @JsonProperty("NAME-TAKEN")
      private String nameTaken = "<red>Arena name is already in use.";
    }

    @Getter
    public static class Selector {
      @JsonProperty("LADDER-NOT-AVAILABLE")
      private String ladderNotAvailable = "<red>This ladder is not available anymore.";

      @JsonProperty("LADDER-FROZEN")
      private String ladderFrozen =
          "<red>This ladder is frozen, so you cannot start a new game with it.";

      @JsonProperty("DUEL")
      private Duel duel = new Duel();

      @JsonProperty("PARTY")
      private Party party = new Party();

      @Getter
      public static class Duel {
        @JsonProperty("TARGET-LEFT")
        private String targetLeft = "<red>Player has left the server.";

        @JsonProperty("ARENA-NOT-AVAILABLE")
        private String arenaNotAvailable = "<red>This arena is not available anymore.";
      }

      @Getter
      public static class Party {
        @JsonProperty("NOT-ENOUGH-PLAYERS")
        private String notEnoughPlayers =
            "<red>The party needs at least two players to be able to start a game.";

        @JsonProperty("NO-AVAILABLE-ARENA")
        private String noAvailableArena = "<red>There is no available arena for your game.";

        @JsonProperty("ARENA-NOT-AVAILABLE")
        private String arenaNotAvailable = "<red>This arena is not available anymore.";

        @JsonProperty("ERROR")
        private String error = "<red>Something went wrong.";

        @JsonProperty("TARGET-PARTY-DISBANDED")
        private String targetPartyDisbanded = "<red>The target party has been disbanded.";

        @JsonProperty("ARENA-CURRENTLY-NOT-AVAILABLE")
        private String arenaCurrentlyNotAvailable = "<red>This arena is currently not available.";
      }
    }

    @Getter
    public static class ArenaTypes {
      @JsonProperty("BASIC")
      private Basic basic = new Basic();

      @JsonProperty("BUILD")
      private Build build = new Build();

      @JsonProperty("FFA")
      private Ffa ffa = new Ffa();

      @Getter
      public static class Basic {
        @JsonProperty("NAME")
        private String name = "Basic (NonBuild)";

        @JsonProperty("DESCRIPTION")
        private List<String> description =
            List.of(
                "<dark_gray><st>----------------------------------",
                "<gray>This type of arena is a normal arena,",
                "<gray>which cannot be duplicated, as several",
                "<gray>matches can take place in it at the same",
                "<gray>time. Non-build ladders can be assigned to it.",
                "<dark_gray><st>----------------------------------");
      }

      @Getter
      public static class Build {
        @JsonProperty("NAME")
        private String name = "Build";

        @JsonProperty("DESCRIPTION")
        private List<String> description =
            List.of(
                "<dark_gray><st>----------------------------------",
                "<gray>This type of arena allows building",
                "<gray>for players, so copies can be created,",
                "<gray>as only one match can take place in",
                "<gray>it at a time. Ladders that allow",
                "<gray>building can be assigned to it.",
                "<dark_gray><st>----------------------------------");
      }

      @Getter
      public static class Ffa {
        @JsonProperty("NAME")
        private String name = "FFA";

        @JsonProperty("DESCRIPTION")
        private List<String> description =
            List.of(
                "<dark_gray><st>----------------------------------",
                "<gray>This type of arena allows players",
                "<gray>to join a match individually.",
                "<gray>They fight with a set up ladder.",
                "<dark_gray><st>----------------------------------");
      }
    }
  }

  @Getter
  public static class Ladder {
    @JsonProperty("CREATE")
    private Create create = new Create();

    @JsonProperty("STATUS-CHANGE")
    private StatusChange statusChange = new StatusChange();

    @JsonProperty("SELECTOR")
    private Selector selector = new Selector();

    @JsonProperty("LADDER-TYPES")
    private LadderTypes ladderTypes = new LadderTypes();

    @JsonProperty("KIT-EDITOR")
    private KitEditor kitEditor = new KitEditor();

    @Getter
    public static class Create {
      @JsonProperty("LADDER-CREATED")
      private String ladderCreated = "<yellow>%ladder% <green>ladder has been created.";

      @JsonProperty("NO-NAME")
      private String noName = "<red>Please type the name in the second line.";

      @JsonProperty("ONLY-ALPHANUMERIC")
      private String onlyAlphanumeric = "<red>You can only use alphanumeric characters.";

      @JsonProperty("NO-ACCENTS")
      private String noAccents = "<red>You cannot use accents in the name.";

      @JsonProperty("FEW-CHAR")
      private String fewChar = "<red>The name must have at least 2 letters.";

      @JsonProperty("MANY-CHAR")
      private String manyChar = "<red>The name cannot be longer than 20 letters.";

      @JsonProperty("NAME-TAKEN")
      private String nameTaken = "<red>Ladder name is already in use.";
    }

    @Getter
    public static class StatusChange {
      @JsonProperty("NO-ICON")
      private String noIcon = "<red>You have to set the ladders icon before enabling.";

      @JsonProperty("NO-CONTENT")
      private String noContent =
          "<red>You have to set the inventory content before enabling the ladder.";

      @JsonProperty("NO-MATCHTYPE-ASSIGNED")
      private String noMatchtypeAssigned =
          "<red>At least one match type must be assigned to the ladder before enabling.";

      @JsonProperty("SKYWARS")
      private Skywars skywars = new Skywars();

      @JsonProperty("CANT-DISABLE")
      private String cantDisable =
          "<red>You can't disable a ladder that is currently used in a match. <newline>You should"
              + " freeze it so the players can't start new matches with it.";

      @Getter
      public static class Skywars {
        @JsonProperty("NO-LOOT")
        private String noLoot =
            "<red>You must set the SkyWars chest loot content before enabling the ladder.";
      }
    }

    @Getter
    public static class Selector {
      @JsonProperty("LADDER-NOT-AVAILABLE")
      private String ladderNotAvailable = "<red>This ladder is not available anymore.";

      @JsonProperty("LADDER-FROZEN")
      private String ladderFrozen =
          "<red>This ladder is frozen, so you cannot start a new game with it.";

      @JsonProperty("CANT-PLAY-CUSTOM-KIT")
      private String cantPlayCustomKit =
          "<red>You don't have permission to play with your custom kit.";

      @JsonProperty("CUSTOM-LADDER-NOT-READY")
      private String customLadderNotReady =
          "<red>Your custom kit is not playable, set up before playing.";

      @JsonProperty("CUSTOM-LADDER-CANT-PLAY-MATCHTYPE")
      private String customLadderCantPlayMatchtype =
          "<red>You can't play this match type with your custom kit.";

      @JsonProperty("DUEL")
      private Duel duel = new Duel();

      @JsonProperty("PARTY")
      private Party party = new Party();

      @Getter
      public static class Duel {
        @JsonProperty("TARGET-LEFT")
        private String targetLeft = "<red>Player has left the server.";
      }

      @Getter
      public static class Party {
        @JsonProperty("NOT-ENOUGH-PLAYERS")
        private String notEnoughPlayers =
            "<red>The party needs at least two players to be able to start a game.";

        @JsonProperty("NO-AVAILABLE-ARENA")
        private String noAvailableArena = "<red>There is no available arena for your game.";

        @JsonProperty("ERROR")
        private String error = "<red>Something went wrong.";

        @JsonProperty("TARGET-PARTY-DISBANDED")
        private String targetPartyDisbanded = "<red>The target party has been disbanded.";

        @JsonProperty("NO-CUSTOM-KIT-SELECTED")
        private String noCustomKitSelected = "<red>You need to select a custom kit first.";
      }
    }

    @Getter
    public static class LadderTypes {
      @JsonProperty("BASIC")
      private LadderTypeMessages basic = LadderTypeMessages.basic();

      @JsonProperty("BUILD")
      private LadderTypeMessages build = LadderTypeMessages.build();

      @JsonProperty("COMBO")
      private LadderTypeMessages combo = LadderTypeMessages.combo();

      @JsonProperty("SUMO")
      private LadderTypeMessages sumo = LadderTypeMessages.sumo();

      @JsonProperty("TNT-SUMO")
      private LadderTypeMessages tntSumo = LadderTypeMessages.tntSumo();

      @JsonProperty("BOXING")
      private LadderTypeMessages boxing = LadderTypeMessages.boxing();

      @JsonProperty("PEARL-FIGHT")
      private LadderTypeMessages pearlFight = LadderTypeMessages.pearlFight();

      @JsonProperty("SPLEEF")
      private LadderTypeMessages spleef = LadderTypeMessages.spleef();

      @JsonProperty("SKYWARS")
      private LadderTypeMessages skywars = LadderTypeMessages.skywars();

      @JsonProperty("BEDWARS")
      private LadderTypeMessages bedwars = LadderTypeMessages.bedwars();

      @JsonProperty("FIREBALL-FIGHT")
      private LadderTypeMessages fireballFight = LadderTypeMessages.fireballFight();

      @JsonProperty("MLG-RUSH")
      private LadderTypeMessages mlgRush = LadderTypeMessages.mlgRush();

      @JsonProperty("BRIDGES")
      private LadderTypeMessages bridges = LadderTypeMessages.bridges();

      @JsonProperty("BATTLE-RUSH")
      private LadderTypeMessages battleRush = LadderTypeMessages.battleRush();

      @JsonProperty("CREEPER")
      private LadderTypeMessages creeper = LadderTypeMessages.creeper();
    }

    @Getter
    public static class KitEditor {
      @JsonProperty("LADDER-SELECTOR")
      private LadderSelector ladderSelector = new LadderSelector();

      @JsonProperty("KIT-SELECTOR")
      private KitSelector kitSelector = new KitSelector();

      @JsonProperty("KIT-EDITOR")
      private KitEditorSection kitEditor = new KitEditorSection();

      @Getter
      public static class LadderSelector {
        @JsonProperty("NOT-AVAILABLE")
        private String notAvailable = "<red>%ladder% ladder is no longer available to edit.";

        @JsonProperty("LADDER-FROZEN")
        private String ladderFrozen =
            "<red>%ladder% ladder is frozen so you can't edit your custom kit.";
      }

      @Getter
      public static class KitSelector {
        @JsonProperty("NOT-AVAILABLE")
        private String notAvailable = "<red>%ladder% ladder is no longer available to edit.";
      }

      @Getter
      public static class KitEditorSection {
        @JsonProperty("NOT-AVAILABLE")
        private String notAvailable = "<red>%ladder% ladder is no longer available to edit.";

        @JsonProperty("WEIGHT-CLASS-CHANGE-COOLDOWN")
        private String weightClassChangeCooldown =
            "<red>You have to wait another <yellow>%timeLeft% seconds.";
      }
    }
  }

  @Getter
  public static class DuelRoundSelector {
    @JsonProperty("LADDER-NOT-AVAILABLE")
    private String ladderNotAvailable = "<red>This ladder is not available anymore.";

    @JsonProperty("LADDER-FROZEN")
    private String ladderFrozen =
        "<red>This ladder is frozen, so you cannot start a new game with it.";

    @JsonProperty("ARENA-NOT-AVAILABLE")
    private String arenaNotAvailable =
        "<red>The arena you selected is disabled or frozen. Continuing with random arena.";

    @JsonProperty("ENEMY-OFFLINE")
    private String enemyOffline = "<red>Enemy player is offline.";

    @JsonProperty("PARTY-NOT-ENOUGH-PLAYERS")
    private String partyNotEnoughPlayers =
        "<red>The party needs at least two players to be able to start a game.";

    @JsonProperty("PARTY-RANDOM-ERROR")
    private String partyRandomError = "<red>There was an error happened contact discord.";

    @JsonProperty("ENEMY-PARTY-OFFLINE")
    private String enemyPartyOffline = "<red>Enemy party not available.";
  }

  @Getter
  public static class Event {
    @JsonProperty("DUEL")
    private Duel duel = new Duel();

    @JsonProperty("EVENT-QUEUE-STARTED-PLAYER")
    private String eventQueueStartedPlayer =
        "<red>%player% <gray>started the <yellow>%event% <gray>event. <click:run_command:/event"
            + " join><hover:show_text:'<green>Click here <yellow>to join the event.'><yellow>Join"
            + " Event</hover></click>";

    @JsonProperty("EVENT-QUEUE-STARTED-CONSOLE")
    private String eventQueueStartedConsole =
        "<red>%consoleName% <gray>started the <yellow>%event% <gray>event."
            + " <click:run_command:/event join><hover:show_text:'<green>Click here <yellow>to join"
            + " the event.'><yellow>Join Event</hover></click>";

    @JsonProperty("EVENT-CANT-START-ONTIME")
    private String eventCantStartOntime =
        "<red>Unfortunately the event could not start on time, so it was cancelled.";

    @JsonProperty("PLAYER-JOINED")
    private String playerJoined =
        "<gray>%player% <yellow>has joined"
            + " (<aqua>%playerSize%<yellow>/<aqua>%maxPlayerSize%<yellow>)!";

    @JsonProperty("EVENT-FULL")
    private String eventFull = "<red>The event is full.";

    @JsonProperty("EVENT-ALREADY-STARTED")
    private String eventAlreadyStarted = "<red>The event has already started.";

    @JsonProperty("PLAYER-QUIT")
    private String playerQuit = "<gray>%player% <yellow>has quit the event!";

    @JsonProperty("EVENT-START-COUNTDOWN-STOPPED")
    private String eventStartCountdownStopped = "<red>Event start countdown has been stopped.";

    @JsonProperty("PLAYER-STOPPED-SPECTATING")
    private String playerStoppedSpectating = "<red>%player% <yellow>has stopped spectating!";

    @JsonProperty("CANT-START-MULTIPLE-EVENTS-AUTOMATICALLY")
    private String cantStartMultipleEventsAutomatically =
        "<red>Can't start %event% because multiple events are disabled.";

    @JsonProperty("CANT-START-EVENT")
    private String cantStartEvent =
        "<red>There is already a <yellow>%event% <red>ongoing. You can't start another one.";

    @JsonProperty("QUEUE-START-COUNTDOWN")
    private String queueStartCountdown =
        "<yellow>The event starts in <red>%seconds% <yellow>%secondName%";

    @JsonProperty("CANT-ENABLE-EVENT")
    private String cantEnableEvent =
        "<red>Event doesn't meet the requirements for it to be enabled.";

    @JsonProperty("EVENT-ENABLED")
    private String eventEnabled =
        "<yellow>You successfully <green>enabled <yellow>the <gold>%event% <yellow>event.";

    @JsonProperty("CANT-DISABLE-ONGOING")
    private String cantDisableOngoing = "<red>You cannot edit the event while it is ongoing.";

    @JsonProperty("EVENT-DISABLED")
    private String eventDisabled =
        "<yellow>You successfully <red>disabled <yellow>the <gold>%event% <yellow>event.";

    @JsonProperty("COMPASS-TRACKER-ACTIONBAR")
    private String compassTrackerActionbar =
        "<red>Nearest player: <gold>%target% <gray>- <red>Distance: <gold>%distance%";

    @JsonProperty("ONLY-ONE-EVENT")
    private String onlyOneEvent = "<red>There can only be one event happening at a time.";

    @JsonProperty("CANT-HOST-EVENT")
    private String cantHostEvent =
        "<red>You don't have permission to host <yellow>%event% <red>event.";

    @JsonProperty("CANT-HOST-EVENT-TODAY")
    private String cantHostEventToday = "<red>You can't host any more events today.";

    @JsonProperty("MAP-SCAN")
    private MapScan mapScan = new MapScan();

    @Getter
    public static class Duel {
      @JsonProperty("EVENT-ROUND-WON-TITLE")
      private String eventRoundWonTitle = "<green><bold>VICTORY!";

      @JsonProperty("EVENT-ROUND-WON-SUBTITLE")
      private String eventRoundWonSubtitle = "<gray>You won this round!";

      @JsonProperty("EVENT-ROUND-LOST-TITLE")
      private String eventRoundLostTitle = "<red><bold>DEFEAT...";

      @JsonProperty("EVENT-ROUND-LOST-SUBTITLE")
      private String eventRoundLostSubtitle = "<gray>Better luck next time!";

      @JsonProperty("EVENT-MATCH-WON-TITLE")
      private String eventMatchWonTitle = "<gold><bold>TOURNAMENT WINNER!";

      @JsonProperty("EVENT-MATCH-WON-SUBTITLE")
      private String eventMatchWonSubtitle = "<yellow>Congratulations on winning the event!";

      @JsonProperty("EVENT-FIGHT-WON-TITLE")
      private String eventFightWonTitle = "<aqua>%winner% <gray>won the round";

      @JsonProperty("POST-KILL-COUNTDOWN-TITLE")
      private String postKillCountdownTitle = "<yellow>Next round in";

      @JsonProperty("POST-KILL-COUNTDOWN-SUBTITLE")
      private String postKillCountdownSubtitle = "<red>%seconds% <gray>%secondName%";
    }

    @Getter
    public static class MapScan {
      @JsonProperty("NO-EVENT-MAP")
      private String noEventMap = "<red>You have to select the <yellow>%event% event <red>map.";

      @JsonProperty("SCAN-STARTED")
      private String scanStarted =
          "<gold>%event% <yellow>event map scanning process has been started. Depends on the size"
              + " of the map, but it could take a while...";

      @JsonProperty("SCAN-ERROR")
      private String scanError =
          "<red>There was an error happened, while scanning the <yellow>%event% <red>event map.";

      @JsonProperty("SCAN-DONE")
      private String scanDone =
          "<yellow>%event% <green>event map scan is done. Found: <gold>%spawnPositionSize%"
              + " <yellow>player spawn positions.";

      @JsonProperty("POSITION-MARKERS-DELETED")
      private String positionMarkersDeleted = "<red>The position markers have been deleted!";

      @JsonProperty("ALREADY-SCANNING")
      private String alreadyScanning =
          "<red>The <yellow>%event% <red>event's map is already being scanned.";

      @JsonProperty("ENDERPEARL-COOLDOWN")
      private String enderpearlCooldown =
          "<red>You cannot use this for another <yellow>%seconds% <red>%secondName%.";
    }
  }

  @Getter
  public static class Party {
    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission.";

    @JsonProperty("ALREADY-PARTY")
    private String alreadyParty = "<red>You are already in a different party.";

    @JsonProperty("CANT-CREATE-PARTY")
    private String cantCreateParty = "<red>You can't create a party right now.";

    @JsonProperty("PARTY-CREATED")
    private String partyCreated =
        "<dark_gray>[<dark_aqua>Party<dark_gray>] <green>You have created a party.";

    @JsonProperty("NEW-LEADER")
    private String newLeader =
        "<dark_gray>[<dark_aqua>Party<dark_gray>] <gold>%player% <gray>is the new leader of the"
            + " party.";

    @JsonProperty("PLAYER-JOINED")
    private String playerJoined =
        "<dark_gray>[<dark_aqua>Party<dark_gray>] <aqua>%player% <green>joined the party.";

    @JsonProperty("PLAYER-KICKED")
    private String playerKicked =
        "<dark_gray>[<dark_aqua>Party<dark_gray>] <red>%player% <gray>has been kicked out of the"
            + " party.";

    @JsonProperty("PLAYER-LEFT")
    private String playerLeft =
        "<dark_gray>[<dark_aqua>Party<dark_gray>] <red>%player% <gray>has left the party.";

    @JsonProperty("PARTY-DISBANDED")
    private String partyDisbanded =
        "<dark_gray>[<dark_aqua>Party<dark_gray>] <red>The party has been disbanded.";

    @JsonProperty("MATCH-END")
    private String matchEnd =
        "<red>The fight was forced to end because the party has been disbanded!";

    @JsonProperty("BROADCAST-MSG")
    private String broadcastMsg =
        "<gold>%player%'s <gray>party is <green>public<gray>"
            + " (<yellow>%members%<gray>/<yellow>%maxPlayerLimit%<gray>)."
            + " <hover:show_text:'<yellow>Click here to <green>join <yellow>the"
            + " party.'><click:run_command:/party join %player%><yellow><u>Click"
            + " here</click></hover> <gray>to join.";

    @JsonProperty("NOT-ENOUGH-PLAYER-FOR-GAME")
    private String notEnoughPlayerForGame =
        "<red>The party needs at least two players to be able to start a game.";

    @JsonProperty("PREVIOUS-MATCH-ENDED")
    private String previousMatchEnded =
        "<red>Your previous party match was ended because a new one was started!";

    @JsonProperty("NOT-LEADER")
    private String notLeader = "<red>You are not the leader of the party.";

    @JsonProperty("CANT-DECREASE-LIMIT")
    private String cantDecreaseLimit =
        "<red>You can't decrease the limit further than the number of players in the party.";

    @JsonProperty("PARTY-NOT-PUBLIC")
    private String partyNotPublic = "<red>You have to have a public party for that.";

    @JsonProperty("NO-BROADCAST-LEFT")
    private String noBroadcastLeft = "<red>You have reached your daily party broadcast limit.";

    @JsonProperty("CANT-DUEL-OWN-PARTY")
    private String cantDuelOwnParty = "<red>You can't duel your own party.";

    @JsonProperty("PARTY-CHAT-DISABLED-BY-LEADER")
    private String partyChatDisabledByLeader = "<red>The party leader has disabled party chat.";

    @JsonProperty("CANT-DUEL-PARTY")
    private String cantDuelParty = "<red>You can't duel this party.";

    @JsonProperty("NO-JOINABLE-PARTY")
    private String noJoinableParty = "<red>There are no public parties right now.";

    @JsonProperty("PARTY-SPLIT")
    private PartySplit partySplit = PartySplit.party();

    @JsonProperty("MATCH-REQUEST-MESSAGE")
    private MatchRequestMessage matchRequestMessage = MatchRequestMessage.party();

    @JsonProperty("ARENA-BUSY")
    private String arenaBusy = "<red>Unfortunately the arena you choose is busy.";

    @JsonProperty("NO-AVAILABLE-ARENA")
    private String noAvailableArena =
        "<red>Unfortunately there are no available arenas for your match.";

    @Getter
    public static class PartySplit {
      @JsonProperty("BOTH-TEAMS-NEEDED")
      private String bothTeamsNeeded;

      static PartySplit party() {
        PartySplit value = new PartySplit();
        value.bothTeamsNeeded = "<red>Both teams need at least one player to start the match.";

        return value;
      }
    }

    @Getter
    public static class MatchRequestMessage {
      @JsonProperty("RANDOM-ARENA-NAME")
      private String randomArenaName;

      @JsonProperty("ALREADY-REQUESTED")
      private String alreadyRequested;

      @JsonProperty("SENDER")
      private List<String> sender;

      @JsonProperty("TARGET")
      private List<String> target;

      static MatchRequestMessage party() {
        MatchRequestMessage value = new MatchRequestMessage();
        value.randomArenaName = "<light_purple>Random";
        value.alreadyRequested = "<red>You already sent a duel request to this party.";
        value.sender =
            List.of(
                "<gray><st>-------------------------------",
                "<dark_aqua><bold>Party Match Request Sent!",
                "",
                " <gray>- <white>Kit: <aqua>%ladder%",
                " <gray>- <white>Map: <aqua>%arena%",
                " <gray>- <white>Rounds: <aqua>%rounds%",
                " <gray>- <white>Opponent Party Leader: <aqua>%target_leader%",
                " <gray>- <white>Opponent Party Players: <aqua>%target_members%",
                "<gray><st>-------------------------------");
        value.target =
            List.of(
                "<gray><st>-------------------------------",
                "<dark_aqua><bold>Party Match Request Received!",
                "",
                " <gray>- <white>Kit: <aqua>%ladder%",
                " <gray>- <white>Map: <aqua>%arena%",
                " <gray>- <white>Rounds: <aqua>%rounds%",
                " <gray>- <white>Opponent Party Leader: <aqua>%sender_leader%",
                " <gray>- <white>Opponent Party Players: <aqua>%sender_members%",
                "",
                "<green><click:run_command:'/accept"
                    + " %sender_leader%'><hover:show_text:'<yellow>Click here to <green>accept"
                    + " <red>%sender_leader%s <yellow>invite.'><u>Accept</u></hover></click> the"
                    + " request!",
                "<gray><st>-------------------------------");

        return value;
      }
    }
  }

  @Getter
  public static class Spectate {
    @JsonProperty("NO-PERMISSIONS")
    private String noPermissions = "<red>You don't have permission.";

    @JsonProperty("VANISH-COOLDOWN")
    private String vanishCooldown =
        "<red>You have to wait <yellow>%seconds% <red>%secondName% before using this again.";

    @JsonProperty("RANDOM-MATCH-COOLDOWN")
    private String randomMatchCooldown =
        "<red>You have to wait <yellow>%seconds% <red>%secondName% before using this again.";

    @JsonProperty("NO-MATCH")
    private String noMatch = "<red>There are no available match to watch.";

    @JsonProperty("MATCH")
    private Match match = new Match();

    @Getter
    public static class Match {
      @JsonProperty("ALREADY-SPECTATING")
      private String alreadySpectating = "<red>You are already spectating this match.";

      @JsonProperty("ONLY-ONE-TO-SPECTATE")
      private String onlyOneToSpectate = "<red>There are no more matches to spectate.";

      @JsonProperty("MATCH-ENDED")
      private String matchEnded = "<red>Match is already ended.";

      @JsonProperty("CANT-SPECTATE")
      private String cantSpectate =
          "<red>You can't spectate this match, because not every player has spectate enabled.";

      @JsonProperty("SPECTATE-START")
      private String spectateStart = "<gold>%player% <yellow>started spectating the match.";
    }
  }

  @Getter
  public static class Leaderboard {
    @JsonProperty("REFRESH-COOLDOWN")
    private String refreshCooldown =
        "<red>You have to wait another <yellow>%seconds% <red>%secondName%.";
  }

  @Getter
  public static class Profile {
    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission.";

    @JsonProperty("CANT-CHANGE-TARGET-SETTINGS")
    private String cantChangeTargetSettings = "<red>You can't change %target%'s settings.";

    @JsonProperty("SIDEBAR-GLOBALLY-DISABLED")
    private String sidebarGloballyDisabled =
        "<red>Sidebar is disabled globally by the server configuration.";

    @JsonProperty("PRIVATE-MSG-OFF")
    private String privateMsgOff = "<red>Private message system is disabled, you cannot toggle it.";

    @JsonProperty("TOGGLE-COOLDOWN")
    private String toggleCooldown =
        "<red>You have to wait another <yellow>%seconds% <red>%secondName%.";

    @JsonProperty("PLAYER-REMOVED-MATCH")
    private String playerRemovedMatch = "<yellow>You removed <red>%target% <yellow>from the match.";

    @JsonProperty("REMOVED-PLAYER-MATCH")
    private String removedPlayerMatch = "<red>%target% <yellow>got removed from the match.";

    @JsonProperty("PLAYER-REMOVED-EVENT")
    private String playerRemovedEvent = "<yellow>You removed <red>%target% <yellow>from the event.";

    @JsonProperty("REMOVED-PLAYER-EVENT")
    private String removedPlayerEvent = "<red>%target% <yellow>got removed from the event.";

    @JsonProperty("PLAYER-REMOVED-SPECTATOR")
    private String playerRemovedSpectator =
        "<yellow>You removed <red>%target% <yellow>from the spectate.";

    @JsonProperty("WORLD-TIME-NAMES")
    private WorldTimeNames worldTimeNames = new WorldTimeNames();

    @JsonProperty("PREFIX-VISIBILITY-NAMES")
    private PrefixVisibilityNames prefixVisibilityNames = new PrefixVisibilityNames();

    @JsonProperty("GROUP-PERMISSION-NAME")
    private String groupPermissionName = "Permission for the %group% player group.";

    @JsonProperty("INACTIVITY-REMOVED")
    private String inactivityRemoved =
        "<red>%count% <yellow>profile got removed because of inactivity.";

    @JsonProperty("NO-GROUP")
    private String noGroup = "<red>You don't have a group.";

    @Getter
    public static class WorldTimeNames {
      @JsonProperty("SUNRISE")
      private String sunrise = "Sunrise";

      @JsonProperty("DAY")
      private String day = "Day";

      @JsonProperty("SUNSET")
      private String sunset = "Sunset";

      @JsonProperty("NIGHT")
      private String night = "Night";
    }

    @Getter
    public static class PrefixVisibilityNames {
      @JsonProperty("NONE")
      private String none = "None";

      @JsonProperty("PREFIX")
      private String prefix = "Prefix";

      @JsonProperty("SUFFIX")
      private String suffix = "Suffix";

      @JsonProperty("PREFIX-AND-SUFFIX")
      private String prefixAndSuffix = "Prefix and Suffix";
    }
  }

  @Getter
  public static class CustomPlayerKit {
    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission.";

    @JsonProperty("NO-CUSTOM-KIT")
    private String noCustomKit = "<red>You don't have access to custom kits.";

    @JsonProperty("NO-CUSTOM-KIT-SELECTED")
    private String noCustomKitSelected = "<red>You don't have a custom kit selected.";

    @JsonProperty("ONLY-SELECT-ENABLED-CUSTOMS")
    private String onlySelectEnabledCustoms = "<red>You can only select enabled custom kits.";

    @JsonProperty("CUSTOM-KIT-SHARE")
    private List<String> customKitShare =
        List.of(
            "",
            "<gray>Command: <hover:show_text:'<white>/copykit"
                + " %code%'><click:SUGGEST_COMMAND:'/copykit %code%'><aqua>/copykit"
                + " %code%</click></hover>",
            "");

    @JsonProperty("COPYKIT")
    private Copykit copykit = new Copykit();

    @Getter
    public static class Copykit {
      @JsonProperty("HELP")
      private String help = "<red>/copykit <code>";

      @JsonProperty("NO-CODE")
      private String noCode = "<red>There is no such code.";

      @JsonProperty("NO-CUSTOM-KIT")
      private String noCustomKit = "<red>You don't have access to custom kits. Upgrade your group.";

      @JsonProperty("COPY-SUCCESS")
      private String copySuccess = "<green>Successfully copied the kit named %name%.";
    }
  }

  @Getter
  public static class Queues {
    @JsonProperty("UNRANKED")
    private Unranked unranked = new Unranked();

    @JsonProperty("RANKED")
    private Ranked ranked = new Ranked();

    @JsonProperty("CUSTOM")
    private Custom custom = new Custom();

    @JsonProperty("MULTI")
    private Multi multi = new Multi();

    @JsonProperty("QUEUE-START")
    private String queueStart = "<green>You are now queued for %weightClass% %ladder%<green>.";

    @JsonProperty("QUEUE-STOPPED")
    private String queueStopped =
        "<green>You are no longer queued for %weightClass% %ladder%<green>.";

    @JsonProperty("LADDER-FROZEN")
    private String ladderFrozen = "<red>%ladder% ladder is no longer available.";

    @JsonProperty("ELO-RANGE")
    private String eloRange = "<red>Elo range: <yellow>%from% <red>-> <yellow>%to%";

    @JsonProperty("NO-AVAILABLE-ARENA")
    private String noAvailableArena = "<red>Can't find available arena!";

    @JsonProperty("NO-MATCH-IN-TIME")
    private String noMatchInTime = "<red>We couldn't find a match in %maxTime% minutes.";

    @Getter
    public static class Unranked {
      @JsonProperty("LADDER-FROZEN")
      private String ladderFrozen =
          "<red>This ladder is frozen, so you cannot start a new game with it.";

      @JsonProperty("LADDER-DISABLED")
      private String ladderDisabled =
          "<red>This ladder is disabled, you cannot start a new game with it.";

      @JsonProperty("SEARCHING-OWN-DIVISION")
      private String searchingOwnDivision =
          "<yellow>Currently searching in %division_fullName%<yellow>.";

      @JsonProperty("SEARCHING-IN-RANGE")
      private String searchingInRange =
          "<yellow>Currently searching between %from_fullName% <yellow>and %to_fullName%<yellow>.";
    }

    @Getter
    public static class Ranked {
      @JsonProperty("LADDER-FROZEN")
      private String ladderFrozen =
          "<red>This ladder is frozen, so you cannot start a new game with it.";

      @JsonProperty("LADDER-DISABLED")
      private String ladderDisabled =
          "<red>This ladder is disabled, you cannot start a new game with it.";

      @JsonProperty("HIGH-PING")
      private String highPing = "<red>Your ping is too high for ranked queue.";

      @JsonProperty("BANNED")
      private String banned =
          "<red>You are banned from playing ranked matches. Reason: <yellow>%reason%";

      @JsonProperty("NO-REASON")
      private String noReason = "NULL";
    }

    @Getter
    public static class Custom {
      @JsonProperty("NO-PERMISSION")
      private String noPermission = "<red>You don't have permission.";

      @JsonProperty("NO-SAVED-KITS")
      private String noSavedKits = "<red>You do not have any saved custom kits ready to host.";

      @JsonProperty("KIT-NOT-READY")
      private String kitNotReady = "<red>This custom kit is not ready for queueing.";

      @JsonProperty("HOSTED")
      private String hosted =
          "<green>You are now hosting a custom queue with <yellow>%kit%<green>.";

      @JsonProperty("CANCELLED")
      private String cancelled = "<yellow>You cancelled your hosted custom queue.";

      @JsonProperty("WAITING-FOR-HOST")
      private String waitingForHost =
          "<yellow>Waiting in queue for someone to host a custom kit...";

      @JsonProperty("SEARCH-CANCELLED")
      private String searchCancelled = "<yellow>You left the custom-kit host queue.";

      @JsonProperty("SEARCHING-ACTIONBAR")
      private String searchingActionbar = "<yellow>Searching hosted custom kits... <gray>%elapsed%";

      @JsonProperty("HOSTING-ACTIONBAR")
      private String hostingActionbar = "<yellow>Hosting custom kit <gold>%kit% <gray>%elapsed%";

      @JsonProperty("JOINED")
      private String joined =
          "<green>You joined <yellow>%player%<green>'s hosted queue using <yellow>%kit%<green>.";

      @JsonProperty("MATCH-FOUND")
      private String matchFound = "<green><yellow>%player% <green>joined your hosted custom queue.";
    }

    @Getter
    public static class Multi {
      @JsonProperty("CANT-MIX-TYPES")
      private String cantMixTypes =
          "<red>You cannot mix ranked and unranked ladders in multi-queue.";

      @JsonProperty("MAX-LADDERS")
      private String maxLadders = "<red>You can only queue for <yellow>%max% <red>ladders at once.";

      @JsonProperty("SIDEBAR-MULTIPLE")
      private String sidebarMultiple = "Multiple Ladders";
    }
  }

  @Getter
  public static class Ffa {
    @JsonProperty("LADDER-DISABLED-REMOVED")
    private String ladderDisabledRemoved = "<red>The ladder you were in has been disabled.";

    @JsonProperty("ALL-LADDERS-DISABLED")
    private String allLaddersDisabled =
        "<red>All ladders have been disabled. You have been removed from the FFA.";

    @JsonProperty("FFA-CLOSED")
    private String ffaClosed = "<red>FFA has been closed.";

    @JsonProperty("COMMAND")
    private CommandSection command = new CommandSection();

    @JsonProperty("GAME")
    private Game game = new Game();

    @Getter
    public static class CommandSection {
      @JsonProperty("JOIN")
      private Join join = new Join();

      @JsonProperty("SPECTATE")
      private Spectate spectate = new Spectate();

      @JsonProperty("LEAVE")
      private Leave leave = new Leave();

      @JsonProperty("KIT")
      private Kit kit = new Kit();

      @JsonProperty("HELP")
      private List<String> help =
          List.of(
              "<gray><st>----------------------------------------",
              "<dark_aqua><bold>FFA Commands",
              "",
              " <yellow>» <white>/%label% join [arena] <gray>- <white>Join a FFA or choose an"
                  + " arena.",
              " <yellow>» <white>/%label% leave <gray>- <white>Leave a FFA.",
              " <yellow>» <white>/%label% kit <gray>- <white>Open FFA kit selector.",
              " <yellow>» <white>/%label% spectate <arena> <gray>- <white>Spectate FFA.",
              " <yellow>» <white>/%label% list <gray>- <white>List all available arenas.",
              "<gray><st>----------------------------------------");

      @JsonProperty("LIST")
      private ListSection list = new ListSection();

      @Getter
      public static class Join {
        @JsonProperty("HELP")
        private String help = "<red>/%label% join [arena]";

        @JsonProperty("ARENA-NOT-FOUND")
        private String arenaNotFound = "<red>Couldn't find the arena you specified.";

        @JsonProperty("ARENA-CLOSED")
        private String arenaClosed = "<red>The arena you specified is closed.";

        @JsonProperty("CANT-JOIN-FFA")
        private String cantJoinFfa = "<red>You can't join the FFA right now.";
      }

      @Getter
      public static class Spectate {
        @JsonProperty("HELP")
        private String help = "<red>/ffa spectate <arena>";

        @JsonProperty("ARENA-NOT-FOUND")
        private String arenaNotFound = "<red>Couldn't find the arena you specified.";

        @JsonProperty("ARENA-CLOSED")
        private String arenaClosed = "<red>The arena you specified is closed.";

        @JsonProperty("CANT-JOIN-FFA")
        private String cantJoinFfa = "<red>You can't spectate the FFA right now.";
      }

      @Getter
      public static class Leave {
        @JsonProperty("NOT-IN-FFA")
        private String notInFfa = "<red>You are not in a FFA.";

        @JsonProperty("IN-COMBAT")
        private String inCombat = "<red>You can't leave the FFA while in combat.";
      }

      @Getter
      public static class Kit {
        @JsonProperty("NOT-IN-FFA")
        private String notInFfa = "<red>You are not in a FFA.";

        @JsonProperty("ALREADY-IN-KIT")
        private String alreadyInKit = "<red>You are already in this kit.";

        @JsonProperty("IN-COMBAT")
        private String inCombat = "<red>You can't switch your kit while in combat.";
      }

      @Getter
      public static class ListSection {
        @JsonProperty("FORMAT")
        private String format = "<yellow>%arena% <gray>- <yellow>%players% <gray>players.";

        @JsonProperty("EMPTY")
        private String empty = "<red>There are no available arenas.";

        @JsonProperty("LIST")
        private List<String> list =
            List.of(
                "<gray><st>----------------------------------------",
                "<dark_aqua><bold>Available Arenas",
                "",
                "<yellow>%arenas%",
                "<gray><st>----------------------------------------");
      }
    }

    @Getter
    public static class Game {
      @JsonProperty("PLAYER-JOIN")
      private String playerJoin = "<gold>%player% <yellow>joined the FFA.";

      @JsonProperty("PLAYER-LEAVE")
      private String playerLeave = "<gold>%player% <yellow>left the FFA.";

      @JsonProperty("KILLSTREAK-BROADCAST")
      private String killstreakBroadcast =
          "<gold>%player% <yellow>is on a <red>%streak% <yellow>killstreak!";

      @JsonProperty("COOLDOWN")
      private Cooldown cooldown = new Cooldown();

      @JsonProperty("ARROW-HIT-PLAYER")
      private String arrowHitPlayer = "<red>%player% is now at %health% ♥";

      @JsonProperty("CANT-BUILD-OVER-LIMIT")
      private String cantBuildOverLimit = "<red>You can't build there.";

      @JsonProperty("CANT-BUILD-OUTSIDE-ARENA")
      private String cantBuildOutsideArena = "<red>You can't build outside the arena.";

      @JsonProperty("CANT-CRAFT")
      private String cantCraft = "<red>You can't craft in non build FFA.";

      @JsonProperty("SPECTATE-START")
      private String spectateStart = "<gold>%player% <yellow>started spectating the match.";

      @Getter
      public static class Cooldown {
        @JsonProperty("ENDER-PEARL")
        private String enderPearl =
            "<red>You cannot use this for another <yellow>%seconds% <red>%secondName%.";

        @JsonProperty("GOLDEN-APPLE")
        private String goldenApple =
            "<red>You cannot eat another <gold>golden apple<red> for another <yellow>%seconds%"
                + " <red>%secondName%.";
      }
    }
  }

  @Getter
  public static class Fight {
    @JsonProperty("DEATH-MESSAGES")
    private DeathMessages deathMessages = new DeathMessages();

    @Getter
    public static class DeathMessages {
      @JsonProperty("VOID")
      private String voidValue = "<red>%player% <yellow>fell into the void.";

      @JsonProperty("VOID-BY-PLAYER")
      private String voidByPlayer =
          "<red>%player% <yellow>was knocked into the void by <gold>%killer%<yellow>.";

      @JsonProperty("WATER")
      private String water = "<red>%player% <yellow>drowned.";

      @JsonProperty("LAVA")
      private String lava = "<red>%player% <yellow>burned in lava.";

      @JsonProperty("FIRE")
      private String fire = "<red>%player% <yellow>burned to death.";

      @JsonProperty("FALL")
      private String fall = "<red>%player% <yellow>fell to their death.";

      @JsonProperty("EXPLOSION")
      private String explosion = "<red>%player% <yellow>blew up.";

      @JsonProperty("EXPLOSION-BY-PLAYER")
      private String explosionByPlayer =
          "<red>%player% <yellow>was blown up by <gold>%killer%<yellow>.";

      @JsonProperty("PLAYER")
      private String player = "<red>%player% <yellow>was killed by <gold>%killer%<yellow>.";

      @JsonProperty("PROJECTILE")
      private String projectile = "<red>%player% <yellow>was shot by <gold>%killer%<yellow>.";

      @JsonProperty("COMBAT-LOG-QUIT")
      private String combatLogQuit =
          "<red>%player% <yellow>was killed by <gold>%killer%<yellow> for logging out.";

      @JsonProperty("SUMO-FALL")
      private String sumoFall = "<red>%player% <yellow>fell out of the sumo circle.";

      @JsonProperty("SPLEEF-FALL")
      private String spleefFall = "<red>%player% <yellow>fell out of the spleef arena.";

      @JsonProperty("OWN-PORTAL-JUMP")
      private String ownPortalJump = "<red>%player% <yellow>jumped into their own portal and died.";

      @JsonProperty("DEFAULT")
      private String defaultValue = "<red>%player% <yellow>died.";
    }
  }
}
