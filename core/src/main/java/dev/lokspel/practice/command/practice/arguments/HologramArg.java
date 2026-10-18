package dev.lokspel.practice.command.practice.arguments;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.hologram.HologramSetupManager;
import dev.lokspel.practice.manager.leaderboard.hologram.Hologram;
import dev.lokspel.practice.manager.leaderboard.hologram.HologramManager;
import dev.lokspel.practice.manager.leaderboard.hologram.HologramType;
import dev.lokspel.practice.manager.leaderboard.hologram.SetupHologramType;
import dev.lokspel.practice.manager.leaderboard.hologram.holograms.FfaGlobalHologram;
import dev.lokspel.practice.manager.leaderboard.hologram.holograms.GlobalHologram;
import dev.lokspel.practice.manager.leaderboard.hologram.holograms.LadderDynamicHologram;
import dev.lokspel.practice.manager.leaderboard.hologram.holograms.LadderStaticHologram;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.server.ServerManager;
import dev.lokspel.practice.manager.server.WorldEnum;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public final class HologramArg {

  private HologramArg() {}

  public static void run(Player player, String label, String[] args) {
    if (!player.hasPermission("ap.setup")) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getPractice().getNoPermission());
      return;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);
    switch (profile.getStatus()) {
      case MATCH:
      case FFA:
      case EVENT:
      case SPECTATE:
        Common.sendMmMessage(
            player, PracticeConfig.language().getCommand().getHologram().getCantUse());
        return;

      default:
        break;
    }

    if (ServerManager.getLobby() == null) {
      Common.sendMmMessage(player, PracticeConfig.language().getSetServerLobby());
      return;
    }

    if (!ServerManager.getInstance().isInWorld(player, WorldEnum.LOBBY)) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getHologram().getOnlyInLobby());
      return;
    }

    if (args.length >= 2 && args[1].equalsIgnoreCase("teleport")) {
      if (args.length != 3) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getHologram()
                .getTeleport()
                .getCommandHelp()
                .replace("%label%", label + " hologram"));
        return;
      }

      Hologram hologram = HologramManager.getInstance().findHologram(args[2]).orElse(null);
      if (hologram == null) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getHologram()
                .getTeleport()
                .getHologramNotExists()
                .replace("%hologram%", args[2]));
        return;
      }

      final boolean enabled = hologram.isEnabled();
      hologram.despawn();
      hologram.moveTo(player.getLocation());
      hologram.setData();

      if (enabled) {
        hologram.updateContent();
      } else {
        hologram.setSetupHologram(SetupHologramType.SETUP);
      }

      GuiManager.getInstance().searchGui(GuiType.Hologram_Summary).update();
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getCommand()
              .getHologram()
              .getTeleport()
              .getTeleported()
              .replace("%hologram%", hologram.getName()));
      return;
    }

    if (args.length != 4 || !args[1].equalsIgnoreCase("create")) {
      for (String line : PracticeConfig.language().getCommand().getHologram().getCommandHelp()) {
        Common.sendMmMessage(player, line.replace("%label%", label + " hologram"));
      }
      return;
    }

    HologramType hologramType;
    switch (args[3].toLowerCase()) {
      case "global":
        hologramType = HologramType.GLOBAL;
        break;
      case "ffa_global", "ffa":
        hologramType = HologramType.FFA_GLOBAL;
        break;
      case "ladder_static":
        hologramType = HologramType.LADDER_STATIC;
        break;
      case "ladder_dynamic":
        hologramType = HologramType.LADDER_DYNAMIC;
        break;
      default:
        Common.sendMmMessage(
            player, PracticeConfig.language().getCommand().getHologram().getInvalidType());
        return;
    }

    if (HologramManager.getInstance().getHolograms().size() >= 18) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getHologram().getReachedMax());
      return;
    }

    String name = args[2];
    if (HologramManager.getInstance().getHologram(name) != null) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getHologram().getHoloExists());
      return;
    }

    if (name.length() > 9) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getCommand().getHologram().getCreateError());
      return;
    }

    Hologram hologram =
        switch (hologramType) {
          case GLOBAL -> new GlobalHologram(name, player.getLocation());
          case FFA_GLOBAL -> new FfaGlobalHologram(name, player.getLocation());
          case LADDER_STATIC -> new LadderStaticHologram(name, player.getLocation());
          case LADDER_DYNAMIC -> new LadderDynamicHologram(name, player.getLocation());
        };

    HologramManager.getInstance().createHologram(hologram);

    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () ->
                HologramSetupManager.getInstance()
                    .getHologramSetupGuis()
                    .get(hologram)
                    .get(GuiType.Hologram_Main)
                    .open(player),
            3L);

    Common.sendMmMessage(
        player,
        PracticeConfig.language()
            .getCommand()
            .getHologram()
            .getCreateSuccess()
            .replace("%hologram%", hologram.getName()));
  }

  public static List<String> tabComplete(Player player, String[] args) {
    List<String> arguments = new ArrayList<>();

    if (!player.hasPermission("ap.setup")) {
      return arguments;
    }

    if (args.length == 2) {
      arguments.add("create");
      arguments.add("teleport");
      return StringUtil.copyPartialMatches(args[1], arguments, new ArrayList<>());
    }

    if (args.length == 3 && args[1].equalsIgnoreCase("teleport")) {
      HologramManager.getInstance()
          .getHolograms()
          .forEach(hologram -> arguments.add(hologram.getName()));
      return StringUtil.copyPartialMatches(args[2], arguments, new ArrayList<>());
    }

    if (args.length == 4 && args[1].equalsIgnoreCase("create")) {
      arguments.add("global");
      arguments.add("ffa_global");
      arguments.add("ladder_static");
      arguments.add("ladder_dynamic");
      return StringUtil.copyPartialMatches(args[3], arguments, new ArrayList<>());
    }

    return arguments;
  }
}
