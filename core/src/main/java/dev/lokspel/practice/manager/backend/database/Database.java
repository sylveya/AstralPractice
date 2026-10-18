package dev.lokspel.practice.manager.backend.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.lokspel.practice.manager.backend.database.repository.FfaGlobalStatsRepository;
import dev.lokspel.practice.manager.backend.database.repository.GlobalStatsRepository;
import dev.lokspel.practice.manager.backend.database.repository.LadderStatsRepository;
import dev.lokspel.practice.manager.backend.database.repository.MatchHistoryRepository;
import dev.lokspel.practice.util.Common;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

@Getter
public class Database {

  private final HikariDataSource dataSource;
  private final ExecutorService executor;

  private final GlobalStatsRepository globalStatsRepository;
  private final LadderStatsRepository ladderStatsRepository;
  private final MatchHistoryRepository matchHistoryRepository;
  private final FfaGlobalStatsRepository ffaGlobalStatsRepository;

  private Database(HikariDataSource dataSource) throws SQLException {
    this.dataSource = dataSource;
    this.executor = Executors.newFixedThreadPool(Math.max(2, dataSource.getMaximumPoolSize() / 2));

    Schema.apply(dataSource);

    this.globalStatsRepository = new GlobalStatsRepository(dataSource, executor);
    this.ladderStatsRepository = new LadderStatsRepository(dataSource, executor);
    this.matchHistoryRepository = new MatchHistoryRepository(dataSource, executor);
    this.ffaGlobalStatsRepository = new FfaGlobalStatsRepository(dataSource, executor);
  }

  public static Database forMariaDb(
      String host, int port, String database, String user, String password, int poolSize)
      throws Exception {
    // The driver is shaded into this jar, so it has to be named by its relocated path.
    Class.forName("dev.lokspel.practice.dependencies.mariadb.Driver");

    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(
        "jdbc:mariadb://"
            + host
            + ":"
            + port
            + "/"
            + database
            + "?useSsl=false&characterEncoding=utf8");
    config.setUsername(user);
    config.setPassword(password);
    config.setPoolName("AstralPractice-Mariadb");
    config.setMaximumPoolSize(poolSize);
    config.setMinimumIdle(1);
    config.setConnectionTimeout(10000L);
    config.setValidationTimeout(5000L);
    config.setLeakDetectionThreshold(0L);

    Database opened = new Database(new HikariDataSource(config));
    Common.sendConsoleMmMessage("<gray>Connected to Mariadb database <white>" + database);
    return opened;
  }

  public void close() {
    executor.shutdown();

    try {
      // Pending saves get a few seconds to finish; whatever is left is dropped.
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        Common.sendConsoleMmMessage("<red>Database tasks did not finish in time, cancelling them.");
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }

    dataSource.close();
  }
}
