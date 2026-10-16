package dev.lokspel.practice.command.practice.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.language.Command;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticPresetUtil;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class PresetArg {

  private PresetArg() {}

  private static Command.Practice.Arguments.Preset messages() {
    return PracticeConfig.language().getCommand().getPractice().getArguments().getPreset();
  }

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.practice.preset")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
      return;
    }

    if (args.length != 2 || !args[1].equalsIgnoreCase("add")) {
      for (String line : messages().getCommandHelp()) {
        Common.sendMmMessage(player, line.replace("%label%", label));
      }
      return;
    }

    CosmeticPresetUtil.createFromHeldItem(player, label);
  }

  public static List<String> tabComplete(Player player, String[] args) {
    List<String> completion = new ArrayList<>();
    if (args.length == 2 && player.hasPermission("ap.practice.preset")) {
      StringUtil.copyPartialMatches(args[1], List.of("add"), completion);
    }
    return completion;
  }
}
