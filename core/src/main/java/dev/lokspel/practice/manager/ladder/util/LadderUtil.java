package dev.lokspel.practice.manager.ladder.util;

import static dev.lokspel.practice.util.startup.PermanentConfig.FIGHT_ENTITY;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.util.BlockUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.manager.gui.setup.hologram.HologramSetupManager;
import dev.lokspel.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.type.FireballFight;
import dev.lokspel.practice.manager.ladder.type.SkyWars;
import dev.lokspel.practice.manager.leaderboard.LeaderboardManager;
import dev.lokspel.practice.manager.leaderboard.types.LbMainType;
import dev.lokspel.practice.manager.leaderboard.types.LbSecondaryType;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

public enum LadderUtil {
  ;

  public static void changeStatus(Player player, NormalLadder ladder) {
    if (!ladder.isEnabled()) {
      if (ladder.getIcon() == null) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getLadder().getStatusChange().getNoIcon());
        return;
      }
      if (!ladder.getKitData().isSet()) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getLadder().getStatusChange().getNoContent());
        return;
      }
      if (ladder instanceof SkyWars && ((SkyWars) ladder).getSkyWarsLoot() == null) {
        Common.sendMmMessage(
            player,
            PracticeConfig.language().getLadder().getStatusChange().getSkywars().getNoLoot());
        return;
      }

      enableLadder(ladder);
    } else {
      if (!MatchManager.getInstance().getLiveMatchesByLadder(ladder).isEmpty()) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getLadder().getStatusChange().getCantDisable());
        return;
      }

      disableLadder(ladder);
    }
  }

  public static void disableLadder(NormalLadder ladder) {
    ladder.setEnabled(false);
    ladder.setFrozen(false);

    // Remove the ladder from the arenas and holograms.
    ArenaManager.getInstance().removeLadder(ladder);
    LeaderboardManager.getInstance().removeLadder(ladder);

    /*
     * Update GUIs
     */
    LadderSetupManager.getInstance()
        .getLadderSetupGuis()
        .get(ladder)
        .get(GuiType.Ladder_Main)
        .update();
    GuiManager.getInstance().searchGui(GuiType.Ladder_Summary).update();
    GuiManager.getInstance().searchGui(GuiType.Arena_Summary).update();

    GuiManager.getInstance().searchGui(GuiType.Queue_Unranked).update(true);
    GuiManager.getInstance().searchGui(GuiType.Queue_Ranked).update(true);
    GuiManager.getInstance().searchGui(GuiType.CustomLadder_Selector).update();

    if (!PracticeConfig.settings().getSetup().isPreserveCustomKitsOnLadderDisable()) {
      Bukkit.getScheduler()
          .runTaskAsynchronously(
              AstralPractice.getInstance(),
              () -> {
                for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                  profile.getFile().deleteCustomKit(ladder);
                  profile.getUnrankedCustomKits().remove(ladder);
                  profile.getRankedCustomKits().remove(ladder);
                }
              });
    }

    /*
     * Delete the ladder statistics from the mariadb table.
     */
    ProfileManager.getInstance().deleteLadderStatsFromDatabase(ladder.getName());
  }

  public static void enableLadder(NormalLadder ladder) {
    ladder.setEnabled(true);

    if (!ladder.getPreviouslyAssignedArenas().isEmpty()) {
      for (String arenaName : new HashSet<>(ladder.getPreviouslyAssignedArenas())) {
        // Try normal arena first
        Arena arena = ArenaManager.getInstance().getNormalArena(arenaName);
        if (arena != null) {
          if (ladder.getPreviouslyAssignedArenas().contains(arena.getName())
              && arena.getAssignedLadderTypes().contains(ladder.getType())) {
            arena.getAssignedLadders().add(ladder);
          }
        } else {
          // Try FFA arena if normal arena not found
          FfaArena ffaArena = ArenaManager.getInstance().getFfaArena(arenaName);
          if (ffaArena != null) {
            if (ladder.getPreviouslyAssignedArenas().contains(ffaArena.getName())) {
              ffaArena.getAssignedLadders().add(ladder);
            }
          }
        }
      }
      ladder.getPreviouslyAssignedArenas().clear();
    }

    // Update GUIs
    ladder.getPreviewGui().update();
    GuiManager.getInstance().searchGui(GuiType.Queue_Ranked).update(true);
    GuiManager.getInstance().searchGui(GuiType.Queue_Unranked).update(true);
    GuiManager.getInstance().searchGui(GuiType.CustomLadder_Selector).update();

    Bukkit.getScheduler()
        .runTaskAsynchronously(
            AstralPractice.getInstance(),
            () -> {
              /*
               * for (Arena arena : ArenaManager.getInstance().getNormalArenas()) {
               * if (arena.getAssignedLadderTypes().contains(ladder.getType()))
               * arena.getAssignedLadders().add(ladder);
               * }
               */

              // Update ladder setup GUIs
              for (Map<GuiType, Gui> map :
                  HologramSetupManager.getInstance().getHologramSetupGuis().values()) {
                map.get(GuiType.Hologram_Ladder).update();
              }
              for (Map<GuiType, Gui> map :
                  ArenaGuiSetupManager.getInstance().getArenaSetupGuis().values()) {
                if (map.containsKey(GuiType.Arena_Ladders_Type)) {
                  map.get(GuiType.Arena_Ladders_Type).update();
                }

                map.get(GuiType.Arena_Ladders_Single).update();
              }

              LadderSetupManager.getInstance()
                  .getLadderSetupGuis()
                  .get(ladder)
                  .get(GuiType.Ladder_Main)
                  .update();
              GuiManager.getInstance().searchGui(GuiType.Ladder_Summary).update();

              for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
                profile.getStats().createLadderStat(ladder);

                // Set the custom ladder kits
                if (ladder.isEditable()) {
                  profile.getUnrankedCustomKits().put(ladder, new HashMap<>());

                  if (ladder.isRanked()) {
                    profile.getRankedCustomKits().put(ladder, new HashMap<>());
                  }
                }
              }

              // Update ladder leaderboard
              for (LbMainType lbMainType : LbMainType.values()) {
                for (LbSecondaryType lbSecondaryType : LbSecondaryType.values()) {
                  if (lbMainType.equals(LbMainType.LADDER)) {
                    LeaderboardManager.getInstance().updateLb(lbMainType, lbSecondaryType, ladder);
                  }
                }
              }
            });
  }

  public static Arena getAvailableArena(Ladder ladder) {
    List<Arena> availableArenas = ladder.getAvailableArenas();

    if (!availableArenas.isEmpty()) {
      return availableArenas.get(new Random().nextInt(availableArenas.size()));
    } else {
      return null;
    }
  }

  public static void loadInventory(
      Player player, ItemStack[] armor, ItemStack[] inventory, ItemStack[] extra) {
    player.getInventory().setArmorContents(armor);
    player.getInventory().setStorageContents(inventory);
    player.getInventory().setExtraContents(extra);
  }

  private static final String[] MATERIAL_TYPES = {
    "_WOOL",
    "_STAINED_CLAY",
    "_STAINED_GLASS",
    "_STAINED_GLASS_PANE",
    "_CARPET",
    "_CONCRETE",
    "_CONCRETE_POWDER",
    "_TERRACOTTA",
    "_GLAZED_TERRACOTTA",
    "_CANDLE",
    "_BANNER"
  };

  public static ItemStack changeItemColor(@NotNull ItemStack item, Component teamColor) {
    String itemType = item.getType().toString();
    TextColor textColor = teamColor.color();
    Color color = Color.YELLOW;
    if (textColor != null) {
      color =
          Color.fromRGB(
              Objects.requireNonNull(teamColor.color()).red(),
              Objects.requireNonNull(teamColor.color()).green(),
              Objects.requireNonNull(teamColor.color()).blue());
    }

    if (item.getType().name().startsWith("LEATHER_")) {
      LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
      if (meta != null) {
        meta.setColor(color);
        item.setItemMeta(meta);
      }
      return item;
    }

    for (String type : MATERIAL_TYPES) {
      if (itemType.contains(type) && textColor != null) {
        try {
          Material material = Material.getMaterial(textColor.toString().toUpperCase() + type);

          if (material != null) {
            return item.withType(material);
          }
        } catch (Exception ignored) {
          break;
        }
      }
    }

    return item;
  }

  public static ItemStack getPotionItem(String string) {
    try {
      if (string.contains("::")) {
        String[] split = string.split("::");
        ItemStack itemStack = new ItemStack(Material.valueOf(split[0]));

        PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
        if (potionMeta != null) {
          potionMeta.setBasePotionType(PotionType.valueOf(split[1]));
        }

        itemStack.setItemMeta(potionMeta);
        return itemStack;
      }
    } catch (Exception e) {
      Common.sendConsoleMmMessage("<red>Invalid item: " + string);
    }
    return null;
  }

  public static boolean isUnbreakable(ItemStack item) {
    if (item.hasItemMeta() && item.getItemMeta() != null) {
      return item.getItemMeta().isUnbreakable();
    }
    return false;
  }

  public static ItemMeta setUnbreakable(ItemMeta itemMeta, boolean unbreakable) {
    if (itemMeta != null) {
      itemMeta.setUnbreakable(unbreakable);
    }
    return itemMeta;
  }

  public static ItemStack setDurability(ItemStack itemStack, int durability) {
    if (itemStack.getItemMeta() != null) {
      ItemMeta itemMeta = itemStack.getItemMeta();
      if (itemMeta instanceof Damageable damageable) {
        int newDamage = itemStack.getType().getMaxDurability() - durability;
        if (newDamage < 0 || newDamage > itemStack.getType().getMaxDurability()) {
          newDamage = itemStack.getType().getMaxDurability();
        }

        damageable.setDamage(newDamage);
        itemStack.setItemMeta(damageable);
        return itemStack;
      }
    }
    return itemStack;
  }

  public static void placeTnt(BlockPlaceEvent e, Match match) {
    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> {
              if (e.isCancelled()) {
                return;
              }

              Block block = e.getBlock();
              block.setBlockData(Material.AIR.createBlockData());
              block.getState().update();

              TNTPrimed tnt =
                  (TNTPrimed)
                      block
                          .getWorld()
                          .spawnEntity(block.getLocation().subtract(-0.5, 0, -0.5), EntityType.TNT);
              BlockUtil.setMetadata(tnt, FIGHT_ENTITY, match);
              tnt.setIsIncendiary(false);

              if (match.getLadder() instanceof FireballFight) {
                BlockUtil.setMetadata(tnt, FireballFight.FIREBALL_FIGHT_TNT, match);
                BlockUtil.setMetadata(tnt, FireballFight.FIREBALL_FIGHT_TNT_SHOOTER, e.getPlayer());
              }
            },
            2L);
  }
}
