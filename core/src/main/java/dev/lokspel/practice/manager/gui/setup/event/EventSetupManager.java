package dev.lokspel.practice.manager.gui.setup.event;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.util.ArenaWorldUtil;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.interfaces.EventData;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.event.eventsettings.EventMainGui;
import dev.lokspel.practice.manager.gui.setup.event.eventsettings.SettingsGui;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EventSetupManager implements Listener {

  private static EventSetupManager instance;

  public static EventSetupManager getInstance() {
    if (instance == null) {
      instance = new EventSetupManager();
    }
    return instance;
  }

  @Getter public static final Map<ItemStack, EventData> eventMarkerList = new HashMap<>();
  @Getter private final Map<EventData, Map<GuiType, Gui>> eventSetupGuis = new HashMap<>();

  public EventSetupManager() {
    Bukkit.getPluginManager().registerEvents(this, AstralPractice.getInstance());
  }

  public void buildEventSetupGuis(EventData eventData) {
    Map<GuiType, Gui> guis = new HashMap<>();

    guis.put(GuiType.Event_Main, new EventMainGui(eventData));
    guis.put(GuiType.Event_Settings, new SettingsGui(eventData));

    eventSetupGuis.put(eventData, guis);
  }

  public void loadGuis() {
    GuiManager.getInstance().addGui(new EventSummaryGui());

    for (EventData eventData : EventManager.getInstance().getEventData().values()) {
      buildEventSetupGuis(eventData);
    }
  }

  /** Re-creates the summary and every event's setup GUIs from the reloaded configuration. */
  public void reloadGuis() {
    eventSetupGuis.clear();
    loadGuis();
  }

  @EventHandler
  public void onEventCornerMarkerUse(PlayerInteractEvent e) {
    Player player = e.getPlayer();
    Profile profile = ProfileManager.getInstance().getProfile(player);

    Action action = e.getAction();
    final ItemStack item = e.getItem();
    final Block block = e.getClickedBlock();

    switch (profile.getStatus()) {
      case MATCH:
      case FFA:
      case EVENT:
        return;

      default:
        break;
    }

    if (!player.hasPermission("ap.setup")) {
      return;
    }
    if (!action.equals(Action.LEFT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_BLOCK)) {
      return;
    }
    if (item == null) {
      return;
    }
    if (block == null) {
      return;
    }

    if (!eventMarkerList.containsKey(item)) {
      return;
    }
    EventData eventData = eventMarkerList.get(item);
    if (eventData == null) {
      return;
    }

    e.setCancelled(true);
    if (eventData.isEnabled()) {
      Common.sendMmMessage(
          player,
          PracticeConfig.language().getCommand().getSetup().getEvent().getCantEditEnabled());
      return;
    }

    Location blockLocation = block.getLocation();
    if (ArenaWorldUtil.getArenasCopyWorld() != null
        && blockLocation.getWorld().equals(ArenaWorldUtil.getArenasCopyWorld())) {
      Common.sendMmMessage(
          player, "<red>You cannot set event corners inside the arena copy world!");
      return;
    }

    try {
      if (action.equals(Action.LEFT_CLICK_BLOCK)) {
        if (eventData.getCuboidLoc2() != null
            && !eventData.getCuboidLoc2().getWorld().equals(blockLocation.getWorld())) {
          eventData.setCuboidLoc2(null);
        }
        eventData.setCuboidLoc1(blockLocation);
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getSetup()
                .getEvent()
                .getSetFirstCorner()
                .replace("%event%", eventData.getType().getName()));
      } else {
        if (eventData.getCuboidLoc1() != null
            && !eventData.getCuboidLoc1().getWorld().equals(blockLocation.getWorld())) {
          Common.sendMmMessage(player, "<red>Both corners must be located in the same world!");
          return;
        }
        eventData.setCuboidLoc2(blockLocation);
        Common.sendMmMessage(
            player,
            PracticeConfig.language()
                .getCommand()
                .getSetup()
                .getEvent()
                .getSetSecondCorner()
                .replace("%event%", eventData.getType().getName()));
      }
    } catch (Exception exception) {
      Common.sendMmMessage(player, "<red>" + exception.getMessage());
      return;
    }

    eventSetupGuis.get(eventData).get(GuiType.Event_Main).update();
  }
}
