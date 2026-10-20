package dev.lokspel.practice.manager.gui.guis.cosmetics;

import dev.lokspel.practice.config.ItemConfig;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsData;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LobbyItemsGui extends Gui {

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(PracticeConfig.guis().getGeneralFillerItem()).get();
  }

  private static ItemStack backItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getCosmetics().getIcons().getBackTo())
        .get();
  }

  private static final int ROWS = 3;
  private static final int BACK_SLOT = 18;

  private final Profile profile;
  private final Gui backToGui;

  public LobbyItemsGui(Profile profile, Gui backToGui) {
    super(GuiType.Cosmetics_LobbyItems);
    this.profile = profile;
    this.backToGui = backToGui;

    String title = PracticeConfig.guis().getGuis().getCosmetics().getLobbyItems().getTitle();
    this.gui.put(1, InventoryUtil.createInventory(title, ROWS));
    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    Inventory inv = gui.get(1);
    inv.clear();

    for (int i = 0; i < inv.getSize(); i++) {
      inv.setItem(i, fillerItem());
    }

    CosmeticsData.LobbyItemType selected = profile.getCosmeticsData().getLobbyItemType();
    CosmeticsData.LobbyItemType[] values = CosmeticsData.LobbyItemType.values();
    Player online = profile.getPlayer().getPlayer();
    int[] optionSlots = getOptionSlots(values.length);

    for (int i = 0; i < optionSlots.length && i < values.length; i++) {
      CosmeticsData.LobbyItemType type = values[i];
      inv.setItem(optionSlots[i], buildOptionItem(type, selected, online));
    }

    inv.setItem(BACK_SLOT, backItem() == null ? new ItemStack(Material.ARROW) : backItem());
    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    e.setCancelled(true);

    Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();

    if (slot == BACK_SLOT) {
      backToGui.update(true);
      backToGui.open(player);
      return;
    }

    CosmeticsData.LobbyItemType clicked = getTypeBySlot(slot);
    if (clicked == null) {
      return;
    }

    if (!CosmeticsPermissionManager.hasLobbyItemPermission(player, clicked)) {
      String noPerm =
          PracticeConfig.guis().getGuis().getCosmetics().getLobbyItems().getNoPermissionMessage();
      Common.sendMmMessage(player, noPerm);
      return;
    }

    CosmeticsData.LobbyItemType current = profile.getCosmeticsData().getLobbyItemType();
    profile
        .getCosmeticsData()
        .setLobbyItemType(current == clicked ? CosmeticsData.LobbyItemType.NONE : clicked);
    profile.save();
    InventoryManager.getInstance().applyLobbyCosmetics(player);

    update(true);
  }

  private CosmeticsData.LobbyItemType getTypeBySlot(int slot) {
    CosmeticsData.LobbyItemType[] values = CosmeticsData.LobbyItemType.values();
    int[] optionSlots = getOptionSlots(values.length);
    for (int i = 0; i < optionSlots.length && i < values.length; i++) {
      if (optionSlots[i] == slot) {
        return values[i];
      }
    }
    return null;
  }

  private static int[] getOptionSlots(int count) {
    return switch (count) {
      case 1 -> new int[] {13};
      case 2 -> new int[] {12, 14};
      case 3 -> new int[] {11, 13, 15};
      case 4 -> new int[] {10, 12, 14, 16};
      case 5 -> new int[] {9, 11, 13, 15, 17};
      default -> new int[] {9, 11, 13, 15, 17};
    };
  }

  private ItemStack buildOptionItem(
      CosmeticsData.LobbyItemType type, CosmeticsData.LobbyItemType selected, Player player) {
    boolean unlocked =
        player != null && CosmeticsPermissionManager.hasLobbyItemPermission(player, type);
    boolean active = type == selected;

    Map<String, ItemConfig> entries =
        PracticeConfig.guis().getGuis().getCosmetics().getLobbyItems().getEntries();
    ItemConfig entry = entries != null ? entries.get(type.name()) : null;
    if (entry == null && entries != null && type == CosmeticsData.LobbyItemType.ELYTRA) {
      entry = entries.get("FIREWORK");
    }
    final String name =
        entry != null && entry.getName() != null ? entry.getName() : "<yellow>" + formatName(type);
    final Material material =
        safeMaterial(entry != null ? entry.getMaterial() : null, defaultMaterial(type));

    List<String> lore = entry != null ? entry.getLore() : null;
    if (lore == null || lore.isEmpty()) {
      lore =
          new ArrayList<>(
              List.of("", "<gray>Status: %status%", "<gray>Required: <white>%permission%", ""));
    } else {
      lore = new ArrayList<>(lore);
    }

    String status = active ? "<green>Selected" : (unlocked ? "<yellow>Unlocked" : "<red>Locked");
    String permission =
        type == CosmeticsData.LobbyItemType.NONE ? "none" : type.getPermissionNode();
    lore.replaceAll(
        line ->
            line.replace("%status%", status)
                .replace("%permission%", permission)
                .replace("%type%", formatName(type)));

    if (active) {
      lore.add("<gray>Click to disable.");
    } else if (unlocked) {
      lore.add("<yellow>Click to select.");
    } else {
      lore.add("<red>You do not have permission.");
    }

    GuiItem item = new GuiItem(name, material, lore);
    item.setGlowing(active);

    return ItemCreateUtil.hideItemFlags(item.get());
  }

  private Material defaultMaterial(CosmeticsData.LobbyItemType type) {
    return switch (type) {
      case NONE -> Material.BARRIER;
      case WIND_CHARGE -> safeMaterial("WIND_CHARGE", Material.FIRE_CHARGE);
      case TRIDENT -> safeMaterial("TRIDENT", Material.IRON_SWORD);
      case SPEAR -> safeMaterial("SPEAR", safeMaterial("TRIDENT", Material.IRON_SWORD));
      case ELYTRA -> safeMaterial("ELYTRA", Material.FEATHER);
    };
  }

  private Material safeMaterial(String raw, Material fallback) {
    if (raw == null || raw.isBlank()) {
      return fallback;
    }

    Material material = Material.matchMaterial(raw.toUpperCase(Locale.ROOT));
    return material == null ? fallback : material;
  }

  private String formatName(CosmeticsData.LobbyItemType type) {
    String text = type.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    return Character.toUpperCase(text.charAt(0)) + text.substring(1);
  }
}
