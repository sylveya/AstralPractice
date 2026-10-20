package dev.lokspel.practice.manager.profile;

import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.data.DataFiles;
import dev.lokspel.practice.manager.fight.match.util.CustomKit;
import dev.lokspel.practice.manager.ladder.LadderManager;
import dev.lokspel.practice.manager.ladder.abstraction.Ladder;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.ladder.abstraction.playercustom.CustomLadderData;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsData;
import dev.lokspel.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.lokspel.practice.manager.profile.cosmetics.armortrim.ArmorSlot;
import dev.lokspel.practice.manager.profile.cosmetics.armortrim.ArmorTrimTier;
import dev.lokspel.practice.manager.profile.cosmetics.deatheffect.DeathEffect;
import dev.lokspel.practice.manager.profile.cosmetics.shield.ShieldLayout;
import dev.lokspel.practice.manager.profile.enums.ProfilePrefixVisibility;
import dev.lokspel.practice.manager.profile.enums.ProfileWorldTime;
import dev.lokspel.practice.manager.profile.group.Group;
import dev.lokspel.practice.manager.profile.group.GroupManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.ItemSerializationUtil;
import dev.lokspel.practice.util.NamespacedKeyUtil;
import dev.lokspel.practice.util.format.NameFormatUtil;
import io.papermc.paper.registry.RegistryKey;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

/**
 * The file of a profile, {@code profiles/<uuid>.yml}. The model is read once when the profile is
 * created and kept: every save updates the parts this plugin manages and writes the whole model, so
 * sections of ladders and tiers that no longer exist stay in the file as before.
 *
 * <p>The model is changed and written while holding the profile's lock, as saves run both on the
 * main thread and asynchronously.
 */
public class ProfileFile {

  private static final String FOLDER = "profiles/";

  private final Profile profile;
  private final String fileName;
  @Getter private final ProfileData profileData;

  public ProfileFile(Profile profile) {
    this.profile = profile;
    this.fileName = FOLDER + profile.getUuid().toString().toLowerCase() + ".yml";
    this.profileData = DataFiles.readOrCreate(fileName, ProfileData.class, ProfileData::new);

    saveFile();
  }

  /** Writes the model to the file. */
  public void saveFile() {
    synchronized (profile) {
      DataFiles.write(fileName, profileData);
    }
  }

  /**
   * Deletes the file.
   *
   * @return whether the file existed and was deleted
   */
  public boolean delete() {
    try {
      return Files.deleteIfExists(
          AstralPractice.getInstance().getDataFolder().toPath().resolve(fileName));
    } catch (IOException e) {
      Common.sendConsoleMmMessage("<red>Couldn't delete " + fileName + ": " + e.getMessage());
      return false;
    }
  }

  /** Writes the profile's settings, display, cosmetics and custom kits, then the file. */
  public void setData() {
    synchronized (profile) {
      setJoinTimestamps();
      setGroup();
      setPrefix();
      setSuffix();
      setNameTemplate();
      setCustomKitPerm();
      setSettings();
      saveCosmetics();
      saveCustomKits();
      saveFile();
    }
  }

  private void setJoinTimestamps() {
    join().setLatest(profile.getLastJoin());
  }

  private void setGroup() {
    profileData.setGroup(profile.getGroup() != null ? profile.getGroup().getName() : null);
  }

  private void setPrefix() {
    profileData.setPrefix(
        profile.getPrefix() != null
            ? AstralPractice.getMiniMessage().serialize(profile.getPrefix())
            : null);
  }

  private void setSuffix() {
    profileData.setSuffix(
        profile.getSuffix() != null
            ? AstralPractice.getMiniMessage().serialize(profile.getSuffix())
            : null);
  }

  private void setNameTemplate() {
    if (profile.getNameTemplate() != null && !profile.getNameTemplate().isEmpty()) {
      profileData.setNameTemplate(profile.getNameTemplate());
    } else {
      profileData.setNameTemplate(null);
    }
  }

  private void setCustomKitPerm() {
    int customKitPerm = profile.getCustomKitPerm();
    if (customKitPerm > 0) {
      profileData.setAllowedCustomKits(customKitPerm);
    }
  }

  /** Writes the sidebar setting, then the file. */
  public void saveSidebarSetting() {
    synchronized (profile) {
      settings().setSidebar(profile.isSidebar());
      saveFile();
    }
  }

  private void setSettings() {
    ProfileData.Settings settings = settings();
    settings.setDuelRequest(profile.isDuelRequest());
    settings.setAutoQueue(profile.isAutoQueue());
    settings.setSidebar(profile.isSidebar());
    settings.setHidePlayers(profile.isHidePlayers());
    settings.setPartyInvites(profile.isPartyInvites());
    settings.setAllowSpectate(profile.isAllowSpectate());
    settings.setFlying(profile.isFlying());
    settings.setPrivateMessages(profile.isPrivateMessages());
    settings.setWorldTime(profile.getWorldTime().toString());
    settings.setPrefixVisibility(profile.getPrefixVisibility().toString());
  }

  private void saveCosmetics() {
    CosmeticsData cosmeticsData = profile.getCosmeticsData();
    if (cosmeticsData == null) {
      return;
    }

    if (profileData.getCosmetics() == null) {
      profileData.setCosmetics(new ProfileData.Cosmetics());
    }
    ProfileData.Cosmetics cosmetics = profileData.getCosmetics();
    cosmetics.setActiveTier(cosmeticsData.getActiveTier().getId());
    cosmetics.setDeathEffect(cosmeticsData.getDeathEffect().getId());
    cosmetics.setLobbyItem(cosmeticsData.getLobbyItemType().name());
    cosmetics.setTrimPreset(cosmeticsData.getActiveTrimPreset());
    cosmetics.setShieldPreset(cosmeticsData.getActiveShieldPreset());

    if (cosmetics.getShield() == null) {
      cosmetics.setShield(new ProfileData.Shield());
    }
    cosmetics.getShield().setActiveLayoutIndex(cosmeticsData.getActiveShieldLayoutIndex());
    cosmetics
        .getShield()
        .setLayouts(
            cosmeticsData.getShieldLayouts().stream().map(ShieldLayout::serialise).toList());

    if (cosmetics.getTiers() == null) {
      cosmetics.setTiers(new LinkedHashMap<>());
    }
    for (ArmorTrimTier tier : ArmorTrimTier.values()) {
      Map<String, ProfileData.Trim> slots =
          cosmetics.getTiers().computeIfAbsent(tier.getId(), _ -> new LinkedHashMap<>());

      for (ArmorSlot slot : ArmorSlot.values()) {
        ProfileData.Trim trim = slots.computeIfAbsent(slot.getId(), _ -> new ProfileData.Trim());

        TrimPattern pattern = cosmeticsData.getPattern(tier, slot);
        trim.setPattern(
            pattern != null
                ? NamespacedKeyUtil.MINECRAFT_NAMESPACE
                    + ":"
                    + CosmeticsPermissionManager.getTrimId(pattern)
                : null);

        TrimMaterial material = cosmeticsData.getMaterial(tier, slot);
        trim.setMaterial(
            material != null
                ? NamespacedKeyUtil.MINECRAFT_NAMESPACE
                    + ":"
                    + CosmeticsPermissionManager.getTrimId(material)
                : null);

        if (trim.getPattern() == null && trim.getMaterial() == null) {
          slots.remove(slot.getId());
        }
      }

      if (slots.isEmpty()) {
        cosmetics.getTiers().remove(tier.getId());
      }
    }
  }

  private void saveCustomKits() {
    for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
      String name = ladder.getName().toLowerCase();

      saveCustomKitSet(profile.getUnrankedCustomKits(), ladder, name, false);
      if (ladder.isRanked()) {
        saveCustomKitSet(profile.getRankedCustomKits(), ladder, name, true);
      }
    }
  }

  private void saveCustomKitSet(
      Map<NormalLadder, Map<Integer, CustomKit>> kits,
      NormalLadder ladder,
      String name,
      boolean ranked) {
    if (kits.isEmpty()) {
      return;
    }

    Map<Integer, CustomKit> ladderKits = kits.get(ladder);
    if (ladderKits == null) {
      return;
    }

    for (int i = 1; i <= 4; i++) {
      CustomKit customKit = ladderKits.get(i);
      if (customKit == null) {
        continue;
      }

      if (profileData.getCustomKits() == null) {
        profileData.setCustomKits(new LinkedHashMap<>());
      }
      ProfileData.CustomKit stored =
          profileData
              .getCustomKits()
              .computeIfAbsent(name, _ -> new LinkedHashMap<>())
              .computeIfAbsent("kit" + i, _ -> new ProfileData.CustomKit());

      ProfileData.KitContents contents = ranked ? stored.getRanked() : stored.getUnranked();
      if (contents == null) {
        contents = new ProfileData.KitContents();
      }
      contents.setInventory(ItemSerializationUtil.itemStackArrayToBase64(customKit.getInventory()));
      contents.setArmor(ItemSerializationUtil.itemStackArrayToBase64(customKit.getArmor()));
      contents.setExtra(ItemSerializationUtil.itemStackArrayToBase64(customKit.getExtra()));

      if (ranked) {
        stored.setRanked(contents);
      } else {
        stored.setUnranked(contents);
      }
    }
  }

  /** Writes the data of a new profile: uuid, first join and the default settings, then the file. */
  public void setDefaultData() {
    synchronized (profile) {
      profileData.setUuid(profile.getUuid().toString());
      join().setFirst(System.currentTimeMillis());

      setCustomKitPerm();

      var defaults = PracticeConfig.settings().getPlayer().getDefaultSettings();
      ProfileData.Settings settings = settings();
      settings.setDuelRequest(defaults.isDuelrequest());
      settings.setAutoQueue(defaults.isAutoqueue());
      settings.setSidebar(defaults.isSidebar());
      settings.setHidePlayers(defaults.isHideplayers());
      settings.setPartyInvites(defaults.isPartyinvites());
      settings.setAllowSpectate(defaults.isAllowspectate());
      settings.setFlying(defaults.isFlying());
      settings.setPrivateMessages(defaults.isPrivatemessage());

      try {
        settings.setWorldTime(
            ProfileWorldTime.valueOf(defaults.getWorldTime().toUpperCase(Locale.ROOT)).toString());
      } catch (Exception ignored) {
        settings.setWorldTime(ProfileWorldTime.DAY.toString());
      }

      try {
        settings.setPrefixVisibility(
            ProfilePrefixVisibility.valueOf(defaults.getPrefixVisibility().toUpperCase(Locale.ROOT))
                .toString());
      } catch (Exception ignored) {
        settings.setPrefixVisibility(ProfilePrefixVisibility.PREFIX_AND_SUFFIX.toString());
      }

      saveFile();
    }
  }

  /**
   * Loads lightweight profile data: timestamps, group, display info, and settings. Does NOT load
   * heavy data (cosmetics, custom kits).
   */
  public void loadLightData() {
    loadJoinTimestamps();
    loadGroup();
    loadPrefix();
    loadSuffix();
    loadNameTemplate();
    loadCustomKitPerm();
    loadSettings();
  }

  /** Loads the profile's settings, display, cosmetics and custom kits from the model. */
  public void getData() {
    loadLightData();
    loadCosmetics();
    loadCustomKits();
  }

  private void loadJoinTimestamps() {
    ProfileData.Join join = profileData.getJoin();
    if (join == null) {
      return;
    }

    if (join.getFirst() != null) {
      profile.setFirstJoin(join.getFirst());
    }

    if (join.getLatest() != null) {
      profile.setLastJoin(join.getLatest());
    }
  }

  private void loadGroup() {
    if (profileData.getGroup() == null) {
      return;
    }

    Group group = GroupManager.getInstance().getGroup(profileData.getGroup());
    if (group != null) {
      try {
        profile.setGroup(group);
      } catch (Exception e) {
        Common.sendConsoleMmMessage(
            "<red>Failed to set group for "
                + profile.getPlayer().getName()
                + "! Error: "
                + e.getMessage());
      }
      profile.setUnrankedLeft(group.getUnrankedLimit());
      profile.setRankedLeft(group.getRankedLimit());
      profile.setEventStartLeft(group.getEventStartLimit());
    }
  }

  private void loadPrefix() {
    if (profileData.getPrefix() != null) {
      profile.setPrefix(NameFormatUtil.parseConfiguredComponent(profileData.getPrefix()));
    }
  }

  private void loadSuffix() {
    if (profileData.getSuffix() != null) {
      profile.setSuffix(NameFormatUtil.parseConfiguredComponent(profileData.getSuffix()));
    }
  }

  private void loadNameTemplate() {
    if (profileData.getNameTemplate() != null) {
      profile.setNameTemplate(profileData.getNameTemplate());
    }
  }

  private void loadCustomKitPerm() {
    if (profileData.getAllowedCustomKits() != null) {
      profile.setAllowedCustomKits(profileData.getAllowedCustomKits());
    }
  }

  private void loadSettings() {
    ProfileData.Settings settings =
        profileData.getSettings() != null ? profileData.getSettings() : new ProfileData.Settings();
    profile.setDuelRequest(isTrue(settings.getDuelRequest()));
    profile.setAutoQueue(isTrue(settings.getAutoQueue()));
    profile.setSidebar(isTrue(settings.getSidebar()));
    profile.setHidePlayers(isTrue(settings.getHidePlayers()));
    profile.setPartyInvites(isTrue(settings.getPartyInvites()));
    profile.setAllowSpectate(isTrue(settings.getAllowSpectate()));
    profile.setFlying(isTrue(settings.getFlying()));
    profile.setPrivateMessages(isTrue(settings.getPrivateMessages()));

    String rawWorldTime =
        settings.getWorldTime() != null ? settings.getWorldTime() : ProfileWorldTime.DAY.toString();
    try {
      profile.setWorldTime(ProfileWorldTime.valueOf(rawWorldTime.toUpperCase(Locale.ROOT)));
    } catch (IllegalArgumentException ignored) {
      profile.setWorldTime(ProfileWorldTime.DAY);
    }

    String rawPrefixVisibility =
        settings.getPrefixVisibility() != null
            ? settings.getPrefixVisibility()
            : ProfilePrefixVisibility.PREFIX_AND_SUFFIX.toString();
    try {
      profile.setPrefixVisibility(
          ProfilePrefixVisibility.valueOf(rawPrefixVisibility.toUpperCase(Locale.ROOT)));
    } catch (IllegalArgumentException ignored) {
      profile.setPrefixVisibility(ProfilePrefixVisibility.PREFIX_AND_SUFFIX);
    }
  }

  private static boolean isTrue(Boolean value) {
    return value != null && value;
  }

  private void loadCosmetics() {
    ProfileData.Cosmetics cosmetics =
        profileData.getCosmetics() != null
            ? profileData.getCosmetics()
            : new ProfileData.Cosmetics();
    ProfileData.Shield shield =
        cosmetics.getShield() != null ? cosmetics.getShield() : new ProfileData.Shield();
    CosmeticsData cosmeticsData = profile.getCosmeticsData();

    try {
      cosmeticsData.setActiveTier(
          ArmorTrimTier.fromId(orDefault(cosmetics.getActiveTier(), "leather")));
      cosmeticsData.setDeathEffect(
          DeathEffect.fromId(orDefault(cosmetics.getDeathEffect(), "none")));
      cosmeticsData.setLobbyItemType(
          CosmeticsData.LobbyItemType.valueOf(
              orDefault(cosmetics.getLobbyItem(), CosmeticsData.LobbyItemType.NONE.name())
                  .toUpperCase(Locale.ROOT)));
      cosmeticsData.setActiveTrimPreset(cosmetics.getTrimPreset());
      cosmeticsData.setActiveShieldPreset(cosmetics.getShieldPreset());

      List<ShieldLayout> shieldLayouts = new ArrayList<>();
      if (shield.getLayouts() != null) {
        for (String serializedLayout : shield.getLayouts()) {
          ShieldLayout layout = ShieldLayout.deserialise(serializedLayout);
          if (layout != null) {
            shieldLayouts.add(layout);
          }
        }
      }
      cosmeticsData.setShieldLayouts(shieldLayouts);

      int activeShieldLayoutIndex =
          shield.getActiveLayoutIndex() != null ? shield.getActiveLayoutIndex() : -1;
      if (activeShieldLayoutIndex < -1 || activeShieldLayoutIndex >= shieldLayouts.size()) {
        activeShieldLayoutIndex = -1;
      }
      cosmeticsData.setActiveShieldLayoutIndex(activeShieldLayoutIndex);

      for (ArmorTrimTier tier : ArmorTrimTier.values()) {
        Map<String, ProfileData.Trim> slots =
            cosmetics.getTiers() != null ? cosmetics.getTiers().get(tier.getId()) : null;
        if (slots == null) {
          continue;
        }

        for (ArmorSlot slot : ArmorSlot.values()) {
          ProfileData.Trim trim = slots.get(slot.getId());
          if (trim == null) {
            continue;
          }

          TrimPattern pattern = getTrimPatternByName(trim.getPattern());
          if (pattern != null) {
            cosmeticsData.setPattern(tier, slot, pattern);
          }

          TrimMaterial material = getTrimMaterialByName(trim.getMaterial());
          if (material != null) {
            cosmeticsData.setMaterial(tier, slot, material);
          }
        }
      }
    } catch (Exception e) {
      // Handle invalid cosmetics data - silently ignore for graceful handling of removed/renamed
      // cosmetics
    }
  }

  private static String orDefault(String value, String fallback) {
    return value != null ? value : fallback;
  }

  private void loadCustomKits() {
    for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
      String name = ladder.getName().toLowerCase();

      profile.getUnrankedCustomKits().put(ladder, loadCustomKitSet(name, false));
      if (ladder.isRanked()) {
        profile.getRankedCustomKits().put(ladder, loadCustomKitSet(name, true));
      }
    }
  }

  private Map<Integer, CustomKit> loadCustomKitSet(String name, boolean ranked) {
    Map<Integer, CustomKit> result = new HashMap<>();

    Map<String, ProfileData.CustomKit> ladderKits =
        profileData.getCustomKits() != null ? profileData.getCustomKits().get(name) : null;
    if (ladderKits == null) {
      return result;
    }

    for (int i = 1; i <= 4; i++) {
      ProfileData.CustomKit stored = ladderKits.get("kit" + i);
      ProfileData.KitContents contents =
          stored == null ? null : ranked ? stored.getRanked() : stored.getUnranked();
      if (contents == null || contents.getInventory() == null) {
        continue;
      }

      ItemStack[] inventory =
          ItemSerializationUtil.itemStackArrayFromBase64(contents.getInventory());
      ItemStack[] armor =
          contents.getArmor() != null
              ? ItemSerializationUtil.itemStackArrayFromBase64(contents.getArmor())
              : null;
      ItemStack[] extra = ItemSerializationUtil.itemStackArrayFromBase64(contents.getExtra());

      result.put(i, new CustomKit(null, inventory, armor, extra));
    }

    return result;
  }

  /**
   * Removes one custom kit of a ladder, then writes the file.
   *
   * @param ladder the ladder
   * @param kit the kit number, 1 to 4
   */
  public void deleteCustomKit(Ladder ladder, int kit) {
    synchronized (profile) {
      Map<String, ProfileData.CustomKit> ladderKits =
          profileData.getCustomKits() != null
              ? profileData.getCustomKits().get(ladder.getName().toLowerCase())
              : null;
      if (ladderKits != null) {
        ladderKits.remove("kit" + kit);
      }
      saveFile();
    }
  }

  /**
   * Removes all custom kits of a ladder, then writes the file.
   *
   * @param ladder the ladder
   */
  public void deleteCustomKit(Ladder ladder) {
    synchronized (profile) {
      if (profileData.getCustomKits() != null) {
        profileData.getCustomKits().remove(ladder.getName().toLowerCase());
      }
      saveFile();
    }
  }

  /**
   * Returns a player custom ladder as stored under {@code player-custom-kit}.
   *
   * @param key the ladder's key, e.g. {@code "0"}
   * @return the stored ladder, or {@code null} if there is none
   */
  public CustomLadderData getCustomLadderData(String key) {
    ProfileData.PlayerCustomKits section = profileData.getPlayerCustomKits();
    return section != null ? section.getLadders().get(key) : null;
  }

  /**
   * Returns a player custom ladder as stored under {@code player-custom-kit}, adding it if missing.
   *
   * @param key the ladder's key, e.g. {@code "0"}
   * @return the stored ladder, to be updated in place
   */
  public CustomLadderData getOrCreateCustomLadderData(String key) {
    if (profileData.getPlayerCustomKits() == null) {
      profileData.setPlayerCustomKits(new ProfileData.PlayerCustomKits());
    }
    return profileData
        .getPlayerCustomKits()
        .getLadders()
        .computeIfAbsent(key, ignored -> new CustomLadderData());
  }

  private ProfileData.Join join() {
    if (profileData.getJoin() == null) {
      profileData.setJoin(new ProfileData.Join());
    }
    return profileData.getJoin();
  }

  private ProfileData.Settings settings() {
    if (profileData.getSettings() == null) {
      profileData.setSettings(new ProfileData.Settings());
    }
    return profileData.getSettings();
  }

  private TrimPattern getTrimPatternByName(String name) {
    return NamespacedKeyUtil.resolve(RegistryKey.TRIM_PATTERN, name);
  }

  private TrimMaterial getTrimMaterialByName(String name) {
    return NamespacedKeyUtil.resolve(RegistryKey.TRIM_MATERIAL, name);
  }
}
