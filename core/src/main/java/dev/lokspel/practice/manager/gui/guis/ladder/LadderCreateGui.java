package dev.lokspel.practice.manager.gui.guis.ladder;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.ItemCreateUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LadderCreateGui extends Gui {

  private static final int[] CONTENT_SLOTS = {
    10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34
  };

  private final String ladderName;
  @Getter private final Map<Integer, LadderType> typeSlots = new HashMap<>();

  public LadderCreateGui(String ladderName) {
    super(GuiType.Ladder_Create);
    this.ladderName = ladderName;
    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis()
                .getGuis()
                .getLadderCreate()
                .getTitle()
                .replace("%ladder%", ladderName),
            5));

    build();
  }

  @Override
  public void build() {
    this.update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);
    this.typeSlots.clear();

    for (int i :
        new int[] {
          0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44
        }) {
      inventory.setItem(i, GuiManager.fillerItem());
    }

    for (int slot : CONTENT_SLOTS) {
      inventory.setItem(slot, null);
    }

    int index = 0;
    for (LadderType type : LadderType.values()) {
      if (index >= CONTENT_SLOTS.length) {
        Common.sendConsoleMmMessage(
            "<yellow>Warning: LadderCreateGui has no free slot for ladder type <white>"
                + type.name());
        break;
      }

      ItemStack item = ItemCreateUtil.createItem("<yellow>" + type.getName(), type.getIcon());
      ItemMeta itemMeta = item.getItemMeta();
      itemMeta.lore(Common.toComponent(type.getDescription()));
      item.setItemMeta(itemMeta);

      int slot = CONTENT_SLOTS[index++];
      typeSlots.put(slot, type);

      inventory.setItem(slot, item);
    }

    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();

    e.setCancelled(true);

    if (!typeSlots.containsKey(slot)) {
      return;
    }

    createLadder(player, typeSlots.get(slot));
  }

  private void createLadder(Player player, LadderType type) {
    try {
      NormalLadder ladder = type.create(ladderName);
      ladder.setData();

      LadderManager.getInstance().addLadder(ladder);

      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getLadder()
              .getCreate()
              .getLadderCreated()
              .replace("%ladder%", ladderName));

      Bukkit.getScheduler()
          .runTaskLater(
              AstralPractice.getInstance(),
              () ->
                  LadderSetupManager.getInstance()
                      .getLadderSetupGuis()
                      .get(ladder)
                      .get(GuiType.Ladder_Main)
                      .open(player),
              3L);
    } catch (Exception e) {
      Common.sendConsoleMmMessage("<red>Error. Please contact us on discord.");
    }
  }
}
