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
import info.faceland.loot.api.items.CustomItem;
import info.faceland.loot.api.items.CustomItemBuilder;
import info.faceland.loot.items.CustomItemDropData;
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

  private final Map<String, CustomItem> customItemMap;
  private final Map<String, CustomItem> uneditedHash;

  private final Map<Integer, CustomItemDropData> cachedRates = new HashMap<>();

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

  public CustomItem getRandomCustomItemByLevel(int level) {
    level = Math.max(0, Math.min(100, level));
    CustomItemDropData cidd = cachedRates.get(level);
    if (cidd == null || cidd.getDropTable().isEmpty()) {
      return null;
    }
    return cidd.getDrop();
  }

  private void cacheDropRates() {
    cachedRates.clear();
    for (int i = 0; i <= 100; i++) {
      double total = 0;
      Map<CustomItem, Double> weightsMap = new HashMap<>();
      for (CustomItem ci : customItemMap.values()) {
        double diff = Math.abs(ci.getLevelBase() - i);
        if (diff >= ci.getLevelRange()) {
          continue;
        }
        double calcWeight = ci.getWeight() * (1 - diff / ci.getLevelRange());
        total += calcWeight;
        weightsMap.put(ci, calcWeight);
      }
      if (weightsMap.size() < 50) {
        Bukkit.getLogger().warning("[Loot] Unique table for Lv" + i + " only contains " + weightsMap.size() + " items");
      }
      cachedRates.put(i, new CustomItemDropData(total, weightsMap));
    }
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
    cacheDropRates();
  }
}
