package dev.lokspel.practice.manager.leaderboard.hologram;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single line in a hologram display. Each HologramLine manages exactly ONE TextDisplay
 * entity with strict lifecycle management.
 */
@Getter
public class HologramLine {

  @Setter private TextDisplay entity;
  private Location location;
  private String text = "";
  private boolean spawned;

  /**
   * Creates an unspawned hologram line. Call {@link #spawn(Location, String)} to create the actual
   * entity.
   */
  public HologramLine() {
    this.spawned = false;
  }

  /**
   * Spawns the text display at the specified location. This method is idempotent - calling multiple
   * times won't create duplicates.
   *
   * @param loc The spawn location
   * @param text The display text (supports color codes)
   * @return The spawned TextDisplay, or existing one if already spawned
   */
  @Nullable
  public TextDisplay spawn(@NotNull Location loc, @NotNull String text) {
    if (spawned && TextDisplayFactory.isAlive(entity)) {
      return entity;
    }

    this.location = loc.clone();
    this.text = text;

    if (location.getWorld() == null) {
      return null;
    }

    this.entity = TextDisplayFactory.create(location, text);
    this.spawned = (entity != null);

    return entity;
  }

  /**
   * Despawns and removes the text display entity. The line can be respawned later with {@link
   * #spawn(Location, String)}.
   */
  public void despawn() {
    if (!spawned) {
      return;
    }

    TextDisplayFactory.safeRemove(entity);
    entity = null;
    spawned = false;
  }

  /**
   * Updates the display text. If the display was externally removed, it will be automatically
   * respawned.
   *
   * @param newText The new display text (supports color codes)
   */
  public void updateText(@NotNull String newText) {
    this.text = newText;

    if (TextDisplayFactory.isAlive(entity)) {
      TextDisplayFactory.updateText(entity, newText);
      return;
    }

    if (spawned && location != null && location.getWorld() != null) {
      this.entity = TextDisplayFactory.create(location, newText);
    }
  }

  /**
   * Checks if this line has a valid, alive text display.
   *
   * @return true if the entity is alive
   */
  public boolean isValid() {
    return spawned && TextDisplayFactory.isAlive(entity);
  }
}
