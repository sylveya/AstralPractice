package dev.lokspel.practice.manager.fight.match.util;

import dev.lokspel.practice.manager.fight.match.enums.TeamEnum;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.gui.guis.cosmetics.shield.ShieldCosmeticsUtil;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.util.LadderUtil;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticPresets;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.lokspel.practice.manager.profile.cosmetics.armortrim.ArmorSlot;
import dev.lokspel.practice.manager.profile.cosmetics.armortrim.ArmorTrimPreset;
import dev.lokspel.practice.manager.profile.cosmetics.armortrim.ArmorTrimTier;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.kit.KitData;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

public enum KitUtil {
  ;

  public static void loadDefaultLadderKit(Player player, TeamEnum team, Ladder ladder) {
    KitData kitData = ladder.getKitData();
    loadKit(player, team, kitData.getArmor(), kitData.getStorage(), kitData.getExtra());
  }

  public static void loadKit(
      Player player, TeamEnum team, ItemStack[] armor, ItemStack[] inventory, ItemStack[] extra) {
    PlayerUtil.clearInventory(player);

    ItemStack[] armorCopy = InventoryUtil.cloneItems(armor);
    ItemStack[] inventoryCopy = InventoryUtil.cloneItems(inventory);
    ItemStack[] extraCopy = InventoryUtil.cloneItems(extra);

    if (team != null) {
      applyTeamColor(armorCopy, team.getColor());
      applyTeamColor(inventoryCopy, team.getColor());
      applyTeamColor(extraCopy, team.getColor());
    }

    LadderUtil.loadInventory(player, armorCopy, inventoryCopy, extraCopy);

    applyArmorTrimCosmetics(player);
    applyShieldCosmetics(player);
    player.updateInventory();
  }

  private static void applyTeamColor(ItemStack[] items, Component color) {
    if (items == null) {
      return;
    }
    for (int i = 0; i < items.length; i++) {
      if (items[i] != null) {
        items[i] = LadderUtil.changeItemColor(items[i], color);
      }
    }
  }

  /**
   * Apply armor trim cosmetics to the player's equipped armor. Retrieves the player's saved
   * cosmetics from their profile and applies them to armor pieces.
   */
  private static void applyArmorTrimCosmetics(Player player) {
    try {
      Profile profile = ProfileManager.getInstance().getProfile(player);
      if (profile == null || profile.getCosmeticsData() == null) {
        return;
      }

      ItemStack[] armorContents = player.getInventory().getArmorContents();
      if (armorContents.length < 4) {
        return;
      }

      // Helmet (index 3)
      applyTrimToArmor(player, profile, armorContents, ArmorSlot.HELMET, 3);

      // Chestplate (index 2)
      applyTrimToArmor(player, profile, armorContents, ArmorSlot.CHESTPLATE, 2);

      // Leggings (index 1)
      applyTrimToArmor(player, profile, armorContents, ArmorSlot.LEGGINGS, 1);

      // Boots (index 0)
      applyTrimToArmor(player, profile, armorContents, ArmorSlot.BOOTS, 0);

      player.getInventory().setArmorContents(armorContents);

    } catch (Exception e) {
      // Silently fail - if cosmetics cannot be applied, continue with kit distribution
    }
  }

  private static void applyShieldCosmetics(Player player) {
    try {
      ShieldCosmeticsUtil.applyShieldToPlayer(player);
    } catch (Exception e) {
      // Silently fail - if shield cosmetics cannot be applied, continue with kit distribution
    }
  }

  /** Apply a trim pattern and material to an armor piece if both are set. */
  private static void applyTrimToArmor(
      Player player, Profile profile, ItemStack[] armorContents, ArmorSlot slot, int armorIndex) {
    ItemStack item = armorContents[armorIndex];
    if (item == null) {
      return;
    }

    if (!(item.getItemMeta() instanceof ArmorMeta armorMeta)) {
      return;
    }

    ArmorTrim targetTrim = null;
    ArmorTrimTier armorTier = getArmorTier(item.getType());
    ArmorTrimPreset preset = CosmeticPresets.getTrimPreset(player, profile.getCosmeticsData());
    if (preset != null && armorTier != null) {
      targetTrim = preset.trims().get(slot);
    } else if (armorTier != null
        && CosmeticsPermissionManager.hasBasePermission(player, armorTier)) {
      TrimPattern pattern = profile.getCosmeticsData().getPattern(armorTier, slot);
      TrimMaterial material = profile.getCosmeticsData().getMaterial(armorTier, slot);
      if (pattern != null
          && material != null
          && CosmeticsPermissionManager.hasPatternPermission(player, pattern)
          && CosmeticsPermissionManager.hasMaterialPermission(player, material)) {
        targetTrim = new ArmorTrim(material, pattern);
      }
    }

    try {
      ArmorTrim currentTrim = armorMeta.getTrim();
      if (!Objects.equals(currentTrim, targetTrim)) {
        armorMeta.setTrim(targetTrim);
        item.setItemMeta(armorMeta);
        armorContents[armorIndex] = item;
      }
    } catch (Exception e) {
      // Silently fail - trim application may not be supported on this version or item type
    }
  }

  private static ArmorTrimTier getArmorTier(Material material) {
    return switch (material) {
      case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS ->
          ArmorTrimTier.LEATHER;
      case GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS -> ArmorTrimTier.GOLD;
      case IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS -> ArmorTrimTier.IRON;
      case DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS ->
          ArmorTrimTier.DIAMOND;
      case NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS ->
          ArmorTrimTier.NETHERITE;
      default -> null;
    };
  }
}
