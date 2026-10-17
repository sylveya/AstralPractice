package dev.lokspel.practice.manager.spectator;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.ffa.FfaManager;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.guis.SpectatorMenuGui;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.StringUtil;
import dev.lokspel.practice.util.cooldown.CooldownObject;
import dev.lokspel.practice.util.cooldown.PlayerCooldown;
import dev.lokspel.practice.util.interfaces.Spectatable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public class SpectatorManager {

  private static SpectatorManager instance;

  public static SpectatorManager getInstance() {
    if (instance == null) {
      instance = new SpectatorManager();
    }
    return instance;
  }

  private final Map<UUID, Spectatable> spectators = new HashMap<>();
  private SpectatorMenuGui spectatorMenuGui;

  public Spectatable getSpectatable(Player player) {
    return player == null ? null : spectators.get(player.getUniqueId());
  }

  public boolean isSpectating(Player player) {
    return player != null && spectators.containsKey(player.getUniqueId());
  }

  private SpectatorManager() {
    Bukkit.getPluginManager().registerEvents(new SpectatorListener(), AstralPractice.getInstance());

    this.spectatorMenuGui =
        (SpectatorMenuGui) GuiManager.getInstance().addGui(new SpectatorMenuGui());
  }

  /** Re-creates the spectator menu from the reloaded configuration. */
  public void reloadGuis() {
    this.spectatorMenuGui =
        (SpectatorMenuGui) GuiManager.getInstance().addGui(new SpectatorMenuGui());
  }

  public void spectateMenuUse(Player player) {
    if (!player.hasPermission("ap.spectate.menu")) {
      Common.sendMmMessage(player, PracticeConfig.language().getSpectate().getNoPermissions());
      return;
    }

    spectatorMenuGui.open(player);
  }

  public static void spectateRandomMatchItemUse(Player player) {
    if (!player.hasPermission("ap.spectate.random")) {
      Common.sendMmMessage(player, PracticeConfig.language().getSpectate().getNoPermissions());
      return;
    }

    if (!player.hasPermission("ap.bypass.cooldown")
        && PlayerCooldown.isActive(player, CooldownObject.RANDOM_MATCH)) {
      Common.sendMmMessage(
          player,
          StringUtil.replaceSecondString(
              PracticeConfig.language().getSpectate().getRandomMatchCooldown(),
              PlayerCooldown.getLeftInDouble(player, CooldownObject.RANDOM_MATCH)));
      return;
    }

    PlayerCooldown.addCooldown(
        player,
        CooldownObject.RANDOM_MATCH,
        PracticeConfig.settings().getSpectatorSettings().getRandomMatchCooldown());
    spectateRandomMatch(player);
  }

  /**
   * Teleports a spectator to a single fighter of the match they are watching. Does nothing if the
   * spectator stopped watching that match in the meantime.
   */
  public void teleportToTarget(Player spectator, Match match, Player target) {
    if (spectator == null || this.spectators.get(spectator.getUniqueId()) != match) {
      return;
    }
    if (!match.getPlayers().contains(target)) {
      return;
    }

    spectator.teleport(target);
  }

  public static void spectateRandomMatch(Player player) {
    List<Spectatable> spectatables = new ArrayList<>(MatchManager.getInstance().getLiveMatches());
    spectatables.addAll(FfaManager.getInstance().getOpenFfas());

    if (spectatables.isEmpty()) {
      Common.sendMmMessage(player, PracticeConfig.language().getSpectate().getNoMatch());
      return;
    }

    List<Spectatable> available = new ArrayList<>(spectatables.size());
    for (Spectatable spectatable : spectatables) {
      if (!spectatable.getSpectators().contains(player)) {
        available.add(spectatable);
      }
    }

    if (available.isEmpty()) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getSpectate().getMatch().getOnlyOneToSpectate());
      return;
    }

    Spectatable match = available.get(ThreadLocalRandom.current().nextInt(available.size()));
    match.addSpectator(player, null, true, false);
  }
}
