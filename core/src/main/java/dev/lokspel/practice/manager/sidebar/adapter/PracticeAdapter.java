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
import dev.lokspel.practice.manager.fight.match.type.playersvsplayers.PlayersVsPlayers;
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
import dev.lokspel.practice.util.PapiUtil;
import dev.lokspel.practice.util.StringUtil;
import dev.lokspel.practice.util.format.NameFormatUtil;
import dev.lokspel.practice.util.interfaces.Spectatable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.RegExp;

public class PracticeAdapter implements SidebarAdapter {

  private static final String NOT_AVAILABLE = "<red>N/A";
  private static final Component NOT_AVAILABLE_COMPONENT =
      Common.deserializeMiniMessage(NOT_AVAILABLE);

  private static String fallbackToZero(String value) {
    return value != null ? value : "0";
  }

  private static TextReplacementConfig match(@RegExp String placeholder, String value) {
    return TextReplacementConfig.builder()
        .match(placeholder)
        .replacement(value != null ? value : "")
        .build();
  }

  private static TextReplacementConfig match(@RegExp String placeholder, Component value) {
    return TextReplacementConfig.builder()
        .match(placeholder)
        .replacement(value != null ? value : Component.empty())
        .build();
  }

  private static TextReplacementConfig literal(String placeholder, String value) {
    return TextReplacementConfig.builder()
        .matchLiteral(placeholder)
        .replacement(value != null ? value : "")
        .build();
  }

  private static TextReplacementConfig literal(String placeholder, Component value) {
    return TextReplacementConfig.builder()
        .matchLiteral(placeholder)
        .replacement(value != null ? value : Component.empty())
        .build();
  }

  private static String bedStatusString(SidebarConfig config, Round round, TeamEnum team) {
    return round != null && Boolean.TRUE.equals(round.getBedStatus().get(team))
        ? config.getMatch().getBedStatus().getNotDestroyed()
        : config.getMatch().getBedStatus().getDestroyed();
  }

  private static Component bedStatusComponent(SidebarConfig config, Round round, TeamEnum team) {
    return Common.deserializeMiniMessage(
        StringUtil.legacyToMiniMessage(bedStatusString(config, round, team)));
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
    return PapiUtil.format(player, PracticeConfig.sidebar().getTitle());
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
    List<String> lines =
        party == null ? config.getLobby().getNormal() : config.getLobby().getParty();

    for (String line : orEmpty(lines)) {
      Component component =
          PapiUtil.format(player, line)
              .replaceText(
                  match("%onlinePlayers%", String.valueOf(Bukkit.getOnlinePlayers().size())))
              .replaceText(
                  match(
                      "%inFightPlayers%",
                      String.valueOf(MatchManager.getInstance().getPlayerInMatchSize())))
              .replaceText(
                  match(
                      "%inQueuePlayer%",
                      String.valueOf(QueueManager.getInstance().getQueues().size())))
              .replaceText(
                  match(
                      "%division%",
                      profile.getStats().getDivision() != null
                          ? profile.getStats().getDivision().getComponentFullName()
                          : Component.empty()))
              .replaceText(
                  match(
                      "%division_short%",
                      profile.getStats().getDivision() != null
                          ? profile.getStats().getDivision().getComponentShortName()
                          : Component.empty()));

      if (party != null) {
        component =
            component
                .replaceText(match("%partyLeader%", AdapterUtil.getSidebarName(party.getLeader())))
                .replaceText(
                    match(
                        "%partyLeaderNameOnly%", AdapterUtil.getSidebarNameOnly(party.getLeader())))
                .replaceText(match("%maxMember%", String.valueOf(party.getMaxPlayerLimit())))
                .replaceText(match("%members%", String.valueOf(party.getMembers().size())));
      }
      sidebar.add(AdapterUtil.replaceArenaPlaceholders(component));
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

    Component divisionFull =
        profile.getStats().getDivision() != null
            ? profile.getStats().getDivision().getComponentFullName()
            : Component.empty();
    Component divisionShort =
        profile.getStats().getDivision() != null
            ? profile.getStats().getDivision().getComponentShortName()
            : Component.empty();
    String online = String.valueOf(Bukkit.getOnlinePlayers().size());
    String inFight = String.valueOf(MatchManager.getInstance().getPlayerInMatchSize());

    if (queue != null) {
      boolean multiQueue = queue.getQueuedLadders().size() > 1;
      String ladderDisplayName =
          queue.getLadder() != null ? queue.getLadder().getDisplayName() : "Unknown";
      String multiQueueLabel = "";
      String multiQueueCurrent = "";

      if (multiQueue) {
        multiQueueLabel =
            PracticeConfig.settings().getQueue().getMulti().getSidebar().getMultipleLabel();
        if (multiQueueLabel == null || multiQueueLabel.isEmpty()) {
          multiQueueLabel = PracticeConfig.language().getQueues().getMulti().getSidebarMultiple();
        }

        String currentFormat =
            PracticeConfig.settings().getQueue().getMulti().getSidebar().getCurrentLadderFormat();
        if (currentFormat != null && !currentFormat.isEmpty()) {
          multiQueueCurrent = currentFormat.replace("%ladder%", queue.getCyclingSidebarLadder());
        }
        ladderDisplayName = multiQueueLabel != null ? multiQueueLabel : "";
      }

      List<String> duelQueueLines =
          multiQueue && config.getLobby().getDuelQueueMulti() != null
              ? config.getLobby().getDuelQueueMulti()
              : config.getLobby().getDuelQueue();
      for (String line : orEmpty(duelQueueLines)) {
        sidebar.add(
            PapiUtil.format(player, line)
                .replaceText(match("%onlinePlayers%", online))
                .replaceText(match("%inFightPlayers%", inFight))
                .replaceText(
                    match(
                        "%inQueuePlayer%",
                        String.valueOf(QueueManager.getInstance().getQueues().size())))
                .replaceText(
                    match(
                        "%weightClass%",
                        queue.isRanked()
                            ? WeightClass.RANKED.getName()
                            : WeightClass.UNRANKED.getName()))
                .replaceText(match("%ladderDisplayName%", parseColoredText(ladderDisplayName)))
                .replaceText(match("%multiQueueLabel%", parseColoredText(multiQueueLabel)))
                .replaceText(
                    match("%multiQueueCurrentLadder%", parseColoredText(multiQueueCurrent)))
                .replaceText(match("%elapsedTime%", queue.getFormattedDuration()))
                .replaceText(match("%division%", divisionFull))
                .replaceText(match("%division_short%", divisionShort)));
      }
      return true;
    } else if (hostedCustomKitQueue != null) {
      String totalQueue =
          String.valueOf(
              QueueManager.getInstance().getQueues().size()
                  + CustomKitQueueManager.getInstance().getHostedQueues().size()
                  + CustomKitQueueManager.getInstance().getJoinSearches().size());
      for (String line : orEmpty(config.getLobby().getCustomKitQueue().getHosting())) {
        sidebar.add(
            PapiUtil.format(player, line)
                .replaceText(match("%onlinePlayers%", online))
                .replaceText(match("%inFightPlayers%", inFight))
                .replaceText(match("%inQueuePlayer%", totalQueue))
                .replaceText(match("%weightClass%", WeightClass.UNRANKED.getName()))
                .replaceText(
                    match(
                        "%ladderDisplayName%",
                        hostedCustomKitQueue.customLadder().getDisplayName()))
                .replaceText(match("%elapsedTime%", hostedCustomKitQueue.getElapsedSeconds() + "s"))
                .replaceText(match("%division%", divisionFull))
                .replaceText(match("%division_short%", divisionShort)));
      }
      return true;
    } else if (customKitJoinSearch != null) {
      String totalQueue =
          String.valueOf(
              QueueManager.getInstance().getQueues().size()
                  + CustomKitQueueManager.getInstance().getHostedQueues().size()
                  + CustomKitQueueManager.getInstance().getJoinSearches().size());
      for (String line : orEmpty(config.getLobby().getCustomKitQueue().getJoinSearch())) {
        sidebar.add(
            PapiUtil.format(player, line)
                .replaceText(match("%onlinePlayers%", online))
                .replaceText(match("%inFightPlayers%", inFight))
                .replaceText(match("%inQueuePlayer%", totalQueue))
                .replaceText(match("%weightClass%", WeightClass.UNRANKED.getName()))
                .replaceText(match("%ladderDisplayName%", "Any Hosted Kit"))
                .replaceText(match("%elapsedTime%", customKitJoinSearch.getElapsedSeconds() + "s"))
                .replaceText(match("%division%", divisionFull))
                .replaceText(match("%division_short%", divisionShort)));
      }
      return true;
    } else if (event != null) {
      String eventQueueTimeLeft =
          event.getQueueRunnable() != null ? event.getQueueRunnable().getFormattedTime() : null;
      List<String> eventLines =
          eventQueueTimeLeft != null
              ? config.getLobby().getEventQueue().getStarting()
              : config.getLobby().getEventQueue().getIdle();

      for (String line : orEmpty(eventLines)) {
        Component c =
            PapiUtil.format(player, line)
                .replaceText(match("%eventName%", event.getType().getName()))
                .replaceText(match("%maxPlayer%", String.valueOf(event.getType().getMaxPlayer())))
                .replaceText(match("%player%", String.valueOf(event.getPlayers().size())))
                .replaceText(match("%division%", divisionFull))
                .replaceText(match("%division_short%", divisionShort));
        if (eventQueueTimeLeft != null) {
          c = c.replaceText(match("%timeLeft%", fallbackToZero(eventQueueTimeLeft)));
        }
        sidebar.add(c);
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
    if (!configLines.isEmpty()) {
      switch (match.getType()) {
        case DUEL -> buildDuelMatchLines(player, config, sidebar, match, ladderType);
        case PARTY_FFA -> buildPartyFfaMatchLines(player, config, sidebar, match, ladderType);
        case PARTY_SPLIT -> buildPartySplitMatchLines(player, config, sidebar, match, ladderType);
        case PARTY_VS_PARTY ->
            buildPartyVsPartyMatchLines(player, config, sidebar, match, ladderType);
        default -> {}
      }
    } else {
      List<String> defaultLines =
          switch (match.getType()) {
            case DUEL -> config.getMatch().getDuel();
            case PARTY_FFA -> config.getMatch().getPartyFfa();
            case PARTY_SPLIT -> config.getMatch().getPartySplit();
            case PARTY_VS_PARTY -> config.getMatch().getPartyVsParty();
          };
      for (String line : orEmpty(defaultLines)) {
        sidebar.add(
            AdapterUtil.replaceMatchPlaceholders(player, PapiUtil.format(player, line), match));
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

    int playerHits =
        match.getCurrentStat(player) != null ? match.getCurrentStat(player).getHit() : 0;
    int enemyHits = match.getCurrentStat(enemy) != null ? match.getCurrentStat(enemy).getHit() : 0;
    int overAllHits = playerHits - enemyHits;

    for (String line : ladderLines(config.getMatch().getLadder(), match)) {
      Component component =
          AdapterUtil.replaceMatchPlaceholders(player, PapiUtil.format(player, line), duel);

      component =
          switch (ladderType) {
            case BOXING ->
                component
                    .replaceText(
                        literal(
                            "%overAllHits%",
                            AstralPractice.getMiniMessage()
                                .deserialize(overAllHits < 0 ? "<red>" : "<green>")
                                .append(Component.text(overAllHits))))
                    .replaceText(literal("%hits%", String.valueOf(playerHits)))
                    .replaceText(literal("%enemyHits%", String.valueOf(enemyHits)));
            case BEDWARS, FIREBALL_FIGHT, MLG_RUSH ->
                component
                    .replaceText(
                        literal(
                            "%playerBedStatus%",
                            bedStatusComponent(config, round, duel.getTeam(player))))
                    .replaceText(
                        literal(
                            "%enemyBedStatus%",
                            bedStatusComponent(config, round, duel.getTeam(enemy))));
            default -> component;
          };

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
          AdapterUtil.replaceMatchPlaceholders(player, PapiUtil.format(player, line), partyFfa);

      if (ladderType == LadderType.BOXING) {
        for (int i = 1; i <= 3; i++) {
          Player topPlayer = MatchUtil.getBoxingTopPlayer(partyFfa, i);
          Component playerName =
              topPlayer != null ? AdapterUtil.getSidebarName(topPlayer) : NOT_AVAILABLE_COMPONENT;
          Component playerHits =
              topPlayer != null && match.getCurrentStat(topPlayer) != null
                  ? Component.text(match.getCurrentStat(topPlayer).getHit())
                  : NOT_AVAILABLE_COMPONENT;

          component =
              component
                  .replaceText(literal("%player" + i + "boxing%", playerName))
                  .replaceText(literal("%player" + i + "boxingHits%", playerHits));
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
          AdapterUtil.replaceMatchPlaceholders(player, PapiUtil.format(player, line), partySplit);

      component =
          switch (ladderType) {
            case BOXING ->
                component
                    .replaceText(
                        literal(
                            "%team1boxingHits%",
                            String.valueOf(
                                Boxing.getTeamBoxingStrokes(
                                    match, partySplit.getTeamPlayers(TeamEnum.TEAM1)))))
                    .replaceText(
                        literal(
                            "%team2boxingHits%",
                            String.valueOf(
                                Boxing.getTeamBoxingStrokes(
                                    match, partySplit.getTeamPlayers(TeamEnum.TEAM2)))));
            case BEDWARS, FIREBALL_FIGHT, MLG_RUSH ->
                component
                    .replaceText(
                        literal(
                            "%team1BedStatus%", bedStatusComponent(config, round, TeamEnum.TEAM1)))
                    .replaceText(
                        literal(
                            "%team2BedStatus%", bedStatusComponent(config, round, TeamEnum.TEAM2)));
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
          AdapterUtil.replaceMatchPlaceholders(player, PapiUtil.format(player, line), partyVsParty);

      component =
          switch (ladderType) {
            case BOXING ->
                component
                    .replaceText(
                        literal(
                            "%partyTeamBoxingHits%",
                            String.valueOf(
                                Boxing.getTeamBoxingStrokes(
                                    match, partyVsParty.getTeamPlayers(team)))))
                    .replaceText(
                        literal(
                            "%enemyTeamBoxingHits%",
                            String.valueOf(
                                Boxing.getTeamBoxingStrokes(
                                    match, partyVsParty.getTeamPlayers(enemyTeam)))));
            case BEDWARS, FIREBALL_FIGHT, MLG_RUSH ->
                component
                    .replaceText(
                        literal("%partyTeamBedStatus%", bedStatusComponent(config, round, team)))
                    .replaceText(
                        literal(
                            "%enemyTeamBedStatus%", bedStatusComponent(config, round, enemyTeam)));
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

    List<String> lines =
        ffa.getBuildRollback() != null
            ? config.getFfa().getGame().getBuild()
            : config.getFfa().getGame().getNonBuild();
    for (String line : orEmpty(lines)) {
      sidebar.add(AdapterUtil.replaceFfaPlaceholders(player, PapiUtil.format(player, line), ffa));
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
              PapiUtil.format(player, line)
                  .replaceText(literal("%players%", String.valueOf(lms.getStartPlayerCount())))
                  .replaceText(literal("%alivePlayers%", String.valueOf(lms.getPlayers().size())))
                  .replaceText(
                      literal("%duration%", lms.getDurationRunnable().getFormattedTime())));
        }
      }
      case OITC -> {
        Oitc oitc = (Oitc) event;
        Player highestPointPlayer = oitc.getHighestPointPlayer();
        Component topPlayerName =
            highestPointPlayer != null
                ? AdapterUtil.getSidebarName(highestPointPlayer)
                : NOT_AVAILABLE_COMPONENT;
        String topPlayerScore =
            highestPointPlayer != null
                ? String.valueOf(oitc.getPlayerPoints().get(highestPointPlayer))
                : "0";

        for (String line : eventLines) {
          sidebar.add(
              PapiUtil.format(player, line)
                  .replaceText(literal("%topPlayer%", topPlayerName))
                  .replaceText(literal("%topScore%", topPlayerScore))
                  .replaceText(literal("%players%", String.valueOf(oitc.getPlayerPoints().size())))
                  .replaceText(
                      literal("%lives%", String.valueOf(oitc.getPlayerLives().get(player))))
                  .replaceText(literal("%alivePlayers%", String.valueOf(oitc.getPlayers().size())))
                  .replaceText(
                      literal("%duration%", oitc.getDurationRunnable().getFormattedTime())));
        }
      }
      case TNTTAG -> {
        TntTag tntTag = (TntTag) event;
        String explosionTime =
            tntTag.getDurationRunnable() != null
                ? String.valueOf(tntTag.getDurationRunnable().getSeconds())
                : "0";
        for (String line : eventLines) {
          sidebar.add(
              PapiUtil.format(player, line)
                  .replaceText(literal("%explosionTime%", explosionTime))
                  .replaceText(
                      literal("%alivePlayers%", String.valueOf(tntTag.getPlayers().size()))));
        }
      }
      case BRACKETS, SUMO -> {
        DuelEvent duelEvent = (DuelEvent) event;
        DuelFight bracketFight = duelEvent.getFight(player);
        List<String> lines =
            bracketFight != null ? eventLines : eventLines(config.getSpectate().getEvent(), event);
        Component enemyComp =
            bracketFight != null
                ? AdapterUtil.getSidebarName(bracketFight.getOtherPlayer(player))
                : Component.empty();
        String timeLeft =
            duelEvent.getDurationRunnable() != null
                ? duelEvent.getDurationRunnable().getFormattedTime()
                : "0";

        for (String line : lines) {
          Component c = PapiUtil.format(player, line);
          if (bracketFight != null) {
            c = c.replaceText(literal("%enemy%", enemyComp));
          }
          c =
              c.replaceText(literal("%players%", String.valueOf(duelEvent.getStartPlayerCount())))
                  .replaceText(
                      literal("%alivePlayers%", String.valueOf(duelEvent.getPlayers().size())))
                  .replaceText(literal("%timeLeft%", timeLeft))
                  .replaceText(literal("%round%", String.valueOf(duelEvent.getRound())));
          sidebar.add(c);
        }
      }
      case SPLEGG -> {
        Splegg splegg = (Splegg) event;
        String timeLeft =
            splegg.getDurationRunnable() != null
                ? splegg.getDurationRunnable().getFormattedTime()
                : "0";
        for (String line : eventLines) {
          sidebar.add(
              PapiUtil.format(player, line)
                  .replaceText(literal("%players%", String.valueOf(splegg.getStartPlayerCount())))
                  .replaceText(
                      literal("%alivePlayers%", String.valueOf(splegg.getPlayers().size())))
                  .replaceText(literal("%timeLeft%", timeLeft))
                  .replaceText(
                      literal("%firedEggs%", String.valueOf(splegg.getShotEggs().get(player))))
                  .replaceText(
                      literal(
                          "%brokenBlocks%", String.valueOf(splegg.getShotBlocks().get(player)))));
        }
      }
      case JUGGERNAUT -> {
        Juggernaut juggernaut = (Juggernaut) event;
        for (String line : eventLines) {
          sidebar.add(
              PapiUtil.format(player, line)
                  .replaceText(
                      literal("%players%", String.valueOf(juggernaut.getStartPlayerCount())))
                  .replaceText(
                      literal("%alivePlayers%", String.valueOf(juggernaut.getPlayers().size() - 1)))
                  .replaceText(
                      literal("%duration%", juggernaut.getDurationRunnable().getFormattedTime())));
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
      List<String> lines =
          ffa.getBuildRollback() != null
              ? config.getFfa().getSpectate().getBuild()
              : config.getFfa().getSpectate().getNonBuild();
      for (String line : orEmpty(lines)) {
        sidebar.add(AdapterUtil.replaceFfaSpecPlaceholders(PapiUtil.format(player, line), ffa));
      }
    } else if (spectatable instanceof Event event) {
      buildSpectateEventLines(player, config, sidebar, event);
    }
  }

  private void buildSpectateMatchLines(
      Player player, SidebarConfig config, List<Component> sidebar, Match match) {
    List<String> configLines = ladderLines(config.getSpectate().getMatch().getLadder(), match);
    if (!configLines.isEmpty()) {
      Round round = match.getCurrentRound();
      LadderType ladderType = match.getLadder().getType();

      switch (match.getType()) {
        case DUEL -> {
          Duel duel = (Duel) match;
          Statistic p1Stats = match.getCurrentStat(duel.getPlayer1());
          Statistic p2Stats = match.getCurrentStat(duel.getPlayer2());

          for (String line : configLines) {
            line =
                switch (ladderType) {
                  case BOXING ->
                      line.replace(
                              "%player1hits%",
                              String.valueOf(p1Stats != null ? p1Stats.getHit() : 0))
                          .replace(
                              "%player2hits%",
                              String.valueOf(p2Stats != null ? p2Stats.getHit() : 0));
                  case BEDWARS, FIREBALL_FIGHT, MLG_RUSH ->
                      line.replace(
                              "%player1BedStatus%",
                              bedStatusString(config, round, duel.getTeam(duel.getPlayer1())))
                          .replace(
                              "%player2BedStatus%",
                              bedStatusString(config, round, duel.getTeam(duel.getPlayer2())));
                  default -> line;
                };
            sidebar.add(
                AdapterUtil.replaceMatchSpectatePlaceholders(PapiUtil.format(player, line), duel));
          }
        }
        case PARTY_FFA -> {
          PartyFfa partyFfa = (PartyFfa) match;

          for (String line : configLines) {
            if (ladderType == LadderType.BOXING) {
              for (int i = 1; i <= 3; i++) {
                Player top = MatchUtil.getBoxingTopPlayer(partyFfa, i);
                String name =
                    top != null
                        ? AstralPractice.getMiniMessage().serialize(AdapterUtil.getSidebarName(top))
                        : NOT_AVAILABLE;
                String hits =
                    top != null && match.getCurrentStat(top) != null
                        ? String.valueOf(match.getCurrentStat(top).getHit())
                        : NOT_AVAILABLE;
                line =
                    line.replace("%player" + i + "boxing%", name)
                        .replace("%player" + i + "boxingHits%", hits);
              }
            }
            sidebar.add(
                AdapterUtil.replaceMatchSpectatePlaceholders(
                    PapiUtil.format(player, line), partyFfa));
          }
        }
        case PARTY_SPLIT, PARTY_VS_PARTY -> {
          PlayersVsPlayers pvp = (PlayersVsPlayers) match;

          for (String line : configLines) {
            line =
                switch (ladderType) {
                  case BOXING ->
                      line.replace(
                              "%team1boxingHits%",
                              String.valueOf(
                                  Boxing.getTeamBoxingStrokes(
                                      match, pvp.getTeamPlayers(TeamEnum.TEAM1))))
                          .replace(
                              "%team2boxingHits%",
                              String.valueOf(
                                  Boxing.getTeamBoxingStrokes(
                                      match, pvp.getTeamPlayers(TeamEnum.TEAM2))));
                  case BEDWARS, FIREBALL_FIGHT, MLG_RUSH ->
                      line.replace(
                              "%team1BedStatus%", bedStatusString(config, round, TeamEnum.TEAM1))
                          .replace(
                              "%team2BedStatus%", bedStatusString(config, round, TeamEnum.TEAM2));
                  default -> line;
                };
            sidebar.add(
                AdapterUtil.replaceMatchSpectatePlaceholders(PapiUtil.format(player, line), match));
          }
        }
        default -> {}
      }
    } else {
      List<String> defaultLines =
          switch (match.getType()) {
            case DUEL -> config.getSpectate().getMatch().getDuel();
            case PARTY_FFA -> config.getSpectate().getMatch().getPartyFfa();
            case PARTY_SPLIT -> config.getSpectate().getMatch().getPartySplit();
            case PARTY_VS_PARTY -> config.getSpectate().getMatch().getPartyVsParty();
          };
      for (String line : orEmpty(defaultLines)) {
        sidebar.add(
            AdapterUtil.replaceMatchSpectatePlaceholders(PapiUtil.format(player, line), match));
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
          sidebar.add(PapiUtil.format(player, line));
        }
      }
      case OITC -> {
        Oitc oitc = (Oitc) event;
        Player highestPointPlayer = oitc.getHighestPointPlayer();
        String topPlayerName =
            highestPointPlayer != null
                ? AstralPractice.getMiniMessage()
                    .serialize(AdapterUtil.getSidebarName(highestPointPlayer))
                : NOT_AVAILABLE;
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
          sidebar.add(PapiUtil.format(player, line));
        }
      }
      case TNTTAG -> {
        TntTag tntTag = (TntTag) event;
        String explosionTime =
            tntTag.getDurationRunnable() != null
                ? String.valueOf(tntTag.getDurationRunnable().getSeconds())
                : "0";
        for (String line : eventLines) {
          line =
              line.replace("%explosionTime%", explosionTime)
                  .replace("%alivePlayers%", String.valueOf(tntTag.getPlayers().size()));
          sidebar.add(PapiUtil.format(player, line));
        }
      }
      case BRACKETS, SUMO -> {
        DuelEvent duelEvent = (DuelEvent) event;
        String timeLeft =
            duelEvent.getDurationRunnable() != null
                ? duelEvent.getDurationRunnable().getFormattedTime()
                : "0";
        for (String line : eventLines) {
          line =
              line.replace("%players%", String.valueOf(duelEvent.getStartPlayerCount()))
                  .replace("%alivePlayers%", String.valueOf(duelEvent.getPlayers().size()))
                  .replace("%timeLeft%", timeLeft)
                  .replace("%round%", String.valueOf(duelEvent.getRound()));
          sidebar.add(PapiUtil.format(player, line));
        }
      }
      case SPLEGG -> {
        Splegg splegg = (Splegg) event;
        String timeLeft =
            splegg.getDurationRunnable() != null
                ? splegg.getDurationRunnable().getFormattedTime()
                : "0";
        for (String line : eventLines) {
          line =
              line.replace("%players%", String.valueOf(splegg.getStartPlayerCount()))
                  .replace("%alivePlayers%", String.valueOf(splegg.getPlayers().size()))
                  .replace("%timeLeft%", timeLeft);
          sidebar.add(PapiUtil.format(player, line));
        }
      }
      case JUGGERNAUT -> {
        Juggernaut juggernaut = (Juggernaut) event;
        for (String line : eventLines) {
          line =
              line.replace("%players%", String.valueOf(juggernaut.getStartPlayerCount()))
                  .replace("%alivePlayers%", String.valueOf(juggernaut.getPlayers().size() - 1))
                  .replace("%duration%", juggernaut.getDurationRunnable().getFormattedTime());
          sidebar.add(PapiUtil.format(player, line));
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
            AdapterUtil.replaceArenaPlaceholders(PapiUtil.format(player, rawLine));
        sidebar.add(NameFormatUtil.applyPlaceholders(extensionLine, profile, player.getName()));
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
      Component line = PapiUtil.format(player, rawLine);
      sidebar.add(AdapterUtil.replaceArenaPlaceholders(line));
    }
  }
}
