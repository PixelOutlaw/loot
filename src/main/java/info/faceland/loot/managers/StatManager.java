package info.faceland.loot.managers;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.api.items.CustomItemBuilder;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.StatResponse;
import info.faceland.loot.items.LootCustomItemBuilder;
import info.faceland.loot.math.LootRandom;
import info.faceland.loot.utils.InventoryUtil;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;

public class StatManager {

  private final Map<String, ItemStat> itemStats;
  private final LootRandom random;

  public StatManager() {
    this.itemStats = new HashMap<>();
    this.random = new LootRandom();
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

  public StatResponse getFinalStat(ItemStat itemStat, double level, double rarity, boolean special) {
    return getFinalStat(itemStat, level, rarity, special, RollStyle.RANDOM);
  }

  public StatResponse getFinalStat(ItemStat itemStat, double level, double rarity, boolean special,
      RollStyle style) {
    StatResponse response = new StatResponse();
    double statValue;
    float statRoll;
    if (itemStat.getMinBaseValue() >= itemStat.getMaxBaseValue()) {
      statValue = itemStat.getMinBaseValue();
      statRoll = 0;
    } else {
      statRoll = switch (style) {
        case MAX -> 1;
        case MIN -> 0;
        case RANDOM -> (float) Math.pow(random.nextDouble(), 2.75);
      };
      statValue = itemStat.getMinBaseValue() + statRoll * (itemStat.getMaxBaseValue() - itemStat.getMinBaseValue());
    }
    response.setStatRoll(statRoll);

    statValue += level * itemStat.getPerLevelIncrease();
    statValue += rarity * itemStat.getPerRarityIncrease();

    double multiplier = 1 + (level * itemStat.getPerLevelMultiplier()) + (rarity * itemStat.getPerRarityMultiplier());
    statValue *= multiplier;

    TextComponent component = new TextComponent();
    component.setItalic(false);
    if (special) {
      component.setColor(ChatColor.of(itemStat.getSpecialStatPrefix()));
      component.setObfuscated(true);
    } else {
      if (StringUtils.isNotBlank(itemStat.getStatPrefix())) {
        if (statRoll >= 0.9) {
          component.setColor(ChatColor.of(itemStat.getPerfectStatPrefix()));
        } else {
          component.setColor(ChatColor.of(itemStat.getStatPrefix()));
        }
      } else {
        double roll = statRoll;
        if (roll < 0.92) {
          roll = Math.max(0, (roll - 0.5) * 2);
        } else {
          roll = 1;
        }
        component.setColor(InventoryUtil.getRollColor(itemStat, roll));
      }
    }

    String value = Integer.toString((int) statValue);
    String statString = itemStat.getStatString().replace("{}", value);
    component.setText(statString);
    response.setStatString(component.toLegacyText());

    if (!itemStat.getNamePrefixes().isEmpty()) {
      response.setStatPrefix(itemStat.getNamePrefixes().get(random.nextInt(itemStat.getNamePrefixes().size())));
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
          stat.setMinBaseValue(cs.getDouble("min-base-value"));
          stat.setMaxBaseValue(cs.getDouble("max-base-value"));
          stat.setPerLevelIncrease(cs.getDouble("per-level-increase"));
          stat.setPerLevelMultiplier(cs.getDouble("per-level-multiplier"));
          stat.setPerRarityIncrease(cs.getDouble("per-rarity-increase"));
          stat.setPerRarityMultiplier(cs.getDouble("per-rarity-multiplier"));
          stat.setStatString(cs.getString("stat-string"));
          stat.setStatPrefix(cs.getString("stat-prefix"));
          stat.setPerfectStatPrefix(cs.getString("perfect-stat-prefix", stat.getStatPrefix()));
          stat.setSpecialStatPrefix(cs.getString("special-stat-prefix", stat.getStatPrefix()));
          stat.setMinHue((float) cs.getDouble("min-hue", 0));
          stat.setMaxHue((float) cs.getDouble("max-hue", 0));
          stat.setMinSaturation((float) cs.getDouble("min-saturation", 0.83));
          stat.setMaxSaturation((float) cs.getDouble("max-saturation", 0.83));
          stat.setMinBrightness((float) cs.getDouble("min-brightness", 1));
          stat.setMaxBrightness((float) cs.getDouble("max-brightness", 1));
          stat.getNamePrefixes().clear();
          stat.getNamePrefixes().addAll(cs.getStringList("prefixes"));
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
