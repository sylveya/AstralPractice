package dev.lokspel.practice.manager.profile.group;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.group.GroupConfig;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.StringUtil;
import dev.lokspel.practice.util.format.NameFormatUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

@Getter
public class GroupManager {

  private static final int DEFAULT_PARTY_MEMBER_LIMIT =
      PracticeConfig.settings().getParty().getSettings().getMaxPartyMembers().getDefaultValue();

  private static GroupManager instance;

  public static GroupManager getInstance() {
    if (instance == null) {
      instance = new GroupManager();
    }
    return instance;
  }

  private List<Group> groups = new ArrayList<>();

  private GroupManager() {
    loadGroups();
  }

  public void loadGroups() {
    GroupConfig groupConfig = PracticeConfig.groups();
    Map<String, List<String>> groupExtensions = PracticeConfig.sidebar().getGroupExtensions();
    List<Group> loadedGroups = new ArrayList<>();

    for (Map.Entry<String, GroupConfig.Group> entry :
        Objects.requireNonNull(groupConfig.getGroups()).entrySet()) {
      String groupName = entry.getKey();
      GroupConfig.Group groupSection = entry.getValue();
      GroupConfig.Group.LobbyNametag nametag =
          groupSection != null ? groupSection.getLobbyNametag() : null;

      String chatFormat = groupSection != null ? groupSection.getChatFormat() : null;

      List<Component> sidebarExtension = new ArrayList<>();
      List<String> sidebarExtensionRaw = new ArrayList<>();
      List<String> extensionLines = groupExtensions != null ? groupExtensions.get(groupName) : null;
      if (extensionLines != null) {
        for (String line : extensionLines) {
          sidebarExtension.add(
              AstralPractice.getMiniMessage().deserialize(StringUtil.legacyToMiniMessage(line)));
          sidebarExtensionRaw.add(line);
        }
      }

      String prefixTemplate = nametag != null ? nametag.getPrefix() : null;
      String configuredName = nametag != null ? nametag.getName() : null;
      String nameTemplate = configuredName != null ? configuredName : "<gray>%player%";
      String suffixTemplate = nametag != null ? nametag.getSuffix() : null;

      Component nameFormat =
          configuredName != null
              ? NameFormatUtil.parseConfiguredComponent(nameTemplate)
              : Component.text("%player%", NamedTextColor.GRAY);

      Integer partyMemberLimit = groupSection != null ? groupSection.getPartyMemberLimit() : null;

      Group group =
          new Group(
              groupName,
              groupSection != null ? groupSection.getName() : null,
              groupSection != null ? intOrZero(groupSection.getWeight()) : 0,
              groupSection != null ? intOrZero(groupSection.getUnrankedPerDay()) : 0,
              groupSection != null ? intOrZero(groupSection.getRankedPerDay()) : 0,
              groupSection != null ? intOrZero(groupSection.getEventStartPerDay()) : 0,
              groupSection != null ? intOrZero(groupSection.getPartyBroadcastPerDay()) : 0,
              partyMemberLimit != null ? partyMemberLimit : DEFAULT_PARTY_MEMBER_LIMIT,
              groupSection != null ? intOrZero(groupSection.getCustomKit()) : 0,
              groupSection != null ? intOrZero(groupSection.getModifiableKitPerLadder()) : 0,
              prefixTemplate,
              NameFormatUtil.parseConfiguredComponent(prefixTemplate),
              nameTemplate,
              nameFormat,
              suffixTemplate,
              NameFormatUtil.parseConfiguredComponent(suffixTemplate),
              nametag != null ? intOrZero(nametag.getSortPriority()) : 0,
              chatFormat,
              sidebarExtension,
              sidebarExtensionRaw);

      loadedGroups.add(group);
    }

    loadedGroups.sort(Comparator.comparing(Group::getWeight));
    groups = loadedGroups;
  }

  private static int intOrZero(Integer value) {
    return value != null ? value : 0;
  }

  public Group getGroup(String name) {
    for (Group group : groups) {
      if (group.getName().equalsIgnoreCase(name)) {
        return group;
      }
    }
    return null;
  }

  public Group getGroup(Player player) {
    Profile profile = ProfileManager.getInstance().getProfile(player);
    if (profile.getPlayer().isOp()) {
      try {
        profile.setGroup(groups.getLast());
      } catch (Exception e) {
        Common.sendConsoleMmMessage(
            "<red>Failed to set group for "
                + profile.getPlayer().getName()
                + "! Error: "
                + e.getMessage());
      }
      return profile.getGroup();
    }

    Group currentGroup = profile.getGroup();

    // If player is not OP and has a current group, validate they still have permission for it
    if (currentGroup != null
        && currentGroup.getPermission() != null
        && !player.hasPermission(currentGroup.getPermission())) {
      currentGroup = null;
    }

    // Find the highest weight group the player has permission for
    for (Group group : groups) {
      if (currentGroup != null && currentGroup.getWeight() > group.getWeight()) {
        continue;
      }
      if (group.getPermission() == null) {
        continue;
      }

      if (player.hasPermission(group.getPermission())) {
        currentGroup = group;
      }
    }

    // If player has no permissions for any group, assign them to the lowest weighted group
    // (default)
    if (currentGroup == null && !groups.isEmpty()) {
      currentGroup = groups.getFirst(); // First group in sorted list = lowest weight
    }

    return currentGroup;
  }
}
