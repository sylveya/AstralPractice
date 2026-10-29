package dev.lokspel.practice.manager.fight.listener;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWaypoint;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.interfaces.Event;
import dev.lokspel.practice.manager.fight.ffa.FfaManager;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import org.bukkit.entity.Player;

public class LocatorBarListener implements PacketListener {

  private static LocatorBarListener instance;

  public static LocatorBarListener getInstance() {
    if (instance == null) {
      instance = new LocatorBarListener();
    }
    return instance;
  }

  protected LocatorBarListener() {
    PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.NORMAL);
  }

  @Override
  public void onPacketSend(PacketSendEvent event) {
    if (event.getPacketType() != PacketType.Play.Server.WAYPOINT) {
      return;
    }

    Player player = event.getPlayer();
    if (player == null) {
      return;
    }

    WrapperPlayServerWaypoint wrapper = new WrapperPlayServerWaypoint(event);
    if (wrapper.getOperation() == WrapperPlayServerWaypoint.Operation.UNTRACK) {
      return;
    }

    if (!isLocatorBarAllowed(player)) {
      event.setCancelled(true);
    }
  }

  private boolean isLocatorBarAllowed(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null || profile.getStatus() == null) {
      return false;
    }

    return switch (profile.getStatus()) {
      case MATCH -> {
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        yield match != null && match.getArena() != null && match.getArena().isLocatorBar();
      }
      case FFA -> {
        Ffa ffa = FfaManager.getInstance().getFfaByPlayer(player);
        yield ffa != null && ffa.getArena() != null && ffa.getArena().isLocatorBar();
      }
      case EVENT -> {
        Event event = EventManager.getInstance().getEventByPlayer(player);
        yield event != null && event.getEventData() != null && event.getEventData().isLocatorBar();
      }
      case SPECTATE -> {
        Match match = MatchManager.getInstance().getLiveMatchBySpectator(player);
        if (match != null && match.getArena() != null) {
          yield match.getArena().isLocatorBar();
        }
        Ffa ffa = FfaManager.getInstance().getFfaBySpectator(player);
        if (ffa != null && ffa.getArena() != null) {
          yield ffa.getArena().isLocatorBar();
        }
        Event event = EventManager.getInstance().getEventBySpectator(player);
        if (event != null && event.getEventData() != null) {
          yield event.getEventData().isLocatorBar();
        }
        yield false;
      }
      default -> false;
    };
  }
}
