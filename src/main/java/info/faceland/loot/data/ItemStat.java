package info.faceland.loot.data;

import java.util.ArrayList;
import java.util.List;

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

  public double getMinBaseValue() {
    return minBaseValue;
  }

  public void setMinBaseValue(double minBaseValue) {
    this.minBaseValue = minBaseValue;
  }

  public double getMaxBaseValue() {
    return maxBaseValue;
  }

  public void setMaxBaseValue(double maxBaseValue) {
    this.maxBaseValue = maxBaseValue;
  }

  public double getPerLevelIncrease() {
    return perLevelIncrease;
  }

  public void setPerLevelIncrease(double perLevelIncrease) {
    this.perLevelIncrease = perLevelIncrease;
  }

  public double getPerLevelMultiplier() {
    return perLevelMultiplier;
  }

  public void setPerLevelMultiplier(double perLevelMultiplier) {
    this.perLevelMultiplier = perLevelMultiplier;
  }

  public double getPerRarityIncrease() {
    return perRarityIncrease;
  }

  public void setPerRarityIncrease(double perRarityIncrease) {
    this.perRarityIncrease = perRarityIncrease;
  }

  public double getPerRarityMultiplier() {
    return perRarityMultiplier;
  }

  public void setPerRarityMultiplier(double perRarityMultiplier) {
    this.perRarityMultiplier = perRarityMultiplier;
  }

  public String getStatString() {
    return statString;
  }

  public void setStatString(String statString) {
    this.statString = statString;
  }

  public String getStatPrefix() {
    return statPrefix;
  }

  public void setStatPrefix(String statPrefix) {
    this.statPrefix = statPrefix;
  }

  public String getPerfectStatPrefix() {
    return perfectStatPrefix;
  }

  public void setPerfectStatPrefix(String perfectStatPrefix) {
    this.perfectStatPrefix = perfectStatPrefix;
  }

  public String getSpecialStatPrefix() {
    return specialStatPrefix;
  }

  public void setSpecialStatPrefix(String specialStatPrefix) {
    this.specialStatPrefix = specialStatPrefix;
  }

  public float getMinHue() {
    return minHue;
  }

  public void setMinHue(float minHue) {
    this.minHue = minHue;
  }

  public float getMaxHue() {
    return maxHue;
  }

  public void setMaxHue(float maxHue) {
    this.maxHue = maxHue;
  }

  public float getMinSaturation() {
    return minSaturation;
  }

  public void setMinSaturation(float minSaturation) {
    this.minSaturation = minSaturation;
  }

  public float getMaxSaturation() {
    return maxSaturation;
  }

  public void setMaxSaturation(float maxSaturation) {
    this.maxSaturation = maxSaturation;
  }

  public float getMinBrightness() {
    return minBrightness;
  }

  public void setMinBrightness(float minBrightness) {
    this.minBrightness = minBrightness;
  }

  public float getMaxBrightness() {
    return maxBrightness;
  }

  public void setMaxBrightness(float maxBrightness) {
    this.maxBrightness = maxBrightness;
  }

  public List<String> getNamePrefixes() {
    return namePrefixes;
  }
}
