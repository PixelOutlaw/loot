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
import info.faceland.loot.data.CraftToolData;
import info.faceland.loot.menu.salvage.SalvageMenu;
import info.faceland.loot.tier.Tier;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import ninja.amp.ampmenus.menus.MenuHolder;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public record SalvageMenuListener(LootPlugin plugin) implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void openSalvage(PlayerInteractEvent event) {
    if (event.getPlayer().getOpenInventory().getType() != InventoryType.CRAFTING) {
      return;
    }
    if (event.getAction() == Action.RIGHT_CLICK_AIR
        || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      if (plugin.getSalvageManager().getToolData(event.getPlayer().getEquipment().getItemInMainHand()) != null) {
        plugin.getSalvageMenu().setTool(event.getPlayer(), event.getPlayer().getEquipment().getItemInMainHand());
        plugin.getSalvageMenu().setEquipment(event.getPlayer(), null);
        plugin.getSalvageMenu().open(event.getPlayer());
      }
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onClickPawnMenu(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }
    if (!(((MenuHolder) event.getInventory().getHolder()).getMenu() instanceof SalvageMenu)) {
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
    SalvageMenu salvageMenu = (SalvageMenu) holder.getMenu();

    CraftToolData data = plugin.getSalvageManager().getToolData(stack);
    if (data != null) {
      salvageMenu.setTool((Player) event.getWhoClicked(), stack);
      ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(),
          Sound.ENTITY_VILLAGER_WORK_TOOLSMITH, 1, 1.2f);
      return;
    }
    Tier t = plugin.getItemGroupManager().getTierFromStack(stack);
    if (t != null) {
      salvageMenu.setEquipment((Player) event.getWhoClicked(), stack);
      ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(),
          Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, 1.1f);
    }
  }

}
