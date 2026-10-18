package dev.lokspel.practice.command.practice.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.util.ArenaWorldUtil;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.util.Common;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public final class ArenasArg {

  private ArenasArg() {}

  public static void run(Player player) {
    if (!player.hasPermission("ap.practice.arenas")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
      return;
    }

    if (ArenaWorldUtil.getArenasWorld() == null) {
      Common.sendMmMessage(player, "<red>The default arenas world is disabled or not loaded.");
      return;
    }

    PlayerUtil.clearInventory(player);
    player.setGameMode(GameMode.CREATIVE);
    player.setAllowFlight(true);
    player.setFlying(true);

    player.teleport(ArenaWorldUtil.getArenasWorld().getSpawnLocation());
  }
}
