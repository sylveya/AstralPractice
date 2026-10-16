package dev.lokspel.practice.command.practice.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.util.Common;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class RenameArg {

  private RenameArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.practice.rename")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
      return;
    }

    if (args.length <= 1) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getRename()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    ItemStack handItem = player.getInventory().getItemInMainHand();
    if (handItem.getType().equals(Material.AIR)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getPractice()
              .getArguments()
              .getRename()
              .getItemInHand());
      return;
    }

    List<String> name = Arrays.asList(args);
    StringBuilder builder = new StringBuilder();

    for (int i = 1; i < name.size(); i++) {
      builder.append(name.get(i));
      if (name.size() - 1 != i) {
        builder.append(" ");
      }
    }

    ItemStack item = player.getInventory().getItemInMainHand();
    ItemMeta itemMeta = item.getItemMeta();
    itemMeta.displayName(Common.toComponent(builder.toString()));
    item.setItemMeta(itemMeta);
    player.getInventory().setItemInMainHand(item);
  }
}
