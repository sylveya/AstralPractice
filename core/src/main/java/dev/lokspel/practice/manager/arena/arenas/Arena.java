package dev.lokspel.practice.manager.arena.arenas;

import dev.lokspel.practice.manager.arena.ArenaData;
import dev.lokspel.practice.manager.arena.ArenaLayoutData;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.ArenaType;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.arena.arenas.interfaces.NormalArena;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
public class Arena extends DisplayArena {

  private final List<ArenaCopy> copies = new ArrayList<>();
  @Setter private boolean copying = false;

  @Setter private Set<LadderType> assignedLadderTypes = new HashSet<>();
  @Setter private boolean allowCustomKitOnMap = true;

  @Setter private boolean frozen; // If the arena is frozen players can't start a new game with it.
  @Setter private int sideBuildLimit = 0;

  public Arena(String name, ArenaType type) {
    this(name, type, null);
  }

  /**
   * Creates a normal arena from its file.
   *
   * @param name the arena's name
   * @param type the arena's type
   * @param data the file's content if it was already read, otherwise {@code null}
   */
  public Arena(String name, ArenaType type, @Nullable ArenaData data) {
    super(name, type, data);

    this.build = type.isBuild();
    this.available = true;
    this.createCuboid();

    if (build) {
      loadCopies();
    }

    this.getData();
  }

  @Override
  public void setData() {
    ArenaData data = arenaFile.getData();

    Map<String, ArenaLayoutData> copyData = new LinkedHashMap<>();
    for (ArenaCopy arenaCopy : copies) {
      ArenaLayoutData layout = new ArenaLayoutData();
      arenaCopy.setBasicData(layout);
      copyData.put(arenaCopy.getName(), layout);
    }
    data.setCopies(copyData.isEmpty() ? null : copyData);

    data.setName(this.name);
    data.setType(this.type.toString());
    data.setEnabled(this.enabled);
    data.setLadderTypes(ArenaUtil.getLadderTypeNames(this));
    data.setLadders(ArenaUtil.getLadderNames(this));
    data.setAllowCustomKitOnMap(this.allowCustomKitOnMap);
    data.setLocatorBar(this.locatorBar);

    if (this.getIcon() != null) {
      data.setIcon(this.getIcon());
    }

    data.setSideBuildLimit(this.getSideBuildLimit());

    super.setBasicData(data);

    arenaFile.save();
  }

  @Override
  public void getData() {
    ArenaData data = arenaFile.getData();

    if (data.getIcon() != null) {
      this.setIcon(data.getIcon());
    }

    if (data.getSideBuildLimit() != null) {
      this.setSideBuildLimit(data.getSideBuildLimit());

      if (this.getSideBuildLimit() < 0 || this.getSideBuildLimit() > 10) {
        this.setSideBuildLimit(0);
      }
    }

    if (data.getEnabled() != null) {
      this.setEnabled(data.getEnabled());
    }

    if (data.getLadderTypes() != null) {
      for (String ladderTypeName : data.getLadderTypes()) {
        assignedLadderTypes.add(LadderType.valueOf(ladderTypeName));
      }
    }

    if (data.getLadders() != null) {
      for (String ladderName : data.getLadders()) {
        NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);

        if (ladder != null
            && ladder.isEnabled()
            && this.getAssignedLadderTypes().contains(ladder.getType())) {
          assignedLadders.add(ladder);
        }
      }
    }

    if (data.getAllowCustomKitOnMap() != null) {
      this.setAllowCustomKitOnMap(data.getAllowCustomKitOnMap());
    }

    if (data.getLocatorBar() != null) {
      this.setLocatorBar(data.getLocatorBar());
    }

    super.getBasicData(data);

    if (this.isEnabled() && !this.isReadyToEnable()) {
      this.setEnabled(false);
    }
  }

  private void loadCopies() {
    Map<String, ArenaLayoutData> copyData = arenaFile.getData().getCopies();
    if (copyData == null) {
      return;
    }

    for (Map.Entry<String, ArenaLayoutData> entry : copyData.entrySet()) {
      ArenaCopy arenaCopy = new ArenaCopy(entry.getKey(), this);
      if (entry.getValue() != null) {
        arenaCopy.getBasicData(entry.getValue());
      }
      copies.add(arenaCopy);
    }
  }

  public void deleteLastCopy(boolean updateGuis) {
    if (!build) {
      return;
    }
    if (copies.isEmpty()) {
      return;
    }

    ArenaCopy arenaCopy = copies.getLast();
    arenaCopy.delete();
    copies.remove(arenaCopy);
    ArenaManager.getInstance().getArenaCuboids().remove(cuboid);

    if (updateGuis) {
      ArenaGuiSetupManager.getInstance()
          .getArenaSetupGuis()
          .get(this)
          .get(GuiType.Arena_Copy)
          .update();
      ArenaGuiSetupManager.getInstance()
          .getArenaSetupGuis()
          .get(this)
          .get(GuiType.Arena_Main)
          .update();
      GuiManager.getInstance().searchGui(GuiType.Arena_Summary).update();
    }
  }

  public void deleteCopies() {
    if (!build) {
      return;
    }

    for (int i = 0; i < copies.size(); i++) {
      deleteLastCopy(i == copies.size() - 1);
    }

    arenaFile.save();
  }

  @Override
  public boolean deleteData() {
    deleteCopies();

    ArenaManager.getInstance().getArenaCuboids().remove(cuboid);
    return arenaFile.delete();
  }

  public NormalArena getAvailableArena() {
    if (!enabled || frozen) {
      return null;
    }
    if (available) {
      return this;
    }

    if (build) {
      for (ArenaCopy arenaCopy : copies) {
        if (arenaCopy.isAvailable()) {
          return arenaCopy;
        }
      }
    }

    return null;
  }

  public boolean hasCopies() {
    return build && !copies.isEmpty();
  }

  @Override
  public boolean isReadyToEnable() {
    if (this.getIcon() != null
        && cuboid != null
        && position1 != null
        && position2 != null
        && !assignedLadderTypes.isEmpty()) {
      if (assignedLadderTypes.contains(LadderType.BEDWARS)
          || assignedLadderTypes.contains(LadderType.FIREBALL_FIGHT)
          || assignedLadderTypes.contains(LadderType.MLG_RUSH)) {
        return bedLoc1 != null && bedLoc2 != null;
      }
      if (assignedLadderTypes.contains(LadderType.BRIDGES)
          || assignedLadderTypes.contains(LadderType.BATTLE_RUSH)) {
        return portalLoc1 != null && portalLoc2 != null;
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public Set<NormalLadder> getAssignableLadders() {
    Set<NormalLadder> list = new HashSet<>();

    for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
      if (this.build == ladder.isBuild()) {
        list.add(ladder);
      }
    }

    return list;
  }
}
