package dev.lokspel.practice.manager.gui.guis;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.enums.EventType;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

@Getter
public class EventHostGui extends Gui {

  private final Map<Integer, EventType> eventSlots = new HashMap<>();

  public EventHostGui() {
    super(GuiType.Event_Host);
    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis().getGuis().getEventHost().getTitle(), 1));

    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    eventSlots.clear();
    gui.get(1).clear();

    for (EventType eventType : EventType.values()) {
      if (EventManager.getInstance().getEventData().get(eventType).isEnabled()) {
        int slot = gui.get(1).firstEmpty();
        eventSlots.put(slot, eventType);

        gui.get(1)
            .setItem(
                slot, EventManager.getInstance().getEventData().get(eventType).getIcon().get());
      }
    }
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    Profile profile = ProfileManager.getInstance().getProfile(player);
    int slot = e.getRawSlot();

    e.setCancelled(true);

    if (e.getView().getTopInventory().getSize() > slot) {
      if (eventSlots.containsKey(slot)) {
        EventType eventType = eventSlots.get(slot);

        if (!EventManager.getInstance().getEvents().isEmpty()
            && !PracticeConfig.settings().getEvent().isMultiple()) {
          Common.sendMmMessage(player, PracticeConfig.language().getEvent().getOnlyOneEvent());
          return;
        }

        if (!player.hasPermission("ap.event.host." + eventType.name().toLowerCase())
            && !player.hasPermission("ap.event.host.all")) {
          Common.sendMmMessage(
              player,
              PracticeConfig.language()
                  .getEvent()
                  .getCantHostEvent()
                  .replace("%event%", eventType.getName()));
          return;
        }

        if (profile.getEventStartLeft() == 0) {
          Common.sendMmMessage(
              player, PracticeConfig.language().getEvent().getCantHostEventToday());
          return;
        }

        if (eventType == EventType.BRACKETS) {
          new BracketsKitSelectorGui(player).open(player);
          return;
        }

        EventManager.getInstance().startEvent(player, eventType);
        player.closeInventory();
      }
    }
  }
}
