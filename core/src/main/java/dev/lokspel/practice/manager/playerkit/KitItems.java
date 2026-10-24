package dev.lokspel.practice.manager.playerkit;

import dev.lokspel.practice.manager.playerkit.items.KitItem;
import dev.lokspel.practice.util.kit.KitData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class KitItems {

  private final KitData kitData;

  private final KitItem helmet;
  private final KitItem chest;
  private final KitItem legs;
  private final KitItem boots;
  private final KitItem offhand;
  private final Map<Integer, KitItem> hotbar = new HashMap<>();
  private final Map<Integer, KitItem> inventory = new HashMap<>();

  private final Map<Integer, KitItem> slots = new HashMap<>();

  public KitItems(final KitData kitData) {
    this.kitData = kitData;

    boolean hasArmor = kitData.getArmor() != null;
    helmet =
        new KitItem(
            StaticItems.mainGuiHeadPlaceholderIcon(), hasArmor ? kitData.getArmor()[3] : null);
    chest =
        new KitItem(
            StaticItems.mainGuiChestPlaceholderIcon(), hasArmor ? kitData.getArmor()[2] : null);
    legs =
        new KitItem(
            StaticItems.mainGuiLegsPlaceholderIcon(), hasArmor ? kitData.getArmor()[1] : null);
    boots =
        new KitItem(
            StaticItems.mainGuiBootsPlaceholderIcon(), hasArmor ? kitData.getArmor()[0] : null);

    offhand =
        new KitItem(
            StaticItems.mainGuiOffHandPlaceholderIcon(),
            kitData.getExtra() != null ? kitData.getExtra()[0] : null);

    ItemStack[] storage = kitData.getStorage();
    for (int i = 0; i < 9; i++) {
      hotbar.put(
          i,
          new KitItem(
              StaticItems.mainGuiHotbarPlaceholderIcon(), storage != null ? storage[i] : null));
    }
    for (int i = 9; i < 36; i++) {
      inventory.put(
          i - 9,
          new KitItem(
              StaticItems.mainGuiInventoryPlaceholderIcon(), storage != null ? storage[i] : null));
    }

    this.assignSlots();
    this.resetNull();
  }

  private void assignSlots() {
    slots.put(10, helmet);
    slots.put(11, chest);
    slots.put(12, legs);
    slots.put(13, boots);
    slots.put(15, offhand);

    for (int i = 18; i <= 44; i++) {
      slots.put(i, inventory.get(i - 18));
    }

    for (int i = 45; i <= 53; i++) {
      slots.put(i, hotbar.get(i - 45));
    }
  }

  public List<ItemStack> getInventoryContent() {
    List<ItemStack> inventoryContent = new ArrayList<>(hotbar.size() + inventory.size());
    for (KitItem kitItem : hotbar.values()) {
      inventoryContent.add(kitItem.get());
    }
    for (KitItem kitItem : inventory.values()) {
      inventoryContent.add(kitItem.get());
    }
    return inventoryContent;
  }

  public List<ItemStack> getArmorContent() {
    return List.of(boots.get(), legs.get(), chest.get(), helmet.get());
  }

  public void reset() {
    slots.values().forEach(KitItem::reset);
  }

  public void resetNull() {
    for (KitItem kitItem : slots.values()) {
      if (kitItem.isNull()) {
        kitItem.reset();
      }
    }
  }

  public void save() {
    this.kitData.setExtra(new ItemStack[] {offhand.get()});
    this.kitData.setStorage(this.getInventoryContent().toArray(ItemStack[]::new));
    this.kitData.setArmor(this.getArmorContent().toArray(ItemStack[]::new));
  }
}
