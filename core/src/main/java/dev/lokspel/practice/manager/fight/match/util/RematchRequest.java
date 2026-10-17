package dev.lokspel.practice.manager.fight.match.util;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.arenas.ArenaCopy;
import dev.lokspel.practice.manager.arena.arenas.interfaces.NormalArena;
import dev.lokspel.practice.manager.duel.DuelManager;
import dev.lokspel.practice.manager.duel.DuelRequest;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.inventory.Inventory;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.inventory.inventories.LobbyInventory;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RematchRequest {

  @Getter private final List<Player> players = new ArrayList<>();
  @Getter private final Ladder ladder;
  private final int rounds;
  private final Arena arena;

  private boolean isRequested = false;
  private boolean invalidated = false;

  public RematchRequest(Match match) {
    this.players.addAll(match.getPlayers());
    this.ladder = match.getLadder();
    this.rounds = match.getWinsNeeded();

    boolean playerSelectedArena = match.isPlayerSelectedArena();
    if (playerSelectedArena) {
      NormalArena matchArena = match.getArena();
      if (matchArena instanceof Arena selectedArena) {
        this.arena = selectedArena;
      } else if (matchArena instanceof ArenaCopy copy) {
        this.arena = copy.getMainArena();
      } else {
        this.arena = null;
      }
    } else {
      this.arena = null;
    }

    setInventories();
    startRunnable();
  }

  public void sendRematchRequest(Player sender) {
    if (invalidated) {
      return;
    }

    Player target = getOtherPlayer(sender);
    if (target == null || !target.isOnline()) {
      String targetName = target != null ? target.getName() : "Unknown";
      Common.sendMmMessage(
          sender,
          PracticeConfig.language()
              .getMatch()
              .getRematchRequest()
              .getTargetOffline()
              .replace("%target%", targetName));
      MatchManager.getInstance().invalidateRematch(this);
      return;
    }

    Profile targetProfile = ProfileManager.getInstance().getProfile(target);

    if (isRequested) {
      if (targetProfile.isParty()) {
        Common.sendMmMessage(
            sender,
            PracticeConfig.language()
                .getMatch()
                .getRematchRequest()
                .getCantSendAnymore()
                .replace("%target%", target.getName()));
        return;
      }

      List<DuelRequest> requests = DuelManager.getInstance().getRequests().get(sender);

      if (requests != null) {
        for (DuelRequest request : requests) {
          if (!request.getSender().equals(target)) {
            continue;
          }

          request.acceptRequest();
          return;
        }
      }

      Common.sendMmMessage(
          sender, PracticeConfig.language().getMatch().getRematchRequest().getAlreadySent());
      return;
    }

    if ((targetProfile.getStatus().equals(ProfileStatus.LOBBY)
            || targetProfile.getStatus().equals(ProfileStatus.EDITOR)
            || targetProfile.getStatus().equals(ProfileStatus.SPECTATE))
        && !targetProfile.isParty()) {
      if (!targetProfile.isDuelRequest()) {
        Common.sendMmMessage(
            sender,
            PracticeConfig.language()
                .getMatch()
                .getRematchRequest()
                .getTargetDontAccept()
                .replace("%target%", target.getName()));
        return;
      }

      DuelRequest request =
          new DuelRequest(
              sender,
              target,
              ladder,
              arena,
              rounds,
              () -> MatchManager.getInstance().invalidateRematch(this));
      DuelManager.getInstance().sendRequest(request);

      isRequested = true;
    } else {
      Common.sendMmMessage(
          sender,
          PracticeConfig.language()
              .getMatch()
              .getRematchRequest()
              .getCantSendAnymore()
              .replace("%target%", target.getName()));
    }
  }

  public Player getOtherPlayer(Player player) {
    for (Player player1 : players) {
      if (player1 != player) {
        return player1;
      }
    }
    return null;
  }

  public void setInventories() {
    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> {
              if (invalidated) {
                return;
              }

              for (Player player : this.players) {
                if (!player.isOnline()) {
                  continue;
                }

                Inventory inventory = InventoryManager.getInstance().getPlayerInventory(player);
                if (inventory instanceof LobbyInventory lobbyInventory) {
                  lobbyInventory.addRematchItem(player, getOtherPlayer(player));
                }
              }
            },
            5L);
  }

  public void startRunnable() {
    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> MatchManager.getInstance().invalidateRematch(this),
            PracticeConfig.settings().getMatchSettings().getRematch().getExpireTime() * 20L);
  }

  public synchronized void invalidate() {
    if (invalidated) {
      return;
    }

    invalidated = true;

    for (Player player : players) {
      if (!player.isOnline()) {
        continue;
      }

      Profile profile = ProfileManager.getInstance().getProfile(player);
      if (profile == null) {
        continue;
      }

      Inventory inventory = InventoryManager.getInstance().getPlayerInventory(player);
      if (inventory instanceof LobbyInventory lobbyInventory
          && (profile.getStatus().equals(ProfileStatus.LOBBY)
              || profile.getStatus().equals(ProfileStatus.SPECTATE))) {
        lobbyInventory.removeRematchItem(player);
      }
    }
  }
}
