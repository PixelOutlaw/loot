package info.faceland.loot.managers;

import info.faceland.loot.api.managers.RarityManager;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.math.LootRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LootRarityManager implements RarityManager {
  private final Map<String, ItemRarity> itemRarities;
  private final LootRandom random;

  public LootRarityManager() {
    this.itemRarities = new HashMap<>();
    this.random = new LootRandom(System.currentTimeMillis());
  }

  @Override
  public ItemRarity getRarity(String name) {
    return itemRarities.get(name);
  }

  @Override
  public void addRarity(String name, ItemRarity rarity) {
    itemRarities.put(name, rarity);
  }

  @Override
  public void removeRarity(String name) {
    itemRarities.remove(name);
  }

  @Override
  public Map<String, ItemRarity> getLoadedRarities() {
    return itemRarities;
  }

  public Set<String> getRarityIds() {
    return itemRarities.keySet().stream().map(value -> value.replace(" ", "_")).collect(Collectors.toSet());
  }

  @Override
  public ItemRarity getRandomRarity() {
    double selectedWeight = random.nextDouble() * getTotalRarityWeight();
    double currentWeight = 0;
    for (ItemRarity rarity : getLoadedRarities().values()) {
      double calcWeight = rarity.getWeight();
      if (calcWeight >= 0) {
        currentWeight += calcWeight;
      }
      if (currentWeight >= selectedWeight) {
        return rarity;
      }
    }
    return null;
  }

  @Override
  public ItemRarity getRandomRarityWithBonus(double bonus) {
    double selectedWeight = random.nextDouble() * getTotalRarityWeightWithBonus(bonus);
    double currentWeight = 0;
    for (ItemRarity rarity : getLoadedRarities().values()) {
      double calcWeight = rarity.getWeight() + rarity.getPower() * bonus;
      if (calcWeight >= 0) {
        currentWeight += calcWeight;
      }
      if (currentWeight >= selectedWeight) {
        return rarity;
      }
    }
    return null;
  }

  @Override
  public ItemRarity getRandomRarityWithMinimum(double minimum) {
    int amount = (int) Math.floor(minimum);
    if (Math.random() < minimum % 1) {
      amount++;
    }
    double selectedWeight = random.nextDouble() * getTotalRarityWeightWithMinimum(amount);
    double currentWeight = 0;
    for (ItemRarity rarity : getLoadedRarities().values()) {
      if (rarity.getPower() < amount) {
        continue;
      }
      double calcWeight = rarity.getWeight();
      if (calcWeight >= 0) {
        currentWeight += calcWeight;
      }
      if (currentWeight >= selectedWeight) {
        return rarity;
      }
    }
    return null;
  }

  @Override
  public ItemRarity getRandomIdRarity() {
    double selectedWeight = random.nextDouble() * getTotalIdRarityWeight();
    double currentWeight = 0;
    for (ItemRarity rarity : getLoadedRarities().values()) {
      double calcWeight = rarity.getIdWeight();
      if (calcWeight >= 0) {
        currentWeight += calcWeight;
      }
      if (currentWeight >= selectedWeight) {
        return rarity;
      }
    }
    return null;
  }

  private double getTotalRarityWeight() {
    double weight = 0;
    for (ItemRarity rarity : getLoadedRarities().values()) {
      double d = rarity.getWeight();
      if (d > 0D) {
        weight += d;
      }
    }
    return weight;
  }

  private double getTotalIdRarityWeight() {
    double weight = 0;
    for (ItemRarity rarity : getLoadedRarities().values()) {
      double d = rarity.getIdWeight();
      if (d > 0D) {
        weight += d;
      }
    }
    return weight;
  }

  private double getTotalRarityWeightWithBonus(double bonus) {
    double weight = 0;
    for (ItemRarity rarity : getLoadedRarities().values()) {
      double d = rarity.getWeight() + rarity.getPower() * bonus;
      if (d > 0D) {
        weight += d;
      }
    }
    return weight;
  }

  private double getTotalRarityWeightWithMinimum(double minimum) {
    int weight = (int) Math.floor(minimum);
    for (ItemRarity rarity : getLoadedRarities().values()) {
      if (rarity.getPower() < minimum) {
        continue;
      }
      double d = rarity.getWeight();
      if (d > 0D) {
        weight += d;
      }
    }
    return weight;
  }
}
