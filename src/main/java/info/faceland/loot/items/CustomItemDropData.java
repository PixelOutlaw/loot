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
package info.faceland.loot.items;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.CustomItem;
import java.util.Map;
import java.util.Map.Entry;
import lombok.Getter;

public class CustomItemDropData {

  @Getter
  private final double totalWeight;
  @Getter
  private final Map<CustomItem, Double> dropTable;

  public CustomItemDropData(double totalWeight, Map<CustomItem, Double> dropTable) {
    this.totalWeight = totalWeight;
    this.dropTable = dropTable;
  }

  public CustomItem getDrop() {
    double selectedWeight = LootPlugin.RNG.nextFloat() * totalWeight;
    double currentWeight = 0D;
    for (Entry<CustomItem, Double> e : dropTable.entrySet()) {
      currentWeight += e.getValue();
      if (currentWeight >= selectedWeight) {
        return e.getKey();
      }
    }
    return null;
  }
}
