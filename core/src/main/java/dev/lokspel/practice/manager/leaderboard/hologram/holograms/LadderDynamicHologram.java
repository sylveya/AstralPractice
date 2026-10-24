package dev.lokspel.practice.manager.leaderboard.hologram.holograms;

import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.leaderboard.Leaderboard;
import dev.lokspel.practice.manager.leaderboard.LeaderboardManager;
import dev.lokspel.practice.manager.leaderboard.hologram.Hologram;
import dev.lokspel.practice.manager.leaderboard.hologram.HologramData;
import dev.lokspel.practice.manager.leaderboard.hologram.HologramType;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.Location;

/** Dynamic hologram that rotates through multiple ladders on a timer. */
@Getter
public class LadderDynamicHologram extends Hologram {

  private List<NormalLadder> ladders;
  private int currentLadderIndex = -1;

  public LadderDynamicHologram(String name, Location baseLocation) {
    super(name, baseLocation, HologramType.LADDER_DYNAMIC);
    this.ladders = new ArrayList<>();
  }

  public LadderDynamicHologram(String name) {
    super(name, HologramType.LADDER_DYNAMIC);
  }

  @Override
  public void getAbstractData(HologramData data) {
    ladders = new ArrayList<>();
    currentLadderIndex = -1;

    List<String> ladderNames = data.getLadders();
    if (ladderNames != null) {
      for (String ladderName : ladderNames) {
        NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);
        if (ladder != null && ladder.isEnabled()) {
          ladders.add(ladder);
        }
      }
    }

    if (!ladders.isEmpty()) {
      currentLadderIndex = 0;
    }
  }

  @Override
  public void setAbstractData(HologramData data) {
    if (!ladders.isEmpty()) {
      data.setLadders(ladders.stream().map(Ladder::getName).toList());
    }
  }

  @Override
  public boolean isReadyToEnable() {
    return !ladders.isEmpty() && leaderboardType != null;
  }

  @Override
  public Leaderboard getNextLeaderboard() {
    Ladder ladder = getCurrentLadder();
    if (ladder == null) {
      return null;
    }
    return LeaderboardManager.getInstance()
        .searchLb(hologramType.getLbMainType(), leaderboardType, ladder);
  }

  /** Gets the current ladder without advancing rotation. */
  public Ladder getCurrentLadder() {
    if (ladders.isEmpty()) {
      currentLadderIndex = -1;
      return null;
    }

    if (currentLadderIndex < 0 || currentLadderIndex >= ladders.size()) {
      currentLadderIndex = 0;
    }

    return ladders.get(currentLadderIndex);
  }

  /** Advances to the next ladder in rotation. Called by HologramRunnable on timer tick. */
  public void rotateLadder() {
    if (ladders.isEmpty()) {
      currentLadderIndex = -1;
      return;
    }
    currentLadderIndex = (currentLadderIndex + 1) % ladders.size();
  }
}
