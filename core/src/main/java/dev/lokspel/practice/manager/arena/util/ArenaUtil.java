package dev.lokspel.practice.manager.arena.util;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaLayoutData;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.arenas.ArenaCopy;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.arena.setup.ArenaSetupManager;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.NumberUtil;
import dev.lokspel.practice.util.items.BasicItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Mannequin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ArenaUtil {

  private ArenaUtil() {}

  public static World createEmptyWorld(String worldName) {
    WorldCreator wc = new WorldCreator(worldName);
    wc.type(WorldType.FLAT);
    wc.generateStructures(false);
    wc.generatorSettings(
        "{\"layers\": [{\"block\": \"air\", \"height\": 1}, {\"block\": \"air\", \"height\": 1}],"
            + " \"biome\":\"plains\"}");
    return wc.createWorld();
  }

  public static Arena getArena(BasicArena arena) {
    if (arena instanceof ArenaCopy) {
      return ((ArenaCopy) arena).getMainArena();
    } else if (arena instanceof Arena) {
      return (Arena) arena;
    }
    return null;
  }

  public static String convertLocation(final Location location) {
    if (location != null) {
      return PracticeConfig.language()
          .getArena()
          .getLocationConversion()
          .getConversion()
          .replace("%x%", String.valueOf(NumberUtil.doubleToInt(Math.ceil(location.getX()))))
          .replace("%y%", String.valueOf(NumberUtil.doubleToInt(Math.ceil(location.getY()))))
          .replace("%z%", String.valueOf(NumberUtil.doubleToInt(Math.ceil(location.getZ()))));
    }
    return PracticeConfig.language().getArena().getLocationConversion().getLocationNull();
  }

  public static String convertLocation(final PortalLocation portalLocation) {
    if (portalLocation != null && portalLocation.getCenter() != null) {
      return PracticeConfig.language()
          .getArena()
          .getLocationConversion()
          .getConversion()
          .replace(
              "%x%",
              String.valueOf(NumberUtil.doubleToInt(Math.ceil(portalLocation.getCenter().getX()))))
          .replace(
              "%y%",
              String.valueOf(NumberUtil.doubleToInt(Math.ceil(portalLocation.getCenter().getY()))))
          .replace(
              "%z%",
              String.valueOf(NumberUtil.doubleToInt(Math.ceil(portalLocation.getCenter().getZ()))));
    }
    return PracticeConfig.language().getArena().getLocationConversion().getLocationNull();
  }

  public static List<String> getLadderTypeNames(Arena arena) {
    return arena.getAssignedLadderTypes().stream().map(Enum::name).toList();
  }

  public static List<String> getLadderNames(DisplayArena arena) {
    return arena.getAssignedLadders().stream().map(Ladder::getName).toList();
  }

  public static boolean changeStatus(Player player, DisplayArena arena) {
    boolean returnVal = true;

    if (!arena.isEnabled()) {
      if (arena.getIcon() == null || arena.getIcon().getType().equals(Material.AIR)) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getArena().getStatusChange().getNoIcon());
        returnVal = false;
      }

      if (arena.getCuboid() == null) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getArena().getStatusChange().getNoCuboid());
        returnVal = false;
      }

      //            if (arena.getAssignedLadders().isEmpty()) {
      //                Common.sendMMMessage(player,
      // PracticeConfig.language().getArena().getStatusChange().getNoLadderAssigned());
      //                returnVal = false;
      //            }

      if (arena instanceof Arena) {
        if (isArenaBedRelated((Arena) arena) && !arena.isBedSet()) {
          Common.sendMmMessage(
              player, PracticeConfig.language().getArena().getStatusChange().getNoBed());
          returnVal = false;
        }

        if (isArenaPortalRelated((Arena) arena) && !arena.isPortalSet()) {
          Common.sendMmMessage(
              player, PracticeConfig.language().getArena().getStatusChange().getNoPortal());
          returnVal = false;
        }

        if (arena.getPosition1() == null || arena.getPosition2() == null) {
          Common.sendMmMessage(
              player, PracticeConfig.language().getArena().getStatusChange().getNoSpawnPositions());
          returnVal = false;
        }
      } else if (arena instanceof FfaArena) {
        if (arena.getFfaPositions().isEmpty()) {
          Common.sendMmMessage(
              player, PracticeConfig.language().getArena().getStatusChange().getNoFfaPositions());
          returnVal = false;
        }
        if (arena.getAssignedLadders().isEmpty()) {
          Common.sendMmMessage(player, "<red>Please assign at least one ladder to the arena!");
          return false;
        }
      }
    } else {
      if (!MatchManager.getInstance().getLiveMatchesByArena(arena).isEmpty()) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getArena().getStatusChange().getCantDisable1());
        returnVal = false;
      } else if (arena.isBuild()) {
        if (arena instanceof Arena && ((Arena) arena).isCopying()) {
          Common.sendMmMessage(
              player, PracticeConfig.language().getArena().getStatusChange().getCantDisable2());
          returnVal = false;
        }
      }
    }

    if (!returnVal) {
      return false;
    }

    if (!arena.isEnabled()) {
      ArenaSetupManager setupManager = ArenaSetupManager.getInstance();

      List<Player> editors = new ArrayList<>(setupManager.getPlayersSettingUpArena(arena));
      for (Player editor : editors) {
        setupManager.stopSetup(editor);
        Common.sendMmMessage(
            editor, "<red>Setup mode force ended because the arena has been <green>ENABLED<red>!");
      }
    }

    arena.setEnabled(!arena.isEnabled());
    arena.setData();

    if (arena instanceof FfaArena) {
      Ffa ffa = ((FfaArena) arena).getFfa();

      if (ffa != null) {
        if (arena.isEnabled()) {
          ffa.open();
        } else {
          ffa.close("");
        }
      }
    }

    return true;
  }

  public static void setGamerules(World world) {
    world.setSpawnLocation(0, 60, 0);
    world.setGameRule(GameRules.SPAWN_MOBS, false);
    world.setGameRule(GameRules.SHOW_DEATH_MESSAGES, false);
    world.setGameRule(GameRules.ADVANCE_WEATHER, false);
    world.setGameRule(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, false);
    world.setGameRule(GameRules.RAIDS, false);
    world.setGameRule(GameRules.SHOW_ADVANCEMENT_MESSAGES, false);
  }

  public static ArenaLayoutData.Bed toBedData(final BedLocation bedLocation) {
    ArenaLayoutData.Bed bed = new ArenaLayoutData.Bed();
    bed.setWorld(bedLocation.getWorld().getName());
    bed.setBlockX(bedLocation.getX());
    bed.setBlockY(bedLocation.getY());
    bed.setBlockZ(bedLocation.getZ());
    bed.setFace(bedLocation.getFacing().name());
    return bed;
  }

  public static BedLocation getBedData(final ArenaLayoutData.Bed bed) {
    return new BedLocation(
        Bukkit.getWorld(bed.getWorld()),
        toInt(bed.getBlockX()),
        toInt(bed.getBlockY()),
        toInt(bed.getBlockZ()),
        BlockFace.valueOf(bed.getFace()));
  }

  private static int toInt(final Double value) {
    return value == null ? 0 : value.intValue();
  }

  public static boolean isArenaBedRelated(Arena arena) {
    Set<LadderType> assignedLadders = arena.getAssignedLadderTypes();
    return assignedLadders.contains(LadderType.BEDWARS)
        || assignedLadders.contains(LadderType.FIREBALL_FIGHT)
        || assignedLadders.contains(LadderType.MLG_RUSH);
  }

  public static boolean isArenaPortalRelated(Arena arena) {
    Set<LadderType> assignedLadders = arena.getAssignedLadderTypes();
    return assignedLadders.contains(LadderType.BRIDGES)
        || assignedLadders.contains(LadderType.BATTLE_RUSH);
  }

  public static boolean turnsToDirt(Block block) {
    if (block == null) {
      return false;
    }
    Material type = block.getType();
    return type == Material.GRASS_BLOCK
        || type == Material.MYCELIUM
        || type == Material.DIRT_PATH
        || type == Material.FARMLAND
        || type == Material.WARPED_NYLIUM;
  }

  public static boolean containsDestroyableBlock(Ladder ladder, Block block) {
    if (!(ladder instanceof NormalLadder normalLadder)) {
      return false;
    }

    if (!ladder.isBuild()) {
      return false;
    }
    if (normalLadder.getDestroyableBlocks().isEmpty()) {
      return false;
    }
    if (block == null) {
      return false;
    }

    for (BasicItem basicItem : normalLadder.getDestroyableBlocks()) {
      if (block.getType() == basicItem.material()) {
        return true;
      }
    }
    return false;
  }

  public static boolean requiresSupport(Block block) {
    if (block == null) {
      return false;
    }
    Material type = block.getType();
    if (Tag.FLOWERS.isTagged(type)
        || Tag.SAPLINGS.isTagged(type)
        || Tag.CROPS.isTagged(type)
        || Tag.WALL_POST_OVERRIDE.isTagged(type)) {
      return true;
    }

    return switch (type) {
      case DEAD_BUSH,
          SHORT_GRASS,
          TALL_GRASS,
          FERN,
          LARGE_FERN,
          SUGAR_CANE,
          CACTUS,
          SNOW,
          TORCH,
          SOUL_TORCH,
          REDSTONE_WIRE,
          REDSTONE_TORCH,
          LEVER,
          COMPARATOR,
          REPEATER,
          TRIPWIRE_HOOK,
          TRIPWIRE,
          LILY_PAD,
          NETHER_WART,
          VINE,
          WEEPING_VINES,
          TWISTING_VINES,
          CAVE_VINES,
          CAVE_VINES_PLANT ->
          true;
      default -> type.name().contains("VINE");
    };
  }

  public static void loadArenaChunks(BasicArena arena) {
    if (arena.getCuboid() == null) {
      return;
    }
    org.bukkit.World world = arena.getCuboid().getWorld();
    if (world == null) {
      return;
    }

    // Calculate chunk coordinate range directly from the cuboid bounds
    // instead of calling getChunks() which synchronously loads all chunks.
    int minCx = arena.getCuboid().getLowerX() >> 4;
    int maxCx = arena.getCuboid().getUpperX() >> 4;
    int minCz = arena.getCuboid().getLowerZ() >> 4;
    int maxCz = arena.getCuboid().getUpperZ() >> 4;

    org.bukkit.plugin.Plugin plugin = AstralPractice.getInstance();
    for (int cx = minCx; cx <= maxCx; cx++) {
      for (int cz = minCz; cz <= maxCz; cz++) {
        if (ArenaManager.LOAD_CHUNKS) {
          ArenaManager.LOADED_CHUNK_KEYS.add(chunkKey(cx, cz));
        }
        // addPluginChunkTicket loads the chunk asynchronously if needed
        // and prevents it from being unloaded — no main-thread stall.
        world.addPluginChunkTicket(cx, cz, plugin);
      }
    }
  }

  public static long chunkKey(int cx, int cz) {
    return ((long) cx << 32) ^ (cz & 0xffffffffL);
  }

  public static void setMannequinItemInHand(
      Mannequin mannequin, ItemStack item, boolean rightHand) {
    if (mannequin == null) {
      return;
    }
    if (mannequin.getEquipment() == null) {
      return;
    }

    if (rightHand) {
      mannequin.getEquipment().setItemInMainHand(item);
    } else {
      mannequin.getEquipment().setItemInOffHand(item);
    }
  }

  public static void setMannequinInvulnerable(Mannequin mannequin) {
    if (mannequin == null) {
      return;
    }
    mannequin.setInvulnerable(true);
    // Keep setup markers non-persistent so they do not survive server restarts.
    mannequin.setPersistent(false);
  }
}
