package dev.lokspel.practice.manager.arena.util;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockTypes;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.Cuboid;
import org.bukkit.Location;

public enum FaweUtil {
  ;

  public static void deleteFawe(final Cuboid cuboid) {
    try (EditSession editSession =
        WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(cuboid.getWorld()))) {
      editSession.setBlocks(
          (Region)
              new CuboidRegion(
                  BukkitAdapter.adapt(cuboid.getLowerNe()).toBlockPoint(),
                  BukkitAdapter.adapt(cuboid.getUpperSw()).toBlockPoint()),
          BlockTypes.AIR);
      editSession.flushQueue();
    } catch (Exception e) {
      Common.sendConsoleMmMessage("<red>Error during delete operation: " + e.getMessage());
    }
  }

  public static boolean copyFaweWithResult(
      final Cuboid copyFrom, final Location reference, final Location newLocation) {
    int targetX = copyFrom.getLowerX() - reference.getBlockX() + newLocation.getBlockX();
    int targetY = copyFrom.getLowerY() - reference.getBlockY() + newLocation.getBlockY();
    int targetZ = copyFrom.getLowerZ() - reference.getBlockZ() + newLocation.getBlockZ();
    Location destination = new Location(newLocation.getWorld(), targetX, targetY, targetZ);

    try {
      ForwardExtentCopy forwardExtentCopy =
          new ForwardExtentCopy(
              BukkitAdapter.adapt(copyFrom.getWorld()),
              new CuboidRegion(
                  BukkitAdapter.adapt(copyFrom.getLowerNe()).toBlockPoint(),
                  BukkitAdapter.adapt(copyFrom.getUpperSw()).toBlockPoint()),
              BukkitAdapter.adapt(newLocation.getWorld()),
              BukkitAdapter.adapt(destination).toBlockPoint());

      forwardExtentCopy.setCopyingEntities(true);

      Operations.complete(forwardExtentCopy);
      return true;

    } catch (Exception e) {
      Common.sendConsoleMmMessage("<red>Error during copy-paste operation: " + e.getMessage());
      return false;
    }
  }
}
