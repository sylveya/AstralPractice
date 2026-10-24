package dev.lokspel.practice.manager.fight.match;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.lokspel.practice.manager.fight.match.listener.LadderTypeListener;
import dev.lokspel.practice.manager.fight.match.listener.MatchEventListener;
import dev.lokspel.practice.manager.fight.match.listener.MatchLifecycleListener;
import dev.lokspel.practice.manager.fight.match.listener.StartListener;
import dev.lokspel.practice.manager.fight.match.type.duel.Duel;
import dev.lokspel.practice.manager.fight.match.util.RematchRequest;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.settings.CentralizedSettingListener;
import dev.lokspel.practice.manager.nametag.NametagManager;
import dev.lokspel.practice.manager.spectator.SpectatorManager;
import dev.lokspel.practice.util.interfaces.Spectatable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public class MatchManager {

  private static MatchManager instance;

  public static MatchManager getInstance() {
    if (instance == null) {
      instance = new MatchManager();
    }
    return instance;
  }

  private final NametagManager belowNameManager;

  private final Map<String, Match> matches = new ConcurrentHashMap<>();
  private final List<Match> liveMatches = new CopyOnWriteArrayList<>();
  private final Map<UUID, Match> playerMatches = new ConcurrentHashMap<>();
  private final Set<RematchRequest> rematches = new HashSet<>();

  private MatchManager() {
    AstralPractice practice = AstralPractice.getInstance();

    // Register match lifecycle listener (start/end events)
    Bukkit.getPluginManager().registerEvents(new MatchLifecycleListener(), practice);

    // Register match event listener (teleport, projectile, kit selection, etc.)
    Bukkit.getPluginManager().registerEvents(new MatchEventListener(), practice);

    // Register centralized setting listener (all setting handlers)
    Bukkit.getPluginManager().registerEvents(new CentralizedSettingListener(), practice);

    // Register start command listener
    Bukkit.getPluginManager().registerEvents(new StartListener(), practice);

    Bukkit.getPluginManager().registerEvents(new LadderTypeListener(), practice);

    this.belowNameManager = NametagManager.getInstance();
    // PacketEvents.getAPI().getEventManager().registerListener(this.belowNameManager,
    // PacketListenerPriority.NORMAL);
  }

  public Match getLiveMatchByPlayer(Player player) {
    if (player == null) {
      return null;
    }
    return getLiveMatchByPlayer(player.getUniqueId());
  }

  public Match getLiveMatchByPlayer(UUID uuid) {
    if (uuid == null) {
      return null;
    }
    return this.playerMatches.get(uuid);
  }

  public Match getLiveMatchBySpectator(Player spectator) {
    if (spectator == null) {
      return null;
    }
    Spectatable spectatable =
        SpectatorManager.getInstance().getSpectators().get(spectator.getUniqueId());
    return spectatable instanceof Match match ? match : null;
  }

  public List<Match> getLiveMatchesByArena(BasicArena arena) {
    return liveMatches.stream().filter(match -> match.getArena().equals(arena)).toList();
  }

  public List<Match> getLiveMatchesByLadder(Ladder ladder) {
    return liveMatches.stream().filter(match -> match.getLadder().equals(ladder)).toList();
  }

  public int getDuelMatchSize(Ladder ladder, boolean ranked) {
    int size = 0;
    for (Match match : liveMatches) {
      if (match.getLadder().equals(ladder)) {
        if (ranked && match instanceof Duel duel) {
          if (duel.isRanked()) {
            size++;
          }
        } else {
          size++;
        }
      }
    }
    return size * 2;
  }

  public int getPlayerInMatchSize() {
    return liveMatches.stream().mapToInt(match -> match.getPlayers().size()).sum();
  }

  public int getPlayerInMatchSize(final Ladder ladder) {
    return liveMatches.stream()
        .filter(match -> match.getLadder().equals(ladder))
        .mapToInt(match -> match.getPlayers().size())
        .sum();
  }

  /**
   * "Get all players in the arena that are not in the match or spectating the match."
   *
   * @param match The match that the players are being hidden from.
   * @return A list of players that are not in the match and are not spectating the match.
   */
  public List<Player> getHidePlayers(Match match) {
    return match.getArena().getCuboid().getPlayers().stream()
        .filter(
            player ->
                !match.getPlayers().contains(player) && !match.getSpectators().contains(player))
        .toList();
  }

  /** End all live matches. */
  public void endMatches() {
    for (Match match : liveMatches) {
      match.endMatch();
    }
  }

  /**
   * Return the rematch request that contains the given player.
   *
   * @param player The player who is requesting the rematch.
   * @return A rematch request
   */
  public RematchRequest getRematchRequest(Player player) {
    for (RematchRequest rematchRequest : rematches) {
      if (rematchRequest.getPlayers().contains(player)) {
        return rematchRequest;
      }
    }
    return null;
  }

  public void invalidateRematch(RematchRequest rematchRequest) {
    if (rematchRequest == null) {
      return;
    }

    if (rematches.remove(rematchRequest)) {
      rematchRequest.invalidate();
    }
  }

  public void invalidateRematchByPlayer(Player player) {
    if (player == null) {
      return;
    }

    RematchRequest rematchRequest = getRematchRequest(player);
    if (rematchRequest != null) {
      invalidateRematch(rematchRequest);
    }
  }
}
