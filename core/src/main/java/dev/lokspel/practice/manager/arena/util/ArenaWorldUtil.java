package dev.lokspel.practice.manager.arena.util;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.settings.SettingsConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.interfaces.EventData;
import dev.lokspel.practice.util.Cuboid;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

public enum ArenaWorldUtil {
  ;

  @Getter public static World arenasWorld;
  @Getter public static World arenasCopyWorld;

  public static void createArenaWorld() {
    SettingsConfig.Arena.Worlds settings = PracticeConfig.settings().getArena().getWorlds();

    if (settings.isCreateDefault()) {
      arenasWorld = Bukkit.getWorld("arenas");
      if (arenasWorld == null) {
        arenasWorld = ArenaUtil.createEmptyWorld("arenas");
      }
      ArenaUtil.setGamerules(arenasWorld);
    } else {
      arenasWorld = null;
    }

    String copyWorld = settings.getCopyWorld();
    arenasCopyWorld = Bukkit.getWorld(copyWorld);
    if (arenasCopyWorld == null) {
      arenasCopyWorld = ArenaUtil.createEmptyWorld(copyWorld);
    }
    ArenaUtil.setGamerules(arenasCopyWorld);
  }

  public static boolean isArenaOrEventWorld(World world) {
    if (world == null) {
      return false;
    }
    if (world.equals(arenasWorld) || world.equals(arenasCopyWorld)) {
      return true;
    }
    return containsArenaOrEventCuboids(world);
  }

  public static boolean containsArenaOrEventCuboids(World world) {
    if (world == null) {
      return false;
    }
    String name = world.getName();
    for (Cuboid cuboid : ArenaManager.getInstance().getArenaCuboids().keySet()) {
      if (cuboid != null && name.equals(cuboid.getWorldName())) {
        return true;
      }
    }
    for (EventData data : EventManager.getInstance().getEventData().values()) {
      if (data != null
          && data.getCuboid() != null
          && name.equals(data.getCuboid().getWorldName())) {
        return true;
      }
    }
    return false;
  }
}
