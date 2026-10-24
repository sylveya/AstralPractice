package dev.lokspel.practice.command.ladder.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class InfoArg {

  private InfoArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.setup")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getLadder().getNoPermission());
      return;
    }

    if (args.length != 2) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getInfo()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    Ladder ladder = LadderManager.getInstance().getLadder(args[1]);
    if (ladder == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getInfo()
              .getNotExists()
              .replace("%ladder%", args[1]));
      return;
    }

    var infoConfig = PracticeConfig.language().getCommand().getLadder().getArguments().getInfo();
    var statusNames = infoConfig.getStatusNames();

    String iconStatus = ladder.getIcon() != null ? statusNames.getSet() : statusNames.getNotSet();
    String invStatus = ladder.getKitData().isSet() ? statusNames.getSet() : statusNames.getNotSet();
    String status = ladder.isEnabled() ? statusNames.getEnabled() : statusNames.getDisabled();
    String effectsCount = String.valueOf(ladder.getKitData().getEffects().size());

    for (String line : infoConfig.getLadderInfo()) {
      Common.sendMmMessage(
          player,
          line.replace("%ladder%", ladder.getName())
              .replace("%type%", ladder.getType().getName())
              .replace("%icon%", iconStatus)
              .replace("%displayName%", ladder.getDisplayName())
              .replace("%inventory%", invStatus)
              .replace("%effects%", effectsCount)
              .replace("%status%", status));
    }
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.setup") || args.length != 2) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[1], LadderManager.getInstance().getLadderNames(), new ArrayList<>());
  }
}
