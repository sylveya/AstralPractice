package dev.lokspel.practice.manager.ladder.abstraction.interfaces;

import static dev.lokspel.practice.util.startup.PermanentConfig.PLACED_IN_FIGHT;

import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.util.BlockUtil;
import dev.lokspel.practice.manager.fight.util.ChangedBlock;
import dev.lokspel.practice.manager.fight.util.ListenerUtil;
import dev.lokspel.practice.util.fightmapchange.BlockPosition;
import dev.lokspel.practice.util.fightmapchange.FightChangeOptimized;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface TempBuild {

  String TEMP_BUILD_BLOCK_ITEM = "ASTRALPRACTICE_TEMP_BUILD_BLOCK_ITEM";

  static void onBucketEmpty(
      final @NotNull PlayerBucketEmptyEvent e, final @NotNull Match match, final int buildDelay) {
    if (e.isCancelled()) {
      return;
    }

    Player player = e.getPlayer();
    Block block = e.getBlockClicked();

    // The liquid lands in e.getBlock(): the clicked face for normal placements, but the
    // clicked block itself when it can hold liquids (slabs, stairs, fences, ...), which are
    // then waterlogged instead of receiving a water block.
    BlockUtil.setMetadata(e.getBlock(), PLACED_IN_FIGHT, match);

    for (BlockFace face : BlockFace.values()) {
      Block relative = block.getRelative(face, 1);
      if (BlockUtil.hasMetadata(relative, PLACED_IN_FIGHT)) {
        Object mv = BlockUtil.getMetadata(relative, PLACED_IN_FIGHT, Object.class);
        if (ListenerUtil.checkMetaData(mv) || relative.getType().isSolid()) {
          continue;
        }

        match
            .getFightChange()
            .addBlockChange(new ChangedBlock(e.getBlock()), player, buildDelay, e.getHand());

        Block b2 = block.getLocation().subtract(0, 1, 0).getBlock();
        if (ArenaUtil.turnsToDirt(b2)) {
          match.getFightChange().addArenaBlockChange(new ChangedBlock(b2));
        }
      }
    }
  }

  static void onBlockPlace(
      final @NotNull BlockPlaceEvent e, final @NotNull Match match, final int buildDelay) {
    if (e.isCancelled()) {
      return;
    }

    Player player = e.getPlayer();
    Block block = e.getBlockPlaced();

    BlockUtil.setMetadata(block, PLACED_IN_FIGHT, match);
    BlockUtil.setMetadata(block, TEMP_BUILD_BLOCK_ITEM, createPlacedReturnItem(e));

    match.getFightChange().addBlockChange(new ChangedBlock(e), player, buildDelay, e.getHand());

    Block block2 = e.getBlockPlaced().getLocation().subtract(0, 1, 0).getBlock();
    if (ArenaUtil.turnsToDirt(block2)) {
      match.getFightChange().addArenaBlockChange(new ChangedBlock(block2));
    }
  }

  static void onBlockBreak(final @NotNull BlockBreakEvent e, final @NotNull Match match) {
    if (e.isCancelled()) {
      return;
    }

    Block block = e.getBlock();
    Location location = block.getLocation();
    FightChangeOptimized fightChange = match.getFightChange();

    if (!BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) {
      return;
    }

    Object mv = BlockUtil.getMetadata(e.getBlock(), PLACED_IN_FIGHT, Object.class);
    if (ListenerUtil.checkMetaData(mv)) {
      e.setCancelled(true);
      return;
    }

    long pos = BlockPosition.encode(location);
    FightChangeOptimized.BlockChangeEntry entry = fightChange.getBlocks().get(pos);
    if (entry != null && entry.getTempData() != null) {
      FightChangeOptimized.TempBlockData tempData = entry.getTempData();
      Player player = tempData.getPlayer();

      if (match.getPlayers().contains(player) && !match.getCurrentStat(player).isSet()) {
        e.setCancelled(true);
        tempData.reset(fightChange, entry.getChangedBlock(), pos);
      }
    }
  }

  private static @NotNull ItemStack createPlacedReturnItem(final @NotNull BlockPlaceEvent e) {
    ItemStack placedItem = e.getItemInHand();
    if (placedItem.getType().isAir()) {
      return new ItemStack(e.getBlockPlaced().getType(), 1);
    }

    ItemStack clone = placedItem.clone();
    clone.setAmount(1);
    return clone;
  }
}
