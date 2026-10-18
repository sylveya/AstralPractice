package dev.lokspel.practice.manager.arena.arenas.interfaces;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.Cuboid;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
public abstract class BasicArena {

  protected final String name;

  protected Location corner1;
  protected Location corner2;
  @Setter protected Cuboid cuboid;

  @Setter protected Location position1;
  @Setter protected Location position2;
  @Setter protected List<Location> ffaPositions = new ArrayList<>();

  @Setter protected boolean buildMax = false;

  @Setter
  protected int buildMaxValue = PracticeConfig.settings().getMatchSettings().getBuildLimitDefault();

  @Setter protected boolean deadZone = false;
  @Setter protected int deadZoneValue = 0;

  @Setter protected boolean available = true;

  protected BasicArena(String name) {
    this.name = name;
  }

  public void setCorner1(Location corner1) {
    if (this.corner2 != null
        && corner1 != null
        && !this.corner2.getWorld().equals(corner1.getWorld())) {
      this.corner2 = null;
      if (this.cuboid != null) {
        ArenaManager.getInstance().getArenaCuboids().remove(this.cuboid);
        this.cuboid = null;
      }
    }
    this.corner1 = corner1;
  }

  public void setCorner2(Location corner2) {
    if (this.corner1 != null
        && corner2 != null
        && !this.corner1.getWorld().equals(corner2.getWorld())) {
      throw new IllegalArgumentException("Both corners must be located in the same world");
    }
    this.corner2 = corner2;
  }

  public void createCuboid() {
    if (corner1 != null && corner2 != null) {
      if (!corner1.getWorld().equals(corner2.getWorld())) {
        return;
      }
      if (cuboid != null) {
        ArenaManager.getInstance().getArenaCuboids().remove(cuboid);
      }
      cuboid = new Cuboid(corner1, corner2);
      ArenaManager.getInstance().getArenaCuboids().put(cuboid, this);
    }
  }

  public Location getAvailableLocation() {
    if (position1 != null) {
      return position1;
    }
    if (position2 != null) {
      return position2;
    }
    if (!ffaPositions.isEmpty()) {
      return ffaPositions.stream().findAny().get();
    }
    if (corner1 != null) {
      return corner1;
    }
    if (corner2 != null) {
      return corner2;
    }
    return null;
  }

  public List<Location> getStandingLocations() {
    List<Location> locations = new ArrayList<>();
    if (position1 != null) {
      locations.add(position1);
    }
    if (position2 != null) {
      locations.add(position2);
    }
    locations.addAll(ffaPositions);
    return locations;
  }

  public boolean teleport(Player player) {
    Location location = getAvailableLocation();
    if (location == null) {
      Common.sendMmMessage(player, PracticeConfig.language().getArena().getNoAvailableLocation());
      return false;
    }

    player.setGameMode(GameMode.CREATIVE);
    player.setAllowFlight(true);
    player.setFlying(true);
    player.teleport(location);
    player.closeInventory();

    return true;
  }

  public void loadChunks() {
    if (this.cuboid != null) {
      Bukkit.getScheduler()
          .runTask(
              AstralPractice.getInstance(),
              () -> {
                ArenaUtil.loadArenaChunks(this);
              });
    }
  }
}
