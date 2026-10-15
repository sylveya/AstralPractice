package dev.lokspel.practice.manager.fight.util;

import dev.lokspel.practice.manager.fight.util.runnable.GameRunnable;
import dev.lokspel.practice.manager.fight.util.stats.Statistic;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.interfaces.Spectatable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public abstract class FightPlayer {

  protected final Player player;
  protected final UUID uuid;
  protected final Profile profile;

  protected final Spectatable spectatable;
  protected final List<GameRunnable> gameRunnables = new ArrayList<>();

  public FightPlayer(Player player, Spectatable spectatable) {
    this.player = player;
    this.uuid = ProfileManager.getInstance().getUuids().get(player);
    this.profile = ProfileManager.getInstance().getProfile(player);
    this.spectatable = spectatable;
  }

  public boolean isExpBarUsed() {
    for (GameRunnable gameRunnable : this.gameRunnables) {
      if (gameRunnable.isExpBarUse()) {
        return true;
      }
    }
    return false;
  }

  public void die(String deathMessage, Statistic statistic) {
    for (GameRunnable gameRunnable : new ArrayList<>(this.gameRunnables)) {
      gameRunnable.cancel(true);
    }

    if (deathMessage != null && !deathMessage.equalsIgnoreCase("")) {
      spectatable.sendMessage(deathMessage.replace("%player%", this.player.getName()), true);
    }

    if (statistic != null) {
      statistic.setDeaths(statistic.getDeaths() + 1);
      statistic.resetKillStreak();
    }
  }
}
