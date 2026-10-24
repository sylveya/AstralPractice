package dev.lokspel.practice.command.party.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PartyInfoArg {

  private PartyInfoArg() {}

  public static void infoCommand(Player player, String label, String[] args) {
    if (args.length != 1 && args.length != 2) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getParty()
              .getArguments()
              .getInfo()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    Party party;
    if (args.length == 1) {
      party = PartyManager.getInstance().getParty(player);
      if (party == null) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getParty()
                .getArguments()
                .getInfo()
                .getNoParty());
        return;
      }
    } else {
      if (!player.hasPermission("ap.party.info.others")) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getParty()
                .getArguments()
                .getInfo()
                .getNoPermission());
      }

      Player target = Bukkit.getPlayer(args[1]);
      if (target == null) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getParty()
                .getArguments()
                .getInfo()
                .getNotOnline()
                .replace("%target%", args[1]));
        return;
      }

      party = PartyManager.getInstance().getParty(target);
      if (party == null) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getParty()
                .getArguments()
                .getInfo()
                .getPlayerNoParty()
                .replace("%target%", target.getName()));
        return;
      }
    }

    var infoConfig = PracticeConfig.language().getCommand().getParty().getArguments().getInfo();
    String partyState =
        party.isPublicParty()
            ? infoConfig.getPartyStates().getPublicValue()
            : infoConfig.getPartyStates().getPrivateValue();
    String leaderName = party.getLeader().getName();
    String memberSize = String.valueOf(party.getMembers().size());
    String maxMembers = String.valueOf(party.getMaxPlayerLimit());
    String membersStr = String.join(", ", party.getMemberNames());

    for (String line : infoConfig.getPartyInfo()) {
      Common.sendMmMessage(
          player,
          line.replace("%leader%", leaderName)
              .replace("%partyState%", partyState)
              .replace("%memberSize%", memberSize)
              .replace("%maxMembers%", maxMembers)
              .replace("%members%", membersStr));
    }
  }
}
