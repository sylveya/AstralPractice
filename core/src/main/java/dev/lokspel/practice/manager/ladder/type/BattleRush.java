package dev.lokspel.practice.manager.ladder.type;

import dev.lokspel.practice.config.language.LadderSpecificMessages;
import dev.lokspel.practice.config.language.MatchTypeMessages;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.Round;
import dev.lokspel.practice.manager.fight.match.enums.RoundStatus;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.DeathResult;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.RespawnableLadder;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.TempBuild;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.TempBuildReturnDelay;
import dev.lokspel.practice.manager.ladder.abstraction.normal.PortalFight;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import dev.lokspel.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class BattleRush extends PortalFight
    implements LadderHandle, TempBuild, RespawnableLadder, TempBuildReturnDelay {

  @Getter @Setter private int respawnTime;

  // Saved by using interface and LadderFile.java
  @Getter @Setter private int tempBuildReturnDelaySeconds;

  public BattleRush(String name, LadderType type) {
    super(name, type);
  }

  @Override
  public DeathResult handlePlayerDeath(Player player, Match match, Round round) {
    // BattleRush always respawns players - they only get eliminated by portal score
    return DeathResult.TEMPORARY_DEATH;
  }

  @Override
  public LadderSpecificMessages getRespawnMessages(
      MatchTypeMessages.LadderSpecific ladderSpecific) {
    return ladderSpecific.getBattleRush();
  }

  @Override
  public boolean handleEvents(Event e, Match match) {
    if (e instanceof BlockBreakEvent breakEvent) {
      onBlockBreak(breakEvent, match);
      TempBuild.onBlockBreak(breakEvent, match);
      return true;
    } else if (e instanceof BlockPlaceEvent placeEvent) {
      onBlockPlace(placeEvent, match);
      TempBuild.onBlockPlace(placeEvent, match, tempBuildReturnDelaySeconds);
      return true;
    } else if (e instanceof PlayerBucketEmptyEvent bucketEvent) {
      onBucketEmpty(bucketEvent, match);
      TempBuild.onBucketEmpty(bucketEvent, match, tempBuildReturnDelaySeconds);
      return true;
    } else if (e instanceof BlockFromToEvent flowEvent) {
      onLiquidFlow(flowEvent);
      return true;
    } else if (e instanceof PlayerMoveEvent moveEvent) {
      onPlayerMove(moveEvent, match);
      return true;
    } else if (e instanceof EntityDamageEvent damageEvent) {
      onPlayerDamage(damageEvent, match);
      return true;
    }
    return false;
  }

  private static void onPlayerDamage(
      final @NotNull EntityDamageEvent e, final @NotNull Match match) {
    if (e.getEntity() instanceof Player player
        && match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
      e.setDamage(0);
      PlayerUtil.healToMaxHealth(player);
    }
  }
}
