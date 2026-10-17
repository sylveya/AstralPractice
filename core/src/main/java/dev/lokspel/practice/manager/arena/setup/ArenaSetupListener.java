package dev.lokspel.practice.manager.arena.setup;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.manager.arena.util.ArenaWorldUtil;
import dev.lokspel.practice.manager.arena.util.BedLocation;
import dev.lokspel.practice.manager.arena.util.PortalLocation;
import dev.lokspel.practice.manager.fight.util.BedUtil;
import dev.lokspel.practice.manager.fight.util.BlockUtil;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.Cuboid;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class ArenaSetupListener implements Listener {

  private final ArenaSetupManager setupManager;

  private final Set<UUID> interactCooldown = new HashSet<>();
  private final Set<UUID> entityInteractCooldown = new HashSet<>();
  private final Set<UUID> markerDamageCooldown = new HashSet<>();

  public ArenaSetupListener(ArenaSetupManager setupManager) {
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

    if (!interactCooldown.add(player.getUniqueId())) {
      return;
    }
    Bukkit.getScheduler()
        .runTask(AstralPractice.getInstance(), () -> interactCooldown.remove(player.getUniqueId()));

    ArenaSetupManager.SetupSession session = setupManager.getSession(player);
    DisplayArena displayArena = session.getArena();

    if (displayArena == null) {
      Common.sendMmMessage(player, "<red>Arena not found!");
      setupManager.stopSetup(player);
      return;
    }

    Action action = event.getAction();

    if (player.isSneaking()) {
      handleModeSwitch(player, session, displayArena, action);
      return;
    }

    SetupMode currentMode = session.getCurrentMode();

    boolean isAirAllowed =
        (currentMode == SetupMode.BUILD_MAX
            || currentMode == SetupMode.DEAD_ZONE
            || currentMode == SetupMode.TOGGLE_STATUS
            || currentMode
                == SetupMode
                    .FFA_POSITIONS // Allow air clicks for FFA (left-click anywhere to remove last)
        );

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
      case CORNERS -> handleCornerSelection(player, displayArena, action, event);
      case POSITIONS -> handleStandardPositions(player, (Arena) displayArena, action, event);
      case FFA_POSITIONS -> handleFfaPositions(player, (FfaArena) displayArena, action, event);
      case BUILD_MAX -> handleBuildMax(player, displayArena, action);
      case DEAD_ZONE -> handleDeadZone(player, displayArena, action);
      case BED_LOCATIONS -> handleBedLocations(player, (Arena) displayArena, action, event);
      case PORTALS -> handlePortals(player, (Arena) displayArena, action, event);
      case TOGGLE_STATUS -> handleToggleStatus(player, displayArena, action);

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

  private void handleModeSwitch(
      Player player, ArenaSetupManager.SetupSession session, DisplayArena arena, Action action) {
    if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
      session.setCurrentMode(setupManager.getNextMode(arena, session.getCurrentMode()));
    } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
      session.setCurrentMode(setupManager.getPreviousMode(arena, session.getCurrentMode()));
    } else {
      return;
    }
    setupManager.updateWand(player);
    Common.sendMmMessage(
        player, "<yellow>Switched to: <white>" + session.getCurrentMode().getDisplayName());
  }

  private void handleStandardPositions(
      Player player, Arena arena, Action action, PlayerInteractEvent event) {
    if (isEditBlocked(player, arena)) {
      return;
    }
    if (isRegionMissing(player, arena)) {
      return;
    }

    Location loc = getSnappedLocation(event.getClickedBlock(), player);

    if (isOutsideRegion(player, arena, loc)) {
      return;
    }

    // DeadZone check
    if (arena.isDeadZone() && (loc.getY() - 1) <= arena.getDeadZoneValue()) {
      arena.setDeadZone(false);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPosition()
              .getDeadzoneToHigh()
              .replace("%arena%", arena.getName()));
    }

    if (action == Action.LEFT_CLICK_BLOCK) {
      // Check minimum distance from position 2
      if (arena.getPosition2() != null && arena.getPosition2().distance(loc) < 1.0) {
        Common.sendMmMessage(player, "<red>Spawn positions must be at least 1 block apart!");
        return;
      }
      arena.setPosition1(loc);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPosition()
              .getSavedPosition()
              .replace("%arena%", arena.getName())
              .replace("%position%", "1."));
    } else {
      // Check minimum distance from position 1
      if (arena.getPosition1() != null && arena.getPosition1().distance(loc) < 1.0) {
        Common.sendMmMessage(player, "<red>Spawn positions must be at least 1 block apart!");
        return;
      }
      arena.setPosition2(loc);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPosition()
              .getSavedPosition()
              .replace("%arena%", arena.getName())
              .replace("%position%", "2."));
    }
    SpawnMarkerManager.getInstance().updateMarkers(arena);
    updateGui(arena);
  }

  private void handleFfaPositions(
      Player player, FfaArena ffaArena, Action action, PlayerInteractEvent event) {
    if (isEditBlocked(player, ffaArena)) {
      return;
    }

    if (action == Action.RIGHT_CLICK_BLOCK) {
      // Add new spawn
      if (isRegionMissing(player, ffaArena)) {
        return;
      }

      Location loc = getSnappedLocation(event.getClickedBlock(), player);
      if (isOutsideRegion(player, ffaArena, loc)) {
        return;
      }

      if (ffaArena.getFfaPositions().size() >= 18) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getFfaPositions()
                .getMax18()
                .replace("%arena%", ffaArena.getName()));
        return;
      }

      // Check minimum distance from all existing spawns (must be at least 1 block apart)
      for (Location existingSpawn : ffaArena.getFfaPositions()) {
        if (existingSpawn.distance(loc) < 1.0) {
          Common.sendMmMessage(
              player, "<red>Spawn positions must be at least 1 block apart from each other!");
          Common.sendMmMessage(player, "<gray>Too close to an existing spawn position.");
          return;
        }
      }

      ffaArena.getFfaPositions().add(loc);
      SpawnMarkerManager.getInstance().updateMarkers(ffaArena);
      updateGui(ffaArena);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getFfaPositions()
              .getSetFfapos()
              .replace("%arena%", ffaArena.getName())
              .replace("%posCount%", String.valueOf(ffaArena.getFfaPositions().size())));

    } else if (action == Action.LEFT_CLICK_BLOCK || action == Action.LEFT_CLICK_AIR) {
      // Left-click anywhere: Remove last spawn
      if (!ffaArena.getFfaPositions().isEmpty()) {
        int index = ffaArena.getFfaPositions().size() - 1;
        ffaArena.getFfaPositions().remove(index);
        SpawnMarkerManager.getInstance().updateMarkers(ffaArena);
        updateGui(ffaArena);
        Common.sendMmMessage(player, "<red>Removed last FFA spawn point. Remaining: " + index);
      } else {
        Common.sendMmMessage(player, "<red>No spawn points to remove.");
      }
    }
  }

  private void handleCornerSelection(
      Player player, DisplayArena arena, Action action, PlayerInteractEvent event) {
    if (isEditBlocked(player, arena)) {
      return;
    }

    Block targetBlock = event.getClickedBlock();
    if (targetBlock == null || targetBlock.getType().equals(Material.AIR)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getCorner()
              .getNoBlock());
      return;
    }

    Location cornerLocation = targetBlock.getLocation();
    if (!cornerLocation.getWorld().equals(ArenaWorldUtil.getArenasWorld())) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getCorner()
              .getCornerWorld());
      return;
    }

    int cornerId = (action == Action.LEFT_CLICK_BLOCK) ? 1 : 2;
    if (cornerId == 1) {
      arena.setCorner1(cornerLocation);
    } else {
      arena.setCorner2(cornerLocation);
    }

    arena.createCuboid();
    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getArena()
            .getArguments()
            .getCorner()
            .getSavedCorner()
            .replace("%arena%", arena.getName())
            .replace("%corner%", cornerId + "."));

    cleanupAfterRegionChange(player, arena);
  }

  private void handleBedLocations(
      Player player, Arena arena, Action action, PlayerInteractEvent event) {
    if (isEditBlocked(player, arena)) {
      return;
    }
    if (isRegionMissing(player, arena)) {
      return;
    }

    Block block = event.getClickedBlock();
    if (block == null) {
      return;
    }

    if (!BlockUtil.isBedMaterial(block.getType())) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getBed()
              .getNoBed()
              .replace("%arena%", arena.getName()));
      return;
    }

    if (isOutsideRegion(player, arena, block.getLocation())) {
      return;
    }

    BedLocation bedLocation = BedUtil.getBedLocation(block);

    if (action == Action.LEFT_CLICK_BLOCK) {
      arena.setBedLoc1(bedLocation);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getBed()
              .getSetBed1()
              .replace("%arena%", arena.getName()));
    } else {
      arena.setBedLoc2(bedLocation);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getBed()
              .getSetBed2()
              .replace("%arena%", arena.getName()));
    }
    updateGui(arena);
  }

  private void handlePortals(Player player, Arena arena, Action action, PlayerInteractEvent event) {
    if (isEditBlocked(player, arena)) {
      return;
    }
    if (!arena.isBuild()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPortal()
              .getNotBuild()
              .replace("%arena%", arena.getName()));
      return;
    }
    if (isRegionMissing(player, arena)) {
      return;
    }

    Block block = event.getClickedBlock();
    if (block == null || block.getType().equals(Material.AIR)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPortal()
              .getNoBlock()
              .replace("%arena%", arena.getName()));
      return;
    }

    Location portalLoc = block.getLocation();
    if (isOutsideRegion(player, arena, portalLoc)) {
      return;
    }

    // Determine effective floor: dead zone takes priority, otherwise lowest arena Y
    int floorY;
    String floorType;
    if (arena.isDeadZone()) {
      floorY = arena.getDeadZoneValue();
      floorType = "dead zone";
    } else {
      floorY = arena.getCuboid().getLowerY();
      floorType = "lowest arena Y";
    }

    // Portal center must be more than 1 block above the floor
    if (portalLoc.getBlockY() <= floorY + 1) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPortal()
              .getTooLowForFloor()
              .replace("%floor_type%", floorType)
              .replace("%floor_y%", String.valueOf(floorY)));
      return;
    }

    PortalLocation portalLocation = new PortalLocation(portalLoc);

    if (action == Action.LEFT_CLICK_BLOCK) {
      if (arena.getPortalLoc2() != null && arena.getPortalLoc2().isOverlap(portalLocation)) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getPortal()
                .getPortalOverlap()
                .replace("%arena%", arena.getName()));
        return;
      }
      arena.setPortalLoc1(portalLocation);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPortal()
              .getSetPortal1()
              .replace("%arena%", arena.getName()));
    } else {
      if (arena.getPortalLoc1() != null && arena.getPortalLoc1().isOverlap(portalLocation)) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getPortal()
                .getPortalOverlap()
                .replace("%arena%", arena.getName()));
        return;
      }
      arena.setPortalLoc2(portalLocation);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPortal()
              .getSetPortal2()
              .replace("%arena%", arena.getName()));
    }
    portalLocation.setPortal();
    updateGui(arena);
  }

  private void handleBuildMax(Player player, DisplayArena arena, Action action) {
    if (isEditBlocked(player, arena)) {
      return;
    }

    if (action.name().contains("LEFT")) {
      arena.setBuildMax(false);
      arena.setBuildMaxValue(PracticeConfig.settings().getMatchSettings().getBuildLimitDefault());
      arena.setData();
      Common.sendMmMessage(
          player,
          "<red>Build Height Limit disabled for "
              + arena.getName()
              + " <gray>(now relative: spawn Y + "
              + PracticeConfig.settings().getMatchSettings().getBuildLimitDefault()
              + " blocks — use right-click to set an absolute limit)");
      return;
    }

    // Set
    if (isRegionMissing(player, arena)) {
      return;
    }
    Location pos = player.getLocation();
    if (isOutsideRegion(player, arena, pos)) {
      return;
    }

    arena.setBuildMaxValue(pos.getBlockY());
    arena.setBuildMax(true);
    arena.setData();

    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getArena()
            .getArguments()
            .getSetbuildmax()
            .getSetBuildmax()
            .replace("%arena%", arena.getName())
            .replace("%y-level%", String.valueOf(pos.getBlockY())));
  }

  private void handleDeadZone(Player player, DisplayArena arena, Action action) {
    if (isEditBlocked(player, arena)) {
      return;
    }

    if (action.name().contains("LEFT")) {
      arena.setDeadZone(false);
      Common.sendMmMessage(player, "<red>Dead Zone disabled for " + arena.getName());
      return;
    }

    // Set
    if (isRegionMissing(player, arena)) {
      return;
    }
    Location pos = player.getLocation();
    if (isOutsideRegion(player, arena, pos)) {
      return;
    }

    int deadZoneY = pos.getBlockY();

    // Check against spawns
    List<Location> spawnPositions = new ArrayList<>();
    if (arena instanceof Arena a) {
      if (a.getPosition1() != null) {
        spawnPositions.add(a.getPosition1());
      }
      if (a.getPosition2() != null) {
        spawnPositions.add(a.getPosition2());
      }
    } else if (arena instanceof FfaArena f) {
      spawnPositions.addAll(f.getFfaPositions());
    }

    for (Location spawnPosition : spawnPositions) {
      if ((spawnPosition.getY() - 1) <= deadZoneY) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getSetdeadzone()
                .getLowerThanSpawn());
        return;
      }
    }

    arena.setDeadZoneValue(deadZoneY);
    arena.setDeadZone(true);

    // Invalidate portals that are now at or below the new dead zone floor
    if (arena instanceof Arena a) {
      if (a.getPortalLoc1() != null && a.getPortalLoc1().getCenter().getBlockY() <= deadZoneY + 1) {
        a.setPortalLoc1(null);
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getPortal()
                .getTooLowForFloor()
                .replace("%floor_type%", "dead zone (portal 1 removed)")
                .replace("%floor_y%", String.valueOf(deadZoneY)));
      }
      if (a.getPortalLoc2() != null && a.getPortalLoc2().getCenter().getBlockY() <= deadZoneY + 1) {
        a.setPortalLoc2(null);
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getPortal()
                .getTooLowForFloor()
                .replace("%floor_type%", "dead zone (portal 2 removed)")
                .replace("%floor_y%", String.valueOf(deadZoneY)));
      }
    }

    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getArena()
            .getArguments()
            .getSetdeadzone()
            .getSetDeadzone()
            .replace("%arena%", arena.getName())
            .replace("%y-level%", String.valueOf(deadZoneY)));
  }

  private void handleToggleStatus(Player player, DisplayArena arena, Action action) {
    if (action.name().contains("RIGHT")) {
      if (arena.isEnabled()) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getEnable()
                .getAlreadyEnabled()
                .replace("%arena%", arena.getName()));
        return;
      }

      if (ArenaUtil.changeStatus(player, arena)) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getEnable()
                .getArenaEnabled()
                .replace("%arena%", arena.getName()));
        updateGui(arena);
      }
    }
  }

  private static boolean isEditBlocked(Player player, DisplayArena arena) {
    if (arena.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPosition()
              .getCantEdit2()
              .replace("%arena%", arena.getName()));
      return true;
    }
    if (arena instanceof Arena && ((Arena) arena).hasCopies()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPosition()
              .getCantEdit()
              .replace("%arena%", arena.getName()));
      return true;
    }
    return false;
  }

  private static boolean isRegionMissing(Player player, DisplayArena arena) {
    if (arena.getCuboid() == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPosition()
              .getNoRegion()
              .replace("%arena%", arena.getName()));
      return true;
    }
    return false;
  }

  private static boolean isOutsideRegion(Player player, DisplayArena arena, Location loc) {
    if (!arena.getCuboid().contains(loc)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getPosition()
              .getPosOutsideRegion()
              .replace("%arena%", arena.getName()));
      return true;
    }
    return false;
  }

  private static Location getSnappedLocation(Block clickedBlock, Player player) {
    Location loc = clickedBlock.getLocation().add(0.5, 1, 0.5);
    float snappedYaw = Math.round(player.getLocation().getYaw() / 45f) * 45f;
    loc.setYaw(snappedYaw);
    loc.setPitch(0.0f);
    return loc;
  }

  private static void updateGui(DisplayArena arena) {
    if (ArenaGuiSetupManager.getInstance().getArenaSetupGuis().containsKey(arena)) {
      ArenaGuiSetupManager.getInstance()
          .getArenaSetupGuis()
          .get(arena)
          .get(GuiType.Arena_Main)
          .update();
    }
  }

  private static void cleanupAfterRegionChange(Player player, DisplayArena arena) {
    Cuboid cuboid = arena.getCuboid();
    if (cuboid == null) {
      updateGui(arena);
      return;
    }

    if (arena.getPosition1() != null && !cuboid.contains(arena.getPosition1())) {
      arena.setPosition1(null);
      arena.setEnabled(false);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getCorner()
              .getPositionRemoved()
              .replace("%arena%", arena.getName())
              .replace("%position%", "1"));
    }
    // Clean Position 2
    if (arena.getPosition2() != null && !cuboid.contains(arena.getPosition2())) {
      arena.setPosition2(null);
      arena.setEnabled(false);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getCorner()
              .getPositionRemoved()
              .replace("%arena%", arena.getName())
              .replace("%position%", "2"));
    }
    // Clean FFA Positions
    if (!arena.getFfaPositions().isEmpty()) {
      arena.getFfaPositions().removeIf(location -> !cuboid.contains(location));
    }
    // Adjust BuildMax if outside cuboid bounds
    if (arena.isBuildMax()) {
      int buildMaxValue = arena.getBuildMaxValue();
      if (buildMaxValue < cuboid.getLowerY() || buildMaxValue > cuboid.getUpperY()) {
        // Clamp build max to cuboid bounds instead of resetting to relative mode
        int clampedValue =
            Math.max(cuboid.getLowerY(), Math.min(buildMaxValue, cuboid.getUpperY()));
        arena.setBuildMaxValue(clampedValue);
        // Keep it in absolute mode (buildMax stays true)
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getCorner()
                .getBuildMaxAdjusted()
                .replace("%arena%", arena.getName())
                .replace("%old%", String.valueOf(buildMaxValue))
                .replace("%new%", String.valueOf(clampedValue)));
      }
    }
    // Clean DeadZone
    if (arena.isDeadZone()
        && (cuboid.getLowerY() > arena.getDeadZoneValue()
            || arena.getDeadZoneValue() > cuboid.getUpperY())) {
      arena.setDeadZone(false);
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getCorner()
              .getDeadZoneRemoved()
              .replace("%arena%", arena.getName()));
    }
    // Clean Portals — must remain at least 1 block above the effective floor Y
    if (arena instanceof Arena a) {
      int floorY = a.isDeadZone() ? a.getDeadZoneValue() : cuboid.getLowerY();
      if (a.getPortalLoc1() != null && a.getPortalLoc1().getCenter().getBlockY() <= floorY + 1) {
        a.setPortalLoc1(null);
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getPortal()
                .getTooLowForFloor()
                .replace("%floor_type%", "new arena floor (portal 1 removed)")
                .replace("%floor_y%", String.valueOf(floorY)));
      }
      if (a.getPortalLoc2() != null && a.getPortalLoc2().getCenter().getBlockY() <= floorY + 1) {
        a.setPortalLoc2(null);
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getPortal()
                .getTooLowForFloor()
                .replace("%floor_type%", "new arena floor (portal 2 removed)")
                .replace("%floor_y%", String.valueOf(floorY)));
      }
    }
    // Clean Beds
    if (arena instanceof Arena a && ArenaUtil.isArenaBedRelated(a)) {
      if (a.getBedLoc1() != null && !cuboid.contains(a.getBedLoc1().getLocation())) {
        a.setBedLoc1(null);
        a.setEnabled(false);
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getCorner()
                .getBedRemoved()
                .replace("%arena%", arena.getName())
                .replace("%bed%", "1"));
      }
      if (a.getBedLoc2() != null && !cuboid.contains(a.getBedLoc2().getLocation())) {
        a.setBedLoc2(null);
        a.setEnabled(false);
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getArena()
                .getArguments()
                .getCorner()
                .getBedRemoved()
                .replace("%arena%", arena.getName())
                .replace("%bed%", "2"));
      }
    }

    // Update spawn markers after cleanup to remove markers for deleted positions
    SpawnMarkerManager.getInstance().updateMarkers(arena);

    updateGui(arena);
  }

  // Handle right-clicking mannequin markers to remove them (FFA ONLY)
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onMarkerInteract(PlayerInteractAtEntityEvent event) {
    if (event.getHand() != EquipmentSlot.HAND) {
      return;
    }

    if (!(event.getRightClicked() instanceof Mannequin mannequin)) {
      return;
    }

    Player player = event.getPlayer();

    // Check if this is a spawn marker
    if (!SpawnMarkerManager.getInstance().isMarker(mannequin)) {
      return;
    }

    event.setCancelled(true);

    if (!entityInteractCooldown.add(player.getUniqueId())) {
      return;
    }
    Bukkit.getScheduler()
        .runTask(
            AstralPractice.getInstance(),
            () -> entityInteractCooldown.remove(player.getUniqueId()));

    // Check if player is in setup mode
    if (!setupManager.isSettingUp(player)) {
      Common.sendMmMessage(player, "<red>You must be in setup mode to remove spawn markers.");
      return;
    }

    // Find which arena this marker belongs to
    DisplayArena arena = SpawnMarkerManager.getInstance().getArenaForMarker(mannequin);
    if (arena == null) {
      Common.sendMmMessage(player, "<red>Could not find arena for this marker.");
      return;
    }

    // ONLY allow for FFA arenas
    if (!(arena instanceof FfaArena ffaArena)) {
      Common.sendMmMessage(player, "<red>Direct marker removal only works for FFA arenas.");
      Common.sendMmMessage(
          player, "<gray>Use left/right click on blocks to set standard arena spawn positions.");
      return;
    }

    // Check if player is setting up this arena
    ArenaSetupManager.SetupSession session = setupManager.getSession(player);
    if (session == null || !session.getArena().equals(arena)) {
      Common.sendMmMessage(player, "<red>You are not currently setting up this arena.");
      return;
    }

    // Check if in correct mode
    if (session.getCurrentMode() != SetupMode.FFA_POSITIONS) {
      Common.sendMmMessage(player, "<red>Switch to FFA Positions mode to remove spawn markers.");
      return;
    }

    // Remove the marker and its spawn position
    boolean removed = SpawnMarkerManager.getInstance().removeMarker(mannequin, arena);
    if (removed) {
      SpawnMarkerManager.getInstance().updateMarkers(arena);
      updateGui(arena);

      Common.sendMmMessage(
          player, "<red>Removed FFA spawn. Remaining: " + ffaArena.getFfaPositions().size());
    } else {
      Common.sendMmMessage(player, "<red>Failed to remove spawn marker.");
    }
  }

  // Prevent damage to marker mannequins AND handle left-click removal
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onMarkerDamage(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Mannequin mannequin)) {
      return;
    }

    if (!SpawnMarkerManager.getInstance().isMarker(mannequin)) {
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

    // Find which arena this marker belongs to
    DisplayArena arena = SpawnMarkerManager.getInstance().getArenaForMarker(mannequin);
    if (arena == null) {
      return;
    }

    // ONLY allow left-click removal for FFA arenas
    if (!(arena instanceof FfaArena ffaArena)) {
      return;
    }

    // Check if player is setting up this arena
    ArenaSetupManager.SetupSession session = setupManager.getSession(player);
    if (session == null || !session.getArena().equals(arena)) {
      return;
    }

    // Check if in correct mode
    if (session.getCurrentMode() != SetupMode.FFA_POSITIONS) {
      return;
    }

    // Left-click on mannequin marker = Remove that specific spawn
    SpawnMarkerManager markerManager = SpawnMarkerManager.getInstance();
    boolean removed = markerManager.removeMarker(mannequin, arena);
    if (removed) {
      markerManager.updateMarkers(arena);
      updateGui(arena);

      Common.sendMmMessage(
          player, "<red>Removed FFA spawn. Remaining: " + ffaArena.getFfaPositions().size());
    } else {
      Common.sendMmMessage(player, "<red>Failed to remove spawn marker.");
    }
  }
}
