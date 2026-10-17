package dev.lokspel.practice.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Path;
import java.time.Duration;
import me.whereareiam.anvil.api.player.SimulatedPlayer;
import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.anvil.api.type.ProcessState;
import me.whereareiam.anvil.capability.inventory.Inventory;
import me.whereareiam.anvil.capability.messages.Messages;
import me.whereareiam.anvil.capability.server.Server;
import me.whereareiam.anvil.capability.session.Session;

/** What a player and the console observe of the plugin on a running server. */
public final class PracticeJourney {
  private static final Duration WAIT = Duration.ofSeconds(20);
  private static final int JOIN_ATTEMPTS = 15;

  private PracticeJourney() {}

  public static Path dataFolder(ScenarioContext anvil) {
    return anvil
        .processes()
        .server(PracticeScenarios.SERVER)
        .workDirectory()
        .resolve("plugins/AstralPractice");
  }

  public static void assertEnabled(ScenarioContext anvil) {
    var server = anvil.processes().server(PracticeScenarios.SERVER);
    assertEquals(ProcessState.READY, server.state());
    String log = String.join("\n", server.console().tail(2000));
    assertFalse(log.contains("Couldn't load the configuration files"), log);
    assertFalse(log.contains("Error occurred while enabling AstralPractice"), log);
  }

  public static SimulatedPlayer join(ScenarioContext anvil, String name)
      throws InterruptedException {
    SimulatedPlayer player = anvil.players().create(name);
    Session session = player.capability(Session.class);
    // The plugin kicks players ("still loading") until its asynchronous startup is done, which
    // ends shortly after Paper reports it is ready.
    for (int attempt = 1; ; attempt++) {
      try {
        session.connect();
        session.connected();
        break;
      } catch (IllegalStateException e) {
        if (attempt == JOIN_ATTEMPTS
            || !String.valueOf(e.getMessage()).contains("did not connect")) {
          throw e;
        }
        Thread.sleep(1000);
      }
    }
    player.capability(Server.class).joined(PracticeScenarios.SERVER);
    return player;
  }

  /** The lobby items come from inventories.yml. */
  public static void assertLobbyItems(SimulatedPlayer player) {
    player
        .capability(Inventory.class)
        .matching(
            state -> state.getItems().stream().filter(item -> item.getAmount() > 0).count() >= 3,
            WAIT);
  }

  public static void command(SimulatedPlayer player, String command, String expected) {
    Messages messages = player.capability(Messages.class);
    int before = messages.checkpoint();
    messages.command(command);
    try {
      messages.received(text -> text.contains(expected), before, WAIT);
    } catch (RuntimeException e) {
      throw new AssertionError(
          "/" + command + " did not answer '" + expected + "'; received: " + messages.history(), e);
    }
  }

  public static void console(ScenarioContext anvil, String command, String expected) {
    var console = anvil.processes().server(PracticeScenarios.SERVER).console();
    long before = console.checkpoint();
    console.sendCommand(command);
    console.await(expected, before, WAIT);
  }

  public static void assertReceived(SimulatedPlayer player, int before, String expected) {
    Messages messages = player.capability(Messages.class);
    try {
      messages.received(text -> text.contains(expected), before, WAIT);
    } catch (RuntimeException e) {
      throw new AssertionError(
          player.name() + " did not receive '" + expected + "'; received: " + messages.history(),
          e);
    }
  }
}
