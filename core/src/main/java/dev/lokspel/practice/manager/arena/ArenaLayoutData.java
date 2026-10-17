package dev.lokspel.practice.manager.arena;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;

/**
 * The places and limits of an arena or of one of its copies, as stored at the top of {@code
 * arenas/<name>.yml} and under each entry of its {@code copies} section.
 */
@Getter
@Setter
@NoArgsConstructor
public class ArenaLayoutData {

  @JsonProperty("corner")
  private Pair<Location> corner;

  @JsonProperty("position")
  private Pair<Location> position;

  @JsonProperty("ffa-positions")
  private List<Location> ffaPositions;

  @JsonProperty("buildmax")
  private Integer buildMax;

  @JsonProperty("deadzone")
  private Integer deadZone;

  @JsonProperty("portalprot")
  private Integer portalProtection;

  @JsonProperty("bedlocation")
  private Pair<Bed> bedLocation;

  @JsonProperty("portallocation")
  private Pair<Location> portalLocation;

  @JsonProperty("party-ffa-center")
  private Location partyFfaCenter;

  @JsonProperty("spectator-position")
  private Location spectatorPosition;

  /**
   * A section holding a first and a second value under the keys {@code 1} and {@code 2}.
   *
   * @param <T> type of the values
   */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Pair<T> {

    @JsonProperty("1")
    private T first;

    @JsonProperty("2")
    private T second;

    /**
     * Creates a section from its two values.
     *
     * @return the section, or {@code null} when both values are {@code null}
     */
    public static <T> Pair<T> of(T first, T second) {
      if (first == null && second == null) {
        return null;
      }

      Pair<T> pair = new Pair<>();
      pair.setFirst(first);
      pair.setSecond(second);
      return pair;
    }
  }

  /** A bed: the block it stands on and the direction it faces. */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class Bed {

    @JsonProperty("world")
    private String world;

    @JsonProperty("x")
    private Double blockX;

    @JsonProperty("y")
    private Double blockY;

    @JsonProperty("z")
    private Double blockZ;

    @JsonProperty("face")
    private String face;
  }
}
