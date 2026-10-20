package dev.lokspel.practice.manager.gui.guis.cosmetics;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.guis.cosmetics.shield.ShieldCosmeticsUtil;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticPresets;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsData;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.lokspel.practice.manager.profile.cosmetics.armortrim.ArmorTrimPreset;
import dev.lokspel.practice.manager.profile.cosmetics.shield.ShieldPreset;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CosmeticPresetsGui extends Gui {

  public enum PresetType {
    TRIM,
    SHIELD
  }

  private static final int ROWS = 6;
  private static final int BACK_SLOT = 45;
  private static final int CUSTOM_SLOT = 49;
  private static final int[] PRESET_SLOTS = {
    10, 11, 12, 13, 14, 15, 16,
    19, 20, 21, 22, 23, 24, 25,
    28, 29, 30, 31, 32, 33, 34
  };

  private final Profile profile;
  private final PresetType type;
  private final Gui backGui;

  public CosmeticPresetsGui(Profile profile, PresetType type, Gui backGui) {
    super(
        type == PresetType.TRIM ? GuiType.Cosmetics_TrimPresets : GuiType.Cosmetics_ShieldPresets);
    this.profile = profile;
    this.type = type;
    this.backGui = backGui;

    String title =
        type == PresetType.TRIM
            ? PracticeConfig.guis().getGuis().getCosmetics().getPresets().getTrimTitle()
            : PracticeConfig.guis().getGuis().getCosmetics().getPresets().getShieldTitle();
    this.gui.put(1, InventoryUtil.createInventory(title, ROWS));
    build();
  }

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(PracticeConfig.guis().getGeneralFillerItem()).get();
  }

  private static ItemStack backItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getCosmetics().getIcons().getBackTo())
        .get();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    Inventory inv = gui.get(1);
    inv.clear();
    for (int i = 0; i < inv.getSize(); i++) {
      inv.setItem(i, fillerItem());
    }

    Player player = profile.getPlayer().getPlayer();
    String activePresetId = getActivePresetId();

    if (type == PresetType.TRIM) {
      List<ArmorTrimPreset> presets = CosmeticPresets.getTrimPresets();
      for (int i = 0; i < PRESET_SLOTS.length && i < presets.size(); i++) {
        inv.setItem(PRESET_SLOTS[i], buildTrimPresetItem(presets.get(i), player, activePresetId));
      }
    } else {
      List<ShieldPreset> presets = CosmeticPresets.getShieldPresets();
      for (int i = 0; i < PRESET_SLOTS.length && i < presets.size(); i++) {
        inv.setItem(PRESET_SLOTS[i], buildShieldPresetItem(presets.get(i), player, activePresetId));
      }
    }

    inv.setItem(
        CUSTOM_SLOT,
        BackendUtil.getGuiItem(
                PracticeConfig.guis().getGuis().getCosmetics().getPresets().getCustomButton())
            .get());
    inv.setItem(BACK_SLOT, backItem() != null ? backItem() : new ItemStack(Material.ARROW));
    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    e.setCancelled(true);
    Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();

    if (slot == BACK_SLOT) {
      if (backGui != null) {
        backGui.update(true);
        backGui.open(player);
      }
      return;
    }

    var presetsConfig = PracticeConfig.guis().getGuis().getCosmetics().getPresets();

    if (slot == CUSTOM_SLOT) {
      applyPresetSelection(player, null);
      Common.sendMmMessage(player, presetsConfig.getCustomMessage());
      return;
    }

    int index = getSlotIndex(slot);
    if (index == -1) {
      return;
    }

    if (type == PresetType.TRIM) {
      List<ArmorTrimPreset> presets = CosmeticPresets.getTrimPresets();
      if (index >= presets.size()) {
        return;
      }

      ArmorTrimPreset preset = presets.get(index);
      if (!CosmeticsPermissionManager.hasTrimPresetPermission(player, preset)) {
        Common.sendMmMessage(player, presetsConfig.getNoPermissionMessage());
        return;
      }

      if (preset.id().equals(getActivePresetId())) {
        applyPresetSelection(player, null);
        Common.sendMmMessage(player, presetsConfig.getCustomMessage());
        return;
      }

      applyPresetSelection(player, preset.id());
      Common.sendMmMessage(
          player, presetsConfig.getSelectedMessage().replace("%preset%", preset.name()));
    } else {
      List<ShieldPreset> presets = CosmeticPresets.getShieldPresets();
      if (index >= presets.size()) {
        return;
      }

      ShieldPreset preset = presets.get(index);
      if (!CosmeticsPermissionManager.hasShieldPresetPermission(player, preset)) {
        Common.sendMmMessage(player, presetsConfig.getNoPermissionMessage());
        return;
      }

      if (preset.id().equals(getActivePresetId())) {
        applyPresetSelection(player, null);
        Common.sendMmMessage(player, presetsConfig.getCustomMessage());
        return;
      }

      applyPresetSelection(player, preset.id());
      Common.sendMmMessage(
          player, presetsConfig.getSelectedMessage().replace("%preset%", preset.name()));
    }
  }

  private void applyPresetSelection(Player player, String presetId) {
    CosmeticsData data = profile.getCosmeticsData();
    if (type == PresetType.TRIM) {
      data.setActiveTrimPreset(presetId);
    } else {
      data.setActiveShieldPreset(presetId);
    }
    profile.save();
    if (type == PresetType.SHIELD) {
      ShieldCosmeticsUtil.applyShieldToPlayer(player);
    }
    update(true);
  }

  private String getActivePresetId() {
    CosmeticsData data = profile.getCosmeticsData();
    return type == PresetType.TRIM ? data.getActiveTrimPreset() : data.getActiveShieldPreset();
  }

  private static int getSlotIndex(int slot) {
    for (int i = 0; i < PRESET_SLOTS.length; i++) {
      if (PRESET_SLOTS[i] == slot) {
        return i;
      }
    }
    return -1;
  }

  private static ItemStack buildTrimPresetItem(
      ArmorTrimPreset preset, Player player, String activePresetId) {
    boolean unlocked = CosmeticsPermissionManager.hasTrimPresetPermission(player, preset);
    boolean selected = preset.id().equals(activePresetId);
    return formatPresetItem(preset.createItem(), preset.name(), selected, unlocked);
  }

  private static ItemStack buildShieldPresetItem(
      ShieldPreset preset, Player player, String activePresetId) {
    boolean unlocked = CosmeticsPermissionManager.hasShieldPresetPermission(player, preset);
    boolean selected = preset.id().equals(activePresetId);
    return formatPresetItem(preset.createItem(), preset.name(), selected, unlocked);
  }

  private static ItemStack formatPresetItem(
      ItemStack item, String name, boolean selected, boolean unlocked) {
    var presetsConfig = PracticeConfig.guis().getGuis().getCosmetics().getPresets();
    String status =
        selected
            ? presetsConfig.getStatusSelected()
            : (unlocked ? presetsConfig.getStatusUnlocked() : presetsConfig.getStatusLocked());

    List<Component> lore =
        presetsConfig.getPresetLore().stream()
            .map(line -> noItalic(line.replace("%status%", status)))
            .toList();

    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.displayName(noItalic(name));
      meta.lore(lore);
      meta.setEnchantmentGlintOverride(selected);
      item.setItemMeta(meta);
    }
    return ItemCreateUtil.hideItemFlags(item);
  }

  private static Component noItalic(String mm) {
    return Common.deserializeMiniMessage(mm)
        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
  }
}
