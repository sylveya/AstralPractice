package dev.lokspel.practice.chat;

import dev.lokspel.practice.config.PracticeJourney;
import dev.lokspel.practice.config.PracticeScenarios;
import me.whereareiam.anvil.api.player.SimulatedPlayer;
import me.whereareiam.anvil.api.scenario.ScenarioContext;
import me.whereareiam.anvil.capability.messages.Messages;
import me.whereareiam.anvil.integration.junit.AnvilTest;
import org.junit.jupiter.api.Test;

class MiniMessageInjectionTest {

  @Test
  @AnvilTest(PracticeScenarios.FreshInstall.class)
  void testPrivateMessageMiniMessageTagInjection(ScenarioContext anvil) throws Exception {
    PracticeJourney.assertEnabled(anvil);

    SimulatedPlayer alice = PracticeJourney.join(anvil, "Alice");
    SimulatedPlayer bob = PracticeJourney.join(anvil, "Bob");

    int beforeBob = bob.capability(Messages.class).checkpoint();
    String payload = "<click:run_command:'/op Bob'>Click</click>";

    PracticeJourney.command(alice, "tell Bob " + payload, payload);
    PracticeJourney.assertReceived(bob, beforeBob, payload);

    int beforeAlice = alice.capability(Messages.class).checkpoint();

    PracticeJourney.command(bob, "reply " + payload, payload);
    PracticeJourney.assertReceived(alice, beforeAlice, payload);

    alice.destroy();
    bob.destroy();
  }
}
