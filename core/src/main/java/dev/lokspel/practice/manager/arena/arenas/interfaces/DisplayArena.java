package dev.lokspel.practice.manager.arena.arenas.interfaces;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.ArenaData;
import dev.lokspel.practice.manager.arena.ArenaFile;
import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.ArenaType;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.arenas.ArenaCopy;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.arena.setup.SpawnMarkerManager;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.util.Common;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Getter
public abstract class DisplayArena extends NormalArena {

  private static final boolean DEFAULT_ICON =
      PracticeConfig.settings().getArena().getDefaultIcon().isEnabled();
  private static final GuiItem DEFAULT_ICON_ITEM =
      BackendUtil.getGuiItem(PracticeConfig.settings().getArena().getDefaultIcon().getIcon());

  protected final ArenaFile arenaFile;
  protected ItemStack icon;
  @Setter protected String displayName;

  protected boolean enabled;
  protected final ArenaType type;
  @Setter protected boolean build;
  @Setter protected Set<NormalLadder> assignedLadders = new HashSet<>();

  /**
   * Creates an arena and opens its file.
   *
   * @param name the arena's name
   * @param type the arena's type
   * @param data the file's content if it was already read, otherwise {@code null}
   */
  protected DisplayArena(String name, ArenaType type, @Nullable ArenaData data) {
    super(name);

    this.type = type;
    this.displayName = name;

    arenaFile = new ArenaFile(this, data);
  }

  public void setIcon(final ItemStack icon) {
    if (icon == null || icon.getType().equals(Material.AIR)) {
      return;
    }

    this.icon = icon.clone();

    if (icon.hasItemMeta()) {
      String iconDisplayName = Common.getItemDisplayNameMiniMessage(icon);
      this.displayName = iconDisplayName.isBlank() ? name : iconDisplayName;
    } else {
      this.displayName = name;
    }
  }

  public ItemStack getIcon() {
    if (this.icon == null) {
      if (DEFAULT_ICON) {
        GuiItem guiItem = DEFAULT_ICON_ITEM.cloneItem();
        guiItem.replace("%arena%", this.name);
        return guiItem.get().clone();
      }
      return null;
    }
    return this.icon.clone();
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;

    if (!enabled) {
      if (this instanceof FfaArena) {
        Ffa ffa = ((FfaArena) this).getFfa();
        if (ffa != null) {
          ffa.close(PracticeConfig.language().getFfa().getAllLaddersDisabled());
        }
      }
    }

    if (enabled) {
      // Clear spawn markers when arena is enabled
      SpawnMarkerManager.getInstance().clearMarkers(this);

      if (ArenaManager.LOAD_CHUNKS) {
        this.loadChunks();

        if (this instanceof Arena arena) {
          if (arena.getCopies() != null && !arena.getCopies().isEmpty()) {
            for (ArenaCopy copy : arena.getCopies()) {
              copy.loadChunks();
            }
          }
        }
      }
    }

    if (ArenaGuiSetupManager.getInstance().getArenaSetupGuis().containsKey(this)) {
      ArenaGuiSetupManager.getInstance()
          .getArenaSetupGuis()
          .get(this)
          .get(GuiType.Arena_Main)
          .update();
    }

    Gui arenaSummary = GuiManager.getInstance().searchGui(GuiType.Arena_Summary);
    if (arenaSummary != null) {
      arenaSummary.update();
    }
  }

  @Override
  public boolean isWorldLoaded() {
    if (!super.isWorldLoaded()) {
      return false;
    }
    if (this instanceof Arena arena) {
      for (ArenaCopy copy : arena.getCopies()) {
        if (!copy.isWorldLoaded()) {
          return false;
        }
      }
    }
    return true;
  }

  public abstract void setData();

  public abstract void getData();

  public abstract boolean isReadyToEnable();

  public abstract Set<NormalLadder> getAssignableLadders();

  public abstract boolean deleteData();
}
