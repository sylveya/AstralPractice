package dev.lokspel.practice.manager.gui;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.ladder.util.LadderUtil;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.StringUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiItem {

  @Getter @Setter private String name;
  @Getter private Material material;
  @Getter private int damage = -1;
  @Getter private int amount = 1;
  @Getter private List<String> lore = new ArrayList<>();
  @Getter private boolean glowing = false;
  @Getter private boolean unbreakable = false;
  private final List<ItemFlag> itemFlags = new ArrayList<>();
  private final Map<Enchantment, Integer> enchantments = new HashMap<>();
  private int durability = -1;
  @Getter private ItemStack baseItemStack = null;

  public GuiItem() {}

  public GuiItem(Material material) {
    this.material = material;
  }

  public GuiItem(String name, Material material) {
    this.name = name;
    this.material = material;
  }

  public GuiItem(String name, Material material, int damage) {
    this.name = name;
    this.material = material;
    this.damage = damage;
  }

  public GuiItem(String name, Material material, List<String> lore) {
    this.name = name;
    this.material = material;
    this.lore = lore;
  }

  public GuiItem(String name, Material material, int damage, List<String> lore) {
    this.name = name;
    this.material = material;
    this.damage = damage;
    this.lore = lore;
  }

  public GuiItem(String name, Material material, int damage, int amount) {
    this.name = name;
    this.material = material;
    this.damage = damage;
    this.amount = amount;
  }

  public GuiItem(String name, Material material, int damage, int amount, List<String> lore) {
    this.name = name;
    this.material = material;
    this.damage = damage;
    this.amount = amount;
    this.lore = lore;
  }

  public GuiItem(ItemStack itemStack) {
    if (itemStack == null) {
      return;
    }

    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta != null) {
      Component displayName = itemMeta.displayName();
      if (displayName != null) {
        this.name = Common.serializeComponentToLegacyString(displayName);
      }
      List<Component> itemLore = itemMeta.lore();
      if (itemLore != null) {
        this.lore =
            itemLore.stream()
                .map(Common::serializeComponentToLegacyString)
                .collect(Collectors.toList());
      }
      this.glowing = itemMeta.hasEnchants();
      if (itemMeta instanceof Damageable damageable) {
        this.damage = damageable.getDamage();
      }
    }
    this.amount = itemStack.getAmount();
    this.material = itemStack.getType();
    // Preserve the full ItemStack so special meta (e.g. PotionMeta) is not lost
    this.baseItemStack = itemStack.clone();
  }

  public GuiItem setGlowing(boolean glowing) {
    this.glowing = glowing;
    this.addItemFlag(ItemFlag.HIDE_ENCHANTS);
    return this;
  }

  public GuiItem setUnbreakable(boolean unbreakable) {
    this.unbreakable = unbreakable;
    return this;
  }

  /**
   * Parses a raw name/lore string into a {@link net.kyori.adventure.text.Component}, supporting all
   * color formats: legacy {@code <red>}, hex {@code &#RRGGBB}, Bungeecord hex {@code
   * &x<reset><reset>&G&G<aqua><aqua>}, and MiniMessage tags {@code <red>}.
   */
  private static Component parseColor(String raw) {
    if (raw == null || raw.isEmpty()) {
      return Component.empty();
    }
    // Explicitly mark italic as false so Minecraft's default item-name italic doesn't apply.
    // Users can still opt back in by writing <italic> in their config.
    return AstralPractice.getMiniMessage()
        .deserialize(StringUtil.legacyToMiniMessage(raw))
        .decorationIfAbsent(
            net.kyori.adventure.text.format.TextDecoration.ITALIC,
            net.kyori.adventure.text.format.TextDecoration.State.FALSE);
  }

  public ItemStack get() {
    ItemStack itemStack;

    if (baseItemStack != null) {
      // Clone the base item to preserve special meta (e.g. PotionMeta for modern Minecraft)
      itemStack = baseItemStack.clone();
      itemStack.setAmount(amount);
      ItemCreateUtil.hideItemFlags(itemStack);
    } else {
      if (material == null) {
        return null;
      }

      if (damage == -1 && amount == 1) {
        itemStack = new ItemStack(material);
      } else if (damage == -1) {
        itemStack = new ItemStack(material, amount);
      } else {
        itemStack = new ItemStack(material, amount);
      }

      if (durability > 0) {
        LadderUtil.setDurability(itemStack, durability);
      }
    }

    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta != null) {
      if (damage != -1 && itemMeta instanceof Damageable damageable) {
        damageable.setDamage(damage);
        itemMeta = damageable;
      }

      if (name != null) {
        itemMeta.displayName(parseColor(name));
      }

      if (lore != null) {
        itemMeta.lore(lore.stream().map(GuiItem::parseColor).collect(Collectors.toList()));
      }

      if (glowing && enchantments.isEmpty()) {
        itemMeta.addEnchant(Enchantment.UNBREAKING, 1, true);
      }

      // Apply unbreakable status if set (for modern versions to prevent durability bars)
      if (unbreakable) {
        itemMeta = LadderUtil.setUnbreakable(itemMeta, true);
      }

      if (!itemFlags.isEmpty()) {
        itemMeta.addItemFlags(itemFlags.toArray(new ItemFlag[0]));
      } else {
        ItemCreateUtil.hideItemFlags(itemMeta);
      }

      for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
        itemMeta.addEnchant(entry.getKey(), entry.getValue(), true);
      }

      itemStack.setItemMeta(itemMeta);
    }

    return itemStack;
  }

  public ItemStack getForPlayerKit() {
    if (material == null) {
      return null;
    }

    ItemStack itemStack;
    if (damage == -1 && amount == 1) {
      itemStack = new ItemStack(material);
    } else if (damage == -1) {
      itemStack = new ItemStack(material, amount);
    } else {
      itemStack = new ItemStack(material, amount);
    }

    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta != null) {
      if (damage != -1 && itemMeta instanceof Damageable damageable) {
        damageable.setDamage(damage);
        itemMeta = damageable;
      }

      if (name != null) {
        itemMeta.displayName(parseColor(name));
      }

      if (lore != null) {
        itemMeta.lore(lore.stream().map(GuiItem::parseColor).collect(Collectors.toList()));
      }

      itemStack.setItemMeta(itemMeta);
    }

    return itemStack;
  }

  public GuiItem replace(String regex, String replacement) {
    if (name != null) {
      this.setName(name.replace(regex, replacement));
    }

    if (lore != null) {
      List<String> replacementLore = new ArrayList<>();
      for (String line : lore) {
        replacementLore.add(line.replace(regex, replacement));
      }
      setLore(replacementLore);
    }

    return this;
  }

  public GuiItem setMaterial(Material material) {
    if (material == null) {
      return this;
    }

    this.material = material;
    return this;
  }

  public GuiItem setDamage(int damage) {
    if (damage == -1) {
      return this;
    }

    this.damage = damage;
    return this;
  }

  public GuiItem setAmount(int amount) {
    this.amount = amount;
    return this;
  }

  public GuiItem setLore(List<String> lore) {
    this.lore = lore;
    return this;
  }

  public void addItemFlag(ItemFlag itemFlag) {
    this.itemFlags.add(itemFlag);
  }

  public void addEnchantment(Enchantment enchantment, int level) {
    this.enchantments.put(enchantment, level);
  }

  public GuiItem setDurability(int durability) {
    this.durability = durability;
    return this;
  }

  public GuiItem setBaseItem(ItemStack itemStack) {
    if (itemStack == null || itemStack.getType() == Material.AIR) {
      return this;
    }
    this.baseItemStack = itemStack.clone();
    this.material = itemStack.getType();
    return this;
  }

  public GuiItem cloneItem() {
    GuiItem guiItem = new GuiItem();
    guiItem.setName(this.name);
    guiItem.setLore(this.lore);
    guiItem.setDamage(this.damage);
    guiItem.setMaterial(this.material);
    guiItem.setGlowing(this.glowing);
    guiItem.setUnbreakable(this.unbreakable);
    guiItem.setAmount(this.amount);
    guiItem.setDurability(this.durability);
    if (this.baseItemStack != null) {
      guiItem.baseItemStack = this.baseItemStack.clone();
    }
    guiItem.itemFlags.addAll(this.itemFlags);
    guiItem.enchantments.putAll(this.enchantments);
    return guiItem;
  }

  public GuiItem replaceMmToNormal() {
    this.name = Common.mmToNormal(this.name);

    List<String> lore = new ArrayList<>();
    for (String s : this.lore) {
      lore.add(Common.mmToNormal(s));
    }
    this.lore = lore;

    return this;
  }
}
