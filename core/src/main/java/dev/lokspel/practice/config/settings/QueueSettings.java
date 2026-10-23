package dev.lokspel.practice.config.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import me.whereareiam.configura.annotation.merge.Merge;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;
import me.whereareiam.configura.type.merge.MissingEntries;

@Getter
public class QueueSettings {
  @JsonProperty("CUSTOM-KIT")
  private CustomKit customKit;

  @Comment("In sec.")
  @JsonProperty("MAX-QUEUE-TIME")
  private Integer maxQueueTime;

  @JsonProperty("GUI-UPDATE-MINUTE")
  private Integer guiUpdateMinute;

  @JsonProperty("DIVISION-SEARCH")
  private DivisionSearch divisionSearch;

  @JsonProperty("SELECTOR-GUI")
  private SelectorGui selectorGui;

  @JsonProperty("DEFAULT-ELO")
  private Integer defaultElo;

  @Comment("Sec")
  @JsonProperty("ELO-RANGE-TIME")
  private Integer eloRangeTime;

  @Comment("Range")
  @JsonProperty("ELO-RANGE-INCREASE")
  private Integer eloRangeIncrease;

  @Comment(
      "Players receive a random elo between these two numbers. Only used when ELO-SYSTEM.MODE is"
          + " RANDOM.")
  @JsonProperty("ELO-CHANGE")
  private String eloChange;

  @Comment({
    "Elo system for ranked match Elo changes.",
    "MODE: SKILL (default) uses the standard Elo formula",
    "      (newElo = oldElo + K * (result - expected)) where expected is based on both players'"
        + " ratings,",
    "      so beating a much higher-rated player rewards more points than beating a much"
        + " lower-rated one.",
    "      RANDOM keeps the old behavior: both players receive the same random value from"
        + " ELO-CHANGE above."
  })
  @JsonProperty("ELO-SYSTEM")
  private EloSystem eloSystem;

  @Comment({
    "In SKILL mode the formula never exceeds K-FACTOR, so setting MAX-CHANGE below K-FACTOR (e.g."
        + " 15) adds a stricter",
    "guard against extreme rating swings and Elo farming. In RANDOM mode it caps the upper bound of"
        + " ELO-CHANGE.",
    "Set to 0 to disable the cap.",
    "Maximum ping a player can join a ranked queue."
  })
  @JsonProperty("MAX-PING")
  private Integer maxPing;

  static QueueSettings unranked() {
    QueueSettings value = new QueueSettings();
    value.customKit = CustomKit.unranked();
    value.maxQueueTime = 300;
    value.guiUpdateMinute = 2;
    value.divisionSearch = DivisionSearch.unranked();
    value.selectorGui = SelectorGui.unranked();

    return value;
  }

  static QueueSettings ranked() {
    QueueSettings value = new QueueSettings();
    value.customKit = CustomKit.ranked();
    value.maxQueueTime = 300;
    value.guiUpdateMinute = 1;
    value.selectorGui = SelectorGui.unranked();
    value.defaultElo = 1000;
    value.eloRangeTime = 5;
    value.eloRangeIncrease = 20;
    value.eloChange = "5-12";
    value.eloSystem = EloSystem.ranked();
    value.maxPing = 300;

    return value;
  }

  @Getter
  public static class CustomKit {
    @Comment("If false, the custom kit queue feature is disabled and only shows filler items")
    @JsonProperty("ENABLED")
    private Boolean enabled;

    @Comment("If true, ranked selector also shows the custom-kit queue entry.")
    @JsonProperty("ENTRY-ENABLED")
    private Boolean entryEnabled;

    static CustomKit unranked() {
      CustomKit value = new CustomKit();
      value.enabled = true;

      return value;
    }

    static CustomKit ranked() {
      CustomKit value = new CustomKit();
      value.entryEnabled = false;

      return value;
    }
  }

  @Getter
  public static class DivisionSearch {
    @JsonProperty("ENABLED")
    private Boolean enabled;

    @JsonProperty("SETTINGS")
    private Settings settings;

    static DivisionSearch unranked() {
      DivisionSearch value = new DivisionSearch();
      value.enabled = true;
      value.settings = Settings.unranked();

      return value;
    }

    @Getter
    public static class Settings {
      @Comment({
        "Mode for unranked division search:",
        "LADDER - kit-specific division (based on wins/elo in that ladder)",
        "GLOBAL - overall profile division (based on total wins/elo)"
      })
      @JsonProperty("MODE")
      private String mode;

      @Comment("In sec.")
      @JsonProperty("SEARCH-IN-OWN-DIVISIONS")
      private Integer searchInOwnDivisions;

      @Comment("In sec.")
      @JsonProperty("EXTEND-DIVISION-SEARCH")
      private Integer extendDivisionSearch;

      static Settings unranked() {
        Settings value = new Settings();
        value.mode = "GLOBAL";
        value.searchInOwnDivisions = 20;
        value.extendDivisionSearch = 10;

        return value;
      }

      public boolean isLadderMode() {
        return "LADDER".equalsIgnoreCase(mode);
      }
    }
  }

  @Getter
  public static class SelectorGui {
    @JsonProperty("SHOW-DISABLED-LADDERS")
    private Boolean showDisabledLadders;

    @JsonProperty("SIZE")
    private Integer size;

    @JsonProperty("QUICK-MATCH-ENABLED")
    private Boolean quickMatchEnabled;

    @JsonProperty("QUICK-MATCH-SLOT")
    private Integer quickMatchSlot;

    @JsonProperty("SWITCH-MODE-SLOT")
    private Integer switchModeSlot;

    @JsonProperty("LADDER-SLOTS")
    private List<Integer> ladderSlots;

    @Merge(missingEntries = MissingEntries.OMIT)
    @JsonProperty("CATEGORY-SELECTOR-SLOTS")
    private Map<String, Integer> categorySelectorSlots;

    @Merge(missingEntries = MissingEntries.OMIT)
    @JsonProperty("CATEGORIES")
    private Map<String, QueueCategory> categories;

    static SelectorGui unranked() {
      SelectorGui value = new SelectorGui();
      value.showDisabledLadders = true;
      value.size = 6;
      value.quickMatchEnabled = true;
      value.quickMatchSlot = 45;
      value.switchModeSlot = 49;
      value.ladderSlots = List.of(20, 21, 22, 23, 24, 29, 30, 31, 32, 33, 38, 39, 40, 41, 42);
      value.categorySelectorSlots = new LinkedHashMap<>();
      value.categorySelectorSlots.put("PVP", 3);
      value.categorySelectorSlots.put("MINIGAMES", 5);
      value.categories = new LinkedHashMap<>();
      value.categories.put("PVP", QueueCategory.pvp());
      value.categories.put("MINIGAMES", QueueCategory.minigames());

      return value;
    }

    @Getter
    public static class QueueCategory {
      @JsonProperty("DISPLAY-NAME")
      private String displayName;

      @JsonProperty("ICON-MATERIAL")
      private String iconMaterial;

      @JsonProperty("SELECTED-ICON-MATERIAL")
      private String selectedIconMaterial;

      @JsonProperty("ICON-DISABLED")
      private Boolean iconDisabled;

      @JsonProperty("LADDERS")
      private List<String> ladders;

      @Comment("Overrides SELECTOR-GUI.LADDER-SLOTS for this category when not empty.")
      @JsonProperty("LADDER-SLOTS")
      private List<Integer> ladderSlots;

      static QueueCategory pvp() {
        QueueCategory value = new QueueCategory();
        value.displayName = "PVP";
        value.iconMaterial = "DIAMOND_SWORD";
        value.selectedIconMaterial = "NETHERITE_SWORD";
        value.iconDisabled = false;
        value.ladders =
            List.of(
                "SPEARMACE",
                "MACE",
                "SPEARELYTRA",
                "SWORD",
                "CRYSTAL",
                "BOXING",
                "BUILDUHC",
                "AXE",
                "GAPPLE",
                "SG",
                "ARCHER",
                "SOUP",
                "CART",
                "NETHERITEPOTION",
                "DIAMONDPOTION");
        value.ladderSlots = List.of();

        return value;
      }

      static QueueCategory minigames() {
        QueueCategory value = new QueueCategory();
        value.displayName = "MINIGAMES";
        value.iconMaterial = "SLIME_BALL";
        value.selectedIconMaterial = "TNT";
        value.iconDisabled = false;
        value.ladders =
            List.of(
                "SUMO",
                "TNTSUMO",
                "SPLEEF",
                "FIREBALL",
                "PEARLFIGHT",
                "MLGRUSH",
                "BRIDGES",
                "BATTLERUSH",
                "CREEPER",
                "BEDWARS",
                "SKYWARS");
        value.ladderSlots = List.of();

        return value;
      }
    }
  }

  @Getter
  public static class EloSystem {
    @Comment(
        "Options: SKILL (rating-based formula, default), RANDOM (fixed random interval from"
            + " ELO-CHANGE).")
    @JsonProperty("MODE")
    private String mode;

    @Comment(
        "Only used in SKILL mode. The maximum base Elo points at stake in a single match (FIDE"
            + " standard is 32).")
    @JsonProperty("K-FACTOR")
    private Integer kfactor;

    @Comment({
      "Higher values make ratings move faster after each match; lower values keep ratings more"
          + " stable. Recommended range: 16-40.",
      "Hard cap on the absolute Elo points a player can gain OR lose in a single ranked match"
          + " (applies to both modes)."
    })
    @JsonProperty("MAX-CHANGE")
    private Integer maxChange;

    static EloSystem ranked() {
      EloSystem value = new EloSystem();
      value.mode = "SKILL";
      value.kfactor = 32;
      value.maxChange = 25;

      return value;
    }
  }
}
