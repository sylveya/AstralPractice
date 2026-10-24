package dev.lokspel.practice.manager.gui;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.guis.customladder.premadecustom.CustomLadderEditorGui;
import dev.lokspel.practice.manager.gui.setup.SetupHubGui;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.CustomKitGui;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.DestroyableBlocksGui;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.InventoryGui;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.SkyWarsLootGui;
import dev.lokspel.practice.manager.gui.setup.server.ServerArmorLobbyGui;
import dev.lokspel.practice.manager.server.sound.SoundEffect;
import dev.lokspel.practice.manager.server.sound.SoundManager;
import dev.lokspel.practice.manager.server.sound.SoundType;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GuiManager implements Listener {

  private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER =
      PlainTextComponentSerializer.plainText();

  private enum GuiClickSoundIntent {
    NONE,
    ADVANCE,
    BACK_OR_CLOSE
  }

  private static final List<String> BACK_OR_CLOSE_KEYWORDS =
      List.of("back", "close", "cancel", "return", "exit", "previous");

  private static GuiManager instance;

  public static GuiManager getInstance() {
    if (instance == null) {
      instance = new GuiManager();
    }
    return instance;
  }

  @Getter private final Map<GuiType, Gui> guis = new HashMap<>();
  @Getter private final Map<Player, Gui> openGui = new HashMap<>();

  public static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(PracticeConfig.guis().getGeneralFillerItem()).get();
  }

  @Getter
  private static final ItemStack DUMMY_ITEM =
      ItemCreateUtil.createItem("DUMMY", Material.GLOWSTONE_DUST);

  private GuiManager() {
    Bukkit.getPluginManager().registerEvents(this, AstralPractice.getInstance());

    this.addGui(new SetupHubGui());
  }

  public Gui searchGui(GuiType type) {
    return guis.get(type);
  }

  public Gui addGui(Gui gui) {
    guis.put(gui.getType(), gui);
    return gui;
  }

  /**
   * Prepares the GUIs for reloaded configuration files: closes every open GUI, drops the cached
   * pages and re-creates the GUIs this manager owns. The managers owning further GUIs re-create
   * theirs afterwards.
   */
  public void reload() {
    closeOpenGuis();
    GuiCache.clearAll();

    addGui(new SetupHubGui());
  }

  /** Closes the GUI of every player who has one open, so no inventory outlives its GUI. */
  private void closeOpenGuis() {
    new HashMap<>(openGui)
        .forEach(
            (player, gui) -> {
              player.closeInventory();
              gui.close(player);
            });
  }

  @EventHandler
  public void onInventoryClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player player)) {
      return;
    }

    Gui gui = openGui.get(player);
    if (gui == null) {
      return;
    }

    if (gui instanceof CustomLadderEditorGui && e.getRawSlot() < 0) {
      e.setCancelled(true);
      player.setItemOnCursor(null);
      return;
    }

    if (shouldHardCancelClick(e, gui)) {
      e.setCancelled(true);
    }

    if (!allowsInventoryMutation(gui)) {
      // Safe default for all normal GUIs: no item movement, only GUI button handling.
      e.setCancelled(true);
    }

    GuiClickSoundIntent soundIntent = resolveClickSoundIntent(e, gui, player);

    if (gui.getInConfirmationGui().containsKey(player)) {
      gui.handleConfirmGuiClick(e);
    } else {
      gui.handleClickEvent(e);
    }

    playGuiClickSound(player, soundIntent);
  }

  private boolean shouldHardCancelClick(InventoryClickEvent e, Gui gui) {
    int topSize = e.getView().getTopInventory().getSize();
    int rawSlot = e.getRawSlot();

    if (rawSlot < 0) {
      return true;
    }

    boolean clickedBottomInventory = rawSlot >= topSize;
    if (clickedBottomInventory && !allowsInventoryMutation(gui)) {
      return true;
    }

    ClickType clickType = e.getClick();
    InventoryAction action = e.getAction();

    if (isHotbarSwap(clickType, action)) {
      return !(clickedBottomInventory && allowsInventoryMutation(gui));
    }

    if (clickType == ClickType.DOUBLE_CLICK
        || clickType == ClickType.CONTROL_DROP
        || clickType == ClickType.CREATIVE
        || clickType == ClickType.MIDDLE) {
      return true;
    }

    return action == InventoryAction.COLLECT_TO_CURSOR
        || action == InventoryAction.MOVE_TO_OTHER_INVENTORY;
  }

  /** Number-key and F-key swaps between a slot and the hotbar/offhand. */
  public static boolean isHotbarSwap(ClickType clickType, InventoryAction action) {
    return clickType == ClickType.NUMBER_KEY
        || clickType == ClickType.SWAP_OFFHAND
        || action == InventoryAction.HOTBAR_SWAP;
  }

  private boolean allowsInventoryMutation(Gui gui) {
    return gui instanceof CustomLadderEditorGui
        || gui instanceof DestroyableBlocksGui
        || gui instanceof InventoryGui
        || gui instanceof CustomKitGui
        || gui instanceof SkyWarsLootGui
        || gui instanceof ServerArmorLobbyGui;
  }

  private GuiClickSoundIntent resolveClickSoundIntent(
      InventoryClickEvent e, Gui gui, Player player) {
    Inventory topInventory = e.getView().getTopInventory();
    int rawSlot = e.getRawSlot();

    if (rawSlot < 0 || rawSlot >= topInventory.getSize()) {
      return GuiClickSoundIntent.NONE;
    }

    if (gui.getInConfirmationGui().containsKey(player)) {
      return rawSlot == 4 ? GuiClickSoundIntent.ADVANCE : GuiClickSoundIntent.BACK_OR_CLOSE;
    }

    ItemStack clickedItem = e.getCurrentItem();
    if (clickedItem == null || clickedItem.getType() == Material.AIR) {
      return GuiClickSoundIntent.NONE;
    }

    return isBackOrCloseItem(clickedItem)
        ? GuiClickSoundIntent.BACK_OR_CLOSE
        : GuiClickSoundIntent.ADVANCE;
  }

  private boolean isBackOrCloseItem(ItemStack itemStack) {
    ItemMeta meta = itemStack.getItemMeta();
    if (meta == null) {
      return false;
    }

    Component displayName = meta.displayName();
    if (displayName != null
        && containsBackOrCloseKeyword(PLAIN_TEXT_SERIALIZER.serialize(displayName))) {
      return true;
    }

    List<Component> lore = meta.lore();
    return lore != null
        && lore.stream()
            .anyMatch(line -> containsBackOrCloseKeyword(PLAIN_TEXT_SERIALIZER.serialize(line)));
  }

  private boolean containsBackOrCloseKeyword(String text) {
    if (text == null || text.isEmpty()) {
      return false;
    }

    String normalized = Common.stripLegacyColor(text).toLowerCase(Locale.ROOT);
    return BACK_OR_CLOSE_KEYWORDS.stream().anyMatch(normalized::contains);
  }

  private void playGuiClickSound(Player player, GuiClickSoundIntent intent) {
    if (intent == GuiClickSoundIntent.NONE) {
      return;
    }

    SoundType soundType =
        intent == GuiClickSoundIntent.BACK_OR_CLOSE
            ? SoundType.GUI_CLICK_BACK
            : SoundType.GUI_CLICK_ADVANCE;

    SoundEffect soundEffect = SoundManager.getInstance().getSound(soundType);
    if (soundEffect != null) {
      soundEffect.play(player);
    }
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent e) {
    if (!(e.getPlayer() instanceof Player player)) {
      return;
    }

    Gui gui = this.openGui.get(player);
    if (gui == null) {
      return;
    }

    if (shouldPlayManualCloseSound(e)) {
      playGuiClickSound(player, GuiClickSoundIntent.BACK_OR_CLOSE);
    }

    gui.handleCloseEvent(e);

    // Closing the gui for the player.
    gui.close(player);
  }

  private boolean shouldPlayManualCloseSound(InventoryCloseEvent e) {
    InventoryCloseEvent.Reason reason = e.getReason();
    return reason == InventoryCloseEvent.Reason.PLAYER
        || reason == InventoryCloseEvent.Reason.UNKNOWN;
  }

  @EventHandler
  public void onInventoryDrag(InventoryDragEvent e) {
    if (!(e.getWhoClicked() instanceof Player player)) {
      return;
    }

    Gui gui = openGui.get(player);
    if (gui == null) {
      return;
    }

    if (!allowsInventoryMutation(gui)) {
      e.setCancelled(true);
      return;
    }

    gui.handleDragEvent(e);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onLadderEditorItemDrop(PlayerDropItemEvent e) {
    Player player = e.getPlayer();
    Gui gui = openGui.get(player);

    if (gui instanceof CustomLadderEditorGui) {
      e.getItemDrop().remove();
      player.setItemOnCursor(null);
    }
  }
}
