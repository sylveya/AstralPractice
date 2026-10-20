package dev.lokspel.practice.manager.gui.guis.cosmetics;

import dev.lokspel.api.cosmetics.CosmeticCategory;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.guis.cosmetics.armortrim.ArmorTrimMainGui;
import dev.lokspel.practice.manager.gui.guis.cosmetics.deatheffect.DeathEffectsGui;
import dev.lokspel.practice.manager.gui.guis.cosmetics.shield.ShieldLayoutListGui;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsCategoryManager;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsData;
import dev.lokspel.practice.manager.profile.cosmetics.deatheffect.DeathEffect;
import dev.lokspel.practice.manager.profile.cosmetics.shield.ShieldLayout;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Main cosmetics hub. Opens from the lobby hotbar item or /cosmetics command. Displays built-in
 * cosmetics categories as well as any addon-registered categories.
 */
public class CosmeticsHubGui extends Gui {

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(PracticeConfig.guis().getGeneralFillerItem()).get();
  }

  private static final int BASE_ROWS = 3;
  private static final int TRIMS_SLOT = 10;
  private static final int SHIELD_SLOT = 12;
  private static final int LOBBY_ITEMS_SLOT = 14;
  private static final int KILL_EFF_SLOT = 16;

  private static final Set<Integer> RESERVED_SLOTS =
      Set.of(TRIMS_SLOT, SHIELD_SLOT, LOBBY_ITEMS_SLOT, KILL_EFF_SLOT);

  private static final int[] AUTO_SLOT_PRIORITY = {
    // Row 2 (centered out)
    22,
    20,
    24,
    21,
    23,
    19,
    25,
    18,
    26,
    // Row 3
    31,
    29,
    33,
    30,
    32,
    28,
    34,
    27,
    35,
    // Row 4
    40,
    38,
    42,
    39,
    41,
    37,
    43,
    36,
    44,
    // Row 5
    49,
    47,
    51,
    48,
    50,
    46,
    52,
    45,
    53,
    // Spare row 1 / 0 slots
    11,
    13,
    15,
    4,
    2,
    6,
    3,
    5,
    1,
    7,
    0,
    8,
    9,
    17
  };

  private final Profile profile;
  private Map<Integer, CosmeticCategory> slotAssignments = new LinkedHashMap<>();

  public CosmeticsHubGui(Profile profile) {
    super(GuiType.Cosmetics_Hub);
    this.profile = profile;

    Player onlinePlayer = profile.getOnlinePlayer();
    this.slotAssignments = calculateSlotAssignments(onlinePlayer);
    int rows = calculateRows(this.slotAssignments);

    String title = PracticeConfig.guis().getGuis().getCosmetics().getHub().getTitle();
    this.gui.put(1, InventoryUtil.createInventory(title, rows));
    build();
  }

  private Map<Integer, CosmeticCategory> calculateSlotAssignments(Player player) {
    Map<Integer, CosmeticCategory> assignments = new LinkedHashMap<>();
    Set<Integer> occupied = new HashSet<>(RESERVED_SLOTS);
    List<CosmeticCategory> autoAssign = new ArrayList<>();

    // 1. Explicit valid slots
    for (CosmeticCategory cat : CosmeticsCategoryManager.getInstance().getCategories()) {
      if (player != null && !cat.isVisible(player)) {
        continue;
      }
      int preferred = cat.getSlot();
      if (preferred >= 0 && preferred < 54 && !occupied.contains(preferred)) {
        assignments.put(preferred, cat);
        occupied.add(preferred);
      } else {
        autoAssign.add(cat);
      }
    }

    // 2. Auto-assign remaining categories
    for (CosmeticCategory cat : autoAssign) {
      for (int candidate : AUTO_SLOT_PRIORITY) {
        if (!occupied.contains(candidate)) {
          assignments.put(candidate, cat);
          occupied.add(candidate);
          break;
        }
      }
    }

    return assignments;
  }

  private int calculateRows(Map<Integer, CosmeticCategory> assignments) {
    int maxSlot = 16;
    for (int slot : assignments.keySet()) {
      if (slot > maxSlot) {
        maxSlot = slot;
      }
    }
    return Math.max(BASE_ROWS, Math.min(6, (maxSlot / 9) + 1));
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

    inv.setItem(TRIMS_SLOT, buildTrimsButton());
    inv.setItem(SHIELD_SLOT, buildShieldButton());
    inv.setItem(LOBBY_ITEMS_SLOT, buildLobbyItemsButton());
    inv.setItem(KILL_EFF_SLOT, buildKillEffectButton());

    Player onlinePlayer = profile.getOnlinePlayer();
    for (Map.Entry<Integer, CosmeticCategory> entry : slotAssignments.entrySet()) {
      int slot = entry.getKey();
      CosmeticCategory category = entry.getValue();
      if (slot < inv.getSize()) {
        try {
          ItemStack icon = category.getIcon(onlinePlayer);
          if (icon != null) {
            inv.setItem(slot, icon);
          }
        } catch (Exception ex) {
          AstralPractice.getInstance()
              .getLogger()
              .warning(
                  "Failed to render icon for cosmetic category '"
                      + category.getId()
                      + "': "
                      + ex.getMessage());
        }
      }
    }

    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    e.setCancelled(true);
    Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();

    switch (slot) {
      case TRIMS_SLOT -> new ArmorTrimMainGui(profile, this).open(player);
      case SHIELD_SLOT -> new ShieldLayoutListGui(profile, this).open(player);
      case LOBBY_ITEMS_SLOT -> new LobbyItemsGui(profile, this).open(player);
      case KILL_EFF_SLOT -> new DeathEffectsGui(profile, this).open(player);

      default -> {
        CosmeticCategory category = slotAssignments.get(slot);
        if (category != null) {
          try {
            category.onClick(player, e);
          } catch (Exception ex) {
            AstralPractice.getInstance()
                .getLogger()
                .warning(
                    "Error executing click for cosmetic category '"
                        + category.getId()
                        + "': "
                        + ex.getMessage());
          }
        }
      }
    }
  }

  // Button builders
  private ItemStack buildTrimsButton() {
    String name =
        PracticeConfig.guis()
            .getGuis()
            .getCosmetics()
            .getHub()
            .getButtons()
            .getArmorTrims()
            .getName();
    Material mat =
        safeMaterial(
            PracticeConfig.guis()
                .getGuis()
                .getCosmetics()
                .getHub()
                .getButtons()
                .getArmorTrims()
                .getMaterial(),
            Material.DIAMOND_CHESTPLATE);
    List<String> lore =
        getOrDefaultLore(
            PracticeConfig.guis()
                .getGuis()
                .getCosmetics()
                .getHub()
                .getButtons()
                .getArmorTrims()
                .getLore(),
            List.of("<gray>Customize your armor tier,", "<gray>trim patterns and materials."));

    GuiItem item = new GuiItem(name, mat, lore);
    item.setGlowing(
        PracticeConfig.guis()
            .getGuis()
            .getCosmetics()
            .getHub()
            .getButtons()
            .getArmorTrims()
            .isGlow());
    return ItemCreateUtil.hideItemFlags(item.get());
  }

  private ItemStack buildShieldButton() {
    String name =
        PracticeConfig.guis().getGuis().getCosmetics().getHub().getButtons().getShield().getName();
    Material mat =
        safeMaterial(
            PracticeConfig.guis()
                .getGuis()
                .getCosmetics()
                .getHub()
                .getButtons()
                .getShield()
                .getMaterial(),
            Material.SHIELD);
    List<String> lore =
        getOrDefaultLore(
            PracticeConfig.guis()
                .getGuis()
                .getCosmetics()
                .getHub()
                .getButtons()
                .getShield()
                .getLore(),
            List.of(
                "<gray>Design your shield with any",
                "<gray>color and pattern combination.",
                "<gray>Save multiple layouts."));

    ShieldLayout active = profile.getCosmeticsData().getActiveShieldLayout();
    List<String> finalLore = new ArrayList<>(lore);
    finalLore.add("");
    finalLore.add(
        "<gray>Active layout: <yellow>" + (active != null ? active.getName() : "<red>None"));
    finalLore.add(
        "<gray>Saved layouts: <yellow>" + profile.getCosmeticsData().getShieldLayouts().size());

    GuiItem item = new GuiItem(name, mat, finalLore);
    item.setGlowing(
        PracticeConfig.guis().getGuis().getCosmetics().getHub().getButtons().getShield().isGlow());
    return item.get();
  }

  private ItemStack buildKillEffectButton() {
    String name =
        PracticeConfig.guis()
            .getGuis()
            .getCosmetics()
            .getHub()
            .getButtons()
            .getKillEffects()
            .getName();
    Material mat =
        safeMaterial(
            PracticeConfig.guis()
                .getGuis()
                .getCosmetics()
                .getHub()
                .getButtons()
                .getKillEffects()
                .getMaterial(),
            Material.BLAZE_POWDER);
    List<String> lore =
        getOrDefaultLore(
            PracticeConfig.guis()
                .getGuis()
                .getCosmetics()
                .getHub()
                .getButtons()
                .getKillEffects()
                .getLore(),
            List.of("<gray>Choose a particle effect", "<gray>that plays when you kill someone."));

    DeathEffect active = profile.getCosmeticsData().getDeathEffect();
    List<String> finalLore = new ArrayList<>(lore);
    finalLore.add("");
    finalLore.add(
        "<gray>Active effect: <yellow>" + (active != null ? active.getDisplayName() : "<red>None"));

    GuiItem item = new GuiItem(name, mat, finalLore);
    item.setGlowing(
        PracticeConfig.guis()
            .getGuis()
            .getCosmetics()
            .getHub()
            .getButtons()
            .getKillEffects()
            .isGlow());
    return item.get();
  }

  private ItemStack buildLobbyItemsButton() {
    String name =
        PracticeConfig.guis()
            .getGuis()
            .getCosmetics()
            .getHub()
            .getButtons()
            .getLobbyItems()
            .getName();
    Material mat =
        safeMaterial(
            PracticeConfig.guis()
                .getGuis()
                .getCosmetics()
                .getHub()
                .getButtons()
                .getLobbyItems()
                .getMaterial(),
            Material.ELYTRA);
    List<String> lore =
        getOrDefaultLore(
            PracticeConfig.guis()
                .getGuis()
                .getCosmetics()
                .getHub()
                .getButtons()
                .getLobbyItems()
                .getLore(),
            List.of("<gray>Select movement cosmetics", "<gray>for your lobby loadout."));

    CosmeticsData.LobbyItemType active = profile.getCosmeticsData().getLobbyItemType();
    List<String> finalLore = new ArrayList<>(lore);
    finalLore.add("");
    finalLore.add(
        "<gray>Active item: <yellow>" + formatName(active == null ? "NONE" : active.name()));

    GuiItem item = new GuiItem(name, mat, finalLore);
    item.setGlowing(
        PracticeConfig.guis()
            .getGuis()
            .getCosmetics()
            .getHub()
            .getButtons()
            .getLobbyItems()
            .isGlow());
    return item.get();
  }

  // Helpers
  private List<String> getOrDefaultLore(List<String> lore, List<String> defaults) {
    return lore == null || lore.isEmpty() ? defaults : new ArrayList<>(lore);
  }

  private static Material safeMaterial(String name, Material fallback) {
    if (name == null || name.isBlank()) {
      return fallback;
    }
    try {
      return Material.valueOf(name.toUpperCase());
    } catch (Exception ignored) {
      return fallback;
    }
  }

  private static String formatName(String raw) {
    if (raw == null) {
      return "None";
    }
    String lower = raw.replace('_', ' ').toLowerCase();
    return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
  }
}
