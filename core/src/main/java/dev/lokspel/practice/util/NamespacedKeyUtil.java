package dev.lokspel.practice.util;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.Locale;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

/** Utility for creating, parsing, and resolving {@link NamespacedKey} instances. */
public final class NamespacedKeyUtil {

  public static final String MINECRAFT_NAMESPACE = "minecraft";

  private NamespacedKeyUtil() {}

  /**
   * Parses a raw string into a {@link NamespacedKey}, defaulting to the {@code minecraft} namespace
   * if no namespace prefix is provided.
   *
   * @param raw the key string, e.g. "eye", "minecraft:eye", or "custom:item"
   * @return the parsed NamespacedKey, or {@code null} if input is null/blank or invalid
   */
  public static NamespacedKey parse(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }

    String normalized = raw.trim().toLowerCase(Locale.ROOT);
    if (!normalized.contains(":")) {
      normalized = MINECRAFT_NAMESPACE + ":" + normalized;
    }

    return NamespacedKey.fromString(normalized);
  }

  /**
   * Creates a {@link NamespacedKey} under the {@code minecraft} namespace.
   *
   * @param key the key name
   * @return the NamespacedKey
   */
  public static NamespacedKey minecraft(String key) {
    if (key == null || key.isBlank()) {
      return null;
    }
    return NamespacedKey.minecraft(key.trim().toLowerCase(Locale.ROOT));
  }

  /**
   * Returns a compact string representation of the key: returns only the key if the namespace is
   * {@code minecraft}, otherwise returns the full namespaced string (e.g. {@code "custom:key"}).
   */
  public static String toShortString(NamespacedKey key) {
    if (key == null) {
      return null;
    }
    return MINECRAFT_NAMESPACE.equals(key.getNamespace()) ? key.getKey() : key.toString();
  }

  /** Resolves the key of a registry value and returns its compact string representation. */
  public static <T extends Keyed> String toShortString(RegistryKey<T> registryKey, T value) {
    if (value == null) {
      return null;
    }
    NamespacedKey key =
        RegistryAccess.registryAccess().getRegistry(registryKey).getKeyOrThrow(value);
    return toShortString(key);
  }

  /**
   * Looks up a value in Paper's registry by its raw key string (with {@code minecraft:} fallback).
   *
   * @param registryKey the Paper registry key
   * @param raw the raw key string
   * @return the registry value, or {@code null} if not found or input is null
   */
  public static <T extends Keyed> T resolve(RegistryKey<T> registryKey, String raw) {
    NamespacedKey key = parse(raw);
    if (key == null) {
      return null;
    }
    return RegistryAccess.registryAccess().getRegistry(registryKey).get(key);
  }
}
