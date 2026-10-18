package dev.lokspel.practice.manager.backend.database.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FfaGlobalStatsRow {

  private int id;
  private String uuid;
  private String username;
  private int kills;
  private int deaths;
  private int bestKillStreak;
}
