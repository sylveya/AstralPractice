package dev.lokspel.api.player;

import dev.lokspel.api.enums.DivisionName;
import dev.lokspel.api.enums.PlayerState;
import dev.lokspel.api.utilities.PlayerNametag;
import java.util.UUID;
import org.bukkit.entity.Player;

/** API section for managing players, states, nametags, divisions, and match limits. */
public interface PlayerApi {

  /**
   * Get the player's current practice state.
   *
   * @param player The player
   * @return The player's current state
   */
  PlayerState getState(Player player);

  /**
   * Get the player's current practice state by UUID.
   *
   * @param uuid The player UUID
   * @return The player's current state
   */
  PlayerState getState(UUID uuid);

  /**
   * Retrieves the nametag information of a given player.
   *
   * @param player The player whose nametag information is to be retrieved
   * @return The {@link PlayerNametag} associated with the player
   */
  PlayerNametag getNametag(Player player);

  /**
   * Get the player's division.
   *
   * @param player The player
   * @param divisionName Short or Full name of the division
   * @return The player's division
   */
  String getDivision(Player player, DivisionName divisionName);

  /**
   * Get the player's division for a specific ladder.
   *
   * @param player The player
   * @param ladder The ladder identifier
   * @param divisionName Short or Full name of the division
   * @return The player's ladder division, or null if not found
   */
  String getDivision(Player player, String ladder, DivisionName divisionName);

  /**
   * Get the player's best ladder division.
   *
   * @param player The player
   * @param divisionName Short or Full name of the division
   * @return The player's best division, or null if none
   */
  String getBestDivision(Player player, DivisionName divisionName);

  /**
   * Get the player's division in their current active match.
   *
   * @param player The player
   * @param divisionName Short or Full name of the division
   * @return The player's match division, or null if none
   */
  String getMatchDivision(Player player, DivisionName divisionName);

  /**
   * Get the amount of unranked matches left for the player today.
   *
   * @param player The player
   * @return The amount of unranked matches left
   */
  int getUnRankedLeft(Player player);

  /**
   * Get the amount of ranked matches left for the player today.
   *
   * @param player The player
   * @return The amount of ranked matches left
   */
  int getRankedLeft(Player player);

  /**
   * Reset the player's unranked matches.
   *
   * @param player The player
   */
  void resetUnRanked(Player player);

  /**
   * Reset the player's ranked matches.
   *
   * @param player The player
   */
  void resetRanked(Player player);

  /**
   * Add unranked matches to the player.
   *
   * @param player The player
   * @param amount The amount of unranked matches to add
   */
  void addUnRanked(Player player, int amount);

  /**
   * Add ranked matches to the player.
   *
   * @param player The player
   * @param amount The amount of ranked matches to add
   */
  void addRanked(Player player, int amount);
}
