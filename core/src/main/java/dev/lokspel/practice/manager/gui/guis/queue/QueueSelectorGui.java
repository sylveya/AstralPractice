package dev.lokspel.practice.manager.gui.guis.queue;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.ItemConfig;
import dev.lokspel.practice.config.gui.QueueGui;
import dev.lokspel.practice.config.settings.QueueSettings;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.enums.MatchType;
import dev.lokspel.practice.manager.fight.match.enums.WeightClass;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.queue.Queue;
import dev.lokspel.practice.manager.queue.QueueManager;
import dev.lokspel.practice.util.InventoryUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

public abstract class QueueSelectorGui extends Gui {

  /** Interval between real-time count updates, in ticks (2 seconds). */
  private static final long TICK_INTERVAL = 40L;

  private static final int DEFAULT_QUICK_MATCH_SLOT = 45;

  private final long updateCooldownMillis =
      this.getUpdateCooldownMinutes() < 0 ? 0 : this.getUpdateCooldownMinutes() * 60 * 1000L;

  // page -> slot -> ladder mapping, used for click-handling and live updates
  private final Map<Integer, Map<Integer, NormalLadder>> pageLadderSlots = new HashMap<>();
  private final Map<Integer, Map<Integer, ItemStack>> templateItems = new HashMap<>();
  private final List<CategoryConfig> categoriesList = new ArrayList<>();
  private final Map<Integer, List<CategoryConfig>> slotToCategories = new LinkedHashMap<>();

  private long lastUpdateTime = -1L;
  private BukkitTask tickerTask = null;

  public QueueSelectorGui(GuiType type) {
    super(type);
    build();
  }

  // -------------------------------------------------------------------------
  // Abstract API
  // -------------------------------------------------------------------------

  protected abstract long getUpdateCooldownMinutes();

  /** The queue settings section ({@code QUEUE.RANKED} or {@code QUEUE.UNRANKED}). */
  protected abstract QueueSettings queueSettings();

  /** The GUI section ({@code GUIS.RANKED-GUI} or {@code GUIS.UNRANKED-GUI}). */
  protected abstract QueueGui gui();

  protected abstract WeightClass getWeightClass();

  protected abstract boolean isRanked();

  protected abstract boolean isValidLadder(NormalLadder ladder);

  protected abstract void onLadderClick(Player player, NormalLadder ladder);

  protected void decoratePage(int pageId, Inventory inventory) {}

  protected boolean handleCustomTopInventoryClick(
      Player player, int rawSlot, InventoryView inventoryView, ItemStack item) {
    return false;
  }

  // -------------------------------------------------------------------------
  // Build / update lifecycle
  // -------------------------------------------------------------------------

  @Override
  public void build() {
    doUpdate();
  }

  @Override
  public void update() {
    update(false);
  }

  @Override
  public boolean update(boolean forceRefresh) {
    if (!Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(AstralPractice.getInstance(), () -> update(forceRefresh));
      return false;
    }

    long now = System.currentTimeMillis();
    if (!forceRefresh && lastUpdateTime >= 0 && (now - lastUpdateTime) < updateCooldownMillis) {
      return false;
    }

    doUpdate();
    return true;
  }

  private void doUpdate() {
    lastUpdateTime = System.currentTimeMillis();
    final QueueSettings.SelectorGui selectorGui = queueSettings().getSelectorGui();
    final QueueGui guiConfig = gui();

    final Map<Integer, Inventory> existingInventories = new HashMap<>(this.gui);

    this.gui.clear();
    this.pageLadderSlots.clear();
    this.templateItems.clear();
    this.categoriesList.clear();
    this.slotToCategories.clear();

    List<CategoryConfig> categories = loadCategories(selectorGui);
    if (categories.isEmpty()) {
      return;
    }
    this.categoriesList.addAll(categories);

    for (CategoryConfig category : categories) {
      slotToCategories
          .computeIfAbsent(category.selectorSlot(), _ -> new ArrayList<>())
          .add(category);
    }

    int rows = Math.clamp(Objects.requireNonNullElse(selectorGui.getSize(), 0), 1, 6);
    ItemStack filler = icon(guiConfig, QueueGui.Icons::getFillerItem).get();
    if (filler == null) {
      filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    }
    GuiItem ladderTemplate = icon(guiConfig, QueueGui.Icons::getLadder);

    for (int i = 0; i < categories.size(); i++) {
      int pageId = i + 1;
      setupCategoryPage(
          pageId,
          rows,
          filler,
          ladderTemplate,
          categories.get(i),
          categories,
          guiConfig,
          existingInventories);
    }

    updatePlayers();
  }

  // -------------------------------------------------------------------------
  // Real-time ticker — only runs while at least one player has the GUI open
  // -------------------------------------------------------------------------

  /**
   * Called when the first player opens the GUI; starts the async ticker that refreshes %in_queue% /
   * %in_fight% counts every {@value #TICK_INTERVAL} ticks.
   */
  private void startTicker() {
    if (tickerTask != null) {
      return;
    }

    tickerTask =
        Bukkit.getScheduler()
            .runTaskTimer(
                AstralPractice.getInstance(),
                () -> {
                  if (inGuiPlayers.isEmpty()) {
                    stopTicker();
                    return;
                  }
                  tickUpdate();
                },
                TICK_INTERVAL,
                TICK_INTERVAL);
  }

  private void stopTicker() {
    if (tickerTask != null) {
      tickerTask.cancel();
      tickerTask = null;
    }
  }

  /**
   * Lightweight async tick: updates only the dynamic data (queue/fight counts, item amount) for
   * every ladder slot already placed in the inventory. Does NOT rebuild the layout or re-read
   * config. The stored template already has static LB lore resolved — only %in_queue% and
   * %in_fight% are substituted here each tick.
   */
  private void tickUpdate() {
    for (Map.Entry<Integer, Map<Integer, ItemStack>> pageEntry : templateItems.entrySet()) {
      int pageId = pageEntry.getKey();
      Inventory inventory = gui.get(pageId);
      if (inventory == null) {
        continue;
      }
      Map<Integer, NormalLadder> slotMap = pageLadderSlots.get(pageId);
      if (slotMap == null) {
        continue;
      }

      for (Map.Entry<Integer, ItemStack> slotEntry : pageEntry.getValue().entrySet()) {
        int slot = slotEntry.getKey();
        ItemStack template = slotEntry.getValue();
        NormalLadder ladder = slotMap.get(slot);
        if (ladder == null || template == null) {
          continue;
        }

        int inQueue = QueueManager.getInstance().getQueueSize(ladder, isRanked());
        int inFight = MatchManager.getInstance().getDuelMatchSize(ladder, isRanked());

        ItemStack updated = template.clone();
        ItemMeta meta = updated.getItemMeta();
        if (meta == null) {
          continue;
        }

        // Replace dynamic placeholders in display name — use Adventure text replacement
        // so hex colors and italic=false decoration are preserved across ticks.
        if (meta.hasDisplayName()) {
          meta.displayName(replacePlaceholders(meta.displayName(), inQueue, inFight));
        }

        // Replace dynamic placeholders in lore
        List<net.kyori.adventure.text.Component> lore = meta.lore();
        if (lore != null) {
          meta.lore(
              lore.stream().map(line -> replacePlaceholders(line, inQueue, inFight)).toList());
        }

        updated.setItemMeta(meta);
        setStackAmount(updated, inFight > 0 ? inFight : 1);

        inventory.setItem(slot, updated);
      }
    }

    applySelectionGlowToOpenPlayers();

    updatePlayers();
  }

  // -------------------------------------------------------------------------
  // Open / close — start/stop ticker accordingly
  // -------------------------------------------------------------------------

  @Override
  public void open(Player player, int page) {
    super.open(player, page);
    // Delay matches the 2L delay in GUI.open so we start only after inGuiPlayers is set
    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> {
              this.startTicker();
              InventoryView view = player.getOpenInventory();
              applySelectionGlow(player, page, view.getTopInventory());
            },
            3L);
  }

  @Override
  public void close(Player player) {
    super.close(player);
    if (inGuiPlayers.isEmpty()) {
      stopTicker();
    }
  }

  // -------------------------------------------------------------------------
  // Inventory setup
  // -------------------------------------------------------------------------

  private void setupCategoryPage(
      int pageId,
      int rows,
      ItemStack filler,
      GuiItem ladderTemplate,
      CategoryConfig currentCategory,
      List<CategoryConfig> allCategories,
      QueueGui guiConfig,
      Map<Integer, Inventory> existingInventories) {

    String title =
        guiConfig
            .getTitle()
            .replace("%weight_class%", getWeightClass().getName())
            .replace("%category%", currentCategory.displayName());

    int size = rows * 9;
    Inventory inventory = existingInventories.get(pageId);
    if (inventory == null || inventory.getSize() != size) {
      inventory = InventoryUtil.createInventory(title, rows);
    } else {
      inventory.clear();
    }

    this.gui.put(pageId, inventory);
    inventory.clear();
    Map<Integer, NormalLadder> slotMap = new HashMap<>();
    pageLadderSlots.put(pageId, slotMap);

    Map<Integer, ItemStack> pageTemplates = new HashMap<>();
    templateItems.put(pageId, pageTemplates);

    int actualSize = inventory.getSize();

    applyLayoutFillers(inventory, filler, actualSize);

    placeCategorySelectors(inventory, currentCategory, actualSize, guiConfig);
    placeQuickMatchItem(inventory, filler, actualSize, guiConfig);

    int ladderAmount =
        Math.min(currentCategory.ladderNames().size(), currentCategory.ladderSlots().size());
    for (int index = 0; index < ladderAmount; index++) {
      String ladderName = currentCategory.ladderNames().get(index).trim();
      int slot = currentCategory.ladderSlots().get(index);

      if (slot < 0 || slot >= actualSize) {
        continue;
      }

      if (slot == getQuickMatchSlot()) {
        continue;
      }

      NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);
      if (ladder == null
          || !isValidLadder(ladder)
          || !ladder.getMatchTypes().contains(MatchType.DUEL)) {
        continue;
      }

      GuiItem icon = getLadderIcon(ladder, ladderTemplate, guiConfig);
      if (icon == null) {
        continue;
      }

      icon.replace("%ladder%", ladder.getDisplayName());

      boolean activeLadder = ladder.isEnabled() && !ladder.isFrozen();

      // Disabled/frozen templates may only define lore/name; fallback to ladder icon in that case.
      if (icon.getMaterial() == null
          && icon.getBaseItemStack() == null
          && ladder.getIcon() != null) {
        icon.setBaseItem(ladder.getIcon());
      }

      if (activeLadder && ladder.getIcon() != null) {
        icon.setBaseItem(ladder.getIcon());
      }

      if (icon.getMaterial() == null && icon.getBaseItemStack() == null) {
        continue;
      }

      // Apply live queue counters only to active ladders.
      if (activeLadder) {
        updateIconWithQueueData(ladder, icon, guiConfig);
      }

      ItemStack renderedItem = icon.get();
      slotMap.put(slot, ladder);
      inventory.setItem(slot, renderedItem);

      if (activeLadder) {
        // Store a raw template (placeholders intact, no live counts) for the ticker.
        GuiItem rawTemplate = ladderTemplate.cloneItem();
        rawTemplate.replace("%ladder%", ladder.getDisplayName());
        if (ladder.getIcon() != null) {
          rawTemplate.setBaseItem(ladder.getIcon());
        }
        // Apply only the LB lore (which is static) but NOT %in_queue%/%in_fight%
        String lbFormat = guiConfig.getLbFormat();
        rawTemplate.setLore(QueueGuiUtil.replaceLore(lbFormat, rawTemplate.getLore(), ladder));
        rawTemplate.replace("%weight_class%", getWeightClass().getName());
        rawTemplate.setAmount(1);
        pageTemplates.put(slot, rawTemplate.get());
      }
    }

    decoratePage(pageId, inventory);

    fillEmptyConfiguredSlots(
        inventory, filler, actualSize, currentCategory, allCategories, guiConfig);
  }

  private void applyLayoutFillers(Inventory inventory, ItemStack filler, int actualSize) {
    if (filler == null || actualSize <= 0) {
      return;
    }

    int rows = actualSize / 9;
    for (int slot = 0; slot < 9 && slot < actualSize; slot++) {
      inventory.setItem(slot, filler);
    }

    for (int row = 0; row < rows; row++) {
      int leftSlot = row * 9;
      int rightSlot = leftSlot + 8;
      inventory.setItem(leftSlot, filler);
      inventory.setItem(rightSlot, filler);
      if (row == 5) {
        inventory.setItem(leftSlot + 1, filler);
        inventory.setItem(rightSlot - 1, filler);
      }
    }
  }

  /**
   * Fills empty slots configured for a specific purpose (ladder, category selector, or quick match)
   * — e.g. when a ladder was deleted, is invalid, or has no icon.
   */
  private void fillEmptyConfiguredSlots(
      Inventory inventory,
      ItemStack fallbackFiller,
      int actualSize,
      CategoryConfig currentCategory,
      List<CategoryConfig> allCategories,
      QueueGui guiConfig) {
    if (actualSize <= 0) {
      return;
    }

    ItemStack filler = icon(guiConfig, QueueGui.Icons::getEmptySlot).get();
    if (filler == null) {
      filler = fallbackFiller;
    }
    if (filler == null) {
      return;
    }

    Set<Integer> configuredSlots =
        getConfiguredSlots(currentCategory, allCategories, actualSize, getQuickMatchSlot());

    for (int slot : configuredSlots) {
      ItemStack item = inventory.getItem(slot);
      if (item == null || item.getType() == Material.AIR) {
        inventory.setItem(slot, filler);
      }
    }
  }

  private Set<Integer> getConfiguredSlots(
      CategoryConfig currentCategory,
      List<CategoryConfig> allCategories,
      int actualSize,
      int quickMatchSlot) {
    Set<Integer> configuredSlots = new HashSet<>();

    for (int slot : currentCategory.ladderSlots()) {
      if (slot >= 0 && slot < actualSize && slot != quickMatchSlot) {
        configuredSlots.add(slot);
      }
    }

    for (CategoryConfig category : allCategories) {
      if (category.iconDisabled()) {
        continue;
      }

      int slot = category.selectorSlot();
      if (slot >= 0 && slot < actualSize) {
        configuredSlots.add(slot);
      }
    }

    if (quickMatchSlot >= 0 && quickMatchSlot < actualSize) {
      configuredSlots.add(quickMatchSlot);
    }

    return configuredSlots;
  }

  private void placeQuickMatchItem(
      Inventory inventory, ItemStack filler, int actualSize, QueueGui guiConfig) {
    int quickMatchSlot = getQuickMatchSlot();
    if (quickMatchSlot < 0 || quickMatchSlot >= actualSize) {
      return;
    }

    if (!isQuickMatchEnabled()) {
      inventory.setItem(quickMatchSlot, filler);
      return;
    }

    GuiItem quickMatch = icon(guiConfig, QueueGui.Icons::getQuickMatch);
    if (!isTemplateConfigured(quickMatch)) {
      return;
    }

    GuiItem icon = quickMatch.cloneItem().replace("%weight_class%", getWeightClass().getName());
    ItemStack renderedItem = icon.get();
    if (renderedItem != null) {
      inventory.setItem(quickMatchSlot, renderedItem);
    }
  }

  private void placeCategorySelectors(
      Inventory inventory, CategoryConfig currentCategory, int actualSize, QueueGui guiConfig) {
    for (Map.Entry<Integer, List<CategoryConfig>> entry : slotToCategories.entrySet()) {
      int selectorSlot = entry.getKey();
      if (selectorSlot < 0 || selectorSlot >= actualSize) {
        continue;
      }

      List<CategoryConfig> slotCategories = entry.getValue();
      if (slotCategories == null || slotCategories.isEmpty()) {
        continue;
      }

      CategoryConfig targetCategory = getNextCategory(slotCategories, currentCategory);
      if (targetCategory.iconDisabled()) {
        continue;
      }

      boolean selected =
          slotCategories.size() == 1
              && currentCategory.key().equalsIgnoreCase(targetCategory.key());
      GuiItem selectorTemplate = getCategorySelectorTemplate(guiConfig, selected);
      if (selectorTemplate == null) {
        continue;
      }

      GuiItem selectorIcon =
          selectorTemplate
              .cloneItem()
              .replace("%category%", targetCategory.displayName())
              .replace("%category_ladders%", getCategoryLadderPreview(targetCategory))
              .replace("%weight_class%", getWeightClass().getName());

      Material categoryMaterial = getCategorySelectorMaterial(targetCategory, selected);
      if (categoryMaterial != null) {
        selectorIcon.setMaterial(categoryMaterial);
      }

      ItemStack selectorItem = selectorIcon.get();
      if (selectorItem != null) {
        inventory.setItem(selectorSlot, selectorItem);
      }
    }
  }

  private CategoryConfig getNextCategory(
      List<CategoryConfig> slotCategories, CategoryConfig currentCategory) {
    if (slotCategories.size() == 1) {
      return slotCategories.get(0);
    }
    int currentIndex = slotCategories.indexOf(currentCategory);
    int nextIndex = currentIndex != -1 ? (currentIndex + 1) % slotCategories.size() : 0;
    return slotCategories.get(nextIndex);
  }

  private CategoryConfig getCategoryByPage(int page) {
    return page >= 1 && page <= categoriesList.size() ? categoriesList.get(page - 1) : null;
  }

  private int getPageByCategory(CategoryConfig category) {
    return categoriesList.indexOf(category) + 1;
  }

  private GuiItem getCategorySelectorTemplate(QueueGui guiConfig, boolean selected) {
    GuiItem normalTemplate = icon(guiConfig, QueueGui.Icons::getCategorySelector);
    if (!isTemplateConfigured(normalTemplate)) {
      return null;
    }

    if (!selected) {
      return normalTemplate;
    }

    GuiItem selectedTemplate = icon(guiConfig, QueueGui.Icons::getSelectedCategorySelector);
    if (isTemplateConfigured(selectedTemplate)) {
      return selectedTemplate;
    }

    return normalTemplate.cloneItem().setGlowing(true);
  }

  private boolean isTemplateConfigured(GuiItem item) {
    return item != null && (item.getMaterial() != null || item.getBaseItemStack() != null);
  }

  private Material getCategorySelectorMaterial(CategoryConfig category, boolean selected) {
    String materialName =
        selected && !category.selectedIconMaterial().isBlank()
            ? category.selectedIconMaterial()
            : category.iconMaterial();

    if (materialName == null || materialName.isBlank()) {
      return null;
    }

    try {
      return Material.valueOf(materialName.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ignored) {
      return null;
    }
  }

  private String getCategoryLadderPreview(CategoryConfig category) {
    if (category.ladderNames().isEmpty()) {
      return "<gray>No ladders";
    }

    List<String> preview = new ArrayList<>();
    int limit = Math.min(4, category.ladderNames().size());
    for (int i = 0; i < limit; i++) {
      String ladderName = category.ladderNames().get(i);
      NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);
      preview.add(ladder != null ? ladder.getDisplayName() : ladderName);
    }

    return String.join("<gray>, <white>", preview);
  }

  private GuiItem getLadderIcon(NormalLadder ladder, GuiItem template, QueueGui guiConfig) {
    QueueSettings.SelectorGui selectorGui = queueSettings().getSelectorGui();
    boolean showDisabled =
        selectorGui != null && Boolean.TRUE.equals(selectorGui.getShowDisabledLadders());

    if (ladder.isEnabled()) {
      if (!ladder.isFrozen()) {
        GuiItem activeIcon = template.cloneItem();
        updateIconWithQueueData(ladder, activeIcon, guiConfig);
        return activeIcon;
      } else {
        return icon(guiConfig, QueueGui.Icons::getFrozenLadderItem).cloneItem();
      }
    }

    if (showDisabled) {
      return icon(guiConfig, QueueGui.Icons::getDisabledLadderItem).cloneItem();
    }

    return null;
  }

  private void updateIconWithQueueData(NormalLadder ladder, GuiItem icon, QueueGui guiConfig) {
    int duelMatchSize = MatchManager.getInstance().getDuelMatchSize(ladder, isRanked());

    icon.replace(
            "%in_queue%",
            String.valueOf(QueueManager.getInstance().getQueueSize(ladder, isRanked())))
        .replace("%in_fight%", String.valueOf(duelMatchSize))
        .replace("%weight_class%", getWeightClass().getName());

    String lbFormat = guiConfig.getLbFormat();
    icon.setLore(QueueGuiUtil.replaceLore(lbFormat, icon.getLore(), ladder));

    icon.replace("%weight_class%", getWeightClass().getName());

    ItemStack item = icon.get();
    setStackAmount(item, duelMatchSize > 0 ? duelMatchSize : 1);
  }

  // -------------------------------------------------------------------------
  // Click handling
  // -------------------------------------------------------------------------

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    final InventoryView inventoryView = e.getView();
    ItemStack item = e.getCurrentItem();
    final int rawSlot = e.getRawSlot();

    if (!this.getInGuiPlayers().containsKey(player)) {
      return;
    }
    final int page = this.getInGuiPlayers().get(player);

    e.setCancelled(true);

    if (item == null || item.getType() == Material.AIR) {
      return;
    }
    if (rawSlot >= inventoryView.getTopInventory().getSize()) {
      return;
    }

    if (handleCustomTopInventoryClick(player, rawSlot, inventoryView, item)) {
      return;
    }

    if (slotToCategories.containsKey(rawSlot)) {
      List<CategoryConfig> slotCategories = slotToCategories.get(rawSlot);
      if (slotCategories != null && !slotCategories.isEmpty()) {
        CategoryConfig nextCategory = getNextCategory(slotCategories, getCategoryByPage(page));
        int targetPage = getPageByCategory(nextCategory);

        if (targetPage != page && this.gui.containsKey(targetPage)) {
          this.open(player, targetPage);
        }
      }
      return;
    }

    if (rawSlot == getQuickMatchSlot() && isQuickMatchEnabled()) {
      Map<Integer, NormalLadder> ladders = pageLadderSlots.get(page);
      if (ladders == null || ladders.isEmpty()) {
        return;
      }

      NormalLadder randomLadder = getRandomQuickMatchLadder(ladders);
      if (randomLadder != null) {
        onLadderClick(player, randomLadder);
        applySelectionGlow(player, page, inventoryView.getTopInventory());
      }
      return;
    }

    Map<Integer, NormalLadder> currentMap = pageLadderSlots.get(page);
    if (currentMap == null) {
      return;
    }

    if (currentMap.containsKey(rawSlot)) {
      onLadderClick(player, currentMap.get(rawSlot));
      applySelectionGlow(player, page, inventoryView.getTopInventory());
    }
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  /**
   * Allows non-stackable items to display with a specific amount by overriding their max stack
   * size. This enables items like swords to show multiple amounts in inventory displays.
   */
  private void setStackAmount(ItemStack item, int amount) {
    if (item == null || amount <= 0) {
      return;
    }

    item.setAmount(Math.min(amount, 64));

    if (amount > 1) {
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
        meta.setMaxStackSize(64);
        item.setItemMeta(meta);
      }
    }
  }

  private void applySelectionGlowToOpenPlayers() {
    for (Map.Entry<Player, Integer> entry : this.getInGuiPlayers().entrySet()) {
      Player player = entry.getKey();
      int page = entry.getValue();

      if (player == null || !player.isOnline()) {
        continue;
      }

      InventoryView view = player.getOpenInventory();
      applySelectionGlow(player, page, view.getTopInventory());
    }
  }

  private void applySelectionGlow(Player player, int page, Inventory inventory) {
    if (inventory == null) {
      return;
    }

    Inventory expectedInventory = this.gui.get(page);
    if (expectedInventory == null || expectedInventory != inventory) {
      return;
    }

    Map<Integer, NormalLadder> ladders = pageLadderSlots.get(page);
    if (ladders == null || ladders.isEmpty()) {
      return;
    }

    Queue queue = QueueManager.getInstance().getQueue(player);
    int inventorySize = inventory.getSize();

    for (Map.Entry<Integer, NormalLadder> entry : ladders.entrySet()) {
      int slot = entry.getKey();
      if (slot < 0 || slot >= inventorySize) {
        continue;
      }

      ItemStack item = inventory.getItem(slot);
      if (item == null || item.getType() == Material.AIR) {
        continue;
      }

      boolean selected =
          queue != null
              && queue.isRanked() == isRanked()
              && queue.isQueued(entry.getValue())
              && QueueManager.getInstance().isMultiQueueAllowed(player);

      setItemGlow(item, selected);
      inventory.setItem(slot, item);
    }
  }

  private void setItemGlow(ItemStack item, boolean glowing) {
    ItemMeta meta = item.getItemMeta();
    if (meta == null) {
      return;
    }

    if (glowing) {
      meta.addEnchant(Enchantment.UNBREAKING, 1, true);
      meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    } else {
      meta.removeEnchant(Enchantment.UNBREAKING);
      meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
    }

    item.setItemMeta(meta);
  }

  private List<CategoryConfig> loadCategories(QueueSettings.SelectorGui selectorGui) {
    if (selectorGui == null) {
      return Collections.emptyList();
    }

    List<Integer> sharedLadderSlots = getConfiguredIntList(selectorGui.getLadderSlots());
    Map<String, QueueSettings.SelectorGui.QueueCategory> categoriesSection =
        selectorGui.getCategories();
    Map<String, Integer> selectorSection = selectorGui.getCategorySelectorSlots();

    if (categoriesSection == null || selectorSection == null) {
      return Collections.emptyList();
    }

    List<CategoryConfig> categories = new ArrayList<>();

    for (Map.Entry<String, QueueSettings.SelectorGui.QueueCategory> entry :
        categoriesSection.entrySet()) {
      String categoryKey = entry.getKey();
      QueueSettings.SelectorGui.QueueCategory category = entry.getValue();
      if (category == null) {
        continue;
      }

      String displayName = Objects.requireNonNullElse(category.getDisplayName(), "");
      if (displayName.isEmpty()) {
        displayName = categoryKey;
      }

      List<String> ladders =
          category.getLadders() != null ? category.getLadders() : new ArrayList<>();
      List<Integer> ladderSlots = getConfiguredIntList(category.getLadderSlots());
      if (ladderSlots.isEmpty()) {
        ladderSlots = sharedLadderSlots;
      }

      Integer configuredSelectorSlot = selectorSection.get(categoryKey);
      int selectorSlot = configuredSelectorSlot != null ? configuredSelectorSlot : -1;
      if (selectorSlot < 0 || ladderSlots.isEmpty()) {
        continue;
      }

      String iconMaterial = Objects.requireNonNullElse(category.getIconMaterial(), "");
      String selectedIconMaterial =
          Objects.requireNonNullElse(category.getSelectedIconMaterial(), "");
      boolean iconDisabled = Boolean.TRUE.equals(category.getIconDisabled());

      categories.add(
          new CategoryConfig(
              categoryKey,
              displayName,
              selectorSlot,
              iconMaterial,
              selectedIconMaterial,
              ladders,
              new ArrayList<>(ladderSlots),
              iconDisabled));
    }

    return categories;
  }

  private List<Integer> getConfiguredIntList(List<Integer> entries) {
    List<Integer> slots = new ArrayList<>();
    if (entries == null) {
      return slots;
    }

    for (Integer entry : entries) {
      if (entry != null) {
        slots.add(entry);
      }
    }

    return slots;
  }

  /** Builds the GUI item of an icon, or an empty item when the icon is not configured. */
  private static GuiItem icon(QueueGui guiConfig, Function<QueueGui.Icons, ItemConfig> getter) {
    QueueGui.Icons icons = guiConfig.getIcons();
    ItemConfig item = icons != null ? getter.apply(icons) : null;
    return item != null ? BackendUtil.getGuiItem(item) : new GuiItem();
  }

  private int getQuickMatchSlot() {
    QueueSettings.SelectorGui selectorGui = queueSettings().getSelectorGui();
    Integer configured = selectorGui != null ? selectorGui.getQuickMatchSlot() : null;
    if (configured == null) {
      return DEFAULT_QUICK_MATCH_SLOT;
    }

    return configured >= 0 ? configured : DEFAULT_QUICK_MATCH_SLOT;
  }

  private boolean isQuickMatchEnabled() {
    QueueSettings.SelectorGui selectorGui = queueSettings().getSelectorGui();
    Boolean configured = selectorGui != null ? selectorGui.getQuickMatchEnabled() : null;
    return configured == null || configured;
  }

  private NormalLadder getRandomQuickMatchLadder(Map<Integer, NormalLadder> laddersInCategory) {
    List<NormalLadder> candidates = new ArrayList<>();
    for (NormalLadder ladder : new LinkedHashSet<>(laddersInCategory.values())) {
      if (ladder != null && ladder.isEnabled() && !ladder.isFrozen()) {
        candidates.add(ladder);
      }
    }

    if (candidates.isEmpty()) {
      candidates.addAll(new LinkedHashSet<>(laddersInCategory.values()));
    }

    if (candidates.isEmpty()) {
      return null;
    }

    return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
  }

  private record CategoryConfig(
      String key,
      String displayName,
      int selectorSlot,
      String iconMaterial,
      String selectedIconMaterial,
      List<String> ladderNames,
      List<Integer> ladderSlots,
      boolean iconDisabled) {}

  /**
   * Replaces %in_queue% and %in_fight% in a Component using Adventure's text replacement API so
   * colors and decorations (italic=false) are preserved.
   */
  private static net.kyori.adventure.text.Component replacePlaceholders(
      net.kyori.adventure.text.Component component, int inQueue, int inFight) {
    return component
        .replaceText(
            b ->
                b.matchLiteral("%in_queue%")
                    .replacement(net.kyori.adventure.text.Component.text(inQueue)))
        .replaceText(
            b ->
                b.matchLiteral("%in_fight%")
                    .replacement(net.kyori.adventure.text.Component.text(inFight)));
  }
}
