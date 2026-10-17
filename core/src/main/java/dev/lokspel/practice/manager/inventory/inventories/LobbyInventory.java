package dev.lokspel.practice.manager.inventory.inventories;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.inventory.Inventory;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.inventory.inventoryitem.InvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.CosmeticsInvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.KitEditorInvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.PartyCreateInvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.PartyJoinInvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.QueueInvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.RankedInvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.RematchInvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.SettingsInvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.SetupInvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.SpectateModeInvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.StaffMode;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.StatisticsInvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.UnrankedInvItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class LobbyInventory extends Inventory {

  public LobbyInventory() {
    super(InventoryType.LOBBY);

    this.invItems.add(new KitEditorInvItem());
    this.invItems.add(new PartyCreateInvItem());
    this.invItems.add(new PartyJoinInvItem());
    if (PracticeConfig.settings().getQueue().getCombined().isEnabled()) {
      this.invItems.add(new QueueInvItem());
    } else {
      this.invItems.add(new RankedInvItem());
      this.invItems.add(new UnrankedInvItem());
    }
    this.invItems.add(new RematchInvItem());
    this.invItems.add(new SettingsInvItem());
    this.invItems.add(new CosmeticsInvItem());
    this.invItems.add(new SpectateModeInvItem());
    this.invItems.add(new StaffMode());
    this.invItems.add(new SetupInvItem());
    this.invItems.add(new StatisticsInvItem());
  }

  @Override
  protected void set(Player player) {
    PlayerInventory playerInventory = player.getInventory();

    boolean setupItemSet = false;

    for (InvItem invItem : invItems) {
      int slot = invItem.getSlot();
      if (slot == -1) {
        continue;
      }

      switch (invItem) {
        case SpectateModeInvItem spectateModeInvItem -> {
          if (!InventoryManager.SPECTATOR_MODE_ENABLED) {
            continue;
          }
        }
        case SetupInvItem setupInvItem -> {
          if (!player.hasPermission("ap.setup")) {
            continue;
          }
          setupItemSet = true;
        }
        case StaffMode staffMode -> {
          if (!player.hasPermission("ap.staffmode")) {
            continue;
          }
          if (setupItemSet) {
            continue;
          }
        }
        case PartyJoinInvItem partyJoinInvItem -> {
          if (!player.hasPermission("ap.party.joinpublic")) {
            continue;
          }
        }
        case RematchInvItem rematchInvItem -> {
          continue;
        }
        case CosmeticsInvItem cosmeticsInvItem -> {
          if (!player.hasPermission("ap.cosmetics.main")) {
            continue;
          }
        }
        default -> {}
      }

      playerInventory.setItem(slot, invItem.getItem());
    }
  }

  public void addRematchItem(Player player, Player opponent) {
    InvItem invItem = getRematchInvItem();
    if (invItem == null) {
      return;
    }

    ItemStack item = RematchInvItem.createRematchItem(opponent);
    if (item == null) {
      item = invItem.getItem();
    }

    player.getInventory().setItem(invItem.getSlot(), item);
  }

  public void removeRematchItem(Player player) {
    InvItem invItem = getRematchInvItem();
    if (invItem == null) {
      return;
    }

    int slot = invItem.getSlot();
    if (slot < 0) {
      return;
    }

    ItemStack current = player.getInventory().getItem(slot);
    if (current == null || current.getType() == Material.AIR) {
      return;
    }

    // Only clear the slot if the current item is still the rematch item.
    if (RematchInvItem.isRematchItem(current) || current.isSimilar(invItem.getItem())) {
      player.getInventory().setItem(slot, null);
      player.updateInventory();
    }
  }

  private InvItem getRematchInvItem() {
    return this.getInvItem();
  }
}
