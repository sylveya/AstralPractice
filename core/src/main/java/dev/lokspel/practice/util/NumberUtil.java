package dev.lokspel.practice.util;

import java.util.concurrent.ThreadLocalRandom;

public final class NumberUtil {

  private NumberUtil() {}

  public static double roundDouble(double value) {
    if (Double.isNaN(value) || Double.isInfinite(value)) {
      return value;
    }
    return Math.round(value * 100.0) / 100.0;
  }

  public static int doubleToInt(double value) {
    return (int) value;
  }

  public static int getRandomNumber(int min, int max) {
    return ThreadLocalRandom.current().nextInt(min, max + 1);
  }

  public static boolean isNotInteger(String s) {
    return !isInteger(s, 10);
  }

  private static boolean isInteger(String s, int radix) {
    if (s == null || s.isEmpty()) {
      return false;
    }
    int start = 0;
    if (s.charAt(0) == '-') {
      if (s.length() == 1) {
        return false;
      }
      start = 1;
    }
    for (int i = start; i < s.length(); i++) {
      if (Character.digit(s.charAt(i), radix) < 0) {
        return false;
      }
    }
    return true;
  }
}
