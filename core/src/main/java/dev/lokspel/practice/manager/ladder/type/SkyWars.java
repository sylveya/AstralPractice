package dev.lokspel.practice.manager.ladder.type;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.enums.RoundStatus;
import dev.lokspel.practice.manager.fight.match.util.MatchUtil;
import dev.lokspel.practice.manager.fight.util.ChangedBlock;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.CustomConfig;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.lokspel.practice.manager.ladder.abstraction.normal.LadderData;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import dev.lokspel.practice.util.items.ItemSerializationUtil;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class SkyWars extends NormalLadder implements CustomConfig, LadderHandle {

  private ItemStack[] skyWarsLoot;

  public SkyWars(String name, LadderType type) {
    super(name, type);
    this.startMove = false;
  }

  @Override
  public boolean isReadyToEnable() {
    return icon != null && kitData.isSet() && !matchTypes.isEmpty() && skyWarsLoot != null;
  }

  @Override
  public void writeCustomConfig(LadderData data) {
    data.setSkyWarsLoot(ItemSerializationUtil.itemStackArrayToBase64(this.skyWarsLoot));
  }

  @Override
  public void readCustomConfig(LadderData data) {
    if (data.getSkyWarsLoot() != null) {
      this.skyWarsLoot = ItemSerializationUtil.itemStackArrayFromBase64(data.getSkyWarsLoot());
    }
  }

  @Override
  public boolean handleEvents(Event e, Match match) {
    if (e instanceof PlayerInteractEvent) {
      onSkyWarsChestLoot((PlayerInteractEvent) e, match, this);
      return true;
    }
    return false;
  }

  private static void onSkyWarsChestLoot(
      final @NotNull PlayerInteractEvent e,
      final @NotNull Match match,
      final @NotNull SkyWars ladder) {
    if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
      return;
    }

    Player player = e.getPlayer();
    Block chest = e.getClickedBlock();

    if (chest == null || !chest.getType().equals(Material.CHEST)) {
      return;
    }

    e.setCancelled(true);

    if (match.getCurrentStat(player).isSet()) {
      return;
    }
    if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
      return;
    }

    List<ItemStack> loot = MatchUtil.getRandomSkyWarsLoot(ladder);
    if (loot.isEmpty()) {
      return;
    }

    for (ItemStack item : loot) {
      if (item != null && !item.getType().equals(Material.AIR)) {
        match.addEntityChange(player.getWorld().dropItemNaturally(chest.getLocation(), item));
      }
    }

    match.addBlockChange(new ChangedBlock(chest));

    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> chest.setBlockData(Material.AIR.createBlockData()),
            2L);
  }
}
