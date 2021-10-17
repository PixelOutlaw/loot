package info.faceland.loot.managers;

import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.StatResponse;
import info.faceland.loot.math.LootRandom;
import info.faceland.loot.utils.InventoryUtil;
import java.util.HashMap;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;

public class StatManager {

  private final Map<String, ItemStat> itemStats;
  private final LootRandom random;

  public StatManager() {
    this.itemStats = new HashMap<>();
    this.random = new LootRandom();
  }

  public ItemStat getStat(String name) {
    if (!itemStats.containsKey(name)) {
      System.out.println("ERROR! No stat found for name " + name + "!");
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
        case RANDOM -> (float) Math.pow(random.nextDouble(), 2.5);
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

  public enum RollStyle {
    MAX,
    MIN,
    RANDOM
  }
}
