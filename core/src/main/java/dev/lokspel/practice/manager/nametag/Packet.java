package dev.lokspel.practice.manager.nametag;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.score.ScoreFormat;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDisplayScoreboard;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerScoreboardObjective;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateScore;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class Packet {

  private Packet() {}

  public static void sendSpawnTextDisplay(
      Player viewer, int entityId, UUID entityUuid, Location location) {
    Vector3d position = new Vector3d(location.getX(), location.getY(), location.getZ());
    WrapperPlayServerSpawnEntity packet =
        new WrapperPlayServerSpawnEntity(
            entityId,
            Optional.of(entityUuid),
            EntityTypes.TEXT_DISPLAY,
            position,
            location.getPitch(),
            location.getYaw(),
            location.getYaw(),
            0,
            Optional.empty());

    PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
  }

  public static void sendMetadataTextDisplay(Player viewer, ClientTextDisplay display) {
    WrapperPlayServerEntityMetadata packet =
        new WrapperPlayServerEntityMetadata(display.getEntityId(), display.createMetadata());
    PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
  }

  public static void sendDestroyTextDisplay(Player viewer, int entityId) {
    WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(entityId);
    PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
  }

  public static void sendSetPassengers(
      Player viewer, int vehicleEntityId, int[] passengerEntityIds) {
    WrapperPlayServerSetPassengers packet =
        new WrapperPlayServerSetPassengers(vehicleEntityId, passengerEntityIds);
    PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
  }

  public static void sendCreateBelowName(Player player, String objectiveName) {
    if (player == null || !player.isOnline()) {
      return;
    }
    sendRemoveBelowName(player, objectiveName);

    WrapperPlayServerScoreboardObjective objective =
        new WrapperPlayServerScoreboardObjective(
            objectiveName,
            WrapperPlayServerScoreboardObjective.ObjectiveMode.CREATE,
            Component.empty(),
            WrapperPlayServerScoreboardObjective.RenderType.INTEGER);
    WrapperPlayServerDisplayScoreboard display =
        new WrapperPlayServerDisplayScoreboard(2, objectiveName);

    PacketEvents.getAPI().getPlayerManager().sendPacket(player, objective);
    PacketEvents.getAPI().getPlayerManager().sendPacket(player, display);
  }

  public static void sendRemoveBelowName(Player player, String objectiveName) {
    if (player == null || !player.isOnline()) {
      return;
    }
    WrapperPlayServerScoreboardObjective objective =
        new WrapperPlayServerScoreboardObjective(
            objectiveName,
            WrapperPlayServerScoreboardObjective.ObjectiveMode.REMOVE,
            Component.empty(),
            WrapperPlayServerScoreboardObjective.RenderType.INTEGER);

    PacketEvents.getAPI().getPlayerManager().sendPacket(player, objective);
  }

  public static void sendUpdateBelowName(
      Player viewer, String targetName, String objectiveName, int score, Component formatted) {
    if (viewer == null || !viewer.isOnline()) {
      return;
    }
    WrapperPlayServerUpdateScore packet =
        new WrapperPlayServerUpdateScore(
            targetName,
            WrapperPlayServerUpdateScore.Action.CREATE_OR_UPDATE_ITEM,
            objectiveName,
            score,
            null,
            ScoreFormat.fixedScore(formatted));

    PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
  }
}
