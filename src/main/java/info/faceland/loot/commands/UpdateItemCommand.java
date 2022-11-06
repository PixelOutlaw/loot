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
package info.faceland.loot.commands;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Default;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.utils.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("updateItem")
public class UpdateItemCommand extends BaseCommand {

  private final LootPlugin plugin;

  public UpdateItemCommand(LootPlugin plugin) {
    this.plugin = plugin;
  }

  @Default
  public void updateItem(Player player) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    if (stack == null || stack.getType() == Material.AIR) {
      PaletteUtil.sendMessage(player, "|yellow|You gotta hold an item ya know");
      return;
    }

    if (MaterialUtil.updateItem(stack)) {
      PaletteUtil.sendMessage(player, "|green|Item updated!");
    } else {
      PaletteUtil.sendMessage(player, "|yellow|Sorry! This item can't be auto-updated!");
    }
  }
}