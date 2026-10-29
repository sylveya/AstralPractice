package dev.lokspel.practice.manager.arena;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

/**
 * The file {@code arenas/<name>.yml}. Normal and FFA arenas share it; each type writes only its own
 * settings and keeps those of the other type it finds.
 */
@Getter
@Setter
@NoArgsConstructor
public class ArenaData extends ArenaLayoutData {

  @JsonProperty("name")
  private String name;

  @JsonProperty("type")
  private String type;

  @JsonProperty("enabled")
  private Boolean enabled;

  @JsonProperty("icon")
  private ItemStack icon;

  @JsonProperty("ladders")
  private List<String> ladders;

  // Normal arenas
  @JsonProperty("ladderTypes")
  private List<String> ladderTypes;

  @JsonProperty("allowCustomKitOnMap")
  private Boolean allowCustomKitOnMap;

  @JsonProperty("locatorBar")
  private Boolean locatorBar;

  @JsonProperty("sideBuildLimit")
  private Integer sideBuildLimit;

  @JsonProperty("copies")
  private Map<String, ArenaLayoutData> copies;

  // FFA arenas
  @JsonProperty("build")
  private Boolean build;

  @JsonProperty("reKitAfterKill")
  private Boolean reKitAfterKill;

  @JsonProperty("lobbyAfterDeath")
  private Boolean lobbyAfterDeath;

  @JsonProperty("healthResetOnKill")
  private Boolean healthResetOnKill;

  @JsonProperty("healthBelowName")
  private Boolean healthBelowName;

  @JsonProperty("mapBlowable")
  private Boolean mapBlowable;

  @JsonProperty("allowDropItems")
  private Boolean allowDropItems;
}
