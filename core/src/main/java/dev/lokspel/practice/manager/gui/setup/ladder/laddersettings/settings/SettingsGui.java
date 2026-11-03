package dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.BlockReturnDelayItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.BoxingHitsItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.BreakAllBlocksItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.CountdownTitlesItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.CreeperExplosionDelayItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.DropInventoryItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.EditableItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.EnderpearlItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.FireballBlockDestroyItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.FireballCooldownItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.GoldenAppleItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.HealthBelowNameItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.HeartsItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.HitdelayItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.HungerItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.ItemPickupItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.KnockbackItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.MaxDurationItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.MultiRoundStartCountdownItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.PullPlayersWithRodItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.RankedItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.RegenerationItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.ResetBuildAfterRoundItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.RoundEndDelayItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.RoundStatusTitlesItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.RoundsItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.SkywarsLootItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.SpleefSnowballModeItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.StartCountdownItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.StartMovingItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.TempBuildReturnDelayItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.TempRespawnTimeItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.TntAutoIgniteItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.TntFuseTimeItem;
import dev.lokspel.practice.manager.gui.setup.ladder.laddersettings.settings.items.WindChargeCooldownItem;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import dev.lokspel.practice.manager.ladder.type.Boxing;
import dev.lokspel.practice.manager.ladder.type.Creeper;
import dev.lokspel.practice.manager.ladder.type.SkyWars;
import dev.lokspel.practice.manager.ladder.type.Spleef;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SettingsGui extends Gui {

  @Getter private final NormalLadder ladder;
  @Getter private final LadderType ladderType;
  private final GuiSlot guiSlot;

  private final List<SettingItem> settingItems = new ArrayList<>();

  public SettingsGui(@NotNull NormalLadder ladder) {
    super(GuiType.Ladder_Settings);

    this.ladder = ladder;
    this.ladderType = ladder.getType();

    if (ladderType.getSettingTypes().size() <= 7) {
      guiSlot = GuiSlot.SIZE_1;
    } else if (ladderType.getSettingTypes().size() <= 14) {
      guiSlot = GuiSlot.SIZE_2;
    } else if (ladderType.getSettingTypes().size() <= 21) {
      guiSlot = GuiSlot.SIZE_3;
    } else {
      guiSlot = GuiSlot.SIZE_4;
    }

    gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getLadder()
                .getSettings()
                .getTitle()
                .replace("%ladder%", ladder.getName()),
            (guiSlot.getRows() + 1)));

    build();
  }

  @Override
  public void build() {
    Inventory inventory = gui.get(1);
    inventory.clear();
    settingItems.clear();

    inventory.setItem(
        guiSlot.getBackSlot(),
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getSetup()
                    .getLadder()
                    .getSettings()
                    .getIcons()
                    .getGoBack())
            .get());

    addItems();

    // Set the setting items
    int currentItemSlot = 0;
    for (SettingItem settingItem : settingItems) {
      if (currentItemSlot >= guiSlot.getSlots().size()) {
        Common.sendConsoleMmMessage(
            "<red>[Ladder Settings GUI] Too many settings for layout: " + ladder.getName());
        break;
      }
      int slot = guiSlot.getSlots().get(currentItemSlot);
      currentItemSlot++;
      settingItem.setSlot(slot);
      settingItem.build(false);
    }

    // Filler items
    for (int i = 0; i < inventory.getSize(); i++) {
      if (inventory.getItem(i) == null) {
        inventory.setItem(i, GuiManager.fillerItem());
      }
    }

    update();
  }

  @Override
  public void update() {
    updatePlayers();
  }

  @Override
  public void handleClickEvent(@NotNull InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    Inventory inventory = e.getView().getTopInventory();

    int slot = e.getRawSlot();
    final ItemStack item = e.getCurrentItem();

    e.setCancelled(true);

    if (slot == guiSlot.getBackSlot()) {
      LadderSetupManager.getInstance()
          .getLadderSetupGuis()
          .get(ladder)
          .get(GuiType.Ladder_Main)
          .open(player);
      return;
    }

    if (inventory.getSize() <= slot) {
      return;
    }
    if (item == null) {
      return;
    }
    if (item.equals(GuiManager.fillerItem())) {
      return;
    }

    if (ladder.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getSetup().getLadder().getCantEditEnabled());
      return;
    }

    SettingItem settingItem = searchBySlot(slot);
    if (settingItem != null) {
      settingItem.clickEvent(e);
    }
  }

  @Contract(pure = true)
  private @Nullable SettingItem searchBySlot(int slot) {
    for (SettingItem settingItem : settingItems) {
      if (settingItem.getSlot() == slot) {
        return settingItem;
      }
    }
    return null;
  }

  @Getter
  public enum GuiSlot {
    SIZE_1(2, new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16))),
    SIZE_2(
        3, new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25))),
    SIZE_3(
        4,
        new ArrayList<>(
            Arrays.asList(
                10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33,
                34))),
    SIZE_4(
        5,
        new ArrayList<>(
            Arrays.asList(
                10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43)));

    private final int rows;
    private final int backSlot;
    private final List<Integer> slots;

    GuiSlot(int rows, List<Integer> slots) {
      this.rows = rows;
      this.backSlot = rows * 9;
      this.slots = slots;
    }
  }

  private void addItems() {
    final List<SettingType> settingTypes = ladderType.getSettingTypes();

    if (settingTypes.contains(SettingType.REGENERATION)) {
      settingItems.add(new RegenerationItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.HUNGER)) {
      settingItems.add(new HungerItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.EDITABLE)) {
      settingItems.add(new EditableItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.ENDER_PEARL_COOLDOWN)) {
      settingItems.add(new EnderpearlItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.GOLDEN_APPLE_COOLDOWN)) {
      settingItems.add(new GoldenAppleItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.HIT_DELAY)) {
      settingItems.add(new HitdelayItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.KNOCKBACK)) {
      settingItems.add(new KnockbackItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.START_COUNTDOWN)) {
      settingItems.add(new StartCountdownItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.START_MOVING)) {
      settingItems.add(new StartMovingItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.MULTI_ROUND_START_COUNTDOWN)) {
      settingItems.add(new MultiRoundStartCountdownItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.DROP_INVENTORY)) {
      settingItems.add(new DropInventoryItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.WEIGHT_CLASS)) {
      settingItems.add(new RankedItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.ROUNDS)) {
      settingItems.add(new RoundsItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.HEARTS)) {
      settingItems.add(new HeartsItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.RESPAWN_TIME)) {
      settingItems.add(new TempRespawnTimeItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.BOXING_HITS)) {
      settingItems.add(new BoxingHitsItem(this, (Boxing) ladder));
    }

    if (settingTypes.contains(SettingType.FIREBALL_COOLDOWN)) {
      settingItems.add(new FireballCooldownItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.FIREBALL_BLOCK_DESTROY)) {
      settingItems.add(new FireballBlockDestroyItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.SKYWARS_LOOT)) {
      settingItems.add(new SkywarsLootItem(this, (SkyWars) ladder));
    }

    if (settingTypes.contains(SettingType.MAX_DURATION)) {
      settingItems.add(new MaxDurationItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.TNT_FUSE_TIME)) {
      settingItems.add(new TntFuseTimeItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.TNT_AUTO_IGNITE)) {
      settingItems.add(new TntAutoIgniteItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.HEALTH_BELOW_NAME)) {
      settingItems.add(new HealthBelowNameItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.RESET_BUILD_AFTER_ROUND)) {
      settingItems.add(new ResetBuildAfterRoundItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.BREAK_ALL_BLOCKS)) {
      settingItems.add(new BreakAllBlocksItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.SPLEEF_SNOWBALL_MODE)) {
      settingItems.add(new SpleefSnowballModeItem(this, (Spleef) ladder));
    }

    if (settingTypes.contains(SettingType.BLOCK_RETURN_DELAY)) {
      settingItems.add(new BlockReturnDelayItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.TEMP_BUILD_RETURN_DELAY)) {
      settingItems.add(new TempBuildReturnDelayItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.ROUND_END_DELAY)) {
      settingItems.add(new RoundEndDelayItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.ROUND_STATUS_TITLES)) {
      settingItems.add(new RoundStatusTitlesItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.COUNTDOWN_TITLES)) {
      settingItems.add(new CountdownTitlesItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.WIND_CHARGE_COOLDOWN)) {
      settingItems.add(new WindChargeCooldownItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.CREEPER_EXPLOSION_DELAY)) {
      settingItems.add(new CreeperExplosionDelayItem(this, (Creeper) ladder));
    }

    if (settingTypes.contains(SettingType.PULL_PLAYERS_WITH_ROD)) {
      settingItems.add(new PullPlayersWithRodItem(this, ladder));
    }

    if (settingTypes.contains(SettingType.ITEM_PICKUP)) {
      settingItems.add(new ItemPickupItem(this, ladder));
    }
  }
}
