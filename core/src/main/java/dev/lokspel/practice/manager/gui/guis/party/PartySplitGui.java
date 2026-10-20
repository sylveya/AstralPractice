package dev.lokspel.practice.manager.gui.guis.party;

import dev.lokspel.practice.config.ItemConfig;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.arena.arenas.Arena;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.match.Match;
import dev.lokspel.practice.manager.fight.match.enums.TeamEnum;
import dev.lokspel.practice.manager.fight.match.type.duel.Duel;
import dev.lokspel.practice.manager.fight.match.type.playersvsplayers.partysplit.PartySplit;
import dev.lokspel.practice.manager.gui.Gui;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.gui.GuiType;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.util.LadderUtil;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.InventoryUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PartySplitGui extends Gui {

  private static ItemStack fillerItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getParty().getPartySplit().getIcons().getFillerItem())
        .get();
  }

  private static final int BACK_SLOT = 45;
  private static final int RANDOM_SPLIT_SLOT = 49;
  private static final int START_MATCH_SLOT = 50;

  private final Party party;
  private final Ladder ladder;
  private final Arena arena;
  private final int rounds;
  private final Gui backTo;
  private final Map<Player, TeamEnum> assignments = new LinkedHashMap<>();
  private final Set<Player> spectators = new LinkedHashSet<>();
  private final Map<Integer, Player> headSlots = new LinkedHashMap<>();

  public PartySplitGui(Party party, Ladder ladder, Arena arena, int rounds, Gui backTo) {
    super(GuiType.Party_Split);
    this.party = party;
    this.ladder = ladder;
    this.arena = arena;
    this.rounds = rounds;
    this.backTo = backTo;

    this.gui.put(
        1,
        InventoryUtil.createInventory(
            PracticeConfig.guis().getGuis().getParty().getPartySplit().getTitle(), 6));

    build();
  }

  @Override
  public void build() {
    for (int i = 0; i < gui.get(1).getSize(); i++) {
      gui.get(1).setItem(i, fillerItem());
    }

    update();
  }

  @Override
  public void update() {
    Inventory inventory = gui.get(1);

    for (int i = 0; i < 45; i++) {
      inventory.setItem(i, fillerItem());
    }

    inventory.setItem(
        BACK_SLOT,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getParty()
                    .getPartySplit()
                    .getIcons()
                    .getBackToSelector())
            .get());
    inventory.setItem(
        RANDOM_SPLIT_SLOT,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getParty()
                    .getPartySplit()
                    .getIcons()
                    .getRandomSplit())
            .get());
    inventory.setItem(
        START_MATCH_SLOT,
        BackendUtil.getGuiItem(
                PracticeConfig.guis()
                    .getGuis()
                    .getParty()
                    .getPartySplit()
                    .getIcons()
                    .getStartMatch())
            .get());

    headSlots.clear();
    int slot = 0;
    for (Player member : party.getMembers()) {
      if (slot >= 45) {
        break;
      }

      headSlots.put(slot, member);
      inventory.setItem(slot, getPlayerItem(member));
      slot++;
    }

    updatePlayers();
  }

  private ItemStack getPlayerItem(Player member) {
    Map<String, ItemConfig> playerItems =
        PracticeConfig.guis().getGuis().getParty().getPartySplit().getIcons().getPlayerItem();
    ItemConfig playerItem = playerItems != null ? playerItems.get(getPlayerItemIcon(member)) : null;
    GuiItem configItem = playerItem != null ? BackendUtil.getGuiItem(playerItem) : new GuiItem();

    GuiItem headItem = new GuiItem(ItemCreateUtil.getPlayerHead(member));
    headItem.setName(configItem.getName());
    headItem.setLore(configItem.getLore());
    return headItem.replace("%player%", member.getName()).get();
  }

  private String getPlayerItemIcon(Player member) {
    if (spectators.contains(member)) {
      return "SPECTATOR";
    }

    TeamEnum team = assignments.get(member);
    if (team == null) {
      return "UNASSIGNED";
    }

    return team == TeamEnum.TEAM1 ? "BLUE" : "RED";
  }

  @Override
  public void handleClickEvent(InventoryClickEvent e) {
    Player player = (Player) e.getWhoClicked();
    Party clickParty = PartyManager.getInstance().getParty(player);
    int slot = e.getRawSlot();
    e.setCancelled(true);

    if (clickParty == null || !clickParty.getLeader().equals(player)) {
      return;
    }
    if (slot >= gui.get(1).getSize()) {
      return;
    }

    if (slot == BACK_SLOT) {
      backTo.open(player);
    } else if (slot == RANDOM_SPLIT_SLOT) {
      randomSplit();
      update();
    } else if (slot == START_MATCH_SLOT) {
      startMatch(player);
    } else if (headSlots.containsKey(slot)) {
      Player member = headSlots.get(slot);
      if (e.isShiftClick() && e.isLeftClick()) {
        setSpectator(member);
      } else if (e.isShiftClick() && e.isRightClick()) {
        unassign(member);
      } else if (e.isLeftClick()) {
        setTeam(member, TeamEnum.TEAM1);
      } else if (e.isRightClick()) {
        setTeam(member, TeamEnum.TEAM2);
      }
      update();
    }
  }

  private void setTeam(Player member, TeamEnum team) {
    spectators.remove(member);
    assignments.put(member, team);
  }

  private void setSpectator(Player member) {
    assignments.remove(member);
    spectators.add(member);
  }

  private void unassign(Player member) {
    assignments.remove(member);
    spectators.remove(member);
  }

  private void randomSplit() {
    assignments.clear();

    List<Player> fighters = new ArrayList<>(party.getMembers());
    fighters.removeIf(spectators::contains);

    Collections.shuffle(fighters);

    List<Player> team1 = fighters.subList(0, fighters.size() / 2);
    List<Player> team2 = fighters.subList(fighters.size() / 2, fighters.size());
    for (Player member : team1) {
      assignments.put(member, TeamEnum.TEAM1);
    }
    for (Player member : team2) {
      assignments.put(member, TeamEnum.TEAM2);
    }
  }

  private void startMatch(Player player) {
    Map<TeamEnum, List<Player>> teamAssignments = null;

    if (party.getMembers().size() > 2) {
      assignUnassignedMembers();

      teamAssignments = buildTeamAssignments();
      if (teamAssignments.get(TeamEnum.TEAM1).isEmpty()
          || teamAssignments.get(TeamEnum.TEAM2).isEmpty()) {
        Common.sendMmMessage(
            player, PracticeConfig.language().getParty().getPartySplit().getBothTeamsNeeded());
        return;
      }
    }

    player.closeInventory();

    Arena selectedArena = arena;
    if (selectedArena == null) {
      selectedArena = LadderUtil.getAvailableArena(ladder);
    }
    if (selectedArena == null || selectedArena.getAvailableArena() == null) {
      Common.sendMmMessage(player, PracticeConfig.language().getParty().getNoAvailableArena());
      return;
    }

    Match match;
    if (party.getMembers().size() == 2) {
      match = new Duel(ladder, selectedArena, new ArrayList<>(party.getMembers()), false, rounds);
    } else {
      match =
          new PartySplit(
              ladder, selectedArena, party, rounds, teamAssignments, new ArrayList<>(spectators));
    }

    party.setMatch(match);
    match.startMatch();
  }

  private void assignUnassignedMembers() {
    for (Player member : party.getMembers()) {
      if (spectators.contains(member) || assignments.containsKey(member)) {
        continue;
      }

      int team1Count = Collections.frequency(assignments.values(), TeamEnum.TEAM1);
      int team2Count = Collections.frequency(assignments.values(), TeamEnum.TEAM2);

      assignments.put(member, team1Count <= team2Count ? TeamEnum.TEAM1 : TeamEnum.TEAM2);
    }
  }

  private Map<TeamEnum, List<Player>> buildTeamAssignments() {
    Map<TeamEnum, List<Player>> teamAssignments = new EnumMap<>(TeamEnum.class);
    teamAssignments.put(TeamEnum.TEAM1, new ArrayList<>());
    teamAssignments.put(TeamEnum.TEAM2, new ArrayList<>());

    for (Map.Entry<Player, TeamEnum> entry : assignments.entrySet()) {
      teamAssignments.get(entry.getValue()).add(entry.getKey());
    }

    return teamAssignments;
  }
}
