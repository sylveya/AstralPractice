package dev.lokspel.practice.manager.leaderboard.hologram.holograms;

import dev.lokspel.practice.manager.leaderboard.Leaderboard;
import dev.lokspel.practice.manager.leaderboard.LeaderboardManager;
import dev.lokspel.practice.manager.leaderboard.hologram.Hologram;
import dev.lokspel.practice.manager.leaderboard.hologram.HologramData;
import dev.lokspel.practice.manager.leaderboard.hologram.HologramType;
import org.bukkit.Location;

public class FfaGlobalHologram extends Hologram {

  public FfaGlobalHologram(String name, Location baseLocation) {
    super(name, baseLocation, HologramType.FFA_GLOBAL);
  }

  public FfaGlobalHologram(String name) {
    super(name, HologramType.FFA_GLOBAL);
  }

  @Override
  public void getAbstractData(HologramData data) {}

  @Override
  public void setAbstractData(HologramData data) {}

  @Override
  public boolean isReadyToEnable() {
    return leaderboardType != null;
  }

  @Override
  public Leaderboard getNextLeaderboard() {
    return LeaderboardManager.getInstance()
        .searchLb(hologramType.getLbMainType(), leaderboardType, null);
  }
}
