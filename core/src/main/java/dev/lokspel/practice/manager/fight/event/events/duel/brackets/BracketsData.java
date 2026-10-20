package dev.lokspel.practice.manager.fight.event.events.duel.brackets;

import dev.lokspel.practice.manager.fight.event.enums.EventType;
import dev.lokspel.practice.manager.fight.event.events.duel.interfaces.DuelEventData;
import dev.lokspel.practice.manager.fight.event.interfaces.EventFileData;
import dev.lokspel.practice.manager.fight.event.interfaces.KitEventFileData;
import dev.lokspel.practice.util.kit.KitData;
import dev.lokspel.practice.util.kit.KitFileData;
import java.io.IOException;
import lombok.Getter;

@Getter
public class BracketsData extends DuelEventData {

  protected final KitData kitData;

  public BracketsData() {
    super(EventType.BRACKETS);
    this.kitData = new KitData();
  }

  @Override
  protected EventFileData createFileData() {
    return new KitEventFileData();
  }

  @Override
  protected void setCustomData(EventFileData data) {
    ((KitEventFileData) data).setKit(KitFileData.of(kitData));
  }

  @Override
  protected void getCustomData(EventFileData data) {
    KitFileData kit = ((KitEventFileData) data).getKit();
    if (kit != null) {
      kit.applyTo(kitData);
    }
  }

  @Override
  protected void enable() throws IOException {
    if (!kitData.isSet()) {
      throw new IOException("Kit data is not set.");
    } else if (spawns.size() != 2) {
      throw new IOException(
          "Spawn positions are not set. Or not equal to 2. Current size: " + spawns.size());
    }
  }
}
