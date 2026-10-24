package dev.lokspel.practice.manager.gui.setup.arena.arenasettings.normal;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.setup.ArenaSetupManager;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.confirmgui.ConfirmGuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaSetupUtil;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaSummaryGui;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
public class ArenaMainGui extends Gui {

  private static ItemStack backToItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaMain()
                .getIcons()
                .getGoBack())
        .get();
  }

  private static ItemStack ladderItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaMain()
                .getIcons()
                .getLadder())
        .get();
  }

  private static ItemStack settingsItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaMain()
                .getIcons()
                .getSettings())
        .get();
  }

  private static ItemStack deleteItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaMain()
                .getIcons()
                .getDelete())
        .get();
  }

  private final Arena arena;

  public ArenaMainGui(Arena arena) {
    super(GuiType.Arena_Main);
    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaMain()
                .getTitle()
                .replace("%arenaName%", arena.getName()),
            4));
    this.arena = arena;

    build();
  }

  @Override
  public void build() {
    Inventory inventory = gui.get(1);

    inventory.setItem(27, backToItem());
    for (int i : new int[] {28, 29, 30, 31, 32, 33}) {
      inventory.setItem(i, GuiManager.fillerItem());
    }

    inventory.setItem(13, settingsItem());
    inventory.setItem(15, ladderItem());
    inventory.setItem(35, deleteItem());

    update();
  }

  @Override
  public void update() {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            AstralPractice.getInstance(),
            () -> {
              Inventory inventory = gui.get(1);

              inventory.setItem(10, ArenaSetupUtil.getNameItem(arena));
              inventory.setItem(11, ArenaSetupUtil.getStatusItem(arena));
              inventory.setItem(16, ArenaSetupUtil.getLocationItem(arena));
              if (arena.isBuild()) {
                inventory.setItem(14, ArenaSetupUtil.getArenaCopiesItem(arena));
              }
              inventory.setItem(34, ArenaSetupUtil.getFreezeItem(arena));

              updatePlayers();
            });
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    Inventory inventory = e.getView().getTopInventory();

    int slot = e.getRawSlot();
    ItemStack currentItem = e.getCurrentItem();
    ClickType clickType = e.getClick();
    e.setCancelled(true);

    if (inventory.getSize() <= slot || currentItem == null) {
      return;
    }

    switch (slot) {
      case 27:
        ArenaSummaryGui arenaSummaryGui =
            (ArenaSummaryGui) GuiManager.getInstance().searchGui(GuiType.Arena_Summary);
        arenaSummaryGui.open(player, arenaSummaryGui.getBackToPage().getOrDefault(player, 1));
        arenaSummaryGui.getBackToPage().remove(player);
        break;
      case 10:
        if (clickType.isLeftClick()) {
          player.performCommand("arena info " + arena.getName());
        } else if (clickType.isRightClick()) {
          if (!ArenaSetupManager.getInstance().startSetup(player, arena)) {
            Common.sendMmMessage(player, "<red>You can't edit an enabled arena.");
          }
        }
        break;
      case 11:
        ArenaUtil.changeStatus(player, arena);
        break;
      case 13:
        ArenaGuiSetupManager.getInstance()
            .getArenaSetupGuis()
            .get(arena)
            .get(GuiType.Arena_Settings)
            .open(player);
        break;
      case 14:
        if (arena.isBuild()) {
          ArenaGuiSetupManager.getInstance()
              .getArenaSetupGuis()
              .get(arena)
              .get(GuiType.Arena_Copy)
              .open(player, 1);
        }
        break;
      case 15:
        ArenaGuiSetupManager.getInstance()
            .getArenaSetupGuis()
            .get(arena)
            .get(GuiType.Arena_Ladders_Type)
            .open(player, 1);
        break;
      case 34:
        player.performCommand("arena freeze " + arena.getName());
        break;
      case 35:
        if (arena.isEnabled()) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language().getCommand().getSetup().getArena().getCantDeleteEnabled());
          return;
        }
        openConfirmGui(
            player,
            ConfirmGuiType.ARENA_DELETE,
            GuiManager.getInstance().searchGui(GuiType.Arena_Summary),
            this);
        break;

      default:
        break;
    }
  }

  @Override
  public void handleConfirm(Player player, ConfirmGuiType confirmGuiType) {
    if (confirmGuiType.equals(ConfirmGuiType.ARENA_DELETE)) {
      player.performCommand("arena delete " + arena.getName());
    }
  }
}
