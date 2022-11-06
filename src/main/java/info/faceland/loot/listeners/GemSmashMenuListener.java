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
package info.faceland.loot.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ExistingSocketData;
import info.faceland.loot.menu.gemsmasher.GemSmashMenu;
import info.faceland.loot.utils.MaterialUtil;
import ninja.amp.ampmenus.menus.MenuHolder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public final class GemSmashMenuListener implements Listener {

  @EventHandler(priority = EventPriority.LOW)
  public void onClickEnchantMenu(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }
    if (!(((MenuHolder) event.getInventory().getHolder()).getMenu() instanceof GemSmashMenu)) {
      return;
    }
    if (event.getClickedInventory() == null) {
      return;
    }
    if (!event.getClickedInventory().equals(event.getView().getBottomInventory())) {
      return;
    }
    ItemStack stack = event.getCurrentItem();
    if (stack == null || stack.getType() == Material.AIR) {
      return;
    }
    if (LootPlugin.getInstance().getStrifePlugin() != null && LootPlugin.getInstance()
        .getStrifePlugin().getAbilityIconManager().isAbilityIcon(stack)) {
      return;
    }
    Player player = (Player) event.getWhoClicked();
    GemSmashMenu gemSmashMenu = (GemSmashMenu) holder.getMenu();
    ExistingSocketData data = MaterialUtil.buildSocketData(stack);
    if (data.hasIndexes()) {
      player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, 1f);
      gemSmashMenu.setData(player, data, stack);
      gemSmashMenu.update(player);
    } else {
      PaletteUtil.sendMessage(player, "|yellow|This item has no removable gems!");
      gemSmashMenu.setData(player, null, null);
      gemSmashMenu.update(player);
    }
  }

}
