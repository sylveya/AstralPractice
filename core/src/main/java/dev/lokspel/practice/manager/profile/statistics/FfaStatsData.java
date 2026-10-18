package dev.lokspel.practice.manager.profile.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/** Persisted statistics model for FFA in the player's profile file. */
@Getter
@Setter
public class FfaStatsData {

  @JsonProperty("kills")
  private Integer kills;

  @JsonProperty("deaths")
  private Integer deaths;

  @JsonProperty("best-kill-streak")
  private Integer bestKillStreak;
}
