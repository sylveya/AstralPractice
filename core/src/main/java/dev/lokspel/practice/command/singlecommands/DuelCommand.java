package dev.lokspel.practice.command.singlecommands;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.duel.DuelManager;
import dev.lokspel.practice.manager.fight.match.enums.MatchType;
import dev.lokspel.practice.manager.gui.guis.selectors.LadderSelectorGui;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.queue.QueueManager;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class DuelCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return false;
    }

    if (!player.hasPermission("ap.duel")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getDuel().getNoPermission());
      return false;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (args.length != 1) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getDuel()
              .getCommandHelp()
              .replace("%label%", label));
      return false;
    }

    if ((!profile.getStatus().equals(ProfileStatus.LOBBY)
            && !profile.getStatus().equals(ProfileStatus.QUEUE))
        || profile.isParty()) {
      Common.sendMmMessage(player, PracticeConfig.language().getCommand().getDuel().getCantDuel());
      return false;
    }

    if (profile.getStatus().equals(ProfileStatus.QUEUE)) {
      QueueManager.getInstance().leaveQueue(player);
    }

    Player target = Bukkit.getPlayer(args[0]);
    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
    if (target == null || targetProfile == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getDuel()
              .getPlayerOffline()
              .replace("%target%", args[0]));
      return false;
    }

    if (target.equals(player)) {
      Common.sendMmMessage(player, PracticeConfig.language().getCommand().getDuel().getSelfDuel());
      return false;
    }

    if (DuelManager.getInstance().isRequested(player, target)
        && !player.hasPermission("ap.duel.infiniteinvite")) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getDuel()
              .getAlreadyInvited()
              .replace("%target%", target.getName()));
      return false;
    }

    if (targetProfile.isParty()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getDuel()
              .getPlayerCantDuel()
              .replace("%target%", target.getName()));
      return false;
    }

    if (!targetProfile.getStatus().equals(ProfileStatus.LOBBY)
        && !targetProfile.getStatus().equals(ProfileStatus.EDITOR)
        && !targetProfile.getStatus().equals(ProfileStatus.SPECTATE)
        && !targetProfile.getStatus().equals(ProfileStatus.QUEUE)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getDuel()
              .getPlayerCantDuel()
              .replace("%target%", target.getName()));
      return false;
    }

    if (!targetProfile.isDuelRequest()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getDuel()
              .getPlayerNotReceive()
              .replace("%target%", target.getName()));
      return false;
    }

    DuelManager.getInstance().getPendingRequestTarget().put(player, target);
    new LadderSelectorGui(profile, MatchType.DUEL).open(player);

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

    if ((!profile.getStatus().equals(ProfileStatus.LOBBY)
            && !profile.getStatus().equals(ProfileStatus.QUEUE))
        || profile.isParty()) {
      return arguments;
    }

    if (args.length == 1) {
      for (Player target : Bukkit.getOnlinePlayers()) {
        if (player.equals(target)) {
          continue;
        }

        Profile targetProfile = ProfileManager.getInstance().getProfile(target);
        if (!targetProfile.getStatus().equals(ProfileStatus.LOBBY)
            && !targetProfile.getStatus().equals(ProfileStatus.EDITOR)
            && !targetProfile.getStatus().equals(ProfileStatus.SPECTATE)
            && !targetProfile.getStatus().equals(ProfileStatus.QUEUE)) {
          continue;
        }

        arguments.add(target.getName());
      }

      StringUtil.copyPartialMatches(args[0], arguments, completion);
    }

    Collections.sort(completion);
    return completion;
  }
}
