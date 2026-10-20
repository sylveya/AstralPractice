package dev.lokspel.practice.util.items;

import dev.lokspel.practice.util.Common;
import java.util.Base64;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ItemSerializationUtil {

  private ItemSerializationUtil() {}

  @Nullable
  public static String itemStackArrayToBase64(ItemStack[] items) {
    if (items == null) {
      return null;
    }

    try {
      return Base64.getEncoder().encodeToString(ItemStack.serializeItemsAsBytes(items));
    } catch (Exception e) {
      Common.sendConsoleMmMessage("<red>Error encoding base 64 itemstack.");
    }
    return null;
  }

  public static ItemStack[] itemStackArrayFromBase64(String data) {
    if (data == null) {
      return new ItemStack[0];
    }

    try {
      return ItemStack.deserializeItemsFromBytes(Base64.getMimeDecoder().decode(data));
    } catch (Exception e) {
      Common.sendConsoleMmMessage("<red>Error decoding base 64 itemstack.");
      return null;
    }
  }
}
