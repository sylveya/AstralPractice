package dev.lokspel.practice.manager.gui.guis.arena;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.ArenaType;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.arena.setup.ArenaSetupManager;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArenaCreateGui extends Gui {

  private final String arenaName;
  @Getter private final Map<Integer, ArenaType> typeSlots = new HashMap<>();

  public ArenaCreateGui(String arenaName) {
    super(GuiType.Arena_Create);
    this.arenaName = arenaName;
    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis()
                .getGuis()
                .getArenaCreate()
                .getTitle()
                .replace("%arena%", arenaName),
            3));

    build();
  }

  @Override
  public void build() {
    Inventory inventory = gui.get(1);

    for (int i = 0; i < inventory.getSize(); i++) {
      inventory.setItem(i, GuiManager.fillerItem());
    }

    update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);

    inventory.setItem(10, null);
    inventory.setItem(13, null);
    inventory.setItem(16, null);

    for (ArenaType type : ArenaType.values()) {
      ItemStack item = ItemCreateUtil.createItem("<yellow>" + type.getName(), type.getIcon());
      ItemMeta itemMeta = item.getItemMeta();
      itemMeta.lore(type.getDescription().stream().map(Common::legacyToComponent).toList());
      item.setItemMeta(itemMeta);

      int slot = inventory.firstEmpty();
      typeSlots.put(slot, type);

      inventory.setItem(slot, item);
    }

    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    final Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();

    e.setCancelled(true);

    if (!typeSlots.containsKey(slot)) {
      return;
    }

    DisplayArena arena;
    if (Objects.requireNonNull(typeSlots.get(slot)) == ArenaType.FFA) {
      arena = new FfaArena(arenaName);
    } else {
      arena = new Arena(arenaName, typeSlots.get(slot));
    }

    arena.setData();

    ArenaManager.getInstance().getArenaList().add(arena);

    ArenaGuiSetupManager.getInstance().buildArenaSetupGuis(arena);
    GuiManager.getInstance().searchGui(GuiType.Arena_Summary).update();

    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getArena()
            .getCreate()
            .getArenaCreated()
            .replace("%arena%", arenaName));

    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> {
              ArenaGuiSetupManager.getInstance()
                  .getArenaSetupGuis()
                  .get(arena)
                  .get(GuiType.Arena_Main)
                  .open(player);
              ArenaSetupManager.getInstance().startSetup(player, arena);
            },
            3L);
  }
}
