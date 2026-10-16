package dev.lokspel.practice.manager.profile.cosmetics;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.profile.cosmetics.armortrim.ArmorTrimPreset;
import dev.lokspel.practice.manager.profile.cosmetics.shield.ShieldPreset;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

public final class CosmeticPresets {

  private static final String FILE_NAME = "cosmetic-presets.yml";
  private static final String TRIM_PATH = "ARMOR-TRIMS";
  private static final String SHIELD_PATH = "SHIELDS";
  private static final String WILDCARD_PERMISSION = "ap.cosmetics.preset.*";

  private static final Map<String, ArmorTrimPreset> TRIM_PRESETS = new LinkedHashMap<>();
  private static final Map<String, ShieldPreset> SHIELD_PRESETS = new LinkedHashMap<>();

  private CosmeticPresets() {}

  public static void load() {
    File file = new File(AstralPractice.getInstance().getDataFolder(), FILE_NAME);
    if (!file.exists()) {
      AstralPractice.getInstance().saveResource(FILE_NAME, false);
    }
    if (!file.exists()) {
      return;
    }

    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

    Map<String, ArmorTrimPreset> loadedTrims = new LinkedHashMap<>();
    ConfigurationSection trimSection = config.getConfigurationSection(TRIM_PATH);
    if (trimSection != null) {
      for (String key : trimSection.getKeys(false)) {
        ArmorTrimPreset preset =
            ArmorTrimPreset.fromConfig(key, trimSection.getConfigurationSection(key));
        if (preset != null) {
          loadedTrims.put(preset.id(), preset);
        }
      }
    }

    Map<String, ShieldPreset> loadedShields = new LinkedHashMap<>();
    ConfigurationSection shieldSection = config.getConfigurationSection(SHIELD_PATH);
    if (shieldSection != null) {
      for (String key : shieldSection.getKeys(false)) {
        ShieldPreset preset =
            ShieldPreset.fromConfig(key, shieldSection.getConfigurationSection(key));
        if (preset != null) {
          loadedShields.put(preset.id(), preset);
        }
      }
    }

    TRIM_PRESETS.clear();
    TRIM_PRESETS.putAll(loadedTrims);
    SHIELD_PRESETS.clear();
    SHIELD_PRESETS.putAll(loadedShields);

    registerPermissions();
  }

  public static void registerPermissions() {
    PluginManager pluginManager = Bukkit.getPluginManager();
    Map<String, Boolean> children = new HashMap<>();

    for (ArmorTrimPreset preset : TRIM_PRESETS.values()) {
      String node = CosmeticsPermissionManager.getTrimPresetPermissionNode(preset.id());
      CosmeticsPermissionManager.registerPermission(
          pluginManager, node, "Use the " + preset.name() + " armor trim preset.");
      children.put(node, true);
    }

    for (ShieldPreset preset : SHIELD_PRESETS.values()) {
      String node = CosmeticsPermissionManager.getShieldPresetPermissionNode(preset.id());
      CosmeticsPermissionManager.registerPermission(
          pluginManager, node, "Use the " + preset.name() + " shield preset.");
      children.put(node, true);
    }

    Permission wildcard = pluginManager.getPermission(WILDCARD_PERMISSION);
    if (wildcard == null) {
      pluginManager.addPermission(
          new Permission(
              WILDCARD_PERMISSION, "Use all cosmetic presets.", PermissionDefault.OP, children));
    } else {
      wildcard.getChildren().putAll(children);
      wildcard.recalculatePermissibles();
    }
  }

  public static boolean hasTrimPreset(String id) {
    return getTrimPreset(id) != null;
  }

  public static boolean hasShieldPreset(String id) {
    return getShieldPreset(id) != null;
  }

  public static ItemStack createOpenButtonItem(ArmorTrimPreset activePreset) {
    return createOpenButtonItem(activePreset != null ? activePreset.name() : null);
  }

  public static ItemStack createOpenButtonItem(ShieldPreset activePreset) {
    return createOpenButtonItem(activePreset != null ? activePreset.name() : null);
  }

  public static ItemStack createOpenButtonItem(String activePresetName) {
    GuiItem presetItem =
        BackendUtil.getGuiItem(
                PracticeConfig.guis().getGuis().getCosmetics().getPresets().getOpenButton())
            .cloneItem();
    String activeName =
        activePresetName != null
            ? activePresetName
            : PracticeConfig.guis().getGuis().getCosmetics().getPresets().getNone();
    presetItem.replace("%active%", activeName);
    return presetItem.get();
  }

  public static List<ArmorTrimPreset> getTrimPresets() {
    return new ArrayList<>(TRIM_PRESETS.values());
  }

  public static ArmorTrimPreset getTrimPreset(String id) {
    return id != null ? TRIM_PRESETS.get(id.toLowerCase(Locale.ROOT)) : null;
  }

  public static ArmorTrimPreset getTrimPreset(Player player, CosmeticsData data) {
    if (player == null || data == null || data.getActiveTrimPreset() == null) {
      return null;
    }
    ArmorTrimPreset preset = getTrimPreset(data.getActiveTrimPreset());
    return CosmeticsPermissionManager.hasTrimPresetPermission(player, preset) ? preset : null;
  }

  public static List<ShieldPreset> getShieldPresets() {
    return new ArrayList<>(SHIELD_PRESETS.values());
  }

  public static ShieldPreset getShieldPreset(String id) {
    return id != null ? SHIELD_PRESETS.get(id.toLowerCase(Locale.ROOT)) : null;
  }

  public static ShieldPreset getShieldPreset(Player player, CosmeticsData data) {
    if (player == null || data == null || data.getActiveShieldPreset() == null) {
      return null;
    }
    ShieldPreset preset = getShieldPreset(data.getActiveShieldPreset());
    return CosmeticsPermissionManager.hasShieldPresetPermission(player, preset) ? preset : null;
  }
}
