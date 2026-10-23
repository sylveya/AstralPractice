<img width="2000" height="1000" alt="image(6)" src="https://i.imgur.com/ydTpj5d.png" />

---

# API

AstralPractice provides an API module (`AstralPractice-Api`) allowing other plugins to query player states, manage matches, inspect statistics, and listen to custom events.

## Dependency Setup

### Gradle (Groovy)

```groovy
repositories {
    mavenCentral()
    maven { url = uri('https://repo.papermc.io/repository/maven-public/') }
    maven { url = uri('https://jitpack.io') }
}

dependencies {
    compileOnly 'com.github.sylveya.AstralPractice:AstralPractice-Api:2.6.0'
}
```

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.sylveya.AstralPractice:AstralPractice-Api:2.6.0")
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.sylveya.AstralPractice</groupId>
        <artifactId>AstralPractice-Api</artifactId>
        <version>2.6.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### plugin.yml

Add AstralPractice as a dependency or soft-dependency:

```yaml
depend: [AstralPractice]
# or
softdepend: [AstralPractice]
```

---

## Accessing the API

The API instance can be accessed statically or retrieved from the Bukkit `ServicesManager`:

```java
// Static access
if (AstralPracticeApi.isAvailable()) {
    AstralPracticeApi api = AstralPracticeApi.getInstance();
}

// Or via Bukkit ServicesManager
AstralPracticeApi api = Bukkit.getServicesManager().load(AstralPracticeApi.class);
```

> [!TIP]
> If your plugin soft-depends on AstralPractice or loads alongside it, listen to `AstralPracticeReadyEvent` to ensure all ladders, arenas, and managers are fully initialized before making API calls.

---

## API Structure

`AstralPracticeApi` provides modular access to 9 dedicated sub-APIs:

| Section | Method | Description |
|---|---|---|
| **Players** | `api.getPlayers()` | Practice states, nametags, divisions, daily match limits |
| **Statistics** | `api.getStats()` | Elo ratings, experience, ladder wins/losses, kills, deaths |
| **Matches** | `api.getMatches()` | Live matches, spectator matches, match termination |
| **Parties** | `api.getParties()` | Party membership and leader queries |
| **Queues** | `api.getQueues()` | Matchmaking queue inspection |
| **Ladders** | `api.getLadders()` | Registered ladder names |
| **FFA** | `api.getFfa()` | Free-For-All arena sessions |
| **Events** | `api.getEvents()` | Tournament event sessions |
| **Cosmetics** | `api.getCosmetics()` | Custom cosmetic categories and menu navigation |

---

## Player API (`api.getPlayers()`)

Query practice state, nametags, divisions, and daily match limits:

```java
AstralPracticeApi api = AstralPracticeApi.getInstance();

// Current practice state (OFFLINE, LOBBY, EDITOR, CUSTOM_EDITOR, QUEUE, MATCH, FFA, EVENT, SPECTATE, STAFF_MODE)
PlayerState state = api.getPlayers().getState(player);
PlayerState stateByUuid = api.getPlayers().getState(uuid);

// Player division name (SHORT or FULL)
String divisionShort = api.getPlayers().getDivision(player, DivisionName.SHORT);
String divisionFull = api.getPlayers().getDivision(player, DivisionName.FULL);
String ladderDivision = api.getPlayers().getDivision(player, "boxing", DivisionName.FULL);
String bestDivision = api.getPlayers().getBestDivision(player, DivisionName.FULL);
String matchDivision = api.getPlayers().getMatchDivision(player, DivisionName.FULL);

// Nametag components (Adventure Component prefix/suffix, color, sort priority)
PlayerNametag nametag = api.getPlayers().getNametag(player);
Component prefix = nametag.getPrefix();
NamedTextColor nameColor = nametag.getColorOfName();
Component suffix = nametag.getSuffix();
int priority = nametag.getSortPriority();

// Daily match limits
int rankedLeft = api.getPlayers().getRankedLeft(player);
int unrankedLeft = api.getPlayers().getUnRankedLeft(player);
api.getPlayers().addRanked(player, 5);
api.getPlayers().resetRanked(player);
```

---

## Matches & Gameplay (`api.getMatches()`, `api.getFfa()`, etc.)

Query ongoing gameplay instances and manage matches:

```java
AstralPracticeApi api = AstralPracticeApi.getInstance();

// Current active match (returns null if not in a match)
Match match = api.getMatches().getMatch(player);
Match liveMatch = api.getMatches().getLiveMatchByPlayer(player);

// Spectated match (returns null if not spectating)
Match spectating = api.getMatches().getSpectatingMatch(player);
Match spectatedLiveMatch = api.getMatches().getLiveMatchBySpectator(player);

// Force-end a player's match with a MiniMessage-formatted reason
api.getMatches().endMatch(player, "<red>Match ended by an admin");

// FFA game (returns null if not in FFA)
Ffa ffa = api.getFfa().getFfa(player);

// Event instance (returns null if not in an event)
Event event = api.getEvents().getEvent(player);

// Party instance (returns null if not in a party)
Party party = api.getParties().getParty(player);

// Waiting queue (returns null if not in a queue)
Queue queue = api.getQueues().getQueue(player);
```

---

## Ladders & Statistics (`api.getLadders()`, `api.getStats()`)

```java
AstralPracticeApi api = AstralPracticeApi.getInstance();

// List all registered ladder names
List<String> ladders = api.getLadders().getLadderNames();

// Player ladder kills and deaths (-1 if unavailable)
int kills = api.getStats().getLadderKills(player, "Nodebuff");
int deaths = api.getStats().getLadderDeaths(player, "Nodebuff");

// Elo rating for a ladder
int elo = api.getStats().getElo(player, "Nodebuff");

// Ladder wins and losses by weight class
int rankedWins = api.getStats().getLadderWins(player, "Nodebuff", WeightClass.RANKED);
int unrankedLosses = api.getStats().getLadderLosses(player, "Nodebuff", WeightClass.UNRANKED);

// Global statistics
int globalWins = api.getStats().getGlobalWins(player);
int exp = api.getStats().getExperience(player);
```

---

## Cosmetics API (`api.getCosmetics()`)

Addons can manage custom cosmetic categories in the main cosmetics hub menu (`/cosmetics`) through the dedicated `CosmeticsApi` section:

### Available Methods

| Method | Return Type | Description |
|---|---|---|
| `api.getCosmetics().registerCategory(CosmeticCategory category)` | `void` | Registers a custom category in `/cosmetics` |
| `api.getCosmetics().unregisterCategory(String categoryId)` | `boolean` | Unregisters a category by its ID |
| `api.getCosmetics().unregisterCategory(CosmeticCategory category)` | `boolean` | Unregisters a category instance |
| `api.getCosmetics().getCategories()` | `List<CosmeticCategory>` | Returns all registered custom categories |
| `api.getCosmetics().getCategory(String categoryId)` | `CosmeticCategory` | Retrieves a category by ID (or `null`) |
| `api.getCosmetics().openMenu(Player player)` | `void` | Opens the main `/cosmetics` hub menu for the player |

---

### Quick Example: Register Category & Give Item On Click

Here is a simple example that registers a category in `/cosmetics` and gives the player an item when clicked:

```java
import dev.lokspel.api.AstralPracticeApi;
import dev.lokspel.api.cosmetics.CosmeticCategory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

AstralPracticeApi api = AstralPracticeApi.getInstance();

// 1. Build the category
CosmeticCategory hatCategory = CosmeticCategory.builder("party_hat")
    .icon(new ItemStack(Material.GOLDEN_HELMET)) // Icon shown in /cosmetics
    .slot(20) // Preferred slot (or omit / set to -1 for auto-placement)
    .onClick(player -> {
        // Give the player a cosmetic item when clicked
        player.getInventory().addItem(new ItemStack(Material.GOLDEN_HELMET));
        player.sendMessage("§aYou received a Party Hat!");
        player.closeInventory();
    })
    .build();

// 2. Register the category via the dedicated Cosmetics section
api.getCosmetics().registerCategory(hatCategory);

// 3. Unregister when your addon disables
api.getCosmetics().unregisterCategory("party_hat");
```

### Alternative: Direct Interface Implementation

You can also implement `CosmeticCategory` directly as a dedicated class:

```java
import dev.lokspel.api.cosmetics.CosmeticCategory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class AurasCategory implements CosmeticCategory {

    @Override
    public String getId() {
        return "auras";
    }

    @Override
    public ItemStack getIcon(Player player) {
        return new ItemStack(Material.BEACON);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event) {
        // Open your custom menu for the clicking player
        aurasMenu.open(player);
    }

    @Override
    public int getSlot() {
        return -1; // -1 for automatic centered placement in the hub menu
    }

    @Override
    public boolean isVisible(Player player) {
        return player.hasPermission("addon.auras");
    }
}
```

---

## Interfaces

### `Match`

The `Match` interface (`dev.lokspel.api.interfaces.Match`) represents an ongoing duel or match:

| Method | Return Type | Description |
|---|---|---|
| `getMatchId()` | `String` | Unique match identifier |
| `getArenaName()` | `String` | Name of the arena (or `null`) |
| `isRanked()` | `boolean` | Whether the match is a ranked duel |
| `getDurationSeconds()` | `int` | Elapsed match duration in seconds |
| `getCurrentRoundNumber()` | `int` | Current round number |
| `getLadderName()` | `String` | Identifier of the match ladder |
| `getLadderDisplayName()` | `String` | Display name of the ladder |
| `getWinsNeeded()` | `int` | Number of round wins required to win the match |
| `getPlayers()` | `List<Player>` | Active players in the match |
| `getSpectators()` | `List<Player>` | Spectators watching the match |
| `addSpectator(...)` | `void` | Add a spectator to the match |
| `removeSpectator(Player)` | `void` | Remove a spectator from the match |
| `sendMessage(String, boolean)` | `void` | Broadcast a MiniMessage string to match players (and spectators if flag is true) |

### `Queue`

The `Queue` interface (`dev.lokspel.api.interfaces.Queue`) represents a player waiting in matchmaking:

| Method | Return Type | Description |
|---|---|---|
| `getPlayer()` | `Player` | Player in the queue |
| `getLadderName()` | `String` | Ladder the player queued for |
| `isRanked()` | `boolean` | Whether this is a ranked queue |
| `getDuration()` | `int` | Elapsed time waiting in queue (in seconds) |

### `Party`

The `Party` interface (`dev.lokspel.api.interfaces.Party`) represents a player group:

| Method | Return Type | Description |
|---|---|---|
| `getLeader()` | `Player` | Party leader |
| `getMembers()` | `List<Player>` | All party members (including leader) |

### `Event`

The `Event` interface (`dev.lokspel.api.interfaces.Event`) represents an active server event:

| Method | Return Type | Description |
|---|---|---|
| `getStarter()` | `Object` | Host or starter of the event |
| `getPlayers()` | `List<Player>` | Remaining participants |
| `getSpectators()` | `List<Player>` | Spectators watching the event |
| `getStartPlayerCount()` | `int` | Initial player count when the event began |
| `getWinner()` | `Player` | Winner of the event (or `null`) |

---

## Events

AstralPractice provides Bukkit events under `dev.lokspel.api.event.*` that your plugin can register and handle:

| Event | Package | Description | Cancellable |
|---|---|---|---|
| `AstralPracticeReadyEvent` | `dev.lokspel.api.event` | Fired when AstralPractice has finished initializing and the API is ready | No |
| `EloChangeEvent` | `dev.lokspel.api.event` | Fired when a player's Elo rating is modified (in a duel or by command) | No |
| `MatchStartEvent` | `dev.lokspel.api.event.match` | Fired when a match starts | Yes |
| `MatchEndEvent` | `dev.lokspel.api.event.match` | Fired when a match concludes | No |
| `MatchRoundStartEvent` | `dev.lokspel.api.event.match` | Fired at the beginning of a round in a match | No |
| `MatchRoundEndEvent` | `dev.lokspel.api.event.match` | Fired at the end of a round in a match | No |
| `QueueStartEvent` | `dev.lokspel.api.event.queue` | Fired when a player enters matchmaking queue | Yes |
| `QueueEndEvent` | `dev.lokspel.api.event.queue` | Fired when a player leaves or matches from a queue | No |
| `PartyCreateEvent` | `dev.lokspel.api.event` | Fired when a new party is created | No |
| `FfaRemovePlayerEvent` | `dev.lokspel.api.event` | Fired when a player leaves an FFA arena | No |
| `SpectateStartEvent` | `dev.lokspel.api.event.spectate.start` | Fired when a player starts spectating | Yes |
| `SpectateEndEvent` | `dev.lokspel.api.event.spectate.end` | Fired when a player stops spectating | No |
| `PlayerStateChangeEvent` | `dev.lokspel.api.event` | Fired when a player's practice state changes (e.g. lobby, queue, match) | No |

### Listening to `PlayerStateChangeEvent`

```java
import dev.lokspel.api.enums.PlayerState;
import dev.lokspel.api.event.PlayerStateChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerStateListener implements Listener {

    @EventHandler
    public void onPlayerStateChange(PlayerStateChangeEvent event) {
        Player player = event.getPlayer();
        PlayerState oldState = event.getOldState();
        PlayerState newState = event.getNewState();

        player.sendMessage("State updated from " + oldState + " to " + newState);
    }
}
```

### Listening to `EloChangeEvent`

```java
import dev.lokspel.api.event.EloChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EloListener implements Listener {

    @EventHandler
    public void onEloChange(EloChangeEvent event) {
        Player player = event.getPlayer();
        String ladder = event.getLadderName();
        int oldElo = event.getOldElo();
        int newElo = event.getNewElo();
        int delta = event.getDelta(); // positive or negative change

        // Match may be null if Elo was modified outside a duel (e.g. by admin command)
        if (event.getMatch() != null) {
            String matchId = event.getMatch().getMatchId();
        }
    }
}
```

### Listening to `AstralPracticeReadyEvent`

```java
import dev.lokspel.api.AstralPracticeApi;
import dev.lokspel.api.event.AstralPracticeReadyEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ReadyListener implements Listener {

    @EventHandler
    public void onReady(AstralPracticeReadyEvent event) {
        AstralPracticeApi api = AstralPracticeApi.getInstance();
        // Safe to query ladders, arenas, and stats
    }
}
```
