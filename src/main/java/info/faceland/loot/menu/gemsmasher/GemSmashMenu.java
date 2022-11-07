/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.menu.gemsmasher;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ExistingSocketData;
import info.faceland.loot.utils.MaterialUtil;
import java.util.Map;
import java.util.WeakHashMap;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GemSmashMenu extends ItemMenu {

  private final Map<Player, ExistingSocketData> data = new WeakHashMap<>();
  private final Map<Player, ItemStack> stacks = new WeakHashMap<>();
  private final Map<Player, Integer> costs = new WeakHashMap<>();

  /*
  00 01 02 03 04 05 06 07 08
  09 10 11 12 13 14 15 16 17
  18 19 20 21 22 23 24 25 26
  27 28 29 30 31 32 33 34 35
  36 37 38 39 40 41 42 43 44
  45 46 47 48 49 50 51 52 53
  */
  public GemSmashMenu(LootPlugin plugin) {
    super(TextUtils.color(plugin.getSettings().getString("language.menu.gem-smash-name",
        "&0&lGemsmasher")), Size.fit(26), plugin);

    setItem(10, new SelectedItem(this));

    setItem(13, new GemOption(this, 0));
    setItem(14, new GemOption(this, 1));
    setItem(15, new GemOption(this, 2));
    setItem(16, new GemOption(this, 3));
  }

  public ExistingSocketData getData(Player player) {
    return data.get(player);
  }

  public ItemStack getStack(Player player) {
    return stacks.get(player);
  }

  public int getCost(Player player) {
    return costs.get(player);
  }

  public void setData(Player player, ExistingSocketData newData, ItemStack stack) {
    data.put(player, newData);
    stacks.put(player, stack);
    if (stack != null) {
      double cost = MaterialUtil.getLevelRequirement(stack) * 73;
      cost -= cost % 10;
      costs.put(player, (int) cost);
    } else {
      costs.put(player, 0);
    }
  }


}
