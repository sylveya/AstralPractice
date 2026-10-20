package dev.lokspel.practice.manager.ladder.enums;

import dev.lokspel.practice.config.language.LadderTypeMessages;
import dev.lokspel.practice.config.language.LanguageConfig;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.SettingType;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import org.bukkit.Material;

/**
 * Builder class for creating LadderType configurations in a more readable and maintainable way.
 * Provides a fluent API for defining ladder properties.
 */
@Getter
public class LadderTypeConfig {

  private final Function<LanguageConfig.Ladder.LadderTypes, LadderTypeMessages> messages;
  private final Material icon;
  private final Class<? extends NormalLadder> classInstance;

  private boolean build = false;
  private boolean isPartyFfaSupported = true;
  private boolean hasBed = false;
  private boolean hasPortal = false;

  private final List<SettingType> settingTypes = new ArrayList<>();

  private LadderTypeConfig(
      Function<LanguageConfig.Ladder.LadderTypes, LadderTypeMessages> messages,
      Material icon,
      Class<? extends NormalLadder> classInstance) {
    this.messages = messages;
    this.icon = icon;
    this.classInstance = classInstance;
  }

  /**
   * Creates a new ladder type configuration builder.
   *
   * @param messages Selects the ladder's name and description section from LADDER.LADDER-TYPES
   * @param icon Material icon for the ladder
   * @param classInstance The ladder implementation class
   * @return A new builder instance
   */
  public static LadderTypeConfig builder(
      Function<LanguageConfig.Ladder.LadderTypes, LadderTypeMessages> messages,
      Material icon,
      Class<? extends NormalLadder> classInstance) {
    return new LadderTypeConfig(messages, icon, classInstance);
  }

  /** Marks this ladder as requiring build mechanics. */
  public LadderTypeConfig withBuild() {
    this.build = true;
    return this;
  }

  /** Marks this ladder as not supporting Party FFA mode. */
  public LadderTypeConfig noPartyFfa() {
    this.isPartyFfaSupported = false;
    return this;
  }

  /** Marks this ladder as using bed mechanics (like BedWars). */
  public LadderTypeConfig withBed() {
    this.hasBed = true;
    return this;
  }

  /** Marks this ladder as using portal mechanics (like Bridges, BattleRush). */
  public LadderTypeConfig withPortal() {
    this.hasPortal = true;
    return this;
  }

  /** Adds a single setting type to this ladder. */
  public LadderTypeConfig withSetting(SettingType setting) {
    this.settingTypes.add(setting);
    return this;
  }

  /** Adds multiple setting types to this ladder. */
  public LadderTypeConfig withSettings(SettingType... settings) {
    this.settingTypes.addAll(Arrays.asList(settings));
    return this;
  }

  public LadderTypeConfig withRegenSettings() {
    return withSettings(
        SettingType.HUNGER,
        SettingType.REGENERATION,
        SettingType.HEALTH_BELOW_NAME,
        SettingType.GOLDEN_APPLE_COOLDOWN);
  }

  /**
   * Adds common settings that most ladders use. Includes: EDITABLE, HIT_DELAY, HUNGER, KNOCKBACK,
   * WEIGHT_CLASS, REGENERATION, ROUNDS, MAX_DURATION, START_COUNTDOWN, HEALTH_BELOW_NAME
   */
  public LadderTypeConfig withCommonSettings() {
    return withSettings(
        SettingType.EDITABLE,
        SettingType.HIT_DELAY,
        SettingType.KNOCKBACK,
        SettingType.WEIGHT_CLASS,
        SettingType.ROUNDS,
        SettingType.HEARTS,
        SettingType.MAX_DURATION,
        SettingType.START_COUNTDOWN,
        SettingType.DROP_INVENTORY,
        SettingType.ROUND_END_DELAY,
        SettingType.ROUND_STATUS_TITLES,
        SettingType.COUNTDOWN_TITLES,
        SettingType.PULL_PLAYERS_WITH_ROD);
  }

  /** Adds pearl-related settings (ender pearl and golden apple cooldowns). */
  public LadderTypeConfig withPearlSettings() {
    return withSettings(SettingType.ENDER_PEARL_COOLDOWN);
  }

  /** Adds team-based settings. */
  public LadderTypeConfig withTeamSettings() {
    return withSettings(SettingType.MULTI_ROUND_START_COUNTDOWN);
  }

  /** Adds respawn-related settings for ladders that support respawning. */
  public LadderTypeConfig withRespawnSettings() {
    return withSettings(SettingType.RESPAWN_TIME, SettingType.MULTI_ROUND_START_COUNTDOWN);
  }

  /** Adds build-related settings (TNT, temp build delay). */
  public LadderTypeConfig withBuildSettings() {
    return withSettings(
        SettingType.TNT_FUSE_TIME,
        SettingType.TNT_AUTO_IGNITE,
        SettingType.BREAK_ALL_BLOCKS,
        SettingType.WIND_CHARGE_COOLDOWN);
  }

  /** Adds movement-related settings. */
  public LadderTypeConfig withMovementSettings() {
    return withSetting(SettingType.START_MOVING);
  }
}
