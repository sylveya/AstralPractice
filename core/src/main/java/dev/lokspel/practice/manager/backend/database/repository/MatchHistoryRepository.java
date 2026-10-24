package dev.lokspel.practice.manager.backend.database.repository;

import dev.lokspel.practice.manager.backend.database.model.MatchHistoryRow;
import dev.lokspel.practice.manager.matchhistory.MatchHistoryEntry;
import dev.lokspel.practice.util.Common;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.sql.DataSource;
import lombok.Getter;

@Getter
public class MatchHistoryRepository {

  private static final String SELECT_HISTORY =
      "SELECT * FROM match_history WHERE uuid = ? ORDER BY matchId DESC LIMIT ?";

  private static final String DELETE_STALE =
      "DELETE FROM match_history WHERE uuid = ? AND matchId <= ("
          + "SELECT matchId FROM ("
          + "SELECT matchId FROM match_history WHERE uuid = ? "
          + "ORDER BY matchId DESC LIMIT 1 OFFSET ?"
          + ") AS stale"
          + ")";

  private static final String INSERT_MATCH =
      "INSERT INTO match_history (uuid, opponentUuid, username, opponentName, kitName, "
          + "arenaName, score, opponentScore, finalHealth, opponentFinalHealth, "
          + "winnerUuid, matchDuration, playedAt) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final String DELETE_HISTORY = "DELETE FROM match_history WHERE uuid = ?";

  private final ExecutorService executor;
  private final DataSource dataSource;

  public MatchHistoryRepository(DataSource dataSource, ExecutorService executor) {
    this.dataSource = dataSource;
    this.executor = executor;
  }

  public CompletableFuture<Void> save(
      MatchHistoryRow playerPov, MatchHistoryRow opponentPov, int retainPerPlayer) {
    return CompletableFuture.runAsync(
        () -> {
          try (Connection connection = dataSource.getConnection()) {
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
              insert(connection, playerPov);
              insert(connection, opponentPov);

              // Each row is stored from one player's point of view, so the two rows'
              // own uuids are the two participants.
              prune(connection, UUID.fromString(playerPov.getUuid()), retainPerPlayer);
              prune(connection, UUID.fromString(opponentPov.getUuid()), retainPerPlayer);

              connection.commit();
            } catch (SQLException e) {
              connection.rollback();
              throw e;
            } finally {
              connection.setAutoCommit(autoCommit);
            }
          } catch (SQLException e) {
            Common.sendConsoleMmMessage(
                "<red>[Database] save match history error: " + e.getMessage());
          }
        },
        executor);
  }

  public CompletableFuture<List<MatchHistoryEntry>> getHistory(UUID playerUuid, int limit) {
    return CompletableFuture.supplyAsync(
        () -> {
          try (Connection connection = dataSource.getConnection();
              PreparedStatement statement = connection.prepareStatement(SELECT_HISTORY)) {

            statement.setString(1, playerUuid.toString());
            statement.setInt(2, Math.max(1, limit));

            List<MatchHistoryEntry> entries = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
              while (resultSet.next()) {
                entries.add(toEntry(read(resultSet)));
              }
            }
            return entries;
          } catch (SQLException e) {
            Common.sendConsoleMmMessage(
                "<red>[Database] load match history error: " + e.getMessage());
            return List.of();
          }
        },
        executor);
  }

  public CompletableFuture<Void> delete(UUID uuid) {
    return CompletableFuture.runAsync(
        () -> {
          try (Connection connection = dataSource.getConnection();
              PreparedStatement statement = connection.prepareStatement(DELETE_HISTORY)) {

            statement.setString(1, uuid.toString());
            statement.executeUpdate();
          } catch (SQLException e) {
            Common.sendConsoleMmMessage(
                "<red>[Database] delete match history error: " + e.getMessage());
          }
        },
        executor);
  }

  /**
   * Drops everything a player has beyond their newest {@code retainPerPlayer} rows, by cutting at
   * the id of their next-oldest match. History is stored per participant, so a shared match id can
   * be deleted without touching the other player's copy of an earlier match.
   *
   * <p>The subquery is wrapped in a derived table because MySQL and MariaDB both refuse to read the
   * table a DELETE is removing from.
   */
  private void prune(Connection connection, UUID playerUuid, int retainPerPlayer)
      throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(DELETE_STALE)) {
      statement.setString(1, playerUuid.toString());
      statement.setString(2, playerUuid.toString());
      statement.setInt(3, retainPerPlayer);
      statement.executeUpdate();
    }
  }

  private static void insert(Connection connection, MatchHistoryRow row) throws SQLException {
    try (PreparedStatement statement = connection.prepareStatement(INSERT_MATCH)) {
      statement.setString(1, row.getUuid());
      statement.setString(2, row.getOpponentUuid());
      statement.setString(3, row.getUsername());
      statement.setString(4, row.getOpponentName());
      statement.setString(5, row.getKitName());
      statement.setString(6, row.getArenaName());
      statement.setInt(7, row.getScore());
      statement.setInt(8, row.getOpponentScore());
      statement.setDouble(9, row.getFinalHealth());
      statement.setDouble(10, row.getOpponentFinalHealth());
      statement.setString(11, row.getWinnerUuid());
      statement.setInt(12, row.getMatchDuration());
      statement.setLong(13, row.getPlayedAt());
      statement.executeUpdate();
    }
  }

  private static MatchHistoryRow read(ResultSet resultSet) throws SQLException {
    MatchHistoryRow row = new MatchHistoryRow();
    row.setMatchId(resultSet.getInt("matchId"));
    row.setUuid(resultSet.getString("uuid"));
    row.setOpponentUuid(resultSet.getString("opponentUuid"));
    row.setUsername(resultSet.getString("username"));
    row.setOpponentName(resultSet.getString("opponentName"));
    row.setKitName(resultSet.getString("kitName"));
    row.setArenaName(resultSet.getString("arenaName"));
    row.setScore(resultSet.getInt("score"));
    row.setOpponentScore(resultSet.getInt("opponentScore"));
    row.setFinalHealth(resultSet.getDouble("finalHealth"));
    row.setOpponentFinalHealth(resultSet.getDouble("opponentFinalHealth"));
    row.setWinnerUuid(resultSet.getString("winnerUuid"));
    row.setMatchDuration(resultSet.getInt("matchDuration"));
    row.setPlayedAt(resultSet.getLong("playedAt"));
    return row;
  }

  private static MatchHistoryEntry toEntry(MatchHistoryRow row) {
    String winner = row.getWinnerUuid();
    return new MatchHistoryEntry(
        row.getMatchId(),
        UUID.fromString(row.getUuid()),
        UUID.fromString(row.getOpponentUuid()),
        row.getUsername(),
        row.getOpponentName(),
        row.getKitName(),
        row.getArenaName(),
        row.getScore(),
        row.getOpponentScore(),
        row.getFinalHealth(),
        row.getOpponentFinalHealth(),
        winner == null || winner.isEmpty() ? null : UUID.fromString(winner),
        row.getMatchDuration(),
        row.getPlayedAt());
  }
}
