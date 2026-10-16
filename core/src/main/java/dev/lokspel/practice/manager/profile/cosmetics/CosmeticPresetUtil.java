package dev.lokspel.practice.manager.profile.cosmetics;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.language.Command;
import dev.lokspel.practice.manager.profile.cosmetics.armortrim.ArmorSlot;
import dev.lokspel.practice.manager.profile.cosmetics.shield.ShieldLayout;
import dev.lokspel.practice.util.Common;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.ShieldMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

public final class CosmeticPresetUtil {

  private static final Object FILE_LOCK = new Object();
  private static final String FILE_NAME = "cosmetic-presets.yml";
  private static final ArmorSlot[] ARMOR_SLOTS = ArmorSlot.values();

  private CosmeticPresetUtil() {}

  private static Command.Practice.Arguments.Preset messages() {
    return PracticeConfig.language().getCommand().getPractice().getArguments().getPreset();
  }

  public static void createFromHeldItem(Player player, String label) {
    ItemStack handItem = player.getInventory().getItemInMainHand();
    ItemMeta meta = handItem.getType().isAir() ? null : handItem.getItemMeta();
    Component displayName = meta != null && meta.hasDisplayName() ? meta.displayName() : null;

    if (displayName == null) {
      Common.sendMmMessage(player, messages().getNoName().replace("%label%", label));
      return;
    }

    String id = sanitizeId(displayName);
    if (id.isEmpty()) {
      Common.sendMmMessage(player, messages().getInvalidName());
      return;
    }

    String displayNameMm = AstralPractice.getMiniMessage().serialize(displayName);

    if (handItem.getType() == Material.SHIELD) {
      saveShieldPreset(player, meta, id, displayNameMm);
    } else {
      saveArmorTrimPreset(player, handItem, id, displayNameMm);
    }
  }

  private static void saveShieldPreset(
      Player player, ItemMeta meta, String id, String displayName) {
    DyeColor baseColor = null;
    List<Pattern> patterns = List.of();

    if (meta instanceof ShieldMeta shieldMeta) {
      baseColor = shieldMeta.getBaseColor();
      patterns = shieldMeta.getPatterns();
    } else if (meta instanceof BlockStateMeta bsm && bsm.getBlockState() instanceof Banner banner) {
      baseColor = banner.getBaseColor();
      patterns = banner.getPatterns();
    }

    if (baseColor == null && patterns.isEmpty()) {
      Common.sendMmMessage(player, messages().getShieldNoDesign());
      return;
    }

    if (patterns.size() > ShieldLayout.MAX_LAYERS) {
      Common.sendMmMessage(
          player,
          messages()
              .getShieldTooManyLayers()
              .replace("%max%", String.valueOf(ShieldLayout.MAX_LAYERS))
              .replace("%layers%", String.valueOf(patterns.size())));
      return;
    }

    if (CosmeticPresets.hasShieldPreset(id)) {
      Common.sendMmMessage(player, messages().getShieldExists().replace("%id%", id));
      return;
    }

    var bannerRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN);
    List<String> layers = new ArrayList<>();
    for (Pattern pattern : patterns) {
      NamespacedKey patternKey = bannerRegistry.getKeyOrThrow(pattern.getPattern());
      String patternId =
          "minecraft".equals(patternKey.getNamespace())
              ? patternKey.getKey()
              : patternKey.toString();
      layers.add(pattern.getColor().name() + ":" + patternId);
    }

    Map<String, Object> values = new LinkedHashMap<>();
    values.put("NAME", displayName);
    values.put("BASE-COLOR", (baseColor != null ? baseColor : DyeColor.WHITE).name());
    values.put("LAYERS", layers);

    savePresetAsync(
        "SHIELDS." + id,
        values,
        () -> {
          String node = CosmeticsPermissionManager.getShieldPresetPermissionNode(id);
          Common.sendMmMessage(
              player,
              messages().getShieldSaved().replace("%id%", id).replace("%permission%", node));
        });
  }

  private static void saveArmorTrimPreset(
      Player player, ItemStack handItem, String id, String displayName) {
    Map<ArmorSlot, ArmorTrim> trims = extractTrims(player, handItem);
    if (trims.isEmpty()) {
      Common.sendMmMessage(player, messages().getNoTrim());
      return;
    }

    if (CosmeticPresets.hasTrimPreset(id)) {
      Common.sendMmMessage(player, messages().getTrimExists().replace("%id%", id));
      return;
    }

    Map<String, Object> values = new LinkedHashMap<>();
    values.put("NAME", displayName);
    values.put("ICON", handItem.getType().name());

    ArmorTrim defaultTrim = null;
    for (ArmorSlot slot : ARMOR_SLOTS) {
      ArmorTrim trim = trims.get(slot);
      if (trim == null) {
        continue;
      }
      if (defaultTrim == null) {
        defaultTrim = trim;
        values.put("PATTERN", getKeyString(RegistryKey.TRIM_PATTERN, trim.getPattern()));
        values.put("MATERIAL", getKeyString(RegistryKey.TRIM_MATERIAL, trim.getMaterial()));
        continue;
      }
      if (defaultTrim.equals(trim)) {
        continue;
      }
      values.put(
          slot.name() + ".PATTERN", getKeyString(RegistryKey.TRIM_PATTERN, trim.getPattern()));
      values.put(
          slot.name() + ".MATERIAL", getKeyString(RegistryKey.TRIM_MATERIAL, trim.getMaterial()));
    }

    savePresetAsync(
        "ARMOR-TRIMS." + id,
        values,
        () -> {
          String node = CosmeticsPermissionManager.getTrimPresetPermissionNode(id);
          Common.sendMmMessage(
              player, messages().getTrimSaved().replace("%id%", id).replace("%permission%", node));
        });
  }

  private static Map<ArmorSlot, ArmorTrim> extractTrims(Player player, ItemStack handItem) {
    Map<ArmorSlot, ArmorTrim> trims = new EnumMap<>(ArmorSlot.class);
    PlayerInventory inv = player.getInventory();
    ItemStack[] armor =
        new ItemStack[] {inv.getHelmet(), inv.getChestplate(), inv.getLeggings(), inv.getBoots()};

    for (int i = 0; i < ARMOR_SLOTS.length; i++) {
      ArmorTrim trim = getTrim(armor[i]);
      if (trim != null) {
        trims.put(ARMOR_SLOTS[i], trim);
      }
    }

    if (trims.isEmpty()) {
      ArmorTrim handTrim = getTrim(handItem);
      if (handTrim != null) {
        for (ArmorSlot slot : ARMOR_SLOTS) {
          trims.put(slot, handTrim);
        }
      }
    }

    return trims;
  }

  private static ArmorTrim getTrim(ItemStack item) {
    if (item == null || item.getType().isAir()) {
      return null;
    }
    return item.getItemMeta() instanceof ArmorMeta armorMeta && armorMeta.hasTrim()
        ? armorMeta.getTrim()
        : null;
  }

  private static String sanitizeId(Component component) {
    return PlainTextComponentSerializer.plainText()
        .serialize(component)
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^a-z0-9_]+", "_")
        .replaceAll("^_+|_+$", "");
  }

  private static <T extends Keyed> String getKeyString(RegistryKey<T> registryKey, T value) {
    NamespacedKey key =
        RegistryAccess.registryAccess().getRegistry(registryKey).getKeyOrThrow(value);
    return "minecraft".equals(key.getNamespace()) ? key.getKey() : key.toString();
  }

  private static void savePresetAsync(
      String pathPrefix, Map<String, Object> values, Runnable callback) {
    AstralPractice plugin = AstralPractice.getInstance();
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            plugin,
            () -> {
              synchronized (FILE_LOCK) {
                File file = new File(plugin.getDataFolder(), FILE_NAME);
                if (!file.exists()) {
                  plugin.saveResource(FILE_NAME, false);
                }
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                values.forEach((key, val) -> config.set(pathPrefix + "." + key, val));
                try {
                  config.save(file);
                } catch (IOException e) {
                  Common.sendConsoleMmMessage(
                      "<red>Could not save cosmetic-presets.yml: " + e.getMessage());
                  return;
                }
              }
              Bukkit.getScheduler()
                  .runTask(
                      plugin,
                      () -> {
                        CosmeticPresets.load();
                        if (callback != null) {
                          callback.run();
                        }
                      });
            });
  }
}
