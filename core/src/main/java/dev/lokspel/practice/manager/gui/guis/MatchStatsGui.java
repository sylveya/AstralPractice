package dev.lokspel.practice.manager.gui.guis;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.Round;
import dev.lokspel.practice.manager.fight.util.stats.Statistic;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.TimeUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

public class MatchStatsGui extends Gui {

  private final Match match;
  private final OfflinePlayer player;
  private final Map<Integer, Statistic> stats = new HashMap<>();

  public MatchStatsGui(Match match, UUID uuid) {
    super(GuiType.MatchStatGui);
    this.match = match;
    this.player = Bukkit.getOfflinePlayer(uuid);

    for (Round round : match.getRounds().values()) {
      Statistic statistic = round.getStatistics().get(uuid);
      if (statistic != null) {
        stats.put(round.getRoundNumber(), statistic);
      }
    }

    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    if (!AstralPractice.getInstance().isEnabled()) {
      return;
    }

    int round = 1;
    while (stats.containsKey(round)) {
      String title;
      if (match.getLadder().getRounds() == 1) {
        title =
            PracticeConfig.guis()
                .getGuis()
                .getMatchStatistics()
                .getTitle()
                .getSingleRound()
                .replace("%player%", Objects.requireNonNull(player.getName()));
      } else {
        title =
            PracticeConfig.guis()
                .getGuis()
                .getMatchStatistics()
                .getTitle()
                .getMultipleRound()
                .replace("%player%", Objects.requireNonNull(player.getName()))
                .replace("%round%", String.valueOf(round));
      }
      gui.put(round, InventoryUtil.createInventory(title, 6));

      Statistic roundStatistic = stats.get(round);
      Inventory inventory = gui.get(round);

      // Inventory Content
      ItemStack[] endInv = roundStatistic.getEndInventory();
      int healthPotionsLeft = 0;
      if (endInv != null) {
        InventoryUtil.populatePlayerInventoryLayout(inventory, endInv);
        for (int i = 0; i < 36 && i < endInv.length; i++) {
          if (isHealingPotion(endInv[i])) {
            healthPotionsLeft++;
          }
        }
      }

      // Armor Content
      ItemStack[] endArmorArray = roundStatistic.getEndArmor();
      if (endArmorArray != null) {
        for (int i = 36; i <= 39 && (i - 36) < endArmorArray.length; i++) {
          inventory.setItem(i, endArmorArray[i - 36]);
        }
      }

      if (roundStatistic.getPotionThrown() != 0) {
        inventory.setItem(47, getPotionItem(roundStatistic, healthPotionsLeft));
      }
      inventory.setItem(48, getHealthItem(roundStatistic.getEndHeart()));
      inventory.setItem(49, getFoodItem(roundStatistic.getEndHunger()));
      inventory.setItem(50, getEffectItem(roundStatistic));
      inventory.setItem(51, getStatsItem(roundStatistic));

      if (match.getLadder().getRounds() != 1) {
        inventory.setItem(53, getNextRoundItem(stats.containsKey(round + 1) ? round + 1 : 1));
      }

      round++;
    }

    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    int round = inGuiPlayers.getOrDefault(player, 1);

    e.setCancelled(true);

    if (e.getRawSlot() == 53 && e.getCurrentItem() != null) {
      int nextRound = stats.containsKey(round + 1) ? round + 1 : 1;
      open(player, nextRound);
    }
  }

  private ItemStack getPotionItem(Statistic roundStatistic, int healthPotionsLeft) {
    ItemStack item =
        BackendUtil.getGuiItem(
                PracticeConfig.guis().getGuis().getMatchStatistics().getIcons().getPotion())
            .replace("%potion_left%", String.valueOf(healthPotionsLeft))
            .replace("%potion_thrown%", String.valueOf(roundStatistic.getPotionThrown()))
            .replace("%potion_missed%", String.valueOf(roundStatistic.getPotionMissed()))
            .replace("%potion_accuracy%", String.valueOf(roundStatistic.getPotionAccuracy()))
            .setAmount(healthPotionsLeft >= 1 && healthPotionsLeft <= 64 ? healthPotionsLeft : 1)
            .get();

    if (item != null && item.getItemMeta() instanceof PotionMeta potionMeta) {
      potionMeta.setBasePotionType(PotionType.HEALING);
      item.setItemMeta(potionMeta);
    }
    return item;
  }

  private boolean isHealingPotion(ItemStack item) {
    if (item == null || !(item.getItemMeta() instanceof PotionMeta potionMeta)) {
      return false;
    }
    PotionType type = potionMeta.getBasePotionType();
    return type == PotionType.HEALING || type == PotionType.STRONG_HEALING;
  }

  private ItemStack getHealthItem(double endHearth) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getMatchStatistics().getIcons().getHealth())
        .replace("%end_hearth%", String.valueOf(endHearth))
        .get();
  }

  private ItemStack getFoodItem(double endHunger) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getMatchStatistics().getIcons().getHunger())
        .replace("%end_hunger%", String.valueOf(endHunger))
        .get();
  }

  private ItemStack getEffectItem(Statistic roundStatistic) {
    if (!roundStatistic.getEndPotionEffects().isEmpty()) {
      List<String> effects = new ArrayList<>();
      for (PotionEffect potionEffect : roundStatistic.getEndPotionEffects()) {
        effects.add(
            PracticeConfig.guis()
                .getGuis()
                .getMatchStatistics()
                .getIcons()
                .getEffect()
                .getHasEffect()
                .getFormat()
                .replace(
                    "%name%",
                    StringUtils.capitalize(
                        potionEffect.getType().getKey().getKey().replace("_", " ").toLowerCase()))
                .replace("%amplifier%", String.valueOf(potionEffect.getAmplifier() + 1))
                .replace(
                    "%time%", TimeUtil.formatDuration((potionEffect.getDuration() / 20) * 1000L)));
      }

      List<String> lore = new ArrayList<>();
      for (String line :
          PracticeConfig.guis()
              .getGuis()
              .getMatchStatistics()
              .getIcons()
              .getEffect()
              .getHasEffect()
              .getIcon()
              .getLore()) {
        if (line.contains("%effects%")) {
          lore.addAll(effects);
        } else {
          lore.add(line);
        }
      }

      ItemStack item =
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getMatchStatistics()
                      .getIcons()
                      .getEffect()
                      .getHasEffect()
                      .getIcon())
              .get();
      ItemMeta itemMeta = item.getItemMeta();

      itemMeta.lore(Common.toComponent(lore));
      item.setItemMeta(itemMeta);

      return item;
    } else {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getMatchStatistics()
                  .getIcons()
                  .getEffect()
                  .getNoEffect())
          .get();
    }
  }

  private ItemStack getStatsItem(Statistic roundStatistic) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getMatchStatistics().getIcons().getStats())
        .replace("%total_hits%", String.valueOf(roundStatistic.getHit()))
        .replace("%total_hits_received%", String.valueOf(roundStatistic.getGetHit()))
        .replace("%longest_combo%", String.valueOf(roundStatistic.getLongestCombo()))
        .replace("%avarage_cps%", String.valueOf(roundStatistic.getAverageCps()))
        .get();
  }

  private ItemStack getNextRoundItem(int nextRound) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getMatchStatistics().getIcons().getViewRound())
        .replace("%round%", String.valueOf(nextRound))
        .get();
  }
}
