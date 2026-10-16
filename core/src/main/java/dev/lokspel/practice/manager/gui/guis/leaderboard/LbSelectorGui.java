package dev.lokspel.practice.manager.gui.guis.leaderboard;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.division.Division;
import dev.lokspel.practice.manager.division.DivisionManager;
import dev.lokspel.practice.manager.division.DivisionUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.guis.DivisionGui;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.StatisticUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class LbSelectorGui extends Gui {

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getStatistics()
                .getSelector()
                .getIcons()
                .getFillerItem())
        .get();
  }

  @Getter private static LbEloGui sharedLbEloGui;
  @Getter private static LbWinGui sharedLbWinGui;

  private final Player opener;
  private final Profile profile;

  private final LbProfileStatGui lbProfileStatGui;

  /** Drops the shared leaderboards, the next selector creates them from the current files. */
  public static void resetSharedGuis() {
    sharedLbEloGui = null;
    sharedLbWinGui = null;
  }

  public LbSelectorGui(Player opener, Profile profile) {
    super(GuiType.Leaderboard_Selector);

    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis().getGuis().getStatistics().getSelector().getTitle(), 4));
    this.opener = opener;
    this.profile = profile;

    this.lbProfileStatGui = new LbProfileStatGui(profile, this);

    if (sharedLbEloGui == null) {
      sharedLbEloGui = new LbEloGui(this);
    }
    if (sharedLbWinGui == null) {
      sharedLbWinGui = new LbWinGui(this);
    }

    build();
  }

  @Override
  public void build() {
    Inventory inventory = gui.get(1);

    for (int i = 0; i < inventory.getSize(); i++) {
      inventory.setItem(i, fillerItem());
    }

    update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);
    if (inventory == null) {
      return;
    }

    inventory.setItem(13, LbGuiUtil.createProfileStatItem(profile, opener));
    inventory.setItem(
        20,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getStatistics()
                    .getSelector()
                    .getIcons()
                    .getEloLeaderboard())
            .get());
    inventory.setItem(22, getDivisionItem(profile));
    inventory.setItem(
        24,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getStatistics()
                    .getSelector()
                    .getIcons()
                    .getTopWinLeaderboard())
            .get());

    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();

    e.setCancelled(true);

    switch (slot) {
      case 13:
        lbProfileStatGui.open(player);
        break;
      case 20:
        sharedLbEloGui.open(player);
        break;
      case 22:
        new DivisionGui(profile, this).open(player);
        break;
      case 24:
        sharedLbWinGui.open(player);
        break;

      default:
        break;
    }
  }

  private static ItemStack getDivisionItem(Profile profile) {
    Division division = profile.getStats().getDivision();
    Division nextDivision = DivisionManager.getInstance().getNextDivision(profile);

    GuiItem guiItem;
    if (nextDivision != null) {
      guiItem =
          BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getStatistics()
                  .getSelector()
                  .getIcons()
                  .getViewDivisions()
                  .getHasNext());

      guiItem
          .replace("%nextDivision_fullName%", Common.mmToNormal(nextDivision.getFullName()))
          .replace("%nextDivision_shortName%", Common.mmToNormal(nextDivision.getShortName()))
          .replace("%nextDivision_exp%", String.valueOf(nextDivision.getExperience()))
          .replace("%nextDivision_wins%", String.valueOf(nextDivision.getWin()))
          .replace(
              "%progress_bar%",
              StatisticUtil.getProgressBar(DivisionUtil.getDivisionProgress(profile, nextDivision)))
          .replace(
              "%progress_percent%",
              String.valueOf(DivisionUtil.getDivisionProgress(profile, nextDivision)));
    } else {
      guiItem =
          BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getStatistics()
                  .getSelector()
                  .getIcons()
                  .getViewDivisions()
                  .getNoNext());
    }

    guiItem
        .replace("%division_fullName%", Common.mmToNormal(division.getFullName()))
        .replace("%division_shortName%", Common.mmToNormal(division.getShortName()))
        .replace("%exp%", String.valueOf(profile.getStats().getExperience()))
        .replace("%elo%", String.valueOf(profile.getStats().getGlobalElo()))
        .replace("%wins%", String.valueOf(profile.getStats().getGlobalWins()));

    return guiItem.get();
  }
}
