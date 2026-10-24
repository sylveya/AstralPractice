package dev.lokspel.practice.manager.ladder.type;

import static dev.lokspel.practice.util.startup.PermanentConfig.PLACED_IN_FIGHT;

import dev.lokspel.practice.config.language.LadderSpecificMessages;
import dev.lokspel.practice.config.language.MatchTypeMessages;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.enums.RoundStatus;
import dev.lokspel.practice.manager.fight.util.BlockUtil;
import dev.lokspel.practice.manager.fight.util.ChangedBlock;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.lokspel.practice.manager.ladder.abstraction.normal.BedFight;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import dev.lokspel.practice.manager.ladder.util.LadderUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;

public class BedWars extends BedFight implements LadderHandle {

  public BedWars(String name, LadderType type) {
    super(name, type);
  }

  @Override
  public LadderSpecificMessages getRespawnMessages(
      MatchTypeMessages.LadderSpecific ladderSpecific) {
    return ladderSpecific.getBedWars();
  }

  @Override
  public boolean handleEvents(Event e, Match match) {
    if (e instanceof EntityDamageEvent damageEvent) {
      onPlayerDamage(damageEvent, match);
      return true;
    } else if (e instanceof PlayerDropItemEvent dropEvent) {
      onItemDrop(dropEvent);
      return true;
    } else if (e instanceof PlayerMoveEvent moveEvent) {
      onPlayerMove(moveEvent, match);
      return true;
    } else if (e instanceof BlockBreakEvent breakEvent) {
      onBedDestroy(breakEvent, match);
      return true;
    } else if (e instanceof BlockPlaceEvent placeEvent) {
      onBlockPlace(placeEvent, match);
      return true;
    }
    return false;
  }

  private static void onBlockPlace(final @NotNull BlockPlaceEvent e, final @NotNull Match match) {
    Block block = e.getBlockPlaced();
    if (block.getType().equals(Material.TNT)) {
      LadderUtil.placeTnt(e, match);
    } else {
      BlockUtil.setMetadata(block, PLACED_IN_FIGHT, match);
      match.addBlockChange(new ChangedBlock(e));

      Block underBlock = e.getBlockPlaced().getLocation().subtract(0, 1, 0).getBlock();
      if (ArenaUtil.turnsToDirt(underBlock)) {
        match.getFightChange().addArenaBlockChange(new ChangedBlock(underBlock));
      }
    }
  }

  private static void onPlayerDamage(final @NotNull EntityDamageEvent e, final Match match) {
    if (!(e.getEntity() instanceof Player player)) {
      return;
    }

    if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
      e.setCancelled(true);
    }
    if (match.getCurrentStat(player).isSet()) {
      return;
    }

    if (e.getCause() == EntityDamageEvent.DamageCause.FALL) {
      e.setCancelled(true);
    }
  }
}
