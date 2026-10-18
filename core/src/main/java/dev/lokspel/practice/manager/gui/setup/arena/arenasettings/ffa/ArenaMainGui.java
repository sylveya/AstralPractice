package dev.lokspel.practice.manager.gui.setup.arena.arenasettings.ffa;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.arena.setup.ArenaSetupManager;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.confirmgui.ConfirmGuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ArenaMainGui extends Gui {

  private static ItemStack backToItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getMain()
                .getIcons()
                .getGoBack())
        .get();
  }

  private static ItemStack deleteItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getMain()
                .getIcons()
                .getDelete())
        .get();
  }

  private static ItemStack statusEnabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getMain()
                .getIcons()
                .getStatus()
                .getEnabled())
        .get();
  }

  private static ItemStack statusDisabledItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getMain()
                .getIcons()
                .getStatus()
                .getDisabled())
        .get();
  }

  private static ItemStack ladderItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getMain()
                .getIcons()
                .getLadder())
        .get();
  }

  private static ItemStack settingsItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getMain()
                .getIcons()
                .getSettings())
        .get();
  }

  private static ItemStack ffaOpenItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getMain()
                .getIcons()
                .getOpenStatus()
                .getOpen())
        .get();
  }

  private static ItemStack ffaCloseItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getMain()
                .getIcons()
                .getOpenStatus()
                .getClose())
        .get();
  }

  private static GuiItem nameItem() {
    return BackendUtil.getGuiItem(
        PracticeConfig.guis()
            .getGuis()
            .getSetup()
            .getFfaArena()
            .getMain()
            .getIcons()
            .getArenaName());
  }

  private static GuiItem locationItem() {
    return BackendUtil.getGuiItem(
        PracticeConfig.guis()
            .getGuis()
            .getSetup()
            .getFfaArena()
            .getMain()
            .getIcons()
            .getLocation());
  }

  private final FfaArena ffaArena;
  private final FfaSettingsGui ffaSettingsGui;

  public ArenaMainGui(FfaArena ffaArena) {
    super(GuiType.Arena_Main);

    this.ffaArena = ffaArena;
    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getFfaArena()
                .getMain()
                .getTitle()
                .replace("%arenaName%", ffaArena.getName()),
            4));
    this.ffaSettingsGui = new FfaSettingsGui(ffaArena, this);

    this.build();
  }

  @Override
  public void build() {
    Inventory inventory = gui.get(1);

    for (int i : new int[] {28, 29, 30, 32, 33, 34}) {
      inventory.setItem(i, GuiManager.fillerItem());
    }

    inventory.setItem(14, settingsItem());
    inventory.setItem(15, ladderItem());
    inventory.setItem(27, backToItem());
    inventory.setItem(35, deleteItem());

    update();

    this.update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);

    inventory.setItem(
        10,
        nameItem()
            .cloneItem()
            .replace("%arenaDisplayName%", ffaArena.getDisplayName())
            .replace("%arenaName%", ffaArena.getName())
            .replace("%arenaType%", ffaArena.getType().toString())
            .setMaterial(ffaArena.getIcon() != null ? ffaArena.getIcon().getType() : null)
            .setDamage(
                ffaArena.getIcon() != null && Common.getItemDamage(ffaArena.getIcon()) != 0
                    ? Common.getItemDamage(ffaArena.getIcon())
                    : -1)
            .get());

    inventory.setItem(11, ffaArena.isEnabled() ? statusEnabledItem() : statusDisabledItem());

    inventory.setItem(15, ladderItem());

    inventory.setItem(
        16,
        locationItem()
            .cloneItem()
            .replace("%arenaName%", ffaArena.getName())
            .replace("%arenaDisplayName%", ffaArena.getDisplayName())
            .replace(
                "%corner1%", Common.mmToNormal(ArenaUtil.convertLocation(ffaArena.getCorner1())))
            .replace(
                "%corner2%", Common.mmToNormal(ArenaUtil.convertLocation(ffaArena.getCorner2())))
            .replace(
                "%spectatorPosition%",
                Common.mmToNormal(ArenaUtil.convertLocation(ffaArena.getSpectatorPosition())))
            .replace("%ffa_pos_num%", String.valueOf(ffaArena.getFfaPositions().size()))
            .get());

    inventory.setItem(31, ffaArena.getFfa().isOpen() ? ffaOpenItem() : ffaCloseItem());

    this.updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    e.setCancelled(true);

    Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();
    ClickType clickType = e.getClick();
    Inventory inventory = e.getClickedInventory();

    if (inventory == null) {
      return;
    }
    if (inventory.getSize() <= slot) {
      return;
    }

    switch (slot) {
      case 10:
        if (clickType.isLeftClick()) {
          player.performCommand("arena info " + ffaArena.getName());
        } else if (clickType.isRightClick()) {
          if (!ArenaSetupManager.getInstance().startSetup(player, ffaArena)) {
            Common.sendMmMessage(player, "<red>You can't edit an enabled arena.");
          }
        }
        break;
      case 11:
        if (ffaArena.isEnabled()) {
          Ffa ffa = ffaArena.getFfa();

          if (ffa != null && ffa.isOpen()) {
            openConfirmGui(player, ConfirmGuiType.FFA_ARENA_DISABLE, this, this);
          } else {
            ArenaUtil.changeStatus(player, ffaArena);
          }
        } else {
          ArenaUtil.changeStatus(player, ffaArena);
        }
        break;
      case 14:
        this.ffaSettingsGui.open(player);
        break;
      case 15:
        ArenaGuiSetupManager.getInstance()
            .getArenaSetupGuis()
            .get(ffaArena)
            .get(GuiType.Arena_Ladders_Single)
            .open(player);
        break;
      case 27:
        GuiManager.getInstance().searchGui(GuiType.Arena_Summary).open(player);
        break;
      case 31:
        if (!ffaArena.isEnabled() && ffaArena.isReadyToEnable()) {
          ArenaUtil.changeStatus(player, ffaArena);
        }

        if (ffaArena.isEnabled()) {
          Ffa ffa = ffaArena.getFfa();

          if (ffa.isOpen()) {
            openConfirmGui(player, ConfirmGuiType.FFA_ARENA_CLOSE, this, this);
          } else {
            ffa.open();
            this.update();
          }
        }
        break;
      case 35:
        if (ffaArena.isEnabled()) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language().getCommand().getSetup().getArena().getCantDeleteEnabled());
          return;
        }
        openConfirmGui(
            player,
            ConfirmGuiType.FFA_ARENA_DELETE,
            GuiManager.getInstance().searchGui(GuiType.Arena_Summary),
            this);
        break;

      default:
        break;
    }
  }

  @Override
  public void handleConfirm(Player player, ConfirmGuiType confirmGuiType) {
    switch (confirmGuiType) {
      case FFA_ARENA_DISABLE:
        ArenaUtil.changeStatus(player, ffaArena);
        break;
      case FFA_ARENA_CLOSE:
        ffaArena.getFfa().close("");
        this.update();
        break;
      case FFA_ARENA_DELETE:
        player.performCommand("arena delete " + ffaArena.getName());
        break;

      default:
        break;
    }
  }
}
