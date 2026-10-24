package dev.lokspel.practice.manager.ladder.type;

import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.Round;
import dev.lokspel.practice.manager.fight.match.enums.MatchType;
import dev.lokspel.practice.manager.fight.match.enums.RoundStatus;
import dev.lokspel.practice.manager.fight.match.enums.TeamEnum;
import dev.lokspel.practice.manager.fight.match.interfaces.PlayerWinner;
import dev.lokspel.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayers;
import dev.lokspel.practice.manager.fight.util.stats.Statistic;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.CustomConfig;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.ScoringLadder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.LadderData;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import dev.lokspel.practice.util.playerutil.PlayerUtil;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
public class Boxing extends NormalLadder implements CustomConfig, LadderHandle, ScoringLadder {

  private int boxingWinHit;

  public Boxing(String name, LadderType type) {
    super(name, type);
    this.hunger = false;
  }

  @Override
  public boolean shouldEndRound(Match match, Round round, Player player) {
    Statistic statistic = match.getCurrentStat(player);
    return statistic != null && statistic.getHit() >= boxingWinHit;
  }

  @Override
  public String getWinConditionMessage() {
    return "reached " + boxingWinHit + " hits";
  }

  @Override
  public String getScoringDisplayName() {
    return "Hits";
  }

  @Override
  public boolean handleEvents(Event e, Match match) {
    if (e instanceof EntityDamageEvent damageEvent) {
      onPlayerDamage(damageEvent, match);

      if (damageEvent instanceof EntityDamageByEntityEvent damageByEntity) {
        onPlayerDamagePlayer(damageByEntity, match, this);
      }

      return true;
    }
    return false;
  }

  @Override
  public void writeCustomConfig(LadderData data) {
    data.setBoxingWinHit(this.boxingWinHit);
  }

  @Override
  public void readCustomConfig(LadderData data) {
    if (data.getBoxingWinHit() instanceof Integer winHit) {
      this.boxingWinHit = winHit;
      if (this.boxingWinHit < 40 || this.boxingWinHit > 600) {
        this.boxingWinHit = 100;
      }
    } else {
      this.boxingWinHit = 100;
    }
  }

  private static void onPlayerDamagePlayer(
      final @NotNull EntityDamageByEntityEvent e,
      final @NotNull Match match,
      final @NotNull Boxing ladder) {
    if (!(e.getDamager() instanceof Player attacker)) {
      return;
    }

    Statistic attackerStat = match.getCurrentStat(attacker);
    if (attackerStat == null) {
      return;
    }

    int requiredStrokes = ladder.getBoxingWinHit();
    MatchType matchType = match.getType();
    Round round = match.getCurrentRound();

    switch (matchType) {
      case DUEL, PARTY_FFA -> {
        if (attackerStat.getHit() >= requiredStrokes
            && round instanceof PlayerWinner playerWinner
            && !attackerStat.isSet()) {
          playerWinner.setRoundWinner(attacker);
          round.endRound();
        }
      }
      case PARTY_SPLIT, PARTY_VS_PARTY -> {
        if (match instanceof PlayersVsPlayers playersVsPlayers) {
          TeamEnum attackerTeam = playersVsPlayers.getTeam(attacker);

          if (getTeamBoxingStrokes(match, playersVsPlayers.getTeamPlayers(attackerTeam))
                  >= requiredStrokes
              && !attackerStat.isSet()) {
            playersVsPlayers.getCurrentRound().setRoundWinner(attackerTeam);
            round.endRound();
          }
        }
      }
      default -> {}
    }
  }

  private static void onPlayerDamage(
      final @NotNull EntityDamageEvent e, final @NotNull Match match) {
    if (e.getEntity() instanceof Player player
        && match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
      e.setDamage(0);
      PlayerUtil.healToMaxHealth(player);
    }
  }

  public static int getTeamBoxingStrokes(Match match, List<Player> team) {
    return team.stream()
        .map(match::getCurrentStat)
        .filter(Objects::nonNull)
        .mapToInt(Statistic::getHit)
        .sum();
  }
}
