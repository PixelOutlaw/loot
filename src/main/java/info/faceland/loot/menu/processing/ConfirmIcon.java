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

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.data.processing.ProcessOutput;
import info.faceland.loot.data.processing.ProcessResults;
import info.faceland.loot.menu.processing.ResultIcon.Row;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ConfirmIcon extends MenuItem {

  private final ProcessingMenu menu;
  private final Row row;

  private static final ItemStack valid = buildValid();
  private static final ItemStack invalid = buildInvalid();

  ConfirmIcon(ProcessingMenu menu, Row row) {
    super("", new ItemStack(Material.AIR));
    this.menu = menu;
    this.row = row;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    if (menu.getSelectedItem().get(player) == null) {
      return invalid;
    } else {
      return valid;
    }
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    if (event.getClickType() == ClickType.LEFT && event.getClickType() != ClickType.DOUBLE_CLICK) {
      switch (row) {
        case ONE -> {
          ItemStack cost = menu.getSelectedItem().get(event.getPlayer()).clone();
          ProcessOutput processOutput = menu.getProcessingGUI().getProcessMatch(cost.getType(),
              ItemUtils.getModelData(cost)).getOutputOne();
          if (!event.getPlayer().getInventory().contains(cost, processOutput.getCost())) {
            PaletteUtil.sendMessage(event.getPlayer(), "|yellow|You don't have enough to do this!");
            return;
          }
          cost.setAmount(processOutput.getCost());
          event.getPlayer().getInventory().removeItem(cost);
          ItemUtils.giveOrDrop(event.getPlayer(), processOutput.getReward().clone());
        }
        case TWO -> {ItemStack cost = menu.getSelectedItem().get(event.getPlayer()).clone();
          ProcessOutput processOutput = menu.getProcessingGUI().getProcessMatch(cost.getType(),
              ItemUtils.getModelData(cost)).getOutputTwo();
          if (!event.getPlayer().getInventory().contains(cost, processOutput.getCost())) {
            PaletteUtil.sendMessage(event.getPlayer(), "|yellow|You don't have enough to do this!");
            return;
          }
          cost.setAmount(processOutput.getCost());
          event.getPlayer().getInventory().removeItem(cost);
          ItemUtils.giveOrDrop(event.getPlayer(), processOutput.getReward().clone());
        }
        case THREE -> {
          ItemStack cost = menu.getSelectedItem().get(event.getPlayer()).clone();
          ProcessOutput processOutput = menu.getProcessingGUI().getProcessMatch(cost.getType(),
              ItemUtils.getModelData(cost)).getOutputThree();
          if (!event.getPlayer().getInventory().contains(cost, processOutput.getCost())) {
            PaletteUtil.sendMessage(event.getPlayer(), "|yellow|You don't have enough to do this!");
            return;
          }
          cost.setAmount(processOutput.getCost());
          event.getPlayer().getInventory().removeItem(cost);
          ItemUtils.giveOrDrop(event.getPlayer(), processOutput.getReward().clone());
        }
      }
    }
  }

  private static ItemStack buildValid() {
    ItemStack stack = new ItemStack(Material.BARRIER);
    ItemStackExtensionsKt.setCustomModelData(stack, 50);
    ItemStackExtensionsKt.setDisplayName(stack, FaceColor.GREEN + "Do The Thing!");
    TextUtils.setLore(stack, List.of(
        FaceColor.GRAY + "Click to process this item!"
    ), false);
    return stack;
  }

  private static ItemStack buildInvalid() {
    ItemStack stack = new ItemStack(Material.BARRIER);
    ItemStackExtensionsKt.setCustomModelData(stack, 50);
    ItemStackExtensionsKt.setDisplayName(stack, FaceColor.YELLOW + "Invalid Selection");
    TextUtils.setLore(stack, List.of(
        FaceColor.GRAY + "Select a valid item to process!"
    ), false);
    return stack;
  }
}
