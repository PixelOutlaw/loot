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

import info.faceland.loot.api.items.CustomItem;
import info.faceland.loot.api.managers.CustomItemManager;
import info.faceland.loot.math.LootRandom;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class LootCustomItemManager implements CustomItemManager {

  private static final double DISTANCE = 1000;
  private static final double DISTANCE_SQUARED = Math.pow(DISTANCE, 2);
  private final Map<String, CustomItem> customItemMap;
  private final LootRandom random;

  public LootCustomItemManager() {
    customItemMap = new HashMap<>();
    random = new LootRandom(System.currentTimeMillis());
  }

  @Override
  public Set<CustomItem> getCustomItems() {
    return new HashSet<>(customItemMap.values());
  }

  @Override
  public Set<String> listCustomItems() {
    return customItemMap.keySet().stream().map(value -> value.replace(" ", "_"))
        .collect(Collectors.toSet());
  }

  @Override
  public CustomItem getCustomItem(String name) {
    if (customItemMap.containsKey(name.toLowerCase())) {
      return customItemMap.get(name.toLowerCase());
    }
    return null;
  }

  @Override
  public void addCustomItem(CustomItem ci) {
    customItemMap.put(ci.getName().toLowerCase(), ci);
  }

  @Override
  public void removeCustomItem(String name) {
    if (customItemMap.containsKey(name.toLowerCase())) {
      customItemMap.remove(name.toLowerCase());
    }
  }

  @Override
  public CustomItem getRandomCustomItem() {
    return getRandomCustomItem(false);
  }

  @Override
  public CustomItem getRandomCustomItem(boolean withChance) {
    return getRandomCustomItem(withChance, 0D);
  }

  @Override
  public CustomItem getRandomCustomItem(boolean withChance, double distance) {
    return getRandomCustomItem(withChance, distance, new HashMap<CustomItem, Double>());
  }

  @Override
  public CustomItem getRandomCustomItem(boolean withChance, double distance,
      Map<CustomItem, Double> map) {
    if (!withChance) {
      Set<CustomItem> set = getCustomItems();
      CustomItem[] array = set.toArray(new CustomItem[set.size()]);
      return array[random.nextInt(array.length)];
    }
    double selectedWeight = random.nextDouble() * getTotalWeight();
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

  @Override
  public CustomItem getRandomCustomItemByLevel(int level) {
    double selectedWeight = random.nextDouble() * getTotalLevelWeight(level);
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

  @Override
  public double getTotalWeight() {
    double d = 0;
    for (CustomItem ci : getCustomItems()) {
      d += ci.getWeight();
    }
    return d;
  }

  @Override
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

}
