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
package info.faceland.loot.api.tier;

import info.faceland.loot.api.groups.ItemGroup;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.tier.Tier;
import java.util.List;
import java.util.Set;
import land.face.market.data.PlayerMarketState.FilterFlagA;

public interface TierBuilder {

    boolean isBuilt();

    Tier build();

    TierBuilder withLevelRequirement(boolean b);

    TierBuilder withName(String s);

    TierBuilder withPrimaryStat(ItemStat s);

    TierBuilder withSecondaryStats(List<ItemStat> s);

    TierBuilder withBonusStats(List<ItemStat> s);

    TierBuilder withSpecialStats(List<ItemStat> s);

    TierBuilder withSpawnWeight(double d);

    TierBuilder withSocketSlots(int d);

    TierBuilder withExtenderSlots(int d);

    TierBuilder withIdentifyWeight(double d);

    TierBuilder withStartingCustomData(int i);

    TierBuilder withCustomDataInterval(int i);

    TierBuilder withFilterFlag(FilterFlagA filterFlag);

    TierBuilder withItemGroups(Set<ItemGroup> s);

}
