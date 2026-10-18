package dev.lokspel.practice.manager.arena.listener;

import static dev.lokspel.practice.manager.arena.ArenaManager.LOADED_CHUNK_KEYS;
import static dev.lokspel.practice.manager.arena.ArenaManager.LOAD_CHUNKS;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.manager.arena.util.ArenaWorldUtil;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.server.ServerManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.Cuboid;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class ArenaListener implements Listener {

  @EventHandler
  public void onPlayerPortalEvent(PlayerPortalEvent e) {
    Player player = e.getPlayer();
    World world = player.getWorld();

    if (ArenaWorldUtil.getArenasCopyWorld() != null
        && world.equals(ArenaWorldUtil.getArenasCopyWorld())) {
      e.setCancelled(true);
    } else if (ArenaWorldUtil.getArenasWorld() != null
        && world.equals(ArenaWorldUtil.getArenasWorld())) {
      e.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBlockBreak(BlockBreakEvent e) {
    if (shouldCancelArenaEdit(e.getPlayer(), e.getBlock().getLocation())) {
      e.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBlockPlace(BlockPlaceEvent e) {
    if (shouldCancelArenaEdit(e.getPlayer(), e.getBlock().getLocation())) {
      e.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerInteract(PlayerInteractEvent e) {
    Action action = e.getAction();
    if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.LEFT_CLICK_BLOCK)) {
      return;
    }

    if (shouldCancelArenaEdit(e.getPlayer(), e.getClickedBlock().getLocation())) {
      e.setCancelled(true);
    }
  }

  private boolean shouldCancelArenaEdit(Player player, Location blockLoc) {
    Profile profile = ProfileManager.getInstance().getProfile(player);

    switch (profile.getStatus()) {
      case MATCH:
      case FFA:
      case EVENT:
      case SPECTATE:
        return false;

      default:
        break;
    }

    World playerWorld = player.getWorld();
    boolean cancelled = false;

    if (ArenaWorldUtil.getArenasCopyWorld() != null
        && playerWorld.equals(ArenaWorldUtil.getArenasCopyWorld())) {
      Common.sendMmMessage(player, PracticeConfig.language().getArena().getCantEditCopies());
      return true;
    }

    for (Cuboid cuboid : ArenaManager.getInstance().getArenaCuboids().keySet()) {
      if (cuboid.contains(blockLoc)) {
        BasicArena arena = ArenaManager.getInstance().getArenaCuboids().get(cuboid);
        Arena mainArena = ArenaUtil.getArena(arena);
        if (mainArena == null) {
          continue;
        }

        if (mainArena.isBuild() && !mainArena.getCopies().isEmpty()) {
          cancelled = true;
          Common.sendMmMessage(
              player,
              PracticeConfig.language()
                  .getArena()
                  .getCantEditArenaWithCopies()
                  .replace("%arena%", mainArena.getDisplayName()));
          break;
        } else if (!MatchManager.getInstance().getLiveMatchesByArena(arena).isEmpty()) {
          cancelled = true;
          Common.sendMmMessage(
              player,
              PracticeConfig.language()
                  .getArena()
                  .getCantEditArenaWithMatch()
                  .replace("%arena%", mainArena.getDisplayName()));
          break;
        }
      }
    }

    return cancelled;
  }

  @EventHandler
  public void onChunkUnload(ChunkUnloadEvent e) {
    if (LOAD_CHUNKS) {
      long chunkKey = ArenaUtil.chunkKey(e.getChunk().getX(), e.getChunk().getZ());
      if (LOADED_CHUNK_KEYS.contains(chunkKey)) {
        // Use addPluginChunkTicket to force-keep the chunk loaded.
        // This is safe from recursion (unlike getChunkAtAsync which can
        // trigger chunk scheduling → more unloads → StackOverflowError).
        e.getChunk().addPluginChunkTicket(AstralPractice.getInstance());
      }
    }
  }

  @EventHandler
  public void onWorldUnload(WorldUnloadEvent e) {
    World world = e.getWorld();
    if (ArenaWorldUtil.isArenaOrEventWorld(world)) {
      e.setCancelled(true);
      return;
    }

    if (ServerManager.getLobby() != null && ServerManager.getLobby().getWorld() == world) {
      e.setCancelled(true);
    }
  }
}
