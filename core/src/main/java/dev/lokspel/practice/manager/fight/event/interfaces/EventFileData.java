package dev.lokspel.practice.manager.fight.event.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 * The file {@code events/<type>.yml}: the keys every event type has. Event types with a kit extend
 * it with their own sections.
 */
@Getter
@Setter
public class EventFileData {

  @JsonProperty("enabled")
  private Boolean enabled;

  @JsonProperty("icon")
  private ItemStack icon;

  @JsonProperty("settings")
  private Settings settings;

  @JsonProperty("cuboid")
  private Corners cuboid;

  @JsonProperty("spawns")
  private List<Location> spawns;

  /** The {@code settings} section; a missing key keeps the event type's default. */
  @Getter
  @Setter
  public static class Settings {

    @JsonProperty("broadcastInterval")
    private Integer broadcastInterval;

    @JsonProperty("waitBeforeStart")
    private Integer waitBeforeStart;

    @JsonProperty("maxQueueTime")
    private Integer maxQueueTime;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("startTime")
    private Integer startTime;

    @JsonProperty("minPlayer")
    private Integer minPlayer;

    @JsonProperty("maxPlayer")
    private Integer maxPlayer;

    @JsonProperty("locatorBar")
    private Boolean locatorBar;
  }

  /**
   * The {@code cuboid} section: the region's two corners under the keys {@code 1} and {@code 2}.
   */
  @Getter
  @Setter
  public static class Corners {

    @JsonProperty("1")
    private Location first;

    @JsonProperty("2")
    private Location second;
  }
}
