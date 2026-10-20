package dev.lokspel.practice.manager.gui.guis.queue;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.gui.QueueGui;
import dev.lokspel.practice.config.settings.QueueSettings;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.match.enums.WeightClass;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.guis.queue.customkit.ChooseQueueTypeGui;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.queue.CustomKitQueueManager;
import dev.lokspel.practice.manager.queue.QueueManager;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class RankedGui extends QueueSelectorGui {

  private static final int DEFAULT_SWITCH_SLOT = 49;

  public RankedGui() {
    super(GuiType.Queue_Ranked);
  }

  @Override
  protected long getUpdateCooldownMinutes() {
    return PracticeConfig.settings().getQueue().getRanked().getGuiUpdateMinute();
  }

  @Override
  protected QueueSettings queueSettings() {
    return PracticeConfig.settings().getQueue().getRanked();
  }

  @Override
  protected QueueGui gui() {
    return PracticeConfig.guis().getGuis().getRankedGui();
  }

  @Override
  protected WeightClass getWeightClass() {
    return WeightClass.RANKED;
  }

  @Override
  protected boolean isRanked() {
    return true;
  }

  @Override
  protected boolean isValidLadder(NormalLadder ladder) {
    return ladder.isRanked();
  }

  @Override
  protected void onLadderClick(Player player, NormalLadder ladder) {
    boolean keepOpen = QueueManager.getInstance().isMultiQueueAllowed(player);
    QueueManager.getInstance().createRankedQueue(player, ladder, keepOpen);
  }

  @Override
  protected void decoratePage(int pageId, Inventory inventory) {
    int slot = inventory.getSize() - 1;
    placeSwitchItem(inventory);

    if (!Boolean.TRUE.equals(
            PracticeConfig.settings().getQueue().getRanked().getCustomKit().getEntryEnabled())
        || !CustomKitQueueManager.getInstance().isCustomKitQueueEnabled()) {
      ItemStack filler =
          BackendUtil.getGuiItem(
                  PracticeConfig.guis().getGuis().getRankedGui().getIcons().getFillerItem())
              .get();
      if (filler != null) {
        inventory.setItem(slot, filler);
      }
      return;
    }

    ItemStack customQueueItem =
        BackendUtil.getGuiItem(
                PracticeConfig.guis().getGuis().getRankedGui().getIcons().getCustomKitQueue())
            .get();
    if (customQueueItem == null) {
      customQueueItem = ItemCreateUtil.createItem("<gold>Custom Kit Queue", Material.BOOK);
    }

    inventory.setItem(slot, customQueueItem);
  }

  @Override
  protected boolean handleCustomTopInventoryClick(
      Player player, int rawSlot, InventoryView inventoryView, ItemStack item) {
    int switchSlot = getSwitchModeSlot();
    if (isQueueGuiSwitchEnabled()
        && switchSlot >= 0
        && switchSlot < inventoryView.getTopInventory().getSize()
        && rawSlot == switchSlot) {
      Gui unrankedGui = GuiManager.getInstance().searchGui(GuiType.Queue_Unranked);
      if (unrankedGui != null) {
        int currentPage = this.getInGuiPlayers().getOrDefault(player, 1);
        unrankedGui.open(player, currentPage);
      }
      return true;
    }

    int customQueueSlot = inventoryView.getTopInventory().getSize() - 1;
    if (rawSlot != customQueueSlot) {
      return false;
    }

    if (!Boolean.TRUE.equals(
            PracticeConfig.settings().getQueue().getRanked().getCustomKit().getEntryEnabled())
        || !CustomKitQueueManager.getInstance().isCustomKitQueueEnabled()) {
      return true;
    }

    Gui gui = GuiManager.getInstance().searchGui(GuiType.Queue_CustomKitChooseType);
    if (gui instanceof ChooseQueueTypeGui chooseQueueTypeGui) {
      chooseQueueTypeGui.openFor(player, GuiType.Queue_Ranked);
    } else if (gui != null) {
      gui.open(player);
    }
    return true;
  }

  private void placeSwitchItem(Inventory inventory) {
    if (!isQueueGuiSwitchEnabled()) {
      return;
    }

    int switchSlot = getSwitchModeSlot();
    if (switchSlot < 0 || switchSlot >= inventory.getSize()) {
      return;
    }

    ItemStack switchItem =
        BackendUtil.getGuiItem(
                PracticeConfig.guis().getGuis().getRankedGui().getIcons().getSwitchToUnranked())
            .get();
    if (switchItem == null) {
      switchItem = ItemCreateUtil.createItem("<green>Switch to Unranked", Material.WOODEN_SWORD);
    }

    inventory.setItem(switchSlot, switchItem);
  }

  private int getSwitchModeSlot() {
    return PracticeConfig.settings().getQueue().getRanked().getSelectorGui().getSwitchModeSlot();
  }

  private boolean isQueueGuiSwitchEnabled() {
    return PracticeConfig.settings().getQueue().getCombined().isSwitchEnabled();
  }
}
