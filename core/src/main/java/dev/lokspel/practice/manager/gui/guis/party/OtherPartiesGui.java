package dev.lokspel.practice.manager.gui.guis.party;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.match.enums.MatchType;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.guis.selectors.LadderSelectorGui;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.ItemCreateUtil;
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
public class OtherPartiesGui extends Gui {

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getParty().getOtherParties().getIcons().getFillerItem())
        .get();
  }

  private final Map<Integer, Party> partySlots = new HashMap<>();

  public OtherPartiesGui() {
    super(GuiType.Party_OtherParties);
    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis().getGuis().getParty().getOtherParties().getTitle(), 6));

    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);
    if (inventory == null) {
      return;
    }
    inventory.clear();
    partySlots.clear();

    if (PartyManager.getInstance() == null) {
      return;
    }

    for (Party party : PartyManager.getInstance().getParties()) {
      int slot = inventory.firstEmpty();

      if (slot < inventory.getSize()) {
        inventory.setItem(slot, getPartyItem(party));
        partySlots.put(slot, party);
      }
    }

    ItemStack filler = fillerItem();
    for (int i = 0; i < inventory.getSize(); i++) {
      if (inventory.getItem(i) == null) {
        inventory.setItem(i, filler);
      }
    }

    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    final Profile profile = ProfileManager.getInstance().getProfile(player);
    Party party = PartyManager.getInstance().getParty(player);

    if (party == null) {
      return;
    }

    ItemStack currentItem = e.getCurrentItem();
    int slot = e.getRawSlot();
    e.setCancelled(true);

    if (currentItem == null) {
      return;
    }
    if (!partySlots.containsKey(slot)) {
      return;
    }

    Party targetParty = partySlots.get(slot);

    if (party == targetParty) {
      Common.sendMmMessage(player, PracticeConfig.language().getParty().getCantDuelOwnParty());
      return;
    }

    if (!targetParty.isDuelRequests()) {
      Common.sendMmMessage(player, PracticeConfig.language().getParty().getCantDuelParty());
      return;
    }

    if (!PartyManager.getInstance().getRequestManager().isRequested(party, targetParty)
        || party.getLeader().hasPermission("ap.party.infiniteinvite")) {
      PartyManager.getInstance()
          .getRequestManager()
          .getPendingRequestTarget()
          .put(party, targetParty);
      new LadderSelectorGui(profile, MatchType.PARTY_VS_PARTY).open(player);
    } else {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getParty().getMatchRequestMessage().getAlreadyRequested());
    }
  }

  public ItemStack getPartyItem(Party party) {
    ItemStack itemStack = ItemCreateUtil.getPlayerHead(party.getLeader());
    ItemMeta itemMeta = itemStack.getItemMeta();

    List<String> lore = new ArrayList<>();
    List<String> memberStrings = new ArrayList<>();
    int count = 0;
    for (Player player : party.getMembers()) {
      if (count < 6) {
        if (!party.getLeader().equals(player)) {
          memberStrings.add(
              PracticeConfig.guis()
                  .getGuis()
                  .getParty()
                  .getOtherParties()
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

    for (String line :
        PracticeConfig.guis()
            .getGuis()
            .getParty()
            .getOtherParties()
            .getIcons()
            .getPartyItem()
            .getLore()
            .getLore()) {
      if (line.contains("%members%")) {
        lore.addAll(memberStrings);
      } else {
        lore.add(line);
      }
    }

    itemMeta.displayName(
        Common.legacyToComponent(
            PracticeConfig.guis()
                .getGuis()
                .getParty()
                .getOtherParties()
                .getIcons()
                .getPartyItem()
                .getName()
                .replace("%leader%", party.getLeader().getName())
                .replace("%partySize%", String.valueOf(party.getMembers().size()))));
    itemMeta.lore(lore.stream().map(Common::legacyToComponent).toList());

    ItemCreateUtil.hideItemFlags(itemMeta);
    itemStack.setItemMeta(itemMeta);

    return itemStack;
  }
}
