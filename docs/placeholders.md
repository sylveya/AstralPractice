<img width="2000" height="1000" alt="image(6)" src="https://i.imgur.com/ydTpj5d.png" />

---

# Placeholders

> [!NOTE]
> [PlaceholderAPI](https://placeholderapi.com) is required for placeholders.
>
> The expansion identifier is `ap`. All placeholders use the format `%ap_<placeholder>%`.

---

## Global

| PlaceholderAPI    | Description                            |
|-------------------|----------------------------------------|
| %ap_in_queue%    | Number of queues                       |
| %ap_in_fight%    | Number of players in matches           |
| %ap_ping%        | The ping of the player in milliseconds |

## Player Stats

| PlaceholderAPI          | Description                            |
|-------------------------|----------------------------------------|
| %ap_wins_global%       | Total wins (ranked + unranked)         |
| %ap_wins_global_r%     | Total ranked wins                      |
| %ap_wins_global_u%     | Total unranked wins                    |
| %ap_wins_ranked%       | Total ranked wins                      |
| %ap_wins_unranked%     | Total unranked wins                    |
| %ap_losses_global%     | Total losses (ranked + unranked)       |
| %ap_losses_global_r%   | Total ranked losses                    |
| %ap_losses_global_u%   | Total unranked losses                  |
| %ap_losses_ranked%     | Total ranked losses                    |
| %ap_losses_unranked%   | Total unranked losses                  |
| %ap_kills_global%      | Total kills in all ladders             |
| %ap_deaths_global%     | Total deaths in all ladders            |
| %ap_kdr_global%        | Global kill/death ratio                |
| %ap_winrate_global%    | Global winrate in percent              |
| %ap_winrate_ranked%    | Ranked winrate in percent              |
| %ap_winrate_unranked%  | Unranked winrate in percent            |
| %ap_elo_global%        | Global elo                             |
| %ap_division_short%    | Global division short name             |
| %ap_division_full%     | Global division full name              |
| %ap_division_weight%   | Global division weight                 |
| %ap_nametag_color%     | Player's nametag color                 |
| %ap_group_name%        | Player group name                      |
| %ap_group_prefix%      | Player group prefix                    |
| %ap_group_suffix%      | Player group suffix                    |
| %ap_group_limit_r%     | Daily ranked match limit of the group  |
| %ap_group_limit_u%     | Daily unranked match limit of the group|

## Ladder Stats

Ladder placeholders use the ladder name instead of the type:

| PlaceholderAPI                  | Description            |
|---------------------------------|------------------------|
| %ap_wins_ladder_<ladder>_u%    | Unranked wins in kit   |
| %ap_wins_ladder_<ladder>_r%    | Ranked wins in kit     |
| %ap_losses_ladder_<ladder>_u%  | Unranked losses in kit |
| %ap_losses_ladder_<ladder>_r%  | Ranked losses in kit   |
| %ap_elo_ladder_<ladder>%       | Elo in kit             |

**Specific ladder example:** `%ap_wins_ladder_Boxing_r%` — returns the player's ranked wins in the Boxing ladder.

## Queue

| PlaceholderAPI                | Description                     |
|-------------------------------|---------------------------------|
| %ap_in_queue_<ladder>%       | Number of queues for a ladder   |
| %ap_in_fight_<ladder>%       | Number of players in a ladder   |

## FFA Arena

| PlaceholderAPI               | Description                                  |
|------------------------------|----------------------------------------------|
| %ap_ffa_<arena>_players%    | Players in the FFA arena                     |
| %ap_ffa_<arena>_spectators% | Spectators in the FFA arena                  |
| %ap_ffa_killstreak%         | Player's current FFA kill streak             |
| %ap_ffa_best_killstreak%    | Player's best FFA kill streak in the session |

## Leaderboards

Format: `%ap_lb_global_<type>_<position>_<name|value>%`

**Available types:** `wins`, `elo`

**Position:** `1` to `10`

| PlaceholderAPI           | Description                              |
|--------------------------|------------------------------------------|
| %ap_lb_global_wins_1_k% | Player name with the most wins (rank 1) |
| %ap_lb_global_wins_1_v% | Win count of the #1 player              |
| %ap_lb_global_elo_3_k%  | Player name ranked #3 in elo            |
| %ap_lb_global_elo_3_v%  | Elo of the #3 player                    |

Format: `%ap_lb_ladder_<ladder>_<type>_<position>_<name|value>%`

| PlaceholderAPI                       | Description                                |
|--------------------------------------|--------------------------------------------|
| %ap_lb_ladder_Boxing_wins_1_k%      | Player name with the most wins in Boxing  |
| %ap_lb_ladder_Boxing_wins_1_v%      | Win count of the #1 player in Boxing      |
| %ap_lb_ladder_Axe_elo_3_k%          | Player name ranked #3 in elo for Axe      |
| %ap_lb_ladder_Axe_elo_3_v%          | Elo of the #3 player in Axe               |