package dev.lokspel.practice.manager.leaderboard.hologram;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendManager;
import dev.lokspel.practice.manager.division.Division;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.hologram.HologramSetupManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.leaderboard.Leaderboard;
import dev.lokspel.practice.manager.leaderboard.types.LbSecondaryType;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.group.Group;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract base class for leaderboard holograms.
 *
 * <p>Architecture:
 *
 * <ul>
 *   <li>Each hologram maintains a List of HologramLines
 *   <li>Smart updates only modify changed content (no flicker)
 *   <li>Thread-safe with atomic state tracking
 *   <li>Auto-recovery for externally removed text displays
 * </ul>
 */
@Getter
public abstract class Hologram {

  // Configuration
  private static final String NULL_LINE_FORMAT =
      PracticeConfig.settings().getLeaderboard().getHologram().getFormat().getNullLine();
  private static final double DEFAULT_SPACING = 0.25;

  // Core properties
  protected final String name;
  protected Location baseLocation;
  protected boolean enabled;

  @Setter protected HologramType hologramType;
  @Setter protected LbSecondaryType leaderboardType;
  @Setter protected HologramRunnable hologramRunnable;
  @Setter protected int showStat;

  // State tracking
  protected Leaderboard currentLb;
  protected Ladder currentLadder;
  protected final List<HologramLine> lines = new ArrayList<>();
  private final AtomicBoolean isUpdating = new AtomicBoolean(false);
  private HologramState currentState = HologramState.UNINITIALIZED;

  protected Hologram(String name, Location baseLocation, HologramType hologramType) {
    this.name = name;
    this.baseLocation = baseLocation.clone().subtract(0, 2, 0);
    this.hologramType = hologramType;
    this.hologramRunnable = new HologramRunnable(this);
    this.showStat = 10;
    this.leaderboardType = LbSecondaryType.ELO;
  }

  protected Hologram(String name, HologramType hologramType) {
    this.name = name;
    this.hologramType = hologramType;
    this.hologramRunnable = new HologramRunnable(this);
    this.leaderboardType = LbSecondaryType.ELO;
    this.getData();

    if (!this.isReadyToEnable()) {
      enabled = false;
    }
  }

  public abstract void getAbstractData(HologramData data);

  public abstract void setAbstractData(HologramData data);

  public abstract boolean isReadyToEnable();

  public abstract Leaderboard getNextLeaderboard();

  public void getData() {
    Map<String, HologramData> holograms = BackendManager.data().getHolograms();
    HologramData data = holograms != null ? holograms.get(name) : null;
    if (data == null) {
      data = new HologramData();
    }

    enabled = data.getEnabled() != null && data.getEnabled();

    if (data.getLbType() != null) {
      leaderboardType = LbSecondaryType.valueOf(data.getLbType());
    }

    if (data.getLocation() != null) {
      baseLocation = data.getLocation();
    }

    showStat = data.getShowStat() != null ? data.getShowStat() : 10;
    getAbstractData(data);
  }

  public void setData() {
    if (name == null) {
      return;
    }

    HologramData data = new HologramData();
    data.setEnabled(enabled);
    data.setType(hologramType.name());
    data.setLbType(leaderboardType.name());
    data.setShowStat(showStat);

    if (baseLocation != null) {
      data.setLocation(baseLocation);
    }

    setAbstractData(data);

    Map<String, HologramData> holograms = BackendManager.data().getHolograms();
    if (holograms == null) {
      holograms = new LinkedHashMap<>();
      BackendManager.data().setHolograms(holograms);
    }
    holograms.put(name, data);
    BackendManager.save();
  }

  /**
   * Moves the hologram to a new player-facing location.
   *
   * <p>The internal base location is stored two blocks below the visible hologram.
   */
  public void moveTo(@NotNull Location location) {
    this.baseLocation = location.clone().subtract(0, 2, 0);
  }

  /** Despawns all hologram lines and clears state. */
  public synchronized void despawn() {
    if (!Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(AstralPractice.getInstance(), this::despawn);
      return;
    }

    lines.forEach(HologramLine::despawn);
    lines.clear();

    currentState = HologramState.DESPAWNED;
    currentLb = null;
    currentLadder = null;
  }

  /** Smart update that modifies content without flickering. */
  private synchronized void updateSmartly(
      @NotNull List<String> textLines, @NotNull List<Double> spacings) {
    if (!Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler()
          .runTask(AstralPractice.getInstance(), () -> updateSmartly(textLines, spacings));
      return;
    }

    if (baseLocation == null || baseLocation.getWorld() == null || textLines.isEmpty()) {
      return;
    }

    List<Location> positions = calculatePositions(textLines.size(), spacings);

    if (lines.isEmpty()) {
      // First spawn - create all lines
      spawnNewLines(textLines, positions);
    } else {
      // Update existing lines
      updateExistingLines(textLines, positions);
    }
  }

  private static final double BASE_HEIGHT_OFFSET = 2.2;

  private List<Location> calculatePositions(int count, List<Double> spacings) {
    List<Location> positions = new ArrayList<>();
    Location loc = baseLocation.clone().add(0, BASE_HEIGHT_OFFSET, 0);

    for (int i = 0; i < count; i++) {
      positions.add(loc.clone());
      double spacing = i < spacings.size() ? spacings.get(i) : DEFAULT_SPACING;
      loc.add(0, spacing, 0);
    }

    return positions;
  }

  private void spawnNewLines(List<String> textLines, List<Location> positions) {
    for (int i = 0; i < textLines.size(); i++) {
      String text = textLines.get(i).isEmpty() ? " " : textLines.get(i);
      HologramLine line = new HologramLine();
      line.spawn(positions.get(i), text);
      lines.add(line);
    }
  }

  private void updateExistingLines(List<String> textLines, List<Location> positions) {
    int minCount = Math.min(lines.size(), textLines.size());

    // Update existing lines
    for (int i = 0; i < minCount; i++) {
      String text = textLines.get(i).isEmpty() ? " " : textLines.get(i);
      lines.get(i).updateText(text);
    }

    // Add new lines if needed
    if (textLines.size() > lines.size()) {
      for (int i = lines.size(); i < textLines.size(); i++) {
        String text = textLines.get(i).isEmpty() ? " " : textLines.get(i);
        HologramLine line = new HologramLine();
        line.spawn(positions.get(i), text);
        lines.add(line);
      }
    }
    // Remove extra lines if needed
    else if (textLines.size() < lines.size()) {
      for (int i = lines.size() - 1; i >= textLines.size(); i--) {
        lines.remove(i).despawn();
      }
    }
  }

  /** Main update method - handles leaderboard changes and content updates. */
  public synchronized void updateContent() {
    if (!isUpdating.compareAndSet(false, true)) {
      return;
    }

    try {
      if (baseLocation == null || baseLocation.getWorld() == null) {
        return;
      }

      Leaderboard leaderboard = getNextLeaderboard();

      if (leaderboard == null) {
        handleNoLeaderboard();
        return;
      }

      if (leaderboard.isEmpty()) {
        handleEmptyLeaderboard();
        return;
      }

      if (hologramType == HologramType.LADDER_DYNAMIC) {
        currentLadder = leaderboard.getLadder();
      }

      currentLb = leaderboard;
      List<String> textLines = buildTextLines(leaderboard);
      List<Double> spacings = buildSpacings(textLines.size(), leaderboard);

      updateSmartly(textLines, spacings);
      currentState = HologramState.DISPLAYING_LEADERBOARD;

    } finally {
      isUpdating.set(false);
    }
  }

  private void handleNoLeaderboard() {
    if (lines.isEmpty() && currentState != HologramState.SETUP_MODE) {
      setSetupHologram(SetupHologramType.SETUP);
    }
  }

  private void handleEmptyLeaderboard() {
    if (lines.isEmpty() && currentState != HologramState.NO_DISPLAY) {
      setSetupHologram(SetupHologramType.NO_DISPLAY);
    }
  }

  private List<String> buildTextLines(@NotNull Leaderboard leaderboard) {
    List<String> configLines = getConfigLines(leaderboard);

    if (configLines.isEmpty()) {
      return List.of("§cNo config lines");
    }

    Collections.reverse(configLines);

    List<String> expandedLines = new ArrayList<>();
    List<String> placementStrings = buildPlacementStrings(leaderboard);

    for (String line : configLines) {
      if (line.contains("%top%")) {
        expandedLines.addAll(placementStrings);
      } else {
        expandedLines.add(Common.mmToNormal(line));
      }
    }

    return expandedLines.isEmpty() ? List.of("§cNo data") : expandedLines;
  }

  private List<String> getConfigLines(Leaderboard leaderboard) {
    return switch (leaderboard.getMainType()) {
      case GLOBAL -> new ArrayList<>(leaderboard.getSecondaryType().getGlobalLines());
      case FFA_GLOBAL -> {
        List<String> ffaLines = new ArrayList<>(leaderboard.getSecondaryType().getGlobalLines());
        ffaLines.replaceAll(
            line ->
                line.replace("<gold>", "<gold>FFA Global ").replace("Win Streak", "Kill Streak"));
        yield ffaLines;
      }
      case LADDER -> {
        List<String> ladderLines = new ArrayList<>(leaderboard.getSecondaryType().getLadderLines());
        if (leaderboard.getLadder() != null) {
          String ladderName = leaderboard.getLadder().getName();
          String displayName = leaderboard.getLadder().getDisplayName();
          ladderLines.replaceAll(
              line ->
                  line.replace("%ladder_name%", ladderName)
                      .replace("%ladder_displayName%", displayName));
        }
        yield ladderLines;
      }
    };
  }

  private List<Double> buildSpacings(int lineCount, Leaderboard leaderboard) {
    double standardSpacing = leaderboard.getSecondaryType().getLineSpacing();
    double titleSpacing = leaderboard.getSecondaryType().getTitleLineSpacing();

    return IntStream.range(0, lineCount)
        .mapToObj(i -> i == lineCount - 1 ? titleSpacing : standardSpacing)
        .toList();
  }

  private List<String> buildPlacementStrings(Leaderboard leaderboard) {
    Map<OfflinePlayer, Integer> playerStats = leaderboard.getList();
    List<OfflinePlayer> topPlayers =
        playerStats.keySet().stream()
            .filter(p -> p.getName() != null && ProfileManager.getInstance().getProfile(p) != null)
            .limit(showStat)
            .toList();

    List<String> placements = new ArrayList<>();

    for (int i = 0; i < showStat; i++) {
      int rank = showStat - i;
      int playerIndex = showStat - 1 - i;

      if (playerIndex < topPlayers.size()) {
        placements.add(formatPlayerEntry(topPlayers.get(playerIndex), playerStats, rank));
      } else {
        placements.add(NULL_LINE_FORMAT.replace("%number%", String.valueOf(rank)));
      }
    }

    return placements;
  }

  private String formatPlayerEntry(
      OfflinePlayer player, Map<OfflinePlayer, Integer> stats, int rank) {
    Profile profile = ProfileManager.getInstance().getProfile(player);

    if (profile == null) {
      return NULL_LINE_FORMAT.replace("%number%", String.valueOf(rank));
    }

    Group group = profile.getGroup();
    Division division = profile.getStats().getDivision();

    return StringUtil.cc(
        leaderboardType
            .getFormat()
            .replace("%placement%", String.valueOf(rank))
            .replace("%score%", String.valueOf(stats.get(player)))
            .replace("%player%", player.getName())
            .replace(
                "%division%", division != null ? Common.mmToNormal(division.getFullName()) : "")
            .replace(
                "%division_short%",
                division != null ? Common.mmToNormal(division.getShortName()) : "")
            .replace("%group%", group != null ? group.getDisplayName() : ""));
  }

  /** Shows a setup/placeholder hologram. */
  public synchronized void setSetupHologram(@NotNull SetupHologramType type) {
    if (baseLocation == null || baseLocation.getWorld() == null) {
      return;
    }

    // Don't replace existing content
    if (!lines.isEmpty()) {
      currentState =
          type == SetupHologramType.SETUP ? HologramState.SETUP_MODE : HologramState.NO_DISPLAY;
      return;
    }

    List<String> setupLines =
        switch (type) {
          case SETUP -> {
            currentState = HologramState.SETUP_MODE;
            yield List.of(
                PracticeConfig.settings()
                    .getLeaderboard()
                    .getHologram()
                    .getFormat()
                    .getSetupHolo()
                    .getTitle(),
                StringUtil.cc(
                    PracticeConfig.settings()
                        .getLeaderboard()
                        .getHologram()
                        .getFormat()
                        .getSetupHolo()
                        .getLine()
                        .replace("%name%", name)));
          }
          case NO_DISPLAY -> {
            currentState = HologramState.NO_DISPLAY;
            yield List.of(
                PracticeConfig.settings()
                    .getLeaderboard()
                    .getHologram()
                    .getFormat()
                    .getNothingToDisplay());
          }
        };

    List<Double> spacings = setupLines.stream().map(l -> DEFAULT_SPACING).toList();
    updateSmartly(setupLines, spacings);
  }

  /** Deletes the hologram completely. */
  public synchronized void deleteHologram(boolean removeFromManager) {
    despawn();

    if (removeFromManager) {
      hologramRunnable.cancel(false);
      HologramManager.getInstance().getHolograms().remove(this);
      HologramSetupManager.getInstance().removeHologramGuis(this);
      Map<String, HologramData> holograms = BackendManager.data().getHolograms();
      if (holograms != null) {
        holograms.remove(name);
      }
      BackendManager.save();
    }
  }

  public void setEnabled(boolean enabled) {
    if (enabled && isReadyToEnable()) {
      this.enabled = true;
      hologramRunnable.begin();
    } else {
      this.enabled = false;
      hologramRunnable.cancel(true);

      // Show setup placeholder immediately when hologram is disabled.
      despawn();
      setSetupHologram(SetupHologramType.SETUP);
    }

    setData();
    updateGuis();
  }

  private void updateGuis() {
    GuiManager.getInstance().searchGui(GuiType.Hologram_Summary).update();

    var setupGuis = HologramSetupManager.getInstance().getHologramSetupGuis();
    if (setupGuis.containsKey(this)) {
      setupGuis.get(this).get(GuiType.Hologram_Main).update();
      if (setupGuis.get(this).containsKey(GuiType.Hologram_Ladder)) {
        setupGuis.get(this).get(GuiType.Hologram_Ladder).update();
      }
    }
  }
}
