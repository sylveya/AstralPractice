package dev.lokspel.practice.manager.profile;

import dev.lokspel.api.event.PlayerStateChangeEvent;
import dev.lokspel.practice.event.ProfileStatusChangeEvent;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.util.CustomKit;
import dev.lokspel.practice.manager.gui.guis.customladder.PlayerCustomKitSelector;
import dev.lokspel.practice.manager.gui.guis.profile.ProfileSettingsGui;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.lokspel.practice.manager.ladder.abstraction.playercustom.CustomLadderData;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsData;
import dev.lokspel.practice.manager.profile.enums.ProfilePrefixVisibility;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.profile.enums.ProfileWorldTime;
import dev.lokspel.practice.manager.profile.group.Group;
import dev.lokspel.practice.manager.profile.group.GroupManager;
import dev.lokspel.practice.manager.profile.statistics.ProfileStat;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.actionbar.ActionBar;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

@Getter
@Setter
public class Profile {

  // Identity
  private final UUID uuid;
  private final OfflinePlayer player;
  private final ProfileFile file;
  private final ProfileStat stats;
  private Group group;

  // Name display
  private Component prefix;
  private String nameTemplate;
  private NamedTextColor nameColor;
  private Component suffix;

  // Join timestamps
  private long firstJoin;
  private long lastJoin;

  // Online state
  private ProfileStatus status;
  private boolean spectatorMode;
  private boolean party;
  private boolean partyChat;
  private boolean hideSpectators;

  // Staff state
  private boolean staffMode;
  private boolean staffChat;
  private boolean hideFromPlayers;
  private Player followTarget;

  // Player settings
  private boolean duelRequest;
  private boolean autoQueue;
  private boolean sidebar;
  private boolean hidePlayers;
  private boolean partyInvites;
  private boolean allowSpectate;
  private boolean privateMessages;
  private ProfileWorldTime worldTime = ProfileWorldTime.DAY;
  private boolean flying;
  private ProfilePrefixVisibility prefixVisibility = ProfilePrefixVisibility.PREFIX_AND_SUFFIX;

  // Custom kits
  private int allowedCustomKits;
  private final Map<NormalLadder, Map<Integer, CustomKit>> unrankedCustomKits = new HashMap<>();
  private final Map<NormalLadder, Map<Integer, CustomKit>> rankedCustomKits = new HashMap<>();

  // Daily limits
  private final List<Profile> ignoredPlayers = new ArrayList<>();
  private int unrankedLeft;
  private int rankedLeft;
  private int eventStartLeft;
  private int partyBroadcastLeft;

  // Misc
  private RankedBan rankedBan = new RankedBan();
  private ProfileSettingsGui settingsGui;
  private ActionBar actionBar;

  // Cosmetics
  private CosmeticsData cosmeticsData = new CosmeticsData();

  // Custom ladder
  private PlayerCustomKitSelector playerCustomKitSelector;
  private final List<CustomLadder> customLadders = new ArrayList<>();
  private CustomLadder selectedCustomLadder;

  public Profile(UUID uuid, OfflinePlayer player) {
    this.uuid = uuid;
    // Never pin a live Player instance here; always resolve online player from UUID.
    this.player =
        (player instanceof Player)
            ? Bukkit.getOfflinePlayer(uuid)
            : Objects.requireNonNullElseGet(player, () -> Bukkit.getOfflinePlayer(uuid));
    this.status = ProfileStatus.OFFLINE;
    this.file = new ProfileFile(this);
    this.stats = new ProfileStat(this);
  }

  public Profile(UUID uuid) {
    this(uuid, Bukkit.getOfflinePlayer(uuid));
  }

  /**
   * Resolves the currently connected player for this profile by UUID. Returns null when the player
   * is offline.
   */
  public Player getOnlinePlayer() {
    return Bukkit.getPlayer(uuid);
  }

  /**
   * The name to record this profile under. A player who has never joined has no name yet, so the
   * uuid stands in for one.
   */
  public String getStoredName() {
    return player != null && player.getName() != null ? player.getName() : uuid.toString();
  }

  // Data persistence

  public synchronized void save() {
    ProfileData data = file.getProfileData();
    data.setRankedBan(rankedBan.setData(data.getRankedBan()));
    saveCustomLadders();
    stats.setData(false);
    file.setData();
  }

  private void saveCustomLadders() {
    for (CustomLadder customLadder : customLadders) {
      customLadder.setData();
    }
    if (selectedCustomLadder != null) {
      file.getProfileData().setSelectedCustomLadder(customLadders.indexOf(selectedCustomLadder));
    }
  }

  public synchronized void load() {
    stats.getLadderStats().clear();
    file.getData();
    stats.getData();

    rankedBan.getData(file.getProfileData().getRankedBan());

    loadCustomLadders();
  }

  private void loadCustomLadders() {
    ProfileData.PlayerCustomKits section = file.getProfileData().getPlayerCustomKits();
    if (section == null) {
      return;
    }

    customLadders.clear();
    if (section.hasLegacyLadder()) {
      loadLegacyCustomLadder(section);
    } else {
      for (Map.Entry<String, CustomLadderData> entry : section.getLadders().entrySet()) {
        if (!tryLoadCustomLadder(entry.getKey(), entry.getValue())) {
          break;
        }
      }
    }

    loadSelectedCustomLadder();
  }

  private boolean tryLoadCustomLadder(String key, CustomLadderData data) {
    try {
      int i = Integer.parseInt(key);
      if (i < 0 || i > 5) {
        return true;
      }

      customLadders.add(new CustomLadder(this, String.valueOf(i), i + 1, data));
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /** Moves the single custom ladder of older versions to key {@code 0}. */
  private void loadLegacyCustomLadder(ProfileData.PlayerCustomKits section) {
    CustomLadderData legacy = new CustomLadderData();
    legacy.setSettings(section.getLegacySettings());
    legacy.setKitData(section.getLegacyKitData());

    customLadders.add(new CustomLadder(this, "0", 1, legacy));
    file.getProfileData().setPlayerCustomKits(null);
    file.saveFile();
  }

  private void loadSelectedCustomLadder() {
    if (customLadders.isEmpty()) {
      return;
    }

    Integer index = file.getProfileData().getSelectedCustomLadder();
    if (index != null && index >= 0 && index < customLadders.size()) {
      selectedCustomLadder = customLadders.get(index);
    }
  }

  /**
   * Called when a player disconnects. Clears ephemeral references but keeps loaded data in memory.
   */
  public synchronized void onQuit() {
    settingsGui = null;
    playerCustomKitSelector = null;
    followTarget = null;
    Player online = getOnlinePlayer();
    if (online != null) {
      for (Player other : Bukkit.getOnlinePlayers()) {
        Profile otherProfile = ProfileManager.getInstance().getProfile(other);
        if (otherProfile != null && online.equals(otherProfile.getFollowTarget())) {
          otherProfile.setFollowTarget(null);
        }
      }
    }
    actionBar = null;
  }

  // Group management

  public void checkGroup() {
    Player online = getOnlinePlayer();
    if (online == null || !online.isOnline()) {
      return;
    }

    Group newGroup = GroupManager.getInstance().getGroup(online);
    if (newGroup == null) {
      Common.sendConsoleMmMessage(
          "<red>Warning: Could not determine group for "
              + online.getName()
              + ". Assigning default (lowest weighted) group to them.");
      return;
    }

    if (group == newGroup) {
      return;
    }

    try {
      setGroup(newGroup);
    } catch (Exception e) {
      Common.sendConsoleMmMessage(
          "<red>Failed to set group for " + online.getName() + "! Error: " + e.getMessage());
    }
  }

  public int getCustomKitPerm() {
    Player onlinePlayer = getOnlinePlayer();
    if (onlinePlayer == null) {
      return 0;
    }

    if (group != null) {
      return group.getModifiableKitLimit();
    }
    return -1;
  }

  public void setGroup(Group group) throws IllegalArgumentException {
    if (group == null) {
      throw new IllegalArgumentException("Group cannot be null");
    }

    this.group = group;
    unrankedLeft = group.getUnrankedLimit();
    rankedLeft = group.getRankedLimit();
    eventStartLeft = group.getEventStartLimit();
    partyBroadcastLeft = group.getPartyBroadcastLimit();

    refreshPartyLimit();
    syncCustomLadderCount();

    // Invalidate the selector so it gets recreated on next access (lazy-loading)
    playerCustomKitSelector = null;
  }

  private void refreshPartyLimit() {
    Player onlinePlayer = getOnlinePlayer();
    if (onlinePlayer == null) {
      return;
    }

    Party partyObj = PartyManager.getInstance().getParty(onlinePlayer);
    if (partyObj != null && onlinePlayer.equals(partyObj.getLeader())) {
      partyObj.refreshMaxPlayerLimitForLeader();
    }
  }

  private void syncCustomLadderCount() {
    while (customLadders.size() < group.getCustomKitLimit()) {
      customLadders.add(
          new CustomLadder(this, String.valueOf(customLadders.size()), customLadders.size() + 1));
    }
    while (customLadders.size() > group.getCustomKitLimit()) {
      customLadders.removeLast();
    }
  }

  // Lazy-loaded accessors

  public PlayerCustomKitSelector getPlayerCustomKitSelector() {
    if (playerCustomKitSelector == null) {
      playerCustomKitSelector = new PlayerCustomKitSelector(this);
    }
    return playerCustomKitSelector;
  }

  public ActionBar getActionBar() {
    if (actionBar == null) {
      actionBar = new ActionBar(this);
    }
    return actionBar;
  }

  // Setters with side effects

  public void setSelectedCustomLadder(CustomLadder customLadder) {
    if (customLadder == null) {
      throw new IllegalArgumentException("Custom ladder cannot be null.");
    }
    if (!customLadders.contains(customLadder)) {
      throw new IllegalArgumentException("Custom ladder not found in profile.");
    }

    selectedCustomLadder = customLadder;
  }

  public void setStatus(ProfileStatus status) {
    ProfileStatus previous = this.status;
    this.status = status;

    Bukkit.getPluginManager().callEvent(new ProfileStatusChangeEvent(this, previous, status));

    Player player = getOnlinePlayer();
    if (player != null && previous != status) {
      Bukkit.getPluginManager()
          .callEvent(
              new PlayerStateChangeEvent(player, previous.toPlayerState(), status.toPlayerState()));
    }

    invalidateRematchIfLeftLobby(previous, status);
  }

  private void invalidateRematchIfLeftLobby(ProfileStatus previous, ProfileStatus status) {
    boolean wasIdle = previous == ProfileStatus.LOBBY || previous == ProfileStatus.SPECTATE;
    boolean isActive =
        status != ProfileStatus.LOBBY
            && status != ProfileStatus.SPECTATE
            && status != ProfileStatus.OFFLINE;

    if (!wasIdle || !isActive) {
      return;
    }

    Player online = getOnlinePlayer();
    if (online != null && online.isOnline()) {
      MatchManager.getInstance().invalidateRematchByPlayer(online);
    }
  }
}
