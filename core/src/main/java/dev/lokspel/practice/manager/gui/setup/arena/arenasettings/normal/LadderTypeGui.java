package dev.lokspel.practice.manager.gui.setup.arena.arenasettings.normal;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LadderTypeGui extends Gui {

  private final Map<Integer, LadderType> ladderTypeSlots = new HashMap<>();
  private int customKitSlot;
  private final Arena arena;

  public LadderTypeGui(Arena arena) {
    super(GuiType.Arena_Ladders_Type);

    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaLaddersType()
                .getTitle()
                .replace("%arenaName%", arena.getName()),
            3));
    this.arena = arena;

    build();
  }

  @Override
  public void build() {
    Inventory inventory = gui.get(1);

    // Set filler items
    for (int i : new int[] {19, 20, 21, 22, 23, 24, 25}) {
      inventory.setItem(i, GuiManager.fillerItem());
    }

    // Navigation item
    inventory.setItem(
        18,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getSetup()
                    .getArena()
                    .getArenaLaddersType()
                    .getIcons()
                    .getBackTo())
            .get());
    inventory.setItem(
        26,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getSetup()
                    .getArena()
                    .getArenaLaddersType()
                    .getIcons()
                    .getGoToLadders())
            .get());

    update();
  }

  @Override
  public void update() {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            AstralPractice.getInstance(),
            () -> {
              Inventory inventory = gui.get(1);
              ladderTypeSlots.clear();

              for (int i = 0; i < 18; i++) {
                inventory.setItem(i, null);
              }

              for (LadderType ladderType : LadderType.values()) {
                GuiItem item;
                if (arena.getAssignedLadderTypes().contains(ladderType)) {
                  item =
                      BackendUtil.getGuiItem(
                          PracticeConfig.guis()
                              .getGuis()
                              .getSetup()
                              .getArena()
                              .getArenaLaddersType()
                              .getIcons()
                              .getLadderTypeIcons()
                              .getEnabled());
                  item.setGlowing(true);
                } else {
                  item =
                      BackendUtil.getGuiItem(
                          PracticeConfig.guis()
                              .getGuis()
                              .getSetup()
                              .getArena()
                              .getArenaLaddersType()
                              .getIcons()
                              .getLadderTypeIcons()
                              .getDisabled());
                  item.setGlowing(false);
                }

                if (item.getMaterial() == null) {
                  item.setMaterial(ladderType.getIcon());
                  item.setDamage(0);
                }

                item.replace("%ladderTypeName%", ladderType.getName());

                int slot = inventory.firstEmpty();
                inventory.setItem(slot, item.get());
                ladderTypeSlots.put(slot, ladderType);
              }

              int slot = inventory.firstEmpty();
              inventory.setItem(
                  slot,
                  BackendUtil.getGuiItem(
                          PracticeConfig.guis()
                              .getGuis()
                              .getSetup()
                              .getArena()
                              .getArenaLaddersType()
                              .getIcons()
                              .getCustomKitIcons()
                              .getIcon())
                      .replace(
                          "%status%",
                          arena.isAllowCustomKitOnMap() ? "<green>Enabled" : "<red>Disabled")
                      .get());
              customKitSlot = slot;

              updatePlayers();
            });
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    final Player player = (Player) e.getWhoClicked();
    Inventory inventory = e.getView().getTopInventory();

    int slot = e.getRawSlot();
    ItemStack item = e.getCurrentItem();
    e.setCancelled(true);

    if (inventory.getSize() <= slot) {
      return;
    }
    if (item == null) {
      return;
    }

    if (slot == 18) {
      ArenaGuiSetupManager.getInstance()
          .getArenaSetupGuis()
          .get(arena)
          .get(GuiType.Arena_Main)
          .open(player);
    } else if (slot == 26) {
      ArenaGuiSetupManager.getInstance()
          .getArenaSetupGuis()
          .get(arena)
          .get(GuiType.Arena_Ladders_Single)
          .open(player);
    } else if (ladderTypeSlots.containsKey(slot)) {
      LadderType ladderType = ladderTypeSlots.get(slot);
      if (arena.getAssignedLadderTypes().contains(ladderType)) {
        if (!arena.isEnabled()) {
          arena.getAssignedLadderTypes().remove(ladderType);
          arena.getAssignedLadders().removeIf(ladder -> ladder.getType().equals(ladderType));

          this.update();
          ArenaGuiSetupManager.getInstance()
              .getArenaSetupGuis()
              .get(arena)
              .get(GuiType.Arena_Ladders_Single)
              .update();
          GuiManager.getInstance().searchGui(GuiType.Arena_Summary).update();
        } else {
          Common.sendMmMessage(
              player,
              PracticeConfig.language()
                  .getCommand()
                  .getSetup()
                  .getArena()
                  .getCantRemoveLadderType());
        }
      } else {
        if (arena.isBuild() == ladderType.isBuild()) {
          if (arena.isEnabled()) {
            if (ladderType.isBed() && !arena.isBedSet()) {
              Common.sendMmMessage(
                  player,
                  "<red>You can't assign bed related ladder to the arena because it's enabled and"
                      + " it doesn't have the beds set up.");
              return;
            }
            if (ladderType.isPortal() && !arena.isPortalSet()) {
              Common.sendMmMessage(
                  player,
                  "<red>You can't assign portal related ladder to the arena because it's enabled"
                      + " and it doesn't have the portals set up.");
              return;
            }
          }
          arena.getAssignedLadderTypes().add(ladderType);

          for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            if (ladder.isEnabled() && ladder.getType().equals(ladderType)) {
              arena.getAssignedLadders().add(ladder);
            }
          }

          this.update();
          ArenaGuiSetupManager.getInstance()
              .getArenaSetupGuis()
              .get(arena)
              .get(GuiType.Arena_Ladders_Single)
              .update();
          GuiManager.getInstance().searchGui(GuiType.Arena_Summary).update();
        } else {
          Common.sendMmMessage(
              player,
              PracticeConfig.language()
                  .getCommand()
                  .getSetup()
                  .getArena()
                  .getCantAssignBuildDiff());
        }
      }
    } else if (slot == customKitSlot) {
      if (arena.isAllowCustomKitOnMap()) {
        if (!arena.isEnabled()) {
          arena.setAllowCustomKitOnMap(false);
          this.update();
        } else {
          Common.sendMmMessage(
              player, "<red>You can't change this setting while the arena is enabled.");
        }
      } else {
        arena.setAllowCustomKitOnMap(true);
        this.update();
      }
    }
  }
}
