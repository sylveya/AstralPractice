package dev.lokspel.practice.util.startup;

import dev.lokspel.practice.config.PracticeConfig;

public final class PermanentConfig {

  private PermanentConfig() {}

  public static final boolean ARENA_FAST_COPY_ENABLED =
      PracticeConfig.settings().getArena().getFastCopy().isEnabled();
  public static final boolean ARENA_COPY_FAWE_ENABLED =
      PracticeConfig.settings().getArena().getFastCopy().isUseFawe();
  public static int ARENA_COPY_MAX_CHANGES = 50;
  public static int ARENA_COPY_MAX_CHECKS = 500;
  public static int MATCH_ROLLBACK_MAX_CHANGES = 100;
  public static int MATCH_ROLLBACK_MAX_CHECKS = 300;
  public static int EVENT_ROLLBACK_MAX_CHANGES = 50;
  public static int EVENT_ROLLBACK_MAX_CHECKS = 100;
  public static final boolean JOIN_TELEPORT_LOBBY =
      PracticeConfig.settings().getPlayer().isJoinTeleportLobby();
  public static final boolean NAMETAG_MANAGEMENT_ENABLED =
      PracticeConfig.settings().getPlayer().getNametagManagement().isEnabled();

  public static final boolean DISPLAY_ARROW_HIT =
      PracticeConfig.settings().getMatchSettings().isDisplayArrowHitHealth();
  public static final boolean PARTY_SPLIT_TEAM_DAMAGE =
      PracticeConfig.settings().getMatchSettings().getParty().isSplitTeamDamage();
  public static final boolean PARTY_VS_PARTY_TEAM_DAMAGE =
      PracticeConfig.settings().getMatchSettings().getParty().isPartyVsPartyTeamDamage();

  public static final String FIGHT_ENTITY = "ASTRALPRACTICE_FIGHT_ENTITY";
  public static final String PLACED_IN_FIGHT = "ASTRALPRACTICE_BLOCK_CHANGE";

  static {
    if (ARENA_FAST_COPY_ENABLED) {
      int multiplier = PracticeConfig.settings().getArena().getFastCopy().getMultiplier();
      if (multiplier < 1) {
        multiplier = 1;
      } else if (multiplier > 100) {
        multiplier = 100;
      }

      ARENA_COPY_MAX_CHANGES = ARENA_COPY_MAX_CHANGES * multiplier;
      ARENA_COPY_MAX_CHECKS = ARENA_COPY_MAX_CHECKS * multiplier;
    }

    int rollbackMultiplier =
        PracticeConfig.settings().getMatchSettings().getRollback().getMultiplier();
    if (rollbackMultiplier < 1) {
      rollbackMultiplier = 1;
    } else if (rollbackMultiplier > 100) {
      rollbackMultiplier = 100;
    }

    MATCH_ROLLBACK_MAX_CHANGES = MATCH_ROLLBACK_MAX_CHANGES * rollbackMultiplier;
    MATCH_ROLLBACK_MAX_CHECKS = MATCH_ROLLBACK_MAX_CHECKS * rollbackMultiplier;
    EVENT_ROLLBACK_MAX_CHANGES = EVENT_ROLLBACK_MAX_CHANGES * rollbackMultiplier;
    EVENT_ROLLBACK_MAX_CHECKS = EVENT_ROLLBACK_MAX_CHECKS * rollbackMultiplier;
  }
}
