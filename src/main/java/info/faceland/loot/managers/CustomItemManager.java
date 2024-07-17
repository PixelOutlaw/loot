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

import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.CustomItemDropData;
import info.faceland.loot.items.NewCustomItem;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;

public final class CustomItemManager {

  private final LootPlugin plugin;

  private final Map<String, NewCustomItem> newUniques;

  private final Map<Integer, CustomItemDropData> cachedRates = new HashMap<>();

  public CustomItemManager(LootPlugin plugin) {
    this.plugin = plugin;
    newUniques = new HashMap<>();
  }

  public Set<NewCustomItem> getCustomItems() {
    return new HashSet<>(newUniques.values());
  }

  public Set<String> listCustomItems() {
    return newUniques.keySet().stream().map(value -> value.replace(" ", "_"))
        .collect(Collectors.toSet());
  }

  @Deprecated
  public NewCustomItem getCustomItem(String name) {
    if (newUniques.containsKey(name.toLowerCase())) {
      return newUniques.get(name.toLowerCase());
    }
    return null;
  }

  public NewCustomItem getUnique(String name) {
    if (newUniques.containsKey(name.toLowerCase())) {
      return newUniques.get(name.toLowerCase());
    }
    return null;
  }

  public NewCustomItem getRandomByLevel(int level) {
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
      Map<NewCustomItem, Double> weightsMap = new HashMap<>();
      for (var ci : newUniques.values()) {
        double diff = Math.abs(ci.getDropLevel() - i);
        if (diff >= ci.getDropRange()) {
          continue;
        }
        double calcWeight = ci.getWeight() * (1 - diff / ci.getDropRange());
        total += calcWeight;
        weightsMap.put(ci, calcWeight);
      }
      if (weightsMap.size() < 50) {
        Bukkit.getLogger().warning("[Loot] Unique table for Lv" + i + " only contains " + weightsMap.size() + " items");
      }
      cachedRates.put(i, new CustomItemDropData(total, weightsMap));
    }
  }

  public void loadNewUniques(List<SmartYamlConfiguration> files) {
    newUniques.clear();
    for (SmartYamlConfiguration file : files) {
      for (String key : file.getKeys(false)) {
        try {
          if (!file.isConfigurationSection(key)) {
            continue;
          }
          NewCustomItem nci = NewCustomItem.fromConfigSection(plugin, key, file.getConfigurationSection(key));
          newUniques.put(key, nci);
        } catch (Exception e) {
          Bukkit.getLogger().warning("[Loot] Failed to load nu-unique named " + key + " from file " + file.getName());
          e.printStackTrace();
        }
      }
    }
    Bukkit.getLogger().info("[Loot] Loaded " + newUniques.size() + " uniques from " + files.size() + " files");
    cacheDropRates();
  }
}
