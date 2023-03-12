/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.managers;

import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.math.LootRandom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LootNameManager {

  private final Map<ItemRarity, List<String>> prefixes = new HashMap<>();
  private final Map<ItemRarity, List<String>> suffixes = new HashMap<>();
  private final LootRandom random;

  public LootNameManager() {
    random = new LootRandom(System.currentTimeMillis());
  }

  public String getRandomPrefix(ItemRarity rarity) {
    return prefixes.get(rarity).get(random.nextInt(prefixes.get(rarity).size()));
  }

  public void setPrefixes(ItemRarity rarity, List<String> prefixes) {
    this.prefixes.put(rarity, prefixes);
  }

  public String getRandomSuffix(ItemRarity rarity) {
    return suffixes.get(rarity).get(random.nextInt(suffixes.get(rarity).size()));
  }

  public void setSuffixes(ItemRarity rarity, List<String> suffixes) {
    this.suffixes.put(rarity, suffixes);
  }
}
