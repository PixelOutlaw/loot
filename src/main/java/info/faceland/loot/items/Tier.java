/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.items;

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.utils.ModifyEquipmentUtil;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.market.data.PlayerMarketState.FilterFlagA;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public final class Tier {
  
  private static final Map<String, Tier> TIERS = new HashMap<>();

  private boolean skillRequirement = false;
  private String id;
  private String plainName;
  private Material material;
  private List<ItemStat> primaryStats;
  private List<ItemStat> secondaryStats;
  private List<ItemStat> bonusStats;
  private Map<StrifeStat, Float> hiddenStats;
  private Map<StrifeStat, Float> weaponMods;
  private List<String> essenceGroup;
  private int upgradeScale;
  private double spawnWeight;
  private double identifyWeight;
  private int customDataStart;
  private int customDataInterval;
  private int essenceId;
  private FilterFlagA filterFlag;
  private Map<String, Integer> statCategoryLimits = new HashMap<>();

  private int extraSockets;

  private List<Material> craftMaterials;

  private Map<ItemRarity, List<String>> suffixes = new HashMap<>();

  public Tier(String id) {
    this.id = id;
    this.secondaryStats = new ArrayList<>();
    this.bonusStats = new ArrayList<>();
  }

  public static Tier get(String id) {
    return TIERS.getOrDefault(id, null);
  }

  public static Tier get(ItemStack stack) {
    return ModifyEquipmentUtil.getTier(stack);
  }

  public static Tier determine(ItemStack stack) {
    if (stack == null) {
      return null;
    }
    Material material = stack.getType();
    int data = ItemUtils.getModelData(stack);
    for (var t : TIERS.values()) {
      int start = t.getCustomDataStart();
      int max = start + 100 / t.getCustomDataInterval();
      if (t.getMaterial() == material && data >= start && data <= max) {
        return t;
      }
    }
    return null;
  }

  public static Tier getRandomTier() {
    double selectedWeight = LootPlugin.RNG.nextFloat() * getTotalTierWeight();
    double currentWeight = 0;
    for (Tier t : TIERS.values()) {
      double calcWeight = t.getSpawnWeight();
      if (calcWeight >= 0) {
        currentWeight += calcWeight;
      }
      if (currentWeight >= selectedWeight) {
        return t;
      }
    }
    return null;
  }

  public static Set<Tier> get() {
    return new HashSet<>(TIERS.values());
  }

  public static Set<String> getIds() {
    return new HashSet<>(TIERS.keySet());
  }

  private static double getTotalTierWeight() {
    double weight = 0;
    for (Tier t : TIERS.values()) {
      double d = t.getSpawnWeight();
      if (d > 0D) {
        weight += d;
      }
    }
    return weight;
  }

  public List<String> getItemSuffixes(ItemRarity rarity) {
    return suffixes.getOrDefault(rarity, Collections.emptyList());
  }

  public void addItemSuffixes(ItemRarity rarity, List<String> list) {
    suffixes.put(rarity, list);
  }

  public static void loadFromFile(LootPlugin plugin, SmartYamlConfiguration tierYAML) {
    TIERS.clear();
    for (String key : tierYAML.getKeys(false)) {
      if (!tierYAML.isConfigurationSection(key)) {
        continue;
      }
      try {
        ConfigurationSection cs = tierYAML.getConfigurationSection(key);
        Tier tier = new Tier(key);
        tier.setPlainName(cs.getString("plain-name"));
        tier.setMaterial(Material.valueOf(cs.getString("material", "APPLE")));
        tier.setSkillRequirement(cs.getBoolean("skill-req", false));
        List<ItemStat> primaryStats = new ArrayList<>();
        for (String statName : cs.getStringList("primary-stats")) {
          primaryStats.add(plugin.getStatManager().getStat(statName));
        }
        tier.setPrimaryStats(primaryStats);

        List<ItemStat> secondaryStats = new ArrayList<>();
        for (String statName : cs.getStringList("secondary-stats")) {
          secondaryStats.add(plugin.getStatManager().getStat(statName));
        }
        tier.setSecondaryStats(secondaryStats);
        tier.setHiddenStats(StatUtil.getStatMapFromSection(cs.getConfigurationSection("hidden-stats")));
        tier.setWeaponMods(StatUtil.getStatMapFromSection(cs.getConfigurationSection("weapon-mods")));
        List<ItemStat> bonusStats = new ArrayList<>();
        for (String statName : cs.getStringList("bonus-stats")) {
          bonusStats.add(plugin.getStatManager().getStat(statName));
        }
        tier.setBonusStats(bonusStats);
        tier.setSpawnWeight(cs.getDouble("spawn-weight"));
        tier.setUpgradeScale(cs.getInt("upgrade-scale", 1));
        tier.setIdentifyWeight(cs.getDouble("identify-weight"));
        tier.setCustomDataStart(cs.getInt("base-custom-data", -1));
        tier.setCustomDataInterval(cs.getInt("custom-data-level-interval", 200));
        tier.setEssenceId(cs.getInt("essence-id", 0));
        tier.setEssenceGroup(cs.getStringList("essence-group"));
        tier.setExtraSockets(cs.getInt("extra-sockets", 0));
        List<Material> mats = new ArrayList<>();
        for (var s : cs.getStringList("craft-materials")) {
          try {
            mats.add(Material.valueOf(s));
          } catch (Exception ignored) {
          }
        }
        tier.setCraftMaterials(mats);
        if (cs.getConfigurationSection("stat-category-limits") != null) {
          Map<String, Integer> limitMap = new HashMap<>();
          for (String category : cs.getConfigurationSection("stat-category-limits").getKeys(false)) {
            limitMap.put(category, cs.getInt("stat-category-limits." + category));
          }
          tier.setStatCategoryLimits(limitMap);
        }

        List<String> suffixes = cs.getStringList("suffixes.generic");
        for (ItemRarity r : plugin.getRarityManager().getLoadedRarities().values()) {
          List<String> combined = new ArrayList<>(suffixes);
          combined.addAll(cs.getStringList("suffixes." + r.getId()));
          tier.addItemSuffixes(r, combined);
        }

        String marketFilterFlag = cs.getString("filter-flag", "ALL");
        tier.setFilterFlag(FilterFlagA.valueOf(marketFilterFlag));

        TIERS.put(key, tier);
      } catch (Exception e) {
        Bukkit.getLogger().info("[Loot] Failed to load tier " + key);
        e.printStackTrace();
      }
    }
    Bukkit.getLogger().info("[Loot] Loaded " + TIERS.size() + " tiers!");
  }
}
