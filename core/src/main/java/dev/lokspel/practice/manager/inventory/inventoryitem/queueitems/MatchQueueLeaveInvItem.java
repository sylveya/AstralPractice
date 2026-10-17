package dev.lokspel.practice.manager.inventory.inventoryitem.queueitems;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.inventory.inventoryitem.InvItem;
import dev.lokspel.practice.manager.queue.QueueManager;
import org.bukkit.entity.Player;

public class MatchQueueLeaveInvItem extends InvItem {

  public MatchQueueLeaveInvItem() {
    super(PracticeConfig.inventories().getQueue().getMatch().getNormal().get("LEAVE-MATCH-QUEUE"));
  }

  @Override
  public void handleClickEvent(Player player) {
    QueueManager.getInstance().leaveQueue(player);
  }
}
