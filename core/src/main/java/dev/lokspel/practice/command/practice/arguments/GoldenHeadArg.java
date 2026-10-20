package dev.lokspel.practice.command.practice.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.server.ServerManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.NumberUtil;
import dev.lokspel.practice.util.items.GoldenHead;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class GoldenHeadArg {

  private GoldenHeadArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.setup")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
      return;
    }

    if (player.getInventory().firstEmpty() == -1) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getGoldenhead()
              .getInventoryFull());
      return;
    }

    GoldenHead goldenHead = ServerManager.getInstance().getGoldenHead();
    if (args.length == 1) {
      player.getInventory().addItem(goldenHead.getItem().clone());
    } else if (args.length == 2) {
      if (NumberUtil.isNotInteger(args[1])) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getGoldenhead()
                .getInvalidAmount());
        return;
      }

      int amount = Integer.parseInt(args[1]);
      if (amount < 1 || amount > 64) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getPractice()
                .getArguments()
                .getGoldenhead()
                .getInvalidAmount2());
        return;
      }

      ItemStack itemStack = goldenHead.getItem().clone();
      itemStack.setAmount(amount);

      player.getInventory().addItem(itemStack);
    } else {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getGoldenhead()
              .getCommandHelp()
              .replace("%label%", label));
    }
  }
}
