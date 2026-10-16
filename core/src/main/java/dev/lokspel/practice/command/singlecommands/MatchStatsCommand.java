package dev.lokspel.practice.command.singlecommands;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.enums.MatchStatus;
import dev.lokspel.practice.util.Common;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MatchStatsCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return false;
    }

    Match playerMatch = MatchManager.getInstance().getLiveMatchByPlayer(player);
    if (playerMatch != null
        && playerMatch.getStatus() != MatchStatus.END
        && playerMatch.getStatus() != MatchStatus.OVER) {
      return false;
    }

    if (args.length != 3) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getMatchStat().getCommandHelp());
      return false;
    }

    Match match = MatchManager.getInstance().getMatches().get(args[0]);
    if (match == null) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getMatchStat().getInvalidMatchId());
      return false;
    }

    UUID target = UUID.fromString(args[1]);
    if (!match.getMatchStatsGuis().containsKey(target)) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getMatchStat().getInvalidPlayer());
      return false;
    }

    int round = Integer.parseInt(args[2]);
    if (!match.getMatchStatsGuis().get(target).getGui().containsKey(round)) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getMatchStat().getInvalidRound());
      return false;
    }

    match.getMatchStatsGuis().get(target).open(player, round);

    return true;
  }
}
