package dev.lokspel.practice.manager.ladder.enums;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.language.LadderTypeMessages;
import dev.lokspel.practice.config.language.LanguageConfig.Ladder.LadderTypes;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.SettingType;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.type.Basic;
import dev.lokspel.practice.manager.ladder.type.BattleRush;
import dev.lokspel.practice.manager.ladder.type.BedWars;
import dev.lokspel.practice.manager.ladder.type.Boxing;
import dev.lokspel.practice.manager.ladder.type.Bridges;
import dev.lokspel.practice.manager.ladder.type.Build;
import dev.lokspel.practice.manager.ladder.type.Combo;
import dev.lokspel.practice.manager.ladder.type.Creeper;
import dev.lokspel.practice.manager.ladder.type.FireballFight;
import dev.lokspel.practice.manager.ladder.type.MlgRush;
import dev.lokspel.practice.manager.ladder.type.PearlFight;
import dev.lokspel.practice.manager.ladder.type.SkyWars;
import dev.lokspel.practice.manager.ladder.type.Spleef;
import dev.lokspel.practice.manager.ladder.type.Sumo;
import dev.lokspel.practice.manager.ladder.type.TntSumo;
import dev.lokspel.practice.util.Common;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import org.bukkit.Material;

/**
 * Enum defining all available ladder types in the practice plugin. Uses LadderTypeConfig builder
 * for cleaner configuration.
 */
public enum LadderType {
  BASIC(
      LadderTypeConfig.builder(LadderTypes::getBasic, Material.DIRT, Basic.class)
          .withMovementSettings()
          .withTeamSettings()
          .withRegenSettings()
          .withCommonSettings()
          .withPearlSettings()),

  BUILD(
      LadderTypeConfig.builder(LadderTypes::getBuild, Material.STONE_PICKAXE, Build.class)
          .withBuild()
          .withMovementSettings()
          .withTeamSettings()
          .withRegenSettings()
          .withCommonSettings()
          .withPearlSettings()
          .withBuildSettings()),

  COMBO(
      LadderTypeConfig.builder(LadderTypes::getCombo, Material.PUFFERFISH, Combo.class)
          .withMovementSettings()
          .withTeamSettings()
          .withRegenSettings()
          .withCommonSettings()
          .withPearlSettings()),

  SUMO(
      LadderTypeConfig.builder(LadderTypes::getSumo, Material.STICK, Sumo.class)
          .withTeamSettings()
          .withCommonSettings()
          .withPearlSettings()),

  TNT_SUMO(
      LadderTypeConfig.builder(LadderTypes::getTntSumo, Material.TNT, TntSumo.class)
          .withBuild()
          .withTeamSettings()
          .withCommonSettings()
          .withPearlSettings()
          .withSetting(SettingType.TEMP_BUILD_RETURN_DELAY)),

  BOXING(
      LadderTypeConfig.builder(LadderTypes::getBoxing, Material.DIAMOND_CHESTPLATE, Boxing.class)
          .withMovementSettings()
          .withTeamSettings()
          .withCommonSettings()
          .withPearlSettings()
          .withSetting(SettingType.BOXING_HITS)),

  PEARL_FIGHT(
      LadderTypeConfig.builder(LadderTypes::getPearlFight, Material.ENDER_PEARL, PearlFight.class)
          .withBuild()
          .withMovementSettings()
          .withCommonSettings()
          .withPearlSettings()
          .withSettings(
              SettingType.GOLDEN_APPLE_COOLDOWN,
              SettingType.TEMP_BUILD_RETURN_DELAY,
              SettingType.MULTI_ROUND_START_COUNTDOWN)
          .withBuildSettings()),

  SPLEEF(
      LadderTypeConfig.builder(LadderTypes::getSpleef, Material.IRON_SHOVEL, Spleef.class)
          .withBuild()
          .withMovementSettings()
          .withSettings(
              SettingType.REGENERATION,
              SettingType.HUNGER,
              SettingType.MULTI_ROUND_START_COUNTDOWN,
              SettingType.HIT_DELAY,
              SettingType.KNOCKBACK,
              SettingType.WEIGHT_CLASS,
              SettingType.ROUNDS,
              SettingType.HEARTS,
              SettingType.MAX_DURATION,
              SettingType.START_COUNTDOWN,
              SettingType.SPLEEF_SNOWBALL_MODE,
              SettingType.ROUND_END_DELAY,
              SettingType.COUNTDOWN_TITLES,
              SettingType.ROUND_STATUS_TITLES)),

  SKYWARS(
      LadderTypeConfig.builder(LadderTypes::getSkywars, Material.ENDER_EYE, SkyWars.class)
          .withBuild()
          .withTeamSettings()
          .withRegenSettings()
          .withCommonSettings()
          .withPearlSettings()
          .withBuildSettings()
          .withSetting(SettingType.SKYWARS_LOOT)),

  BEDWARS(
      LadderTypeConfig.builder(LadderTypes::getBedwars, Material.RED_BED, BedWars.class)
          .withBuild()
          .withBed()
          .noPartyFfa()
          .withRespawnSettings()
          .withRegenSettings()
          .withCommonSettings()
          .withPearlSettings()
          .withBuildSettings()),

  FIREBALL_FIGHT(
      LadderTypeConfig.builder(
              LadderTypes::getFireballFight, Material.FIRE_CHARGE, FireballFight.class)
          .withBuild()
          .withBed()
          .noPartyFfa()
          .withRespawnSettings()
          .withRegenSettings()
          .withRegenSettings()
          .withCommonSettings()
          .withPearlSettings()
          .withBuildSettings()
          .withSetting(SettingType.FIREBALL_COOLDOWN)
          .withSetting(SettingType.FIREBALL_BLOCK_DESTROY)),

  MLG_RUSH(
      LadderTypeConfig.builder(LadderTypes::getMlgRush, Material.BLUE_TERRACOTTA, MlgRush.class)
          .withBuild()
          .withBed()
          .noPartyFfa()
          .withRespawnSettings()
          .withCommonSettings()
          .withPearlSettings()
          .withSetting(SettingType.BLOCK_RETURN_DELAY)),

  BRIDGES(
      LadderTypeConfig.builder(LadderTypes::getBridges, Material.RED_TERRACOTTA, Bridges.class)
          .withBuild()
          .withPortal()
          .noPartyFfa()
          .withRespawnSettings()
          .withCommonSettings()
          .withPearlSettings()
          .withBuildSettings()
          .withSetting(SettingType.RESET_BUILD_AFTER_ROUND)),

  BATTLE_RUSH(
      LadderTypeConfig.builder(LadderTypes::getBattleRush, Material.LILY_PAD, BattleRush.class)
          .withBuild()
          .withPortal()
          .noPartyFfa()
          .withRespawnSettings()
          .withCommonSettings()
          .withPearlSettings()
          .withBuildSettings()
          .withSetting(SettingType.TEMP_BUILD_RETURN_DELAY)),

  CREEPER(
      LadderTypeConfig.builder(LadderTypes::getCreeper, Material.CREEPER_HEAD, Creeper.class)
          .withBuild()
          .withRegenSettings()
          .withCommonSettings()
          .withPearlSettings()
          .withBuildSettings()
          .withSetting(SettingType.CREEPER_EXPLOSION_DELAY));

  private final Function<LadderTypes, LadderTypeMessages> messages;

  @Getter private final Material icon;
  @Getter private final boolean build;
  @Getter private final boolean isPartyFfaSupported;
  @Getter private final Class<? extends NormalLadder> classInstance;
  @Getter private final List<SettingType> settingTypes;
  @Getter private final boolean bed;
  @Getter private final boolean portal;

  LadderType(LadderTypeConfig config) {
    this.messages = config.getMessages();
    this.icon = config.getIcon();
    this.build = config.isBuild();
    this.isPartyFfaSupported = config.isPartyFfaSupported();
    this.classInstance = config.getClassInstance();
    this.settingTypes = config.getSettingTypes();
    this.bed = config.isHasBed();
    this.portal = config.isHasPortal();
  }

  public String getName() {
    return Common.mmToNormal(getMessages().getName());
  }

  public List<String> getDescription() {
    return Common.mmToNormal(getMessages().getDescription());
  }

  /**
   * Creates an instance of this type, loading its {@code ladders/<name>.yml} file when it exists.
   *
   * @param name the ladder's name
   * @return the created ladder
   */
  public NormalLadder create(String name) {
    try {
      return classInstance.getConstructor(String.class, LadderType.class).newInstance(name, this);
    } catch (NoSuchMethodException
        | InvocationTargetException
        | InstantiationException
        | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private LadderTypeMessages getMessages() {
    return this.messages.apply(PracticeConfig.language().getLadder().getLadderTypes());
  }
}
