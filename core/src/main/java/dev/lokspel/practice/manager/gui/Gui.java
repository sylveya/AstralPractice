package dev.lokspel.practice.manager.gui;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.gui.confirmgui.ConfirmGui;
import dev.lokspel.practice.manager.gui.confirmgui.ConfirmGuiType;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

@Getter
public abstract class Gui {

  protected final GuiType type;

  protected final Map<Integer, Inventory> gui = new HashMap<>();

  protected final Map<Player, Integer> inGuiPlayers = new HashMap<>();
  protected final Map<Player, ConfirmGui> inConfirmationGui = new HashMap<>();

  public Gui(GuiType type) {
    this.type = type;
  }

  public abstract void build();

  public abstract void update();

  /**
   * Update with optional cache bypass. For cacheable GUIs, this checks cache validity first.
   *
   * @param forceRefresh If true, bypass cache and rebuild
   */
  public boolean update(boolean forceRefresh) {
    // If this GUI type should be cached and we're not forcing refresh
    if (!forceRefresh && GuiCache.shouldCache(type)) {
      // Check if we have valid cached data
      if (GuiCache.isCacheValid(type)) {
        // Load from cache instead of rebuilding
        Map<Integer, Inventory> cached = GuiCache.getCached(type);
        if (cached != null) {
          gui.clear();
          gui.putAll(cached);
          updatePlayers();
          return false;
        }
      }
    }

    // Either not cacheable, cache expired, or force refresh requested
    update();

    // After update, cache if this is a cacheable GUI type
    if (GuiCache.shouldCache(type)) {
      GuiCache.putCache(type, gui);
    }

    return true;
  }

  public void open(Player player, int page) {
    inConfirmationGui.remove(player);

    if (gui.containsKey(page)) {
      player.openInventory(gui.get(page));

      Bukkit.getScheduler()
          .runTaskLater(
              AstralPractice.getInstance(),
              () -> {
                inGuiPlayers.put(player, page);
                GuiManager.getInstance().getOpenGui().put(player, this);
              },
              2L);
    } else if (page > 1) {
      open(player, page - 1);
    } else {
      player.closeInventory();
    }
  }

  public void open(Player player) {
    open(player, 1);
  }

  /** Applies rebuilt pages and slot mappings, moving viewers off removed pages. */
  protected <T> void updatePages(
      Map<Integer, Inventory> newPages,
      Map<Integer, Map<Integer, T>> slots,
      Map<Integer, Map<Integer, T>> newSlots) {
    for (Map.Entry<Integer, Inventory> entry : new LinkedHashMap<>(gui).entrySet()) {
      if (newPages.containsKey(entry.getKey())) {
        continue;
      }

      gui.remove(entry.getKey());
      // Opening an inventory can synchronously remove viewers via its close event.
      for (Map.Entry<Player, Integer> viewer : new HashMap<>(inGuiPlayers).entrySet()) {
        if (viewer.getValue().equals(entry.getKey())) {
          open(viewer.getKey(), entry.getKey() - 1);
        }
      }
    }

    gui.putAll(newPages);
    slots.clear();
    slots.putAll(newSlots);

    updatePlayers();
  }

  protected void updatePlayers() {
    if (inGuiPlayers.isEmpty()) {
      return;
    }

    for (Player player : inGuiPlayers.keySet()) {
      if (player != null && player.isOnline() && inGuiPlayers.get(player) != -1) {
        player.updateInventory();
      }
    }
  }

  public void close(Player player) {
    inGuiPlayers.remove(player);
    inConfirmationGui.remove(player);
    GuiManager.getInstance().getOpenGui().remove(player);
  }

  public abstract void handleClickEvent(InventoryClickEvent e);

  public void handleCloseEvent(InventoryCloseEvent e) {}

  public void handleDragEvent(InventoryDragEvent e) {}

  public void openConfirmGui(
      Player player, ConfirmGuiType confirmGuiType, Gui backToConfirm, Gui backToCancel) {
    ConfirmGui confirmGui = new ConfirmGui(confirmGuiType, backToConfirm, backToCancel);
    confirmGui.openInventory(player);

    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> {
              inGuiPlayers.put(player, -1);
              inConfirmationGui.put(player, confirmGui);
              GuiManager.getInstance().getOpenGui().put(player, this);
            },
            2L);
  }

  public void handleConfirmGuiClick(InventoryClickEvent e) {
    e.setCancelled(true);

    Player player = (Player) e.getWhoClicked();

    if (!inConfirmationGui.containsKey(player)) {
      return;
    }
    ConfirmGui confirmGui = inConfirmationGui.get(player);

    int slot = e.getRawSlot();
    Inventory inventory = e.getView().getTopInventory();

    if (inventory == null) {
      return;
    }
    if (inventory.getSize() <= slot) {
      return;
    }

    if (slot != 4) {
      if (confirmGui.getBackToCancel() != null) {
        confirmGui.getBackToCancel().open(player);
      } else {
        player.closeInventory();
      }
    } else {
      handleConfirm(player, confirmGui.getType());

      if (confirmGui.getBackToConfirm() != null) {
        confirmGui.getBackToConfirm().open(player);
      } else {
        player.closeInventory();
      }
    }
  }

  public void handleConfirm(Player player, ConfirmGuiType confirmGuiType) {}
}
