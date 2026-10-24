package dev.lokspel.practice.command.singlecommands;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.gui.guis.DivisionGui;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DivisionsCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return false;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (!player.hasPermission("ap.divisions.view")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getDivisions().getNoPermission());
      return false;
    }

    if (!player.hasPermission("ap.admin")) {
      switch (profile.getStatus()) {
        case MATCH, FFA, EVENT -> {
          Common.sendMmMessage(
              player, PracticeConfig.language().getCommand().getDivisions().getCantUse());
          return false;
        }
        default -> {}
      }
    }

    new DivisionGui(profile, null).open(player);

    return true;
  }
}
