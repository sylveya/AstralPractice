package dev.lokspel.practice.manager.gui.guis.ladder;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.KitData;
import dev.lokspel.practice.util.TimeUtil;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class LadderPreviewGui extends Gui {

  private final NormalLadder ladder;
  private final KitData kitData;

  public LadderPreviewGui(NormalLadder ladder) {
    super(GuiType.Ladder_Preview);
    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis()
                .getGuis()
                .getLadderPreview()
                .getTitle()
                .replace("%ladder%", ladder.getDisplayName()),
            6));

    this.ladder = ladder;
    this.kitData = ladder.getKitData();

    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);
    if (inventory == null) {
      return;
    }
    inventory.clear();

    // Set filler items
    ItemStack filler = GuiManager.fillerItem();
    for (int i : new int[] {36, 37, 38, 39, 40, 41, 42, 43, 44, 46, 47, 48, 49}) {
      inventory.setItem(i, filler);
    }

    inventory.setItem(45, getEffectItem());

    // Inventory Content
    if (kitData.getStorage() != null) {
      InventoryUtil.mapPlayerInventoryToGui(inventory, kitData.getStorage());
    }

    if (kitData.getArmor() != null) {
      ItemStack[] armor = kitData.getArmor();
      for (int i = 50; i < 54 && (i - 50) < armor.length; i++) {
        inventory.setItem(i, armor[i - 50]);
      }
    }

    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    e.setCancelled(true);
  }

  private ItemStack getEffectItem() {
    if (!kitData.getEffects().isEmpty()) {
      List<String> effects = new ArrayList<>();
      for (PotionEffect potionEffect : kitData.getEffects()) {
        effects.add(
            PracticeConfig.guis()
                .getGuis()
                .getLadderPreview()
                .getIcons()
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
              .getLadderPreview()
              .getIcons()
              .getHasEffect()
              .getIcon()
              .getLore()) {
        if (line.contains("%effects%")) {
          lore.addAll(effects);
        } else {
          lore.add(line.replace("%ladder%", ladder.getDisplayName()));
        }
      }

      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getLadderPreview()
                  .getIcons()
                  .getHasEffect()
                  .getIcon())
          .replace("%ladder%", ladder.getDisplayName())
          .setLore(lore)
          .get();
    } else {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis().getGuis().getLadderPreview().getIcons().getNoEffect())
          .get();
    }
  }
}
