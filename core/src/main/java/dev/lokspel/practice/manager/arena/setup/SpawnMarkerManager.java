package dev.lokspel.practice.manager.arena.setup;

import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.ItemCreateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Manages mannequin markers that show spawn positions in arena setup mode. Mannequins face the
 * direction the player will spawn and hold a sword.
 */
@Getter
public class SpawnMarkerManager {

  private static SpawnMarkerManager instance;

  public static SpawnMarkerManager getInstance() {
    if (instance == null) {
      instance = new SpawnMarkerManager();
    }
    return instance;
  }

  // Map: Arena -> List of marker mannequins
  private final Map<DisplayArena, List<Mannequin>> arenaMarkers = new HashMap<>();

  // Set of all marker mannequin UUIDs for quick lookup
  private final Set<UUID> markerStandIds = new HashSet<>();

  // Map: Main marker mannequin -> spawn index (for FFA arenas)
  private final Map<UUID, Integer> markerToSpawnIndex = new HashMap<>();

  private SpawnMarkerManager() {}

  /** Shows all spawn position markers for an arena. */
  public void showMarkers(DisplayArena arena) {
    if (arena == null) {
      return;
    }

    // Clear existing markers first
    clearMarkers(arena);

    List<Mannequin> markers = new ArrayList<>();

    if (arena instanceof Arena standardArena) {
      // Show position 1
      if (standardArena.getPosition1() != null) {
        markers.add(createMarker(standardArena.getPosition1(), "<red><bold>Spawn 1"));
      }

      // Show position 2
      if (standardArena.getPosition2() != null) {
        markers.add(createMarker(standardArena.getPosition2(), "<red><bold>Spawn 2"));
      }
    } else if (arena instanceof FfaArena ffaArena) {
      // Show all FFA spawn positions with two-line labels
      int index = 0; // Use 0-based index to match the list
      for (Location spawnLoc : ffaArena.getFfaPositions()) {
        // Create main marker with player model
        Mannequin marker =
            createMarker(spawnLoc, "<red><bold>FFA Spawn #" + (index + 1)); // Display as 1-based
        markers.add(marker);
        // Track this main marker to its spawn index
        markerToSpawnIndex.put(marker.getUniqueId(), index);

        // Create second mannequin above for instruction text (closer spacing)
        Location labelLoc = spawnLoc.clone().add(0, 2.3, 0);
        Mannequin labelStand = createLabelOnly(labelLoc);
        if (labelStand != null) {
          markers.add(labelStand);
          // Track label stand to spawn index as well so interactions on it also work
          markerToSpawnIndex.put(labelStand.getUniqueId(), index);
        }
        index++;
      }
    }

    if (arena.getSpectatorPosition() != null) {
      markers.add(createSpectatorMarker(arena.getSpectatorPosition()));
    }

    if (!markers.isEmpty()) {
      arenaMarkers.put(arena, markers);
    }
  }

  /** Creates a mannequin marker for spectator spawn. */
  private Mannequin createSpectatorMarker(@NotNull Location location) {
    Mannequin mannequin = spawnBaseMannequin(location, "<aqua><bold>Spectator Spawn", false);

    // Give spectator distinctive gear: Spyglass, Ender Eye in offhand, Elytra, and Aqua boots
    ArenaUtil.setMannequinItemInHand(mannequin, new ItemStack(Material.SPYGLASS), true);
    ArenaUtil.setMannequinItemInHand(mannequin, new ItemStack(Material.ENDER_EYE), false);
    mannequin.getEquipment().setHelmet(new ItemStack(Material.PLAYER_HEAD));
    mannequin.getEquipment().setChestplate(new ItemStack(Material.ELYTRA));
    mannequin.getEquipment().setBoots(createDyedBoots(Color.AQUA));

    return mannequin;
  }

  /** Creates a mannequin marker at the specified location. */
  private Mannequin createMarker(@NotNull Location location, @NotNull String name) {
    Mannequin mannequin = spawnBaseMannequin(location, name, false);

    // Give fighter spawn marker standard combat gear
    ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
    ArenaUtil.setMannequinItemInHand(mannequin, sword, true);
    mannequin.getEquipment().setHelmet(new ItemStack(Material.PLAYER_HEAD));
    mannequin.getEquipment().setBoots(ItemCreateUtil.getRedBoots());

    return mannequin;
  }

  private static final String REMOVE_INSTRUCTION = "<gray>(Right-click to remove)";

  /** Creates a small invisible mannequin just for displaying text label. */
  private Mannequin createLabelOnly(Location location) {
    if (location == null || location.getWorld() == null) {
      return null;
    }
    return spawnBaseMannequin(location, REMOVE_INSTRUCTION, true);
  }

  /** Spawns and configures a base mannequin with common setup marker properties. */
  private Mannequin spawnBaseMannequin(
      @NotNull Location location, @NotNull String name, boolean invisible) {
    Location markerLoc = location.clone();
    Mannequin mannequin =
        (Mannequin) markerLoc.getWorld().spawnEntity(markerLoc, EntityType.MANNEQUIN);

    mannequin.setInvisible(invisible);
    mannequin.setGravity(false);
    mannequin.setCanPickupItems(false);
    mannequin.setCustomNameVisible(true);
    mannequin.customName(Common.deserializeMiniMessage(name));
    mannequin.setAI(false);
    mannequin.setCollidable(false);
    mannequin.setSilent(true);
    mannequin.setImmovable(true);

    ArenaUtil.setMannequinInvulnerable(mannequin);
    markerStandIds.add(mannequin.getUniqueId());

    return mannequin;
  }

  private static ItemStack createDyedBoots(Color color) {
    ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
    LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
    if (meta != null) {
      meta.setColor(color);
      boots.setItemMeta(meta);
    }
    return boots;
  }

  /** Clears all markers for a specific arena. */
  public void clearMarkers(DisplayArena arena) {
    if (arena == null) {
      return;
    }

    List<Mannequin> markers = arenaMarkers.remove(arena);
    if (markers != null) {
      for (Mannequin marker : markers) {
        if (marker != null) {
          // Always clean up tracking data
          markerStandIds.remove(marker.getUniqueId());
          markerToSpawnIndex.remove(marker.getUniqueId()); // Clean up spawn index mapping

          // Attempt to remove the mannequin if it's still valid
          if (marker.isValid()) {
            marker.remove();
          }
        }
      }
    }
  }

  /** Clears all markers for all arenas. */
  public void clearAllMarkers() {
    for (List<Mannequin> markers : arenaMarkers.values()) {
      if (markers != null) {
        for (Mannequin marker : markers) {
          if (marker != null) {
            // Always clean up tracking data
            markerStandIds.remove(marker.getUniqueId());
            markerToSpawnIndex.remove(marker.getUniqueId()); // Clean up spawn index mapping

            // Attempt to remove the armor stand if it's still valid
            if (marker.isValid()) {
              marker.remove();
            }
          }
        }
      }
    }
    arenaMarkers.clear();
    markerToSpawnIndex.clear(); // Clear all mappings
  }

  /** Updates markers for an arena (re-creates them). */
  public void updateMarkers(DisplayArena arena) {
    clearMarkers(arena);
    showMarkers(arena);
  }

  /** Checks if a mannequin is a spawn marker. */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isMarker(Mannequin mannequin) {
    return mannequin != null && markerStandIds.contains(mannequin.getUniqueId());
  }

  /** Finds which arena a marker mannequin belongs to. */
  public DisplayArena getArenaForMarker(Mannequin mannequin) {
    if (mannequin == null) {
      return null;
    }

    for (Map.Entry<DisplayArena, List<Mannequin>> entry : arenaMarkers.entrySet()) {
      if (entry.getValue().contains(mannequin)) {
        return entry.getKey();
      }
    }
    return null;
  }

  /**
   * Removes a specific marker mannequin and its associated spawn position.
   *
   * @return true if the marker was found and removed
   */
  public boolean removeMarker(Mannequin mannequin, DisplayArena arena) {
    if (mannequin == null || arena == null) {
      return false;
    }

    // Check if this mannequin is tracked with a spawn index (it's a main marker)
    Integer spawnIndex = markerToSpawnIndex.get(mannequin.getUniqueId());
    if (spawnIndex == null) {
      return false;
    } // Not a main marker or not tracked

    // Remove from FFA arena
    if (arena instanceof FfaArena ffaArena) {
      if (spawnIndex >= 0 && spawnIndex < ffaArena.getFfaPositions().size()) {
        ffaArena.getFfaPositions().remove(spawnIndex.intValue());
      }
    }

    // Clean up the mapping
    markerStandIds.remove(mannequin.getUniqueId());
    markerToSpawnIndex.remove(mannequin.getUniqueId());

    // Remove the mannequin from tracking list
    List<Mannequin> markers = arenaMarkers.get(arena);
    if (markers != null) {
      markers.remove(mannequin);
    }

    mannequin.remove();

    return true;
  }

  /**
   * Removes all orphaned marker mannequins from a world.
   *
   * @param world The world to clean up
   */
  public void cleanupOrphanedMarkers(World world) {
    if (world == null) {
      return;
    }

    List<Entity> toRemove = new ArrayList<>();

    for (Entity entity : world.getEntities()) {
      if (entity instanceof Mannequin mannequin) {
        String customName =
            mannequin.customName() == null
                ? null
                : Common.serializeComponentToLegacyString(mannequin.customName());
        if (customName != null && !markerStandIds.contains(mannequin.getUniqueId())) {
          if (customName.contains("Spawn") || customName.contains("Right-click to remove")) {
            toRemove.add(mannequin);
          }
        }
      }
    }

    for (Entity entity : toRemove) {
      entity.remove();
    }
  }
}
