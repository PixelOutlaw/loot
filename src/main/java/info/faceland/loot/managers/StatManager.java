package info.faceland.loot.managers;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.StatResponse;
import info.faceland.loot.utils.InventoryUtil;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

public class StatManager {

  private final Map<String, ItemStat> itemStats;

  public StatManager() {
    this.itemStats = new HashMap<>();
  }

  public ItemStat getStat(String name) {
    if (!itemStats.containsKey(name)) {
      Bukkit.getLogger().warning("[Loot] ERROR! No stat found for name " + name + "!");
      return null;
    }
    return itemStats.get(name);
  }

  public void addStat(String name, ItemStat itemStat) {
    itemStats.put(name, itemStat);
  }

  public void removeStat(String name) {
    itemStats.remove(name);
  }

  public Map<String, ItemStat> getLoadedStats() {
    return itemStats;
  }

  public StatResponse getFinalStat(ItemStat itemStat, float level, float rarity) {
    return getFinalStat(itemStat, level, rarity, RollStyle.RANDOM);
  }

  public StatResponse getFinalStat(ItemStat itemStat, float level, float rarity, RollStyle style) {
    StatResponse response = new StatResponse();
    TextComponent component = new TextComponent();
    component.setItalic(false);

    float statRoll = switch (style) {
      case MAX -> 1;
      case MIN -> 0;
      case RANDOM -> (float) Math.pow(LootPlugin.RNG.nextFloat(), 2.85);
    };
    String minMaxId = level + "|" + rarity;
    float minVal;
    float maxVal;
    if (itemStat.getMinValues().containsKey(minMaxId)) {
      minVal = itemStat.getMinValues().get(minMaxId);
      maxVal = itemStat.getMaxValues().get(minMaxId);
    } else {
      minVal = itemStat.getValue(level, rarity, 0);
      maxVal = itemStat.getValue(level, rarity, 1);
      itemStat.getMinValues().put(minMaxId, minVal);
      itemStat.getMaxValues().put(minMaxId, maxVal);
    }
    int statValue = (int) itemStat.getValue(level, rarity, statRoll);
    if (statRoll < 0.5 ||
        (itemStat.getMinHue() == itemStat.getMaxHue() && itemStat.getMinBrightness() == itemStat.getMaxBrightness())) {
      component.setColor(InventoryUtil.getRollColor(itemStat, 0f));
      response.setStatRoll(statRoll);
    } else {
      int maxRange = (int) (maxVal - minVal);
      float valueOverMinimum = statValue - minVal;
      float actualRoll = valueOverMinimum / maxRange;
      float scaledRoll = (actualRoll - 0.5f) * 2f;
      float finalRoll = Math.max(0f, Math.min(1f, scaledRoll));
      component.setColor(InventoryUtil.getRollColor(itemStat, finalRoll));
      response.setStatRoll(actualRoll);
    }
    String value = Integer.toString(statValue);
    String statString = itemStat.getStatString().replace("{}", value);
    component.setText(statString);
    response.setStatString(component.toLegacyText());
    if (!itemStat.getNamePrefixes().isEmpty()) {
      response.setStatPrefix(itemStat.getNamePrefixes().get(LootPlugin.RNG.nextInt(itemStat.getNamePrefixes().size())));
    }
    return response;
  }

  public void loadFromFiles(List<SmartYamlConfiguration> files) {
    getLoadedStats().clear();
    for (SmartYamlConfiguration file : files) {
      for (String key : file.getKeys(false)) {
        try {
          if (!file.isConfigurationSection(key)) {
            continue;
          }
          ConfigurationSection cs = file.getConfigurationSection(key);
          ItemStat stat = new ItemStat();
          stat.setMinBaseValue((float) cs.getDouble("min-base-value"));
          stat.setMaxBaseValue((float) cs.getDouble("max-base-value"));
          stat.setPerLevelIncrease((float) cs.getDouble("per-level-increase"));
          stat.setPerLevelMultiplier((float) cs.getDouble("per-level-multiplier"));
          stat.setPerRarityIncrease((float) cs.getDouble("per-rarity-increase"));
          stat.setPerRarityMultiplier((float) cs.getDouble("per-rarity-multiplier"));
          stat.setStatString(cs.getString("stat-string"));
          stat.setMinHue((float) cs.getDouble("min-hue", 0));
          stat.setMaxHue((float) cs.getDouble("max-hue", 0));
          stat.setMinSaturation((float) cs.getDouble("min-saturation", 0.83));
          stat.setMaxSaturation((float) cs.getDouble("max-saturation", 0.83));
          stat.setMinBrightness((float) cs.getDouble("min-brightness", 1));
          stat.setMaxBrightness((float) cs.getDouble("max-brightness", 1));
          stat.setMinimumItemLevel(cs.getInt("minimum-item-level", -10));
          stat.getNamePrefixes().clear();
          stat.getNamePrefixes().addAll(cs.getStringList("prefixes"));
          stat.setCategory(cs.getString("category", null));
          addStat(key, stat);
        } catch (Exception e) {
          Bukkit.getLogger().warning("[Loot] Failed to load stat named " + key + " from file " + file.getName());
          e.printStackTrace();
        }
      }
    }
    Bukkit.getLogger().info("[Loot] Loaded " + getLoadedStats().size() + " stats from " + files.size() + " files");
  }

  public enum RollStyle {
    MAX,
    MIN,
    RANDOM
  }
}
