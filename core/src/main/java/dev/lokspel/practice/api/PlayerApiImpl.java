package dev.lokspel.practice.api;

import dev.lokspel.api.enums.DivisionName;
import dev.lokspel.api.enums.PlayerState;
import dev.lokspel.api.player.PlayerApi;
import dev.lokspel.api.utilities.PlayerNametag;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.group.Group;
import dev.lokspel.practice.util.LobbyNametagUtil;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerApiImpl implements PlayerApi {

  @Override
  public PlayerState getState(Player player) {
    if (player == null || !player.isOnline()) {
      return PlayerState.OFFLINE;
    }
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null || profile.getStatus() == null) {
      return PlayerState.OFFLINE;
    }
    return profile.getStatus().toPlayerState();
  }

  @Override
  public PlayerState getState(UUID uuid) {
    if (uuid == null) {
      return PlayerState.OFFLINE;
    }
    Player player = Bukkit.getPlayer(uuid);
    if (player == null || !player.isOnline()) {
      return PlayerState.OFFLINE;
    }
    return getState(player);
  }

  @Override
  public PlayerNametag getNametag(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return null;
    }
    return LobbyNametagUtil.getLobbyNametag(profile);
  }

  @Override
  public String getDivision(Player player, DivisionName divisionName) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null || profile.getStats().getDivision() == null) {
      return null;
    }
    return switch (divisionName) {
      case FULL -> profile.getStats().getDivision().getFullName();
      case SHORT -> profile.getStats().getDivision().getShortName();
    };
  }

  @Override
  public int getUnRankedLeft(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return -1;
    }
    return profile.getUnrankedLeft();
  }

  @Override
  public int getRankedLeft(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return -1;
    }
    return profile.getRankedLeft();
  }

  @Override
  public void resetUnRanked(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return;
    }

    Group group = profile.getGroup();
    if (group == null) {
      return;
    }

    profile.setUnrankedLeft(group.getUnrankedLimit());
  }

  @Override
  public void resetRanked(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return;
    }

    Group group = profile.getGroup();
    if (group == null) {
      return;
    }

    profile.setRankedLeft(group.getRankedLimit());
  }

  @Override
  public void addUnRanked(Player player, int amount) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return;
    }

    profile.setUnrankedLeft(profile.getUnrankedLeft() + amount);
  }

  @Override
  public void addRanked(Player player, int amount) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile == null) {
      return;
    }

    profile.setRankedLeft(profile.getRankedLeft() + amount);
  }
}
