package dev.lokspel.practice.manager.fight.event.events.ffa.lms;

import dev.lokspel.practice.manager.fight.event.enums.EventType;
import dev.lokspel.practice.manager.fight.event.interfaces.EventData;
import dev.lokspel.practice.manager.fight.event.interfaces.EventFileData;
import dev.lokspel.practice.manager.fight.event.interfaces.KitEventFileData;
import dev.lokspel.practice.util.kit.KitData;
import dev.lokspel.practice.util.kit.KitFileData;
import java.io.IOException;
import lombok.Getter;

@Getter
public class LmsData extends EventData {

  private final KitData kitData = new KitData();

  public LmsData() {
    super(EventType.LMS);
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
  protected void enable() throws NullPointerException, IOException {
    if (kitData != null && !kitData.isSet()) {
      throw new IOException("Kit data is not set.");
    }
  }
}
