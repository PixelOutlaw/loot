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
package info.faceland.loot.sockets;

import info.faceland.loot.api.groups.ItemGroup;
import info.faceland.loot.api.sockets.SocketGemBuilder;
import info.faceland.loot.api.sockets.effects.SocketEffect;
import java.util.List;

public final class LootSocketGemBuilder implements SocketGemBuilder {

  private boolean built = false;
  private SocketGem gem;

  public LootSocketGemBuilder(String name) {
    this.gem = new SocketGem(name);
  }

  @Override
  public boolean isBuilt() {
    return built;
  }

  @Override
  public SocketGem build() {
    if (isBuilt()) {
      throw new IllegalStateException("already built");
    }
    built = true;
    return gem;
  }

  @Override
  public SocketGemBuilder withWeight(double d) {
    gem.setWeight(d);
    return this;
  }

  @Override
  public SocketGemBuilder withPrefix(String s) {
    gem.setPrefix(s);
    return this;
  }

  @Override
  public SocketGemBuilder withSuffix(String s) {
    gem.setSuffix(s);
    return this;
  }

  @Override
  public SocketGemBuilder withLore(List<String> l) {
    gem.setLore(l);
    return this;
  }

  @Override
  public SocketGemBuilder withSocketEffects(List<SocketEffect> effects) {
    gem.setSocketEffects(effects);
    return this;
  }

  @Override
  public SocketGemBuilder withItemGroups(List<ItemGroup> itemGroups) {
    gem.setItemGroups(itemGroups);
    return this;
  }

  @Override
  public SocketGemBuilder withDistanceWeight(double d) {
    gem.setDistanceWeight(d);
    return this;
  }

  @Override
  public SocketGemBuilder withWeightPerLevel(double d) {
    gem.setWeightPerLevel(d);
    return this;
  }

  @Override
  public SocketGemBuilder withCustomModelData(int customModelData) {
    gem.setCustomModelData(customModelData);
    return this;
  }

  @Override
  public SocketGemBuilder withBroadcast(boolean b) {
    gem.setBroadcast(b);
    return this;
  }

  @Override
  public SocketGemBuilder withTriggerable(boolean b) {
    gem.setTriggerable(b);
    return this;
  }

  @Override
  public SocketGemBuilder withTriggerText(String s) {
    gem.setTriggerText(s);
    return this;
  }

  @Override
  public SocketGemBuilder withBonusWeight(double d) {
    gem.setBonusWeight(d);
    return this;
  }

  @Override
  public SocketGemBuilder withGemType(SocketGem.GemType type) {
    gem.setGemType(type);
    return this;
  }

  @Override
  public SocketGemBuilder withLoreAbilityId(String id) {
    gem.setStrifeLoreAbility(id);
    return this;
  }

}
