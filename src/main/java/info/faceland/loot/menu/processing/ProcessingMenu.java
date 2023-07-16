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
package info.faceland.loot.menu.processing;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.processing.ProcessingGUI;
import info.faceland.loot.menu.processing.ResultIcon.Row;
import java.util.Map;
import java.util.WeakHashMap;
import lombok.Getter;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ProcessingMenu extends ItemMenu {

  @Getter
  private final LootPlugin plugin;
  @Getter
  private ProcessingGUI processingGUI;
  @Getter
  private Map<Player, ItemStack> selectedItem = new WeakHashMap<>();

  /*
  00 01 02 03 04 05 06 07 08
  09 10 11 12 13 14 15 16 17
  18 19 20 21 22 23 24 25 26
  27 28 29 30 31 32 33 34 35
  36 37 38 39 40 41 42 43 44
  45 46 47 48 49 50 51 52 53
  */

  public ProcessingMenu(LootPlugin plugin, ProcessingGUI processingGUI, String name) {
    super(PaletteUtil.culturallyEnrich(plugin.getSettings()
        .getString("language.menu.processing-name", "") + name), Size.SIX_LINE, plugin);
    this.plugin = plugin;
    this.processingGUI = processingGUI;

    setItem(11, new CostIcon(this, Row.ONE));
    setItem(20, new CostIcon(this, Row.TWO));
    setItem(29, new CostIcon(this, Row.THREE));

    setItem(14, new ResultIcon(this, Row.ONE));
    setItem(23, new ResultIcon(this, Row.TWO));
    setItem(32, new ResultIcon(this, Row.THREE));

    setItem(15, new ConfirmIcon(this, Row.ONE));
    setItem(24, new ConfirmIcon(this, Row.TWO));
    setItem(33, new ConfirmIcon(this, Row.THREE));
  }
}
