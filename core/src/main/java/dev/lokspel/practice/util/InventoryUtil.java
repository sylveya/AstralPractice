package dev.lokspel.practice.util;

import dev.lokspel.practice.AstralPractice;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class InventoryUtil {

  private InventoryUtil() {}

  public static Inventory createInventory(String title, int row) {
    Component component =
        AstralPractice.getMiniMessage().deserialize(StringUtil.legacyToMiniMessage(title));
    return Bukkit.getServer().createInventory(null, row * 9, component);
  }

  /** Clones the array and each non-null item, preserving null arrays and slots. */
  public static ItemStack[] cloneItems(ItemStack[] source) {
    if (source == null) {
      return null;
    }

    ItemStack[] copy = source.clone();
    for (int i = 0; i < copy.length; i++) {
      if (copy[i] != null) {
        copy[i] = copy[i].clone();
      }
    }
    return copy;
  }

  /**
   * Calculates the starting slot index to center {@code itemCount} items horizontally and
   * vertically within an inventory of size {@code inventorySize}.
   *
   * @param inventorySize the total number of slots in the inventory (e.g. 27, 54)
   * @param itemCount the number of items to display
   * @return the starting slot index, or 0 if inputs are invalid
   */
  public static int getCenteredStartSlot(int inventorySize, int itemCount) {
    if (inventorySize <= 0 || itemCount <= 0) {
      return 0;
    }

    int rows = inventorySize / 9;
    int itemRows = (int) Math.ceil((double) Math.min(itemCount, inventorySize) / 9.0);
    int startRow = Math.max(0, (rows - itemRows) / 2);
    int itemsInFirstRow = Math.min(itemCount, 9);
    int startCol = Math.max(0, (9 - itemsInFirstRow) / 2);

    return startRow * 9 + startCol;
  }
}
