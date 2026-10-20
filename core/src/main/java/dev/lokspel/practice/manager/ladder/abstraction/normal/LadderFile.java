package dev.lokspel.practice.manager.ladder.abstraction.normal;

import dev.lokspel.practice.data.DataFiles;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.BlockReturnDelay;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.CustomConfig;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.RespawnableLadder;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.TempBuildReturnDelay;
import dev.lokspel.practice.manager.ladder.enums.WeightClassType;
import dev.lokspel.practice.manager.ladder.type.Creeper;
import dev.lokspel.practice.manager.ladder.util.LadderFileUtil;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.items.BasicItem;
import dev.lokspel.practice.util.items.ItemSerializationUtil;
import dev.lokspel.practice.util.kit.KitData;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * Reads and writes the file {@code ladders/<name>.yml} of a ladder. The model read from the file is
 * kept and updated on every save, so keys the ladder does not write itself stay in the file.
 */
public class LadderFile {

  private final NormalLadder ladder;
  private final String file;
  private final LadderData data;

  public LadderFile(NormalLadder ladder) {
    this.ladder = ladder;
    this.file = "ladders/" + ladder.getName().toLowerCase() + ".yml";
    this.data = DataFiles.readOrCreate(file, LadderData.class, LadderData::new);
    if (data.getSettings() == null) {
      data.setSettings(new LadderData.Settings());
    }
  }

  /** Writes the ladder to its file. */
  public void setData() {
    data.setName(ladder.getName());
    data.setEnabled(ladder.isEnabled());
    data.setType(ladder.getType().toString());

    LadderData.Settings settings = data.getSettings();
    settings.setRegen(ladder.isRegen());
    settings.setHunger(ladder.isHunger());
    settings.setWeightClass(ladder.getWeightClass().toString());
    settings.setEditable(ladder.isEditable());
    settings.setDropInventory(ladder.isDropInventory());
    settings.setMultiRoundStartCountdown(ladder.isMultiRoundStartCountdown());
    settings.setHitDelay(ladder.getAttackCooldownModifier());
    settings.setRounds(ladder.getRounds());
    settings.setHearts(ladder.getHearts());
    settings.setMaxDuration(ladder.getMaxDuration());
    settings.setEnderPearlCooldown(ladder.getEnderPearlCooldown());
    settings.setGoldenAppleCooldown(ladder.getGoldenAppleCooldown());
    settings.setFireworkCooldown(ladder.getFireworkRocketCooldown());
    settings.setWindChargeCooldown(ladder.getWindChargeCooldown());
    settings.setStartCountdown(ladder.getStartCountdown());
    settings.setStartMove(ladder.isStartMove());
    settings.setMatchTypes(LadderFileUtil.getMatchTypeNames(ladder.getMatchTypes()));
    settings.setKnockback(ladder.getLadderKnockback().get());
    settings.setTntFuseTime(ladder.getTntFuseTime());
    settings.setTntAutoIgnite(ladder.isTntAutoIgnite());
    settings.setPullPlayersWithRod(ladder.isPullPlayersWithRod());
    settings.setHealthBelowName(ladder.isHealthBelowName());
    settings.setResetBuildAfterRound(ladder.isResetBuildAfterRound());
    settings.setBreakAllBlocks(ladder.isBreakAllBlocks());
    settings.setRoundEndDelay(ladder.getRoundEndDelay());
    settings.setRoundStatusTitles(ladder.isRoundStatusTitles());
    settings.setCountdownTitles(ladder.isCountdownTitles());

    if (ladder instanceof RespawnableLadder respawnableLadder) {
      settings.setRespawnTime(respawnableLadder.getRespawnTime());
    }

    if (ladder instanceof BlockReturnDelay blockReturnDelay) {
      settings.setBlockReturnDelay(blockReturnDelay.getBlockReturnDelaySeconds());
    }

    if (ladder instanceof TempBuildReturnDelay tempBuildReturnDelay) {
      settings.setTempBuildReturnDelay(tempBuildReturnDelay.getTempBuildReturnDelaySeconds());
    }

    if (ladder instanceof Creeper creeper) {
      settings.setCreeperExplosionDelay(creeper.getCreeperExplosionDelay());
    }

    data.setIcon(ladder.getIcon());

    if (ladder.getKitData() != null) {
      writeKit(ladder.getKitData());
    }

    if (ladder.isBuild()) {
      List<String> destroyableBlocks = new ArrayList<>();
      for (BasicItem destroyableBlock : ladder.getDestroyableBlocks()) {
        if (destroyableBlock.damage() == 0) {
          destroyableBlocks.add(destroyableBlock.material().name());
        } else {
          destroyableBlocks.add(
              destroyableBlock.material().name() + "::" + destroyableBlock.damage());
        }
      }

      data.setDestroyableBlocks(destroyableBlocks.isEmpty() ? null : destroyableBlocks);
    }

    String unranked =
        ItemSerializationUtil.itemStackArrayToBase64(ladder.getCustomKitExtraItems().get(false));
    String ranked =
        ItemSerializationUtil.itemStackArrayToBase64(ladder.getCustomKitExtraItems().get(true));
    if (unranked == null && ranked == null) {
      data.setCustomKitExtraItems(null);
    } else {
      LadderData.CustomKitExtraItems extraItems = new LadderData.CustomKitExtraItems();
      extraItems.setUnranked(unranked);
      extraItems.setRanked(ranked);
      data.setCustomKitExtraItems(extraItems);
    }

    if (ladder instanceof CustomConfig customConfig) {
      customConfig.writeCustomConfig(data);
    }

    DataFiles.write(file, data);
  }

  /** Applies the file's settings to the ladder, using defaults for absent or invalid values. */
  public void getData() {
    LadderData.Settings settings = data.getSettings();

    if (data.getEnabled() != null) {
      ladder.setEnabled(data.getEnabled());
    }

    if (settings.getRegen() != null) {
      ladder.setRegen(settings.getRegen());
    }

    if (settings.getHunger() != null) {
      ladder.setHunger(settings.getHunger());
    }

    if (settings.getLegacyRanked() != null) {
      ladder.setWeightClass(
          settings.getLegacyRanked()
              ? WeightClassType.UNRANKED_AND_RANKED
              : WeightClassType.UNRANKED);
    }

    if (settings.getWeightClass() != null) {
      ladder.setWeightClass(WeightClassType.valueOf(settings.getWeightClass()));
    }

    if (settings.getEditable() != null) {
      ladder.setEditable(settings.getEditable());
    }

    if (settings.getDropInventory() != null) {
      ladder.setDropInventory(settings.getDropInventory());
    }

    if (settings.getMultiRoundStartCountdown() != null) {
      ladder.setMultiRoundStartCountdown(settings.getMultiRoundStartCountdown());
    }

    if (settings.getStartMove() != null) {
      ladder.setStartMove(settings.getStartMove());
    }

    if (settings.getHealthBelowName() != null) {
      ladder.setHealthBelowName(settings.getHealthBelowName());
    }

    if (settings.getResetBuildAfterRound() != null) {
      ladder.setResetBuildAfterRound(settings.getResetBuildAfterRound());
    }

    if (settings.getBreakAllBlocks() != null) {
      ladder.setBreakAllBlocks(settings.getBreakAllBlocks());
    }

    if (settings.getRoundEndDelay() instanceof Integer roundEndDelay) {
      ladder.setRoundEndDelay(roundEndDelay < 0 || roundEndDelay > 30 ? 3 : roundEndDelay);
    } else {
      ladder.setRoundEndDelay(3);
    }

    ladder.setRoundStatusTitles(!Boolean.FALSE.equals(settings.getRoundStatusTitles()));
    ladder.setCountdownTitles(!Boolean.FALSE.equals(settings.getCountdownTitles()));

    if (settings.getHitDelay() instanceof Integer hitDelay) {
      // Convert old int-based hitdelay (ticks) to multiplier: divide by 20 (default ticks)
      ladder.setAttackCooldownModifier(Math.clamp(hitDelay / 20.0, 0, 3.0));
    } else if (settings.getHitDelay() instanceof Double hitDelay) {
      ladder.setAttackCooldownModifier(hitDelay < 0 || hitDelay > 3.0 ? 1.0 : hitDelay);
    } else {
      ladder.setAttackCooldownModifier(1.0);
    }

    if (settings.getRounds() instanceof Integer rounds) {
      ladder.setRounds(rounds < 1 || rounds > 5 ? 1 : rounds);
    } else {
      ladder.setRounds(1);
    }

    ladder.setHearts(settings.getHearts() instanceof Integer hearts ? hearts : 10);
    ladder.setMaxDuration(settings.getMaxDuration() instanceof Integer duration ? duration : 600);

    if (settings.getEnderPearlCooldown() != null) {
      ladder.setEnderPearlCooldown(
          Math.clamp(settings.getEnderPearlCooldown().doubleValue(), 0.0, 60.0));
    }

    if (settings.getGoldenAppleCooldown() != null) {
      ladder.setGoldenAppleCooldown(
          Math.clamp(settings.getGoldenAppleCooldown().doubleValue(), 0.0, 30.0));
    }

    if (settings.getFireworkCooldown() instanceof Integer fireworkCooldown) {
      ladder.setFireworkRocketCooldown(
          fireworkCooldown < 0 || fireworkCooldown > 30 ? 1 : fireworkCooldown);
    }

    if (settings.getWindChargeCooldown() != null) {
      ladder.setWindChargeCooldown(
          Math.clamp(settings.getWindChargeCooldown().doubleValue(), 0.0, 30.0));
    } else {
      ladder.setWindChargeCooldown(0);
    }

    if (settings.getStartCountdown() instanceof Integer startCountdown) {
      ladder.setStartCountdown(startCountdown < 2 || startCountdown > 5 ? 3 : startCountdown);
    } else {
      ladder.setStartCountdown(3);
    }

    if (settings.getTntFuseTime() instanceof Integer tntFuseTime) {
      ladder.setTntFuseTime(tntFuseTime < 1 || tntFuseTime > 10 ? 4 : tntFuseTime);
    } else {
      ladder.setTntFuseTime(4);
    }

    ladder.setTntAutoIgnite(Boolean.TRUE.equals(settings.getTntAutoIgnite()));
    ladder.setPullPlayersWithRod(!Boolean.FALSE.equals(settings.getPullPlayersWithRod()));

    if (ladder instanceof RespawnableLadder respawnableLadder) {
      if (settings.getRespawnTime() instanceof Integer respawnTime) {
        respawnableLadder.setRespawnTime(respawnTime < 0 || respawnTime > 10 ? 3 : respawnTime);
      } else {
        respawnableLadder.setRespawnTime(3);
      }
    }

    if (ladder instanceof BlockReturnDelay blockReturnDelay) {
      // Earlier names of the setting, newest first.
      Integer buildDelay =
          firstInteger(
              settings.getBlockReturnDelay(),
              settings.getLegacyTempBuildDelay(),
              data.getLegacyTempBuildDelay(),
              data.getLegacyBlockReturnDelaySeconds());
      blockReturnDelay.setBlockReturnDelaySeconds(buildDelay(buildDelay));
    }

    if (ladder instanceof TempBuildReturnDelay tempBuildReturnDelay) {
      Integer buildDelay =
          firstInteger(
              settings.getTempBuildReturnDelay(),
              settings.getLegacyTempBuildDelay(),
              data.getLegacyTempBuildDelay(),
              settings.getBlockReturnDelay(),
              data.getLegacyBlockReturnDelaySeconds());
      tempBuildReturnDelay.setTempBuildReturnDelaySeconds(buildDelay(buildDelay));
    }

    if (ladder instanceof Creeper creeper) {
      Number creeperExplosionDelay = settings.getCreeperExplosionDelay();

      if (creeperExplosionDelay == null) {
        creeper.setCreeperExplosionDelay(1.0);
      } else {
        creeper.setCreeperExplosionDelay(
            Math.clamp(creeperExplosionDelay.doubleValue(), 0.5, 10.0));
      }
    }

    if (settings.getKnockback() != null) {
      ladder.getLadderKnockback().get(settings.getKnockback());
    }

    if (settings.getMatchTypes() != null) {
      LadderFileUtil.getLadderMatchTypes(
          ladder.getMatchTypes(),
          settings.getMatchTypes().stream().filter(Objects::nonNull).toList());
    }

    readKit(ladder.getKitData());

    if (ladder.isBuild() && data.getDestroyableBlocks() != null) {
      for (String destroyableBlock : data.getDestroyableBlocks()) {
        if (destroyableBlock == null) {
          continue;
        }

        try {
          if (destroyableBlock.contains("::")) {
            String[] s2 = destroyableBlock.split("::");
            ladder
                .getDestroyableBlocks()
                .add(new BasicItem(Material.valueOf(s2[0]), Integer.parseInt(s2[1])));
          } else {
            ladder.getDestroyableBlocks().add(new BasicItem(Material.valueOf(destroyableBlock), 0));
          }
        } catch (IllegalArgumentException e) {
          Common.sendConsoleMmMessage(
              "<red>Incorrectly formatted destroyable block: " + destroyableBlock);
        }
      }
    }

    if (data.getIcon() != null) {
      ladder.setIcon(data.getIcon());
    }

    LadderData.CustomKitExtraItems extraItems = data.getCustomKitExtraItems();
    if (extraItems != null && extraItems.getUnranked() != null) {
      ladder
          .getCustomKitExtraItems()
          .put(false, ItemSerializationUtil.itemStackArrayFromBase64(extraItems.getUnranked()));
    }
    if (extraItems != null && extraItems.getRanked() != null) {
      ladder
          .getCustomKitExtraItems()
          .put(true, ItemSerializationUtil.itemStackArrayFromBase64(extraItems.getRanked()));
    }

    if (ladder instanceof CustomConfig customConfig) {
      customConfig.readCustomConfig(data);
    }

    if (ladder.isEnabled() && !ladder.isReadyToEnable()) {
      ladder.setEnabled(false);
    }
  }

  /** Deletes the ladder's file. */
  public void delete() {
    DataFiles.delete(file);
  }

  /** Writes the ladder's file again under another name, disabled. */
  public void copyTo(String name) {
    data.setEnabled(false);
    data.setName(name);
    DataFiles.write("ladders/" + name.toLowerCase() + ".yml", data);
  }

  private void writeKit(KitData kitData) {
    // Like before, a part of the kit that is not set leaves the file's value as it is.
    if (kitData.getStorage() != null) {
      data.setInventory(ItemSerializationUtil.itemStackArrayToBase64(kitData.getStorage()));
    }
    if (kitData.getArmor() != null) {
      data.setArmor(ItemSerializationUtil.itemStackArrayToBase64(kitData.getArmor()));
    }
    if (kitData.getExtra() != null) {
      data.setExtra(ItemSerializationUtil.itemStackArrayToBase64(kitData.getExtra()));
    }
    if (!kitData.getEffects().isEmpty()) {
      data.setEffects(new ArrayList<>(kitData.getEffects()));
    }
  }

  private void readKit(KitData kitData) {
    ItemStack[] storage = decode(data.getInventory());
    if (storage != null) {
      kitData.setStorage(storage);
    }
    ItemStack[] armor = decode(data.getArmor());
    if (armor != null) {
      kitData.setArmor(armor);
    }
    ItemStack[] extra = decode(data.getExtra());
    if (extra != null) {
      kitData.setExtra(extra);
    }
    if (data.getEffects() != null) {
      List<PotionEffect> effects = new ArrayList<>();
      for (PotionEffect effect : data.getEffects()) {
        if (effect != null) {
          effects.add(effect);
        }
      }
      kitData.setEffects(effects);
    }
  }

  private static ItemStack[] decode(String base64) {
    return base64 == null ? null : ItemSerializationUtil.itemStackArrayFromBase64(base64);
  }

  private static Integer firstInteger(Number... values) {
    for (Number value : values) {
      if (value instanceof Integer integer) {
        return integer;
      }
    }
    return null;
  }

  private static int buildDelay(Integer buildDelay) {
    if (buildDelay == null || buildDelay < -1 || buildDelay > 30) {
      return 6;
    }
    return buildDelay;
  }
}
