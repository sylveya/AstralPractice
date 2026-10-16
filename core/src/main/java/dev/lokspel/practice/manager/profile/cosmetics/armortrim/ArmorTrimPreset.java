package dev.lokspel.practice.manager.profile.cosmetics.armortrim;

import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.NamespacedKeyUtil;
import io.papermc.paper.registry.RegistryKey;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public record ArmorTrimPreset(
    String id, String name, Material icon, Map<ArmorSlot, ArmorTrim> trims) {

  public static ArmorTrimPreset fromConfig(String key, ConfigurationSection entry) {
    if (entry == null || !entry.getBoolean("ENABLED", true)) {
      return null;
    }

    String id = key.toLowerCase(Locale.ROOT);
    Map<ArmorSlot, ArmorTrim> trims = new EnumMap<>(ArmorSlot.class);

    for (ArmorSlot slot : ArmorSlot.values()) {
      String patternKey = entry.getString(slot.name() + ".PATTERN", entry.getString("PATTERN", ""));
      String materialKey =
          entry.getString(slot.name() + ".MATERIAL", entry.getString("MATERIAL", ""));
      ArmorTrim trim = resolveTrim(patternKey, materialKey);
      if (trim != null) {
        trims.put(slot, trim);
      }
    }

    if (trims.isEmpty()) {
      Common.sendConsoleMmMessage("<red>Armor trim preset '" + key + "' has no valid trims.");
      return null;
    }

    Material icon = Material.matchMaterial(entry.getString("ICON", "DIAMOND_CHESTPLATE"));
    return new ArmorTrimPreset(
        id, entry.getString("NAME", key), icon != null ? icon : Material.DIAMOND_CHESTPLATE, trims);
  }

  public ItemStack createItem() {
    ItemStack item = new ItemStack(icon != null ? icon : Material.DIAMOND_CHESTPLATE);
    ArmorTrim trim =
        trims.getOrDefault(ArmorSlot.CHESTPLATE, trims.values().stream().findFirst().orElse(null));
    if (trim != null && item.getItemMeta() instanceof ArmorMeta armorMeta) {
      armorMeta.setTrim(trim);
      item.setItemMeta(armorMeta);
    }
    return item;
  }

  private static ArmorTrim resolveTrim(String patternStr, String materialStr) {
    TrimPattern pattern = NamespacedKeyUtil.resolve(RegistryKey.TRIM_PATTERN, patternStr);
    TrimMaterial material = NamespacedKeyUtil.resolve(RegistryKey.TRIM_MATERIAL, materialStr);
    if (pattern == null || material == null) {
      return null;
    }

    return new ArmorTrim(material, pattern);
  }
}
