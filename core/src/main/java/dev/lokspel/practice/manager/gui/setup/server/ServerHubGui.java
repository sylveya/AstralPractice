package dev.lokspel.practice.manager.gui.setup.server;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.division.DivisionManager;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.server.ServerManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.cooldown.CooldownObject;
import dev.lokspel.practice.util.cooldown.PlayerCooldown;
import dev.lokspel.practice.util.playerutil.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ServerHubGui extends Gui {

  private final ServerSaveGui serverSaveGui;
  private final ServerMatchesGui serverMatchesGui;
  private final ServerEventsGui serverEventsGui;
  private final ServerArmorLobbyGui serverArmorLobbyGui;

  public ServerHubGui() {
    super(GuiType.Server_Hub);
    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis().getGuis().getSetup().getServer().getServerManager().getTitle(),
            4));

    this.serverSaveGui = new ServerSaveGui(this);
    this.serverMatchesGui = new ServerMatchesGui(this);
    this.serverEventsGui = new ServerEventsGui(this);
    this.serverArmorLobbyGui = new ServerArmorLobbyGui(this);

    build();
  }

  @Override
  public void build() {
    Inventory inventory = gui.get(1);

    inventory.setItem(
        27,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getSetup()
                    .getServer()
                    .getServerManager()
                    .getIcons()
                    .getBackTo())
            .get());

    for (int i : new int[] {28, 29, 30, 31, 32, 33, 34, 35}) {
      inventory.setItem(i, GuiManager.fillerItem());
    }

    inventory.setItem(
        13,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getSetup()
                    .getServer()
                    .getServerManager()
                    .getIcons()
                    .getSave())
            .get());
    inventory.setItem(
        14,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getSetup()
                    .getServer()
                    .getServerManager()
                    .getIcons()
                    .getLobbyArmors())
            .get());

    update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);

    inventory.setItem(10, getInformationItem());
    inventory.setItem(15, getMatchesItem());
    inventory.setItem(16, getEventsItem());

    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    int slot = e.getRawSlot();
    Inventory inventory = e.getClickedInventory();
    ClickType click = e.getClick();

    e.setCancelled(true);

    if (inventory == null) {
      return;
    }

    if (inventory.getSize() > slot) {
      switch (slot) {
        case 27:
          GuiManager.getInstance().searchGui(GuiType.Setup_Hub).open(player);
          break;
        case 13:
          if (click.isLeftClick()) {
            serverSaveGui.update();
            serverSaveGui.open(player);
          } else if (click.isRightClick()) {
            if (PlayerCooldown.isActive(player, CooldownObject.SERVER_SETUP_SAVEALLDATA)) {
              Common.sendMmMessage(
                  player,
                  PracticeConfig.language().getCommand().getSetup().getServer().getWaitBefore());
              return;
            }

            Bukkit.getScheduler()
                .runTaskAsynchronously(
                    AstralPractice.getInstance(),
                    () -> ServerManager.getInstance().getAutoSaveRunnable().save());

            Common.sendMmMessage(
                player,
                PracticeConfig.language().getCommand().getSetup().getServer().getSavedAllFiles());

            PlayerCooldown.addCooldown(player, CooldownObject.SERVER_SETUP_SAVEALLDATA, 15);
          }
          break;
        case 14:
          serverArmorLobbyGui.update();
          serverArmorLobbyGui.open(player);
          break;
        case 15:
          serverMatchesGui.update();
          serverMatchesGui.open(player);
          break;
        case 16:
          serverEventsGui.update();
          serverEventsGui.open(player);
          break;

        default:
          break;
      }
    }
  }

  private ItemStack getInformationItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getServer()
                .getServerManager()
                .getIcons()
                .getInformations())
        .replace("%onlinePlayers%", String.valueOf(Bukkit.getOnlinePlayers().size()))
        .replace("%onlineStaffs%", String.valueOf(PlayerUtil.getOnlineStaff().size()))
        .replace(
            "%requiredDivision%",
            DivisionManager.getInstance().getMinimumForRanked() != null
                ? DivisionManager.getInstance().getMinimumForRanked().getFullName()
                : "<red>N/A")
        .replace(
            "%lobbyStatus%",
            ServerManager.getLobby() != null
                ? PracticeConfig.guis()
                    .getGuis()
                    .getSetup()
                    .getServer()
                    .getServerManager()
                    .getIcons()
                    .getInformations()
                    .getStatusNames()
                    .getSet()
                : PracticeConfig.guis()
                    .getGuis()
                    .getSetup()
                    .getServer()
                    .getServerManager()
                    .getIcons()
                    .getInformations()
                    .getStatusNames()
                    .getUnset())
        .replace(
            "%enabledArena%",
            String.valueOf(
                ArenaManager.getInstance().getEnabledArenas().size()
                    + ArenaManager.getInstance().getEnabledFfaArenas().size()))
        .replace(
            "%enabledLadder%",
            String.valueOf(LadderManager.getInstance().getEnabledLadders().size()))
        .replace(
            "%enabledEvents%", String.valueOf(EventManager.getInstance().getEnabledEvents().size()))
        .get();
  }

  private ItemStack getMatchesItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getServer()
                .getServerManager()
                .getIcons()
                .getMatches())
        .replace(
            "%liveMatches%", String.valueOf(MatchManager.getInstance().getLiveMatches().size()))
        .get();
  }

  private ItemStack getEventsItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getServer()
                .getServerManager()
                .getIcons()
                .getEvents())
        .replace("%liveEvents%", String.valueOf(EventManager.getInstance().getEvents().size()))
        .get();
  }
}
