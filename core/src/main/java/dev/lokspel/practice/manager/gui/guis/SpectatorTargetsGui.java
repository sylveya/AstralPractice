package dev.lokspel.practice.manager.gui.guis;

import dev.lokspel.practice.config.ItemConfig;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.enums.TeamEnum;
import dev.lokspel.practice.manager.fight.match.interfaces.Team;
import dev.lokspel.practice.manager.fight.util.stats.Statistic;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
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

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getSpectatorTargets().getIcons().getFillerItem())
        .get();
  }

  private static ItemStack fillerItem2() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getSpectatorTargets().getIcons().getFillerItem2())
        .get();
  }

  private static final int COLUMNS_PER_ROW = 9;
  private static final int TEAM_COLUMNS = 4;
  private static final int DIVIDER_COLUMN = 4;
  private static final int TEAM1_BANNER_SLOT = 1;
  private static final int TEAM2_BANNER_SLOT = 7;
  private static final int MAX_PLAYER_ROWS = 4;
  private static final int TEAM_CAPACITY = MAX_PLAYER_ROWS * TEAM_COLUMNS;
  private static final int FFA_CAPACITY = MAX_PLAYER_ROWS * COLUMNS_PER_ROW;

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
    boolean teamMatch = match instanceof Team;
    List<Player> players = match.getPlayers();
    List<Player> firstTeam = teamMatch ? getTeamPlayers(TeamEnum.TEAM1) : List.of();
    List<Player> secondTeam = teamMatch ? getTeamPlayers(TeamEnum.TEAM2) : List.of();

    int capacity = teamMatch ? TEAM_CAPACITY : FFA_CAPACITY;
    int playersOnLargestPage =
        teamMatch ? Math.max(firstTeam.size(), secondTeam.size()) : players.size();
    int maxPage = PageUtil.getMaxPage(playersOnLargestPage, capacity);

    int columnsPerTeam = teamMatch ? TEAM_COLUMNS : COLUMNS_PER_ROW;
    int playerRows =
        Math.clamp(
            (playersOnLargestPage + columnsPerTeam - 1) / columnsPerTeam, 1, MAX_PLAYER_ROWS);
    int totalRows = playerRows + 2;
    int totalSlots = totalRows * COLUMNS_PER_ROW;

    Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
    Map<Integer, Inventory> newGui = new HashMap<>();
    Map<Integer, Map<Integer, Player>> newPageSlots = new HashMap<>();

    for (int page = 1; page <= maxPage; page++) {
      Inventory inventory = existingInventories.get(page);
      if (inventory == null || inventory.getSize() != totalSlots) {
        inventory = InventoryUtil.createInventory(getTitle(page), totalRows);
      } else {
        inventory.clear();
      }
      newGui.put(page, inventory);

      fillBorders(inventory, totalRows);

      Map<Integer, Player> currentSlots = new HashMap<>();
      if (teamMatch) {
        fillPage(inventory, currentSlots, firstTeam, page, 0, TEAM_COLUMNS, playerRows);
        fillPage(
            inventory,
            currentSlots,
            secondTeam,
            page,
            DIVIDER_COLUMN + 1,
            TEAM_COLUMNS,
            playerRows);
        fillDivider(inventory, totalRows);
        fillBanners(inventory, firstTeam, secondTeam);
      } else {
        fillPage(inventory, currentSlots, players, page, 0, COLUMNS_PER_ROW, playerRows);
      }
      newPageSlots.put(page, currentSlots);

      fillNavigation(inventory, page, maxPage, totalRows);
    }

    updatePages(newGui, pageSlots, newPageSlots);
  }

  private void fillPage(
      Inventory inventory,
      Map<Integer, Player> slots,
      List<Player> players,
      int page,
      int startColumn,
      int columnsPerRow,
      int playerRows) {
    int capacity = columnsPerRow * playerRows;
    int startIndex = (page - 1) * capacity;
    int endIndex = Math.min(startIndex + capacity, players.size());

    for (int index = startIndex; index < endIndex; index++) {
      int offset = index - startIndex;
      int row = 1 + (offset / columnsPerRow);
      int slot = row * COLUMNS_PER_ROW + startColumn + (offset % columnsPerRow);

      Player target = players.get(index);
      slots.put(slot, target);
      inventory.setItem(slot, getTargetItem(target));
    }
  }

  private void fillBorders(Inventory inventory, int totalRows) {
    ItemStack filler = fillerItem();
    for (int col = 0; col < COLUMNS_PER_ROW; col++) {
      inventory.setItem(col, filler);
    }
    int bottomRowStart = (totalRows - 1) * COLUMNS_PER_ROW;
    for (int col = 0; col < COLUMNS_PER_ROW; col++) {
      inventory.setItem(bottomRowStart + col, filler);
    }
  }

  private void fillDivider(Inventory inventory, int totalRows) {
    ItemStack divider = fillerItem2();
    for (int row = 0; row < totalRows; row++) {
      inventory.setItem(row * COLUMNS_PER_ROW + DIVIDER_COLUMN, divider);
    }
  }

  private void fillBanners(Inventory inventory, List<Player> firstTeam, List<Player> secondTeam) {
    inventory.setItem(
        TEAM1_BANNER_SLOT, getBannerItem(TeamEnum.TEAM1, countAlive(firstTeam), firstTeam.size()));
    inventory.setItem(
        TEAM2_BANNER_SLOT,
        getBannerItem(TeamEnum.TEAM2, countAlive(secondTeam), secondTeam.size()));
  }

  private void fillNavigation(Inventory inventory, int page, int maxPage, int totalRows) {
    int bottomRowStart = (totalRows - 1) * COLUMNS_PER_ROW;

    if (page > 1) {
      ItemStack previous =
          getNavigationItem(
              PracticeConfig.guis().getGuis().getSpectatorTargets().getIcons().getPageLeft(),
              page - 1);
      inventory.setItem(bottomRowStart, previous);
    }

    if (page < maxPage) {
      ItemStack next =
          getNavigationItem(
              PracticeConfig.guis().getGuis().getSpectatorTargets().getIcons().getPageRight(),
              page + 1);
      inventory.setItem(bottomRowStart + COLUMNS_PER_ROW - 1, next);
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

    List<String> lore = new ArrayList<>(configItem.getLore());
    if (!(match instanceof Team)) {
      lore.removeIf(line -> line.contains("%team%"));
    }
    headItem.setLore(lore);

    double health = target.isOnline() && !target.isDead() ? Math.max(0.0, target.getHealth()) : 0.0;

    return headItem
        .replace("%player%", target.getName())
        .replace("%team%", getTeam(target).getNameMm())
        .replace("%health%", String.format("%.1f", health))
        .get();
  }

  private ItemStack getBannerItem(TeamEnum team, int alive, int total) {
    ItemConfig config =
        team == TeamEnum.TEAM1
            ? PracticeConfig.guis().getGuis().getSpectatorTargets().getIcons().getTeam1Banner()
            : PracticeConfig.guis().getGuis().getSpectatorTargets().getIcons().getTeam2Banner();

    return BackendUtil.getGuiItem(config)
        .replace("%team%", team.getNameMm())
        .replace("%alive%", String.valueOf(alive))
        .replace("%total%", String.valueOf(total))
        .get();
  }

  private boolean isAlive(Player player) {
    if (!player.isOnline() || !match.getPlayers().contains(player)) {
      return false;
    }
    Statistic stat = match.getCurrentStat(player);
    return stat == null || !stat.isSet();
  }

  private int countAlive(List<Player> teamPlayers) {
    int alive = 0;
    for (Player p : teamPlayers) {
      if (isAlive(p)) {
        alive++;
      }
    }
    return alive;
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

    if (slot < 0 || slot >= inventory.getSize()) {
      return;
    }

    int page = inGuiPlayers.getOrDefault(spectator, 1);
    int lastRowStart = inventory.getSize() - COLUMNS_PER_ROW;

    if (slot == lastRowStart) {
      if (page > 1) {
        open(spectator, page - 1);
      }
      return;
    }

    if (slot == inventory.getSize() - 1) {
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
