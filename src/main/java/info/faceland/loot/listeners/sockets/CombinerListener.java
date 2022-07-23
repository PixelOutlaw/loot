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
package info.faceland.loot.listeners.sockets;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.menu.transmute.TransmuteMenu;
import ninja.amp.ampmenus.menus.MenuHolder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public final class CombinerListener implements Listener {

  private LootPlugin plugin;
  public static String transmuteFormat;

  public CombinerListener(LootPlugin plugin) {
    this.plugin = plugin;
    transmuteFormat = plugin.getSettings().getString("language.broadcast.transmute-gem", "");
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onClickPawnMenu(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }
    if (!(holder.getMenu() instanceof TransmuteMenu transmuteMenu)) {
      return;
    }
    if (event.getClickedInventory() == null) {
      return;
    }
    if (!event.getClickedInventory().equals(event.getView().getBottomInventory())) {
      return;
    }
    event.setCancelled(true);
    if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) {
      ItemStack stack = event.getClickedInventory().getItem(event.getSlot());
      if (stack == null || stack.getType() == Material.AIR) {
        return;
      }
      transmuteMenu.ejectResult((Player) event.getWhoClicked());
      transmuteMenu.attemptGemEntry((Player) event.getWhoClicked(), stack, event.getSlot());
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onClose(InventoryCloseEvent event) {
    if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }
    if (!(holder.getMenu() instanceof TransmuteMenu transmuteMenu)) {
      return;
    }
    transmuteMenu.ejectResult((Player) event.getPlayer());
  }
}
