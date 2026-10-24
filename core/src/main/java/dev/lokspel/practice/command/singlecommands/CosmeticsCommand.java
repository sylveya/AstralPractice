package dev.lokspel.practice.command.singlecommands;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.gui.guis.cosmetics.CosmeticsHubGui;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.util.Common;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

/** Command to open the cosmetics GUI for armor trim customization. */
public class CosmeticsCommand implements CommandExecutor {

  @Override
  public boolean onCommand(
      @NonNull CommandSender sender,
      @NonNull Command cmd,
      @NonNull String label,
      String @NonNull [] args) {
    if (!(sender instanceof Player player)) {
      return false;
    }

    if (!player.hasPermission("ap.cosmetics.main")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getCosmetics().getNoPermission());
      return false;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getCosmetics().getFailedLoadProfile());
      return false;
    }

    if (profile.getStatus() != ProfileStatus.LOBBY
        && profile.getStatus() != ProfileStatus.STAFF_MODE) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getCosmetics().getOnlyInLobby());
      return false;
    }

    // Open the main cosmetics GUI with no parent GUI (player can close it normally)
    CosmeticsHubGui cosmeticsHubGui = new CosmeticsHubGui(profile);
    cosmeticsHubGui.open(player);

    return true;
  }
}
