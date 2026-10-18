package dev.lokspel.practice.manager.arena.setup;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ArenaSetupManager {

  private static ArenaSetupManager instance;

  public static ArenaSetupManager getInstance() {
    if (instance == null) {
      instance = new ArenaSetupManager();
    }
    return instance;
  }

  private ArenaSetupManager() {
    ArenaSetupListener arenaSetupListener = new ArenaSetupListener(this);
    Bukkit.getPluginManager().registerEvents(arenaSetupListener, AstralPractice.getInstance());
  }

  @Getter
  @Setter
  public static class SetupSession {
    private final DisplayArena arena;
    private SetupMode currentMode;

    public SetupSession(DisplayArena arenaName) {
      this.arena = arenaName;
      this.currentMode = SetupMode.CORNERS;
    }
  }

  private final Map<Player, SetupSession> setupSessions = new HashMap<>();

  public boolean startSetup(Player player, DisplayArena arena) {
    if (arena.isEnabled()) {
      return false;
    }

    if (isSettingUp(player)) {
      stopSetup(player);
    }

    setupSessions.put(player, new SetupSession(arena));

    if (arena.getCuboid() != null && !arena.getCuboid().contains(player.getLocation())) {
      arena.teleport(player);
    } else {
      player.setAllowFlight(true);
      player.setFlying(true);
      player.setGameMode(GameMode.CREATIVE);
    }

    updateWand(player);

    // Show spawn position markers
    SpawnMarkerManager.getInstance().showMarkers(arena);

    Common.sendMmMessage(
        player, "<green>Setup mode started for arena: <yellow>" + arena.getName() + "<green>.");
    return true;
  }

  public void stopSetup(Player player) {
    DisplayArena arena = getSession(player).getArena();
    setupSessions.remove(player);

    ItemStack[] contents = player.getInventory().getContents();
    for (int i = 0; i < contents.length; i++) {
      ItemStack item = contents[i];

      if (isSetupWand(item)) {
        player.getInventory().clear(i);
      }
    }

    // Clear markers if no one else is setting up this arena
    if (getPlayersSettingUpArena(arena).isEmpty()) {
      SpawnMarkerManager.getInstance().clearMarkers(arena);
    }

    Common.sendMmMessage(player, "<red>Setup mode ended for arena: <red>" + arena.getName() + ".");
  }

  public SetupSession getSession(Player player) {
    return setupSessions.get(player);
  }

  public boolean isSettingUp(Player player) {
    return setupSessions.containsKey(player);
  }

  public List<Player> getPlayersSettingUpArena(DisplayArena arena) {
    List<Player> players = new ArrayList<>();

    for (Map.Entry<Player, SetupSession> entry : setupSessions.entrySet()) {
      if (entry.getValue().getArena().equals(arena)) {
        players.add(entry.getKey());
      }
    }

    return players;
  }

  public boolean isSetupWand(ItemStack item) {
    return item != null
        && item.getType() == Material.BLAZE_ROD
        && item.hasItemMeta()
        && Common.getItemDisplayName(item).contains("Arena Wand");
  }

  public SetupMode getNextMode(DisplayArena arena, SetupMode current) {
    List<SetupMode> validModes = getValidModes(arena);
    int currentIndex = validModes.indexOf(current);
    if (currentIndex == -1) {
      return validModes.getFirst();
    }

    int nextIndex = (currentIndex + 1) % validModes.size();
    return validModes.get(nextIndex);
  }

  public SetupMode getPreviousMode(DisplayArena arena, SetupMode current) {
    List<SetupMode> validModes = getValidModes(arena);
    int currentIndex = validModes.indexOf(current);
    if (currentIndex == -1) {
      return validModes.getFirst();
    }

    int prevIndex = (currentIndex - 1 + validModes.size()) % validModes.size();
    return validModes.get(prevIndex);
  }

  private List<SetupMode> getValidModes(DisplayArena arena) {
    List<SetupMode> modes = new ArrayList<>();

    if (arena instanceof FfaArena a) {
      modes.add(SetupMode.FFA_POSITIONS);
      modes.add(SetupMode.SPECTATOR_POSITION);
      modes.add(SetupMode.DEAD_ZONE);

      if (a.isBuild()) {
        modes.add(SetupMode.BUILD_MAX);
      }
    } else if (arena instanceof Arena a) {
      modes.add(SetupMode.POSITIONS);
      modes.add(SetupMode.SPECTATOR_POSITION);
      modes.add(SetupMode.DEAD_ZONE);

      if (a.isBuild()) {
        modes.add(SetupMode.BUILD_MAX);
        modes.add(SetupMode.BED_LOCATIONS);
        modes.add(SetupMode.PORTALS);
      }
    }

    modes.add(SetupMode.TOGGLE_STATUS);
    modes.add(SetupMode.CORNERS);

    return modes;
  }

  public void updateWand(Player player) {
    SetupSession session = getSession(player);
    if (session == null) {
      return;
    }

    DisplayArena arena = session.getArena();
    if (arena == null) {
      return;
    }

    ItemStack wand = new ItemStack(Material.BLAZE_ROD);
    ItemMeta meta = wand.getItemMeta();
    SetupMode mode = session.getCurrentMode();

    meta.displayName(
        Common.toComponent("<gold>Arena Wand <gray>(<yellow>" + mode.getDisplayName() + "<gray>)"));

    List<Component> lore = new ArrayList<>();
    lore.add(Common.toComponent("<gray>Editing: <green>" + arena.getName()));
    lore.add(
        Common.toComponent(
            "<gray>Type: <aqua>" + (arena instanceof FfaArena ? "FFA" : "Standard")));
    lore.add(Component.empty());
    lore.add(Common.toComponent("<yellow>Current Mode: <white>" + mode.getDisplayName()));
    lore.add(Component.empty());
    lore.add(Common.toComponent("<gray>Controls:"));

    for (String line : mode.getDescription()) {
      lore.add(Common.toComponent(line));
    }

    lore.add(Component.empty());
    lore.add(Common.toComponent("<light_purple>Shift + Left: <gray>Next Mode"));
    lore.add(Common.toComponent("<light_purple>Shift + Right: <gray>Prev Mode"));
    lore.add(Component.empty());
    lore.add(Common.toComponent("<red>Drop (Q): <gray>Exit Setup"));

    meta.lore(lore);
    wand.setItemMeta(meta);

    player.getInventory().setItemInMainHand(wand);
  }
}
