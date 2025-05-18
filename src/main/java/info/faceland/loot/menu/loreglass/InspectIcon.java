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
package info.faceland.loot.menu.loreglass;

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.StoredStat;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InspectIcon extends MenuItem {

  private final LootPlugin plugin;
  private final LoreglassMenu menu;

  InspectIcon(LootPlugin plugin, LoreglassMenu menu) {
    super(TextUtils.color("&cClick to cut a gem!"), ItemUtils.BLANK.clone());
    this.plugin = plugin;
    this.menu = menu;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    if (menu.getSelectedStack(player) == null || menu.getSelectedStack(player).getAmount() < 1) {
      ItemStack stack = getIcon().clone();
      ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color("&7Select An Item To Inspect!"));
      return stack;
    }
    ItemStack stack = menu.getSelectedStack(player).clone();
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      ItemRenderer.renderEquipmentItem(plugin, stack, data, true);
      return stack;
    }
    StoredStat stat = PersistHelper.getEssenceStat(stack);
    if (stat != null) {
      ItemRenderer.renderEssence(stack, stat);
      return stack;
    }
    return new ItemStack(Material.AIR);
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    event.setWillUpdate(false);
    event.setWillClose(false);
  }
}
