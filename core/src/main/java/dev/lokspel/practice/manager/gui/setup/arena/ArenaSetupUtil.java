package dev.lokspel.practice.manager.gui.setup.arena;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.ItemCreateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum ArenaSetupUtil {
  ;

  @Getter private static final Map<ItemStack, DisplayArena> arenaMarkerList = new HashMap<>();

  // Name & Icon & Information item
  public static ItemStack getNameItem(Arena arena) {
    List<String> lore = new ArrayList<>();
    for (String line :
        PracticeConfig.guis()
            .getGuis()
            .getSetup()
            .getArena()
            .getArenaMain()
            .getIcons()
            .getArenaName()
            .getLore()) {
      lore.add(
          line.replace("%arenaName%", arena.getName())
              .replace("%arenaDisplayName%", arena.getDisplayName())
              .replace("%arenaType%", arena.getType().getName()));
    }

    if (arena.getIcon() != null) {
      return ItemCreateUtil.createItem(
          arena.getIcon(),
          PracticeConfig.guis()
              .getGuis()
              .getSetup()
              .getArena()
              .getArenaMain()
              .getIcons()
              .getArenaName()
              .getName()
              .replace("%arenaDisplayName%", arena.getDisplayName())
              .replace("%arenaName%", arena.getName()),
          lore);
    } else {
      return ItemCreateUtil.createItem(
          PracticeConfig.guis()
              .getGuis()
              .getSetup()
              .getArena()
              .getArenaMain()
              .getIcons()
              .getArenaName()
              .getName()
              .replace("%arenaDisplayName%", arena.getDisplayName())
              .replace("%arenaName%", arena.getName()),
          Material.valueOf(
              PracticeConfig.guis()
                  .getGuis()
                  .getSetup()
                  .getArena()
                  .getArenaMain()
                  .getIcons()
                  .getArenaName()
                  .getMaterial()),
          lore);
    }
  }

  // Status item
  public static ItemStack getStatusItem(Arena arena) {
    if (arena.isEnabled()) {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getSetup()
                  .getArena()
                  .getArenaMain()
                  .getIcons()
                  .getStatus()
                  .getEnabled())
          .get();
    } else {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getSetup()
                  .getArena()
                  .getArenaMain()
                  .getIcons()
                  .getStatus()
                  .getDisabled())
          .get();
    }
  }

  // Arena copies item
  public static ItemStack getArenaCopiesItem(Arena arena) {
    if (!arena.isBuild()) {
      return null;
    }

    int size = arena.getCopies().size();
    if (size < 1) {
      size = 1;
    }

    ItemStack item =
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getSetup()
                    .getArena()
                    .getArenaMain()
                    .getIcons()
                    .getCopies())
            .get();
    item.setAmount(size);

    return item;
  }

  public static ItemStack getCopyArenaItem(Arena arena, int number) {
    GuiItem guiItem =
        BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaCopy()
                .getIcons()
                .getCopyArena());

    guiItem
        .replace("%arenaName%", arena.getName())
        .replace("%arenaDisplayName%", arena.getDisplayName())
        .replace("%copyNumber%", String.valueOf(number));

    ItemStack itemStack = guiItem.get();
    itemStack.setAmount(number);

    return itemStack;
  }

  // Locations item
  public static ItemStack getLocationItem(Arena arena) {
    GuiItem guiItem;
    if (arena.isBuild()) {
      guiItem =
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getSetup()
                      .getArena()
                      .getArenaMain()
                      .getIcons()
                      .getLocation()
                      .getBuild())
              .replace("%arenaName%", arena.getName())
              .replace("%arenaDisplayName%", arena.getDisplayName())
              .replace(
                  "%corner1%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getCorner1())))
              .replace(
                  "%corner2%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getCorner2())))
              .replace(
                  "%position1%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getPosition1())))
              .replace(
                  "%position2%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getPosition2())))
              .replace(
                  "%spectatorPosition%",
                  Common.mmToNormal(ArenaUtil.convertLocation(arena.getSpectatorPosition())))
              .replace("%bed1%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getBedLoc1())))
              .replace("%bed2%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getBedLoc2())))
              .replace(
                  "%portal1%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getPortalLoc1())))
              .replace(
                  "%portal2%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getPortalLoc2())))
              .replace("%sideBuildLimit%", String.valueOf(arena.getSideBuildLimit()))
              .replace(
                  "%buildMaxY%",
                  arena.isBuildMax() ? String.valueOf(arena.getBuildMaxValue()) : "<red>Not Set")
              .replace(
                  "%deathZoneY%",
                  arena.isDeadZone() ? String.valueOf(arena.getDeadZoneValue()) : "<red>Not Set");
    } else {
      guiItem =
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getSetup()
                      .getArena()
                      .getArenaMain()
                      .getIcons()
                      .getLocation()
                      .getNotBuild())
              .replace("%arenaName%", arena.getName())
              .replace("%arenaDisplayName%", arena.getDisplayName())
              .replace(
                  "%corner1%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getCorner1())))
              .replace(
                  "%corner2%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getCorner2())))
              .replace(
                  "%position1%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getPosition1())))
              .replace(
                  "%position2%", Common.mmToNormal(ArenaUtil.convertLocation(arena.getPosition2())))
              .replace(
                  "%spectatorPosition%",
                  Common.mmToNormal(ArenaUtil.convertLocation(arena.getSpectatorPosition())))
              .replace(
                  "%deathZoneY%",
                  arena.isDeadZone() ? String.valueOf(arena.getDeadZoneValue()) : "<red>Not Set");
    }

    return guiItem.get();
  }

  public static ItemStack getFreezeItem(Arena arena) {
    if (arena.isFrozen()) {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getSetup()
                  .getArena()
                  .getArenaMain()
                  .getIcons()
                  .getFreeze()
                  .getFrozen())
          .get();
    } else {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getSetup()
                  .getArena()
                  .getArenaMain()
                  .getIcons()
                  .getFreeze()
                  .getNotFrozen())
          .get();
    }
  }

  public static ItemStack getCopyGuiNavMainItem(Arena arena, int copies) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaCopy()
                .getIcons()
                .getNavMain())
        .replace("%arenaDisplayName%", arena.getDisplayName())
        .replace("%arenaName%", arena.getName())
        .replace("%copies%", String.valueOf(copies))
        .get();
  }

  public static ItemStack getAssignedLadderItem(Ladder ladder) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaLaddersSingle()
                .getIcons()
                .getLadderIcons()
                .getAssigned())
        .replace("%ladderDisplayName%", ladder.getDisplayName())
        .replace("%ladderName%", ladder.getName())
        .get();
  }

  public static ItemStack getNotAssignedLadderItem(Ladder ladder) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaLaddersSingle()
                .getIcons()
                .getLadderIcons()
                .getNotAssigned())
        .replace("%ladderDisplayName%", ladder.getDisplayName())
        .replace("%ladderName%", ladder.getName())
        .get();
  }

  public static ItemStack getDisabledLadderItem(Ladder ladder) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaLaddersSingle()
                .getIcons()
                .getLadderIcons()
                .getDisabled())
        .replace("%ladderDisplayName%", ladder.getDisplayName())
        .replace("%ladderName%", ladder.getName())
        .get();
  }

  public static ItemStack getNonCompatibleLadderItem(Ladder ladder) {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getArena()
                .getArenaLaddersSingle()
                .getIcons()
                .getLadderIcons()
                .getNotCompatible())
        .replace("%ladderDisplayName%", ladder.getDisplayName())
        .replace("%ladderName%", ladder.getName())
        .get();
  }
}
