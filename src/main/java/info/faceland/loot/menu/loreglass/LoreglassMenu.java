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
package info.faceland.loot.menu.loreglass;

import com.sentropic.guiapi.gui.GUI;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LoreglassMenu extends ItemMenu {

  private final LootPlugin plugin;

  private final Map<Player, ItemStack> selectedStacks = new WeakHashMap<>();
  private static final Map<Integer, TextComponent> PERCENT_TEXTS = buildInspectPercents();

  /*
  00 01 02 03 04 05 06 07 08
  09 10 11 12 13 14 15 16 17
  18 19 20 21 22 23 24 25 26
  27 28 29 30 31 32 33 34 35
  36 37 38 39 40 41 42 43 44
  45 46 47 48 49 50 51 52 53
  */

  public LoreglassMenu(LootPlugin plugin) {
    super(TextUtils.color(plugin.getSettings()
        .getString("language.menu.loreglass-name","&0&lMortar")), Size.TWO_LINE, plugin);
    this.plugin = plugin;

    setItem(4, new InspectIcon(plugin, this));
  }

  @Override
  public void open(Player player) {
    setSelectedItem(player, null);
    super.open(player);
  }

  public ItemStack getSelectedStack(Player player) {
    return selectedStacks.get(player);
  }

  public void setSelectedItem(Player player, ItemStack item) {
    if (item == null || item.getType() == Material.AIR) {
      selectedStacks.put(player, null);
      return;
    }
    selectedStacks.put(player, item);
    update(player);
  }

  public static TextComponent getPercentText(float roll) {
    int val = Math.round(100 * roll);
    val = Math.max(0, Math.min(100, val));
    return PERCENT_TEXTS.get(val);
  }

  private static Map<Integer, TextComponent> buildInspectPercents() {
    Map<Integer, TextComponent> sneed = new HashMap<>();
    for (int i = 0; i <= 100; i++) {
      int number = i;
      if (i < 1) {
        number = 1;
      } else if (i > 99) {
        number = 99;
      }
      String str = " 懻";
      if (i < 10) {
        str += GUI.spacesOf(-19);
      } else {
        str += GUI.spacesOf(-23);
      }
      String num = "" + number;
      num = num.replaceAll("0", "\uF801懰")
          .replaceAll("1", "\uF801懱")
          .replaceAll("2", "\uF801懲")
          .replaceAll("3", "\uF801懳")
          .replaceAll("4", "\uF801懴")
          .replaceAll("5", "\uF801懵")
          .replaceAll("6", "\uF801懶")
          .replaceAll("7", "\uF801懷")
          .replaceAll("8", "\uF801懸")
          .replaceAll("9", "\uF801懹");
      num += "\uF801懺";
      str += num;
      sneed.put(i, Component.text(str));
    }
    return sneed;
  }
}

/*
00 01 02 03 04 05 06 07 08
09 10 11 12 13 14 15 16 17
18 19 20 21 22 23 24 25 26
27 28 29 30 31 32 33 34 35
36 37 38 39 40 41 42 43 44
45 46 47 48 49 50 51 52 53
*/
