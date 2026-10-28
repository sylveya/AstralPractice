package dev.lokspel.practice.command.singlecommands;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.queue.QueueManager;
import dev.lokspel.practice.util.Common;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnrankedCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      return false;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);

    boolean lobbyOrQueue =
        profile.getStatus().equals(ProfileStatus.LOBBY)
            || profile.getStatus().equals(ProfileStatus.QUEUE);
    if (!lobbyOrQueue || profile.isStaffMode() || profile.isParty()) {
      Common.sendMmMessage(player, PracticeConfig.language().getCantUseCommand());
      return false;
    }

    if (args.length == 0) {
      GuiManager.getInstance().searchGui(GuiType.Queue_Unranked).open(player);
      return true;
    }

    if (!player.hasPermission("ap.bypass.unranked.limit") && profile.getUnrankedLeft() == 0) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getQueues().getUnranked().getNoUnrankedLeft());
      return false;
    }

    if (args.length == 1) {
      NormalLadder ladder = LadderManager.getInstance().getLadder(args[0]);
      if (ladder == null) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getQueues()
                .getUnranked()
                .getLadderNotFound()
                .replace("%ladder%", args[0]));
        return false;
      }

      QueueManager.getInstance().createUnrankedQueue(player, ladder);
    }

    return true;
  }
}
