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
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.AspectOfPerfection;
import info.faceland.loot.items.prefabs.AwakeningStone;
import info.faceland.loot.items.prefabs.DistortionCell;
import info.faceland.loot.items.prefabs.DivineHammer;
import info.faceland.loot.items.prefabs.ManaCrystal;
import info.faceland.loot.items.prefabs.MissingStat;
import info.faceland.loot.items.prefabs.NormalItem;
import info.faceland.loot.items.prefabs.OrbOfManyFaces;
import info.faceland.loot.items.prefabs.OrbOfOscillation;
import info.faceland.loot.items.prefabs.PerfectSphere;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.items.prefabs.TinkerersGear;
import info.faceland.loot.items.prefabs.TooltipPaint;
import info.faceland.loot.items.prefabs.TraceOfTrash;
import info.faceland.loot.menu.upgrade.EnchantMenu;
import info.faceland.loot.utils.PersistHelper;
import ninja.amp.ampmenus.menus.MenuHolder;
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

    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (stack.getAmount() == 1 && data != null) {
      enchantMenu.setSelectedEquipment(player, stack);
      enchantMenu.update((Player) event.getWhoClicked());
      player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1, 0.75f);
      return;
    }

    boolean select = switch (PersistHelper.getItemType(stack)) {
      // noinspection Yes IDE I know the first case is a dupe of default, it's also way more
      // common, and I'd rather do it before 20 string compares
      case "", "unknown" -> false;
      case SocketGem.TYPE_KEY, EnchantmentTome.TYPE_KEY, PurifyingScroll.TYPE_KEY, ArcaneEnhancer.TYPE_KEY,
           SocketExtender.TYPE_KEY, TinkerersGear.TYPE_KEY, OrbOfManyFaces.TYPE_KEY, UpgradeScroll.TYPE_KEY,
           PerfectSphere.TYPE_KEY, DivineHammer.TYPE_KEY, ManaCrystal.TYPE_KEY, DistortionCell.TYPE_KEY,
           AspectOfPerfection.TYPE_KEY, NormalItem.TYPE_KEY, AwakeningStone.TYPE_KEY, TooltipPaint.TYPE_KEY,
           MissingStat.TYPE_KEY, TraceOfTrash.TYPE_KEY, OrbOfOscillation.TYPE_KEY -> true;
      default -> false;
    };
    if (select) {
      enchantMenu.setSelectedUpgradeItem(player, stack);
      enchantMenu.update(player);
      player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 2f);
    }
  }

}
