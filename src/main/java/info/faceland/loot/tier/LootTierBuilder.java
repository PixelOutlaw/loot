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
import info.faceland.loot.api.tier.TierBuilder;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Set;

public final class LootTierBuilder implements TierBuilder {
    private boolean built = false;
    private LootTier tier;

    public LootTierBuilder(String name) {
        tier = new LootTier(name);
    }

    @Override
    public boolean isBuilt() {
        return built;
    }

    @Override
    public Tier build() {
        if (isBuilt()) {
            throw new IllegalStateException("already built");
        }
        built = true;
        return tier;
    }

    @Override
    public TierBuilder withDisplayName(String s) {
        tier.setDisplayName(s);
        return this;
    }

    @Override
    public TierBuilder withDisplayColor(ChatColor c) {
        tier.setDisplayColor(c);
        return this;
    }

    @Override
    public TierBuilder withIdentificationColor(ChatColor c) {
        tier.setIdentificationColor(c);
        return this;
    }

    @Override
    public TierBuilder withSpawnWeight(double d) {
        tier.setSpawnWeight(d);
        return this;
    }

    @Override
    public TierBuilder withIdentifyWeight(double d) {
        tier.setIdentifyWeight(d);
        return this;
    }

    @Override
    public TierBuilder withMinimumSockets(int i) {
        tier.setMinimumSockets(i);
        return this;
    }

    @Override
    public TierBuilder withMaximumSockets(int i) {
        tier.setMaximumSockets(i);
        return this;
    }

    @Override
    public TierBuilder withMinimumBonusLore(int i) {
        tier.setMinimumBonusLore(i);
        return this;
    }

    @Override
    public TierBuilder withMaximumBonusLore(int i) {
        tier.setMaximumBonusLore(i);
        return this;
    }

    @Override
    public TierBuilder withBaseLore(List<String> l) {
        tier.setBaseLore(l);
        return this;
    }

    @Override
    public TierBuilder withBonusLore(List<String> l) {
        tier.setBonusLore(l);
        return this;
    }

    @Override
    public TierBuilder withItemGroups(Set<ItemGroup> s) {
        tier.setItemGroups(s);
        return this;
    }

    @Override
    public TierBuilder withMinimumDurability(double d) {
        tier.setMinimumDurability(d);
        return this;
    }

    @Override
    public TierBuilder withMaximumDurability(double d) {
        tier.setMaximumDurability(d);
        return this;
    }

    @Override
    public TierBuilder withDistanceWeight(double d) {
        tier.setDistanceWeight(d);
        return this;
    }

    @Override
    public TierBuilder withEnchantable(boolean b) {
        tier.setEnchantable(b);
        return this;
    }

    @Override
    public TierBuilder withBroadcast(boolean b) {
        tier.setBroadcast(b);
        return this;
    }

    @Override
    public TierBuilder withExtendableChance(double d) {
        tier.setExtendableChance(d);
        return this;
    }

}
