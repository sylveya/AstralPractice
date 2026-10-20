package dev.lokspel.practice.manager.gui.guis;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.enums.EventType;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.kit.KitData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class BracketsKitSelectorGui extends Gui {

  private static final int[] CUSTOM_SLOTS = new int[] {10, 11, 12, 13, 14, 15, 16};

  private final Player host;
  private final Profile hostProfile;

  private final Map<Integer, KitData> selectableKits = new HashMap<>();

  public BracketsKitSelectorGui(Player host) {
    super(GuiType.Event_BracketsKitSelector);
    this.host = host;
    this.hostProfile = ProfileManager.getInstance().getProfile(host);

    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis().getGuis().getBracketsKitSelector().getTitle(), 3));
    this.build();
  }

  @Override
  public void build() {
    this.update();
  }

  @Override
  public void update() {
    Inventory inventory = this.gui.get(1);
    inventory.clear();
    this.selectableKits.clear();

    for (int i = 0; i < inventory.getSize(); i++) {
      inventory.setItem(i, GuiManager.fillerItem());
    }

    GuiItem defaultKitIcon =
        BackendUtil.getGuiItem(
                PracticeConfig.guis().getGuis().getBracketsKitSelector().getIcons().getDefaultKit())
            .cloneItem();
    inventory.setItem(4, defaultKitIcon.get());
    this.selectableKits.put(4, null);

    List<CustomLadder> availableCustomKits = getAvailableCustomKits();
    int customIndex = 0;
    for (CustomLadder customLadder : availableCustomKits) {
      if (customIndex >= CUSTOM_SLOTS.length) {
        break;
      }

      int slot = CUSTOM_SLOTS[customIndex++];
      GuiItem customKitItem =
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getBracketsKitSelector()
                      .getIcons()
                      .getCustomKit())
              .cloneItem()
              .replace("%kitName%", customLadder.getDisplayName());

      if (customLadder.getIcon() != null) {
        customKitItem.setBaseItem(customLadder.getIcon());
      }

      inventory.setItem(slot, customKitItem.get());
      this.selectableKits.put(slot, new KitData(customLadder.getKitData()));
    }

    if (availableCustomKits.isEmpty()) {
      inventory.setItem(
          13,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getBracketsKitSelector()
                      .getIcons()
                      .getNoCustomKit())
              .get());
    }
  }

  private List<CustomLadder> getAvailableCustomKits() {
    List<CustomLadder> available = new ArrayList<>();

    if (this.hostProfile == null) {
      return available;
    }

    for (CustomLadder customLadder : this.hostProfile.getCustomLadders()) {
      if (customLadder != null && customLadder.getKitData().isSet()) {
        available.add(customLadder);
      }
    }

    return available;
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    e.setCancelled(true);

    Player player = (Player) e.getWhoClicked();
    if (!player.equals(this.host)) {
      return;
    }

    int slot = e.getRawSlot();
    if (e.getView().getTopInventory().getSize() <= slot) {
      return;
    }

    if (!this.selectableKits.containsKey(slot)) {
      return;
    }

    KitData selectedKitData = this.selectableKits.get(slot);
    EventManager.getInstance().startEvent(this.host, EventType.BRACKETS, selectedKitData);
    this.host.closeInventory();
  }
}
