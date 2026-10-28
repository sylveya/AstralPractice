package dev.lokspel.practice.manager.gui.setup.arena.arenasettings.normal;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ArenaSettingsGui extends Gui {

  private static ItemStack goBackItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaSettings()
                .getIcons()
                .getGoBack())
        .get();
  }

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaSettings()
                .getIcons()
                .getFiller())
        .get();
  }

  private static ItemStack customKitsEnabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaSettings()
                .getIcons()
                .getCustomKits()
                .getEnabled())
        .get();
  }

  private static ItemStack customKitsDisabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaSettings()
                .getIcons()
                .getCustomKits()
                .getDisabled())
        .get();
  }

  private static ItemStack locatorBarEnabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaSettings()
                .getIcons()
                .getLocatorBar()
                .getEnabled())
        .get();
  }

  private static ItemStack locatorBarDisabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaSettings()
                .getIcons()
                .getLocatorBar()
                .getDisabled())
        .get();
  }

  private final Arena arena;

  public ArenaSettingsGui(Arena arena) {
    super(GuiType.Arena_Settings);

    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaSettings()
                .getTitle()
                .replace("%arenaName%", arena.getName()),
            4));
    this.arena = arena;

    build();
  }

  private ItemStack sideBuildLimitItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaSettings()
                .getIcons()
                .getSideBuildLimit())
        .replace("%sideBuildLimit%", String.valueOf(arena.getSideBuildLimit()))
        .get();
  }

  private ItemStack portalProtectionItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaSettings()
                .getIcons()
                .getPortalProtection())
        .replace("%portalProtection%", String.valueOf(arena.getPortalProtectionValue()))
        .get();
  }

  @Override
  public void build() {
    Inventory inventory = gui.get(1);

    inventory.setItem(27, goBackItem());
    for (int i : new int[] {28, 29, 30, 31, 32, 33, 34, 35}) {
      inventory.setItem(i, fillerItem());
    }

    update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);

    if (arena.isBuild()) {
      inventory.setItem(
          10, arena.isLocatorBar() ? locatorBarEnabledItem() : locatorBarDisabledItem());
      inventory.setItem(
          12, arena.isAllowCustomKitOnMap() ? customKitsEnabledItem() : customKitsDisabledItem());
      inventory.setItem(14, sideBuildLimitItem());
      inventory.setItem(16, portalProtectionItem());
    } else {
      inventory.setItem(
          12, arena.isLocatorBar() ? locatorBarEnabledItem() : locatorBarDisabledItem());
      inventory.setItem(
          14, arena.isAllowCustomKitOnMap() ? customKitsEnabledItem() : customKitsDisabledItem());
    }

    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();
    final ClickType clickType = e.getClick();
    Inventory inventory = e.getView().getTopInventory();
    e.setCancelled(true);

    if (inventory.getSize() <= slot) {
      return;
    }

    if (slot == 27) {
      ArenaGuiSetupManager.getInstance()
          .getArenaSetupGuis()
          .get(arena)
          .get(GuiType.Arena_Main)
          .open(player);
      return;
    }

    if (arena.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getSetup()
              .getCantEdit());
      return;
    }

    if (arena.isBuild()) {
      switch (slot) {
        case 10 -> handleLocatorBarClick();
        case 12 -> handleCustomKitsClick();
        case 14 -> handleSideBuildLimitClick(clickType);
        case 16 -> handlePortalProtectionClick(clickType);
        default -> {}
      }
    } else {
      if (slot == 12) {
        handleLocatorBarClick();
      } else if (slot == 14) {
        handleCustomKitsClick();
      }
    }
  }

  private void handleLocatorBarClick() {
    arena.setLocatorBar(!arena.isLocatorBar());
    arena.setData();
    update();
  }

  private void handleCustomKitsClick() {
    arena.setAllowCustomKitOnMap(!arena.isAllowCustomKitOnMap());
    arena.setData();
    update();
  }

  private void handleSideBuildLimitClick(ClickType clickType) {
    int current = arena.getSideBuildLimit();
    if (clickType.isLeftClick()) {
      current = Math.min(10, current + 1);
    } else if (clickType.isRightClick()) {
      current = Math.max(0, current - 1);
    }

    arena.setSideBuildLimit(current);
    arena.setData();
    ArenaGuiSetupManager.getInstance()
        .getArenaSetupGuis()
        .get(arena)
        .get(GuiType.Arena_Main)
        .update();
    update();
  }

  private void handlePortalProtectionClick(ClickType clickType) {
    int current = arena.getPortalProtectionValue();
    if (clickType.isLeftClick()) {
      current = Math.min(20, current + 1);
    } else if (clickType.isRightClick()) {
      current = Math.max(0, current - 1);
    }

    arena.setPortalProtectionValue(current);
    arena.setPortalProtection(current > 0);
    arena.setData();
    ArenaGuiSetupManager.getInstance()
        .getArenaSetupGuis()
        .get(arena)
        .get(GuiType.Arena_Main)
        .update();
    update();
  }
}
