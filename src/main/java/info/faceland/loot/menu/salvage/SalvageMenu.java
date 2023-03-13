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
package info.faceland.loot.menu.salvage;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import java.util.Map;
import java.util.WeakHashMap;
import lombok.Getter;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SalvageMenu extends ItemMenu {

  @Getter
  private final LootPlugin plugin;

  private final Map<Player, ItemStack> tools = new WeakHashMap<>();
  private final Map<Player, ItemStack> equipment = new WeakHashMap<>();

  /*
  00 01 02 03 04 05 06 07 08
  09 10 11 12 13 14 15 16 17
  18 19 20 21 22 23 24 25 26
  27 28 29 30 31 32 33 34 35
  36 37 38 39 40 41 42 43 44
  45 46 47 48 49 50 51 52 53
  */

  public SalvageMenu(LootPlugin plugin) {
    super(TextUtils.color(plugin.getSettings().getString("language.menu.salvage-name",
        "&0&lSalvage")), Size.fit(18), plugin);
    this.plugin = plugin;

    setItem(2, new EquipmentIcon(this));
    setItem(6, new ToolIcon(this));

    setItem(12, new SalvageConfirmIcon(this));
    setItem(13, new SalvageConfirmIcon(this));
    setItem(14, new SalvageConfirmIcon(this));
  }

  public ItemStack getEquipment(Player player) {
    return equipment.get(player);
  }

  public ItemStack getTool(Player player) {
    return tools.get(player);
  }

  public void setEquipment(Player player, ItemStack item) {
    equipment.put(player, item);
    update(player);
  }

  public void setTool(Player player, ItemStack item) {
    tools.put(player, item);
    update(player);
  }

  void doDestroy(Player player) {
    ItemStack selectedStack = equipment.get(player);
    ItemStack toolStack = tools.get(player);
    plugin.getSalvageManager().destroy(player, selectedStack, toolStack);
  }
}
