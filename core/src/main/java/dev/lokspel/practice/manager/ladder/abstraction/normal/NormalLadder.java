package dev.lokspel.practice.manager.ladder.abstraction.normal;

import dev.lokspel.practice.manager.arena.ArenaManager;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.guis.ladder.LadderPreviewGui;
import dev.lokspel.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import dev.lokspel.practice.manager.ladder.enums.WeightClassType;
import dev.lokspel.practice.manager.ladder.util.LadderUtil;
import dev.lokspel.practice.util.items.BasicItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
public abstract class NormalLadder extends Ladder {

  protected final LadderFile ladderFile;

  // Settings the admins can set.
  @Setter protected WeightClassType weightClass = WeightClassType.UNRANKED;
  protected boolean frozen; // If the ladder is frozen, players can't start a new game with it.
  @Setter protected boolean editable = true;

  protected List<BasicItem> destroyableBlocks = new ArrayList<>();

  // Extra items for custom kits
  protected Map<Boolean, ItemStack[]> customKitExtraItems = new HashMap<>();

  // Preview gui
  @Setter protected LadderPreviewGui previewGui;

  /**
   * Names of arenas this ladder was assigned to at the time it was disabled. Populated by {@link
   * ArenaManager#removeLadder} and consumed (then cleared) by {@link LadderUtil#enableLadder} so
   * the assignments are automatically restored when the ladder is re-enabled.
   */
  protected final Set<String> previouslyAssignedArenas = new HashSet<>();

  protected NormalLadder(String name, LadderType type) {
    super(name, type);

    this.ladderFile = new LadderFile(this);
    this.build = type.isBuild();
    this.getData();

    this.previewGui = new LadderPreviewGui(this);
  }

  public void setData() {
    ladderFile.setData();
  }

  public void getData() {
    ladderFile.getData();
  }

  public void deleteData() {
    ladderFile.delete();
  }

  public void copyData(String name) {
    ladderFile.copyTo(name);
  }

  public boolean isReadyToEnable() {
    return icon != null && kitData.isSet();
  }

  public void setFrozen(boolean frozen) {
    this.frozen = frozen;

    GuiManager.getInstance().searchGui(GuiType.Ladder_Summary).update();
    LadderSetupManager.getInstance()
        .getLadderSetupGuis()
        .get(this)
        .get(GuiType.Ladder_Main)
        .update();
    GuiManager.getInstance().searchGui(GuiType.Queue_Unranked).update(true);

    if (this.isRanked()) {
      GuiManager.getInstance().searchGui(GuiType.Queue_Ranked).update(true);
    }
  }

  public boolean isUnranked() {
    return weightClass.equals(WeightClassType.UNRANKED)
        || weightClass.equals(WeightClassType.UNRANKED_AND_RANKED);
  }

  public boolean isRanked() {
    return weightClass.equals(WeightClassType.RANKED)
        || weightClass.equals(WeightClassType.UNRANKED_AND_RANKED);
  }

  @Override
  public List<Arena> getArenas() {
    List<Arena> arenas = new ArrayList<>();
    for (Arena arena : ArenaManager.getInstance().getNormalArenas()) {
      if (arena.isEnabled()) {
        if (arena.getAssignedLadders().contains(this)) {
          arenas.add(arena);
        }
      }
    }
    return arenas;
  }
}
