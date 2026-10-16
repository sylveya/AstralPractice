package dev.lokspel.practice.manager.inventory.inventoryitem.spectatoritems.spectatormodeitems.match;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.gui.guis.SpectatorTargetsGui;
import dev.lokspel.practice.manager.inventory.inventoryitem.InvItem;
import dev.lokspel.practice.manager.spectator.SpectatorManager;
import dev.lokspel.practice.util.Common;
import org.bukkit.entity.Player;

public class SpectatorTargetsInvItem extends InvItem {

  public SpectatorTargetsInvItem() {
    super(PracticeConfig.inventories().getSpectator().getMatch().getNormal().get("TARGETS"));
  }

  @Override
  public void handleClickEvent(Player player) {
    if (!player.hasPermission("ap.spectate.targets")) {
      Common.sendMmMessage(player, PracticeConfig.language().getSpectate().getNoPermissions());
      return;
    }

    if (SpectatorManager.getInstance().getSpectatable(player) instanceof Match match) {
      new SpectatorTargetsGui(match).open(player);
    }
  }
}
