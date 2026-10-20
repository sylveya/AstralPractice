package dev.lokspel.practice.manager.fight.event.events.onevsall.juggernaut;

import dev.lokspel.practice.manager.fight.event.enums.EventType;
import dev.lokspel.practice.manager.fight.event.interfaces.EventData;
import dev.lokspel.practice.manager.fight.event.interfaces.EventFileData;
import dev.lokspel.practice.util.kit.KitData;
import dev.lokspel.practice.util.kit.KitFileData;
import java.io.IOException;
import lombok.Getter;

@Getter
public class JuggernautData extends EventData {

  private final KitData juggernautKitData = new KitData();
  private final KitData playerKitData = new KitData();

  public JuggernautData() {
    super(EventType.JUGGERNAUT);
  }

  @Override
  protected EventFileData createFileData() {
    return new JuggernautFileData();
  }

  @Override
  protected void setCustomData(EventFileData data) {
    JuggernautFileData juggernautData = (JuggernautFileData) data;
    juggernautData.setJuggernautKit(KitFileData.of(juggernautKitData));
    juggernautData.setPlayerKit(KitFileData.of(playerKitData));
  }

  @Override
  protected void getCustomData(EventFileData data) {
    JuggernautFileData juggernautData = (JuggernautFileData) data;
    if (juggernautData.getJuggernautKit() != null) {
      juggernautData.getJuggernautKit().applyTo(juggernautKitData);
    }
    if (juggernautData.getPlayerKit() != null) {
      juggernautData.getPlayerKit().applyTo(playerKitData);
    }
  }

  @Override
  protected void enable() throws IOException {
    if (juggernautKitData == null || !juggernautKitData.isSet()) {
      throw new IOException("Juggernaut kit data is not set.");
    }

    if (playerKitData == null || !playerKitData.isSet()) {
      throw new IOException("Player kit data is not set.");
    }
  }
}
