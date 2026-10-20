package dev.lokspel.practice.manager.gui.guis;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.gui.Guis;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.matchhistory.MatchHistoryEntry;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.TimeUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MatchHistoryGui extends Gui {

  private final UUID targetUuid;
  private final String targetName;
  private final List<MatchHistoryEntry> entries;

  public MatchHistoryGui(UUID targetUuid, String targetName, List<MatchHistoryEntry> entries) {
    super(GuiType.MatchHistory_Gui);
    this.targetUuid = targetUuid;
    this.targetName = targetName;
    this.entries = entries;
    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    Guis.MatchHistory config = PracticeConfig.guis().getGuis().getMatchHistory();
    String title =
        Objects.requireNonNullElse(
                config.getTitle(), "<dark_gray>Match History <gray>- <gold>%player%")
            .replace("%player%", targetName);

    int size = config.getSize();
    if (size < 9 || size > 54 || size % 9 != 0) {
      size = 27;
    }

    Inventory inventory = InventoryUtil.createInventory(title, size / 9);

    GuiItem fillerItem = BackendUtil.getGuiItem(config.getFillerItem());
    ItemStack filler = fillerItem.get();
    if (filler != null) {
      for (int i = 0; i < size; i++) {
        inventory.setItem(i, filler);
      }
    }

    int startSlot =
        config.getStartSlot() >= 0
            ? config.getStartSlot()
            : (config.isCenterItems()
                ? InventoryUtil.getCenteredStartSlot(size, entries.size())
                : 0);

    for (int i = 0; i < entries.size(); i++) {
      int slot = startSlot + i;
      if (slot >= size) {
        break;
      }
      inventory.setItem(slot, buildMatchItem(entries.get(i), config));
    }

    gui.put(1, inventory);
  }

  private ItemStack buildMatchItem(MatchHistoryEntry entry, Guis.MatchHistory config) {
    boolean isTarget = entry.getPlayerUuid().equals(targetUuid);
    String opponentName = isTarget ? entry.getOpponentName() : entry.getPlayerName();
    UUID opponentUuid = isTarget ? entry.getOpponentUuid() : entry.getPlayerUuid();

    double myHealth = isTarget ? entry.getPlayerFinalHealth() : entry.getOpponentFinalHealth();
    double oppHealth = isTarget ? entry.getOpponentFinalHealth() : entry.getPlayerFinalHealth();
    int myScore = isTarget ? entry.getPlayerScore() : entry.getOpponentScore();
    int oppScore = isTarget ? entry.getOpponentScore() : entry.getPlayerScore();

    boolean won = entry.isWinner(targetUuid);
    boolean draw = entry.getWinnerUuid() == null;

    Guis.MatchHistory.Messages messages = config.getMessages();
    String result =
        won
            ? Objects.requireNonNullElse(messages.getWin(), "<green>Win")
            : draw
                ? Objects.requireNonNullElse(messages.getDraw(), "<yellow>Equality")
                : Objects.requireNonNullElse(messages.getLoss(), "<red>Loss");

    Guis.MatchHistory.MatchItem matchItemConfig = config.getMatchItem();
    GuiItem guiItem;
    if (matchItemConfig.isUsePlayerHead()) {
      OfflinePlayer opponent = Bukkit.getOfflinePlayer(opponentUuid);
      guiItem = new GuiItem(ItemCreateUtil.getPlayerHead(opponent));
      guiItem.setName(matchItemConfig.getName());
      guiItem.setLore(matchItemConfig.getLore());
    } else {
      guiItem = BackendUtil.getGuiItem(matchItemConfig).cloneItem();
    }

    if (matchItemConfig.isGlowOnWin() && won) {
      guiItem.setGlowing(true);
    }

    return guiItem
        .replace("%opponent%", opponentName)
        .replace("%result%", result)
        .replace("%score%", myScore + " - " + oppScore)
        .replace("%kit%", entry.getKitName())
        .replace("%arena%", entry.getArenaName())
        .replace("%player_health%", String.format("%.1f❤", myHealth / 2.0))
        .replace("%opponent_health%", String.format("%.1f❤", oppHealth / 2.0))
        .replace("%duration%", entry.getFormattedDuration())
        .replace("%date%", TimeUtil.formatDate(entry.getPlayedAt()))
        .get();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    e.setCancelled(true);
  }
}
