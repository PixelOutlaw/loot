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

import info.faceland.loot.api.sockets.effects.SocketEffect;
import info.faceland.loot.groups.ItemGroup;
import java.util.List;

public final class SocketGemBuilder {

  private boolean built = false;
  private final SocketGem gem;

  public SocketGemBuilder(String name) {
    this.gem = new SocketGem(name);
  }

  public boolean isBuilt() {
    return built;
  }

  public SocketGem build() {
    if (isBuilt()) {
      throw new IllegalStateException("already built");
    }
    built = true;
    return gem;
  }

  public SocketGemBuilder withWeight(double d) {
    gem.setWeight(d);
    return this;
  }

  public SocketGemBuilder withPrefix(String s) {
    gem.setPrefix(s);
    return this;
  }

  public SocketGemBuilder withSuffix(String s) {
    gem.setSuffix(s);
    return this;
  }

  public SocketGemBuilder withTypeDesc(String s) {
    gem.setTypeDesc(s);
    return this;
  }

  public SocketGemBuilder withLore(List<String> l) {
    gem.setLore(l);
    return this;
  }

  public SocketGemBuilder withSocketEffects(List<SocketEffect> effects) {
    gem.setSocketEffects(effects);
    return this;
  }

  public SocketGemBuilder withItemGroups(List<ItemGroup> itemGroups) {
    gem.setItemGroups(itemGroups);
    return this;
  }

  public SocketGemBuilder withDistanceWeight(double d) {
    gem.setDistanceWeight(d);
    return this;
  }

  public SocketGemBuilder withWeightPerLevel(double d) {
    gem.setWeightPerLevel(d);
    return this;
  }

  public SocketGemBuilder withCustomModelData(int customModelData) {
    gem.setCustomModelData(customModelData);
    return this;
  }

  public SocketGemBuilder withBroadcast(boolean b) {
    gem.setBroadcast(b);
    return this;
  }

  public SocketGemBuilder withTriggerable(boolean b) {
    gem.setTriggerable(b);
    return this;
  }

  public SocketGemBuilder withTriggerText(String s) {
    gem.setTriggerText(s);
    return this;
  }

  public SocketGemBuilder withBonusWeight(double d) {
    gem.setBonusWeight(d);
    return this;
  }

  public SocketGemBuilder withGemType(SocketGem.GemType type) {
    gem.setGemType(type);
    return this;
  }

  public SocketGemBuilder withLoreAbilityId(String id) {
    gem.setStrifeLoreAbility(id);
    return this;
  }

}
