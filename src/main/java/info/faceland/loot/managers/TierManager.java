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

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.math.LootRandom;
import info.faceland.loot.tier.Tier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;

public final class TierManager {

  private final Set<Tier> loadedTiers = new HashSet<>();
  private final LootRandom random = new LootRandom();

  public List<Tier> getTiersFromName(String id) {
    if (StringUtils.isBlank(id)) {
      return null;
    }
    List<Tier> tiers = new ArrayList<>();
    for (Tier t : getLoadedTiers()) {
      if (t.getName().equals(id)) {
        tiers.add(t);
      }
    }
    return tiers;
  }

  public Tier getTier(String id) {
    if (StringUtils.isBlank(id)) {
      return null;
    }
    for (Tier t : getLoadedTiers()) {
      if (t.getId().replace(" ", "").equalsIgnoreCase(id.replace(" ", ""))) {
        return t;
      }
    }
    return null;
  }

  public void addTier(Tier tier) {
    if (tier != null) {
      loadedTiers.add(tier);
    }
  }

  public void removeTier(String name) {
    Tier t = getTier(name);
    if (t != null) {
      loadedTiers.remove(t);
    }
  }

  public Tier getRandomTier() {
    Set<Tier> allTiers = getLoadedTiers();
    double selectedWeight = random.nextDouble() * getTotalTierWeight();
    double currentWeight = 0;
    for (Tier t : allTiers) {
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

  public Set<Tier> getLoadedTiers() {
    return new HashSet<>(loadedTiers);
  }

  public Set<String> getTierIds() {
    return loadedTiers.stream().map(value -> value.getId()
        .replace(" ", "_")).collect(Collectors.toSet());
  }

  public double getTotalTierWeight() {
    double weight = 0;
    for (Tier t : getLoadedTiers()) {
      double d = t.getSpawnWeight();
      if (d > 0D) {
        weight += d;
      }
    }
    return weight;
  }
}
