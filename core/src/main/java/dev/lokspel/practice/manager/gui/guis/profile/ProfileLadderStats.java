package dev.lokspel.practice.manager.gui.guis.profile;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.statistics.LadderStats;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.PageUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ProfileLadderStats extends Gui {

  private final Profile profile;
  private final Gui backTo;
  private final int spaces = 45;
  private final Map<Integer, Map<Integer, NormalLadder>> ladderSlots = new HashMap<>();

  public ProfileLadderStats(Profile profile, Gui backTo) {
    super(GuiType.Profile_LadderStats);
    this.profile = profile;
    this.backTo = backTo;

    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    List<NormalLadder> ladders = new ArrayList<>(LadderManager.getInstance().getLadders());
    ladders.sort(Comparator.comparing(NormalLadder::getName, String::compareToIgnoreCase));

    Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
    Map<Integer, Inventory> newGui = new HashMap<>();
    Map<Integer, Map<Integer, NormalLadder>> newLadderSlots = new HashMap<>();

    for (int page = 1; PageUtil.isPageValid(ladders.size(), page, spaces) || page == 1; page++) {
      Inventory inventory = existingInventories.get(page);
      String title =
          PracticeConfig.guis()
              .getGuis()
              .getPlayerInformation()
              .getLadderStats()
              .getTitle()
              .replace("%player%", Objects.requireNonNull(profile.getPlayer().getName()))
              .replace("%page%", String.valueOf(page));
      if (inventory == null || inventory.getSize() != 6 * 9) {
        inventory = InventoryUtil.createInventory(title, 6);
      } else {
        inventory.clear();
      }
      newGui.put(page, inventory);

      for (int i = 45; i < 54; i++) {
        inventory.setItem(i, GuiManager.fillerItem());
      }

      Map<Integer, NormalLadder> pageSlots = new HashMap<>();
      int startIndex = (page - 1) * spaces;
      int endIndex = Math.min(startIndex + spaces, ladders.size());
      for (int i = startIndex; i < endIndex; i++) {
        int slot = inventory.firstEmpty();
        if (slot == -1 || slot >= 45) {
          break;
        }

        NormalLadder ladder = ladders.get(i);
        inventory.setItem(slot, getLadderStatItem(ladder));
        pageSlots.put(slot, ladder);
      }
      newLadderSlots.put(page, pageSlots);

      inventory.setItem(
          45,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getPlayerInformation()
                      .getLadderStats()
                      .getIcons()
                      .getBackToHub())
              .get());

      inventory.setItem(
          46,
          page > 1
              ? BackendUtil.getGuiItem(
                      PracticeConfig.guis()
                          .getGuis()
                          .getPlayerInformation()
                          .getLadderStats()
                          .getIcons()
                          .getPageLeft())
                  .replace("%page%", String.valueOf(page - 1))
                  .get()
              : GuiManager.fillerItem());
      inventory.setItem(
          47,
          PageUtil.isPageValid(ladders.size(), page + 1, spaces)
              ? BackendUtil.getGuiItem(
                      PracticeConfig.guis()
                          .getGuis()
                          .getPlayerInformation()
                          .getLadderStats()
                          .getIcons()
                          .getPageRight())
                  .replace("%page%", String.valueOf(page + 1))
                  .get()
              : GuiManager.fillerItem());

      inventory.setItem(
          49,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getPlayerInformation()
                      .getLadderStats()
                      .getIcons()
                      .getRefresh())
              .get());
      inventory.setItem(
          53,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getPlayerInformation()
                      .getLadderStats()
                      .getIcons()
                      .getResetAllStats())
              .get());
    }

    updatePages(newGui, ladderSlots, newLadderSlots);
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    final Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();
    Inventory inventory = e.getView().getTopInventory();
    ItemStack item = e.getCurrentItem();

    e.setCancelled(true);

    if (inventory.getSize() <= slot) {
      return;
    }
    if (item == null) {
      return;
    }

    int page = inGuiPlayers.getOrDefault(player, 1);
    switch (slot) {
      case 45:
        backTo.open(player);
        break;
      case 46:
        if (page > 1 && gui.containsKey(page - 1)) {
          open(player, page - 1);
        }
        break;
      case 47:
        if (gui.containsKey(page + 1)) {
          open(player, page + 1);
        }
        break;
      case 49:
        update();
        break;
      case 53:
        if (!player.hasPermission("ap.practice.info.resetstats")) {
          Common.sendMmMessage(player, PracticeConfig.language().getProfile().getNoPermission());
          return;
        }

        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
          profile.getStats().loadDefaultStats(ladder);
        }
        update();
        break;
      default:
        if (!ladderSlots.containsKey(page) || !ladderSlots.get(page).containsKey(slot)) {
          return;
        }

        if (!player.hasPermission("ap.practice.info.resetstats")) {
          Common.sendMmMessage(player, PracticeConfig.language().getProfile().getNoPermission());
          return;
        }

        NormalLadder ladder = ladderSlots.get(page).get(slot);
        profile.getStats().loadDefaultStats(ladder);
        update();
        break;
    }
  }

  private ItemStack getLadderStatItem(NormalLadder ladder) {
    List<String> lore = new ArrayList<>();
    String nullString =
        PracticeConfig.guis()
            .getGuis()
            .getPlayerInformation()
            .getLadderStats()
            .getIcons()
            .getLadder()
            .getNullStat();

    if (!ladder.isRanked()) {
      for (String line :
          PracticeConfig.guis()
              .getGuis()
              .getPlayerInformation()
              .getLadderStats()
              .getIcons()
              .getLadder()
              .getUnrankedLadderStats()
              .getLore()) {
        lore.add(
            line.replace("%ladder%", ladder.getDisplayName())
                .replace(
                    "%wins%",
                    String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedWins()))
                .replace(
                    "%losses%",
                    String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedLosses()))
                .replace(
                    "%custom_kits%",
                    profile.getUnrankedCustomKits().containsKey(ladder)
                        ? String.valueOf(profile.getUnrankedCustomKits().get(ladder).size())
                        : nullString));
      }

      if (ladder.getIcon() != null) {
        return ItemCreateUtil.createItem(
            ladder.getIcon(),
            PracticeConfig.guis()
                .getGuis()
                .getPlayerInformation()
                .getLadderStats()
                .getIcons()
                .getLadder()
                .getUnrankedLadderStats()
                .getName()
                .replace("%ladder%", ladder.getDisplayName()),
            lore);
      } else {
        return ItemCreateUtil.createItem(
            PracticeConfig.guis()
                .getGuis()
                .getPlayerInformation()
                .getLadderStats()
                .getIcons()
                .getLadder()
                .getUnrankedLadderStats()
                .getName()
                .replace("%ladder%", ladder.getDisplayName()),
            Material.valueOf(
                PracticeConfig.guis()
                    .getGuis()
                    .getPlayerInformation()
                    .getLadderStats()
                    .getIcons()
                    .getLadder()
                    .getUnrankedLadderStats()
                    .getDefaultMaterial()),
            lore);
      }
    } else {
      LadderStats ladderStats = profile.getStats().getLadderStat(ladder);
      for (String line :
          PracticeConfig.guis()
              .getGuis()
              .getPlayerInformation()
              .getLadderStats()
              .getIcons()
              .getLadder()
              .getRankedLadderStats()
              .getLore()) {
        lore.add(
            line.replace("%unranked_wins%", String.valueOf(ladderStats.getUnRankedWins()))
                .replace("%unranked_losses%", String.valueOf(ladderStats.getUnRankedLosses()))
                .replace(
                    "%unranked_custom_kits%",
                    String.valueOf(
                        profile.getUnrankedCustomKits().containsKey(ladder)
                            ? profile.getUnrankedCustomKits().get(ladder).size()
                            : nullString))
                .replace("%ranked_wins%", String.valueOf(ladderStats.getRankedWins()))
                .replace("%ranked_losses%", String.valueOf(ladderStats.getRankedLosses()))
                .replace(
                    "%ranked_custom_kits%",
                    String.valueOf(
                        profile.getRankedCustomKits().containsKey(ladder)
                            ? profile.getRankedCustomKits().get(ladder).size()
                            : nullString))
                .replace("%elo%", String.valueOf(ladderStats.getElo())));
      }

      if (ladder.getIcon() != null) {
        return ItemCreateUtil.createItem(
            ladder.getIcon(),
            PracticeConfig.guis()
                .getGuis()
                .getPlayerInformation()
                .getLadderStats()
                .getIcons()
                .getLadder()
                .getRankedLadderStats()
                .getName()
                .replace("%ladder%", ladder.getDisplayName()),
            lore);
      } else {
        return ItemCreateUtil.createItem(
            PracticeConfig.guis()
                .getGuis()
                .getPlayerInformation()
                .getLadderStats()
                .getIcons()
                .getLadder()
                .getRankedLadderStats()
                .getName()
                .replace("%ladder%", ladder.getDisplayName()),
            Material.valueOf(
                PracticeConfig.guis()
                    .getGuis()
                    .getPlayerInformation()
                    .getLadderStats()
                    .getIcons()
                    .getLadder()
                    .getRankedLadderStats()
                    .getDefaultMaterial()),
            lore);
      }
    }
  }
}
