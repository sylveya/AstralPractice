package dev.lokspel.practice.manager.gui.guis;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.util.FightUtil;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.PageUtil;
import dev.lokspel.practice.util.interfaces.Spectatable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@Getter
public class SpectatorMenuGui extends Gui {

  private static final int SPACES = 27;
  private final Map<Integer, Map<Integer, Spectatable>> pageSlots = new HashMap<>();
  private int totalSpectatables = 0;

  public SpectatorMenuGui() {
    super(GuiType.Spectator_Menu);

    build();
  }

  @Override
  public void build() {
    update();
  }

  @Override
  public void update() {
    if (!AstralPractice.getInstance().isEnabled()) {
      return;
    }

    List<Spectatable> spectatables = new ArrayList<>();
    for (Spectatable spectatable : FightUtil.getSpectatables()) {
      if (spectatable.canDisplay()) {
        spectatables.add(spectatable);
      }
    }
    this.totalSpectatables = spectatables.size();

    Map<Integer, Inventory> newGui = new HashMap<>();
    Map<Integer, Map<Integer, Spectatable>> newPageSlots = new HashMap<>();

    ItemStack fillerItem =
        BackendUtil.getGuiItem(
                PracticeConfig.guis().getGuis().getSpectatorMenu().getIcons().getFillerItem())
            .get();

    int maxPage = PageUtil.getMaxPage(spectatables.size(), SPACES);
    for (int page = 1; page <= maxPage; page++) {
      Inventory inventory = gui.get(page);
      if (inventory == null || inventory.getSize() != 5 * 9) {
        inventory =
            InventoryUtil.createInventory(
                PracticeConfig.guis()
                    .getGuis()
                    .getSpectatorMenu()
                    .getTitle()
                    .replace("%page%", String.valueOf(page)),
                5);
      } else {
        inventory.clear();
      }
      newGui.put(page, inventory);

      // Frame
      for (int i : new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 36, 37, 38, 39, 40, 41, 42, 43, 44}) {
        inventory.setItem(i, fillerItem);
      }

      Map<Integer, Spectatable> currentSlots = new HashMap<>();
      int startIndex = (page - 1) * SPACES;
      int endIndex = Math.min(startIndex + SPACES, spectatables.size());
      for (int i = startIndex; i < endIndex; i++) {
        Spectatable spectatable = spectatables.get(i);
        int slot = inventory.firstEmpty();
        if (slot != -1 && slot < inventory.getSize()) {
          inventory.setItem(slot, spectatable.getSpectatorMenuItem().get());
          currentSlots.put(slot, spectatable);
        }
      }
      newPageSlots.put(page, currentSlots);

      // Left navigation
      ItemStack left =
          page > 1
              ? BackendUtil.getGuiItem(
                      PracticeConfig.guis().getGuis().getSpectatorMenu().getIcons().getPageLeft())
                  .replace("%page%", String.valueOf(page - 1))
                  .get()
              : BackendUtil.getGuiItem(
                      PracticeConfig.guis().getGuis().getSpectatorMenu().getIcons().getClose())
                  .get();
      inventory.setItem(36, left);

      // Right navigation
      ItemStack right =
          page < maxPage
              ? BackendUtil.getGuiItem(
                      PracticeConfig.guis().getGuis().getSpectatorMenu().getIcons().getPageRight())
                  .replace("%page%", String.valueOf(page + 1))
                  .get()
              : fillerItem;
      inventory.setItem(44, right);
    }

    updatePages(newGui, pageSlots, newPageSlots);
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    final Profile profile = ProfileManager.getInstance().getProfile(player);

    final Inventory inventory = e.getView().getTopInventory();
    ItemStack item = e.getCurrentItem();
    final int slot = e.getRawSlot();

    e.setCancelled(true);

    if (item == null || item.getType() == Material.AIR) {
      return;
    }

    if (inventory.getSize() > slot) {
      int page = inGuiPlayers.getOrDefault(player, 1);

      switch (slot) {
        case 36:
          if (page == 1) {
            player.closeInventory();
          } else {
            open(player, page - 1);
          }
          break;
        case 44:
          if (PageUtil.isPageValid(totalSpectatables, page + 1, SPACES)) {
            open(player, page + 1);
          }
          break;
        default:
          Map<Integer, Spectatable> currentSlots = pageSlots.get(page);
          if (currentSlots == null || !currentSlots.containsKey(slot)) {
            return;
          }
          Spectatable spectatable = currentSlots.get(slot);
          if (spectatable == null) {
            return;
          }

          if (!spectatable.canDisplay()) {
            this.update();
            return;
          }
          spectatable.addSpectator(player, null, true, !profile.isStaffMode());
          break;
      }
    }
  }
}
