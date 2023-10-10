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

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static info.faceland.loot.utils.InventoryUtil.getFirstColor;
import static info.faceland.loot.utils.MaterialUtil.FAILURE_BONUS;
import static org.bukkit.ChatColor.stripColor;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.data.GemCacheData;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.items.prefabs.ShardOfFailure;
import info.faceland.loot.menu.upgrade.EnchantMenu;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import java.util.Objects;
import land.face.strife.StrifePlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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

  @EventHandler(priority = EventPriority.LOWEST)
  public void onInventoryOpenEvent(InventoryOpenEvent event) {
    if (event.getInventory() instanceof EnchantingInventory && customEnchantingEnabled) {
      event.setCancelled(true);
      EnchantMenu enchantMenu = new EnchantMenu(plugin);
      StrifePlugin.getInstance().getStrifeMobManager().getStatMob(event.getPlayer());
      enchantMenu.open((Player) event.getPlayer());
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onInventoryCloseEvent(InventoryCloseEvent event) {
    if (!plugin.getSettings().getBoolean("config.socket-gems.use-potion-triggers")) {
      return;
    }
    Inventory inv = event.getInventory();
    if (!(inv instanceof CraftingInventory)) {
      return;
    }
    InventoryHolder holder = inv.getHolder();
    if (!(holder instanceof Player player)) {
      return;
    }
    if (player.isDead() || player.getHealth() <= 0D) {
      return;
    }
    GemCacheData gemCacheData = plugin.getGemCacheManager().getGemCacheData(player.getUniqueId());
    gemCacheData.updateArmorCache();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onQuickShard(InventoryClickEvent event) {
    if (event.getClick() != ClickType.SHIFT_RIGHT || !(event
        .getClickedInventory() instanceof PlayerInventory)) {
      return;
    }
    if (event.getCurrentItem() == null || event.getCursor() == null
        || event.getCurrentItem().getType() == Material.AIR
        || event.getCursor().getType() == Material.AIR || !(event
        .getWhoClicked() instanceof Player player)) {
      return;
    }

    ItemStack targetItem = new ItemStack(event.getCurrentItem());
    ItemStack cursor = new ItemStack(event.getCursor());
    String cursorName = ItemStackExtensionsKt.getDisplayName(cursor);
    int amount = cursor.getAmount();

    if (StringUtils.isBlank(cursorName) || !ShardOfFailure.isSimilar(cursor)) {
      return;
    }

    UpgradeScroll scroll = plugin.getScrollManager().getScroll(targetItem);
    if (scroll == null) {
      return;
    }
    if (targetItem.getAmount() > 1) {
      sendMessage(player, plugin.getSettings().getString("language.augment.stack-size", ""));
      return;
    }
    List<String> lore = TextUtils.getLore(targetItem);
    int failureBonus = MaterialUtil.getFailureBonus(targetItem);
    if (failureBonus > 0) {
      lore.set(0, StringExtensionsKt.chatColorize(FAILURE_BONUS + " +" + (failureBonus + amount)));
    } else {
      lore.add(0, StringExtensionsKt.chatColorize(FAILURE_BONUS + " +" + amount));
    }
    TextUtils.setLore(targetItem, lore);
    event.setCurrentItem(targetItem);
    event.getCursor().setAmount(0);
    event.setCancelled(true);
    event.setResult(Event.Result.DENY);
    player.updateInventory();

    player.playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1L, 2F);
  }


  @EventHandler(priority = EventPriority.HIGHEST)
  public void onRightClickUse(InventoryClickEvent event) {
    if (event.getClick() != ClickType.RIGHT || !(event
        .getClickedInventory() instanceof PlayerInventory)) {
      return;
    }
    if (event.getCurrentItem() == null || event.getCursor() == null ||
        event.getCurrentItem().getType() == Material.AIR ||
        event.getCursor().getType() == Material.AIR ||
        !(event.getWhoClicked() instanceof Player player)) {
      return;
    }

    ItemStack targetItem = new ItemStack(event.getCurrentItem());
    ItemStack cursor = new ItemStack(event.getCursor());
    String cursorName = ItemStackExtensionsKt.getDisplayName(cursor);

    if (StringUtils.isBlank(cursorName)) {
      return;
    }

    if (ShardOfFailure.isSimilar(cursor)) {
      UpgradeScroll scroll = plugin.getScrollManager().getScroll(targetItem);
      if (scroll == null) {
        return;
      }
      if (targetItem.getAmount() > 1) {
        sendMessage(player, plugin.getSettings().getString("language.augment.stack-size", ""));
        return;
      }
      List<String> lore = TextUtils.getLore(targetItem);
      boolean hasFailureBonus = false;
      for (String s : lore) {
        if (s.startsWith(FAILURE_BONUS)) {
          hasFailureBonus = true;
          break;
        }
      }
      if (hasFailureBonus) {
        int oldAmount = MaterialUtil.getDigit(lore.get(0));
        lore.set(0, TextUtils.color(FAILURE_BONUS + " +" + (oldAmount + 1)));
      } else {
        lore.add(0, TextUtils.color(FAILURE_BONUS + " +1"));
      }
      TextUtils.setLore(targetItem, lore);
      event.setCurrentItem(targetItem);
      cursor.setAmount(cursor.getAmount() - 1);
      if (cursor.getAmount() == 0) {
        event.getCursor().setAmount(0);
      } else  {
        TextUtils.setLore(event.getCursor(), TextUtils.getLore(cursor));
        event.getCursor().setAmount(cursor.getAmount());
      }
      event.setCancelled(true);
      event.setResult(Event.Result.DENY);
      player.updateInventory();
      player.playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1L, 2F);
    }
  }

  private void doItemRenameEffects(ItemStack targetItem, ItemStack cursor, String targetItemName,
      Player player, InventoryClickEvent event) {
    if (TextUtils.getLore(cursor).get(3).equals(ChatColor.WHITE + "none")) {
      sendMessage(player, plugin.getSettings().getString("language.rename.notset", ""));
      return;
    }
    if (isBannedMaterial(targetItem)) {
      sendMessage(player, plugin.getSettings().getString("language.rename.invalid", ""));
      return;
    }
    if (targetItem.hasItemMeta() && targetItem.getItemMeta().hasLore()) {
      for (String s : TextUtils.getLore(targetItem)) {
        if ("[ Crafting Component ]".equals(stripColor(s))) {
          sendMessage(player, plugin.getSettings().getString("language.rename.invalid", ""));
          return;
        }
      }
    }
    int level = stripColor(targetItemName).startsWith("+") ?
        MaterialUtil.getDigit(targetItemName) : 0;
    if (level > 0) {
      ItemStackExtensionsKt.setDisplayName(
          targetItem, getFirstColor(targetItemName) + "+" + level + " "
              + stripColor(TextUtils.getLore(cursor).get(3)));
    } else {
      ItemStackExtensionsKt.setDisplayName(
          targetItem, getFirstColor(targetItemName)
              + stripColor(TextUtils.getLore(cursor).get(3)));
    }

    sendMessage(player, plugin.getSettings().getString("language.rename.success", ""));
    player.playSound(player.getEyeLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 0.8F);
    updateItem(event, targetItem);
  }

  private void updateItem(InventoryClickEvent e, ItemStack currentItem) {
    e.setCurrentItem(currentItem);
    Objects.requireNonNull(e.getCursor()).setAmount(e.getCursor().getAmount() - 1);
    e.setCancelled(true);
    e.setResult(Event.Result.DENY);
    ((Player) e.getWhoClicked()).updateInventory();
  }

  private boolean isBannedMaterial(ItemStack item) {
    return switch (item.getType()) {
      case BOOK, EMERALD, PAPER, NETHER_STAR, DIAMOND, GHAST_TEAR, ENCHANTED_BOOK, NAME_TAG, ARROW, QUARTZ -> true;
      default -> false;
    };
  }

}
