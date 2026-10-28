package dev.lokspel.practice.manager.arena.arenas;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaData;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.ArenaType;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class FfaArena extends DisplayArena {

  private static final boolean DEFAULT_HEALTH_RESET_ON_KILL =
      PracticeConfig.settings().getFfa().isHealthResetOnKill();
  private static final boolean DEFAULT_HEALTH_BELOW_NAME =
      PracticeConfig.settings().getFfa().isHealthBelowName();
  private static final boolean DEFAULT_ALLOW_DROP_ITEMS =
      PracticeConfig.settings().getFfa().isAllowDropItems();

  private final Ffa ffa;
  private boolean reKitAfterKill;
  private boolean lobbyAfterDeath;
  private boolean healthResetOnKill;
  private boolean healthBelowName;
  private boolean mapBlowable;
  private boolean allowDropItems;

  public FfaArena(String name) {
    this(name, null);
  }

  /**
   * Creates an FFA arena from its file.
   *
   * @param name the arena's name
   * @param data the file's content if it was already read, otherwise {@code null}
   */
  public FfaArena(String name, @Nullable ArenaData data) {
    super(name, ArenaType.FFA, data);

    this.bedLoc1 = null;
    this.bedLoc2 = null;
    this.portalLoc1 = null;
    this.portalLoc2 = null;
    this.portalProtection = false;
    this.healthResetOnKill = DEFAULT_HEALTH_RESET_ON_KILL;
    this.healthBelowName = DEFAULT_HEALTH_BELOW_NAME;
    this.allowDropItems = DEFAULT_ALLOW_DROP_ITEMS;

    this.getData();

    this.ffa = new Ffa(this);
    this.ffa.open();
  }

  @Override
  public void setData() {
    ArenaData data = arenaFile.getData();

    data.setName(this.name);
    data.setType(this.type.toString());
    data.setEnabled(this.enabled);

    if (this.getIcon() != null) {
      data.setIcon(this.getIcon());
    }

    data.setBuild(this.build);
    data.setReKitAfterKill(this.reKitAfterKill);
    data.setLobbyAfterDeath(this.lobbyAfterDeath);
    data.setHealthResetOnKill(this.healthResetOnKill);
    data.setHealthBelowName(this.healthBelowName);
    data.setMapBlowable(this.mapBlowable);
    data.setAllowDropItems(this.allowDropItems);
    data.setLocatorBar(this.locatorBar);

    data.setLadders(ArenaUtil.getLadderNames(this));

    super.setBasicData(data);

    arenaFile.save();
  }

  @Override
  public void getData() {
    ArenaData data = arenaFile.getData();

    if (data.getIcon() != null) {
      this.setIcon(data.getIcon());
    }

    if (data.getBuild() != null) {
      this.setBuild(data.getBuild());
    }

    if (data.getReKitAfterKill() != null) {
      this.setReKitAfterKill(data.getReKitAfterKill());
    }

    if (data.getLobbyAfterDeath() != null) {
      this.setLobbyAfterDeath(data.getLobbyAfterDeath());
    }

    if (data.getHealthResetOnKill() != null) {
      this.setHealthResetOnKill(data.getHealthResetOnKill());
    }

    if (data.getHealthBelowName() != null) {
      this.setHealthBelowName(data.getHealthBelowName());
    }

    if (data.getMapBlowable() != null) {
      this.setMapBlowable(data.getMapBlowable());
    }

    if (data.getAllowDropItems() != null) {
      this.setAllowDropItems(data.getAllowDropItems());
    }

    if (data.getLocatorBar() != null) {
      this.setLocatorBar(data.getLocatorBar());
    }

    if (data.getLadders() != null) {
      for (String ladderName : data.getLadders()) {
        NormalLadder ladder = LadderManager.getInstance().getLadder(ladderName);

        if (ladder != null && ladder.isEnabled()) {
          assignedLadders.add(ladder);
        }
      }
    }

    super.getBasicData(data);

    if (data.getEnabled() != null) {
      this.setEnabled(data.getEnabled());
    }

    if (this.isEnabled() && !this.isReadyToEnable()) {
      this.setEnabled(false);
    }
  }

  @Override
  public boolean isReadyToEnable() {
    return this.getIcon() != null
        && cuboid != null
        && !ffaPositions.isEmpty()
        && !assignedLadders.isEmpty();
  }

  @Override
  public Set<NormalLadder> getAssignableLadders() {
    Set<NormalLadder> list = new HashSet<>();

    for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
      if (this.build
          && ladder.getType().equals(LadderType.BUILD)
          && ladder.getLadderKnockback().isDefault()) {
        list.add(ladder);
      } else if (!this.build
          && ladder.getType().equals(LadderType.BASIC)
          && ladder.getLadderKnockback().isDefault()) {
        list.add(ladder);
      }
    }

    return list;
  }

  @Override
  public boolean deleteData() {
    ArenaManager.getInstance().getArenaCuboids().remove(cuboid);
    return arenaFile.delete();
  }

  public void setReKitAfterKill(boolean reKitAfterKill) throws IllegalStateException {
    if (this.enabled) {
      throw new IllegalStateException("Cannot edit while arena is enabled.");
    }

    this.reKitAfterKill = reKitAfterKill;
  }

  public void setLobbyAfterDeath(boolean lobbyAfterDeath) throws IllegalStateException {
    if (this.enabled) {
      throw new IllegalStateException("Cannot edit while arena is enabled.");
    }

    this.lobbyAfterDeath = lobbyAfterDeath;
  }

  public void setHealthResetOnKill(boolean healthResetOnKill) throws IllegalStateException {
    if (this.enabled) {
      throw new IllegalStateException("Cannot edit while arena is enabled.");
    }

    this.healthResetOnKill = healthResetOnKill;
  }

  public void setHealthBelowName(boolean healthBelowName) throws IllegalStateException {
    if (this.enabled) {
      throw new IllegalStateException("Cannot edit while arena is enabled.");
    }

    this.healthBelowName = healthBelowName;
  }

  public void setMapBlowable(boolean mapBlowable) {
    if (this.enabled) {
      throw new IllegalStateException("Cannot edit while arena is enabled.");
    }

    this.mapBlowable = mapBlowable;
  }

  public void setAllowDropItems(boolean allowDropItems) {
    if (this.enabled) {
      throw new IllegalStateException("Cannot edit while arena is enabled.");
    }

    this.allowDropItems = allowDropItems;
  }

  public void setBuild(boolean build) {
    if (this.enabled) {
      throw new IllegalStateException("Cannot edit while arena is enabled.");
    }

    this.build = build;

    Set<NormalLadder> assignableLadders = this.getAssignableLadders();
    this.assignedLadders.removeIf(ladder -> !assignableLadders.contains(ladder));

    Gui ladderGui =
        ArenaGuiSetupManager.getInstance()
            .getArenaSetupGuis()
            .getOrDefault(this, new HashMap<>())
            .get(GuiType.Arena_Ladders_Single);
    if (ladderGui != null) {
      ladderGui.update();
    }
  }
}
