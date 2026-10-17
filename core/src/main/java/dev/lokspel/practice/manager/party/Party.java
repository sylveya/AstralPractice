package dev.lokspel.practice.manager.party;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.enums.MatchStatus;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.guis.party.PartySettingsGui;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.playerutil.PlayerUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

@Getter
public class Party implements dev.lokspel.api.interfaces.Party {

  private static final boolean DEFAULT_PUBLIC_PARTY =
      PracticeConfig.settings().getParty().getSettings().getDefaultValue().isPublicParty();
  private static final boolean DEFAULT_ALL_INVITE =
      PracticeConfig.settings().getParty().getSettings().getDefaultValue().isAllInvite();
  private static final boolean DEFAULT_PARTY_CHAT =
      PracticeConfig.settings().getParty().getSettings().getDefaultValue().isPartyChat();
  private static final boolean DEFAULT_DUEL_REQUEST =
      PracticeConfig.settings().getParty().getSettings().getDefaultValue().isDuelRequests();

  @Setter private Player leader;
  private final List<Player> members = new ArrayList<>();
  private final Map<Player, Long> invites = new HashMap<>();

  @Setter private BroadcastTask broadcastTask = new BroadcastTask(this);
  private PartySettingsGui partySettingsGui;

  @Setter private int maxPlayerLimit;
  @Setter private boolean publicParty;
  @Setter private boolean broadcastParty;
  @Setter private boolean allInvite;
  @Setter private boolean partyChat;
  @Setter private boolean duelRequests;

  private Match match;

  /**
   * Sets the party's current match.
   *
   * <p>If the party already had a match that is still running, it is ended first. Otherwise the old
   * match would stay registered among the live matches (inflating the "in fights" count) and its
   * arena would never roll back.
   *
   * @param match the new match, or null to clear the current one
   */
  public void setMatch(Match match) {
    if (match != null
        && this.match != null
        && !this.match.equals(match)
        && PartyManager.getInstance().hasActiveMatch(this)
        && this.match.getStatus() != MatchStatus.END
        && this.match.getStatus() != MatchStatus.OVER) {

      this.match.sendMessage(PracticeConfig.language().getParty().getPreviousMatchEnded(), true);
      this.match.endMatch();
    }

    this.match = match;
  }

  public Party(Player owner) {
    this.leader = owner;
    this.members.add(owner);

    this.maxPlayerLimit = PartyManager.getInstance().resolvePartyMemberLimit(owner);
    this.publicParty = DEFAULT_PUBLIC_PARTY;
    this.broadcastParty = DEFAULT_PUBLIC_PARTY;
    this.allInvite = DEFAULT_ALL_INVITE;
    this.partyChat = DEFAULT_PARTY_CHAT;
    this.duelRequests = DEFAULT_DUEL_REQUEST;

    this.partySettingsGui =
        (PartySettingsGui) GuiManager.getInstance().addGui(new PartySettingsGui(this));
  }

  /** Re-creates the settings GUI from the reloaded configuration. */
  public void reloadSettingsGui() {
    this.partySettingsGui =
        (PartySettingsGui) GuiManager.getInstance().addGui(new PartySettingsGui(this));
  }

  public void setNewOwner(Player newOwner) {
    if (broadcastTask.isRunning()) {
      broadcastTask.cancel();
    }

    leader = newOwner;
    refreshMaxPlayerLimitForLeader();
    sendMessage(
        PracticeConfig.language()
            .getParty()
            .getNewLeader()
            .replace("%player%", newOwner.getName()));
  }

  public void refreshMaxPlayerLimitForLeader() {
    int leaderLimit = PartyManager.getInstance().resolvePartyMemberLimit(leader);
    this.maxPlayerLimit = Math.max(members.size(), leaderLimit);

    if (members.size() >= this.maxPlayerLimit && isBroadcastParty()) {
      broadcastTask.cancel();
    }
  }

  public void addMember(Player member) {
    Profile memberProfile = ProfileManager.getInstance().getProfile(member);

    members.add(member);
    memberProfile.setParty(true);
    InventoryManager.getInstance().setLobbyInventory(member, false);

    sendMessage(
        PracticeConfig.language()
            .getParty()
            .getPlayerJoined()
            .replace("%player%", member.getName()));
    GuiManager.getInstance().searchGui(GuiType.Party_OtherParties).update();
    GuiManager.getInstance().searchGui(GuiType.Party_PublicParties).update();

    // If the party is full, stop broadcasting it.
    if (members.size() == maxPlayerLimit && isBroadcastParty()) {
      broadcastTask.cancel();
    }
  }

  public void removeMember(Player member, boolean kick) {
    final Profile memberProfile = ProfileManager.getInstance().getProfile(member);

    if (kick) {
      sendMessage(
          PracticeConfig.language()
              .getParty()
              .getPlayerKicked()
              .replace("%player%", member.getName()));
    } else {
      sendMessage(
          PracticeConfig.language()
              .getParty()
              .getPlayerLeft()
              .replace("%player%", member.getName()));
    }

    if (member.equals(leader)) {
      disband();
    }

    members.remove(member);
    memberProfile.setParty(false);
    memberProfile.setPartyChat(false);

    if (memberProfile.getStatus().equals(ProfileStatus.LOBBY)) {
      InventoryManager.getInstance().setLobbyInventory(member, false);
    }

    GuiManager.getInstance().searchGui(GuiType.Party_OtherParties).update();
    GuiManager.getInstance().searchGui(GuiType.Party_PublicParties).update();
  }

  public void disband() {
    broadcastTask.cancel();
    sendMessage(PracticeConfig.language().getParty().getPartyDisbanded());

    for (Player member : members) {
      Profile memberProfile = ProfileManager.getInstance().getProfile(member);
      memberProfile.setParty(false);
      memberProfile.setPartyChat(false);
    }

    List<Player> membersCopy = new ArrayList<>(members);
    members.clear();

    if (match != null) {
      if (PracticeConfig.settings().getParty().isEndMatchOnDisband()) {
        match.sendMessage(PracticeConfig.language().getParty().getMatchEnd(), true);
        match.endMatch();
      }
    } else {
      for (Player member : membersCopy) {
        InventoryManager.getInstance().setLobbyInventory(member, false);
      }
    }

    GuiManager.getInstance().searchGui(GuiType.Party_OtherParties).update();
    GuiManager.getInstance().searchGui(GuiType.Party_PublicParties).update();
    PartyManager.getInstance().getParties().remove(this);
  }

  public boolean canUsePartyChat(Player player) {
    return partyChat || leader.equals(player);
  }

  public void sendMessage(String message) {
    for (Player player : members) {
      Common.sendMmMessage(player, message);
    }
  }

  public void sendMessage(Component component) {
    for (Player player : members) {
      Common.send(player, component);
    }
  }

  public List<String> getMemberNames() {
    return PlayerUtil.getPlayerNames(members);
  }
}
