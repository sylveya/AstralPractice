package dev.lokspel.practice.manager.party;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BroadcastTask extends BukkitRunnable {

  private final Party party;
  @Getter private boolean running = false;
  @Getter private int count;

  public BroadcastTask(Party party) {
    this.party = party;
  }

  public void begin() {
    count = 0;
    running = true;
    party.setBroadcastParty(true);

    this.runTaskTimerAsynchronously(
        AstralPractice.getInstance(),
        0,
        PracticeConfig.settings().getParty().getPublicBroadcastTime() * 20L);
  }

  @Override
  public void cancel() {
    if (running) {
      running = false;
      party.setBroadcastParty(false);

      Bukkit.getScheduler().cancelTask(this.getTaskId());
      party.setBroadcastTask(new BroadcastTask(party));
    }
  }

  @Override
  public void run() {
    if (count > PracticeConfig.settings().getParty().getBroadcastTimes()) {
      cancel();
      return;
    }

    String broadcastMsg =
        PracticeConfig.language()
            .getParty()
            .getBroadcastMsg()
            .replace("%player%", party.getLeader().getName())
            .replace("%members%", String.valueOf(party.getMembers().size()))
            .replace("%maxPlayerLimit%", String.valueOf(party.getMaxPlayerLimit()));

    for (Player online : Bukkit.getOnlinePlayers()) {
      Profile profile = ProfileManager.getInstance().getProfile(online);
      switch (profile.getStatus()) {
        case MATCH:
        case FFA:
        case EVENT:
          continue;

        default:
          break;
      }

      Party playerParty = PartyManager.getInstance().getParty(online);

      if (playerParty != null
          && playerParty.equals(party)
          && !playerParty.getLeader().equals(online)) {
        return;
      }

      Common.sendMmMessage(online, broadcastMsg);
    }

    count++;
  }
}
