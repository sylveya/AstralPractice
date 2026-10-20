package dev.lokspel.practice.manager.ladder.abstraction.playercustom;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.fight.match.enums.MatchType;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import dev.lokspel.practice.manager.playerkit.guis.MainGui;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.util.kit.KitFileData;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class CustomLadder extends Ladder {

  private static final String DEFAULT_NAME =
      PracticeConfig.playerKit().getDefaultSettings().getName();
  private static final boolean DEFAULT_REGEN =
      PracticeConfig.playerKit().getDefaultSettings().isRegeneration();
  private static final boolean DEFAULT_HUNGER =
      PracticeConfig.playerKit().getDefaultSettings().isHunger();
  private static final boolean DEFAULT_BUILD =
      PracticeConfig.playerKit().getDefaultSettings().isBuild();
  private static final int DEFAULT_ROUNDS =
      PracticeConfig.playerKit().getDefaultSettings().getRounds();
  private static final double DEFAULT_HITDELAY =
      convertHitDelayToMultiplier(PracticeConfig.playerKit().getDefaultSettings().getHitdelay());
  private static final double DEFAULT_EP_COOLDOWN =
      PracticeConfig.playerKit().getDefaultSettings().getEpCooldown();
  private static final double DEFAULT_GA_COOLDOWN =
      PracticeConfig.playerKit().getDefaultSettings().getGaCooldown();
  private static final double DEFAULT_WIND_CHARGE_COOLDOWN =
      PracticeConfig.playerKit().getDefaultSettings().getWindChargeCooldown();
  private static final boolean DEFAULT_HEALTH_BELOW_NAME =
      PracticeConfig.playerKit().getDefaultSettings().isHealthBelowName();

  private MainGui mainGui;

  private static final List<MatchType> MATCH_TYPES = new ArrayList<>();

  static {
    for (String matchType : PracticeConfig.playerKit().getMatchTypes()) {
      MATCH_TYPES.add(MatchType.valueOf(matchType));
    }
  }

  private final Profile profile;

  /** Key of this ladder in the profile file's {@code player-custom-kit} section. */
  private final String key;

  /**
   * Creates a player's custom ladder and loads what the profile file holds under its key.
   *
   * @param profile the owning profile
   * @param key key in the profile file's {@code player-custom-kit} section, e.g. {@code "0"}
   * @param id number shown in the default name
   */
  public CustomLadder(final Profile profile, final String key, final int id) {
    this(profile, key, id, profile.getFile().getCustomLadderData(key));
  }

  /**
   * Creates a player's custom ladder from the given data.
   *
   * @param profile the owning profile
   * @param key key in the profile file's {@code player-custom-kit} section, e.g. {@code "0"}
   * @param id number shown in the default name
   * @param data the stored ladder, or {@code null} to keep the defaults
   */
  public CustomLadder(
      final Profile profile, final String key, final int id, final CustomLadderData data) {
    super(DEFAULT_NAME.replace("%id%", String.valueOf(id)), LadderType.BASIC);

    this.profile = profile;
    this.key = key;

    this.setDisplayName(this.getName());
    this.setRegen(DEFAULT_REGEN);
    this.setHunger(DEFAULT_HUNGER);
    this.setBuild(DEFAULT_BUILD);
    this.setRounds(DEFAULT_ROUNDS);
    this.setAttackCooldownModifier(DEFAULT_HITDELAY);
    this.setEnderPearlCooldown(DEFAULT_EP_COOLDOWN);
    this.setGoldenAppleCooldown(DEFAULT_GA_COOLDOWN);
    this.setWindChargeCooldown(DEFAULT_WIND_CHARGE_COOLDOWN);
    this.setHealthBelowName(DEFAULT_HEALTH_BELOW_NAME);
    this.matchTypes = new ArrayList<>(MATCH_TYPES);

    if (data != null) {
      this.load(data);
    }
  }

  public CustomLadder(final CustomLadder customLadder, final Profile profile, final String key) {
    super(customLadder);

    this.profile = profile;
    this.key = key;
  }

  public MainGui getMainGui() {
    if (mainGui == null) {
      mainGui = new MainGui(this);
    }
    return mainGui;
  }

  /**
   * Drops the kit editor unless a player has it open, the next {@link #getMainGui()} creates it
   * from the current files.
   */
  public void resetMainGui() {
    if (mainGui == null || !mainGui.getInGuiPlayers().isEmpty()) {
      return;
    }

    mainGui = null;
  }

  @Override
  public boolean isReadyToEnable() {
    return kitData.isSet() && !matchTypes.isEmpty();
  }

  /** Loads this ladder again from what the profile file's model holds under its key. */
  public void getData() {
    CustomLadderData data = profile.getFile().getCustomLadderData(key);
    if (data != null) {
      load(data);
    }
  }

  private void load(CustomLadderData data) {
    CustomLadderData.Settings settings = data.getSettings();
    if (settings != null) {
      loadSettings(settings);
    }

    KitFileData kit = data.getKitData();
    if (kit != null) {
      kit.applyTo(kitData);
    }
  }

  private void loadSettings(CustomLadderData.Settings settings) {
    if (settings.getName() != null) {
      displayName = settings.getName();
    }
    if (settings.getRegen() != null) {
      regen = settings.getRegen();
    }
    if (settings.getHunger() != null) {
      hunger = settings.getHunger();
    }
    if (settings.getBuild() != null) {
      this.setBuild(settings.getBuild());
    }
    if (settings.getRounds() != null) {
      rounds = settings.getRounds();
    }
    // A whole number is the legacy hit delay in ticks, a decimal the multiplier
    Number hitDelay = settings.getHitDelay();
    if (hitDelay instanceof Integer || hitDelay instanceof Long) {
      attackCooldownModifier = convertHitDelayToMultiplier(hitDelay.intValue());
    } else if (hitDelay != null) {
      attackCooldownModifier = Math.clamp(hitDelay.doubleValue(), 0, 3.0);
    }
    if (settings.getKnockback() != null) {
      ladderKnockback.get(settings.getKnockback());
    }
    if (settings.getEnderPearlCooldown() != null) {
      enderPearlCooldown = Math.clamp(settings.getEnderPearlCooldown().doubleValue(), 0.0, 60.0);
    }
    if (settings.getGoldenAppleCooldown() != null) {
      goldenAppleCooldown = Math.clamp(settings.getGoldenAppleCooldown().doubleValue(), 0.0, 30.0);
    }
    if (settings.getWindChargeCooldown() != null) {
      windChargeCooldown = Math.clamp(settings.getWindChargeCooldown().doubleValue(), 0.0, 30.0);
    }
    if (settings.getHealthBelowName() != null) {
      healthBelowName = settings.getHealthBelowName();
    }
  }

  @Override
  public List<Arena> getArenas() {
    List<Arena> arenas = new ArrayList<>();
    for (Arena arena : ArenaManager.getInstance().getNormalArenas()) {
      if (arena.isEnabled() && arena.isAllowCustomKitOnMap()) {
        if (arena.getAssignedLadderTypes().contains(this.type)) {
          arenas.add(arena);
        }
      }
    }
    return arenas;
  }

  /**
   * Writes this ladder into the profile file's model under its key. The file itself is written by
   * the profile's save.
   */
  public void setData() {
    CustomLadderData data = profile.getFile().getOrCreateCustomLadderData(key);

    CustomLadderData.Settings settings =
        data.getSettings() != null ? data.getSettings() : new CustomLadderData.Settings();
    settings.setName(displayName);
    settings.setRegen(regen);
    settings.setHunger(hunger);
    settings.setBuild(build);
    settings.setRounds(rounds);
    settings.setHitDelay(attackCooldownModifier);
    settings.setKnockback(ladderKnockback.get());
    settings.setEnderPearlCooldown(enderPearlCooldown);
    settings.setGoldenAppleCooldown(goldenAppleCooldown);
    settings.setWindChargeCooldown(windChargeCooldown);
    settings.setHealthBelowName(healthBelowName);
    data.setSettings(settings);

    // Like before, a part of the kit that is not set leaves what the file held
    KitFileData kit = data.getKitData() != null ? data.getKitData() : new KitFileData();
    kit.update(kitData);
    data.setKitData(kit);
  }

  @Override
  public void setBuild(boolean build) {
    if (build) {
      this.setType(LadderType.BUILD);
    } else {
      this.setType(LadderType.BASIC);
    }

    this.build = build;
  }

  @Override
  public boolean isEnabled() {
    return isReadyToEnable();
  }

  /**
   * Converts hitdelay ticks to multiplier (old format to new format). Default 20 ticks = 1.0
   * multiplier.
   */
  private static double convertHitDelayToMultiplier(int ticks) {
    if (ticks <= 0) {
      return 0;
    }
    return Math.clamp(ticks / 20.0, 0, 3.0);
  }
}
