package dev.lokspel.practice.command.singlecommands;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.server.ServerHubGui;
import dev.lokspel.practice.manager.inventory.Inventory;
import dev.lokspel.practice.manager.inventory.InventoryManager;
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

public class SetupCommand implements CommandExecutor, TabCompleter {

  @Override
  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
    if (!(sender instanceof Player player)) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getCantUseConsole());
      return false;
    }

    if (!player.hasPermission("ap.setup")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getSetup().getNoPermission());
      return false;
    }

    if (args.length == 0) {
      GuiManager.getInstance().searchGui(GuiType.Setup_Hub).open(player);
    } else if (args.length == 1) {
      switch (args[0]) {
        case "arena" -> GuiManager.getInstance().searchGui(GuiType.Arena_Summary).open(player);
        case "ladder" -> GuiManager.getInstance().searchGui(GuiType.Ladder_Summary).open(player);
        case "hologram" ->
            GuiManager.getInstance().searchGui(GuiType.Hologram_Summary).open(player);
        case "event" -> GuiManager.getInstance().searchGui(GuiType.Event_Summary).open(player);
        case "server" -> new ServerHubGui().open(player);
        case "on" -> {
          InventoryManager.getInstance().getSetupModePlayers().add(player);
          InventoryManager.getInstance().setInventory(player, null);
          player.setGameMode(org.bukkit.GameMode.CREATIVE);
          player.setFlying(true);
        }
        case "off" -> {
          InventoryManager.getInstance().getSetupModePlayers().remove(player);
          InventoryManager.getInstance().setInventory(player, Inventory.InventoryType.LOBBY);
        }
        default -> {}
      }
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

    if (args.length == 1) {
      if (player.hasPermission("ap.setup")) {
        arguments.add("arena");
        arguments.add("ladder");
        arguments.add("hologram");
        arguments.add("event");
        arguments.add("server");
        arguments.add("on");
        arguments.add("off");
      }

      StringUtil.copyPartialMatches(args[0], arguments, completion);
    }

    Collections.sort(completion);
    return completion;
  }
}
