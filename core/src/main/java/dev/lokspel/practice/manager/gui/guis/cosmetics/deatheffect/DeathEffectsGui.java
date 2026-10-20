package dev.lokspel.practice.manager.gui.guis.cosmetics.deatheffect;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.gui.Cosmetics;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.lokspel.practice.manager.profile.cosmetics.deatheffect.DeathEffect;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** GUI for selecting a kill effect cosmetic. Opens via the cosmetics hub → Kill Effects section. */
public class DeathEffectsGui extends Gui {

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(PracticeConfig.guis().getGeneralFillerItem()).get();
  }

  private static ItemStack backItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getCosmetics().getIcons().getBackTo())
        .get();
  }

  /* layout: 5 rows, last row = back + filler */
  private static final int ROWS = 4;
  private static final int BACK_SLOT = 27;

  /* effect slots — 3 rows of 7 centered (cols 1-7 of rows 1-3) */
  private static final int[] EFFECT_SLOTS = {
    10, 11, 12, 13, 14, 15, 16,
    19, 20, 21, 22, 23, 24, 25
  };

  /* border around effect area: top row, bottom row, and left/right sides (rows 1-4) */
  private static final int[] FRAME_SLOTS = {
    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35
  };

  private final Profile profile;
  private final Gui backToGui;

  public DeathEffectsGui(Profile profile, Gui backToGui) {
    super(GuiType.Cosmetics_DeathEffects);
    this.profile = profile;
    this.backToGui = backToGui;

    String title = PracticeConfig.guis().getGuis().getCosmetics().getDeathEffects().getTitle();
    this.gui.put(1, InventoryUtil.createInventory(title, ROWS));
    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    Inventory inv = gui.get(1);
    inv.clear();

    for (int frameSlot : FRAME_SLOTS) {
      inv.setItem(frameSlot, fillerItem());
    }

    DeathEffect active = profile.getCosmeticsData().getDeathEffect();
    DeathEffect[] effects = DeathEffect.values();
    Player profilePlayer = profile.getPlayer().getPlayer();

    for (int i = 0; i < EFFECT_SLOTS.length && i < effects.length; i++) {
      DeathEffect deathEffect = effects[i];
      inv.setItem(EFFECT_SLOTS[i], buildEffectItem(deathEffect, active, profilePlayer));
    }

    inv.setItem(BACK_SLOT, backItem() != null ? backItem() : new ItemStack(Material.ARROW));
    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    e.setCancelled(true);
    Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();

    if (slot == BACK_SLOT) {
      backToGui.update(true);
      backToGui.open(player);
      return;
    }

    // Identify which effect was clicked
    for (int i = 0; i < EFFECT_SLOTS.length && i < DeathEffect.values().length; i++) {
      if (slot == EFFECT_SLOTS[i]) {
        DeathEffect deathEffect = DeathEffect.values()[i];
        handleEffectClick(player, deathEffect);
        return;
      }
    }
  }

  private void handleEffectClick(Player player, DeathEffect deathEffect) {
    // Permission check
    if (!CosmeticsPermissionManager.hasDeathEffectPermission(player, deathEffect)) {
      String msg =
          PracticeConfig.guis().getGuis().getCosmetics().getDeathEffects().getNoPermissionMessage();
      Common.sendMmMessage(player, msg);
      return;
    }

    // Toggle off if already selected
    if (profile.getCosmeticsData().getDeathEffect() == deathEffect) {
      profile.getCosmeticsData().setDeathEffect(DeathEffect.NONE);
    } else {
      profile.getCosmeticsData().setDeathEffect(deathEffect);

      try {
        deathEffect.play(player.getLocation(), List.of(player));
      } catch (Exception ignored) {
        // This best-effort operation may fail without interrupting its caller.

      }
    }

    profile.save();
    update(true);
  }

  private ItemStack buildEffectItem(DeathEffect deathEffect, DeathEffect active, Player player) {
    Material mat = deathEffect.getConfiguredIcon();
    GuiItem item = new GuiItem(mat);

    boolean isActive = (deathEffect == active);
    boolean hasPerms =
        player == null || CosmeticsPermissionManager.hasDeathEffectPermission(player, deathEffect);

    Map<String, Cosmetics.DeathEffects.DeathEffectEntry> entries =
        PracticeConfig.guis().getGuis().getCosmetics().getDeathEffects().getEntries();
    Cosmetics.DeathEffects.DeathEffectEntry entry =
        entries != null ? entries.get(deathEffect.name()) : null;
    String nameTemplate =
        entry != null && entry.getDisplayName() != null
            ? entry.getDisplayName()
            : deathEffect.getDefaultDisplayName();

    String statusPrefix;
    if (isActive) {
      statusPrefix = "<green><bold>Selected <dark_gray>| <white>";
    } else if (hasPerms) {
      statusPrefix = "<yellow><bold>Unlocked <dark_gray>| <white>";
    } else {
      statusPrefix = "<red><bold>Locked <dark_gray>| <white>";
    }

    item.setName(statusPrefix + nameTemplate);

    List<String> lore = new ArrayList<>();
    List<String> loreTemplate = entry != null ? entry.getLore() : null;
    if (loreTemplate == null || loreTemplate.isEmpty()) {
      loreTemplate =
          PracticeConfig.guis().getGuis().getCosmetics().getDeathEffects().getDefaultLore();
    }
    for (String line : loreTemplate) {
      lore.add(
          line.replace(
              "%status%",
              isActive ? "<green>Selected" : (hasPerms ? "<yellow>Unlocked" : "<red>Locked")));
    }

    lore.add(
        0,
        "<dark_gray>Status: "
            + (isActive ? "<green>Selected" : (hasPerms ? "<yellow>Unlocked" : "<red>Locked")));
    lore.add(1, "");

    if (isActive) {
      lore.add("");
      lore.add(
          PracticeConfig.guis().getGuis().getCosmetics().getDeathEffects().getClickToDeselect());
    } else if (hasPerms) {
      lore.add("");
      lore.add(PracticeConfig.guis().getGuis().getCosmetics().getDeathEffects().getClickToSelect());
      lore.add("<gray>A preview will play for you.");
    } else {
      lore.add("");
      lore.add("<red>You do not have permission for this effect.");
    }

    item.setLore(lore);
    item.setGlowing(isActive);

    ItemStack stack = item.get();

    if (isActive && stack.hasItemMeta()) {
      ItemMeta meta = stack.getItemMeta();
      meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
      stack.setItemMeta(meta);
    }

    return ItemCreateUtil.hideItemFlags(stack);
  }
}
