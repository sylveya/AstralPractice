package dev.lokspel.practice.listener;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.ffa.FfaManager;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.enums.RoundStatus;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.fight.util.stats.Statistic;
import dev.lokspel.practice.manager.ladder.type.Boxing;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.sidebar.SidebarManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class StatisticListener implements Listener {

  private static boolean customCooldownEnabled;
  private static boolean onlySword;
  private static double customCooldownValue;

  @Getter protected final AstralPractice practice = AstralPractice.getInstance();
  private static final Map<UUID, List<Long>> clickTimestamps = new HashMap<>();
  private static final Map<UUID, Integer> currentCombo = new HashMap<>();

  public static int getCurrentCombo(Player player) {
    if (player == null) {
      return 0;
    }
    return currentCombo.getOrDefault(player.getUniqueId(), 0);
  }

  public static void clearPlayer(Player player) {
    if (player == null) {
      return;
    }
    clearPlayer(player.getUniqueId());
  }

  public static void clearPlayer(UUID uuid) {
    if (uuid == null) {
      return;
    }
    clickTimestamps.remove(uuid);
    currentCombo.remove(uuid);
  }

  public StatisticListener() {
    reload();
  }

  public static void reload() {
    var settings = PracticeConfig.settings();
    if (settings == null || settings.getMatchSettings() == null || settings.getMatchSettings().getBoxing() == null) {
      return;
    }
    var boxing = settings.getMatchSettings().getBoxing().getCustomAttackCooldown();
    if (boxing == null) {
      return;
    }
    customCooldownEnabled = boxing.isEnabled();
    onlySword = boxing.isOnlySword();
    double value = boxing.getCountFrom();
    if (value < 0.0 || value > 1.0) {
      value = 0.0;
    }
    customCooldownValue = value;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onClick(PlayerInteractEvent e) {
    Player player = e.getPlayer();
    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (PlayerUtil.getItemInUse(player, Material.FISHING_ROD) != null) {
      return;
    }

    Statistic statistic = null;
    switch (profile.getStatus()) {
      case MATCH:
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match != null && match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
          statistic = match.getCurrentStat(player);
        }
        break;
      case FFA:
        Ffa ffa = FfaManager.getInstance().getFfaByPlayer(player);
        if (ffa != null) {
          statistic = ffa.getStatistics().get(player);
        }
        break;

      default:
        break;
    }

    if (!e.getAction().equals(Action.LEFT_CLICK_AIR)
        && !e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
      return;
    }

    if (statistic == null || statistic.isSet()) {
      return;
    }

    UUID uuid = player.getUniqueId();
    long now = System.currentTimeMillis();
    List<Long> timestamps = clickTimestamps.computeIfAbsent(uuid, _ -> new ArrayList<>());
    timestamps.add(now);
    long windowStart = now - 1000L;
    timestamps.removeIf(time -> time < windowStart);

    int count = timestamps.size();
    if (count > 2) {
      statistic.getCps().put(now, count);
    }
  }

  protected static void processHit(
      final Player attacker,
      final Statistic attackerStats,
      final Player defender,
      final Statistic defenderStats) {
    UUID attackerUuid = attacker.getUniqueId();
    UUID defenderUuid = defender.getUniqueId();

    if (attackerStats != null) {
      attackerStats.setHit(attackerStats.getHit() + 1);

      int attackerCombo = currentCombo.getOrDefault(attackerUuid, 0) + 1;
      currentCombo.put(attackerUuid, attackerCombo);
    }

    if (defenderStats != null) {
      defenderStats.setGetHit(defenderStats.getGetHit() + 1);

      int defenderCombo = currentCombo.getOrDefault(defenderUuid, 0);
      if (defenderStats.getLongestCombo() < defenderCombo) {
        defenderStats.setLongestCombo(defenderCombo);
      }
      currentCombo.put(defenderUuid, 0);
    }

    SidebarManager.getInstance().updatePlayersSidebar(attacker, defender);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerHit(EntityDamageByEntityEvent e) {
    if (e.isCancelled()) {
      return;
    }

    if (!(e.getDamager() instanceof Player attacker)) {
      return;
    }
    Profile attackerProfile = ProfileManager.getInstance().getProfile(attacker);

    if (!(e.getEntity() instanceof Player defender)) {
      return;
    }
    Profile defenderProfile = ProfileManager.getInstance().getProfile(defender);

    Statistic attackerStats = null;
    Statistic defenderStats = null;
    switch (attackerProfile.getStatus()) {
      case MATCH:
        if (!defenderProfile.getStatus().equals(ProfileStatus.MATCH)) {
          return;
        }

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(attacker);
        if (match == null) {
          return;
        }
        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
          return;
        }

        attackerStats = match.getCurrentStat(attacker);
        defenderStats = match.getCurrentStat(defender);

        if (match.getLadder() instanceof Boxing) {
          if (customCooldownEnabled) {
            if (attacker.getAttackCooldown() < customCooldownValue) {
              return;
            }
            if (onlySword) {
              ItemStack itemInHand = attacker.getInventory().getItemInMainHand();
              if (!itemInHand.getType().name().toUpperCase().contains("SWORD")) {
                return;
              }
            }
          }
        }
        break;
      case FFA:
        Ffa ffa = FfaManager.getInstance().getFfaByPlayer(attacker);
        if (ffa == null) {
          return;
        }

        attackerStats = ffa.getStatistics().get(attacker);
        defenderStats = ffa.getStatistics().get(defender);
        break;

      default:
        break;
    }

    if (attackerStats == null || attackerStats.isSet()) {
      return;
    }

    processHit(attacker, attackerStats, defender, defenderStats);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPotionSplash(PotionSplashEvent e) {
    ThrownPotion potion = e.getPotion();
    if (!(potion.getShooter() instanceof Player player)) {
      return;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);

    Statistic statistic = null;
    switch (profile.getStatus()) {
      case MATCH:
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) {
          return;
        }

        statistic = match.getCurrentStat(player);
        break;
      case FFA:
        Ffa ffa = FfaManager.getInstance().getFfaByPlayer(player);
        if (ffa == null) {
          return;
        }

        statistic = ffa.getStatistics().get(player);
        break;

      default:
        break;
    }

    if (statistic == null || statistic.isSet()) {
      return;
    }

    // Check if the potion is a health potion
    if (isHealthPotion(potion)) {
      statistic.setPotionThrown(statistic.getPotionThrown() + 1);

      if (!e.getAffectedEntities().contains(player)) {
        statistic.setPotionMissed(statistic.getPotionMissed() + 1);
      }
    }
  }

  private static boolean isHealthPotion(ThrownPotion potion) {
    for (PotionEffect potionEffect : potion.getEffects()) {
      if (potionEffect.getType().equals(PotionEffectType.INSTANT_HEALTH)) {
        return true;
      }
    }
    return false;
  }
}
