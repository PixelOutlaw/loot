package info.faceland.loot.managers;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemRarity;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RarityManager {

  private final Map<String, ItemRarity> itemRarities;
  private double lowestRarityWeight;

  public RarityManager() {
    this.itemRarities = new HashMap<>();
  }

  public ItemRarity getRarity(String name) {
    return itemRarities.get(name);
  }

  public void addRarity(String name, ItemRarity rarity) {
    itemRarities.put(name, rarity);
  }

  public void removeRarity(String name) {
    itemRarities.remove(name);
  }

  public Map<String, ItemRarity> getLoadedRarities() {
    return itemRarities;
  }

  public Set<String> getRarityIds() {
    return itemRarities.keySet().stream().map(value -> value.replace(" ", "_")).collect(Collectors.toSet());
  }

  public ItemRarity getRandomRarity(double bonus, double minimum) {
    bonus -= 1;
    double selectedWeight = LootPlugin.RNG.nextFloat() * getTotalRarityWeightWithBonus(bonus, minimum);
    double weight = 0;
    for (ItemRarity rarity : getLoadedRarities().values()) {
      weight += calcWeight(rarity, bonus, minimum);
      if (weight >= selectedWeight) {
        return rarity;
      }
    }
    return null;
  }

  private double getTotalRarityWeightWithBonus(double bonus, double minimum) {
    double weight = 0;
    for (ItemRarity rarity : getLoadedRarities().values()) {
      weight += calcWeight(rarity, bonus, minimum);
    }
    return weight;
  }

  private double calcWeight(ItemRarity rarity, double bonus, double minimum) {
    if (rarity.getPower() < minimum) {
      return 0;
    }
    double amount = rarity.getWeight() + lowestRarityWeight * bonus;
    return Math.max(0, amount);
  }

  public void setLowestRarityWeight() {
    double weight = Integer.MAX_VALUE;
    for (ItemRarity rarity : getLoadedRarities().values()) {
      weight = Math.min(rarity.getWeight(), weight);
    }
    lowestRarityWeight = weight;
  }
}
