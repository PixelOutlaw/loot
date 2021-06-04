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

import info.faceland.loot.api.items.CustomItem;
import info.faceland.loot.api.items.CustomItemBuilder;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

public final class LootCustomItemBuilder implements CustomItemBuilder {

  private boolean built = false;
  private LootCustomItem customItem;

  public LootCustomItemBuilder(String name, Material material) {
    this.customItem = new LootCustomItem(name, material);
  }

  @Override
  public boolean isBuilt() {
    return built;
  }

  @Override
  public CustomItem build() {
    if (isBuilt()) {
      throw new IllegalStateException("already built");
    }
    this.built = true;
    return customItem;
  }

  @Override
  public CustomItemBuilder withDisplayName(String displayName) {
    customItem.setDisplayName(displayName);
    return this;
  }

  @Override
  public CustomItemBuilder withLore(List<String> lore) {
    customItem.setLore(lore);
    return this;
  }

  @Override
  public CustomItemBuilder withWeight(double d) {
    customItem.setWeight(d);
    return this;
  }

  @Override
  public CustomItemBuilder withDistanceWeight(double d) {
    customItem.setDistanceWeight(d);
    return this;
  }

  @Override
  public CustomItemBuilder withLevelBase(int i) {
    customItem.setLevelBase(i);
    return this;
  }

  @Override
  public CustomItemBuilder withLevelRange(int i) {
    customItem.setLevelRange(i);
    return this;
  }

  @Override
  public CustomItemBuilder withCustomData(int i) {
    customItem.setCustomDataNumber(i);
    return this;
  }

  @Override
  public CustomItemBuilder withBroadcast(boolean b) {
    customItem.setBroadcast(b);
    return this;
  }

  @Override
  public CustomItemBuilder withQuality(boolean b) {
    customItem.setQuality(b);
    return this;
  }

  @Override
  public CustomItemBuilder withFlags(Set<ItemFlag> flags) {
    customItem.setFlags(flags);
    return this;
  }

  @Override
  public CustomItemBuilder withCanBreak(Set<String> keys) {
    customItem.setCanBreak(keys);
    return this;
  }

}
