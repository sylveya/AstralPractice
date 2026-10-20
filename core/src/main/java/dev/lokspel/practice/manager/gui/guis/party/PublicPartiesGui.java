package dev.lokspel.practice.manager.gui.guis.party;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.PageUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Getter
public class PublicPartiesGui extends Gui {

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getParty()
                .getPublicParties()
                .getIcons()
                .getFillerItem())
        .get();
  }

  private final int spaces = 45;
  private final Map<Integer, Map<Integer, Party>> partySlots = new HashMap<>();

  public PublicPartiesGui() {
    super(GuiType.Party_PublicParties);

    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    List<Party> parties = new ArrayList<>();
    for (Party party : PartyManager.getInstance().getParties()) {
      if (!party.isPublicParty()) {
        continue;
      }
      if (party.getMembers().size() >= party.getMaxPlayerLimit()) {
        continue;
      }

      parties.add(party);
    }

    Map<Integer, Inventory> existingInventories = new HashMap<>(gui);
    Map<Integer, Inventory> newGui = new HashMap<>();
    Map<Integer, Map<Integer, Party>> newPartySlots = new HashMap<>();

    for (int page = 1; PageUtil.isPageValid(parties.size(), page, spaces) || page == 1; page++) {
      Inventory inventory = existingInventories.get(page);
      String title =
          PracticeConfig.guis()
              .getGuis()
              .getParty()
              .getPublicParties()
              .getTitle()
              .replace("%page%", String.valueOf(page));
      if (inventory == null || inventory.getSize() != 6 * 9) {
        inventory = InventoryUtil.createInventory(title, 6);
      } else {
        inventory.clear();
      }
      newGui.put(page, inventory);

      for (int i = 45; i < 54; i++) {
        inventory.setItem(i, fillerItem());
      }

      Map<Integer, Party> pageSlots = new HashMap<>();
      int startIndex = (page - 1) * spaces;
      int endIndex = Math.min(startIndex + spaces, parties.size());
      int slot = 0;
      for (int i = startIndex; i < endIndex; i++) {
        Party party = parties.get(i);
        inventory.setItem(slot, getPartyItem(party));
        pageSlots.put(slot, party);
        slot++;
      }

      for (int i = slot; i < spaces; i++) {
        inventory.setItem(i, fillerItem());
      }
      newPartySlots.put(page, pageSlots);

      ItemStack left =
          page == 1
              ? fillerItem()
              : BackendUtil.getGuiItem(
                      PracticeConfig.guis()
                          .getGuis()
                          .getParty()
                          .getPublicParties()
                          .getIcons()
                          .getPageLeft())
                  .replace("%page%", String.valueOf(page - 1))
                  .get();
      inventory.setItem(45, left);

      ItemStack right =
          PageUtil.isPageValid(parties.size(), page + 1, spaces)
              ? BackendUtil.getGuiItem(
                      PracticeConfig.guis()
                          .getGuis()
                          .getParty()
                          .getPublicParties()
                          .getIcons()
                          .getPageRight())
                  .replace("%page%", String.valueOf(page + 1))
                  .get()
              : fillerItem();
      inventory.setItem(53, right);
    }

    updatePages(newGui, partySlots, newPartySlots);
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    final Player player = (Player) e.getWhoClicked();
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

    int page = inGuiPlayers.getOrDefault(player, 1);
    if (slot == 45) {
      if (page > 1) {
        open(player, page - 1);
      }
    } else if (slot == 53) {
      if (gui.containsKey(page + 1)) {
        open(player, page + 1);
      }
    } else if (partySlots.containsKey(page) && partySlots.get(page).containsKey(slot)) {
      if (joinParty(player, partySlots.get(page).get(slot))) {
        player.closeInventory();
      }
    }
  }

  private boolean joinParty(Player player, Party party) {
    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (!player.hasPermission("ap.party.joinpublic")) {
      Common.sendMmMessage(player, PracticeConfig.language().getParty().getNoPermission());
      return false;
    }

    if (!profile.getStatus().equals(ProfileStatus.LOBBY)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getParty()
              .getArguments()
              .getAccept()
              .getCantJoinNow());
      return false;
    }

    if (PartyManager.getInstance().getParty(player) != null) {
      Common.sendMmMessage(player, PracticeConfig.language().getParty().getAlreadyParty());
      return false;
    }

    if (!party.isPublicParty()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getParty()
              .getArguments()
              .getAccept()
              .getNoInvite()
              .replace("%leader%", party.getLeader().getName()));
      return false;
    }

    if (party.getMembers().size() >= party.getMaxPlayerLimit()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getParty()
              .getArguments()
              .getAccept()
              .getPartyFull()
              .replace("%leader%", party.getLeader().getName()));
      return false;
    }

    profile.setSpectatorMode(false);
    party.addMember(player);
    return true;
  }

  private ItemStack getPartyItem(Party party) {
    ItemStack itemStack = ItemCreateUtil.getPlayerHead(party.getLeader());
    ItemMeta itemMeta = itemStack.getItemMeta();

    List<String> memberStrings = new ArrayList<>();
    int count = 0;
    for (Player player : party.getMembers()) {
      if (count < 6) {
        if (!party.getLeader().equals(player)) {
          memberStrings.add(
              PracticeConfig.guis()
                  .getGuis()
                  .getParty()
                  .getPublicParties()
                  .getIcons()
                  .getPartyItem()
                  .getLore()
                  .getMemberFormat()
                  .replace("%player%", player.getName()));
          count++;
        }
      } else {
        break;
      }
    }

    List<String> lore = new ArrayList<>();
    for (String line :
        PracticeConfig.guis()
            .getGuis()
            .getParty()
            .getPublicParties()
            .getIcons()
            .getPartyItem()
            .getLore()
            .getLore()) {
      if (line.contains("%members%")) {
        lore.addAll(memberStrings);
      } else {
        lore.add(
            line.replace("%size%", String.valueOf(party.getMembers().size()))
                .replace("%maxsize%", String.valueOf(party.getMaxPlayerLimit())));
      }
    }

    itemMeta.displayName(
        Common.legacyToComponent(
            PracticeConfig.guis()
                .getGuis()
                .getParty()
                .getPublicParties()
                .getIcons()
                .getPartyItem()
                .getName()
                .replace("%leader%", party.getLeader().getName())
                .replace("%partySize%", String.valueOf(party.getMembers().size()))
                .replace("%maxPartySize%", String.valueOf(party.getMaxPlayerLimit()))));
    itemMeta.lore(lore.stream().map(Common::legacyToComponent).toList());

    ItemCreateUtil.hideItemFlags(itemMeta);
    itemStack.setItemMeta(itemMeta);

    return itemStack;
  }
}
