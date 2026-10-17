package dev.lokspel.practice.manager.inventory;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.inventory.InventoryLayout;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.inventory.inventoryitem.ExtraInvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.InvArmor;
import dev.lokspel.practice.manager.inventory.inventoryitem.InvItem;
import dev.lokspel.practice.manager.inventory.inventoryitem.lobbyitems.RematchInvItem;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public abstract class Inventory {

  public enum InventoryType {
    LOBBY(() -> PracticeConfig.inventories().getLobbyBasic()),
    MATCH_QUEUE(() -> PracticeConfig.inventories().getQueue().getMatch()),
    EVENT_QUEUE(() -> PracticeConfig.inventories().getQueue().getEvent()),
    PARTY(() -> PracticeConfig.inventories().getParty()),
    SPEC_MODE_LOBBY(() -> PracticeConfig.inventories().getSpectator().getLobby()),
    SPECTATE_MATCH(() -> PracticeConfig.inventories().getSpectator().getMatch()),
    SPECTATE_EVENT(() -> PracticeConfig.inventories().getSpectator().getEvent()),
    SPECTATE_FFA(() -> PracticeConfig.inventories().getSpectator().getFfa()),
    STAFF_MODE(() -> PracticeConfig.inventories().getStaffMode());

    private final Supplier<InventoryLayout> layout;

    InventoryType(final Supplier<InventoryLayout> layout) {
      this.layout = layout;
    }

    /** Returns this inventory's section of inventories.yml as currently loaded. */
    public InventoryLayout layout() {
      return layout.get();
    }
  }

  protected final InventoryType type;
  protected final List<Player> players;
  protected final List<InvItem> invItems; // Contains extra as well
  protected final InvArmor invArmor = new InvArmor();

  protected Inventory(InventoryType type) {
    this.type = type;
    this.players = new ArrayList<>();
    this.invItems = new ArrayList<>();

    this.getExtraItems();
  }

  protected abstract void set(Player player);

  public void setInventory(Player player) {
    Inventory currentInventory = InventoryManager.getInstance().getPlayerInventory(player);
    if (currentInventory != null) {
      currentInventory.getPlayers().remove(player);
    }

    PlayerUtil.clearArmor(player);
    PlayerUtil.clearMainInventory(player);

    players.add(player);

    this.set(player);
    this.setArmor(player);
    InventoryManager.getInstance().applyLobbyCosmetics(player);

    player.updateInventory();
  }

  public void setArmor(Player player) {
    if (invArmor.getHelmet() != null) {
      player.getInventory().setHelmet(invArmor.getHelmet());
    }
    if (invArmor.getChestplate() != null) {
      player.getInventory().setChestplate(invArmor.getChestplate());
    }
    if (invArmor.getLeggings() != null) {
      player.getInventory().setLeggings(invArmor.getLeggings());
    }
    if (invArmor.getBoots() != null) {
      player.getInventory().setBoots(invArmor.getBoots());
    }
  }

  public InvItem getHoldItem(ItemStack item, String name, Material material, int slot) {
    if (RematchInvItem.isRematchItem(item)) {
      InvItem rematch = this.getInvItem();
      if (rematch != null) {
        return rematch;
      }
    }

    InvItem invItem = this.getInvItem(name, material);

    if (invItem == null && slot != -1) {
      invItem = this.getInvItem(slot, material);
    }

    return invItem;
  }

  public InvItem getHoldItem(String name, Material material, int slot) {
    return getHoldItem(null, name, material, slot);
  }

  private void getExtraItems() {
    InventoryLayout layout = type.layout();
    if (layout == null || layout.getExtra() == null) {
      return;
    }

    for (InventoryLayout.ExtraItem extraItem : layout.getExtra().values()) {
      invItems.add(new ExtraInvItem(extraItem));
    }
  }

  protected InvItem getInvItem(final int slot, final Material material) {
    return invItems.stream()
        .filter(
            invItem ->
                invItem.getItem() != null
                    && invItem.getSlot() == slot
                    && invItem.getItem().getType().equals(material)
                    && invItem.getSlot() != -1)
        .findFirst()
        .orElse(null);
  }

  protected InvItem getInvItem(final String name, final Material material) {
    return invItems.stream()
        .filter(
            invItem ->
                invItem.getItem() != null
                    && Common.getItemDisplayName(invItem.getItem()).equalsIgnoreCase(name)
                    && invItem.getItem().getType().equals(material)
                    && invItem.getSlot() != -1)
        .findFirst()
        .orElse(null);
  }

  protected InvItem getInvItem() {
    return invItems.stream()
        .filter(
            invItem -> invItem.getClass().equals(RematchInvItem.class) && invItem.getSlot() != -1)
        .findFirst()
        .orElse(null);
  }
}
