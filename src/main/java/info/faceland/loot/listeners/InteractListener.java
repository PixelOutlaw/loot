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

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.items.NewCustomItem;
import info.faceland.loot.items.prefabs.UnidEnchant;
import info.faceland.loot.menu.tomereader.ReaderMenu;
import info.faceland.loot.menu.upgrade.EnchantMenu;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import land.face.strife.StrifePlugin;
import ninja.amp.ampmenus.menus.MenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public final class InteractListener implements Listener {

  private final LootPlugin plugin;

  private final boolean customEnchantingEnabled;

  public InteractListener(LootPlugin plugin) {
    this.plugin = plugin;
    customEnchantingEnabled = plugin.getSettings().getBoolean("config.custom-enchanting", true);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onItemSpawnEvent(ItemSpawnEvent event) {
    if (!plugin.getSettings().getBoolean("config.show-item-nameplates", true)) {
      return;
    }
    ItemStack itemStack = new ItemStack(event.getEntity().getItemStack());
    String name = ItemStackExtensionsKt.getDisplayName(itemStack);
    if (StringUtils.isBlank(name)) {
      return;
    }
    if (name.equals(ChatColor.GOLD + "REWARD!")) {
      return;
    }
    event.getEntity().setCustomName(name);
    event.getEntity().setCustomNameVisible(true);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onInteractUnique(PlayerInteractEvent event) {
    if (event.getPlayer().getOpenInventory().getType() != InventoryType.CRAFTING) {
      return;
    }
    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      ItemStack stack;
      if (event.getHand() == EquipmentSlot.HAND) {
        stack = event.getPlayer().getEquipment().getItemInMainHand();
      } else if (event.getHand() == EquipmentSlot.OFF_HAND) {
        stack = event.getPlayer().getEquipment().getItemInOffHand();
      } else {
        return;
      }
      if (UnidEnchant.matches(stack)) {
        ReaderMenu.generateOptions(plugin, event.getPlayer());
        stack.setAmount(stack.getAmount() - 1);
        ReaderMenu.show(event.getPlayer());
        return;
      }
      NewCustomItem nci = PersistHelper.getNonEquipmentUniqueItem(stack);
      if (nci == null || nci.getRightClickActions().isEmpty()) {
        return;
      }
      for (String s : nci.getRightClickActions()) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), s.replace("@p", event.getPlayer().getName()));
      }
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void runUniqueFromInvy(InventoryClickEvent event) {
    if (event.getWhoClicked().getOpenInventory().getType() != InventoryType.CRAFTING) {
      return;
    }
    if (event.getWhoClicked().getOpenInventory().getBottomInventory() != event.getClickedInventory()) {
      return;
    }
    if (event.getClick() != ClickType.RIGHT || event.getCursor().getType() != Material.AIR) {
      return;
    }
    if (UnidEnchant.matches(event.getCurrentItem())) {
      ReaderMenu.generateOptions(plugin, (Player) event.getWhoClicked());
      event.getCurrentItem().setAmount(event.getCurrentItem().getAmount() - 1);
      ReaderMenu.show((Player) event.getWhoClicked());
      return;
    }
    NewCustomItem nci = PersistHelper.getNonEquipmentUniqueItem(event.getCurrentItem());
    if (nci == null || nci.getRightClickActions().isEmpty()) {
      return;
    }
    for (String s : nci.getRightClickActions()) {
      Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), s.replace("@p", event.getWhoClicked().getName()));
    }
    event.setCancelled(true);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onInventoryOpenEvent(InventoryOpenEvent event) {
    if (event.getInventory() instanceof EnchantingInventory && customEnchantingEnabled) {
      event.setCancelled(true);
      EnchantMenu enchantMenu = new EnchantMenu(plugin);
      StrifePlugin.getInstance().getStrifeMobManager().getStatMob(event.getPlayer());
      enchantMenu.open((Player) event.getPlayer());
    }
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onClose(InventoryCloseEvent event) {
    if (!(event.getInventory().getHolder() instanceof MenuHolder holder)) {
      return;
    }
    if (!(holder.getMenu() instanceof ReaderMenu transmuteMenu)) {
      return;
    }
    if (ReaderMenu.TOME_OPTIONS.containsKey(event.getPlayer().getUniqueId())) {
      transmuteMenu.rewardOptions((Player) event.getPlayer(), -1);
    }
  }

  // See git history for shards of failure

  @EventHandler(priority = EventPriority.MONITOR)
  public void onItemPickupEvent(EntityPickupItemEvent event) {
    if (!event.isCancelled()) {
      ItemRenderer.renderItem(event.getItem().getItemStack(), false);
    }
  }

}
