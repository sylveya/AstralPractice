package dev.lokspel.practice.manager.fight.util;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.events.duel.brackets.Brackets;
import dev.lokspel.practice.manager.fight.event.events.duel.interfaces.DuelFight;
import dev.lokspel.practice.manager.fight.event.interfaces.Event;
import dev.lokspel.practice.manager.fight.ffa.FfaManager;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.server.ServerManager;
import dev.lokspel.practice.manager.server.WorldEnum;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.Nullable;

public class EntityHiderListener implements PacketListener, Listener {

  private static EntityHiderListener instance;

  public static EntityHiderListener getInstance() {
    if (instance == null) {
      instance = new EntityHiderListener();
    }
    return instance;
  }

  protected EntityHiderListener() {
    Bukkit.getPluginManager().registerEvents(this, AstralPractice.getInstance());
    PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.NORMAL);
  }

  private final Set<UUID> effectTo = ConcurrentHashMap.newKeySet();
  private final ConcurrentHashMap<UUID, AtomicInteger> allowedParticlePackets =
      new ConcurrentHashMap<>();
  private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<ParticleBurstPermit>>
      allowedParticleBursts = new ConcurrentHashMap<>();

  private final ConcurrentHashMap<Integer, Location> entityLocations = new ConcurrentHashMap<>();

  public void allowNextParticlePackets(Player player, int packetCount) {
    if (player == null || packetCount <= 0) {
      return;
    }

    allowedParticlePackets
        .computeIfAbsent(player.getUniqueId(), ignored -> new AtomicInteger())
        .addAndGet(packetCount);
  }

  public void allowNextParticleBurst(
      Player player, double x, double y, double z, int packetCount, long ttlMillis, double radius) {
    if (player == null || packetCount <= 0 || ttlMillis <= 0L || radius <= 0.0D) {
      return;
    }

    allowedParticleBursts
        .computeIfAbsent(player.getUniqueId(), ignored -> new CopyOnWriteArrayList<>())
        .add(
            new ParticleBurstPermit(
                x, y, z, radius * radius, System.currentTimeMillis() + ttlMillis, packetCount));
  }

  private boolean consumeAllowedParticlePacket(Player player) {
    AtomicInteger allowance = allowedParticlePackets.get(player.getUniqueId());
    if (allowance == null) {
      return false;
    }

    int left = allowance.decrementAndGet();
    if (left <= 0) {
      allowedParticlePackets.remove(player.getUniqueId());
    }
    return true;
  }

  private boolean consumeAllowedParticleBurst(
      Player player, WrapperPlayServerParticle particleWrapper) {
    CopyOnWriteArrayList<ParticleBurstPermit> permits =
        allowedParticleBursts.get(player.getUniqueId());
    if (permits == null || permits.isEmpty()) {
      return false;
    }

    Vector3d position = particleWrapper.getPosition();
    long now = System.currentTimeMillis();
    boolean consumed = false;

    for (ParticleBurstPermit permit : permits) {
      if (permit.isExpired(now)) {
        permits.remove(permit);
        continue;
      }

      if (!permit.matches(position)) {
        continue;
      }

      if (permit.consume()) {
        consumed = true;
      }

      if (permit.isDepleted()) {
        permits.remove(permit);
      }

      if (consumed) {
        break;
      }
    }

    if (permits.isEmpty()) {
      allowedParticleBursts.remove(player.getUniqueId());
    }

    return consumed;
  }

  private boolean checkPlayer(Player player) {
    if (!ServerManager.getInstance().getInWorld().containsKey(player)) {
      return false;
    }

    WorldEnum worldEnum = ServerManager.getInstance().getInWorld().get(player);
    if (worldEnum == null) {
      return false;
    }

    switch (worldEnum) {
      case LOBBY:
      case OTHER:
        return false;

      default:
        break;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);
    switch (profile.getStatus()) {
      case MATCH:
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match != null && !match.getLadder().isBuild()) {
          return true;
        }
      // fall through
      case EVENT:
        Event event = EventManager.getInstance().getEventByPlayer(player);
        if (event instanceof Brackets) {
          return true;
        }
      // fall through
      case SPECTATE:
        Match spectateMatch = MatchManager.getInstance().getLiveMatchBySpectator(player);
        if (spectateMatch != null && !spectateMatch.getLadder().isBuild()) {
          return true;
        }
        Event spectateEvent = EventManager.getInstance().getEventBySpectator(player);
        if (spectateEvent instanceof Brackets) {
          return true;
        }
      // fall through
      default:
        return false;
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerMove(PlayerMoveEvent e) {
    Location from = e.getFrom();
    Location to = e.getTo();
    if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
      return;
    }

    Player player = e.getPlayer();
    int entityId = player.getEntityId();

    if (this.checkPlayer(player)) {
      entityLocations.put(entityId, to);
    } else {
      entityLocations.remove(entityId);
    }
  }

  public void clearPlayer(Player player) {
    if (player == null) {
      return;
    }
    entityLocations.remove(player.getEntityId());
    effectTo.remove(player.getUniqueId());
    allowedParticlePackets.remove(player.getUniqueId());
    allowedParticleBursts.remove(player.getUniqueId());
  }

  @Override
  public void onPacketSend(PacketSendEvent e) {
    @Nullable Player player = e.getPlayer();

    // noinspection
    if (player == null) {
      return;
    }

    UUID uuid = player.getUniqueId();

    if (!this.checkPlayer(player)) {
      effectTo.remove(uuid);
      entityLocations.remove(player.getEntityId());
      allowedParticlePackets.remove(uuid);
      allowedParticleBursts.remove(uuid);
      return;
    }

    if (e.getPacketType() instanceof PacketType.Play.Server serverPacket) {
      switch (serverPacket) {
        case SPAWN_ENTITY -> {
          WrapperPlayServerSpawnEntity event = new WrapperPlayServerSpawnEntity(e);

          Entity entity =
              SpigotConversionUtil.getEntityById(player.getWorld(), event.getEntityId());
          if (entity instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
              if (!player.canSee(shooter)) {
                e.setCancelled(true);
              }
            }
          }
        }
        case EFFECT -> {
          if (!effectTo.contains(uuid)) {
            e.setCancelled(true);
            return;
          }

          effectTo.remove(uuid);
        }
        case PARTICLE -> {
          WrapperPlayServerParticle particleWrapper = new WrapperPlayServerParticle(e);
          if (!consumeAllowedParticleBurst(player, particleWrapper)
              && !consumeAllowedParticlePacket(player)) {
            e.setCancelled(true);
          }
        }
        case SOUND_EFFECT -> {
          WrapperPlayServerSoundEffect soundWrapper = new WrapperPlayServerSoundEffect(e);
          Vector3d pos = soundWrapper.getPosition();

          Location location = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ());

          int closestEntityId = -1;
          double closestDistanceSquared = Double.MAX_VALUE;

          for (Map.Entry<Integer, Location> entry : entityLocations.entrySet()) {
            if (location.getWorld() != entry.getValue().getWorld()) {
              continue;
            }

            double distanceSquared = entry.getValue().distanceSquared(location);

            if (distanceSquared < closestDistanceSquared) {
              closestDistanceSquared = distanceSquared;
              closestEntityId = entry.getKey();
            }
          }

          if (closestEntityId != -1) {
            Entity closestEntity =
                SpigotConversionUtil.getEntityById(player.getWorld(), closestEntityId);
            if (closestEntity instanceof Player target) {
              if (!player.canSee(target)) {
                e.setCancelled(true);
              }
            }
          }
        }
        case ENTITY_EFFECT -> {
          WrapperPlayServerEntityEffect event = new WrapperPlayServerEntityEffect(e);
          Entity entity =
              SpigotConversionUtil.getEntityById(player.getWorld(), event.getEntityId());
          if (entity instanceof Player target) {
            if (!player.canSee(target)) {
              e.setCancelled(true);
            }
          }
        }
        default -> {}
      }
    }
  }

  private record ParticleBurstPermit(
      double positionX,
      double positionY,
      double positionZ,
      double radiusSquared,
      long expiresAtMillis,
      AtomicInteger remaining) {

    private ParticleBurstPermit(
        double x, double y, double z, double radiusSquared, long expiresAtMillis, int packetCount) {
      this(x, y, z, radiusSquared, expiresAtMillis, new AtomicInteger(packetCount));
    }

    private boolean isExpired(long now) {
      return now > expiresAtMillis;
    }

    private boolean matches(Vector3d position) {
      double dx = position.getX() - positionX;
      double dy = position.getY() - positionY;
      double dz = position.getZ() - positionZ;
      return (dx * dx) + (dy * dy) + (dz * dz) <= radiusSquared;
    }

    private boolean consume() {
      return remaining.decrementAndGet() >= 0;
    }

    private boolean isDepleted() {
      return remaining.get() <= 0;
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onPotionSplash(PotionSplashEvent e) {
    if (!(e.getEntity().getShooter() instanceof Player player)) {
      return;
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);
    ProfileStatus profileStatus = profile.getStatus();
    switch (profileStatus) {
      case MATCH:
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) {
          return;
        }

        for (LivingEntity entity : e.getAffectedEntities()) {
          if (!(entity instanceof Player livingPlayer)) {
            continue;
          }

          if (!match.getPlayers().contains(livingPlayer)) {
            e.setIntensity(entity, 0);
          }
        }

        if (!match.getLadder().isBuild()) {
          for (Player p : match.getPlayers()) {
            effectTo.add(p.getUniqueId());
          }
          for (Player p : match.getSpectators()) {
            effectTo.add(p.getUniqueId());
          }
        }
        break;
      case FFA:
        Ffa ffa = FfaManager.getInstance().getFfaByPlayer(player);
        if (ffa == null) {
          return;
        }

        for (LivingEntity entity : e.getAffectedEntities()) {
          if (!ffa.getPlayers().containsKey((Player) entity)) {
            e.setIntensity(entity, 0);
          }
        }
        break;
      case EVENT:
        Event event = EventManager.getInstance().getEventByPlayer(player);
        if (!(event instanceof Brackets brackets)) {
          return;
        }

        DuelFight duelFight = brackets.getFight(player);
        if (duelFight == null) {
          return;
        }

        for (LivingEntity entity : e.getAffectedEntities()) {
          if (!duelFight.getPlayers().contains((Player) entity)) {
            e.setIntensity(entity, 0);
          }

          for (Player p : duelFight.getPlayers()) {
            effectTo.add(p.getUniqueId());
          }
          for (Player p : duelFight.getSpectators()) {
            effectTo.add(p.getUniqueId());
          }
        }
        break;

      default:
        break;
    }
  }
}
