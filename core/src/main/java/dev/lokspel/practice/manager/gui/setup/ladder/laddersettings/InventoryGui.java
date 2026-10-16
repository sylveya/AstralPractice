package dev.lokspel.practice.manager.gui.setup.ladder.laddersettings;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.ladder.LadderSetupManager;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.KitData;
import dev.lokspel.practice.util.TimeUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class InventoryGui extends Gui {

  @Getter private final NormalLadder ladder;
  private final KitData kitData;

  public InventoryGui(NormalLadder ladder) {
    super(GuiType.Ladder_Inventory);
    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getLadder()
                .getInventory()
                .getTitle()
                .replace("%ladder%", ladder.getName()),
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

    ItemStack filler = GuiManager.fillerItem();
    for (int i : new int[] {36, 37, 38, 39, 40, 41, 42, 43, 44, 46}) {
      inventory.setItem(i, filler);
    }

    inventory.setItem(
        45,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getSetup()
                    .getLadder()
                    .getInventory()
                    .getIcons()
                    .getBackTo())
            .get());
    inventory.setItem(47, getCustomKitEditorItem(ladder));
    inventory.setItem(48, getEffectItem(kitData.getEffects()));

    if (kitData.isSet()) {
      // Inventory content
      if (kitData.getStorage() != null) {
        InventoryUtil.mapPlayerInventoryToGui(inventory, kitData.getStorage());
      }

      // Armor content
      if (kitData.getArmor() != null) {
        ItemStack[] armorContent = kitData.getArmor();
        for (int i = 50; i < 54 && (i - 50) < armorContent.length; i++) {
          inventory.setItem(i, armorContent[i - 50]);
        }
      }

      if (kitData.getExtra() != null && kitData.getExtra().length > 0) {
        inventory.setItem(49, kitData.getExtra()[0]);
      }
    }

    updatePlayers();
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    Inventory inventory = e.getView().getTopInventory();
    ClickType click = e.getClick();
    int slot = e.getRawSlot();

    if (inventory.getSize() > slot) {
      if (slot == 45
          || slot == 46
          || slot == 47
          || slot == 48
          || Objects.equals(e.getCurrentItem(), GuiManager.fillerItem())) {
        e.setCancelled(true);

        if (slot == 45) {
          LadderSetupManager.getInstance()
              .getLadderSetupGuis()
              .get(ladder)
              .get(GuiType.Ladder_Main)
              .open(player);
        }
        if (slot == 47 && ladder.isEditable()) {
          LadderSetupManager.getInstance()
              .getLadderSetupGuis()
              .get(ladder)
              .get(GuiType.Ladder_CustomKitExtra_unRanked)
              .open(player);
        }
      } else {
        if (!ladder.isEnabled()) {
          if (click.equals(ClickType.LEFT) || click.equals(ClickType.RIGHT)) {
            ItemStack cursor = player.getItemOnCursor();
            if (!cursor.getType().isAir()) {
              EquipmentSlot requiredSlot =
                  switch (slot) {
                    case 50 -> EquipmentSlot.FEET;
                    case 51 -> EquipmentSlot.LEGS;
                    case 52 -> EquipmentSlot.CHEST;
                    case 53 -> EquipmentSlot.HEAD;
                    default -> null;
                  };
              if (requiredSlot != null && cursor.getType().getEquipmentSlot() != requiredSlot) {
                e.setCancelled(true);
              }
            }
          } else {
            e.setCancelled(true);
          }
        } else {
          e.setCancelled(true);
          Common.sendMmMessage(
              player,
              PracticeConfig.language().getCommand().getSetup().getLadder().getCantEditEnabled());
        }
      }
    }
  }

  public void handleCloseEvent(InventoryCloseEvent e) {
    if (LadderManager.getInstance().getLadders().contains(ladder) && !ladder.isEnabled()) {
      this.save();
    }
  }

  @Override
  public void handleDragEvent(InventoryDragEvent e) {
    e.setCancelled(true);
  }

  public void save() {
    List<ItemStack> inventoryContent = new ArrayList<>();
    List<ItemStack> armorContent = new ArrayList<>();

    Inventory inventory = gui.get(1);
    for (int i = 27; i < 36; i++) {
      inventoryContent.add(inventory.getItem(i));
    }
    for (int i = 0; i < 27; i++) {
      inventoryContent.add(inventory.getItem(i));
    }
    for (int i = 50; i < 54; i++) {
      armorContent.add(inventory.getItem(i));
    }

    List<ItemStack> extraContent = new ArrayList<>();
    extraContent.add(inventory.getItem(49));
    kitData.setExtra(extraContent.toArray(new ItemStack[0]));

    kitData.setStorage(inventoryContent.toArray(new ItemStack[0]));
    kitData.setArmor(armorContent.toArray(new ItemStack[0]));
  }

  private static ItemStack getCustomKitEditorItem(NormalLadder ladder) {
    if (ladder.isEditable()) {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getSetup()
                  .getLadder()
                  .getInventory()
                  .getIcons()
                  .getCustomKitEditor()
                  .getEditable())
          .get();
    } else {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getSetup()
                  .getLadder()
                  .getInventory()
                  .getIcons()
                  .getCustomKitEditor()
                  .getNotEditable())
          .get();
    }
  }

  private ItemStack getEffectItem(List<PotionEffect> effects) {
    if (!effects.isEmpty()) {
      GuiItem guiItem =
          BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getSetup()
                      .getLadder()
                      .getInventory()
                      .getIcons()
                      .getEffects()
                      .getHasEffect())
              .replace("%ladder%", ladder.getName());

      List<String> effectStrings = new ArrayList<>();
      for (PotionEffect potionEffect : effects) {
        effectStrings.add(
            PracticeConfig.guis()
                .getGuis()
                .getSetup()
                .getLadder()
                .getInventory()
                .getIcons()
                .getEffects()
                .getHasEffect()
                .getEffectFormat()
                .replace(
                    "%name%",
                    StringUtils.capitalize(
                        potionEffect.getType().getKey().getKey().replace("_", " ").toLowerCase()))
                .replace("%amplifier%", String.valueOf(potionEffect.getAmplifier() + 1))
                .replace(
                    "%time%", TimeUtil.formatDuration((potionEffect.getDuration() / 20) * 1000L)));
      }

      List<String> lore = new ArrayList<>();
      for (String line : guiItem.getLore()) {
        if (line.contains("%effects%")) {
          lore.addAll(effectStrings);
        } else {
          lore.add(line);
        }
      }

      guiItem.setLore(lore);
      return guiItem.get();
    } else {
      return BackendUtil.getGuiItem(
              PracticeConfig.guis()
                  .getGuis()
                  .getSetup()
                  .getLadder()
                  .getInventory()
                  .getIcons()
                  .getEffects()
                  .getNoEffect())
          .get();
    }
  }
}
