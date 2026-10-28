package dev.lokspel.practice.config.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import me.whereareiam.configura.annotation.merge.Merge;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;
import me.whereareiam.configura.type.merge.MissingEntries;

@Getter
public class GroupConfig {
  @Comment({
    "Player Group settings",
    "",
    "Permission: ap.group.%name_in_lower_case% for example: ap.group.premium",
    "Op's get the rank with the highest weight possible."
  })
  @Merge(missingEntries = MissingEntries.OMIT)
  @JsonProperty("GROUPS")
  private Map<String, Group> groups = defaultGroups();

  private static Map<String, Group> defaultGroups() {
    Map<String, Group> values = new LinkedHashMap<>();
    values.put("DEFAULT", Group.defaultGroup());
    values.put("PREMIUM", Group.premium());
    values.put("SUPREME", Group.supreme());
    values.put("STAFF", Group.staff());
    values.put("ADMIN", Group.admin());

    return values;
  }

  @Getter
  public static class Group {
    @JsonProperty("NAME")
    private String name;

    @JsonProperty("WEIGHT")
    private Integer weight;

    @Comment("Set to -1 for unlimited, 0 to disable.")
    @JsonProperty("UNRANKED-PER-DAY")
    private Integer unrankedPerDay;

    @Comment("Set to -1 for unlimited, 0 to disable.")
    @JsonProperty("RANKED-PER-DAY")
    private Integer rankedPerDay;

    @Comment("Set to -1 for unlimited, 0 to disable.")
    @JsonProperty("EVENT-START-PER-DAY")
    private Integer eventStartPerDay;

    @Comment("Set to -1 for unlimited, 0 to disable.")
    @JsonProperty("PARTY-BROADCAST-PER-DAY")
    private Integer partyBroadcastPerDay;

    @JsonProperty("PARTY-MEMBER-LIMIT")
    private Integer partyMemberLimit;

    @Comment("Maximum 5")
    @JsonProperty("CUSTOM-KIT")
    private Integer customKit;

    @JsonProperty("MODIFIABLE-KIT-PER-LADDER")
    private Integer modifiableKitPerLadder;

    @JsonProperty("LOBBY-NAMETAG")
    private LobbyNametag lobbyNametag;

    @Comment("You can delete this line in groups, they just get the default server-chat format.")
    @JsonProperty("CHAT-FORMAT")
    private String chatFormat;

    static Group defaultGroup() {
      Group value = new Group();
      value.name = "<gray>Default";
      value.weight = 0;
      value.unrankedPerDay = 20;
      value.rankedPerDay = 10;
      value.eventStartPerDay = 0;
      value.partyBroadcastPerDay = 0;
      value.partyMemberLimit = 6;
      value.customKit = 1;
      value.modifiableKitPerLadder = 1;
      value.lobbyNametag = LobbyNametag.defaultLobbyNametag();
      value.chatFormat = "%%player%%: <white>%%message%%";

      return value;
    }

    static Group premium() {
      Group value = new Group();
      value.name = "<red>Premium";
      value.weight = 1;
      value.unrankedPerDay = 50;
      value.rankedPerDay = 30;
      value.eventStartPerDay = 1;
      value.partyBroadcastPerDay = 1;
      value.partyMemberLimit = 10;
      value.customKit = 2;
      value.modifiableKitPerLadder = 2;
      value.lobbyNametag = LobbyNametag.premium();
      value.chatFormat = "%%player%%: <white>%%message%%";

      return value;
    }

    static Group supreme() {
      Group value = new Group();
      value.name = "<gold>Supreme";
      value.weight = 2;
      value.unrankedPerDay = 100;
      value.rankedPerDay = 50;
      value.eventStartPerDay = 2;
      value.partyBroadcastPerDay = 3;
      value.partyMemberLimit = 14;
      value.customKit = 3;
      value.modifiableKitPerLadder = 4;
      value.lobbyNametag = LobbyNametag.supreme();
      value.chatFormat = "%%player%%: <white>%%message%%";

      return value;
    }

    static Group staff() {
      Group value = new Group();
      value.name = "<green>Staff";
      value.weight = 3;
      value.unrankedPerDay = 0;
      value.rankedPerDay = 0;
      value.eventStartPerDay = 3;
      value.partyBroadcastPerDay = 5;
      value.partyMemberLimit = 20;
      value.customKit = 0;
      value.modifiableKitPerLadder = 0;
      value.lobbyNametag = LobbyNametag.staff();
      value.chatFormat = "%%player%%: <white>%%message%%";

      return value;
    }

    static Group admin() {
      Group value = new Group();
      value.name = "<light_purple>Admin";
      value.weight = 4;
      value.unrankedPerDay = 500;
      value.rankedPerDay = 100;
      value.eventStartPerDay = 100;
      value.partyBroadcastPerDay = 100;
      value.partyMemberLimit = 25;
      value.customKit = 5;
      value.modifiableKitPerLadder = 4;
      value.lobbyNametag = LobbyNametag.admin();
      value.chatFormat = "%%player%%: <white>%%message%%";

      return value;
    }

    @Getter
    public static class LobbyNametag {
      @JsonProperty("PREFIX")
      private String prefix;

      @JsonProperty("NAME")
      private String name;

      @JsonProperty("SUFFIX")
      private String suffix;

      @JsonProperty("SORT-PRIORITY")
      private Integer sortPriority;

      static LobbyNametag defaultLobbyNametag() {
        LobbyNametag value = new LobbyNametag();
        value.prefix = "%division_short% <dark_gray>|<gray> ";
        value.name = "<gradient:green:blue>%player%</gradient>";
        value.suffix = "";
        value.sortPriority = 4;

        return value;
      }

      static LobbyNametag premium() {
        LobbyNametag value = new LobbyNametag();
        value.prefix = "%division_short% <dark_gray>| ";
        value.name = "<gradient:green:blue>%player%</gradient>";
        value.suffix = " <dark_gray>[<red>Premium<dark_gray>]<gray>";
        value.sortPriority = 3;

        return value;
      }

      static LobbyNametag supreme() {
        LobbyNametag value = new LobbyNametag();
        value.prefix = "%division_short% <dark_gray>| ";
        value.name = "<gray>%player%";
        value.suffix = " <dark_gray>[<gold>Supreme<dark_gray>]<gray>";
        value.sortPriority = 2;

        return value;
      }

      static LobbyNametag staff() {
        LobbyNametag value = new LobbyNametag();
        value.prefix = "<dark_gray>[<green>Staff<dark_gray>]<gray> ";
        value.name = "<gray>%player%";
        value.suffix = "";
        value.sortPriority = 1;

        return value;
      }

      static LobbyNametag admin() {
        LobbyNametag value = new LobbyNametag();
        value.prefix = "<dark_gray>[<light_purple>Admin<dark_gray>]<gray> ";
        value.name = "<gray>%player%";
        value.suffix = "";
        value.sortPriority = 0;

        return value;
      }
    }
  }
}
