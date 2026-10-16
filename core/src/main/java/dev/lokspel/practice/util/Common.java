package dev.lokspel.practice.util;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.profile.Profile;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public final class Common {

  private Common() {}

  public static void send(CommandSender sender, Component component) {
    if (sender == null) {
      return;
    }
    sender.sendMessage(component);
  }

  public static void sendMmMessage(Player player, String line) {
    if (line == null || line.isEmpty()) {
      return;
    }

    if (SoftDependUtil.isPAPI_ENABLED) {
      line = PlaceholderAPI.setPlaceholders(player, line);
    }

    send(player, toComponent(line));
  }

  public static void sendConsoleMmMessage(String string) {
    if (string == null || string.isEmpty()) {
      return;
    }
    send(AstralPractice.getInstance().getServer().getConsoleSender(), toComponent(string));
  }

  public static void playDeathEffect(
      Profile killerProfile, Location location, List<Player> viewers) {
    try {
      if (killerProfile == null || killerProfile.getCosmeticsData() == null) {
        return;
      }
      var deathEffect = killerProfile.getCosmeticsData().getDeathEffect();
      if (deathEffect == null) {
        return;
      }
      deathEffect.play(location, viewers);
    } catch (Exception ignored) {
      // This best-effort operation may fail without interrupting its caller.

    }
  }

  public static void sendMessage(
      Collection<? extends Player> players,
      Collection<? extends Player> spectators,
      String message,
      boolean includeSpectators) {
    for (Player player : players) {
      sendMmMessage(player, message);
    }
    if (includeSpectators) {
      for (Player spectator : spectators) {
        sendMmMessage(spectator, message);
      }
    }
  }

  public static Component toComponent(String text) {
    if (text == null || text.isEmpty()) {
      return Component.empty();
    }
    return AstralPractice.getMiniMessage().deserialize(StringUtil.legacyToMiniMessage(text));
  }

  public static List<Component> toComponent(List<String> list) {
    if (list == null) {
      return new ArrayList<>();
    }
    List<Component> components = new ArrayList<>(list.size());
    for (String line : list) {
      components.add(toComponent(line));
    }
    return components;
  }

  public static String toLegacy(Component component) {
    if (component == null) {
      return "";
    }
    return LegacyComponentSerializer.legacySection().serialize(component);
  }

  public static String toLegacy(String text) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    return toLegacy(toComponent(text));
  }

  public static List<String> toLegacy(List<String> list) {
    if (list == null) {
      return new ArrayList<>();
    }
    List<String> result = new ArrayList<>(list.size());
    for (String line : list) {
      result.add(toLegacy(line));
    }
    return result;
  }

  public static Component deserializeMiniMessage(String line) {
    return toComponent(line);
  }

  public static Component legacyToComponent(String message) {
    return toComponent(message);
  }

  public static String serializeComponentToLegacyString(Component component) {
    return toLegacy(component);
  }

  public static String mmToNormal(String line) {
    return toLegacy(line);
  }

  public static List<String> mmToNormal(List<String> list) {
    return toLegacy(list);
  }

  public static String serializeNormalToMmString(String normalString) {
    return StringUtil.legacyToMiniMessage(normalString);
  }

  public static String stripLegacyColor(String message) {
    if (message == null || message.isEmpty()) {
      return "";
    }
    return PlainTextComponentSerializer.plainText().serialize(toComponent(message));
  }

  public static String getItemDisplayName(ItemMeta itemMeta) {
    if (itemMeta == null) {
      return "";
    }
    Component displayName = itemMeta.displayName();
    if (displayName == null) {
      return "";
    }
    return serializeComponentToLegacyString(displayName);
  }

  public static String getItemDisplayName(ItemStack itemStack) {
    if (itemStack == null || !itemStack.hasItemMeta()) {
      return "";
    }
    return getItemDisplayName(itemStack.getItemMeta());
  }

  public static String getItemDisplayNameMiniMessage(ItemMeta itemMeta) {
    if (itemMeta == null) {
      return "";
    }
    Component displayName = itemMeta.displayName();
    if (displayName == null) {
      return "";
    }
    return AstralPractice.getMiniMessage().serialize(displayName);
  }

  public static String getItemDisplayNameMiniMessage(ItemStack itemStack) {
    if (itemStack == null || !itemStack.hasItemMeta()) {
      return "";
    }
    return getItemDisplayNameMiniMessage(itemStack.getItemMeta());
  }

  public static short getItemDamage(ItemStack itemStack) {
    if (itemStack == null || !itemStack.hasItemMeta()) {
      return 0;
    }
    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta instanceof Damageable damageable) {
      return (short) damageable.getDamage();
    }
    return 0;
  }

  public static Iterable<Enchantment> getAllEnchantments() {
    return RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
  }

  public static Enchantment resolveEnchantment(String enchantmentName) {
    return NamespacedKeyUtil.resolve(RegistryKey.ENCHANTMENT, enchantmentName);
  }
}
