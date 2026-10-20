package dev.lokspel.practice.util.items;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.util.StringUtil;
import java.util.List;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemCreateUtil {

  /**
   * Parses a raw config string into a {@link Component}, supporting all color formats: legacy
   * {@code <red>} / {@code §c} codes, shorthand hex {@code &#RRGGBB}, Bungeecord hex {@code
   * &x<reset><reset>&G&G<aqua><aqua>}, bare {@code #RRGGBB}, and MiniMessage tags {@code <red>}.
   */
  private static Component parseColor(String raw) {
    if (raw == null || raw.isEmpty()) {
      return Component.empty();
    }
    return AstralPractice.getMiniMessage()
        .deserialize(StringUtil.legacyToMiniMessage(raw))
        .decorationIfAbsent(
            net.kyori.adventure.text.format.TextDecoration.ITALIC,
            net.kyori.adventure.text.format.TextDecoration.State.FALSE);
  }

  private static List<Component> parseColorLore(List<String> lore) {
    return lore.stream().map(ItemCreateUtil::parseColor).collect(Collectors.toList());
  }

  public static ItemStack createItem(
      String displayname, Material material, Short damage, int amount, List<String> lore) {
    ItemStack itemStack = new ItemStack(material, amount);
    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta != null) {
      itemMeta.displayName(parseColor(displayname));
      itemMeta.lore(parseColorLore(lore));
      if (itemMeta instanceof Damageable) {
        ((Damageable) itemMeta).setDamage(damage);
      }
      hideItemFlags(itemMeta);
      itemStack.setItemMeta(itemMeta);
    }
    return itemStack;
  }

  public static ItemStack createItem(String displayname, Material material) {
    ItemStack itemStack = new ItemStack(material);
    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta != null) {
      itemMeta.displayName(parseColor(displayname));
      hideItemFlags(itemMeta);
      itemStack.setItemMeta(itemMeta);
    }
    return itemStack;
  }

  public static ItemStack createItem(Material material, Short damage) {
    ItemStack itemStack = new ItemStack(material);
    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta != null) {
      if (itemMeta instanceof Damageable) {
        ((Damageable) itemMeta).setDamage(damage);
      }
      hideItemFlags(itemMeta);
      itemStack.setItemMeta(itemMeta);
    }
    return itemStack;
  }

  public static ItemStack createItem(String displayname, Material material, Short damage) {
    ItemStack itemStack = new ItemStack(material);
    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta != null) {
      itemMeta.displayName(parseColor(displayname));
      if (itemMeta instanceof Damageable) {
        ((Damageable) itemMeta).setDamage(damage);
      }
      hideItemFlags(itemMeta);
      itemStack.setItemMeta(itemMeta);
    }
    return itemStack;
  }

  public static ItemStack createItem(String displayname, Material material, List<String> lore) {
    ItemStack itemStack = new ItemStack(material);
    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta != null) {
      itemMeta.displayName(parseColor(displayname));
      itemMeta.lore(parseColorLore(lore));
      hideItemFlags(itemMeta);
      itemStack.setItemMeta(itemMeta);
    }
    return itemStack;
  }

  public static ItemStack createItem(
      String displayname, Material material, Short damage, List<String> lore) {
    ItemStack itemStack = new ItemStack(material);
    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta != null) {
      itemMeta.displayName(parseColor(displayname));
      itemMeta.lore(parseColorLore(lore));
      if (itemMeta instanceof Damageable) {
        ((Damageable) itemMeta).setDamage(damage);
      }
      hideItemFlags(itemMeta);
      itemStack.setItemMeta(itemMeta);
    }
    return itemStack;
  }

  public static ItemStack createItem(ItemStack item, List<String> lore) {
    ItemStack itemStack = new ItemStack(item);
    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta != null) {
      itemMeta.lore(parseColorLore(lore));
      hideItemFlags(itemMeta);
      itemStack.setItemMeta(itemMeta);
    }
    return itemStack;
  }

  public static ItemStack createItem(ItemStack item, String name, List<String> lore) {
    ItemStack itemStack = new ItemStack(item);
    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta != null) {
      itemMeta.displayName(parseColor(name));
      itemMeta.lore(parseColorLore(lore));
      hideItemFlags(itemMeta);
      itemStack.setItemMeta(itemMeta);
    }
    return itemStack;
  }

  public static void hideItemFlags(ItemMeta itemMeta) {
    itemMeta.addItemFlags(
        ItemFlag.HIDE_ENCHANTS,
        ItemFlag.HIDE_ATTRIBUTES,
        ItemFlag.HIDE_DESTROYS,
        ItemFlag.HIDE_UNBREAKABLE,
        ItemFlag.HIDE_PLACED_ON,
        ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
  }

  public static ItemStack hideItemFlags(ItemStack item) {
    ItemMeta itemMeta = item.getItemMeta();
    hideItemFlags(itemMeta);
    item.setItemMeta(itemMeta);
    return item;
  }

  public static ItemStack getPlayerHead(OfflinePlayer player) {
    ItemStack item = new ItemStack(Material.PLAYER_HEAD);
    SkullMeta meta = (SkullMeta) item.getItemMeta();
    if (meta != null && player != null) {
      PlayerProfile profile = Bukkit.createProfile(player.getUniqueId(), player.getName());
      Player onlinePlayer = player.getPlayer();
      if (onlinePlayer != null) {
        profile = onlinePlayer.getPlayerProfile();
      }
      meta.setPlayerProfile(profile);
    }
    item.setItemMeta(meta);
    return item;
  }

  public static ItemStack getRedBoots() {
    ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
    LeatherArmorMeta meta = (LeatherArmorMeta) boots.getItemMeta();
    if (meta != null) {
      meta.setColor(Color.RED);
      boots.setItemMeta(meta);
    }
    return boots;
  }
}
