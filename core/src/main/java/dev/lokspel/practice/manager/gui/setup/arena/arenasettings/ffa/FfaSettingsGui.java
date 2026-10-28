package dev.lokspel.practice.manager.gui.setup.arena.arenasettings.ffa;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class FfaSettingsGui extends Gui {

  private static ItemStack goBackItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getGoBack())
        .get();
  }

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getFiller())
        .get();
  }

  private static ItemStack buildEnabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getBuild()
                .getEnabled())
        .get();
  }

  private static ItemStack buildDisabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getBuild()
                .getDisabled())
        .get();
  }

  private static ItemStack tntEnabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getMapBlowable()
                .getEnabled())
        .get();
  }

  private static ItemStack tntDisabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getMapBlowable()
                .getDisabled())
        .get();
  }

  private static ItemStack rekitEnabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getReKitAfterKill()
                .getEnabled())
        .get();
  }

  private static ItemStack rekitDisabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getReKitAfterKill()
                .getDisabled())
        .get();
  }

  private static ItemStack healthBelowNameEnabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getHealthBelowName()
                .getEnabled())
        .get();
  }

  private static ItemStack healthBelowNameDisabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getHealthBelowName()
                .getDisabled())
        .get();
  }

  private static ItemStack healthResetEnabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getHealthResetOnKill()
                .getEnabled())
        .get();
  }

  private static ItemStack healthResetDisabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getHealthResetOnKill()
                .getDisabled())
        .get();
  }

  private static ItemStack lobbydeathEnabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getLobbyAfterDeath()
                .getEnabled())
        .get();
  }

  private static ItemStack lobbydeathDisabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getLobbyAfterDeath()
                .getDisabled())
        .get();
  }

  private static ItemStack allowDropEnabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getAllowDropItems()
                .getEnabled())
        .get();
  }

  private static ItemStack allowDropDisabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getAllowDropItems()
                .getDisabled())
        .get();
  }

  private static ItemStack locatorBarEnabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
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
                .getFfaArena()
                .getSettings()
                .getIcons()
                .getLocatorBar()
                .getDisabled())
        .get();
  }

  private final FfaArena ffaArena;
  private final ArenaMainGui arenaMainGui;

  public FfaSettingsGui(FfaArena ffaArena, ArenaMainGui arenaMainGui) {
    super(GuiType.FFA_Arena_Settings);

    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getSettings()
                .getTitle()
                .replace("%arenaName%", ffaArena.getName()),
            4));
    this.ffaArena = ffaArena;
    this.arenaMainGui = arenaMainGui;

    this.build();
  }

  @Override
  public void build() {
    Inventory inventory = this.gui.get(1);

    inventory.setItem(27, goBackItem());
    for (int i : new int[] {28, 29, 30, 31, 32, 33, 34, 35}) {
      inventory.setItem(i, fillerItem());
    }

    this.update();
  }

  @Override
  public void update() {
    Inventory inventory = this.gui.get(1);

    if (ffaArena.isBuild()) {
      inventory.setItem(10, buildEnabledItem());
    } else {
      inventory.setItem(10, buildDisabledItem());
    }

    if (ffaArena.isMapBlowable()) {
      inventory.setItem(11, tntEnabledItem());
    } else {
      inventory.setItem(11, tntDisabledItem());
    }

    if (ffaArena.isLocatorBar()) {
      inventory.setItem(13, locatorBarEnabledItem());
    } else {
      inventory.setItem(13, locatorBarDisabledItem());
    }

    if (ffaArena.isReKitAfterKill()) {
      inventory.setItem(12, rekitEnabledItem());
    } else {
      inventory.setItem(12, rekitDisabledItem());
    }

    if (ffaArena.isLobbyAfterDeath()) {
      inventory.setItem(14, lobbydeathEnabledItem());
    } else {
      inventory.setItem(14, lobbydeathDisabledItem());
    }

    if (ffaArena.isHealthBelowName()) {
      inventory.setItem(15, healthBelowNameEnabledItem());
    } else {
      inventory.setItem(15, healthBelowNameDisabledItem());
    }

    if (ffaArena.isHealthResetOnKill()) {
      inventory.setItem(16, healthResetEnabledItem());
    } else {
      inventory.setItem(16, healthResetDisabledItem());
    }

    if (ffaArena.isAllowDropItems()) {
      inventory.setItem(17, allowDropEnabledItem());
    } else {
      inventory.setItem(17, allowDropDisabledItem());
    }

    this.updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();

    e.setCancelled(true);

    try {
      switch (e.getRawSlot()) {
        case 10:
          ffaArena.setBuild(!ffaArena.isBuild());
          this.update();
          break;
        case 11:
          ffaArena.setMapBlowable(!ffaArena.isMapBlowable());
          this.update();
          break;
        case 12:
          ffaArena.setReKitAfterKill(!ffaArena.isReKitAfterKill());
          this.update();
          break;
        case 13:
          ffaArena.setLocatorBar(!ffaArena.isLocatorBar());
          ffaArena.setData();
          this.update();
          break;
        case 14:
          ffaArena.setLobbyAfterDeath(!ffaArena.isLobbyAfterDeath());
          this.update();
          break;
        case 16:
          ffaArena.setHealthResetOnKill(!ffaArena.isHealthResetOnKill());
          this.update();
          break;
        case 15:
          ffaArena.setHealthBelowName(!ffaArena.isHealthBelowName());
          this.update();
          break;
        case 17:
          ffaArena.setAllowDropItems(!ffaArena.isAllowDropItems());
          this.update();
          break;
        case 27:
          arenaMainGui.open(player);
          break;

        default:
          break;
      }

    } catch (IllegalStateException exception) {
      Common.sendMmMessage(player, "<red>" + exception.getMessage());
    }
  }
}
