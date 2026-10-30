package dev.lokspel.practice.config.language;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;

@Getter
public class Command {
  @JsonProperty("IGNORE-QUEUE")
  private IgnoreQueue ignoreQueue = new IgnoreQueue();

  @JsonProperty("NICK")
  private Nick nick = new Nick();

  @JsonProperty("QUEUES")
  private Queues queues = new Queues();

  @JsonProperty("LEAVE")
  private Leave leave = new Leave();

  @JsonProperty("DUEL")
  private Duel duel = new Duel();

  @JsonProperty("ACCEPT")
  private Accept accept = new Accept();

  @Comment("%arena% placeholder is available in every string if there is a specific arena.")
  @JsonProperty("ARENA")
  private CommandArena arena = new CommandArena();

  @JsonProperty("LADDER")
  private Ladder ladder = new Ladder();

  @JsonProperty("EVENT")
  private CommandEvent event = new CommandEvent();

  @JsonProperty("HOLOGRAM")
  private Hologram hologram = new Hologram();

  @JsonProperty("MATCH-STAT")
  private MatchStat matchStat = new MatchStat();

  @JsonProperty("COSMETICS")
  private Cosmetics cosmetics = new Cosmetics();

  @JsonProperty("MATCH-HISTORY")
  private MatchHistory matchHistory = new MatchHistory();

  @JsonProperty("PARTY")
  private Party party = new Party();

  @JsonProperty("PRACTICE")
  private Practice practice = new Practice();

  @JsonProperty("PREVIEW")
  private Preview preview = new Preview();

  @JsonProperty("PRIVATE-MESSAGE")
  private PrivateMessage privateMessage = new PrivateMessage();

  @JsonProperty("DIVISIONS")
  private Divisions divisions = new Divisions();

  @JsonProperty("SETTINGS")
  private Settings settings = new Settings();

  @JsonProperty("SETUP")
  private Setup setup = new Setup();

  @JsonProperty("SPECTATE")
  private Spectate spectate = new Spectate();

  @JsonProperty("STAFF")
  private Staff staff = new Staff();

  @JsonProperty("FOLLOW")
  private Follow follow = new Follow();

  @JsonProperty("STATISTICS")
  private Statistics statistics = new Statistics();

  @Getter
  public static class IgnoreQueue {
    @JsonProperty("NO-PERM")
    private String noPerm = "<red>You don't have permission.";

    @JsonProperty("USE")
    private String use = "<red>/ignorequeue <player>";

    @JsonProperty("PLAYER-OFFLINE")
    private String playerOffline = "<red>%target% cannot be found.";

    @JsonProperty("CANT-YOURSELF")
    private String cantYourself = "<red>You can't ignore yourself.";

    @JsonProperty("IGNORE-ON")
    private String ignoreOn = "<green>You are now ignoring <yellow>%target%<green>.";

    @JsonProperty("IGNORE-OFF")
    private String ignoreOff =
        "<green>You are <red>no <green>longer ignoring <yellow>%target%<green>.";
  }

  @Getter
  public static class Nick {
    @JsonProperty("HELP")
    private String help =
        "<red>/%label% [player] <formatted name> <gray>or <red>/%label% reset [player]";

    @JsonProperty("TARGET-OFFLINE")
    private String targetOffline = "<red>%target% is not online.";

    @JsonProperty("SELF-NAME-LOCKED")
    private String selfNameLocked =
        "<red>You can only style your own name. The text must stay your real username.";

    @JsonProperty("DIFFERENT-NAME-NO-PERM")
    private String differentNameNoPerm =
        "<red>You can only style the real username. You need extra permission to set a different"
            + " name.";

    @JsonProperty("SET-SELF")
    private String setSelf = "<green>Your display name has been set to %name%<green>.";

    @JsonProperty("SET-OTHER")
    private String setOther =
        "<green>You set <yellow>%target%<green>'s display name to %name%<green>.";

    @JsonProperty("SET-BY")
    private String setBy = "<yellow>%player% <green>set your display name to %name%<green>.";

    @JsonProperty("RESET-SELF")
    private String resetSelf = "<green>Your display name has been reset to default.";

    @JsonProperty("RESET-OTHER")
    private String resetOther = "<green>You reset <yellow>%target%<green>'s display name.";

    @JsonProperty("RESETED-BY")
    private String resetedBy = "<yellow>%player% <green>reset your display name.";

    @JsonProperty("INVALID-NAME")
    private String invalidName =
        "<red>Invalid name format. The name must be 3-16 characters long and contain only letters,"
            + " numbers, and underscores.";

    @JsonProperty("BLOCKED-FORMATTING")
    private String blockedFormatting =
        "<red>Your nickname cannot contain blocked formatting tags or codes.";
  }

  @Getter
  public static class Queues {
    @JsonProperty("UNRANKED")
    private Unranked unranked = new Unranked();

    @JsonProperty("RANKED")
    private Ranked ranked = new Ranked();

    @JsonProperty("CUSTOM")
    private Custom custom = new Custom();

    @Getter
    public static class Unranked {
      @JsonProperty("NO-UNRANKED-LEFT")
      private String noUnrankedLeft =
          "<red>You have already played all your allowed unranked matches.";

      @JsonProperty("LADDER-NOT-FOUND")
      private String ladderNotFound = "<red>Ladder %ladder% not found.";
    }

    @Getter
    public static class Ranked {
      @JsonProperty("NO-RANKED-LEFT")
      private String noRankedLeft = "<red>You have already played all your allowed ranked matches.";

      @JsonProperty("LADDER-NOT-FOUND")
      private String ladderNotFound = "<red>Ladder %ladder% not found.";

      @JsonProperty("PROGRESS-HEADER")
      private String progressHeader = "<gold><bold>Progress towards Ranked:";

      @JsonProperty("PROGRESS-EXP")
      private String progressExp =
          " <gray>• <white>Experience: <yellow>%current%<gray>/<yellow>%required%"
              + " <green>(%percent%%<green>)";

      @JsonProperty("PROGRESS-WINS")
      private String progressWins =
          " <gray>• <white>Wins: <yellow>%current%<gray>/<yellow>%required%"
              + " <green>(%percent%%<green>)";
    }

    @Getter
    public static class Custom {
      @JsonProperty("NO-PERMISSION")
      private String noPermission = "<red>You don't have permission.";

      @JsonProperty("HELP")
      private String help = "<red>/customqueue <open|join|host <kit>|leave>";

      @JsonProperty("HOST-USAGE")
      private String hostUsage = "<red>/customqueue host <kit>";

      @JsonProperty("KIT-NOT-FOUND")
      private String kitNotFound = "<red>Custom kit %kit% was not found or is not ready.";

      @JsonProperty("ALREADY-SEARCHING")
      private String alreadySearching =
          "<yellow>You are already searching for a hosted custom kit.";

      @JsonProperty("ALREADY-HOSTING")
      private String alreadyHosting = "<yellow>You are already hosting a custom kit queue.";

      @JsonProperty("NOT-IN-CUSTOM-QUEUE")
      private String notInCustomQueue = "<red>You are not in a custom kit queue.";
    }
  }

  @Getter
  public static class Leave {
    @JsonProperty("NOT-IN-MATCH")
    private String notInMatch = "<red>You are not in a match.";

    @JsonProperty("CANT-LEAVE-DUEL-UNRANKED")
    private String cantLeaveDuelUnranked = "<red>You can't leave unranked duel matches.";

    @JsonProperty("CANT-LEAVE-DUEL-RANKED")
    private String cantLeaveDuelRanked = "<red>You can't leave ranked duel matches.";
  }

  @Getter
  public static class Duel {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% <player>";

    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission.";

    @JsonProperty("CANT-DUEL")
    private String cantDuel = "<red>You can't do this right now.";

    @JsonProperty("PLAYER-OFFLINE")
    private String playerOffline = "<red>%target% cannot be found.";

    @JsonProperty("SELF-DUEL")
    private String selfDuel = "<red>You can't duel yourself.";

    @JsonProperty("ALREADY-INVITED")
    private String alreadyInvited =
        "<red>You have already invited %target% to a duel, you have to wait until %target% accepts"
            + " or the invitation expires.";

    @JsonProperty("PLAYER-CANT-DUEL")
    private String playerCantDuel = "<red>You can't duel %target% right now.";

    @JsonProperty("PLAYER-NOT-RECEIVE")
    private String playerNotReceive = "<red>%target% doesn't accept duel requests.";

    @JsonProperty("ARENA-BUSY")
    private String arenaBusy = "<red>Unfortunately the arena you choose was busy. (%arena%<red>)";

    @JsonProperty("NO-AVAILABLE-ARENA")
    private String noAvailableArena =
        "<red>Unfortunately there are no available arenas for your match.";

    @JsonProperty("REQUEST-MESSAGE")
    private RequestMessage requestMessage = new RequestMessage();

    @Getter
    public static class RequestMessage {
      @JsonProperty("RANDOM-ARENA-NAME")
      private String randomArenaName = "<light_purple>Random";

      @JsonProperty("SENDER")
      private List<String> sender =
          List.of(
              "<gray><st>-------------------------------",
              "<gold><bold>Duel Request Sent!",
              "",
              " <gray>- <white>Kit: <yellow>%ladder%",
              " <gray>- <white>Map: <yellow>%arena%",
              " <gray>- <white>Rounds: <aqua>%rounds%",
              " <gray>- <white>Opponent: <yellow>%target%",
              " <gray>- <white>Opponent Connection: <yellow>%targetPing%ms",
              "<gray><st>-------------------------------");

      @JsonProperty("TARGET")
      private List<String> target =
          List.of(
              "<gray><st>-------------------------------",
              "<gold><bold>Duel Request Received!",
              "",
              " <gray>- <white>Kit: <yellow>%ladder%",
              " <gray>- <white>Map: <yellow>%arena%",
              " <gray>- <white>Rounds: <aqua>%rounds%",
              " <gray>- <white>Opponent: <yellow>%sender%",
              " <gray>- <white>Opponent Connection: <yellow>%senderPing%ms",
              "",
              "<green><click:run_command:'/accept %sender%'><hover:show_text:'<yellow>Click here to"
                  + " <green>accept <red>%sender%s <yellow>invite.'><u>Accept</u></hover></click>"
                  + " the duel!",
              "<gray><st>-------------------------------");
    }
  }

  @Getter
  public static class Accept {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% <player>";

    @JsonProperty("CANT-ACCEPT")
    private String cantAccept = "<red>You can't accept this invite right now.";

    @JsonProperty("PLAYER-OFFLINE")
    private String playerOffline = "<red>%target% cannot be found.";

    @JsonProperty("SELF-DUEL")
    private String selfDuel = "<red>You can't duel yourself.";

    @JsonProperty("NO-LONGER-PARTY")
    private String noLongerParty = "<red>%target% is no longer in a party.";

    @JsonProperty("PARTY-NOT-AVAILABLE")
    private String partyNotAvailable = "<red>%target%'s party is no longer available for the game.";

    @JsonProperty("PARTY-NO-INVITE")
    private String partyNoInvite =
        "<red>You don't have a game invite from this party, or the invite has expired.";

    @JsonProperty("PLAYER-NOT-AVAILABLE")
    private String playerNotAvailable = "<red>%target% is no longer available for the game.";

    @JsonProperty("PLAYER-NO-INVITE")
    private String playerNoInvite =
        "<red>You don't have duel invite from %target%, or the invite has expired.";
  }

  @Getter
  public static class Ladder {
    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission!";

    @JsonProperty("ARGUMENTS")
    private Arguments arguments = new Arguments();

    @Getter
    public static class Arguments {
      @JsonProperty("HELP")
      private Help help = new Help();

      @JsonProperty("CREATE")
      private Create create = new Create();

      @JsonProperty("LOAD")
      private Load load = new Load();

      @JsonProperty("CLONE")
      private Clone clone = new Clone();

      @JsonProperty("DELETE")
      private Delete delete = new Delete();

      @JsonProperty("INFO")
      private Info info = new Info();

      @JsonProperty("ICON")
      private Icon icon = new Icon();

      @JsonProperty("INVENTORY")
      private Inventory inventory = new Inventory();

      @JsonProperty("EFFECT")
      private Effect effect = new Effect();

      @JsonProperty("STOP")
      private Stop stop = new Stop();

      @JsonProperty("FREEZE")
      private Freeze freeze = new Freeze();

      @Getter
      public static class Help {
        @JsonProperty("STAFF")
        private List<String> staff =
            List.of(
                "<red><st>------------------------------------------",
                " <red>Ladder Help",
                "<red><st>------------------------------------------",
                " <red>» <click:suggest_command:'/%label% freeze '><hover:show_text:'<gray>Freeze a"
                    + " ladder.'>/%label% freeze <ladder>",
                " <red>» <click:suggest_command:'/%label% stop '><hover:show_text:'<gray>Stop all"
                    + " the games in an arena.'>/%label% stop <arena>",
                "<red><st>------------------------------------------");

        @JsonProperty("ADMIN")
        private List<String> admin =
            List.of(
                "<red><st>------------------------------------------",
                " <red>Ladder Help",
                "<red><st>------------------------------------------",
                " <red>» <click:suggest_command:'/%label% info '><hover:show_text:'<gray>Detailed"
                    + " information of the ladder.'>/%label% info <ladder>",
                " <red>» <click:suggest_command:'/%label% create '><hover:show_text:'<gray>Create a"
                    + " new ladder.'>/%label% create <name>",
                " <red>» <click:suggest_command:'/%label% load '><hover:show_text:'<gray>Load a"
                    + " ladder kit into your inventory.'>/%label% load <ladder>",
                " <red>» <click:suggest_command:'/%label% clone '><hover:show_text:'<gray>Clone a"
                    + " ladder under a new name.'>/%label% clone <ladder> <name>",
                " <red>» <click:suggest_command:'/%label% delete '><hover:show_text:'<gray>Delete a"
                    + " ladder.'>/%label% delete <ladder>",
                " <red>» <click:suggest_command:'/%label% set '><hover:show_text:'<gray>Set up"
                    + " various details of the ladder.'>/%label% set <sub_command>",
                " <red>» <click:suggest_command:'/%label% freeze '><hover:show_text:'<gray>Freeze"
                    + " an ladder.'>/%label% freeze <ladder>",
                " <red>» <click:suggest_command:'/%label% stop '><hover:show_text:'<gray>Stop all"
                    + " the games with a specific ladder.'>/%label% stop <ladder>",
                "<red><st>------------------------------------------");

        @JsonProperty("SET-COMMAND")
        private List<String> setCommand =
            List.of(
                "<red><st>------------------------------------------",
                " <red>» <click:suggest_command:'/%label% set icon '><hover:show_text:'<gray>Set"
                    + " the icon.'>/%label% set icon <ladder>",
                " <red>» <click:suggest_command:'/%label% set inv '><hover:show_text:'<gray>Set the"
                    + " inventory & effects.'>/%label% set inv <ladder>",
                " <red>» <click:suggest_command:'/%label% set effects '><hover:show_text:'<gray>Set"
                    + " the effects.'>/%label% set effects <ladder>",
                "<red><st>------------------------------------------");
      }

      @Getter
      public static class Create {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% create <name>";

        @JsonProperty("REACHED-MAX")
        private String reachedMax =
            "<red>The server has reached the maximum number of ladders. (%max%/%max%)";
      }

      @Getter
      public static class Load {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% load <ladder>";

        @JsonProperty("NOT-EXISTS")
        private String notExists = "<red>%ladder% ladder doesn't exists.";

        @JsonProperty("NOT-SET")
        private String notSet = "<red>The %ladder% ladder has no kit set.";

        @JsonProperty("LOAD-SUCCESS")
        private String loadSuccess =
            "<green>You successfully <red>loaded <green>the <gold>%ladder%<green> kit.";
      }

      @Getter
      public static class Clone {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% clone <ladder> <name>";

        @JsonProperty("NOT-EXISTS")
        private String notExists = "<red>%ladder% ladder doesn't exists.";

        @JsonProperty("CLONE-SUCCESS")
        private String cloneSuccess =
            "<green>You successfully <red>cloned <gold>%ladder% <green>as <gold>%clone%<green>.";
      }

      @Getter
      public static class Delete {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% delete <ladder>";

        @JsonProperty("NOT-EXISTS")
        private String notExists = "<red>%ladder% ladder doesn't exists.";

        @JsonProperty("LADDER-ENABLED")
        private String ladderEnabled = "<red>You can't delete an enabled ladder.";

        @JsonProperty("DELETE-SUCCESS")
        private String deleteSuccess =
            "<green>You successfully <red>deleted <green>arena <gold>%ladder%<green>.";
      }

      @Getter
      public static class Info {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% info <ladder>";

        @JsonProperty("NOT-EXISTS")
        private String notExists = "<red>%ladder% ladder doesn't exists.";

        @JsonProperty("STATUS-NAMES")
        private StatusNames statusNames = new StatusNames();

        @JsonProperty("LADDER-INFO")
        private List<String> ladderInfo =
            List.of(
                "<gray><st>-----------------------------------",
                " <gold>Ladder: <white>%ladder%",
                "<gray><st>-----------------------------------",
                " <gray>» <gold>Type: <white>%type%",
                " <gray>» <gold>Icon: %icon%",
                " <gray>» <gold>Display Name: <white>%displayName%",
                " <gray>» <gold>Inventory: %inventory%",
                " <gray>» <gold>Effects: <white>%effects%",
                "",
                " <gray>» <gold>Status: %status%",
                "<gray><st>-----------------------------------");

        @Getter
        public static class StatusNames {
          @JsonProperty("ENABLED")
          private String enabled = "<green>Enabled";

          @JsonProperty("DISABLED")
          private String disabled = "<red>Disabled";

          @JsonProperty("SET")
          private String set = "<green>Set";

          @JsonProperty("NOT-SET")
          private String notSet = "<red>Not-Set";
        }
      }

      @Getter
      public static class Icon {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% set icon <ladder>";

        @JsonProperty("NOT-EXISTS")
        private String notExists = "<red>%ladder% ladder doesn't exists.";

        @JsonProperty("LADDER-ENABLED")
        private String ladderEnabled = "<red>You can't edit an enabled ladder.";

        @JsonProperty("NO-ITEM")
        private String noItem = "<red>You must hold the icon item in your hand.";

        @JsonProperty("NO-DISPLAYNAME")
        private String noDisplayname =
            "<red>You should name the icon before set it for the ladder.";

        @JsonProperty("SAVED-ICON")
        private String savedIcon =
            "<green>You saved the <yellow>icon <green>for <gold>%ladder% <green>ladder.";
      }

      @Getter
      public static class Inventory {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% set inv <ladder>";

        @JsonProperty("NOT-EXISTS")
        private String notExists = "<red>%ladder% ladder doesn't exists.";

        @JsonProperty("LADDER-ENABLED")
        private String ladderEnabled = "<red>You can't edit an enabled ladder.";

        @JsonProperty("SAVED-INVENTORY")
        private String savedInventory =
            "<green>You saved the <yellow>inventory<green>, <yellow>armor <green>and <yellow>potion"
                + " effects <green>for <gold>%ladder% <green>ladder.";
      }

      @Getter
      public static class Effect {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% set effect <ladder>";

        @JsonProperty("NOT-EXISTS")
        private String notExists = "<red>Ladder not found.";

        @JsonProperty("LADDER-ENABLED")
        private String ladderEnabled = "<red>You can't edit an enabled ladder.";

        @JsonProperty("NO-EFFECTS")
        private String noEffects = "<red>You don't have any effects on you.";

        @JsonProperty("SET-EFFECTS")
        private String setEffects =
            "<red><green>You saved the <yellow>potion effects <green>for <gold>%ladder%"
                + " <green>ladder.";
      }

      @Getter
      public static class Stop {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% stop <ladder>";

        @JsonProperty("NOT-EXISTS")
        private String notExists = "<red>Ladder cannot be found.";

        @JsonProperty("NOT-EXITS")
        private String notExits =
            "<red>The ladder is disabled, so there can be no matches with it.";

        @JsonProperty("LADDER-DISABLED")
        private String ladderDisabled =
            "<red>The ladder is disabled, so there can be no matches with it.";

        @JsonProperty("NO-MATCHES")
        private String noMatches = "<red>There are currently no live matches with the ladder.";

        @JsonProperty("MATCH-END-MESSAGE")
        private String matchEndMessage = "<red>The match has been ended by %player%.";

        @JsonProperty("PLAYER-ENDED")
        private String playerEnded = "<red>You successfully ended all the matches with the ladder.";
      }

      @Getter
      public static class Freeze {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label freeze <ladder>";

        @JsonProperty("NOT-EXISTS")
        private String notExists = "<red>Ladder cannot be found.";

        @JsonProperty("LADDER-DISABLED")
        private String ladderDisabled = "<red>You can't freeze a disabled ladder.";

        @JsonProperty("FREEZE-SUCCESS")
        private String freezeSuccess =
            "<yellow>You have successfully <aqua>frozen <yellow>the <gold>%ladder% <yellow>ladder.";

        @JsonProperty("UNFREEZE-SUCCESS")
        private String unfreezeSuccess =
            "<yellow>You have successfully <green>defrozen <yellow>the <gold>%ladder%"
                + " <yellow>ladder.";
      }
    }
  }

  @Getter
  public static class Hologram {
    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission.";

    @JsonProperty("CANT-USE")
    private String cantUse = "<red>You can't use this right now!";

    @JsonProperty("COMMAND-HELP")
    private List<String> commandHelp =
        List.of("<red>/%label% create <name> <type>", "<red>/%label% teleport <name>");

    @JsonProperty("INVALID-TYPE")
    private String invalidType =
        "<red>Invalid hologram type. Available types: <yellow>global, ladder_static,"
            + " ladder_dynamic";

    @JsonProperty("ONLY-IN-LOBBY")
    private String onlyInLobby = "<red>You can only create holograms in the lobby.";

    @JsonProperty("REACHED-MAX")
    private String reachedMax = "<red>You can't create more than 18 holograms.";

    @JsonProperty("HOLO-EXISTS")
    private String holoExists = "<red>Hologram with this name is already exists.";

    @JsonProperty("CREATE-ERROR")
    private String createError = "<red>Hologram name must be 9 character or less.";

    @JsonProperty("CREATE-SUCCESS")
    private String createSuccess =
        "<green>You successfully created the <yellow>%hologram% <green>hologram.";

    @JsonProperty("TELEPORT")
    private Teleport teleport = new Teleport();

    @Getter
    public static class Teleport {
      @JsonProperty("COMMAND-HELP")
      private String commandHelp = "<red>/%label% teleport <name>";

      @JsonProperty("HOLOGRAM-NOT-EXISTS")
      private String hologramNotExists = "<red>%hologram% hologram doesn't exists.";

      @JsonProperty("TELEPORTED")
      private String teleported =
          "<green>You moved <yellow>%hologram% <green>to your current location.";
    }
  }

  @Getter
  public static class MatchStat {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/matchinv <matchID> <player_uuid> <round>";

    @JsonProperty("INVALID-MATCH-ID")
    private String invalidMatchId = "<red>Match id is either invalid or it has expired.";

    @JsonProperty("INVALID-PLAYER")
    private String invalidPlayer = "<red>Player statistics are not available for this match.";

    @JsonProperty("INVALID-ROUND")
    private String invalidRound = "<red>Round cannot be found in statistics.";
  }

  @Getter
  public static class Cosmetics {
    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission to use cosmetics!";

    @JsonProperty("FAILED-LOAD-PROFILE")
    private String failedLoadProfile = "<red>Failed to load your profile!";

    @JsonProperty("ONLY-IN-LOBBY")
    private String onlyInLobby = "<red>You can only open cosmetics while in the lobby!";
  }

  @Getter
  public static class MatchHistory {
    @JsonProperty("NO-HISTORY")
    private String noHistory = "<red>This player has no match history yet.";

    @JsonProperty("PLAYER-NOT-FOUND")
    private String playerNotFound = "<red>Player not found.";
  }

  @Getter
  public static class Party {
    @JsonProperty("CANT-USE-COMMAND")
    private String cantUseCommand = "<red>You can't use this right now.";

    @JsonProperty("ARGUMENTS")
    private Arguments arguments = new Arguments();

    @Getter
    public static class Arguments {
      @JsonProperty("ACCEPT")
      private Accept accept = new Accept();

      @JsonProperty("CHAT")
      private Chat chat = new Chat();

      @JsonProperty("DISBAND")
      private Disband disband = new Disband();

      @JsonProperty("HELP")
      private List<String> help =
          List.of(
              "<gray><st>------------------------------------------------",
              " <aqua><bold>Party Help <gray>- <white>Information on how to use party commands.",
              "<gray><st>------------------------------------------------",
              " <aqua>Party Commands:",
              " <white>/%label% create <dark_gray>- <gray>Create a party.",
              " <white>/%label% join <dark_gray>- <gray>Join a public party.",
              " <white>/%label% public <dark_gray>- <gray>Browse the public parties.",
              " <white>/%label% accept <player> <dark_gray>- <gray>Accept a party invitation.",
              " <white>/%label% leave <dark_gray>- <gray>Accept a party invitation.",
              " <white>/%label% info <player> <dark_gray>- <gray>Party information.",
              "",
              " <aqua>Party Leader Commands:",
              " <white>/%label% invite <player> <dark_gray>- <gray>Invite a player to your party.",
              " <white>/%label% kick <player> <dark_gray>- <gray>Kick a player from your party.",
              " <white>/%label% leader <player> <dark_gray>- <gray>Transfer party leadership.",
              " <white>/%label% disband <dark_gray>- <gray>Disband party.",
              "",
              " <aqua>Party Chat:",
              " <white>/%label% chat <dark_gray>- <gray>Toggle party chat.",
              " <gray>or type '<aqua>%shortcut%<gray>' in front of your message to send it to your"
                  + " party once.",
              "<gray><st>------------------------------------------------");

      @JsonProperty("INFO")
      private Info info = new Info();

      @JsonProperty("INVITE")
      private Invite invite = new Invite();

      @JsonProperty("KICK")
      private Kick kick = new Kick();

      @JsonProperty("LEADER")
      private Leader leader = new Leader();

      @JsonProperty("LEAVE")
      private Leave leave = new Leave();

      @Getter
      public static class Accept {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% accept <player>";

        @JsonProperty("COMMAND-HELP2")
        private String commandHelp2 = "<red>/%label% join <player>";

        @JsonProperty("CANT-JOIN-NOW")
        private String cantJoinNow = "<red>You cannot join a party right now.";

        @JsonProperty("ALREADY-MEMBER")
        private String alreadyMember = "<red>You are already a member of a party.";

        @JsonProperty("TARGET-OFFLINE")
        private String targetOffline = "<red>Targeted party leader is not online.";

        @JsonProperty("PARTY-NOT-EXISTS")
        private String partyNotExists = "<red>Party doesn't exits anymore.";

        @JsonProperty("PARTY-FULL")
        private String partyFull =
            "<red>The party is full. You can contact %leader% to increase the limit.";

        @JsonProperty("NO-INVITE")
        private String noInvite = "<red>You are not invited to this party.";

        @JsonProperty("INVITE-EXPIRED")
        private String inviteExpired = "<red>Your party invite has expired.";
      }

      @Getter
      public static class Chat {
        @JsonProperty("DISABLED")
        private String disabled = "<red>Party chat is disabled on this server.";

        @JsonProperty("NO-PARTY")
        private String noParty = "<red>You are not a member of a party.";

        @JsonProperty("CHAT-ENABLED")
        private String chatEnabled = "<yellow>Party chat <green>enabled<yellow>.";

        @JsonProperty("CHAT-DISABLED")
        private String chatDisabled = "<yellow>Party chat <red>disabled<yellow>.";
      }

      @Getter
      public static class Disband {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% disband";

        @JsonProperty("NO-PARTY")
        private String noParty = "<red>You are not a member of a party.";

        @JsonProperty("NOT-LEADER")
        private String notLeader = "<red>You are not the leader of the party.";
      }

      @Getter
      public static class Info {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% info / info <player>";

        @JsonProperty("NO-PERMISSION")
        private String noPermission = "<red>You don't have permission.";

        @JsonProperty("NO-PARTY")
        private String noParty = "<red>You are not in a party.";

        @JsonProperty("NOT-ONLINE")
        private String notOnline = "<red>%target% is not online.";

        @JsonProperty("PLAYER-NO-PARTY")
        private String playerNoParty = "<red>%target% is not in a party.";

        @JsonProperty("PARTY-STATES")
        private PartyStates partyStates = new PartyStates();

        @JsonProperty("PARTY-INFO")
        private List<String> partyInfo =
            List.of(
                "<gray><st>------------------------------------------------",
                " <aqua><bold>Party Info<gray>:",
                "",
                " <aqua>Leader<gray>: <white>%leader%",
                " <aqua>Party State<gray>: %partyState%",
                " <aqua>Party Members (%maxPlayerLimit%/%memberSize%)<gray>: <white>%members%",
                "<gray><st>------------------------------------------------");

        @Getter
        public static class PartyStates {
          @JsonProperty("PUBLIC")
          private String publicValue = "<green>Public";

          @JsonProperty("PRIVATE")
          private String privateValue = "<red>Private";
        }
      }

      @Getter
      public static class Invite {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% invite <player>";

        @JsonProperty("NO-PARTY")
        private String noParty = "<red>You are not in a party.";

        @JsonProperty("CANT-INVITE")
        private String cantInvite = "<red>You can't invite players to the party.";

        @JsonProperty("TARGET-OFFLINE")
        private String targetOffline = "<red>Player is not online.";

        @JsonProperty("CANT-INVITE-YOURSELF")
        private String cantInviteYourself = "<red>You can't invite yourself to the party.";

        @JsonProperty("PLAYER-ALREADY-IN-PARTY")
        private String playerAlreadyInParty = "<red>%target% is already in your party.";

        @JsonProperty("NO-INVITES")
        private String noInvites = "<red>%target% doesn't accept party invites.";

        @JsonProperty("ALREADY-INVITED")
        private String alreadyInvited = "<red>%target% is already invited to the party.";

        @JsonProperty("PLAYER-INVITED")
        private String playerInvited = "<green>%target% <yellow>has been invited to the party!";

        @JsonProperty("PLAYER-GOT-INVITE")
        private String playerGotInvite =
            "<aqua>%inviter% <yellow>has invited you to their party."
                + " <hover:show_text:'<yellow>Click here to accept the"
                + " invite.'><click:run_command:/party accept %inviter%><green>[ACCEPT]";
      }

      @Getter
      public static class Kick {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% kick <player>";

        @JsonProperty("NO-PARTY")
        private String noParty = "<red>You are not a member of a party.";

        @JsonProperty("NOT-LEADER")
        private String notLeader = "<red>You are not the leader of the party.";

        @JsonProperty("TARGET-OFFLINE")
        private String targetOffline = "<red>%target% is not online.";

        @JsonProperty("CANT-KICK-YOURSELF")
        private String cantKickYourself = "<red>You can't kick yourself out.";

        @JsonProperty("NOT-MEMBER")
        private String notMember = "<red>%target% is not a member of the party.";
      }

      @Getter
      public static class Leader {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% leader <player>";

        @JsonProperty("NO-PARTY")
        private String noParty = "<red>You are not a member of a party.";

        @JsonProperty("NOT-LEADER")
        private String notLeader = "<red>You are not the leader of the party.";

        @JsonProperty("TARGET-OFFLINE")
        private String targetOffline = "<red>%target% is not online.";

        @JsonProperty("ALREADY-LEADER")
        private String alreadyLeader = "<red>You are already the leader of the party.";

        @JsonProperty("NOT-MEMBER")
        private String notMember = "<red>%target% is not a member of the party.";
      }

      @Getter
      public static class Leave {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% leave";

        @JsonProperty("NO-PARTY")
        private String noParty = "<red>You are not a member of a party.";
      }
    }
  }

  @Getter
  public static class Practice {
    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission.";

    @JsonProperty("ARGUMENTS")
    private Arguments arguments = new Arguments();

    @Getter
    public static class Arguments {
      @JsonProperty("ELO")
      private Elo elo = new Elo();

      @JsonProperty("HELP")
      private Help help = new Help();

      @JsonProperty("INFO")
      private Info info = new Info();

      @JsonProperty("GOLDENHEAD")
      private Goldenhead goldenhead = new Goldenhead();

      @JsonProperty("LOBBY")
      private Lobby lobby = new Lobby();

      @JsonProperty("UNRANKED")
      private Unranked unranked = new Unranked();

      @JsonProperty("RANKED")
      private Ranked ranked = new Ranked();

      @JsonProperty("EXPERIENCE")
      private Experience experience = new Experience();

      @JsonProperty("BAN")
      private Ban ban = new Ban();

      @JsonProperty("UNBAN")
      private Unban unban = new Unban();

      @JsonProperty("RENAME")
      private Rename rename = new Rename();

      @JsonProperty("PRESET")
      private Preset preset = new Preset();

      @JsonProperty("RESET")
      private Reset reset = new Reset();

      @JsonProperty("RELOAD")
      private Reload reload = new Reload();

      @JsonProperty("NAMETAG")
      private Nametag nametag = new Nametag();

      @JsonProperty("TELEPORT")
      private Teleport teleport = new Teleport();

      @Getter
      public static class Elo {
        @JsonProperty("COMMAND-HELP")
        private List<String> commandHelp =
            List.of(
                "<red><st>------------------------------------------------",
                "<red>/%label% elo reset <player> <ladder/*> - Reset to default elo.",
                "<red>/%label% elo set <player> <ladder/*> <number>",
                "<red><st>------------------------------------------------");

        @JsonProperty("TARGET-OFFLINE")
        private String targetOffline = "<red>%target% is not online.";

        @JsonProperty("TARGET-OP")
        private String targetOp = "<red>You can't change %target%'s elo.";

        @JsonProperty("LADDER-NOT-EXISTS")
        private String ladderNotExists = "<red>%ladder% doesn't exists.";

        @JsonProperty("SPECIFIC-LADDER-ELO-RESET")
        private String specificLadderEloReset =
            "<yellow>You have reset <red>%target%'s <white>%ladder% <yellow>ladder elo to the"
                + " default <red>%defaultElo%<yellow>.";

        @JsonProperty("ALL-LADDER-ELO-RESET")
        private String allLadderEloReset =
            "<yellow>You have reset <red>%target%'s all ladder elo to the default"
                + " <red>%defaultElo%<yellow>.";

        @JsonProperty("SPECIFIC-LADDER-ELO-SET")
        private String specificLadderEloSet =
            "<yellow>You have set <red>%target%'s <white>%ladder% <yellow>ladder elo to"
                + " <red>%newElo%<yellow>.";

        @JsonProperty("ALL-LADDER-ELO-SET")
        private String allLadderEloSet =
            "<yellow>You have set <red>%target%'s all ladder elo to <red>%newElo%<yellow>.";

        @JsonProperty("INVALID-NUMBER")
        private String invalidNumber = "<red>Please enter a number greater or equal to 0.";
      }

      @Getter
      public static class Help {
        @JsonProperty("STAFF")
        private List<String> staff =
            List.of(
                "<red><st>------------------------------------------------",
                " <red> » /%label% info <player> <gray>- <white>View player's info.",
                " <red> » /%label% elo <gray>- <white>Manage the elo of the player.",
                " <red> » /%label% ranked <gray>- <white>Manage the ranked daily limit of the"
                    + " player.",
                " <red> » /%label% unranked <gray>- <white>Manage the unranked daily limit of the"
                    + " player.",
                " <red> » /%label% nametag <gray>- <white>Manage the nametag of the player.",
                "<red><st>------------------------------------------------");

        @JsonProperty("ADMIN")
        private List<String> admin =
            List.of(
                "<red><st>------------------------------------------------",
                " <red> » /%label% lobby <gray>- <white>Teleport to lobby.",
                " <red> » /%label% lobby set <gray>- <white>Set lobby location.",
                " <red> » /%label% lobby load <gray>- <white>Load lobby inventory.",
                " <red> » /%label% arenas <gray>- <white>Teleport to arenas world.",
                " <red> » /%label% hologram <gray>- <white>Manage practice holograms.",
                " <red> » /%label% rename <gray>- <white>Rename the item in your hand.",
                " <red> » /%label% preset add <gray>- <white>Save a cosmetic preset.",
                " <red> » /%label% info <player> <gray>- <white>View player's info.",
                " <red> » /%label% goldenhead <amount> - Get some golden heads.",
                " <red> » /%label% reset <player> <gray>- <white>Fully reset a player.",
                " <red> » /%label% elo <gray>- <white>Manage the elo of the player.",
                " <red> » /%label% ranked <gray>- <white>Manage the ranked daily limit of the"
                    + " player.",
                " <red> » /%label% unranked <gray>- <white>Manage the unranked daily limit of the"
                    + " player.",
                " <red> » /%label% exp <gray>- <white>Manage the experience of the player.",
                " <red> » /%label% nametag <gray>- <white>Manage the nametag of the player.",
                " <red> » /%label% reload <gray>- <white>Reload practice config files.",
                "<red><st>------------------------------------------------");
      }

      @Getter
      public static class Info {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% info <player>";

        @JsonProperty("TARGET-NOT-FOUND")
        private String targetNotFound = "<red>%target% cannot be found.";

        @JsonProperty("TARGET-OP")
        private String targetOp = "<red>You can't open %target%'s information GUI.";
      }

      @Getter
      public static class Goldenhead {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% goldenhead <amount>";

        @JsonProperty("INVENTORY-FULL")
        private String inventoryFull = "<red>Your inventory is full.";

        @JsonProperty("INVALID-AMOUNT")
        private String invalidAmount = "<red>Please enter an integer for the amount.";

        @JsonProperty("INVALID-AMOUNT-2")
        private String invalidAmount2 = "<red>Amount has to be at least 1 and maximum 64.";
      }

      @Getter
      public static class Lobby {
        @JsonProperty("COMMAND-HELP")
        private List<String> commandHelp =
            List.of(
                "<red><st>------------------------------------------------",
                "<red>/%label% lobby <gray>- <white>Teleport to the lobby.",
                "<red>/%label% lobby set <gray>- <white>Set the lobby",
                "<red>/%label% lobby load <gray>- <white>Load the default lobby inventory",
                "<red><st>------------------------------------------------");

        @JsonProperty("LOBBY-NOT-SET")
        private String lobbyNotSet =
            "<red>Lobby location is not set yet. You can set it by using the"
                + " <click:run_command:'/%label% lobby set'><hover:show_text:'<yellow>Click here to"
                + " set the lobby.<newline><green>line break'><bold>/practice lobby"
                + " set</hover></click> <red>command.";

        @JsonProperty("LOBBY-SET")
        private String lobbySet = "<green>You successfully set the <yellow>lobby spawn<green>.";

        @JsonProperty("NO-ARENAS-WORLD")
        private String noArenasWorld =
            "<red>You can't place the lobby location in the arena worlds.";
      }

      @Getter
      public static class Unranked {
        @JsonProperty("COMMAND-HELP")
        private List<String> commandHelp =
            List.of(
                "<red><st>------------------------------------------------",
                "<red>/%label% unranked reset <player> - Reset to default unrankeds.",
                "<red>/%label% unranked add <player> <number> - Add extra unrankeds.",
                "<red><st>------------------------------------------------");

        @JsonProperty("TARGET-OFFLINE")
        private String targetOffline = "<red>%target% is not online.";

        @JsonProperty("TARGET-OP")
        private String targetOp = "<red>You can't change %target%'s unranked limit.";

        @JsonProperty("NO-GROUP")
        private String noGroup =
            "<red>%target%'s unranked limit cannot be restored, because they doesn't have a group.";

        @JsonProperty("RESET-DAILY-LIMIT")
        private String resetDailyLimit =
            "<yellow>You have reset <red>%target%'s <yellow>daily unranked limit to"
                + " <red>%dailyUnrankedLimit%<yellow>.";

        @JsonProperty("INVALID-NUMBER")
        private String invalidNumber = "<red>Please enter a number greater than 0.";

        @JsonProperty("ADD-EXTRA-UNRANKED")
        private String addExtraUnranked =
            "<yellow>You have added <red>%extraUnranked% <yellow>extra unranked to"
                + " <aqua>%target%<yellow>'s daily ranked limit. Now the player has"
                + " <red>%newUnranked%<yellow>.";
      }

      @Getter
      public static class Ranked {
        @JsonProperty("COMMAND-HELP")
        private List<String> commandHelp =
            List.of(
                "<red><st>------------------------------------------------",
                "<red>/%label% ranked reset <player> - Reset to default ranked.",
                "<red>/%label% ranked add <player> <number> - Add extra rankeds.",
                "<red>/%label% ranked ban <player> <reason> - Ban player from playing ranked.",
                "<red><st>------------------------------------------------");

        @JsonProperty("TARGET-NOT-FOUND")
        private String targetNotFound = "<red>%target% cannot be found.";

        @JsonProperty("TARGET-OP")
        private String targetOp = "<red>You can't change %target%'s ranked limit.";

        @JsonProperty("NO-GROUP")
        private String noGroup =
            "<red>%target%'s ranked limit cannot be restored, because they doesn't have a group.";

        @JsonProperty("RESET-DAILY-LIMIT")
        private String resetDailyLimit =
            "<yellow>You have reset <red>%target%'s <yellow>daily ranked limit to"
                + " <red>%dailyRankedLimit%<yellow>.";

        @JsonProperty("INVALID-NUMBER")
        private String invalidNumber = "<red>Please enter a number greater than 0.";

        @JsonProperty("ADD-EXTRA-RANKED")
        private String addExtraRanked =
            "<yellow>You have added <red>%extraRanked% <yellow>extra ranked to"
                + " <aqua>%target%<yellow>'s daily ranked limit. Now the player has"
                + " <red>%newRanked%<yellow>.";

        @JsonProperty("CANT-BAN-PLAYER")
        private String cantBanPlayer = "<red>You can't ban %target% from playing ranked.";
      }

      @Getter
      public static class Experience {
        @JsonProperty("COMMAND-HELP")
        private List<String> commandHelp =
            List.of(
                "<red><st>------------------------------------------------",
                "<red>/%label% exp reset <player> - Reset to default experience (0).",
                "<red>/%label% exp add <player> <number> - Add extra experience.",
                "<red>/%label% exp set <player> <number> - Set the experience.",
                "<red><st>------------------------------------------------");

        @JsonProperty("TARGET-NOT-FOUND")
        private String targetNotFound = "<red>%target% cannot be found.";

        @JsonProperty("TARGET-OP")
        private String targetOp = "<red>You can't change %target%'s experience.";

        @JsonProperty("TARGET-RESET")
        private String targetReset =
            "<yellow>You have reset <red>%target%'s <yellow>experience to <red>0<yellow>.";

        @JsonProperty("INVALID-NUMBER")
        private String invalidNumber = "<red>Please enter a number greater than 0.";

        @JsonProperty("ADD-EXTRA-EXP")
        private String addExtraExp =
            "<yellow>You have added <red>%extraExp% <yellow>extra experience to"
                + " <aqua>%target%<yellow>'s experience. Now the player has <red>%newExp%<yellow>.";

        @JsonProperty("SET-EXP")
        private String setExp =
            "<yellow>You have set <red>%target%<yellow>'s experience to <red>%newExp%<yellow>.";
      }

      @Getter
      public static class Ban {
        @JsonProperty("PLAYER-ALREADY-BANNED")
        private String playerAlreadyBanned = "<red>%target% is already banned from playing ranked.";

        @JsonProperty("BANNED-FROM-RANKED")
        private String bannedFromRanked =
            "<yellow>You have banned <red>%target% <yellow>from playing ranked matches. Reason:"
                + " <red>%reason%";

        @JsonProperty("BANNED-FROM-RANKED-PLAYER")
        private String bannedFromRankedPlayer =
            "<red>You have been banned from playing ranked matches. Reason: <red>%reason%";

        @JsonProperty("NO-REASON")
        private String noReason = "<red>NULL";
      }

      @Getter
      public static class Unban {
        @JsonProperty("PLAYER-NOT-BANNED")
        private String playerNotBanned = "<red>%target% is not banned from playing ranked.";

        @JsonProperty("UNBANNED-FROM-RANKED")
        private String unbannedFromRanked =
            "<yellow>You have unbanned <red>%target% <yellow>from playing ranked matches.";

        @JsonProperty("UNBANNED-FROM-RANKED-PLAYER")
        private String unbannedFromRankedPlayer =
            "<yellow>You have been unbanned from playing ranked matches.";
      }

      @Getter
      public static class Rename {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% rename <name>";

        @JsonProperty("ITEM-IN-HAND")
        private String itemInHand = "<red>You must hold an item in your hand.";
      }

      @Getter
      public static class Preset {
        @JsonProperty("COMMAND-HELP")
        private List<String> commandHelp =
            List.of(
                "<red><st>------------------------------------------------",
                "<red>/%label% preset add <gray>- <white>Save a cosmetic preset.",
                "<gray>Hold a custom shield or wear trimmed armor.",
                "<red><st>------------------------------------------------");

        @JsonProperty("NO-NAME")
        private String noName =
            "<red>Rename the item in your hand first with <yellow>/%label% rename <name></yellow>,"
                + " that name becomes the preset name.";

        @JsonProperty("INVALID-NAME")
        private String invalidName = "<red>The item name must contain letters or numbers.";

        @JsonProperty("SHIELD-NO-DESIGN")
        private String shieldNoDesign = "<red>This shield has no base color or patterns.";

        @JsonProperty("SHIELD-TOO-MANY-LAYERS")
        private String shieldTooManyLayers =
            "<red>Shield presets support at most %max% pattern layers, this shield has %layers%.";

        @JsonProperty("SHIELD-EXISTS")
        private String shieldExists =
            "<red>A shield preset with the id <yellow>%id%</yellow> already exists in"
                + " cosmetic-presets.yml.";

        @JsonProperty("SHIELD-SAVED")
        private String shieldSaved =
            "<green>Saved the shield preset <yellow>%id%</yellow>. Players need"
                + " <yellow>%permission%</yellow> to use it.";

        @JsonProperty("NO-TRIM")
        private String noTrim = "<red>Wear armor with trims, or hold a trimmed armor piece.";

        @JsonProperty("TRIM-EXISTS")
        private String trimExists =
            "<red>An armor trim preset with the id <yellow>%id%</yellow> already exists in"
                + " cosmetic-presets.yml.";

        @JsonProperty("TRIM-SAVED")
        private String trimSaved =
            "<green>Saved the armor trim preset <yellow>%id%</yellow>. Players need"
                + " <yellow>%permission%</yellow> to use it.";
      }

      @Getter
      public static class Reset {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% reset <player>";

        @JsonProperty("TARGET-OFFLINE")
        private String targetOffline = "<red>%target% is not online.";

        @JsonProperty("TARGET-OP")
        private String targetOp = "<red>You cannot reset %target%'s data.";

        @JsonProperty("RESET-PLAYER")
        private String resetPlayer =
            "<yellow>You have successfully reset <red>%target%'s <yellow>all data.";
      }

      @Getter
      public static class Reload {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% reload";

        @JsonProperty("RELOADED")
        private String reloaded = "<green>Practice config files have been reloaded.";

        @JsonProperty("FAILED")
        private String failed = "<red>Reload failed. Check console for details.";
      }

      @Getter
      public static class Nametag {
        @JsonProperty("COMMAND-HELP")
        private List<String> commandHelp =
            List.of(
                "<red><st>------------------------------------------------",
                "<red>/%label% nametag prefix <player> <prefix> - Set the prefix.",
                "<red>/%label% nametag suffix <player> <suffix> - Set the suffix",
                "<red>/%label% nametag name <player> <name> - Set the MiniMessage name (chat/tab).",
                "<red>/%label% nametag reset <player> - Reset the nametag of the player.",
                "<red><st>------------------------------------------------");

        @JsonProperty("TARGET-OFFLINE")
        private String targetOffline = "<red>%target% is not online.";

        @JsonProperty("TARGET-BYPASS")
        private String targetBypass = "<red>You can't modify %target%'s nametag.";

        @JsonProperty("NAMETAG-DISABLED")
        private String nametagDisabled = "<red>Lobby nametag is disabled.";

        @JsonProperty("PREFIX-SET")
        private String prefixSet =
            "<yellow>You set <gold>%target%'s <yellow>prefix to %prefix%<yellow>.";

        @JsonProperty("PLAYER-PREFIX-SET")
        private String playerPrefixSet =
            "<gold>%player% <yellow>set your prefix to %prefix%<yellow>.";

        @JsonProperty("SUFFIX-SET")
        private String suffixSet =
            "<yellow>You set <gold>%target%'s <yellow>suffix to %suffix%<yellow>.";

        @JsonProperty("PLAYER-SUFFIX-SET")
        private String playerSuffixSet =
            "<gold>%player% <yellow>set your suffix to %suffix%<yellow>.";

        @JsonProperty("NAME-SET")
        private String nameSet = "<yellow>You set <gold>%target%'s <yellow>name to %name%<yellow>.";

        @JsonProperty("PLAYER-NAME-SET")
        private String playerNameSet =
            "<gold>%player% <yellow>set your name format to %name%<yellow>.";

        @JsonProperty("RELOADED")
        private String reloaded = "<yellow>You reseted <gold>%target%'s <yellow>nametag.";

        @JsonProperty("PLAYER-RELOADED")
        private String playerReloaded = "<gold>%player% <yellow>has reset your nametag.";
      }

      @Getter
      public static class Teleport {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% teleport <world>";

        @JsonProperty("WORLD-DONT-EXISTS")
        private String worldDontExists = "<red>%world% doesn't exists.";

        @JsonProperty("TELEPORTED")
        private String teleported =
            "<yellow>You have been teleported to the default spawn position of world %world%.";
      }
    }
  }

  @Getter
  public static class Preview {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% <ladder>";

    @JsonProperty("CANT-USE")
    private String cantUse = "<red>You can't use this right now.";

    @JsonProperty("LADDER-NOT-EXISTS")
    private String ladderNotExists = "<red>Ladder cannot be found.";

    @JsonProperty("LADDER-DISABLED")
    private String ladderDisabled = "<red>%ladder% is not enabled.";
  }

  @Getter
  public static class PrivateMessage {
    @JsonProperty("MESSAGE")
    private Message message = new Message();

    @JsonProperty("REPLY")
    private Reply reply = new Reply();

    @Getter
    public static class Message {
      @JsonProperty("COMMAND-HELP")
      private String commandHelp = "<red>/%label% <player> <message>";

      @JsonProperty("CANT-SEND")
      private String cantSend =
          "<red>You can't send private message if it's turned off in your settings.";

      @JsonProperty("TARGET-OFFLINE")
      private String targetOffline = "<red>%target% is not online.";

      @JsonProperty("TARGET-HIDE")
      private String targetHide = "<red>%target% is offline.";

      @JsonProperty("CANT-SEND2")
      private String cantSend2 = "<red>You can't send private messages to %target%.";

      @JsonProperty("SENDER-MESSAGE")
      private String senderMessage =
          "<dark_gray>[<white>%sender% <gray>» <white>%receiver%<dark_gray>] <yellow>%message%";

      @JsonProperty("RECEIVER-MESSAGE")
      private String receiverMessage =
          "<dark_gray>[<white>%sender% <gray>» <white>%receiver%<dark_gray>] <yellow>%message%";
    }

    @Getter
    public static class Reply {
      @JsonProperty("COMMAND-HELP")
      private String commandHelp = "<red>/%label% <message>";

      @JsonProperty("NO-MESSAGE")
      private String noMessage = "<red>No one messaged you recently.";

      @JsonProperty("CANT-REPLY")
      private String cantReply =
          "<red>You can't reply to private message if it's turned off in your settings.";

      @JsonProperty("PLAYER-LEFT")
      private String playerLeft = "<red>%target% has left the server.";

      @JsonProperty("CANT-REPLY2")
      private String cantReply2 = "<red>You can't send private messages to %target% anymore.";

      @JsonProperty("SENDER-MESSAGE")
      private String senderMessage =
          "<dark_gray>[<white>%sender% <gray>» <white>%receiver%<dark_gray>] <yellow>%message%";

      @JsonProperty("RECEIVER-MESSAGE")
      private String receiverMessage =
          "<dark_gray>[<white>%sender% <gray>» <white>%receiver%<dark_gray>] <yellow>%message%";
    }
  }

  @Getter
  public static class Divisions {
    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission.";

    @JsonProperty("CANT-USE")
    private String cantUse = "<red>You can't use this right now!";
  }

  @Getter
  public static class Settings {
    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission.";

    @JsonProperty("CANT-USE")
    private String cantUse = "<red>You can't use this right now.";
  }

  @Getter
  public static class Setup {
    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission.";

    @JsonProperty("GUI-LOADING")
    private String guiLoading = "<red>GUI is still loading...";

    @JsonProperty("ARENA")
    private Arena arena = new Arena();

    @JsonProperty("LADDER")
    private Ladder ladder = new Ladder();

    @JsonProperty("EVENT")
    private Event event = new Event();

    @JsonProperty("HOLOGRAM")
    private Hologram hologram = new Hologram();

    @JsonProperty("SERVER")
    private Server server = new Server();

    @Getter
    public static class Arena {
      @JsonProperty("CANT-DELETE-ENABLED")
      private String cantDeleteEnabled = "<red>You can't delete an enabled arena.";

      @JsonProperty("CANT-DELETE-COPIES")
      private String cantDeleteCopies = "<red>You can't delete copies for an enabled arena.";

      @JsonProperty("ALREADY-GENERATING")
      private String alreadyGenerating =
          "<red>You can't copy an arena, while it's already generating one.";

      @JsonProperty("ARENA-DISABLED")
      private String arenaDisabled = "<red>You can't create copies for a disabled arena.";

      @JsonProperty("CANT-EDIT-ENABLED")
      private String cantEditEnabled = "<red>You can't edit an enabled arena.";

      @JsonProperty("CANT-REMOVE-LADDER-TYPE")
      private String cantRemoveLadderType =
          "<red>You can't remove ladder type from an enabled arena.";

      @JsonProperty("CANT-ASSIGN-BUILD-DIFF")
      private String cantAssignBuildDiff =
          "<red>You can't assign build ladder types to non-build arenas and vice-versa.";

      @JsonProperty("CANT-REMOVE-LADDER")
      private String cantRemoveLadder = "<red>You can't remove ladders from an enabled arena.";
    }

    @Getter
    public static class Ladder {
      @JsonProperty("CANT-DELETE-ENABLED")
      private String cantDeleteEnabled = "<red>You can't delete an enabled ladder.";

      @JsonProperty("CANT-EDIT-ENABLED")
      private String cantEditEnabled = "<red>You can't edit enabled ladders.";

      @JsonProperty("ONLY-PUT-BLOCKS")
      private String onlyPutBlocks = "<red>You can only put blocks in this GUI.";

      @JsonProperty("CANT-EDIT-RANKED-WEIGHTCLASS-MATCHTYPE")
      private String cantEditRankedWeightclassMatchtype =
          "<red>You can't edit the only ranked weight class ladder's match types.";
    }

    @Getter
    public static class Event {
      @JsonProperty("CANT-EDIT-ENABLED")
      private String cantEditEnabled = "<red>You can't edit an enabled event.";

      @JsonProperty("NO-ITEM-ON-CURSOR")
      private String noItemOnCursor = "<red>You have to hold the icon item in your cursor.";

      @JsonProperty("NO-LOCATION")
      private String noLocation = "<red>Event doesn't have any available location.";

      @JsonProperty("ARENAS-WORLD")
      private String arenasWorld = "<red>You must be in the arenas world to set up the event.";

      @JsonProperty("SET-FIRST-CORNER")
      private String setFirstCorner =
          "<yellow>You set the <green>first <yellow>corner for the <red>%event% <yellow>event.";

      @JsonProperty("SET-SECOND-CORNER")
      private String setSecondCorner =
          "<yellow>You set the <green>seconds <yellow>corner for the <red>%event% <yellow>event.";

      @JsonProperty("MAP-SET")
      private String mapSet =
          "<green>You have successfully selected the area for the <yellow>%event% event<green>.";
    }

    @Getter
    public static class Hologram {
      @JsonProperty("CANT-EDIT-ENABLED")
      private String cantEditEnabled = "<red>You can't edit enabled holograms.";

      @JsonProperty("NO-REQUIREMENTS")
      private String noRequirements = "<red>Hologram doesn't meet the requirements.";

      @JsonProperty("HOLOGRAM-DELETED")
      private String hologramDeleted =
          "<green>You successfully deleted hologram <yellow>%hologram%<green>.";
    }

    @Getter
    public static class Server {
      @JsonProperty("WAIT-BEFORE")
      private String waitBefore = "<red>Please wait before using this again.";

      @JsonProperty("SAVED-ALL-FILES")
      private String savedAllFiles = "<yellow>You saved all the server data.";

      @JsonProperty("ENDED-EVENT")
      private String endedEvent = "<yellow>You ended the event <red>%event%<yellow>.";

      @JsonProperty("MATCH-ENDED")
      private String matchEnded = "<red>The match has been ended by a moderator.";

      @JsonProperty("PLAYER-ENDED-MATCH")
      private String playerEndedMatch = "<yellow>You ended match %matchId%.";

      @JsonProperty("MATCH-ALREADY-ENDED")
      private String matchAlreadyEnded = "<red>Match is already ended.";

      @JsonProperty("MARIADB-DISABLED")
      private String mariadbDisabled =
          "<red>You cannot save the mariadb data because it is disabled.";

      @JsonProperty("DATA-SAVED-MANUALLY")
      private String dataSavedManually = "<green>Successful data save.";
    }
  }

  @Getter
  public static class Spectate {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% <player>";

    @JsonProperty("NO-SPECTATE")
    private String noSpectate = "<red>You don't have permission.";

    @JsonProperty("CANT-SPECTATE")
    private String cantSpectate = "<red>You can't spectate anyone right now.";

    @JsonProperty("TARGET-OFFLINE")
    private String targetOffline = "<red>%target% is not online.";

    @JsonProperty("TARGET-NOT-PLAYING")
    private String targetNotPlaying = "<red>%target% is not playing right now. Try again later.";

    @JsonProperty("CANT-SPECTATE2")
    private String cantSpectate2 = "<red>You can't spectate yourself.";

    @JsonProperty("PLAYER-INACTIVE")
    private String playerInactive = "<red>Player isn't in match or event.";
  }

  @Getter
  public static class Follow {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% <player>";

    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission.";

    @JsonProperty("CANT-FOLLOW")
    private String cantFollow = "<red>You can only follow players from the lobby.";

    @JsonProperty("IN-STAFF-MODE")
    private String inStaffMode = "<red>You can't follow players while in staff mode.";

    @JsonProperty("IN-PARTY")
    private String inParty = "<red>You can't follow players while in a party.";

    @JsonProperty("TARGET-OFFLINE")
    private String targetOffline = "<red>%target% is not online.";

    @JsonProperty("CANT-SELF")
    private String cantSelf = "<red>You can't follow yourself.";

    @JsonProperty("NOT-ALLOWED")
    private String notAllowed = "<red>%target% doesn't allow spectators.";

    @JsonProperty("STARTED")
    private String started =
        "<green>You are now following <yellow>%target%<green>. You will spectate their matches."
            + " <gray>(/unfollow to stop)";

    @JsonProperty("STOPPED")
    private String stopped = "<yellow>You stopped following <gold>%target%<yellow>.";

    @JsonProperty("NOT-FOLLOWING")
    private String notFollowing = "<red>You are not following anyone.";

    @JsonProperty("CANT-QUEUE")
    private String cantQueue =
        "<red>You can't queue while following a player. Use /unfollow first.";
  }

  @Getter
  public static class Staff {
    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission.";

    @JsonProperty("ARGUMENTS")
    private Arguments arguments = new Arguments();

    @Getter
    public static class Arguments {
      @JsonProperty("CHAT")
      private Chat chat = new Chat();

      @JsonProperty("ENABLE")
      private Enable enable = new Enable();

      @JsonProperty("FORCE-END")
      private ForceEnd forceEnd = new ForceEnd();

      @JsonProperty("STOP")
      private Stop stop = new Stop();

      @JsonProperty("FOLLOW")
      private Follow follow = new Follow();

      @JsonProperty("HELP")
      private List<String> help =
          List.of(
              "<red><st>------------------------------------------------",
              " <red> » /%label% enable - Enable staff mode.",
              " <red> » /%label% chat - Toggle staff chat.",
              " <red> » /%label% chat <message> - Send message in staff chat.",
              " <red> » /%label% vanish - Toggle visibility.",
              " <red> » /%label% forceend <player> - Force end any match or event.",
              " <red> » /%label% stop <player> - Remove a player from a match or event.",
              "<red><st>------------------------------------------------");

      @JsonProperty("VANISH")
      private Vanish vanish = new Vanish();

      @Getter
      public static class Chat {
        @JsonProperty("DISABLED")
        private String disabled = "<red>Staff chat is disabled.";

        @JsonProperty("CHAT-ENABLED")
        private String chatEnabled = "<yellow>Staff chat <green>enabled<yellow>.";

        @JsonProperty("CHAT-DISABLED")
        private String chatDisabled = "<yellow>Staff chat <red>disabled<yellow>.";
      }

      @Getter
      public static class Follow {
        @JsonProperty("NO-PERMISSION")
        private String noPermission = "<red>You don't have permission.";

        @JsonProperty("UNAVAILABLE")
        private String unavailable = "<red>Currently not a feature of the plugin.";
      }

      @Getter
      public static class Enable {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% enable";

        @JsonProperty("ONLY-IN-LOBBY")
        private String onlyInLobby = "<red>You can only enable staff mode in the lobby.";
      }

      @Getter
      public static class ForceEnd {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% forceend <player>";

        @JsonProperty("ONLY-IN-STAFFMODE")
        private String onlyInStaffmode = "<red>You can only use this command in staff mode.";

        @JsonProperty("TARGET-OFFLINE")
        private String targetOffline = "<red>%target% is not online.";

        @JsonProperty("MATCH")
        private Match match = new Match();

        @JsonProperty("EVENT")
        private Event event = new Event();

        @JsonProperty("PLAYER-INACTIVE")
        private String playerInactive = "<red>Player is not in a match, nor event.";

        @Getter
        public static class Match {
          @JsonProperty("ENDED")
          private String ended = "<red>The match has already ended.";

          @JsonProperty("CANT-END")
          private String cantEnd = "<red>You can't end this match!";

          @JsonProperty("PLAYER-END-MSG")
          private String playerEndMsg = "<yellow>You ended <red>%target%'s <yellow> match.";

          @JsonProperty("MATCH-END-MSG")
          private String matchEndMsg = "<red>The match has been ended by %player%.";
        }

        @Getter
        public static class Event {
          @JsonProperty("ENDED")
          private String ended = "<red>Event has already ended.";

          @JsonProperty("CANT-END")
          private String cantEnd = "<red>You can't end this event!";

          @JsonProperty("PLAYER-END-MSG")
          private String playerEndMsg = "<yellow>You successfully ended the event.";

          @JsonProperty("MATCH-END-MSG")
          private String matchEndMsg = "<red>The event forcefully ended by %player%.";
        }
      }

      @Getter
      public static class Stop {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% stop <player>";

        @JsonProperty("TARGET-OFFLINE")
        private String targetOffline = "<red>%target% is not online.";

        @JsonProperty("TARGET-BYPASS")
        private String targetBypass = "<red>%target%'s activities cannot be stopped.";

        @JsonProperty("MATCH")
        private Match match = new Match();

        @JsonProperty("EVENT")
        private Event event = new Event();

        @JsonProperty("SPECTATE")
        private Spectate spectate = new Spectate();

        @Getter
        public static class Match {
          @JsonProperty("PLAYER")
          private String player = "<yellow>You removed <red>%target% <yellow>from the match.";

          @JsonProperty("TARGET")
          private String target =
              "<yellow>You've got removed from the match by <red>%player%<yellow>.";

          @JsonProperty("MATCH")
          private String match = "<red>%target% <yellow>got removed from the match.";
        }

        @Getter
        public static class Event {
          @JsonProperty("PLAYER")
          private String player = "<yellow>You removed <red>%target% <yellow>from the event.";

          @JsonProperty("TARGET")
          private String target =
              "<yellow>You've got removed from the event by <red>%player%<yellow>.";

          @JsonProperty("EVENT")
          private String event = "<red>%target% <yellow>got removed from the event.";
        }

        @Getter
        public static class Spectate {
          @JsonProperty("PLAYER")
          private String player = "<yellow>You removed <red>%target% <yellow> as spectator.";

          @JsonProperty("TARGET")
          private String target =
              "<yellow>You've got removed from spectating by <red>%player%<yellow>.";
        }
      }

      @Getter
      public static class Vanish {
        @JsonProperty("COMMAND-HELP")
        private String commandHelp = "<red>/%label% vanish";

        @JsonProperty("CANT-USE")
        private String cantUse = "<red>You can't use this command right now.";

        @JsonProperty("INVISIBLE")
        private String invisible =
            "<yellow>You are now <red>invisible <yellow>for the players of the server.";

        @JsonProperty("VISIBLE")
        private String visible =
            "<yellow>You are now <red>visible <yellow>for the players of the server.";
      }
    }
  }

  @Getter
  public static class Statistics {
    @JsonProperty("CANT-USE")
    private String cantUse = "<red>You can't use this right now!";

    @JsonProperty("NO-PERMISSION")
    private String noPermission = "<red>You don't have permission.";

    @JsonProperty("TARGET-NOT-EXISTS")
    private String targetNotExists = "<red>%target% cannot be found.";

    @JsonProperty("COMMAND-HELP")
    private List<String> commandHelp =
        List.of("<red>/%label% stats", "<red>/%label% stats <player>");
  }
}
