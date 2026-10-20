package dev.lokspel.practice.manager.profile;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.manager.ladder.abstraction.playercustom.CustomLadderData;
import dev.lokspel.practice.manager.profile.statistics.FfaStatsData;
import dev.lokspel.practice.manager.profile.statistics.LadderStatsData;
import dev.lokspel.practice.util.kit.KitFileData;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * A profile file, {@code profiles/<uuid>.yml}: the player's settings, display, cosmetics, custom
 * kits, ranked ban and statistics.
 */
@Getter
@Setter
public class ProfileData {

  @JsonProperty("uuid")
  private String uuid;

  @JsonProperty("join")
  private Join join;

  @JsonProperty("group")
  private String group;

  @JsonProperty("prefix")
  private String prefix;

  @JsonProperty("suffix")
  private String suffix;

  @JsonProperty("name-template")
  private String nameTemplate;

  @JsonProperty("allowed-custom-kits")
  private Integer allowedCustomKits;

  @JsonProperty("settings")
  private Settings settings;

  @JsonProperty("cosmetics")
  private Cosmetics cosmetics;

  /** Ladder custom kits: ladder name (lower case), then {@code kit1} to {@code kit4}. */
  @JsonProperty("customkit")
  private Map<String, Map<String, CustomKit>> customKits;

  @JsonProperty("ranked-ban")
  private RankedBan rankedBan;

  @JsonProperty("selected-custom-ladder")
  private Integer selectedCustomLadder;

  @JsonProperty("player-custom-kit")
  private PlayerCustomKits playerCustomKits;

  @JsonProperty("stats")
  private Stats stats;

  @JsonProperty("experience")
  private Integer experience;

  @JsonProperty("winStreak")
  private Integer winStreak;

  @JsonProperty("bestWinStreak")
  private Integer bestWinStreak;

  @JsonProperty("loseStreak")
  private Integer loseStreak;

  @JsonProperty("bestLoseStreak")
  private Integer bestLoseStreak;

  /** Join timestamps in epoch milliseconds. */
  @Getter
  @Setter
  public static class Join {

    @JsonProperty("first")
    private Long first;

    @JsonProperty("latest")
    private Long latest;
  }

  /** The player's settings; enum values are stored by name. */
  @Getter
  @Setter
  public static class Settings {

    @JsonProperty("duelrequest")
    private Boolean duelRequest;

    @JsonProperty("autoqueue")
    private Boolean autoQueue;

    @JsonProperty("sidebar")
    private Boolean sidebar;

    @JsonProperty("hideplayers")
    private Boolean hidePlayers;

    @JsonProperty("partyinvites")
    private Boolean partyInvites;

    @JsonProperty("allowspectate")
    private Boolean allowSpectate;

    @JsonProperty("flying")
    private Boolean flying;

    @JsonProperty("messages")
    private Boolean privateMessages;

    @JsonProperty("worldtime")
    private String worldTime;

    @JsonProperty("prefix-visibility")
    private String prefixVisibility;
  }

  /**
   * The cosmetics. Armor trims are kept per tier and slot under {@code tiers}; the per-slot
   * sections directly under {@code cosmetics} are the older layout without tiers and are only read.
   */
  @Getter
  @Setter
  public static class Cosmetics {

    @JsonProperty("active-tier")
    private String activeTier;

    @JsonProperty("death-effect")
    private String deathEffect;

    @JsonProperty("lobby-item")
    private String lobbyItem;

    @JsonProperty("trim-preset")
    private String trimPreset;

    @JsonProperty("shield-preset")
    private String shieldPreset;

    @JsonProperty("shield")
    private Shield shield;

    /** Trim per tier id, then per slot id. */
    @JsonProperty("tiers")
    private Map<String, Map<String, Trim>> tiers;
  }

  /** The shield cosmetics. */
  @Getter
  @Setter
  public static class Shield {

    @JsonProperty("active-layout-index")
    private Integer activeLayoutIndex;

    @JsonProperty("layouts")
    private List<String> layouts;
  }

  /** An armor trim: pattern and material as namespaced keys, e.g. {@code minecraft:coast}. */
  @Getter
  @Setter
  public static class Trim {

    @JsonProperty("pattern")
    private String pattern;

    @JsonProperty("material")
    private String material;
  }

  /** One custom kit of a ladder, per weight class. */
  @Getter
  @Setter
  public static class CustomKit {

    @JsonProperty("unranked")
    private KitContents unranked;

    @JsonProperty("ranked")
    private KitContents ranked;
  }

  /** Item contents of a custom kit as Base64 item stack arrays. */
  @Getter
  @Setter
  public static class KitContents {

    @JsonProperty("inventory")
    private String inventory;

    @JsonProperty("armor")
    private String armor;

    @JsonProperty("extra")
    private String extra;
  }

  /** The ranked ban; the section is absent when the player is not banned. */
  @Getter
  @Setter
  public static class RankedBan {

    /** UUID of the staff member who banned. */
    @JsonProperty("banner")
    private String banner;

    @JsonProperty("banned")
    private Boolean banned;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("bannedAt")
    private Long bannedAt;
  }

  /**
   * The player's own custom ladders, keyed {@code 0} to {@code 5}. Files of older versions kept a
   * single custom ladder directly in this section ({@code settings}, {@code kit-data}); it is read
   * once and moved to key {@code 0}.
   */
  public static class PlayerCustomKits {

    @Getter
    @Setter
    @JsonProperty("settings")
    private CustomLadderData.Settings legacySettings;

    @Getter
    @Setter
    @JsonProperty("kit-data")
    private KitFileData legacyKitData;

    private final Map<String, CustomLadderData> ladders = new LinkedHashMap<>();

    /**
     * Returns the custom ladders in file order.
     *
     * @return the custom ladders by key, modifiable
     */
    @JsonAnyGetter
    public Map<String, CustomLadderData> getLadders() {
      return ladders;
    }

    @JsonAnySetter
    private void putLadder(String key, CustomLadderData ladder) {
      ladders.put(key, ladder);
    }

    /**
     * Tells whether the section holds a custom ladder in the layout of older versions.
     *
     * @return whether {@code settings} or {@code kit-data} is present
     */
    public boolean hasLegacyLadder() {
      return legacySettings != null || legacyKitData != null;
    }
  }

  /** Per-ladder and FFA statistics. */
  @Getter
  @Setter
  public static class Stats {

    /** Statistics per ladder name (lower case). */
    @JsonProperty("ladder-stats")
    private Map<String, LadderStatsData> ladderStats;

    /** Statistics for FFA. */
    @JsonProperty("ffa-stats")
    private FfaStatsData ffaStats;
  }
}
