package dev.lokspel.practice.config.gui;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.config.ItemConfig;
import java.util.List;
import lombok.Getter;

@Getter
public class Setup {
  @JsonProperty("HUB")
  private Hub hub = new Hub();

  @JsonProperty("FFA-ARENA")
  private SetupFfaArena ffaArena = new SetupFfaArena();

  @JsonProperty("ARENA")
  private SetupArena arena = new SetupArena();

  @JsonProperty("LADDER")
  private SetupLadder ladder = new SetupLadder();

  @JsonProperty("EVENT")
  private Event event = new Event();

  @JsonProperty("HOLOGRAM")
  private Hologram hologram = new Hologram();

  @JsonProperty("SERVER")
  private Server server = new Server();

  @Getter
  public static class Hub {
    @JsonProperty("TITLE")
    private String title = "<red>Server Manager";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("GENERAL-FILLER-ITEM")
      private ItemConfig generalFillerItem = ItemConfig.of("ORANGE_STAINED_GLASS_PANE").name(" ");

      @JsonProperty("ARENA-MANAGER")
      private ItemConfig arenaManager = ItemConfig.of("NETHER_STAR").name("<gold>Arena Manager");

      @JsonProperty("LADDER-MANAGER")
      private ItemConfig ladderManager =
          ItemConfig.of("GOLDEN_SWORD").name("<yellow>Ladder Manager");

      @JsonProperty("HOLOGRAM-MANAGER")
      private ItemConfig hologramManager = ItemConfig.of("PAPER").name("<aqua>Hologram Manager");

      @JsonProperty("EVENT-MANAGER")
      private ItemConfig eventManager =
          ItemConfig.of("IRON_HELMET").name("<dark_green>Event Manager");

      @JsonProperty("SERVER-MANAGER")
      private ItemConfig serverManager = ItemConfig.of("NAME_TAG").name("<red>Server Manager");
    }
  }

  @Getter
  public static class Event {
    @JsonProperty("EVENT-MANAGER")
    private EventManager eventManager = new EventManager();

    @JsonProperty("EVENT-MAIN")
    private EventMain eventMain = new EventMain();

    @JsonProperty("EVENT-SETTINGS")
    private EventSettings eventSettings = new EventSettings();

    @Getter
    public static class EventManager {
      @JsonProperty("TITLE")
      private String title = "<dark_gray>Event Manager";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("BACK-TO")
        private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Back to Server Manager");

        @JsonProperty("EVENT-ICON")
        private EventIcon eventIcon = new EventIcon();

        @Getter
        public static class EventIcon extends ItemConfig {
          @JsonProperty("STATUS-NAMES")
          private StatusNames statusNames = new StatusNames();

          {
            name("<yellow>%eventName%");
            lore(
                "",
                "<gold>Event Information:",
                " <gray>» <yellow>State: %state%",
                "",
                "<aqua><bold>LEFT-CLICK <aqua>to open event settings.",
                "<green><bold>RIGHT-CLICK <green>to teleport to the event.");
          }

          @Getter
          public static class StatusNames {
            @JsonProperty("ENABLED")
            private String enabled = "<green>Enabled";

            @JsonProperty("DISABLED")
            private String disabled = "<red>Disabled";
          }
        }
      }
    }

    @Getter
    public static class EventMain {
      @JsonProperty("TITLE")
      private String title = "%eventName% <dark_gray>- Event";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("BACK-TO")
        private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Go Back");

        @JsonProperty("EVENT-NAME")
        private ItemConfig eventName =
            new ItemConfig()
                .name("<yellow>%eventName%")
                .lore(
                    "",
                    "<gray><bold>Icon Customization:",
                    "<aqua>» LEFT-CLICK <gray>with an item to set as icon",
                    "",
                    "<gray><bold>Quick Teleport:",
                    "<aqua>» RIGHT-CLICK <gray>to teleport to event arena",
                    "",
                    "<gray>Change the visual icon shown in the",
                    "<gray>Event Host GUI by clicking with any item.");

        @JsonProperty("SETTINGS")
        private ItemConfig settings =
            ItemConfig.of("REDSTONE")
                .name("<dark_red>Settings")
                .lore(
                    "",
                    "<yellow><bold>CLICK <gray>to configure event settings:",
                    "",
                    " <dark_gray>» <red>Broadcast Interval <gray>(chat announcements)",
                    " <dark_gray>» <red>Queue Wait Time <gray>(before start)",
                    " <dark_gray>» <red>Max Queue Time <gray>(before cancel)",
                    " <dark_gray>» <red>Game Duration <gray>(match length)",
                    " <dark_gray>» <red>Start Countdown <gray>(game start delay)",
                    " <dark_gray>» <red>Min/Max Players <gray>(requirements)",
                    "",
                    "<gray>Fine-tune event behavior and timings.");

        @JsonProperty("STATUS")
        private Status status = new Status();

        @JsonProperty("LOCATION")
        private ItemConfig location =
            ItemConfig.of("COMPASS")
                .name("<dark_purple>Locations")
                .lore(
                    "",
                    "<light_purple><bold>LEFT-CLICK <gray>to start interactive setup mode.",
                    "",
                    "<dark_purple><bold>Setup Process:",
                    "<gray>1. <light_purple>Receive Event Wand <gray>to configure locations",
                    "<gray>2. <light_purple>Set Corners: <gray>Left/Right click to mark region",
                    "<gray>3. <light_purple>Add Spawns: <gray>Right click blocks to add spawn"
                        + " points",
                    "<gray>4. <light_purple>Cycle Modes: <gray>Shift + Left/Right click",
                    "<gray>5. <light_purple>Exit Setup: <gray>Drop (Q) the wand",
                    "",
                    "<dark_purple><bold>Currently Configured:",
                    " <dark_purple>» <light_purple>Corner 1: <gray>%corner1%",
                    " <dark_purple>» <light_purple>Corner 2: <gray>%corner2%",
                    " <dark_purple>» <light_purple>Spawn Points: <gray>%spawnPositions% set",
                    "",
                    "<gray><i>No commands needed - use the wand!");

        @Getter
        public static class Status {
          @JsonProperty("ENABLED")
          private ItemConfig enabled =
              ItemConfig.of("LIME_STAINED_GLASS_PANE")
                  .name("<gray>Status: <green>Enabled")
                  .lore(
                      "",
                      "<yellow><bold>Click here <gray>to <red>disable <gray>the event.",
                      "",
                      "<yellow>You can only edit the event if its disabled.");

          @JsonProperty("DISABLED")
          private ItemConfig disabled =
              ItemConfig.of("RED_STAINED_GLASS_PANE")
                  .name("<gray>Status: <red>Disabled")
                  .lore(
                      "",
                      "<yellow><bold>CLICK <gray>to <green>enable <gray>the event.",
                      "",
                      "<red><bold>Requirements to Enable:",
                      " <dark_gray>» <yellow>Event region (2 corners) must be set",
                      " <dark_gray>» <yellow>At least 2 spawn points must be set",
                      " <dark_gray>» <yellow>All settings must be configured",
                      "",
                      "<gray><i>Use the LOCATION button to set these up!");
        }
      }
    }

    @Getter
    public static class EventSettings {
      @JsonProperty("TITLE")
      private String title = "<dark_blue>%eventName% <dark_gray>- Settings";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("BACK-TO")
        private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Go Back");

        @JsonProperty("BROADCAST-INTERVAL")
        private ItemConfig broadcastInterval =
            ItemConfig.of("CLOCK")
                .name("<gray>Broadcast Interval: <gold>%broadcastInterval% <gray>sec")
                .lore(
                    "",
                    "<gray>The event will be announced",
                    "<gray>every so often on the chat.",
                    "",
                    "<green><bold>LEFT-CLICK <green>to reduce the time.",
                    "<aqua><bold>RIGHT-CLICK <aqua>to increase the time.");

        @JsonProperty("WAIT-BEFORE-START")
        private ItemConfig waitBeforeStart =
            ItemConfig.of("ANVIL")
                .name("<gray>Wait before event start: <gold>%waitBeforeStart% <gray>sec")
                .lore(
                    "",
                    "<gray>The event waits up to this number",
                    "<gray>of seconds before starts the game",
                    "<gray>if it reaches the minimum number of players.",
                    "",
                    "<green><bold>LEFT-CLICK <green>to reduce the time.",
                    "<aqua><bold>RIGHT-CLICK <aqua>to increase the time.");

        @JsonProperty("MAX-QUEUE-TIME")
        private ItemConfig maxQueueTime =
            ItemConfig.of("BOOK")
                .name("<gray>Maximum Queue Time: <gold>%queueTime% <gray>min")
                .lore(
                    "",
                    "<gray>The event waits up to this number",
                    "<gray>of minutes before it stops the",
                    "<gray>queue if the game does not start.",
                    "",
                    "<green><bold>LEFT-CLICK <green>to reduce the time.",
                    "<aqua><bold>RIGHT-CLICK <aqua>to increase the time.");

        @JsonProperty("DURATION")
        private Duration duration = new Duration();

        @JsonProperty("LOCATOR-BAR")
        private LocatorBar locatorBar = new LocatorBar();

        @JsonProperty("START-TIME")
        private ItemConfig startTime =
            ItemConfig.of("FEATHER")
                .name("<gray>Start Countdown: <gold>%startTime% <gray>sec")
                .lore(
                    "",
                    "<gray>The game starts after this time.",
                    "",
                    "<green><bold>LEFT-CLICK <green>to reduce the time.",
                    "<aqua><bold>RIGHT-CLICK <aqua>to increase the time.");

        @JsonProperty("MIN-PLAYER")
        private ItemConfig minPlayer =
            ItemConfig.of("SNOWBALL")
                .name("<gray>Min Player: <gold>%minPlayer%")
                .lore(
                    "",
                    "<gray>At least this many players must",
                    "<gray>join the event for it to start.",
                    "",
                    "<green><bold>LEFT-CLICK <green>to reduce the amount.",
                    "<aqua><bold>RIGHT-CLICK <aqua>to increase the amount.");

        @JsonProperty("MAX-PLAYER")
        private ItemConfig maxPlayer =
            ItemConfig.of("GHAST_TEAR")
                .name("<gray>Max Player: <gold>%maxPlayer%")
                .lore(
                    "",
                    "<gray>Maximum number of players",
                    "<gray>allowed to join the event.",
                    "",
                    "<green><bold>LEFT-CLICK <green>to reduce the amount.",
                    "<aqua><bold>RIGHT-CLICK <aqua>to increase the amount.");

        @Getter
        public static class LocatorBar {
          @JsonProperty("ENABLED")
          private ItemConfig enabled =
              ItemConfig.of("COMPASS")
                  .name("<gray>Locator Bar: <green>Enabled")
                  .lore(
                      "",
                      "<yellow><bold>Click here <gray>to <red>disable <gray>the",
                      "<dark_green>locator bar <gray>in the event.");

          @JsonProperty("DISABLED")
          private ItemConfig disabled =
              ItemConfig.of("RED_STAINED_GLASS_PANE")
                  .name("<gray>Locator Bar: <red>Disabled")
                  .lore(
                      "",
                      "<yellow><bold>Click here <gray>to <green>enable <gray>the",
                      "<dark_green>locator bar <gray>in the event.");
        }

        @Getter
        public static class Duration {
          @JsonProperty("TNTTAG")
          private ItemConfig tnttag =
              ItemConfig.of("TNT")
                  .name("<gray>Tnt Explode Time: <gold>%explodeTime% <gray>sec")
                  .lore(
                      "",
                      "<gray>After all this time, the tnt explodes.",
                      "",
                      "<green><bold>LEFT-CLICK <green>to reduce the time.",
                      "<aqua><bold>RIGHT-CLICK <aqua>to increase the time.");

          @JsonProperty("SUMO&BRACKETS")
          private ItemConfig sumoAndBrackets =
              ItemConfig.of("BLAZE_ROD")
                  .name("<gray>Duel Duration: <gold>%roundDuration% <gray>min")
                  .lore(
                      "",
                      "<gray>This is how long 1 game session lasts.",
                      "",
                      "<green><bold>LEFT-CLICK <green>to reduce the time.",
                      "<aqua><bold>RIGHT-CLICK <aqua>to increase the time.");

          @JsonProperty("OTHER")
          private ItemConfig other =
              ItemConfig.of("BLAZE_ROD")
                  .name("<gray>Game Duration: <gold>%duration% <gray>min")
                  .lore(
                      "",
                      "<gray>That's how long the game lasts.",
                      "",
                      "<green><bold>LEFT-CLICK <green>to reduce the time.",
                      "<aqua><bold>RIGHT-CLICK <aqua>to increase the time.");
        }
      }
    }
  }

  @Getter
  public static class Hologram {
    @JsonProperty("HOLOGRAM-MANAGER")
    private HologramManager hologramManager = new HologramManager();

    @JsonProperty("HOLOGRAM-MAIN")
    private HologramMain hologramMain = new HologramMain();

    @JsonProperty("HOLOGRAM-LADDERS")
    private HologramLadders hologramLadders = new HologramLadders();

    @Getter
    public static class HologramManager {
      @JsonProperty("TITLE")
      private String title = "<dark_gray>Hologram Manager";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("BACK-TO")
        private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Back to Server Manager");

        @JsonProperty("HOLOGRAM-ICON")
        private HologramIcon hologramIcon = new HologramIcon();

        @JsonProperty("PAGE-LEFT")
        private ItemConfig pageLeft = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

        @JsonProperty("PAGE-RIGHT")
        private ItemConfig pageRight = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

        @Getter
        public static class HologramIcon extends ItemConfig {
          @JsonProperty("STATUS-NAMES")
          private StatusNames statusNames = new StatusNames();

          {
            material("PAPER");
            name("<gold>%hologramName%");
            lore(
                "",
                "<gold>Hologram Information:",
                " <gray>» <yellow>State: %state%",
                " <gray>» <yellow>Type: <white>%type%",
                " <gray>» <yellow>Stats Show: <white>%statsShow%",
                "",
                "<aqua><bold>LEFT-CLICK <aqua>to open hologram settings.",
                "<green><bold>RIGHT-CLICK <green>to teleport to the hologram.");
          }

          @Getter
          public static class StatusNames {
            @JsonProperty("ENABLED")
            private String enabled = "<green>Enabled";

            @JsonProperty("DISABLED")
            private String disabled = "<red>Disabled";

            @JsonProperty("TYPE-NULL")
            private String typeNull = "<red>N/A";
          }
        }
      }
    }

    @Getter
    public static class HologramMain {
      @JsonProperty("TITLE")
      private String title = "<dark_blue>%hologram% <dark_gray>- Hologram";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("BACK-TO")
        private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Go Back");

        @JsonProperty("DELETE")
        private ItemConfig delete = ItemConfig.of("RED_DYE").name("<red>Delete Hologram");

        @JsonProperty("EVENT-TYPE")
        private ItemConfig eventType =
            ItemConfig.of("NETHER_STAR")
                .name("<gold>Hologram Type")
                .lore(
                    "",
                    "%eventTypes%",
                    "",
                    "<aqua><bold>Click here <aqua>to change the hologram's type.");

        @JsonProperty("LADDER")
        private Ladder ladder = new Ladder();

        @JsonProperty("SHOW-STATS")
        private ItemConfig showStats =
            ItemConfig.of("PLAYER_HEAD")
                .name("<red>Show Statistics: <white>%showStats%")
                .lore(
                    "",
                    "<gray>The hologram will show the",
                    "<gray>first this many statistics.",
                    "",
                    "<green><bold>LEFT-CLICK <green>to decrease the number.",
                    "<aqua><bold>RIGHT-CLICK <aqua>to increase the number.");

        @JsonProperty("STATUS")
        private Status status = new Status();

        @Getter
        public static class Ladder {
          @JsonProperty("NO-LADDER-SETTINGS")
          private ItemConfig noLadderSettings =
              ItemConfig.of("RED_STAINED_GLASS_PANE")
                  .name("<red>This hologram type has no ladder settings!");

          @JsonProperty("HAS-LADDER-SETTINGS")
          private ItemConfig hasLadderSettings =
              ItemConfig.of("GOLDEN_SWORD")
                  .name("<dark_aqua>Ladder Settings")
                  .lore(
                      "",
                      "<aqua><bold>Click here <aqua>to open the hologram's",
                      "<aqua>ladder settings.");
        }

        @Getter
        public static class Status {
          @JsonProperty("ENABLED")
          private ItemConfig enabled =
              ItemConfig.of("LIME_STAINED_GLASS_PANE")
                  .name("<gray>Status: <green>Enabled")
                  .lore("", "<yellow><bold>Click here <gray>to <red>disable <gray>the hologram.");

          @JsonProperty("DISABLED")
          private ItemConfig disabled =
              ItemConfig.of("RED_STAINED_GLASS_PANE")
                  .name("<gray>Status: <red>Disabled")
                  .lore(
                      "",
                      "<yellow><bold>Click here <gray>to <green>enable <gray>the hologram.",
                      "",
                      "<red><bold>Note: <gray>The hologram has to meet some",
                      "<gray>requirements to be enabled:",
                      " <dark_gray>» <yellow>Set a type.",
                      " <dark_gray>» <yellow>Set the ladder(s) if necessary.");
        }
      }
    }

    @Getter
    public static class HologramLadders {
      @JsonProperty("TITLE")
      private String title = "<dark_blue>%hologram% <dark_gray>- Ladders";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("GO-BACK")
        private ItemConfig goBack = ItemConfig.of("ARROW").name("<red>Go Back");

        @JsonProperty("ENABLED-LADDER")
        private ItemConfig enabledLadder =
            ItemConfig.of("LIME_STAINED_GLASS_PANE")
                .name("<green>%ladder%")
                .lore(
                    "",
                    "<gray>This ladder is <green>enabled<gray>.",
                    "",
                    "<yellow>Click here <gray>to <red>disable<gray>.");

        @JsonProperty("DISABLED-LADDER")
        private ItemConfig disabledLadder =
            ItemConfig.of("RED_STAINED_GLASS_PANE")
                .name("<red>%ladder%")
                .lore(
                    "",
                    "<gray>This ladder is <red>disabled<gray>.",
                    "",
                    "<yellow>Click here <gray>to <green>enable<gray>.");

        @JsonProperty("PAGE-LEFT")
        private ItemConfig pageLeft = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

        @JsonProperty("PAGE-RIGHT")
        private ItemConfig pageRight = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");
      }
    }
  }

  @Getter
  public static class Server {
    @JsonProperty("SERVER-MANAGER")
    private ServerManager serverManager = new ServerManager();

    @JsonProperty("LOBBY-ARMORS")
    private LobbyArmors lobbyArmors = new LobbyArmors();

    @JsonProperty("EVENTS")
    private Events events = new Events();

    @JsonProperty("MATCHES")
    private Matches matches = new Matches();

    @JsonProperty("FILE-SAVE")
    private FileSave fileSave = new FileSave();

    @Getter
    public static class ServerManager {
      @JsonProperty("TITLE")
      private String title = "<dark_gray>Server Manager";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("BACK-TO")
        private ItemConfig backTo = ItemConfig.of("ARROW").name("<yellow>Back to hub");

        @JsonProperty("INFORMATIONS")
        private Informations informations = new Informations();

        @JsonProperty("LOBBY-ARMORS")
        private ItemConfig lobbyArmors =
            ItemConfig.of("DIAMOND_CHESTPLATE")
                .name("<yellow>Lobby Armors")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to edit the lobby armors.",
                    "<dark_gray><st>------------------------");

        @JsonProperty("SAVE")
        private ItemConfig save =
            ItemConfig.of("SUGAR")
                .name("<yellow>Save Data")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<green>Left Click <gray>to open a GUI",
                    "<gray>where you can save your",
                    "<gray>server data in sections.",
                    "",
                    "<aqua>Right Click <gray>to save all",
                    "<gray>the server data manually.",
                    "<dark_gray><st>------------------------");

        @JsonProperty("MATCHES")
        private ItemConfig matches =
            ItemConfig.of("IRON_SWORD")
                .name("<yellow>Live Matches")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to view the",
                    "<gray>live matches.",
                    "",
                    "<gray>Live matches: <yellow>%liveMatches%",
                    "<dark_gray><st>------------------------");

        @JsonProperty("EVENTS")
        private ItemConfig events =
            ItemConfig.of("BOW")
                .name("<yellow>Live Events")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to view the",
                    "<gray>live events.",
                    "",
                    "<gray>Live events: <yellow>%liveEvents%",
                    "<dark_gray><st>------------------------");

        @Getter
        public static class Informations extends ItemConfig {
          @JsonProperty("STATUS-NAMES")
          private StatusNames statusNames = new StatusNames();

          {
            material("BOOK");
            name("<yellow>Informations");
            lore(
                "<dark_gray><st>------------------------",
                "<gray>Online players: <yellow>%onlinePlayers%",
                "<gray>Online staff: <yellow>%onlineStaffs%",
                "",
                "<gray>Required unranked for ranked matches: <yellow>%requiredDivision%",
                "",
                "<gray>Lobby status: %lobbyStatus%",
                "",
                "<gray>Enabled arenas: <yellow>%enabledArena%",
                "<gray>Enabled ladders: <yellow>%enabledLadder%",
                "<gray>Enabled events: <yellow>%enabledEvents%",
                "<dark_gray><st>------------------------");
          }

          @Getter
          public static class StatusNames {
            @JsonProperty("SET")
            private String set = "<green>Set";

            @JsonProperty("UNSET")
            private String unset = "<red>Not-Set";
          }
        }
      }
    }

    @Getter
    public static class LobbyArmors {
      @JsonProperty("TITLE")
      private String title = "<dark_gray>Lobby Armors";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("BACK-TO")
        private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Back to Server Manager");

        @JsonProperty("FILLER-ITEM")
        private ItemConfig fillerItem = ItemConfig.of("BLACK_STAINED_GLASS_PANE");

        @JsonProperty("FILLER-ITEM2")
        private ItemConfig fillerItem2 = ItemConfig.of("ORANGE_STAINED_GLASS_PANE");

        @JsonProperty("NAME-ITEM")
        private ItemConfig nameItem = ItemConfig.of("NAME_TAG").name("<yellow>%inventoryName%");
      }
    }

    @Getter
    public static class Events {
      @JsonProperty("TITLE")
      private String title = "<dark_gray>Events";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("BACK-TO")
        private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Back to Server Manager");

        @JsonProperty("REFRESH-PAGE")
        private ItemConfig refreshPage =
            ItemConfig.of("NETHER_STAR")
                .name("<green>Refresh Page")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to refresh the page.",
                    "<dark_gray><st>------------------------");

        @JsonProperty("EVENT-ITEM")
        private EventItem eventItem = new EventItem();

        @Getter
        public static class EventItem {
          @JsonProperty("NAME")
          private String name = "%eventName%";

          @JsonProperty("LORE")
          private List<String> lore =
              List.of(
                  "<dark_gray><st>------------------------",
                  "<yellow>Event Type: <white>%type%",
                  "",
                  "<yellow>Players: <white>%players%",
                  "<yellow>Spectators: <white>%spectators%",
                  "",
                  "<green>Left Click <gray>to spectate the event.",
                  "<aqua>Right Click <gray>to <red>end <gray>the event.",
                  "<dark_gray><st>------------------------");
        }
      }
    }

    @Getter
    public static class Matches {
      @JsonProperty("TITLE")
      private String title = "<dark_gray>Matches - Page %page%";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("BACK-TO")
        private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Back to Server Manager");

        @JsonProperty("GO-PAGE-LEFT")
        private ItemConfig goPageLeft = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

        @JsonProperty("GO-PAGE-RIGHT")
        private ItemConfig goPageRight = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

        @JsonProperty("REFRESH")
        private ItemConfig refresh =
            ItemConfig.of("NETHER_STAR")
                .name("<green>Refresh Pages")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to refresh the pages.",
                    "<dark_gray><st>------------------------");

        @JsonProperty("MATCH-ICON")
        private MatchIcon matchIcon = new MatchIcon();

        @Getter
        public static class MatchIcon {
          @JsonProperty("NAME")
          private String name = "%matchType%";

          @JsonProperty("LORE")
          private List<String> lore =
              List.of(
                  "<dark_gray><st>------------------------",
                  "<dark_gray>#%matchId%",
                  "%weightClass%",
                  "",
                  "<yellow>Match Type: <white>%matchType%",
                  "<yellow>Kit: <white>%ladder%",
                  "<yellow>Arena: <white>%arena%",
                  "<yellow>Duration: <white>%roundDuration%",
                  "<yellow>Spectators: <white>%spectators%",
                  "",
                  "<green>Left Click <gray>to spectate the match.",
                  "<aqua>Right Click <gray>to <red>end <gray>the match.",
                  "<dark_gray><st>------------------------");
        }
      }
    }

    @Getter
    public static class FileSave {
      @JsonProperty("TITLE")
      private String title = "<dark_gray>Data Saver";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("BACK-TO")
        private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Back to Server Manager");

        @JsonProperty("DATA-SAVE")
        private ItemConfig dataSave =
            ItemConfig.of("PAPER")
                .name("<yellow>%data% Data Save")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to manually",
                    "<gray>save the %data% data.",
                    "<dark_gray><st>------------------------");
      }
    }
  }
}
