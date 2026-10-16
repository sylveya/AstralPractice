package dev.lokspel.practice.manager.gui.guis.profile;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.interfaces.Event;
import dev.lokspel.practice.manager.fight.ffa.FfaManager;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.RankedBan;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.spectator.SpectatorManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.ItemCreateUtil;
import dev.lokspel.practice.util.TimeUtil;
import dev.lokspel.practice.util.interfaces.Spectatable;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ProfileSetupGui extends Gui {

  private final Profile profile;
  private final ProfileLadderStats profileLadderStats;

  public ProfileSetupGui(Profile profile) {
    super(GuiType.Profile_Setup);
    this.profile = profile;
    this.profileLadderStats = new ProfileLadderStats(profile, this);

    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis()
                .getGuis()
                .getPlayerInformation()
                .getMainPage()
                .getTitle()
                .replace("%player%", Objects.requireNonNull(profile.getPlayer().getName())),
            4));

    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);
    if (inventory == null) {
      return;
    }

    ItemStack filler = GuiManager.fillerItem();
    for (int i : new int[] {27, 28, 29, 30, 31, 32, 33, 34, 35}) {
      inventory.setItem(i, filler);
    }

    inventory.setItem(10, getBasicInfoItem(profile));
    inventory.setItem(11, getOnlineItem(profile));
    inventory.setItem(13, getPartyItem(profile));
    inventory.setItem(14, getGameItem(profile));
    inventory.setItem(
        16,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getPlayerInformation()
                    .getMainPage()
                    .getIcons()
                    .getStatistics())
            .get());
    inventory.setItem(19, getRankedBanItem(profile));
    inventory.setItem(
        31,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getPlayerInformation()
                    .getMainPage()
                    .getIcons()
                    .getRefresh())
            .get());

    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();
    Inventory inventory = e.getView().getTopInventory();
    ItemStack item = e.getCurrentItem();
    ClickType click = e.getClick();

    e.setCancelled(true);

    if (inventory.getSize() > slot && item != null) {
      switch (slot) {
        case 14:
          Player target = profile.getPlayer().getPlayer();
          if (target == null) {
            return;
          }

          ProfileStatus profileStatus = profile.getStatus();
          switch (profileStatus) {
            case MATCH:
              Match match = MatchManager.getInstance().getLiveMatchByPlayer(target);
              if (match == null) {
                return;
              }

              if (click.isLeftClick()) {
                if (!player.hasPermission("ap.practice.info.teleport")) {
                  Common.sendMmMessage(
                      player, PracticeConfig.language().getProfile().getNoPermission());
                  return;
                }

                match.addSpectator(player, target, true, false);
              } else if (click.isRightClick()) {
                if (!player.hasPermission("ap.practice.info.cancel")) {
                  Common.sendMmMessage(
                      player, PracticeConfig.language().getProfile().getNoPermission());
                  return;
                }

                Common.sendMmMessage(
                    player,
                    PracticeConfig.language()
                        .getProfile()
                        .getPlayerRemovedMatch()
                        .replace("%target%", target.getName()));
                match.sendMessage(
                    PracticeConfig.language()
                        .getProfile()
                        .getRemovedPlayerMatch()
                        .replace("%target%", target.getName()),
                    true);

                match.removePlayer(target, false);
              }
              break;
            case FFA:
              Ffa ffa = FfaManager.getInstance().getFfaByPlayer(target);
              if (ffa == null) {
                return;
              }

              if (click.isLeftClick()) {
                if (!player.hasPermission("ap.practice.info.teleport")) {
                  Common.sendMmMessage(
                      player, PracticeConfig.language().getProfile().getNoPermission());
                  return;
                }

                ffa.addSpectator(player, target, true, false);
              } else if (click.isRightClick()) {
                if (!player.hasPermission("ap.practice.info.cancel")) {
                  Common.sendMmMessage(
                      player, PracticeConfig.language().getProfile().getNoPermission());
                  return;
                }

                ffa.removePlayer(target);
              }
              break;
            case EVENT:
              Event event = EventManager.getInstance().getEventByPlayer(target);
              if (event == null) {
                return;
              }

              if (click.isLeftClick()) {
                if (!player.hasPermission("ap.practice.info.teleport")) {
                  Common.sendMmMessage(
                      player, PracticeConfig.language().getProfile().getNoPermission());
                  return;
                }

                event.addSpectator(player, target, true, false);
              } else if (click.isRightClick()) {
                if (!player.hasPermission("ap.practice.info.cancel")) {
                  Common.sendMmMessage(
                      player, PracticeConfig.language().getProfile().getNoPermission());
                  return;
                }

                Common.sendMmMessage(
                    player,
                    PracticeConfig.language()
                        .getProfile()
                        .getPlayerRemovedEvent()
                        .replace("%target%", target.getName()));
                event.sendMessage(
                    PracticeConfig.language()
                        .getProfile()
                        .getRemovedPlayerEvent()
                        .replace("%target%", target.getName()),
                    true);

                event.removePlayer(target, false);
              }
              break;
            case SPECTATE:
              Spectatable spectatable =
                  SpectatorManager.getInstance().getSpectators().get(target.getUniqueId());

              if (click.isLeftClick()) {
                if (!player.hasPermission("ap.practice.info.teleport")) {
                  Common.sendMmMessage(
                      player, PracticeConfig.language().getProfile().getNoPermission());
                  return;
                }

                if (spectatable != null) {
                  spectatable.addSpectator(player, target, true, false);
                }
              } else if (click.isRightClick()) {
                if (!player.hasPermission("ap.practice.info.cancel")) {
                  Common.sendMmMessage(
                      player, PracticeConfig.language().getProfile().getNoPermission());
                  return;
                }

                if (spectatable != null) {
                  spectatable.removeSpectator(target);
                }

                Common.sendMmMessage(
                    player,
                    PracticeConfig.language()
                        .getProfile()
                        .getPlayerRemovedSpectator()
                        .replace("%target%", target.getName()));
              }
              break;

            default:
              break;
          }
          break;
        case 16:
          profileLadderStats.update();
          profileLadderStats.open(player);
          break;
        case 19:
          player.performCommand("practice ranked unban " + profile.getPlayer().getName());
          this.update();
          break;
        case 31:
          this.update();
          break;

        default:
          break;
      }
    }
  }

  private static ItemStack getBasicInfoItem(Profile profile) {
    GuiItem guiItem = new GuiItem(ItemCreateUtil.getPlayerHead(profile.getPlayer()));
    guiItem.setName(
        PracticeConfig.guis()
            .getGuis()
            .getPlayerInformation()
            .getMainPage()
            .getIcons()
            .getBasicInfo()
            .getName());
    guiItem.setLore(
        PracticeConfig.guis()
            .getGuis()
            .getPlayerInformation()
            .getMainPage()
            .getIcons()
            .getBasicInfo()
            .getLore());

    guiItem
        .replace("%player%", profile.getPlayer().getName())
        .replace("%uuid%", String.valueOf(profile.getUuid()))
        .replace("%first_played%", TimeUtil.formatDate(profile.getFirstJoin()))
        .replace(
            "%last_played%",
            (profile.getStatus().equals(ProfileStatus.OFFLINE)
                ? TimeUtil.formatDate(profile.getLastJoin())
                : PracticeConfig.guis()
                    .getGuis()
                    .getPlayerInformation()
                    .getMainPage()
                    .getIcons()
                    .getBasicInfo()
                    .getOnlineStatus()))
        .replace("%unranked_left%", String.valueOf(profile.getUnrankedLeft()))
        .replace("%ranked_left%", String.valueOf(profile.getRankedLeft()))
        .replace(
            "%division_fullName%",
            profile.getStats().getDivision() != null
                ? Common.mmToNormal(profile.getStats().getDivision().getFullName())
                : "<red>N/A")
        .replace(
            "%division_shortName%",
            profile.getStats().getDivision() != null
                ? Common.mmToNormal(profile.getStats().getDivision().getShortName())
                : "<red>N/A");

    return guiItem.get();
  }

  private static ItemStack getOnlineItem(Profile profile) {
    Player player = profile.getPlayer().getPlayer();

    if (profile.getStatus().equals(ProfileStatus.OFFLINE) || player == null) {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerInformation()
                  .getMainPage()
                  .getIcons()
                  .getOnlineInfo()
                  .getPlayerOffline())
          .get();
    } else {
      player.playerListName();
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerInformation()
                  .getMainPage()
                  .getIcons()
                  .getOnlineInfo()
                  .getPlayerOnline())
          .replace("%world%", player.getWorld().getName())
          .replace("%gamemode%", player.getGameMode().name())
          .replace("%flying%", String.valueOf(player.isFlying()))
          .replace(
              "%tablist_name%", Common.serializeComponentToLegacyString(player.playerListName()))
          .replace("%health%", String.valueOf(player.getHealth()))
          .replace("%food%", String.valueOf(player.getFoodLevel()))
          .replace("%hit_delay%", String.valueOf(player.getMaximumNoDamageTicks()))
          .replace("%ping%", String.valueOf(PlayerUtil.getPing(player)))
          .get();
    }
  }

  private static ItemStack getGameItem(Profile profile) {
    ProfileStatus profileStatus = profile.getStatus();

    return switch (profileStatus) {
      case OFFLINE ->
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getPlayerInformation()
                      .getMainPage()
                      .getIcons()
                      .getOnlineInfo()
                      .getGame()
                      .getOffline())
              .get();
      case MATCH ->
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getPlayerInformation()
                      .getMainPage()
                      .getIcons()
                      .getOnlineInfo()
                      .getGame()
                      .getInMatch())
              .get();
      case FFA ->
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getPlayerInformation()
                      .getMainPage()
                      .getIcons()
                      .getOnlineInfo()
                      .getGame()
                      .getInFfa())
              .get();
      case EVENT ->
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getPlayerInformation()
                      .getMainPage()
                      .getIcons()
                      .getOnlineInfo()
                      .getGame()
                      .getInEvent())
              .get();
      case SPECTATE ->
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getPlayerInformation()
                      .getMainPage()
                      .getIcons()
                      .getOnlineInfo()
                      .getGame()
                      .getSpectating())
              .get();
      default ->
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getPlayerInformation()
                      .getMainPage()
                      .getIcons()
                      .getOnlineInfo()
                      .getGame()
                      .getNothing())
              .get();
    };
  }

  private static ItemStack getPartyItem(Profile profile) {
    if (profile.isParty()) {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerInformation()
                  .getMainPage()
                  .getIcons()
                  .getParty()
                  .getInParty())
          .get();
    } else {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerInformation()
                  .getMainPage()
                  .getIcons()
                  .getParty()
                  .getNotInParty())
          .get();
    }
  }

  private static ItemStack getRankedBanItem(Profile profile) {
    if (profile.getRankedBan().isBanned()) {
      RankedBan rankedBan = profile.getRankedBan();
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getPlayerInformation()
                  .getMainPage()
                  .getIcons()
                  .getRankedBan())
          .replace("%player%", profile.getPlayer().getName())
          .replace(
              "%banner%",
              rankedBan.getBanner() == null
                  ? "<red>Console"
                  : rankedBan.getBanner().getPlayer().getName())
          .replace("%reason%", rankedBan.getReason() == null ? "<red>N/A" : rankedBan.getReason())
          .replace("%time%", TimeUtil.formatDate(rankedBan.getTime()))
          .get();
    } else {
      return null;
    }
  }
}
