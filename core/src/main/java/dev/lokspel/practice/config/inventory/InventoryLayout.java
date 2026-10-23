package dev.lokspel.practice.config.inventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.lokspel.practice.config.ItemConfig;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import me.whereareiam.configura.annotation.merge.Merge;
import me.whereareiam.configura.feature.comment.api.annotation.Comment;
import me.whereareiam.configura.type.merge.MissingEntries;

@Getter
public class InventoryLayout {
  @JsonProperty("NORMAL")
  private Map<String, InventoryItem> normal;

  @Comment({
    "Extra items, for example:",
    "STUFF-ITEM:",
    "  COMMAND: unranked # Don't write the / character before the command.",
    "  SLOT: 2",
    "  ITEM:",
    "    NAME: \"<red>Stuff Item\"",
    "    MATERIAL: DIRT",
    "    LORE:",
    "      - \"\"",
    "      - \"<light_purple>Lore\""
  })
  @Merge(missingEntries = MissingEntries.OMIT)
  @JsonProperty("EXTRA")
  private Map<String, ExtraItem> extra;

  static InventoryLayout lobbyBasic() {
    InventoryLayout value = new InventoryLayout();
    value.normal = new LinkedHashMap<>();
    value.normal.put("QUEUE", InventoryItem.queue());
    value.normal.put("UNRANKED", InventoryItem.unranked());
    value.normal.put("RANKED", InventoryItem.ranked());
    value.normal.put("COSMETICS", InventoryItem.cosmetics());
    value.normal.put("ENABLE-SPECTATE-MODE", InventoryItem.enableSpectateMode());
    value.normal.put("PARTY-CREATE", InventoryItem.partyCreate());
    value.normal.put("PARTY-JOIN", InventoryItem.partyJoin());
    value.normal.put("SETTINGS", InventoryItem.settings());
    value.normal.put("KIT-EDITOR", InventoryItem.kitEditor());
    value.normal.put("SETUP", InventoryItem.setup());
    value.normal.put("STAFF-MODE", InventoryItem.staffMode());
    value.normal.put("REMATCH", InventoryItem.rematch());
    value.normal.put("STATISTICS", InventoryItem.statistics());
    value.extra = new LinkedHashMap<>();

    return value;
  }

  static InventoryLayout party() {
    InventoryLayout value = new InventoryLayout();
    value.normal = new LinkedHashMap<>();
    value.normal.put("HOST-PARTY-GAME", InventoryItem.hostPartyGame());
    value.normal.put("PARTY-INFO", InventoryItem.partyInfo());
    value.normal.put("OTHER-PARTIES", InventoryItem.otherParties());
    value.normal.put("LEAVE-PARTY", InventoryItem.leaveParty());
    value.normal.put("PARTY-SETTINGS", InventoryItem.partySettings());
    value.normal.put("PARTY-KIT-EDITOR", InventoryItem.partyKitEditor());
    value.extra = new LinkedHashMap<>();

    return value;
  }

  static InventoryLayout queueMatch() {
    InventoryLayout value = new InventoryLayout();
    value.normal = new LinkedHashMap<>();
    value.normal.put("LEAVE-MATCH-QUEUE", InventoryItem.leaveMatchQueue());
    value.extra = new LinkedHashMap<>();

    return value;
  }

  static InventoryLayout queueEvent() {
    InventoryLayout value = new InventoryLayout();
    value.normal = new LinkedHashMap<>();
    value.normal.put("LEAVE-EVENT-QUEUE", InventoryItem.leaveEventQueue());
    value.extra = new LinkedHashMap<>();

    return value;
  }

  static InventoryLayout lobby() {
    InventoryLayout value = new InventoryLayout();
    value.normal = new LinkedHashMap<>();
    value.normal.put("MENU", InventoryItem.menu());
    value.normal.put("DISABLE", InventoryItem.disable());
    value.normal.put("RANDOM", InventoryItem.lobbyRandom());
    value.extra = new LinkedHashMap<>();

    return value;
  }

  static InventoryLayout spectatorMatch() {
    InventoryLayout value = new InventoryLayout();
    value.normal = new LinkedHashMap<>();
    value.normal.put("MENU", InventoryItem.menu());
    value.normal.put("RANDOM", InventoryItem.matchRandom());
    value.normal.put("TARGETS", InventoryItem.targets());
    value.normal.put("SHOW-SPECTATORS", InventoryItem.showSpectators());
    value.normal.put("HIDE-SPECTATORS", InventoryItem.hideSpectators());
    value.normal.put("LEAVE", InventoryItem.leave());
    value.extra = new LinkedHashMap<>();

    return value;
  }

  static InventoryLayout spectatorEvent() {
    InventoryLayout value = new InventoryLayout();
    value.normal = new LinkedHashMap<>();
    value.normal.put("LEAVE", InventoryItem.leave());
    value.extra = new LinkedHashMap<>();

    return value;
  }

  static InventoryLayout staffMode() {
    InventoryLayout value = new InventoryLayout();
    value.normal = new LinkedHashMap<>();
    value.normal.put("RANDOM-GAME-SPECTATE", InventoryItem.randomGameSpectate());
    value.normal.put("PLAYER-INVENTORY", InventoryItem.playerInventory());
    value.normal.put("HIDE-FROM-PLAYERS-ON", InventoryItem.hideFromPlayersOn());
    value.normal.put("HIDE-FROM-PLAYERS-OFF", InventoryItem.hideFromPlayersOff());
    value.normal.put("TURN-OFF", InventoryItem.turnOff());
    value.normal.put("LEAVE-SPECTATE", InventoryItem.leaveSpectate());
    value.extra = new LinkedHashMap<>();

    return value;
  }

  @Getter
  public static class InventoryItem {
    @JsonProperty("SLOT")
    private Integer slot;

    @Comment(
        "true = the rematch item becomes the opponent's player head (MATERIAL is then ignored).")
    @JsonProperty("OPPONENT-HEAD")
    private Boolean opponentHead;

    @JsonProperty("ITEM")
    private ItemConfig item;

    @JsonProperty("FLAGS")
    private List<String> flags;

    public boolean isOpponentHead() {
      return opponentHead != null && opponentHead;
    }

    static InventoryItem queue() {
      InventoryItem value = new InventoryItem();
      value.slot = 0;
      value.item = ItemConfig.of("GOLDEN_SWORD").name("<gold>Queue <gray>(Right-Click)");

      return value;
    }

    static InventoryItem unranked() {
      InventoryItem value = new InventoryItem();
      value.slot = 0;
      value.item =
          ItemConfig.of("WOODEN_SWORD")
              .name("<yellow>Unranked Queue <gray>(Right-Click)")
              .durability(100);

      return value;
    }

    static InventoryItem ranked() {
      InventoryItem value = new InventoryItem();
      value.slot = 1;
      value.item = ItemConfig.of("IRON_SWORD").name("<red>Ranked Queue <gray>(Right-Click)");

      return value;
    }

    static InventoryItem cosmetics() {
      InventoryItem value = new InventoryItem();
      value.slot = 4;
      value.item =
          ItemConfig.of("NETHER_STAR")
              .name("<light_purple>Cosmetics Hub <gray>(Right-Click)")
              .lore(
                  "",
                  "<gray>Customize your style with",
                  "<white>Armor Trims<gray>, <white>Shield Layouts<gray>,",
                  "and <white>Death Effects<gray>.",
                  "",
                  "<yellow>Open the cosmetics menu.")
              .flags("HIDE_ENCHANTS")
              .enchantments("UNBREAKING:1");

      return value;
    }

    static InventoryItem enableSpectateMode() {
      InventoryItem value = new InventoryItem();
      value.slot = -1;
      value.item =
          ItemConfig.of("REDSTONE_TORCH")
              .name("<green>Enable <yellow>Spectate Mode <gray>(Right-Click)");

      return value;
    }

    static InventoryItem partyCreate() {
      InventoryItem value = new InventoryItem();
      value.slot = 7;
      value.item = ItemConfig.of("NAME_TAG").name("<light_purple>Create Party <gray>(Right-Click)");

      return value;
    }

    static InventoryItem partyJoin() {
      InventoryItem value = new InventoryItem();
      value.slot = -1;
      value.item =
          ItemConfig.of("LIME_DYE")
              .name("<green>Join Party <gray>(Right-Click)")
              .lore(
                  "",
                  "<gray>Join a <green>public party <gray>without",
                  "<gray>asking for an invite.",
                  "",
                  "<yellow>Open the public party list.");

      return value;
    }

    static InventoryItem settings() {
      InventoryItem value = new InventoryItem();
      value.slot = -1;
      value.item =
          ItemConfig.of("CLOCK")
              .name("<green>Settings <gray>(Right-Click)")
              .amount(1)
              .enchantments("UNBREAKING:1");
      value.flags = List.of("HIDE_POTION_EFFECTS");

      return value;
    }

    static InventoryItem kitEditor() {
      InventoryItem value = new InventoryItem();
      value.slot = 8;
      value.item = ItemConfig.of("BOOK").name("<gold>Kit Editor <gray>(Right-Click)");

      return value;
    }

    static InventoryItem setup() {
      InventoryItem value = new InventoryItem();
      value.slot = -1;
      value.item = ItemConfig.of("REDSTONE").name("<red>Manage Server <gray>(Right-Click)");

      return value;
    }

    static InventoryItem staffMode() {
      InventoryItem value = new InventoryItem();
      value.slot = -1;
      value.item =
          ItemConfig.of("PINK_DYE")
              .name("<green>Enable <light_purple>Staff Mode <gray>(Right-Click)");

      return value;
    }

    static InventoryItem rematch() {
      InventoryItem value = new InventoryItem();
      value.slot = 4;
      value.opponentHead = true;
      value.item =
          ItemConfig.of("BLAZE_POWDER")
              .name("<gold>Rematch <yellow>%opponent% <gray>(Right-Click)");

      return value;
    }

    static InventoryItem statistics() {
      InventoryItem value = new InventoryItem();
      value.slot = -1;
      value.item = ItemConfig.of("EMERALD").name("<dark_green>Statistics <gray>(Right-Click)");

      return value;
    }

    static InventoryItem hostPartyGame() {
      InventoryItem value = new InventoryItem();
      value.slot = 0;
      value.item = ItemConfig.of("GOLDEN_AXE").name("<yellow>Host Party Event <gray>(Right-Click)");

      return value;
    }

    static InventoryItem partyInfo() {
      InventoryItem value = new InventoryItem();
      value.slot = 1;
      value.item = ItemConfig.of("NETHER_STAR").name("<aqua>Party Information <gray>(Right-Click)");

      return value;
    }

    static InventoryItem otherParties() {
      InventoryItem value = new InventoryItem();
      value.slot = 2;
      value.item = ItemConfig.of("PLAYER_HEAD").name("<green>Other Parties <gray>(Right-Click)");

      return value;
    }

    static InventoryItem leaveParty() {
      InventoryItem value = new InventoryItem();
      value.slot = 8;
      value.item = ItemConfig.of("RED_DYE").name("<red>Leave Party <gray>(Right-Click)");

      return value;
    }

    static InventoryItem partySettings() {
      InventoryItem value = new InventoryItem();
      value.slot = 4;
      value.item = ItemConfig.of("CLOCK").name("<gold>Party Settings <gray>(Right-Click)");

      return value;
    }

    static InventoryItem partyKitEditor() {
      InventoryItem value = new InventoryItem();
      value.slot = 7;
      value.item = ItemConfig.of("BOOK").name("<gold>Kit Editor <gray>(Right-Click)");

      return value;
    }

    static InventoryItem leaveMatchQueue() {
      InventoryItem value = new InventoryItem();
      value.slot = 4;
      value.item = ItemConfig.of("RED_DYE").name("<red>Leave Queue <gray>(Right-Click)");

      return value;
    }

    static InventoryItem leaveEventQueue() {
      InventoryItem value = new InventoryItem();
      value.slot = 4;
      value.item =
          ItemConfig.of("RED_DYE")
              .name("<red>Leave <yellow>%event% <red>Event Queue <gray>(Right-Click)");

      return value;
    }

    static InventoryItem menu() {
      InventoryItem value = new InventoryItem();
      value.slot = 5;
      value.item = ItemConfig.of("PAPER").name("<aqua>Spectate Menu <gray>(Right-Click)");

      return value;
    }

    static InventoryItem disable() {
      InventoryItem value = new InventoryItem();
      value.slot = 4;
      value.item =
          ItemConfig.of("REDSTONE_TORCH")
              .name("<red>Disable <yellow>Spectate Mode <gray>(Right-Click)");

      return value;
    }

    static InventoryItem lobbyRandom() {
      InventoryItem value = new InventoryItem();
      value.slot = 3;
      value.item =
          ItemConfig.of("COMPASS").name("<green>Spectate Random Match <gray>(Right-Click)");

      return value;
    }

    static InventoryItem matchRandom() {
      InventoryItem value = new InventoryItem();
      value.slot = 4;
      value.item =
          ItemConfig.of("COMPASS").name("<light_purple>Spectate Random Match <gray>(Right-Click)");

      return value;
    }

    static InventoryItem targets() {
      InventoryItem value = new InventoryItem();
      value.slot = 3;
      value.item = ItemConfig.of("ENDER_EYE").name("<yellow>Fighters <gray>(Right-Click)");

      return value;
    }

    static InventoryItem showSpectators() {
      InventoryItem value = new InventoryItem();
      value.slot = 0;
      value.item = ItemConfig.of("GRAY_DYE").name("<yellow>Show Spectators <gray>(Right-Click)");

      return value;
    }

    static InventoryItem hideSpectators() {
      InventoryItem value = new InventoryItem();
      value.slot = 0;
      value.item = ItemConfig.of("LIME_DYE").name("<yellow>Hide Spectators <gray>(Right-Click)");

      return value;
    }

    static InventoryItem leave() {
      InventoryItem value = new InventoryItem();
      value.slot = 8;
      value.item = ItemConfig.of("RED_DYE").name("<red>Stop Spectating <gray>(Right-Click)");

      return value;
    }

    static InventoryItem randomGameSpectate() {
      InventoryItem value = new InventoryItem();
      value.slot = 0;
      value.item =
          ItemConfig.of("COMPASS").name("<light_purple>Spectate Random Match <gray>(Right-Click)");

      return value;
    }

    static InventoryItem playerInventory() {
      InventoryItem value = new InventoryItem();
      value.slot = 1;
      value.item = ItemConfig.of("STICK").name("<gold>View Player's Inventory <gray>(Right-Click)");

      return value;
    }

    static InventoryItem hideFromPlayersOn() {
      InventoryItem value = new InventoryItem();
      value.slot = 8;
      value.item =
          ItemConfig.of("GRAY_DYE").name("<yellow>Show Myself To Players <gray>(Right-Click)");

      return value;
    }

    static InventoryItem hideFromPlayersOff() {
      InventoryItem value = new InventoryItem();
      value.slot = 8;
      value.item =
          ItemConfig.of("LIME_DYE").name("<yellow>Hide Myself From Players <gray>(Right-Click)");

      return value;
    }

    static InventoryItem turnOff() {
      InventoryItem value = new InventoryItem();
      value.slot = 4;
      value.item = ItemConfig.of("RED_DYE").name("<red>Turn Off StaffMode <gray>(Right-Click)");

      return value;
    }

    static InventoryItem leaveSpectate() {
      InventoryItem value = new InventoryItem();
      value.slot = 7;
      value.item = ItemConfig.of("RED_DYE").name("<red>Leave Spectation <gray>(Right-Click)");

      return value;
    }
  }

  @Getter
  public static class ExtraItem extends InventoryItem {
    @JsonProperty("COMMAND")
    private String command;
  }
}
