package dev.lokspel.practice.listener;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.command.privatemessage.MessageCommand;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.nametag.NametagManager;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.server.ServerManager;
import dev.lokspel.practice.util.cooldown.PlayerCooldown;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerQuit(PlayerQuitEvent e) {
    e.quitMessage(Component.empty());
    final Player player = e.getPlayer();
    NametagManager.getInstance().onPlayerQuit(player);
    ServerManager.getInstance().onPlayerQuit(player);
    PlayerCooldown.clearPlayer(player);

    StatisticListener.clearPlayer(player);
    MessageCommand.clearPlayer(player);

    final Profile profile = ProfileManager.getInstance().getProfile(player);
    final Party party = PartyManager.getInstance().getParty(player);

    if (party != null) {
      party.removeMember(player, false);
    }

    MatchManager.getInstance().invalidateRematchByPlayer(player);

    if (profile != null) {
      profile.getActionBar().resetForReconnect();
      profile.setStaffMode(false);
      profile.setHideFromPlayers(false);

      profile.setLastJoin(System.currentTimeMillis());

      // Check how many custom kits the player is allowed to save.
      int customKitPerm = profile.getCustomKitPerm();
      if (customKitPerm > 0) {
        profile.setAllowedCustomKits(customKitPerm);
      }

      profile.setPlayerCustomKitSelector(null);

      if (AstralPractice.getInstance().isEnabled()) {
        Bukkit.getScheduler()
            .runTaskLater(
                AstralPractice.getInstance(), () -> profile.setStatus(ProfileStatus.OFFLINE), 5L);

        UUID uuid = player.getUniqueId();
        Bukkit.getScheduler()
            .runTaskLater(
                AstralPractice.getInstance(),
                () -> ProfileManager.getInstance().demoteOfflineProfile(uuid),
                40L);
      }
    }

    ProfileManager.getInstance().clearPlayerReference(player);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerKick(PlayerKickEvent e) {
    Player player = e.getPlayer();
    NametagManager.getInstance().onPlayerQuit(player);
    ServerManager.getInstance().onPlayerQuit(player);
    PlayerCooldown.clearPlayer(player);

    StatisticListener.clearPlayer(player);
    MessageCommand.clearPlayer(player);

    MatchManager.getInstance().invalidateRematchByPlayer(player);

    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile != null) {
      profile.getActionBar().resetForReconnect();
      profile.setLastJoin(System.currentTimeMillis());

      if (AstralPractice.getInstance().isEnabled()) {
        UUID uuid = player.getUniqueId();
        Bukkit.getScheduler()
            .runTaskLater(
                AstralPractice.getInstance(),
                () -> {
                  profile.setStatus(ProfileStatus.OFFLINE);
                  ProfileManager.getInstance().demoteOfflineProfile(uuid);
                },
                40L);
      }
    }

    ProfileManager.getInstance().clearPlayerReference(player);
  }
}
