package dev.lokspel.practice.manager.ladder.abstraction;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.fight.match.enums.MatchType;
import dev.lokspel.practice.manager.ladder.enums.KnockbackType;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import dev.lokspel.practice.manager.ladder.util.LadderKnockback;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.kit.KitData;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
public abstract class Ladder {

  protected final String name;
  @Setter protected String displayName;
  protected ItemStack icon;
  @Setter protected LadderType type;
  @Setter protected boolean enabled;

  // Settings the admins/players can set.
  @Setter protected KitData kitData;
  protected final LadderKnockback ladderKnockback;
  @Setter protected boolean regen = true;
  @Setter protected boolean hunger = true;
  @Setter protected boolean build;
  @Setter protected double attackCooldownModifier = 1.0;
  @Setter protected int rounds = 1;
  protected int hearts = 10;

  // Cooldowns
  @Setter
  protected double enderPearlCooldown =
      PracticeConfig.settings().getMatchSettings().getCooldown().getEnderPearl().getSeconds();

  @Setter
  protected double goldenAppleCooldown =
      PracticeConfig.settings().getMatchSettings().getCooldown().getGoldenApple().getSeconds();

  @Setter
  protected double fireworkRocketCooldown =
      PracticeConfig.settings().getMatchSettings().getCooldown().getFireworkRocket().getSeconds();

  @Setter
  protected double windChargeCooldown =
      PracticeConfig.settings().getMatchSettings().getCooldown().getWindCharge().getSeconds();

  protected List<MatchType> matchTypes = new ArrayList<>();

  // Cannot be set by player in custom
  @Setter protected int startCountdown = 3;
  @Setter protected int tntFuseTime = 4;
  @Setter protected boolean tntAutoIgnite = false;
  @Setter protected boolean pullPlayersWithRod = true;
  @Setter protected int maxDuration = 600;
  @Setter protected boolean multiRoundStartCountdown = true;
  @Setter protected boolean dropInventory = false;
  @Setter protected boolean startMove = true;
  @Setter protected boolean healthBelowName = false;
  @Setter protected boolean resetBuildAfterRound = false;
  @Setter protected boolean breakAllBlocks = false;
  @Setter protected int roundEndDelay = 3;
  @Setter protected boolean roundStatusTitles = true;
  @Setter protected boolean countdownTitles = true;

  protected Ladder(String name, LadderType type) {
    this.name = name;
    this.displayName = name;
    this.type = type;
    this.kitData = new KitData();
    this.ladderKnockback = new LadderKnockback();
  }

  protected Ladder(Ladder ladder) {
    this.name = ladder.getName();
    this.displayName = ladder.getDisplayName();
    if (ladder.getIcon() != null) {
      this.icon = ladder.getIcon().clone();
    }
    this.kitData = new KitData(ladder.getKitData());
    this.type = ladder.getType();
    this.enabled = ladder.isEnabled();
    this.ladderKnockback = new LadderKnockback(ladder.getLadderKnockback());
    this.regen = ladder.isRegen();
    this.hunger = ladder.isHunger();
    this.build = ladder.isBuild();
    this.attackCooldownModifier = ladder.getAttackCooldownModifier();
    this.rounds = ladder.getRounds();
    this.hearts = ladder.getHearts();
    this.enderPearlCooldown = ladder.getEnderPearlCooldown();
    this.goldenAppleCooldown = ladder.getGoldenAppleCooldown();
    this.fireworkRocketCooldown = ladder.getFireworkRocketCooldown();
    this.windChargeCooldown = ladder.getWindChargeCooldown();
    this.matchTypes = new ArrayList<>(ladder.getMatchTypes());
    this.startCountdown = ladder.getStartCountdown();
    this.tntFuseTime = ladder.getTntFuseTime();
    this.tntAutoIgnite = ladder.isTntAutoIgnite();
    this.pullPlayersWithRod = ladder.isPullPlayersWithRod();
    this.maxDuration = ladder.getMaxDuration();
    this.multiRoundStartCountdown = ladder.isMultiRoundStartCountdown();
    this.dropInventory = ladder.isDropInventory();
    this.startMove = ladder.isStartMove();
    this.healthBelowName = ladder.isHealthBelowName();
    this.resetBuildAfterRound = ladder.isResetBuildAfterRound();
    this.breakAllBlocks = ladder.isBreakAllBlocks();
    this.roundEndDelay = ladder.getRoundEndDelay();
    this.roundStatusTitles = ladder.isRoundStatusTitles();
    this.countdownTitles = ladder.isCountdownTitles();
  }

  public abstract List<Arena> getArenas();

  public void setHearts(int hearts) {
    this.hearts = Math.clamp(hearts, 1, 20);
  }

  public List<Arena> getAvailableArenas() {
    List<Arena> arenas = new ArrayList<>();
    for (Arena arena : getArenas()) {
      if (arena.getAvailableArena() != null) {
        arenas.add(arena);
      }
    }
    return arenas;
  }

  public void setIcon(final ItemStack icon) {
    if (icon == null || icon.getType().equals(Material.AIR)) {
      return;
    }

    this.icon = icon.clone();

    String iconDisplayName = Common.getItemDisplayNameMiniMessage(icon);
    if (!iconDisplayName.isBlank()) {
      this.displayName = iconDisplayName;
    } else {
      this.displayName = name;
    }
  }

  public ItemStack getIcon() {
    if (this.icon == null) {
      return null;
    }
    return this.icon.clone();
  }

  public abstract void setData();

  public abstract void getData();

  public abstract boolean isReadyToEnable();

  public boolean isCombo() {
    return this.type == LadderType.COMBO
        || (this.ladderKnockback != null
            && this.ladderKnockback.getKnockbackType() == KnockbackType.COMBO);
  }
}
