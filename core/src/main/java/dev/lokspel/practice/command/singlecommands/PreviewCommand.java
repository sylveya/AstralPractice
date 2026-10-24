package dev.lokspel.practice.command.singlecommands;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class PreviewCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return false;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);

    switch (profile.getStatus()) {
      case MATCH, FFA, EVENT, SPECTATE -> {
        Common.sendMmMessage(
            player, PracticeConfig.language().getCommand().getPreview().getCantUse());
        return false;
      }
      default -> {}
    }

    if (args.length != 1) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPreview()
              .getCommandHelp()
              .replace("%label%", label));
      return false;
    }

    NormalLadder ladder = LadderManager.getInstance().getLadder(args[0]);
    if (ladder == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPreview()
              .getLadderNotExists()
              .replace("%ladder%", args[0]));
      return false;
    }

    if (!ladder.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPreview()
              .getLadderDisabled()
              .replace("%ladder%", ladder.getDisplayName()));
      return false;
    }

    ladder.getPreviewGui().open(player);

    return true;
  }

  @Override
  public List<String> onTabComplete(
      CommandSender sender, Command command, String alias, String[] args) {
    List<String> arguments = new ArrayList<>();
    List<String> completion = new ArrayList<>();
    if (!(sender instanceof Player player)) {
      return arguments;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (profile.getStatus().equals(ProfileStatus.MATCH)
        || profile.getStatus().equals(ProfileStatus.SPECTATE)) {
      return arguments;
    }

    if (args.length == 1) {
      for (Ladder ladder : LadderManager.getInstance().getLadders()) {
        if (ladder.isEnabled()) {
          arguments.add(ladder.getName());
        }
      }

      StringUtil.copyPartialMatches(args[0], arguments, completion);
    }

    Collections.sort(completion);
    return completion;
  }
}
