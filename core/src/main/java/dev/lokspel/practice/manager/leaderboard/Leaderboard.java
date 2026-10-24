package dev.lokspel.practice.manager.leaderboard;

import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.leaderboard.types.LbMainType;
import dev.lokspel.practice.manager.leaderboard.types.LbSecondaryType;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

@Getter
@AllArgsConstructor
public class Leaderboard {

  private final LbMainType mainType;
  private final LbSecondaryType secondaryType;
  private final NormalLadder ladder;
  @Setter private Map<OfflinePlayer, Integer> list;

  public boolean isEmpty() {
    return list == null || list.isEmpty();
  }
}
