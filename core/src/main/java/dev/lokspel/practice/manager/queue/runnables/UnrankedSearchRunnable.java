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

  private final List<Division> acceptableDivisions = new ArrayList<>();
  private final Division currentDivision;
  private Division nextDivision = null;
  private Division previousDivision = null;

  public UnrankedSearchRunnable(final Queue queue) {
    super(queue, SEARCH_IN_OWN_DIVISION * 20L, SEARCH_IN_NEXT_DIVISION * 20L, false);

    boolean ladderMode =
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
    if (currentDivision != null) {
      this.acceptableDivisions.add(currentDivision);
    }
    this.sendMsg();

    this.searching =
        new BukkitRunnable() {
          @Override
          public void run() {
            for (Queue q : queueManager.getQueues()) {
              if (q == queue || q.getPlayer() == queue.getPlayer()) {
                continue;
              }

              if (queue.getProfile().getIgnoredPlayers().contains(q.getProfile())
                  || q.getProfile().getIgnoredPlayers().contains(queue.getProfile())) {
                continue;
              }

              if (q.isRanked()) {
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

                if (acceptableDivisions.contains(targetDivision)) {
                  this.cancel();
                  queue.startMatch(q, ladder);
                  return;
                }
              }
            }
          }
        }.runTaskTimer(AstralPractice.getInstance(), 0, 20L);
  }

  @Override
  public void run() {
    if (currentDivision == null) {
      return;
    }

    Division nextDivision2;
    if (nextDivision == null) {
      nextDivision2 = DivisionManager.getInstance().getNextDivision(currentDivision);
    } else {
      nextDivision2 = DivisionManager.getInstance().getNextDivision(nextDivision);
    }

    if (nextDivision2 != null) {
      nextDivision = nextDivision2;
      if (!acceptableDivisions.contains(nextDivision)) {
        acceptableDivisions.add(nextDivision);
      }
    }

    Division previousDivision2;
    if (previousDivision == null) {
      previousDivision2 = DivisionManager.getInstance().getPreviousDivision(currentDivision);
    } else {
      previousDivision2 = DivisionManager.getInstance().getPreviousDivision(previousDivision);
    }

    if (previousDivision2 != null) {
      previousDivision = previousDivision2;
      if (!acceptableDivisions.contains(previousDivision)) {
        acceptableDivisions.add(previousDivision);
      }
    }

    this.sendMsg();
  }

  private void sendMsg() {
    if (currentDivision == null) {
      return;
    }

    if (acceptableDivisions.size() == 1) {
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
