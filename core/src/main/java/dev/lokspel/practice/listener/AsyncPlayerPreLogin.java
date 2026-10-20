package dev.lokspel.practice.listener;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * Prevents players from joining the server until the plugin is fully loaded. Also prevents
 * duplicate username logins.
 */
public class AsyncPlayerPreLogin implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
    if (!AstralPractice.isFullyLoaded()) {
      event.disallow(
          AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
          Common.deserializeMiniMessage(PracticeConfig.language().getPluginLoadingMessage()));
      return;
    }

    boolean nameTakenByOther = Bukkit.getOnlinePlayers().stream()
        .anyMatch(
            p ->
                p.getName().equalsIgnoreCase(event.getName())
                    && !p.getUniqueId().equals(event.getUniqueId()));
    if (nameTakenByOther) {
      event.disallow(
          AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
          Common.deserializeMiniMessage(PracticeConfig.language().getDuplicateUsernameKick()));
    }
  }
}
