package dev.lokspel.practice.config.settings;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.config.ItemConfig;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import me.whereareiam.configura.annotation.merge.Merge;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;
import me.whereareiam.configura.type.merge.MissingEntries;

@Getter
public class MatchSettings {
  @Comment(
      "If true, all online players remain visible in the tab list even when their entities are"
          + " hidden.")
  @JsonProperty("SHOW-PLAYERS-IN-TAB")
  private boolean showPlayersInTab = false;

  @Comment(
      "If true, players in the lobby keep every online player in their tab list, even the ones"
          + " whose entities are hidden. Only has an effect while SHOW-PLAYERS-IN-TAB is false.")
  @JsonProperty("SHOW-PLAYERS-IN-LOBBY-TAB")
  private boolean showPlayersInLobbyTab = false;

  @Comment("If true, the fighters of a match see its spectators in their tab list.")
  @JsonProperty("SHOW-SPECTATORS-IN-TAB")
  private boolean showSpectatorsInTab = false;

  @Comment(
      "Prefix in front of a spectator's name in the tab list of the fighters. Supports %player%,"
          + " \"\" for none.")
  @JsonProperty("SPECTATOR-TAB-PREFIX")
  private String spectatorTabPrefix = "<dark_gray>[<gray>Spec<dark_gray>] ";

  @JsonProperty("TEAMS")
  private Teams teams = new Teams();

  @JsonProperty("MATCH-TYPE-NAMES")
  private MatchTypeNames matchTypeNames = new MatchTypeNames();

  @JsonProperty("WEIGHT-CLASS")
  private WeightClass weightClass = new WeightClass();

  @JsonProperty("LEAVE-COMMAND")
  private LeaveCommand leaveCommand = new LeaveCommand();

  @JsonProperty("END-COMMAND")
  private EndCommand endCommand = new EndCommand();

  @Merge(missingEntries = MissingEntries.OMIT)
  @JsonProperty("START-COMMAND")
  private Map<String, StartCommand> startCommand = defaultStartCommand();

  @JsonProperty("MATCH-STATISTIC")
  private MatchStatistic matchStatistic = new MatchStatistic();

  @JsonProperty("TITLE")
  private Title title = new Title();

  @JsonProperty("MATCH-FOUND")
  private MatchFound matchFound = new MatchFound();

  @JsonProperty("HEALTH-BELOW-NAME")
  private HealthBelowName healthBelowName = new HealthBelowName();

  @Comment(
      "Counts from the first players positions Y level. You can set a unique number for this for"
          + " every arena, then it will consider where you are standing.")
  @JsonProperty("BUILD-LIMIT-DEFAULT")
  private int buildLimitDefault = 4;

  @Comment(
      "After the match is over this countdown start, and when it's over, the players teleport back"
          + " to the lobby.")
  @JsonProperty("AFTER-COUNTDOWN")
  private int afterCountdown = 3;

  @JsonProperty("ROLLBACK")
  private Rollback rollback = new Rollback();

  @Comment("Golden head settings.")
  @JsonProperty("GOLDEN-HEAD")
  private GoldenHead goldenHead = new GoldenHead();

  @Comment("Removes empty potion bottles after the player use it.")
  @JsonProperty("REMOVE-EMPTY-BOTTLE")
  private boolean removeEmptyBottle = true;

  @JsonProperty("SOUP")
  private Soup soup = new Soup();

  @JsonProperty("DUEL")
  private Duel duel = new Duel();

  @JsonProperty("COOLDOWN")
  private Cooldown cooldown = new Cooldown();

  @Comment(
      "These are multiplier values, so please test what is optimal for you and don't change them"
          + " too much at once or the difference will multiply.")
  @JsonProperty("FIREBALL-FIGHT")
  private FireballFight fireballFight = new FireballFight();

  @JsonProperty("TNT-SUMO")
  private TntSumo tntSumo = new TntSumo();

  @JsonProperty("BOXING")
  private Boxing boxing = new Boxing();

  @JsonProperty("PARTY")
  private Party party = new Party();

  @Comment(
      "Players get an item in their lobby inventory after duels, so they can easily request a"
          + " rematch.")
  @JsonProperty("REMATCH")
  private Rematch rematch = new Rematch();

  @Comment(
      "When enabled, players who turned on the Auto-Queue setting are automatically re-queued into"
          + " the same ladder and weight class after their duel ends.")
  @JsonProperty("AUTO-QUEUE")
  private AutoQueue autoQueue = new AutoQueue();

  @JsonProperty("KIT-ITEMS")
  private KitItems kitItems = new KitItems();

  @JsonProperty("LADDER-SETTINGS")
  private LadderSettings ladderSettings = new LadderSettings();

  @Comment("If true, the player will see the health of the player they hit.")
  @JsonProperty("DISPLAY-ARROW-HIT-HEALTH")
  private boolean displayArrowHitHealth = true;

  private static Map<String, StartCommand> defaultStartCommand() {
    Map<String, StartCommand> values = new LinkedHashMap<>();
    values.put("LADDER_NAME", StartCommand.ladderName());

    return values;
  }

  @Getter
  public static class Teams {
    @JsonProperty("TEAM1")
    private Team1 team1 = new Team1();

    @JsonProperty("TEAM2")
    private Team2 team2 = new Team2();

    @JsonProperty("FFA")
    private Ffa ffa = new Ffa();

    @Getter
    public static class Team1 {
      @JsonProperty("NAME")
      private String name = "<blue>[B]";

      @JsonProperty("COLOR")
      private String color = "<blue>";

      @JsonProperty("NAMETAG")
      private Nametag nametag = new Nametag();

      @Getter
      public static class Nametag {
        @JsonProperty("PREFIX")
        private String prefix = "";

        @JsonProperty("NAME-COLOR")
        private String nameColor = "BLUE";

        @JsonProperty("SUFFIX")
        private String suffix = "";
      }
    }

    @Getter
    public static class Team2 {
      @JsonProperty("NAME")
      private String name = "<red>[R]";

      @JsonProperty("COLOR")
      private String color = "<red>";

      @JsonProperty("NAMETAG")
      private Nametag nametag = new Nametag();

      @Getter
      public static class Nametag {
        @JsonProperty("PREFIX")
        private String prefix = "";

        @JsonProperty("NAME-COLOR")
        private String nameColor = "RED";

        @JsonProperty("SUFFIX")
        private String suffix = "";
      }
    }

    @Getter
    public static class Ffa {
      @JsonProperty("COLOR")
      private String color = "<yellow>";
    }
  }

  @Getter
  public static class MatchTypeNames {
    @JsonProperty("DUEL")
    private String duel = "<yellow><bold>Duel";

    @JsonProperty("PARTY_FFA")
    private String partyFfa = "<yellow><bold>Party FFA";

    @JsonProperty("PARTY_SPLIT")
    private String partySplit = "<yellow><bold>Party Split";

    @JsonProperty("PARTY_VS_PARTY")
    private String partyVsParty = "<yellow><bold>Party Vs Party";
  }

  @Getter
  public static class WeightClass {
    @JsonProperty("UNRANKED")
    private String unranked = "<green>Unranked";

    @JsonProperty("RANKED")
    private String ranked = "<red>Ranked";
  }

  @Getter
  public static class LeaveCommand {
    @JsonProperty("ENABLED")
    private boolean enabled = true;

    @JsonProperty("WEIGHT-CLASS")
    private WeightClass weightClass = new WeightClass();

    @Getter
    public static class WeightClass {
      @JsonProperty("UNRANKED")
      private boolean unranked = true;

      @JsonProperty("RANKED")
      private boolean ranked = false;
    }
  }

  @Getter
  public static class EndCommand {
    @JsonProperty("DUEL")
    private Duel duel = new Duel();

    @Getter
    public static class Duel {
      @Comment("Both commands have %player% and %opponent% placeholders.")
      @JsonProperty("UNRANKED")
      private Unranked unranked = new Unranked();

      @Comment("Both commands have %player% and %opponent% placeholders.")
      @JsonProperty("RANKED")
      private Ranked ranked = new Ranked();

      @Getter
      public static class Unranked {
        @JsonProperty("WINNER-COMMANDS")
        private List<String> winnerCommands = List.of("");

        @JsonProperty("LOSER-COMMANDS")
        private List<String> loserCommands = List.of("");
      }

      @Getter
      public static class Ranked {
        @JsonProperty("WINNER-COMMANDS")
        private List<String> winnerCommands = List.of("");

        @JsonProperty("LOSER-COMMANDS")
        private List<String> loserCommands = List.of("");
      }
    }
  }

  @Getter
  public static class StartCommand {
    @JsonProperty("MATCH")
    private List<String> match;

    @JsonProperty("ROUND")
    private List<String> round;

    static StartCommand ladderName() {
      StartCommand value = new StartCommand();
      value.match = List.of("");
      value.round = List.of("");

      return value;
    }
  }

  @Getter
  public static class MatchStatistic {
    @Comment("After the seconds, the players won't be able to open their match statistics.")
    @JsonProperty("REMOVE-AFTER")
    private int removeAfter = 60;
  }

  @Getter
  public static class Title {
    @JsonProperty("START-COUNTDOWN-FROM")
    private int startCountdownFrom = 5;
  }

  @Getter
  public static class MatchFound {
    @Comment(
        "If true, when a match is found while queueing, the ladder's icon is briefly shown to both"
            + " players with the totem-of-undying animation.")
    @JsonProperty("ICON-ANIMATION")
    private boolean iconAnimation = false;

    @Comment("Ticks before players are teleported after the animation starts.")
    @JsonProperty("ICON-ANIMATION-DELAY")
    private int iconAnimationDelay = 40;
  }

  @Getter
  public static class HealthBelowName {
    @Comment("If true, the below-name heart icon turns yellow when the player is saturated.")
    @JsonProperty("SATURATED-HEART-INDICATOR")
    private boolean saturatedHeartIndicator = true;

    @JsonProperty("DECIMAL-HEART-INDICATOR")
    private DecimalHeartIndicator decimalHeartIndicator = new DecimalHeartIndicator();

    @JsonProperty("LOW-HEALTH-THRESHOLD")
    private double lowHealthThreshold = 4.0;

    @Comment(
        "The scale setting defines the health display range between 10 and 100 with a default value"
            + " of 20.")
    @JsonProperty("SCALE")
    private double scale = 20.0;

    @Comment("The symbol displayed after the health value in the below-name display.")
    @JsonProperty("SYMBOL")
    private String symbol = "♥";

    @Comment("If true, a drop shadow is drawn behind the above-head nametag.")
    @JsonProperty("TEXT-SHADOW")
    private boolean textShadow = false;

    @Comment("Background of the above-head nametag as an ARGB value, 0 for none.")
    @JsonProperty("BACKGROUND")
    private int background = 0;

    @Comment("Line drawn above the nametag, supports PlaceholderAPI placeholders.")
    @JsonProperty("TOP-LINE")
    private String topLine = "";

    @Comment("Line drawn below the nametag, supports PlaceholderAPI placeholders.")
    @JsonProperty("BOTTOM-LINE")
    private String bottomLine = "";

    @Getter
    public static class DecimalHeartIndicator {
      @Comment("If true, the below-name heart icon always shows the decimal hearts.")
      @JsonProperty("ALWAYS-SHOW")
      private boolean alwaysShow = true;

      @Comment("If true, health below 2 hearts is shown as a decimal ratio (current/max).")
      @JsonProperty("LOW-HEALTH-DECIMAL-RATIO")
      private boolean lowHealthDecimalRatio = true;
    }
  }

  @Getter
  public static class Rollback {
    @Comment(
        "Multiplies rollback speed/work per tick for match/event/ffa rollback. Increase carefully.")
    @JsonProperty("MULTIPLIER")
    private int multiplier = 1;
  }

  @Getter
  public static class GoldenHead {
    @Comment(
        "Optional: direct skin URL or Base64 texture value. If set, item renders as a custom player"
            + " head.")
    @JsonProperty("TEXTURE")
    private String texture =
        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0"
            + "L3RleHR1cmUvOGUzNGM1MGZkY2E4ZTM2YzFlMDUwYjk2M2YyZTZlMWY3MGU0NjVlNTVjNDQzZjY4"
            + "OTQ3NDQzNDA5Y2M2YTMzOCJ9fX0=";

    @Comment(
        "If true, eating a golden head plays the eating animation like food instead of consuming"
            + " instantly.")
    @JsonProperty("ANIMATION")
    private boolean animation = false;

    @Comment("Cooldown in seconds between Golden Head uses.")
    @JsonProperty("COOLDOWN")
    private int cooldown = 10;

    @JsonProperty("ACTION-BAR-COOLDOWN-MSG")
    private String actionBarCooldownMsg = "<gold>🕒 <yellow>%remaining%s";

    @JsonProperty("ITEM")
    private Item item = new Item();

    @Comment({
      "You can add any potion effects for the golden head.",
      "But it has to follow a strict format like below:",
      "POTION_TYPE::DURATION_IN_SECONDS::STRENGTH",
      "You can find the potion types in here:"
          + " https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html"
    })
    @JsonProperty("EFFECTS")
    private List<String> effects = List.of("ABSORPTION::90::1", "REGENERATION::20::2");

    @Getter
    public static class Item extends ItemConfig {
      @JsonProperty("DAMAGE")
      private int damage = 1;

      {
        material("GOLDEN_APPLE");
        name("<yellow><bold>Golden Head");
        lore("", "<gray>This apple gives you extra", "<gray>health and effects.", "");
      }
    }
  }

  @Getter
  public static class Soup {
    @Comment(
        "Health points a mushroom stew restores when used in a match, FFA or event. 1.0 is half a"
            + " heart, so 6.5 equals 3.25 hearts.")
    @JsonProperty("HEAL")
    private double heal = 6.5;
  }

  @Getter
  public static class Duel {
    @Comment(
        "When enabled, hitting a player with the unranked sword in the lobby sends a duel request.")
    @JsonProperty("SWORD-TO-DUEL")
    private boolean swordToDuel = true;

    @Comment("After the seconds, the duel request will expire.")
    @JsonProperty("INVITATION-EXPIRY")
    private int invitationExpiry = 60;

    @JsonProperty("ROUND-SELECTOR")
    private RoundSelector roundSelector = new RoundSelector();

    @Getter
    public static class RoundSelector {
      @JsonProperty("MAX")
      private int max = 10;

      @Comment("Can't be lower than 1.")
      @JsonProperty("MIN")
      private int min = 1;
    }
  }

  @Getter
  public static class Cooldown {
    @JsonProperty("ENDER-PEARL")
    private EnderPearl enderPearl = new EnderPearl();

    @JsonProperty("WIND-CHARGE")
    private WindCharge windCharge = new WindCharge();

    @JsonProperty("FIREWORK-ROCKET")
    private FireworkRocket fireworkRocket = new FireworkRocket();

    @JsonProperty("GOLDEN-APPLE")
    private GoldenApple goldenApple = new GoldenApple();

    @Getter
    public static class EnderPearl {
      @Comment("This is the default cooldown. You can change it in the ladder settings.")
      @JsonProperty("SECONDS")
      private double seconds = 13.0;

      @Comment("Countdown shows on exp bar.")
      @JsonProperty("EXP-BAR")
      private boolean expBar = true;
    }

    @Getter
    public static class WindCharge {
      @Comment("Default wind charge cooldown in seconds. You can change it in the ladder settings.")
      @JsonProperty("SECONDS")
      private double seconds = 0.5;
    }

    @Getter
    public static class FireworkRocket {
      @Comment("Cooldown in seconds for using firework rockets with elytra. Default is 1 second.")
      @JsonProperty("SECONDS")
      private double seconds = 1.0;
    }

    @Getter
    public static class GoldenApple {
      @Comment("Cooldown in seconds for using golden apples. Default is 0 seconds (no cooldown).")
      @JsonProperty("SECONDS")
      private double seconds = 0.0;
    }
  }

  @Getter
  public static class FireballFight {
    @JsonProperty("FIREBALL-SPEED")
    private double fireballSpeed = 1.0;

    @JsonProperty("FIREBALL-YIELD")
    private double fireballYield = 2.8;

    @JsonProperty("EXPLOSION")
    private Explosion explosion = new Explosion();

    @Getter
    public static class Explosion {
      @JsonProperty("TNT")
      private Tnt tnt = new Tnt();

      @JsonProperty("FIREBALL")
      private Fireball fireball = new Fireball();

      @Getter
      public static class Tnt {
        @JsonProperty("HORIZONTAL")
        private double horizontal = 1.4;

        @JsonProperty("VERTICAL")
        private double vertical = 1.3;
      }

      @Getter
      public static class Fireball {
        @JsonProperty("HORIZONTAL")
        private double horizontal = 1.6;

        @JsonProperty("VERTICAL")
        private double vertical = 1.4;
      }
    }
  }

  @Getter
  public static class TntSumo {
    @JsonProperty("EXPLOSION")
    private Explosion explosion = new Explosion();

    @Getter
    public static class Explosion {
      @Comment("--- Radius Settings ---")
      @JsonProperty("HORIZONTAL-RADIUS")
      private double horizontalRadius = 4.0;

      @JsonProperty("VERTICAL-RADIUS")
      private double verticalRadius = 2.5;

      @Comment("--- Multipliers (Base) ---")
      @JsonProperty("HORIZONTAL-MULTIPLIER")
      private double horizontalMultiplier = 1.6;

      @JsonProperty("VERTICAL-MULTIPLIER")
      private double verticalMultiplier = 0.75;

      @JsonProperty("SWEET-SPOT-Y-BOOST")
      private double sweetSpotYboost = 0.85;

      @Comment({
        "--- Grounded Physics ---",
        "Controls how players behave when they are touching the block/ground"
      })
      @JsonProperty("GROUNDED-HORIZONTAL-MULTIPLIER")
      private double groundedHorizontalMultiplier = 0.08;

      @JsonProperty("GROUNDED-VERTICAL-MULTIPLIER")
      private double groundedVerticalMultiplier = 0.08;

      @JsonProperty("GROUNDED-CHAIN-LIFT")
      private double groundedChainLift = 0.12;

      @Comment({
        "--- Distance & Falloff ---",
        "Defines how knockback scales as you move away from the TNT center"
      })
      @JsonProperty("NEAR-FULL-FORCE-DISTANCE")
      private double nearFullForceDistance = 1.5;

      @JsonProperty("SHARP-FALLOFF-START-DISTANCE")
      private double sharpFalloffStartDistance = 3.5;

      @JsonProperty("MID-FORCE-AT-FALLOFF-START")
      private double midForceAtFalloffStart = 0.35;

      @Comment({
        "--- Airborne & Juggling Logic ---",
        "Logic for stacking hits while a player is already in the air"
      })
      @JsonProperty("AIRBORNE-JUGGLE-MULTIPLIER")
      private double airborneJuggleMultiplier = 1.08;

      @JsonProperty("AIRBORNE-STACK-WINDOW-MILLIS")
      private int airborneStackWindowMillis = 55;

      @JsonProperty("AIRBORNE-OVERFLOW-PER-EXTRA-HIT")
      private double airborneOverflowPerExtraHit = 0.08;

      @JsonProperty("AIRBORNE-OVERFLOW-MAX")
      private double airborneOverflowMax = 0.24;
    }
  }

  @Getter
  public static class Boxing {
    @JsonProperty("CUSTOM-ATTACK-COOLDOWN")
    private CustomAttackCooldown customAttackCooldown = new CustomAttackCooldown();

    @Getter
    public static class CustomAttackCooldown {
      @JsonProperty("ENABLED")
      private boolean enabled = true;

      @Comment("0.0-1.0 value")
      @JsonProperty("COUNT-FROM")
      private double countFrom = 0.8;

      @JsonProperty("ONLY-SWORD")
      private boolean onlySword = true;
    }
  }

  @Getter
  public static class Party {
    @Comment("In party split games, players with the same team can't hurt each other (if false).")
    @JsonProperty("SPLIT-TEAM-DAMAGE")
    private boolean splitTeamDamage = false;

    @Comment(
        "In party vs party games, players with the same party can't hurt each other (if false).")
    @JsonProperty("PARTY-VS-PARTY-TEAM-DAMAGE")
    private boolean partyVsPartyTeamDamage = false;
  }

  @Getter
  public static class Rematch {
    @JsonProperty("ENABLED")
    private boolean enabled = true;

    @Comment("sec")
    @JsonProperty("EXPIRE-TIME")
    private int expireTime = 20;
  }

  @Getter
  public static class AutoQueue {
    @JsonProperty("ENABLED")
    private boolean enabled = true;
  }

  @Getter
  public static class KitItems {
    @JsonProperty("DEFAULT-KIT")
    private ItemConfig defaultKit =
        ItemConfig.of("ENCHANTED_BOOK").name("<yellow><bold>Default Kit");

    @JsonProperty("CUSTOM-KIT")
    private ItemConfig customKit = ItemConfig.of("ENCHANTED_BOOK").name("<green><bold>Kit %kit%");
  }

  @Getter
  public static class LadderSettings {
    @JsonProperty("BRIDGE")
    private Bridge bridge = new Bridge();

    @Getter
    public static class Bridge {
      @JsonProperty("REGENERATING-ARROW")
      private RegeneratingArrow regeneratingArrow = new RegeneratingArrow();

      @Getter
      public static class RegeneratingArrow {
        @JsonProperty("ENABLED")
        private boolean enabled = true;

        @JsonProperty("EXP-BAR")
        private boolean expBar = true;

        @Comment("In sec.")
        @JsonProperty("TIME")
        private int time = 5;
      }
    }
  }
}
