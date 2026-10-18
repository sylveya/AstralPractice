package dev.lokspel.practice.manager.backend.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

final class Schema {

  private static final String GLOBAL_STATS =
      """
      CREATE TABLE IF NOT EXISTS global_stats (
          id INT NOT NULL AUTO_INCREMENT,
          username VARCHAR(100) NOT NULL,
          uuid VARCHAR(36) NOT NULL,
          firstJoin BIGINT NOT NULL DEFAULT 0,
          lastJoin BIGINT NOT NULL DEFAULT 0,
          unrankedWins INT DEFAULT 0,
          unrankedLosses INT DEFAULT 0,
          rankedWins INT DEFAULT 0,
          rankedLosses INT DEFAULT 0,
          globalElo INT DEFAULT 0,
          globalRank VARCHAR(100) DEFAULT NULL,
          experience INT DEFAULT 0,
          winStreak INT DEFAULT 0,
          bestWinStreak INT DEFAULT 0,
          loseStreak INT DEFAULT 0,
          bestLoseStreak INT DEFAULT 0,

          PRIMARY KEY (id),
          UNIQUE KEY uk_global_stats_uuid (uuid)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
      """;

  private static final String LADDER_STATS =
      """
      CREATE TABLE IF NOT EXISTS ladder_stats (
          id INT NOT NULL AUTO_INCREMENT,
          username VARCHAR(100) NOT NULL,
          uuid VARCHAR(36) NOT NULL,
          ladder VARCHAR(100) NOT NULL,

          unrankedWins INT DEFAULT 0,
          unrankedLosses INT DEFAULT 0,
          unrankedWinStreak INT DEFAULT 0,
          unrankedBestWinStreak INT DEFAULT 0,
          unrankedLoseStreak INT DEFAULT 0,
          unrankedBestLoseStreak INT DEFAULT 0,

          rankedWins INT DEFAULT 0,
          rankedLosses INT DEFAULT 0,
          rankedWinStreak INT DEFAULT 0,
          rankedBestWinStreak INT DEFAULT 0,
          rankedLoseStreak INT DEFAULT 0,
          rankedBestLoseStreak INT DEFAULT 0,

          elo INT DEFAULT 0,
          rank VARCHAR(100) DEFAULT NULL,
          kills INT DEFAULT 0,
          deaths INT DEFAULT 0,

          PRIMARY KEY (id),
          UNIQUE KEY uk_ladder_stats_uuid_ladder (uuid, ladder)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
      """;

  private static final String MATCH_HISTORY =
      """
      CREATE TABLE IF NOT EXISTS match_history (
          matchId INT NOT NULL AUTO_INCREMENT,
          uuid VARCHAR(36) NOT NULL,
          opponentUuid VARCHAR(36) NOT NULL,
          username VARCHAR(64) NOT NULL,
          opponentName VARCHAR(64) NOT NULL,
          kitName VARCHAR(100) NOT NULL,
          arenaName VARCHAR(100) NOT NULL,

          score INT DEFAULT 0,
          opponentScore INT DEFAULT 0,
          finalHealth DOUBLE DEFAULT 0,
          opponentFinalHealth DOUBLE DEFAULT 0,
          winnerUuid VARCHAR(36) DEFAULT NULL,
          matchDuration INT DEFAULT 0,
          playedAt BIGINT NOT NULL,

          PRIMARY KEY (matchId),
          KEY idx_mh_uuid (uuid),
          KEY idx_mh_opponent_uuid (opponentUuid)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
      """;

  private static final String FFA_GLOBAL_STATS =
      """
      CREATE TABLE IF NOT EXISTS ffa_global_stats (
          id INT NOT NULL AUTO_INCREMENT,
          username VARCHAR(100) NOT NULL,
          uuid VARCHAR(36) NOT NULL,
          kills INT DEFAULT 0,
          deaths INT DEFAULT 0,
          bestKillStreak INT DEFAULT 0,

          PRIMARY KEY (id),
          UNIQUE KEY uk_ffa_global_stats_uuid (uuid)
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
      """;

  static void apply(DataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {

      statement.executeUpdate(GLOBAL_STATS);
      statement.executeUpdate(LADDER_STATS);
      statement.executeUpdate(MATCH_HISTORY);
      statement.executeUpdate(FFA_GLOBAL_STATS);
    }
  }
}
