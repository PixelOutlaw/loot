package info.faceland.loot.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemStat {

  public static Map<StrifeStat, Set<String>> LIMIT_CATEGORY_IDS = new HashMap<>();
  public static Map<String, Set<StrifeStat>> LIMIT_CATEGORY_STATS = new HashMap<>();

  private String id;

  private float minBaseValue;
  private float maxBaseValue;
  private float perLevelIncrease;
  private float perLevelMultiplier;
  private float perRarityIncrease;
  private float perRarityMultiplier;

  private final List<String> namePrefixes = new ArrayList<>();
  private int minimumItemLevel;
  private boolean hide;
  private StrifeStat stat;
  private boolean percentage;

  // Coloring
  private float minHue;
  private float maxHue;
  private float minSaturation;
  private float maxSaturation;
  private float minBrightness;
  private float maxBrightness;

  private boolean hasColorRange;

  public ItemStat(String id) {
    this.id = id;
  }

  public void calcColorRange() {
    hasColorRange = !(minHue == maxHue && minBrightness == maxBrightness && minSaturation == maxSaturation);
  }

  public float getValue(float level, float rarity, float roll) {
    float statValue = minBaseValue + roll * (maxBaseValue - minBaseValue);
    statValue += level * perLevelIncrease;
    float multiplier = 1 + (level * perLevelMultiplier) + (rarity * perRarityMultiplier);
    statValue *= multiplier;
    statValue += rarity * perRarityIncrease;
    return statValue;
  }
}
