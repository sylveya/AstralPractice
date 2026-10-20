package dev.lokspel.practice.util.entityhider;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import dev.lokspel.api.event.spectate.end.MatchSpectateEndEvent;
import dev.lokspel.api.event.spectate.start.MatchSpectateStartEvent;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.nametag.NametagManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.server.ServerManager;
import dev.lokspel.practice.manager.server.WorldEnum;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.NameFormatUtil;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerHider implements Listener {

  private static PlayerHider instance;

  public static PlayerHider getInstance() {
    if (instance == null) {
      instance = new PlayerHider();
    }
    return instance;
  }

  private PlayerHider() {
    Bukkit.getPluginManager().registerEvents(this, AstralPractice.getInstance());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void playerJoin(PlayerJoinEvent e) {
    if (checkInvalidLobby()) {
      return;
    }

    final Player player = e.getPlayer();
    final Profile profile = ProfileManager.getInstance().getProfile(player);

    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> {
              for (Player online : Bukkit.getOnlinePlayers()) {
                if (player == online) {
                  continue;
                }

                Profile onlineProfile = ProfileManager.getInstance().getProfile(online);
                if (onlineProfile == null) {
                  continue;
                }
                ProfileStatus onlineStatus = onlineProfile.getStatus();

                /*
                 * Hide the player from the online.
                 */
                if (onlineStatus.equals(ProfileStatus.MATCH)
                    || onlineStatus.equals(ProfileStatus.EVENT)
                    || onlineStatus.equals(ProfileStatus.FFA)) {
                  hidePlayer(online, player);
                  if (!PracticeConfig.settings().getMatchSettings().isShowPlayersInTab()) {
                    hidePlayer(player, online);
                  }
                } else if (!onlineStatus.equals(ProfileStatus.SPECTATE)
                    && onlineProfile.isHidePlayers()) {
                  hidePlayer(online, player);
                } else if (profile.isHideFromPlayers()
                    && !online.hasPermission("ap.staffmode.see")) {
                  hidePlayer(online, player);
                }

                /*
                 * Hide the online from the player.
                 */
                if (onlineProfile.isHideFromPlayers()
                    && !player.hasPermission("ap.staffmode.see")) {
                  hidePlayer(player, online);
                } else if (profile.isHidePlayers()
                    && ServerManager.getInstance().isInWorld(online, WorldEnum.LOBBY)) {
                  hidePlayer(player, online);
                }
              }
            },
            2L);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void playerTeleport(PlayerTeleportEvent e) {
    if (checkInvalidLobby()) {
      return;
    }
    if (e.getFrom().getWorld().equals(e.getTo().getWorld())) {
      return;
    }

    Player player = e.getPlayer();
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return;
    }

    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> {
              if (!ServerManager.getInstance().isInWorld(player, WorldEnum.LOBBY)) {
                return;
              }

              for (Player online : Bukkit.getOnlinePlayers()) {
                if (player == online) {
                  continue;
                }

                Profile onlineProfile = ProfileManager.getInstance().getProfile(online);
                if (onlineProfile == null) {
                  continue;
                }

                // Handle the teleported player
                if (profile.isHidePlayers()
                    && ServerManager.getInstance().isInWorld(online, WorldEnum.LOBBY)) {
                  hidePlayer(player, online);
                } else if (!onlineProfile.isHideFromPlayers()
                    || player.hasPermission("ap.staffmode.see")) {
                  showPlayer(player, online);
                } else {
                  hidePlayer(player, online);
                }

                // Handle the online player
                if (!(onlineProfile.getStatus().equals(ProfileStatus.MATCH)
                    || onlineProfile.getStatus().equals(ProfileStatus.EVENT)
                    || onlineProfile.getStatus().equals(ProfileStatus.FFA))) {
                  if (onlineProfile.isHidePlayers()
                      && ServerManager.getInstance().isInWorld(online, WorldEnum.LOBBY)) {
                    hidePlayer(online, player);
                  } else if (!profile.isHideFromPlayers()
                      || online.hasPermission("ap.staffmode.see")) {
                    showPlayer(online, player);
                    showTabEntry(online, player);
                  } else if (profile.isHideFromPlayers()
                      && !online.hasPermission("ap.staffmode.see")) {
                    hidePlayer(online, player);
                  }
                } else {
                  hidePlayer(online, player, false);
                  if (!PracticeConfig.settings().getMatchSettings().isShowPlayersInTab()) {
                    hidePlayer(player, online, false);
                  }
                }
              }
            },
            2L);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(PlayerQuitEvent e) {
    if (!PracticeConfig.settings().getMatchSettings().isShowPlayersInTab()
        && !PracticeConfig.settings().getMatchSettings().isShowSpectatorsInTab()) {
      return;
    }

    final UUID uuid = e.getPlayer().getUniqueId();

    /*
     * The tab list entry of a hidden player (SHOW-PLAYERS-IN-TAB) is sent manually, so the server
     * does not remove it when the player disconnects. Removing it for everyone is safe, clients
     * ignore the removal of a player they do not know.
     */
    final WrapperPlayServerPlayerInfoRemove packet = new WrapperPlayServerPlayerInfoRemove(uuid);

    for (Player online : Bukkit.getOnlinePlayers()) {
      if (online.getUniqueId().equals(uuid)) {
        continue;
      }

      PacketEvents.getAPI().getPlayerManager().sendPacket(online, packet);
    }
  }

  /**
   * When a player starts spectating a match, hide the player from other spectators if they have the
   * option enabled, and show the player to the players in the match.
   *
   * @param e The event that is being called.
   */
  @EventHandler
  public void onSpectatingStart(MatchSpectateStartEvent e) {
    Player player = e.getPlayer();
    Profile profile = ProfileManager.getInstance().getProfile(player);
    dev.lokspel.api.interfaces.Match match = e.getMatch();

    for (Player online : match.getSpectators()) {
      if (player == online) {
        continue;
      }

      if (profile.isHideSpectators()) {
        hidePlayer(player, online, false);
      }

      if (ProfileManager.getInstance().getProfile(online).isHidePlayers()) {
        hidePlayer(online, player);
      }
    }

    // Show match players.
    for (Player matchPlayer : match.getPlayers()) {
      showPlayer(player, matchPlayer);
    }

    // Show the spectator in the tab list of the fighters, with the configured prefix in front of
    // the
    // name. Sent per viewer, because the tab list name of a player is global.
    if (!e.isCancelled() && PracticeConfig.settings().getMatchSettings().isShowSpectatorsInTab()) {
      Component tabName = NametagManager.getInstance().getTabListName(player);
      String prefix = PracticeConfig.settings().getMatchSettings().getSpectatorTabPrefix();

      if (!prefix.isEmpty()) {
        tabName =
            NameFormatUtil.parseConfiguredComponent(prefix.replace("%player%", player.getName()))
                .append(tabName);
      }

      for (Player matchPlayer : match.getPlayers()) {
        showTabEntry(matchPlayer, player, tabName);
      }
    }

    // Hide other players.
    if (match instanceof Match) {
      for (Player hide : MatchManager.getInstance().getHidePlayers((Match) match)) {
        this.hidePlayer(player, hide, false);
      }
    }
  }

  /**
   * Takes the spectator back out of the tab list of the fighters once they stopped spectating. The
   * global tab list name only updates the name and keeps the entry listed, so it has to be undone
   * here.
   */
  @EventHandler
  public void onSpectatingEnd(MatchSpectateEndEvent e) {
    boolean keepInTab = PracticeConfig.settings().getMatchSettings().isShowPlayersInTab();
    Player spectator = e.getPlayer();

    for (Player fighter : e.getMatch().getPlayers()) {
      if (fighter.equals(spectator)) {
        continue;
      }

      if (keepInTab) {
        showTabEntry(fighter, spectator);
      } else {
        removeTabEntry(fighter, spectator);
      }
    }
  }

  /**
   * If the player is in the lobby, hide or show all players in the lobby.
   *
   * @param player The player who is toggling their lobby visibility.
   */
  public void toggleLobbyVisibility(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (!ServerManager.getInstance().isInWorld(player, WorldEnum.LOBBY)) {
      return;
    }

    for (Player online : Bukkit.getOnlinePlayers()) {
      if (player.equals(online)) {
        continue;
      }
      if (!ServerManager.getInstance().isInWorld(online, WorldEnum.LOBBY)) {
        continue;
      }

      if (profile.isHidePlayers()) {
        hidePlayer(player, online);
      } else {
        Profile onlineProfile = ProfileManager.getInstance().getProfile(online);

        if (onlineProfile != null
            && (!onlineProfile.isHideFromPlayers() || player.hasPermission("ap.staffmode.see"))) {
          showPlayer(player, online);
        }
      }
    }
  }

  /**
   * If the player is spectating, hide or show all the other spectators in the match.
   *
   * @param player The player who is toggling their visibility
   */
  public void toggleSpectatorVisibility(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
      Match match = MatchManager.getInstance().getLiveMatchBySpectator(player);

      if (match != null && match.getSpectators().size() > 1) {
        for (Player online : match.getSpectators()) {
          if (player.equals(online)) {
            continue;
          }

          Profile onlineProfile = ProfileManager.getInstance().getProfile(online);
          if (profile.isHideSpectators()) {
            hidePlayer(player, online);
          } else {
            if (!onlineProfile.isHideFromPlayers() || player.hasPermission("ap.staffmode.see")) {
              showPlayer(player, online);
            }
          }
        }
      }
      /*
       *
       * CURRENTLY SPECTATOR MODE DURING EVENTS IS NOT SUPPORTED.
       *
       */
    }
  }

  /**
   * If the player is in staff mode, hide them from all players who are not in staff mode.
   *
   * @param player The player who is toggling staff mode.
   */
  public void toggleStaffVisibility(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);

    for (Player online : Bukkit.getOnlinePlayers()) {
      if (player.equals(online)) {
        continue;
      }

      if (profile.isHideFromPlayers()) {
        if (!online.hasPermission("ap.staffmode.see")) {
          hidePlayer(online, player, false);
        }
      } else {
        if (online.getWorld().equals(player.getWorld())) {
          Profile onlineProfile = ProfileManager.getInstance().getProfile(online);
          if (onlineProfile == null || !onlineProfile.isHidePlayers()) {
            showPlayer(online, player);
          }
        }
      }
    }

    NametagManager.getInstance().onVisibilityStateChange(player);
  }

  public void hidePlayer(Player observer, Player target) {
    hidePlayer(
        observer,
        target,
        PracticeConfig.settings().getMatchSettings().isShowPlayersInLobbyTab()
            && isLobbyPlayer(observer));
  }

  /**
   * Hides {@code target} from {@code observer}.
   *
   * @param keepInLobbyTab whether the {@code SHOW-PLAYERS-IN-LOBBY-TAB} exemption applies to {@code
   *     observer}. It has to be passed explicitly while a player is being moved out of the lobby,
   *     because the world tracking is only updated once the teleport event fired, so the observer
   *     still counts as a lobby player at that point.
   */
  public void hidePlayer(Player observer, Player target, boolean keepInLobbyTab) {
    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
    boolean isTargetVanished =
        targetProfile != null
            && targetProfile.isHideFromPlayers()
            && !observer.hasPermission("ap.staffmode.see");

    boolean inLobby =
        !isTargetVanished
            && keepInLobbyTab
            && PracticeConfig.settings().getMatchSettings().isShowPlayersInLobbyTab();
    boolean showPlayersInTab =
        !isTargetVanished
            && (PracticeConfig.settings().getMatchSettings().isShowPlayersInTab() || inLobby);

    observer.hidePlayer(AstralPractice.getInstance(), target);

    if (showPlayersInTab) {
      // Re-adding the entry resets the name on the client side, so the lobby tab list has to
      // carry the same name NametagManager broadcasts globally.
      showTabEntry(
          observer, target, inLobby ? NametagManager.getInstance().getTabListName(target) : null);
    } else {
      removeTabEntry(observer, target);
    }
  }

  /**
   * Whether the player is a lobby player, which is what {@code SHOW-PLAYERS-IN-LOBBY-TAB} is about.
   * The world alone is not enough, because a player who just entered a match, an event or a
   * spectated is still mapped to the lobby world until their teleport event fired.
   */
  private boolean isLobbyPlayer(Player player) {
    if (!ServerManager.getInstance().isInWorld(player, WorldEnum.LOBBY)) {
      return false;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return false;
    }

    ProfileStatus status = profile.getStatus();
    return status.equals(ProfileStatus.LOBBY)
        || status.equals(ProfileStatus.QUEUE)
        || status.equals(ProfileStatus.EDITOR)
        || status.equals(ProfileStatus.CUSTOM_EDITOR)
        || status.equals(ProfileStatus.STAFF_MODE);
  }

  /**
   * Takes the tab list entry of {@code target} away from {@code observer} without touching the
   * entity visibility, so the caller stays in charge of who can see the player.
   */
  private void removeTabEntry(Player observer, Player target) {
    WrapperPlayServerPlayerInfoUpdate.PlayerInfo playerInfo =
        new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(target.getUniqueId());
    playerInfo.setListed(false);

    WrapperPlayServerPlayerInfoUpdate playerInfoUpdate =
        new WrapperPlayServerPlayerInfoUpdate(
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED, playerInfo);

    PacketEvents.getAPI().getPlayerManager().sendPacket(observer, playerInfoUpdate);
  }

  public void showPlayer(Player observer, Player target) {
    observer.showPlayer(AstralPractice.getInstance(), target);
    showTabEntry(observer, target);
  }

  /**
   * Re-lists {@code target} in the tab list of {@code observer} without touching the display name,
   * so the global name the server already broadcasts (LuckPerms, TAB, {@link NametagManager}) is
   * kept.
   */
  private void showTabEntry(Player observer, Player target) {
    showTabEntry(observer, target, NametagManager.getInstance().getTabListName(target));
  }

  /**
   * Sends the tab list entry of {@code target} to {@code observer} only, using the given display
   * name. The name stays viewer specific, so it does not overwrite the global tab list name other
   * viewers see.
   */
  private void showTabEntry(Player observer, Player target, Component tabName) {
    if (!observer.isOnline() || !target.isOnline()) {
      return;
    }

    List<TextureProperty> properties =
        target.getPlayerProfile().getProperties().stream()
            .map(
                property ->
                    new TextureProperty(
                        property.getName(), property.getValue(), property.getSignature()))
            .toList();

    EnumSet<WrapperPlayServerPlayerInfoUpdate.Action> actions =
        EnumSet.of(
            WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LIST_ORDER);

    if (tabName != null) {
      actions.add(WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME);
    }

    WrapperPlayServerPlayerInfoUpdate packet =
        new WrapperPlayServerPlayerInfoUpdate(
            actions,
            new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                new UserProfile(target.getUniqueId(), target.getName(), properties),
                true,
                target.getPing(),
                GameMode.valueOf(target.getGameMode().name()),
                tabName,
                null,
                target.getPlayerListOrder()));

    PacketEvents.getAPI().getPlayerManager().sendPacket(observer, packet);
  }

  private boolean checkInvalidLobby() {
    if (ServerManager.getLobby() == null) {
      Common.sendConsoleMmMessage(PracticeConfig.language().getSetServerLobby());
      return true;
    }
    return false;
  }
}
