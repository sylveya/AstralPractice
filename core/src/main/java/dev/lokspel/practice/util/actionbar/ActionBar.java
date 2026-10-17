package dev.lokspel.practice.util.actionbar;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.util.Common;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class ActionBar {

  private final Profile profile;
  private static final int TICK_PERIOD = 2;
  private static final long INFINITE_EXPIRY = Long.MAX_VALUE;
  private static final long KEEPALIVE_INTERVAL_MILLIS = 1200L;
  private static final PlainTextComponentSerializer PLAIN_TEXT =
      PlainTextComponentSerializer.plainText();

  /** Stores active messages using an ID (e.g., "golden_head", "queue"). */
  private final Map<String, ActionMessage> activeMessages = new HashMap<>();

  private long sequenceCounter = 0L;

  private String lastSentSignature = "";
  private long lastSendAtMillis = 0L;

  /** Internal runnable to update the action bar periodically. */
  private BukkitRunnable actionBarRunnable;

  public ActionBar(Profile profile) {
    this.profile = profile;
  }

  /**
   * Set or update a message in the action bar.
   *
   * @param id Unique identifier for this action (e.g., "golden_head")
   * @param text The text to display (MiniMessage format)
   * @param duration Duration in seconds (-1 for infinite)
   * @param priority Priority level (higher weight overrides lower weight)
   */
  public void setMessage(String id, String text, int duration, ActionBarPriority priority) {
    if (!Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler()
          .runTask(AstralPractice.getInstance(), () -> setMessage(id, text, duration, priority));
      return;
    }

    if (id == null || id.isEmpty() || priority == null) {
      return;
    }

    Component component = Common.toComponent(text);
    String plainText = PLAIN_TEXT.serialize(component);
    if (plainText.isBlank()) {
      removeMessage(id);
      return;
    }

    long expiresAtMillis =
        duration < 0
            ? INFINITE_EXPIRY
            : (System.currentTimeMillis() + (Math.max(1, duration) * 1000L));

    activeMessages.put(
        id, new ActionMessage(component, plainText, expiresAtMillis, priority, ++sequenceCounter));

    startRunnable();
    sendHighestPriority(resolveLivePlayer(), true);
  }

  public void resetForReconnect() {
    if (!Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(AstralPractice.getInstance(), this::resetForReconnect);
      return;
    }

    activeMessages.clear();
    stopRunnable();

    Player player = resolveLivePlayer();
    if (player != null && player.isOnline()) {
      clearActionBarIfNeeded(player);
    } else {
      resetLastSentState();
    }
  }

  /**
   * Manually remove a specific action bar message before its duration expires.
   *
   * @param id Unique identifier of the message
   */
  public void removeMessage(String id) {
    if (!Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(AstralPractice.getInstance(), () -> removeMessage(id));
      return;
    }

    if (id == null || id.isEmpty()) {
      return;
    }

    if (activeMessages.remove(id) == null) {
      return;
    }

    Player player = resolveLivePlayer();
    if (player != null && player.isOnline()) {
      if (activeMessages.isEmpty()) {
        clearActionBarIfNeeded(player);
      } else {
        sendHighestPriority(player, true);
      }
    }

    if (activeMessages.isEmpty()) {
      stopRunnable();
    }
  }

  /** Starts the internal runnable if it's not already running. */
  private void startRunnable() {
    if (actionBarRunnable != null) {
      return;
    }

    actionBarRunnable =
        new BukkitRunnable() {
          @Override
          public void run() {
            try {
              tick();
            } catch (Exception exception) {
              AstralPractice.getInstance()
                  .getLogger()
                  .warning(
                      "ActionBar tick failed for "
                          + profile.getUuid()
                          + ": "
                          + exception.getMessage());
              stopRunnable();
            }
          }
        };

    actionBarRunnable.runTaskTimer(AstralPractice.getInstance(), TICK_PERIOD, TICK_PERIOD);
  }

  /** Stops the internal runnable if running. */
  private void stopRunnable() {
    if (actionBarRunnable != null) {
      actionBarRunnable.cancel();
      actionBarRunnable = null;
    }
  }

  /**
   * Called periodically by the internal runnable. Handles duration updates, message expiration, and
   * sending the highest priority message.
   */
  private void tick() {
    Player player = resolveLivePlayer();
    if (player == null || !player.isOnline()) {
      activeMessages.clear();
      resetLastSentState();
      stopRunnable();
      return;
    }

    pruneExpiredMessages();

    if (!activeMessages.isEmpty()) {
      sendHighestPriority(player, false);
    } else {
      clearActionBarIfNeeded(player);
      stopRunnable();
    }
  }

  private void pruneExpiredMessages() {
    long now = System.currentTimeMillis();
    activeMessages.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
  }

  /**
   * Sends the highest priority message to the player. If multiple messages share the same priority,
   * the most recently updated one is shown.
   */
  private void sendHighestPriority(Player player, boolean force) {
    if (player == null || !player.isOnline()) {
      return;
    }

    String highestId = null;
    ActionMessage highest = null;
    for (Map.Entry<String, ActionMessage> entry : activeMessages.entrySet()) {
      if (highest == null || isHigherPriority(entry.getValue(), highest)) {
        highest = entry.getValue();
        highestId = entry.getKey();
      }
    }

    if (highest == null) {
      return;
    }

    long now = System.currentTimeMillis();
    String signature = highestId + "|" + highest.priority + "|" + highest.plainText;
    boolean winnerChanged = !signature.equals(lastSentSignature);
    boolean keepAliveDue = (now - lastSendAtMillis) >= KEEPALIVE_INTERVAL_MILLIS;

    if (!force && !winnerChanged && !keepAliveDue) {
      return;
    }

    try {
      player.sendActionBar(highest.component);
      lastSentSignature = signature;
      lastSendAtMillis = now;
    } catch (Exception exception) {
      AstralPractice.getInstance()
          .getLogger()
          .warning(
              "ActionBar send failed for " + profile.getUuid() + ": " + exception.getMessage());
    }
  }

  private boolean isHigherPriority(ActionMessage candidate, ActionMessage current) {
    if (candidate.priority.getWeight() != current.priority.getWeight()) {
      return candidate.priority.getWeight() > current.priority.getWeight();
    }
    return candidate.updatedAtSequence > current.updatedAtSequence;
  }

  private Player resolveLivePlayer() {
    return profile.getOnlinePlayer();
  }

  private void clearActionBarIfNeeded(Player player) {
    if (lastSentSignature.isEmpty()) {
      return;
    }

    player.sendActionBar(Component.empty());
    resetLastSentState();
  }

  private void resetLastSentState() {
    lastSentSignature = "";
    lastSendAtMillis = 0L;
  }

  /** Represents a single action bar message. */
  private record ActionMessage(
      Component component,
      String plainText,
      long expiresAtMillis,
      ActionBarPriority priority,
      long updatedAtSequence) {

    private boolean isExpired(long now) {
      return expiresAtMillis != INFINITE_EXPIRY && now >= expiresAtMillis;
    }
  }
}
