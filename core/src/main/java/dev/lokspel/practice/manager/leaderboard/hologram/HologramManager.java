package dev.lokspel.practice.manager.leaderboard.hologram;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.manager.backend.BackendManager;
import dev.lokspel.practice.manager.gui.GuiManager;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.hologram.HologramSetupManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.leaderboard.hologram.holograms.FfaGlobalHologram;
import dev.lokspel.practice.manager.leaderboard.hologram.holograms.GlobalHologram;
import dev.lokspel.practice.manager.leaderboard.hologram.holograms.LadderDynamicHologram;
import dev.lokspel.practice.manager.leaderboard.hologram.holograms.LadderStaticHologram;
import dev.lokspel.practice.manager.leaderboard.types.LbSecondaryType;
import dev.lokspel.practice.util.Common;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import org.bukkit.Bukkit;

/**
 * Manages all leaderboard holograms in the plugin. Handles loading, saving, creation, and lifecycle
 * management.
 */
@Getter
public class HologramManager {

  private static final int STARTUP_DELAY_TICKS = 20;
  private static final int STARTUP_STAGGER_TICKS = 10;
  private static final double DUPLICATE_DISTANCE_THRESHOLD = 2.0;

  private static HologramManager instance;

  private final List<Hologram> holograms = new ArrayList<>();
  private final List<LbSecondaryType> lbSecondaryTypes = List.of(LbSecondaryType.values());

  private HologramManager() {}

  public static HologramManager getInstance() {
    if (instance == null) {
      instance = new HologramManager();
    }
    return instance;
  }

  /** Finds a hologram by name (case-insensitive). */
  public Optional<Hologram> findHologram(String name) {
    return holograms.stream().filter(h -> h.getName().equalsIgnoreCase(name)).findFirst();
  }

  /**
   * Finds a hologram by name, or returns {@code null} if none exists.
   *
   * @deprecated Use {@link #findHologram(String)} instead
   */
  @Deprecated
  public Hologram getHologram(String name) {
    return findHologram(name).orElse(null);
  }

  /** Creates and registers a new hologram. */
  public void createHologram(Hologram hologram) {
    holograms.add(hologram);
    hologram.setSetupHologram(SetupHologramType.SETUP);

    HologramSetupManager.getInstance().buildHologramSetupGuis(hologram);
    GuiManager.getInstance().searchGui(GuiType.Hologram_Summary).update();
  }

  /** Loads all holograms from configuration. */
  public void loadHolograms() {
    Map<String, HologramData> data = BackendManager.data().getHolograms();
    if (data == null) {
      HologramSetupManager.getInstance().loadGuis();
      startHologramsWithDelay();
      return;
    }

    List.copyOf(data.keySet()).forEach(this::loadHologram);

    HologramSetupManager.getInstance().loadGuis();
    startHologramsWithDelay();
  }

  /** Loads a single hologram from configuration. */
  private void loadHologram(String name) {
    try {
      HologramData data = BackendManager.data().getHolograms().get(name);
      HologramType type = HologramType.valueOf(data.getType());

      Hologram hologram = createHologramByType(name, type);
      if (hologram == null) {
        return;
      }

      if (hologram.getBaseLocation() != null && isDuplicateLocation(hologram)) {
        Common.sendConsoleMmMessage(
            "<red>Warning: Hologram '" + name + "' has duplicate location! Disabling.");
        hologram.setEnabled(false);
      }

      holograms.add(hologram);
    } catch (Exception e) {
      Common.sendConsoleMmMessage("<red>Error loading hologram " + name + "!");
      e.printStackTrace();
    }
  }

  /** Creates a hologram instance based on type. */
  private Hologram createHologramByType(String name, HologramType type) {
    return switch (type) {
      case GLOBAL -> new GlobalHologram(name);
      case FFA_GLOBAL -> new FfaGlobalHologram(name);
      case LADDER_STATIC -> new LadderStaticHologram(name);
      case LADDER_DYNAMIC -> new LadderDynamicHologram(name);
    };
  }

  /** Starts holograms with staggered delays to prevent lag spikes. */
  private void startHologramsWithDelay() {
    for (int i = 0; i < holograms.size(); i++) {
      Hologram hologram = holograms.get(i);
      long delay = STARTUP_DELAY_TICKS + ((long) i * STARTUP_STAGGER_TICKS);

      Bukkit.getScheduler()
          .runTaskLater(
              AstralPractice.getInstance(),
              () -> {
                if (hologram.isEnabled()) {
                  hologram.getHologramRunnable().begin();
                } else {
                  hologram.setSetupHologram(SetupHologramType.SETUP);
                }
              },
              delay);
    }
  }

  /** Checks if a hologram's location conflicts with existing holograms. */
  private boolean isDuplicateLocation(Hologram newHologram) {
    if (newHologram.getBaseLocation() == null || newHologram.getBaseLocation().getWorld() == null) {
      return false;
    }

    return holograms.stream()
        .filter(h -> h.getBaseLocation() != null)
        .filter(h -> h.getBaseLocation().getWorld() != null)
        .filter(
            h -> h.getBaseLocation().getWorld().equals(newHologram.getBaseLocation().getWorld()))
        .anyMatch(
            h ->
                h.getBaseLocation().distance(newHologram.getBaseLocation())
                    < DUPLICATE_DISTANCE_THRESHOLD);
  }

  /** Saves all hologram data without despawning. */
  public void saveHolograms() {
    holograms.forEach(Hologram::setData);
  }

  /** Saves and despawns all holograms (for shutdown). */
  public void saveAndDespawnHolograms() {
    holograms.forEach(
        hologram -> {
          hologram.setData();
          hologram.deleteHologram(false);
        });
  }

  /** Removes a ladder from all holograms that reference it. */
  public void removeLadder(NormalLadder ladder) {
    holograms.forEach(
        hologram -> {
          if (hologram instanceof LadderStaticHologram staticHolo
              && staticHolo.getLadder() == ladder) {
            staticHolo.setLadder(null);
            hologram.setEnabled(false);
          } else if (hologram instanceof LadderDynamicHologram dynamicHolo
              && dynamicHolo.getLadders().contains(ladder)) {
            dynamicHolo.getLadders().remove(ladder);
            if (dynamicHolo.getLadders().isEmpty()) {
              hologram.setEnabled(false);
            }
          }
        });
  }

  /** Gets the secondary leaderboard types supported by the given hologram type. */
  public List<LbSecondaryType> getLbSecondaryTypes(HologramType hologramType) {
    if (hologramType == HologramType.FFA_GLOBAL) {
      return List.of(
          LbSecondaryType.KILLS, LbSecondaryType.DEATHS, LbSecondaryType.BEST_WIN_STREAK);
    }
    return lbSecondaryTypes;
  }

  /** Gets the next leaderboard type in the cycle for a specific hologram. */
  public LbSecondaryType getNextType(Hologram hologram) {
    List<LbSecondaryType> types = getLbSecondaryTypes(hologram.getHologramType());
    if (hologram.getLeaderboardType() == null || !types.contains(hologram.getLeaderboardType())) {
      return types.get(0);
    }

    int index = types.indexOf(hologram.getLeaderboardType());
    return types.get((index + 1) % types.size());
  }

  /** Gets the next leaderboard type in the cycle. */
  public LbSecondaryType getNextType(LbSecondaryType current) {
    if (current == null) {
      return lbSecondaryTypes.get(0);
    }

    int index = lbSecondaryTypes.indexOf(current);
    return lbSecondaryTypes.get((index + 1) % lbSecondaryTypes.size());
  }
}
