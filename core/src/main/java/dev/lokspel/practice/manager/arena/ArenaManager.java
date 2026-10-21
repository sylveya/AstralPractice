package dev.lokspel.practice.manager.arena;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.command.arena.arguments.CreateArg;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.data.DataFiles;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.lokspel.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.gui.setup.arena.ArenaGuiSetupManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.Cuboid;
import dev.lokspel.practice.util.startup.StartUpCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

@Getter
public class ArenaManager implements Listener {

  private static ArenaManager instance;

  public static ArenaManager getInstance() {
    if (instance == null) {
      instance = new ArenaManager();
    }
    return instance;
  }

  public static final boolean LOAD_CHUNKS = PracticeConfig.settings().getArena().isLoadChunks();
  public static final Set<Long> LOADED_CHUNK_KEYS = ConcurrentHashMap.newKeySet();

  private final List<DisplayArena> arenaList = new CopyOnWriteArrayList<>();
  private final Map<Cuboid, BasicArena> arenaCuboids = new HashMap<>();

  private ArenaManager() {}

  public DisplayArena getArena(String arenaName) {
    for (DisplayArena arena : this.arenaList) {
      if (arena.getName().equalsIgnoreCase(arenaName)) {
        return arena;
      }
    }
    return null;
  }

  public Arena getNormalArena(String arenaName) {
    for (Arena arena : this.getNormalArenas()) {
      if (arena.getName().equalsIgnoreCase(arenaName)) {
        return arena;
      }
    }
    return null;
  }

  public List<Arena> getNormalArenas() {
    List<Arena> arenas = new ArrayList<>();
    for (DisplayArena arena : this.arenaList) {
      if (arena instanceof Arena) {
        arenas.add((Arena) arena);
      }
    }
    return arenas;
  }

  public FfaArena getFfaArena(String arenaName) {
    for (FfaArena arena : this.getFfaArenas()) {
      if (arena.getName().equalsIgnoreCase(arenaName)) {
        return arena;
      }
    }
    return null;
  }

  public List<FfaArena> getFfaArenas() {
    List<FfaArena> arenas = new ArrayList<>();
    for (DisplayArena arena : this.arenaList) {
      if (arena instanceof FfaArena) {
        arenas.add((FfaArena) arena);
      }
    }
    return arenas;
  }

  public List<Arena> getEnabledArenas() {
    List<Arena> enabledArenas = new ArrayList<>();
    for (Arena arena : this.getNormalArenas()) {
      if (arena.isEnabled()) {
        enabledArenas.add(arena);
      }
    }
    return enabledArenas;
  }

  public List<FfaArena> getEnabledFfaArenas() {
    List<FfaArena> enabledArenas = new ArrayList<>();
    for (FfaArena arena : this.getFfaArenas()) {
      if (arena.isEnabled()) {
        enabledArenas.add(arena);
      }
    }
    return enabledArenas;
  }

  public void loadArenas(final StartUpCallback boolCallback) {
    Bukkit.getScheduler()
        .runTaskAsynchronously(
            AstralPractice.getInstance(),
            () -> {
              for (String file : DataFiles.list("arenas")) {
                ArenaData data = DataFiles.read(file, ArenaData.class);
                if (data == null) {
                  continue;
                }

                String name = data.getName();
                if (name == null) {
                  Common.sendConsoleMmMessage(
                      "<red>Skipping arena file '" + file + "': missing or null arena name.");
                  continue;
                }

                String invalidReason = CreateArg.checkName(name);
                if (invalidReason != null) {
                  Common.sendConsoleMmMessage(
                      "<red>Skipping arena file '"
                          + file
                          + "': invalid arena name '"
                          + name
                          + "'. "
                          + invalidReason);
                  continue;
                }

                try {
                  ArenaType type = ArenaType.valueOf(String.valueOf(data.getType()));
                  // An arena keeps its data in the file named after it.
                  ArenaData preloaded = file.equals(ArenaFile.path(name)) ? data : null;

                  Bukkit.getScheduler()
                      .runTask(
                          AstralPractice.getInstance(),
                          () -> {
                            if (type == ArenaType.FFA) {
                              arenaList.add(new FfaArena(name, preloaded));
                            } else {
                              arenaList.add(new Arena(name, type, preloaded));
                            }
                          });
                } catch (IllegalArgumentException e) {
                  Common.sendConsoleMmMessage(
                      "<red>Can't load arena "
                          + name
                          + " because the type format isn't set correctly in the config.");
                }
              }

              Bukkit.getScheduler()
                  .runTask(AstralPractice.getInstance(), boolCallback::onLoadingDone);
            });
  }

  public void saveArenas() {
    for (DisplayArena arena : arenaList) {
      arena.setData();
    }
  }

  public void removeLadder(NormalLadder ladder) {
    for (DisplayArena arena : arenaList) {
      if (arena.getAssignedLadders().contains(ladder)) {
        // Remember this arena so we can restore the assignment when re-enabled
        ladder.getPreviouslyAssignedArenas().add(arena.getName());

        arena.getAssignedLadders().remove(ladder);

        ArenaGuiSetupManager.getInstance()
            .getArenaSetupGuis()
            .get(arena)
            .get(GuiType.Arena_Ladders_Single)
            .update();
      }

      if (arena instanceof FfaArena) {
        Ffa ffa = ((FfaArena) arena).getFfa();

        for (Map.Entry<Player, NormalLadder> ffaPlayer :
            new ArrayList<>(ffa.getPlayers().entrySet())) {
          if (ffaPlayer.getValue() == ladder) {
            ffa.removePlayer(ffaPlayer.getKey());
            Common.sendMmMessage(
                ffaPlayer.getKey(), PracticeConfig.language().getFfa().getLadderDisabledRemoved());
          }
        }
      }
    }
  }
}
