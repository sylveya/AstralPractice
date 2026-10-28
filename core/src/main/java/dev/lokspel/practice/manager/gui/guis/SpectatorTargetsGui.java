package dev.lokspel.practice.manager.gui.guis;

import dev.lokspel.practice.config.ItemConfig;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.enums.TeamEnum;
import dev.lokspel.practice.manager.fight.match.interfaces.Team;
import dev.lokspel.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayers;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.spectator.SpectatorManager;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.PageUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SpectatorTargetsGui extends Gui {

  private static final int COLUMNS_PER_ROW = 9;
  private static final int ROWS = 6;
  private static final int CONTENT_ROWS = ROWS - 1;
  private static final int PAGE_LEFT_SLOT = CONTENT_ROWS * COLUMNS_PER_ROW;
  private static final int PAGE_RIGHT_SLOT = ROWS * COLUMNS_PER_ROW - 1;

  /** Team members take four columns each; the remaining centre column separates the teams. */
  private static final int TEAM_COLUMNS = 4;

  private static final int DIVIDER_COLUMN = TEAM_COLUMNS;

  private final Match match;
  private final Map<Integer, Map<Integer, Player>> pageSlots = new HashMap<>();

  public SpectatorTargetsGui(Match match) {
    super(GuiType.Spectator_Targets);

    this.match = match;

    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    // Only party team games are split into halves; 1v1 duels keep the plain list.
    boolean teamMatch = match instanceof PlayersVsPlayers;
    List<Player> players = match.getPlayers();
    List<Player> firstTeam = teamMatch ? getTeamPlayers(TeamEnum.TEAM1) : List.of();
    List<Player> secondTeam = teamMatch ? getTeamPlayers(TeamEnum.TEAM2) : List.of();

    int capacity = pageCapacity(teamMatch ? TEAM_COLUMNS : COLUMNS_PER_ROW);
    int playersOnLargestPage =
        teamMatch ? Math.max(firstTeam.size(), secondTeam.size()) : players.size();
    int maxPage = PageUtil.getMaxPage(playersOnLargestPage, capacity);

    Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
    Map<Integer, Inventory> newGui = new HashMap<>();
    Map<Integer, Map<Integer, Player>> newPageSlots = new HashMap<>();

    for (int page = 1; page <= maxPage; page++) {
      Inventory inventory = existingInventories.get(page);
      if (inventory == null || inventory.getSize() != ROWS * COLUMNS_PER_ROW) {
        inventory = InventoryUtil.createInventory(getTitle(page), ROWS);
      } else {
        inventory.clear();
      }
      newGui.put(page, inventory);

      Map<Integer, Player> currentSlots = new HashMap<>();
      if (teamMatch) {
        fillPage(inventory, currentSlots, firstTeam, page, 0, TEAM_COLUMNS);
        fillPage(inventory, currentSlots, secondTeam, page, DIVIDER_COLUMN + 1, TEAM_COLUMNS);
        fillDivider(inventory);
      } else {
        fillPage(inventory, currentSlots, players, page, 0, COLUMNS_PER_ROW);
      }
      newPageSlots.put(page, currentSlots);

      fillNavigation(inventory, page, maxPage);
    }

    updatePages(newGui, pageSlots, newPageSlots);
  }

  private static int pageCapacity(int columnsPerRow) {
    return columnsPerRow * CONTENT_ROWS;
  }

  private void fillPage(
      Inventory inventory,
      Map<Integer, Player> slots,
      List<Player> players,
      int page,
      int startColumn,
      int columnsPerRow) {
    int capacity = pageCapacity(columnsPerRow);
    int startIndex = (page - 1) * capacity;
    int endIndex = Math.min(startIndex + capacity, players.size());

    for (int index = startIndex; index < endIndex; index++) {
      int offset = index - startIndex;
      int slot = offset / columnsPerRow * COLUMNS_PER_ROW + startColumn + offset % columnsPerRow;

      Player target = players.get(index);
      slots.put(slot, target);
      inventory.setItem(slot, getTargetItem(target));
    }
  }

  private void fillDivider(Inventory inventory) {
    ItemStack divider = GuiManager.fillerItem();

    for (int row = 0; row < CONTENT_ROWS; row++) {
      inventory.setItem(row * COLUMNS_PER_ROW + DIVIDER_COLUMN, divider);
    }
  }

  private void fillNavigation(Inventory inventory, int page, int maxPage) {
    ItemStack filler = GuiManager.fillerItem();
    for (int slot = PAGE_LEFT_SLOT; slot < inventory.getSize(); slot++) {
      inventory.setItem(slot, filler);
    }

    if (page > 1) {
      ItemStack previous =
          getNavigationItem(
              PracticeConfig.guis().getGuis().getSpectatorTargets().getIcons().getPageLeft(),
              page - 1);
      inventory.setItem(PAGE_LEFT_SLOT, previous);
    }

    if (page < maxPage) {
      ItemStack next =
          getNavigationItem(
              PracticeConfig.guis().getGuis().getSpectatorTargets().getIcons().getPageRight(),
              page + 1);
      inventory.setItem(PAGE_RIGHT_SLOT, next);
    }
  }

  private ItemStack getNavigationItem(ItemConfig item, int targetPage) {
    return BackendUtil.getGuiItem(item).replace("%page%", String.valueOf(targetPage)).get();
  }

  private String getTitle(int page) {
    return PracticeConfig.guis()
        .getGuis()
        .getSpectatorTargets()
        .getTitle()
        .replace("%page%", String.valueOf(page));
  }

  private List<Player> getTeamPlayers(TeamEnum team) {
    List<Player> players = new ArrayList<>();

    for (Player player : match.getPlayers()) {
      if (getTeam(player) == team) {
        players.add(player);
      }
    }

    return players;
  }

  private ItemStack getTargetItem(Player target) {
    GuiItem configItem =
        BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getSpectatorTargets().getIcons().getTargetItem());

    GuiItem headItem = new GuiItem(ItemCreateUtil.getPlayerHead(target));
    headItem.setName(configItem.getName());
    headItem.setLore(configItem.getLore());

    return headItem
        .replace("%player%", target.getName())
        .replace("%team%", getTeam(target).getNameMm())
        .replace("%health%", String.format("%.1f", target.getHealth()))
        .get();
  }

  private TeamEnum getTeam(Player target) {
    return match instanceof Team team ? team.getTeam(target) : TeamEnum.FFA;
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    e.setCancelled(true);

    Player spectator = (Player) e.getWhoClicked();
    Inventory inventory = e.getView().getTopInventory();
    int slot = e.getRawSlot();

    if (slot >= inventory.getSize()) {
      return;
    }

    int page = inGuiPlayers.getOrDefault(spectator, 1);

    if (slot == PAGE_LEFT_SLOT) {
      if (page > 1) {
        open(spectator, page - 1);
      }
      return;
    }

    if (slot == PAGE_RIGHT_SLOT) {
      if (gui.containsKey(page + 1)) {
        open(spectator, page + 1);
      }
      return;
    }

    Player target = pageSlots.getOrDefault(page, Map.of()).get(slot);
    if (target == null) {
      return;
    }

    SpectatorManager.getInstance().teleportToTarget(spectator, match, target);
    spectator.closeInventory();
  }
}
