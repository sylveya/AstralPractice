package dev.lokspel.practice.manager.queue.runnables;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.queue.Queue;
import org.bukkit.scheduler.BukkitRunnable;

public class RankedSearchRunnable extends SearchRunnable {

  private static final int RANGE_INCREASE =
      PracticeConfig.settings().getQueue().getRanked().getEloRangeIncrease();
  private static final int ELO_RANGE_TIME =
      PracticeConfig.settings().getQueue().getRanked().getEloRangeTime();
  private int range = PracticeConfig.settings().getQueue().getRanked().getEloRangeIncrease();

  public RankedSearchRunnable(final Queue queue) {
    super(queue, 0, 20L * ELO_RANGE_TIME, false);

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

                int selfElo = queue.getProfile().getStats().getLadderStat(ladder).getElo();
                int queueElo = q.getProfile().getStats().getLadderStat(ladder).getElo();

                if ((selfElo - range) <= queueElo && (selfElo + range) >= queueElo) {
                  queue.startMatch(q, ladder);

                  this.cancel();
                  return;
                }
              }
            }
          }
        }.runTaskTimer(AstralPractice.getInstance(), 0, 20L);
  }

  @Override
  public void run() {
    if (queueManager.getQueue(queue.getPlayer()) == null || queue.getQueuedLadders().isEmpty()) {
      this.cancel();
      return;
    }

    this.range += RANGE_INCREASE;

    int minElo = Integer.MAX_VALUE;
    for (NormalLadder ladder : queue.getQueuedLadders()) {
      int ladderElo = queue.getProfile().getStats().getLadderStat(ladder).getElo();
      minElo = Math.min(minElo, ladderElo);
    }

    if (minElo == Integer.MAX_VALUE) {
      minElo = 0;
    }

    this.sendMsg(minElo);
  }

  private void sendMsg(int elo) {
    this.updateQueueActionBar(
        PracticeConfig.language()
            .getQueues()
            .getEloRange()
            .replace("%from%", String.valueOf(elo - range))
            .replace("%to%", String.valueOf(elo + range)));
  }
}
