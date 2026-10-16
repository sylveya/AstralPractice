package dev.lokspel.practice.manager.fight.match.listener;

import dev.lokspel.api.event.match.MatchEndEvent;
import dev.lokspel.api.event.match.MatchStartEvent;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.enums.MatchType;
import dev.lokspel.practice.manager.fight.match.type.duel.Duel;
import dev.lokspel.practice.manager.fight.match.util.DeleteRunnable;
import dev.lokspel.practice.manager.fight.match.util.RematchRequest;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.type.TntSumo;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.spectator.SpectatorManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Handles match lifecycle events (start/end) and match management. Does NOT handle setting-specific
 * events - those are handled by CentralizedSettingListener.
 */
public class MatchLifecycleListener implements Listener {

  @EventHandler
  public void onMatchStart(MatchStartEvent e) {
    Match match = (Match) e.getMatch();

    // Register match in manager
    for (Player player : match.getPlayers()) {
      MatchManager.getInstance().getPlayerMatches().put(player.getUniqueId(), match);
    }

    MatchManager.getInstance().getMatches().put(match.getId(), match);
    MatchManager.getInstance().getLiveMatches().add(match);

    // Update GUIs
    if (match instanceof Duel && ((Duel) match).isRanked()) {
      GuiManager.getInstance().searchGui(GuiType.Queue_Ranked).update();
    } else {
      GuiManager.getInstance().searchGui(GuiType.Queue_Unranked).update();
    }
  }

  @EventHandler
  public void onMatchEnd(MatchEndEvent e) {
    Match match = (Match) e.getMatch();

    // If the player already joined a new match, keep that mapping.
    for (Player player : match.getPlayers()) {
      MatchManager.getInstance().getPlayerMatches().remove(player.getUniqueId(), match);
    }

    // A party-vs-party match belongs to both parties, so the ended match
    // must be cleared from all of them, not only the first one found.
    for (Party party : PartyManager.getInstance().getParties()) {
      if (match.equals(party.getMatch())) {
        party.setMatch(null);
      }
    }

    // Live match removal is deferred to after rollback completes in Match.endMatch().
    // This ensures block event listeners can still resolve the match via cuboid lookup
    // during the multi-tick rollback window, preventing untracked block changes.
    // MatchManager.getInstance().getLiveMatches().remove(match);

    // Update GUIs
    if (match instanceof Duel && ((Duel) match).isRanked()) {
      GuiManager.getInstance().searchGui(GuiType.Queue_Ranked).update();
    } else {
      GuiManager.getInstance().searchGui(GuiType.Queue_Unranked).update();
    }

    SpectatorManager.getInstance().getSpectatorMenuGui().update();

    DeleteRunnable.start(match);

    // Set rematch request items
    if (AstralPractice.getInstance().isEnabled()
        && match.getType().equals(MatchType.DUEL)
        && PracticeConfig.settings().getMatchSettings().getRematch().isEnabled()) {

      boolean sendRematchRequest = true;
      for (Player matchPlayer : match.getPlayers()) {
        Profile matchPlayerProfile = ProfileManager.getInstance().getProfile(matchPlayer);
        if (matchPlayerProfile.isParty()) {
          sendRematchRequest = false;
          break;
        }

        if (match.getLadder() instanceof TntSumo tntSumo) {
          tntSumo.cleanup(matchPlayer);
        }
      }

      if (sendRematchRequest) {
        RematchRequest rematchRequest = new RematchRequest(match);
        MatchManager.getInstance().getRematches().add(rematchRequest);
      }
    }
  }
}
