package dev.lokspel.practice.util;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public final class PageUtil {

  private PageUtil() {}

  public static List<ItemStack> getPageItems(List<ItemStack> items, int page, int spaces) {
    if (items == null || page <= 0 || spaces <= 0) {
      return new ArrayList<>();
    }
    int lowerBound = (page - 1) * spaces;
    int upperBound = Math.min(page * spaces, items.size());
    if (lowerBound >= items.size()) {
      return new ArrayList<>();
    }

    return new ArrayList<>(items.subList(lowerBound, upperBound));
  }

  public static boolean isPageValid(List<ItemStack> items, int page, int spaces) {
    return items != null && isPageValid(items.size(), page, spaces);
  }

  public static boolean isPageValid(int size, int page, int spaces) {
    if (page <= 0 || spaces <= 0) {
      return false;
    }

    int lowerBound = (page - 1) * spaces;
    return size > lowerBound;
  }

  public static int getMaxPage(int size, int spaces) {
    if (size <= 0 || spaces <= 0) {
      return 1;
    }
    return Math.max(1, (int) Math.ceil((double) size / spaces));
  }

  public static int getMaxPage(List<ItemStack> icons, int spaces) {
    if (icons == null) {
      return 1;
    }
    return getMaxPage(icons.size(), spaces);
  }
}
