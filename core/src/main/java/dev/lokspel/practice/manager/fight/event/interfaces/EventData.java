package dev.lokspel.practice.manager.fight.event.interfaces;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.data.DataFiles;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.enums.EventType;
import dev.lokspel.practice.manager.fight.event.setup.EventSpawnMarkerManager;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.util.Cuboid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;

@Getter
@Setter
public abstract class EventData {

  protected EventType type;
  protected boolean enabled;
  protected GuiItem icon;

  protected final String path;

  protected Cuboid cuboid;
  protected Location cuboidLoc1;
  protected Location cuboidLoc2;

  protected final List<Location> spawns;

  protected int broadcastInterval;
  protected int waitBeforeStart;
  protected int maxQueueTime;
  protected int duration;
  protected int startTime;
  protected int minPlayer;
  protected int maxPlayer;
  protected boolean locatorBar = false;

  public EventData(final EventType type) {
    this.path = "events/" + type.getName().toLowerCase() + ".yml";
    this.type = type;
    this.spawns = new ArrayList<>();

    this.enabled = false;
    this.icon = new GuiItem(type.getIcon());
    this.broadcastInterval = type.getBroadcastInterval();
    this.waitBeforeStart = type.getWaitBeforeStart();
    this.maxQueueTime = type.getMaxQueueTime();
    this.duration = type.getDuration();
    this.startTime = type.getStartTime();
    this.minPlayer = type.getMinPlayer();
    this.maxPlayer = type.getMaxPlayer();
  }

  /** Writes this event type's file. */
  public synchronized void setData() {
    EventFileData data = createFileData();
    data.setEnabled(enabled);
    data.setIcon(icon.get());

    EventFileData.Settings settings = new EventFileData.Settings();
    settings.setBroadcastInterval(broadcastInterval);
    settings.setWaitBeforeStart(waitBeforeStart);
    settings.setMaxQueueTime(maxQueueTime);
    settings.setDuration(duration);
    settings.setStartTime(startTime);
    settings.setMinPlayer(minPlayer);
    settings.setMaxPlayer(maxPlayer);
    settings.setLocatorBar(locatorBar);
    data.setSettings(settings);

    if (cuboidLoc1 != null || cuboidLoc2 != null) {
      EventFileData.Corners corners = new EventFileData.Corners();
      corners.setFirst(cuboidLoc1);
      corners.setSecond(cuboidLoc2);
      data.setCuboid(corners);
    }
    data.setSpawns(spawns.isEmpty() ? null : new ArrayList<>(spawns));

    setCustomData(data);

    DataFiles.write(path, data);
  }

  /**
   * Creates the model of this event type's file; types with their own keys return a subclass.
   *
   * @return an empty model
   */
  protected EventFileData createFileData() {
    return new EventFileData();
  }

  /**
   * Puts the keys of this event type into its file's model.
   *
   * @param data the model {@link #createFileData()} created
   */
  protected abstract void setCustomData(EventFileData data);

  /** Reads this event type's file; a missing file leaves the defaults. */
  public void getData() {
    spawns.clear();

    EventFileData data = DataFiles.read(path, createFileData().getClass());
    if (data == null) {
      data = createFileData();
    }

    if (data.getIcon() != null) {
      this.icon = new GuiItem(data.getIcon());
    }

    EventFileData.Settings settings = data.getSettings();
    if (settings != null) {
      if (settings.getBroadcastInterval() != null) {
        this.broadcastInterval = settings.getBroadcastInterval();
      }
      if (settings.getWaitBeforeStart() != null) {
        this.waitBeforeStart = settings.getWaitBeforeStart();
      }
      if (settings.getMaxQueueTime() != null) {
        this.maxQueueTime = settings.getMaxQueueTime();
      }
      if (settings.getDuration() != null) {
        this.duration = settings.getDuration();
      }
      if (settings.getStartTime() != null) {
        this.startTime = settings.getStartTime();
      }
      if (settings.getMinPlayer() != null) {
        this.minPlayer = settings.getMinPlayer();
      }
      if (settings.getMaxPlayer() != null) {
        this.maxPlayer = settings.getMaxPlayer();
      }
      if (settings.getLocatorBar() != null) {
        this.locatorBar = settings.getLocatorBar();
      }
    }

    EventFileData.Corners corners = data.getCuboid();
    if (corners != null) {
      if (corners.getFirst() != null) {
        this.setCuboidLoc1(corners.getFirst());
      }
      if (corners.getSecond() != null) {
        this.setCuboidLoc2(corners.getSecond());
      }
    }

    if (data.getSpawns() != null) {
      for (Location location : data.getSpawns()) {
        if (location != null) {
          spawns.add(location.clone());
        }
      }

      if (cuboid != null) {
        spawns.removeIf(spawn -> !cuboid.contains(spawn));
      }
    }

    if (data.getEnabled() != null) {
      this.enabled = data.getEnabled();
    }

    this.getCustomData(data);
  }

  /**
   * Reads the keys of this event type from its file's model.
   *
   * @param data the model read from the file, of the type {@link #createFileData()} creates
   */
  protected abstract void getCustomData(EventFileData data);

  public void addSpawn(final Location spawn) {
    if (enabled) {
      throw new IllegalStateException("Cannot add spawn while event is enabled.");
    }

    if (cuboid == null) {
      throw new IllegalStateException(
          PracticeConfig.language()
              .getCommand()
              .getEvent()
              .getArguments()
              .getSpawnPosition()
              .getNoRegion());
    }

    if (!cuboid.contains(spawn)) {
      throw new IllegalStateException("Spawn point not in region.");
    }

    for (Location loc : this.spawns) {
      if (loc.getBlock().equals(spawn.getBlock())) {
        throw new IllegalStateException("Spawn point already exists.");
      }
    }

    spawns.add(spawn);
  }

  public void removeSpawn(final Location spawn) throws IllegalStateException, IOException {
    if (enabled) {
      throw new IllegalStateException("Cannot remove spawn while event is enabled.");
    }

    for (Location loc : this.spawns) {
      if (loc.getBlock().equals(spawn.getBlock())) {
        spawns.remove(loc);
        return;
      }
    }

    throw new IOException("Spawn not found.");
  }

  public void clearSpawn() throws IllegalStateException {
    if (enabled) {
      throw new IllegalStateException("Cannot clear spawns while event is enabled.");
    }

    spawns.clear();
  }

  public void setIcon(final GuiItem icon) throws IllegalStateException {
    if (enabled) {
      throw new IllegalStateException("Cannot change icon while event is enabled");
    }

    if (icon.getMaterial().equals(Material.AIR)) {
      throw new IllegalStateException("Icon cannot be air.");
    }

    if (icon.getName() == null || icon.getName().isEmpty()) {
      throw new IllegalStateException("Icon name cannot be empty.");
    }

    this.icon = icon;
  }

  public GuiItem getIcon() {
    return icon.cloneItem();
  }

  public void setCuboidLoc1(final Location cuboidLoc1) throws IllegalStateException {
    if (enabled) {
      throw new IllegalStateException("Cannot change cuboid while event is enabled");
    }

    if (this.cuboidLoc2 != null
        && cuboidLoc1 != null
        && !this.cuboidLoc2.getWorld().equals(cuboidLoc1.getWorld())) {
      this.cuboidLoc2 = null;
      this.cuboid = null;
    }

    this.cuboidLoc1 = cuboidLoc1;
    createCuboid();
  }

  public void setCuboidLoc2(final Location cuboidLoc2) {
    if (enabled) {
      throw new IllegalStateException("Cannot change cuboid while event is enabled");
    }

    if (this.cuboidLoc1 != null
        && cuboidLoc2 != null
        && !this.cuboidLoc1.getWorld().equals(cuboidLoc2.getWorld())) {
      throw new IllegalArgumentException("Both corners must be located in the same world");
    }

    this.cuboidLoc2 = cuboidLoc2;
    createCuboid();
  }

  private void createCuboid() {
    if (cuboidLoc1 != null && cuboidLoc2 != null) {
      if (!cuboidLoc1.getWorld().equals(cuboidLoc2.getWorld())) {
        this.cuboid = null;
        return;
      }
      this.cuboid = new Cuboid(cuboidLoc1, cuboidLoc2);

      spawns.removeIf(spawn -> !cuboid.contains(spawn));
    }
  }

  public void setEnabled(final boolean enabled) throws IOException {
    if (this.enabled && !enabled) {
      if (EventManager.getInstance().isEventLive(type)) {
        throw new IOException("Cannot disable event while it is live.");
      }
    } else if (!this.enabled && enabled) {
      if (cuboid == null) {
        throw new IOException("Cuboid not set.");
      } else if (spawns.isEmpty()) {
        throw new IOException("No spawns set.");
      } else if (icon == null) {
        throw new IOException("Icon not set.");
      } else {
        // Always remove spawn-point marker armor stands before going live.
        // This must happen unconditionally here rather than at each call-site
        // so that every enable path (wand, GUI, command) is covered.
        EventSpawnMarkerManager.getInstance().clearMarkers(this);
        enable();
      }
    } else {
      throw new IOException("Event already " + (enabled ? "enabled" : "disabled" + "."));
    }

    this.enabled = enabled;
  }

  protected abstract void enable() throws IOException;

  public Location getAvailableLocation() {
    if (!spawns.isEmpty()) {
      return spawns.get(0);
    }
    if (cuboidLoc1 != null) {
      return cuboidLoc1;
    }
    if (cuboidLoc2 != null) {
      return cuboidLoc2;
    }

    return null;
  }
}
