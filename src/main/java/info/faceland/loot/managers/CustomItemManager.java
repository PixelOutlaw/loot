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
package info.faceland.loot.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.UnicodeUtil;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.CustomItem;
import info.faceland.loot.api.items.CustomItemBuilder;
import info.faceland.loot.items.LootCustomItemBuilder;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public final class CustomItemManager {

  private static final double DISTANCE = 1000;
  private static final double DISTANCE_SQUARED = Math.pow(DISTANCE, 2);
  private final Map<String, CustomItem> customItemMap;
  private final Map<String, CustomItem> uneditedHash;

  public CustomItemManager() {
    customItemMap = new HashMap<>();
    uneditedHash = new HashMap<>();
  }

  public Set<CustomItem> getCustomItems() {
    return new HashSet<>(customItemMap.values());
  }

  public Set<String> listCustomItems() {
    return customItemMap.keySet().stream().map(value -> value.replace(" ", "_"))
        .collect(Collectors.toSet());
  }

  public CustomItem getCustomItem(String name) {
    if (customItemMap.containsKey(name.toLowerCase())) {
      return customItemMap.get(name.toLowerCase());
    }
    return null;
  }

  public CustomItem getCustomItemFromStack(ItemStack stack) {
    return uneditedHash.get(customItemSerial(stack));
  }

  private String customItemSerial(ItemStack stack) {
    String material = stack.getType().toString();
    String name = ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    boolean enchantable = MaterialUtil.hasEnchantmentTag(stack);
    String gemData = MaterialUtil.getSocketString(stack);
    return material + name + enchantable + gemData;
  }

  public void addCustomItem(String key, CustomItem ci) {
    customItemMap.put(key.toLowerCase(), ci);
    uneditedHash.put(customItemSerial(ci.toItemStack(1)), ci);
  }

  public CustomItem getRandomCustomItem() {
    return getRandomCustomItem(false);
  }

  public CustomItem getRandomCustomItem(boolean withChance) {
    return getRandomCustomItem(withChance, 0D);
  }

  public CustomItem getRandomCustomItem(boolean withChance, double distance) {
    return getRandomCustomItem(withChance, distance, new HashMap<CustomItem, Double>());
  }

  public CustomItem getRandomCustomItem(boolean withChance, double distance,
      Map<CustomItem, Double> map) {
    if (!withChance) {
      Set<CustomItem> set = getCustomItems();
      CustomItem[] array = set.toArray(new CustomItem[set.size()]);
      return array[LootPlugin.RNG.nextInt(array.length)];
    }
    double selectedWeight = LootPlugin.RNG.nextFloat() * getTotalWeight();
    double currentWeight = 0D;
    for (CustomItem ci : getCustomItems()) {
      double calcWeight = ci.getWeight() + ((distance / DISTANCE_SQUARED) * ci.getDistanceWeight());
      if (map.containsKey(ci)) {
        calcWeight *= map.get(ci);
      }
      currentWeight += calcWeight;
      if (currentWeight >= selectedWeight) {
        return ci;
      }
    }
    return null;
  }

  public CustomItem getRandomCustomItemByLevel(int level) {
    double selectedWeight = LootPlugin.RNG.nextFloat() * getTotalLevelWeight(level);
    double currentWeight = 0D;
    for (CustomItem ci : getCustomItems()) {
      double diff = Math.abs(ci.getLevelBase() - level);
      if (diff >= ci.getLevelRange()) {
        continue;
      }
      double calcWeight = ci.getWeight() * (1 - diff / ci.getLevelRange());
      currentWeight += calcWeight;
      if (currentWeight >= selectedWeight) {
        return ci;
      }
    }
    return null;
  }

  public double getTotalWeight() {
    double d = 0;
    for (CustomItem ci : getCustomItems()) {
      d += ci.getWeight();
    }
    return d;
  }

  public double getTotalLevelWeight(int level) {
    double weight = 0;
    for (CustomItem ci : getCustomItems()) {
      double diff = Math.abs(ci.getLevelBase() - level);
      if (diff >= ci.getLevelRange()) {
        continue;
      }
      double d = ci.getWeight() * (1 - diff / ci.getLevelRange());
      weight += d;
    }
    return weight;
  }

  public void loadFromFiles(List<SmartYamlConfiguration> files) {
    customItemMap.clear();
    for (SmartYamlConfiguration file : files) {
      for (String key : file.getKeys(false)) {
        try {
          if (!file.isConfigurationSection(key)) {
            continue;
          }
          ConfigurationSection cs = file.getConfigurationSection(key);
          String matString = cs.getString("material");
          Material material = Material.valueOf(matString);
          CustomItemBuilder builder = new LootCustomItemBuilder(key, material);
          builder.withDisplayName(cs.getString("display-name"));
          builder.withLore(UnicodeUtil.unicodePlacehold(cs.getStringList("lore")));
          double weight = cs.getDouble("weight");
          builder.withWeight(weight);
          builder.withLevelBase(cs.getInt("level-base"));
          builder.withLevelRange(cs.getInt("level-range"));
          builder.withCustomData(cs.getInt("custom-data-value", -1));
          builder.withBroadcast(cs.getBoolean("broadcast"));
          builder.withExport(cs.getBoolean("export", weight > 0));
          List<String> flags = cs.getStringList("flags");
          Set<ItemFlag> itemFlags = new HashSet<>();
          for (String s : flags) {
            itemFlags.add(ItemFlag.valueOf(s));
          }
          builder.withFlags(itemFlags);
          builder.withCanBreak(new HashSet<>(cs.getStringList("can-break")));
          addCustomItem(key, builder.build());
        } catch (Exception e) {
          Bukkit.getLogger().warning("[Loot] Failed to load unique named " + key + " from file " + file.getName());
          e.printStackTrace();
        }
      }
    }
    Bukkit.getLogger().info("[Loot] Loaded " + customItemMap.size() + " items from " + files.size() + " files");
  }

}
