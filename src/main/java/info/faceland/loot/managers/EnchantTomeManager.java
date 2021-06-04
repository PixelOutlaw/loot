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

import info.faceland.loot.enchantments.EnchantmentTome;
import info.faceland.loot.math.LootRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class EnchantTomeManager {

  private final Map<String, EnchantmentTome> tomeMap;
  private final LootRandom random;

  public EnchantTomeManager() {
    this.tomeMap = new HashMap<>();
    this.random = new LootRandom(System.currentTimeMillis());
  }

  public EnchantmentTome getEnchantTome(String name) {
    if (tomeMap.containsKey(name.toLowerCase())) {
      return tomeMap.get(name.toLowerCase());
    }
    if (tomeMap.containsKey(name.toLowerCase().replace(" ", "_"))) {
      return tomeMap.get(name.toLowerCase().replace(" ", "_"));
    }
    if (tomeMap.containsKey(name.toLowerCase().replace("_", " "))) {
      return tomeMap.get(name.toLowerCase().replace("_", " "));
    }
    return null;
  }

  public void addEnchantTome(EnchantmentTome gem) {
    if (gem != null) {
      tomeMap.put(gem.getName().toLowerCase(), gem);
    }
  }

  public void removeEnchantTome(String name) {
    if (name != null) {
      tomeMap.remove(name.toLowerCase());
    }
  }

  public EnchantmentTome getRandomEnchantTome() {
    return getRandomEnchantTome(1D);
  }

  public Set<String> getTomeIds() {
    return tomeMap.keySet().stream().map(value -> value.replace(" ", "_")).collect(Collectors.toSet());
  }

  public EnchantmentTome getRandomEnchantTome(double bonus) {
    bonus -= 1;
    double selectedWeight = random.nextDouble() * getTotalWeight(bonus);
    double currentWeight = 0D;
    for (EnchantmentTome tome : tomeMap.values()) {
      double weight = Math.max(0, tome.getWeight() + bonus * tome.getBonusWeight());
      currentWeight += weight;
      if (currentWeight >= selectedWeight) {
        return tome;
      }
    }
    return null;
  }

  public double getTotalWeight(double bonus) {
    double total = 0;
    for (EnchantmentTome tome : tomeMap.values()) {
      total += Math.max(0, tome.getWeight() + tome.getBonusWeight() * bonus);
    }
    return total;
  }

}
