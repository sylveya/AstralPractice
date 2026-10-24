package dev.lokspel.practice.command.arena.arguments.set;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
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
          player, PracticeConfig.language().getCommand().getArena().getNoPermission());
      return;
    }

    if (args.length != 3) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getIcon()
              .getCommandHelp()
              .replace("%label%", label));
      return;
    }

    DisplayArena arena = ArenaManager.getInstance().getArena(args[2]);
    if (arena == null) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getIcon()
              .getArenaNotExists()
              .replace("%arena%", args[2]));
      return;
    }

    if (arena.isEnabled() && !PracticeConfig.settings().getSetup().isAllowIconEditWhileEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getIcon()
              .getCantEdit()
              .replace("%arena%", arena.getName()));
      return;
    }

    ItemStack icon = PlayerUtil.getPlayerMainHand(player);
    if (icon.getType().equals(Material.AIR)) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getIcon()
              .getIconInHand());
      return;
    }

    if (icon.getItemMeta() == null || !icon.getItemMeta().hasDisplayName()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getArena()
              .getArguments()
              .getIcon()
              .getNoDisplayname()
              .replace("%arena%", arena.getDisplayName()));
      return;
    }

    arena.setIcon(icon);

    GuiManager.getInstance().searchGui(GuiType.Arena_Summary).update();
    ArenaGuiSetupManager.getInstance()
        .getArenaSetupGuis()
        .get(arena)
        .get(GuiType.Arena_Main)
        .update();

    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getArena()
            .getArguments()
            .getIcon()
            .getSavedIcon()
            .replace("%arena%", arena.getName()));
  }

  public static List<String> tabComplete(Player player, String[] args) {
    if (!player.hasPermission("ap.setup") || args.length != 3) {
      return List.of();
    }

    return StringUtil.copyPartialMatches(
        args[2], ArenaManager.getInstance().getArenaNames(), new ArrayList<>());
  }
}
