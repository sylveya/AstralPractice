package dev.lokspel.practice.config.gui;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.config.ItemConfig;
import lombok.Getter;

@Getter
public class SetupFfaArena {
  @JsonProperty("MAIN")
  private Main main = new Main();

  @JsonProperty("SETTINGS")
  private Settings settings = new Settings();

  @JsonProperty("ARENA-LADDERS-SINGLE")
  private ArenaLaddersSingle arenaLaddersSingle = new ArenaLaddersSingle();

  @Getter
  public static class Main {
    @JsonProperty("TITLE")
    private String title = "<blue>%arenaName% <dark_gray>- Arena";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("GO-BACK")
      private ItemConfig goBack = ItemConfig.of("ARROW").name("<red>Go Back");

      @JsonProperty("DELETE")
      private ItemConfig delete = ItemConfig.of("RED_DYE").name("<red>Delete Arena");

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

      @JsonProperty("SETTINGS")
      private ItemConfig settings =
          ItemConfig.of("REDSTONE")
              .name("<dark_purple>Settings")
              .lore(
                  "",
                  "<yellow><bold>Click here <gray>to open the settings gui.",
                  "",
                  "<red><bold>Note: <gray>You can change the build setting,",
                  "<gray>re-kit after kill, health reset on kill",
                  "<gray>and the lobby after death setting.");

      @JsonProperty("STATUS")
      private Status status = new Status();

      @JsonProperty("LOCATION")
      private ItemConfig location =
          ItemConfig.of("COMPASS")
              .name("<dark_purple>Locations")
              .lore(
                  "",
                  "<dark_purple><bold>Currently Marked:",
                  " <dark_purple>» <light_purple>Corner 1: <gray>%corner1%",
                  " <dark_purple>» <light_purple>Corner 2: <gray>%corner2%",
                  "",
                  " <dark_purple>» <light_purple>Spawn Positions: <gray>%ffa_pos_num%");

      @JsonProperty("LADDER")
      private ItemConfig ladder =
          ItemConfig.of("GOLDEN_SWORD")
              .name("<red>Ladder Types")
              .lore(
                  "",
                  "<yellow><bold>Click here <gray>to open the ladder assign gui.",
                  "",
                  "<red><bold>Note: <gray>You can only assign basic or build",
                  "<gray>ladders to your FFA arena depending on the arenas",
                  "<gray>build setting.",
                  "",
                  "<red><bold>Note: <gray>You have to assign at least",
                  "<gray>one ladder before enabling.");

      @JsonProperty("OPEN-STATUS")
      private OpenStatus openStatus = new OpenStatus();

      @Getter
      public static class Status {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("LIME_STAINED_GLASS_PANE")
                .name("<gray>Status: <green>Enabled")
                .lore(
                    "",
                    "<yellow><bold>Click here <gray>to <red>disable <gray>the arena.",
                    "",
                    "<red><bold>Note: <gray>This will remove all the",
                    "<gray>players from the arena.");

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
                    " <dark_gray>» <yellow>Add spawn positions.",
                    " <dark_gray>» <yellow>Set an icon.");
      }

      @Getter
      public static class OpenStatus {
        @JsonProperty("OPEN")
        private ItemConfig open =
            ItemConfig.of("LIME_STAINED_GLASS_PANE")
                .name("<gray>The <yellow>FFA <gray>is currently <green>open")
                .lore(
                    "",
                    "<gray>This means that players can join",
                    "<gray>and play in the arena.",
                    "",
                    "<yellow><bold>Click here <gray>to <red>CLOSE <gray>the arena.",
                    "",
                    "<red><bold>Note: <gray>This will remove all the",
                    "<gray>players from the arena.");

        @JsonProperty("CLOSE")
        private ItemConfig close =
            ItemConfig.of("RED_STAINED_GLASS_PANE")
                .name("<gray>The <yellow>FFA <gray>is currently <red>closed")
                .lore(
                    "",
                    "<yellow><bold>Click here <gray>to <green>OPEN <gray>the arena.",
                    "",
                    "<red><bold>Note: <gray>The arena has to be enabled.");
      }
    }
  }

  @Getter
  public static class Settings {
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

      @JsonProperty("BUILD")
      private Build build = new Build();

      @JsonProperty("MAP-BLOWABLE")
      private MapBlowable mapBlowable = new MapBlowable();

      @JsonProperty("RE-KIT-AFTER-KILL")
      private ReKitAfterKill reKitAfterKill = new ReKitAfterKill();

      @JsonProperty("ALLOW-DROP-ITEMS")
      private AllowDropItems allowDropItems = new AllowDropItems();

      @JsonProperty("HEALTH-RESET-ON-KILL")
      private HealthResetOnKill healthResetOnKill = new HealthResetOnKill();

      @JsonProperty("HEALTH-BELOW-NAME")
      private HealthBelowName healthBelowName = new HealthBelowName();

      @JsonProperty("LOBBY-AFTER-DEATH")
      private LobbyAfterDeath lobbyAfterDeath = new LobbyAfterDeath();

      @JsonProperty("LOCATOR-BAR")
      private LocatorBar locatorBar = new LocatorBar();

      @Getter
      public static class Build {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("IRON_PICKAXE")
                .name("<gray>Build: <green>Enabled")
                .lore(
                    "",
                    "<yellow><bold>Click here <gray>to <red>disable <gray>the",
                    "<dark_green>building <gray>in the arena.",
                    "",
                    "<red><bold>Note: <gray>This will remove all the",
                    "<gray>assigned ladders from the arena.");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("RED_STAINED_GLASS_PANE")
                .name("<gray>Build: <red>Disabled")
                .lore(
                    "",
                    "<yellow><bold>Click here <gray>to <green>enable <gray>the",
                    "<dark_green>building <gray>in the arena.",
                    "",
                    "<red><bold>Note: <gray>This will remove all the",
                    "<gray>assigned ladders from the arena.");
      }

      @Getter
      public static class MapBlowable {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("TNT")
                .name("<gray>Blowable: <green>Enabled")
                .lore(
                    "",
                    "<yellow><bold>Click here <gray>to <red>disable <gray>map",
                    "<dark_green>blowable <gray>in the arena.",
                    "",
                    "<gray>When enabled, explosions can",
                    "<gray>blow up the entire arena map.");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("RED_STAINED_GLASS_PANE")
                .name("<gray>Blowable: <red>Disabled")
                .lore(
                    "",
                    "<yellow><bold>Click here <gray>to <green>enable <gray>map",
                    "<dark_green>blowable <gray>in the arena.",
                    "",
                    "<gray>When enabled, explosions can",
                    "<gray>blow up the entire arena map.");
      }

      @Getter
      public static class ReKitAfterKill {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("DIAMOND_SWORD")
                .name("<gray>Re-Kit After Kill: <green>Enabled")
                .lore(
                    "",
                    "<yellow>After a player kills somebody",
                    "<yellow>they get a new kit.",
                    "",
                    "<yellow><bold>Click here <gray>to <red>disable <gray>the",
                    "<dark_green>re-kit after kill <gray>in the arena.");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("RED_STAINED_GLASS_PANE")
                .name("<gray>Re-Kit After Kill: <red>Disabled")
                .lore(
                    "",
                    "<yellow>After a player kills somebody",
                    "<yellow>they get a new kit.",
                    "",
                    "<yellow><bold>Click here <gray>to <green>enable <gray>the",
                    "<dark_green>re-kit after kill <gray>in the arena.");
      }

      @Getter
      public static class AllowDropItems {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("CHEST")
                .name("<gray>Allow Drop Items: <green>Enabled")
                .lore(
                    "",
                    "<yellow>Players can drop items",
                    "<yellow>from their inventory.",
                    "",
                    "<yellow><bold>Click here <gray>to <red>disable <gray>the",
                    "<dark_green>allow drop items <gray>in the arena.");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("RED_STAINED_GLASS_PANE")
                .name("<gray>Allow Drop Items: <red>Disabled")
                .lore(
                    "",
                    "<yellow>Players can drop items",
                    "<yellow>from their inventory.",
                    "",
                    "<yellow><bold>Click here <gray>to <green>enable <gray>the",
                    "<dark_green>allow drop items <gray>in the arena.");
      }

      @Getter
      public static class HealthResetOnKill {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("GOLDEN_APPLE")
                .name("<gray>Health Reset On Kill: <green>Enabled")
                .lore(
                    "",
                    "<yellow>After a player kills somebody",
                    "<yellow>they are fully healed.",
                    "",
                    "<yellow><bold>Click here <gray>to <red>disable <gray>the",
                    "<dark_green>health reset on kill <gray>in the arena.");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("RED_STAINED_GLASS_PANE")
                .name("<gray>Health Reset On Kill: <red>Disabled")
                .lore(
                    "",
                    "<yellow>After a player kills somebody",
                    "<yellow>they are fully healed.",
                    "",
                    "<yellow><bold>Click here <gray>to <green>enable <gray>the",
                    "<dark_green>health reset on kill <gray>in the arena.");
      }

      @Getter
      public static class HealthBelowName {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("PLAYER_HEAD")
                .name("<gray>Health Below Name: <green>Enabled")
                .lore(
                    "",
                    "<yellow>Shows players' health under",
                    "<yellow>their name tags in this FFA.",
                    "",
                    "<yellow><bold>Click here <gray>to <red>disable <gray>this setting.");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("RED_STAINED_GLASS_PANE")
                .name("<gray>Health Below Name: <red>Disabled")
                .lore(
                    "",
                    "<yellow>Shows players' health under",
                    "<yellow>their name tags in this FFA.",
                    "",
                    "<yellow><bold>Click here <gray>to <green>enable <gray>this setting.");
      }

      @Getter
      public static class LocatorBar {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("COMPASS")
                .name("<gray>Locator Bar: <green>Enabled")
                .lore(
                    "",
                    "<yellow><bold>Click here <gray>to <red>disable <gray>the",
                    "<dark_green>locator bar <gray>in the arena.");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("RED_STAINED_GLASS_PANE")
                .name("<gray>Locator Bar: <red>Disabled")
                .lore(
                    "",
                    "<yellow><bold>Click here <gray>to <green>enable <gray>the",
                    "<dark_green>locator bar <gray>in the arena.");
      }

      @Getter
      public static class LobbyAfterDeath {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("DIAMOND_SWORD")
                .name("<gray>Lobby After Death: <green>Enabled")
                .lore(
                    "",
                    "<yellow>After a player dies they get",
                    "<yellow>teleported back to the arena.",
                    "",
                    "<yellow><bold>Click here <gray>to <red>disable <gray>the",
                    "<dark_green>lobby after death <gray>setting in the arena.");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("RED_STAINED_GLASS_PANE")
                .name("<gray>Lobby After Death: <red>Disabled")
                .lore(
                    "",
                    "<yellow>After a player dies they get",
                    "<yellow>teleported back to the arena.",
                    "",
                    "<yellow><bold>Click here <gray>to <green>enable <gray>the",
                    "<dark_green>lobby after death <gray>setting in the arena.");
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
      }
    }
  }
}
