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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class StatisticListener implements Listener {

  private boolean customCooldownEnabled;
  private boolean onlySword;
  private double customCooldownValue;

  @Getter protected final AstralPractice practice = AstralPractice.getInstance();
  private static final Map<UUID, Integer> currentCps = new ConcurrentHashMap<>();
  private static final Map<UUID, Integer> cps = new ConcurrentHashMap<>();
  private static final Map<UUID, Integer> currentCombo = new ConcurrentHashMap<>();

  public static int getCurrentCombo(Player player) {
    if (player == null) {
      return 0;
    }
    return currentCombo.getOrDefault(player.getUniqueId(), 0);
  }

  public static int getCps(Player player) {
    if (player == null) {
      return 0;
    }
    return cps.getOrDefault(player.getUniqueId(), 0);
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
    currentCps.remove(uuid);
    cps.remove(uuid);
    currentCombo.remove(uuid);
  }

  public StatisticListener() {
    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> {
              customCooldownEnabled =
                  PracticeConfig.settings()
                      .getMatchSettings()
                      .getBoxing()
                      .getCustomAttackCooldown()
                      .isEnabled();
              onlySword =
                  PracticeConfig.settings()
                      .getMatchSettings()
                      .getBoxing()
                      .getCustomAttackCooldown()
                      .isOnlySword();
              double value =
                  PracticeConfig.settings()
                      .getMatchSettings()
                      .getBoxing()
                      .getCustomAttackCooldown()
                      .getCountFrom();
              if (value < 0.0 || value > 1.0) {
                value = 0.0;
              }
              customCooldownValue = value;
            },
            20L);
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
    currentCps.putIfAbsent(uuid, 1);
    currentCps.computeIfPresent(uuid, (key, val) -> val + 1);

    BukkitRunnable task = cpsRunnable(statistic, uuid);
    task.runTaskLaterAsynchronously(AstralPractice.getInstance(), 20L);
  }

  protected static @NotNull BukkitRunnable cpsRunnable(final Statistic statistic, UUID uuid) {
    return new BukkitRunnable() {
      @Override
      public void run() {
        // Remove atomically — avoids the TOCTOU race between containsKey and get
        // that can return null and cause an NPE when unboxing to int.
        Integer current = currentCps.remove(uuid);
        if (current != null && current > 2) {
          statistic.getCps().put(System.currentTimeMillis(), current);
          cps.put(uuid, current);
        }
      }
    };
  }

  protected static @NotNull BukkitRunnable hitRunnable(
      final Player attacker,
      final Statistic attackerStats,
      final Player defender,
      final Statistic defenderStats) {
    UUID attackerUuid = attacker.getUniqueId();
    UUID defenderUuid = defender.getUniqueId();
    return new BukkitRunnable() {
      @Override
      public void run() {
        if (attackerStats != null) {
          attackerStats.setHit(attackerStats.getHit() + 1);

          currentCombo.putIfAbsent(attackerUuid, 1);
          currentCombo.computeIfPresent(attackerUuid, (key, val) -> val + 1);
        }

        if (defenderStats != null) {
          defenderStats.setGetHit(defenderStats.getGetHit() + 1);

          Integer defenderCombo = currentCombo.get(defenderUuid);
          if (defenderCombo != null && defenderStats.getLongestCombo() < defenderCombo) {
            defenderStats.setLongestCombo(defenderCombo);
          }
          currentCombo.put(defenderUuid, 0);
        }

        // Immediately update scoreboards for real-time hit counter display
        // Schedule on main thread since scoreboard updates must be on main thread
        if (attacker != null && defender != null) {
          AstralPractice.getInstance()
              .getServer()
              .getScheduler()
              .runTask(
                  AstralPractice.getInstance(),
                  () -> SidebarManager.getInstance().updatePlayersSidebar(attacker, defender));
        }
      }
    };
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

    BukkitRunnable task = hitRunnable(attacker, attackerStats, defender, defenderStats);
    task.runTaskAsynchronously(AstralPractice.getInstance());
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
