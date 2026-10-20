package dev.lokspel.practice.util.startup;

import dev.lokspel.practice.AstralPractice;

public final class StartUpUtil {

  private StartUpUtil() {}

  public static void loadStartUpProgressMap() {
    for (StartUpTypes startUpType : StartUpTypes.values()) {
      AstralPractice.getInstance().getStartUpProgress().put(startUpType, false);
    }
  }
}
