package dev.lokspel.practice.manager.gui.guis.customladder.premadecustom;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.PageUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomLadderSelectorGui extends Gui {

  private final int spaces = 45;
  private final Map<Integer, Map<Integer, NormalLadder>> ladderSlots = new HashMap<>();

  public CustomLadderSelectorGui() {
    super(GuiType.CustomLadder_Selector);
    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    List<NormalLadder> ladders = new ArrayList<>();
    for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
      if (ladder.isEnabled() && ladder.isEditable()) {
        ladders.add(ladder);
      }
    }
    ladders.sort(Comparator.comparing(Ladder::getName, String::compareToIgnoreCase));

    Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
    Map<Integer, Inventory> newGui = new HashMap<>();
    Map<Integer, Map<Integer, NormalLadder>> newLadderSlots = new HashMap<>();

    for (int page = 1; PageUtil.isPageValid(ladders.size(), page, spaces) || page == 1; page++) {
      Inventory inventory = existingInventories.get(page);
      String title =
          PracticeConfig.guis()
              .getGuis()
              .getKitEditor()
              .getLadderSelector()
              .getTitle()
              .replace("%page%", String.valueOf(page));
      if (inventory == null || inventory.getSize() != 6 * 9) {
        inventory = InventoryUtil.createInventory(title, 6);
      } else {
        inventory.clear();
      }
      newGui.put(page, inventory);

      for (int i = 45; i < 54; i++) {
        inventory.setItem(i, GuiManager.fillerItem());
      }
      for (int i = 0; i < 45; i++) {
        inventory.setItem(i, null);
      }

      Map<Integer, NormalLadder> pageSlots = new HashMap<>();
      int startIndex = (page - 1) * spaces;
      int endIndex = Math.min(startIndex + spaces, ladders.size());
      for (int i = startIndex; i < endIndex; i++) {
        int slot = inventory.firstEmpty();
        if (slot == -1 || slot >= 45) {
          break;
        }

        NormalLadder ladder = ladders.get(i);
        inventory.setItem(slot, getLadderItem(ladder));
        pageSlots.put(slot, ladder);
      }
      newLadderSlots.put(page, pageSlots);

      ItemStack left =
          page == 1
              ? GuiManager.fillerItem()
              : BackendUtil.getGuiItem(
                      PracticeConfig.guis()
                          .getGuis()
                          .getKitEditor()
                          .getLadderSelector()
                          .getIcons()
                          .getPageLeft())
                  .replace("%page%", String.valueOf(page - 1))
                  .get();
      inventory.setItem(45, left);

      ItemStack right =
          PageUtil.isPageValid(ladders.size(), page + 1, spaces)
              ? BackendUtil.getGuiItem(
                      PracticeConfig.guis()
                          .getGuis()
                          .getKitEditor()
                          .getLadderSelector()
                          .getIcons()
                          .getPageRight())
                  .replace("%page%", String.valueOf(page + 1))
                  .get()
              : GuiManager.fillerItem();
      inventory.setItem(53, right);
    }

    updatePages(newGui, ladderSlots, newLadderSlots);
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (!profile.getStatus().equals(ProfileStatus.LOBBY)) {
      return;
    }

    Inventory inventory = e.getView().getTopInventory();

    int slot = e.getRawSlot();
    ItemStack currentItem = e.getCurrentItem();

    e.setCancelled(true);

    if (inventory.getSize() <= slot) {
      return;
    }
    if (currentItem == null) {
      return;
    }
    if (currentItem.equals(GuiManager.fillerItem())) {
      return;
    }

    int page = inGuiPlayers.getOrDefault(player, 1);
    if (slot == 45) {
      if (gui.containsKey(page - 1)) {
        open(player, page - 1);
      }
    } else if (slot == 53) {
      if (gui.containsKey(page + 1)) {
        open(player, page + 1);
      }
    } else if (ladderSlots.containsKey(page) && ladderSlots.get(page).containsKey(slot)) {
      NormalLadder ladder = ladderSlots.get(page).get(slot);

      if (!ladder.isEnabled() || !ladder.isEditable()) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getLadder()
                .getKitEditor()
                .getLadderSelector()
                .getNotAvailable()
                .replace("%ladder%", ladder.getDisplayName()));
        update();
        return;
      }

      if (ladder.isFrozen()) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getLadder()
                .getKitEditor()
                .getLadderSelector()
                .getLadderFrozen()
                .replace("%ladder%", ladder.getDisplayName()));
        return;
      }

      new CustomLadderSumGui(profile, ladder).open(player);
    }
  }

  private ItemStack getLadderItem(NormalLadder ladder) {
    ItemStack icon = ladder.getIcon();
    ItemMeta iconMeta = icon.getItemMeta();
    if (iconMeta != null) {
      iconMeta.displayName(
          Common.legacyToComponent(
              PracticeConfig.guis()
                  .getGuis()
                  .getKitEditor()
                  .getLadderSelector()
                  .getIcons()
                  .getName()
                  .replace("%ladder%", ladder.getDisplayName())
                  .replace("%ladderOriginal%", ladder.getName())));
      ItemCreateUtil.hideItemFlags(iconMeta);

      List<String> lore = new ArrayList<>();
      for (String line :
          PracticeConfig.guis().getGuis().getKitEditor().getLadderSelector().getIcons().getLore()) {
        lore.add(
            line.replace("%ladder%", ladder.getDisplayName())
                .replace("%ladderOriginal%", ladder.getName()));
      }
      iconMeta.lore(lore.stream().map(Common::legacyToComponent).toList());
      icon.setItemMeta(iconMeta);
    }
    return icon;
  }
}
