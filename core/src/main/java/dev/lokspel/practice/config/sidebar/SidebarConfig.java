package dev.lokspel.practice.config.sidebar;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import me.whereareiam.configura.annotation.merge.Merge;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;
import me.whereareiam.configura.type.merge.MissingEntries;

@Getter
public class SidebarConfig {
  @JsonProperty("TITLE")
  private String title = "<gold><b>PRACTICE";

  @Comment({
    "Controls how player names are displayed in the sidebar.",
    "Options:",
    "  NAME_ONLY            - Just the player name (no prefix, no suffix)",
    "  PREFIX_NAME          - Prefix + Name (no suffix)",
    "  NAME_SUFFIX          - Name + Suffix (no prefix)",
    "  PREFIX_NAME_SUFFIX   - Full format: Prefix + Name + Suffix (default)"
  })
  @JsonProperty("SIDEBAR-NAME-FORMAT")
  private String sidebarNameFormat = "NAME_SUFFIX";

  @JsonProperty("LOBBY")
  private Lobby lobby = new Lobby();

  @Comment({
    "Available variables in every case:",
    "%arena% - Display name of the arena",
    "%ladder% - Display name of the ladder"
  })
  @JsonProperty("MATCH")
  private Match match = new Match();

  @JsonProperty("EVENT")
  private Map<String, List<String>> event = defaultEvent();

  @JsonProperty("FFA")
  private Ffa ffa = new Ffa();

  @JsonProperty("SPECTATE")
  private Spectate spectate = new Spectate();

  @Comment({
    "First you have to set up the groups in the config.yml",
    "You can add/remove groups to this list",
    "The extension will always be added at the end of the sidebar."
  })
  @Merge(missingEntries = MissingEntries.OMIT)
  @JsonProperty("GROUP-EXTENSIONS")
  private Map<String, List<String>> groupExtensions = defaultGroupExtensions();

  private static Map<String, List<String>> defaultEvent() {
    Map<String, List<String>> values = new LinkedHashMap<>();
    values.put(
        "LMS",
        List.of(
            "<gray><st>---------------------",
            "<gold>Last Man Standing",
            "",
            "<white>Alive: <gold>%players%<gray>/<yellow>%alivePlayers% players",
            "<white>Duration: <gold>%duration%",
            "<gray><st>---------------------"));
    values.put(
        "OITC",
        List.of(
            "<gray><st>---------------------",
            "<gold>One In The Chamber",
            "",
            "<white>Highest Point:",
            "<gold>%topPlayer% : <yellow>%topScore%",
            "",
            "<white>Lives: <green>%lives%",
            "",
            "<white>Alive: <gold>%players%<gray>/<yellow>%alivePlayers% players",
            "<white>Duration: <gold>%duration%",
            "<gray><st>---------------------"));
    values.put(
        "TNTTAG",
        List.of(
            "<gray><st>---------------------",
            "<gold>TNT Tag",
            "",
            "<white>Explosion in <gold>%explosionTime%s",
            "",
            "<white>Alive: <gold>%alivePlayers% players",
            "<gray><st>---------------------"));
    values.put(
        "BRACKETS",
        List.of(
            "<gray><st>---------------------",
            "<gold>Brackets",
            "",
            "<red>Enemy: <white>%enemy%",
            "",
            "<white>Alive: <gold>%players%<gray>/<yellow>%alivePlayers% players",
            "<white>Time Left: <gold>%timeLeft%",
            "",
            "<gray>Round #%round%",
            "<gray><st>---------------------"));
    values.put(
        "SUMO",
        List.of(
            "<gray><st>---------------------",
            "<gold>Sumo",
            "",
            "<red>Enemy: <white>%enemy%",
            "",
            "<white>Alive: <gold>%players%<gray>/<yellow>%alivePlayers% players",
            "<white>Time Left: <gold>%timeLeft%",
            "",
            "<gray>Round #%round%",
            "<gray><st>---------------------"));
    values.put(
        "SPLEGG",
        List.of(
            "<gray><st>---------------------",
            "<gold>Splegg",
            "",
            "<white>Alive: <gold>%players%<gray>/<yellow>%alivePlayers% players",
            "<white>Time Left: <gold>%timeLeft%",
            "",
            "<white>Eggs fired: <gold>%firedEggs%",
            "<white>Blocks broken: <gold>%brokenBlocks%",
            "<gray><st>---------------------"));
    values.put(
        "JUGGERNAUT",
        List.of(
            "<gray><st>---------------------",
            "<gold>Juggernaut",
            "",
            "<white>Alive: <gold>%players%<gray>/<yellow>%alivePlayers% players",
            "<white>Duration: <gold>%duration%",
            "<gray><st>---------------------"));

    return values;
  }

  private static Map<String, List<String>> defaultGroupExtensions() {
    Map<String, List<String>> values = new LinkedHashMap<>();
    values.put("PREMIUM", List.of("<white>Rank: <red>Premium", "<gray><st>---------------------"));
    values.put("SUPREME", List.of("<white>Rank: <gold>Supreme", "<gray><st>---------------------"));

    return values;
  }

  @Getter
  public static class Lobby {
    @JsonProperty("NORMAL")
    private List<String> normal =
        List.of(
            "<gray><st>---------------------",
            "<white>Online: <gold>%onlinePlayers%",
            "<white>In Fights: <gold>%inFightPlayers%",
            "<white>In Queue: <gold>%inQueuePlayer%",
            "",
            "<white>Division: %division%",
            "",
            "<gold>play.astralpractice.com",
            "<gray><st>---------------------");

    @JsonProperty("PARTY")
    private List<String> party =
        List.of(
            "<gray><st>---------------------",
            "<white>Online: <gold>%onlinePlayers%",
            "<white>In Fights: <gold>%inFightPlayers%",
            "<white>In Queue: <gold>%inQueuePlayer%",
            "",
            "<gold>play.astralpractice.com",
            "<gray><st>---------------------",
            "<gold>Party",
            "<white>Leader: <gold>%partyLeaderNameOnly%",
            "<white>Members: <gold>%maxMember%<gray>/<yellow>%members%",
            "<gray><st>---------------------");

    @JsonProperty("DUEL-QUEUE")
    private List<String> duelQueue =
        List.of(
            "<gray><st>---------------------",
            "<white>Online: <gold>%onlinePlayers%",
            "<white>In Fights: <gold>%inFightPlayers%",
            "<white>In Queue: <gold>%inQueuePlayer%",
            "<gray><st>---------------------",
            "%weightClass% %ladderDisplayName%",
            "<white>Time: <gold>%elapsedTime%",
            "",
            "<gold>play.astralpractice.com",
            "<gray><st>---------------------");

    @JsonProperty("DUEL-QUEUE-MULTI")
    private List<String> duelQueueMulti =
        List.of(
            "<gray><st>---------------------",
            "<white>Online: <gold>%onlinePlayers%",
            "<white>In Fights: <gold>%inFightPlayers%",
            "<white>In Queue: <gold>%inQueuePlayer%",
            "<gray><st>---------------------",
            "%weightClass% %multiQueueLabel%",
            "%multiQueueCurrentLadder%",
            "<white>Time: <gold>%elapsedTime%",
            "",
            "<gold>play.astralpractice.com",
            "<gray><st>---------------------");

    @JsonProperty("CUSTOM-KIT-QUEUE")
    private CustomKitQueue customKitQueue = new CustomKitQueue();

    @JsonProperty("EVENT-QUEUE")
    private EventQueue eventQueue = new EventQueue();

    @Getter
    public static class CustomKitQueue {
      @JsonProperty("HOSTING")
      private List<String> hosting =
          List.of(
              "<gray><st>---------------------",
              "<white>Online: <gold>%onlinePlayers%",
              "<white>In Fights: <gold>%inFightPlayers%",
              "<white>In Queue: <gold>%inQueuePlayer%",
              "<gray><st>---------------------",
              "<gold>Custom Kit Host",
              "<white>Kit: <gold>%ladderDisplayName%",
              "<white>Time: <gold>%elapsedTime%",
              "",
              "<gold>play.astralpractice.com",
              "<gray><st>---------------------");

      @JsonProperty("JOIN-SEARCH")
      private List<String> joinSearch =
          List.of(
              "<gray><st>---------------------",
              "<white>Online: <gold>%onlinePlayers%",
              "<white>In Fights: <gold>%inFightPlayers%",
              "<white>In Queue: <gold>%inQueuePlayer%",
              "<gray><st>---------------------",
              "<gold>Custom Kit Queue",
              "<white>Searching: <gold>%ladderDisplayName%",
              "<white>Time: <gold>%elapsedTime%",
              "",
              "<gold>play.astralpractice.com",
              "<gray><st>---------------------");
    }

    @Getter
    public static class EventQueue {
      @JsonProperty("IDLE")
      private List<String> idle =
          List.of(
              "<gray><st>---------------------",
              "<gold>Event Queue",
              "",
              "<white>Event: <gold>%eventName%",
              "<white>Players: <gold>%maxPlayer%<gray>/<yellow>%player%",
              "",
              "<gold>play.astralpractice.com",
              "<gray><st>---------------------");

      @JsonProperty("STARTING")
      private List<String> starting =
          List.of(
              "<gray><st>---------------------",
              "<gold>Event Queue",
              "",
              "<white>Event: <gold>%eventName%",
              "<white>Players: <gold>%maxPlayer%<gray>/<yellow>%player%",
              "",
              "<white>Starting in: <gold>%timeLeft%",
              "",
              "<gold>play.astralpractice.com",
              "<gray><st>---------------------");
    }
  }

  @Getter
  public static class Match {
    @JsonProperty("ROUND-SYMBOL")
    private String roundSymbol = "⬤";

    @Comment("If wins-needed is above this, show rounds as won/total instead of repeated symbols.")
    @JsonProperty("ROUND-SYMBOL-MAX-ROUNDS")
    private int roundSymbolMaxRounds = 3;

    @JsonProperty("BED-STATUS")
    private BedStatus bedStatus = new BedStatus();

    @Comment("Basic sidebars if the ladder doesn't have a special one")
    @JsonProperty("DUEL")
    private List<String> duel =
        List.of(
            "<gray><st>---------------------",
            "<gold><b>Info",
            "  <white>Fighting: <yellow>%enemyNameOnly%",
            "  <white>Duration: <yellow>%roundDuration%",
            "  <white>Ping: <green>%ping%<gray>ms | <red>%enemyPing%<gray>ms",
            "",
            "<gold>play.astralpractice.com",
            "<gray><st>---------------------");

    @JsonProperty("PARTY_FFA")
    private List<String> partyFfa =
        List.of(
            "<gray><st>---------------------",
            "<gold><b>Info",
            "  <white>Players: <gold>%players%<gray>/<yellow>%alivePlayers%",
            "  <white>Duration: <yellow>%roundDuration%",
            "  <white>Ping: <green>%ping% ms",
            "",
            "<gold>play.astralpractice.com",
            "<gray><st>---------------------");

    @JsonProperty("PARTY_SPLIT")
    private List<String> partySplit =
        List.of(
            "<gray><st>---------------------",
            "<gold><b>Info",
            "  <white>Duration: <yellow>%roundDuration%",
            "  <white>Ping: <green>%ping% ms",
            "",
            " %team1name%<gray>: %team1color%%team1players%<gray>/<green>%team1alivePlayers%",
            " %team2name%<gray>: %team2color%%team2players%<gray>/<green>%team2alivePlayers%",
            "",
            "<gold>play.astralpractice.com",
            "<gray><st>---------------------");

    @JsonProperty("PARTY_VS_PARTY")
    private List<String> partyVsParty =
        List.of(
            "<gray><st>---------------------",
            "<gold><b>Info",
            "  <white>Duration: <yellow>%roundDuration%",
            "  <white>Ping: <green>%ping% ms",
            "",
            " %partyTeamName%<gray>:"
                + " %partyTeamColor%%partyTeamPlayers%<gray>/<green>%partyTeamAlivePlayers%",
            " %enemyTeamName%<gray>:"
                + " %enemyTeamColor%%enemyTeamPlayers%<gray>/<green>%enemyTeamAlivePlayers%",
            "",
            "<gold>play.astralpractice.com",
            "<gray><st>---------------------");

    @Comment({"Here you can set the sidebar for", "specific ladders."})
    @Merge(missingEntries = MissingEntries.OMIT)
    @JsonProperty("LADDER")
    private Map<String, LadderLines> ladder = defaultLadder();

    private static Map<String, LadderLines> defaultLadder() {
      Map<String, LadderLines> values = new LinkedHashMap<>();
      values.put("SUMO", LadderLines.matchSumo());
      values.put("PEARLFIGHT", LadderLines.matchPearlfight());
      values.put("BOXING", LadderLines.matchBoxing());
      values.put("BEDWARS", LadderLines.matchBedwars());
      values.put("FIREBALL", LadderLines.matchBedwars());
      values.put("BRIDGES", LadderLines.matchBridges());
      values.put("MLGRUSH", LadderLines.matchBridges());
      values.put("BATTLERUSH", LadderLines.matchBridges());

      return values;
    }

    @Getter
    public static class BedStatus {
      @JsonProperty("NOT-DESTROYED")
      private String notDestroyed = "<green>✓";

      @JsonProperty("DESTROYED")
      private String destroyed = "<red>✗";
    }
  }

  @Getter
  public static class Ffa {
    @Comment({
      "Available session placeholders:",
      "  %ffa_session_kills%, %ffa_session_deaths%, %ffa_session_streak%,"
          + " %ffa_session_best_streak%",
      "Available lifetime placeholders:",
      "  %ffa_lifetime_kills%, %ffa_lifetime_deaths%, %ffa_kdr%",
      "Other placeholders: %players%, %spectators%, %arena%, %ladder%, %nextReset%, %ping%"
    })
    @JsonProperty("GAME")
    private Game game = new Game();

    @JsonProperty("SPECTATE")
    private Spectate spectate = new Spectate();

    @Getter
    public static class Game {
      @JsonProperty("BUILD")
      private List<String> build =
          List.of(
              "<gray><st>---------------------",
              "<gold><b>Session",
              "  <white>Kills: <green>%ffa_session_kills% <gray>| "
                  + "<white>Deaths: <red>%ffa_session_deaths%",
              "  <white>Streak: <yellow>%ffa_session_streak% <gray>(Best: "
                  + "<yellow>%ffa_session_best_streak%<gray>)",
              "",
              "<gold><b>Lifetime",
              "  <white>Kills: <green>%ffa_lifetime_kills% <gray>| "
                  + "<white>Deaths: <red>%ffa_lifetime_deaths%",
              "  <white>KDR: <yellow>%ffa_kdr%",
              "",
              "<gold><b>FFA Info",
              "  <white>Players: <yellow>%players%",
              "  <white>Next Reset: <yellow>%nextReset%",
              "",
              "<gold>play.astralpractice.com",
              "<gray><st>---------------------");

      @JsonProperty("NON-BUILD")
      private List<String> nonBuild =
          List.of(
              "<gray><st>---------------------",
              "<gold><b>Session",
              "  <white>Kills: <green>%ffa_session_kills% <gray>| "
                  + "<white>Deaths: <red>%ffa_session_deaths%",
              "  <white>Streak: <yellow>%ffa_session_streak% <gray>(Best: "
                  + "<yellow>%ffa_session_best_streak%<gray>)",
              "",
              "<gold><b>Lifetime",
              "  <white>Kills: <green>%ffa_lifetime_kills% <gray>| "
                  + "<white>Deaths: <red>%ffa_lifetime_deaths%",
              "  <white>KDR: <yellow>%ffa_kdr%",
              "",
              "<gold><b>FFA Info",
              "  <white>Players: <yellow>%players%",
              "  <white>Arena: <green>%arena%",
              "",
              "<gold>play.astralpractice.com",
              "<gray><st>---------------------");
    }

    @Getter
    public static class Spectate {
      @JsonProperty("BUILD")
      private List<String> build =
          List.of(
              "<gray><st>---------------------",
              "<gold><b>FFA Info",
              "  <white>Player: <yellow>%players%",
              "  <white>Spectators: <yellow>%spectators%",
              "  <white>Next Reset: <yellow>%nextReset%",
              "",
              "  <white>Arena: <green>%arena%",
              "",
              "<gold>play.astralpractice.com",
              "<gray><st>---------------------");

      @JsonProperty("NON-BUILD")
      private List<String> nonBuild =
          List.of(
              "<gray><st>---------------------",
              "<gold><b>FFA Info",
              "  <white>Player: <yellow>%players%",
              "  <white>Spectators: <yellow>%spectators%",
              "",
              "  <white>Arena: <green>%arena%",
              "",
              "<gold>play.astralpractice.com",
              "<gray><st>---------------------");
    }
  }

  @Getter
  public static class Spectate {
    @JsonProperty("MATCH")
    private Match match = new Match();

    @JsonProperty("EVENT")
    private Map<String, List<String>> event = defaultEvent();

    @Comment("Permission: ap.admin.scoreboard")
    @JsonProperty("ADMIN-EXTENSION")
    private List<String> adminExtension =
        List.of(
            "<white>Arenas: <gold>%arenas%<gray>/<yellow>%enabledArenas%",
            "<gray><st>---------------------");

    private static Map<String, List<String>> defaultEvent() {
      Map<String, List<String>> values = new LinkedHashMap<>();
      values.put(
          "LMS",
          List.of(
              "<gray><st>---------------------",
              "<gold>Last Man Standing",
              "",
              "<white>Alive: <gold>%players%<gray>/<yellow>%alivePlayers% players",
              "<white>Duration: <gold>%duration%",
              "<gray><st>---------------------"));
      values.put(
          "OITC",
          List.of(
              "<gray><st>---------------------",
              "<gold>One In The Chamber",
              "",
              "<white>Highest Point:",
              "<gold>%topPlayer% + : <yellow>%topScore%",
              "",
              "<white>Alive: <gold>%players%<gray>/<yellow>%alivePlayers% players",
              "<white>Duration: <gold>%duration%",
              "<gray><st>---------------------"));
      values.put(
          "TNTTAG",
          List.of(
              "<gray><st>---------------------",
              "<gold>TNT Tag",
              "",
              "<white>Explosion in <gold>%explosionTime%s",
              "",
              "<white>Alive: <gold>%alivePlayers% players",
              "<gray><st>---------------------"));
      values.put(
          "BRACKETS",
          List.of(
              "<gray><st>---------------------",
              "<gold>Brackets",
              "",
              "<white>Alive: <gold>%players%<gray>/<yellow>%alivePlayers% players",
              "<white>Time Left: <gold>%timeLeft%",
              "",
              "<gray>Round #%round%",
              "<gray><st>---------------------"));
      values.put(
          "SUMO",
          List.of(
              "<gray><st>---------------------",
              "<gold>Sumo",
              "",
              "<white>Alive: <gold>%players%<gray>/<yellow>%alivePlayers% players",
              "<white>Time Left: <gold>%timeLeft%",
              "",
              "<gray>Round #%round%",
              "<gray><st>---------------------"));
      values.put(
          "SPLEGG",
          List.of(
              "<gray><st>---------------------",
              "<gold>Splegg",
              "",
              "<white>Alive: <gold>%players%<gray>/<yellow>%alivePlayers% players",
              "<white>Time Left: <gold>%timeLeft%",
              "<gray><st>---------------------"));
      values.put(
          "JUGGERNAUT",
          List.of(
              "<gray><st>---------------------",
              "<gold>Juggernaut",
              "",
              "<white>Alive: <gold>%players%<gray>/<yellow>%alivePlayers% players",
              "<white>Duration: <gold>%duration%",
              "<gray><st>---------------------"));

      return values;
    }

    @Getter
    public static class Match {
      @JsonProperty("DUEL")
      private List<String> duel =
          List.of(
              "<gray><st>---------------------",
              "<gold><b>Info",
              "  <white>Kit: <yellow>%ladder%",
              "  <white>Arena: <yellow>%arena%",
              "  <white>Duration: <yellow>%roundDuration%",
              "",
              "<gold>play.astralpractice.com",
              "<gray><st>---------------------");

      @JsonProperty("PARTY_FFA")
      private List<String> partyFfa =
          List.of(
              "<gray><st>---------------------",
              "<gold><b>Info",
              "  <white>Kit: <yellow>%ladder%",
              "  <white>Arena: <yellow>%arena%",
              "  <white>Duration: <yellow>%roundDuration%",
              "",
              "  <white>Players: <gold>%players%<gray>/<yellow>%alivePlayers%",
              "",
              "<gold>play.astralpractice.com",
              "<gray><st>---------------------");

      @JsonProperty("PARTY_SPLIT")
      private List<String> partySplit =
          List.of(
              "<gray><st>---------------------",
              "<gold><b>Info",
              "  <white>Kit: <yellow>%ladder%",
              "  <white>Arena: <yellow>%arena%",
              "  <white>Duration: <yellow>%roundDuration%",
              "",
              " %team1name%<gray>: %team1color%%team1players%<gray>/<green>%team1alivePlayers%",
              " %team2name%<gray>: %team2color%%team2players%<gray>/<green>%team2alivePlayers%",
              "",
              "<gold>play.astralpractice.com",
              "<gray><st>---------------------");

      @JsonProperty("PARTY_VS_PARTY")
      private List<String> partyVsParty =
          List.of(
              "<gray><st>---------------------",
              "<gold><b>Info",
              "  <white>Kit: <yellow>%ladder%",
              "  <white>Arena: <yellow>%arena%",
              "  <white>Duration: <yellow>%roundDuration%",
              "",
              " %team1name%<gray>: %team1color%%team1players%<gray>/<green>%team1alivePlayers%",
              " %team2name%<gray>: %team2color%%team2players%<gray>/<green>%team2alivePlayers%",
              "",
              "<gold>play.astralpractice.com",
              "<gray><st>---------------------");

      @Merge(missingEntries = MissingEntries.OMIT)
      @JsonProperty("LADDER")
      private Map<String, LadderLines> ladder = defaultLadder();

      private static Map<String, LadderLines> defaultLadder() {
        Map<String, LadderLines> values = new LinkedHashMap<>();
        values.put("SUMO", LadderLines.spectateSumo());
        values.put("PEARLFIGHT", LadderLines.spectatePearlfight());
        values.put("BOXING", LadderLines.spectateBoxing());
        values.put("BEDWARS", LadderLines.spectateBedwars());
        values.put("FIREBALL", LadderLines.spectateBedwars());
        values.put("BRIDGES", LadderLines.spectateBridges());
        values.put("MLGRUSH", LadderLines.spectateBridges());
        values.put("BATTLERUSH", LadderLines.spectateBridges());

        return values;
      }
    }
  }
}
