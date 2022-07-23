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

import info.faceland.loot.api.managers.ItemGroupManager;
import info.faceland.loot.data.MatchMaterial;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.tier.Tier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class LootItemGroupManager implements ItemGroupManager {

  private final Map<String, ItemGroup> itemGroupMap;
  private final Set<MatchMaterial> matchMaterials;

  public LootItemGroupManager() {
    itemGroupMap = new HashMap<>();
    matchMaterials = new HashSet<>();
  }

  @Override
  public Set<ItemGroup> getItemGroups() {
    return new HashSet<>(itemGroupMap.values());
  }

  @Override
  public void addItemGroup(ItemGroup itemGroup) {
    if (itemGroup != null) {
      itemGroupMap.put(itemGroup.getName().toLowerCase(), itemGroup);
    }
  }

  @Override
  public void removeItemGroup(String name) {
    if (name != null) {
      itemGroupMap.remove(name.toLowerCase());
    }
  }

  @Override
  public ItemGroup getItemGroup(String name) {
    if (name != null && itemGroupMap.containsKey(name.toLowerCase())) {
      return itemGroupMap.get(name.toLowerCase());
    }
    return null;
  }

  @Override
  public void addMatchMaterial(MatchMaterial material) {
    matchMaterials.add(material);
  }

  @Override
  public Tier getTierFromStack(ItemStack stack) {
    for (MatchMaterial material : matchMaterials) {
      if (stack.getType() == material.getMaterial()) {
        int modelData = stack.getItemMeta().hasCustomModelData() ? stack.getItemMeta().getCustomModelData() : 0;
        if (modelData >= material.getMinCustomData() &&
            (material.getMaxCustomData() == -1 || modelData <= material.getMaxCustomData())) {
          return material.getTier();
        }
      }
    }
    return null;
  }

  @Override
  public Set<ItemGroup> getMatchingItemGroups(Material m) {
    Set<ItemGroup> groups = new HashSet<>();
    for (ItemGroup ig : getItemGroups()) {
      if (ig.hasMaterial(m)) {
        groups.add(ig);
      }
    }
    return groups;
  }

  @Override
  public Set<MatchMaterial> getMatchMaterials() {
    return matchMaterials;
  }

}
