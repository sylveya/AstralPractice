package dev.lokspel.practice.manager.fight.event;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.event.enums.EventStatus;
import dev.lokspel.practice.manager.fight.event.enums.EventType;
import dev.lokspel.practice.manager.fight.event.events.duel.brackets.Brackets;
import dev.lokspel.practice.manager.fight.event.events.duel.brackets.BracketsData;
import dev.lokspel.practice.manager.fight.event.events.duel.brackets.BracketsListener;
import dev.lokspel.practice.manager.fight.event.events.duel.sumo.Sumo;
import dev.lokspel.practice.manager.fight.event.events.duel.sumo.SumoData;
import dev.lokspel.practice.manager.fight.event.events.duel.sumo.SumoListener;
import dev.lokspel.practice.manager.fight.event.events.ffa.lms.Lms;
import dev.lokspel.practice.manager.fight.event.events.ffa.lms.LmsData;
import dev.lokspel.practice.manager.fight.event.events.ffa.lms.LmsListener;
import dev.lokspel.practice.manager.fight.event.events.ffa.oitc.Oitc;
import dev.lokspel.practice.manager.fight.event.events.ffa.oitc.OitcData;
import dev.lokspel.practice.manager.fight.event.events.ffa.oitc.OitcListener;
import dev.lokspel.practice.manager.fight.event.events.ffa.splegg.Splegg;
import dev.lokspel.practice.manager.fight.event.events.ffa.splegg.SpleggData;
import dev.lokspel.practice.manager.fight.event.events.ffa.splegg.SpleggListener;
import dev.lokspel.practice.manager.fight.event.events.onevsall.juggernaut.Juggernaut;
import dev.lokspel.practice.manager.fight.event.events.onevsall.juggernaut.JuggernautData;
import dev.lokspel.practice.manager.fight.event.events.onevsall.juggernaut.JuggernautListener;
import dev.lokspel.practice.manager.fight.event.events.onevsall.tnttag.TntTag;
import dev.lokspel.practice.manager.fight.event.events.onevsall.tnttag.TntTagData;
import dev.lokspel.practice.manager.fight.event.events.onevsall.tnttag.TntTagListener;
import dev.lokspel.practice.manager.fight.event.interfaces.Event;
import dev.lokspel.practice.manager.fight.event.interfaces.EventData;
import dev.lokspel.practice.manager.fight.event.interfaces.EventListenerInterface;
import dev.lokspel.practice.manager.fight.event.setup.EventWandSetupManager;
import dev.lokspel.practice.manager.fight.match.util.TitleUtil;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.guis.EventHostGui;
import dev.lokspel.practice.manager.gui.setup.event.EventSetupManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.kit.KitData;
import dev.lokspel.practice.util.startup.StartUpCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Getter
public class EventManager {

  private static final long TITLE_FADE_IN_MS = 200;
  private static final long TITLE_STAY_MS = 1500;
  private static final long TITLE_FADE_OUT_MS = 300;

  private static EventManager instance;

  public static EventManager getInstance() {
    if (instance == null) {
      instance = new EventManager();
    }
    return instance;
  }

  private final List<Event> events;
  private final Map<EventType, EventData> eventData;
  private final Map<EventType, EventListenerInterface> eventListeners;

  private final EventListener listener;

  @Getter private AutoEventScheduler autoEventScheduler;

  public static final ItemStack PLAYER_TRACKER =
      BackendUtil.getGuiItem(PracticeConfig.settings().getEvent().getPlayerTracker()).get();

  private EventManager() {
    this.events = new ArrayList<>();
    this.eventData = new HashMap<>();
    this.eventListeners = new HashMap<>();

    this.listener = new EventListener(this);
    Bukkit.getServer()
        .getPluginManager()
        .registerEvents(this.listener, AstralPractice.getInstance());
  }

  public void loadEventData(final StartUpCallback startUpCallback) {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            AstralPractice.getInstance(),
            () -> {
              this.eventData.put(EventType.BRACKETS, new BracketsData());
              this.eventListeners.put(EventType.BRACKETS, new BracketsListener());

              this.eventData.put(EventType.SUMO, new SumoData());
              this.eventListeners.put(EventType.SUMO, new SumoListener());

              this.eventData.put(EventType.LMS, new LmsData());
              this.eventListeners.put(EventType.LMS, new LmsListener());

              this.eventData.put(EventType.OITC, new OitcData());
              this.eventListeners.put(EventType.OITC, new OitcListener());

              this.eventData.put(EventType.SPLEGG, new SpleggData());
              this.eventListeners.put(EventType.SPLEGG, new SpleggListener());

              this.eventData.put(EventType.JUGGERNAUT, new JuggernautData());
              this.eventListeners.put(EventType.JUGGERNAUT, new JuggernautListener());

              this.eventData.put(EventType.TNTTAG, new TntTagData());
              this.eventListeners.put(EventType.TNTTAG, new TntTagListener());

              for (EventData data : this.eventData.values()) {
                data.getData();
              }

              Bukkit.getScheduler()
                  .runTask(
                      AstralPractice.getInstance(),
                      () -> {
                        autoEventScheduler = new AutoEventScheduler();
                        autoEventScheduler.start();
                        startUpCallback.onLoadingDone();
                      });
            });
  }

  public void loadGuis() {
    GuiManager.getInstance().addGui(new EventHostGui());
    EventSetupManager.getInstance().loadGuis();

    // Initialize the wand-based setup manager
    EventWandSetupManager.getInstance();
  }

  /**
   * Re-creates the host GUI and the event setup GUIs from the reloaded configuration. Does nothing
   * while the event data is still loading, the GUIs are created once it is done.
   */
  public void reloadGuis() {
    if (GuiManager.getInstance().searchGui(GuiType.Event_Host) == null) {
      return;
    }

    GuiManager.getInstance().addGui(new EventHostGui());
    EventSetupManager.getInstance().reloadGuis();
  }

  public void endEvents() {
    if (autoEventScheduler != null) {
      autoEventScheduler.cancel();
    }
    for (Event event : new java.util.ArrayList<>(events)) {
      event.forceEnd(null);
    }
  }

  public void saveEventData() {
    for (EventData data : eventData.values()) {
      data.setData();
    }
  }

  public void startEvent(Player starter, EventType eventType) {
    startEvent(starter, eventType, null);
  }

  public void startEvent(Player starter, EventType eventType, KitData bracketsKitOverride) {
    if (eventType == null) {
      return;
    }

    if (!getEventData().get(eventType).isEnabled()) {
      AstralPractice.getInstance()
          .getLogger()
          .warning("Event " + eventType.getName() + " is not enabled.");
      return;
    }

    if (starter != null) {
      if (!starter.hasPermission("ap.event.host")
          || (!starter.hasPermission("ap.event.host." + eventType.name().toLowerCase())
              && !starter.hasPermission("ap.event.host.all"))) {
        Common.sendMmMessage(
            starter,
            PracticeConfig.language()
                .getEvent()
                .getCantHostEvent()
                .replace("%event%", eventType.getName()));
        return;
      }

      Profile starterProfile = ProfileManager.getInstance().getProfile(starter);
      if (starterProfile == null || starterProfile.getEventStartLeft() <= 0) {
        Common.sendMmMessage(starter, PracticeConfig.language().getEvent().getCantHostEventToday());
        return;
      }
    }

    if (!this.events.isEmpty() && PracticeConfig.settings().getEvent().isMultiple()) {
      for (Event liveEvent : this.events) {
        if (liveEvent.getStatus().equals(EventStatus.COLLECTING)) {
          if (starter != null) {
            Common.sendMmMessage(
                starter,
                PracticeConfig.language()
                    .getCommand()
                    .getEvent()
                    .getArguments()
                    .getHost()
                    .getCantHostNow());
          }
          return;
        }
      }
    } else if (!this.events.isEmpty() && !PracticeConfig.settings().getEvent().isMultiple()) {
      if (starter != null) {
        Common.sendMmMessage(starter, PracticeConfig.language().getEvent().getOnlyOneEvent());
      }
      return;
    }

    if (this.isEventLive(eventType)) {
      String msg =
          PracticeConfig.language()
              .getEvent()
              .getCantStartEvent()
              .replace("%event%", eventType.getName());
      if (starter != null) {
        Common.sendMmMessage(starter, msg);
      } else {
        Common.sendConsoleMmMessage(msg);
      }
      return;
    }

    Event event =
        switch (eventType) {
          case LMS -> new Lms(starter, (LmsData) eventData.get(EventType.LMS));
          case OITC -> new Oitc(starter, (OitcData) eventData.get(EventType.OITC));
          case TNTTAG -> new TntTag(starter, (TntTagData) eventData.get(EventType.TNTTAG));
          case BRACKETS ->
              new Brackets(
                  starter, (BracketsData) eventData.get(EventType.BRACKETS), bracketsKitOverride);
          case SUMO -> new Sumo(starter, (SumoData) eventData.get(EventType.SUMO));
          case SPLEGG -> new Splegg(starter, (SpleggData) eventData.get(EventType.SPLEGG));
          case JUGGERNAUT ->
              new Juggernaut(starter, (JuggernautData) eventData.get(EventType.JUGGERNAUT));
        };

    events.add(event);
    if (!event.startQueue()) {
      events.remove(event);
      return;
    }

    if (starter != null) {
      Profile starterProfile = ProfileManager.getInstance().getProfile(starter);
      if (starterProfile != null) {
        starterProfile.setEventStartLeft(Math.max(0, starterProfile.getEventStartLeft() - 1));
      }
    }
  }

  public boolean isEventLive(EventType eventType) {
    return events.stream().anyMatch(event -> event.getType().equals(eventType));
  }

  public List<EventData> getEnabledEvents() {
    return eventData.values().stream().filter(EventData::isEnabled).toList();
  }

  public Event getEventByPlayer(Player player) {
    return events.stream()
        .filter(event -> event.getPlayers().contains(player))
        .findFirst()
        .orElse(null);
  }

  public Event getEventBySpectator(Player player) {
    return events.stream()
        .filter(event -> event.getSpectators().contains(player))
        .findFirst()
        .orElse(null);
  }

  public int getPostKillDelayTicks() {
    int seconds = PracticeConfig.settings().getEvent().getDuel().getPostKillDelay();
    return Math.max(0, seconds) * 20;
  }

  public void sendConfiguredEventTitle(
      Player player, String title, String subtitle, Map<String, String> placeholders) {
    if (player == null) {
      return;
    }

    String titleRaw = applyPlaceholders(title, placeholders);
    String subtitleRaw = applyPlaceholders(subtitle, placeholders);

    TitleUtil.sendTitle(
        player,
        toTitleComponent(titleRaw),
        toTitleComponent(subtitleRaw),
        TITLE_FADE_IN_MS,
        TITLE_STAY_MS,
        TITLE_FADE_OUT_MS);
  }

  public void sendRawEventTitle(Player player, String titleRaw, String subtitleRaw) {
    if (player == null) {
      return;
    }

    TitleUtil.sendTitle(
        player,
        toTitleComponent(titleRaw),
        toTitleComponent(subtitleRaw),
        TITLE_FADE_IN_MS,
        TITLE_STAY_MS,
        TITLE_FADE_OUT_MS);
  }

  private String applyPlaceholders(String raw, Map<String, String> placeholders) {
    if (raw == null || placeholders == null || placeholders.isEmpty()) {
      return raw == null ? "" : raw;
    }

    String parsed = raw;
    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      parsed = parsed.replace(entry.getKey(), entry.getValue());
    }
    return parsed;
  }

  private Component toTitleComponent(String line) {
    if (line == null || line.isEmpty()) {
      return Component.empty();
    }

    return Common.deserializeMiniMessage(line);
  }
}
