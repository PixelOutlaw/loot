package info.faceland.loot.data;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ItemStat {

  private double minBaseValue;
  private double maxBaseValue;
  private double perLevelIncrease;
  private double perLevelMultiplier;
  private double perRarityIncrease;
  private double perRarityMultiplier;
  private String statString;
  private String statPrefix;
  private String perfectStatPrefix;
  private String specialStatPrefix;
  private float minHue;
  private float maxHue;
  private float minSaturation;
  private float maxSaturation;
  private float minBrightness;
  private float maxBrightness;
  private final List<String> namePrefixes = new ArrayList<>();
  private int minimumItemLevel;
}
