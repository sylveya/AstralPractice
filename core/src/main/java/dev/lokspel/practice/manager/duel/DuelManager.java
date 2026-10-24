package dev.lokspel.practice.manager.duel;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class DuelManager {

  private static DuelManager instance;

  public static DuelManager getInstance() {
    if (instance == null) {
      instance = new DuelManager();
    }
    return instance;
  }

  private DuelManager() {}

  private final Map<Player, Player> pendingRequestTarget = new HashMap<>();
  private final Map<Player, List<DuelRequest>> requests = new HashMap<>();

  public boolean isRequested(Player sender, Player target) {
    List<DuelRequest> targetRequests = requests.get(target);
    return targetRequests != null
        && targetRequests.stream().anyMatch(request -> request.getSender().equals(sender));
  }

  public void sendRequest(DuelRequest request) {
    final Player sender = request.getSender();
    Player target = request.getTarget();

    List<DuelRequest> targetRequests = requests.computeIfAbsent(target, k -> new ArrayList<>());
    targetRequests.removeIf(oldRequest -> oldRequest.getSender().equals(sender));
    targetRequests.add(request);

    sender.closeInventory();
    request.sendRequest();

    new BukkitRunnable() {
      @Override
      public void run() {
        List<DuelRequest> currentRequests = requests.get(target);
        if (currentRequests == null) {
          return;
        }

        boolean removed = currentRequests.remove(request);
        if (currentRequests.isEmpty()) {
          requests.remove(target);
        }

        if (removed) {
          request.handleExpiry();
        }
      }
    }.runTaskLater(
        AstralPractice.getInstance(),
        20L * PracticeConfig.settings().getMatchSettings().getDuel().getInvitationExpiry());
  }
}
