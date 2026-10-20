package dev.lokspel.practice.manager.gui.guis.leaderboard;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.division.Division;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.leaderboard.Leaderboard;
import dev.lokspel.practice.manager.leaderboard.LeaderboardManager;
import dev.lokspel.practice.manager.leaderboard.types.LbMainType;
import dev.lokspel.practice.manager.leaderboard.types.LbSecondaryType;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.StringUtil;
import dev.lokspel.practice.util.items.ItemCreateUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public enum LbGuiUtil {
  ;

  private static Component parseColor(String raw) {
    if (raw == null || raw.isEmpty()) {
      return Component.empty();
    }
    return AstralPractice.getMiniMessage()
        .deserialize(StringUtil.legacyToMiniMessage(raw))
        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
  }

  private static List<Component> parseColorLore(List<String> lore) {
    return lore.stream().map(LbGuiUtil::parseColor).collect(Collectors.toList());
  }

  // if it says not used don't listen to it, buggy
  public static ItemStack createProfileStatItem(Profile profile, Player opener) {
    String playerName = profile.getPlayer().getName();
    if (playerName == null) {
      playerName = "Unknown";
    }

    ItemStack itemStack = ItemCreateUtil.getPlayerHead(profile.getPlayer());
    ItemMeta itemMeta = itemStack.getItemMeta();

    String displayName;
    List<String> lore = new ArrayList<>();

    if (opener.equals(profile.getPlayer())) {
      for (String line :
          PracticeConfig.guis()
              .getGuis()
              .getStatistics()
              .getSelector()
              .getIcons()
              .getOwnPlayerStats()
              .getLore()) {
        lore.add(line.replace("%player%", playerName));
      }
      displayName =
          PracticeConfig.guis()
              .getGuis()
              .getStatistics()
              .getSelector()
              .getIcons()
              .getOwnPlayerStats()
              .getName()
              .replace("%player%", playerName);
    } else {
      for (String line :
          PracticeConfig.guis()
              .getGuis()
              .getStatistics()
              .getSelector()
              .getIcons()
              .getPlayerStats()
              .getLore()) {
        lore.add(line.replace("%target%", playerName));
      }
      displayName =
          PracticeConfig.guis()
              .getGuis()
              .getStatistics()
              .getSelector()
              .getIcons()
              .getPlayerStats()
              .getName()
              .replace("%target%", playerName);
    }

    itemMeta.displayName(parseColor(displayName));
    itemMeta.lore(parseColorLore(lore));
    ItemCreateUtil.hideItemFlags(itemMeta);
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static GuiItem createLadderStatItem(Profile profile, NormalLadder ladder) {
    GuiItem guiItem =
        switch (ladder.getWeightClass()) {
          case UNRANKED ->
              BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getStatistics()
                      .getPlayerStatistics()
                      .getIcons()
                      .getUnrankedLadderStats());
          case RANKED ->
              BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getStatistics()
                      .getPlayerStatistics()
                      .getIcons()
                      .getRankedLadderStats());
          case UNRANKED_AND_RANKED ->
              BackendUtil.getGuiItem(
                  PracticeConfig.guis()
                      .getGuis()
                      .getStatistics()
                      .getPlayerStatistics()
                      .getIcons()
                      .getUnrankedRankedStats());
        };

    switch (ladder.getWeightClass()) {
      case RANKED:
      case UNRANKED_AND_RANKED:
        guiItem.replace("%elo%", String.valueOf(profile.getStats().getLadderStat(ladder).getElo()));
        break;

      default:
        break;
    }

    String divisionName =
        profile.getStats().getDivision() != null
            ? Common.mmToNormal(profile.getStats().getDivision().getFullName())
            : "<red>N/A";
    String divisionShort =
        profile.getStats().getDivision() != null
            ? Common.mmToNormal(profile.getStats().getDivision().getShortName())
            : "<red>N/A";

    guiItem
        .replace("%ladder%", ladder.getDisplayName())
        .replace(
            "%unranked_wins%",
            String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedWins()))
        .replace(
            "%unranked_losses%",
            String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedLosses()))
        .replace(
            "%unranked_w/l_ratio%",
            String.valueOf(profile.getStats().getLadderRatio(ladder, false)))
        .replace(
            "%ranked_wins%",
            String.valueOf(profile.getStats().getLadderStat(ladder).getRankedWins()))
        .replace(
            "%ranked_losses%",
            String.valueOf(profile.getStats().getLadderStat(ladder).getRankedLosses()))
        .replace(
            "%ranked_w/l_ratio%", String.valueOf(profile.getStats().getLadderRatio(ladder, true)))
        .replace("%overall_w/l_ratio%", String.valueOf(profile.getStats().getOverallRatio(ladder)))
        .replace("%division%", divisionName)
        .replace("%division_short%", divisionShort);

    if (ladder.getIcon() != null) {
      guiItem.setBaseItem(ladder.getIcon());
    }
    return guiItem;
  }

  public static ItemStack createProfileAllStatItem(Profile profile) {
    ItemStack itemStack = ItemCreateUtil.getPlayerHead(profile.getPlayer());
    ItemMeta itemMeta = itemStack.getItemMeta();
    List<String> lore = new ArrayList<>();

    String divisionName =
        profile.getStats().getDivision() != null
            ? Common.mmToNormal(profile.getStats().getDivision().getFullName())
            : "<red>N/A";
    String divisionShort =
        profile.getStats().getDivision() != null
            ? Common.mmToNormal(profile.getStats().getDivision().getShortName())
            : "<red>N/A";

    for (String line :
        PracticeConfig.guis()
            .getGuis()
            .getStatistics()
            .getPlayerStatistics()
            .getIcons()
            .getAllStat()
            .getLore()) {
      lore.add(
          line.replace("%unranked_wins%", String.valueOf(profile.getStats().getWins(false)))
              .replace("%unranked_losses%", String.valueOf(profile.getStats().getLosses(false)))
              .replace("%unranked_w/l_ratio%", String.valueOf(profile.getStats().getRatio(false)))
              .replace("%ranked_wins%", String.valueOf(profile.getStats().getWins(true)))
              .replace("%ranked_losses%", String.valueOf(profile.getStats().getLosses(true)))
              .replace("%ranked_w/l_ratio%", String.valueOf(profile.getStats().getRatio(true)))
              .replace("%global_wins%", String.valueOf(profile.getStats().getGlobalWins()))
              .replace("%global_losses%", String.valueOf(profile.getStats().getGlobalLosses()))
              .replace("%w/l_ratio%", String.valueOf(profile.getStats().getGlobalRatio()))
              .replace("%global_elo%", String.valueOf(profile.getStats().getGlobalElo()))
              .replace("%division%", divisionName)
              .replace("%division_short%", divisionShort));
    }

    String name =
        PracticeConfig.guis()
            .getGuis()
            .getStatistics()
            .getPlayerStatistics()
            .getIcons()
            .getAllStat()
            .getName()
            .replace("%player%", Objects.requireNonNull(profile.getPlayer().getName()));
    itemMeta.displayName(parseColor(name));
    itemMeta.lore(parseColorLore(lore));
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  public static ItemStack createEloLbItem(NormalLadder ladder) {
    List<String> lore = new ArrayList<>();
    Leaderboard leaderboard =
        LeaderboardManager.getInstance().searchLb(LbMainType.LADDER, LbSecondaryType.ELO, ladder);
    int showPlayers = 10;

    if (leaderboard == null) {
      lore.addAll(
          PracticeConfig.guis()
              .getGuis()
              .getStatistics()
              .getEloLeaderboard()
              .getIcons()
              .getLadderLeaderboard()
              .getLore()
              .getNoLeaderboard());
    } else {
      List<OfflinePlayer> topPlayers = new ArrayList<>();
      Map<OfflinePlayer, Integer> list = leaderboard.getList();
      for (OfflinePlayer player : list.keySet()) {
        if (player.getName() != null && ProfileManager.getInstance().getProfile(player) != null) {
          topPlayers.add(player);
        }
        if (topPlayers.size() >= showPlayers) {
          break;
        }
      }

      List<String> topStrings = new ArrayList<>();
      for (int i = 1; i <= showPlayers; i++) {
        if (topPlayers.size() > i - 1) {
          OfflinePlayer target = topPlayers.get(i - 1);
          Profile targetProfile = ProfileManager.getInstance().getProfile(target);
          Division division = targetProfile.getStats().getDivision();
          int stat = list.get(target);
          topStrings.add(
              PracticeConfig.guis()
                  .getGuis()
                  .getStatistics()
                  .getEloLeaderboard()
                  .getIcons()
                  .getLadderLeaderboard()
                  .getLore()
                  .getFormat()
                  .replace("%number%", String.valueOf(i))
                  .replace("%player%", target.getName())
                  .replace("%ladder_elo%", String.valueOf(stat))
                  .replace(
                      "%division%",
                      division != null ? Common.mmToNormal(division.getFullName()) : "")
                  .replace(
                      "%division_short%",
                      division != null ? Common.mmToNormal(division.getShortName()) : ""));
        } else {
          topStrings.add(
              PracticeConfig.guis()
                  .getGuis()
                  .getStatistics()
                  .getEloLeaderboard()
                  .getIcons()
                  .getLadderLeaderboard()
                  .getLore()
                  .getFormatNull()
                  .replace("%number%", String.valueOf(i)));
        }
      }

      for (String line :
          PracticeConfig.guis()
              .getGuis()
              .getStatistics()
              .getEloLeaderboard()
              .getIcons()
              .getLadderLeaderboard()
              .getLore()
              .getLeaderboard()) {
        if (line.contains("%top%")) {
          lore.addAll(topStrings);
        } else {
          lore.add(line);
        }
      }
    }

    String name =
        PracticeConfig.guis()
            .getGuis()
            .getStatistics()
            .getEloLeaderboard()
            .getIcons()
            .getLadderLeaderboard()
            .getName()
            .replace("%ladder%", ladder.getDisplayName())
            .replace("%number%", String.valueOf(showPlayers));
    return buildItem(ladder.getIcon(), name, lore);
  }

  public static ItemStack createGlobalEloLb() {
    List<String> lore = new ArrayList<>();
    Leaderboard leaderboard =
        LeaderboardManager.getInstance().searchLb(LbMainType.GLOBAL, LbSecondaryType.ELO, null);
    int showPlayers = 10;

    if (leaderboard == null) {
      lore.addAll(
          PracticeConfig.guis()
              .getGuis()
              .getStatistics()
              .getEloLeaderboard()
              .getIcons()
              .getGlobalLeaderboard()
              .getLore()
              .getNoLeaderboard());
    } else {
      List<OfflinePlayer> topPlayers = new ArrayList<>();
      Map<OfflinePlayer, Integer> list = leaderboard.getList();
      for (OfflinePlayer player : list.keySet()) {
        if (player.getName() != null && ProfileManager.getInstance().getProfile(player) != null) {
          topPlayers.add(player);
        }
        if (topPlayers.size() >= showPlayers) {
          break;
        }
      }

      List<String> topStrings = new ArrayList<>();
      for (int i = 1; i <= showPlayers; i++) {
        if (topPlayers.size() > i - 1) {
          OfflinePlayer target = topPlayers.get(i - 1);
          Profile targetProfile = ProfileManager.getInstance().getProfile(target);
          Division division = targetProfile.getStats().getDivision();
          int stat = list.get(target);
          topStrings.add(
              PracticeConfig.guis()
                  .getGuis()
                  .getStatistics()
                  .getEloLeaderboard()
                  .getIcons()
                  .getGlobalLeaderboard()
                  .getLore()
                  .getFormat()
                  .replace("%number%", String.valueOf(i))
                  .replace(
                      "%division%",
                      division != null ? Common.mmToNormal(division.getFullName()) : "")
                  .replace(
                      "%division_short%",
                      division != null ? Common.mmToNormal(division.getShortName()) : "")
                  .replace("%player%", target.getName())
                  .replace("%global_elo%", String.valueOf(stat)));
        } else {
          topStrings.add(
              PracticeConfig.guis()
                  .getGuis()
                  .getStatistics()
                  .getEloLeaderboard()
                  .getIcons()
                  .getGlobalLeaderboard()
                  .getLore()
                  .getFormatNull()
                  .replace("%number%", String.valueOf(i)));
        }
      }

      for (String line :
          PracticeConfig.guis()
              .getGuis()
              .getStatistics()
              .getEloLeaderboard()
              .getIcons()
              .getGlobalLeaderboard()
              .getLore()
              .getLeaderboard()) {
        if (line.contains("%top%")) {
          lore.addAll(topStrings);
        } else {
          lore.add(line);
        }
      }
    }

    String name =
        PracticeConfig.guis()
            .getGuis()
            .getStatistics()
            .getEloLeaderboard()
            .getIcons()
            .getGlobalLeaderboard()
            .getName()
            .replace("%number%", String.valueOf(showPlayers));
    Material mat =
        Material.valueOf(
            PracticeConfig.guis()
                .getGuis()
                .getStatistics()
                .getEloLeaderboard()
                .getIcons()
                .getGlobalLeaderboard()
                .getMaterial());
    return buildItem(mat, name, lore);
  }

  public static ItemStack createWinLbItem(NormalLadder ladder) {
    List<String> lore = new ArrayList<>();
    Leaderboard leaderboard =
        LeaderboardManager.getInstance().searchLb(LbMainType.LADDER, LbSecondaryType.WIN, ladder);
    int showPlayers = 10;

    if (leaderboard == null) {
      lore.addAll(
          PracticeConfig.guis()
              .getGuis()
              .getStatistics()
              .getWinLeaderboard()
              .getIcons()
              .getLadderLeaderboard()
              .getLore()
              .getNoLeaderboard());
    } else {
      List<OfflinePlayer> topPlayers = new ArrayList<>();
      Map<OfflinePlayer, Integer> list = leaderboard.getList();
      for (OfflinePlayer player : list.keySet()) {
        if (player.getName() != null && ProfileManager.getInstance().getProfile(player) != null) {
          topPlayers.add(player);
        }
        if (topPlayers.size() >= showPlayers) {
          break;
        }
      }

      List<String> topStrings = new ArrayList<>();
      for (int i = 1; i <= showPlayers; i++) {
        if (topPlayers.size() > i - 1) {
          OfflinePlayer target = topPlayers.get(i - 1);
          Profile targetProfile = ProfileManager.getInstance().getProfile(target);
          Division division = targetProfile.getStats().getDivision();
          int stat = list.get(target);
          topStrings.add(
              PracticeConfig.guis()
                  .getGuis()
                  .getStatistics()
                  .getWinLeaderboard()
                  .getIcons()
                  .getLadderLeaderboard()
                  .getLore()
                  .getFormat()
                  .replace("%number%", String.valueOf(i))
                  .replace("%player%", Objects.requireNonNull(target.getName()))
                  .replace("%ladder_win%", String.valueOf(stat))
                  .replace(
                      "%division%",
                      division != null ? Common.mmToNormal(division.getFullName()) : "")
                  .replace(
                      "%division_short%",
                      division != null ? Common.mmToNormal(division.getShortName()) : ""));
        } else {
          topStrings.add(
              PracticeConfig.guis()
                  .getGuis()
                  .getStatistics()
                  .getWinLeaderboard()
                  .getIcons()
                  .getLadderLeaderboard()
                  .getLore()
                  .getFormatNull()
                  .replace("%number%", String.valueOf(i)));
        }
      }

      for (String line :
          PracticeConfig.guis()
              .getGuis()
              .getStatistics()
              .getWinLeaderboard()
              .getIcons()
              .getLadderLeaderboard()
              .getLore()
              .getLeaderboard()) {
        if (line.contains("%top%")) {
          lore.addAll(topStrings);
        } else {
          lore.add(line);
        }
      }
    }

    String name =
        PracticeConfig.guis()
            .getGuis()
            .getStatistics()
            .getWinLeaderboard()
            .getIcons()
            .getLadderLeaderboard()
            .getName()
            .replace("%ladder%", ladder.getDisplayName())
            .replace("%number%", String.valueOf(showPlayers));
    return buildItem(ladder.getIcon(), name, lore);
  }

  public static ItemStack createGlobalWinLb() {
    List<String> lore = new ArrayList<>();
    Leaderboard leaderboard =
        LeaderboardManager.getInstance().searchLb(LbMainType.GLOBAL, LbSecondaryType.WIN, null);
    int showPlayers = 10;

    if (leaderboard == null) {
      lore.addAll(
          PracticeConfig.guis()
              .getGuis()
              .getStatistics()
              .getWinLeaderboard()
              .getIcons()
              .getGlobalLeaderboard()
              .getLore()
              .getNoLeaderboard());
    } else {
      List<OfflinePlayer> topPlayers = new ArrayList<>();
      Map<OfflinePlayer, Integer> list = leaderboard.getList();
      for (OfflinePlayer player : list.keySet()) {
        if (player.getName() != null && ProfileManager.getInstance().getProfile(player) != null) {
          topPlayers.add(player);
        }
        if (topPlayers.size() >= showPlayers) {
          break;
        }
      }

      List<String> topStrings = new ArrayList<>();
      for (int i = 1; i <= showPlayers; i++) {
        if (topPlayers.size() > i - 1) {
          OfflinePlayer target = topPlayers.get(i - 1);
          Profile targetProfile = ProfileManager.getInstance().getProfile(target);
          Division division = targetProfile.getStats().getDivision();
          int stat = list.get(target);
          topStrings.add(
              PracticeConfig.guis()
                  .getGuis()
                  .getStatistics()
                  .getWinLeaderboard()
                  .getIcons()
                  .getGlobalLeaderboard()
                  .getLore()
                  .getFormat()
                  .replace("%number%", String.valueOf(i))
                  .replace(
                      "%division%",
                      division != null ? Common.mmToNormal(division.getFullName()) : "")
                  .replace(
                      "%division_short%",
                      division != null ? Common.mmToNormal(division.getShortName()) : "")
                  .replace("%player%", target.getName())
                  .replace("%global_win%", String.valueOf(stat)));
        } else {
          topStrings.add(
              PracticeConfig.guis()
                  .getGuis()
                  .getStatistics()
                  .getWinLeaderboard()
                  .getIcons()
                  .getGlobalLeaderboard()
                  .getLore()
                  .getFormatNull()
                  .replace("%number%", String.valueOf(i)));
        }
      }

      for (String line :
          PracticeConfig.guis()
              .getGuis()
              .getStatistics()
              .getEloLeaderboard()
              .getIcons()
              .getGlobalLeaderboard()
              .getLore()
              .getLeaderboard()) {
        if (line.contains("%top%")) {
          lore.addAll(topStrings);
        } else {
          lore.add(line);
        }
      }
    }

    String name =
        PracticeConfig.guis()
            .getGuis()
            .getStatistics()
            .getWinLeaderboard()
            .getIcons()
            .getGlobalLeaderboard()
            .getName()
            .replace("%number%", String.valueOf(showPlayers));
    Material mat =
        Material.valueOf(
            PracticeConfig.guis()
                .getGuis()
                .getStatistics()
                .getWinLeaderboard()
                .getIcons()
                .getGlobalLeaderboard()
                .getMaterial());
    return buildItem(mat, name, lore);
  }

  public static ItemStack getCacheInfoItem() {
    List<String> lore = new ArrayList<>();
    lore.add("<dark_gray><st>------------------------");
    lore.add("<gray>This leaderboard automatically");
    lore.add("<gray>updates every <yellow>5 minutes<gray>.");
    lore.add("");
    lore.add("<gray>Last update: <green>Recently");
    lore.add("<gray>Next update: <yellow>Within 5 minutes");
    lore.add("<dark_gray><st>------------------------");
    return buildItem(Material.CLOCK, "<yellow>Auto-Update Info", lore);
  }

  // Item building helpers
  /** Builds an ItemStack from an existing icon with a parsed name and lore. */
  private static ItemStack buildItem(ItemStack icon, String name, List<String> lore) {
    ItemStack item = icon != null ? icon.clone() : new ItemStack(Material.BARRIER);
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.displayName(parseColor(name));
      meta.lore(parseColorLore(lore));
      ItemCreateUtil.hideItemFlags(meta);
      item.setItemMeta(meta);
    }
    return item;
  }

  /** Builds an ItemStack from a material with a parsed name and lore. */
  private static ItemStack buildItem(Material material, String name, List<String> lore) {
    ItemStack item = new ItemStack(material);
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.displayName(parseColor(name));
      meta.lore(parseColorLore(lore));
      ItemCreateUtil.hideItemFlags(meta);
      item.setItemMeta(meta);
    }
    return item;
  }
}
