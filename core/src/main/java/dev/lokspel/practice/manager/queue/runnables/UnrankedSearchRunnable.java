package dev.lokspel.practice.manager.queue.runnables;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.division.Division;
import dev.lokspel.practice.manager.division.DivisionManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.queue.Queue;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.scheduler.BukkitRunnable;

public class UnrankedSearchRunnable extends SearchRunnable {

  private static final int SEARCH_IN_OWN_DIVISION =
      PracticeConfig.settings()
          .getQueue()
          .getUnranked()
          .getDivisionSearch()
          .getSettings()
          .getSearchInOwnDivisions();
  private static final int SEARCH_IN_NEXT_DIVISION =
      PracticeConfig.settings()
          .getQueue()
          .getUnranked()
          .getDivisionSearch()
          .getSettings()
          .getExtendDivisionSearch();

  private final boolean ladderMode;
  private final Division currentDivision;
  private Division nextDivision = null;
  private Division previousDivision = null;
  private int expansionStep = 0;

  public UnrankedSearchRunnable(final Queue queue) {
    super(queue, SEARCH_IN_OWN_DIVISION * 20L, SEARCH_IN_NEXT_DIVISION * 20L, false);

    this.ladderMode =
        PracticeConfig.settings()
            .getQueue()
            .getUnranked()
            .getDivisionSearch()
            .getSettings()
            .isLadderMode();
    Division initial = null;
    if (ladderMode && queue.getLadder() != null) {
      initial =
          DivisionManager.getInstance().getLadderDivision(queue.getProfile(), queue.getLadder());
    }
    if (initial == null) {
      initial = queue.getProfile().getStats().getDivision();
    }
    this.currentDivision = initial;
    this.sendMsg();

    this.searching =
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Queue q : queueManager.getQueues()) {
              if (!queue.canMatchWith(q)) {
                continue;
              }

              for (NormalLadder ladder : getShuffledQueuedLadders()) {
                if (!q.isQueued(ladder)) {
                  continue;
                }

                Division targetDivision =
                    ladderMode
                        ? DivisionManager.getInstance().getLadderDivision(q.getProfile(), ladder)
                        : q.getProfile().getStats().getDivision();

                if (targetDivision != null
                    && getAcceptableDivisions(ladder).contains(targetDivision)) {
                  this.cancel();
                  queue.startMatch(q, ladder);
                  return;
                }
              }
            }
          }
        }.runTaskTimer(AstralPractice.getInstance(), 0, 20L);
  }

  /**
   * Resolves the base division for the player on a specific ladder.
   *
   * @param ladder the ladder to resolve the division for
   * @return the player's division for the ladder, or their global division
   */
  public Division getBaseDivision(NormalLadder ladder) {
    if (ladderMode && ladder != null) {
      Division div = DivisionManager.getInstance().getLadderDivision(queue.getProfile(), ladder);
      if (div != null) {
        return div;
      }
    }
    return queue.getProfile().getStats().getDivision();
  }

  /**
   * Computes the list of acceptable divisions for the given ladder based on the current expansion.
   *
   * @param ladder the ladder to evaluate
   * @return acceptable divisions for the given ladder
   */
  public List<Division> getAcceptableDivisions(NormalLadder ladder) {
    Division base = getBaseDivision(ladder);
    if (base == null) {
      return List.of();
    }

    List<Division> acceptable = new ArrayList<>();
    acceptable.add(base);

    Division currentUp = base;
    Division currentDown = base;

    for (int i = 0; i < expansionStep; i++) {
      if (currentUp != null) {
        currentUp = DivisionManager.getInstance().getNextDivision(currentUp);
        if (currentUp != null && !acceptable.contains(currentUp)) {
          acceptable.add(currentUp);
        }
      }

      if (currentDown != null) {
        currentDown = DivisionManager.getInstance().getPreviousDivision(currentDown);
        if (currentDown != null && !acceptable.contains(currentDown)) {
          acceptable.add(currentDown);
        }
      }
    }

    return acceptable;
  }

  @Override
  public void run() {
    if (currentDivision == null) {
      return;
    }

    expansionStep++;

    Division nextDivision2;
    if (nextDivision == null) {
      nextDivision2 = DivisionManager.getInstance().getNextDivision(currentDivision);
    } else {
      nextDivision2 = DivisionManager.getInstance().getNextDivision(nextDivision);
    }

    if (nextDivision2 != null) {
      nextDivision = nextDivision2;
    }

    Division previousDivision2;
    if (previousDivision == null) {
      previousDivision2 = DivisionManager.getInstance().getPreviousDivision(currentDivision);
    } else {
      previousDivision2 = DivisionManager.getInstance().getPreviousDivision(previousDivision);
    }

    if (previousDivision2 != null) {
      previousDivision = previousDivision2;
    }

    this.sendMsg();
  }

  private void sendMsg() {
    if (currentDivision == null) {
      return;
    }

    if (expansionStep == 0) {
      this.updateQueueActionBar(
          PracticeConfig.language()
              .getQueues()
              .getUnranked()
              .getSearchingOwnDivision()
              .replace("%division_fullName%", currentDivision.getFullName())
              .replace("%division_shortName%", currentDivision.getShortName()));
    } else if (previousDivision != null && nextDivision == null) {
      this.updateQueueActionBar(
          PracticeConfig.language()
              .getQueues()
              .getUnranked()
              .getSearchingInRange()
              .replace("%from_fullName%", previousDivision.getFullName())
              .replace("%from_shortName%", previousDivision.getShortName())
              .replace("%to_fullName%", currentDivision.getFullName())
              .replace("%to_shortName%", currentDivision.getShortName()));
    } else if (previousDivision == null && nextDivision != null) {
      this.updateQueueActionBar(
          PracticeConfig.language()
              .getQueues()
              .getUnranked()
              .getSearchingInRange()
              .replace("%from_fullName%", currentDivision.getFullName())
              .replace("%from_shortName%", currentDivision.getShortName())
              .replace("%to_fullName%", nextDivision.getFullName())
              .replace("%to_shortName%", nextDivision.getShortName()));
    } else if (previousDivision != null) {
      this.updateQueueActionBar(
          PracticeConfig.language()
              .getQueues()
              .getUnranked()
              .getSearchingInRange()
              .replace("%from_fullName%", previousDivision.getFullName())
              .replace("%from_shortName%", previousDivision.getShortName())
              .replace("%to_fullName%", nextDivision.getFullName())
              .replace("%to_shortName%", nextDivision.getShortName()));
    }
  }
}
