package dev.lokspel.practice.listener;

import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.util.BlockUtil;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import io.papermc.paper.event.player.PlayerFlowerPotManipulateEvent;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerInteract implements Listener {

  private static final double SOUP_HEAL =
      PracticeConfig.settings().getMatchSettings().getSoup().getHeal();

  @EventHandler
  public void onFlowerPotManipulate(PlayerFlowerPotManipulateEvent e) {
    // Prevent taking flowers out of pots in MATCH, FFA and EVENT.
    if (e.isPlacing()) {
      return;
    }

    Profile profile = ProfileManager.getInstance().getProfile(e.getPlayer());
    if (profile == null) {
      return;
    }

    switch (profile.getStatus()) {
      case MATCH, FFA, EVENT -> e.setCancelled(true);
      default -> {}
    }
  }

  @EventHandler
  public void onSoup(PlayerInteractEvent e) {
    Player player = e.getPlayer();
    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (profile == null) {
      return;
    }

    Action action = e.getAction();
    if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR) {
      return;
    }

    ItemStack item = e.getItem();
    if (item == null || item.getType() != Material.MUSHROOM_STEW) {
      return;
    }

    switch (profile.getStatus()) {
      case MATCH, FFA, EVENT -> {
        if (player.getFoodLevel() < 20) {
          e.setCancelled(true);
          return;
        }

        double health = player.getHealth();
        double maxHealth =
            Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();

        if (health >= maxHealth) {
          return;
        }

        double newHealth = Math.min(health + SOUP_HEAL, maxHealth);

        consumeUsedSoup(player, e.getHand());
        player.setHealth(newHealth);
        player.updateInventory();
      }
      default -> {}
    }
  }

  private void consumeUsedSoup(Player player, EquipmentSlot hand) {
    if (hand == EquipmentSlot.OFF_HAND) {
      player.getInventory().setItemInOffHand(null);
    } else {
      player.getInventory().setItemInMainHand(null);
    }
  }

  @EventHandler
  public void onBedInteract(PlayerInteractEvent e) {
    Player player = e.getPlayer();
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return;
    }

    if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
      return;
    }
    if (player.isSneaking()) {
      return;
    }

    Block block = e.getClickedBlock();
    if (block == null || !BlockUtil.isBedMaterial(block.getType())) {
      return;
    }

    switch (profile.getStatus()) {
      case MATCH, FFA, EVENT -> e.setCancelled(true);
      default -> {}
    }
  }

  @EventHandler
  public void onBedEnter(PlayerBedEnterEvent e) {
    Profile profile = ProfileManager.getInstance().getProfile(e.getPlayer());
    if (profile == null) {
      return;
    }

    switch (profile.getStatus()) {
      case MATCH, FFA, EVENT -> e.setCancelled(true);
      default -> {}
    }
  }
}
