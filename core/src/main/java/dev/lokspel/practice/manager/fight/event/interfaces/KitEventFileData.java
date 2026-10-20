package dev.lokspel.practice.manager.fight.event.interfaces;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.util.kit.KitFileData;
import lombok.Getter;
import lombok.Setter;

/** The file of an event type with one kit (brackets, sumo, LMS): the kit is under {@code kit}. */
@Getter
@Setter
public class KitEventFileData extends EventFileData {

  @JsonProperty("kit")
  private KitFileData kit;
}
