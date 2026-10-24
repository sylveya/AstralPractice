package dev.lokspel.practice.manager.fight.match.listener;

import dev.lokspel.api.event.match.MatchRoundStartEvent;
import dev.lokspel.api.event.match.MatchStartEvent;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.settings.MatchSettings;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.server.ServerManager;
import java.util.List;
import java.util.Map;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class StartListener implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onMatchStart(MatchStartEvent e) {
    Match match = (Match) e.getMatch();

    if (!(match.getLadder() instanceof NormalLadder ladder)) {
      return;
    }

    MatchSettings.StartCommand startCommand = getStartCommand(ladder);
    List<String> commands = startCommand != null ? startCommand.getMatch() : null;
    if (commands != null) {
      for (String command : commands) {
        e.getMatch()
            .getPlayers()
            .forEach(
                player ->
                    ServerManager.runConsoleCommand(command.replace("%player%", player.getName())));
      }
    }
  }

  @EventHandler
  public void onMatchRoundStart(MatchRoundStartEvent e) {
    Match match = (Match) e.getMatch();

    if (!(match.getLadder() instanceof NormalLadder ladder)) {
      return;
    }

    MatchSettings.StartCommand startCommand = getStartCommand(ladder);
    List<String> commands = startCommand != null ? startCommand.getRound() : null;
    if (commands != null) {
      for (String command : commands) {
        e.getMatch()
            .getPlayers()
            .forEach(
                player ->
                    ServerManager.runConsoleCommand(command.replace("%player%", player.getName())));
      }
    }
  }

  private static MatchSettings.StartCommand getStartCommand(NormalLadder ladder) {
    Map<String, MatchSettings.StartCommand> startCommands =
        PracticeConfig.settings().getMatchSettings().getStartCommand();
    return startCommands != null ? startCommands.get(ladder.getName().toUpperCase()) : null;
  }
}
