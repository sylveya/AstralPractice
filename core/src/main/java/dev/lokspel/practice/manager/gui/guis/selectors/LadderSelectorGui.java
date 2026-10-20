package dev.lokspel.practice.manager.gui.guis.selectors;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.duel.DuelManager;
import dev.lokspel.practice.manager.duel.DuelRequest;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.enums.MatchType;
import dev.lokspel.practice.manager.fight.match.type.duel.Duel;
import dev.lokspel.practice.manager.fight.match.type.partyffa.PartyFfa;
import dev.lokspel.practice.manager.fight.match.type.playersvsplayers.partysplit.PartySplit;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.guis.party.PartySplitGui;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.lokspel.practice.manager.ladder.util.LadderUtil;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.party.matchrequest.PartyRequest;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.util.Common;
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
import org.jetbrains.annotations.Nullable;

public class LadderSelectorGui extends Gui {

  private final Profile profile;
  private final MatchType matchType;

  private final int spaces = 45;
  private final Map<Integer, Map<Integer, Ladder>> ladderSlots = new HashMap<>();

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSelectors()
                .getLadderSelector()
                .getIcons()
                .getFillerItem())
        .get();
  }

  private static GuiItem customPlayerKitItem() {
    return BackendUtil.getGuiItem(
        PracticeConfig.guis()
            .getGuis()
            .getSelectors()
            .getLadderSelector()
            .getIcons()
            .getBaseCustomPlayerKitIcon());
  }

  private static final int CUSTOM_KIT_SLOT = 53;
  private static final int PAGE_LEFT_SLOT = 45;
  private static final int PAGE_RIGHT_SLOT = 49;

  public LadderSelectorGui(Profile profile, MatchType matchType) {
    super(GuiType.Ladder_Selector);
    this.profile = profile;
    this.matchType = matchType;

    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    List<NormalLadder> ladders = new ArrayList<>();
    for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
      if (ladder.isEnabled() && ladder.isUnranked() && ladder.getMatchTypes().contains(matchType)) {
        ladders.add(ladder);
      }
    }

    Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
    Map<Integer, Inventory> newGui = new HashMap<>();
    Map<Integer, Map<Integer, Ladder>> newLadderSlots = new HashMap<>();

    for (int page = 1; PageUtil.isPageValid(ladders.size(), page, spaces) || page == 1; page++) {
      Inventory inventory = existingInventories.get(page);
      String title =
          PracticeConfig.guis()
              .getGuis()
              .getSelectors()
              .getLadderSelector()
              .getTitle()
              .replace("%matchType%", this.matchType.getName(false));
      if (inventory == null || inventory.getSize() != 6 * 9) {
        inventory = InventoryUtil.createInventory(title, 6);
      } else {
        inventory.clear();
      }
      newGui.put(page, inventory);

      for (int i = 45; i < 54; i++) {
        inventory.setItem(i, fillerItem());
      }

      Map<Integer, Ladder> pageSlots = new HashMap<>();
      int startIndex = (page - 1) * spaces;
      int endIndex = Math.min(startIndex + spaces, ladders.size());
      for (int i = startIndex; i < endIndex; i++) {
        int slot = inventory.firstEmpty();
        if (slot == -1 || slot >= 45) {
          break;
        }

        NormalLadder ladder = ladders.get(i);
        List<String> lore = new ArrayList<>();
        for (String line :
            PracticeConfig.guis()
                .getGuis()
                .getSelectors()
                .getLadderSelector()
                .getIcons()
                .getLadder()
                .getLore()) {
          lore.add(line.replace("%ladder%", ladder.getDisplayName()));
        }

        ItemStack icon =
            ItemCreateUtil.createItem(
                ladder.getIcon(),
                PracticeConfig.guis()
                    .getGuis()
                    .getSelectors()
                    .getLadderSelector()
                    .getIcons()
                    .getLadder()
                    .getName()
                    .replace("%ladder%", ladder.getDisplayName()),
                lore);

        inventory.setItem(slot, icon);
        pageSlots.put(slot, ladder);
      }

      if (profile.getSelectedCustomLadder() != null || isPartyCustomKitSelector()) {
        GuiItem customPlayerKitItem = customPlayerKitItem().cloneItem();
        CustomLadder selectedCustomLadder = profile.getSelectedCustomLadder();
        ItemStack ladderIcon = selectedCustomLadder != null ? selectedCustomLadder.getIcon() : null;
        if (ladderIcon != null) {
          // TODO: Custom ladder icon with name
          if (customPlayerKitItem.getName() == null) {
            customPlayerKitItem.setName(customPlayerKitItem().getName());
          }

          customPlayerKitItem.setBaseItem(ladderIcon);
        }

        inventory.setItem(CUSTOM_KIT_SLOT, customPlayerKitItem.get());

        if (selectedCustomLadder != null) {
          pageSlots.put(CUSTOM_KIT_SLOT, selectedCustomLadder);
        }
      }

      newLadderSlots.put(page, pageSlots);

      ItemStack left =
          page == 1
              ? fillerItem()
              : BackendUtil.getGuiItem(
                      PracticeConfig.guis()
                          .getGuis()
                          .getSelectors()
                          .getLadderSelector()
                          .getIcons()
                          .getPageLeft())
                  .replace("%page%", String.valueOf(page - 1))
                  .get();
      inventory.setItem(PAGE_LEFT_SLOT, left);

      ItemStack right =
          PageUtil.isPageValid(ladders.size(), page + 1, spaces)
              ? BackendUtil.getGuiItem(
                      PracticeConfig.guis()
                          .getGuis()
                          .getSelectors()
                          .getLadderSelector()
                          .getIcons()
                          .getPageRight())
                  .replace("%page%", String.valueOf(page + 1))
                  .get()
              : fillerItem();
      inventory.setItem(PAGE_RIGHT_SLOT, right);
    }

    updatePages(newGui, ladderSlots, newLadderSlots);
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    final Party party = PartyManager.getInstance().getParty(player);
    Inventory inventory = e.getView().getTopInventory();

    int slot = e.getRawSlot();
    ItemStack currentItem = e.getCurrentItem();
    final boolean customKitButtonClick = isPartyCustomKitButton(slot);

    e.setCancelled(true);

    if (inventory.getSize() <= slot) {
      return;
    }
    if (currentItem == null) {
      return;
    }
    if (currentItem.equals(fillerItem())) {
      return;
    }

    int page = inGuiPlayers.getOrDefault(player, 1);
    if (slot == PAGE_LEFT_SLOT) {
      if (page > 1 && gui.containsKey(page - 1)) {
        open(player, page - 1);
      }
    } else if (slot == PAGE_RIGHT_SLOT) {
      if (gui.containsKey(page + 1)) {
        open(player, page + 1);
      }
    } else if (customKitButtonClick && profile.getSelectedCustomLadder() == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getLadder().getSelector().getParty().getNoCustomKitSelected());
    } else if (ladderSlots.containsKey(page) && ladderSlots.get(page).containsKey(slot)) {
      Ladder ladder = ladderSlots.get(page).get(slot);

      if (ladder instanceof NormalLadder) {
        if (!ladder.isEnabled() || !ladder.getMatchTypes().contains(matchType)) {
          Common.sendMmMessage(
              player, PracticeConfig.language().getLadder().getSelector().getLadderNotAvailable());
          update();
          return;
        } else if (((NormalLadder) ladder).isFrozen()) {
          Common.sendMmMessage(
              player, PracticeConfig.language().getLadder().getSelector().getLadderFrozen());
          return;
        }
      } else if (ladder instanceof CustomLadder) {
        if (!ladder.isEnabled()) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language().getLadder().getSelector().getCustomLadderNotReady());
          return;
        } else if (!ladder.getMatchTypes().contains(matchType)) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language()
                  .getLadder()
                  .getSelector()
                  .getCustomLadderCantPlayMatchtype());
          return;
        }
      }

      /*
       * Duel games ladder selector
       */
      if (party == null) {
        if (player.hasPermission("ap.duel.selectarena")) {
          new ArenaSelectorGui(ladder, matchType, this).open(player);
        } else if (player.hasPermission("ap.duel.selectrounds")) {
          new DuelRoundSelectorGui(matchType, ladder, null, this, null).open(player);
        } else {
          Player target = DuelManager.getInstance().getPendingRequestTarget().get(player);
          DuelRequest request = new DuelRequest(player, target, ladder, null, ladder.getRounds());

          if (target.isOnline()) {
            DuelManager.getInstance().sendRequest(request);
          } else {
            Common.sendMmMessage(
                player,
                PracticeConfig.language().getLadder().getSelector().getDuel().getTargetLeft());
            player.closeInventory();
          }
        }
      }
      /*
       * Party games ladder selector
       */
      else {
        /*
         * Own party game ladder selector
         */
        if (!this.matchType.equals(MatchType.PARTY_VS_PARTY)) {
          if (party.getMembers().size() < 2) {
            player.closeInventory();
            Common.sendMmMessage(
                player,
                PracticeConfig.language()
                    .getLadder()
                    .getSelector()
                    .getParty()
                    .getNotEnoughPlayers());
            return;
          }

          if (customKitButtonClick && ladder instanceof CustomLadder) {
            startPartyMatch(player, party, ladder, ladder.getRounds());
            return;
          }

          if (player.hasPermission("ap.party.selectarena")) {
            new ArenaSelectorGui(ladder, matchType, this).open(player);
          } else if (player.hasPermission("ap.party.selectrounds")) {
            new DuelRoundSelectorGui(matchType, ladder, null, this, party).open(player);
          } else {
            startPartyMatch(player, party, ladder, ladder.getRounds());
          }
        }
        /*
         * Party vs party ladder selector
         */
        else {
          Party target =
              PartyManager.getInstance().getRequestManager().getPendingRequestTarget().get(party);

          if (!PartyManager.getInstance().getParties().contains(target)) {
            Common.sendMmMessage(
                player,
                PracticeConfig.language()
                    .getLadder()
                    .getSelector()
                    .getParty()
                    .getTargetPartyDisbanded());
            GuiManager.getInstance().searchGui(GuiType.Party_OtherParties).open(player);
            return;
          }

          if (player.hasPermission("ap.party.selectarena")) {
            new ArenaSelectorGui(ladder, matchType, this).open(player);
          } else if (player.hasPermission("ap.party.selectrounds")) {
            new DuelRoundSelectorGui(matchType, ladder, null, this, party).open(player);
          } else {
            player.closeInventory();

            Arena arena = LadderUtil.getAvailableArena(ladder);
            if (arena == null) {
              Common.sendMmMessage(
                  player,
                  PracticeConfig.language()
                      .getLadder()
                      .getSelector()
                      .getParty()
                      .getNoAvailableArena());
              return;
            }

            // Send the game request
            PartyRequest partyRequest =
                new PartyRequest(party, target, ladder, arena, ladder.getRounds());
            partyRequest.sendRequest();
          }
        }
      }
    }
  }

  @Nullable
  private Match getMatch(Party party, Ladder ladder, Arena arena, int rounds) {
    Match match = null;

    if (party.getMembers().size() == 2) {
      match = new Duel(ladder, arena, party.getMembers(), false, rounds);
    } else {
      if (matchType.equals(MatchType.PARTY_FFA)) {
        match = new PartyFfa(ladder, arena, party, rounds);
      } else if (matchType.equals(MatchType.PARTY_SPLIT)) {
        match = new PartySplit(ladder, arena, party, rounds);
      }
    }
    return match;
  }

  private boolean isPartyCustomKitSelector() {
    return matchType.equals(MatchType.PARTY_FFA) || matchType.equals(MatchType.PARTY_SPLIT);
  }

  private boolean isPartyCustomKitButton(int slot) {
    return isPartyCustomKitSelector() && slot == CUSTOM_KIT_SLOT;
  }

  private void startPartyMatch(Player player, Party party, Ladder ladder, int rounds) {
    Arena arena = LadderUtil.getAvailableArena(ladder);
    if (arena == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getLadder().getSelector().getParty().getNoAvailableArena());
      return;
    }

    if (matchType.equals(MatchType.PARTY_SPLIT) && party.getMembers().size() > 2) {
      new PartySplitGui(party, ladder, arena, rounds, this).open(player);
      return;
    }

    player.closeInventory();

    Match match = getMatch(party, ladder, arena, rounds);
    if (match == null) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getLadder().getSelector().getParty().getError());
      return;
    }

    party.setMatch(match);
    match.startMatch();
  }
}
