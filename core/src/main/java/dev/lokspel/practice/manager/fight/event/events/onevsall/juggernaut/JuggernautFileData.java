package dev.lokspel.practice.manager.fight.event.events.onevsall.juggernaut;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.manager.fight.event.interfaces.EventFileData;
import dev.lokspel.practice.util.kit.KitFileData;
import lombok.Getter;
import lombok.Setter;

/** The file {@code events/juggernaut.yml}: the juggernaut's kit and the other players' kit. */
@Getter
@Setter
public class JuggernautFileData extends EventFileData {

  @JsonProperty("juggernaut-kit")
  private KitFileData juggernautKit;

  @JsonProperty("player-kit")
  private KitFileData playerKit;
}
