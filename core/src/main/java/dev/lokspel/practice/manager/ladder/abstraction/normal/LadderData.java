package dev.lokspel.practice.manager.ladder.abstraction.normal;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * The file {@code ladders/<name>.yml}.
 *
 * <p>Numeric values are kept as {@link Number} so the reader can tell an integer from a decimal the
 * way the earlier {@code YamlConfiguration} checks did ({@code settings.hitdelay} means ticks when
 * it is an integer and a multiplier when it is a decimal). Keys of other ladder types, keys of
 * earlier versions and keys this model does not know are kept as they were read, so writing the
 * model back never drops anything from the file.
 */
@Getter
@Setter
@NoArgsConstructor
public class LadderData {

  @JsonProperty("name")
  private String name;

  @JsonProperty("enabled")
  private Boolean enabled;

  @JsonProperty("type")
  private String type;

  @JsonProperty("settings")
  private Settings settings;

  @JsonProperty("icon")
  private ItemStack icon;

  /** Kit storage contents, Base64. */
  @JsonProperty("inventory")
  private String inventory;

  /** Kit armor contents, Base64. */
  @JsonProperty("armor")
  private String armor;

  /** Kit extra contents (off hand), Base64. */
  @JsonProperty("extra")
  private String extra;

  @JsonProperty("effects")
  private List<PotionEffect> effects;

  /** Blocks players may break in build ladders, {@code MATERIAL} or {@code MATERIAL::damage}. */
  @JsonProperty("destroyable-blocks")
  private List<String> destroyableBlocks;

  @JsonProperty("custom-kit-extra-items")
  private CustomKitExtraItems customKitExtraItems;

  // Boxing
  @JsonProperty("boxing-winhit")
  private Number boxingWinHit;

  // Fireball Fight
  @JsonProperty("fireball-cooldown")
  private Number fireballCooldown;

  @JsonProperty("fireball-block-destroy")
  private Boolean fireballBlockDestroy;

  // SkyWars, chest loot as Base64
  @JsonProperty("skywars-loot")
  private String skyWarsLoot;

  // Earlier names of settings.block-return-delay and settings.temp-build-return-delay, read only
  @JsonProperty("tempbuild-delay")
  private Number legacyTempBuildDelay;

  @JsonProperty("block-return-delay-seconds")
  private Number legacyBlockReturnDelaySeconds;

  @JsonIgnore
  @Getter(AccessLevel.NONE)
  @Setter(AccessLevel.NONE)
  private final Map<String, Object> unknownKeys = new LinkedHashMap<>();

  @JsonAnyGetter
  public Map<String, Object> unknownKeys() {
    return unknownKeys;
  }

  @JsonAnySetter
  public void unknownKey(String key, Object value) {
    unknownKeys.put(key, value);
  }

  /** The {@code settings} section. */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Settings {

    @JsonProperty("regen")
    private Boolean regen;

    @JsonProperty("hunger")
    private Boolean hunger;

    /** Replaced by {@code weightClass}, read only. */
    @JsonProperty("ranked")
    private Boolean legacyRanked;

    @JsonProperty("weightClass")
    private String weightClass;

    @JsonProperty("editable")
    private Boolean editable;

    @JsonProperty("drop-inventory")
    private Boolean dropInventory;

    @JsonProperty("multiRoundStartCountdown")
    private Boolean multiRoundStartCountdown;

    @JsonProperty("hitdelay")
    private Number hitDelay;

    @JsonProperty("rounds")
    private Number rounds;

    @JsonProperty("hearts")
    private Number hearts;

    @JsonProperty("maxduration")
    private Number maxDuration;

    @JsonProperty("epcooldown")
    private Number enderPearlCooldown;

    @JsonProperty("gacooldown")
    private Number goldenAppleCooldown;

    @JsonProperty("fireworkcooldown")
    private Number fireworkCooldown;

    @JsonProperty("windchargecooldown")
    private Number windChargeCooldown;

    @JsonProperty("startcountdown")
    private Number startCountdown;

    @JsonProperty("startmove")
    private Boolean startMove;

    @JsonProperty("matchtypes")
    private List<String> matchTypes;

    @JsonProperty("knockback")
    private String knockback;

    @JsonProperty("tntfusetime")
    private Number tntFuseTime;

    @JsonProperty("tntautoignite")
    private Boolean tntAutoIgnite;

    @JsonProperty("pullplayerswithrod")
    private Boolean pullPlayersWithRod;

    @JsonProperty("item-pickup")
    private Boolean itemPickup;

    @JsonProperty("healthbelowname")
    private Boolean healthBelowName;

    @JsonProperty("resetbuildafterround")
    private Boolean resetBuildAfterRound;

    @JsonProperty("breakallblocks")
    private Boolean breakAllBlocks;

    @JsonProperty("roundenddelay")
    private Number roundEndDelay;

    @JsonProperty("roundstatustitles")
    private Boolean roundStatusTitles;

    @JsonProperty("countdowntitles")
    private Boolean countdownTitles;

    // Ladders with respawns
    @JsonProperty("respawntime")
    private Number respawnTime;

    // Ladders whose placed blocks return after a delay
    @JsonProperty("block-return-delay")
    private Number blockReturnDelay;

    @JsonProperty("temp-build-return-delay")
    private Number tempBuildReturnDelay;

    /** Earlier name of the two delays above, read only. */
    @JsonProperty("tempbuild-delay")
    private Number legacyTempBuildDelay;

    // Creeper
    @JsonProperty("creeper-explosion-delay")
    private Number creeperExplosionDelay;

    // Spleef
    @JsonProperty("spleef-snowball-mode")
    private Boolean spleefSnowballMode;

    @JsonIgnore
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Map<String, Object> unknownKeys = new LinkedHashMap<>();

    @JsonAnyGetter
    public Map<String, Object> unknownKeys() {
      return unknownKeys;
    }

    @JsonAnySetter
    public void unknownKey(String key, Object value) {
      unknownKeys.put(key, value);
    }
  }

  /** The {@code custom-kit-extra-items} section: items added to custom kits, Base64. */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class CustomKitExtraItems {

    @JsonProperty("unranked")
    private String unranked;

    @JsonProperty("ranked")
    private String ranked;
  }
}
