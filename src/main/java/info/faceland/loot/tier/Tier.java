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
package info.faceland.loot.tier;

import info.faceland.loot.data.ItemStat;
import info.faceland.loot.groups.ItemGroup;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import land.face.market.data.PlayerMarketState.FilterFlagA;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public final class Tier implements Comparable<Tier> {

  private boolean levelRequirement;
  private String id;
  private String name;
  private ItemStat primaryStat;
  private List<ItemStat> secondaryStats;
  private List<ItemStat> bonusStats;
  private List<ItemStat> specialStats;
  private double spawnWeight;
  private double identifyWeight;
  private int customDataStart;
  private int customDataInterval;
  private FilterFlagA filterFlag;
  private Set<ItemGroup> itemGroups;
  private List<String> suffixes = new ArrayList<>();

  @Getter @Setter
  private int minimumSockets;
  @Getter @Setter
  private int maximumSockets;
  @Getter @Setter
  private int minimumExtendSlots;
  @Getter @Setter
  private int maximumExtendSlots;

  public Tier(String id) {
    this.id = id;
    this.secondaryStats = new ArrayList<>();
    this.bonusStats = new ArrayList<>();
    this.itemGroups = new HashSet<>();
  }

  public boolean isLevelRequirement() {
    return levelRequirement;
  }

  public void setLevelRequirement(boolean levelRequirement) {
    this.levelRequirement = levelRequirement;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  public ItemStat getPrimaryStat() {
    return primaryStat;
  }

  public void setPrimaryStat(ItemStat primaryStat) {
    this.primaryStat = primaryStat;
  }

  public List<ItemStat> getSecondaryStats() {
    return secondaryStats;
  }

  public void setSecondaryStats(List<ItemStat> secondaryStats) {
    this.secondaryStats = secondaryStats;
  }

  public List<ItemStat> getBonusStats() {
    return bonusStats;
  }

  public void setBonusStats(List<ItemStat> bonusStats) {
    this.bonusStats = bonusStats;
  }

  public List<ItemStat> getSpecialStats() {
    return specialStats;
  }

  public void setSpecialStats(List<ItemStat> specialStats) {
    this.specialStats = specialStats;
  }

  public double getSpawnWeight() {
    return spawnWeight;
  }

  public void setSpawnWeight(double spawnWeight) {
    this.spawnWeight = spawnWeight;
  }

  public double getIdentifyWeight() {
    return identifyWeight;
  }

  public void setIdentifyWeight(double identifyWeight) {
    this.identifyWeight = identifyWeight;
  }

  public int getCustomDataStart() {
    return customDataStart;
  }

  public void setCustomDataStart(int customDataStart) {
    this.customDataStart = customDataStart;
  }

  public int getCustomDataInterval() {
    return customDataInterval;
  }

  public void setCustomDataInterval(int customDataInterval) {
    this.customDataInterval = customDataInterval;
  }

  public Set<ItemGroup> getItemGroups() {
    return itemGroups;
  }

  public void setItemGroups(Set<ItemGroup> itemGroups) {
    this.itemGroups = itemGroups;
  }

  public Set<Material> getAllowedMaterials() {
    Set<Material> materials = new HashSet<>();
    for (ItemGroup ig : getItemGroups()) {
      if (!ig.isInverse()) {
        if (materials.isEmpty()) {
          materials.addAll(ig.getMaterials());
          continue;
        }
        materials.retainAll(ig.getMaterials());
      }
    }
    for (ItemGroup ig : getItemGroups()) {
      if (ig.isInverse()) {
        for (Material m : ig.getMaterials()) {
          materials.remove(m);
        }
      }
    }
    if (materials.isEmpty()) {
      Bukkit.getLogger().warning("[Loot] Tier " + name + "has invalid item groups! No materials!");
      materials.add(Material.WOODEN_SWORD);
    }
    return materials;
  }

  public List<String> getItemSuffixes() {
    return suffixes;
  }

  public FilterFlagA getFilterFlag() {
    return filterFlag;
  }

  public void setFilterFlag(FilterFlagA filterFlag) {
    this.filterFlag = filterFlag;
  }


  public int compareTo(Tier o) {
    if (o == null) {
      return 1;
    }
    if (this.equals(o)) {
      return 0;
    }
    return Double.compare(getSpawnWeight(), o.getSpawnWeight());
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Tier tier = (Tier) o;

    return Objects.equals(id, tier.id);
  }
}
