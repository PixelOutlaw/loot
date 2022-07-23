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

import info.faceland.loot.LootPlugin;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.menu.upgrade.EnchantMenu;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import ninja.amp.ampmenus.menus.MenuHolder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public final class EnchantMenuListener implements Listener {

  @EventHandler(priority = EventPriority.LOW)
  public void onClickEnchantMenu(InventoryClickEvent event) {
    if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }
    if (!(((MenuHolder) event.getInventory().getHolder()).getMenu() instanceof EnchantMenu)) {
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
    EnchantMenu enchantMenu = (EnchantMenu) holder.getMenu();
    if (MaterialUtil.isEnchantmentItem(stack)) {
      enchantMenu.setSelectedUpgradeItem(player, stack);
      enchantMenu.update(player);
      player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 2f);
    } else if (LootPlugin.getInstance().getScrollManager().getScroll(stack) != null) {
      enchantMenu.setSelectedUpgradeItem(player, stack);
      enchantMenu.update(player);
      player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 2f);
    } else if (SocketExtender.isSimilar(stack)) {
      enchantMenu.setSelectedUpgradeItem(player, stack);
      enchantMenu.update(player);
      player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 2f);
    } else if (ArcaneEnhancer.isSimilar(stack)) {
      enchantMenu.setSelectedUpgradeItem(player, stack);
      enchantMenu.update(player);
      player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 2f);
    } else if (PurifyingScroll.isSimilar(stack)) {
      enchantMenu.setSelectedUpgradeItem(player, stack);
      enchantMenu.update(player);
      player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 2f);
    } else if ("Magic Crystal".equals(
        ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack)))) {
      enchantMenu.setSelectedUpgradeItem(player, stack);
      enchantMenu.update(player);
      player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 2f);
    } else if (stack.getAmount() == 1 &&
        (MaterialUtil.isEquipmentItem(stack) || MaterialUtil.hasEnchantmentTag(stack))) {
      enchantMenu.setSelectedEquipment(player, stack);
      enchantMenu.update((Player) event.getWhoClicked());
      player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 2f);
    }
  }

}
