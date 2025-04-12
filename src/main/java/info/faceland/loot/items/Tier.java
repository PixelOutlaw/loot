/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.items;

import com.destroystokyo.paper.Namespaced;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.tuple.Pair;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.ModifyEquipmentUtil;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import land.face.market.data.PlayerMarketState.FilterFlagA;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.scheduler.BukkitTask;

@Getter
@Setter
public final class Tier {

  private static final Map<String, Tier> TIERS = new HashMap<>();
  public static final Map<Recipe, Tier> RECIPES = new HashMap<>();
  public static final Set<Material> EVERY_MAT = new HashSet<>();
  public static final Map<Integer, Double> SPAWN_WEIGHT_BY_LEVEL = new HashMap<>();

  private boolean skillRequirement = false;
  private String id;
  private String plainName;
  private Material material;
  private List<ItemStat> primaryStats;
  private List<ItemStat> secondaryStats;
  private List<ItemStat> bonusStats;
  private Map<StrifeStat, Float> hiddenStats;
  private Map<StrifeStat, Float> weaponMods;
  private Set<Namespaced> destroyKeys;
  private List<String> essenceGroup;
  private int upgradeScale;
  private double spawnWeight;
  private int essenceId;
  private int minItemLevel;
  private FilterFlagA filterFlag;
  private Map<String, Integer> statCategoryLimits = new HashMap<>();
  private Map<String, Integer> minimumSocketsByRarity = new HashMap<>();
  private Map<String, Integer> maximumSocketsByRarity = new HashMap<>();

  private List<Material> craftMaterials;
  private List<String> shape;
  private List<Pair<Character, Material>> materials;

  private Map<ItemRarity, List<String>> suffixes = new HashMap<>();

  private List<SocketGem> randomGemOptions = new ArrayList<>();
  private List<EnchantmentTome> randomTomeOptions = new ArrayList<>();

  public Tier(String id) {
    this.id = id;
    this.secondaryStats = new ArrayList<>();
    this.bonusStats = new ArrayList<>();
  }

  public static Tier get(String id) {
    return TIERS.getOrDefault(id, null);
  }

  public static Tier get(@Nullable ItemStack stack) {
    return ModifyEquipmentUtil.getTier(stack);
  }

  public static Tier getRandomTier() {
    return getRandomTier(50);
  }

  public static Tier getRandomTier(int level) {
    double selectedWeight = LootPlugin.RNG.nextFloat() * SPAWN_WEIGHT_BY_LEVEL.get(level);
    double currentWeight = 0;
    for (Tier t : TIERS.values()) {
      if (level < t.getMinItemLevel()) {
        continue;
      }
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

  private static double getTotalTierWeight(int itemLevel) {
    double weight = 0;
    for (Tier t : TIERS.values()) {
      if (itemLevel < t.getMinItemLevel()) {
        continue;
      }
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

  public int getMinSockets(String rarity) {
    return minimumSocketsByRarity.getOrDefault(rarity, 0);
  }

  public int getMaxSockets(String rarity) {
    return maximumSocketsByRarity.getOrDefault(rarity, 0);
  }

  public static void generateRandomUpgradeOptions(SmartYamlConfiguration config) {
    var autoGemSection = config.getConfigurationSection("auto-gem-options");
    var autoTomeSection = config.getConfigurationSection("auto-tome-options");
    for (var tier : TIERS.values()) {
      tier.getRandomGemOptions().clear();
      tier.getRandomTomeOptions().clear();

      for (String groupId : autoGemSection.getKeys(false)) {
        var group = ItemGroup.get(groupId);
        if (group == null) {
          Bukkit.getLogger().info("[Loot] Auto gem group option " + groupId + " does not exist!");
          continue;
        }
        if (group.getValidTiers().contains(tier.getId())) {
          for (String gemId : autoGemSection.getStringList(groupId)) {
            var gem = SocketGem.get(gemId);
            if (gem == null) {
              Bukkit.getLogger().info("[Loot] Auto gem option " + gemId + " does not exist for group " + groupId);
              continue;
            }
            tier.getRandomGemOptions().add(gem);
          }
        }
      }

      for (String groupId : autoTomeSection.getKeys(false)) {
        var group = ItemGroup.get(groupId);
        if (group == null) {
          Bukkit.getLogger().info("[Loot] Auto tome group option " + groupId + " does not exist!");
          continue;
        }
        if (group.getValidTiers().contains(tier.getId())) {
          for (String tomeId : autoTomeSection.getStringList(groupId)) {
            var tome = EnchantmentTome.get(tomeId);
            if (tome == null) {
              Bukkit.getLogger().info("[Loot] Auto tome option " + tomeId + " does not exist for group " + groupId);
              continue;
            }
            tier.getRandomTomeOptions().add(tome);
          }
        }
      }
    }
  }

  public static void loadFromFile(LootPlugin plugin, File dataFolder) {
    TIERS.clear();
    RECIPES.clear();
    EVERY_MAT.clear();
    SPAWN_WEIGHT_BY_LEVEL.clear();
    Map<String, SmartYamlConfiguration> equipmentTypes = new HashMap<>();
    File folder = new File(dataFolder, "equipment_types");
    File[] listOfFiles = folder.listFiles();
    for (File f : Objects.requireNonNull(listOfFiles)) {
      equipmentTypes.put(f.getName().replace(".yml", ""), new SmartYamlConfiguration(f));
    }
    for (var entry : equipmentTypes.entrySet()) {
      SmartYamlConfiguration tierYAML = entry.getValue();
      String key = entry.getKey();
      try {
        Tier tier = new Tier(key);
        tier.setPlainName(tierYAML.getString("plain-name"));
        tier.setMaterial(Material.valueOf(tierYAML.getString("material", "APPLE")));
        EVERY_MAT.add(tier.material);
        tier.setSkillRequirement(tierYAML.getBoolean("skill-req", false));
        List<ItemStat> primaryStats = new ArrayList<>();
        for (String statName : tierYAML.getStringList("primary-stats")) {
          primaryStats.add(plugin.getStatManager().getStat(statName));
        }
        tier.setPrimaryStats(primaryStats);

        List<ItemStat> secondaryStats = new ArrayList<>();
        for (String statName : tierYAML.getStringList("secondary-stats")) {
          secondaryStats.add(plugin.getStatManager().getStat(statName));
        }
        tier.setSecondaryStats(secondaryStats);
        tier.setHiddenStats(StatUtil.getStatMapFromSection(tierYAML.getConfigurationSection("hidden-stats")));
        tier.setWeaponMods(StatUtil.getStatMapFromSection(tierYAML.getConfigurationSection("weapon-mods")));
        List<ItemStat> bonusStats = new ArrayList<>();
        for (String statName : tierYAML.getStringList("bonus-stats")) {
          bonusStats.add(plugin.getStatManager().getStat(statName));
        }
        tier.setBonusStats(bonusStats);
        tier.setSpawnWeight(tierYAML.getDouble("spawn-weight"));
        tier.setUpgradeScale(tierYAML.getInt("upgrade-scale", 1));
        tier.setMinItemLevel(tierYAML.getInt("min-item-level", 0));
        tier.setEssenceId(tierYAML.getInt("essence-id", 0));
        tier.setEssenceGroup(tierYAML.getStringList("essence-group"));
        Set<Namespaced> breakSpaces = new HashSet<>();
        for (String s : tierYAML.getStringList("destroy-keys")) {
          breakSpaces.add(NamespacedKey.minecraft(s));
        }
        tier.setDestroyKeys(breakSpaces);
        List<Material> mats = new ArrayList<>();
        for (var s : tierYAML.getStringList("craft-materials")) {
          try {
            mats.add(Material.valueOf(s));
          } catch (Exception ignored) {
          }
        }
        tier.setCraftMaterials(mats);
        if (tierYAML.getConfigurationSection("stat-category-limits") != null) {
          Map<String, Integer> limitMap = new HashMap<>();
          for (String category : tierYAML.getConfigurationSection("stat-category-limits").getKeys(false)) {
            limitMap.put(category, tierYAML.getInt("stat-category-limits." + category));
          }
          tier.setStatCategoryLimits(limitMap);
        }
        if (tierYAML.getConfigurationSection("minimum-sockets") != null) {
          Map<String, Integer> map = new HashMap<>();
          for (String rarity : tierYAML.getConfigurationSection("minimum-sockets").getKeys(false)) {
            map.put(rarity, tierYAML.getInt("minimum-sockets." + rarity));
          }
          tier.setMinimumSocketsByRarity(map);
        }
        if (tierYAML.getConfigurationSection("maximum-sockets") != null) {
          Map<String, Integer> map = new HashMap<>();
          for (String rarity : tierYAML.getConfigurationSection("maximum-sockets").getKeys(false)) {
            map.put(rarity, tierYAML.getInt("maximum-sockets." + rarity));
          }
          tier.setMaximumSocketsByRarity(map);
        }

        List<String> suffixes = tierYAML.getStringList("suffixes.generic");
        for (ItemRarity r : plugin.getRarityManager().getLoadedRarities().values()) {
          List<String> combined = new ArrayList<>(suffixes);
          combined.addAll(tierYAML.getStringList("suffixes." + r.getId()));
          tier.addItemSuffixes(r, combined);
        }

        tier.setShape(tierYAML.getStringList("shape"));
        tier.setMaterials(new ArrayList<>());
        for (String s : tierYAML.getStringList("mats")) {
          var split = s.split("~");
          tier.getMaterials().add(Pair.of(split[0].toCharArray()[0], Material.valueOf(split[1])));
        }

        String marketFilterFlag = tierYAML.getString("filter-flag", "ALL");
        tier.setFilterFlag(FilterFlagA.valueOf(marketFilterFlag));

        TIERS.put(key, tier);

        if (tier.getSpawnWeight() > 0) {
          try {
            var recipe = constructCraftRecipe(tier);
            RECIPES.put(recipe, tier);
          } catch (Exception e) {
            Bukkit.getLogger().info("[Loot] Failed to build recipe for " + tier.getId());
            e.printStackTrace();
          }
        } else {
          Bukkit.getLogger().info("[Loot] Skipped recipe for " + tier.getId() + " (no drop weight)");
        }

      } catch (Exception e) {
        Bukkit.getLogger().info("[Loot] Failed to load tier " + key);
        e.printStackTrace();
      }
    }
    Bukkit.getLogger().info("[Loot] Loaded " + TIERS.size() + " tiers!");
    for (int i = -1; i <= 300; i++) {
      SPAWN_WEIGHT_BY_LEVEL.put(i, getTotalTierWeight(i));
    }
  }

  private static Recipe constructCraftRecipe(Tier tier) {
    NamespacedKey key = new NamespacedKey(LootPlugin.getInstance(), "tier_" + tier.getId());

    var result = new ItemStack(tier.material);
    result.editMeta(m -> {
      m.setDisplayName(FaceColor.YELLOW + "Equipment Crafting");
      m.setLore(PaletteUtil.color(List.of(
          "|white|Crafting: " + tier.getPlainName(),
          "",
          "|lgray|Craft an item of this type! The",
          "|white|level |lgray|of the result is determined",
          "|lgray|by the average level of materials",
          "|lgray|used. The rarity is randomized, but",
          "|lgray|higher quality materials increase",
          "|lgray|the odds of a rarer result!"
      )));
    });
    ItemUtils.setCustomData(result, List.of(tier.id, "common", "t1"));
    ItemRenderer.applyCommonFlags(result);

    ShapedRecipe recipe = new ShapedRecipe(key, result);

    switch (tier.getShape().size()) {
      case 1 -> recipe.shape(tier.getShape().get(0));
      case 2 -> recipe.shape(tier.getShape().get(0), tier.getShape().get(1));
      case 3 -> recipe.shape(tier.getShape().get(0), tier.getShape().get(1), tier.getShape().get(2));
    }
    tier.getMaterials().forEach(pair -> recipe.setIngredient(pair.getKey(), pair.getValue()));

    return recipe;
  }
}
