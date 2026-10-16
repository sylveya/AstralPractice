package dev.lokspel.practice.manager.fight.match.type.partyffa;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.Round;
import dev.lokspel.practice.manager.fight.match.enums.MatchStatus;
import dev.lokspel.practice.manager.fight.match.enums.MatchType;
import dev.lokspel.practice.manager.fight.match.util.MatchPlayerUtil;
import dev.lokspel.practice.manager.fight.match.util.TempKillPlayer;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.fight.util.stats.Statistic;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.DeathResult;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.server.sound.SoundManager;
import dev.lokspel.practice.manager.server.sound.SoundType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class PartyFfa extends Match {

  private Player matchWinner;

  public PartyFfa(Ladder ladder, Arena arena, Party party, int winsNeeded) {
    super(ladder, arena, new ArrayList<>(party.getMembers()), winsNeeded);
    this.type = MatchType.PARTY_FFA;
  }

  @Override
  public void startNextRound() {
    PartyFfaRound round = new PartyFfaRound(this, this.rounds.size() + 1);
    this.rounds.put(round.getRoundNumber(), round);

    if (round.getRoundNumber() == 1) {
      for (String line : PracticeConfig.language().getMatch().getPartyFfa().getMatchStart()) {
        this.sendMessage(
            line.replace("%matchTypeName%", MatchType.PARTY_FFA.getName(true))
                .replace("%ladder%", ladder.getDisplayName())
                .replace("%arena%", arena.getDisplayName())
                .replace("%rounds%", String.valueOf(this.winsNeeded))
                .replace(
                    "%players%",
                    dev.lokspel.practice.util.playerutil.PlayerUtil.getPlayerNames(players)
                        .toString()
                        .replace("[", "")
                        .replace("]", "")),
            false);
      }
    }

    round.startRound();
  }

  @Override
  public PartyFfaRound getCurrentRound() {
    return (PartyFfaRound) this.rounds.get(this.rounds.size());
  }

  @Override
  public int getWonRounds(Player player) {
    int wonRounds = 0;
    for (Round round : this.rounds.values()) {
      if (((PartyFfaRound) round).getRoundWinner() == player) {
        wonRounds++;
      }
    }
    return wonRounds;
  }

  @Override
  public void teleportPlayer(Player player) {
    if (arena.getPartyFfaCenter() != null) {
      player.teleport(arena.getPartyFfaCenter());
      return;
    }

    int randomNum;
    if (arena.getFfaPositions().isEmpty()) {
      randomNum = new Random().nextInt(2);
      if (randomNum == 0) {
        player.teleport(arena.getPosition1());
      } else {
        player.teleport(arena.getPosition2());
      }
    } else {
      randomNum = new Random().nextInt(arena.getFfaPositions().size());
      player.teleport(arena.getFfaPositions().get(randomNum));
    }
  }

  @Override
  protected void killPlayer(Player player, String deathMessage) {
    PartyFfaRound round = this.getCurrentRound();

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
        return;

      case ELIMINATED:
        if (isScoringLadder()) {
          // Scoring ladder (like Boxing) - death doesn't end round
          return;
        }

        // Standard or respawnable-eliminated death behavior
        this.getCurrentStat(player).end(true);
        SoundManager.getInstance().getSound(SoundType.MATCH_PLAYER_DEATH).play(this.getPeople());

        dev.lokspel.practice.util.playerutil.PlayerUtil.setFightPlayer(player, ladder);

        if (ladder.isDropInventory()) {
          addEntityChange(PlayerUtil.dropPlayerInventory(player));
        } else {
          PlayerUtil.clearInventory(player);
        }

        // Send a death notification message
        String playerDieMsg = PracticeConfig.language().getMatch().getPartyFfa().getPlayerDie();
        if (playerDieMsg != null) {
          this.sendMessage(
              playerDieMsg
                  .replace("%player%", player.getName())
                  .replace("%alivePlayers%", String.valueOf(this.getAlivePlayers().size())),
              true);
        }

        Player winnerPlayer = this.getWinnerPlayer();
        if (winnerPlayer != null) {
          round.setRoundWinner(winnerPlayer);
          round.endRound();
        } else {
          MatchPlayerUtil.hidePlayerPartyGames(player, this.players);
        }
        break;

      case NO_ACTION:
        // Ladder handled everything
        break;

      default:
        break;
    }
  }

  @Override
  public void removePlayer(Player player, boolean quit) {
    if (!players.contains(player)) {
      return;
    }

    players.remove(player);
    MatchManager.getInstance().getPlayerMatches().remove(player.getUniqueId(), this);

    // Only process quit logic if the match hasn't ended yet
    // When match status is END or OVER, players are being removed as part of cleanup
    if (quit && !this.status.equals(MatchStatus.END) && !this.status.equals(MatchStatus.OVER)) {
      this.getCurrentStat(player).end(true);

      this.sendMessage(
          PracticeConfig.language()
              .getMatch()
              .getPartyFfa()
              .getPlayerLeft()
              .replace("%player%", player.getName()),
          true);

      Player winnerPlayer = this.getWinnerPlayer();
      if (winnerPlayer != null) {
        PartyFfaRound round = this.getCurrentRound();
        round.setRoundWinner(winnerPlayer);
        round.endRound();
      } else if (this.players.isEmpty() || getAlivePlayers().isEmpty()) {
        this.setStatus(MatchStatus.END);
        PartyFfaRound round = this.getCurrentRound();
        round.endRound();
      }
    }

    this.removePlayerFromBelowName(player);

    if (AstralPractice.getInstance().isEnabled() && player.isOnline()) {
      // Set the player inventory to lobby inventory
      InventoryManager.getInstance().setLobbyInventory(player, true);
    }
  }

  @Override
  public boolean isEndMatch() {
    if (this.getStatus().equals(MatchStatus.END)) {
      return true;
    }

    if (this.players.size() <= 1) {
      if (status.equals(MatchStatus.START) || this.players.isEmpty()) {
        this.matchWinner = null;
      } else {
        this.matchWinner = this.players.stream().findAny().get();
      }

      return true;
    }

    for (Player player : this.getPlayers()) {
      if (this.getWonRounds(player) == this.winsNeeded) {
        this.matchWinner = player;
        return true;
      }
    }

    return false;
  }

  public Player getWinnerPlayer() {
    Player winnerPlayer = null;
    for (Player player : this.players) {
      Statistic statistic = this.getCurrentStat(player);
      if (!statistic.isSet()) {
        if (winnerPlayer == null) {
          winnerPlayer = statistic.getPlayer();
        } else {
          return null;
        }
      }
    }
    return winnerPlayer;
  }

  public List<Player> getAlivePlayers() {
    List<Player> alivePlayers = new ArrayList<>();

    for (Player player : this.players) {
      if (!this.getCurrentStat(player).isSet()) {
        alivePlayers.add(player);
      }
    }

    return alivePlayers;
  }

  public Player getTopPlayer(int rank) {
    Map<Player, Integer> wonRounds = new HashMap<>();
    for (Player player : this.players) {
      wonRounds.put(player, this.getWonRounds(player));
    }

    if (wonRounds.size() < rank) {
      return null;
    }

    return new ArrayList<>(
            dev.lokspel.practice.util.playerutil.PlayerUtil.sortByValue(wonRounds).keySet())
        .get(rank - 1);
  }
}
