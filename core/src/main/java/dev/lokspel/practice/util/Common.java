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

    send(player, AstralPractice.getMiniMessage().deserialize(StringUtil.legacyToMiniMessage(line)));
  }

  public static void sendConsoleMmMessage(String string) {
    send(
        AstralPractice.getInstance().getServer().getConsoleSender(),
        AstralPractice.getMiniMessage().deserialize(StringUtil.legacyToMiniMessage(string)));
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

  public static Component deserializeMiniMessage(String line) {
    if (line == null || line.isEmpty()) {
      return Component.empty();
    }
    return AstralPractice.getMiniMessage().deserialize(StringUtil.legacyToMiniMessage(line));
  }

  public static String serializeComponentToLegacyString(Component component) {
    return LegacyComponentSerializer.legacyAmpersand().serialize(component);
  }

  public static String mmToNormal(String line) {
    return StringUtil.cc(serializeComponentToLegacyString(deserializeMiniMessage(line)));
  }

  public static List<String> mmToNormal(List<String> list) {
    List<String> newList = new ArrayList<>();
    for (String line : list) {
      newList.add(mmToNormal(line));
    }
    return newList;
  }

  public static String serializeNormalToMmString(String normalString) {
    return StringUtil.legacyToMiniMessage(normalString);
  }

  public static String colorize(String message) {
    return serializeComponentToLegacyString(deserializeMiniMessage(message));
  }

  public static Component legacyToComponent(String message) {
    return deserializeMiniMessage(message);
  }

  public static String stripLegacyColor(String message) {
    if (message == null || message.isEmpty()) {
      return "";
    }
    return PlainTextComponentSerializer.plainText().serialize(deserializeMiniMessage(message));
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
