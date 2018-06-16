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
package info.faceland.loot.enchantments;

import info.faceland.loot.api.enchantments.EnchantmentTome;
import info.faceland.loot.api.enchantments.EnchantmentTomeBuilder;
import info.faceland.loot.api.groups.ItemGroup;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class LootEnchantmentTomeBuilder implements EnchantmentTomeBuilder {

    private boolean built = false;
    private LootEnchantmentTome stone;

    public LootEnchantmentTomeBuilder(String name) {
        stone = new LootEnchantmentTome(name);
    }

    @Override
    public boolean isBuilt() {
        return built;
    }

    @Override
    public EnchantmentTome build() {
        if (isBuilt()) {
            throw new IllegalStateException("already built");
        }
        built = true;
        return stone;
    }

    @Override
    public EnchantmentTomeBuilder withLore(List<String> s) {
        stone.setLore(s);
        return this;
    }

    @Override
    public EnchantmentTomeBuilder withStat(String s) {
        stone.setStat(s);
        return this;
    }

    @Override
    public EnchantmentTomeBuilder withBar(boolean b) {
        stone.setBar(b);
        return this;
    }

    @Override
    public EnchantmentTomeBuilder withWeight(double d) {
        stone.setWeight(d);
        return this;
    }

    @Override
    public EnchantmentTomeBuilder withDistanceWeight(double d) {
        stone.setDistanceWeight(d);
        return this;
    }

    @Override
    public EnchantmentTomeBuilder withBroadcast(boolean b) {
        stone.setBroadcast(b);
        return this;
    }

    @Override
    public EnchantmentTomeBuilder withItemGroups(List<ItemGroup> l) {
        stone.setItemGroups(l);
        return this;
    }

    @Override
    public EnchantmentTomeBuilder withDescription(String s) {
        stone.setDescription(s);
        return this;
    }

    @Override
    public EnchantmentTomeBuilder withEnchantments(Map<Enchantment, Integer> map) {
        stone.setEnchantments(map);
        return this;
    }

}
