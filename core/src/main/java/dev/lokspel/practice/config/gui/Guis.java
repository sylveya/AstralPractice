package dev.lokspel.practice.config.gui;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.config.ItemConfig;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;

@Getter
public class Guis {
  @JsonProperty("ARENA-CREATE")
  private ArenaCreate arenaCreate = new ArenaCreate();

  @JsonProperty("LADDER-CREATE")
  private LadderCreate ladderCreate = new LadderCreate();

  @JsonProperty("VALIDATION-GUI")
  private ValidationGui validationGui = new ValidationGui();

  @Comment(
      "%ladder% display the Display Name of the ladder (official icon name), but you can also use"
          + " %ladderOriginal% which is the given name of the ladder.")
  @JsonProperty("KIT-EDITOR")
  private KitEditor kitEditor = new KitEditor();

  @JsonProperty("STATISTICS")
  private Statistics statistics = new Statistics();

  @JsonProperty("PARTY")
  private Party party = new Party();

  @JsonProperty("FFA")
  private Ffa ffa = new Ffa();

  @JsonProperty("DIVISION")
  private Division division = new Division();

  @JsonProperty("PLAYER-SETTINGS")
  private PlayerSettings playerSettings = new PlayerSettings();

  @Comment("/prac info <name> command")
  @JsonProperty("PLAYER-INFORMATION")
  private PlayerInformation playerInformation = new PlayerInformation();

  @JsonProperty("SELECTORS")
  private Selectors selectors = new Selectors();

  @Comment("You can set each event icon in the game.")
  @JsonProperty("EVENT-HOST")
  private EventHost eventHost = new EventHost();

  @JsonProperty("BRACKETS-KIT-SELECTOR")
  private BracketsKitSelector bracketsKitSelector = new BracketsKitSelector();

  @JsonProperty("LADDER-PREVIEW")
  private LadderPreview ladderPreview = new LadderPreview();

  @JsonProperty("MATCH-STATISTICS")
  private MatchStatistics matchStatistics = new MatchStatistics();

  @JsonProperty("PLAYER-INVENTORY")
  private PlayerInventory playerInventory = new PlayerInventory();

  @JsonProperty("UNRANKED-GUI")
  private QueueGui unrankedGui = QueueGui.unrankedGui();

  @JsonProperty("RANKED-GUI")
  private QueueGui rankedGui = QueueGui.rankedGui();

  @JsonProperty("CUSTOM-KIT-QUEUE")
  private CustomKitQueue customKitQueue = new CustomKitQueue();

  @JsonProperty("SPECTATOR-MENU")
  private SpectatorMenu spectatorMenu = new SpectatorMenu();

  @JsonProperty("SPECTATOR-TARGETS")
  private SpectatorTargets spectatorTargets = new SpectatorTargets();

  @JsonProperty("SETUP")
  private Setup setup = new Setup();

  @JsonProperty("COSMETICS")
  private Cosmetics cosmetics = new Cosmetics();

  @JsonProperty("MATCH-HISTORY")
  private MatchHistory matchHistory = new MatchHistory();

  @Getter
  public static class ArenaCreate {
    @JsonProperty("TITLE")
    private String title = "<gold>What type for <red>%arena%<gold>?";
  }

  @Getter
  public static class LadderCreate {
    @JsonProperty("TITLE")
    private String title = "<gold>What type for <red>%ladder%<gold>?";
  }

  @Getter
  public static class ValidationGui {
    @JsonProperty("TITLE")
    private String title = "<dark_gray>Validation";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("CANCEL")
      private ItemConfig cancel = ItemConfig.of("RED_STAINED_GLASS_PANE").name("<red>Cancel");

      @JsonProperty("APPROVE")
      private ItemConfig approve = ItemConfig.of("LIME_STAINED_GLASS_PANE").name("<green>Approve");
    }
  }

  @Getter
  public static class Statistics {
    @JsonProperty("SELECTOR")
    private Selector selector = new Selector();

    @JsonProperty("ELO-LEADERBOARD")
    private LeaderboardGui eloLeaderboard = LeaderboardGui.eloLeaderboard();

    @JsonProperty("WIN-LEADERBOARD")
    private LeaderboardGui winLeaderboard = LeaderboardGui.winLeaderboard();

    @JsonProperty("PLAYER-STATISTICS")
    private PlayerStatistics playerStatistics = new PlayerStatistics();

    @Getter
    public static class Selector {
      @JsonProperty("TITLE")
      private String title = "<red>Select statistics to view";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("FILLER-ITEM")
        private ItemConfig fillerItem = ItemConfig.of("BLACK_STAINED_GLASS_PANE");

        @JsonProperty("OWN-PLAYER-STATS")
        private OwnPlayerStats ownPlayerStats = new OwnPlayerStats();

        @JsonProperty("PLAYER-STATS")
        private PlayerStats playerStats = new PlayerStats();

        @JsonProperty("ELO-LEADERBOARD")
        private ItemConfig eloLeaderboard =
            ItemConfig.of("SNOWBALL")
                .name("<yellow>ELO Leaderboards")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to view the",
                    "<yellow>elo <gray>leaderboards.",
                    "<dark_gray><st>------------------------");

        @JsonProperty("TOP-WIN-LEADERBOARD")
        private ItemConfig topWinLeaderboard =
            ItemConfig.of("SLIME_BALL")
                .name("<gold>Top Win Leaderboard")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to view the",
                    "<gold>top win <gray>leaderboards.",
                    "<dark_gray><st>------------------------");

        @JsonProperty("VIEW-DIVISIONS")
        private ViewDivisions viewDivisions = new ViewDivisions();

        @Getter
        public static class OwnPlayerStats {
          @JsonProperty("NAME")
          private String name = "<red>%player% <gray>- <white>Statistic";

          @JsonProperty("LORE")
          private List<String> lore =
              List.of(
                  "<dark_gray><st>------------------------",
                  "<gray>Click here to view your personal",
                  "<gray>statistics for each kit.",
                  "<dark_gray><st>------------------------");
        }

        @Getter
        public static class PlayerStats {
          @JsonProperty("NAME")
          private String name = "<red>%target% <gray>- <white>Statistic";

          @JsonProperty("LORE")
          private List<String> lore =
              List.of(
                  "<dark_gray><st>------------------------",
                  "<gray>Click here to view",
                  "<red>%target%'s <gray>kit statistics.",
                  "<dark_gray><st>------------------------");
        }

        @Getter
        public static class ViewDivisions {
          @JsonProperty("HAS-NEXT")
          private ItemConfig hasNext =
              ItemConfig.of("BOOK")
                  .name("<red>Divisions")
                  .lore(
                      "",
                      " <red>┃ <white>Current Division: %division_fullName%",
                      " <red>┃ <white>Experience: <red>%exp%",
                      " <red>┃ <white>Wins: <red>%wins%",
                      " <red>┃ <white>Elo: <red>%elo%",
                      "",
                      " <red>┃ <white>Next Division: %nextDivision_fullName%",
                      " <red>┃ <white>Progress: <green>%progress_bar% <white>(%progress_percent%%)",
                      "",
                      "<green>Left Click <gray>to view all division.");

          @JsonProperty("NO-NEXT")
          private ItemConfig noNext =
              ItemConfig.of("BOOK")
                  .name("<red>Divisions")
                  .lore(
                      "",
                      " <red>┃ <white>Your Division: %division_fullName%",
                      " <red>┃ <white>Experience: <red>%exp%",
                      " <red>┃ <white>Wins: <red>%wins%",
                      " <red>┃ <white>Elo: <red>%elo%",
                      "",
                      "<yellow>You have reached the last division.");
        }
      }
    }

    @Getter
    public static class PlayerStatistics {
      @JsonProperty("TITLE")
      private String title = "<red>%player%'s <dark_gray>Stats";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("FILLER-ITEM")
        private ItemConfig fillerItem = ItemConfig.of("ORANGE_STAINED_GLASS_PANE").name(" ");

        @JsonProperty("BACK-TO-HUB")
        private ItemConfig backToHub = ItemConfig.of("ARROW").name("<yellow>Back to hub");

        @JsonProperty("ALL-STAT")
        private AllStat allStat = new AllStat();

        @JsonProperty("UNRANKED-LADDER-STATS")
        private ItemConfig unrankedLadderStats =
            new ItemConfig()
                .name("%ladder%")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<yellow>Unranked Wins: <white>%unranked_wins%",
                    "<yellow>Unranked Losses: <white>%unranked_losses%",
                    "<yellow>Unranked W/L Ratio: <white>%unranked_w/l_ratio%",
                    "<dark_gray><st>------------------------");

        @JsonProperty("RANKED-LADDER-STATS")
        private ItemConfig rankedLadderStats =
            new ItemConfig()
                .name("%ladder%")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<yellow>Ranked Wins: <white>%ranked_wins%",
                    "<yellow>Ranked Losses: <white>%ranked_losses%",
                    "<yellow>Ranked W/L Ratio: <white>%ranked_w/l_ratio%",
                    "",
                    "<yellow>ELO: <white>%elo%",
                    "<dark_gray><st>------------------------");

        @JsonProperty("UNRANKED-RANKED-STATS")
        private ItemConfig unrankedRankedStats =
            new ItemConfig()
                .name("%ladder%")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<yellow>Unranked Wins: <white>%unranked_wins%",
                    "<yellow>Unranked Losses: <white>%unranked_losses%",
                    "<yellow>Unranked W/L Ratio: <white>%unranked_w/l_ratio%",
                    "",
                    "<yellow>Ranked Wins: <white>%ranked_wins%",
                    "<yellow>Ranked Losses: <white>%ranked_losses%",
                    "<yellow>Ranked W/L Ratio: <white>%ranked_w/l_ratio%",
                    "",
                    "<yellow>ELO: <white>%elo%",
                    "",
                    "<yellow>Overall W/L Ratio: <white>%overall_w/l_ratio%",
                    "<yellow>Division: %division%",
                    "<dark_gray><st>------------------------");

        @Getter
        public static class AllStat {
          @JsonProperty("NAME")
          private String name = "<red>%player% <gray>- <white>Statistics";

          @JsonProperty("LORE")
          private List<String> lore =
              List.of(
                  "<dark_gray><st>------------------------",
                  "<yellow>Unranked Wins: <white>%unranked_wins%",
                  "<yellow>Unranked Losses: <white>%unranked_losses%",
                  "<yellow>Unranked W/L Ratio: <white>%unranked_w/l_ratio%",
                  "",
                  "<yellow>Ranked Wins: <white>%ranked_wins%",
                  "<yellow>Ranked Losses: <white>%ranked_losses%",
                  "<yellow>Ranked W/L Ratio: <white>%ranked_w/l_ratio%",
                  "",
                  "<yellow>Wins: <white>%global_wins%",
                  "<yellow>Losses: <white>%global_losses%",
                  "<yellow>W/L Ratio: <white>%w/l_ratio%",
                  "",
                  "<yellow>Global ELO: <white>%global_elo%",
                  "<yellow>Division: %division%",
                  "<dark_gray><st>------------------------");
        }
      }
    }
  }

  @Getter
  public static class Ffa {
    @JsonProperty("ARENA-SELECTOR")
    private ArenaSelector arenaSelector = new ArenaSelector();

    @JsonProperty("LADDER-SELECTOR")
    private LadderSelector ladderSelector = new LadderSelector();

    @Getter
    public static class ArenaSelector {
      @JsonProperty("TITLE")
      private String title = "<dark_gray>Select an <gold>FFA Arena";

      @JsonProperty("ROWS")
      private int rows = 3;

      @Comment({
        "Arena slots (0-based, 0 = top-left), for example:",
        "ARENA-SLOTS:",
        "  plains: 11"
      })
      @JsonProperty("ARENA-SLOTS")
      private Map<String, Integer> arenaSlots = new LinkedHashMap<>();

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("ARENA")
        private Arena arena = new Arena();

        @JsonProperty("FILLER")
        private ItemConfig filler = ItemConfig.of("ORANGE_STAINED_GLASS_PANE");

        @Getter
        public static class Arena extends ItemConfig {
          @JsonProperty("BUILD-STATUS")
          private BuildStatus buildStatus = new BuildStatus();

          {
            name("<gold>%arena%");
            lore(
                "",
                "<yellow>Build: <white>%build_status%",
                "<yellow>Re-Kit After Kill: <white>%rekit_after_kill%",
                "<yellow>Health Reset On Kill: <white>%health_reset_on_kill%",
                "<yellow>Lobby After Death: <white>%lobby_after_death%",
                "<yellow>Ladders: <white>%ladders%",
                "<yellow>Players: <white>%players%",
                "",
                "<green>Click to join this arena!");
          }

          @Getter
          public static class BuildStatus {
            @JsonProperty("ENABLED")
            private String enabled = "<green>Enabled";

            @JsonProperty("DISABLED")
            private String disabled = "<red>Disabled";
          }
        }
      }
    }

    @Getter
    public static class LadderSelector {
      @JsonProperty("TITLE")
      private String title = "<dark_gray>Select a <gold>ladder <dark_gray>to join";

      @JsonProperty("ROWS")
      private int rows = 3;

      @Comment({
        "Ladder slots (0-based, 0 = top-left), for example:",
        "LADDER-SLOTS:",
        "  NoDebuff: 11"
      })
      @JsonProperty("LADDER-SLOTS")
      private Map<String, Integer> ladderSlots = new LinkedHashMap<>();

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("LADDER")
        private ItemConfig ladder =
            new ItemConfig()
                .name("%ladder%")
                .lore("", "<green>Click here to select %ladder%<green>.");

        @JsonProperty("FILLER")
        private ItemConfig filler = ItemConfig.of("ORANGE_STAINED_GLASS_PANE");
      }
    }
  }

  @Getter
  public static class Division {
    @JsonProperty("TITLE")
    private String title = "<dark_gray>Division";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("BACK-TO")
      private ItemConfig backTo = ItemConfig.of("ARROW").name("<red>Back to");

      @JsonProperty("FILLER-ITEM")
      private ItemConfig fillerItem = ItemConfig.of("BLACK_STAINED_GLASS_PANE");

      @JsonProperty("FILLER-ITEM2")
      private ItemConfig fillerItem2 = ItemConfig.of("ORANGE_STAINED_GLASS_PANE");

      @JsonProperty("PAST-DIVISION")
      private ItemConfig pastDivision =
          new ItemConfig()
              .name("%fullName% <red>(Past)")
              .lore(
                  "",
                  " %color%┃ <white>Progress: <green>%progress_bar% <white>(%progress_percent%%)",
                  "",
                  " <red>┃ <white>You have passed this division.");

      @JsonProperty("CURRENT-DIVISION")
      private ItemConfig currentDivision =
          new ItemConfig()
              .name("%fullName% <green>(Current)")
              .lore(
                  "",
                  " %color%┃ <white>Progress: <green>%progress_bar% <white>(%progress_percent%%)",
                  "",
                  " <red>┃ <white>This is your current division.");

      @JsonProperty("NEXT-DIVISION")
      private ItemConfig nextDivision =
          new ItemConfig()
              .name("%fullName%")
              .lore(
                  "",
                  " %color%┃ <white>Wins: %color%%current_wins%/%required_wins%"
                      + " <white>(%win_progress_percent%%)",
                  " %color%┃ <white>Elo: %color%%current_elo%/%required_elo%"
                      + " <white>(%elo_progress_percent%%)",
                  " %color%┃ <white>Experience: %color%%current_exp%/%required_exp%"
                      + " <white>(%exp_progress_percent%%)",
                  "",
                  " %color%┃ <white>Progress: <green>%progress_bar% <white>(%progress_percent%%)",
                  "");
    }
  }

  @Getter
  public static class PlayerSettings {
    @Comment("%player%")
    @JsonProperty("TITLE")
    private String title = "<dark_gray>Player Settings";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("FILLER-ITEM")
      private ItemConfig fillerItem = ItemConfig.of("BLACK_STAINED_GLASS_PANE").name(" ");

      @JsonProperty("DUEL-REQUEST")
      private DuelRequest duelRequest = new DuelRequest();

      @JsonProperty("AUTO-QUEUE")
      private AutoQueue autoQueue = new AutoQueue();

      @JsonProperty("SIDEBAR")
      private Sidebar sidebar = new Sidebar();

      @Getter
      public static class DuelRequest {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("DIAMOND_SWORD")
                .name("<red>Toggle Duel Requests")
                .lore(
                    "<dark_gray><st>--------------------------------",
                    "<gray>If enabled, player will be",
                    "<gray>able to send you duel requests.",
                    "",
                    " <green><bold>► <gray>Allow other players to duel you.",
                    " <gray><bold>► <gray>Don't allow other players to duel you.",
                    "<dark_gray><st>--------------------------------");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("DIAMOND_SWORD")
                .name("<red>Toggle Duel Requests")
                .lore(
                    "<dark_gray><st>--------------------------------",
                    "<gray>If enabled, player will be",
                    "<gray>able to send you duel requests.",
                    "",
                    " <gray><bold>► <gray>Allow other players to duel you.",
                    " <red><bold>► <gray>Don't allow other players to duel you.",
                    "<dark_gray><st>--------------------------------");
      }

      @Getter
      public static class AutoQueue {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("GOLDEN_SWORD")
                .name("<aqua>Toggle Auto-Queue")
                .lore(
                    "<dark_gray><st>--------------------------------",
                    "<gray>If enabled, you will be",
                    "<gray>automatically re-queued into",
                    "<gray>the same ladder after a match.",
                    "",
                    " <green><bold>► <gray>Auto-queue after matches.",
                    " <gray><bold>► <gray>Return to the lobby after matches.",
                    "<dark_gray><st>--------------------------------");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("GOLDEN_SWORD")
                .name("<aqua>Toggle Auto-Queue")
                .lore(
                    "<dark_gray><st>--------------------------------",
                    "<gray>If enabled, you will be",
                    "<gray>automatically re-queued into",
                    "<gray>the same ladder after a match.",
                    "",
                    " <gray><bold>► <gray>Auto-queue after matches.",
                    " <red><bold>► <gray>Return to the lobby after matches.",
                    "<dark_gray><st>--------------------------------");
      }

      @Getter
      public static class Sidebar {
        @JsonProperty("ENABLED")
        private ItemConfig enabled =
            ItemConfig.of("PAINTING")
                .name("<gold>Toggle Scoreboard")
                .lore(
                    "<dark_gray><st>--------------------------------",
                    "<gray>If enabled, you will be able",
                    "<gray>to see the scoreboard.",
                    "",
                    " <green><bold>► <gray>Show scoreboard.",
                    " <gray><bold>► <gray>Hide scoreboard.",
                    "<dark_gray><st>--------------------------------");

        @JsonProperty("DISABLED")
        private ItemConfig disabled =
            ItemConfig.of("PAINTING")
                .name("<gold>Toggle Scoreboard")
                .lore(
                    "<dark_gray><st>--------------------------------",
                    "<gray>If enabled, you will be able",
                    "<gray>to see the scoreboard.",
                    "",
                    " <gray><bold>► <gray>Show scoreboard.",
                    " <red><bold>► <gray>Hide scoreboard.",
                    "<dark_gray><st>--------------------------------");

        @JsonProperty("PARTY-INVITE")
        private PartyInvite partyInvite = new PartyInvite();

        @JsonProperty("PRIVATE-MESSAGE")
        private PrivateMessage privateMessage = new PrivateMessage();

        @JsonProperty("HIDE-PLAYERS")
        private HidePlayers hidePlayers = new HidePlayers();

        @JsonProperty("ALLOW-SPECTATORS")
        private AllowSpectators allowSpectators = new AllowSpectators();

        @JsonProperty("FLYING")
        private Flying flying = new Flying();

        @JsonProperty("WORLD-TIME")
        private ItemConfig worldTime =
            ItemConfig.of("BLAZE_POWDER")
                .name("<yellow>World Time: <green>%worldTime%")
                .lore(
                    "<dark_gray><st>--------------------------------",
                    "<gray>Click here to change time.",
                    "<dark_gray><st>--------------------------------");

        @JsonProperty("PREFIX-VISIBILITY")
        private ItemConfig prefixVisibility =
            ItemConfig.of("NAME_TAG")
                .name("<light_purple>Name Decorations: <white>%prefixVisibility%")
                .lore(
                    "<dark_gray><st>--------------------------------",
                    "<gray>Choose what to show from your",
                    "<gray>chat/tab/sidebar/nametag decorations.",
                    "",
                    "<green>Modes: NONE, PREFIX, SUFFIX, BOTH",
                    "<dark_gray><st>--------------------------------");

        @Getter
        public static class PartyInvite {
          @JsonProperty("ENABLED")
          private ItemConfig enabled =
              ItemConfig.of("FEATHER")
                  .name("<light_purple>Party Invite Requests")
                  .lore(
                      "<dark_gray><st>--------------------------------",
                      "<gray>If enabled, players will be able",
                      "<gray>to send you party invites.",
                      "",
                      " <green><bold>► <gray>Allow party leaders to invite you.",
                      " <gray><bold>► <gray>Don't allow party leaders to invite you.",
                      "<dark_gray><st>--------------------------------");

          @JsonProperty("DISABLED")
          private ItemConfig disabled =
              ItemConfig.of("FEATHER")
                  .name("<light_purple>Party Invite Requests")
                  .lore(
                      "<dark_gray><st>--------------------------------",
                      "<gray>If enabled, players will be able",
                      "<gray>to send you party invites.",
                      "",
                      " <gray><bold>► <gray>Allow party leaders to invite you.",
                      " <red><bold>► <gray>Don't allow party leaders to invite you.",
                      "<dark_gray><st>--------------------------------");
        }

        @Getter
        public static class PrivateMessage {
          @JsonProperty("ENABLED")
          private ItemConfig enabled =
              ItemConfig.of("PAPER")
                  .name("<blue>Private Messages")
                  .lore(
                      "<dark_gray><st>--------------------------------",
                      "<gray>If enabled, players will be",
                      "<gray>able to message you.",
                      "",
                      " <green><bold>► <gray>Let players send messages to you.",
                      " <gray><bold>► <gray>Don't let players send messages to you.",
                      "<dark_gray><st>--------------------------------");

          @JsonProperty("DISABLED")
          private ItemConfig disabled =
              ItemConfig.of("PAPER")
                  .name("<blue>Private Messages")
                  .lore(
                      "<dark_gray><st>--------------------------------",
                      "<gray>If enabled, players will be",
                      "<gray>able to message you.",
                      "",
                      " <gray><bold>► <gray>Let players send messages to you.",
                      " <red><bold>► <gray>Don't let players send messages to you.",
                      "<dark_gray><st>--------------------------------");
        }

        @Getter
        public static class HidePlayers {
          @JsonProperty("ENABLED")
          private ItemConfig enabled =
              ItemConfig.of("GHAST_TEAR")
                  .name("<blue>Player Visibility")
                  .lore(
                      "<dark_gray><st>--------------------------------",
                      "<gray>If enabled, you will be able to",
                      "<gray>see other players in the lobby.",
                      "",
                      " <green><bold>► <gray>See players in the lobby.",
                      " <gray><bold>► <gray>Don't see players in the lobby.",
                      "<dark_gray><st>--------------------------------");

          @JsonProperty("DISABLED")
          private ItemConfig disabled =
              ItemConfig.of("GHAST_TEAR")
                  .name("<blue>Player Visibility")
                  .lore(
                      "<dark_gray><st>--------------------------------",
                      "<gray>If enabled, you will be able to",
                      "<gray>see other players in the lobby.",
                      "",
                      " <gray><bold>► <gray>See players in the lobby.",
                      " <red><bold>► <gray>Don't see players in the lobby.",
                      "<dark_gray><st>--------------------------------");
        }

        @Getter
        public static class AllowSpectators {
          @JsonProperty("ENABLED")
          private ItemConfig enabled =
              ItemConfig.of("EMERALD")
                  .name("<gray>Allow Spectators")
                  .lore(
                      "<dark_gray><st>--------------------------------",
                      "<gray>If enabled, players can",
                      "<gray>spectate your matches.",
                      "",
                      " <green><bold>► <gray>Let players spectate your matches.",
                      " <gray><bold>► <gray>Don't let players spectate your matches.",
                      "<dark_gray><st>--------------------------------");

          @JsonProperty("DISABLED")
          private ItemConfig disabled =
              ItemConfig.of("EMERALD")
                  .name("<gray>Allow Spectators")
                  .lore(
                      "<dark_gray><st>--------------------------------",
                      "<gray>If enabled, players can",
                      "<gray>spectate your matches.",
                      "",
                      " <gray><bold>► <gray>Let players spectate your matches.",
                      " <red><bold>► <gray>Don't let players spectate your matches.",
                      "<dark_gray><st>--------------------------------");
        }

        @Getter
        public static class Flying {
          @JsonProperty("ENABLED")
          private ItemConfig enabled =
              ItemConfig.of("ELYTRA")
                  .name("<gray>Flying")
                  .lore(
                      "<dark_gray><st>--------------------------------",
                      "<gray>If enabled, you can fly in the lobby.",
                      "",
                      " <green><bold>► <gray>Enabled.",
                      " <gray><bold>► <gray>Disabled.",
                      "<dark_gray><st>--------------------------------");

          @JsonProperty("DISABLED")
          private ItemConfig disabled =
              ItemConfig.of("ELYTRA")
                  .name("<gray>Flying")
                  .lore(
                      "<dark_gray><st>--------------------------------",
                      "<gray>If enabled, you can fly in the lobby.",
                      "",
                      " <gray><bold>► <gray>Enabled.",
                      " <red><bold>► <gray>Disabled.",
                      "<dark_gray><st>--------------------------------");
        }
      }
    }
  }

  @Getter
  public static class PlayerInformation {
    @JsonProperty("MAIN-PAGE")
    private MainPage mainPage = new MainPage();

    @JsonProperty("LADDER-STATS")
    private LadderStats ladderStats = new LadderStats();

    @Getter
    public static class MainPage {
      @JsonProperty("TITLE")
      private String title = "<red>%player%'s <dark_gray>info";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("REFRESH")
        private ItemConfig refresh =
            ItemConfig.of("NETHER_STAR")
                .name("<green>Refresh Page")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to refresh the page.",
                    "<dark_gray><st>------------------------");

        @JsonProperty("BASIC-INFO")
        private BasicInfo basicInfo = new BasicInfo();

        @JsonProperty("RANKED-BAN")
        private ItemConfig rankedBan =
            ItemConfig.of("EMERALD")
                .name("<gold>%player% <gray>is banned from playing ranked.")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<yellow>Banner: <gray>%banner%",
                    "<yellow>Reason: <gray>%reason%",
                    "<yellow>Banned at: <gray>%time%",
                    "<dark_gray><st>------------------------",
                    "<green>Click here <gray>to <yellow>unban <gray>the player.",
                    "<dark_gray><st>------------------------");

        @JsonProperty("ONLINE-INFO")
        private OnlineInfo onlineInfo = new OnlineInfo();

        @JsonProperty("PARTY")
        private PartySection party = new PartySection();

        @JsonProperty("STATISTICS")
        private ItemConfig statistics =
            ItemConfig.of("FIREWORK_STAR")
                .name("<red>Statistics")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to view the",
                    "<gray>player's statistics.",
                    "<dark_gray><st>------------------------");

        @Getter
        public static class BasicInfo {
          @JsonProperty("NAME")
          private String name = "<gold>Basic Informations";

          @JsonProperty("ONLINE-STATUS")
          private String onlineStatus = "<green>Online";

          @JsonProperty("LORE")
          private List<String> lore =
              List.of(
                  "<dark_gray><st>------------------------",
                  "<yellow>Name: <gray>%player%",
                  "<yellow>UUID: <gray>%uuid%",
                  "",
                  "<yellow>First Played: <gray>%first_played%",
                  "<yellow>Last Joined: <gray>%last_played%",
                  "",
                  "<yellow>Unranked Left: <gray>%unranked_left%",
                  "<yellow>Ranked Left: <gray>%ranked_left%",
                  "",
                  "<yellow>Division: <gray>%division_fullName%",
                  "<dark_gray><st>------------------------");
        }

        @Getter
        public static class OnlineInfo {
          @JsonProperty("PLAYER-OFFLINE")
          private ItemConfig playerOffline =
              ItemConfig.of("FEATHER").name("<red>Player is not online!");

          @JsonProperty("PLAYER-ONLINE")
          private ItemConfig playerOnline =
              ItemConfig.of("FEATHER")
                  .name("<gold>Online Players Information's")
                  .lore(
                      "<dark_gray><st>------------------------",
                      "<yellow>In World: <gray>%world%",
                      "<yellow>Gamemode: <gray>%gamemode%",
                      "<yellow>Flying: <gray>%flying%",
                      "<yellow>Tablist Name: <gray>%tablist_name%",
                      "<yellow>Health: <gray>%health%",
                      "<yellow>Food Level: <gray>%food%",
                      "<yellow>Hit Delay: <gray>%hit_delay%",
                      "<yellow>Ping: <gray>%ping%ms",
                      "<dark_gray><st>------------------------");

          @JsonProperty("GAME")
          private Game game = new Game();

          @Getter
          public static class Game {
            @JsonProperty("OFFLINE")
            private ItemConfig offline = ItemConfig.of("REDSTONE").name("<red>Player is offline");

            @JsonProperty("IN-MATCH")
            private ItemConfig inMatch =
                ItemConfig.of("CLOCK")
                    .name("<yellow>Player is in a match.")
                    .lore(
                        "<dark_gray><st>------------------------",
                        "<yellow>Left-Click <gray>to start spectating the match.",
                        "<gold>Right-Click <gray>to get the player out of the match.",
                        "<dark_gray><st>------------------------");

            @JsonProperty("IN-EVENT")
            private ItemConfig inEvent =
                ItemConfig.of("CLOCK")
                    .name("<yellow>Player is in a event.")
                    .lore(
                        "<dark_gray><st>------------------------",
                        "<yellow>Left-Click <gray>to start spectating the event.",
                        "<gold>Right-Click <gray>to get the player out of the event.",
                        "<dark_gray><st>------------------------");

            @JsonProperty("IN-FFA")
            private ItemConfig inFfa =
                ItemConfig.of("CLOCK")
                    .name("<yellow>Player is in a FFA.")
                    .lore(
                        "<dark_gray><st>------------------------",
                        "<yellow>Left-Click <gray>to start spectating the FFA.",
                        "<gold>Right-Click <gray>to get the player out of the FFA.",
                        "<dark_gray><st>------------------------");

            @JsonProperty("SPECTATING")
            private ItemConfig spectating =
                ItemConfig.of("CLOCK")
                    .name("<yellow>Player is spectating.")
                    .lore(
                        "<dark_gray><st>------------------------",
                        "<yellow>Left-Click <gray>to start spectating the same thing.",
                        "<gold>Right-Click <gray>to end the spectating.",
                        "<dark_gray><st>------------------------");

            @JsonProperty("NOTHING")
            private ItemConfig nothing =
                ItemConfig.of("REDSTONE")
                    .name("<red>Player does nothing")
                    .lore(
                        "<dark_gray><st>------------------------",
                        "<gray>Player does not play in a",
                        "<gray>match or event and does not",
                        "<gray>spectate anything.",
                        "<dark_gray><st>------------------------");
          }
        }

        @Getter
        public static class PartySection {
          @JsonProperty("IN-PARTY")
          private ItemConfig inParty =
              ItemConfig.of("NAME_TAG").name("<green>Player is in a party.");

          @JsonProperty("NOT-IN-PARTY")
          private ItemConfig notInParty =
              ItemConfig.of("NAME_TAG").name("<red>Player is not in a party.");
        }
      }
    }

    @Getter
    public static class LadderStats {
      @JsonProperty("TITLE")
      private String title = "<dark_gray>%player% ladder stats";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("BACK-TO-HUB")
        private ItemConfig backToHub = ItemConfig.of("ARROW").name("<yellow>Back to hub");

        @JsonProperty("REFRESH")
        private ItemConfig refresh =
            ItemConfig.of("NETHER_STAR")
                .name("<green>Refresh Page")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to refresh the page.",
                    "<dark_gray><st>------------------------");

        @JsonProperty("RESET-ALL-STATS")
        private ItemConfig resetAllStats =
            ItemConfig.of("GHAST_TEAR")
                .name("<red>Reset ALL Stats")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to restart all",
                    "<gray>the player's ladder statistics.",
                    "<dark_gray><st>------------------------");

        @JsonProperty("PAGE-LEFT")
        private ItemConfig pageLeft = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

        @JsonProperty("PAGE-RIGHT")
        private ItemConfig pageRight = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

        @JsonProperty("LADDER")
        private Ladder ladder = new Ladder();

        @Getter
        public static class Ladder {
          @JsonProperty("NULL-STAT")
          private String nullStat = "<red>N/A";

          @JsonProperty("UNRANKED-LADDER-STATS")
          private UnrankedLadderStats unrankedLadderStats = new UnrankedLadderStats();

          @JsonProperty("RANKED-LADDER-STATS")
          private RankedLadderStats rankedLadderStats = new RankedLadderStats();

          @Getter
          public static class UnrankedLadderStats {
            @JsonProperty("NAME")
            private String name = "<yellow>%ladder% ladder";

            @JsonProperty("DEFAULT-MATERIAL")
            private String defaultMaterial = "BEDROCK";

            @JsonProperty("LORE")
            private List<String> lore =
                List.of(
                    "<dark_gray><st>------------------------",
                    "<yellow>Wins: <white>%wins%",
                    "<yellow>Losses: <white>%losses%",
                    "",
                    "<yellow>Custom Kits: <white>%custom_kits%",
                    "<dark_gray><st>------------------------",
                    "<red>Click here <gray>to reset the ladder stats.",
                    "<dark_gray><st>------------------------");
          }

          @Getter
          public static class RankedLadderStats {
            @JsonProperty("NAME")
            private String name = "<yellow>%ladder% ladder";

            @JsonProperty("DEFAULT-MATERIAL")
            private String defaultMaterial = "BEDROCK";

            @JsonProperty("LORE")
            private List<String> lore =
                List.of(
                    "<dark_gray><st>------------------------",
                    "<yellow>Unranked Wins: <white>%unranked_wins%",
                    "<yellow>Unranked Losses: <white>%unranked_losses%",
                    "<yellow>Unranked Custom Kits: <white>%unranked_custom_kits%",
                    "",
                    "<yellow>Ranked Wins: <white>%ranked_wins%",
                    "<yellow>Ranked Losses: <white>%ranked_losses%",
                    "<yellow>Ranked Custom Kits: <white>%ranked_custom_kits%",
                    "",
                    "<yellow>ELO: <white>%elo%",
                    "<dark_gray><st>------------------------",
                    "<red>Click here <gray>to reset the ladder stats.",
                    "<dark_gray><st>------------------------");
          }
        }
      }
    }
  }

  @Getter
  public static class Selectors {
    @JsonProperty("ARENA-SELECTOR")
    private ArenaSelector arenaSelector = new ArenaSelector();

    @JsonProperty("LADDER-SELECTOR")
    private LadderSelector ladderSelector = new LadderSelector();

    @Getter
    public static class ArenaSelector {
      @JsonProperty("TITLE")
      private String title = "%matchType% <dark_gray>- Arena";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("BACK-TO-SELECTOR")
        private ItemConfig backToSelector =
            ItemConfig.of("ARROW").name("<yellow>Back to kit selector");

        @JsonProperty("RANDOM-ARENA")
        private ItemConfig randomArena =
            ItemConfig.of("NETHER_STAR")
                .name("<gold>Random Arena")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to pick a",
                    "<yellow>random <gray>arena for the match.",
                    "<dark_gray><st>------------------------");

        @JsonProperty("ARENA-ICON")
        private ArenaIcon arenaIcon = new ArenaIcon();

        @JsonProperty("PAGE-LEFT")
        private ItemConfig pageLeft = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

        @JsonProperty("PAGE-RIGHT")
        private ItemConfig pageRight = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

        @Getter
        public static class ArenaIcon {
          @JsonProperty("NAME")
          private String name = "%arena%";

          @JsonProperty("LORE")
          private List<String> lore =
              List.of(
                  "<dark_gray><st>------------------------",
                  "<gray>Click here to select",
                  "<gray>arena %arena%<gray>.",
                  "<dark_gray><st>------------------------");
        }
      }
    }

    @Getter
    public static class LadderSelector {
      @JsonProperty("TITLE")
      private String title = "%matchType% <dark_gray>- Kit";

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Icons {
        @JsonProperty("LADDER")
        private Ladder ladder = new Ladder();

        @JsonProperty("FILLER-ITEM")
        private ItemConfig fillerItem = ItemConfig.of("GRAY_STAINED_GLASS_PANE");

        @JsonProperty("BASE-CUSTOM-PLAYER-KIT-ICON")
        private ItemConfig baseCustomPlayerKitIcon =
            ItemConfig.of("DIAMOND_SWORD")
                .name("<gold>Custom Kit")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Click here to play",
                    "<gray>with your own custom kit.",
                    "<dark_gray><st>------------------------");

        @JsonProperty("PAGE-LEFT")
        private ItemConfig pageLeft = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

        @JsonProperty("PAGE-RIGHT")
        private ItemConfig pageRight = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

        @Getter
        public static class Ladder {
          @JsonProperty("NAME")
          private String name = "%ladder%";

          @JsonProperty("LORE")
          private List<String> lore =
              List.of(
                  "<dark_gray><st>------------------------",
                  "<gray>Click here to select",
                  "<gray>ladder %ladder%<gray>.",
                  "<dark_gray><st>------------------------");
        }
      }
    }
  }

  @Getter
  public static class EventHost {
    @JsonProperty("TITLE")
    private String title = "<dark_red>Host Event";
  }

  @Getter
  public static class BracketsKitSelector {
    @JsonProperty("TITLE")
    private String title = "<dark_gray>Brackets Kit Source";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("DEFAULT-KIT")
      private ItemConfig defaultKit =
          ItemConfig.of("CHEST")
              .name("<gold>Use Admin Preset")
              .lore(
                  "<dark_gray><st>------------------------",
                  "<gray>Start Brackets with the",
                  "<gray>configured admin event kit.",
                  "",
                  "<green>Click to select",
                  "<dark_gray><st>------------------------");

      @JsonProperty("CUSTOM-KIT")
      private ItemConfig customKit =
          ItemConfig.of("BOOK")
              .name("<green>%kitName%")
              .lore(
                  "<dark_gray><st>------------------------",
                  "<gray>Use this custom player kit",
                  "<gray>for the entire Brackets event.",
                  "",
                  "<green>Click to select",
                  "<dark_gray><st>------------------------");

      @JsonProperty("NO-CUSTOM-KIT")
      private ItemConfig noCustomKit =
          ItemConfig.of("BARRIER")
              .name("<red>No available custom kits")
              .lore(
                  "<dark_gray><st>------------------------",
                  "<gray>Enable and configure your",
                  "<gray>custom kits in the player kit menu",
                  "<gray>to list them here.",
                  "<dark_gray><st>------------------------");
    }
  }

  @Getter
  public static class LadderPreview {
    @JsonProperty("TITLE")
    private String title = "<dark_gray>Preview of %ladder%";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("NO-EFFECT")
      private NoEffect noEffect = new NoEffect();

      @JsonProperty("HAS-EFFECT")
      private HasEffect hasEffect = new HasEffect();

      @Getter
      public static class NoEffect extends ItemConfig {
        @JsonProperty("DAMAGE")
        private int damage = 8233;

        {
          material("POTION");
          name("<dark_purple>Ladder Effects");
          lore(
              "<dark_gray><st>------------------------",
              "<gray>This ladder has no effects.",
              "<dark_gray><st>------------------------");
          flags("HIDE_POTION_EFFECTS");
        }
      }

      @Getter
      public static class HasEffect {
        @JsonProperty("FORMAT")
        private String format = "<light_purple>%name% %amplifier% <gray>for %time%";

        @JsonProperty("ICON")
        private Icon icon = new Icon();

        @Getter
        public static class Icon extends ItemConfig {
          @JsonProperty("DAMAGE")
          private int damage = 8233;

          {
            material("POTION");
            name("<dark_purple>Ladder Effects");
            lore(
                "<dark_gray><st>------------------------",
                "<yellow>You will get these effects,",
                "<yellow>when the game starts.",
                "",
                "%effects%",
                "<dark_gray><st>------------------------");
            flags("HIDE_POTION_EFFECTS");
          }
        }
      }
    }
  }

  @Getter
  public static class MatchStatistics {
    @JsonProperty("TITLE")
    private Title title = new Title();

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Title {
      @JsonProperty("MULTIPLE-ROUND")
      private String multipleRound = "<dark_gray>%player%'s Stat - R: %round%";

      @JsonProperty("SINGLE-ROUND")
      private String singleRound = "<dark_gray>%player%'s Stat";
    }

    @Getter
    public static class Icons {
      @JsonProperty("POTION")
      private Potion potion = new Potion();

      @JsonProperty("HEALTH")
      private ItemConfig health =
          ItemConfig.of("PLAYER_HEAD").name("<red>Health: <white>20/%end_hearth% <red>♥");

      @JsonProperty("HUNGER")
      private ItemConfig hunger =
          ItemConfig.of("COOKED_BEEF").name("<red>Hunger: <white>20/%end_hunger%");

      @JsonProperty("EFFECT")
      private Effect effect = new Effect();

      @JsonProperty("STATS")
      private ItemConfig stats =
          ItemConfig.of("DIAMOND_SWORD")
              .name("<red>Match Statistics")
              .lore(
                  "<dark_gray><st>------------------------",
                  " <white>✱ <red>Total Hits: <white>%total_hits%",
                  " <white>✱ <red>Total Hits Received: <white>%total_hits_received%",
                  "",
                  " <white>✱ <red>Longest Combo: <white>%longest_combo%",
                  " <white>✱ <red>Avarage CPS: <white>%avarage_cps%",
                  "<dark_gray><st>------------------------");

      @JsonProperty("VIEW-ROUND")
      private ItemConfig viewRound =
          ItemConfig.of("PAPER")
              .name(
                  "<gray><bold>Click-here <gray>to view your round <red>%round% <gray>statstics.");

      @Getter
      public static class Potion extends ItemConfig {
        @JsonProperty("DAMAGE")
        private int damage = 16421;

        {
          material("POTION");
          name("<red>Health Potions: <white>%potion_left%");
          lore(
              "<dark_gray><st>--------------------",
              "<red>Potions Thrown: <white>%potion_thrown%",
              "<red>Potions Missed: <white>%potion_missed%",
              "<red>Potion Accuracy: <white>%potion_accuracy%",
              "<dark_gray><st>--------------------");
        }
      }

      @Getter
      public static class Effect {
        @JsonProperty("NO-EFFECT")
        private ItemConfig noEffect =
            ItemConfig.of("BREWING_STAND")
                .name("<red>Potion Effects")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<red>Player didn't have any effects",
                    "<red>left at the end of the game.",
                    "<dark_gray><st>------------------------");

        @JsonProperty("HAS-EFFECT")
        private HasEffect hasEffect = new HasEffect();

        @Getter
        public static class HasEffect {
          @JsonProperty("FORMAT")
          private String format = " <white>✱ <red>%name% %amplifier% <gray>- <white> %time%";

          @JsonProperty("ICON")
          private Icon icon = new Icon();

          @Getter
          public static class Icon extends ItemConfig {
            @JsonProperty("DAMAGE")
            private int damage = 8233;

            {
              material("POTION");
              name("<red>Potion Effects");
              lore(
                  "<dark_gray><st>------------------------",
                  "%effects%",
                  "<dark_gray><st>------------------------");
            }
          }
        }
      }
    }
  }

  @Getter
  public static class PlayerInventory {
    @JsonProperty("TITLE")
    private String title = "<red>%player%'s <dark_gray>inventory";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("FILLER-ITEM")
      private ItemConfig fillerItem = ItemConfig.of("BLACK_STAINED_GLASS_PANE").name(" ");

      @JsonProperty("HEALTH")
      private ItemConfig health =
          ItemConfig.of("REDSTONE").name("<red>Health: <white>20/%health% <red>♥");

      @JsonProperty("HUNGER")
      private ItemConfig hunger =
          ItemConfig.of("COOKED_BEEF").name("<red>Hunger: <white>20/%hunger%");

      @JsonProperty("EFFECT")
      private Effect effect = new Effect();

      @Getter
      public static class Effect {
        @JsonProperty("FORMAT")
        private String format = " <white>✱ <red>%name% %amplifier% <gray>- <white> %time%";

        @JsonProperty("ICON")
        private Icon icon = new Icon();

        @Getter
        public static class Icon extends ItemConfig {
          @JsonProperty("DAMAGE")
          private int damage = 8233;

          {
            material("POTION");
            name("<red>Potion Effects");
            lore(
                "<dark_gray><st>------------------------",
                "%effects%",
                "<dark_gray><st>------------------------");
          }
        }
      }
    }
  }

  @Getter
  public static class CustomKitQueue {
    @JsonProperty("CHOOSE-TYPE")
    private ChooseType chooseType = new ChooseType();

    @JsonProperty("HOST-SELECTOR")
    private HostSelector hostSelector = new HostSelector();

    @JsonProperty("QUEUE-OTHERS")
    private QueueOthers queueOthers = new QueueOthers();

    @Getter
    public static class ChooseType {
      @JsonProperty("TITLE")
      private String title = "<dark_gray>Custom Kit Queue";

      @JsonProperty("SIZE")
      private int size = 3;

      @JsonProperty("SLOTS")
      private Slots slots = new Slots();

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Slots {
        @JsonProperty("HOST-OWN")
        private int hostOwn = 11;

        @JsonProperty("JOIN-OTHERS")
        private int joinOthers = 15;

        @JsonProperty("BACK")
        private int back = 18;
      }

      @Getter
      public static class Icons {
        @JsonProperty("FILLER-ITEM")
        private ItemConfig fillerItem = ItemConfig.of("BLACK_STAINED_GLASS_PANE").name(" ");

        @JsonProperty("QUEUE-OWN-KIT")
        private ItemConfig queueOwnKit =
            ItemConfig.of("WRITABLE_BOOK")
                .name("<green>Queue Your Own Kit")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Select one of your saved",
                    "<gray>custom kits and host it",
                    "",
                    "<yellow>Click to continue",
                    "<dark_gray><st>------------------------");

        @JsonProperty("QUEUE-OTHERS-KITS")
        private ItemConfig queueOthersKits =
            ItemConfig.of("BOOK")
                .name("<yellow>Queue Others Kits")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Join the first available",
                    "<gray>hosted custom-kit queue",
                    "",
                    "<yellow>Click to search",
                    "<dark_gray><st>------------------------");

        @JsonProperty("BACK")
        private ItemConfig back = ItemConfig.of("ARROW").name("<red>Back");
      }
    }

    @Getter
    public static class HostSelector {
      @JsonProperty("TITLE")
      private String title = "<dark_gray>Select Custom Kit To Host";

      @JsonProperty("SIZE")
      private int size = 3;

      @JsonProperty("SLOTS")
      private Slots slots = new Slots();

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Slots {
        @JsonProperty("BACK")
        private int back = 22;

        @JsonProperty("NO-KITS")
        private int noKits = 13;

        @JsonProperty("KIT-SLOTS")
        private List<Integer> kitSlots = List.of(10, 11, 12, 13, 14, 15, 16);
      }

      @Getter
      public static class Icons {
        @JsonProperty("FILLER-ITEM")
        private ItemConfig fillerItem = ItemConfig.of("BLACK_STAINED_GLASS_PANE").name(" ");

        @JsonProperty("NO-KITS")
        private ItemConfig noKits =
            ItemConfig.of("BARRIER")
                .name("<red>No Saved Custom Kits")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>Create and enable a custom kit",
                    "<gray>first, then come back here.",
                    "",
                    "<yellow>Use custom kit editor",
                    "<dark_gray><st>------------------------");

        @JsonProperty("BACK")
        private ItemConfig back = ItemConfig.of("ARROW").name("<red>Back");
      }
    }

    @Getter
    public static class QueueOthers {
      @JsonProperty("TITLE")
      private String title = "<dark_gray>Join Custom Kit Queue";

      @JsonProperty("SIZE")
      private int size = 3;

      @JsonProperty("SLOTS")
      private Slots slots = new Slots();

      @JsonProperty("ICONS")
      private Icons icons = new Icons();

      @Getter
      public static class Slots {
        @JsonProperty("NO-HOSTED")
        private int noHosted = 13;

        @JsonProperty("BACK")
        private int back = 22;
      }

      @Getter
      public static class Icons {
        @JsonProperty("FILLER-ITEM")
        private ItemConfig fillerItem = ItemConfig.of("BLACK_STAINED_GLASS_PANE").name(" ");

        @JsonProperty("NO-HOSTED")
        private ItemConfig noHosted =
            ItemConfig.of("BARRIER")
                .name("<red>No One Hosting")
                .lore(
                    "<dark_gray><st>------------------------",
                    "<gray>There are no custom kit",
                    "<gray>queues available at the moment.",
                    "",
                    "<yellow>Wait for others to host",
                    "<dark_gray><st>------------------------");

        @JsonProperty("BACK")
        private ItemConfig back = ItemConfig.of("ARROW").name("<red>Back");
      }
    }
  }

  @Getter
  public static class SpectatorMenu {
    @JsonProperty("TITLE")
    private String title = "<dark_gray>Spectator Menu - Page %page%";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("PAGE-LEFT")
      private ItemConfig pageLeft = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

      @JsonProperty("CLOSE")
      private ItemConfig close = ItemConfig.of("ARROW").name("<red>Close Spectator Menu");

      @JsonProperty("PAGE-RIGHT")
      private ItemConfig pageRight = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

      @JsonProperty("FILLER-ITEM")
      private ItemConfig fillerItem = ItemConfig.of("BLACK_STAINED_GLASS_PANE").name(" ");

      @JsonProperty("MATCH-ICON")
      private ItemConfig matchIcon =
          new ItemConfig()
              .name("%match_type%")
              .lore(
                  "<dark_gray>#%match_id%",
                  "%weight_class%",
                  "",
                  "<yellow>Match Type: <white>%match_type%",
                  "<yellow>Kit: <white>%ladder%",
                  "<yellow>Arena: <white>%arena%",
                  "<yellow>Round: <white>%round%",
                  "<yellow>Duration: <white>%roundDuration%",
                  "<yellow>Spectators: <white>%spectators%",
                  "",
                  "<green>» Click to spectate «");

      @JsonProperty("EVENT-ICON")
      private ItemConfig eventIcon =
          new ItemConfig()
              .name("<yellow>%event_type%")
              .lore(
                  "<yellow>Duration: <white>%event_duration%",
                  "<yellow>Players: <white>%players%",
                  "<yellow>Spectators: <white>%spectators%",
                  "",
                  "<green>» Click to spectate «");

      @JsonProperty("FFA-ICON")
      private FfaIcon ffaIcon = new FfaIcon();

      @Getter
      public static class FfaIcon extends ItemConfig {
        @JsonProperty("BUILD-STATUS")
        private BuildStatus buildStatus = new BuildStatus();

        {
          name("<gold>FFA");
          lore(
              "<yellow>Build: <white>%build_status%",
              "<yellow>Arena: <white>%arena%",
              "<yellow>Players: <white>%players%",
              "<yellow>Spectators: <white>%spectators%",
              "",
              "<green>» Click to spectate «");
        }

        @Getter
        public static class BuildStatus {
          @JsonProperty("ENABLED")
          private String enabled = "<green>Enabled";

          @JsonProperty("DISABLED")
          private String disabled = "<red>Disabled";
        }
      }
    }
  }

  @Getter
  public static class SpectatorTargets {
    @JsonProperty("TITLE")
    private String title = "<dark_gray>Fighters";

    @JsonProperty("ICONS")
    private Icons icons = new Icons();

    @Getter
    public static class Icons {
      @JsonProperty("PAGE-LEFT")
      private ItemConfig pageLeft = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

      @JsonProperty("PAGE-RIGHT")
      private ItemConfig pageRight = ItemConfig.of("ARROW").name("<gold>Go to Page %page%");

      @JsonProperty("TARGET-ITEM")
      private ItemConfig targetItem =
          new ItemConfig()
              .name("<yellow>%player%")
              .lore(
                  "<dark_gray><st>------------------------",
                  "<gray>Team: <white>%team%",
                  "<gray>Health: <white>%health%",
                  "",
                  "<green>» Click to teleport «");

      @JsonProperty("TEAM1-BANNER")
      private ItemConfig team1Banner =
          ItemConfig.of("BLUE_BANNER")
              .name("%team% <white>Team")
              .lore("<gray>Alive: <white>%alive%/%total%");

      @JsonProperty("TEAM2-BANNER")
      private ItemConfig team2Banner =
          ItemConfig.of("RED_BANNER")
              .name("%team% <white>Team")
              .lore("<gray>Alive: <white>%alive%/%total%");

      @JsonProperty("FILLER-ITEM")
      private ItemConfig fillerItem = ItemConfig.of("GRAY_STAINED_GLASS_PANE").name(" ");

      @JsonProperty("FILLER-ITEM2")
      private ItemConfig fillerItem2 = ItemConfig.of("BLACK_STAINED_GLASS_PANE").name(" ");
    }
  }

  @Getter
  public static class MatchHistory {
    @Comment("GUI title. %player% = the target player's name.")
    @JsonProperty("TITLE")
    private String title = "<dark_gray>Match History <gray>- <gold>%player%";

    @Comment("Total number of slots. Must be a multiple of 9, max 54.")
    @JsonProperty("SIZE")
    private int size = 27;

    @Comment({
      "When true, match items are automatically centred inside the GUI.",
      "Set false to start them at slot 0 (or use START-SLOT)."
    })
    @JsonProperty("CENTER-ITEMS")
    private boolean centerItems = true;

    @Comment("Override the first item slot. Set to -1 to use CENTER-ITEMS logic.")
    @JsonProperty("START-SLOT")
    private int startSlot = -1;

    @Comment("Filler item placed in every unused slot.")
    @JsonProperty("FILLER-ITEM")
    private ItemConfig fillerItem = ItemConfig.of("BLACK_STAINED_GLASS_PANE").name(" ");

    @JsonProperty("MATCH-ITEM")
    private MatchItem matchItem = new MatchItem();

    @Comment("Result label strings inserted into %result%.")
    @JsonProperty("MESSAGES")
    private Messages messages = new Messages();

    @Getter
    public static class MatchItem extends ItemConfig {
      @Comment({
        "Set to true to show the opponent's player head as the item.",
        "Set to false to use MATERIAL instead."
      })
      @JsonProperty("USE-PLAYER-HEAD")
      private boolean usePlayerHead = true;

      @Comment("Enchantment glow on the item when the viewer won this match.")
      @JsonProperty("GLOW-ON-WIN")
      private boolean glowOnWin = true;

      {
        material("PAPER");
        name("<yellow>Match vs <white>%opponent%");
        lore(
            "<dark_gray><st>--------------------",
            "<gray>Result: %result%",
            "<gray>Score: <white>%score%",
            "<gray>Kit: <white>%kit%",
            "<gray>Arena: <white>%arena%",
            "",
            "<gray>Your Health: %player_health%",
            "<gray>Opp Health: %opponent_health%",
            "",
            "<gray>Duration: <white>%duration%",
            "<gray>Played: <white>%date%",
            "<dark_gray><st>--------------------");
      }
    }

    @Getter
    public static class Messages {
      @JsonProperty("WIN")
      private String win = "<green>Win";

      @JsonProperty("LOSS")
      private String loss = "<red>Loss";

      @JsonProperty("DRAW")
      private String draw = "<yellow>Draw";
    }
  }
}
