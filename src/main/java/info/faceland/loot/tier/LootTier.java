/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.loot.tier;

import info.faceland.loot.api.groups.ItemGroup;
import info.faceland.loot.api.tier.Tier;
import info.faceland.loot.data.ItemStat;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LootTier implements Tier {

    private boolean levelRequirement;
    private String name;
    private ItemStat primaryStat;
    private List<ItemStat> secondaryStats;
    private List<ItemStat> bonusStats;
    private double spawnWeight;
    private double identifyWeight;
    private Set<ItemGroup> itemGroups;

    public LootTier(String name) {
        this.name = name;
        this.secondaryStats = new ArrayList<>();
        this.bonusStats = new ArrayList<>();
        this.itemGroups = new HashSet<>();
    }

    @Override
    public boolean isLevelRequirement() {
        return levelRequirement;
    }

    public void setLevelRequirement(boolean levelRequirement) {
        this.levelRequirement = levelRequirement;
    }

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public ItemStat getPrimaryStat() {
        return primaryStat;
    }

    public void setPrimaryStat(ItemStat primaryStat) {
        this.primaryStat = primaryStat;
    }

    @Override
    public List<ItemStat> getSecondaryStats() {
        return secondaryStats;
    }

    public void setSecondaryStats(List<ItemStat> secondaryStats) {
        this.secondaryStats = secondaryStats;
    }

    @Override
    public List<ItemStat> getBonusStats() {
        return bonusStats;
    }

    public void setBonusStats(List<ItemStat> bonusStats) {
        this.bonusStats = bonusStats;
    }

    @Override
    public double getSpawnWeight() {
        return spawnWeight;
    }

    public void setSpawnWeight(double spawnWeight) {
        this.spawnWeight = spawnWeight;
    }

    @Override
    public double getIdentifyWeight() {
        return identifyWeight;
    }

    public void setIdentifyWeight(double identifyWeight) {
        this.identifyWeight = identifyWeight;
    }

    @Override
    public Set<ItemGroup> getItemGroups() {
        return itemGroups;
    }

    public void setItemGroups(Set<ItemGroup> itemGroups) {
        this.itemGroups = itemGroups;
    }

    @Override
    public Set<Material> getAllowedMaterials() {
        Set<Material> materials = new HashSet<>();
        for (ItemGroup ig : getItemGroups()) {
            if (!ig.isInverse()) {
                for (Material m : ig.getMaterials()) {
                    materials.add(m);
                }
            }
        }
        for (ItemGroup ig : getItemGroups()) {
            if (ig.isInverse()) {
                for (Material m : ig.getMaterials()) {
                    materials.remove(m);
                }
            }
        }
        return materials;
    }

    @Override
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

        LootTier lootTier = (LootTier) o;

        return !(name != null ? !name.equals(lootTier.name) : lootTier.name != null);
    }
}
