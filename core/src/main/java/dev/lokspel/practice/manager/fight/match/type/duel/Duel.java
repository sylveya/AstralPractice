package dev.lokspel.practice.manager.fight.match.type.duel;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.Round;
import dev.lokspel.practice.manager.fight.match.enums.MatchStatus;
import dev.lokspel.practice.manager.fight.match.enums.MatchType;
import dev.lokspel.practice.manager.fight.match.enums.TeamEnum;
import dev.lokspel.practice.manager.fight.match.enums.WeightClass;
import dev.lokspel.practice.manager.fight.match.interfaces.Team;
import dev.lokspel.practice.manager.fight.match.util.TeamUtil;
import dev.lokspel.practice.manager.fight.match.util.TempKillPlayer;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.DeathResult;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.queue.QueueManager;
import dev.lokspel.practice.manager.server.sound.SoundManager;
import dev.lokspel.practice.manager.server.sound.SoundType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public class Duel extends Match implements Team {

  private final boolean ranked;

  @Getter @Setter private boolean queuedMatch = false;

  private final Player player1;
  private final Player player2;

  private final Map<Player, Profile> playerProfiles = new HashMap<>();

  @Getter private Player matchWinner;
  @Getter private Player loser;

  public Duel(Ladder ladder, Arena arena, List<Player> players, boolean ranked, int winsNeeded) {
    super(ladder, arena, new ArrayList<>(players), winsNeeded);

    this.type = MatchType.DUEL;
    this.ranked = ranked;

    this.player1 = players.getFirst();
    this.playerProfiles.put(player1, ProfileManager.getInstance().getProfile(player1));

    if (players.size() == 2) {
      this.player2 = players.get(1);
      this.playerProfiles.put(player2, ProfileManager.getInstance().getProfile(player2));
    } else {
      this.player2 = null;
    }
  }

  @Override
  public void startNextRound() {
    DuelRound round = new DuelRound(this, this.rounds.size() + 1);
    this.rounds.put(round.getRoundNumber(), round);

    if (round.getRoundNumber() == 1) {

      // If it's the first round
      if (ranked && ladder instanceof NormalLadder normalLadder) {

        for (String line : PracticeConfig.language().getMatch().getDuel().getMatchStartRanked()) {
          this.sendMessage(
              line.replace("%matchTypeName%", MatchType.DUEL.getName(true))
                  .replace("%weightClassName%", WeightClass.RANKED.getMmName())
                  .replace("%ladder%", ladder.getDisplayName())
                  .replace("%arena%", arena.getDisplayName())
                  .replace("%rounds%", String.valueOf(this.winsNeeded))
                  .replace("%player1%", player1.getName())
                  .replace("%player2%", player2.getName())
                  .replace(
                      "%player1elo%",
                      String.valueOf(
                          playerProfiles
                              .get(player1)
                              .getStats()
                              .getLadderStat(normalLadder)
                              .getElo()))
                  .replace(
                      "%player1win%",
                      String.valueOf(
                          playerProfiles
                              .get(player1)
                              .getStats()
                              .getLadderStat(normalLadder)
                              .getRankedWins()))
                  .replace(
                      "%player2elo%",
                      String.valueOf(
                          playerProfiles
                              .get(player2)
                              .getStats()
                              .getLadderStat(normalLadder)
                              .getElo()))
                  .replace(
                      "%player2win%",
                      String.valueOf(
                          playerProfiles
                              .get(player2)
                              .getStats()
                              .getLadderStat(normalLadder)
                              .getRankedWins())),
              false);
        }
      } else {
        for (String line : PracticeConfig.language().getMatch().getDuel().getMatchStartUnranked()) {
          this.sendMessage(
              line.replace("%matchTypeName%", MatchType.DUEL.getName(true))
                  .replace("%weightClassName%", WeightClass.UNRANKED.getMmName())
                  .replace("%ladder%", ladder.getDisplayName())
                  .replace("%arena%", arena.getDisplayName())
                  .replace("%rounds%", String.valueOf(this.winsNeeded)),
              false);
        }
      }
    }

    round.startRound();
  }

  @Override
  public DuelRound getCurrentRound() {
    return (DuelRound) this.rounds.get(this.rounds.size());
  }

  @Override
  public int getWonRounds(Player player) {
    UUID playerUuid = player.getUniqueId();
    int wonRounds = 0;
    for (Round round : this.rounds.values()) {
      Player winner = ((DuelRound) round).getRoundWinner();
      if (winner != null && winner.getUniqueId().equals(playerUuid)) {
        wonRounds++;
      }
    }
    return wonRounds;
  }

  @Override
  public void teleportPlayer(Player player) {
    if (player.equals(player1)) {
      player.teleport(arena.getPosition1());
    } else {
      player.teleport(arena.getPosition2());
    }
  }

  @Override
  protected void killPlayer(Player player, String deathMessage) {
    DuelRound round = this.getCurrentRound();
    Player winnerPlayer = this.getOppositePlayer(player);
    boolean endRound = false;

    // Use the Match helper method to handle ladder-specific death behavior
    DeathResult result = handleLadderDeath(player);

    switch (result) {
      case TEMPORARY_DEATH:
        // Ladder supports respawning - create temp kill
        asRespawnableLadder()
            .ifPresent(
                respawnableLadder -> {
                  new TempKillPlayer(round, player, respawnableLadder.getRespawnTime());
                  SoundManager.getInstance()
                      .getSound(SoundType.MATCH_PLAYER_TEMP_DEATH)
                      .play(this.getPeople());
                });
        PlayerUtil.clearInventory(player);
        dev.lokspel.practice.util.playerutil.PlayerUtil.healToMaxHealth(player);
        break;

      case ELIMINATED:
        if (isRespawnableLadder()) {
          // Respawnable ladder but player is eliminated (e.g., bed destroyed)
          this.getCurrentStat(player).end(true);
          if (this.getLadder().getRoundEndDelay() <= 0 || this.wasLastDeathVoid(player)) {
            this.teleportPlayer(player);
          }
          endRound = true;
          SoundManager.getInstance().getSound(SoundType.MATCH_PLAYER_DEATH).play(this.getPeople());
          PlayerUtil.clearInventory(player);
          dev.lokspel.practice.util.playerutil.PlayerUtil.healToMaxHealth(player);
        } else if (isScoringLadder()) {
          // Scoring ladder (like Boxing) - death doesn't end round
          return;
        } else {
          // Default death behavior for standard ladders
          this.getCurrentStat(player).end(true);
          dev.lokspel.practice.util.playerutil.PlayerUtil.setFightPlayer(player, ladder);
          if (ladder.isDropInventory()) {
            addEntityChange(PlayerUtil.dropPlayerInventory(player));
          }
          PlayerUtil.clearInventory(player);
          dev.lokspel.practice.util.playerutil.PlayerUtil.healToMaxHealth(player);
          SoundManager.getInstance().getSound(SoundType.MATCH_PLAYER_DEATH).play(this.getPeople());
          endRound = true;
        }
        break;

      case NO_ACTION:
        // Ladder handled everything
        return;

      default:
        break;
    }

    if (endRound) {
      round.setRoundWinner(winnerPlayer);
      round.endRound();
    }
  }

  @Override
  public void removePlayer(Player player, boolean quit) {
    if (!players.contains(player)) {
      return;
    }

    players.remove(player);
    matchPlayers.remove(player);
    MatchManager.getInstance().getPlayerMatches().remove(player.getUniqueId(), this);

    // Only process quit logic if the match hasn't ended yet
    // When match status is END or OVER, players are being removed as part of cleanup
    if (quit && !this.status.equals(MatchStatus.END) && !this.status.equals(MatchStatus.OVER)) {
      this.getCurrentStat(player).end(true);

      this.sendMessage(
          TeamUtil.replaceTeamNames(
              PracticeConfig.language().getMatch().getDuel().getPlayerLeft(),
              player,
              this.getTeam(player)),
          true);

      DuelRound duelRound = this.getCurrentRound();
      duelRound.setRoundWinner(this.getOppositePlayer(player));
      duelRound.endRound();
    }

    this.removePlayerFromBelowName(player);

    if (AstralPractice.getInstance().isEnabled()) {
      // Remove 1 from the player's left matches
      Profile profile = ProfileManager.getInstance().getProfile(player);
      if (ranked) {
        profile.setRankedLeft(profile.getRankedLeft() - 1);
      } else {
        profile.setUnrankedLeft(profile.getUnrankedLeft() - 1);
      }

      // Set the player inventory to lobby inventory
      if (player.isOnline()) {
        InventoryManager.getInstance().setLobbyInventory(player, true);
      }

      // Auto-queue: re-queue the player into the same ladder + weight class if enabled.
      scheduleAutoQueue(player, this.ranked, this.ladder);
    }
  }

  /**
   * Schedules an auto-queue attempt for {@code player} shortly after the match cleanup. The delay
   * mirrors {@code RematchRequest} so it runs after the lobby inventory is set. Only matches
   * started by the matchmaking queue are re-queued, a {@code /duel} request already picked an
   * opponent, so those players return to the lobby instead. All queue validation (frozen/disabled
   * ladder, ranked limits/ban/ping) is delegated to {@link QueueManager}, which fails gracefully
   * with a player message instead of throwing.
   */
  private void scheduleAutoQueue(Player player, boolean ranked, Ladder ladder) {
    if (!AstralPractice.getInstance().isEnabled()) {
      return;
    }
    if (!this.queuedMatch) {
      return;
    }
    if (!(ladder instanceof NormalLadder normalLadder)) {
      return;
    }

    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> {
              boolean masterEnabled =
                  PracticeConfig.settings().getMatchSettings().getAutoQueue().isEnabled();
              Profile profile = ProfileManager.getInstance().getProfile(player);
              if (profile == null) {
                return;
              }

              boolean online = player.isOnline();
              boolean inLobby = profile.getStatus() == ProfileStatus.LOBBY;

              if (!shouldAutoQueue(
                  masterEnabled, profile.isAutoQueue(), this.queuedMatch, online, inLobby)) {
                return;
              }

              if (ranked) {
                QueueManager.getInstance().createRankedQueue(player, normalLadder);
              } else {
                QueueManager.getInstance().createUnrankedQueue(player, normalLadder);
              }
            },
            5L);
  }

  /**
   * Pure eligibility check for auto-queue. Extracted so it can be unit tested without a live
   * server.
   *
   * @return true only when all conditions hold.
   */
  static boolean shouldAutoQueue(
      boolean masterEnabled,
      boolean playerAutoQueue,
      boolean queuedMatch,
      boolean online,
      boolean inLobby) {
    return masterEnabled && playerAutoQueue && queuedMatch && online && inLobby;
  }

  @Override
  public boolean isEndMatch() {
    if (this.getStatus().equals(MatchStatus.END)) {
      return true;
    }

    if (this.players.size() == 1) {
      if (status.equals(MatchStatus.START)) {
        this.matchWinner = null;
      } else {
        this.matchWinner = this.players.stream().findAny().get();
        this.loser = this.getOppositePlayer(this.matchWinner);
      }
      return true;
    }

    for (Player player : this.players) {
      if (this.getWonRounds(player) == this.winsNeeded) {
        this.matchWinner = player;
        this.loser = this.getOppositePlayer(player);
        return true;
      }
    }

    return false;
  }

  @Override
  public TeamEnum getTeam(Player player) {
    if (player.equals(player1)) {
      return TeamEnum.TEAM1;
    } else {
      return TeamEnum.TEAM2;
    }
  }

  public Player getOppositePlayer(Player player) {
    if (player1 == player) {
      return player2;
    } else {
      return player1;
    }
  }
}
