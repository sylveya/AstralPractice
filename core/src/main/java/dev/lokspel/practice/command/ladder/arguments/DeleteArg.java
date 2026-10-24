package dev.lokspel.practice.command.ladder.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class DeleteArg {

  private DeleteArg() {}

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
              .getDelete()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    NormalLadder ladder = LadderManager.getInstance().getLadder(args[1]);
    if (ladder == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getDelete()
              .getNotExists()
              .replace("%ladder%", args[1]));
      return;
    }

    if (ladder.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getDelete()
              .getLadderEnabled()
              .replace("%ladder%", ladder.getDisplayName()));
      return;
    }

    // Delete the ladder data.
    ladder.deleteData();
    // Delete the ladder from the ladders list.
    LadderManager.getInstance().getLadders().remove(ladder);
    LadderManager.getInstance().getLadders().sort(Comparator.comparing(Ladder::getName));

    // Remove the ladder from the arenas and update the ladder GUI in the arena settings.
    ArenaManager.getInstance().removeLadder(ladder);
    for (Map<GuiType, Gui> map : ArenaGuiSetupManager.getInstance().getArenaSetupGuis().values()) {
      map.get(GuiType.Arena_Ladders_Single).update();
    }

    GuiManager.getInstance().searchGui(GuiType.Queue_Unranked).update(true);
    if (ladder.isRanked()) {
      GuiManager.getInstance().searchGui(GuiType.Queue_Ranked).update(true);
    }

    // Update GUIs.
    LadderSetupManager.getInstance().removeLadderGuis(ladder);

    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getLadder()
            .getArguments()
            .getDelete()
            .getDeleteSuccess()
            .replace("%ladder%", ladder.getDisplayName()));
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.setup") || args.length != 2) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[1],
        LadderManager.getInstance().getLadderNames(ladder -> !ladder.isEnabled()),
        new ArrayList<>());
  }
}
