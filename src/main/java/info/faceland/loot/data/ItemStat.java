package info.faceland.loot.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ItemStat {

  private float minBaseValue;
  private float maxBaseValue;
  private float perLevelIncrease;
  private float perLevelMultiplier;
  private float perRarityIncrease;
  private float perRarityMultiplier;
  private String statString;
  private String specialStatPrefix;
  private float minHue;
  private float maxHue;
  private float minSaturation;
  private float maxSaturation;
  private float minBrightness;
  private float maxBrightness;
  private final List<String> namePrefixes = new ArrayList<>();
  private int minimumItemLevel;
  private String category;

  private Map<String, Float> minValues = new HashMap<>();
  private Map<String, Float> maxValues = new HashMap<>();

  public float getValue(float level, float rarity, float roll) {
    float statValue;
    if (minBaseValue >= maxBaseValue) {
      statValue = minBaseValue;
    } else {
      statValue = minBaseValue + roll * (maxBaseValue - minBaseValue);
    }
    statValue += level * perLevelIncrease;
    statValue += rarity * perRarityIncrease;
    float multiplier = 1 + (level * perLevelMultiplier) + (rarity * perRarityMultiplier);
    statValue *= multiplier;
    return statValue;
  }
}
