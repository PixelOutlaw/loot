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
package info.faceland.loot.menu.processing;

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import info.faceland.loot.menu.processing.ResultIcon.Row;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CostIcon extends MenuItem {

  private final ProcessingMenu menu;
  private final Row row;

  CostIcon(ProcessingMenu menu, Row row) {
    super("", new ItemStack(Material.AIR));
    this.menu = menu;
    this.row = row;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    switch (row) {
      case ONE -> {
        ItemStack stack = menu.getSelectedItem().get(player).clone();
        stack.setAmount(menu.getProcessingGUI().getProcessMatch(stack.getType(),
            ItemUtils.getModelData(stack)).getOutputOne().getCost());
        return stack;
      }
      case TWO -> {
        ItemStack stack = menu.getSelectedItem().get(player).clone();
        stack.setAmount(menu.getProcessingGUI().getProcessMatch(stack.getType(),
            ItemUtils.getModelData(stack)).getOutputTwo().getCost());
        return stack;
      }
      case THREE -> {
        ItemStack stack = menu.getSelectedItem().get(player).clone();
        stack.setAmount(menu.getProcessingGUI().getProcessMatch(stack.getType(),
            ItemUtils.getModelData(stack)).getOutputThree().getCost());
        return stack;
      }
    }
    return new ItemStack(Material.AIR);
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
  }
}
