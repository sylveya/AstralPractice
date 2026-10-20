package dev.lokspel.practice.manager.gui.guis.queue.customkit;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.queue.CustomKitQueueManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomKitHostSelectorGui extends Gui {

  private final Profile profile;
  private final Map<Integer, CustomLadder> slotToKit = new HashMap<>();

  public CustomKitHostSelectorGui(Profile profile) {
    super(GuiType.Queue_CustomKitHostSelector);
    this.profile = profile;

    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis().getGuis().getCustomKitQueue().getHostSelector().getTitle(),
            getRows()));
    build();
  }

  private int getRows() {
    int rows = PracticeConfig.guis().getGuis().getCustomKitQueue().getHostSelector().getSize();
    return Math.clamp(rows <= 0 ? 3 : rows, 1, 6);
  }

  private int backSlot() {
    return PracticeConfig.guis()
        .getGuis()
        .getCustomKitQueue()
        .getHostSelector()
        .getSlots()
        .getBack();
  }

  private int noKitsSlot() {
    return PracticeConfig.guis()
        .getGuis()
        .getCustomKitQueue()
        .getHostSelector()
        .getSlots()
        .getNoKits();
  }

  private List<Integer> kitSlots() {
    List<Integer> slots =
        PracticeConfig.guis()
            .getGuis()
            .getCustomKitQueue()
            .getHostSelector()
            .getSlots()
            .getKitSlots();
    if (slots == null || slots.isEmpty()) {
      return List.of(10, 11, 12, 13, 14, 15, 16);
    }
    return slots;
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);
    inventory.clear();
    slotToKit.clear();

    ItemStack filler =
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getCustomKitQueue()
                    .getHostSelector()
                    .getIcons()
                    .getFillerItem())
            .get();
    if (filler == null) {
      filler = ItemCreateUtil.createItem(" ", Material.BLACK_STAINED_GLASS_PANE);
    }

    for (int i = 0; i < inventory.getSize(); i++) {
      inventory.setItem(i, filler);
    }

    if (!CustomKitQueueManager.getInstance().isCustomKitQueueEnabled()) {
      updatePlayers();
      return;
    }

    ItemStack backItem =
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getCustomKitQueue()
                    .getHostSelector()
                    .getIcons()
                    .getBack())
            .get();
    if (backItem == null) {
      backItem = ItemCreateUtil.createItem("<red>Back", Material.ARROW);
    }
    if (backSlot() >= 0 && backSlot() < inventory.getSize()) {
      inventory.setItem(backSlot(), backItem);
    }

    List<CustomLadder> hostableKits =
        new ArrayList<>(CustomKitQueueManager.getInstance().getHostableKits(profile));
    if (hostableKits.isEmpty()) {
      ItemStack noKitsItem =
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getCustomKitQueue()
                      .getHostSelector()
                      .getIcons()
                      .getNoKits())
              .get();
      if (noKitsItem == null) {
        noKitsItem = ItemCreateUtil.createItem("<red>No Saved Custom Kits", Material.BARRIER);
      }

      if (noKitsSlot() >= 0 && noKitsSlot() < inventory.getSize()) {
        inventory.setItem(noKitsSlot(), noKitsItem);
      }
      updatePlayers();
      return;
    }

    List<Integer> kitSlots = kitSlots();
    for (int i = 0; i < hostableKits.size() && i < kitSlots.size(); i++) {
      CustomLadder customLadder = hostableKits.get(i);

      ItemStack icon = customLadder.getIcon();
      if (icon == null || icon.getType() == Material.AIR) {
        icon =
            ItemCreateUtil.createItem(
                "<yellow>" + customLadder.getDisplayName(), Material.WRITABLE_BOOK);
      } else {
        icon = icon.clone();
        ItemMeta itemMeta = icon.getItemMeta();
        if (itemMeta != null) {
          itemMeta.displayName(
              Common.legacyToComponent("<yellow>" + customLadder.getDisplayName()));
          icon.setItemMeta(itemMeta);
        }
      }

      int targetSlot = kitSlots.get(i);
      if (targetSlot < 0 || targetSlot >= inventory.getSize()) {
        continue;
      }
      inventory.setItem(targetSlot, icon);
      slotToKit.put(targetSlot, customLadder);
    }

    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    e.setCancelled(true);

    Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();

    if (slot >= e.getView().getTopInventory().getSize()) {
      return;
    }

    Profile currentProfile = ProfileManager.getInstance().getProfile(player);
    if (currentProfile == null || currentProfile.getStatus() != ProfileStatus.LOBBY) {
      Common.sendMmMessage(player, PracticeConfig.language().getCantUseCommand());
      player.closeInventory();
      return;
    }

    if (slot == backSlot()) {
      Gui backGui = GuiManager.getInstance().searchGui(GuiType.Queue_CustomKitChooseType);
      if (backGui != null) {
        backGui.open(player);
      }
      return;
    }

    if (!slotToKit.containsKey(slot)) {
      return;
    }

    CustomLadder selectedKit = slotToKit.get(slot);
    if (CustomKitQueueManager.getInstance().hostQueue(player, selectedKit)) {
      player.closeInventory();
    }
  }
}
