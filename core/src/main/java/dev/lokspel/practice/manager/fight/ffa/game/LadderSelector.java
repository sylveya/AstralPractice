package dev.lokspel.practice.manager.fight.ffa.game;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LadderSelector extends Gui {

  private static GuiItem ladderItem() {
    return BackendUtil.getGuiItem(
        PracticeConfig.guis().getGuis().getFfa().getLadderSelector().getIcons().getLadder());
  }

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getFfa().getLadderSelector().getIcons().getFiller())
        .get();
  }

  private final Ffa ffa;
  private final FfaArena ffaArena;

  private final Map<Integer, NormalLadder> ladderSlots = new HashMap<>();

  public LadderSelector(Ffa ffa) {
    super(GuiType.FFA_Ladder_Selector);
    this.ffa = ffa;
    this.ffaArena = ffa.getArena();

    build();
  }

  @Override
  public void build() {
    int rows =
        Math.clamp(PracticeConfig.guis().getGuis().getFfa().getLadderSelector().getRows(), 1, 6);
    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis().getGuis().getFfa().getLadderSelector().getTitle(), rows));
    update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);
    inventory.clear();
    ladderSlots.clear();

    Map<String, Integer> customSlots =
        PracticeConfig.guis().getGuis().getFfa().getLadderSelector().getLadderSlots();

    List<NormalLadder> ladders = new ArrayList<>(ffaArena.getAssignedLadders());
    if (customSlots != null) {
      ladders.sort(Comparator.comparing(ladder -> !customSlots.containsKey(ladder.getName())));
    }

    for (NormalLadder ladder : ladders) {
      GuiItem guiItem = ladderItem().cloneItem().replace("%ladder%", ladder.getDisplayName());

      if (ladder.getIcon() != null) {
        guiItem.setBaseItem(ladder.getIcon());
      }

      ItemStack ladderItem = guiItem.get();

      Integer customSlot = customSlots != null ? customSlots.get(ladder.getName()) : null;
      int slot =
          (customSlot != null
                  && customSlot >= 0
                  && customSlot < inventory.getSize()
                  && inventory.getItem(customSlot) == null)
              ? customSlot
              : inventory.firstEmpty();

      if (slot != -1) {
        inventory.setItem(slot, ladderItem);
        ladderSlots.put(slot, ladder);
      }
    }

    for (int i = 0; i < inventory.getSize(); i++) {
      if (inventory.getItem(i) == null) {
        inventory.setItem(i, fillerItem());
      }
    }

    this.updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    e.setCancelled(true);

    Player player = (Player) e.getWhoClicked();
    int slot = e.getSlot();

    if (ladderSlots.isEmpty()) {
      player.closeInventory();
      return;
    }

    if (!ladderSlots.containsKey(slot)) {
      return;
    }

    NormalLadder ladder = ladderSlots.get(slot);
    if (ladder == null) {
      update();
      return;
    }

    if (!ladder.isEnabled() || !ffaArena.getAssignedLadders().contains(ladder)) {
      update();
      return;
    } else if (ladder.isFrozen()) {
      update();
      return;
    }

    if (!ffa.isOpen()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getFfa()
              .getFfaClosed()
              .replace("%arena%", ffaArena.getDisplayName()));
      player.closeInventory();
      return;
    }

    if (ffa.getPlayers().containsKey(player)) {
      if (ffa.isFfaKitBlocked() && ffa.isFfaInCombat(player)) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getFfa().getCommand().getKit().getInCombat());
        player.closeInventory();
        return;
      }

      Ladder oldLadder = ffa.getPlayers().get(player);
      if (oldLadder == ladder) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getFfa()
                .getCommand()
                .getKit()
                .getAlreadyInKit()
                .replace("%ladder%", ladder.getDisplayName()));
        return;
      }

      ffa.changePlayerLadder(player, ladder);
    } else {
      ffa.addPlayer(player, ladder);
    }

    player.closeInventory();
  }
}
