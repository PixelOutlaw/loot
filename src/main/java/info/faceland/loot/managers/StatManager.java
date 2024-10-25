package info.faceland.loot.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.StatResponse;
import info.faceland.loot.utils.InventoryUtil;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.stats.StrifeStat;
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

  public StatResponse getFinalStat(ItemStat itemStat, float level, float rarity, float multiplier, float flatBonus) {
    return getFinalStat(itemStat, level, rarity, RollStyle.RANDOM, multiplier, flatBonus);
  }

  public StatResponse getFinalStat(ItemStat itemStat, float level, float rarity, RollStyle style,
      float multiplier, float flatBonus) {
    float statRoll = switch (style) {
      case MAX -> 1;
      case MIN -> 0;
      case RANDOM -> (float) Math.pow(LootPlugin.RNG.nextFloat(), 2.85);
    };
    return getFinalStat(itemStat, level, rarity, statRoll, multiplier, flatBonus);
  }

  public StatResponse getFinalStat(ItemStat itemStat, float level, float rarity, float statRoll,
      float multiplier, float flatBonus) {
    StatResponse response = new StatResponse();
    TextComponent component = new TextComponent();
    component.setItalic(false);

    int minVal = (int) Math.floor(itemStat.getValue(level, rarity, 0));
    int maxVal = (int) Math.floor(itemStat.getValue(level, rarity, 1));
    int statValue = (int) Math.floor(itemStat.getValue(level, rarity, statRoll));
    statValue = statValue == 0 ? 1 : statValue;

    if (statValue < 0) {
      component.setColor(FaceColor.RED.getColor());
      response.setStatRoll(statRoll);
    } else if (minVal == maxVal || statValue >= maxVal) {
      component.setColor(InventoryUtil.getRollColor(itemStat, 1));
      response.setStatRoll(1);
    } else if (statRoll < 0.5 || !itemStat.isHasColorRange()) {
      component.setColor(InventoryUtil.getRollColor(itemStat, 0f));
      response.setStatRoll(statRoll);
    } else {
      float scaledRoll = (statRoll - 0.5f) * 2f;
      //float finalRoll = Math.max(0f, Math.min(1f, scaledRoll));
      component.setColor(InventoryUtil.getRollColor(itemStat, scaledRoll));
      response.setStatRoll(statRoll);
    }
    float rawStatValue = statValue;
    statValue = (int) Math.floor(((float) statValue * multiplier) + flatBonus);
    statValue = statValue == 0 ? 1 : statValue;

    String value = Integer.toString(statValue);
    if (itemStat.getStat() != null) {
      if (statValue >= 0) {
        component.setText("+" + value + (itemStat.isPercentage() ? "% " : " ") + itemStat.getStat().getName());
      } else {
        component.setText(value + (itemStat.isPercentage() ? "% " : " ") + itemStat.getStat().getName());
      }
    } else {
      component.setColor(FaceColor.CRIMSON.getColor());
      component.setText("UNKNOWN STAT");
    }
    response.setStatString(component.toLegacyText());
    if (!itemStat.getNamePrefixes().isEmpty()) {
      response.setStatPrefix(itemStat.getNamePrefixes().get(LootPlugin.RNG.nextInt(itemStat.getNamePrefixes().size())));
    }
    response.setValue(statValue);
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
          ItemStat stat = new ItemStat(key);
          stat.setMinBaseValue((float) cs.getDouble("min-base-value"));
          stat.setMaxBaseValue((float) cs.getDouble("max-base-value"));
          stat.setPerLevelIncrease((float) cs.getDouble("per-level-increase"));
          stat.setPerLevelMultiplier((float) cs.getDouble("per-level-multiplier"));
          stat.setPerRarityIncrease((float) cs.getDouble("per-rarity-increase"));
          stat.setPerRarityMultiplier((float) cs.getDouble("per-rarity-multiplier"));
          stat.setMinHue((float) cs.getDouble("min-hue", 0));
          stat.setMaxHue((float) cs.getDouble("max-hue", 0));
          stat.setMinSaturation((float) cs.getDouble("min-saturation", 0.83));
          stat.setMaxSaturation((float) cs.getDouble("max-saturation", 0.83));
          stat.setMinBrightness((float) cs.getDouble("min-brightness", 1));
          stat.setMaxBrightness((float) cs.getDouble("max-brightness", 1));
          stat.setMinimumItemLevel(cs.getInt("minimum-item-level", -10));
          stat.getNamePrefixes().clear();
          stat.getNamePrefixes().addAll(cs.getStringList("prefixes"));
          String statStr = cs.getString("stat", "");
          if (!statStr.isEmpty()) {
            stat.setStat(StrifeStat.valueOf(statStr));
          }
          stat.setPercentage(cs.getBoolean("percentage", false));
          stat.calcColorRange();
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
