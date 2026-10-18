package dev.lokspel.practice.util;

import dev.lokspel.practice.AstralPractice;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public final class PapiUtil {

  private PapiUtil() {}

  public static Component runThroughFormat(Player player, String line) {
    return format(player, line);
  }

  public static Component format(Player player, String line) {
    if (line == null || line.isEmpty()) {
      return Component.empty();
    }

    if (SoftDependUtil.isPAPI_ENABLED) {
      return AstralPractice.getMiniMessage()
          .deserialize(
              StringUtil.legacyToMiniMessage(PlaceholderAPI.setPlaceholders(player, line)));
    }

    return AstralPractice.getMiniMessage().deserialize(StringUtil.legacyToMiniMessage(line));
  }

  public static Component format(Player player, Component component) {
    if (component == null || player == null || !SoftDependUtil.isPAPI_ENABLED) {
      return component != null ? component : Component.empty();
    }
    String serialized = AstralPractice.getMiniMessage().serialize(component);
    String resolved = PlaceholderAPI.setPlaceholders(player, serialized);
    return AstralPractice.getMiniMessage().deserialize(StringUtil.legacyToMiniMessage(resolved));
  }
}
