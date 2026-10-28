package dev.lokspel.practice.manager.gui.guis.party;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.StringUtil;
import dev.lokspel.practice.util.cooldown.CooldownObject;
import dev.lokspel.practice.util.cooldown.PlayerCooldown;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PartySettingsGui extends Gui {

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getParty()
                .getPartySettings()
                .getIcons()
                .getFillerItem())
        .get();
  }

  private final Party party;

  public PartySettingsGui(Party party) {
    super(GuiType.Party_Settings);
    this.party = party;

    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis().getGuis().getParty().getPartySettings().getTitle(), 3));

    build();
  }

  @Override
  public void build() {
    for (int i = 0; i < gui.get(1).getSize(); i++) {
      gui.get(1).setItem(i, fillerItem());
    }

    update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);

    if (party.isDuelRequests()) {
      inventory.setItem(
          10,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getParty()
                      .getPartySettings()
                      .getIcons()
                      .getDuelRequests()
                      .getEnabled())
              .get());
    } else {
      inventory.setItem(
          10,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getParty()
                      .getPartySettings()
                      .getIcons()
                      .getDuelRequests()
                      .getDisabled())
              .get());
    }

    inventory.setItem(11, getPlayerLimitItem(party.getMaxPlayerLimit()));

    if (party.isPartyChat()) {
      inventory.setItem(
          12,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getParty()
                      .getPartySettings()
                      .getIcons()
                      .getPartyChat()
                      .getEnabled())
              .get());
    } else {
      inventory.setItem(
          12,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getParty()
                      .getPartySettings()
                      .getIcons()
                      .getPartyChat()
                      .getDisabled())
              .get());
    }

    if (party.isAllInvite()) {
      inventory.setItem(
          14,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getParty()
                      .getPartySettings()
                      .getIcons()
                      .getAllInvite()
                      .getEnabled())
              .get());
    } else {
      inventory.setItem(
          14,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getParty()
                      .getPartySettings()
                      .getIcons()
                      .getAllInvite()
                      .getDisabled())
              .get());
    }

    if (party.isPublicParty()) {
      inventory.setItem(
          15,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getParty()
                      .getPartySettings()
                      .getIcons()
                      .getAccessModifiers()
                      .getPublicValue())
              .get());
    } else {
      inventory.setItem(
          15,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getParty()
                      .getPartySettings()
                      .getIcons()
                      .getAccessModifiers()
                      .getPrivateValue())
              .get());
    }

    if (party.isBroadcastParty()) {
      inventory.setItem(
          16,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getParty()
                      .getPartySettings()
                      .getIcons()
                      .getBroadcast()
                      .getEnabled())
              .get());
    } else {
      inventory.setItem(
          16,
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getParty()
                      .getPartySettings()
                      .getIcons()
                      .getBroadcast()
                      .getDisabled())
              .get());
    }

    this.updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    Party party = PartyManager.getInstance().getParty(player);

    final ClickType clickType = e.getClick();
    ItemStack currentItem = e.getCurrentItem();
    final int slot = e.getRawSlot();
    e.setCancelled(true);

    if (party == null) {
      return;
    }
    if (currentItem == null) {
      return;
    }

    switch (slot) {
      case 10:
        if (player.hasPermission("ap.party.duelrequest")) {
          party.setDuelRequests(!party.isDuelRequests());
          update();
        } else {
          Common.sendMmMessage(player, PracticeConfig.language().getParty().getNoPermission());
        }
        break;
      case 11:
        if (player.hasPermission("ap.party.changelimit")) {
          int groupPartyLimit =
              PartyManager.getInstance().resolvePartyMemberLimit(party.getLeader());
          if (clickType.isLeftClick() && party.getMaxPlayerLimit() > 2) {
            if (party.getMembers().size() < party.getMaxPlayerLimit()) {
              party.setMaxPlayerLimit(party.getMaxPlayerLimit() - 1);
              update();
            } else {
              Common.sendMmMessage(
                  player, PracticeConfig.language().getParty().getCantDecreaseLimit());
            }
          } else if (clickType.isRightClick() && party.getMaxPlayerLimit() < groupPartyLimit) {
            party.setMaxPlayerLimit(party.getMaxPlayerLimit() + 1);
            GuiManager.getInstance().searchGui(GuiType.Party_PublicParties).update();
            update();
          }
        } else {
          Common.sendMmMessage(player, PracticeConfig.language().getParty().getNoPermission());
        }
        break;
      case 12:
        if (player.hasPermission("ap.party.partychat")) {
          boolean partyChat = !party.isPartyChat();
          party.setPartyChat(partyChat);

          // Turning party chat off also turns it off for every member,
          // so nobody keeps a toggle the party no longer allows.
          if (!partyChat) {
            String disabled = PracticeConfig.language().getParty().getPartyChatDisabledByLeader();

            for (Player member : party.getMembers()) {
              if (member.equals(party.getLeader())) {
                continue;
              }

              Profile memberProfile = ProfileManager.getInstance().getProfile(member);
              if (!memberProfile.isPartyChat()) {
                continue;
              }

              memberProfile.setPartyChat(false);
              Common.sendMmMessage(member, disabled);
            }
          }

          update();
        } else {
          Common.sendMmMessage(player, PracticeConfig.language().getParty().getNoPermission());
        }
        break;
      case 14:
        if (player.hasPermission("ap.party.allinvite")) {
          party.setAllInvite(!party.isAllInvite());
          update();
        } else {
          Common.sendMmMessage(player, PracticeConfig.language().getParty().getNoPermission());
        }
        break;
      case 15:
        if (player.hasPermission("ap.party.public")) {
          if (PlayerCooldown.isActive(player, CooldownObject.PUBLIC_PARTY_CHANGE)) {
            Common.sendMmMessage(
                player,
                StringUtil.replaceSecondString(
                    PracticeConfig.language().getWaitForCooldown(),
                    PlayerCooldown.getLeftInDouble(
                        player, CooldownObject.LEADERBOARD_GUI_REFRESH)));
            return;
          }

          PlayerCooldown.addCooldown(player, CooldownObject.PUBLIC_PARTY_CHANGE, 5);

          if (party.isPublicParty() && party.isBroadcastParty()) {
            party.getBroadcastTask().cancel();
          }

          party.setPublicParty(!party.isPublicParty());
          GuiManager.getInstance().searchGui(GuiType.Party_PublicParties).update();
          update();
        } else {
          Common.sendMmMessage(player, PracticeConfig.language().getParty().getNoPermission());
        }
        break;
      case 16:
        if (player.hasPermission("ap.party.broadcast")) {
          if (party.isBroadcastParty()) {
            if (PlayerCooldown.isActive(player, CooldownObject.BROADCAST_PARTY_CHANGE)) {
              Common.sendMmMessage(
                  player,
                  StringUtil.replaceSecondString(
                      PracticeConfig.language().getWaitForCooldown(),
                      PlayerCooldown.getLeftInDouble(
                          player, CooldownObject.BROADCAST_PARTY_CHANGE)));
              return;
            }

            PlayerCooldown.addCooldown(player, CooldownObject.BROADCAST_PARTY_CHANGE, 5);
            party.getBroadcastTask().cancel();
            update();
          } else {
            if (party.isPublicParty()) {
              Profile leaderProfile = ProfileManager.getInstance().getProfile(player);
              if (!player.hasPermission("ap.bypass.party.broadcast.limit")
                  && leaderProfile.getPartyBroadcastLeft() == 0) {
                Common.sendMmMessage(
                    player, PracticeConfig.language().getParty().getNoBroadcastLeft());
                return;
              }
              if (!player.hasPermission("ap.bypass.party.broadcast.limit")
                  && leaderProfile.getPartyBroadcastLeft() > 0) {
                leaderProfile.setPartyBroadcastLeft(leaderProfile.getPartyBroadcastLeft() - 1);
              }

              if (PlayerCooldown.isActive(player, CooldownObject.BROADCAST_PARTY_CHANGE)) {
                Common.sendMmMessage(
                    player,
                    StringUtil.replaceSecondString(
                        PracticeConfig.language().getWaitForCooldown(),
                        PlayerCooldown.getLeftInDouble(
                            player, CooldownObject.BROADCAST_PARTY_CHANGE)));
                return;
              }

              PlayerCooldown.addCooldown(player, CooldownObject.BROADCAST_PARTY_CHANGE, 5);
              party.getBroadcastTask().begin();
              update();
            } else {
              Common.sendMmMessage(
                  player, PracticeConfig.language().getParty().getPartyNotPublic());
            }
          }
        } else {
          Common.sendMmMessage(player, PracticeConfig.language().getParty().getNoPermission());
        }
        break;

      default:
        break;
    }
  }

  public ItemStack getPlayerLimitItem(int limit) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getParty()
                .getPartySettings()
                .getIcons()
                .getPlayerLimit())
        .replace("%limit%", String.valueOf(limit))
        .get();
  }
}
