package dev.lokspel.practice.config.playerkit;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.config.ItemConfig;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class Gui {
  @JsonProperty("MAIN")
  private Main main = new Main();

  @JsonProperty("MAIN-ARMOR")
  private MainArmor mainArmor = new MainArmor();

  @JsonProperty("CUSTOM-SETTINGS")
  private CustomSettings customSettings = new CustomSettings();

  @JsonProperty("ITEMS")
  private Items items = new Items();

  @Getter
  public static class Main {
    @JsonProperty("TITLE")
    private String title = "<dark_gray>%name%";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("BACK-TO")
      private ItemConfig backTo =
          ItemConfig.of("ARROW").name("<gray><bold>Back").lore("<gray>Click to go back");

      @JsonProperty("CUSTOM-SETTINGS")
      private ItemConfig customSettings = ItemConfig.of("COMPARATOR").name("<aqua>Custom Settings");

      @JsonProperty("GUIDE")
      private ItemConfig guide =
          ItemConfig.of("PAPER")
              .name("<aqua><bold>Guide")
              .lore(
                  "<white>Left click to change item",
                  "<white>Shift left click to remove item",
                  "<white>Right click to enchant or change count",
                  "<white>Shift right click to copy or paste item");

      @JsonProperty("RESET-KIT")
      private ItemConfig resetKit = ItemConfig.of("GUNPOWDER").name("<red>Reset Kit");

      @JsonProperty("CHANGE-NAME")
      private ItemConfig changeName =
          ItemConfig.of("NAME_TAG").name("<aqua>Change the name of kit");

      @JsonProperty("SHARE-KIT")
      private ItemConfig shareKit = ItemConfig.of("OAK_SIGN").name("<aqua>Share Kit");

      @JsonProperty("KIT-SLOT-PLACEHOLDERS")
      private KitSlotPlaceholders kitSlotPlaceholders = new KitSlotPlaceholders();

      @Getter
      public static class KitSlotPlaceholders {
        @JsonProperty("HEAD")
        private ItemConfig head =
            ItemConfig.of("BLUE_STAINED_GLASS_PANE")
                .name("<gray>Head")
                .lore("<dark_aqua>Click to add");

        @JsonProperty("CHEST")
        private ItemConfig chest =
            ItemConfig.of("BLUE_STAINED_GLASS_PANE")
                .name("<gray>Chest")
                .lore("<dark_aqua>Click to add");

        @JsonProperty("LEGS")
        private ItemConfig legs =
            ItemConfig.of("BLUE_STAINED_GLASS_PANE")
                .name("<gray>Legs")
                .lore("<dark_aqua>Click to add");

        @JsonProperty("BOOTS")
        private ItemConfig boots =
            ItemConfig.of("BLUE_STAINED_GLASS_PANE")
                .name("<gray>Feet")
                .lore("<dark_aqua>Click to add");

        @JsonProperty("OFFHAND")
        private ItemConfig offhand =
            ItemConfig.of("BLUE_STAINED_GLASS_PANE")
                .name("<gray>Offhand")
                .lore("<dark_aqua>Click to add");

        @JsonProperty("HOTBAR")
        private ItemConfig hotbar =
            ItemConfig.of("LIGHT_BLUE_STAINED_GLASS_PANE")
                .name("<gray>None")
                .lore("<dark_aqua>Click to add");

        @JsonProperty("OTHER-INVENTORY")
        private ItemConfig otherInventory =
            ItemConfig.of("GRAY_STAINED_GLASS_PANE")
                .name("<gray>None")
                .lore("<dark_aqua>Click to add");
      }
    }
  }

  @Getter
  public static class MainArmor {
    @JsonProperty("TITLE")
    private String title = "<dark_gray>Armor";

    @JsonProperty("SIZE")
    private int size = 6;

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("OFFICIAL")
      private Official official = new Official();

      @JsonProperty("HELMETS")
      private Map<String, ArmorIcon> helmets = defaultHelmets();

      @JsonProperty("CHESTPLATES")
      private Map<String, ArmorIcon> chestplates = defaultChestplates();

      @JsonProperty("LEGGINGS")
      private Map<String, ArmorIcon> leggings = defaultLeggings();

      @JsonProperty("BOOTS")
      private Map<String, ArmorIcon> boots = defaultBoots();

      private static Map<String, ArmorIcon> defaultHelmets() {
        Map<String, ArmorIcon> values = new LinkedHashMap<>();
        values.put("LEATHER", ArmorIcon.helmetsLeather());
        values.put("GOLD", ArmorIcon.helmetsGold());
        values.put("CHAIN", ArmorIcon.helmetsChain());
        values.put("TURTLE", ArmorIcon.turtle());
        values.put("IRON", ArmorIcon.helmetsIron());
        values.put("DIAMOND", ArmorIcon.helmetsDiamond());
        values.put("NETHERITE", ArmorIcon.helmetsNetherite());
        values.put("PUMPKIN", ArmorIcon.pumpkin());

        return values;
      }

      private static Map<String, ArmorIcon> defaultChestplates() {
        Map<String, ArmorIcon> values = new LinkedHashMap<>();
        values.put("LEATHER", ArmorIcon.chestplatesLeather());
        values.put("GOLD", ArmorIcon.chestplatesGold());
        values.put("CHAIN", ArmorIcon.chestplatesChain());
        values.put("IRON", ArmorIcon.chestplatesIron());
        values.put("DIAMOND", ArmorIcon.chestplatesDiamond());
        values.put("NETHERITE", ArmorIcon.chestplatesNetherite());
        values.put("ELYTRA", ArmorIcon.elytra());

        return values;
      }

      private static Map<String, ArmorIcon> defaultLeggings() {
        Map<String, ArmorIcon> values = new LinkedHashMap<>();
        values.put("LEATHER", ArmorIcon.leggingsLeather());
        values.put("GOLD", ArmorIcon.leggingsGold());
        values.put("CHAIN", ArmorIcon.leggingsChain());
        values.put("IRON", ArmorIcon.leggingsIron());
        values.put("DIAMOND", ArmorIcon.leggingsDiamond());
        values.put("NETHERITE", ArmorIcon.leggingsNetherite());

        return values;
      }

      private static Map<String, ArmorIcon> defaultBoots() {
        Map<String, ArmorIcon> values = new LinkedHashMap<>();
        values.put("LEATHER", ArmorIcon.bootsLeather());
        values.put("GOLD", ArmorIcon.bootsGold());
        values.put("CHAIN", ArmorIcon.bootsChain());
        values.put("IRON", ArmorIcon.bootsIron());
        values.put("DIAMOND", ArmorIcon.bootsDiamond());
        values.put("NETHERITE", ArmorIcon.bootsNetherite());

        return values;
      }

      @Getter
      public static class Official {
        @JsonProperty("BACK-TO")
        private BackTo backTo = new BackTo();

        @JsonProperty("NONE")
        private None none = new None();

        @Getter
        public static class BackTo extends ItemConfig {
          @JsonProperty("SLOT")
          private int slot = 45;

          {
            material("ARROW");
            name("<gray><bold>Back");
            lore("<gray>Click to go back");
          }
        }

        @Getter
        public static class None extends ItemConfig {
          @JsonProperty("SLOT")
          private int slot = 18;

          {
            material("GRAY_STAINED_GLASS_PANE");
            name("<gray>None");
          }
        }
      }

      @Getter
      public static class ArmorIcon extends ItemConfig {
        @JsonProperty("SLOT")
        private Integer slot;

        static ArmorIcon helmetsLeather() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 19;
          value.material("LEATHER_HELMET");

          return value;
        }

        static ArmorIcon helmetsGold() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 20;
          value.material("GOLDEN_HELMET");

          return value;
        }

        static ArmorIcon helmetsChain() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 21;
          value.material("CHAINMAIL_HELMET");

          return value;
        }

        static ArmorIcon turtle() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 22;
          value.material("TURTLE_HELMET");

          return value;
        }

        static ArmorIcon helmetsIron() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 23;
          value.material("IRON_HELMET");

          return value;
        }

        static ArmorIcon helmetsDiamond() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 24;
          value.material("DIAMOND_HELMET");

          return value;
        }

        static ArmorIcon helmetsNetherite() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 25;
          value.material("NETHERITE_HELMET");

          return value;
        }

        static ArmorIcon pumpkin() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 26;
          value.material("CARVED_PUMPKIN");

          return value;
        }

        static ArmorIcon chestplatesLeather() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 19;
          value.material("LEATHER_CHESTPLATE");

          return value;
        }

        static ArmorIcon chestplatesGold() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 20;
          value.material("GOLDEN_CHESTPLATE");

          return value;
        }

        static ArmorIcon chestplatesChain() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 21;
          value.material("CHAINMAIL_CHESTPLATE");

          return value;
        }

        static ArmorIcon chestplatesIron() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 22;
          value.material("IRON_CHESTPLATE");

          return value;
        }

        static ArmorIcon chestplatesDiamond() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 23;
          value.material("DIAMOND_CHESTPLATE");

          return value;
        }

        static ArmorIcon chestplatesNetherite() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 24;
          value.material("NETHERITE_CHESTPLATE");

          return value;
        }

        static ArmorIcon elytra() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 25;
          value.material("ELYTRA");

          return value;
        }

        static ArmorIcon leggingsLeather() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 19;
          value.material("LEATHER_LEGGINGS");

          return value;
        }

        static ArmorIcon leggingsGold() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 20;
          value.material("GOLDEN_LEGGINGS");

          return value;
        }

        static ArmorIcon leggingsChain() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 21;
          value.material("CHAINMAIL_LEGGINGS");

          return value;
        }

        static ArmorIcon leggingsIron() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 22;
          value.material("IRON_LEGGINGS");

          return value;
        }

        static ArmorIcon leggingsDiamond() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 23;
          value.material("DIAMOND_LEGGINGS");

          return value;
        }

        static ArmorIcon leggingsNetherite() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 24;
          value.material("NETHERITE_LEGGINGS");

          return value;
        }

        static ArmorIcon bootsLeather() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 19;
          value.material("LEATHER_BOOTS");

          return value;
        }

        static ArmorIcon bootsGold() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 20;
          value.material("GOLDEN_BOOTS");

          return value;
        }

        static ArmorIcon bootsChain() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 21;
          value.material("CHAINMAIL_BOOTS");

          return value;
        }

        static ArmorIcon bootsIron() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 22;
          value.material("IRON_BOOTS");

          return value;
        }

        static ArmorIcon bootsDiamond() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 23;
          value.material("DIAMOND_BOOTS");

          return value;
        }

        static ArmorIcon bootsNetherite() {
          ArmorIcon value = new ArmorIcon();
          value.slot = 24;
          value.material("NETHERITE_BOOTS");

          return value;
        }
      }
    }
  }

  @Getter
  public static class CustomSettings {
    @JsonProperty("TITLE")
    private String title = "<dark_gray>Settings";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("NAV")
      private Nav nav = new Nav();

      @JsonProperty("NORMAL-SETTINGS")
      private NormalSettings normalSettings = new NormalSettings();

      @Getter
      public static class Nav {
        @JsonProperty("GO-BACK")
        private ItemConfig goBack = ItemConfig.of("ARROW").name("<red>Go Back");
      }

      @Getter
      public static class NormalSettings {
        @JsonProperty("REGENERATION")
        private Regeneration regeneration = new Regeneration();

        @JsonProperty("HUNGER")
        private Hunger hunger = new Hunger();

        @JsonProperty("BUILD")
        private Build build = new Build();

        @JsonProperty("ROUNDS")
        private ItemConfig rounds =
            ItemConfig.of("FIRE_CHARGE")
                .name("<gray>Rounds: <gold>%rounds%")
                .lore(
                    "",
                    "<gray>The first player/team to reach",
                    "<gray>the, winning rounds wins.",
                    "",
                    "<green><bold>LEFT-CLICK <green>to reduce the rounds.",
                    "<aqua><bold>RIGHT-CLICK <aqua>to increase the rounds.");

        @JsonProperty("KNOCKBACK")
        private ItemConfig knockback =
            ItemConfig.of("STICK").name("<yellow>Knockback Modifier").lore("", "%knockbackTypes%");

        @JsonProperty("HITDELAY")
        private ItemConfig hitdelay =
            ItemConfig.of("DIAMOND_SWORD")
                .name("<gray>Attack Cooldown: <gold>%hitdelay%x")
                .lore(
                    "",
                    "<gray>1.0 = Normal speed (20 ticks)",
                    "<gray>0.5 = 2x faster",
                    "<gray>2.0 = 2x slower",
                    "",
                    "<red><bold>Note: <gray>1 sec = 20 tick",
                    "",
                    "<green><bold>LEFT-CLICK <green>to reduce the rounds.",
                    "<aqua><bold>RIGHT-CLICK <aqua>to increase the rounds.");

        @JsonProperty("ENDERPEARL-COOLDOWN")
        private ItemConfig enderpearlCooldown =
            ItemConfig.of("ENDER_PEARL")
                .name("<gray>EnderPearl Cooldown: <gold>%epCooldown% sec")
                .lore(
                    "",
                    "<gray>Players must wait this long before",
                    "<gray>they can throw the next enderpearl.",
                    "",
                    "<green><bold>LEFT-CLICK <green>to reduce the cooldown.",
                    "<aqua><bold>RIGHT-CLICK <aqua>to increase the cooldown.");

        @JsonProperty("GOLDENAPPLE-COOLDOWN")
        private ItemConfig goldenappleCooldown =
            ItemConfig.of("GOLDEN_APPLE")
                .name("<gray>Golden Apple Cooldown: <gold>%gaCooldown%")
                .lore(
                    "",
                    "<gray>Players must wait this long before",
                    "<gray>they can consume the next golden apple.",
                    "",
                    "<gray>This setting includes both enchanted",
                    "<gray>and non-enchanted golden apples.",
                    "",
                    "<green><bold>LEFT-CLICK <green>to reduce the cooldown.",
                    "<aqua><bold>RIGHT-CLICK <aqua>to increase the cooldown.");

        @JsonProperty("WIND-CHARGE-COOLDOWN")
        private ItemConfig windChargeCooldown =
            ItemConfig.of("WIND_CHARGE")
                .name("<gray>Wind Charge Cooldown: <gold>%windChargeCooldown% sec")
                .lore(
                    "",
                    "<gray>Players must wait this long before",
                    "<gray>they can launch the next wind charge.",
                    "",
                    "<green><bold>LEFT-CLICK <green>to reduce the cooldown.",
                    "<aqua><bold>RIGHT-CLICK <aqua>to increase the cooldown.");

        @JsonProperty("HEALTH-BELOW-NAME")
        private HealthBelowName healthBelowName = new HealthBelowName();

        @Getter
        public static class Regeneration {
          @JsonProperty("ENABLED")
          private ItemConfig enabled =
              ItemConfig.of("GLOWSTONE_DUST")
                  .name("<gray>Regeneration: <green>Enabled")
                  .lore(
                      "",
                      "<gray>Players health is automatically regenerate.",
                      "",
                      "<yellow><bold>Click here <gray>to <red>disable<gray>.");

          @JsonProperty("DISABLED")
          private ItemConfig disabled =
              ItemConfig.of("GLOWSTONE_DUST")
                  .name("<gray>Regeneration: <red>Disabled")
                  .lore(
                      "",
                      "<gray>Players health is not regenerate.",
                      "",
                      "<yellow><bold>Click here <gray>to <green>enable<gray>.");
        }

        @Getter
        public static class Hunger {
          @JsonProperty("ENABLED")
          private ItemConfig enabled =
              ItemConfig.of("COOKED_BEEF")
                  .name("<gray>Hunger: <green>Enabled")
                  .lore(
                      "",
                      "<gray>Players starve during the game.",
                      "",
                      "<yellow><bold>Click here <gray>to <red>disable<gray>.");

          @JsonProperty("DISABLED")
          private ItemConfig disabled =
              ItemConfig.of("COOKED_BEEF")
                  .name("<gray>Hunger: <red>Disabled")
                  .lore(
                      "",
                      "<gray>Players don't starve.",
                      "",
                      "<yellow><bold>Click here <gray>to <green>enable<gray>.");
        }

        @Getter
        public static class Build {
          @JsonProperty("ENABLED")
          private ItemConfig enabled =
              ItemConfig.of("DIAMOND_PICKAXE")
                  .name("<gray>Build: <green>Enabled")
                  .lore(
                      "",
                      "<gray>Players can build during the game.",
                      "",
                      "<yellow><bold>Click here <gray>to <red>disable<gray>.");

          @JsonProperty("DISABLED")
          private ItemConfig disabled =
              ItemConfig.of("DIAMOND_PICKAXE")
                  .name("<gray>Build: <red>Disabled")
                  .lore(
                      "",
                      "<gray>Players can't build.",
                      "",
                      "<yellow><bold>Click here <gray>to <green>enable<gray>.");
        }

        @Getter
        public static class HealthBelowName {
          @JsonProperty("ENABLED")
          private ItemConfig enabled =
              ItemConfig.of("RED_DYE")
                  .name("<gray>Health Below Name: <green>Enabled")
                  .lore(
                      "",
                      "<gray>Players will display their hearts",
                      "<gray>during the match.",
                      "",
                      "<yellow><bold>Click here <gray>to <red>disable<gray>.");

          @JsonProperty("DISABLED")
          private ItemConfig disabled =
              ItemConfig.of("RED_DYE")
                  .name("<gray>Health Below Name: <red>Disabled")
                  .lore(
                      "",
                      "<gray>Players will display their hearts",
                      "<gray>during the match.",
                      "",
                      "<yellow><bold>Click here <gray>to <green>enable<gray>.");
        }
      }
    }
  }
}
