package dev.lokspel.practice.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.util.actionbar.ActionBarPriority;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.FoodProperties;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class GoldenHead implements Listener {

  private final NamespacedKey goldenHeadKey =
      new NamespacedKey(AstralPractice.getInstance(), "golden_head_item");
  private ItemStack goldenHeadItem;
  private final List<PotionEffect> effects = new ArrayList<>();
  private int consumeCooldownSeconds;

  private final Map<UUID, Long> lastConsumeAt = new HashMap<>();
  private final Map<UUID, BukkitTask> cooldownActionBarTasks = new HashMap<>();

  public GoldenHead() {
    Bukkit.getPluginManager().registerEvents(this, AstralPractice.getInstance());
    reload();
  }

  public void reload() {
    this.consumeCooldownSeconds =
        Math.max(0, PracticeConfig.settings().getMatchSettings().getGoldenHead().getCooldown());

    ItemStack item =
        BackendUtil.getGuiItem(
                PracticeConfig.settings().getMatchSettings().getGoldenHead().getItem())
            .get();
    this.goldenHeadItem =
        Objects.requireNonNullElseGet(item, () -> new ItemStack(Material.GOLDEN_APPLE));

    this.goldenHeadItem = applyCustomTexture(this.goldenHeadItem);
    markAsGoldenHead(this.goldenHeadItem);
    applyAnimationComponents(this.goldenHeadItem);
    this.goldenHeadItem.setAmount(1);

    this.effects.clear();
    loadEffects();
  }

  public ItemStack getItem() {
    return this.goldenHeadItem.clone();
  }

  private void loadEffects() {
    for (String effect :
        PracticeConfig.settings().getMatchSettings().getGoldenHead().getEffects()) {
      String[] split = effect.split("::");
      if (split.length != 3) {
        continue;
      }

      PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(split[0].toLowerCase()));
      if (type == null) {
        type =
            Registry.EFFECT.get(NamespacedKey.minecraft(split[0].replace(' ', '_').toLowerCase()));
      }
      if (type == null) {
        continue;
      }

      if (NumberUtil.isNotInteger(split[1])) {
        continue;
      }
      int duration = Integer.parseInt(split[1]);

      if (NumberUtil.isNotInteger(split[2])) {
        continue;
      }
      int amplifier = Integer.parseInt(split[2]);
      if (amplifier < 1) {
        continue;
      }

      PotionEffect potionEffect = new PotionEffect(type, duration * 20, (amplifier - 1));
      effects.add(potionEffect);
    }
  }

  private void markAsGoldenHead(ItemStack itemStack) {
    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta == null) {
      return;
    }

    itemMeta.getPersistentDataContainer().set(goldenHeadKey, PersistentDataType.BYTE, (byte) 1);
    itemStack.setItemMeta(itemMeta);
  }

  public boolean isGoldenHead(ItemStack itemStack) {
    if (itemStack == null || !itemStack.hasItemMeta()) {
      return false;
    }

    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta != null
        && itemMeta.getPersistentDataContainer().has(goldenHeadKey, PersistentDataType.BYTE)) {
      return true;
    }

    return this.goldenHeadItem != null && itemStack.isSimilar(this.goldenHeadItem);
  }

  @SuppressWarnings("HttpUrlsUsage")
  private ItemStack applyCustomTexture(ItemStack item) {
    String textureConfigValue =
        PracticeConfig.settings().getMatchSettings().getGoldenHead().getTexture().trim();
    if (textureConfigValue.isEmpty()) {
      return item;
    }

    if (item.getType() != Material.PLAYER_HEAD) {
      item = item.withType(Material.PLAYER_HEAD);
    }

    SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
    if (skullMeta == null) {
      return item;
    }

    PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "GoldenHead");

    if (textureConfigValue.startsWith("http://") || textureConfigValue.startsWith("https://")) {
      String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + textureConfigValue + "\"}}}";
      textureConfigValue =
          Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    profile.setProperty(new ProfileProperty("textures", textureConfigValue));
    skullMeta.setPlayerProfile(profile);

    item.setItemMeta(skullMeta);
    return item;
  }

  private long getRemainingCooldownSeconds(UUID playerId) {
    if (this.consumeCooldownSeconds < 1) {
      return 0;
    }

    Long lastUse = this.lastConsumeAt.get(playerId);
    if (lastUse == null) {
      return 0;
    }

    long elapsedSeconds = (System.currentTimeMillis() - lastUse) / 1000L;
    long remaining = this.consumeCooldownSeconds - elapsedSeconds;
    return Math.max(0, remaining);
  }

  private void startCooldownActionBar(Profile profile) {
    if (this.consumeCooldownSeconds < 1) {
      return;
    }

    UUID playerId = profile.getUuid();

    BukkitTask existingTask = this.cooldownActionBarTasks.remove(playerId);
    if (existingTask != null) {
      existingTask.cancel();
    }

    BukkitTask task =
        Bukkit.getScheduler()
            .runTaskTimer(
                AstralPractice.getInstance(),
                () -> {
                  Player onlinePlayer = profile.getPlayer().getPlayer();

                  if (onlinePlayer == null
                      || !onlinePlayer.isOnline()
                      || (!profile.getStatus().equals(ProfileStatus.MATCH)
                          && !profile.getStatus().equals(ProfileStatus.EVENT)
                          && !profile.getStatus().equals(ProfileStatus.FFA))) {
                    profile.getActionBar().removeMessage("golden_head");
                    BukkitTask removed = this.cooldownActionBarTasks.remove(playerId);
                    if (removed != null) {
                      removed.cancel();
                    }
                    if (onlinePlayer != null && onlinePlayer.isOnline()) {
                      onlinePlayer.setCooldown(
                          goldenHeadItem != null ? goldenHeadItem.getType() : Material.PLAYER_HEAD,
                          0);
                    }
                    return;
                  }

                  long remaining = getRemainingCooldownSeconds(playerId);

                  if (remaining <= 0) {
                    profile.getActionBar().removeMessage("golden_head");
                    BukkitTask removed = this.cooldownActionBarTasks.remove(playerId);
                    if (removed != null) {
                      removed.cancel();
                    }
                    return;
                  }

                  profile
                      .getActionBar()
                      .setMessage(
                          "golden_head",
                          PracticeConfig.settings()
                              .getMatchSettings()
                              .getGoldenHead()
                              .getActionBarCooldownMsg()
                              .replace("%remaining%", String.valueOf(remaining)),
                          2,
                          ActionBarPriority.HIGHEST);
                },
                0L,
                20L);

    this.cooldownActionBarTasks.put(playerId, task);
  }

  public boolean isAnimationEnabled() {
    return PracticeConfig.settings().getMatchSettings().getGoldenHead().isAnimation();
  }

  public void applyAnimationComponents(ItemStack item) {
    if (item == null) {
      return;
    }
    if (isAnimationEnabled()) {
      item.setData(
          DataComponentTypes.FOOD,
          FoodProperties.food().nutrition(4).saturation(9.6f).canAlwaysEat(true).build());
      item.setData(
          DataComponentTypes.CONSUMABLE,
          Consumable.consumable()
              .consumeSeconds(1.6f)
              .animation(ItemUseAnimation.EAT)
              .hasConsumeParticles(true)
              .sound(Key.key("entity.generic.eat"))
              .build());
    } else {
      item.resetData(DataComponentTypes.FOOD);
      item.resetData(DataComponentTypes.CONSUMABLE);
    }
  }

  public void syncComponents(ItemStack item) {
    if (item != null && isGoldenHead(item)) {
      applyAnimationComponents(item);
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onGoldenHeadInteract(PlayerInteractEvent e) {
    Action action = e.getAction();
    if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    ItemStack item = e.getItem();
    if (item == null || !isGoldenHead(item)) {
      return;
    }

    Player player = e.getPlayer();
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return;
    }

    long remainingSeconds = getRemainingCooldownSeconds(player.getUniqueId());
    if (remainingSeconds > 0 || player.hasCooldown(item.getType())) {
      e.setCancelled(true);
      startCooldownActionBar(profile);
      if (remainingSeconds > 0 && !player.hasCooldown(item.getType())) {
        player.setCooldown(item.getType(), (int) (remainingSeconds * 20));
      }
      return;
    }

    if (isAnimationEnabled()) {
      syncComponents(item);
      if (action == Action.RIGHT_CLICK_BLOCK) {
        e.setUseInteractedBlock(Event.Result.DENY);
        e.setUseItemInHand(Event.Result.ALLOW);
      }
      return;
    }

    e.setCancelled(true);
    consumeGoldenHead(player, profile, item, e.getHand());
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onGoldenHeadConsume(PlayerItemConsumeEvent e) {
    ItemStack item = e.getItem();
    if (!isGoldenHead(item)) {
      return;
    }

    Player player = e.getPlayer();
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return;
    }

    long remainingSeconds = getRemainingCooldownSeconds(player.getUniqueId());
    if (remainingSeconds > 0 || player.hasCooldown(item.getType())) {
      e.setCancelled(true);
      startCooldownActionBar(profile);
      return;
    }

    this.lastConsumeAt.put(player.getUniqueId(), System.currentTimeMillis());
    startCooldownActionBar(profile);
    if (this.consumeCooldownSeconds > 0) {
      player.setCooldown(item.getType(), (int) (this.consumeCooldownSeconds * 20));
    }

    applyEffects(player);
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onBlockPlace(BlockPlaceEvent e) {
    if (isGoldenHead(e.getItemInHand())) {
      e.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onItemHeld(PlayerItemHeldEvent e) {
    ItemStack item = e.getPlayer().getInventory().getItem(e.getNewSlot());
    if (item != null && isGoldenHead(item)) {
      syncComponents(item);
    }
  }

  private void consumeGoldenHead(
      Player player, Profile profile, ItemStack item, EquipmentSlot hand) {
    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1.0f, 1.0f);

    this.lastConsumeAt.put(player.getUniqueId(), System.currentTimeMillis());
    startCooldownActionBar(profile);
    if (this.consumeCooldownSeconds > 0) {
      player.setCooldown(item.getType(), (int) (this.consumeCooldownSeconds * 20));
    }

    if (hand != null) {
      int amount = item.getAmount();
      if (amount <= 1) {
        player.getInventory().setItem(hand, null);
      } else {
        item.setAmount(amount - 1);
      }
    }

    applyEffects(player);
  }

  private void applyEffects(Player player) {
    for (PotionEffect effect : effects) {
      PotionEffect active = player.getPotionEffect(effect.getType());
      if (active != null
          && (effect.getAmplifier() < active.getAmplifier()
              || effect.getDuration() < active.getDuration())) {
        continue;
      }
      player.addPotionEffect(effect);
    }
  }
}
