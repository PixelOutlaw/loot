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

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.PriceData;
import info.faceland.loot.menu.pawn.PawnMenu;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashMap;
import ninja.amp.ampmenus.menus.MenuHolder;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public record PawnMenuListener(LootPlugin plugin) implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void closePawnWindow(InventoryCloseEvent event) {
    if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }
    if (!(((MenuHolder) event.getInventory().getHolder()).getMenu() instanceof PawnMenu)) {
      return;
    }
    PawnMenu pawnMenu = (PawnMenu) holder.getMenu();
    HashMap<Integer, ItemStack> overflow = event.getPlayer().getInventory()
        .addItem(pawnMenu.getReturnStacks().toArray(new ItemStack[0]));

    for (ItemStack stack : overflow.values()) {
      Item item = event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), stack);
      item.setOwner(event.getPlayer().getUniqueId());
    }
    pawnMenu.destroy();
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onClickPawnMenu(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }
    if (!(((MenuHolder) event.getInventory().getHolder()).getMenu() instanceof PawnMenu)) {
      return;
    }
    if (event.getClickedInventory() == null) {
      return;
    }
    if (!event.getClickedInventory().equals(event.getView().getBottomInventory())) {
      return;
    }
    ItemStack stack = event.getClickedInventory().getItem(event.getSlot());
    if (stack == null || stack.getType() == Material.AIR) {
      return;
    }
    String name = ItemStackExtensionsKt.getDisplayName(stack);
    if (StringUtils.isNotBlank(name) && name.contains("Ability:")) {
      return;
    }
    PawnMenu pawnMenu = (PawnMenu) holder.getMenu();

    PriceData priceData = plugin.getPawnManager().getPrice(stack);
    if (priceData.getPrice() == -1) {
      return;
    }
    if (pawnMenu.addItem((Player) event.getWhoClicked(), stack, priceData)) {
      event.getClickedInventory().setItem(event.getSlot(), null);
    }
  }

}
