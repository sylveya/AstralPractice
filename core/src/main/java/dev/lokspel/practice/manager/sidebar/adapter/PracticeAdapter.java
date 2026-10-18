package dev.lokspel.practice.manager.sidebar.adapter;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.sidebar.LadderLines;
import dev.lokspel.practice.config.sidebar.SidebarConfig;
import dev.lokspel.practice.manager.fight.event.EventManager;
import dev.lokspel.practice.manager.fight.event.events.duel.interfaces.DuelEvent;
import dev.lokspel.practice.manager.fight.event.events.duel.interfaces.DuelFight;
import dev.lokspel.practice.manager.fight.event.events.ffa.lms.Lms;
import dev.lokspel.practice.manager.fight.event.events.ffa.oitc.Oitc;
import dev.lokspel.practice.manager.fight.event.events.ffa.splegg.Splegg;
import dev.lokspel.practice.manager.fight.event.events.onevsall.juggernaut.Juggernaut;
import dev.lokspel.practice.manager.fight.event.events.onevsall.tnttag.TntTag;
import dev.lokspel.practice.manager.fight.event.interfaces.Event;
import dev.lokspel.practice.manager.fight.ffa.FfaManager;
import dev.lokspel.practice.manager.fight.ffa.game.Ffa;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.Round;
import dev.lokspel.practice.manager.fight.match.enums.TeamEnum;
import dev.lokspel.practice.manager.fight.match.enums.WeightClass;
import dev.lokspel.practice.manager.fight.match.type.duel.Duel;
import dev.lokspel.practice.manager.fight.match.type.partyffa.PartyFfa;
import dev.lokspel.practice.manager.fight.match.type.playersvsplayers.partysplit.PartySplit;
import dev.lokspel.practice.manager.fight.match.type.playersvsplayers.partyvsparty.PartyVsParty;
import dev.lokspel.practice.manager.fight.match.util.MatchUtil;
import dev.lokspel.practice.manager.fight.match.util.TeamUtil;
import dev.lokspel.practice.manager.fight.util.stats.Statistic;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.enums.LadderType;
import dev.lokspel.practice.manager.ladder.type.Boxing;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.group.Group;
import dev.lokspel.practice.manager.queue.CustomKitQueueManager;
import dev.lokspel.practice.manager.queue.Queue;
import dev.lokspel.practice.manager.queue.QueueManager;
import dev.lokspel.practice.manager.queue.runnables.CustomKitSearchRunnable;
import dev.lokspel.practice.manager.spectator.SpectatorManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.NameFormatUtil;
import dev.lokspel.practice.util.PapiUtil;
import dev.lokspel.practice.util.StringUtil;
import dev.lokspel.practice.util.interfaces.Spectatable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PracticeAdapter implements SidebarAdapter {

  private static String fallbackToZero(String value) {
    return value != null ? value : "0";
  }

  private static Component displayName(Player target) {
    Profile targetProfile = ProfileManager.getInstance().getProfile(target);
    if (targetProfile == null) {
      return Component.text(target.getName());
    }

    // Read the SIDEBAR-NAME-FORMAT config option to determine how names should be displayed.
    String format = PracticeConfig.sidebar().getSidebarNameFormat();
    return switch (format.toUpperCase()) {
      case "NAME_ONLY" -> NameFormatUtil.resolveName(targetProfile, target.getName(), target, null);
      case "PREFIX_NAME" ->
          NameFormatUtil.resolvePrefix(targetProfile, target)
              .append(
                  NameFormatUtil.resolveName(
                      targetProfile,
                      target.getName(),
                      target,
                      NameFormatUtil.extractTrailingColor(
                          NameFormatUtil.resolvePrefix(targetProfile, target))));
      case "NAME_SUFFIX" ->
          NameFormatUtil.resolveName(targetProfile, target.getName(), target, null)
              .append(NameFormatUtil.resolveSuffix(targetProfile, target));
      default -> NameFormatUtil.resolveFullName(targetProfile, target.getName(), target);
    };
  }

  private static List<String> orEmpty(List<String> lines) {
    return lines != null ? lines : List.of();
  }

  private static List<String> ladderLines(Map<String, LadderLines> ladders, Match match) {
    LadderLines lines =
        ladders != null ? ladders.get(match.getLadder().getName().toUpperCase()) : null;
    if (lines == null) {
      return List.of();
    }

    return orEmpty(
        switch (match.getType()) {
          case DUEL -> lines.getDuel();
          case PARTY_FFA -> lines.getPartyFfa();
          case PARTY_SPLIT -> lines.getPartySplit();
          case PARTY_VS_PARTY -> lines.getPartyVsParty();
        });
  }

  private static List<String> eventLines(Map<String, List<String>> events, Event event) {
    return events != null ? orEmpty(events.get(event.getType().name().toUpperCase())) : List.of();
  }

  private static Component parseColoredText(String text) {
    if (text == null || text.isEmpty()) {
      return Component.empty();
    }

    return Common.deserializeMiniMessage(text);
  }

  @Override
  public Component getTitle(Player player) {
    return PapiUtil.runThroughFormat(player, PracticeConfig.sidebar().getTitle());
  }

  @Override
  public List<Component> getLines(Player player) {
    SidebarConfig config = PracticeConfig.sidebar();
    List<Component> sidebar = new ArrayList<>();
    Profile profile = ProfileManager.getInstance().getProfile(player);

    switch (profile.getStatus()) {
      case LOBBY, EDITOR, STAFF_MODE, CUSTOM_EDITOR ->
          buildLobbyLines(player, config, profile, sidebar);
      case QUEUE -> {
        // After a match is found the player leaves the queue but keeps the QUEUE status while
        // waiting for the teleport (e.g. totem animation delay). Fall back to the lobby scoreboard
        // so it doesn't render an empty queue board until the player is teleported into the arena.
        if (!buildQueueLines(player, config, profile, sidebar)) {
          buildLobbyLines(player, config, profile, sidebar);
        }
      }
      case MATCH -> buildMatchLines(player, config, sidebar);
      case FFA -> buildFfaLines(player, config, sidebar);
      case EVENT -> buildEventLines(player, config, sidebar);
      case SPECTATE -> buildSpectateLines(player, config, sidebar);

      default -> {}
    }

    buildFooterLines(player, config, profile, sidebar);
    return sidebar;
  }

  private void buildLobbyLines(
      Player player, SidebarConfig config, Profile profile, List<Component> sidebar) {
    Party party = PartyManager.getInstance().getParty(player);

    if (party == null) {
      for (String line : orEmpty(config.getLobby().getNormal())) {
        Component component =
            PapiUtil.runThroughFormat(player, line)
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%onlinePlayers%")
                        .replacement(String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%inFightPlayers%")
                        .replacement(
                            String.valueOf(MatchManager.getInstance().getPlayerInMatchSize()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%inQueuePlayer%")
                        .replacement(String.valueOf(QueueManager.getInstance().getQueues().size()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%division%")
                        .replacement(
                            profile.getStats().getDivision() != null
                                ? profile.getStats().getDivision().getComponentFullName()
                                : Component.empty())
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%division_short%")
                        .replacement(
                            profile.getStats().getDivision() != null
                                ? profile.getStats().getDivision().getComponentShortName()
                                : Component.empty())
                        .build());
        sidebar.add(AdapterUtil.replaceArenaPlaceholders(component));
      }
    } else {
      for (String line : orEmpty(config.getLobby().getParty())) {
        Component component =
            PapiUtil.runThroughFormat(player, line)
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%onlinePlayers%")
                        .replacement(String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%inFightPlayers%")
                        .replacement(
                            String.valueOf(MatchManager.getInstance().getPlayerInMatchSize()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%inQueuePlayer%")
                        .replacement(String.valueOf(QueueManager.getInstance().getQueues().size()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%partyLeader%")
                        .replacement(displayName(party.getLeader()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%partyLeaderNameOnly%")
                        .replacement(AdapterUtil.getSidebarNameOnly(party.getLeader()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%maxMember%")
                        .replacement(String.valueOf(party.getMaxPlayerLimit()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%members%")
                        .replacement(String.valueOf(party.getMembers().size()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%division%")
                        .replacement(
                            profile.getStats().getDivision() != null
                                ? profile.getStats().getDivision().getComponentFullName()
                                : Component.empty())
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%division_short%")
                        .replacement(
                            profile.getStats().getDivision() != null
                                ? profile.getStats().getDivision().getComponentShortName()
                                : Component.empty())
                        .build());
        sidebar.add(AdapterUtil.replaceArenaPlaceholders(component));
      }
    }
  }

  private boolean buildQueueLines(
      Player player, SidebarConfig config, Profile profile, List<Component> sidebar) {
    Queue queue = QueueManager.getInstance().getQueue(player);
    Event event = EventManager.getInstance().getEventByPlayer(player);
    CustomKitQueueManager.HostedCustomKitQueue hostedCustomKitQueue =
        CustomKitQueueManager.getInstance().getHostedQueue(player);
    CustomKitSearchRunnable customKitJoinSearch =
        CustomKitQueueManager.getInstance().getJoinSearch(player);

    if (queue != null) {
      boolean multiQueue = queue.getQueuedLadders().size() > 1;
      String ladderDisplayName =
          queue.getLadder() != null ? queue.getLadder().getDisplayName() : "Unknown";
      String multiQueueLabel = "";
      String multiQueueCurrent = "";

      if (multiQueue) {
        multiQueueLabel =
            PracticeConfig.settings().getQueue().getMulti().getSidebar().getMultipleLabel();
        if (multiQueueLabel.isEmpty()) {
          multiQueueLabel = PracticeConfig.language().getQueues().getMulti().getSidebarMultiple();
        }
        if (multiQueueLabel.isEmpty()) {
          multiQueueLabel = "Multiple Ladders";
        }

        String currentFormat =
            PracticeConfig.settings().getQueue().getMulti().getSidebar().getCurrentLadderFormat();
        if (currentFormat.isEmpty()) {
          currentFormat = "<white>Current: <gold>%ladder%";
        }

        multiQueueCurrent = currentFormat.replace("%ladder%", queue.getCyclingSidebarLadder());
        ladderDisplayName = multiQueueLabel;
      }

      List<String> duelQueueLines =
          multiQueue && config.getLobby().getDuelQueueMulti() != null
              ? config.getLobby().getDuelQueueMulti()
              : config.getLobby().getDuelQueue();
      for (String line : orEmpty(duelQueueLines)) {
        sidebar.add(
            PapiUtil.runThroughFormat(player, line)
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%onlinePlayers%")
                        .replacement(String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%inFightPlayers%")
                        .replacement(
                            String.valueOf(MatchManager.getInstance().getPlayerInMatchSize()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%inQueuePlayer%")
                        .replacement(String.valueOf(QueueManager.getInstance().getQueues().size()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%weightClass%")
                        .replacement(
                            queue.isRanked()
                                ? WeightClass.RANKED.getName()
                                : WeightClass.UNRANKED.getName())
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%ladderDisplayName%")
                        .replacement(parseColoredText(ladderDisplayName))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%multiQueueLabel%")
                        .replacement(parseColoredText(multiQueueLabel))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%multiQueueCurrentLadder%")
                        .replacement(parseColoredText(multiQueueCurrent))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%elapsedTime%")
                        .replacement(queue.getFormattedDuration())
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%division%")
                        .replacement(
                            profile.getStats().getDivision() != null
                                ? profile.getStats().getDivision().getComponentFullName()
                                : Component.empty())
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%division_short%")
                        .replacement(
                            profile.getStats().getDivision() != null
                                ? profile.getStats().getDivision().getComponentShortName()
                                : Component.empty())
                        .build()));
      }
      return true;
    } else if (hostedCustomKitQueue != null) {
      for (String line : orEmpty(config.getLobby().getCustomKitQueue().getHosting())) {
        sidebar.add(
            PapiUtil.runThroughFormat(player, line)
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%onlinePlayers%")
                        .replacement(String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%inFightPlayers%")
                        .replacement(
                            String.valueOf(MatchManager.getInstance().getPlayerInMatchSize()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%inQueuePlayer%")
                        .replacement(
                            String.valueOf(
                                QueueManager.getInstance().getQueues().size()
                                    + CustomKitQueueManager.getInstance().getHostedQueues().size()
                                    + CustomKitQueueManager.getInstance().getJoinSearches().size()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%weightClass%")
                        .replacement(WeightClass.UNRANKED.getName())
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%ladderDisplayName%")
                        .replacement(hostedCustomKitQueue.customLadder().getDisplayName())
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%elapsedTime%")
                        .replacement(hostedCustomKitQueue.getElapsedSeconds() + "s")
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%division%")
                        .replacement(
                            profile.getStats().getDivision() != null
                                ? profile.getStats().getDivision().getComponentFullName()
                                : Component.empty())
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%division_short%")
                        .replacement(
                            profile.getStats().getDivision() != null
                                ? profile.getStats().getDivision().getComponentShortName()
                                : Component.empty())
                        .build()));
      }
      return true;
    } else if (customKitJoinSearch != null) {
      for (String line : orEmpty(config.getLobby().getCustomKitQueue().getJoinSearch())) {
        sidebar.add(
            PapiUtil.runThroughFormat(player, line)
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%onlinePlayers%")
                        .replacement(String.valueOf(Bukkit.getOnlinePlayers().size()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%inFightPlayers%")
                        .replacement(
                            String.valueOf(MatchManager.getInstance().getPlayerInMatchSize()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%inQueuePlayer%")
                        .replacement(
                            String.valueOf(
                                QueueManager.getInstance().getQueues().size()
                                    + CustomKitQueueManager.getInstance().getHostedQueues().size()
                                    + CustomKitQueueManager.getInstance().getJoinSearches().size()))
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%weightClass%")
                        .replacement(WeightClass.UNRANKED.getName())
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%ladderDisplayName%")
                        .replacement("Any Hosted Kit")
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%elapsedTime%")
                        .replacement(customKitJoinSearch.getElapsedSeconds() + "s")
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%division%")
                        .replacement(
                            profile.getStats().getDivision() != null
                                ? profile.getStats().getDivision().getComponentFullName()
                                : Component.empty())
                        .build())
                .replaceText(
                    TextReplacementConfig.builder()
                        .match("%division_short%")
                        .replacement(
                            profile.getStats().getDivision() != null
                                ? profile.getStats().getDivision().getComponentShortName()
                                : Component.empty())
                        .build()));
      }
      return true;
    } else if (event != null) {
      String eventQueueTimeLeft =
          event.getQueueRunnable() != null ? event.getQueueRunnable().getFormattedTime() : null;

      if (eventQueueTimeLeft != null) {
        for (String line : orEmpty(config.getLobby().getEventQueue().getStarting())) {
          sidebar.add(
              PapiUtil.runThroughFormat(player, line)
                  .replaceText(
                      TextReplacementConfig.builder()
                          .match("%eventName%")
                          .replacement(event.getType().getName())
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .match("%maxPlayer%")
                          .replacement(String.valueOf(event.getType().getMaxPlayer()))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .match("%player%")
                          .replacement(String.valueOf(event.getPlayers().size()))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .match("%timeLeft%")
                          .replacement(fallbackToZero(eventQueueTimeLeft))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .match("%division%")
                          .replacement(
                              profile.getStats().getDivision() != null
                                  ? profile.getStats().getDivision().getComponentFullName()
                                  : Component.empty())
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .match("%division_short%")
                          .replacement(
                              profile.getStats().getDivision() != null
                                  ? profile.getStats().getDivision().getComponentShortName()
                                  : Component.empty())
                          .build()));
        }
      } else {
        for (String line : orEmpty(config.getLobby().getEventQueue().getIdle())) {
          sidebar.add(
              PapiUtil.runThroughFormat(player, line)
                  .replaceText(
                      TextReplacementConfig.builder()
                          .match("%eventName%")
                          .replacement(event.getType().getName())
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .match("%maxPlayer%")
                          .replacement(String.valueOf(event.getType().getMaxPlayer()))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .match("%player%")
                          .replacement(String.valueOf(event.getPlayers().size()))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .match("%division%")
                          .replacement(
                              profile.getStats().getDivision() != null
                                  ? profile.getStats().getDivision().getComponentFullName()
                                  : Component.empty())
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .match("%division_short%")
                          .replacement(
                              profile.getStats().getDivision() != null
                                  ? profile.getStats().getDivision().getComponentShortName()
                                  : Component.empty())
                          .build()));
        }
      }
    }
    return false;
  }

  private void buildMatchLines(Player player, SidebarConfig config, List<Component> sidebar) {
    Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
    if (match == null) {
      return;
    }

    Ladder ladder = match.getLadder();
    LadderType ladderType = ladder.getType();

    List<String> configLines = ladderLines(config.getMatch().getLadder(), match);
    boolean useLadderSpecificConfig = !configLines.isEmpty();

    if (useLadderSpecificConfig) {
      switch (match.getType()) {
        case DUEL -> buildDuelMatchLines(player, config, sidebar, match, ladderType);
        case PARTY_FFA -> buildPartyFfaMatchLines(player, config, sidebar, match, ladderType);
        case PARTY_SPLIT -> buildPartySplitMatchLines(player, config, sidebar, match, ladderType);
        case PARTY_VS_PARTY ->
            buildPartyVsPartyMatchLines(player, config, sidebar, match, ladderType);

        default -> {}
      }
    } else {
      for (String line :
          orEmpty(
              switch (match.getType()) {
                case DUEL -> config.getMatch().getDuel();
                case PARTY_FFA -> config.getMatch().getPartyFfa();
                case PARTY_SPLIT -> config.getMatch().getPartySplit();
                case PARTY_VS_PARTY -> config.getMatch().getPartyVsParty();
              })) {
        sidebar.add(
            AdapterUtil.replaceMatchPlaceholders(
                player, PapiUtil.runThroughFormat(player, line), match));
      }
    }
  }

  private void buildDuelMatchLines(
      Player player,
      SidebarConfig config,
      List<Component> sidebar,
      Match match,
      LadderType ladderType) {
    Duel duel = (Duel) match;
    Round round = duel.getCurrentRound();
    Player enemy = duel.getOppositePlayer(player);

    for (String line : ladderLines(config.getMatch().getLadder(), match)) {
      Component component =
          AdapterUtil.replaceMatchPlaceholders(
              player, PapiUtil.runThroughFormat(player, line), duel);

      switch (ladderType) {
        case BOXING:
          int playerHits =
              match.getCurrentStat(player) != null ? match.getCurrentStat(player).getHit() : 0;
          int enemyHits =
              match.getCurrentStat(enemy) != null ? match.getCurrentStat(enemy).getHit() : 0;
          int overAllHits = playerHits - enemyHits;

          component =
              component
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%overAllHits%")
                          .replacement(
                              AstralPractice.getMiniMessage()
                                  .deserialize((overAllHits < 0 ? "<red>" : "<green>"))
                                  .append(Component.text(overAllHits)))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%hits%")
                          .replacement(String.valueOf(playerHits))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%enemyHits%")
                          .replacement(String.valueOf(enemyHits))
                          .build());
          break;
        case BEDWARS:
        case FIREBALL_FIGHT:
        case MLG_RUSH:
          component =
              component
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%playerBedStatus%")
                          .replacement(
                              AstralPractice.getMiniMessage()
                                  .deserialize(
                                      StringUtil.legacyToMiniMessage(
                                          Objects.requireNonNull(
                                              round != null
                                                      && round
                                                          .getBedStatus()
                                                          .get(duel.getTeam(player))
                                                  ? config
                                                      .getMatch()
                                                      .getBedStatus()
                                                      .getNotDestroyed()
                                                  : config
                                                      .getMatch()
                                                      .getBedStatus()
                                                      .getDestroyed()))))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%enemyBedStatus%")
                          .replacement(
                              AstralPractice.getMiniMessage()
                                  .deserialize(
                                      StringUtil.legacyToMiniMessage(
                                          Objects.requireNonNull(
                                              round != null
                                                      && round
                                                          .getBedStatus()
                                                          .get(duel.getTeam(enemy))
                                                  ? config
                                                      .getMatch()
                                                      .getBedStatus()
                                                      .getNotDestroyed()
                                                  : config
                                                      .getMatch()
                                                      .getBedStatus()
                                                      .getDestroyed()))))
                          .build());
          break;

        default:
          break;
      }

      sidebar.add(component);
    }
  }

  private void buildPartyFfaMatchLines(
      Player player,
      SidebarConfig config,
      List<Component> sidebar,
      Match match,
      LadderType ladderType) {
    PartyFfa partyFfa = (PartyFfa) match;

    for (String line : ladderLines(config.getMatch().getLadder(), match)) {
      Component component =
          AdapterUtil.replaceMatchPlaceholders(
              player, PapiUtil.runThroughFormat(player, line), partyFfa);

      if (ladderType == LadderType.BOXING) {
        for (int i = 1; i <= 3; i++) {
          Player topPlayer = MatchUtil.getBoxingTopPlayer(partyFfa, i);
          Component playerName =
              topPlayer != null
                  ? displayName(topPlayer)
                  : AstralPractice.getMiniMessage().deserialize("<red>N/A");
          Component playerHits =
              topPlayer != null && match.getCurrentStat(topPlayer) != null
                  ? Component.text(match.getCurrentStat(topPlayer).getHit())
                  : AstralPractice.getMiniMessage().deserialize("<red>N/A");

          component =
              component
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%player" + i + "boxing%")
                          .replacement(playerName)
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%player" + i + "boxingHits%")
                          .replacement(playerHits)
                          .build());
        }
      }

      sidebar.add(component);
    }
  }

  private void buildPartySplitMatchLines(
      Player player,
      SidebarConfig config,
      List<Component> sidebar,
      Match match,
      LadderType ladderType) {
    PartySplit partySplit = (PartySplit) match;
    Round round = partySplit.getCurrentRound();

    for (String line : ladderLines(config.getMatch().getLadder(), match)) {
      Component component =
          AdapterUtil.replaceMatchPlaceholders(
              player, PapiUtil.runThroughFormat(player, line), partySplit);

      component =
          switch (ladderType) {
            case BOXING ->
                component
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%team1boxingHits%")
                            .replacement(
                                String.valueOf(
                                    Boxing.getTeamBoxingStrokes(
                                        match, partySplit.getTeamPlayers(TeamEnum.TEAM1))))
                            .build())
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%team2boxingHits%")
                            .replacement(
                                String.valueOf(
                                    Boxing.getTeamBoxingStrokes(
                                        match, partySplit.getTeamPlayers(TeamEnum.TEAM2))))
                            .build());
            case BEDWARS, FIREBALL_FIGHT, MLG_RUSH ->
                component
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%team1BedStatus%")
                            .replacement(
                                AstralPractice.getMiniMessage()
                                    .deserialize(
                                        StringUtil.legacyToMiniMessage(
                                            Objects.requireNonNull(
                                                round != null
                                                        && round.getBedStatus().get(TeamEnum.TEAM1)
                                                    ? config
                                                        .getMatch()
                                                        .getBedStatus()
                                                        .getNotDestroyed()
                                                    : config
                                                        .getMatch()
                                                        .getBedStatus()
                                                        .getDestroyed()))))
                            .build())
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%team2BedStatus%")
                            .replacement(
                                AstralPractice.getMiniMessage()
                                    .deserialize(
                                        StringUtil.legacyToMiniMessage(
                                            Objects.requireNonNull(
                                                round != null
                                                        && round.getBedStatus().get(TeamEnum.TEAM2)
                                                    ? config
                                                        .getMatch()
                                                        .getBedStatus()
                                                        .getNotDestroyed()
                                                    : config
                                                        .getMatch()
                                                        .getBedStatus()
                                                        .getDestroyed()))))
                            .build());
            default -> component;
          };

      sidebar.add(component);
    }
  }

  private void buildPartyVsPartyMatchLines(
      Player player,
      SidebarConfig config,
      List<Component> sidebar,
      Match match,
      LadderType ladderType) {
    PartyVsParty partyVsParty = (PartyVsParty) match;
    Round round = partyVsParty.getCurrentRound();
    TeamEnum team = partyVsParty.getTeam(player);
    TeamEnum enemyTeam = TeamUtil.getOppositeTeam(team);

    for (String line : ladderLines(config.getMatch().getLadder(), match)) {
      Component component =
          AdapterUtil.replaceMatchPlaceholders(
              player, PapiUtil.runThroughFormat(player, line), partyVsParty);

      component =
          switch (ladderType) {
            case BOXING ->
                component
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%partyTeamBoxingHits%")
                            .replacement(
                                String.valueOf(
                                    Boxing.getTeamBoxingStrokes(
                                        match, partyVsParty.getTeamPlayers(team))))
                            .build())
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%enemyTeamBoxingHits%")
                            .replacement(
                                String.valueOf(
                                    Boxing.getTeamBoxingStrokes(
                                        match, partyVsParty.getTeamPlayers(enemyTeam))))
                            .build());
            case BEDWARS, FIREBALL_FIGHT, MLG_RUSH ->
                component
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%partyTeamBedStatus%")
                            .replacement(
                                Objects.requireNonNull(
                                    round != null && round.getBedStatus().get(team)
                                        ? config.getMatch().getBedStatus().getNotDestroyed()
                                        : config.getMatch().getBedStatus().getDestroyed()))
                            .build())
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%enemyTeamBedStatus%")
                            .replacement(
                                Objects.requireNonNull(
                                    round != null && round.getBedStatus().get(enemyTeam)
                                        ? config.getMatch().getBedStatus().getNotDestroyed()
                                        : config.getMatch().getBedStatus().getDestroyed()))
                            .build());
            default -> component;
          };

      sidebar.add(component);
    }
  }

  private void buildFfaLines(Player player, SidebarConfig config, List<Component> sidebar) {
    Ffa ffa = FfaManager.getInstance().getFfaByPlayer(player);
    if (ffa == null) {
      return;
    }

    if (ffa.getBuildRollback() != null) {
      for (String line : orEmpty(config.getFfa().getGame().getBuild())) {
        sidebar.add(
            AdapterUtil.replaceFfaPlaceholders(
                player, PapiUtil.runThroughFormat(player, line), ffa));
      }
    } else {
      for (String line : orEmpty(config.getFfa().getGame().getNonBuild())) {
        sidebar.add(
            AdapterUtil.replaceFfaPlaceholders(
                player, PapiUtil.runThroughFormat(player, line), ffa));
      }
    }
  }

  private void buildEventLines(Player player, SidebarConfig config, List<Component> sidebar) {
    Event event = EventManager.getInstance().getEventByPlayer(player);
    if (event == null) {
      return;
    }

    List<String> eventLines = eventLines(config.getEvent(), event);
    switch (event.getType()) {
      case LMS -> {
        Lms lms = (Lms) event;
        for (String line : eventLines) {
          sidebar.add(
              PapiUtil.runThroughFormat(player, line)
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%players%")
                          .replacement(String.valueOf(lms.getStartPlayerCount()))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%alivePlayers%")
                          .replacement(String.valueOf(lms.getPlayers().size()))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%duration%")
                          .replacement(lms.getDurationRunnable().getFormattedTime())
                          .build()));
        }
      }
      case OITC -> {
        Oitc oitc = (Oitc) event;
        Player highestPointPlayer = oitc.getHighestPointPlayer();
        Component topPlayerName =
            highestPointPlayer != null
                ? displayName(highestPointPlayer)
                : AstralPractice.getMiniMessage().deserialize("<red>N/A");
        String topPlayerScore =
            highestPointPlayer != null
                ? String.valueOf(oitc.getPlayerPoints().get(highestPointPlayer))
                : "0";

        for (String line : eventLines) {
          sidebar.add(
              PapiUtil.runThroughFormat(player, line)
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%topPlayer%")
                          .replacement(topPlayerName)
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%topScore%")
                          .replacement(topPlayerScore)
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%players%")
                          .replacement(String.valueOf(oitc.getPlayerPoints().size()))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%lives%")
                          .replacement(String.valueOf(oitc.getPlayerLives().get(player)))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%alivePlayers%")
                          .replacement(String.valueOf(oitc.getPlayers().size()))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%duration%")
                          .replacement(oitc.getDurationRunnable().getFormattedTime())
                          .build()));
        }
      }
      case TNTTAG -> {
        TntTag tntTag = (TntTag) event;
        for (String line : eventLines) {
          sidebar.add(
              PapiUtil.runThroughFormat(player, line)
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%explosionTime%")
                          .replacement(
                              tntTag.getDurationRunnable() != null
                                  ? String.valueOf(tntTag.getDurationRunnable().getSeconds())
                                  : "0")
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%alivePlayers%")
                          .replacement(String.valueOf(tntTag.getPlayers().size()))
                          .build()));
        }
      }
      case BRACKETS, SUMO -> {
        DuelEvent duelEvent = (DuelEvent) event;
        DuelFight bracketFight = duelEvent.getFight(player);
        if (bracketFight != null) {
          for (String line : eventLines) {
            sidebar.add(
                PapiUtil.runThroughFormat(player, line)
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%enemy%")
                            .replacement(displayName(bracketFight.getOtherPlayer(player)))
                            .build())
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%players%")
                            .replacement(String.valueOf(duelEvent.getStartPlayerCount()))
                            .build())
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%alivePlayers%")
                            .replacement(String.valueOf(duelEvent.getPlayers().size()))
                            .build())
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%timeLeft%")
                            .replacement(
                                duelEvent.getDurationRunnable() != null
                                    ? duelEvent.getDurationRunnable().getFormattedTime()
                                    : "0")
                            .build())
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%round%")
                            .replacement(String.valueOf(duelEvent.getRound()))
                            .build()));
          }
        } else {
          for (String line : eventLines(config.getSpectate().getEvent(), event)) {
            sidebar.add(
                PapiUtil.runThroughFormat(player, line)
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%players%")
                            .replacement(String.valueOf(duelEvent.getStartPlayerCount()))
                            .build())
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%alivePlayers%")
                            .replacement(String.valueOf(duelEvent.getPlayers().size()))
                            .build())
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%timeLeft%")
                            .replacement(
                                duelEvent.getDurationRunnable() != null
                                    ? duelEvent.getDurationRunnable().getFormattedTime()
                                    : "0")
                            .build())
                    .replaceText(
                        TextReplacementConfig.builder()
                            .matchLiteral("%round%")
                            .replacement(String.valueOf(duelEvent.getRound()))
                            .build()));
          }
        }
      }
      case SPLEGG -> {
        Splegg splegg = (Splegg) event;
        for (String line : eventLines) {
          sidebar.add(
              PapiUtil.runThroughFormat(player, line)
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%players%")
                          .replacement(String.valueOf(splegg.getStartPlayerCount()))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%alivePlayers%")
                          .replacement(String.valueOf(splegg.getPlayers().size()))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%timeLeft%")
                          .replacement(
                              splegg.getDurationRunnable() != null
                                  ? splegg.getDurationRunnable().getFormattedTime()
                                  : "0")
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%firedEggs%")
                          .replacement(String.valueOf(splegg.getShotEggs().get(player)))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%brokenBlocks%")
                          .replacement(String.valueOf(splegg.getShotBlocks().get(player)))
                          .build()));
        }
      }
      case JUGGERNAUT -> {
        Juggernaut juggernaut = (Juggernaut) event;
        for (String line : eventLines) {
          sidebar.add(
              PapiUtil.runThroughFormat(player, line)
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%players%")
                          .replacement(String.valueOf(juggernaut.getStartPlayerCount()))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%alivePlayers%")
                          .replacement(String.valueOf(juggernaut.getPlayers().size() - 1))
                          .build())
                  .replaceText(
                      TextReplacementConfig.builder()
                          .matchLiteral("%duration%")
                          .replacement(juggernaut.getDurationRunnable().getFormattedTime())
                          .build()));
        }
      }

      default -> {}
    }
  }

  private void buildSpectateLines(Player player, SidebarConfig config, List<Component> sidebar) {
    if (player == null) {
      return;
    }
    Spectatable spectatable =
        SpectatorManager.getInstance().getSpectators().get(player.getUniqueId());
    if (spectatable == null) {
      return;
    }

    if (spectatable instanceof Match match) {
      buildSpectateMatchLines(player, config, sidebar, match);
    } else if (spectatable instanceof Ffa ffa) {
      if (ffa.getBuildRollback() != null) {
        for (String line : orEmpty(config.getFfa().getSpectate().getBuild())) {
          sidebar.add(
              AdapterUtil.replaceFfaSpecPlaceholders(PapiUtil.runThroughFormat(player, line), ffa));
        }
      } else {
        for (String line : orEmpty(config.getFfa().getSpectate().getNonBuild())) {
          sidebar.add(
              AdapterUtil.replaceFfaSpecPlaceholders(PapiUtil.runThroughFormat(player, line), ffa));
        }
      }
    } else if (spectatable instanceof Event event) {
      buildSpectateEventLines(player, config, sidebar, event);
    }
  }

  private void buildSpectateMatchLines(
      Player player, SidebarConfig config, List<Component> sidebar, Match match) {
    Round round;
    List<String> configLines = ladderLines(config.getSpectate().getMatch().getLadder(), match);
    boolean useLadderSpecificConfig = !configLines.isEmpty();

    if (useLadderSpecificConfig) {
      switch (match.getType()) {
        case DUEL -> {
          Duel duel = (Duel) match;
          round = duel.getCurrentRound();
          LadderType ladderType = match.getLadder().getType();

          for (String line : configLines) {
            switch (ladderType) {
              case BOXING:
                Statistic player1stats = match.getCurrentStat(duel.getPlayer1());
                Statistic player2stats = match.getCurrentStat(duel.getPlayer2());
                line =
                    line.replace(
                            "%player1hits%",
                            String.valueOf(player1stats != null ? player1stats.getHit() : 0))
                        .replace(
                            "%player2hits%",
                            String.valueOf(player2stats != null ? player2stats.getHit() : 0));
                break;
              case BEDWARS:
              case FIREBALL_FIGHT:
              case MLG_RUSH:
                line =
                    line.replace(
                            "%player1BedStatus%",
                            (Objects.requireNonNull(
                                round != null
                                        && round.getBedStatus().get(duel.getTeam(duel.getPlayer1()))
                                    ? config.getMatch().getBedStatus().getNotDestroyed()
                                    : config.getMatch().getBedStatus().getDestroyed())))
                        .replace(
                            "%player2BedStatus%",
                            (Objects.requireNonNull(
                                round != null
                                        && round.getBedStatus().get(duel.getTeam(duel.getPlayer2()))
                                    ? config.getMatch().getBedStatus().getNotDestroyed()
                                    : config.getMatch().getBedStatus().getDestroyed())));
                break;

              default:
                break;
            }
            sidebar.add(
                AdapterUtil.replaceMatchSpectatePlaceholders(
                    PapiUtil.runThroughFormat(player, line), duel));
          }
        }
        case PARTY_FFA -> {
          PartyFfa partyFfa = (PartyFfa) match;
          LadderType ladderType = match.getLadder().getType();

          for (String line : configLines) {
            if (ladderType == LadderType.BOXING) {
              Player player1 = MatchUtil.getBoxingTopPlayer(partyFfa, 1);
              if (player1 != null) {
                line =
                    line.replace(
                            "%player1boxing%",
                            AstralPractice.getMiniMessage().serialize(displayName(player1)))
                        .replace(
                            "%player1boxingHits%",
                            String.valueOf(match.getCurrentStat(player1).getHit()));
              } else {
                line =
                    line.replace("%player1boxing%", "<red>N/A")
                        .replace("%player1boxingHits%", "<red>N/A");
              }

              Player player2 = MatchUtil.getBoxingTopPlayer(partyFfa, 2);
              if (player2 != null) {
                line =
                    line.replace(
                            "%player2boxing%",
                            AstralPractice.getMiniMessage().serialize(displayName(player2)))
                        .replace(
                            "%player2boxingHits%",
                            String.valueOf(match.getCurrentStat(player2).getHit()));
              } else {
                line =
                    line.replace("%player2boxing%", "<red>N/A")
                        .replace("%player2boxingHits%", "<red>N/A");
              }

              Player player3 = MatchUtil.getBoxingTopPlayer(partyFfa, 3);
              if (player3 != null) {
                line =
                    line.replace(
                            "%player3boxing%",
                            AstralPractice.getMiniMessage().serialize(displayName(player3)))
                        .replace(
                            "%player3boxingHits%",
                            String.valueOf(match.getCurrentStat(player3).getHit()));
              } else {
                line =
                    line.replace("%player3boxing%", "<red>N/A")
                        .replace("%player3boxingHits%", "<red>N/A");
              }
            }
            sidebar.add(
                AdapterUtil.replaceMatchSpectatePlaceholders(
                    PapiUtil.runThroughFormat(player, line), partyFfa));
          }
        }
        case PARTY_SPLIT -> {
          PartySplit partySplit = (PartySplit) match;
          round = partySplit.getCurrentRound();
          LadderType ladderType = match.getLadder().getType();

          for (String line : configLines) {
            switch (ladderType) {
              case BOXING:
                line =
                    line.replace(
                            "%team1boxingHits%",
                            String.valueOf(
                                Boxing.getTeamBoxingStrokes(
                                    match, partySplit.getTeamPlayers(TeamEnum.TEAM1))))
                        .replace(
                            "%team2boxingHits%",
                            String.valueOf(
                                Boxing.getTeamBoxingStrokes(
                                    match, partySplit.getTeamPlayers(TeamEnum.TEAM2))));
                break;
              case BEDWARS:
              case FIREBALL_FIGHT:
              case MLG_RUSH:
                line =
                    line.replace(
                            "%team1BedStatus%",
                            (Objects.requireNonNull(
                                round != null && round.getBedStatus().get(TeamEnum.TEAM1)
                                    ? config.getMatch().getBedStatus().getNotDestroyed()
                                    : config.getMatch().getBedStatus().getDestroyed())))
                        .replace(
                            "%team2BedStatus%",
                            (Objects.requireNonNull(
                                round != null && round.getBedStatus().get(TeamEnum.TEAM2)
                                    ? config.getMatch().getBedStatus().getNotDestroyed()
                                    : config.getMatch().getBedStatus().getDestroyed())));
                break;

              default:
                break;
            }
            sidebar.add(
                AdapterUtil.replaceMatchSpectatePlaceholders(
                    PapiUtil.runThroughFormat(player, line), partySplit));
          }
        }
        case PARTY_VS_PARTY -> {
          PartyVsParty partyVsParty = (PartyVsParty) match;
          round = partyVsParty.getCurrentRound();
          LadderType ladderType = match.getLadder().getType();

          for (String line : configLines) {
            switch (ladderType) {
              case BOXING:
                line =
                    line.replace(
                            "%team1boxingHits%",
                            String.valueOf(
                                Boxing.getTeamBoxingStrokes(
                                    match, partyVsParty.getTeamPlayers(TeamEnum.TEAM1))))
                        .replace(
                            "%team2boxingHits%",
                            String.valueOf(
                                Boxing.getTeamBoxingStrokes(
                                    match, partyVsParty.getTeamPlayers(TeamEnum.TEAM2))));
                break;
              case BEDWARS:
              case FIREBALL_FIGHT:
              case MLG_RUSH:
                line =
                    line.replace(
                            "%team1BedStatus%",
                            (Objects.requireNonNull(
                                round.getBedStatus().get(TeamEnum.TEAM1)
                                    ? config.getMatch().getBedStatus().getNotDestroyed()
                                    : config.getMatch().getBedStatus().getDestroyed())))
                        .replace(
                            "%team2BedStatus%",
                            (Objects.requireNonNull(
                                round.getBedStatus().get(TeamEnum.TEAM2)
                                    ? config.getMatch().getBedStatus().getNotDestroyed()
                                    : config.getMatch().getBedStatus().getDestroyed())));
                break;

              default:
                break;
            }
            sidebar.add(
                AdapterUtil.replaceMatchSpectatePlaceholders(
                    PapiUtil.runThroughFormat(player, line), partyVsParty));
          }
        }

        default -> {}
      }
    } else {
      for (String line :
          orEmpty(
              switch (match.getType()) {
                case DUEL -> config.getSpectate().getMatch().getDuel();
                case PARTY_FFA -> config.getSpectate().getMatch().getPartyFfa();
                case PARTY_SPLIT -> config.getSpectate().getMatch().getPartySplit();
                case PARTY_VS_PARTY -> config.getSpectate().getMatch().getPartyVsParty();
              })) {
        sidebar.add(
            AdapterUtil.replaceMatchSpectatePlaceholders(
                PapiUtil.runThroughFormat(player, line), match));
      }
    }
  }

  private void buildSpectateEventLines(
      Player player, SidebarConfig config, List<Component> sidebar, Event event) {
    List<String> eventLines = eventLines(config.getSpectate().getEvent(), event);
    switch (event.getType()) {
      case LMS -> {
        Lms lms = (Lms) event;
        for (String line : eventLines) {
          line =
              line.replace("%players%", String.valueOf(lms.getStartPlayerCount()))
                  .replace("%alivePlayers%", String.valueOf(lms.getPlayers().size()))
                  .replace("%duration%", lms.getDurationRunnable().getFormattedTime());
          sidebar.add(PapiUtil.runThroughFormat(player, line));
        }
      }
      case OITC -> {
        Oitc oitc = (Oitc) event;
        Player highestPointPlayer = oitc.getHighestPointPlayer();
        String topPlayerName =
            highestPointPlayer != null
                ? AstralPractice.getMiniMessage().serialize(displayName(highestPointPlayer))
                : "<red>N/A";
        String topPlayerScore =
            highestPointPlayer != null
                ? String.valueOf(oitc.getPlayerPoints().get(highestPointPlayer))
                : "0";

        for (String line : eventLines) {
          line =
              line.replace("%topPlayer%", topPlayerName)
                  .replace("%topScore%", topPlayerScore)
                  .replace("%players%", String.valueOf(oitc.getPlayerPoints().size()))
                  .replace("%alivePlayers%", String.valueOf(oitc.getPlayers().size()))
                  .replace("%duration%", oitc.getDurationRunnable().getFormattedTime());
          sidebar.add(PapiUtil.runThroughFormat(player, line));
        }
      }
      case TNTTAG -> {
        TntTag tntTag = (TntTag) event;
        for (String line : eventLines) {
          line =
              line.replace(
                      "%explosionTime%",
                      (tntTag.getDurationRunnable() != null
                          ? String.valueOf(tntTag.getDurationRunnable().getSeconds())
                          : "0"))
                  .replace("%alivePlayers%", String.valueOf(tntTag.getPlayers().size()));
          sidebar.add(PapiUtil.runThroughFormat(player, line));
        }
      }
      case BRACKETS, SUMO -> {
        DuelEvent duelEvent = (DuelEvent) event;
        for (String line : eventLines) {
          line =
              line.replace("%players%", String.valueOf(duelEvent.getStartPlayerCount()))
                  .replace("%alivePlayers%", String.valueOf(duelEvent.getPlayers().size()))
                  .replace(
                      "%timeLeft%",
                      (duelEvent.getDurationRunnable() != null
                          ? duelEvent.getDurationRunnable().getFormattedTime()
                          : "0"))
                  .replace("%round%", String.valueOf(duelEvent.getRound()));
          sidebar.add(PapiUtil.runThroughFormat(player, line));
        }
      }
      case SPLEGG -> {
        Splegg splegg = (Splegg) event;
        for (String line : eventLines) {
          line =
              line.replace("%players%", String.valueOf(splegg.getStartPlayerCount()))
                  .replace("%alivePlayers%", String.valueOf(splegg.getPlayers().size()))
                  .replace(
                      "%timeLeft%",
                      (splegg.getDurationRunnable() != null
                          ? splegg.getDurationRunnable().getFormattedTime()
                          : "0"));
          sidebar.add(PapiUtil.runThroughFormat(player, line));
        }
      }
      case JUGGERNAUT -> {
        Juggernaut juggernaut = (Juggernaut) event;
        for (String line : eventLines) {
          line =
              line.replace("%players%", String.valueOf(juggernaut.getStartPlayerCount()))
                  .replace("%alivePlayers%", String.valueOf(juggernaut.getPlayers().size() - 1))
                  .replace("%duration%", juggernaut.getDurationRunnable().getFormattedTime());
          sidebar.add(PapiUtil.runThroughFormat(player, line));
        }
      }

      default -> {}
    }
  }

  private void buildFooterLines(
      Player player, SidebarConfig config, Profile profile, List<Component> sidebar) {
    Group group = profile.getGroup();
    if (group != null
        && group.getSidebarExtensionRaw() != null
        && !group.getSidebarExtensionRaw().isEmpty()) {
      for (String rawLine : group.getSidebarExtensionRaw()) {
        Component extensionLine =
            AdapterUtil.replaceArenaPlaceholders(PapiUtil.runThroughFormat(player, rawLine));
        sidebar.add(
            NameFormatUtil.applyPlaceholders(extensionLine, profile, player.getName()));
      }
    }

    if (player.hasPermission("ap.admin.scoreboard")) {
      buildAdminExtensionLines(player, config, sidebar);
    }
  }

  private void buildAdminExtensionLines(
      Player player, SidebarConfig config, List<Component> sidebar) {
    List<String> adminLines = config.getSpectate().getAdminExtension();
    if (adminLines == null || adminLines.isEmpty()) {
      return;
    }

    for (String rawLine : adminLines) {
      Component line = PapiUtil.runThroughFormat(player, rawLine);
      sidebar.add(AdapterUtil.replaceArenaPlaceholders(line));
    }
  }
}
