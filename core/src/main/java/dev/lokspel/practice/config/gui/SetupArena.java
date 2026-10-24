package dev.lokspel.practice.config.gui;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.config.ItemConfig;
import lombok.Getter;

@Getter
public class SetupArena {
  @JsonProperty("ARENA-MANAGER")
  private ArenaManager arenaManager = new ArenaManager();

  @JsonProperty("ARENA-MAIN")
  private ArenaMain arenaMain = new ArenaMain();

  @JsonProperty("ARENA-COPY")
  private ArenaCopy arenaCopy = new ArenaCopy();

  @JsonProperty("ARENA-LADDERS-TYPE")
  private ArenaLaddersType arenaLaddersType = new ArenaLaddersType();

  @JsonProperty("ARENA-LADDERS-SINGLE")
  private ArenaLaddersSingle arenaLaddersSingle = new ArenaLaddersSingle();

  @JsonProperty("ARENA-SETTINGS")
  private ArenaSettings arenaSettings = new ArenaSettings();

  @Getter
  public static class ArenaManager {
    @JsonProperty("TITLE")
    private String title = "<dark_gray>Arena Manager - Page %page%";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("PAGE-LEFT")
      private ItemConfig pageLeft = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

      @JsonProperty("BACK-TO")
      private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Back to Server Manager");

      @JsonProperty("PAGE-RIGHT")
      private ItemConfig pageRight = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

      @JsonProperty("ARENA-ICON")
      private ArenaIcon arenaIcon = new ArenaIcon();

      @Getter
      public static class ArenaIcon {
        @JsonProperty("STATUS-NAMES")
        private StatusNames statusNames = new StatusNames();

        @JsonProperty("BUILD")
        private ItemConfig build =
            ItemConfig.of("BEDROCK")
                .name("<gold>%arenaName%")
                .lore(
                    "",
                    "<gold>Arena Information:",
                    " <gray>» <yellow>Type: <aqua>%type%",
                    " <gray>» <yellow>State: <aqua>%state%",
                    " <gray>» <yellow>Copies: <gray>%copies%/64",
                    " <gray>» <yellow>Ladders: <gray>%assigned_ladders%/%assignable_ladders%",
                    "",
                    "<green><bold>Left-Click <green>to open arena settings.",
                    "<aqua><bold>Right-Click <aqua>to to teleport to arena.");

        @JsonProperty("NON-BUILD")
        private ItemConfig nonBuild =
            ItemConfig.of("BEDROCK")
                .name("<gold>%arenaName%")
                .lore(
                    "",
                    "<gold>Arena Information:",
                    " <gray>» <yellow>Type: <aqua>%type%",
                    " <gray>» <yellow>State: <aqua>%state%",
                    " <gray>» <yellow>Ladders: <gray>%assigned_ladders%/%assignable_ladders%",
                    "",
                    "<green><bold>Left-Click <green>to open arena settings.",
                    "<aqua><bold>Right-Click <aqua>to to teleport to arena.");

        @JsonProperty("FFA")
        private ItemConfig ffa =
            ItemConfig.of("BEDROCK")
                .name("<gold>%arenaName%")
                .lore(
                    "",
                    "<gold>Arena Information:",
                    " <gray>» <yellow>Type: <aqua>FFA",
                    " <gray>» <yellow>State: <aqua>%state%",
                    " <gray>» <yellow>Build: <aqua>%build%",
                    "",
                    "<green><bold>Left-Click <green>to open arena settings.",
                    "<aqua><bold>Right-Click <aqua>to to teleport to arena.");

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
  public static class ArenaMain {
    @JsonProperty("TITLE")
    private String title = "<dark_red>%arenaName% <dark_gray>- Arena";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("GO-BACK")
      private ItemConfig goBack = ItemConfig.of("ARROW").name("<red>Go Back");

      @JsonProperty("DELETE")
      private ItemConfig delete = ItemConfig.of("RED_DYE").name("<red>Delete Arena");

      @JsonProperty("SETTINGS")
      private ItemConfig settings =
          ItemConfig.of("REDSTONE")
              .name("<dark_purple>Settings")
              .lore(
                  "",
                  "<yellow><bold>Click here <gray>to open the arena settings gui.",
                  "",
                  "<red><bold>Note: <gray>Configure custom kits availability",
                  "<gray>and arena build limits.");

      @JsonProperty("ARENA-NAME")
      private ItemConfig arenaName =
          ItemConfig.of("BEDROCK")
              .name("<gold>%arenaDisplayName%")
              .lore(
                  "",
                  "<yellow>Arena Type: <aqua>%arenaType%",
                  "",
                  "<yellow><bold>Left-Click here <gray>to get detailed information.",
                  "<gold><bold>Right-Click here <gray>to get a setup item for the arena.",
                  "",
                  "<gray>You can set the arenas icon by using the",
                  "<gray><bold>/arena set icon %arenaName% <gray>command.",
                  "",
                  "<red><bold>Note: <gray>You have to hold the item in your hand",
                  "<gray>and name it first with the <gray><bold>/prac rename <gray>command.");

      @JsonProperty("STATUS")
      private Status status = new Status();

      @JsonProperty("LOCATION")
      private Location location = new Location();

      @JsonProperty("LADDER")
      private ItemConfig ladder =
          ItemConfig.of("GOLDEN_SWORD")
              .name("<red>Ladder Types")
              .lore(
                  "",
                  "<yellow><bold>Click here <gray>to open the ladder type setup gui.",
                  "",
                  "<red><bold>Note: <gray>You won't be able to enable",
                  "<gray>non-build ladders for build arenas and vice versa.",
                  "",
                  "<red><bold>Note: <gray>You have to add at least",
                  "<gray>one ladder before enabling.");

      @JsonProperty("COPIES")
      private ItemConfig copies =
          ItemConfig.of("HOPPER")
              .name("<yellow>Arena Copies")
              .lore(
                  "",
                  "<yellow><bold>Click here <gray>to complete the following",
                  "<gray>tasks with the arena copies:",
                  " <dark_gray>» <gray>Create",
                  " <dark_gray>» <gray>Delete",
                  " <dark_gray>» <gray>Look through",
                  " <dark_gray>» <gray>Teleport");

      @JsonProperty("FREEZE")
      private Freeze freeze = new Freeze();

      @Getter
      public static class Status {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("LIME_STAINED_GLASS_PANE")
                .name("<gray>Status: <green>Enabled")
                .lore("", "<yellow><bold>Click here <gray>to <red>disable <gray>the arena.");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("RED_STAINED_GLASS_PANE")
                .name("<gray>Status: <red>Disabled")
                .lore(
                    "",
                    "<yellow><bold>Click here <gray>to <green>enable <gray>the arena.",
                    "",
                    "<red><bold>Note: <gray>The arena has to meet some",
                    "<gray>requirements to be enable:",
                    " <dark_gray>» <yellow>Mark all the corners.",
                    " <dark_gray>» <yellow>Set all the positions.",
                    " <dark_gray>» <yellow>Set an icon.");
      }

      @Getter
      public static class Location {
        @JsonProperty("BUILD")
        private ItemConfig build =
            ItemConfig.of("COMPASS")
                .name("<dark_purple>Locations")
                .lore(
                    "",
                    "<dark_purple><bold>Currently Marked:",
                    " <dark_purple>» <light_purple>Corner 1: <gray>%corner1%",
                    " <dark_purple>» <light_purple>Corner 2: <gray>%corner2%",
                    "",
                    " <dark_purple>» <light_purple>Side Build Limit: <gray>%sideBuildLimit%",
                    " <dark_purple>» <light_purple>Build Max Y: <gray>%buildMaxY%",
                    " <dark_purple>» <light_purple>Death Zone Y: <gray>%deathZoneY%",
                    "",
                    " <dark_purple>» <light_purple>Player 1 Position: <gray>%position1%",
                    " <dark_purple>» <light_purple>Player 2 Position: <gray>%position2%",
                    "",
                    " <dark_purple>» <light_purple>Bed 1: <gray>%bed1%",
                    " <dark_purple>» <light_purple>Bed 2: <gray>%bed2%",
                    " <dark_purple>» <light_purple>Portal 1: <gray>%portal1%",
                    " <dark_purple>» <light_purple>Portal 2: <gray>%portal2%");

        @JsonProperty("NOT-BUILD")
        private ItemConfig notBuild =
            ItemConfig.of("COMPASS")
                .name("<dark_purple>Locations")
                .lore(
                    "",
                    "<dark_purple><bold>Currently Marked:",
                    " <dark_purple>» <light_purple>Corner 1: <gray>%corner1%",
                    " <dark_purple>» <light_purple>Corner 2: <gray>%corner2%",
                    "",
                    " <dark_purple>» <light_purple>Death Zone Y: <gray>%deathZoneY%",
                    "",
                    " <dark_purple>» <light_purple>Player 1 Position: <gray>%position1%",
                    " <dark_purple>» <light_purple>Player 2 Position: <gray>%position2%");
      }

      @Getter
      public static class Freeze {
        @JsonProperty("FROZEN")
        private ItemConfig frozen =
            ItemConfig.of("SNOWBALL")
                .name("<yellow>The arena is <aqua>frozen<yellow>.")
                .lore(
                    "",
                    "<gray>Click here to <green>defrost<gray>.",
                    "",
                    "<gray>The players will be able to",
                    "<gray>play in this arena again.");

        @JsonProperty("NOT-FROZEN")
        private ItemConfig notFrozen =
            ItemConfig.of("SUNFLOWER")
                .name("<yellow>The arena is <green>not frozen<yellow>.")
                .lore(
                    "",
                    "<yellow>Click here to <aqua>freeze <yellow>it.",
                    "",
                    "<gray>This means that the players won't be",
                    "<gray>able to start a new match in this arena.");
      }
    }
  }

  @Getter
  public static class ArenaCopy {
    @JsonProperty("TITLE")
    private String title = "<dark_red>%arenaName% <dark_gray>- Copies, Page %page%";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("NAV-MAIN")
      private ItemConfig navMain =
          ItemConfig.of("REDSTONE")
              .name("<red>%arenaDisplayName% <gray>Arena Copies Manager")
              .lore(
                  "",
                  "<gray>Copies: <yellow>%copies%<gray>/64",
                  "",
                  "<red>Note: <gray>You can't change anything in the",
                  "<gray>copies, nor the arena, if it has copies.");

      @JsonProperty("COPY-ARENA")
      private ItemConfig copyArena =
          ItemConfig.of("HOPPER")
              .name("<red>%arenaDisplayName% <gray>(<yellow>%copyNumber%<gray>)")
              .lore("", "<yellow><bold>Click here <gray>to teleport to the copy arena.");

      @JsonProperty("PAGE-LEFT")
      private ItemConfig pageLeft = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

      @JsonProperty("BACK-TO")
      private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Go Back");

      @JsonProperty("PAGE-RIGHT")
      private ItemConfig pageRight = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

      @JsonProperty("DELETE")
      private ItemConfig delete =
          ItemConfig.of("ARROW")
              .name("<yellow>Click here to <red>delete <yellow>the <gold>last <yellow>copy.");

      @JsonProperty("GENERATE")
      private ItemConfig generate =
          ItemConfig.of("ARROW")
              .name("<yellow>Click here to <green>generate <yellow>a <gold>new <yellow>copy.");
    }
  }

  @Getter
  public static class ArenaLaddersType {
    @JsonProperty("TITLE")
    private String title = "<dark_red>%arenaName% <dark_gray>- Ladders";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("BACK-TO")
      private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Go Back");

      @JsonProperty("GO-TO-LADDERS")
      private ItemConfig goToLadders =
          ItemConfig.of("GOLDEN_CARROT")
              .name("<red>Ladders")
              .lore(
                  "",
                  "<yellow><bold>Click here <gray>to open the ladder setup gui.",
                  "",
                  "<red><bold>Note: <gray>The plugin automatically assign",
                  "<gray>every ladder of which type is enabled.");

      @JsonProperty("LADDER-TYPE-ICONS")
      private LadderTypeIcons ladderTypeIcons = new LadderTypeIcons();

      @JsonProperty("CUSTOM-KIT-ICONS")
      private CustomKitIcons customKitIcons = new CustomKitIcons();

      @Getter
      public static class LadderTypeIcons {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            new ItemConfig()
                .name("<green>%ladderTypeName%")
                .lore(
                    "",
                    "<gray>Ladder type is <green>enabled<gray>.",
                    "",
                    "<yellow>Click here <gray>to <red>disable <gray>it.");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            new ItemConfig()
                .name("<red>%ladderTypeName%")
                .lore(
                    "",
                    "<gray>Ladder type is <red>disabled<gray>.",
                    "",
                    "<yellow>Click here <gray>to <green>enable <gray>it.");
      }

      @Getter
      public static class CustomKitIcons {
        @JsonProperty("ICON")
        private ItemConfig icon =
            ItemConfig.of("ARROW")
                .name("<yellow>Custom Kit: %status%")
                .lore(
                    "",
                    "<gray>This setting decides if the players",
                    "<gray>can use their custom kits in this arena.");
      }
    }
  }

  @Getter
  public static class ArenaLaddersSingle {
    @JsonProperty("TITLE")
    private String title = "<dark_red>%arenaName% <dark_gray>- Ladders";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("BACK-TO")
      private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Go Back");

      @JsonProperty("PAGE-LEFT")
      private ItemConfig pageLeft = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

      @JsonProperty("PAGE-RIGHT")
      private ItemConfig pageRight = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

      @JsonProperty("LADDER-ICONS")
      private LadderIcons ladderIcons = new LadderIcons();

      @Getter
      public static class LadderIcons {
        @JsonProperty("ASSIGNED")
        private ItemConfig assigned =
            ItemConfig.of("LIME_STAINED_GLASS_PANE")
                .name("<green>%ladderDisplayName%")
                .lore(
                    "",
                    "<gray>This ladder is <green>assigned<gray> for the arena.",
                    "",
                    "<yellow>Click here <gray>to <red>unassign<gray>.");

        @JsonProperty("NOT-ASSIGNED")
        private ItemConfig notAssigned =
            ItemConfig.of("RED_STAINED_GLASS_PANE")
                .name("<red>%ladderDisplayName%")
                .lore(
                    "",
                    "<gray>This ladder is <red>unassigned<gray> for the arena.",
                    "",
                    "<yellow>Click here <gray>to <green>assign<gray>.");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("GRAY_STAINED_GLASS_PANE")
                .name("<gray>%ladderDisplayName%")
                .lore(
                    "",
                    "<gray>This ladder or it's ladder type",
                    "<gray>is currently <red>disabled<gray>.");

        @JsonProperty("NOT-COMPATIBLE")
        private ItemConfig notCompatible =
            ItemConfig.of("GRAY_STAINED_GLASS_PANE")
                .name("<gray>%ladderDisplayName%")
                .lore(
                    "",
                    "<gray>This ladder is <red>not compatible",
                    "<gray>with the arena because of the",
                    "<gray>build status.");
      }
    }
  }

  @Getter
  public static class ArenaSettings {
    @JsonProperty("TITLE")
    private String title = "<dark_red>%arenaName% <dark_gray>- Settings";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("GO-BACK")
      private ItemConfig goBack = ItemConfig.of("ARROW").name("<red>Go Back");

      @JsonProperty("FILLER")
      private ItemConfig filler = ItemConfig.of("BLACK_STAINED_GLASS_PANE");

      @JsonProperty("CUSTOM-KITS")
      private CustomKits customKits = new CustomKits();

      @JsonProperty("SIDE-BUILD-LIMIT")
      private ItemConfig sideBuildLimit =
          ItemConfig.of("BRICKS")
              .name("<yellow>Side Build Limit: <aqua>%sideBuildLimit% <gray>blocks")
              .lore(
                  "",
                  "<gray>Maximum blocks players can build",
                  "<gray>outside the arena boundary.",
                  "",
                  "<yellow><bold>Left-Click: <green>+1 block",
                  "<yellow><bold>Right-Click: <red>-1 block",
                  "",
                  "<red><bold>Note: <gray>Allowed range is 0-10 blocks.");

      @JsonProperty("PORTAL-PROTECTION")
      private ItemConfig portalProtection =
          ItemConfig.of("END_PORTAL_FRAME")
              .name("<yellow>Portal Protection: <aqua>%portalProtection% <gray>blocks")
              .lore(
                  "",
                  "<gray>Protected radius around portals",
                  "<gray>preventing block placement and destruction.",
                  "",
                  "<yellow><bold>Left-Click: <green>+1 block",
                  "<yellow><bold>Right-Click: <red>-1 block",
                  "",
                  "<red><bold>Note: <gray>Allowed range is 0-20 blocks (0 = disabled).");

      @Getter
      public static class CustomKits {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("CHEST")
                .name("<gray>Custom Kits: <green>Allowed")
                .lore(
                    "",
                    "<gray>Players <green>can <gray>use their custom kits",
                    "<gray>in this arena.",
                    "",
                    "<yellow><bold>Click here <gray>to <red>disallow<gray>.");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("BARRIER")
                .name("<gray>Custom Kits: <red>Disallowed")
                .lore(
                    "",
                    "<gray>Players <red>cannot <gray>use custom kits",
                    "<gray>in this arena.",
                    "",
                    "<yellow><bold>Click here <gray>to <green>allow<gray>.");
      }
    }
  }
}
