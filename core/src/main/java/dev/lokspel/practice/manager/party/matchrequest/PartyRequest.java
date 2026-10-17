package dev.lokspel.practice.manager.party.matchrequest;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.type.duel.Duel;
import dev.lokspel.practice.manager.fight.match.type.playersvsplayers.partyvsparty.PartyVsParty;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.util.LadderUtil;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.playerutil.PlayerUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PartyRequest {

  RequestManager requestManager = PartyManager.getInstance().getRequestManager();

  private final PartyRequest partyRequest;
  @Getter @Setter private Party sender;
  @Getter @Setter private Party target;
  @Getter @Setter private Ladder ladder;
  @Getter @Setter private Arena arena;
  @Getter @Setter private int rounds;

  public PartyRequest(Party sender, Party target, Ladder ladder, Arena arena, int rounds) {
    this.partyRequest = this;

    this.sender = sender;
    this.target = target;
    this.ladder = ladder;
    this.arena = arena;
    this.rounds = rounds;
  }

  public void sendRequest() {
    List<PartyRequest> partyRequests;

    if (requestManager.getRequests().containsKey(target)) {
      partyRequests = new ArrayList<>(requestManager.getRequests().get(target));
    } else {
      partyRequests = new ArrayList<>();
    }

    partyRequests.removeIf(oldRequest -> oldRequest.getSender().equals(sender));

    partyRequests.add(this);
    requestManager.getRequests().put(target, partyRequests);

    sender.getLeader().closeInventory();
    sendRequestMessage();

    new BukkitRunnable() {
      @Override
      public void run() {
        List<PartyRequest> targetRequests = requestManager.getRequests().get(target);
        if (targetRequests != null) {
          targetRequests.remove(partyRequest);
          if (targetRequests.isEmpty()) {
            requestManager.getRequests().remove(target);
          }
        }
      }
    }.runTaskLater(
        AstralPractice.getInstance(),
        20L * PracticeConfig.settings().getParty().getRequestExpiry());
  }

  public void sendRequestMessage() {
    String arenaName;
    if (arena != null) {
      arenaName = arena.getDisplayName();
    } else {
      arenaName =
          PracticeConfig.language().getParty().getMatchRequestMessage().getRandomArenaName();
    }

    for (String line : PracticeConfig.language().getParty().getMatchRequestMessage().getSender()) {
      Common.sendMmMessage(
          sender.getLeader(),
          line.replace("%ladder%", ladder.getDisplayName())
              .replace("%arena%", arenaName)
              .replace("%rounds%", String.valueOf(rounds))
              .replace("%target_leader%", target.getLeader().getName())
              .replace(
                  "%target_members%",
                  PlayerUtil.getPlayerNames(target.getMembers())
                      .toString()
                      .replace("[", "")
                      .replace("]", "")));
    }

    for (String line : PracticeConfig.language().getParty().getMatchRequestMessage().getTarget()) {
      Common.sendMmMessage(
          target.getLeader(),
          line.replace("%ladder%", ladder.getDisplayName())
              .replace("%arena%", arenaName)
              .replace("%rounds%", String.valueOf(rounds))
              .replace("%sender_leader%", sender.getLeader().getName())
              .replace(
                  "%sender_members%",
                  PlayerUtil.getPlayerNames(sender.getMembers())
                      .toString()
                      .replace("[", "")
                      .replace("]", "")));
    }
  }

  public void acceptRequest() {
    requestManager.getRequests().get(target).remove(this);

    Arena arena;
    if (this.getArena() != null) {
      if (this.getArena().getAvailableArena() != null) {
        arena = this.getArena();
      } else {
        Common.sendMmMessage(
            sender.getLeader(), PracticeConfig.language().getParty().getArenaBusy());
        arena = LadderUtil.getAvailableArena(ladder);
      }
    } else {
      arena = LadderUtil.getAvailableArena(ladder);
    }

    if (arena != null && arena.getAvailableArena() != null) {
      List<Player> matchPlayers = new ArrayList<>();
      matchPlayers.addAll(sender.getMembers());
      matchPlayers.addAll(target.getMembers());

      Match match;
      if (matchPlayers.size() == 2) {
        match = new Duel(ladder, arena, matchPlayers, false, rounds);
      } else {
        match = new PartyVsParty(ladder, arena, sender, target, matchPlayers, rounds);
      }

      sender.setMatch(match);
      target.setMatch(match);
      match.startMatch();
    } else {
      sender.sendMessage(PracticeConfig.language().getParty().getNoAvailableArena());
      target.sendMessage(PracticeConfig.language().getParty().getNoAvailableArena());
    }
  }
}
