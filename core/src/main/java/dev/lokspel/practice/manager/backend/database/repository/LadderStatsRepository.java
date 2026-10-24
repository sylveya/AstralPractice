package dev.lokspel.practice.manager.backend.database.repository;

import dev.lokspel.practice.manager.backend.database.model.LadderStatsRow;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.statistics.LadderStats;
import dev.lokspel.practice.util.Common;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import javax.sql.DataSource;
import lombok.Getter;

@Getter
public class LadderStatsRepository {

  private static final String SELECT_STATS = "SELECT * FROM ladder_stats WHERE uuid = ?";

  private static final String INSERT_STATS =
      "INSERT INTO ladder_stats (username, uuid, ladder, unrankedWins, unrankedLosses,"
          + " unrankedWinStreak, unrankedBestWinStreak, unrankedLoseStreak, unrankedBestLoseStreak,"
          + " rankedWins, rankedLosses, rankedWinStreak, rankedBestWinStreak, rankedLoseStreak,"
          + " rankedBestLoseStreak, elo, kills, deaths) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
          + " ?, ?, ?, ?, ?, ?)";

  private static final String DELETE_BY_PLAYER = "DELETE FROM ladder_stats WHERE uuid = ?";
  private static final String DELETE_BY_LADDER = "DELETE FROM ladder_stats WHERE ladder = ?";

  private final ExecutorService executor;
  private final DataSource dataSource;

  public LadderStatsRepository(DataSource dataSource, ExecutorService executor) {
    this.dataSource = dataSource;
    this.executor = executor;
  }

  public CompletableFuture<List<LadderStatsRow>> getStats(UUID uuid) {
    return CompletableFuture.supplyAsync(
        () -> {
          try (Connection connection = dataSource.getConnection();
              PreparedStatement statement = connection.prepareStatement(SELECT_STATS)) {

            statement.setString(1, uuid.toString());

            List<LadderStatsRow> rows = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
              while (resultSet.next()) {
                rows.add(read(resultSet));
              }
            }
            return rows;
          } catch (SQLException e) {
            Common.sendConsoleMmMessage(
                "<red>[Database] get ladder stats error: " + e.getMessage());
            return List.of();
          }
        },
        executor);
  }

  public CompletableFuture<Void> save(Profile profile) {
    return CompletableFuture.runAsync(
        () -> {
          try (Connection connection = dataSource.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
              replaceRows(connection, profile);
              connection.commit();
            } catch (SQLException e) {
              connection.rollback();
              throw e;
            } finally {
              connection.setAutoCommit(previousAutoCommit);
            }
          } catch (SQLException e) {
            Common.sendConsoleMmMessage(
                "<red>[Database] save ladder stats error: " + e.getMessage());
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
                replaceRows(connection, profile);
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
                "<red>[Database] save ladder stats error: " + e.getMessage());
          }
        },
        executor);
  }

  public CompletableFuture<Void> delete(UUID uuid) {
    return CompletableFuture.runAsync(
        () -> {
          try (Connection connection = dataSource.getConnection();
              PreparedStatement statement = connection.prepareStatement(DELETE_BY_PLAYER)) {

            statement.setString(1, uuid.toString());
            statement.executeUpdate();
          } catch (SQLException e) {
            Common.sendConsoleMmMessage(
                "<red>[Database] delete ladder stats error: " + e.getMessage());
          }
        },
        executor);
  }

  public CompletableFuture<Void> deleteLadder(String ladderName) {
    return CompletableFuture.runAsync(
        () -> {
          try (Connection connection = dataSource.getConnection();
              PreparedStatement statement = connection.prepareStatement(DELETE_BY_LADDER)) {

            statement.setString(1, ladderName);
            statement.executeUpdate();
          } catch (SQLException e) {
            Common.sendConsoleMmMessage(
                "<red>[Database] delete ladder stats error: " + e.getMessage());
          }
        },
        executor);
  }

  private void replaceRows(Connection connection, Profile profile) throws SQLException {
    try (PreparedStatement delete = connection.prepareStatement(DELETE_BY_PLAYER)) {
      delete.setString(1, profile.getUuid().toString());
      delete.executeUpdate();
    }

    List<LadderStatsRow> rows = toRows(profile);
    if (rows.isEmpty()) {
      return;
    }

    try (PreparedStatement insert = connection.prepareStatement(INSERT_STATS)) {
      for (LadderStatsRow row : rows) {
        bind(insert, row);
        insert.addBatch();
      }
      insert.executeBatch();
    }
  }

  private static void bind(PreparedStatement statement, LadderStatsRow row) throws SQLException {
    statement.setString(1, row.getUsername());
    statement.setString(2, row.getUuid());
    statement.setString(3, row.getLadder());
    statement.setInt(4, row.getUnrankedWins());
    statement.setInt(5, row.getUnrankedLosses());
    statement.setInt(6, row.getUnrankedWinStreak());
    statement.setInt(7, row.getUnrankedBestWinStreak());
    statement.setInt(8, row.getUnrankedLoseStreak());
    statement.setInt(9, row.getUnrankedBestLoseStreak());
    statement.setInt(10, row.getRankedWins());
    statement.setInt(11, row.getRankedLosses());
    statement.setInt(12, row.getRankedWinStreak());
    statement.setInt(13, row.getRankedBestWinStreak());
    statement.setInt(14, row.getRankedLoseStreak());
    statement.setInt(15, row.getRankedBestLoseStreak());
    statement.setInt(16, row.getElo());
    statement.setInt(17, row.getKills());
    statement.setInt(18, row.getDeaths());
  }

  private static List<LadderStatsRow> toRows(Profile profile) {
    Map<NormalLadder, LadderStats> ladderStats = profile.getStats().getLadderStats();
    if (ladderStats.isEmpty()) {
      return List.of();
    }

    List<Map.Entry<NormalLadder, LadderStats>> ordered = new ArrayList<>(ladderStats.entrySet());
    ordered.sort(
        Comparator.comparing(entry -> entry.getKey().getName(), String.CASE_INSENSITIVE_ORDER));

    String username = profile.getStoredName();
    List<LadderStatsRow> rows = new ArrayList<>(ordered.size());
    for (Map.Entry<NormalLadder, LadderStats> entry : ordered) {
      rows.add(toRow(profile, entry.getKey(), entry.getValue(), username));
    }
    return rows;
  }

  private static LadderStatsRow toRow(
      Profile profile, NormalLadder ladder, LadderStats stats, String username) {
    LadderStatsRow row = new LadderStatsRow();
    row.setUuid(profile.getUuid().toString());
    row.setUsername(username);
    row.setLadder(ladder.getName());
    row.setUnrankedWins(stats.getUnRankedWins());
    row.setUnrankedLosses(stats.getUnRankedLosses());
    row.setUnrankedWinStreak(stats.getUnRankedWinStreak());
    row.setUnrankedBestWinStreak(stats.getUnRankedBestWinStreak());
    row.setUnrankedLoseStreak(stats.getUnRankedLoseStreak());
    row.setUnrankedBestLoseStreak(stats.getUnRankedBestLoseStreak());
    row.setRankedWins(stats.getRankedWins());
    row.setRankedLosses(stats.getRankedLosses());
    row.setRankedWinStreak(stats.getRankedWinStreak());
    row.setRankedBestWinStreak(stats.getRankedBestWinStreak());
    row.setRankedLoseStreak(stats.getRankedLoseStreak());
    row.setRankedBestLoseStreak(stats.getRankedBestLoseStreak());
    row.setElo(stats.getElo());
    row.setKills(stats.getKills());
    row.setDeaths(stats.getDeaths());
    return row;
  }

  private static LadderStatsRow read(ResultSet resultSet) throws SQLException {
    LadderStatsRow row = new LadderStatsRow();
    row.setId(resultSet.getInt("id"));
    row.setUuid(resultSet.getString("uuid"));
    row.setUsername(resultSet.getString("username"));
    row.setLadder(resultSet.getString("ladder"));
    row.setUnrankedWins(resultSet.getInt("unrankedWins"));
    row.setUnrankedLosses(resultSet.getInt("unrankedLosses"));
    row.setUnrankedWinStreak(resultSet.getInt("unrankedWinStreak"));
    row.setUnrankedBestWinStreak(resultSet.getInt("unrankedBestWinStreak"));
    row.setUnrankedLoseStreak(resultSet.getInt("unrankedLoseStreak"));
    row.setUnrankedBestLoseStreak(resultSet.getInt("unrankedBestLoseStreak"));
    row.setRankedWins(resultSet.getInt("rankedWins"));
    row.setRankedLosses(resultSet.getInt("rankedLosses"));
    row.setRankedWinStreak(resultSet.getInt("rankedWinStreak"));
    row.setRankedBestWinStreak(resultSet.getInt("rankedBestWinStreak"));
    row.setRankedLoseStreak(resultSet.getInt("rankedLoseStreak"));
    row.setRankedBestLoseStreak(resultSet.getInt("rankedBestLoseStreak"));
    row.setElo(resultSet.getInt("elo"));
    row.setRank(resultSet.getString("rank"));
    row.setKills(resultSet.getInt("kills"));
    row.setDeaths(resultSet.getInt("deaths"));
    return row;
  }
}
