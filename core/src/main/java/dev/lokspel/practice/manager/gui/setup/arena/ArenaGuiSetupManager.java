package dev.lokspel.practice.manager.gui.setup.arena;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.arenasettings.normal.ArenaMainGui;
import dev.lokspel.practice.manager.gui.setup.arena.arenasettings.normal.ArenaSettingsGui;
import dev.lokspel.practice.manager.gui.setup.arena.arenasettings.normal.CopyGui;
import dev.lokspel.practice.manager.gui.setup.arena.arenasettings.normal.LadderSingleGui;
import dev.lokspel.practice.manager.gui.setup.arena.arenasettings.normal.LadderTypeGui;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@Getter
public class ArenaGuiSetupManager implements Listener {

  private static ArenaGuiSetupManager instance;

  public static ArenaGuiSetupManager getInstance() {
    if (instance == null) {
      instance = new ArenaGuiSetupManager();
    }
    return instance;
  }

  private final Map<DisplayArena, Map<GuiType, Gui>> arenaSetupGuis = new HashMap<>();

  public ArenaGuiSetupManager() {
    Bukkit.getPluginManager().registerEvents(this, AstralPractice.getInstance());
  }

  public void buildArenaSetupGuis(DisplayArena arena) {
    Map<GuiType, Gui> guis = new HashMap<>();

    if (arena instanceof FfaArena) {
      guis.put(
          GuiType.Arena_Main,
          new dev.lokspel.practice.manager.gui.setup.arena.arenasettings.ffa.ArenaMainGui(
              (FfaArena) arena));
      guis.put(
          GuiType.Arena_Ladders_Single,
          new dev.lokspel.practice.manager.gui.setup.arena.arenasettings.ffa.LadderSingleGui(
              (FfaArena) arena));
    } else {
      guis.put(GuiType.Arena_Main, new ArenaMainGui((Arena) arena));
      guis.put(GuiType.Arena_Ladders_Single, new LadderSingleGui((Arena) arena));
      guis.put(GuiType.Arena_Ladders_Type, new LadderTypeGui((Arena) arena));
      guis.put(GuiType.Arena_Settings, new ArenaSettingsGui((Arena) arena));
      if (arena.isBuild()) {
        guis.put(GuiType.Arena_Copy, new CopyGui((Arena) arena));
      }
    }

    arenaSetupGuis.put(arena, guis);
  }

  public void loadGuis() {
    Bukkit.getScheduler().runTaskAsynchronously(AstralPractice.getInstance(), this::createGuis);
  }

  /** Re-creates the summary and every arena's setup GUIs from the reloaded configuration. */
  public void reloadGuis() {
    arenaSetupGuis.clear();
    createGuis();
  }

  private void createGuis() {
    GuiManager.getInstance().addGui(new ArenaSummaryGui());

    for (DisplayArena arena : new ArrayList<>(ArenaManager.getInstance().getArenaList())) {
      buildArenaSetupGuis(arena);
    }
  }

  public void removeArenaGuis(DisplayArena arena) {
    for (Gui gui : arenaSetupGuis.get(arena).values()) {
      for (Player player : gui.getInGuiPlayers().keySet()) {
        GuiManager.getInstance().searchGui(GuiType.Arena_Summary).open(player);
      }
    }

    arenaSetupGuis.remove(arena);
    GuiManager.getInstance().searchGui(GuiType.Arena_Summary).update();
  }

  @EventHandler
  public void onArenaCornerMarkerUse(PlayerInteractEvent e) {
    Player player = e.getPlayer();
    Profile profile = ProfileManager.getInstance().getProfile(player);

    Action action = e.getAction();
    final ItemStack item = e.getItem();

    switch (profile.getStatus()) {
      case MATCH:
      case FFA:
      case EVENT:
        return;

      default:
        break;
    }

    if (!player.hasPermission("ap.setup")) {
      return;
    }
    if (!action.equals(Action.LEFT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_BLOCK)) {
      return;
    }
    if (item == null) {
      return;
    }

    if (!ArenaSetupUtil.getArenaMarkerList().containsKey(item)) {
      return;
    }
    DisplayArena arena = ArenaSetupUtil.getArenaMarkerList().get(item);
    if (arena == null) {
      return;
    }

    e.setCancelled(true);

    if (action.equals(Action.LEFT_CLICK_BLOCK)) {
      player.performCommand("arena set corner " + arena.getName() + " 1");
    } else {
      player.performCommand("arena set corner " + arena.getName() + " 2");
    }
  }
}
