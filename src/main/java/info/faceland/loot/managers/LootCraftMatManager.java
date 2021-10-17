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

import info.faceland.loot.data.MatchMaterial;
import info.faceland.loot.utils.MaterialUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class LootCraftMatManager {

  private final Map<Material, String> craftMaterials;
  private final Set<MatchMaterial> matchMaterialSet;

  public LootCraftMatManager() {
    matchMaterialSet = new HashSet<>();
    craftMaterials = new HashMap<>();
  }

  public void addCraftMaterial(Material material, String name) {
    craftMaterials.put(material, name);
  }

  public Map<Material, String> getCraftMaterials() {
    return craftMaterials;
  }

  public void addDeconstructData(MatchMaterial matchMaterial) {
    matchMaterialSet.add(matchMaterial);
  }

  public Set<MatchMaterial> getDeconstructDataSet() {
    return matchMaterialSet;
  }

  public Material getMaterial(ItemStack stack) {
    for (MatchMaterial data : matchMaterialSet) {
      if (data.getMaterial() != stack.getType()) {
        continue;
      }
      int customData = MaterialUtil.getCustomData(stack);
      if (data.getMinCustomData() == -1 && data.getMaxCustomData() == -1) {
        return MatchMaterial.getResultMaterial(data, craftMaterials.keySet());
      }
      if (customData >= data.getMinCustomData() && customData <= data.getMaxCustomData()) {
        return MatchMaterial.getResultMaterial(data, craftMaterials.keySet());
      }
    }
    return null;
  }
}
