package dev.lokspel.practice.manager.ladder.type;

import static dev.lokspel.practice.util.startup.PermanentConfig.FIGHT_ENTITY;
import static dev.lokspel.practice.util.startup.PermanentConfig.PLACED_IN_FIGHT;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.settings.MatchSettings;
import dev.lokspel.practice.manager.arena.util.ArenaUtil;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.enums.RoundStatus;
import dev.lokspel.practice.manager.fight.util.BlockUtil;
import dev.lokspel.practice.manager.fight.util.ChangedBlock;
import dev.lokspel.practice.manager.fight.util.DeathCause;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.TempBuild;
import dev.lokspel.practice.manager.ladder.abstraction.interfaces.TempBuildReturnDelay;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.actionbar.ActionBarPriority;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class TntSumo extends NormalLadder implements LadderHandle, TempBuild, TempBuildReturnDelay {

  private static final int TNT_LIMIT = 10;
  private static final String TNT_ATTACK_REWARD_ACTIONBAR_ID = "tnt_sumo_tnt_attack_reward";
  private static final String TNT_EARN_ACTIONBAR_MESSAGE =
      PracticeConfig.language().getMatch().getActionbar().getTntSumoTntEarn();

  private static final Map<UUID, ItemStack> TNT_SUMO_PLAYER_TNT_CACHE = new HashMap<>();

  private static final String TNT_SUMO_TNT = "ASTRALPRACTICE_TNT_SUMO_TNT";
  private static final String TNT_SUMO_TNT_OWNER = "ASTRALPRACTICE_TNT_SUMO_TNT_OWNER";
  private static final String TNT_SUMO_BLOCK_OWNER = "ASTRALPRACTICE_TNT_SUMO_BLOCK_OWNER";
  private static final String TNT_SUMO_BLOCK_MATERIAL = "ASTRALPRACTICE_TNT_SUMO_BLOCK_MATERIAL";
  private static final String TNT_SUMO_BLOCK_ITEM = "ASTRALPRACTICE_TNT_SUMO_BLOCK_ITEM";

  // The old lookups read EXPLOSION.EXPLOSION-HORIZONTAL-RADIUS / EXPLOSION-VERTICAL-RADIUS, which
  // never existed, so these two always used their defaults.
  private static final double TNT_SUMO_EXPLOSION_HORIZONTAL_RADIUS = 4.0;
  private static final double TNT_SUMO_EXPLOSION_VERTICAL_RADIUS = 2.5;
  private static final double TNT_SUMO_HORIZONTAL_MULTIPLIER =
      explosion().getHorizontalMultiplier();
  private static final double TNT_SUMO_VERTICAL_MULTIPLIER = explosion().getVerticalMultiplier();
  private static final double TNT_SUMO_SWEET_SPOT_Y_BOOST = explosion().getSweetSpotYboost();
  private static final double TNT_SUMO_GROUNDED_HORIZONTAL_MULTIPLIER =
      explosion().getGroundedHorizontalMultiplier();
  private static final double TNT_SUMO_GROUNDED_VERTICAL_MULTIPLIER =
      explosion().getGroundedVerticalMultiplier();
  private static final double TNT_SUMO_GROUNDED_CHAIN_LIFT = explosion().getGroundedChainLift();
  private static final double TNT_SUMO_NEAR_FULL_FORCE_DISTANCE =
      explosion().getNearFullForceDistance();
  private static final double TNT_SUMO_SHARP_FALLOFF_START_DISTANCE =
      explosion().getSharpFalloffStartDistance();
  private static final double TNT_SUMO_MID_FORCE_AT_FALLOFF_START =
      explosion().getMidForceAtFalloffStart();
  private static final double TNT_SUMO_AIRBORNE_JUGGLE_MULTIPLIER =
      explosion().getAirborneJuggleMultiplier();
  private static final long TNT_SUMO_AIRBORNE_STACK_WINDOW_MILLIS =
      explosion().getAirborneStackWindowMillis();
  private static final double TNT_SUMO_AIRBORNE_OVERFLOW_PER_EXTRA_HIT =
      explosion().getAirborneOverflowPerExtraHit();
  private static final double TNT_SUMO_AIRBORNE_OVERFLOW_MAX = explosion().getAirborneOverflowMax();

  private static final double TNT_SUMO_SWEET_SPOT_MAX_Y_DIFF = 1.25;
  private static final long TNT_SUMO_CHAIN_WINDOW_MILLIS = 850L;
  private static final int TNT_SUMO_CHAIN_LIFT_START_HIT = 2;
  private static final double TNT_SUMO_EFFECTIVE_GROUNDED_Y_VELOCITY_THRESHOLD = 0.15;

  // Increased ceilings to allow the SWEET SPOT boost to actually launch the player
  private static final double TNT_SUMO_MAX_HORIZONTAL_VELOCITY = 1.85;
  private static final double TNT_SUMO_MAX_UPWARD_VELOCITY = 2.2;

  private static final Map<UUID, ChainState> TNT_SUMO_CHAIN_STATES = new HashMap<>();
  private static final Map<UUID, AirborneStackState> TNT_SUMO_AIRBORNE_STACK_STATES =
      new HashMap<>();

  @Getter @Setter private int tempBuildReturnDelaySeconds;

  public TntSumo(String name, LadderType type) {
    super(name, type);
    this.startMove = false;
    this.hunger = false;
  }

  @Override
  public boolean handleEvents(Event e, Match match) {
    if (e instanceof BlockPlaceEvent blockPlaceEvent) {
      onBlockPlace(blockPlaceEvent, match);
      return true;
    }
    if (e instanceof BlockBreakEvent blockBreakEvent) {
      onBlockBreak(blockBreakEvent, match);
      return true;
    }
    if (e instanceof PlayerBucketEmptyEvent playerBucketEmptyEvent) {
      onBucketEmpty(playerBucketEmptyEvent, match);
      return true;
    }
    if (e instanceof EntityDamageEvent entityDamageEvent) {
      onPlayerDamage(entityDamageEvent, match);
      return true;
    }
    if (e instanceof EntityExplodeEvent entityExplodeEvent) {
      onEntityExplode(entityExplodeEvent, match);
      return true;
    }
    if (e instanceof org.bukkit.event.block.BlockExplodeEvent blockExplodeEvent) {
      onBlockExplode(blockExplodeEvent, match);
      return true;
    }
    if (e instanceof PlayerMoveEvent playerMoveEvent) {
      onPlayerMove(playerMoveEvent, match);
      return true;
    }
    if (e instanceof org.bukkit.event.player.PlayerDropItemEvent playerDropItemEvent) {
      onItemDrop(playerDropItemEvent);
      return true;
    }
    return false;
  }

  private static void onBlockPlace(@NotNull BlockPlaceEvent e, @NotNull Match match) {
    Block block = e.getBlockPlaced();
    Player player = e.getPlayer();

    if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
      return;
    }

    Material blockMaterial = block.getType();

    if (!blockMaterial.equals(Material.TNT)) {
      TempBuild.onBlockPlace(
          e, match, ((TntSumo) match.getLadder()).getTempBuildReturnDelaySeconds());
      return;
    }

    ItemStack returnItem = createPlacedReturnItem(e, blockMaterial);
    TNT_SUMO_PLAYER_TNT_CACHE.put(player.getUniqueId(), returnItem.clone());

    BlockUtil.setMetadata(block, PLACED_IN_FIGHT, match);
    BlockUtil.setMetadata(block, TNT_SUMO_BLOCK_OWNER, player.getUniqueId());
    BlockUtil.setMetadata(block, TNT_SUMO_BLOCK_MATERIAL, blockMaterial);
    BlockUtil.setMetadata(block, TNT_SUMO_BLOCK_ITEM, returnItem);
    match.addBlockChange(new ChangedBlock(e));

    Block underBlock = block.getLocation().subtract(0, 1, 0).getBlock();
    if (ArenaUtil.turnsToDirt(underBlock)) {
      match.getFightChange().addArenaBlockChange(new ChangedBlock(underBlock));
    }

    Location tntLocation = block.getLocation().clone();
    block.setType(Material.AIR, false);

    TNTPrimed tntPrimed =
        block.getWorld().spawn(tntLocation.clone().add(0.5, 0.0, 0.5), TNTPrimed.class);
    tntPrimed.setFuseTicks(0);
    tntPrimed.setIsIncendiary(false);
    tntPrimed.setSource(player);

    BlockUtil.setMetadata(tntPrimed, FIGHT_ENTITY, match);
    BlockUtil.setMetadata(tntPrimed, TNT_SUMO_TNT, match);
    BlockUtil.setMetadata(tntPrimed, TNT_SUMO_TNT_OWNER, player.getUniqueId());
  }

  private static void onBlockBreak(@NotNull BlockBreakEvent e, @NotNull Match match) {
    final Block block = e.getBlock();

    if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
      return;
    }

    TempBuild.onBlockBreak(e, match);
    if (e.isCancelled()) {
      return;
    }

    if (!BlockUtil.hasMetadata(block, TNT_SUMO_BLOCK_OWNER)) {
      return;
    }

    e.setDropItems(false);

    UUID ownerId = BlockUtil.getMetadata(block, TNT_SUMO_BLOCK_OWNER, UUID.class);
    if (ownerId == null) {
      return;
    }

    Player owner = Bukkit.getPlayer(ownerId);
    if (owner == null
        || !owner.isOnline()
        || MatchManager.getInstance().getLiveMatchByPlayer(owner) != match) {
      return;
    }

    Material material = BlockUtil.getMetadata(block, TNT_SUMO_BLOCK_MATERIAL, Material.class);
    if (material == null) {
      material = block.getType();
    }

    if (material.isAir() || (material.equals(Material.TNT) && countTnt(owner) >= TNT_LIMIT)) {
      return;
    }

    ItemStack returnItem = getStoredBlockItem(block, material);
    PlayerUtil.returnItemToCurrentSlotOrInventory(owner, returnItem);
    owner.updateInventory();
  }

  private static void onEntityExplode(@NotNull EntityExplodeEvent e, @NotNull Match match) {
    Entity entity = e.getEntity();
    if (!BlockUtil.hasMetadata(entity, TNT_SUMO_TNT)) {
      return;
    }

    Match metadataMatch = BlockUtil.getMetadata(entity, TNT_SUMO_TNT, Match.class);
    if (metadataMatch != match
        || !match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
      e.blockList().clear();
      return;
    }

    e.setYield(0F);
    List<Block> destroyedBlocks = destroyPlacedBlocksWithoutDrops(e.blockList());
    e.blockList().clear();
    returnDestroyedBlocksToOwners(destroyedBlocks, match);
  }

  private static void onBlockExplode(
      @NotNull org.bukkit.event.block.BlockExplodeEvent e, @NotNull Match match) {
    if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
      e.blockList().clear();
      return;
    }

    e.setYield(0F);
    List<Block> destroyedBlocks = destroyPlacedBlocksWithoutDrops(e.blockList());
    e.blockList().clear();
    returnDestroyedBlocksToOwners(destroyedBlocks, match);
  }

  private static @NotNull List<Block> destroyPlacedBlocksWithoutDrops(
      @NotNull List<Block> explosionBlocks) {
    List<Block> destroyedBlocks = new ArrayList<>();
    for (Block block : explosionBlocks) {
      if (!BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) {
        continue;
      }
      destroyedBlocks.add(block);
      block.setType(Material.AIR, false);
    }
    return destroyedBlocks;
  }

  private static void returnDestroyedBlocksToOwners(
      @NotNull List<Block> destroyedBlocks, @NotNull Match match) {
    for (Block block : destroyedBlocks) {
      if (!BlockUtil.hasMetadata(block, TNT_SUMO_BLOCK_OWNER)) {
        continue;
      }

      UUID ownerId = BlockUtil.getMetadata(block, TNT_SUMO_BLOCK_OWNER, UUID.class);
      if (ownerId == null) {
        continue;
      }

      Player owner = Bukkit.getPlayer(ownerId);
      if (owner == null
          || !owner.isOnline()
          || MatchManager.getInstance().getLiveMatchByPlayer(owner) != match) {
        continue;
      }

      Material material = BlockUtil.getMetadata(block, TNT_SUMO_BLOCK_MATERIAL, Material.class);
      if (material == null) {
        material = block.getType();
      }

      if (material.isAir() || (material.equals(Material.TNT) && countTnt(owner) >= TNT_LIMIT)) {
        continue;
      }

      ItemStack returnItem = getStoredBlockItem(block, material);
      PlayerUtil.returnItemToCurrentSlotOrInventory(owner, returnItem);
      owner.updateInventory();
    }
  }

  private static @NotNull ItemStack createPlacedReturnItem(
      @NotNull BlockPlaceEvent e, @NotNull Material fallbackMaterial) {
    ItemStack placedItem = e.getItemInHand();
    if (placedItem.getType().isAir()) {
      return new ItemStack(fallbackMaterial, 1);
    }
    ItemStack clone = placedItem.clone();
    clone.setAmount(1);
    return clone;
  }

  private static @NotNull ItemStack getStoredBlockItem(
      @NotNull Block block, @NotNull Material fallbackMaterial) {
    ItemStack storedItem = BlockUtil.getMetadata(block, TNT_SUMO_BLOCK_ITEM, ItemStack.class);
    if (storedItem == null || storedItem.getType().isAir()) {
      return new ItemStack(fallbackMaterial, 1);
    }
    ItemStack clone = storedItem.clone();
    clone.setAmount(1);
    return clone;
  }

  private static void onPlayerDamage(@NotNull EntityDamageEvent e, @NotNull Match match) {
    if (!(e.getEntity() instanceof Player player)) {
      return;
    }

    if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
      e.setCancelled(true);
      return;
    }

    if (e instanceof EntityDamageByEntityEvent entityDamageByEntityEvent
        && entityDamageByEntityEvent.getDamager() instanceof TNTPrimed tnt
        && BlockUtil.hasMetadata(tnt, TNT_SUMO_TNT)) {
      Match metadataMatch = BlockUtil.getMetadata(tnt, TNT_SUMO_TNT, Match.class);
      if (metadataMatch != match) {
        e.setCancelled(true);
        return;
      }

      e.setCancelled(true);
      applyTntSumoKnockback(player, tnt);
      e.setDamage(0);
      return;
    }

    if (e instanceof EntityDamageByEntityEvent entityDamageByEntityEvent
        && entityDamageByEntityEvent.getDamager() instanceof Player attacker
        && entityDamageByEntityEvent.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
        && !attacker.getUniqueId().equals(player.getUniqueId())
        && match.getPlayers().contains(attacker)
        && !match.getCurrentStat(attacker).isSet()) {
      rewardTntForMeleeHit(attacker);
    }

    if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
      match.killPlayer(player, null, DeathCause.SUMO.getMessage());
      return;
    }

    e.setDamage(0);
    dev.lokspel.practice.util.playerutil.PlayerUtil.healToMaxHealth(player);
  }

  private static void onPlayerMove(@NotNull PlayerMoveEvent e, @NotNull Match match) {
    if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
      return;
    }

    Player player = e.getPlayer();
    Location playerLocation = player.getLocation();
    Material blockAtPlayer = playerLocation.getBlock().getType();
    Material blockBelow = playerLocation.clone().subtract(0, 1, 0).getBlock().getType();

    if (blockAtPlayer.equals(Material.WATER)
        || blockBelow.equals(Material.WATER)
        || blockAtPlayer.equals(Material.LAVA)
        || blockBelow.equals(Material.LAVA)) {
      match.killPlayer(player, null, DeathCause.SUMO.getMessage());
    }
  }

  private static int countTnt(@NotNull Player player) {
    int amount = 0;
    for (ItemStack itemStack : player.getInventory().getStorageContents()) {
      if (itemStack != null && itemStack.getType().equals(Material.TNT)) {
        amount += itemStack.getAmount();
      }
    }
    ItemStack offHand = player.getInventory().getItemInOffHand();
    if (offHand.getType().equals(Material.TNT)) {
      amount += offHand.getAmount();
    }
    return amount;
  }

  private static void onItemDrop(@NotNull org.bukkit.event.player.PlayerDropItemEvent e) {
    e.setCancelled(true);
  }

  private static void onBucketEmpty(@NotNull PlayerBucketEmptyEvent e, @NotNull Match match) {
    if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
      return;
    }

    TempBuild.onBucketEmpty(
        e, match, ((TntSumo) match.getLadder()).getTempBuildReturnDelaySeconds());
  }

  private static void applyTntSumoKnockback(Player player, TNTPrimed tnt) {
    final Location tntCenter = tnt.getLocation().clone();

    Bukkit.getScheduler()
        .runTaskLater(
            AstralPractice.getInstance(),
            () -> {
              Location currentPlayerLocation = player.getLocation();
              final Vector currentVelocity = player.getVelocity().clone();
              double horizontalRadius = Math.max(0.0, TNT_SUMO_EXPLOSION_HORIZONTAL_RADIUS);
              double verticalRadius = Math.max(0.0, TNT_SUMO_EXPLOSION_VERTICAL_RADIUS);

              if (horizontalRadius <= 0.0 || verticalRadius <= 0.0) {
                return;
              }

              double verticalDifference = currentPlayerLocation.getY() - tntCenter.getY();
              if (Math.abs(verticalDifference) > verticalRadius) {
                return;
              }

              double dx = currentPlayerLocation.getX() - tntCenter.getX();
              double dz = currentPlayerLocation.getZ() - tntCenter.getZ();
              double horizontalDistanceSquared = (dx * dx) + (dz * dz);
              double horizontalRadiusSquared = horizontalRadius * horizontalRadius;

              if (horizontalDistanceSquared > horizontalRadiusSquared) {
                return;
              }

              Vector explosionToPlayer =
                  currentPlayerLocation.toVector().subtract(tntCenter.toVector());
              if (explosionToPlayer.lengthSquared() <= 0.000001) {
                return;
              }

              double horizontalDistance = Math.sqrt(horizontalDistanceSquared);
              double forceFactor =
                  calculateForceFactor(
                      horizontalDistance, horizontalRadius, verticalDifference, verticalRadius);
              if (forceFactor <= 0.0) {
                return;
              }

              Vector direction = explosionToPlayer.clone().normalize();
              UUID playerId = player.getUniqueId();
              boolean isEffectivelyGrounded =
                  isEffectivelyGrounded(player, currentPlayerLocation, currentVelocity);
              boolean isOwner = isTntOwner(player, tnt);

              if (isEffectivelyGrounded) {
                int chainHits = updateChainHits(playerId);
                double groundedHorizontalX =
                    direction.getX() * TNT_SUMO_GROUNDED_HORIZONTAL_MULTIPLIER * forceFactor;
                double groundedHorizontalZ =
                    direction.getZ() * TNT_SUMO_GROUNDED_HORIZONTAL_MULTIPLIER * forceFactor;
                double groundedVerticalY = 0.0;

                if (isOwner) {
                  Vector lookDir = player.getLocation().getDirection();
                  lookDir.setY(0);
                  if (lookDir.lengthSquared() > 0) {
                    lookDir.normalize().multiply(0.12);
                    groundedHorizontalX = lookDir.getX();
                    groundedHorizontalZ = lookDir.getZ();
                  } else {
                    groundedHorizontalX = 0.0;
                    groundedHorizontalZ = 0.0;
                  }
                  groundedVerticalY = 0.05;
                } else {
                  double baseGroundedVerticalY =
                      Math.max(0.0, TNT_SUMO_GROUNDED_VERTICAL_MULTIPLIER * forceFactor);
                  double chainLiftBonus = calculateGroundedChainLiftBonus(chainHits, forceFactor);
                  groundedVerticalY = Math.max(0.0, baseGroundedVerticalY + chainLiftBonus);
                }

                Vector groundedVelocity =
                    new Vector(groundedHorizontalX, groundedVerticalY, groundedHorizontalZ);
                Vector newVelocity = currentVelocity.add(groundedVelocity);

                if (newVelocity.getY() < groundedVerticalY) {
                  newVelocity.setY(groundedVerticalY);
                }

                player.setVelocity(limitVelocity(newVelocity, 1.0));
                return;
              }

              clearChainHits(playerId);

              final double horizontalX =
                  direction.getX() * TNT_SUMO_HORIZONTAL_MULTIPLIER * forceFactor;
              final double horizontalZ =
                  direction.getZ() * TNT_SUMO_HORIZONTAL_MULTIPLIER * forceFactor;
              double verticalY = direction.getY() * TNT_SUMO_VERTICAL_MULTIPLIER * forceFactor;

              if (verticalDifference > 0.0
                  && verticalDifference <= TNT_SUMO_SWEET_SPOT_MAX_Y_DIFF) {
                verticalY = Math.max(0.0, verticalY) + (TNT_SUMO_SWEET_SPOT_Y_BOOST * forceFactor);
              } else if (verticalDifference <= 0.0) {
                verticalY = Math.max(0.05, verticalY * 0.2);
              }

              boolean isFalling = currentVelocity.getY() < 0.0;
              boolean isTntBelowPlayer = verticalDifference > 0.0;
              if (isFalling && isTntBelowPlayer) {
                currentVelocity.setY(0.0);
              }

              if (currentVelocity.getY() > 0.0 && isTntBelowPlayer) {
                currentVelocity.setY(currentVelocity.getY() * TNT_SUMO_AIRBORNE_JUGGLE_MULTIPLIER);
              }

              Vector explosionVelocity = new Vector(horizontalX, verticalY, horizontalZ);
              Vector newVelocity = currentVelocity.add(explosionVelocity);
              int airborneStackHits = updateAirborneStackHits(playerId);
              double overflowMultiplier = calculateAirborneOverflowMultiplier(airborneStackHits);
              player.setVelocity(limitVelocity(newVelocity, overflowMultiplier));
            },
            1L);
  }

  private static double calculateForceFactor(
      double horizontalDistance,
      double horizontalRadius,
      double verticalDifference,
      double verticalRadius) {
    double nearFullDistance = Math.clamp(horizontalRadius, 0.0, TNT_SUMO_NEAR_FULL_FORCE_DISTANCE);
    double sharpFalloffStart =
        Math.clamp(horizontalRadius, nearFullDistance, TNT_SUMO_SHARP_FALLOFF_START_DISTANCE);

    double horizontalFactor;
    if (horizontalDistance <= nearFullDistance) {
      horizontalFactor = 1.0;
    } else if (horizontalDistance <= sharpFalloffStart) {
      double midRange = Math.max(0.0001, sharpFalloffStart - nearFullDistance);
      double t = (horizontalDistance - nearFullDistance) / midRange;
      double targetAtFalloffStart = Math.clamp(TNT_SUMO_MID_FORCE_AT_FALLOFF_START, 0.0, 1.0);
      horizontalFactor = 1.0 - ((1.0 - targetAtFalloffStart) * t);
    } else {
      double sharpRange = Math.max(0.0001, horizontalRadius - sharpFalloffStart);
      double t = Math.clamp((horizontalDistance - sharpFalloffStart) / sharpRange, 0.0, 1.0);
      double targetAtFalloffStart = Math.clamp(TNT_SUMO_MID_FORCE_AT_FALLOFF_START, 0.0, 1.0);
      double sharpDrop = 1.0 - (t * t * t);
      horizontalFactor = targetAtFalloffStart * Math.max(0.0, sharpDrop);
    }

    double verticalFactor = Math.max(0.0, 1.0 - (Math.abs(verticalDifference) / verticalRadius));
    return Math.max(0.0, horizontalFactor * verticalFactor);
  }

  private static boolean isEffectivelyGrounded(
      @NotNull Player player, @NotNull Location playerLocation, @NotNull Vector currentVelocity) {
    if (player.isOnGround()) {
      return true;
    }

    // Slight buffer allows chained logic to stack correctly without instantly losing grounded
    // status
    if (Math.abs(currentVelocity.getY()) > TNT_SUMO_EFFECTIVE_GROUNDED_Y_VELOCITY_THRESHOLD) {
      return false;
    }

    Block blockBelow = playerLocation.clone().subtract(0.0, 0.2, 0.0).getBlock();
    return blockBelow.getType().isSolid();
  }

  private static int updateChainHits(@NotNull UUID playerId) {
    long now = System.currentTimeMillis();
    ChainState state = TNT_SUMO_CHAIN_STATES.get(playerId);

    if (state == null || now - state.lastHitMillis > TNT_SUMO_CHAIN_WINDOW_MILLIS) {
      state = new ChainState(1, now);
      TNT_SUMO_CHAIN_STATES.put(playerId, state);
      return 1;
    }

    state.chainHits += 1;
    state.lastHitMillis = now;
    return state.chainHits;
  }

  private static double calculateGroundedChainLiftBonus(int chainHits, double forceFactor) {
    if (chainHits < TNT_SUMO_CHAIN_LIFT_START_HIT) {
      return 0.0;
    }
    int extraHits = chainHits - TNT_SUMO_CHAIN_LIFT_START_HIT + 1;
    double rawBonus = extraHits * Math.max(0.0, TNT_SUMO_GROUNDED_CHAIN_LIFT) * forceFactor;
    return Math.min(0.85, rawBonus);
  }

  private static void clearChainHits(@NotNull UUID playerId) {
    TNT_SUMO_CHAIN_STATES.remove(playerId);
  }

  private static int updateAirborneStackHits(@NotNull UUID playerId) {
    long now = System.currentTimeMillis();
    AirborneStackState state = TNT_SUMO_AIRBORNE_STACK_STATES.get(playerId);

    if (state == null || now - state.lastHitMillis > TNT_SUMO_AIRBORNE_STACK_WINDOW_MILLIS) {
      state = new AirborneStackState(1, now);
      TNT_SUMO_AIRBORNE_STACK_STATES.put(playerId, state);
      return 1;
    }

    state.hits += 1;
    state.lastHitMillis = now;
    return state.hits;
  }

  private static double calculateAirborneOverflowMultiplier(int airborneStackHits) {
    if (airborneStackHits <= 1) {
      return 1.0;
    }

    double extra =
        (airborneStackHits - 1) * Math.max(0.0, TNT_SUMO_AIRBORNE_OVERFLOW_PER_EXTRA_HIT);
    double cappedExtra = Math.clamp(TNT_SUMO_AIRBORNE_OVERFLOW_MAX, 0.0, extra);
    return 1.0 + cappedExtra;
  }

  private static @NotNull Vector limitVelocity(
      @NotNull Vector velocity, double overflowMultiplier) {
    double overflow = Math.max(1.0, overflowMultiplier);
    double maxHorizontal = Math.max(0.0, TNT_SUMO_MAX_HORIZONTAL_VELOCITY) * overflow;
    double x = velocity.getX();
    double z = velocity.getZ();
    double horizontalLength = Math.sqrt((x * x) + (z * z));

    if (horizontalLength > maxHorizontal) {
      double scale = maxHorizontal / horizontalLength;
      x *= scale;
      z *= scale;
    }

    double maxUpward = Math.max(0.0, TNT_SUMO_MAX_UPWARD_VELOCITY) * overflow;
    double y = velocity.getY();
    y = Math.min(y, maxUpward);

    return new Vector(x, y, z);
  }

  private static MatchSettings.TntSumo.Explosion explosion() {
    return PracticeConfig.settings().getMatchSettings().getTntSumo().getExplosion();
  }

  private static boolean isTntOwner(@NotNull Player player, @NotNull TNTPrimed tnt) {
    UUID ownerId = BlockUtil.getMetadata(tnt, TNT_SUMO_TNT_OWNER, UUID.class);
    if (ownerId != null) {
      return ownerId.equals(player.getUniqueId());
    }

    Entity source = tnt.getSource();
    return source instanceof Player sourcePlayer
        && sourcePlayer.getUniqueId().equals(player.getUniqueId());
  }

  private static void rewardTntForMeleeHit(@NotNull Player attacker) {
    if (countTnt(attacker) >= TNT_LIMIT) {
      return;
    }

    PlayerUtil.returnItemToCurrentSlotOrInventory(attacker, getTntItemStack(attacker));
    attacker.updateInventory();

    Profile profile = ProfileManager.getInstance().getProfile(attacker);
    if (profile != null) {
      profile
          .getActionBar()
          .setMessage(
              TNT_ATTACK_REWARD_ACTIONBAR_ID,
              TNT_EARN_ACTIONBAR_MESSAGE,
              3,
              ActionBarPriority.NORMAL);
    }
  }

  private static @NotNull ItemStack getTntItemStack(@NotNull Player player) {
    for (ItemStack itemStack : player.getInventory().getStorageContents()) {
      if (itemStack != null && itemStack.getType().equals(Material.TNT)) {
        ItemStack clone = itemStack.clone();
        clone.setAmount(1);
        TNT_SUMO_PLAYER_TNT_CACHE.put(player.getUniqueId(), clone);
        return clone;
      }
    }

    ItemStack offHand = player.getInventory().getItemInOffHand();
    if (offHand.getType().equals(Material.TNT)) {
      ItemStack clone = offHand.clone();
      clone.setAmount(1);
      TNT_SUMO_PLAYER_TNT_CACHE.put(player.getUniqueId(), clone);
      return clone;
    }

    ItemStack cached = TNT_SUMO_PLAYER_TNT_CACHE.get(player.getUniqueId());
    if (cached != null) {
      return cached.clone();
    }

    return new ItemStack(Material.TNT, 1);
  }

  public void cleanup(Player player) {
    UUID uuid = player.getUniqueId();
    TNT_SUMO_CHAIN_STATES.remove(uuid);
    TNT_SUMO_AIRBORNE_STACK_STATES.remove(uuid);
    TNT_SUMO_PLAYER_TNT_CACHE.remove(uuid);
  }

  private static final class ChainState {
    private int chainHits;
    private long lastHitMillis;

    private ChainState(int chainHits, long lastHitMillis) {
      this.chainHits = chainHits;
      this.lastHitMillis = lastHitMillis;
    }
  }

  private static final class AirborneStackState {
    private int hits;
    private long lastHitMillis;

    private AirborneStackState(int hits, long lastHitMillis) {
      this.hits = hits;
      this.lastHitMillis = lastHitMillis;
    }
  }
}
