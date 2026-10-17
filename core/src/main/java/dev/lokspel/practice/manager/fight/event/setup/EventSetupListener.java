package dev.lokspel.practice.manager.fight.event.setup;

import dev.lokspel.api.event.event.EventEndEvent;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.util.ArenaWorldUtil;
import dev.lokspel.practice.manager.fight.event.interfaces.Event;
import dev.lokspel.practice.manager.fight.event.interfaces.EventData;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.event.EventSetupManager;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class EventSetupListener implements Listener {

  private final EventWandSetupManager setupManager;

  /**
   * Paper 1.9+ fires {@link PlayerInteractEvent} twice per physical click – once for the main hand
   * and once for the off hand. We track which players have already been processed this tick and
   * skip any duplicate invocation within the same tick.
   */
  private final Set<UUID> interactCooldown = new HashSet<>();

  private final Set<UUID> entityInteractCooldown = new HashSet<>();
  private final Set<UUID> markerDamageCooldown = new HashSet<>();

  public EventSetupListener(EventWandSetupManager setupManager) {
    this.setupManager = setupManager;
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (event.getHand() != EquipmentSlot.HAND) {
      return;
    }

    Player player = event.getPlayer();

    if (!setupManager.isSettingUp(player) || !setupManager.isSetupWand(event.getItem())) {
      return;
    }

    event.setCancelled(true);

    // Paper fires PlayerInteractEvent twice per click (main hand + off hand).
    // The first call adds the UUID; the second call finds it already present and bails out.
    // The UUID is removed at the end of the same tick so the next click is processed normally.
    if (!interactCooldown.add(player.getUniqueId())) {
      return;
    }
    Bukkit.getScheduler()
        .runTask(AstralPractice.getInstance(), () -> interactCooldown.remove(player.getUniqueId()));

    EventWandSetupManager.SetupSession session = setupManager.getSession(player);
    EventData eventData = session.getEventData();

    if (eventData == null) {
      Common.sendMmMessage(player, "<red>Event not found!");
      setupManager.stopSetup(player);
      return;
    }

    Action action = event.getAction();

    if (player.isSneaking()) {
      handleModeSwitch(player, session, action);
      return;
    }

    EventSetupMode currentMode = session.getCurrentMode();

    boolean isAirAllowed =
        (currentMode == EventSetupMode.TOGGLE_STATUS || currentMode == EventSetupMode.SPAWN_POINTS);

    if (!isAirAllowed) {
      if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) {
        return;
      }
    } else {
      if (action == Action.PHYSICAL) {
        return;
      }
    }

    switch (currentMode) {
      case CORNERS -> handleCornerSelection(player, eventData, action, event);
      case SPAWN_POINTS -> handleSpawnPoints(player, eventData, action, event);
      case TOGGLE_STATUS -> handleToggleStatus(player, eventData, action);

      default -> {}
    }
  }

  @EventHandler
  public void onPlayerLeave(PlayerQuitEvent e) {
    Player player = e.getPlayer();

    if (setupManager.isSettingUp(player)) {
      setupManager.stopSetup(player);
    }
  }

  @EventHandler
  public void onPlayerDropItem(PlayerDropItemEvent e) {
    Player player = e.getPlayer();

    if (!setupManager.isSettingUp(player)) {
      return;
    }

    if (setupManager.isSetupWand(e.getItemDrop().getItemStack())) {
      e.getItemDrop().remove();
      setupManager.stopSetup(player);
    }
  }

  @EventHandler
  public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
    if (event.getHand() != EquipmentSlot.HAND) {
      return;
    }

    Player player = event.getPlayer();
    if (!setupManager.isSettingUp(player)
        || !setupManager.isSetupWand(player.getInventory().getItemInMainHand())) {
      return;
    }

    if (event.getRightClicked() instanceof Mannequin mannequin
        && EventSpawnMarkerManager.getInstance().isMarker(mannequin)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
    if (event.getHand() != EquipmentSlot.HAND) {
      return;
    }

    Player player = event.getPlayer();

    if (!setupManager.isSettingUp(player)
        || !setupManager.isSetupWand(player.getInventory().getItemInMainHand())) {
      return;
    }

    if (!entityInteractCooldown.add(player.getUniqueId())) {
      return;
    }
    Bukkit.getScheduler()
        .runTask(
            AstralPractice.getInstance(),
            () -> entityInteractCooldown.remove(player.getUniqueId()));

    EventWandSetupManager.SetupSession session = setupManager.getSession(player);
    if (session == null) {
      return;
    }

    if (session.getCurrentMode() != EventSetupMode.SPAWN_POINTS) {
      return;
    }

    if (!(event.getRightClicked() instanceof Mannequin mannequin)) {
      return;
    }

    event.setCancelled(true);

    EventData eventData = session.getEventData();
    if (eventData == null) {
      return;
    }

    EventSpawnMarkerManager markerManager = EventSpawnMarkerManager.getInstance();

    if (!markerManager.isMarker(mannequin)) {
      return;
    }

    if (!markerManager.isSpawnMarker(mannequin)) {
      return;
    }

    int spawnIndex = markerManager.getSpawnIndex(mannequin);
    if (spawnIndex < 0 || spawnIndex >= eventData.getSpawns().size()) {
      markerManager.updateMarkers(eventData);
      updateGui(eventData);
      Common.sendMmMessage(
          player, "<red>This spawn marker is outdated. Markers have been refreshed.");
      return;
    }

    eventData.getSpawns().remove(spawnIndex);
    markerManager.updateMarkers(eventData);
    updateGui(eventData);
    scheduleSave(eventData);

    Common.sendMmMessage(
        player,
        "<green>Removed spawn point #"
            + (spawnIndex + 1)
            + ". Remaining: "
            + eventData.getSpawns().size());
  }

  // Prevent damage to marker mannequins
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onMarkerDamage(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Mannequin mannequin)) {
      return;
    }

    if (!EventSpawnMarkerManager.getInstance().isMarker(mannequin)) {
      return;
    }

    // Cancel damage in all cases
    event.setCancelled(true);

    // Check if the damager is a player (left-click)
    if (!(event.getDamager() instanceof Player player)) {
      return;
    }

    // Some client/server combos can emit duplicate damage callbacks for one swing.
    if (!markerDamageCooldown.add(player.getUniqueId())) {
      return;
    }
    Bukkit.getScheduler()
        .runTask(
            AstralPractice.getInstance(), () -> markerDamageCooldown.remove(player.getUniqueId()));

    // Check if player is in setup mode
    if (!setupManager.isSettingUp(player)) {
      return;
    }

    EventWandSetupManager.SetupSession session = setupManager.getSession(player);
    if (session == null) {
      return;
    }

    EventData eventData = session.getEventData();
    if (eventData == null) {
      return;
    }

    // Check if in correct mode
    if (session.getCurrentMode() != EventSetupMode.SPAWN_POINTS) {
      return;
    }

    // Left-click on mannequin marker = Remove that specific spawn
    EventSpawnMarkerManager markerManager = EventSpawnMarkerManager.getInstance();
    if (!markerManager.isSpawnMarker(mannequin)) {
      return;
    }

    int spawnIndex = markerManager.getSpawnIndex(mannequin);
    if (spawnIndex < 0 || spawnIndex >= eventData.getSpawns().size()) {
      markerManager.updateMarkers(eventData);
      updateGui(eventData);
      Common.sendMmMessage(
          player, "<red>This spawn marker is outdated. Markers have been refreshed.");
      return;
    }

    eventData.getSpawns().remove(spawnIndex);
    markerManager.updateMarkers(eventData);
    updateGui(eventData);
    scheduleSave(eventData);

    Common.sendMmMessage(
        player,
        "<green>Removed spawn point #"
            + (spawnIndex + 1)
            + ". Remaining: "
            + eventData.getSpawns().size());
  }

  @EventHandler
  public void onEventEnd(EventEndEvent event) {
    // When an event ends, clean up any spawn markers for that event's EventData
    // This prevents markers from persisting after the event is stopped
    if (event.getEvent() instanceof Event practiceEvent) {
      EventData eventData = practiceEvent.getEventData();
      if (eventData != null) {
        EventSpawnMarkerManager.getInstance().clearMarkers(eventData);
      }
    }
  }

  private void handleModeSwitch(
      Player player, EventWandSetupManager.SetupSession session, Action action) {
    if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
      session.setCurrentMode(setupManager.getNextMode(session.getCurrentMode()));
    } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
      session.setCurrentMode(setupManager.getPreviousMode(session.getCurrentMode()));
    }

    setupManager.updateWand(player);
    Common.sendMmMessage(
        player, "<yellow>Switched to mode: <white>" + session.getCurrentMode().getDisplayName());
  }

  private void handleCornerSelection(
      Player player, EventData eventData, Action action, PlayerInteractEvent event) {
    if (eventData.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getSetup().getEvent().getCantEditEnabled());
      return;
    }

    Block targetBlock = event.getClickedBlock();
    if (targetBlock == null || targetBlock.getType().equals(Material.AIR)) {
      Common.sendMmMessage(player, "<red>Block location cannot be found!");
      return;
    }

    Location cornerLocation = targetBlock.getLocation();
    if (!cornerLocation.getWorld().equals(ArenaWorldUtil.getArenasWorld())) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getSetup().getEvent().getArenasWorld());
      return;
    }

    int cornerId = (action == Action.LEFT_CLICK_BLOCK) ? 1 : 2;
    if (cornerId == 1) {
      eventData.setCuboidLoc1(cornerLocation);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getSetup()
              .getEvent()
              .getSetFirstCorner()
              .replace("%event%", eventData.getType().getName()));
    } else {
      eventData.setCuboidLoc2(cornerLocation);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getSetup()
              .getEvent()
              .getSetSecondCorner()
              .replace("%event%", eventData.getType().getName()));
    }

    updateGui(eventData);
    scheduleSave(eventData);
  }

  private void handleSpawnPoints(
      Player player, EventData eventData, Action action, PlayerInteractEvent event) {
    if (eventData.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getSetup().getEvent().getCantEditEnabled());
      return;
    }

    if (eventData.getCuboid() == null) {
      Common.sendMmMessage(player, "<red>You must set corners first!");
      return;
    }

    // Right-click on a block to add a spawn point
    if (action == Action.RIGHT_CLICK_BLOCK) {
      Block block = event.getClickedBlock();
      if (block == null) {
        return;
      }

      Location spawnLoc = getSnappedLocation(block, player);

      if (!eventData.getCuboid().contains(spawnLoc)) {
        Common.sendMmMessage(player, "<red>Spawn point must be within the event cuboid!");
        return;
      }

      try {
        eventData.addSpawn(spawnLoc);
      } catch (IllegalStateException ex) {
        // Ignore duplicate/invalid add attempts and show the setup error to the player.
        Common.sendMmMessage(player, "<red>" + ex.getMessage());
        return;
      }

      EventSpawnMarkerManager.getInstance().updateMarkers(eventData);
      updateGui(eventData);
      scheduleSave(eventData);

      Common.sendMmMessage(
          player,
          "<green>Added spawn point #" + eventData.getSpawns().size() + " at your location.");
    }
    // Left-click anywhere to remove the last spawn point
    else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
      if (!eventData.getSpawns().isEmpty()) {
        int index = eventData.getSpawns().size() - 1;
        eventData.getSpawns().remove(index);
        EventSpawnMarkerManager.getInstance().updateMarkers(eventData);
        updateGui(eventData);
        scheduleSave(eventData);
        Common.sendMmMessage(player, "<red>Removed last spawn point. Remaining: " + index);
      } else {
        Common.sendMmMessage(player, "<red>No spawn points to remove.");
      }
    }
  }

  private void handleToggleStatus(Player player, EventData eventData, Action action) {
    if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    try {
      if (eventData.isEnabled()) {
        eventData.setEnabled(false);
        Common.sendMmMessage(
            player, "<red>Disabled event: <yellow>" + eventData.getType().getName());
      } else {
        if (eventData.getCuboidLoc1() == null || eventData.getCuboidLoc2() == null) {
          Common.sendMmMessage(player, "<red>You must set both corners first!");
          return;
        }
        if (eventData.getSpawns().isEmpty()) {
          Common.sendMmMessage(player, "<red>You must set at least one spawn point!");
          return;
        }

        eventData.setEnabled(true);
        Common.sendMmMessage(
            player, "<green>Enabled event: <yellow>" + eventData.getType().getName());

        // End setup mode for all players currently setting up this event
        List<Player> playersSettingUp =
            new ArrayList<>(setupManager.getPlayersSettingUpEvent(eventData));
        for (Player settingUpPlayer : playersSettingUp) {
          setupManager.stopSetup(settingUpPlayer);
        }
      }

      updateGui(eventData);
      scheduleSave(eventData);
    } catch (Exception e) {
      Common.sendMmMessage(player, "<red>Error toggling status: " + e.getMessage());
    }
  }

  private void updateGui(EventData eventData) {
    if (EventSetupManager.getInstance().getEventSetupGuis().containsKey(eventData)) {
      if (EventSetupManager.getInstance()
          .getEventSetupGuis()
          .get(eventData)
          .containsKey(GuiType.Event_Main)) {
        EventSetupManager.getInstance()
            .getEventSetupGuis()
            .get(eventData)
            .get(GuiType.Event_Main)
            .update();
      }
      GuiManager.getInstance().getGuis().get(GuiType.Event_Summary).update();
    }
  }

  private static Location getSnappedLocation(Block clickedBlock, Player player) {
    Location loc = clickedBlock.getLocation().add(0.5, 1, 0.5);
    float snappedYaw = Math.round(player.getLocation().getYaw() / 45f) * 45f;
    loc.setYaw(snappedYaw);
    loc.setPitch(0.0f);
    return loc;
  }

  private static void scheduleSave(EventData eventData) {
    Bukkit.getScheduler().runTaskAsynchronously(AstralPractice.getInstance(), eventData::setData);
  }
}
