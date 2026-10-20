package dev.lokspel.practice.manager.gui.guis.profile;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.nametag.LobbyNametagUtil;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfilePrefixVisibility;
import dev.lokspel.practice.manager.profile.enums.ProfileWorldTime;
import dev.lokspel.practice.manager.sidebar.SidebarManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.StringUtil;
import dev.lokspel.practice.util.cooldown.CooldownObject;
import dev.lokspel.practice.util.cooldown.PlayerCooldown;
import dev.lokspel.practice.util.entityhider.PlayerHider;
import dev.lokspel.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
public class ProfileSettingsGui extends Gui {

  private final Profile profile;

  public ProfileSettingsGui(final Profile profile) {
    super(GuiType.Profile_Settings);
    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis()
                .getGuis()
                .getPlayerSettings()
                .getTitle()
                .replace("%player%", profile.getPlayer().getName()),
            4));
    this.profile = profile;

    this.build();
  }

  @Override
  public void build() {
    Inventory inventory = gui.get(1);
    if (inventory == null) {
      return;
    }
    ItemStack fillerItem =
        BackendUtil.getGuiItem(
                PracticeConfig.guis().getGuis().getPlayerSettings().getIcons().getFillerItem())
            .get();

    for (int i = 0; i < inventory.getSize(); i++) {
      inventory.setItem(i, fillerItem);
    }

    update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);

    inventory.setItem(10, getDuelRequestItem(profile.isDuelRequest()));
    inventory.setItem(20, getAutoQueueItem(profile.isAutoQueue()));
    inventory.setItem(11, getSidebarItem(profile.isSidebar()));
    inventory.setItem(12, getPartyInviteItem(profile.isPartyInvites()));
    inventory.setItem(13, getPrivateMessageItem(profile.isPrivateMessages()));
    inventory.setItem(14, getHidePlayersItem(profile.isHidePlayers()));
    inventory.setItem(15, getAllowSpectatorsItem(profile.isAllowSpectate()));
    inventory.setItem(
        16,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getPlayerSettings()
                    .getIcons()
                    .getSidebar()
                    .getWorldTime())
            .replace("%worldTime%", profile.getWorldTime().getName())
            .get());
    inventory.setItem(23, getFlyingItem(profile.isFlying()));
    inventory.setItem(21, getPrefixVisibilityItem(profile.getPrefixVisibility()));

    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    Profile profile = ProfileManager.getInstance().getProfile(player);
    ItemStack item = e.getCurrentItem();
    final int slot = e.getRawSlot();

    e.setCancelled(true);

    if (profile == null) {
      return;
    }
    if (item == null) {
      return;
    }
    if (item.equals(GuiManager.fillerItem())) {
      return;
    }

    if (e.getView().getTopInventory().getSize() > slot) {
      if (profile != this.profile) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getProfile()
                .getCantChangeTargetSettings()
                .replace("%target%", this.getProfile().getPlayer().getName()));
        return;
      }

      if (PlayerCooldown.isActive(player, CooldownObject.PLAYER_SETTINGS)) {
        Common.sendMmMessage(
            player,
            StringUtil.replaceSecondString(
                PracticeConfig.language().getProfile().getToggleCooldown(),
                PlayerCooldown.getLeftInDouble(player, CooldownObject.PLAYER_SETTINGS)));
        return;
      }

      switch (slot) {
        case 10:
          if (player.hasPermission("ap.settings.duelrequest")) {
            profile.setDuelRequest(!profile.isDuelRequest());

            update();
            if (!player.hasPermission("ap.bypass.cooldown")) {
              PlayerCooldown.addCooldown(
                  player,
                  CooldownObject.PLAYER_SETTINGS,
                  PracticeConfig.settings().getPlayer().getSettingsDelay());
            }
          } else {
            Common.sendMmMessage(player, PracticeConfig.language().getProfile().getNoPermission());
          }

          break;
        case 20:
          if (player.hasPermission("ap.settings.autoqueue")) {
            profile.setAutoQueue(!profile.isAutoQueue());

            update();
            if (!player.hasPermission("ap.bypass.cooldown")) {
              PlayerCooldown.addCooldown(
                  player,
                  CooldownObject.PLAYER_SETTINGS,
                  PracticeConfig.settings().getPlayer().getSettingsDelay());
            }
          } else {
            Common.sendMmMessage(player, PracticeConfig.language().getProfile().getNoPermission());
          }

          break;
        case 11:
          if (player.hasPermission("ap.settings.scoreboard")) {
            if (!SidebarManager.getInstance().isSidebarGloballyEnabled()) {
              Common.sendMmMessage(
                  player, PracticeConfig.language().getProfile().getSidebarGloballyDisabled());
              return;
            }

            profile.setSidebar(!profile.isSidebar());

            if (profile.isSidebar()) {
              SidebarManager.getInstance().loadSidebar(player);
            } else {
              SidebarManager.getInstance().unLoadSidebar(player);
            }

            profile.getFile().saveSidebarSetting();
            update();
            if (!player.hasPermission("ap.bypass.cooldown")) {
              PlayerCooldown.addCooldown(
                  player,
                  CooldownObject.PLAYER_SETTINGS,
                  PracticeConfig.settings().getPlayer().getSettingsDelay());
            }
          } else {
            Common.sendMmMessage(player, PracticeConfig.language().getProfile().getNoPermission());
          }

          break;
        case 12:
          if (player.hasPermission("ap.settings.partyinvite")) {
            profile.setPartyInvites(!profile.isPartyInvites());

            update();
            if (!player.hasPermission("ap.bypass.cooldown")) {
              PlayerCooldown.addCooldown(
                  player,
                  CooldownObject.PLAYER_SETTINGS,
                  PracticeConfig.settings().getPlayer().getSettingsDelay());
            }
          } else {
            Common.sendMmMessage(player, PracticeConfig.language().getProfile().getNoPermission());
          }

          break;
        case 13:
          if (player.hasPermission("ap.settings.privatemessage")) {
            if (player.hasPermission("ap.settings.privatemessage")) {
              profile.setPrivateMessages(!profile.isPrivateMessages());

              update();
              if (!player.hasPermission("ap.bypass.cooldown")) {
                PlayerCooldown.addCooldown(
                    player,
                    CooldownObject.PLAYER_SETTINGS,
                    PracticeConfig.settings().getPlayer().getSettingsDelay());
              }
            } else {
              Common.sendMmMessage(
                  player, PracticeConfig.language().getProfile().getNoPermission());
            }
          } else {
            Common.sendMmMessage(player, PracticeConfig.language().getProfile().getPrivateMsgOff());
          }

          break;
        case 14:
          if (player.hasPermission("ap.settings.playerhide")) {
            profile.setHidePlayers(!profile.isHidePlayers());
            PlayerHider.getInstance().toggleLobbyVisibility(player);

            update();
            if (!player.hasPermission("ap.bypass.cooldown")) {
              PlayerCooldown.addCooldown(
                  player,
                  CooldownObject.PLAYER_SETTINGS,
                  PracticeConfig.settings().getPlayer().getSettingsDelay());
            }
          } else {
            Common.sendMmMessage(player, PracticeConfig.language().getProfile().getNoPermission());
          }

          break;
        case 15:
          if (player.hasPermission("ap.settings.allowspectate")) {
            profile.setAllowSpectate(!profile.isAllowSpectate());

            update();
            if (!player.hasPermission("ap.bypass.cooldown")) {
              PlayerCooldown.addCooldown(
                  player,
                  CooldownObject.PLAYER_SETTINGS,
                  PracticeConfig.settings().getPlayer().getSettingsDelay());
            }
          } else {
            Common.sendMmMessage(player, PracticeConfig.language().getProfile().getNoPermission());
          }

          break;
        case 16:
          if (player.hasPermission("ap.settings.worldtime")) {
            ProfileWorldTime newTime = ProfileWorldTime.getNextWorldTime(profile.getWorldTime());
            profile.setWorldTime(newTime);
            PlayerUtil.setPlayerWorldTime(player);

            update();
            if (!player.hasPermission("ap.bypass.cooldown")) {
              PlayerCooldown.addCooldown(
                  player,
                  CooldownObject.PLAYER_SETTINGS,
                  PracticeConfig.settings().getPlayer().getSettingsDelay());
            }
          } else {
            Common.sendMmMessage(player, PracticeConfig.language().getProfile().getNoPermission());
          }

          break;
        case 23:
          if (player.hasPermission("ap.settings.fly")) {
            profile.setFlying(!profile.isFlying());

            if (profile.isFlying()) {
              player.setAllowFlight(true);
              player.setFlying(true);
            } else {
              player.setFlying(false);
              player.setAllowFlight(false);
            }

            update();
            if (!player.hasPermission("ap.bypass.cooldown")) {
              PlayerCooldown.addCooldown(
                  player,
                  CooldownObject.PLAYER_SETTINGS,
                  PracticeConfig.settings().getPlayer().getSettingsDelay());
            }
          } else {
            Common.sendMmMessage(player, PracticeConfig.language().getProfile().getNoPermission());
          }
          break;
        case 21:
          if (player.hasPermission("ap.settings.prefixvisibility")) {
            ProfilePrefixVisibility newVisibility =
                ProfilePrefixVisibility.getNext(profile.getPrefixVisibility());
            profile.setPrefixVisibility(newVisibility);
            LobbyNametagUtil.setLobbyNametag(player, profile);

            update();
            if (!player.hasPermission("ap.bypass.cooldown")) {
              PlayerCooldown.addCooldown(
                  player,
                  CooldownObject.PLAYER_SETTINGS,
                  PracticeConfig.settings().getPlayer().getSettingsDelay());
            }
          } else {
            Common.sendMmMessage(player, PracticeConfig.language().getProfile().getNoPermission());
          }
          break;

        default:
          break;
      }
    }
  }

  private static ItemStack getDuelRequestItem(boolean duelRequest) {
    if (duelRequest) {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerSettings()
                  .getIcons()
                  .getDuelRequest()
                  .getEnabled())
          .get();
    }

    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getPlayerSettings()
                .getIcons()
                .getDuelRequest()
                .getDisabled())
        .get();
  }

  private static ItemStack getAutoQueueItem(boolean autoQueue) {
    if (autoQueue) {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerSettings()
                  .getIcons()
                  .getAutoQueue()
                  .getEnabled())
          .get();
    }

    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getPlayerSettings()
                .getIcons()
                .getAutoQueue()
                .getDisabled())
        .get();
  }

  private static ItemStack getSidebarItem(boolean sidebar) {
    if (sidebar) {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerSettings()
                  .getIcons()
                  .getSidebar()
                  .getEnabled())
          .get();
    }

    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getPlayerSettings()
                .getIcons()
                .getSidebar()
                .getDisabled())
        .get();
  }

  private static ItemStack getPartyInviteItem(boolean partyInvites) {
    if (partyInvites) {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerSettings()
                  .getIcons()
                  .getSidebar()
                  .getPartyInvite()
                  .getEnabled())
          .get();
    }

    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getPlayerSettings()
                .getIcons()
                .getSidebar()
                .getPartyInvite()
                .getDisabled())
        .get();
  }

  private static ItemStack getPrivateMessageItem(boolean privatMessage) {
    if (privatMessage) {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerSettings()
                  .getIcons()
                  .getSidebar()
                  .getPrivateMessage()
                  .getEnabled())
          .get();
    }

    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getPlayerSettings()
                .getIcons()
                .getSidebar()
                .getPrivateMessage()
                .getDisabled())
        .get();
  }

  private static ItemStack getHidePlayersItem(boolean hidePlayers) {
    if (!hidePlayers) {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerSettings()
                  .getIcons()
                  .getSidebar()
                  .getHidePlayers()
                  .getEnabled())
          .get();
    }

    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getPlayerSettings()
                .getIcons()
                .getSidebar()
                .getHidePlayers()
                .getDisabled())
        .get();
  }

  private static ItemStack getAllowSpectatorsItem(boolean allowSpectate) {
    if (allowSpectate) {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerSettings()
                  .getIcons()
                  .getSidebar()
                  .getAllowSpectators()
                  .getEnabled())
          .get();
    }

    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getPlayerSettings()
                .getIcons()
                .getSidebar()
                .getAllowSpectators()
                .getDisabled())
        .get();
  }

  private static ItemStack getFlyingItem(boolean flying) {
    if (flying) {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerSettings()
                  .getIcons()
                  .getSidebar()
                  .getFlying()
                  .getEnabled())
          .get();
    } else {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerSettings()
                  .getIcons()
                  .getSidebar()
                  .getFlying()
                  .getDisabled())
          .get();
    }
  }

  private static ItemStack getPrefixVisibilityItem(ProfilePrefixVisibility visibility) {
    if (visibility == null) {
      visibility = ProfilePrefixVisibility.PREFIX_AND_SUFFIX;
    }

    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getPlayerSettings()
                .getIcons()
                .getSidebar()
                .getPrefixVisibility())
        .replace("%prefixVisibility%", visibility.getName())
        .get();
  }
}
