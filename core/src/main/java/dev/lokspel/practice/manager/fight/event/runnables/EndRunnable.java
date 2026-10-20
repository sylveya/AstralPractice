package dev.lokspel.practice.manager.fight.event.runnables;

import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.interfaces.Event;
import dev.lokspel.practice.util.interfaces.Runnable;
import dev.lokspel.practice.util.startup.PermanentConfig;

public class EndRunnable extends Runnable {

  private final Event event;

  public EndRunnable(final Event event) {
    super(0, 20, false);

    this.event = event;
    this.seconds = 5;
  }

  @Override
  public void run() {
    if (this.seconds == 0) {
      this.end();
    } else {
      this.seconds--;
    }
  }

  public void end() {
    this.cancel();

    this.event.removeAll();
    this.event
        .getFightChange()
        .rollback(
            PermanentConfig.EVENT_ROLLBACK_MAX_CHECKS, PermanentConfig.EVENT_ROLLBACK_MAX_CHANGES);

    EventManager.getInstance().getEvents().remove(event);
  }
}
