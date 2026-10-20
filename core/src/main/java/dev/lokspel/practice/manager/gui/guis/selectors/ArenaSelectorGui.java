package dev.lokspel.practice.manager.gui.guis.selectors;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.duel.DuelManager;
import dev.lokspel.practice.manager.duel.DuelRequest;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.enums.MatchType;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.util.LadderUtil;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.party.matchrequest.PartyRequest;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.PageUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ArenaSelectorGui extends MatchStarterGui {

  private static ItemStack backToItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSelectors()
                .getArenaSelector()
                .getIcons()
                .getBackToSelector())
        .get();
  }

  private static ItemStack randomArenaItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSelectors()
                .getArenaSelector()
                .getIcons()
                .getRandomArena())
        .get();
  }

  private final int spaces = 45;
  private final Map<Integer, Map<Integer, Arena>> arenaIcons = new HashMap<>();

  public ArenaSelectorGui(Ladder ladder, MatchType matchType, Gui backTo) {
    super(GuiType.Arena_Selector, matchType, ladder, backTo);

    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    List<Arena> arenas = new ArrayList<>(ladder.getAvailableArenas());
    arenas.sort(Comparator.comparing(Arena::getName, String::compareToIgnoreCase));

    Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
    Map<Integer, Inventory> newGui = new HashMap<>();
    Map<Integer, Map<Integer, Arena>> newArenaIcons = new HashMap<>();

    for (int page = 1; PageUtil.isPageValid(arenas.size(), page, spaces) || page == 1; page++) {
      Inventory inventory = existingInventories.get(page);
      String title =
          PracticeConfig.guis()
              .getGuis()
              .getSelectors()
              .getArenaSelector()
              .getTitle()
              .replace("%matchType%", this.matchType.getName(false))
              .replace("%page%", String.valueOf(page));
      if (inventory == null || inventory.getSize() != 6 * 9) {
        inventory = InventoryUtil.createInventory(title, 6);
      } else {
        inventory.clear();
      }
      newGui.put(page, inventory);

      // Frame
      for (int i : new int[] {45, 46, 47, 48, 49, 50, 51, 52, 53}) {
        inventory.setItem(i, GuiManager.fillerItem());
      }

      Map<Integer, Arena> pageSlots = new HashMap<>();
      int startIndex = (page - 1) * spaces;
      int endIndex = Math.min(startIndex + spaces, arenas.size());
      for (int i = startIndex; i < endIndex; i++) {
        int slot = inventory.firstEmpty();
        if (slot == -1 || slot >= 45) {
          break;
        }

        Arena arena = arenas.get(i);
        List<String> lore = new ArrayList<>();
        for (String line :
            PracticeConfig.guis()
                .getGuis()
                .getSelectors()
                .getArenaSelector()
                .getIcons()
                .getArenaIcon()
                .getLore()) {
          lore.add(line.replace("%arena%", arena.getDisplayName()));
        }

        ItemStack icon =
            ItemCreateUtil.createItem(
                arena.getIcon(),
                PracticeConfig.guis()
                    .getGuis()
                    .getSelectors()
                    .getArenaSelector()
                    .getIcons()
                    .getArenaIcon()
                    .getName()
                    .replace("%arena%", arena.getDisplayName()),
                lore);

        inventory.setItem(slot, icon);
        pageSlots.put(slot, arena);
      }
      newArenaIcons.put(page, pageSlots);

      ItemStack left =
          page == 1
              ? backToItem()
              : BackendUtil.getGuiItem(
                      PracticeConfig.guis()
                          .getGuis()
                          .getSelectors()
                          .getArenaSelector()
                          .getIcons()
                          .getPageLeft())
                  .replace("%page%", String.valueOf(page - 1))
                  .get();
      inventory.setItem(45, left);

      inventory.setItem(49, randomArenaItem());

      ItemStack right =
          PageUtil.isPageValid(arenas.size(), page + 1, spaces)
              ? BackendUtil.getGuiItem(
                      PracticeConfig.guis()
                          .getGuis()
                          .getSelectors()
                          .getArenaSelector()
                          .getIcons()
                          .getPageRight())
                  .replace("%page%", String.valueOf(page + 1))
                  .get()
              : GuiManager.fillerItem();
      inventory.setItem(53, right);
    }

    updatePages(newGui, arenaIcons, newArenaIcons);
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    final Party party = PartyManager.getInstance().getParty(player);
    Inventory inventory = e.getView().getTopInventory();
    int slot = e.getRawSlot();
    ItemStack currentItem = e.getCurrentItem();

    e.setCancelled(true);

    if (inventory.getSize() <= slot) {
      return;
    }
    if (currentItem == null) {
      return;
    }
    if (currentItem.equals(GuiManager.fillerItem())) {
      return;
    }

    int page = inGuiPlayers.getOrDefault(player, 1);

    if (slot == 45) {
      if (page > 1 && gui.containsKey(page - 1)) {
        open(player, page - 1);
      } else if (page == 1) {
        backTo.open(player);
      }
      return;
    }

    if (slot == 53) {
      if (gui.containsKey(page + 1)) {
        open(player, page + 1);
      }
      return;
    }

    if (!ladder.isEnabled() || !ladder.getMatchTypes().contains(matchType)) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getArena().getSelector().getLadderNotAvailable());
      backTo.update();
      backTo.open(player);
      return;
    }

    if (ladder instanceof NormalLadder && ((NormalLadder) ladder).isFrozen()) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getArena().getSelector().getLadderFrozen());
      backTo.open(player);
      return;
    }

    /*
     * Duel games arena selector
     */
    if (party == null) {
      Player target = DuelManager.getInstance().getPendingRequestTarget().get(player);

      if (!target.isOnline()) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getArena().getSelector().getDuel().getTargetLeft());
        player.closeInventory();
        return;
      }

      if (slot == 49) {
        if (player.hasPermission("ap.duel.selectrounds")) {
          new DuelRoundSelectorGui(matchType, ladder, null, this, null).open(player);
        } else {
          // Send the duel request
          DuelManager.getInstance()
              .sendRequest(new DuelRequest(player, target, ladder, null, ladder.getRounds()));
          player.closeInventory();
        }
      } else {
        if (!arenaIcons.containsKey(page) || !arenaIcons.get(page).containsKey(slot)) {
          return;
        }

        Arena arena = arenaIcons.get(page).get(slot);
        if (arena.getAvailableArena() == null) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language().getArena().getSelector().getDuel().getArenaNotAvailable());
          update();
          return;
        }

        if (player.hasPermission("ap.duel.selectrounds")) {
          new DuelRoundSelectorGui(matchType, ladder, arena, this, null).open(player);
        } else {
          // Send the duel request
          DuelManager.getInstance()
              .sendRequest(new DuelRequest(player, target, ladder, arena, ladder.getRounds()));
        }
      }
    }
    /*
     * Party games arena selector
     */
    else {
      /*
       * Own party game arena selector
       */
      if (!this.matchType.equals(MatchType.PARTY_VS_PARTY)) {
        if (party.getMembers().size() < 2) {
          player.closeInventory();
          Common.sendMmMessage(
              player,
              PracticeConfig.language().getArena().getSelector().getParty().getNotEnoughPlayers());
          return;
        }

        Arena arena;
        if (slot == 49) {
          arena = LadderUtil.getAvailableArena(ladder);

          if (arena == null) {
            Common.sendMmMessage(
                player,
                PracticeConfig.language()
                    .getArena()
                    .getSelector()
                    .getParty()
                    .getNoAvailableArena());
            return;
          }
        } else if (arenaIcons.containsKey(page) && arenaIcons.get(page).containsKey(slot)) {
          arena = arenaIcons.get(page).get(slot);
        } else {
          return;
        }

        if (arena.getAvailableArena() == null) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language().getArena().getSelector().getParty().getArenaNotAvailable());
          update();
          return;
        }

        if (player.hasPermission("ap.party.selectrounds")) {
          new DuelRoundSelectorGui(matchType, ladder, arena, this, party).open(player);
        } else {
          if (openPartySplitGui(player, party, arena, ladder.getRounds())) {
            return;
          }

          Match match = getMatch(party, arena, ladder.getRounds());
          if (match == null) {
            Common.sendMmMessage(
                player, PracticeConfig.language().getArena().getSelector().getParty().getError());
            return;
          }

          party.setMatch(match);
          match.startMatch();
        }
      }
      /*
       * Party vs party game arena selector
       */
      else {
        Party target =
            PartyManager.getInstance().getRequestManager().getPendingRequestTarget().get(party);

        if (!PartyManager.getInstance().getParties().contains(target)) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language()
                  .getArena()
                  .getSelector()
                  .getParty()
                  .getTargetPartyDisbanded());
          GuiManager.getInstance().searchGui(GuiType.Party_OtherParties).open(player);
          return;
        }

        if (slot == 49) {
          player.closeInventory();

          if (player.hasPermission("ap.party.selectrounds")) {
            new DuelRoundSelectorGui(matchType, ladder, null, this, party).open(player);
          } else {
            PartyRequest partyRequest =
                new PartyRequest(party, target, ladder, null, ladder.getRounds());
            partyRequest.sendRequest();
          }
        } else {
          if (!arenaIcons.containsKey(page) || !arenaIcons.get(page).containsKey(slot)) {
            return;
          }

          Arena arena = arenaIcons.get(page).get(slot);

          if (arena.getAvailableArena() == null) {
            Common.sendMmMessage(
                player,
                PracticeConfig.language()
                    .getArena()
                    .getSelector()
                    .getParty()
                    .getArenaCurrentlyNotAvailable());
            update();
            return;
          }

          if (player.hasPermission("ap.party.selectrounds")) {
            new DuelRoundSelectorGui(matchType, ladder, arena, this, party).open(player);
          } else {
            PartyRequest partyRequest =
                new PartyRequest(party, target, ladder, arena, ladder.getRounds());
            partyRequest.sendRequest();
          }
        }
      }
    }
  }
}
