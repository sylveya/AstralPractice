package dev.lokspel.practice.manager.server;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.fight.match.enums.WeightClass;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.group.Group;
import dev.lokspel.practice.manager.profile.group.GroupManager;
import dev.lokspel.practice.util.Common;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ProfileLimitRunnable extends BukkitRunnable {

  public void begin() {
    ZonedDateTime zdt =
        LocalDate.now(TimeZone.getDefault().toZoneId())
            .atTime(LocalTime.of(23, 59, 59))
            .atZone(TimeZone.getDefault().toZoneId());

    long i2 = zdt.toInstant().toEpochMilli() - System.currentTimeMillis();
    long delayTicks = Math.max(1L, (i2 / 1000) * 20);
    long periodTicks = 20L * 60 * 60 * 24;

    this.runTaskTimerAsynchronously(AstralPractice.getInstance(), delayTicks, periodTicks);
  }

  @Override
  public void run() {
    for (Profile profile : ProfileManager.getInstance().getProfiles().values()) {
      Group group = profile.getGroup();

      profile.setRankedLeft(group != null ? group.getRankedLimit() : 0);
      profile.setUnrankedLeft(group != null ? group.getUnrankedLimit() : 0);
      profile.setEventStartLeft(group != null ? group.getEventStartLimit() : 0);
      profile.setPartyBroadcastLeft(group != null ? group.getPartyBroadcastLimit() : 0);
    }

    boolean hasGameLimits = false;
    for (Group group : GroupManager.getInstance().getGroups()) {
      if (group.getRankedLimit() > 0 || group.getUnrankedLimit() > 0) {
        hasGameLimits = true;
        break;
      }
    }

    String gamesResetMessage = PracticeConfig.language().getGamesReset();
    if (hasGameLimits && gamesResetMessage != null && !gamesResetMessage.isBlank()) {
      for (Player player : Bukkit.getOnlinePlayers()) {
        Common.sendMmMessage(
            player,
            gamesResetMessage
                .replace("%unranked%", WeightClass.UNRANKED.getMmName())
                .replace("%ranked%", WeightClass.RANKED.getMmName()));
      }
    }
  }
}
