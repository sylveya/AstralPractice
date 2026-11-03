package dev.lokspel.practice.config.gui;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.config.ItemConfig;
import lombok.Getter;

@Getter
public class SetupLadderSettingsIcons {
  @JsonProperty("GO-BACK")
  private ItemConfig goBack = ItemConfig.of("ARROW").name("<red>Go Back");

  @JsonProperty("REGENERATION")
  private Regeneration regeneration = new Regeneration();

  @JsonProperty("HUNGER")
  private Hunger hunger = new Hunger();

  @JsonProperty("ROUNDS")
  private ItemConfig rounds =
      ItemConfig.of("FIREWORK_STAR")
          .name("<gray>Rounds: <gold>%rounds%")
          .lore(
              "",
              "<gray>The first player/team to reach",
              "<gray>the, winning rounds wins.",
              "",
              "<green><bold>LEFT-CLICK <green>to reduce the rounds.",
              "<aqua><bold>RIGHT-CLICK <aqua>to increase the rounds.");

  @JsonProperty("HEARTS")
  private ItemConfig hearts =
      ItemConfig.of("RED_DYE")
          .name("<gray>Hearts: <red>%hearts%")
          .lore(
              "",
              "<gray>Sets the maximum hearts players",
              "<gray>can have in this ladder.",
              "",
              "<green><bold>LEFT-CLICK <green>to reduce hearts.",
              "<aqua><bold>RIGHT-CLICK <aqua>to increase hearts.");

  @JsonProperty("KNOCKBACK")
  private ItemConfig knockback =
      ItemConfig.of("STICK")
          .name("<yellow>Knockback Modifier")
          .lore(
              "",
              "%knockbackTypes%",
              "",
              "<red><bold>Note: <gray>Default won't change the knockback.",
              "<gray>You can change the normal and combo knockbacks",
              "<gray>in the config file.");

  @JsonProperty("ATTACKCOOLDOWN")
  private ItemConfig attackcooldown =
      ItemConfig.of("DIAMOND_SWORD")
          .name("<gray>Attack Cooldown: <gold>%attackcooldown%x")
          .lore(
              "",
              "<gray>Adjust the speed at which the",
              "<gray>attack strength bar recharges.",
              "",
              "<yellow>Current Multiplier: <white>%attackcooldown%x",
              "<gray>(Lower = Faster swinging)",
              "",
              "<green><bold>LEFT-CLICK <gray>to decrease by <red>0.1",
              "<aqua><bold>RIGHT-CLICK <gray>to increase by <aqua>0.1");

  @JsonProperty("MAX-DURATION")
  private ItemConfig maxDuration =
      ItemConfig.of("CLOCK")
          .name("<gray>Max Duration: <gold>%maxDuration% sec")
          .lore(
              "",
              "<gray>The maximum time the match can last.",
              "",
              "<green><bold>LEFT-CLICK <green>to reduce the time.",
              "<aqua><bold>RIGHT-CLICK <aqua>to increase the time.");

  @JsonProperty("TNT-FUSE-TIME")
  private ItemConfig tntFuseTime =
      ItemConfig.of("TNT")
          .name("<gray>TNT Fuse Time: <gold>%tntFuseTime% sec")
          .lore(
              "",
              "<gray>The time until the tnt explodes.",
              "",
              "<green><bold>LEFT-CLICK <green>to reduce the time.",
              "<aqua><bold>RIGHT-CLICK <aqua>to increase the time.");

  @JsonProperty("TNT-AUTO-IGNITE")
  private TntAutoIgnite tntAutoIgnite = new TntAutoIgnite();

  @JsonProperty("EDITABLE")
  private Editable editable = new Editable();

  @JsonProperty("WEIGHT-CLASS")
  private ItemConfig weightClass =
      ItemConfig.of("SUNFLOWER")
          .name("<yellow>Weight Class Selector")
          .lore("", "%weightClassTypes%");

  @JsonProperty("START-COUNTDOWN")
  private ItemConfig startCountdown =
      ItemConfig.of("CLOCK")
          .name("<gray>Start Countdown: <gold>%startCountdown%")
          .lore(
              "",
              "<gray>When the match/round starts the server",
              "<gray>counts down this amount of time.",
              "",
              "<green><bold>LEFT-CLICK <green>to reduce the time.",
              "<aqua><bold>RIGHT-CLICK <aqua>to increase the time.");

  @JsonProperty("START-MOVING")
  private StartMoving startMoving = new StartMoving();

  @JsonProperty("HEALTH-BELOW-NAME")
  private HealthBelowName healthBelowName = new HealthBelowName();

  @JsonProperty("RESET-BUILD-AFTER-ROUND")
  private ResetBuildAfterRound resetBuildAfterRound = new ResetBuildAfterRound();

  @JsonProperty("BREAK-ALL-BLOCKS")
  private BreakAllBlocks breakAllBlocks = new BreakAllBlocks();

  @JsonProperty("FIREBALL-BLOCK-DESTROY")
  private FireballBlockDestroy fireballBlockDestroy = new FireballBlockDestroy();

  @JsonProperty("SPLEEF-SNOWBALL-MODE")
  private SpleefSnowballMode spleefSnowballMode = new SpleefSnowballMode();

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
          .name("<gray>Golden Apple Cooldown: <gold>%golden_apple_cooldown%")
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

  @JsonProperty("DROP-INVENTORY-PARTY-GAMES")
  private DropInventoryPartyGames dropInventoryPartyGames = new DropInventoryPartyGames();

  @JsonProperty("MULTI-ROUND-START-COUNTDOWN")
  private MultiRoundStartCountdown multiRoundStartCountdown = new MultiRoundStartCountdown();

  @JsonProperty("SKYWARS")
  private ItemConfig skywars =
      ItemConfig.of("CHEST")
          .name("<gold>SkyWars Chest Loot")
          .lore(
              "",
              "<yellow><bold>Click here <gray>to edit the",
              "<gray>loot which the players get when",
              "<gray>they open chests during the game.");

  @JsonProperty("BLOCK-RETURN-DELAY")
  private ItemConfig blockReturnDelay =
      ItemConfig.of("BLUE_TERRACOTTA")
          .name("<gray>%blockReturnDelayTarget% Return Delay: <gold>%blockReturnDelay%")
          .lore(
              "",
              "<gray>This controls the %blockReturnDelayTarget%",
              "<gray>return delay after placement.",
              "<gray>Set to <yellow>-1 <gray>to disable auto return.",
              "",
              "<green><bold>LEFT-CLICK <green>to reduce the delay.",
              "<aqua><bold>RIGHT-CLICK <aqua>to increase the delay.");

  @JsonProperty("TEMP-BUILD-RETURN-DELAY")
  private ItemConfig tempBuildReturnDelay =
      ItemConfig.of("RED_TERRACOTTA")
          .name("<gray>Temporary Block Return Delay: <gold>%tempBuildReturnDelay%")
          .lore(
              "",
              "<gray>This controls how long placed",
              "<gray>temporary blocks stay before",
              "<gray>they automatically disappear.",
              "<gray>Set to <yellow>-1 <gray>to disable auto return.",
              "",
              "<green><bold>LEFT-CLICK <green>to reduce the delay.",
              "<aqua><bold>RIGHT-CLICK <aqua>to increase the delay.");

  @JsonProperty("ROUND-END-DELAY")
  private ItemConfig roundEndDelay =
      ItemConfig.of("CLOCK")
          .name("<gray>Round End Delay: <gold>%roundEndDelay% sec")
          .lore(
              "",
              "<gray>Time to wait after a round ends before",
              "<gray>starting the next transition. Allows",
              "<gray>death effects to complete.",
              "",
              "<green><bold>LEFT-CLICK <green>to reduce the delay.",
              "<aqua><bold>RIGHT-CLICK <aqua>to increase the delay.");

  @JsonProperty("ROUND-STATUS-TITLES")
  private RoundStatusTitles roundStatusTitles = new RoundStatusTitles();

  @JsonProperty("COUNTDOWN-TITLES")
  private CountdownTitles countdownTitles = new CountdownTitles();

  @JsonProperty("BOXING")
  private ItemConfig boxing =
      ItemConfig.of("LEVER")
          .name("<gray>Boxing Strokes to Win: <gold>%boxingWinHits%")
          .lore(
              "",
              "<gray>A player or team wins if they have",
              "<gray>gained that many strokes.",
              "<gray>In a team match, player's",
              "<gray>strokes are added together.",
              "",
              "<green><bold>LEFT-CLICK <green>to reduce the strokes.",
              "<aqua><bold>RIGHT-CLICK <aqua>to increase the strokes.");

  @JsonProperty("RESPAWN")
  private ItemConfig respawn =
      ItemConfig.of("RED_BED")
          .name("<gray>Respawn Time: <gold>%respawnTime%")
          .lore(
              "",
              "<gray>That's how long it takes",
              "<gray>for a player to come back to life",
              "<gray>when he dies without his bed being broken",
              "<gray>or when the ladder type is battle rush / bridges.",
              "",
              "<green><bold>LEFT-CLICK <green>to reduce the time.",
              "<aqua><bold>RIGHT-CLICK <aqua>to increase the time.");

  @JsonProperty("FIREBALL-COOLDOWN")
  private ItemConfig fireballCooldown =
      ItemConfig.of("FIRE_CHARGE")
          .name("<gray>Fireball Cooldown: <gold>%cooldown% sec")
          .lore(
              "",
              "<gray>Players must wait this long before",
              "<gray>they throw another fireball.",
              "",
              "<green><bold>LEFT-CLICK <green>to reduce the cooldown.",
              "<aqua><bold>RIGHT-CLICK <aqua>to increase the cooldown.");

  @JsonProperty("CREEPER-EXPLOSION-DELAY")
  private ItemConfig creeperExplosionDelay =
      ItemConfig.of("CREEPER_HEAD")
          .name("<gray>Creeper Explosion Delay: <gold>%creeperExplosionDelay% sec")
          .lore(
              "",
              "<gray>How long a creeper stays primed before it explodes.",
              "",
              "<green><bold>LEFT-CLICK <green>to reduce the delay.",
              "<aqua><bold>RIGHT-CLICK <aqua>to increase the delay.");

  @JsonProperty("PULL-PLAYERS-WITH-ROD")
  private PullPlayersWithRod pullPlayersWithRod = new PullPlayersWithRod();

  @JsonProperty("ITEM-PICKUP")
  private ItemPickup itemPickup = new ItemPickup();

  @Getter
  public static class Regeneration {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("GLOWSTONE_DUST")
            .name("<gray>Regeneration: <green>Enabled")
            .lore(
                "",
                "<gray>Players health is automatically regenerate (normally).",
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
                "<yellow><bold>Click here <gray>to <red>disable <gray>.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("COOKED_BEEF")
            .name("<gray>Hunger: <red>Disabled")
            .lore(
                "",
                "<gray>Players don't starve.",
                "",
                "<yellow><bold>Click here <gray>to <green>enable <gray>.");
  }

  @Getter
  public static class TntAutoIgnite {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("TNT")
            .name("<gray>TNT Auto Ignite: <green>Enabled")
            .lore(
                "",
                "<gray>TNT explodes on its own after being placed.",
                "<gray>The fuse time above is still used.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("TNT")
            .name("<gray>TNT Auto Ignite: <red>Disabled")
            .lore(
                "",
                "<gray>TNT has to be ignited with flint and steel.",
                "",
                "<green><bold>LEFT-CLICK <green>to toggle.");
  }

  @Getter
  public static class Editable {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("BOOK")
            .name("<gray>Editable: <green>Enabled")
            .lore(
                "",
                "<gray>Players can customize the ladder.",
                "",
                "<yellow><bold>Click here <gray>to <red>disable<gray>.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("BOOK")
            .name("<gray>Editable: <red>Disabled")
            .lore(
                "",
                "<gray>Players can't customize the ladder.",
                "",
                "<yellow><bold>Click here <gray>to <green>enable<gray>.");
  }

  @Getter
  public static class StartMoving {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("DIRT")
            .name("<gray>Start moving: <green>Enabled")
            .lore(
                "",
                "<gray>Players can move during",
                "<gray>round start countdown.",
                "",
                "<yellow><bold>Click here <gray>to <red>disable<gray>.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("DIRT")
            .name("<gray>Start moving: <red>Disabled")
            .lore(
                "",
                "<gray>Players can't move during",
                "<gray>round start countdown.",
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

  @Getter
  public static class ResetBuildAfterRound {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("GRASS_BLOCK")
            .name("<gray>Reset Build After Round: <green>Enabled")
            .lore(
                "",
                "<gray>The arena will be reset after each",
                "<gray>round, before the next one starts.",
                "",
                "<yellow><bold>Click here <gray>to <red>disable<gray>.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("GRASS_BLOCK")
            .name("<gray>Reset Build After Round: <red>Disabled")
            .lore(
                "",
                "<gray>The arena will not be reset between",
                "<gray>rounds. Placed blocks persist.",
                "",
                "<yellow><bold>Click here <gray>to <green>enable<gray>.");
  }

  @Getter
  public static class BreakAllBlocks {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("DIAMOND_PICKAXE")
            .name("<gray>Break All Blocks: <green>Enabled")
            .lore(
                "",
                "<gray>Players can break any block in the arena,",
                "<gray>not just blocks they placed themselves.",
                "",
                "<yellow><bold>Click here <gray>to <red>disable<gray>.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("WOODEN_PICKAXE")
            .name("<gray>Break All Blocks: <red>Disabled")
            .lore(
                "",
                "<gray>Players can only break blocks they",
                "<gray>placed during the match.",
                "",
                "<yellow><bold>Click here <gray>to <green>enable<gray>.");
  }

  @Getter
  public static class FireballBlockDestroy {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("FIRE_CHARGE")
            .name("<gray>Fireball Block Destroy: <green>Enabled")
            .lore(
                "",
                "<gray>Fireball explosions will destroy",
                "<gray>blocks placed by players.",
                "<dark_gray>(Arena blocks are not affected)",
                "",
                "<yellow><bold>Click here <gray>to <red>disable<gray>.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("FIRE_CHARGE")
            .name("<gray>Fireball Block Destroy: <red>Disabled")
            .lore(
                "",
                "<gray>Fireball explosions will not",
                "<gray>destroy any blocks.",
                "",
                "<yellow><bold>Click here <gray>to <green>enable<gray>.");
  }

  @Getter
  public static class SpleefSnowballMode {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("SNOWBALL")
            .name("<gray>Snowball Mode: <green>Enabled")
            .lore(
                "",
                "<gray>Breaking a snow block gives a snowball.",
                "<gray>Throw snowballs to destroy snow blocks",
                "<gray>beneath other players.",
                "",
                "<yellow><bold>Click here <gray>to <red>disable<gray>.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("SNOWBALL")
            .name("<gray>Snowball Mode: <red>Disabled")
            .lore(
                "",
                "<gray>Enable to let players collect and throw",
                "<gray>snowballs that destroy snow blocks.",
                "",
                "<yellow><bold>Click here <gray>to <green>enable<gray>.");
  }

  @Getter
  public static class DropInventoryPartyGames {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("GHAST_TEAR")
            .name("<gray>Drop Inventory: <green>Enabled")
            .lore(
                "",
                "<gray>When several players are playing a",
                "<gray>party match and someone dies, should",
                "<gray>the inventory be dropped out or not.",
                "",
                "<yellow><bold>Click here <gray>to <red>disable<gray>.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("GHAST_TEAR")
            .name("<gray>Drop Inventory: <red>Disabled")
            .lore(
                "",
                "<gray>When several players are playing a",
                "<gray>party match and someone dies, should",
                "<gray>the inventory be dropped out or not.",
                "",
                "<yellow><bold>Click here <gray>to <green>enable<gray>.");
  }

  @Getter
  public static class MultiRoundStartCountdown {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("NETHER_WART")
            .name("<gray>Multi Round Start Countdown: <green>Enabled")
            .lore(
                "",
                "<gray>If a ladder has several rounds,",
                "<gray>you have the option to turn off",
                "<gray>the start counter before the",
                "<gray>round with this setting.",
                "",
                "<red><bold>Note: <gray>At the start of the match,",
                "<gray>the counter will work the same way.",
                "",
                "<yellow><bold>Click here <gray>to <red>disable<gray>.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("NETHER_WART")
            .name("<gray>Multi Round Start Countdown: <red>Disabled")
            .lore(
                "",
                "<gray>If a ladder has several rounds,",
                "<gray>you have the option to turn off",
                "<gray>the start counter before the",
                "<gray>round with this setting.",
                "",
                "<red><bold>Note: <gray>At the start of the match,",
                "<gray>the counter will work the same way.",
                "",
                "<yellow><bold>Click here <gray>to <green>enable<gray>.");
  }

  @Getter
  public static class RoundStatusTitles {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("WRITTEN_BOOK")
            .name("<gray>Round Status Titles: <green>Enabled")
            .lore(
                "",
                "<gray>Shows green <green>VICTORY! <gray>and red",
                "<red>DEFEAT <gray>titles when a round ends.",
                "",
                "<yellow><bold>Click here <gray>to <red>disable<gray>.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("WRITTEN_BOOK")
            .name("<gray>Round Status Titles: <red>Disabled")
            .lore(
                "",
                "<gray>Shows green <green>VICTORY! <gray>and red",
                "<red>DEFEAT <gray>titles when a round ends.",
                "",
                "<yellow><bold>Click here <gray>to <green>enable<gray>.");
  }

  @Getter
  public static class CountdownTitles {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("CLOCK")
            .name("<gray>Countdown Titles: <green>Enabled")
            .lore(
                "",
                "<gray>Displays <gold>3, 2, 1, FIGHT! <gray>titles",
                "<gray>during the round start countdown.",
                "",
                "<yellow><bold>Click here <gray>to <red>disable<gray>.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("CLOCK")
            .name("<gray>Countdown Titles: <red>Disabled")
            .lore(
                "",
                "<gray>Displays <gold>3, 2, 1, FIGHT! <gray>titles",
                "<gray>during the round start countdown.",
                "",
                "<yellow><bold>Click here <gray>to <green>enable<gray>.");
  }

  @Getter
  public static class PullPlayersWithRod {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("FISHING_ROD")
            .name("<gray>Pull Players With Rod: <green>Enabled")
            .lore("", "<gray>Players can be pulled with a fishing rod.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("FISHING_ROD")
            .name("<gray>Pull Players With Rod: <red>Disabled")
            .lore(
                "",
                "<gray>Players cannot be pulled with a fishing rod.",
                "",
                "<green><bold>LEFT-CLICK <green>to toggle.");
  }

  @Getter
  public static class ItemPickup {
    @JsonProperty("ENABLED")
    private ItemConfig enabled =
        ItemConfig.of("HOPPER")
            .name("<gray>Item Pickup: <green>Enabled")
            .lore(
                "",
                "<gray>Players can pick up any items during the match.",
                "",
                "<yellow><bold>Click here <gray>to <red>disable<gray>.");

    @JsonProperty("DISABLED")
    private ItemConfig disabled =
        ItemConfig.of("HOPPER")
            .name("<gray>Item Pickup: <red>Disabled")
            .lore(
                "",
                "<gray>Players can only pick up player-dropped items.",
                "",
                "<yellow><bold>Click here <gray>to <green>enable<gray>.");
  }
}
