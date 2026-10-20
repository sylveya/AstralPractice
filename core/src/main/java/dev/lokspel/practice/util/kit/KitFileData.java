package dev.lokspel.practice.util.kit;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.util.items.ItemSerializationUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * A {@link KitData} as data files store it: the inventory, armor and extra contents as Base64
 * strings and the potion effects as Bukkit objects. Events ({@code kit}, {@code juggernaut-kit},
 * {@code player-kit}) and player custom ladders ({@code kit-data}) use this section.
 */
@Getter
@Setter
public class KitFileData {

  @JsonProperty("inventory")
  private String inventory;

  @JsonProperty("armor")
  private String armor;

  @JsonProperty("extra")
  private String extra;

  @JsonProperty("effects")
  private List<PotionEffect> effects;

  /**
   * Builds the section of a kit.
   *
   * @param kitData the kit to write
   * @return the section, or {@code null} if the kit holds nothing to write
   */
  public static KitFileData of(KitData kitData) {
    KitFileData data = new KitFileData();
    data.update(kitData);

    boolean empty =
        data.inventory == null && data.armor == null && data.extra == null && data.effects == null;
    return empty ? null : data;
  }

  /**
   * Writes the parts of a kit that are set into this section; parts that are not set keep what the
   * section held.
   *
   * @param kitData the kit to write
   */
  public void update(KitData kitData) {
    if (kitData.getStorage() != null) {
      inventory = ItemSerializationUtil.itemStackArrayToBase64(kitData.getStorage());
    }
    if (kitData.getArmor() != null) {
      armor = ItemSerializationUtil.itemStackArrayToBase64(kitData.getArmor());
    }
    if (kitData.getExtra() != null) {
      extra = ItemSerializationUtil.itemStackArrayToBase64(kitData.getExtra());
    }
    if (!kitData.getEffects().isEmpty()) {
      effects = new ArrayList<>(kitData.getEffects());
    }
  }

  /**
   * Copies this section into a kit; parts that are absent leave the kit's value as it is.
   *
   * @param kitData the kit to fill
   */
  public void applyTo(KitData kitData) {
    ItemStack[] storage = fromBase64(inventory);
    if (storage != null) {
      kitData.setStorage(storage);
    }
    ItemStack[] armorContents = fromBase64(armor);
    if (armorContents != null) {
      kitData.setArmor(armorContents);
    }
    ItemStack[] extraContents = fromBase64(extra);
    if (extraContents != null) {
      kitData.setExtra(extraContents);
    }
    if (effects != null) {
      List<PotionEffect> loaded = new ArrayList<>();
      for (PotionEffect effect : effects) {
        if (effect != null) {
          loaded.add(effect);
        }
      }
      kitData.setEffects(loaded);
    }
  }

  private static ItemStack[] fromBase64(String data) {
    return data != null ? ItemSerializationUtil.itemStackArrayFromBase64(data) : null;
  }
}
