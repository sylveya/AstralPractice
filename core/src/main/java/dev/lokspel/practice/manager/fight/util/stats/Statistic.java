package dev.lokspel.practice.manager.fight.util.stats;

import dev.lokspel.practice.listener.StatisticListener;
import dev.lokspel.practice.util.NumberUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

@Getter
@Setter
public class Statistic {

  private final UUID uuid;
  private boolean isSet;

  private final Map<Long, Integer> cps = new HashMap<>();
  private double averageCps = 0.0;
  private int hit = 0;
  private int getHit = 0;
  private int longestCombo = 0;

  private int potionThrown = 0;
  private int potionMissed = 0;

  private int kills = 0;
  private int deaths = 0;
  private int killStreak = 0;
  private int bestKillStreak = 0;

  private double endHeart = 0;
  private double endHunger = 0;
  private List<PotionEffect> endPotionEffects = new ArrayList<>();
  private ItemStack[] endArmor = null;
  private ItemStack[] endInventory = null;

  public Statistic(UUID uuid) {
    this.uuid = uuid;
  }

  public Player getPlayer() {
    return Bukkit.getPlayer(uuid);
  }

  public void increaseKillStreak() {
    killStreak++;
    if (killStreak > bestKillStreak) {
      bestKillStreak = killStreak;
    }
  }

  public void resetKillStreak() {
    killStreak = 0;
  }

  public int getPotionAccuracy() {
    int var1 = potionThrown;
    return var1 == 0
        ? 0
        : (100 - (int) Math.round(Math.ceil(this.potionMissed / (double) var1 * 100.0D)));
  }

  public void end(boolean dead) {
    if (isSet) {
      return;
    }
    isSet = true;

    Player player = getPlayer();

    int sumCps = cps.values().stream().mapToInt(Integer::intValue).sum();
    if (sumCps != 0) {
      averageCps = NumberUtil.roundDouble(sumCps * 1.0 / cps.size());
      if (averageCps < 2) {
        averageCps = 0;
      }
    } else {
      averageCps = 0;
    }

    if (player != null) {
      if (StatisticListener.getCURRENT_COMBO().containsKey(player)) {
        int combo = StatisticListener.getCURRENT_COMBO().get(player);
        if (combo > longestCombo) {
          longestCombo = combo;
        }
      }

      if (!dead) {
        endHeart = NumberUtil.roundDouble(player.getHealth());
      } else {
        endHeart = 0;
      }

      endHunger = NumberUtil.roundDouble(player.getFoodLevel());
      endPotionEffects.addAll(player.getActivePotionEffects());
      endArmor = player.getInventory().getArmorContents();
      endInventory = player.getInventory().getContents();

      StatisticListener.getCURRENT_CPS().remove(player);
      StatisticListener.getCPS().remove(player);
      StatisticListener.getCURRENT_COMBO().remove(player);
    }
  }
}
