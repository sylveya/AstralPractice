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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.ItemStack;
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

    if (!markers.isEmpty()) {
      arenaMarkers.put(arena, markers);
    }
  }

  /** Creates a mannequin marker at the specified location. */
  private Mannequin createMarker(@NotNull Location location, @NotNull String name) {
    // Spawn mannequin at exact player spawn position
    Location markerLoc = location.clone();
    Mannequin mannequin =
        (Mannequin) markerLoc.getWorld().spawnEntity(markerLoc, EntityType.MANNEQUIN);

    // Configure mannequin to look like a player marker while staying static.
    mannequin.setInvisible(false); // Show body to represent player
    mannequin.setGravity(false);
    mannequin.setCanPickupItems(false);
    mannequin.setCustomNameVisible(true);
    mannequin.customName(Common.deserializeMiniMessage(name));
    mannequin.setAI(false);
    mannequin.setCollidable(false);
    mannequin.setSilent(true);
    mannequin.setImmovable(true);

    // Make it invulnerable and prevent interaction
    ArenaUtil.setMannequinInvulnerable(mannequin);

    // Give diamond sword to right hand
    ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
    ArenaUtil.setMannequinItemInHand(mannequin, sword, true);

    // Set player head (Steve head) for helmet
    ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
    mannequin.getEquipment();
    mannequin.getEquipment().setHelmet(playerHead);

    // Set red boots for visibility
    ItemStack boots = ItemCreateUtil.getRedBoots();
    mannequin.getEquipment();
    mannequin.getEquipment().setBoots(boots);

    // Track this mannequin
    markerStandIds.add(mannequin.getUniqueId());

    return mannequin;
  }

  private static final String REMOVE_INSTRUCTION = "<gray>(Right-click to remove)";

  /** Creates a small invisible mannequin just for displaying text label. */
  private Mannequin createLabelOnly(Location location) {
    if (location == null || location.getWorld() == null) {
      return null;
    }

    Mannequin labelStand =
        (Mannequin) location.getWorld().spawnEntity(location, EntityType.MANNEQUIN);

    // Configure as invisible text-only display
    labelStand.setInvisible(true); // Invisible
    labelStand.setGravity(false);
    labelStand.setCanPickupItems(false);
    labelStand.setCustomNameVisible(true);
    labelStand.customName(Common.deserializeMiniMessage(REMOVE_INSTRUCTION));
    labelStand.setAI(false);
    labelStand.setCollidable(false);
    labelStand.setSilent(true);
    labelStand.setImmovable(true);

    // Make it invulnerable
    ArenaUtil.setMannequinInvulnerable(labelStand);

    // Track this mannequin too
    markerStandIds.add(labelStand.getUniqueId());

    return labelStand;
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
   * Removes all orphaned marker mannequins from a world. This is useful for cleaning up mannequins
   * that persisted after server restart or were not properly removed due to timing issues.
   *
   * <p>Orphaned markers are identified by: - Having a custom name starting with "<red><bold>" (our
   * marker naming pattern) - Being in the arenas world - Not being tracked in our current marker
   * lists
   *
   * @param world The world to clean up
   */
  @SuppressWarnings("unused")
  public void cleanupOrphanedMarkers(World world) {
    if (world == null) {
      return;
    }

    List<Entity> toRemove = new ArrayList<>();

    // Find all mannequins in the world
    for (Entity entity : world.getEntities()) {
      if (entity instanceof Mannequin mannequin) {
        // Check if this looks like one of our markers but isn't tracked
        String customName =
            mannequin.customName() == null
                ? null
                : Common.serializeComponentToLegacyString(mannequin.customName());
        if (customName != null && !markerStandIds.contains(mannequin.getUniqueId())) {

          // Check if it matches our marker naming patterns
          if (customName.contains("Spawn") || customName.contains("Right-click to remove")) {
            toRemove.add(mannequin);
          }
        }
      }
    }

    // Remove the orphaned markers
    for (Entity entity : toRemove) {
      entity.remove();
    }
  }
}
