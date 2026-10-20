package dev.lokspel.practice.util.cooldown;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public final class PlayerCooldown {

  private PlayerCooldown() {}

  private static final Map<UUID, Map<CooldownObject, Long>> cooldowns = new HashMap<>();

  public static void addCooldown(Player player, CooldownObject object, int time) {
    addCooldown(player, object, (double) time);
  }

  public static void addCooldown(Player player, CooldownObject object, double time) {
    if (player == null || object == null) {
      return;
    }
    addCooldown(player.getUniqueId(), object, time);
  }

  private static void addCooldown(UUID uuid, CooldownObject object, double time) {
    if (uuid == null || object == null) {
      return;
    }
    cooldowns
        .computeIfAbsent(uuid, _ -> new EnumMap<>(CooldownObject.class))
        .put(object, (long) (System.currentTimeMillis() + time * 1000L));
  }

  public static void removeCooldown(Player player, CooldownObject object) {
    if (player != null) {
      removeCooldown(player.getUniqueId(), object);
    }
  }

  private static void removeCooldown(UUID uuid, CooldownObject object) {
    if (uuid == null || object == null) {
      return;
    }
    Map<CooldownObject, Long> playerCooldowns = cooldowns.get(uuid);
    if (playerCooldowns != null) {
      playerCooldowns.remove(object);
    }
  }

  public static void clearPlayer(Player player) {
    if (player != null) {
      clearPlayer(player.getUniqueId());
    }
  }

  private static void clearPlayer(UUID uuid) {
    if (uuid != null) {
      cooldowns.remove(uuid);
    }
  }

  public static boolean isActive(Player player, CooldownObject object) {
    return player != null && isActive(player.getUniqueId(), object);
  }

  private static boolean isActive(UUID uuid, CooldownObject object) {
    if (uuid == null || object == null) {
      return false;
    }
    Map<CooldownObject, Long> playerCooldowns = cooldowns.get(uuid);
    if (playerCooldowns != null) {
      Long expiry = playerCooldowns.get(object);
      return expiry != null && System.currentTimeMillis() < expiry;
    }
    return false;
  }

  public static long getLeft(Player player, CooldownObject object) {
    return player != null ? getLeft(player.getUniqueId(), object) : 0L;
  }

  private static long getLeft(UUID uuid, CooldownObject object) {
    if (uuid == null || object == null) {
      return 0L;
    }
    Map<CooldownObject, Long> playerCooldowns = cooldowns.get(uuid);
    if (playerCooldowns != null) {
      Long expiry = playerCooldowns.get(object);
      if (expiry != null && System.currentTimeMillis() < expiry) {
        return expiry - System.currentTimeMillis();
      }
    }
    return 0L;
  }

  public static double getLeftInDouble(Player player, CooldownObject object) {
    return player != null ? getLeftInDouble(player.getUniqueId(), object) : 0.0;
  }

  private static double getLeftInDouble(UUID uuid, CooldownObject object) {
    long left = getLeft(uuid, object);
    return Math.round(left / 100.0) / 10.0;
  }
}
