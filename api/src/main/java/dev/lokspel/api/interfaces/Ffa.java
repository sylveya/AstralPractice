package dev.lokspel.api.interfaces;

public interface Ffa extends Spectatable {

  /**
   * Get the ladder name of the FFA arena.
   *
   * @return The ladder name of the FFA arena, or null if not set or multiple ladders are assigned
   */
  default String getLadderName() {
    return null;
  }

  /**
   * Get the ladder display name of the FFA arena.
   *
   * @return The ladder display name of the FFA arena, or null if not set or multiple ladders are
   *     assigned
   */
  default String getLadderDisplayName() {
    return null;
  }
}
