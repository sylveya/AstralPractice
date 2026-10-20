package dev.lokspel.practice.manager.gui.guis.queue.customkit;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.queue.CustomKitQueueManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ChooseQueueTypeGui extends Gui {

  private final Map<UUID, GuiType> backTargets = new HashMap<>();

  public ChooseQueueTypeGui() {
    super(GuiType.Queue_CustomKitChooseType);

    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis().getGuis().getCustomKitQueue().getChooseType().getTitle(),
            getRows()));
    build();
  }

  private int getRows() {
    int rows = PracticeConfig.guis().getGuis().getCustomKitQueue().getChooseType().getSize();
    return Math.clamp(rows <= 0 ? 3 : rows, 1, 6);
  }

  private int hostOwnSlot() {
    return PracticeConfig.guis()
        .getGuis()
        .getCustomKitQueue()
        .getChooseType()
        .getSlots()
        .getHostOwn();
  }

  private int joinOthersSlot() {
    return PracticeConfig.guis()
        .getGuis()
        .getCustomKitQueue()
        .getChooseType()
        .getSlots()
        .getJoinOthers();
  }

  private int backSlot() {
    return PracticeConfig.guis().getGuis().getCustomKitQueue().getChooseType().getSlots().getBack();
  }

  public void openFor(Player player, GuiType backTarget) {
    if (player == null) {
      return;
    }

    if (backTarget != null) {
      backTargets.put(player.getUniqueId(), backTarget);
    }

    open(player);
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    Inventory inventory = this.gui.get(1);
    inventory.clear();

    ItemStack filler =
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getCustomKitQueue()
                    .getChooseType()
                    .getIcons()
                    .getFillerItem())
            .get();
    if (filler == null) {
      filler = ItemCreateUtil.createItem(" ", Material.BLACK_STAINED_GLASS_PANE);
    }

    for (int i = 0; i < inventory.getSize(); i++) {
      inventory.setItem(i, filler);
    }

    ItemStack hostItem =
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getCustomKitQueue()
                    .getChooseType()
                    .getIcons()
                    .getQueueOwnKit())
            .get();
    if (hostItem == null) {
      hostItem = ItemCreateUtil.createItem("<green>Queue Your Own Kit", Material.WRITABLE_BOOK);
    }

    ItemStack joinOthersItem =
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getCustomKitQueue()
                    .getChooseType()
                    .getIcons()
                    .getQueueOthersKits())
            .get();
    if (joinOthersItem == null) {
      joinOthersItem = ItemCreateUtil.createItem("<yellow>Queue Others Kits", Material.BOOK);
    }

    ItemStack backItem =
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getCustomKitQueue()
                    .getChooseType()
                    .getIcons()
                    .getBack())
            .get();
    if (backItem == null) {
      backItem = ItemCreateUtil.createItem("<red>Back", Material.ARROW);
    }

    if (hostOwnSlot() >= 0 && hostOwnSlot() < inventory.getSize()) {
      inventory.setItem(hostOwnSlot(), hostItem);
    }
    if (joinOthersSlot() >= 0 && joinOthersSlot() < inventory.getSize()) {
      inventory.setItem(joinOthersSlot(), joinOthersItem);
    }
    if (backSlot() >= 0 && backSlot() < inventory.getSize()) {
      inventory.setItem(backSlot(), backItem);
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

    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null || profile.getStatus() != ProfileStatus.LOBBY) {
      Common.sendMmMessage(player, PracticeConfig.language().getCantUseCommand());
      player.closeInventory();
      return;
    }

    if (slot == hostOwnSlot()) {
      if (CustomKitQueueManager.getInstance().getHostableKits(profile).isEmpty()) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getQueues().getCustom().getNoSavedKits());
        return;
      }

      if (!player.hasPermission("ap.playerkit.queue.host")) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getQueues().getCustom().getNoPermission());
        return;
      }

      new CustomKitHostSelectorGui(profile).open(player);
      return;
    }

    if (slot == joinOthersSlot()) {
      if (CustomKitQueueManager.getInstance().startJoinSearch(player)) {
        player.closeInventory();
      }
      return;
    }

    if (slot == backSlot()) {
      GuiType backTarget = backTargets.getOrDefault(player.getUniqueId(), GuiType.Queue_Unranked);
      Gui backGui = GuiManager.getInstance().searchGui(backTarget);
      if (backGui != null) {
        backGui.open(player);
      }
    }
  }

  @Override
  public void handleCloseEvent(InventoryCloseEvent e) {
    if (e.getPlayer() instanceof Player player) {
      backTargets.remove(player.getUniqueId());
    }
  }
}
