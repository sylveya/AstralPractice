package dev.lokspel.practice.manager.inventory.inventoryitem.staffitems;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.inventory.inventoryitem.InvItem;
import dev.lokspel.practice.manager.spectator.SpectatorManager;
import dev.lokspel.practice.util.interfaces.Spectatable;
import org.bukkit.entity.Player;

public class LeaveSpecInvItem extends InvItem {

  public LeaveSpecInvItem() {
    super(PracticeConfig.inventories().getStaffMode().getNormal().get("LEAVE-SPECTATE"));
  }

  @Override
  public void handleClickEvent(Player player) {
    if (player == null) {
      return;
    }
    Spectatable spectatable =
        SpectatorManager.getInstance().getSpectators().get(player.getUniqueId());
    if (spectatable != null) {
      spectatable.removeSpectator(player);
    }
  }
}
