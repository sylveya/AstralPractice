package dev.lokspel.practice.manager.inventory;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendManager;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.inventory.inventories.EventQueueInventory;
import dev.lokspel.practice.manager.inventory.inventories.LobbyInventory;
import dev.lokspel.practice.manager.inventory.inventories.MatchQueueInventory;
import dev.lokspel.practice.manager.inventory.inventories.PartyInventory;
import dev.lokspel.practice.manager.inventory.inventories.StaffInventory;
import dev.lokspel.practice.manager.inventory.inventories.spectate.SpecEventInventory;
import dev.lokspel.practice.manager.inventory.inventories.spectate.SpecFfaInventory;
import dev.lokspel.practice.manager.inventory.inventories.spectate.SpecMatchInventory;
import dev.lokspel.practice.manager.inventory.inventories.spectate.SpecModeLobbyInventory;
import dev.lokspel.practice.manager.inventory.service.InventoryCosmeticService;
import dev.lokspel.practice.manager.inventory.service.InventoryTransitionService;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsData;
import dev.lokspel.practice.util.items.ItemSerializationUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class InventoryManager {

  private static InventoryManager instance;

  public static InventoryManager getInstance() {
    if (instance == null) {
      instance = new InventoryManager();
    }
    return instance;
  }

  public static final boolean SPECTATOR_MODE_ENABLED =
      PracticeConfig.settings().getSpectatorSettings().isSpectatorMode();
  public static final boolean SPECTATOR_MENU_ENABLED =
      PracticeConfig.settings().getSpectatorSettings().isSpectatorMenu();
  public static final double STAFF_SPECTATOR_SPEED =
      PracticeConfig.settings().getStaffMode().getStaffSpectatorSpeed();

  private final Map<Inventory.InventoryType, Inventory> inventories = new HashMap<>();
  private final List<Player> setupModePlayers = new ArrayList<>();
  private final NamespacedKey lobbyCosmeticItemKey =
      new NamespacedKey(AstralPractice.getInstance(), "ap-cosmetic-item");
  private final NamespacedKey lobbyCosmeticTypeKey =
      new NamespacedKey(AstralPractice.getInstance(), "ap-cosmetic-type");
  private final InventoryCosmeticService cosmeticService = new InventoryCosmeticService();
  private final InventoryTransitionService transitionService = new InventoryTransitionService(this);

  private InventoryManager() {
    Bukkit.getPluginManager().registerEvents(new InventoryListener(), AstralPractice.getInstance());
  }

  public void loadInventories() {
    this.inventories.putAll(createInventories());

    this.getData();
  }

  /**
   * Rebuilds every inventory from inventories.yml and moves the players and armor over. If an item
   * cannot be built, the current inventories stay in place.
   */
  public void reload() {
    Map<Inventory.InventoryType, Inventory> rebuilt = createInventories();
    this.inventories.forEach(
        (type, old) -> {
          Inventory inventory = rebuilt.get(type);
          if (inventory == null) {
            return;
          }
          inventory.getPlayers().addAll(old.getPlayers());
          inventory.getInvArmor().setArmorContent(old.getInvArmor().getArmorContent());
        });

    this.inventories.clear();
    this.inventories.putAll(rebuilt);
  }

  private static Map<Inventory.InventoryType, Inventory> createInventories() {
    Map<Inventory.InventoryType, Inventory> inventories = new HashMap<>();
    inventories.put(Inventory.InventoryType.LOBBY, new LobbyInventory());
    inventories.put(Inventory.InventoryType.PARTY, new PartyInventory());
    inventories.put(Inventory.InventoryType.MATCH_QUEUE, new MatchQueueInventory());
    inventories.put(Inventory.InventoryType.EVENT_QUEUE, new EventQueueInventory());
    inventories.put(Inventory.InventoryType.STAFF_MODE, new StaffInventory());
    inventories.put(Inventory.InventoryType.SPECTATE_EVENT, new SpecEventInventory());
    inventories.put(Inventory.InventoryType.SPECTATE_FFA, new SpecFfaInventory());
    inventories.put(Inventory.InventoryType.SPECTATE_MATCH, new SpecMatchInventory());
    inventories.put(Inventory.InventoryType.SPEC_MODE_LOBBY, new SpecModeLobbyInventory());

    return inventories;
  }

  public Inventory getPlayerInventory(Player player) {
    for (Inventory inventory : this.inventories.values()) {
      if (inventory.getPlayers().contains(player)) {
        return inventory;
      }
    }
    return null;
  }

  public void setInventory(Player player, Inventory.InventoryType inventoryType) {
    if (inventoryType == null) {
      Inventory playerInv = this.getPlayerInventory(player);
      if (playerInv != null) {
        playerInv.getPlayers().remove(player);
      }
      PlayerUtil.clearInventory(player);
      return;
    }

    Inventory inventory = this.inventories.get(inventoryType);
    if (inventory != null) {
      inventory.setInventory(player);
    }
  }

  public void setLobbyInventory(Player player, boolean teleport) {
    transitionService.setLobbyInventory(player, teleport);
  }

  public void setMatchQueueInventory(Player player) {
    setMatchQueueInventory(player, true);
  }

  public void setMatchQueueInventory(Player player, boolean closeInventory) {
    transitionService.setMatchQueueInventory(player, closeInventory);
  }

  public void applyLobbyCosmetics(Player player) {
    cosmeticService.applyLobbyCosmetics(player);
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean shouldRenderLobbyCosmetics(Profile profile) {
    return cosmeticService.shouldRenderLobbyCosmetics(profile);
  }

  public boolean isLobbyCosmeticItem(org.bukkit.inventory.ItemStack itemStack) {
    return cosmeticService.isLobbyCosmeticItem(itemStack);
  }

  public CosmeticsData.LobbyItemType getLobbyCosmeticType(
      org.bukkit.inventory.ItemStack itemStack) {
    return cosmeticService.getLobbyCosmeticType(itemStack);
  }

  public boolean isOffhandElytraCosmetic(CosmeticsData.LobbyItemType type) {
    return cosmeticService.isOffhandElytraCosmetic(type);
  }

  public void setEventQueueInventory(Player player) {
    transitionService.setEventQueueInventory(player);
  }

  public void setStaffModeInventory(Player player) {
    transitionService.setStaffModeInventory(player);
  }

  public void setData() {
    Map<String, String> invArmors = BackendManager.data().getInvArmors();
    if (invArmors == null) {
      invArmors = new LinkedHashMap<>();
      BackendManager.data().setInvArmors(invArmors);
    }

    for (Inventory inventory : this.inventories.values()) {
      String key = inventory.getType().name().toUpperCase();
      if (inventory.getInvArmor().isNull()) {
        invArmors.remove(key);
      } else {
        invArmors.put(
            key,
            ItemSerializationUtil.itemStackArrayToBase64(
                inventory.getInvArmor().getArmorContent()));
      }
    }

    BackendManager.save();
  }

  public void getData() {
    Map<String, String> invArmors = BackendManager.data().getInvArmors();
    if (invArmors == null) {
      return;
    }

    for (Map.Entry<String, String> entry : invArmors.entrySet()) {
      try {
        Inventory inventory = this.inventories.get(Inventory.InventoryType.valueOf(entry.getKey()));
        if (inventory == null) {
          continue;
        }

        ItemStack[] items = ItemSerializationUtil.itemStackArrayFromBase64(entry.getValue());
        if (items != null) {
          inventory.getInvArmor().setArmorContent(items);
        }
      } catch (IllegalArgumentException ignored) {
        // Skip unknown inventory types in old data
      }
    }
  }
}
