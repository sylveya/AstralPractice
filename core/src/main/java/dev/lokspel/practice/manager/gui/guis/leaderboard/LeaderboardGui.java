package dev.lokspel.practice.manager.gui.guis.leaderboard;

import dev.lokspel.practice.config.ItemConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.match.enums.MatchType;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiCache;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.util.InventoryUtil;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class LeaderboardGui extends Gui {

  private final Supplier<dev.lokspel.practice.config.gui.LeaderboardGui> config;
  private final Gui backTo;

  protected LeaderboardGui(
      GuiType type, Supplier<dev.lokspel.practice.config.gui.LeaderboardGui> config, Gui backTo) {
    super(type);
    this.config = config;
    this.backTo = backTo;
    this.gui.put(1, InventoryUtil.createInventory(config.get().getTitle(), 6));
  }

  @Override
  public void build() {
    update(false);
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);
    if (inventory == null) {
      return;
    }
    inventory.clear();

    ItemStack filler = GuiManager.fillerItem();
    for (int i = 45; i < 54; i++) {
      inventory.setItem(i, filler);
    }

    for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
      if (!isLadderVisible(ladder)) {
        continue;
      }

      int slot = inventory.firstEmpty();
      if (slot == -1) {
        break;
      }
      inventory.setItem(slot, createLadderItem(ladder));
    }

    ItemStack fillerItem =
        icon(dev.lokspel.practice.config.gui.LeaderboardGui.Icons::getFillerItem).get();
    for (int i = 0; i < 45; i++) {
      ItemStack current = inventory.getItem(i);
      if (current == null || current.getType() == Material.AIR) {
        inventory.setItem(i, fillerItem);
      }
    }

    inventory.setItem(
        45, icon(dev.lokspel.practice.config.gui.LeaderboardGui.Icons::getBackToHub).get());
    inventory.setItem(49, createGlobalItem());
    inventory.setItem(53, LbGuiUtil.getCacheInfoItem());

    updatePlayers();
  }

  /** Builds the GUI item of an icon, or an empty item when the icon is not configured. */
  private GuiItem icon(
      Function<dev.lokspel.practice.config.gui.LeaderboardGui.Icons, ItemConfig> getter) {
    dev.lokspel.practice.config.gui.LeaderboardGui.Icons icons = config.get().getIcons();
    ItemConfig item = icons != null ? getter.apply(icons) : null;
    return item != null ? BackendUtil.getGuiItem(item) : new GuiItem();
  }

  protected boolean isLadderVisible(NormalLadder ladder) {
    return ladder.isEnabled() && ladder.getMatchTypes().contains(MatchType.DUEL);
  }

  protected abstract ItemStack createLadderItem(NormalLadder ladder);

  protected abstract ItemStack createGlobalItem();

  @Override
  public void open(Player player, int page) {
    if (GuiCache.shouldCache(type) && GuiCache.isCacheValid(type)) {
      Map<Integer, Inventory> cached = GuiCache.getCached(type);
      if (cached != null) {
        gui.clear();
        gui.putAll(cached);
      }
    } else {
      update();
    }

    super.open(player, page);
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();

    e.setCancelled(true);

    if (slot == 45) {
      if (backTo != null) {
        backTo.open(player);
      } else {
        player.closeInventory();
      }
    }
  }
}
