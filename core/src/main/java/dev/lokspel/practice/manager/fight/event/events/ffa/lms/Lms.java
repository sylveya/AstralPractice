package dev.lokspel.practice.manager.fight.event.events.ffa.lms;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.event.events.ffa.interfaces.FfaEvent;
import dev.lokspel.practice.util.kit.KitData;
import org.bukkit.entity.Player;

public class Lms extends FfaEvent {

  public Lms(Object starter, LmsData eventData) {
    super(
        starter,
        eventData,
        () -> PracticeConfig.language().getCommand().getEvent().getArguments().getLms());
  }

  @Override
  public LmsData getEventData() {
    return (LmsData) eventData;
  }

  @Override
  protected void customCustomStart() {}

  @Override
  protected void loadInventory(Player player) {
    KitData kitData = this.getEventData().getKitData();
    kitData.loadKitData(player, true);
  }
}
