package dev.lokspel.practice.util;

import dev.lokspel.practice.config.PracticeConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;

public final class StringUtil {

  private StringUtil() {}

  public static String cc(String string) {
    if (string == null) {
      return "";
    }
    return LegacyComponentSerializer.legacySection()
        .serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(string));
  }

  public static List<String> cc(List<String> stringlist) {
    List<String> list = new ArrayList<>();
    for (String string : stringlist) {
      list.add(cc(string));
    }
    return list;
  }

  private static final Map<Character, String> LEGACY_TO_MM =
      Map.ofEntries(
          Map.entry('0', "black"),
          Map.entry('1', "dark_blue"),
          Map.entry('2', "dark_green"),
          Map.entry('3', "dark_aqua"),
          Map.entry('4', "dark_red"),
          Map.entry('5', "dark_purple"),
          Map.entry('6', "gold"),
          Map.entry('7', "gray"),
          Map.entry('8', "dark_gray"),
          Map.entry('9', "blue"),
          Map.entry('a', "green"),
          Map.entry('b', "aqua"),
          Map.entry('c', "red"),
          Map.entry('d', "light_purple"),
          Map.entry('e', "yellow"),
          Map.entry('f', "white"),
          Map.entry('k', "obfuscated"),
          Map.entry('l', "bold"),
          Map.entry('m', "strikethrough"),
          Map.entry('n', "underlined"),
          Map.entry('o', "italic"),
          Map.entry('r', "reset"));

  public static String legacyToMiniMessage(String text) {
    if (text == null || text.isEmpty()) {
      return text;
    }

    StringBuilder out = new StringBuilder();

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);

      if ((c == '&' || c == '§') && i + 1 < text.length()) {
        char code = Character.toLowerCase(text.charAt(++i));

        // hex
        if (code == '#' && i + 6 < text.length()) {
          String hex = text.substring(i + 1, i + 7);

          if (isHex6(hex)) {
            out.append("<#").append(hex).append(">");
            i += 6;
            continue;
          }
        }

        // legacy
        String tag = LEGACY_TO_MM.get(code);
        if (tag != null) {
          out.append('<').append(tag).append('>');
          continue;
        }

        out.append(c).append(code);
        continue;
      }

      out.append(c);
    }

    return out.toString();
  }

  private static boolean isHex6(String s) {
    for (int j = 0; j < 6; j++) {
      char ch = s.charAt(j);
      if (!((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F'))) {
        return false;
      }
    }
    return true;
  }

  public static String replaceSecondString(String string, double seconds) {
    String secondsStr =
        ((seconds == Math.floor(seconds)) && !Double.isInfinite(seconds))
            ? String.valueOf(NumberUtil.doubleToInt(seconds))
            : String.valueOf(seconds);
    String secondName =
        (seconds < 2
            ? PracticeConfig.language().getSecondName().getOneSecond()
            : PracticeConfig.language().getSecondName().getUnderOneSecond());

    return string.replace("%seconds%", secondsStr).replace("%secondName%", secondName);
  }

  public static String getNormalizedName(String name) {
    return name == null
        ? null
        : StringUtils.capitalize(name.replace('_', ' ').toLowerCase(Locale.ROOT));
  }

  public static boolean containsBlockedFormatting(String text) {
    String blockedPattern = PracticeConfig.settings().getPlayer().getNickname().getBlockedPattern();
    return containsPattern(text, blockedPattern);
  }

  private static boolean containsPattern(String text, String regex) {
    if (text == null || text.isEmpty() || regex == null || regex.isEmpty()) {
      return false;
    }
    try {
      return Pattern.compile(regex).matcher(text).find();
    } catch (Exception ignored) {
      return false;
    }
  }

  public static String normalizeSpaces(String text) {
    if (text == null || !text.contains("  ")) {
      return text;
    }

    StringBuilder sb = new StringBuilder(text.length());
    boolean lastWasSpace = false;
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == ' ') {
        if (!lastWasSpace) {
          sb.append(' ');
          lastWasSpace = true;
        }
      } else {
        sb.append(c);
        lastWasSpace = false;
      }
    }
    return sb.toString();
  }
}
