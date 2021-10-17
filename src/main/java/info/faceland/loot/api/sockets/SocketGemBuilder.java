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
package info.faceland.loot.api.sockets;

import info.faceland.loot.api.groups.ItemGroup;
import info.faceland.loot.api.sockets.effects.SocketEffect;
import info.faceland.loot.sockets.SocketGem;
import java.util.List;

public interface SocketGemBuilder {

    boolean isBuilt();

    SocketGem build();

    SocketGemBuilder withWeight(double d);

    SocketGemBuilder withPrefix(String s);

    SocketGemBuilder withSuffix(String s);

    SocketGemBuilder withLore(List<String> l);

    SocketGemBuilder withSocketEffects(List<SocketEffect> effects);

    SocketGemBuilder withItemGroups(List<ItemGroup> itemGroups);

    SocketGemBuilder withDistanceWeight(double d);

    SocketGemBuilder withWeightPerLevel(double d);

    SocketGemBuilder withCustomModelData(int i);

    SocketGemBuilder withBroadcast(boolean b);

    SocketGemBuilder withTriggerable(boolean b);

    SocketGemBuilder withTriggerText(String s);

    SocketGemBuilder withBonusWeight(double d);

    SocketGemBuilder withGemType(SocketGem.GemType type);

    SocketGemBuilder withLoreAbilityId(String string);
}
