package dev.lokspel.practice.data;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.util.Common;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import me.whereareiam.configura.Configura;
import me.whereareiam.configura.exception.ConfigException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reads and writes the data files the plugin keeps about the server: {@code backend.yml} and the
 * files under {@code arenas/}, {@code ladders/}, {@code events/}, {@code profiles/} and {@code
 * match-history/}. Each file is bound to a typed model with Configura; Bukkit objects keep the
 * format {@code YamlConfiguration} used, so files from earlier versions load unchanged.
 *
 * <p>Paths are relative to the plugin's data folder, for example {@code "arenas/duel1.yml"}. Writes
 * replace the file atomically.
 */
public enum DataFiles {
  ;

  private static final Configura CONFIGURA = Configura.builder().module(new BukkitModule()).build();

  /**
   * Reads a data file.
   *
   * @param file path relative to the data folder
   * @param type model of the file
   * @return the bound model, or {@code null} if the file does not exist or cannot be read; an
   *     unreadable file is copied aside before anything can overwrite it
   */
  public static <T> @Nullable T read(String file, Class<T> type) {
    try {
      return readOrFail(file, type);
    } catch (ConfigException e) {
      Common.sendConsoleMmMessage(
          "<red>Couldn't read " + file + ": " + PracticeConfig.describeFailure(e));
      keepBrokenCopy(path(file));
      return null;
    }
  }

  /**
   * Reads a data file that may not be readable yet, for example because a world its locations are
   * in is not loaded. A failure is not reported and the file is left as it is.
   *
   * @param file path relative to the data folder
   * @param type model of the file
   * @return the bound model, or {@code null} if the file does not exist or cannot be read now
   */
  public static <T> @Nullable T readIfReady(String file, Class<T> type) {
    try {
      return readOrFail(file, type);
    } catch (ConfigException e) {
      return null;
    }
  }

  /**
   * Tells whether a data file exists.
   *
   * @param file path relative to the data folder
   * @return {@code true} if the file exists
   */
  public static boolean exists(String file) {
    return Files.exists(path(file));
  }

  /** Earlier versions left empty files behind; those read as missing. */
  private static <T> @Nullable T readOrFail(String file, Class<T> type) {
    Path path = path(file);
    if (!Files.exists(path) || isBlank(path)) {
      return null;
    }

    return CONFIGURA.read(path, type);
  }

  private static boolean isBlank(Path path) {
    try {
      return Files.size(path) == 0 || Files.readString(path).isBlank();
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Reads a data file, or creates its model when the file does not exist or cannot be read.
   *
   * @param file path relative to the data folder
   * @param type model of the file
   * @param empty creates the model of a missing file
   * @return the bound or new model
   */
  public static <T> @NotNull T readOrCreate(String file, Class<T> type, Supplier<T> empty) {
    T data = read(file, type);
    return data != null ? data : empty.get();
  }

  /**
   * Writes a data file, replacing what it contained.
   *
   * @param file path relative to the data folder
   * @param data the model to write
   */
  public static void write(String file, Object data) {
    try {
      CONFIGURA.write(path(file), data);
    } catch (ConfigException e) {
      Common.sendConsoleMmMessage(
          "<red>Couldn't write " + file + ": " + PracticeConfig.describeFailure(e));
    }
  }

  /**
   * Deletes a data file if it exists.
   *
   * @param file path relative to the data folder
   */
  public static void delete(String file) {
    try {
      Files.deleteIfExists(path(file));
    } catch (IOException e) {
      Common.sendConsoleMmMessage("<red>Couldn't delete " + file + ": " + e.getMessage());
    }
  }

  /**
   * Lists the {@code .yml} files of a data folder.
   *
   * @param folder folder relative to the data folder, for example {@code "arenas"}
   * @return the files' paths relative to the data folder, empty if the folder does not exist
   */
  public static List<String> list(String folder) {
    Path directory = path(folder);
    if (!Files.isDirectory(directory)) {
      return List.of();
    }

    try (Stream<Path> files = Files.list(directory)) {
      return files
          .filter(Files::isRegularFile)
          .map(file -> file.getFileName().toString())
          .filter(name -> name.endsWith(".yml"))
          .sorted()
          .map(name -> folder + "/" + name)
          .toList();
    } catch (IOException e) {
      Common.sendConsoleMmMessage("<red>Couldn't list " + folder + ": " + e.getMessage());
      return List.of();
    }
  }

  private static Path path(String file) {
    return AstralPractice.getInstance().getDataFolder().toPath().resolve(file);
  }

  private static void keepBrokenCopy(Path path) {
    Path copy = path.resolveSibling(path.getFileName() + ".broken-" + System.currentTimeMillis());
    try {
      Files.copy(path, copy, StandardCopyOption.REPLACE_EXISTING);
      Common.sendConsoleMmMessage("<red>Kept a copy of it as " + copy.getFileName() + ".");
    } catch (IOException e) {
      Common.sendConsoleMmMessage("<red>Couldn't keep a copy of " + path + ": " + e.getMessage());
    }
  }
}
