package dev.lokspel.practice.util;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.enums.ProfilePrefixVisibility;
import dev.lokspel.practice.manager.profile.group.Group;
import java.util.regex.Pattern;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

/**
 * Utility for resolving, formatting, and placeholder-injecting player names, prefixes, suffixes,
 * and scoreboard colors based on profile and group configurations.
 */
public final class NameFormatUtil {

  private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER =
      PlainTextComponentSerializer.plainText();

  private static final Pattern PLAYER_PATTERN = Pattern.compile("%%?player%%?");
  private static final Pattern DIVISION_SHORT_PATTERN = Pattern.compile("%%?division_short%%?");
  private static final Pattern DIVISION_PATTERN = Pattern.compile("%%?division%%?");

  private NameFormatUtil() {}

  private static TextColor findFirstExplicitColor(Component component) {
    if (component == null) {
      return null;
    }
    if (component.color() != null) {
      return component.color();
    }
    for (Component child : component.children()) {
      TextColor childColor = findFirstExplicitColor(child);
      if (childColor != null) {
        return childColor;
      }
    }
    return null;
  }

  /**
   * Returns the last (trailing) explicit color in a component tree — i.e. the color that would
   * visually "bleed" into the next component appended after this one.
   */
  private static TextColor findLastExplicitColor(Component component) {
    if (component == null) {
      return null;
    }
    TextColor last = component.color();
    for (Component child : component.children()) {
      TextColor childLast = findLastExplicitColor(child);
      if (childLast != null) {
        last = childLast;
      }
    }
    return last;
  }

  /**
   * Public wrapper used to extract the trailing prefix color so it can be inherited by the name
   * component.
   */
  public static TextColor extractTrailingColor(Component component) {
    return findLastExplicitColor(component);
  }

  public static Component parseConfiguredComponent(String raw) {
    if (raw == null || raw.isEmpty()) {
      return Component.empty();
    }
    return AstralPractice.getMiniMessage().deserialize(StringUtil.legacyToMiniMessage(raw));
  }

  public static Component applyPlaceholders(Component template, Profile profile) {
    if (template == null) {
      return Component.empty();
    }
    if (profile == null) {
      return template;
    }
    var divisionStat = profile.getStats().getDivision();
    Component division =
        divisionStat != null ? divisionStat.getComponentFullName() : Component.empty();
    Component divisionShort =
        divisionStat != null ? divisionStat.getComponentShortName() : Component.empty();

    return template
        .replaceText(
            TextReplacementConfig.builder()
                .match(DIVISION_SHORT_PATTERN)
                .replacement(divisionShort)
                .build())
        .replaceText(
            TextReplacementConfig.builder()
                .match(DIVISION_PATTERN)
                .replacement(division)
                .build());
  }

  public static Component applyPlaceholders(
      Component template, Profile profile, String playerName) {
    if (template == null) {
      return Component.empty();
    }
    Component resolved = applyPlaceholders(template, profile);
    if (playerName == null) {
      return resolved;
    }
    return resolved.replaceText(
        TextReplacementConfig.builder()
            .match(PLAYER_PATTERN)
            .replacement(Component.text(playerName))
            .build());
  }

  public static String normalizeTemplate(String rawTemplate) {
    if (rawTemplate == null || rawTemplate.isEmpty()) {
      return rawTemplate;
    }
    if (rawTemplate.contains("%player%") || rawTemplate.contains("%%player%%")) {
      return rawTemplate;
    }
    String plainText =
        PLAIN_TEXT_SERIALIZER
            .serialize(
                AstralPractice.getMiniMessage()
                    .deserialize(StringUtil.legacyToMiniMessage(rawTemplate)))
            .trim();
    if (!plainText.isEmpty()) {
      return rawTemplate;
    }
    return rawTemplate + "%player%";
  }

  public static Component renderTemplate(String rawTemplate, String playerName) {
    return renderTemplate(rawTemplate, null, playerName, null);
  }

  public static Component renderTemplate(
      String rawTemplate, Profile profile, String playerName, Player player) {
    if (rawTemplate == null || rawTemplate.isEmpty()) {
      return Component.empty();
    }

    String normalized = rawTemplate;

    if (player != null && SoftDependUtil.isPAPI_ENABLED) {
      normalized = PlaceholderAPI.setPlaceholders(player, normalized);
    }

    normalized = StringUtil.legacyToMiniMessage(normalized);

    var divisionStat = profile.getStats().getDivision();
    String division =
        divisionStat != null
            ? AstralPractice.getMiniMessage().serialize(divisionStat.getComponentFullName())
            : "";
    String divisionShort =
        divisionStat != null
            ? AstralPractice.getMiniMessage().serialize(divisionStat.getComponentShortName())
            : "";

    normalized =
        normalized
            .replace("%%division_short%%", divisionShort)
            .replace("%division_short%", divisionShort)
            .replace("%%division%%", division)
            .replace("%division%", division);

    if (playerName != null) {
      normalized = normalized.replace("%%player%%", playerName).replace("%player%", playerName);
    }

    return AstralPractice.getMiniMessage().deserialize(normalized);
  }

  private static Component resolveAffix(
      Profile profile,
      Player player,
      boolean visible,
      String groupTemplate,
      Component groupComponent,
      Component customOverride) {
    if (!visible) {
      return Component.empty();
    }

    Component affix = Component.empty();

    if (groupComponent != null) {
      affix =
          groupTemplate != null
              ? renderTemplate(groupTemplate, profile, null, player)
              : PapiUtil.format(player, groupComponent);
    }

    if (customOverride != null) {
      affix = PapiUtil.format(player, customOverride);
    }

    return applyPlaceholders(affix, profile);
  }

  public static Component resolvePrefix(Profile profile, Player player) {
    ProfilePrefixVisibility visibility = profile.getPrefixVisibility();
    boolean visible = visibility != null && visibility.isShowPrefix();
    Group group = profile.getGroup();

    String groupTemplate = group != null ? group.getPrefixTemplate() : null;
    Component groupPrefix = group != null ? group.getPrefix() : null;

    return resolveAffix(
        profile, player, visible, groupTemplate, groupPrefix, profile.getPrefix());
  }

  public static Component resolveSuffix(Profile profile) {
    return resolveSuffix(profile, null);
  }

  public static Component resolveSuffix(Profile profile, Player player) {
    ProfilePrefixVisibility visibility = profile.getPrefixVisibility();
    boolean visible = visibility != null && visibility.isShowSuffix();
    Group group = profile.getGroup();

    String groupTemplate = group != null ? group.getSuffixTemplate() : null;
    Component groupSuffix = group != null ? group.getSuffix() : null;

    return resolveAffix(
        profile, player, visible, groupTemplate, groupSuffix, profile.getSuffix());
  }

  public static Component resolveName(Profile profile, String playerName) {
    return resolveName(profile, playerName, null, null);
  }

  public static Component resolveName(
      Profile profile, String playerName, Player player, TextColor prefixColor) {
    Group group = profile.getGroup();
    String safeName = playerName != null ? playerName : "";

    Component nameComponent;
    if (profile.getNameTemplate() != null && !profile.getNameTemplate().isEmpty()) {
      nameComponent = renderTemplate(profile.getNameTemplate(), profile, safeName, player);
    } else if (group != null && group.getNameTemplate() != null) {
      nameComponent = renderTemplate(group.getNameTemplate(), profile, safeName, player);
    } else if (group != null && group.getNameFormat() != null) {
      nameComponent = applyPlaceholders(group.getNameFormat(), profile, safeName);
    } else {
      nameComponent = Component.text(safeName, NamedTextColor.GRAY);
    }

    if (prefixColor != null && findFirstExplicitColor(nameComponent) == null) {
      nameComponent = nameComponent.color(prefixColor);
    }

    return nameComponent;
  }

  public static Component resolveFullName(Profile profile, String playerName) {
    return resolveFullName(profile, playerName, null);
  }

  public static Component resolveFullName(Profile profile, String playerName, Player player) {
    Component prefix = resolvePrefix(profile, player);
    Component name = resolveName(profile, playerName, player, extractTrailingColor(prefix));
    Component suffix = resolveSuffix(profile, player);
    return prefix.append(name).append(suffix);
  }

  public static NamedTextColor resolveScoreboardColor(
      Profile profile, String playerName, NamedTextColor fallback) {
    TextColor color = findFirstExplicitColor(resolveName(profile, playerName));
    if (color != null) {
      return color instanceof NamedTextColor named ? named : NamedTextColor.nearestTo(color);
    }
    return fallback != null ? fallback : NamedTextColor.GRAY;
  }
}
