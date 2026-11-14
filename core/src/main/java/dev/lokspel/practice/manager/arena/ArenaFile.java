package dev.lokspel.practice.manager.arena;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.data.DataFiles;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.util.Common;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * The file {@code arenas/<name>.yml} of an arena. The read model stays in memory; the arena updates
 * it and {@link #save()} writes it back.
 */
@Getter
public class ArenaFile {

  private final DisplayArena arena;
  private final String path;
  private final ArenaData data;

  public ArenaFile(DisplayArena arena) {
    this(arena, null);
  }

  /**
   * Opens the file of an arena.
   *
   * @param arena the arena
   * @param data the file's content if it was already read, otherwise {@code null}
   */
  public ArenaFile(DisplayArena arena, @Nullable ArenaData data) {
    this.arena = arena;
    this.path = path(arena.getName());
    this.data = data != null ? data : DataFiles.readOrCreate(path, ArenaData.class, ArenaData::new);
  }

  /** Path of an arena's file relative to the data folder. */
  public static String path(String arenaName) {
    return "arenas/" + arenaName.toLowerCase() + ".yml";
  }

  public void save() {
    // Never rewrite an arena file whose world is not loaded: the in-memory model may reference
    // unloaded worlds, and writing it would destroy the (still valid) data on disk.
    if (arena != null && !arena.isWorldLoaded()) {
      Common.sendConsoleMmMessage(
          "<yellow>Skipping save of arena file '" + path + "': its world is not loaded.");
      return;
    }
    DataFiles.write(path, data);
  }

  /**
   * Deletes the file.
   *
   * @return whether the file existed and is gone now
   */
  public boolean delete() {
    Path file = AstralPractice.getInstance().getDataFolder().toPath().resolve(path);
    if (!Files.exists(file)) {
      return false;
    }

    DataFiles.delete(path);
    return !Files.exists(file);
  }
}
