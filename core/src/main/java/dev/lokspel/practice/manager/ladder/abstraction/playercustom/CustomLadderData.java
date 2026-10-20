package dev.lokspel.practice.manager.ladder.abstraction.playercustom;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.util.kit.KitFileData;
import lombok.Getter;
import lombok.Setter;

/**
 * A player's own custom ladder, stored in the profile file under {@code player-custom-kit.<key>}.
 */
@Getter
@Setter
public class CustomLadderData {

  @JsonProperty("settings")
  private Settings settings;

  @JsonProperty("kit-data")
  private KitFileData kitData;

  /**
   * The ladder's settings. The numeric values are kept as written: {@code hit-delay} is a
   * multiplier, or ticks in files of older versions when it is a whole number.
   */
  @Getter
  @Setter
  public static class Settings {

    @JsonProperty("name")
    private String name;

    @JsonProperty("regen")
    private Boolean regen;

    @JsonProperty("hunger")
    private Boolean hunger;

    @JsonProperty("build")
    private Boolean build;

    @JsonProperty("rounds")
    private Integer rounds;

    @JsonProperty("hit-delay")
    private Number hitDelay;

    @JsonProperty("knockback")
    private String knockback;

    @JsonProperty("ep-cooldown")
    private Number enderPearlCooldown;

    @JsonProperty("ga-cooldown")
    private Number goldenAppleCooldown;

    @JsonProperty("wind-charge-cooldown")
    private Number windChargeCooldown;

    @JsonProperty("health-below-name")
    private Boolean healthBelowName;
  }
}
