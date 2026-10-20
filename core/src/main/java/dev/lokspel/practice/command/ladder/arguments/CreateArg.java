package dev.lokspel.practice.command.ladder.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.gui.guis.ladder.LadderCreateGui;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.util.Common;
import java.text.Normalizer;
import org.bukkit.entity.Player;

public final class CreateArg {

  private CreateArg() {}

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
              .getCreate()
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

    String name = args[1];
    String response = checkName(name);
    if (response != null) {
      Common.sendMmMessage(player, response);
      return;
    }

    new LadderCreateGui(name).open(player);
  }

  public static String checkName(String input) {
    if (input.isEmpty()) {
      return PracticeConfig.language().getLadder().getCreate().getNoName();
    } else if (input.contains(" ")) {
      return PracticeConfig.language().getLadder().getCreate().getOnlyAlphanumeric();
    } else if (!Normalizer.isNormalized(input, Normalizer.Form.NFD)) {
      return PracticeConfig.language().getLadder().getCreate().getNoAccents();
    } else if (input.length() < 2) {
      return PracticeConfig.language().getLadder().getCreate().getFewChar();
    } else if (input.length() > 20) {
      return PracticeConfig.language().getLadder().getCreate().getManyChar();
    } else if (LadderManager.getInstance().getLadder(input) != null) {
      return PracticeConfig.language().getLadder().getCreate().getNameTaken();
    } else {
      return null;
    }
  }
}
