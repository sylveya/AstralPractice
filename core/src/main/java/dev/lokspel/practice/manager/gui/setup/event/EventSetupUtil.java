package dev.lokspel.practice.manager.gui.setup.event;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.event.interfaces.EventData;
import org.bukkit.inventory.ItemStack;

public enum EventSetupUtil {
  ;

  public static ItemStack getBroadcastIntervalItem(final int broadcastInterval) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getEvent()
                .getEventSettings()
                .getIcons()
                .getBroadcastInterval())
        .replace("%broadcastInterval%", String.valueOf(broadcastInterval))
        .get();
  }

  public static ItemStack getWaitBeforeStartItem(final int waitBeforeStart) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getEvent()
                .getEventSettings()
                .getIcons()
                .getWaitBeforeStart())
        .replace("%waitBeforeStart%", String.valueOf(waitBeforeStart))
        .get();
  }

  public static ItemStack getMaxQueueTimeItem(int queueTime) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getEvent()
                .getEventSettings()
                .getIcons()
                .getMaxQueueTime())
        .replace("%queueTime%", String.valueOf(queueTime / 60))
        .get();
  }

  public static ItemStack getDurationItem(EventData eventData) {
    return switch (eventData.getType()) {
      case TNTTAG ->
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getSetup()
                      .getEvent()
                      .getEventSettings()
                      .getIcons()
                      .getDuration()
                      .getTnttag())
              .replace("%explodeTime%", String.valueOf(eventData.getDuration()))
              .get();
      // This used to read ICONS.DURATION.SUMO.RACKETS, which never existed (the configured item is
      // SUMO&BRACKETS), so these events have always shown no duration item.
      case BRACKETS, SUMO -> null;
      default ->
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getSetup()
                      .getEvent()
                      .getEventSettings()
                      .getIcons()
                      .getDuration()
                      .getOther())
              .replace("%duration%", String.valueOf(eventData.getDuration() / 60))
              .get();
    };
  }

  public static ItemStack getStartTimeItem(int startTime) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getEvent()
                .getEventSettings()
                .getIcons()
                .getStartTime())
        .replace("%startTime%", String.valueOf(startTime))
        .get();
  }

  public static ItemStack getMinPlayerItem(int minPlayer) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getEvent()
                .getEventSettings()
                .getIcons()
                .getMinPlayer())
        .replace("%minPlayer%", String.valueOf(minPlayer))
        .get();
  }

  public static ItemStack getLocatorBarItem(boolean enabled) {
    return BackendUtil.getGuiItem(
            enabled
                ? PracticeConfig.guis()
                    .getGuis()
                    .getSetup()
                    .getEvent()
                    .getEventSettings()
                    .getIcons()
                    .getLocatorBar()
                    .getEnabled()
                : PracticeConfig.guis()
                    .getGuis()
                    .getSetup()
                    .getEvent()
                    .getEventSettings()
                    .getIcons()
                    .getLocatorBar()
                    .getDisabled())
        .get();
  }

  public static ItemStack getMaxPlayerItem(int maxPlayer) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getEvent()
                .getEventSettings()
                .getIcons()
                .getMaxPlayer())
        .replace("%maxPlayer%", String.valueOf(maxPlayer))
        .get();
  }
}
