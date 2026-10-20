package dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.ItemConfig;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.inventory.InventoryLayout;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.util.RematchRequest;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.InvItem;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class RematchInvItem extends InvItem {

  public static final NamespacedKey REMATCH_KEY =
      new NamespacedKey(AstralPractice.getInstance(), "rematch_item");

  public RematchInvItem() {
    super(PracticeConfig.inventories().getLobbyBasic().getNormal().get("REMATCH"));
  }

  public static boolean isRematchItem(ItemStack item) {
    if (item == null || !item.hasItemMeta()) {
      return false;
    }
    ItemMeta meta = item.getItemMeta();
    return meta != null
        && meta.getPersistentDataContainer().has(REMATCH_KEY, PersistentDataType.BYTE);
  }

  public static ItemStack createRematchItem(Player opponent) {
    InventoryLayout.InventoryItem config =
        PracticeConfig.inventories().getLobbyBasic().getNormal().get("REMATCH");
    if (config == null || config.getItem() == null) {
      return null;
    }

    ItemConfig itemConfig = config.getItem();
    String opponentName = opponent != null ? opponent.getName() : "Unknown";

    GuiItem guiItem;
    if (config.isOpponentHead() && opponent != null) {
      guiItem = new GuiItem(ItemCreateUtil.getPlayerHead(opponent));
      guiItem.setName(itemConfig.getName());
      guiItem.setLore(itemConfig.getLore());
    } else {
      guiItem = BackendUtil.getGuiItem(itemConfig).cloneItem();
    }

    ItemStack result = guiItem.replace("%opponent%", opponentName).get();
    if (result != null && result.hasItemMeta()) {
      ItemMeta meta = result.getItemMeta();
      if (meta != null) {
        meta.getPersistentDataContainer().set(REMATCH_KEY, PersistentDataType.BYTE, (byte) 1);
        result.setItemMeta(meta);
      }
    }

    return result;
  }

  @Override
  public void handleClickEvent(Player player) {
    RematchRequest rematchRequest = MatchManager.getInstance().getRematchRequest(player);

    if (rematchRequest != null) {
      rematchRequest.sendRematchRequest(player);
    }
  }
}
