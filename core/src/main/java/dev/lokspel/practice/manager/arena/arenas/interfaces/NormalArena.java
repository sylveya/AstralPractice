package dev.lokspel.practice.manager.arena.arenas.interfaces;

import dev.lokspel.practice.manager.arena.ArenaLayoutData;
import dev.lokspel.practice.manager.arena.ArenaLayoutData.Pair;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.manager.arena.util.BedLocation;
import dev.lokspel.practice.manager.arena.util.PortalLocation;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public abstract class NormalArena extends BasicArena implements DisplayName {

  protected boolean portalProtection = false;
  protected int portalProtectionValue;

  // In bed related arenas
  protected BedLocation bedLoc1;
  protected BedLocation bedLoc2;

  // Portal related arenas
  protected PortalLocation portalLoc1;
  protected PortalLocation portalLoc2;

  // Optional shared spawn point used by Party FFA style modes.
  protected Location partyFfaCenter;

  // Spectator position
  protected Location spectatorPosition;

  protected NormalArena(String name) {
    super(name);
  }

  @Override
  public Location getAvailableLocation() {
    if (position1 != null) {
      return position1;
    }
    if (position2 != null) {
      return position2;
    }
    if (bedLoc1 != null) {
      return bedLoc1.getLocation();
    }
    if (bedLoc2 != null) {
      return bedLoc2.getLocation();
    }
    if (portalLoc1 != null) {
      return portalLoc1.getCenter();
    }
    if (portalLoc2 != null) {
      return portalLoc2.getCenter();
    }
    if (corner1 != null) {
      return corner1;
    }
    if (corner2 != null) {
      return corner2;
    }
    if (!ffaPositions.isEmpty()) {
      return ffaPositions.stream().findAny().get();
    }
    return null;
  }

  /** Writes the places and limits of this arena into its file's model. */
  public void setBasicData(ArenaLayoutData data) {
    data.setCorner(Pair.of(corner1, corner2));
    data.setPosition(Pair.of(position1, position2));
    data.setFfaPositions(ffaPositions.isEmpty() ? null : new ArrayList<>(ffaPositions));
    data.setBuildMax(isBuildMax() ? buildMaxValue : null);
    data.setDeadZone(isDeadZone() ? deadZoneValue : null);
    data.setPortalProtection(isPortalProtection() ? portalProtectionValue : null);
    data.setBedLocation(
        Pair.of(
            bedLoc1 != null ? ArenaUtil.toBedData(bedLoc1) : null,
            bedLoc2 != null ? ArenaUtil.toBedData(bedLoc2) : null));
    data.setPortalLocation(
        Pair.of(
            portalLoc1 != null ? portalLoc1.getCenter() : null,
            portalLoc2 != null ? portalLoc2.getCenter() : null));
    data.setPartyFfaCenter(partyFfaCenter);
    data.setSpectatorPosition(spectatorPosition);
  }

  /** Reads the places and limits of this arena from its file's model. */
  public void getBasicData(ArenaLayoutData data) {
    Pair<Location> corner = data.getCorner();
    if (corner != null && corner.getFirst() != null) {
      corner1 = corner.getFirst();
    }
    if (corner != null && corner.getSecond() != null) {
      corner2 = corner.getSecond();
    }
    createCuboid();

    Pair<Location> position = data.getPosition();
    if (position != null && position.getFirst() != null) {
      position1 = position.getFirst();
    }
    if (position != null && position.getSecond() != null) {
      position2 = position.getSecond();
    }

    if (data.getFfaPositions() != null) {
      ffaPositions = new ArrayList<>(data.getFfaPositions());
    }

    if (data.getBuildMax() != null) {
      setBuildMax(true);
      setBuildMaxValue(data.getBuildMax());
    }

    if (data.getDeadZone() != null) {
      setDeadZone(true);
      setDeadZoneValue(data.getDeadZone());
    }

    if (data.getPortalProtection() != null) {
      setPortalProtection(true);
      setPortalProtectionValue(data.getPortalProtection());
    }

    Pair<ArenaLayoutData.Bed> bedLocation = data.getBedLocation();
    if (bedLocation != null && bedLocation.getFirst() != null) {
      setBedLoc1(ArenaUtil.getBedData(bedLocation.getFirst()));
    }
    if (bedLocation != null && bedLocation.getSecond() != null) {
      setBedLoc2(ArenaUtil.getBedData(bedLocation.getSecond()));
    }

    Pair<Location> portalLocation = data.getPortalLocation();
    if (portalLocation != null && portalLocation.getFirst() != null) {
      portalLoc1 = new PortalLocation(portalLocation.getFirst());
    }
    if (portalLocation != null && portalLocation.getSecond() != null) {
      portalLoc2 = new PortalLocation(portalLocation.getSecond());
    }

    if (data.getPartyFfaCenter() != null) {
      partyFfaCenter = data.getPartyFfaCenter();
    }

    if (data.getSpectatorPosition() != null) {
      spectatorPosition = data.getSpectatorPosition();
    }
  }

  public List<PortalLocation> getPortalLocations() {
    final List<PortalLocation> portalLocations = new ArrayList<>();
    portalLocations.add(portalLoc1);
    portalLocations.add(portalLoc2);
    return portalLocations;
  }

  public boolean isBedSet() {
    return bedLoc1 != null && bedLoc2 != null;
  }

  public boolean isPortalSet() {
    return portalLoc1 != null && portalLoc2 != null;
  }
}
