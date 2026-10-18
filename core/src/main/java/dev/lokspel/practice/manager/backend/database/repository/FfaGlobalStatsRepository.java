package dev.lokspel.practice.manager.backend.database.repository;

import dev.lokspel.practice.manager.backend.database.model.FfaGlobalStatsRow;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.statistics.FfaStats;
import dev.lokspel.practice.util.Common;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.sql.DataSource;
import lombok.Getter;

@Getter
public class FfaGlobalStatsRepository {

  private static final String SELECT_STATS = "SELECT * FROM ffa_global_stats WHERE uuid = ?";

  private static final String UPSERT_STATS =
      "INSERT INTO ffa_global_stats (username, uuid, kills, deaths, bestKillStreak)"
          + " VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE username = VALUES(username),"
          + " kills = VALUES(kills), deaths = VALUES(deaths),"
          + " bestKillStreak = VALUES(bestKillStreak)";

  private static final String DELETE_STATS = "DELETE FROM ffa_global_stats WHERE uuid = ?";

  private final ExecutorService executor;
  private final DataSource dataSource;

  public FfaGlobalStatsRepository(DataSource dataSource, ExecutorService executor) {
    this.dataSource = dataSource;
    this.executor = executor;
  }

  public CompletableFuture<FfaGlobalStatsRow> getStats(UUID uuid) {
    return CompletableFuture.supplyAsync(
        () -> {
          try (Connection connection = dataSource.getConnection();
              PreparedStatement statement = connection.prepareStatement(SELECT_STATS)) {

            statement.setString(1, uuid.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
              return resultSet.next() ? read(resultSet) : null;
            }
          } catch (SQLException e) {
            Common.sendConsoleMmMessage(
                "<red>[Database] get ffa global stats error: " + e.getMessage());
            return null;
          }
        },
        executor);
  }

  public CompletableFuture<Void> save(Profile profile) {
    return CompletableFuture.runAsync(
        () -> {
          try (Connection connection = dataSource.getConnection()) {
            write(connection, profile);
          } catch (SQLException e) {
            Common.sendConsoleMmMessage(
                "<red>[Database] save ffa global stats error: " + e.getMessage());
          }
        },
        executor);
  }

  public CompletableFuture<Void> saveAll(Collection<Profile> profiles) {
    return CompletableFuture.runAsync(
        () -> {
          try (Connection connection = dataSource.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
              for (Profile profile : profiles) {
                write(connection, profile);
              }
              connection.commit();
            } catch (SQLException e) {
              connection.rollback();
              throw e;
            } finally {
              connection.setAutoCommit(previousAutoCommit);
            }
          } catch (SQLException e) {
            Common.sendConsoleMmMessage(
                "<red>[Database] save ffa global stats error: " + e.getMessage());
          }
        },
        executor);
  }

  public CompletableFuture<Void> delete(UUID uuid) {
    return CompletableFuture.runAsync(
        () -> {
          try (Connection connection = dataSource.getConnection();
              PreparedStatement statement = connection.prepareStatement(DELETE_STATS)) {

            statement.setString(1, uuid.toString());
            statement.executeUpdate();
          } catch (SQLException e) {
            Common.sendConsoleMmMessage(
                "<red>[Database] delete ffa global stats error: " + e.getMessage());
          }
        },
        executor);
  }

  private void write(Connection connection, Profile profile) throws SQLException {
    FfaStats ffaStats = profile.getStats().getFfaStats();

    try (PreparedStatement statement = connection.prepareStatement(UPSERT_STATS)) {
      statement.setString(1, profile.getStoredName());
      statement.setString(2, profile.getUuid().toString());
      statement.setInt(3, ffaStats.getKills());
      statement.setInt(4, ffaStats.getDeaths());
      statement.setInt(5, ffaStats.getBestKillStreak());
      statement.executeUpdate();
    }
  }

  private static FfaGlobalStatsRow read(ResultSet resultSet) throws SQLException {
    FfaGlobalStatsRow row = new FfaGlobalStatsRow();
    row.setId(resultSet.getInt("id"));
    row.setUuid(resultSet.getString("uuid"));
    row.setUsername(resultSet.getString("username"));
    row.setKills(resultSet.getInt("kills"));
    row.setDeaths(resultSet.getInt("deaths"));
    row.setBestKillStreak(resultSet.getInt("bestKillStreak"));
    return row;
  }
}
