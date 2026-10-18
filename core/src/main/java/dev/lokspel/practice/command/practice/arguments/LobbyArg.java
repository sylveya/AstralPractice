package dev.lokspel.practice.command.practice.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.util.ArenaWorldUtil;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.server.ServerManager;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class LobbyArg {

  private LobbyArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.practice.lobby")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
      return;
    }

    if (args.length == 1) {
      if (ServerManager.getLobby() != null) {
        Location lobbyLocation = ServerManager.getLobby();
        player.teleport(lobbyLocation);
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getLobby()
                .getLobbyNotSet()
                .replace("%label%", label));
      }
    } else if (args.length == 2 && args[1].equalsIgnoreCase("set")) {
      Location lobbyLocation = player.getLocation();

      boolean inArenas =
          ArenaWorldUtil.getArenasWorld() != null
              && lobbyLocation.getWorld().equals(ArenaWorldUtil.getArenasWorld());
      boolean inCopy =
          ArenaWorldUtil.getArenasCopyWorld() != null
              && lobbyLocation.getWorld().equals(ArenaWorldUtil.getArenasCopyWorld());

      if (!inArenas && !inCopy) {
        ServerManager.getInstance().setLobby(player, lobbyLocation);

        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getLobby()
                .getLobbySet());
      } else {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getLobby()
                .getNoArenasWorld());
      }
    } else if (args.length == 2 && args[1].equalsIgnoreCase("load")) {
      InventoryManager.getInstance().setLobbyInventory(player, ServerManager.getLobby() != null);
    } else {
      for (String line :
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getLobby()
              .getCommandHelp()) {
        Common.sendMmMessage(player, line.replace("%label%", label));
      }
    }
  }

  public static List<String> tabComplete(Player player, String[] args) {
    List<String> arguments = new ArrayList<>();

    if (!player.hasPermission("ap.practice.lobby")) {
      return arguments;
    }

    if (args.length == 2) {
      arguments.add("set");
      arguments.add("load");

      return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
    }

    return arguments;
  }
}
