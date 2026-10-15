package dev.lokspel.practice.manager.fight.ffa.game;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.ffa.FfaManager;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiType;
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

public class FfaArenaSelectorGui extends Gui {

  private static String buildOn() {
    return PracticeConfig.guis()
        .getGuis()
        .getFfa()
        .getArenaSelector()
        .getIcons()
        .getArena()
        .getBuildStatus()
        .getEnabled();
  }

  private static String buildOff() {
    return PracticeConfig.guis()
        .getGuis()
        .getFfa()
        .getArenaSelector()
        .getIcons()
        .getArena()
        .getBuildStatus()
        .getDisabled();
  }

  private static GuiItem arenaItem() {
    return BackendUtil.getGuiItem(
        PracticeConfig.guis().getGuis().getFfa().getArenaSelector().getIcons().getArena());
  }

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getFfa().getArenaSelector().getIcons().getFiller())
        .get();
  }

  private final Map<Integer, FfaArena> arenaSlots = new HashMap<>();

  public FfaArenaSelectorGui() {
    super(GuiType.FFA_Arena_Selector);

    int rows =
        Math.clamp(PracticeConfig.guis().getGuis().getFfa().getArenaSelector().getRows(), 1, 6);
    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis().getGuis().getFfa().getArenaSelector().getTitle(), rows));
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);
    inventory.clear();
    arenaSlots.clear();

    Map<String, Integer> customSlots =
        PracticeConfig.guis().getGuis().getFfa().getArenaSelector().getArenaSlots();

    List<FfaArena> openArenas =
        new ArrayList<>(
            ArenaManager.getInstance().getFfaArenas().stream()
                .filter(
                    arena -> arena.isEnabled() && arena.getFfa() != null && arena.getFfa().isOpen())
                .toList());
    if (customSlots != null) {
      openArenas.sort(Comparator.comparing(arena -> !customSlots.containsKey(arena.getName())));
    }

    for (FfaArena arena : openArenas) {
      Ffa ffa = arena.getFfa();

      GuiItem guiItem =
          arenaItem()
              .cloneItem()
              .replace("%arena%", arena.getDisplayName())
              .replace("%players%", String.valueOf(ffa.getPlayers().size()))
              .replace("%build_status%", arena.isBuild() ? buildOn() : buildOff())
              .replace("%rekit_after_kill%", arena.isReKitAfterKill() ? buildOn() : buildOff())
              .replace(
                  "%health_reset_on_kill%", arena.isHealthResetOnKill() ? buildOn() : buildOff())
              .replace("%lobby_after_death%", arena.isLobbyAfterDeath() ? buildOn() : buildOff())
              .replace("%ladders%", String.valueOf(arena.getAssignedLadders().size()));

      if (arena.getIcon() != null) {
        guiItem.setBaseItem(arena.getIcon());
      }

      Integer customSlot = customSlots != null ? customSlots.get(arena.getName()) : null;
      int slot =
          (customSlot != null
                  && customSlot >= 0
                  && customSlot < inventory.getSize()
                  && inventory.getItem(customSlot) == null)
              ? customSlot
              : inventory.firstEmpty();

      if (slot == -1) {
        continue;
      }

      inventory.setItem(slot, guiItem.get());
      arenaSlots.put(slot, arena);
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

    if (!arenaSlots.containsKey(slot)) {
      return;
    }

    FfaArena arena = arenaSlots.get(slot);
    if (arena == null) {
      return;
    }

    if (!FfaManager.getInstance().joinArena(player, arena)) {
      player.closeInventory();
    }
  }
}
