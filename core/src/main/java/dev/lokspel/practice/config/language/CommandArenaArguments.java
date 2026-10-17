package dev.lokspel.practice.config.language;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
public class CommandArenaArguments {
  @JsonProperty("HELP")
  private Help help = new Help();

  @JsonProperty("INFO")
  private Info info = new Info();

  @JsonProperty("CREATE")
  private Create create = new Create();

  @JsonProperty("DELETE")
  private Delete delete = new Delete();

  @JsonProperty("ICON")
  private Icon icon = new Icon();

  @JsonProperty("SETUP")
  private Setup setup = new Setup();

  @JsonProperty("CORNER")
  private Corner corner = new Corner();

  @JsonProperty("POSITION")
  private Position position = new Position();

  @JsonProperty("FFA-POSITIONS")
  private FfaPositions ffaPositions = new FfaPositions();

  @JsonProperty("BED")
  private Bed bed = new Bed();

  @JsonProperty("PORTAL")
  private Portal portal = new Portal();

  @JsonProperty("PORTAL-PROTECTION")
  private PortalProtection portalProtection = new PortalProtection();

  @JsonProperty("SETBUILDMAX")
  private Setbuildmax setbuildmax = new Setbuildmax();

  @JsonProperty("SETDEADZONE")
  private Setdeadzone setdeadzone = new Setdeadzone();

  @JsonProperty("FREEZE")
  private Freeze freeze = new Freeze();

  @JsonProperty("ENABLE")
  private Enable enable = new Enable();

  @JsonProperty("DISABLE")
  private Disable disable = new Disable();

  @JsonProperty("STOP")
  private Stop stop = new Stop();

  @JsonProperty("TELEPORT")
  private Teleport teleport = new Teleport();

  @JsonProperty("SIDEBUILDLIMIT")
  private Sidebuildlimit sidebuildlimit = new Sidebuildlimit();

  @JsonProperty("PARTYFFACENTER")
  private Partyffacenter partyffacenter = new Partyffacenter();

  @JsonProperty("SPECTATOR")
  private Spectator spectator = new Spectator();

  @Getter
  public static class Help {
    @JsonProperty("STAFF")
    private List<String> staff =
        List.of(
            "<red><st>------------------------------------------",
            " <red>Arena Help",
            "<red><st>------------------------------------------",
            " <red>» <click:suggest_command:'/%label% freeze '><hover:show_text:'<gray>Freeze an"
                + " arena.'>/%label% freeze <arena>",
            " <red>» <click:suggest_command:'/%label% stop '><hover:show_text:'<gray>Stop all the"
                + " games in an arena.'>/%label% stop <arena>",
            "<red><st>------------------------------------------");

    @JsonProperty("ADMIN")
    private List<String> admin =
        List.of(
            "<red><st>------------------------------------------",
            " <red>Arena Help",
            "<red><st>------------------------------------------",
            " <red>» <click:suggest_command:'/%label% info '><hover:show_text:'<gray>Detailed"
                + " information of the arena.'>/%label% info <arena>",
            " <red>» <click:suggest_command:'/%label% create '><hover:show_text:'<gray>Create a new"
                + " arena.'>/%label% create <name>",
            " <red>» <click:suggest_command:'/%label% setup '><hover:show_text:'<gray>Setup an"
                + " arena.'>/%label% setup <name>",
            " <red>» <click:suggest_command:'/%label% delete '><hover:show_text:'<gray>Delete an"
                + " arena.'>/%label% delete <arena>",
            " <red>» <click:suggest_command:'/%label% set '><hover:show_text:'<gray>Set up various"
                + " details of the arena.'>/%label% set <sub_command>",
            " <red>» <click:suggest_command:'/%label% freeze '><hover:show_text:'<gray>Freeze an"
                + " arena.'>/%label% freeze <arena>",
            " <red>» <click:suggest_command:'/%label% stop '><hover:show_text:'<gray>Stop all the"
                + " games in an arena.'>/%label% stop <arena>",
            "<red><st>------------------------------------------");

    @JsonProperty("SET-COMMAND")
    private List<String> setCommand =
        List.of(
            "<red><st>------------------------------------------",
            " <red>» <click:suggest_command:'/%label% set icon '><hover:show_text:'<gray>Set the"
                + " icon.'>/%label% set icon <arena>",
            " <red>» <click:suggest_command:'/%label% set pos '><hover:show_text:'<gray>Set the"
                + " teleport positions.'>/%label% set pos <arena> <1/2>",
            " <red>» <click:suggest_command:'/%label% set ffapos '><hover:show_text:'<gray>Set the"
                + " ffa positions.'>/%label% set ffapos <arena>",
            " <red>» <click:suggest_command:'/%label% set bed '><hover:show_text:'<gray>Set the"
                + " beds of a bedwars arena.'>/%label% set bed <arena> <1/2>",
            " <red>» <click:suggest_command:'/%label% set portal '><hover:show_text:'<gray>Set the"
                + " portals for portal arena.'>/%label% set portal <arena> <1/2>",
            " <red>» <click:suggest_command:'/%label% set buildmax '><hover:show_text:'<gray>Set"
                + " the maximum build height.'>/%label% set buildmax <arena>",
            " <red>» <click:suggest_command:'/%label% set deadzone '><hover:show_text:'<gray>Set"
                + " the deadzone Y level.'>/%label% set deadzone <arena>",
            " <red>» <click:suggest_command:'/%label% set sidebuildlimit"
                + " '><hover:show_text:'<gray>Set the side building limit.'>/%label% set"
                + " sidebuildlimit <arena>",
            " <red>» <click:suggest_command:'/%label% set partyffacenter"
                + " '><hover:show_text:'<gray>Set Party FFA center to your location.'>/%label% set"
                + " partyffacenter <arena>",
            " <red>» <click:suggest_command:'/%label% set spectator '><hover:show_text:'<gray>Set"
                + " spectator position to your location.'>/%label% set spectator <arena>",
            "<red><st>------------------------------------------");
  }

  @Getter
  public static class Info {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% info <name>";

    @JsonProperty("NOT-EXISTS")
    private String notExists = "<red>Arena %arena% doesn't exists.";

    @JsonProperty("STATUS-NAMES")
    private StatusNames statusNames = new StatusNames();

    @JsonProperty("ARENA-INFO")
    private List<String> arenaInfo =
        List.of(
            "<gray><st>-----------------------------------",
            " <gold>Arena: <white>%arena%",
            "<gray><st>-----------------------------------",
            " <gray>» <gold>Type: <white>%type%",
            " <gray>» <gold>Icon: <white>%icon%",
            " <gray>» <gold>Display Name: <white>%displayName%",
            " <gray>» <gold>Ladders: <white>%ladders%",
            "",
            " <gray>» <gold>Corner 1: <white>%corner1%",
            " <gray>» <gold>Corner 2: <white>%corner2%",
            "",
            " <gray>» <gold>Player 2 Position: <white>%position1%",
            " <gray>» <gold>Player 2 Position: <white>%position2%",
            " <gray>» <gold>Party FFA Center: <white>%partyFfaCenter%",
            " <gray>» <gold>Spectator Position: <white>%spectatorPosition%",
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
  public static class Create {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% create <name>";

    @JsonProperty("REACHED-MAX")
    private String reachedMax =
        "<red>The server has reached the maximum number of arenas. (243/243)";
  }

  @Getter
  public static class Delete {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% delete <arena>";

    @JsonProperty("NOT-EXISTS")
    private String notExists = "<red>Arena %arena% doesn't exists.";

    @JsonProperty("ARENA-ENABLED")
    private String arenaEnabled = "<red>You can't delete an enabled arena.";

    @JsonProperty("DELETE-SUCCESS")
    private String deleteSuccess =
        "<green>You successfully <red>deleted <green>arena <gold>%arena%<green>.";
  }

  @Getter
  public static class Icon {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% set icon <arena>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("CANT-EDIT")
    private String cantEdit = "<red>You can't edit a enabled arena.";

    @JsonProperty("NO-DISPLAYNAME")
    private String noDisplayname = "<red>You should name the icon before set it for the arena.";

    @JsonProperty("ICON-IN-HAND")
    private String iconInHand = "<red>You must hold the icon item in your hand.";

    @JsonProperty("SAVED-ICON")
    private String savedIcon =
        "<green>You saved the <yellow>icon <green>for <gold>%arena% <green>arena.";
  }

  @Getter
  public static class Setup {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% setup <arena>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("CANT-EDIT")
    private String cantEdit = "<red>You can't edit a enabled arena.";
  }

  @Getter
  public static class Corner {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% set corner <arena> <1/2>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("CANT-EDIT")
    private String cantEdit = "<red>You can't edit arenas that have copies. (%arena%<red>)";

    @JsonProperty("CANT-EDIT2")
    private String cantEdit2 = "<red>You can't edit a enabled arena.";

    @JsonProperty("NO-BLOCK")
    private String noBlock = "<red>Block location can not be found!";

    @JsonProperty("CORNER-WORLD")
    private String cornerWorld = "<red>The corner must be located in the arenas world.";

    @JsonProperty("INVALID-NUMBER")
    private String invalidNumber = "<red>Invalid corner number!";

    @JsonProperty("SAVED-CORNER")
    private String savedCorner =
        "<green>You saved the <yellow>%corner% <green>corner for arena <gold>%arena%<green>.";

    @JsonProperty("POSITION-REMOVED")
    private String positionRemoved =
        "<red>Position <yellow>%position% <red>got removed from arena %arena% <red>because it was"
            + " out of the arena cube.";

    @JsonProperty("BUILD-MAX-REMOVED")
    private String buildMaxRemoved =
        "<red>BuildMax Y level has been removed from arena %arena% because it was no longer inside"
            + " the arena cube.";

    @JsonProperty("BUILD-MAX-ADJUSTED")
    private String buildMaxAdjusted =
        "<yellow>BuildMax for arena %arena% has been adjusted from Y=%old% to Y=%new% to fit within"
            + " arena bounds.";

    @JsonProperty("DEAD-ZONE-REMOVED")
    private String deadZoneRemoved =
        "<red>DeadZone Y level has been removed from arena %arena% because it was no longer inside"
            + " the arena cube.";

    @JsonProperty("BED-REMOVED")
    private String bedRemoved =
        "<red>Bed location %bed% got removed from arena %arena% <red>because it was out of the"
            + " arena cube.";
  }

  @Getter
  public static class Position {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% set %label2% <arena> <1/2>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("CANT-EDIT")
    private String cantEdit = "<red>You can't edit arenas that have copies. (%arena%)";

    @JsonProperty("CANT-EDIT2")
    private String cantEdit2 = "<red>You can't edit a enabled arena.";

    @JsonProperty("NO-REGION")
    private String noRegion = "<red>First you have to set the two corner of the arena.";

    @JsonProperty("INVALID-NUMBER")
    private String invalidNumber = "<red>Invalid number.";

    @JsonProperty("POS-OUTSIDE-REGION")
    private String posOutsideRegion = "<red>The position must be inside the arena cube.";

    @JsonProperty("DEADZONE-TO-HIGH")
    private String deadzoneToHigh =
        "<red>Arena DeadZone's Y level was higher than the new spawn position so it got removed.";

    @JsonProperty("SAVED-POSITION")
    private String savedPosition =
        "<green>You saved the %position% <yellow>position <green>for arena <gold>%arena%<green>.";
  }

  @Getter
  public static class FfaPositions {
    @JsonProperty("COMMAND-HELP")
    private List<String> commandHelp =
        List.of(
            "<red>/%label% set ffapos <arena> add - Add a FFA spawn position.",
            "<red>/%label% set ffapos <arena> reset - Reset all the FFA spawn positions.");

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("CANT-EDIT")
    private String cantEdit = "<red>You can't edit a enabled arena.";

    @JsonProperty("CANT-EDIT2")
    private String cantEdit2 = "<red>You can't edit arenas that have copies. (%arena%)";

    @JsonProperty("NO-REGION")
    private String noRegion = "<red>You must set the two corners of the arena first.";

    @JsonProperty("POS-OUTSIDE-REGION")
    private String posOutsideRegion = "<red>The position must be inside the arena cube.";

    @JsonProperty("MAX-18")
    private String max18 = "<red>You can't have more than 18 FFA spawn positions in an arena.";

    @JsonProperty("SET-FFAPOS")
    private String setFfapos =
        "<yellow>You added the FFA spawn position for the arena <gold>%arena%<yellow>. It has now"
            + " <red>%posCount% <yellow>positions.";

    @JsonProperty("RESET-FFAPOS")
    private String resetFfapos =
        "<yellow>You reset all the FFA spawn positions for the arena <gold>%arena%<yellow>.";
  }

  @Getter
  public static class Bed {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% set bed <arena> <1/2>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("ARENA-ENABLED")
    private String arenaEnabled = "<red>You can't edit an enabled arena.";

    @JsonProperty("NOT-BUILD")
    private String notBuild = "<red>Arena must be build to set this property.";

    @JsonProperty("NO-REGION")
    private String noRegion = "<red>You must set the two corners of the arena first.";

    @JsonProperty("NO-BED")
    private String noBed = "<red>You must be looking at a bed right now.";

    @JsonProperty("NOT-IN-REGION")
    private String notInRegion = "<red>The bed must be in the arena world inside the arenas cube.";

    @JsonProperty("INVALID-NUMBER")
    private String invalidNumber = "<red>Invalid team number!";

    @JsonProperty("SET-BED1")
    private String setBed1 = "<green>You set the first bed location for the arena %arena%";

    @JsonProperty("SET-BED2")
    private String setBed2 = "<green>You set the second bed location for the arena %arena%";
  }

  @Getter
  public static class Portal {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% set portal <arena> <1/2>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("ARENA-ENABLED")
    private String arenaEnabled = "<red>You can't edit an enabled arena.";

    @JsonProperty("NOT-BUILD")
    private String notBuild = "<red>Arena must be build to set this property.";

    @JsonProperty("NO-REGION")
    private String noRegion = "<red>You must set the two corners of the arena first.";

    @JsonProperty("NO-BLOCK")
    private String noBlock = "<red>You must be look at a block right now.";

    @JsonProperty("NOT-IN-REGION")
    private String notInRegion =
        "<red>The portal must be in the arena world inside the arenas cube.";

    @JsonProperty("INVALID-NUMBER")
    private String invalidNumber = "<red>Invalid team number!";

    @JsonProperty("PORTAL-OVERLAP")
    private String portalOverlap = "<red>Portals cannot overlap with X and Z coordinates.";

    @JsonProperty("TOO-LOW-FOR-FLOOR")
    private String tooLowForFloor =
        "<red>The portal must be at least 1 block above the %floor_type% (Y:"
            + " <yellow>%floor_y%<red>).";

    @JsonProperty("SET-PORTAL1")
    private String setPortal1 = "<green>You set the first portal location for the arena %arena%";

    @JsonProperty("SET-PORTAL2")
    private String setPortal2 = "<green>You set the second portal location for the arena %arena%";
  }

  @Getter
  public static class PortalProtection {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% set portalprot <arena> <number>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("ARENA-ENABLED")
    private String arenaEnabled = "<red>You can't edit a enabled arena.";

    @JsonProperty("NOT-BUILD")
    private String notBuild = "<red>Arena must be build to set this property.";

    @JsonProperty("NOT-INTEGER")
    private String notInteger = "<red>Please give me a correctly formatted number between 0 and 20";

    @JsonProperty("SET-PORTALPROT")
    private String setPortalprot =
        "<green>You set the portal protection for the arena <yellow>%arena%<green>. Portal"
            + " Protection: <red>%portalProt%";
  }

  @Getter
  public static class Setbuildmax {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% set buildmax <arena>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("ARENA-NOT-BUILD")
    private String arenaNotBuild = "<red>You can't set build-max property to a non build arena.";

    @JsonProperty("CANT-EDIT")
    private String cantEdit = "<red>You can't edit arenas that have copies. (%arena%)";

    @JsonProperty("CANT-EDIT2")
    private String cantEdit2 = "<red>You can't edit a enabled arena.";

    @JsonProperty("NO-REGION")
    private String noRegion = "<red>First you have to set the two corner of the arena.";

    @JsonProperty("POS-OUTSIDE-REGION")
    private String posOutsideRegion = "<red>The position must be inside the arena cube.";

    @JsonProperty("SET-BUILDMAX")
    private String setBuildmax =
        "<green>You set the build max for the arena <yellow>%arena%<green>. Y level:"
            + " <red>%y-level%";
  }

  @Getter
  public static class Setdeadzone {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% set deadzone <arena>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("CANT-EDIT")
    private String cantEdit = "<red>You can't edit arenas that have copies. (%arena%)";

    @JsonProperty("CANT-EDIT2")
    private String cantEdit2 = "<red>You can't edit a enabled arena.";

    @JsonProperty("NO-REGION")
    private String noRegion = "<red>First you have to set the two corner of the arena.";

    @JsonProperty("POS-OUTSIDE-REGION")
    private String posOutsideRegion = "<red>The position must be inside the arena cube.";

    @JsonProperty("LOWER-THAN-SPAWN")
    private String lowerThanSpawn =
        "<red>You have to set the <yellow>DeadZone <red>lower than the lowest spawn position's Y"
            + " level.";

    @JsonProperty("SET-DEADZONE")
    private String setDeadzone =
        "<green>You set the dead zone for the arena <yellow>%arena%<green>. Y level:"
            + " <red>%y-level%";
  }

  @Getter
  public static class Freeze {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% freeze <arena>";

    @JsonProperty("NOT-EXISTS")
    private String notExists = "<red>Arena %arena% doesn't exists.";

    @JsonProperty("ARENA-FROZEN")
    private String arenaFrozen = "<red>You can't freeze a disabled arena.";

    @JsonProperty("FREEZE-SUCCESS")
    private String freezeSuccess =
        "<yellow>You have successfully <aqua>frozen <yellow>the <gold>%arena%<yellow>arena.";

    @JsonProperty("UNFREEZE-SUCCESS")
    private String unfreezeSuccess =
        "<yellow>You have successfully <green>defrost <yellow>the <gold>%arena% <yellow>arena.";
  }

  @Getter
  public static class Enable {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% enable <arena>";

    @JsonProperty("NOT-EXISTS")
    private String notExists = "<red>Arena %arena% doesn't exists.";

    @JsonProperty("ALREADY-ENABLED")
    private String alreadyEnabled = "<yellow>%arena% <red>arena is already enabled.";

    @JsonProperty("ARENA-ENABLED")
    private String arenaEnabled = "<gold>%arena% <yellow>arena is now <green>enabled<yellow>.";
  }

  @Getter
  public static class Disable {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% disable <arena>";

    @JsonProperty("NOT-EXISTS")
    private String notExists = "<red>Arena %arena% doesn't exists.";

    @JsonProperty("ALREADY-DISABLED")
    private String alreadyDisabled = "<yellow>%arena% <red>arena is already disabled.";

    @JsonProperty("ARENA-DISABLED")
    private String arenaDisabled = "<gold>%arena% <yellow>arena is now <red>disabled<yellow>.";
  }

  @Getter
  public static class Stop {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% stop <arena>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("ARENA-ENABLED")
    private String arenaEnabled = "<red>The arena is disabled, so there can be no matches in it.";

    @JsonProperty("NO-MATCHES")
    private String noMatches = "<red>There are currently no live matches in the arena.";

    @JsonProperty("MATCH-END-MESSAGE")
    private String matchEndMessage = "<red>The match has been ended by %player%.";

    @JsonProperty("PLAYER-ENDED")
    private String playerEnded = "<red>You successfully ended all the matches in the arena.";
  }

  @Getter
  public static class Teleport {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% teleport <name>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("NO-POSITION")
    private String noPosition = "<red>Arena does not have any available positions.";
  }

  @Getter
  public static class Sidebuildlimit {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% set sidebuildlimit <arena> <number>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("ARENA-ENABLED")
    private String arenaEnabled = "<red>You can't edit a enabled arena.";

    @JsonProperty("NOT-BUILD")
    private String notBuild = "<red>Arena must be build to set this property.";

    @JsonProperty("INVALID-NUMBER")
    private String invalidNumber = "<red>Please give a correctly formatted number between 0 and 10";

    @JsonProperty("SET-SIDEBUILDLIMIT")
    private String setSidebuildlimit =
        "<green>You set the side build limit for the arena <yellow>%arena%<green>. Side Build"
            + " Limit: <red>%sideBuildLimit%";
  }

  @Getter
  public static class Partyffacenter {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% set partyffacenter <arena>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("ARENA-ENABLED")
    private String arenaEnabled = "<red>You can't edit an enabled arena.";

    @JsonProperty("CANT-EDIT")
    private String cantEdit = "<red>You can't edit arenas that have copies. (%arena%)";

    @JsonProperty("NO-REGION")
    private String noRegion = "<red>You must set the two corners of the arena first.";

    @JsonProperty("POS-OUTSIDE-REGION")
    private String posOutsideRegion = "<red>The center must be inside the arena cube.";

    @JsonProperty("SET")
    private String set = "<green>You set the Party FFA center for arena <yellow>%arena%<green>.";
  }

  @Getter
  public static class Spectator {
    @JsonProperty("COMMAND-HELP")
    private String commandHelp = "<red>/%label% set spectator <arena>";

    @JsonProperty("ARENA-NOT-EXISTS")
    private String arenaNotExists = "<red>%arena% arena doesn't exists.";

    @JsonProperty("ARENA-ENABLED")
    private String arenaEnabled = "<red>You can't edit an enabled arena.";

    @JsonProperty("CANT-EDIT")
    private String cantEdit = "<red>You can't edit arenas that have copies. (%arena%)";

    @JsonProperty("NO-REGION")
    private String noRegion = "<red>You must set the two corners of the arena first.";

    @JsonProperty("POS-OUTSIDE-REGION")
    private String posOutsideRegion = "<red>The spectator position must be inside the arena cube.";

    @JsonProperty("SET")
    private String set = "<green>You set the spectator position for arena <yellow>%arena%<green>.";
  }
}
