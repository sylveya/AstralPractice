package dev.lokspel.practice.command.singlecommands;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.duel.DuelManager;
import dev.lokspel.practice.manager.duel.DuelRequest;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.party.matchrequest.PartyRequest;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.queue.QueueManager;
import dev.lokspel.practice.manager.spectator.SpectatorManager;
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

public class AcceptCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return false;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (args.length != 1) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getAccept()
              .getCommandHelp()
              .replace("%label%", label));
      return false;
    }

    if ((!profile.getStatus().equals(ProfileStatus.LOBBY)
        && !profile.getStatus().equals(ProfileStatus.SPECTATE)
        && !profile.getStatus().equals(ProfileStatus.QUEUE))) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getAccept().getCantAccept());
      return false;
    }

    Player target = Bukkit.getPlayer(args[0]);
    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
    if (target == null || targetProfile == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getAccept()
              .getPlayerOffline()
              .replace("%target%", args[0]));
      return false;
    }

    if (target.equals(player)) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getAccept().getSelfDuel());
      return false;
    }

    if (profile.isParty()) {
      if (!targetProfile.isParty()) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getAccept()
                .getNoLongerParty()
                .replace("%target%", target.getName()));
        return false;
      }

      Party playerParty = PartyManager.getInstance().getParty(player);
      Party targetParty = PartyManager.getInstance().getParty(target);

      List<PartyRequest> partyRequests =
          PartyManager.getInstance().getRequestManager().getRequests().get(playerParty);
      if (partyRequests != null) {
        for (PartyRequest partyRequest : partyRequests) {
          if (!partyRequest.getSender().equals(targetParty)) {
            continue;
          }

          if (targetProfile.getStatus().equals(ProfileStatus.LOBBY)
              || targetProfile.getStatus().equals(ProfileStatus.EDITOR)) {
            partyRequest.acceptRequest();
          } else {
            Common.sendMmMessage(
                player,
                PracticeConfig.language()
                    .getCommand()
                    .getAccept()
                    .getPartyNotAvailable()
                    .replace("%target%", target.getName()));
          }

          return true;
        }
      }
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getAccept().getPartyNoInvite());
    } else {
      List<DuelRequest> requests = DuelManager.getInstance().getRequests().get(player);

      if (requests != null) {
        for (DuelRequest request : requests) {
          if (!request.getSender().equals(target)) {
            continue;
          }

          if ((targetProfile.getStatus().equals(ProfileStatus.LOBBY)
                  || targetProfile.getStatus().equals(ProfileStatus.EDITOR)
                  || targetProfile.getStatus().equals(ProfileStatus.QUEUE))
              && !targetProfile.isParty()) {
            if (profile.getStatus().equals(ProfileStatus.QUEUE)) {
              QueueManager.getInstance().leaveQueue(player);
            }
            if (targetProfile.getStatus().equals(ProfileStatus.QUEUE)) {
              QueueManager.getInstance().leaveQueue(target);
            }
            if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
              SpectatorManager.getInstance().getSpectators().get(player).removeSpectator(player);
            }

            // Update sender reference to the current online Player instance
            // in case the original sender disconnected and rejoined since the request was sent.
            request.setSender(target);
            request.acceptRequest();
          } else {
            Common.sendMmMessage(
                player,
                PracticeConfig.language()
                    .getCommand()
                    .getAccept()
                    .getPlayerNotAvailable()
                    .replace("%target%", target.getName()));
          }

          return true;
        }
      }
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getAccept()
              .getPlayerNoInvite()
              .replace("%target%", target.getName()));
    }

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
        && !profile.getStatus().equals(ProfileStatus.SPECTATE)
        && !profile.getStatus().equals(ProfileStatus.QUEUE))) {
      return arguments;
    }

    if (args.length == 1) {
      if (profile.isParty()) {
        List<PartyRequest> requests =
            PartyManager.getInstance()
                .getRequestManager()
                .getRequests()
                .get(PartyManager.getInstance().getParty(player));

        if (requests != null) {
          for (PartyRequest partyRequest : requests) {
            if (!arguments.contains(partyRequest.getSender().getLeader().getName())) {
              arguments.add(partyRequest.getSender().getLeader().getName());
            }
          }
        }
      } else {
        List<DuelRequest> requests = DuelManager.getInstance().getRequests().get(player);

        if (requests != null) {
          for (DuelRequest duelRequest : DuelManager.getInstance().getRequests().get(player)) {
            if (!arguments.contains(duelRequest.getSender().getName())) {
              arguments.add(duelRequest.getSender().getName());
            }
          }
        }
      }

      StringUtil.copyPartialMatches(args[0], arguments, completion);
    }

    Collections.sort(completion);
    return completion;
  }
}
