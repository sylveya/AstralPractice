package dev.lokspel.practice.manager.leaderboard.hologram;

import dev.lokspel.practice.manager.leaderboard.types.LbMainType;
import lombok.Getter;

@Getter
public enum HologramType {
  GLOBAL("Global", "Global", "This hologram shows global stats.", LbMainType.GLOBAL),
  FFA_GLOBAL(
      "FFA Global", "FFA Global", "This hologram shows global FFA stats.", LbMainType.FFA_GLOBAL),
  LADDER_STATIC(
      "Ladder Static",
      "Ladder Static",
      "This hologram shows a specific ladder's stats.",
      LbMainType.LADDER),
  LADDER_DYNAMIC(
      "Ladder Dynamic",
      "Ladder Dynamic",
      "This hologram shows multiple ladder's stats.",
      LbMainType.LADDER);

  private final String name;
  private final String holoName;
  private final String description;
  private final LbMainType lbMainType;

  HologramType(String name, String holoName, String description, LbMainType lbMainType) {
    this.name = name;
    this.holoName = holoName;
    this.description = description;
    this.lbMainType = lbMainType;
  }
}
