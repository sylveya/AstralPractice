package dev.lokspel.practice.manager.party;

import dev.lokspel.api.event.PartyCreateEvent;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.guis.party.OtherPartiesGui;
import dev.lokspel.practice.manager.gui.guis.party.PartyEventsGui;
import dev.lokspel.practice.manager.gui.guis.party.PublicPartiesGui;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.party.matchrequest.RequestManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.profile.group.Group;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

@Getter
public class PartyManager implements Listener {

  private static PartyManager instance;

  public static PartyManager getInstance() {
    if (instance == null) {
      instance = new PartyManager();
    }
    return instance;
  }

  private final RequestManager requestManager = new RequestManager();
  private final List<Party> parties = new ArrayList<>();

  public static final long INVITE_COOLDOWN =
      PracticeConfig.settings().getParty().getPartyInviteCooldown() * 1000L;
  private static final int DEFAULT_MAX_PARTY_MEMBERS =
      PracticeConfig.settings().getParty().getSettings().getMaxPartyMembers().getDefaultValue();

  private PartyManager() {
    // Assigned before the GUIs below are built, some of them read from this manager while being
    // constructed.
    instance = this;

    Bukkit.getPluginManager().registerEvents(this, AstralPractice.getInstance());

    GuiManager.getInstance().addGui(new OtherPartiesGui());
    GuiManager.getInstance().addGui(new PublicPartiesGui());
    GuiManager.getInstance().addGui(new PartyEventsGui());
  }

  /**
   * Re-creates the party GUIs, including every party's settings, from the reloaded configuration.
   */
  public void reloadGuis() {
    GuiManager.getInstance().addGui(new OtherPartiesGui());
    GuiManager.getInstance().addGui(new PublicPartiesGui());
    GuiManager.getInstance().addGui(new PartyEventsGui());

    for (Party party : parties) {
      party.reloadSettingsGui();
    }
  }

  public Party getParty(Player player) {
    return parties.stream()
        .filter(party -> party.getMembers().contains(player))
        .findFirst()
        .orElse(null);
  }

  public Party getParty(Match match) {
    return parties.stream()
        .filter(party -> match != null && match.equals(party.getMatch()))
        .findFirst()
        .orElse(null);
  }

  /**
   * Whether the given party currently has a match that is still running.
   *
   * <p>A match only counts as active while it is registered among the live matches, so a stale
   * {@code party.match} reference left behind after a match ends (or after a cancelled start) is
   * ignored.
   *
   * @param party the party to check
   * @return true if the party has an ongoing match
   */
  public boolean hasActiveMatch(Party party) {
    if (party == null || party.getMatch() == null) {
      return false;
    }

    return MatchManager.getInstance().getLiveMatches().contains(party.getMatch());
  }

  public boolean hasJoinablePublicParty() {
    return parties.stream()
        .anyMatch(
            party ->
                party.isPublicParty() && party.getMembers().size() < party.getMaxPlayerLimit());
  }

  public int resolvePartyMemberLimit(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    Group group = profile != null ? profile.getGroup() : null;
    return Math.max(2, group != null ? group.getPartyMemberLimit() : DEFAULT_MAX_PARTY_MEMBERS);
  }

  public void createParty(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (!player.hasPermission("ap.party.create")) {
      Common.sendMmMessage(player, PracticeConfig.language().getParty().getNoPermission());
      return;
    }

    if (PartyManager.getInstance().getParty(player) != null) {
      Common.sendMmMessage(player, PracticeConfig.language().getParty().getAlreadyParty());
      return;
    }

    if (!profile.getStatus().equals(ProfileStatus.LOBBY)) {
      Common.sendMmMessage(player, PracticeConfig.language().getParty().getCantCreateParty());
      return;
    }

    Party party = new Party(player);
    PartyCreateEvent partyCreateEvent = new PartyCreateEvent(party);
    Bukkit.getPluginManager().callEvent(partyCreateEvent);

    if (!partyCreateEvent.isCancelled()) {
      parties.add(party);
      profile.setParty(true);
      profile.setFollowTarget(null);

      InventoryManager.getInstance().setLobbyInventory(player, false);
      GuiManager.getInstance().searchGui(GuiType.Party_OtherParties).update();
      GuiManager.getInstance().searchGui(GuiType.Party_PublicParties).update();

      Common.sendMmMessage(player, PracticeConfig.language().getParty().getPartyCreated());
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlayerQuit(PlayerQuitEvent e) {
    Player player = e.getPlayer();
    Party party = this.getParty(player);

    if (party != null) {
      party.removeMember(player, false);
    }
  }
}
