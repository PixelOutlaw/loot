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
import java.util.List;
import java.util.Set;
import land.face.market.data.PlayerMarketState.FilterFlagA;

public final class TierBuilder {

  private boolean built = false;
  private final Tier tier;

  public TierBuilder(String id) {
    tier = new Tier(id);
  }

  public boolean isBuilt() {
    return built;
  }

  public Tier build() {
    if (isBuilt()) {
      throw new IllegalStateException("already built");
    }
    built = true;
    return tier;
  }

  public TierBuilder withSkillRequirement(boolean b) {
    tier.setSkillRequirement(b);
    return this;
  }

  public TierBuilder withName(String s) {
    tier.setName(s);
    return this;
  }

  public TierBuilder withPrimaryStat(ItemStat itemStat) {
    tier.setPrimaryStat(itemStat);
    return this;
  }

  public TierBuilder withSecondaryStats(List<ItemStat> itemStats) {
    tier.setSecondaryStats(itemStats);
    return this;
  }

  public TierBuilder withBonusStats(List<ItemStat> itemStats) {
    tier.setBonusStats(itemStats);
    return this;
  }

  public TierBuilder withSpecialStats(List<ItemStat> itemStats) {
    tier.setSpecialStats(itemStats);
    return this;
  }

  public TierBuilder withSpawnWeight(double d) {
    tier.setSpawnWeight(d);
    return this;
  }

  public TierBuilder withMinimumSockets(int d) {
    tier.setMinimumSockets(d);
    return this;
  }

  public TierBuilder withMaximumSockets(int d) {
    tier.setMaximumSockets(d);
    return this;
  }

  public TierBuilder withMinimumExtends(int d) {
    tier.setMinimumExtendSlots(d);
    return this;
  }

  public TierBuilder withMaximumExtends(int d) {
    tier.setMaximumExtendSlots(d);
    return this;
  }

  public TierBuilder withIdentifyWeight(double d) {
    tier.setIdentifyWeight(d);
    return this;
  }

  public TierBuilder withStartingCustomData(int i) {
    tier.setCustomDataStart(i);
    return this;
  }

  public TierBuilder withCustomDataInterval(int i) {
    tier.setCustomDataInterval(i);
    return this;
  }

  public TierBuilder withFilterFlag(FilterFlagA filterFlag) {
    tier.setFilterFlag(filterFlag);
    return this;
  }

  public TierBuilder withItemGroups(Set<ItemGroup> s) {
    tier.setItemGroups(s);
    return this;
  }
}
