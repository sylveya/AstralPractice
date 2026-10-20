package dev.lokspel.practice.manager.nametag;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import lombok.Getter;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.event.EventBus;
import me.neznamy.tab.api.event.player.PlayerLoadEvent;
import me.neznamy.tab.api.event.plugin.TabLoadEvent;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TabIntegration {

  private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

  private final TabAPI tabApi;
  private NameTagManager nameTagManager;
  private TabListFormatManager tabListFormatManager;

  @Getter private final boolean available;

  public TabIntegration() {
    TabAPI api = null;
    NameTagManager nametags = null;
    TabListFormatManager tabList = null;

    try {
      api = TabAPI.getInstance();
      nametags = api.getNameTagManager();
      tabList = api.getTabListFormatManager();
    } catch (NoClassDefFoundError | Exception ignored) {
      // This best-effort operation may fail without interrupting its caller.

    }

    this.tabApi = api;
    this.nameTagManager = nametags;
    this.tabListFormatManager = tabList;
    this.available = api != null;

    if (available) {
      registerPlayerLoadHandler();
      Bukkit.getOnlinePlayers().forEach(this::syncPlayer);
    }
  }

  public void hideNametag(Player player) {
    if (player == null) {
      return;
    }

    hideNametag(getTabPlayer(player));
  }

  public void hideNametag(TabPlayer tabPlayer) {
    if (nameTagManager == null || tabPlayer == null) {
      return;
    }

    try {
      nameTagManager.hideNameTag(tabPlayer);
    } catch (Exception ignored) {
      // This best-effort operation may fail without interrupting its caller.

    }
  }

  public boolean setTabListName(Player player, Component prefix, Component name, Component suffix) {
    if (tabListFormatManager == null) {
      return false;
    }

    TabPlayer tabPlayer = getTabPlayer(player);
    if (tabPlayer == null) {
      return false;
    }

    try {
      tabListFormatManager.setPrefix(tabPlayer, toLegacy(prefix));
      tabListFormatManager.setName(tabPlayer, toLegacy(name));
      tabListFormatManager.setSuffix(tabPlayer, toLegacy(suffix));
      return true;
    } catch (Exception ignored) {
      return false;
    }
  }

  private void registerPlayerLoadHandler() {
    try {
      EventBus eventBus = tabApi.getEventBus();
      if (eventBus == null) {
        return;
      }

      eventBus.register(
          PlayerLoadEvent.class,
          event -> {
            Object playerObj = event.getPlayer().getPlayer();
            if (playerObj instanceof Player player && player.isOnline()) {
              syncPlayer(player);
            }
          });

      eventBus.register(
          TabLoadEvent.class,
          _ -> {
            refreshManagers();
            // TAB rebuilds its managers on reload, so re-apply our per-player overrides immediately
            // after.
            Bukkit.getOnlinePlayers().forEach(this::syncPlayer);
          });
    } catch (Exception ignored) {
      // This best-effort operation may fail without interrupting its caller.

    }
  }

  private void refreshManagers() {
    try {
      nameTagManager = tabApi.getNameTagManager();
      tabListFormatManager = tabApi.getTabListFormatManager();
    } catch (Exception ignored) {
      // This best-effort operation may fail without interrupting its caller.

    }
  }

  private void syncPlayer(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile != null) {
      // This reuses the normal lobby formatting path so TAB and vanilla fallback stay consistent.
      Bukkit.getScheduler()
          .runTask(
              AstralPractice.getInstance(),
              () -> LobbyNametagUtil.setLobbyNametag(player, profile));
    }
  }

  private TabPlayer getTabPlayer(Player player) {
    if (!available || player == null) {
      return null;
    }

    try {
      return tabApi.getPlayer(player.getUniqueId());
    } catch (Exception ignored) {
      return null;
    }
  }

  private String toLegacy(Component component) {
    return component == null ? "" : LEGACY.serialize(component);
  }
}
