package dev.lokspel.practice.util;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

@Getter
public class KitData {

  protected ItemStack[] storage;
  protected ItemStack[] armor;
  protected ItemStack[] extra;
  @Setter protected List<PotionEffect> effects = new ArrayList<>();

  public KitData() {}

  public KitData(KitData kitData) {
    if (kitData.getStorage() != null) {
      this.storage = kitData.getStorage().clone();
    }

    if (kitData.getArmor() != null) {
      this.armor = kitData.getArmor().clone();
    }

    if (kitData.getExtra() != null) {
      this.extra = kitData.getExtra().clone();
    }

    this.effects = new ArrayList<>(kitData.getEffects());
  }

  public boolean isSet() {
    return storage != null && armor != null;
  }

  public void setKitData(final Player player, final boolean setEffect) {
    this.setStorage(player);
    this.setArmor(player);
    this.setExtra(player);

    if (setEffect && !player.getActivePotionEffects().isEmpty()) {
      this.effects = new ArrayList<>();

      for (PotionEffect effect : player.getActivePotionEffects()) {
        int durationInSeconds = effect.getDuration() / 20;
        int roundedDurationInSeconds = (int) Math.ceil(durationInSeconds / 10.0) * 10;
        int roundedDurationInMillis = roundedDurationInSeconds * 20;
        this.effects.add(
            new PotionEffect(effect.getType(), roundedDurationInMillis, effect.getAmplifier()));
      }
    }
  }

  public void loadKitData(final Player player, final boolean clearActiveEffects) {
    this.loadStorage(player);
    this.loadArmor(player);
    this.loadExtra(player);

    if (!effects.isEmpty()) {
      if (clearActiveEffects) {
        player.getActivePotionEffects().clear();
      }

      for (PotionEffect effect : effects) {
        player.addPotionEffect(effect);
      }
    }

    player.updateInventory();
  }

  public void setStorage(Player player) {
    this.storage = player.getInventory().getStorageContents().clone();
  }

  public void setStorage(ItemStack[] storage) {
    this.storage = storage.clone();
  }

  public void setArmor(Player player) {
    this.armor = player.getInventory().getArmorContents().clone();
  }

  public void setArmor(ItemStack[] armor) {
    this.armor = armor.clone();
  }

  public void setExtra(Player player) {
    this.extra = player.getInventory().getExtraContents().clone();
  }

  public void setExtra(ItemStack[] extra) {
    this.extra = extra.clone();
  }

  private void loadStorage(Player player) {
    player.getInventory().setContents(this.storage.clone());
  }

  private void loadArmor(Player player) {
    player.getInventory().setArmorContents(this.armor.clone());
  }

  private void loadExtra(Player player) {
    player.getInventory().setExtraContents(this.extra.clone());
  }
}
