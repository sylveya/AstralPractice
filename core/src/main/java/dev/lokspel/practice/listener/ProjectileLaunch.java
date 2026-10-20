package dev.lokspel.practice.listener;

import dev.lokspel.practice.manager.fight.ffa.FfaManager;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.util.BlockUtil;
import dev.lokspel.practice.util.startup.PermanentConfig;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileLaunch implements Listener {

  @EventHandler
  public void onProjHit(ProjectileHitEvent e) {
    if (!(e.getEntity() instanceof Arrow arrow)) {
      return;
    }

    if (BlockUtil.hasMetadata(arrow, PermanentConfig.FIGHT_ENTITY)) {
      return;
    }

    // Fallback: if launch tagging was missed, recover ownership from shooter context.
    if (arrow.getShooter() instanceof Player shooter) {
      Match match = MatchManager.getInstance().getLiveMatchByPlayer(shooter);
      if (match != null) {
        BlockUtil.setMetadata(arrow, PermanentConfig.FIGHT_ENTITY, match);
        return;
      }

      Ffa ffa = FfaManager.getInstance().getFfaByPlayer(shooter);
      if (ffa != null) {
        BlockUtil.setMetadata(arrow, PermanentConfig.FIGHT_ENTITY, ffa);
        return;
      }
    }

    // Arrows that belong to a fight (match or FFA) are managed by the fight system:
    // they persist on the ground for up to 5 minutes (vanilla behaviour) and are
    // cleaned up automatically when the arena rolls back.
    // Only remove arrows that are NOT part of any fight (e.g. shot by a lobby player).
    arrow.remove();
  }
}
