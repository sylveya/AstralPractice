package dev.lokspel.practice.manager.fight.ffa;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.fight.ffa.game.FfaArenaSelectorGui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.spectator.SpectatorManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.interfaces.Spectatable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class FfaManager {

  private static FfaManager instance;

  public static FfaManager getInstance() {
    if (instance == null) {
      instance = new FfaManager();
    }
    return instance;
  }

  private FfaArenaSelectorGui arenaSelectorGui;

  private FfaManager() {
    this.arenaSelectorGui = new FfaArenaSelectorGui();
    GuiManager.getInstance().addGui(this.arenaSelectorGui);
  }

  /** Re-creates the arena selector from the reloaded configuration. */
  public void reloadGuis() {
    this.arenaSelectorGui = new FfaArenaSelectorGui();
    GuiManager.getInstance().addGui(this.arenaSelectorGui);
  }

  /** Returns true if the player joins the arena or its ladder selector opens. */
  public boolean joinArena(Player player, FfaArena arena) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (!profile.getStatus().equals(ProfileStatus.LOBBY)) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getFfa().getCommand().getJoin().getCantJoinFfa());
      return false;
    }

    if (arena == null) {
      Common.sendMmMessage(
          player, PracticeConfig.language().getFfa().getCommand().getJoin().getArenaNotFound());
      return false;
    }

    Ffa ffa = arena.getFfa();
    if (!arena.isEnabled() || ffa == null || !ffa.isOpen()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language()
              .getFfa()
              .getCommand()
              .getJoin()
              .getArenaClosed()
              .replace("%arena%", arena.getDisplayName()));
      return false;
    }

    if (arena.getAssignedLadders().size() == 1) {
      NormalLadder ladder = arena.getAssignedLadders().iterator().next();
      player.closeInventory();
      ffa.addPlayer(player, ladder);
    } else {
      ffa.getLadderSelectorGui().update();
      ffa.getLadderSelectorGui().open(player);
    }

    return true;
  }

  public List<Ffa> getOpenFfas() {
    List<Ffa> ffas = new ArrayList<>();
    for (FfaArena ffaArena : ArenaManager.getInstance().getFfaArenas()) {
      if (ffaArena.getFfa().isOpen()) {
        ffas.add(ffaArena.getFfa());
      }
    }
    return ffas;
  }

  public void endFfas() {
    for (FfaArena ffaArena : ArenaManager.getInstance().getFfaArenas()) {
      ffaArena.getFfa().close("");
    }
  }

  public Ffa getFfaByPlayer(Player player) {
    for (FfaArena ffaArena : ArenaManager.getInstance().getFfaArenas()) {
      Ffa ffa = ffaArena.getFfa();
      if (ffa.getPlayers().containsKey(player)) {
        return ffa;
      }

      // Fallback for stale Player-object map keys.
      for (Player ffaPlayer : ffa.getPlayers().keySet()) {
        if (player.getUniqueId().equals(ffaPlayer.getUniqueId())) {
          return ffa;
        }
      }
    }
    return null;
  }

  public Ffa getFfaBySpectator(Player player) {
    if (player == null) {
      return null;
    }
    Spectatable spectatable =
        SpectatorManager.getInstance().getSpectators().get(player.getUniqueId());
    if (spectatable instanceof Ffa) {
      return (Ffa) spectatable;
    } else {
      return null;
    }
  }
}
