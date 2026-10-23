package dev.lokspel.practice.manager.fight.ffa.game;

import static dev.lokspel.practice.manager.fight.util.PlayerUtil.isPlayerStuck;

import dev.lokspel.api.event.FfaRemovePlayerEvent;
import dev.lokspel.api.event.spectate.end.FfaSpectateEndEvent;
import dev.lokspel.api.event.spectate.start.FfaSpectateStartEvent;
import dev.lokspel.practice.AstralPractice;
import dev.lokspel.practice.config.PracticeConfig;
import dev.lokspel.practice.config.settings.SettingsConfig;
import dev.lokspel.practice.manager.arena.arenas.FfaArena;
import dev.lokspel.practice.manager.backend.BackendUtil;
import dev.lokspel.practice.manager.fight.ffa.FfaFightPlayer;
import dev.lokspel.practice.manager.fight.match.MatchManager;
import dev.lokspel.practice.manager.fight.match.enums.TeamEnum;
import dev.lokspel.practice.manager.fight.match.util.KitUtil;
import dev.lokspel.practice.manager.fight.util.DeathCause;
import dev.lokspel.practice.manager.fight.util.PlayerUtil;
import dev.lokspel.practice.manager.fight.util.stats.Statistic;
import dev.lokspel.practice.manager.gui.GuiItem;
import dev.lokspel.practice.manager.inventory.Inventory;
import dev.lokspel.practice.manager.inventory.InventoryManager;
import dev.lokspel.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.lokspel.practice.manager.nametag.NametagManager;
import dev.lokspel.practice.manager.party.Party;
import dev.lokspel.practice.manager.party.PartyManager;
import dev.lokspel.practice.manager.profile.Profile;
import dev.lokspel.practice.manager.profile.ProfileManager;
import dev.lokspel.practice.manager.profile.enums.ProfileStatus;
import dev.lokspel.practice.manager.sidebar.SidebarManager;
import dev.lokspel.practice.manager.spectator.SpectatorManager;
import dev.lokspel.practice.util.Common;
import dev.lokspel.practice.util.Cuboid;
import dev.lokspel.practice.util.entityhider.PlayerHider;
import dev.lokspel.practice.util.fight.CombatLogUtil;
import dev.lokspel.practice.util.fight.LastAttackerTracker;
import dev.lokspel.practice.util.fightmapchange.FightChangeOptimized;
import dev.lokspel.practice.util.interfaces.Spectatable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Getter
public class Ffa implements Spectatable, dev.lokspel.api.interfaces.Ffa {

  private static final Random random = new Random();

  private final Map<Player, NormalLadder> players = new HashMap<>();
  private final Map<Player, FfaFightPlayer> fightPlayers = new HashMap<>();
  private final Map<Player, Statistic> statistics = new HashMap<>();

  private final List<Player> spectators = new ArrayList<>();
  private final FfaArena arena;
  private final LadderSelector ladderSelectorGui;

  private boolean build;
  private boolean mapBlowable;
  private BuildRollback buildRollback;

  private boolean open;

  // Last player to damage this player (used for void kill credit)
  private final LastAttackerTracker lastAttackerTracker = new LastAttackerTracker();

  public Ffa(FfaArena arena) {
    this.arena = arena;
    this.build = arena.isBuild();
    this.mapBlowable = arena.isMapBlowable();
    this.ladderSelectorGui = new LadderSelector(this);
    this.open = false;
  }

  public void open() {
    if (this.open) {
      return;
    }

    if (!this.arena.isEnabled()) {
      return;
    }

    this.build = this.arena.isBuild();
    this.mapBlowable = this.arena.isMapBlowable();
    this.open = true;

    if (this.build) {
      this.buildRollback =
          new BuildRollback(new FightChangeOptimized(this), this::handleRollbackComplete);
      this.buildRollback.begin();
    }

    SpectatorManager.getInstance().getSpectatorMenuGui().update();
    this.ladderSelectorGui.update();
  }

  public void close(String message) {
    if (!this.open) {
      return;
    }

    if (this.build) {
      this.buildRollback.cancel();
      this.buildRollback = null;
    }

    if (!AstralPractice.getInstance().isEnabled()) {
      return;
    }

    if (message != null) {
      this.sendMessage(message, true);
    }

    this.open = false;

    for (Player player : new ArrayList<>(players.keySet())) {
      removePlayer(player);
    }
    for (Player spectator : new ArrayList<>(spectators)) {
      removeSpectator(spectator);
    }

    SpectatorManager.getInstance().getSpectatorMenuGui().update();
  }

  public void addPlayer(Player player, NormalLadder ladder) {
    if (players.containsKey(player)) {
      return;
    }

    players.put(player, ladder);

    // A player who joins an FFA while at a party must leave that party, so that
    // they are not pulled into the party's game when it starts (which would
    // otherwise kill them and leave them in an inconsistent flight state).
    Party party = PartyManager.getInstance().getParty(player);
    if (party != null) {
      party.removeMember(player, false);
    }

    // Use FFAFightPlayer to handle custom kit selection
    FfaFightPlayer ffaFightPlayer = new FfaFightPlayer(player, this, ladder);
    fightPlayers.put(player, ffaFightPlayer);
    statistics.put(player, new Statistic(player.getUniqueId()));

    // Hide the spectators
    for (Player spectator : this.spectators) {
      // The joining player is still mapped to the lobby world until teleportPlayer below,
      // so they must not be treated as a lobby player here.
      PlayerHider.getInstance().hidePlayer(player, spectator, false);
      PlayerHider.getInstance().showPlayer(spectator, player);
    }

    // Show other players
    for (Player ffaPlayer : this.players.keySet()) {
      if (!ffaPlayer.equals(player)) {
        PlayerHider.getInstance().showPlayer(ffaPlayer, player);
        PlayerHider.getInstance().showPlayer(player, ffaPlayer);
      }
    }

    teleportPlayer(player);
    this.sendMessage(
        PracticeConfig.language()
            .getFfa()
            .getGame()
            .getPlayerJoin()
            .replace("%player%", player.getName()),
        true);

    dev.lokspel.practice.util.playerutil.PlayerUtil.setFightPlayer(player, ladder);
    this.addPlayerToBelowName(player);

    // Show kit chooser or apply default kit
    ffaFightPlayer.showKitChooserOrApplyKit();

    PlayerUtil.setAttackSpeed(player, ladder.getAttackCooldownModifier());

    ProfileManager.getInstance().getProfile(player).setStatus(ProfileStatus.FFA);
    NametagManager.getInstance().updateNametag(player);
    SpectatorManager.getInstance().getSpectatorMenuGui().update();
  }

  public void changePlayerLadder(Player player, NormalLadder ladder) {
    if (!players.containsKey(player) || ladder == null) {
      return;
    }

    players.put(player, ladder);
    dev.lokspel.practice.util.playerutil.PlayerUtil.setFightPlayer(player, ladder);
    KitUtil.loadDefaultLadderKit(player, TeamEnum.FFA, ladder);
    PlayerUtil.setAttackSpeed(player, ladder.getAttackCooldownModifier());

    FfaFightPlayer ffaFightPlayer = fightPlayers.get(player);
    if (ffaFightPlayer != null) {
      ffaFightPlayer.resetForNewLadder(ladder);
    }
  }

  /**
   * Called when a player selects a custom kit from their inventory. Once a kit is selected, the
   * player becomes a full combatant.
   *
   * @param player the player selecting a kit
   * @param slot the inventory slot of the selected kit
   */
  public void playerSelectKit(Player player, int slot) {
    FfaFightPlayer ffaFightPlayer = fightPlayers.get(player);
    if (ffaFightPlayer == null) {
      return;
    }
    ffaFightPlayer.selectKit(slot);
  }

  /**
   * Returns whether a player is waiting to select a kit. Players waiting for kit selection cannot
   * be hurt or interact with others.
   *
   * @param player the player to check
   * @return true if player is waiting for kit selection, false otherwise
   */
  public boolean isPlayerWaitingForKitSelection(Player player) {
    FfaFightPlayer ffaFightPlayer = fightPlayers.get(player);
    if (ffaFightPlayer == null) {
      return false;
    }
    return ffaFightPlayer.isWaitingForKitSelection();
  }

  public void removePlayer(Player player) {
    if (!players.containsKey(player)) {
      return;
    }

    Bukkit.getPluginManager().callEvent(new FfaRemovePlayerEvent(this, player));

    this.sendMessage(
        PracticeConfig.language()
            .getFfa()
            .getGame()
            .getPlayerLeave()
            .replace("%player%", player.getName()),
        true);

    // Remove in-flight ender pearls to prevent the player from being
    // teleported back to the arena world after they have left.
    for (Entity entity : player.getWorld().getEntities()) {
      if (entity instanceof EnderPearl pearl && player.equals(pearl.getShooter())) {
        pearl.remove();
      }
    }

    players.remove(player);
    fightPlayers.remove(player);
    statistics.remove(player);
    this.removePlayerFromBelowName(player);
    this.clearFfaCombat(player);
    PlayerUtil.resetAttackSpeed(player);

    InventoryManager.getInstance().setLobbyInventory(player, true);
    SpectatorManager.getInstance().getSpectatorMenuGui().update();
  }

  public void killPlayer(Player player, Player killer, String deathMessage) {
    if (!players.containsKey(player)) {
      return;
    }

    // If no explicit killer and the death message is the plain void message,
    // check whether a recent attacker should be credited instead.
    if (killer == null) {
      Player lastAttacker = getLastAttacker(player);
      if (lastAttacker != null
          && !lastAttacker.equals(player)
          && deathMessage != null
          && deathMessage.equals(DeathCause.VOID.getMessage())) {
        killer = lastAttacker;
        deathMessage = DeathCause.VOID_BY_PLAYER.getMessage().replace("%killer%", killer.getName());
      }
    }

    fightPlayers.get(player).die(deathMessage, statistics.get(player));
    Profile deadProfile = fightPlayers.get(player).getProfile();
    deadProfile.getStats().getLadderStat(players.get(player)).increaseDeaths();
    deadProfile.getStats().getFfaStats().increaseDeaths();
    this.clearFfaCombat(player);

    if (killer != null && !killer.equals(player)) {
      Profile killerProfile = fightPlayers.get(killer).getProfile();
      killerProfile.getStats().getLadderStat(players.get(killer)).increaseKills();
      killerProfile.getStats().getFfaStats().increaseKills();

      increaseFfaSessionKills(killer);
      broadcastKillstreak(killer);
      Statistic killerStat = statistics.get(killer);
      if (killerStat != null) {
        killerProfile.getStats().getFfaStats().updateBestKillStreak(killerStat.getBestKillStreak());
      }

      playDeathEffect(killer, player);

      if (arena.isReKitAfterKill()) {
        applySelectedOrDefaultKit(killer);
      }

      if (arena.isHealthResetOnKill()) {
        applyHealthResetOnKill(killer);
      }

      if (CombatLogUtil.getInstance().isClearOnKill()) {
        this.clearFfaCombat(killer);
      }

      SidebarManager.getInstance().updatePlayerSidebar(killer);
    }

    SidebarManager.getInstance().updatePlayerSidebar(player);

    if (arena.isLobbyAfterDeath()) {
      this.removePlayer(player);
    } else {
      dev.lokspel.practice.util.playerutil.PlayerUtil.setFightPlayer(player, players.get(player));
      applySelectedOrDefaultKit(player);
      PlayerUtil.setAttackSpeed(player, players.get(player).getAttackCooldownModifier());

      Bukkit.getScheduler()
          .runTaskLater(AstralPractice.getInstance(), () -> teleportPlayer(player), 1L);
    }
  }

  private void applySelectedOrDefaultKit(Player player) {
    FfaFightPlayer ffaFightPlayer = fightPlayers.get(player);
    if (ffaFightPlayer != null) {
      ffaFightPlayer.restoreKitOnDeath();
      return;
    }

    NormalLadder ladder = players.get(player);
    if (ladder != null) {
      KitUtil.loadDefaultLadderKit(player, TeamEnum.FFA, ladder);
    }
  }

  private void playDeathEffect(Player killer, Player victim) {
    if (killer == null || victim == null) {
      return;
    }

    Profile killerProfile =
        fightPlayers.containsKey(killer)
            ? fightPlayers.get(killer).getProfile()
            : ProfileManager.getInstance().getProfile(killer);

    List<Player> viewers = new ArrayList<>(players.keySet());
    viewers.addAll(spectators);
    Common.playDeathEffect(killerProfile, victim.getLocation(), viewers);
  }

  private void applyHealthResetOnKill(Player killer) {
    AttributeInstance maxHealth = killer.getAttribute(Attribute.MAX_HEALTH);
    double maxHealthValue = maxHealth != null ? maxHealth.getValue() : 20.0D;
    killer.setHealth(Math.max(1.0D, maxHealthValue));
    killer.setFoodLevel(20);
    killer.setSaturation(20.0F);
    killer.setFireTicks(0);
    killer.setFallDistance(0.0F);
  }

  /**
   * Records that {@code attacker} last hit {@code victim}. Called from damage listeners so void
   * deaths can be attributed correctly.
   */
  public void recordAttack(Player victim, Player attacker) {
    lastAttackerTracker.recordAttack(victim, attacker);
  }

  /**
   * Returns the last player who hit {@code victim} within the expiry window, or {@code null} if
   * there is none.
   */
  public @Nullable Player getLastAttacker(Player victim) {
    return lastAttackerTracker.getLastAttacker(victim, players.keySet());
  }

  public boolean isFfaKitBlocked() {
    return CombatLogUtil.getInstance().isEnabled()
        && PracticeConfig.settings().getFfa().getCombatLog().isBlockKit();
  }

  public boolean isFfaLeaveBlocked() {
    return CombatLogUtil.getInstance().isEnabled()
        && PracticeConfig.settings().getFfa().getCombatLog().isBlockLeave();
  }

  public boolean isFfaKillOnQuit() {
    return CombatLogUtil.getInstance().isKillOnQuit();
  }

  public void tagFfaCombat(Player victim, Player attacker) {
    CombatLogUtil.getInstance().tag(victim, attacker);
  }

  public boolean isFfaInCombat(Player player) {
    return CombatLogUtil.getInstance().isInCombat(player);
  }

  public void clearFfaCombat(Player player) {
    CombatLogUtil.getInstance().clear(player);
  }

  public Player getFfaCombatLastAttacker(Player player) {
    return CombatLogUtil.getInstance().getLastAttacker(player);
  }

  /**
   * Handles a player disconnecting while in combat. If enabled, the disconnect counts as a kill for
   * the player's most recent attacker.
   */
  public void handleFfaCombatLogQuit(Player player) {
    if (!isFfaKillOnQuit()) {
      return;
    }
    if (!players.containsKey(player)) {
      return;
    }

    Player attacker = getFfaCombatLastAttacker(player);
    if (attacker == null || attacker.equals(player) || !players.containsKey(attacker)) {
      return;
    }

    Profile deadProfile = fightPlayers.get(player).getProfile();
    deadProfile.getStats().getLadderStat(players.get(player)).increaseDeaths();
    deadProfile.getStats().getFfaStats().increaseDeaths();

    Profile killerProfile = fightPlayers.get(attacker).getProfile();
    killerProfile.getStats().getLadderStat(players.get(attacker)).increaseKills();
    killerProfile.getStats().getFfaStats().increaseKills();

    increaseFfaSessionKills(attacker);
    broadcastKillstreak(attacker);
    Statistic attackerStat = statistics.get(attacker);
    if (attackerStat != null) {
      killerProfile.getStats().getFfaStats().updateBestKillStreak(attackerStat.getBestKillStreak());
    }

    // Finalize the quitter's session stats.
    Statistic deadStatistic = statistics.get(player);
    if (deadStatistic != null) {
      deadStatistic.end(true);
    }

    playDeathEffect(attacker, player);

    if (arena.isReKitAfterKill()) {
      applySelectedOrDefaultKit(attacker);
    }

    if (arena.isHealthResetOnKill()) {
      applyHealthResetOnKill(attacker);
    }

    SidebarManager.getInstance().updatePlayerSidebar(attacker);

    // Clear the attacker's combat tag now that the fight is over.
    if (CombatLogUtil.getInstance().isClearOnKill()) {
      this.clearFfaCombat(attacker);
    }

    this.sendMessage(
        PracticeConfig.language()
            .getFight()
            .getDeathMessages()
            .getCombatLogQuit()
            .replace("%player%", player.getName())
            .replace("%killer%", attacker.getName()),
        true);
  }

  /**
   * Increments the FFA session kill counter for {@code killer}. This is the counter shown on the
   * FFA sidebar ({@code %kills%}).
   */
  public void increaseFfaSessionKills(Player killer) {
    Statistic statistic = statistics.computeIfAbsent(killer, p -> new Statistic(p.getUniqueId()));
    statistic.setKills(statistic.getKills() + 1);
    statistic.increaseKillStreak();
  }

  /**
   * Broadcasts the killer's current killstreak to the FFA when it matches a configured milestone.
   */
  private void broadcastKillstreak(Player killer) {
    Statistic statistic = statistics.get(killer);
    if (statistic == null) {
      return;
    }

    SettingsConfig.Ffa.Killstreak config = PracticeConfig.settings().getFfa().getKillstreak();
    int streak = statistic.getKillStreak();
    if (!config.isEnabled() || !isKillstreakMilestone(streak)) {
      return;
    }

    this.sendMessage(
        PracticeConfig.language()
            .getFfa()
            .getGame()
            .getKillstreakBroadcast()
            .replace("%player%", killer.getName())
            .replace("%streak%", String.valueOf(streak)),
        true);
  }

  /**
   * Returns whether {@code streak} is a configured milestone. After the highest milestone, the
   * streak is also a milestone every {@code REPEAT-EVERY} kills (if enabled), so long streaks keep
   * being announced.
   */
  private boolean isKillstreakMilestone(int streak) {
    List<Integer> milestones = PracticeConfig.settings().getFfa().getKillstreak().getMilestones();
    if (milestones.isEmpty()) {
      return false;
    }
    if (milestones.contains(streak)) {
      return true;
    }

    int repeatEvery = PracticeConfig.settings().getFfa().getKillstreak().getRepeatEvery();
    if (repeatEvery <= 0) {
      return false;
    }

    int highest = Collections.max(milestones);
    return streak > highest && (streak - highest) % repeatEvery == 0;
  }

  public void teleportPlayer(Player player) {
    player.teleport(arena.getFfaPositions().get(random.nextInt(arena.getFfaPositions().size())));
  }

  public void sendMessage(String message, boolean spectator) {
    Common.sendMessage(players.keySet(), spectators, message, spectator);
  }

  private void handleRollbackComplete() {
    if (!this.open || !this.build) {
      return;
    }

    if (PracticeConfig.settings().getFfa().getRollback().isTeleportToSpawn()) {
      for (Player player : new ArrayList<>(this.players.keySet())) {
        if (player == null || !player.isOnline()) {
          continue;
        }
        teleportPlayer(player);
      }
    }

    teleportStuckPlayersAfterRollback();
  }

  private void teleportStuckPlayersAfterRollback() {
    if (!this.open || !this.build) {
      return;
    }

    List<Player> activePlayers = new ArrayList<>(this.players.keySet());

    for (Player spectator : new ArrayList<>(this.spectators)) {
      if (spectator == null || !spectator.isOnline()) {
        continue;
      }

      if (!isPlayerStuck(spectator)) {
        continue;
      }

      if (!activePlayers.isEmpty()) {
        spectator.teleport(activePlayers.get(random.nextInt(activePlayers.size())));
      } else if (!this.arena.getFfaPositions().isEmpty()) {
        spectator.teleport(
            this.arena.getFfaPositions().get(random.nextInt(this.arena.getFfaPositions().size())));
      } else {
        spectator.teleport(this.arena.getCuboid().getCenter().add(0, 1, 0));
      }
    }

    for (Player player : activePlayers) {
      if (player == null || !player.isOnline()) {
        continue;
      }

      if (!isPlayerStuck(player)) {
        continue;
      }

      player.teleport(
          player.getWorld().getHighestBlockAt(player.getLocation()).getLocation().add(0, 1, 0));
    }
  }

  @Override
  public FightChangeOptimized getFightChange() {
    if (this.getBuildRollback() == null) {
      return null;
    }
    return this.getBuildRollback().getFightChange();
  }

  @Override
  public void addSpectator(Player player, Player target, boolean teleport, boolean message) {
    if (this.spectators.contains(player)) {
      return;
    }

    FfaSpectateStartEvent ffaSpectateStartEvent = new FfaSpectateStartEvent(player, this);
    Bukkit.getPluginManager().callEvent(ffaSpectateStartEvent);
    if (ffaSpectateStartEvent.isCancelled()) {
      return;
    }

    // If the spectator was spectating another match/FFA, remove them first.
    Spectatable previousSpectatable =
        SpectatorManager.getInstance().getSpectators().get(player.getUniqueId());
    if (previousSpectatable != null) {
      previousSpectatable.removeSpectator(player);
    }

    Profile profile = ProfileManager.getInstance().getProfile(player);

    this.spectators.add(player);
    SpectatorManager.getInstance().getSpectators().put(player.getUniqueId(), this);
    profile.setStatus(ProfileStatus.SPECTATE);
    this.addPlayerToBelowName(player);

    // Hide spectator from players.
    for (Player eventPlayer : this.players.keySet()) {
      PlayerHider.getInstance().hidePlayer(eventPlayer, player);
      PlayerHider.getInstance().showPlayer(player, eventPlayer);
    }

    // Hide spectators from each other.
    for (Player eventSpectator : this.spectators) {
      if (!eventSpectator.equals(player)) {
        PlayerHider.getInstance().hidePlayer(eventSpectator, player);
        PlayerHider.getInstance().hidePlayer(player, eventSpectator);
      }
    }

    if (target != null && this.players.containsKey(target)) {
      player.teleport(target);
    } else if (this.arena.getSpectatorPosition() != null) {
      player.teleport(this.arena.getSpectatorPosition());
    } else {
      if (players.isEmpty()) {
        this.teleportPlayer(player);
      } else {
        player.teleport(new ArrayList<>(players.keySet()).get(random.nextInt(players.size())));
      }
    }

    if (profile.isStaffMode()) {
      InventoryManager.getInstance().setStaffModeInventory(player);
      player.setFlySpeed((float) InventoryManager.STAFF_SPECTATOR_SPEED / 10);
    } else {
      InventoryManager.getInstance().setInventory(player, Inventory.InventoryType.SPECTATE_FFA);
    }

    if (message) {
      this.sendMessage(
          PracticeConfig.language()
              .getFfa()
              .getGame()
              .getSpectateStart()
              .replace("%player%", player.getName()),
          true);
    }

    SpectatorManager.getInstance().getSpectatorMenuGui().update();
  }

  @Override
  public void removeSpectator(Player player) {
    if (!this.spectators.contains(player)) {
      return;
    }

    this.spectators.remove(player);
    SpectatorManager.getInstance().getSpectators().remove(player.getUniqueId());
    this.removePlayerFromBelowName(player);

    if (AstralPractice.getInstance().isEnabled() && player.isOnline()) {
      InventoryManager.getInstance().setLobbyInventory(player, true);
    }

    SpectatorManager.getInstance().getSpectatorMenuGui().update();

    FfaSpectateEndEvent ffaSpectateEndEvent = new FfaSpectateEndEvent(player, this);
    Bukkit.getPluginManager().callEvent(ffaSpectateEndEvent);
  }

  private static String buildOn() {
    return PracticeConfig.guis()
        .getGuis()
        .getSpectatorMenu()
        .getIcons()
        .getFfaIcon()
        .getBuildStatus()
        .getEnabled();
  }

  private static String buildOff() {
    return PracticeConfig.guis()
        .getGuis()
        .getSpectatorMenu()
        .getIcons()
        .getFfaIcon()
        .getBuildStatus()
        .getDisabled();
  }

  @Override
  public GuiItem getSpectatorMenuItem() {
    return BackendUtil.getGuiItem(
            PracticeConfig.guis().getGuis().getSpectatorMenu().getIcons().getFfaIcon())
        .setMaterial(arena.getIcon().getType())
        .setDamage(Common.getItemDamage(arena.getIcon()))
        .replace("%players%", String.valueOf(players.size()))
        .replace("%spectators%", String.valueOf(spectators.size()))
        .replace("%arena%", arena.getDisplayName())
        .replace("%build_status%", arena.isBuild() ? buildOn() : buildOff());
  }

  @Override
  public List<Player> getActivePlayerList() {
    return new ArrayList<>(players.keySet());
  }

  @Override
  public boolean canDisplay() {
    return this.open;
  }

  @Override
  public boolean isBuild() {
    return this.build;
  }

  @Override
  public boolean isMapBlowable() {
    return this.mapBlowable;
  }

  @Override
  public Cuboid getCuboid() {
    return arena.getCuboid();
  }

  private void addPlayerToBelowName(Player player) {
    if (!this.arena.isHealthBelowName()) {
      return;
    }

    MatchManager.getInstance().getBelowNameManager().initForUser(player);
  }

  private void removePlayerFromBelowName(Player player) {
    if (!this.arena.isHealthBelowName()) {
      return;
    }

    MatchManager.getInstance().getBelowNameManager().cleanUpForUser(player);
  }

  /**
   * Returns the single ladder configured for this FFA arena, or {@code null} if multiple or no
   * ladders are assigned.
   *
   * @return the assigned {@link NormalLadder}, or {@code null}
   */
  public @Nullable NormalLadder getLadder() {
    if (this.arena == null || this.arena.getAssignedLadders().size() != 1) {
      return null;
    }
    return this.arena.getAssignedLadders().iterator().next();
  }

  @Override
  public @Nullable String getLadderName() {
    NormalLadder ladder = getLadder();
    return ladder != null ? ladder.getName() : null;
  }

  @Override
  public String getLadderDisplayName() {
    NormalLadder ladder = getLadder();
    if (ladder != null) {
      return ladder.getDisplayName();
    }

    if (this.arena != null && this.arena.getAssignedLadders().size() > 1) {
      String label =
          PracticeConfig.settings().getQueue().getMulti().getSidebar().getMultipleLabel();
      if (label == null || label.isBlank()) {
        label = PracticeConfig.language().getQueues().getMulti().getSidebarMultiple();
      }
      return (label == null || label.isBlank()) ? "Multiple Ladders" : label;
    }

    return "Unknown";
  }
}
