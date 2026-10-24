package dev.lokspel.practice.listener;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.nametag.NametagManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.cosmetics.armortrim.CosmeticsPermissionSanitizer;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.sidebar.SidebarManager;
import dev.lokspel.practice.util.playerutil.PlayerUtil;
import dev.lokspel.practice.util.startup.PermanentConfig;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerJoin(PlayerJoinEvent e) {
    final Player player = e.getPlayer();
    final UUID uuid = player.getUniqueId();
    Profile profile = ProfileManager.getInstance().getProfile(player);

    // Suppress default join message if disabled in config
    if (!PracticeConfig.settings().getPlayer().isJoinMessage()) {
      e.joinMessage(Component.empty());
    }

    if (profile == null) {
      profile = ProfileManager.getInstance().newProfile(player, uuid);
    }

    // Ensure action-bar state from any previous session is fully reset before new queue/status
    // flows start.
    profile.getActionBar().resetForReconnect();

    profile.checkGroup();

    // Send nametag teams
    NametagManager.getInstance().sendTeams(player);

    profile.setLastJoin(System.currentTimeMillis());

    // Check how many custom kits the player is allowed to save.
    int customKitPerm = profile.getCustomKitPerm();
    if (customKitPerm > 0) {
      profile.setAllowedCustomKits(customKitPerm);
    }

    // Set the lobby inventory
    if (PermanentConfig.JOIN_TELEPORT_LOBBY) {
      final Profile profile1 = profile;
      Bukkit.getScheduler()
          .runTaskLater(
              AstralPractice.getInstance(),
              () -> {
                if (!player.isOnline()) {
                  return;
                }
                PlayerUtil.setPlayerWorldTime(player);

                if (PracticeConfig.settings().getStaffMode().isJoinHideFromPlayers()
                    && player.hasPermission("ap.staffmode")) {
                  profile1.setHideFromPlayers(true);
                }
              },
              10L);

      // If the player was disconnected while in a match, remove them from it
      // to prevent ending up in the match with lobby items on rejoin
      if (profile.getStatus() == ProfileStatus.MATCH) {
        Match liveMatch = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (liveMatch != null) {
          liveMatch.removePlayer(player, true);
        }
      }

      InventoryManager.getInstance().setLobbyInventory(player, true);
    } else {
      ProfileManager.getInstance().getProfile(player).setStatus(ProfileStatus.OFFLINE);
      SidebarManager.getInstance().unLoadSidebar(player);
    }

    // Revalidate saved cosmetics after permission plugins have finished loading player nodes.
    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> {
              if (!player.isOnline()) {
                return;
              }

              Profile liveProfile = ProfileManager.getInstance().getProfile(player);
              if (liveProfile == null) {
                return;
              }

              CosmeticsPermissionSanitizer.sanitize(player, liveProfile);
            },
            40L);
  }
}
