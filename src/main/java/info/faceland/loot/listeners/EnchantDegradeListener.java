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
package info.faceland.loot.listeners;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.utils.MaterialUtil;
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.dinvy.entity.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public final class EnchantDegradeListener implements Listener {

  private final LootPlugin plugin;

  public EnchantDegradeListener(LootPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEntityDeath(EntityDeathEvent event) {
    LivingEntity dyingEntity = event.getEntity();
    Player p = dyingEntity.getKiller();
    if (p == null) {
      return;
    }
    if (LootPlugin.RNG.nextFloat() > plugin.getSettings().getDouble("config.enchantment-degrade", 1.0)) {
      return;
    }
    PlayerData data = DeluxeInvyPlugin.getInstance().getPlayerManager().getPlayerData(p);
    ItemStack item = switch (LootPlugin.RNG.nextInt(11)) {
      case 0 -> p.getEquipment().getItemInMainHand();
      case 1 -> data.getEquipmentItem(DeluxeSlot.OFF_HAND);
      case 2 -> data.getEquipmentItem(DeluxeSlot.HELMET);
      case 3 -> data.getEquipmentItem(DeluxeSlot.BODY);
      case 4 -> data.getEquipmentItem(DeluxeSlot.LEGS);
      case 5 -> data.getEquipmentItem(DeluxeSlot.BOOTS);
      case 6 -> data.getEquipmentItem(DeluxeSlot.RING_1);
      case 7 -> data.getEquipmentItem(DeluxeSlot.RING_2);
      case 8 -> data.getEquipmentItem(DeluxeSlot.EARRING_1);
      case 9 -> data.getEquipmentItem(DeluxeSlot.EARRING_2);
      case 10 -> data.getEquipmentItem(DeluxeSlot.NECKLACE);
      default -> null;
    };
    if (item == null || item.getType() == Material.AIR) {
      return;
    }
    MaterialUtil.depleteEnchantment(item, p, data);
  }
}