package dev.lokspel.practice.command.ladder.arguments;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

public final class IconArg {

  private IconArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.setup")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getLadder().getNoPermission());
      return;
    }

    if (args.length != 3) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getIcon()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    NormalLadder ladder = LadderManager.getInstance().getLadder(args[2]);
    if (ladder == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getIcon()
              .getNotExists()
              .replace("%ladder%", args[2]));
      return;
    }

    if (ladder.isEnabled() && !PracticeConfig.settings().getSetup().isAllowIconEditWhileEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getIcon()
              .getLadderEnabled()
              .replace("%ladder%", ladder.getDisplayName()));
      return;
    }

    ItemStack icon = PlayerUtil.getPlayerMainHand(player);
    if (icon.getType().equals(Material.AIR)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getIcon()
              .getNoItem()
              .replace("%ladder%", ladder.getDisplayName()));
      return;
    }

    if (icon.getItemMeta() == null || !icon.getItemMeta().hasDisplayName()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getLadder()
              .getArguments()
              .getIcon()
              .getNoDisplayname()
              .replace("%ladder%", ladder.getDisplayName()));
      return;
    }

    ladder.setIcon(icon);

    LadderSetupManager.getInstance()
        .getLadderSetupGuis()
        .get(ladder)
        .get(GuiType.Ladder_Main)
        .update();
    GuiManager.getInstance().searchGui(GuiType.Ladder_Summary).update();

    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getLadder()
            .getArguments()
            .getIcon()
            .getSavedIcon()
            .replace("%ladder%", ladder.getDisplayName()));
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.setup") || args.length != 3) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[2], LadderManager.getInstance().getLadderNames(), new ArrayList<>());
  }
}
