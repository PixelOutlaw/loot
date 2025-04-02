package info.faceland.loot.data.pdc;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemStat;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoredStat implements Serializable {

  private Map<String, String> stringData = new HashMap<>();
  private Map<String, Integer> intData = new HashMap<>();
  private Map<String, Double> doubleData = new HashMap<>();
  private Map<String, Boolean> booleanData = new HashMap<>();

  private transient ItemStat stat;

  public StoredStat(String statId, double statRoll) {
    stringData.put("statId", statId);
    stringData.put("originalStatId", statId);
    stat = LootPlugin.getInstance().getStatManager().getStat(statId);
    doubleData.put("statRoll", statRoll);
    booleanData.put("essence", false);
  }

  public StoredStat(String statId, double statRoll, boolean essenceSlot) {
    stringData.put("statId", statId);
    stat = LootPlugin.getInstance().getStatManager().getStat(statId);
    doubleData.put("statRoll", statRoll);
    booleanData.put("essence", essenceSlot);
  }

  public ItemStat getStat() {
    if (stat == null) {
      stat = LootPlugin.getInstance().getStatManager().getStat(getString("statId"));
    }
    return stat;
  }

  public void setStat(String statId) {
    stringData.put("statId", statId);
    stat = LootPlugin.getInstance().getStatManager().getStat(statId);
  }

  public double getStatRoll() {
    return doubleData.getOrDefault("statRoll", 0D);
  }

  public void setStatRoll(double statRoll) {
    doubleData.put("statRoll", statRoll);
  }

  public StoredStat setEssenceSlot(boolean val) {
    if (val) {
      StoredStat removed = new StoredStat(stringData.get("statId"), doubleData.get("statRoll"));
      removed.copyFrom(this);
      setBoolean("essence", true);
      setCrafted(false);
      setDistorted(false);
      setCachedValue(0);
      return removed;
    } else {
      setBoolean("essence", false);
      return null;
    }
  }

  public boolean isEssenceSlot() {
    return getBoolean("essence");
  }

  public boolean isDistorted() {
    return getBoolean("distorted");
  }

  public void setDistorted(boolean distorted) {
    setBoolean("distorted", distorted);
  }

  public boolean isInverted() {
    return getBoolean("inverted");
  }

  public void setInverted(boolean inverted) {
    setBoolean("inverted", inverted);
  }

  public void setCrafted(boolean crafted) {
    setBoolean("crafted", crafted);
  }

  public boolean isCrafted() {
    return getBoolean("crafted", false);
  }

  public String getOriginTier() {
    return stringData.getOrDefault("tier", "");
  }

  public void setOriginTier(String tier) {
    stringData.put("tier", tier);
  }

  public String getOriginalStatId() {
    return stringData.getOrDefault("originalStatId", "");
  }

  public int getLevel() {
    return intData.getOrDefault("level", 0);
  }

  public int getLevel(int fallback) {
    return intData.getOrDefault("level", fallback);
  }

  public void setLevel(int value) {
    intData.put("level", value);
  }

  public int getEssenceLevel(int fallback) {
    return intData.getOrDefault("essenceLevel", fallback);
  }

  public void setEssenceLevel(int value) {
    intData.put("essenceLevel", Math.max(1, value));
  }

  public double getRarityPower() {
    return doubleData.getOrDefault("rarityPower", 0D);
  }

  public double getRarityPower(double fallback) {
    return doubleData.getOrDefault("rarityPower", fallback);
  }

  public void setRarityPower(double value) {
    doubleData.put("rarityPower", value);
  }

  public double getUniqueMult() {
    return doubleData.getOrDefault("uniqueMult", 0D);
  }

  public double getUniqueMult(double fallback) {
    return doubleData.getOrDefault("uniqueMult", fallback);
  }

  public void setUniqueMult(double value, String id) {
    doubleData.put("uniqueMult", value);
    stringData.put("originUnique", id);
  }

  public String getOriginUnique() {
    return stringData.getOrDefault("originUnique", "");
  }

  public double getSortOrder() {
    return doubleData.getOrDefault("sortOrder", 0D);
  }

  public void setSortOrder(double value) {
    doubleData.put("sortOrder", value);
  }

  public int getCachedValue() {
    return intData.getOrDefault("cachedValue", 0);
  }

  public void setCachedValue(int value) {
    //System.out.println("val: " + value);
    intData.put("cachedValue", value);
  }

  public String getString(String key) {
    return stringData.getOrDefault(key, "");
  }

  private String getString(String key, String fallback) {
    return stringData.getOrDefault(key, fallback);
  }

  private void setString(String key, String str) {
    stringData.put(key, str);
  }

  private int getInt(String key) {
    return intData.getOrDefault(key, 0);
  }

  private int getInt(String key, int fallback) {
    return intData.getOrDefault(key, fallback);
  }

  private void setInt(String key, int val) {
    intData.put(key, val);
  }

  private double getDouble(String key) {
    return doubleData.getOrDefault(key, 0D);
  }

  private double getDouble(String key, double fallback) {
    return doubleData.getOrDefault(key, fallback);
  }

  private void setDouble(String key, double val) {
    doubleData.put(key, val);
  }

  private boolean getBoolean(String key) {
    return booleanData.getOrDefault(key, false);
  }

  private boolean getBoolean(String key, boolean fallback) {
    return booleanData.getOrDefault(key, fallback);
  }

  private void setBoolean(String key, boolean val) {
    booleanData.put(key, val);
  }

  public void copyFrom(StoredStat oldStat) {
    String originalStatId = getOriginalStatId();
    booleanData.clear();
    booleanData.putAll(oldStat.getBooleanData());
    stringData.clear();
    stringData.putAll(oldStat.getStringData());
    intData.clear();
    intData.putAll(oldStat.getIntData());
    doubleData.clear();
    doubleData.putAll(oldStat.getDoubleData());
    stringData.put("originalStatId", originalStatId);
    stat = LootPlugin.getInstance().getStatManager().getStat(getString("statId"));
  }

}
