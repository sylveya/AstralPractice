package dev.lokspel.practice.manager.spectator;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.Round;
import dev.lokspel.practice.manager.fight.match.enums.MatchStatus;
import dev.lokspel.practice.manager.fight.match.enums.MatchType;
import dev.lokspel.practice.manager.fight.util.stats.Statistic;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.util.Cuboid;
import dev.lokspel.practice.util.interfaces.Spectatable;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.projectiles.ProjectileSource;

public class SpectatorListener implements Listener {

  /**
   * Spectator restrictions apply to: - real spectator profiles, - Bukkit spectator game mode, -
   * dead or temp-dead players still inside active matches.
   */
  private boolean hasSpectatorRestrictions(Player player) {
    if (player.getGameMode() == GameMode.SPECTATOR) {
      return true;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return false;
    }

    if (profile.getStatus() == ProfileStatus.SPECTATE) {
      return true;
    }

    if (profile.getStatus() != ProfileStatus.MATCH) {
      return false;
    }

    Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
    if (match == null) {
      return false;
    }

    if (match.getType().equals(MatchType.DUEL)) {
      return false;
    }

    if (match.getStatus().equals(MatchStatus.END)) {
      return false;
    }

    Statistic stat = match.getCurrentStat(player);
    Round round = match.getCurrentRound();
    return (stat != null && stat.isSet())
        || (round != null && round.getTempKill(player) != null);
  }

  private void ensureSpectatorFlight(Player player) {
    if (!hasSpectatorRestrictions(player)) {
      return;
    }

    if (!player.getAllowFlight()) {
      player.setAllowFlight(true);
    }

    if (!player.isFlying()) {
      player.setFlying(true);
    }
  }

  @EventHandler
  public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
    if (e.getDamager() instanceof Player attacker && hasSpectatorRestrictions(attacker)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onEntityDamage(EntityDamageEvent e) {
    if (e.getEntity() instanceof Player player) {
      if (hasSpectatorRestrictions(player)) {
        e.setCancelled(true);
      }
    }
  }

  @EventHandler
  public void onVehicleDamage(VehicleDamageEvent e) {
    if (e.getAttacker() instanceof Player attacker && hasSpectatorRestrictions(attacker)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onVehicleDestroy(VehicleDestroyEvent e) {
    if (e.getAttacker() instanceof Player attacker && hasSpectatorRestrictions(attacker)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
    if (e.getRemover() instanceof Player remover && hasSpectatorRestrictions(remover)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent e) {
    if (hasSpectatorRestrictions(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
    if (hasSpectatorRestrictions(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
    if (hasSpectatorRestrictions(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent e) {
    Player player = e.getPlayer();
    ensureSpectatorFlight(player);

    // The client ignores ability packets sent during a teleport until it has
    // acknowledged the new position, so the flight state has to be re-sent after it.
    AstralPractice plugin = AstralPractice.getInstance();
    if (plugin != null && plugin.isEnabled()) {
      Bukkit.getScheduler().runTask(plugin, () -> ensureSpectatorFlight(player));
      Bukkit.getScheduler()
          .runTaskLater(
              plugin,
              () -> {
                // Skip if the player is no longer a spectator by the time this runs.
                if (player.isOnline() && hasSpectatorRestrictions(player)) {
                  player.setAllowFlight(false);
                  ensureSpectatorFlight(player);
                }
              },
              2L);
    }
  }

  @EventHandler
  public void onLeaveCuboid(PlayerMoveEvent e) {
    Player player = e.getPlayer();

    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (profile != null && profile.getStatus().equals(ProfileStatus.SPECTATE)) {
      Spectatable spectatable =
          SpectatorManager.getInstance().getSpectators().get(player.getUniqueId());
      if (spectatable != null) {
        Cuboid cuboid = spectatable.getCuboid();

        if (!cuboid.contains(e.getTo())) {
          player.teleport(cuboid.getCenter());
        }
      }
    }
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent e) {
    if (hasSpectatorRestrictions(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onBlockPlace(BlockPlaceEvent e) {
    if (hasSpectatorRestrictions(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onBucketEmpty(PlayerBucketEmptyEvent e) {
    if (hasSpectatorRestrictions(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    if (hasSpectatorRestrictions(player)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onItemDrop(PlayerDropItemEvent e) {
    if (hasSpectatorRestrictions(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onItemPickup(EntityPickupItemEvent e) {
    if (e.getEntity() instanceof Player player && hasSpectatorRestrictions(player)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onArrowPickup(PlayerPickupArrowEvent e) {
    if (hasSpectatorRestrictions(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onExpPickup(PlayerPickupExperienceEvent e) {
    if (hasSpectatorRestrictions(e.getPlayer())) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onProjectileLaunch(ProjectileLaunchEvent e) {
    ProjectileSource shooter = e.getEntity().getShooter();
    if (shooter instanceof Player player && hasSpectatorRestrictions(player)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onHunger(FoodLevelChangeEvent e) {
    if (!(e.getEntity() instanceof Player player)) {
      return;
    }

    if (hasSpectatorRestrictions(player)) {
      e.setFoodLevel(20);
    }
  }

  @EventHandler
  public void onCraft(CraftItemEvent e) {
    Player player = (Player) e.getWhoClicked();
    if (hasSpectatorRestrictions(player)) {
      e.setCancelled(true);
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    Player player = e.getPlayer();
    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (profile != null && profile.getStatus().equals(ProfileStatus.SPECTATE)) {
      Spectatable spectatable =
          SpectatorManager.getInstance().getSpectators().get(player.getUniqueId());
      if (spectatable != null) {
        spectatable.removeSpectator(player);
      }
    }
  }
}
