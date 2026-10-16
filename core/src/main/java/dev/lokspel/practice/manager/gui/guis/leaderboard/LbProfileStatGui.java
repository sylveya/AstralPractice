package dev.lokspel.practice.manager.gui.guis.leaderboard;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LbProfileStatGui extends Gui {

  private final Profile profile;
  private final Gui backTo;

  public LbProfileStatGui(Profile profile, Gui backTo) {
    super(GuiType.Leaderboard_Profile);
    this.profile = profile;
    this.backTo = backTo;

    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis()
                .getGuis()
                .getStatistics()
                .getPlayerStatistics()
                .getTitle()
                .replace("%player%", profile.getPlayer().getName()),
            6));

    build();
  }

  @Override
  public void build() {
    update();
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
      if (!ladder.isEnabled()) {
        continue;
      }

      int slot = inventory.firstEmpty();
      if (slot == -1) {
        break;
      }
      inventory.setItem(slot, LbGuiUtil.createLadderStatItem(profile, ladder).get());
    }

    ItemStack fillerItem =
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getStatistics()
                    .getPlayerStatistics()
                    .getIcons()
                    .getFillerItem())
            .get();
    for (int i = 0; i < 45; i++) {
      ItemStack current = inventory.getItem(i);
      if (current == null || current.getType() == Material.AIR) {
        inventory.setItem(i, fillerItem);
      }
    }

    inventory.setItem(
        45,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getStatistics()
                    .getPlayerStatistics()
                    .getIcons()
                    .getBackToHub())
            .get());
    inventory.setItem(49, LbGuiUtil.createProfileAllStatItem(profile));

    updatePlayers();
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
