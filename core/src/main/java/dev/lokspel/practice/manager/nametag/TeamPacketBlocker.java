package dev.lokspel.practice.manager.nametag;

import dev.lokspel.practice.util.startup.PermanentConfig;
import lombok.Getter;
import org.bukkit.Bukkit;

public class TeamPacketBlocker {

  private static TeamPacketBlocker instance;

  @Getter private TabIntegration tabIntegration;

  private TeamPacketBlocker() {}

  public static TeamPacketBlocker getInstance() {
    if (instance == null) {
      instance = new TeamPacketBlocker();
    }
    return instance;
  }

  public void register() {
    if (!PermanentConfig.isNametagManagementEnabled()) {
      unregister();
      return;
    }

    if (Bukkit.getPluginManager().isPluginEnabled("TAB")) {
      try {
        tabIntegration = new TabIntegration();
      } catch (Throwable ignored) {
        tabIntegration = null;
      }
    }
  }

  public void unregister() {
    tabIntegration = null;
  }
}
