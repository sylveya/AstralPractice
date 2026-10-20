package dev.lokspel.practice.command.ladder.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class CloneArg {

  private CloneArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.setup")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getLadder().getNoPermission());
      return;
    }

    if (args.length != 3) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getClone()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    if (LadderManager.getInstance().isFull()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getCreate()
              .getReachedMax()
              .replace(
                  "%max%", String.valueOf(PracticeConfig.settings().getSetup().getMaxLadders())));
      return;
    }

    NormalLadder source = LadderManager.getInstance().getLadder(args[1]);
    if (source == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getClone()
              .getNotExists()
              .replace("%ladder%", args[1]));
      return;
    }

    String name = args[2];
    String response = CreateArg.checkName(name);
    if (response != null) {
      Common.sendMmMessage(player, response);
      return;
    }

    // Flush the source's live state (its kit in particular) before copying its file.
    source.setData();
    source.copyData(name);

    NormalLadder ladder = source.getType().create(name);

    LadderManager.getInstance().addLadder(ladder);

    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getLadder()
            .getArguments()
            .getClone()
            .getCloneSuccess()
            .replace("%ladder%", source.getDisplayName())
            .replace("%clone%", name));
  }

  public static List<String> tabComplete(Player player, String[] args) {
    List<String> completion = new ArrayList<>();
    if (!player.hasPermission("ap.setup") || args.length != 2) {
      return completion;
    }

    List<String> arguments = new ArrayList<>();
    for (Ladder ladder : LadderManager.getInstance().getLadders()) {
      arguments.add(ladder.getName());
    }

    return StringUtil.copyPartialMatches(args[1], arguments, completion);
  }
}
