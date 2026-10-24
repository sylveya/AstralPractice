package dev.lokspel.practice.command.party.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class PartyInviteArg {

  private PartyInviteArg() {}

  public static void inviteCommand(Player player, String label, String[] args) {
    if (args.length != 2) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getParty()
              .getArguments()
              .getInvite()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    Party party = PartyManager.getInstance().getParty(player);
    if (party == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getParty()
              .getArguments()
              .getInvite()
              .getNoParty());
      return;
    }

    if (!party.isAllInvite() && !party.getLeader().equals(player)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getParty()
              .getArguments()
              .getInvite()
              .getCantInvite());
      return;
    }

    Player target = Bukkit.getPlayer(args[1]);
    if (target == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getParty()
              .getArguments()
              .getInvite()
              .getTargetOffline()
              .replace("%target%", args[1]));
      return;
    }

    if (player.equals(target)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getParty()
              .getArguments()
              .getInvite()
              .getCantInviteYourself());
      return;
    }

    if (party.getMembers().contains(target)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getParty()
              .getArguments()
              .getInvite()
              .getPlayerAlreadyInParty()
              .replace("%target%", target.getName()));
      return;
    }

    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
    if (!targetProfile.isPartyInvites()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getParty()
              .getArguments()
              .getInvite()
              .getNoInvites()
              .replace("%target%", target.getName()));
      return;
    }

    if (party.getInvites().getOrDefault(target, 0L) + PartyManager.INVITE_COOLDOWN
        > System.currentTimeMillis()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getParty()
              .getArguments()
              .getInvite()
              .getAlreadyInvited()
              .replace("%target%", target.getName()));
      return;
    }

    party.getInvites().put(target, System.currentTimeMillis());

    party.sendMessage(
        PracticeConfig.language()
            .getCommand()
            .getParty()
            .getArguments()
            .getInvite()
            .getPlayerInvited()
            .replace("%target%", target.getName()));
    Common.sendMmMessage(
        target,
        PracticeConfig.language()
            .getCommand()
            .getParty()
            .getArguments()
            .getInvite()
            .getPlayerGotInvite()
            .replace("%inviter%", player.getName()));
  }

  public static List<String> tabComplete(Player player, String[] args) {
    Party party = PartyManager.getInstance().getParty(player);
    if (party != null
        && (party.getLeader().equals(player) || party.isAllInvite())
        && args.length == 2) {
      List<String> arguments =
          Bukkit.getOnlinePlayers().stream()
              .filter(online -> !online.equals(player))
              .filter(online -> !ProfileManager.getInstance().getProfile(online).isParty())
              .map(Player::getName)
              .toList();

      return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
    }

    return List.of();
  }
}
