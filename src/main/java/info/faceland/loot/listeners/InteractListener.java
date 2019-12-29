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

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.data.GemCacheData;
import info.faceland.loot.api.sockets.SocketGem;
import info.faceland.loot.api.tier.Tier;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.math.LootRandom;
import info.faceland.loot.menu.upgrade.EnchantMenu;
import info.faceland.loot.utils.inventory.InventoryUtil;
import info.faceland.loot.utils.inventory.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.PlayerDataUtil;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.List;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static info.faceland.loot.utils.inventory.InventoryUtil.*;
import static info.faceland.loot.utils.inventory.MaterialUtil.FAILURE_BONUS;
import static org.bukkit.ChatColor.stripColor;

public final class InteractListener implements Listener {

  private final LootPlugin plugin;
  private LootRandom random;

  private boolean customEnchantingEnabled;

  public InteractListener(LootPlugin plugin) {
    this.plugin = plugin;
    this.random = new LootRandom();
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
    if (name.startsWith("***{")) {
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
      enchantMenu.open((Player) event.getPlayer());
    }
  }

  // Block heads from being placed if they have a specific data
  @EventHandler(priority = EventPriority.MONITOR)
  public void onHeadBlockPlace(BlockPlaceEvent event) {
    Material material = event.getItemInHand().getType();
    if (material != Material.PLAYER_HEAD) {
      return;
    }
    if (MaterialUtil.getCustomData(event.getItemInHand()) == 2000) {
      event.setCancelled(true);
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
    if (!(holder instanceof Player)) {
      return;
    }
    Player player = (Player) holder;
    if (player.isDead() || player.getHealth() <= 0D) {
      return;
    }
    GemCacheData gemCacheData = plugin.getGemCacheManager().getGemCacheData(player.getUniqueId());
    gemCacheData.updateArmorCache();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onRightClickUse(InventoryClickEvent event) {
    if (!(event.getClickedInventory() instanceof PlayerInventory)) {
      return;
    }
    if (event.getCurrentItem() == null || event.getCursor() == null ||
        event.getCurrentItem().getType() == Material.AIR ||
        event.getCursor().getType() == Material.AIR ||
        !(event.getWhoClicked() instanceof Player) || event.getClick() != ClickType.RIGHT) {
      return;
    }

    Player player = (Player) event.getWhoClicked();
    ItemStack targetItem = new ItemStack(event.getCurrentItem());
    ItemStack cursor = new ItemStack(event.getCursor());
    String targetItemName = ItemStackExtensionsKt.getDisplayName(targetItem);
    String cursorName = ItemStackExtensionsKt.getDisplayName(cursor);

    if (StringUtils.isBlank(cursorName)) {
      return;
    }

    if (StringUtils.isBlank(targetItemName)) {
      targetItemName = WordUtils
          .capitalize(targetItem.getType().toString().toLowerCase().replaceAll("_", " "));
    }

    if (cursorName.startsWith(ChatColor.GOLD + "Socket Gem - ")) {
      String gemName = stripColor(cursorName.replace(ChatColor.GOLD + "Socket Gem - ", ""));
      SocketGem gem = plugin.getSocketGemManager().getSocketGem(gemName);

      if (gem == null) {
        return;
      }

      if (!plugin.getItemGroupManager().getMatchingItemGroups(targetItem.getType()).containsAll(
          gem.getItemGroups())) {
        sendMessage(player, plugin.getSettings().getString("language.socket.failure", ""));
        player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
        return;
      }

      List<String> lore = ItemStackExtensionsKt.getLore(targetItem);
      List<String> strippedLore = InventoryUtil.stripColor(lore);
      if (!strippedLore.contains("(Socket)")) {
        sendMessage(player,
            plugin.getSettings().getString("language.socket.needs-sockets", ""));
        player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
        return;
      }
      int index = strippedLore.indexOf("(Socket)");

      lore.remove(index);
      lore.addAll(index, TextUtils.color(gem.getLore()));

      ItemStackExtensionsKt.setLore(targetItem, lore);

      // strip color, check against that
      // k
      ChatColor firstColor = getFirstColor(targetItemName);
      ChatColor lastColor = getLastColor(targetItemName);
      targetItemName = stripColor(targetItemName);
      int level = MaterialUtil.getDigit(targetItemName);
      targetItemName = targetItemName.replace("+" + level + " ", "");
      String prefix = "";
      String suffix = "";
      if (!gem.getPrefix().isEmpty()) {
        if (!targetItemName.contains(gem.getPrefix())) {
          if (stripColor(targetItemName).startsWith("The ")) {
            targetItemName = targetItemName.replace("The ", "");
            prefix = "The " + gem.getPrefix() + " ";
          } else {
            prefix = gem.getPrefix() + " ";
          }
        }
      }
      if (!gem.getSuffix().isEmpty()) {
        if (!targetItemName.contains(gem.getSuffix())) {
          suffix = " " + gem.getSuffix();
        }
      }
      targetItemName = firstColor + (level > 0 ? "+" + level + " " : "") + prefix + targetItemName
          + suffix + lastColor;
      ItemStackExtensionsKt.setDisplayName(targetItem, targetItemName);

      sendMessage(player, plugin.getSettings().getString("language.socket.success", ""));
      player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1L, 2.0F);
      updateItem(event, targetItem);
    } else if (cursorName.equals(ChatColor.DARK_AQUA + "Socket Extender")) {
      List<String> lore = ItemStackExtensionsKt.getLore(targetItem);
      List<String> stripColor = InventoryUtil.stripColor(lore);
      if (!stripColor.contains("(+)")) {
        sendMessage(player, plugin.getSettings().getString("language.extend.failure", ""));
        player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
        return;
      }
      int index = stripColor.indexOf("(+)");
      lore.set(index, ChatColor.GOLD + "(Socket)");
      ItemStackExtensionsKt.setLore(targetItem, lore);

      sendMessage(player, plugin.getSettings().getString("language.extend.success", ""));
      player.playSound(player.getEyeLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1L, 2.0F);
      updateItem(event, targetItem);
    } else if (cursorName.equals(ChatColor.DARK_PURPLE + "Identity Tome")) {
      if (!targetItemName.equals(ChatColor.LIGHT_PURPLE + "Unidentified Item")) {
        return;
      }
      int itemLevel = Math.max(1, MaterialUtil.getDigit(targetItem.getItemMeta().getLore().get(0)));
      ItemRarity r = plugin.getRarityManager().getRandomIdRarity();
      Tier t = plugin.getTierManager().getRandomTier();
      targetItem = plugin.getNewItemBuilder()
          .withRarity(r)
          .withTier(t)
          .withLevel(itemLevel)
          .build().getStack();
      if (r.isBroadcast()) {
        broadcast(player, targetItem,
            plugin.getSettings().getString("language.broadcast.ided-item"));
      }
      sendMessage(player, plugin.getSettings().getString("language.identify.success", ""));
      player.playSound(player.getEyeLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1L, 2.0F);
      updateItem(event, targetItem);
    } else if (cursorName.equals(ChatColor.DARK_AQUA + "Faceguy's Tears")) {
      if (isBannedMaterial(targetItem)) {
        return;
      }
      String name = targetItemName;
      if (plugin.getSettings().getStringList("config.cannot-be-upgraded", new ArrayList<String>())
          .contains(stripColor(name))) {
        return;
      }
      boolean succeed = false;
      List<String> lore = ItemStackExtensionsKt.getLore(targetItem);
      List<String> strip = InventoryUtil.stripColor(lore);
      int line = 0;
      for (String s : strip) {
        if (s.startsWith("+")) {
          String loreLev = CharMatcher.digit().or(CharMatcher.is('-')).retainFrom(s);
          int loreLevel = NumberUtils.toInt(loreLev);
          lore.set(line, s.replace("+" + loreLevel, ChatColor.DARK_AQUA + "+" + (loreLevel + 1)));
          succeed = true;
          break;
        }
        line++;
      }
      if (!succeed) {
        return;
      }
      ItemStackExtensionsKt.setLore(targetItem, lore);
      sendMessage(player, plugin.getSettings().getString("language.upgrade.success", ""));
      player.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 2F);
      updateItem(event, targetItem);
    } else if (cursorName.equals(ChatColor.RED + "Shard Of Failure")) {
      UpgradeScroll scroll = plugin.getScrollManager().getScroll(cursor);
      if (scroll == null) {
        return;
      }
      if (targetItem.getAmount() > 1) {
        sendMessage(player, plugin.getSettings().getString("language.augment.stack-size", ""));
        return;
      }
      List<String> lore = ItemStackExtensionsKt.getLore(targetItem);
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
      ItemStackExtensionsKt.setLore(targetItem, lore);
      event.setCurrentItem(targetItem);
      cursor.setAmount(cursor.getAmount() - 1);
      event.setCursor(cursor.getAmount() == 0 ? null : cursor);
      event.setCancelled(true);
      event.setResult(Event.Result.DENY);
      player.updateInventory();
      player.playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1L, 2F);
      plugin.getStrifePlugin().getSkillExperienceManager()
          .addExperience(player, LifeSkillType.ENCHANTING, 1, false);
    } else if (cursorName.equals(ChatColor.WHITE + "Item Rename Tag")) {
      if (ItemStackExtensionsKt.getLore(cursor).get(3).equals(ChatColor.WHITE + "none")) {
        sendMessage(player, plugin.getSettings().getString("language.rename.notset", ""));
        return;
      }
      if (isBannedMaterial(targetItem)) {
        sendMessage(player, plugin.getSettings().getString("language.rename.invalid", ""));
        return;
      }
      if (targetItem.hasItemMeta() && targetItem.getItemMeta().hasLore()) {
        for (String s : ItemStackExtensionsKt.getLore(targetItem)) {
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
                + stripColor(ItemStackExtensionsKt.getLore(cursor).get(3)));
      } else {
        ItemStackExtensionsKt.setDisplayName(
            targetItem, getFirstColor(targetItemName)
                + stripColor(ItemStackExtensionsKt.getLore(cursor).get(3)));
      }

      sendMessage(player, plugin.getSettings().getString("language.rename.success", ""));
      player.playSound(player.getEyeLocation(), Sound.ENTITY_BAT_TAKEOFF, 1F, 0.8F);
      updateItem(event, targetItem);
    } else if (cursorName.startsWith(ChatColor.DARK_PURPLE + "Magic Crystal")) {
      List<String> lore = ItemStackExtensionsKt.getLore(targetItem);
      boolean valid = false;
      int index = 0;
      int addAmount = 0;
      for (String str : ItemStackExtensionsKt.getLore(targetItem)) {
        if (str.startsWith(ChatColor.BLUE + "[") && str.contains("" + ChatColor.BLACK)) {
          valid = true;
          int barIndex = str.indexOf("" + ChatColor.BLACK);
          if (barIndex == str.length() - 5) {
            sendMessage(player, plugin.getSettings().getString("language.enchant.full", ""));
            return;
          }
          double enchantLevel = PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.ENCHANTING);
          double itemLevel = MaterialUtil.getItemLevel(targetItem);
          addAmount =
              2 + (int) (random.nextDouble() * (2 + Math.max(0, (enchantLevel - itemLevel) * 0.2)));
          str = str.replace("" + ChatColor.BLACK, "");
          str = new StringBuilder(str)
              .insert(Math.min(str.length() - 3, barIndex + addAmount), ChatColor.BLACK + "")
              .toString();
          lore.set(index, str);
        }
        index++;
      }
      if (valid) {
        ItemStackExtensionsKt.setLore(targetItem, lore);
        plugin.getStrifePlugin().getSkillExperienceManager()
            .addExperience(player, LifeSkillType.ENCHANTING, 10f + addAmount, false);
        sendMessage(player, plugin.getSettings().getString("language.enchant.refill", ""));
        player.playSound(player.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1F, 1.2F);
        player.playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1F, 1F);
        updateItem(event, targetItem);
      }
    }
  }

  private void updateItem(InventoryClickEvent e, ItemStack currentItem) {
    e.setCurrentItem(currentItem);
    e.getCursor().setAmount(e.getCursor().getAmount() - 1);
    if (e.getCursor().getAmount() <= 0) {
      e.setCursor(null);
    }
    e.setCancelled(true);
    e.setResult(Event.Result.DENY);
    ((Player) e.getWhoClicked()).updateInventory();
  }

  private boolean isBlockWithinRadius(Material material, Location location, int radius) {
    int minX = location.getBlockX() - radius;
    int maxX = location.getBlockX() + radius;
    int minY = location.getBlockY() - radius;
    int maxY = location.getBlockY() + radius;
    int minZ = location.getBlockZ() - radius;
    int maxZ = location.getBlockZ() + radius;
    for (int x = minX; x < maxX; x++) {
      for (int y = minY; y < maxY; y++) {
        for (int z = minZ; z < maxZ; z++) {
          Block block = location.getWorld().getBlockAt(x, y, z);
          if (block.getType() == material) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean isBannedMaterial(ItemStack item) {
    switch (item.getType()) {
      case BOOK:
      case EMERALD:
      case PAPER:
      case NETHER_STAR:
      case DIAMOND:
      case GHAST_TEAR:
      case ENCHANTED_BOOK:
      case NAME_TAG:
      case ARROW:
      case QUARTZ:
        return true;
      default:
        return false;
    }
  }

}
