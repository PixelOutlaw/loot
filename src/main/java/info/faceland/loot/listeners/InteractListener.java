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
import static info.faceland.loot.listeners.DeconstructListener.getHexFromString;
import static info.faceland.loot.listeners.DeconstructListener.isValidStealColor;
import static info.faceland.loot.utils.InventoryUtil.getFirstColor;
import static info.faceland.loot.utils.MaterialUtil.FAILURE_BONUS;
import static org.bukkit.ChatColor.stripColor;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang.WordUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.data.GemCacheData;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.items.prefabs.ShardOfFailure;
import info.faceland.loot.items.prefabs.TinkerersGear;
import info.faceland.loot.listeners.crafting.PreCraftListener;
import info.faceland.loot.managers.SocketGemManager;
import info.faceland.loot.math.LootRandom;
import info.faceland.loot.menu.upgrade.EnchantMenu;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class InteractListener implements Listener {

  private final LootPlugin plugin;
  private final LootRandom random;

  private final boolean customEnchantingEnabled;
  private final String noDropMessage;
  private final Set<UUID> dropFromInvySet = new HashSet<>();

  public InteractListener(LootPlugin plugin) {
    this.plugin = plugin;
    this.random = new LootRandom();
    customEnchantingEnabled = plugin.getSettings().getBoolean("config.custom-enchanting", true);
    noDropMessage = StringExtensionsKt.chatColorize(plugin.getSettings().getString(
        "language.generic.no-drop", "aaaa"));
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

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerDropItem(InventoryClickEvent e) {
    if (e.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
      return;
    }
    //Bukkit.getLogger().info("cursor: " + e.getCursor());
    if (e.getAction() == InventoryAction.DROP_ALL_CURSOR ||
        e.getAction() == InventoryAction.DROP_ONE_CURSOR ||
        e.getAction() == InventoryAction.DROP_ALL_SLOT ||
        e.getAction() == InventoryAction.DROP_ONE_SLOT) {
      dropFromInvySet.add(e.getWhoClicked().getUniqueId());
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerDropItem(PlayerDropItemEvent e) {
    if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
      return;
    }
    if (dropFromInvySet.contains(e.getPlayer().getUniqueId())) {
      dropFromInvySet.remove(e.getPlayer().getUniqueId());
      return;
    }
    if (e.getPlayer().getInventory().firstEmpty() != -1 || e.getItemDrop().getItemStack()
        .isSimilar(e.getPlayer().getInventory().getItem(e.getPlayer().getInventory().getHeldItemSlot()))) {
      e.setCancelled(true);
      MessageUtils.sendMessage(e.getPlayer(), noDropMessage);
    }
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

  /*
  @EventHandler(priority = EventPriority.LOWEST)
  public void shearsEquip(InventoryClickEvent event) {
    if (event.getClickedInventory() == null || event.getClickedInventory().getType()
        != InventoryType.PLAYER) {
      return;
    }
    if (event.getSlot() == 39 && event.getCursor() != null &&
        event.getCursor().getType() == Material.SHEARS && !event.isShiftClick()) {
      event.setCancelled(true);
      if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
        event.getWhoClicked().getInventory().setItem(39, event.getCursor().clone());
        event.getCursor().setAmount(0);
      } else {
        ItemStack currentItem = event.getWhoClicked().getInventory().getItem(39).clone();
        event.getWhoClicked().getInventory().setItem(39, event.getCursor().clone());
        event.getCursor().setType(currentItem.getType());
        event.getCursor().setAmount(currentItem.getAmount());
        event.getCursor().setItemMeta(currentItem.getItemMeta());
      }
    }
  }
  */

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
    String targetItemName = ItemStackExtensionsKt.getDisplayName(targetItem);
    String cursorName = ItemStackExtensionsKt.getDisplayName(cursor);

    if (StringUtils.isBlank(cursorName)) {
      return;
    }

    String strippedCursorName = net.md_5.bungee.api.ChatColor.stripColor(cursorName);

    if (StringUtils.isBlank(targetItemName)) {
      targetItemName = WordUtils.capitalize(targetItem.getType().toString()
          .toLowerCase().replaceAll("_", " "));
    }

    if (strippedCursorName.startsWith("Socket Gem - ")) {
      String gemName = strippedCursorName.replace("Socket Gem - ", "");
      SocketGem gem = plugin.getSocketGemManager().getSocketGem(gemName);

      if (gem == null) {
        return;
      }

      if (!MaterialUtil.matchesGroups(targetItem, gem.getItemGroups())) {
        sendMessage(player, plugin.getSettings().getString("language.socket.failure", ""));
        player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
        return;
      }

      List<String> lore = TextUtils.getLore(targetItem);
      int index = MaterialUtil.indexOfSocket(lore);
      if (index == -1) {
        sendMessage(player, plugin.getSettings().getString("language.socket.needs-sockets", ""));
        player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
        return;
      }

      if (gem.getLore().get(0).contains(" - ")) {
        String firstLine = ChatColor.stripColor(gem.getLore().get(0));
        for (String l : lore) {
          if (l.contains(firstLine)) {
            sendMessage(player, plugin.getSettings().getString("language.socket.dupe-effect", ""));
            player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
            return;
          }
        }
      }

      List<String> addLore = TextUtils.color(gem.getLore());
      switch (gem.getCustomModelData()) {
        case 2005, 2004 -> addLore.set(0, SocketGemManager.GEM_SPECIAL_PREFIX + addLore.get(0));
        case 2003 -> addLore.set(0, SocketGemManager.GEM_4_PREFIX + addLore.get(0));
        case 2002 -> addLore.set(0, SocketGemManager.GEM_3_PREFIX + addLore.get(0));
        case 2001 -> addLore.set(0, SocketGemManager.GEM_2_PREFIX + addLore.get(0));
        default -> addLore.set(0, SocketGemManager.GEM_1_PREFIX + addLore.get(0));
      }

      lore.remove(index);
      lore.addAll(index, addLore);
      TextUtils.setLore(targetItem, lore);

      String strippedName = ChatColor.stripColor(targetItemName);
      int level = MaterialUtil.getUpgradeLevel(targetItemName);
      String rawName = strippedName.replace("+" + level + " ", "");

      String prefix = "";
      String suffix = "";
      if (!gem.getPrefix().isEmpty()) {
        if (!targetItemName.contains(gem.getPrefix())) {
          if (rawName.startsWith("The ")) {
            rawName = rawName.replace("The ", "");
            prefix = "The " + gem.getPrefix() + " ";
          } else {
            prefix = gem.getPrefix() + " ";
          }
        }
      }
      if (!gem.getSuffix().isEmpty()) {
        if (!strippedName.contains(gem.getSuffix())) {
          suffix = " " + gem.getSuffix();
        }
      }
      String newName = prefix + rawName + suffix;
      if (level > 0) {
        newName = "+" + level + " " + newName;
      }
      newName = targetItemName.replace(strippedName, newName);
      ItemStackExtensionsKt.setDisplayName(targetItem, newName);

      sendMessage(player, plugin.getSettings().getString("language.socket.success", ""));
      player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1L, 2.0F);
      updateItem(event, targetItem);
    } else if (ShardOfFailure.isSimilar(cursor)) {
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
    } else if (cursorName.equals(ChatColor.WHITE + "Item Rename Tag")) {
      doItemRenameEffects(targetItem, cursor, targetItemName, player, event);
    } else if (TinkerersGear.isSimilar(cursor)) {
      doTinkerEffects(targetItem, player, event);
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

  private void doTinkerEffects(ItemStack targetItem, Player player, InventoryClickEvent event) {
    List<String> lore = TextUtils.getLore(targetItem);
    Map<Integer, String> validTargetStats = new HashMap<>();
    int loreIndex = -1;
    for (String str : lore) {
      loreIndex++;
      if (FaceColor.CYAN.isStartOf(str)) {
        sendMessage(player, TextUtils.color(plugin.getSettings()
            .getString("language.tinker.only-one-crafted-stat", "Only one stat can be tinkered!")));
        return;
      }
      if (!ChatColor.stripColor(str).startsWith("+")) {
        continue;
      }
      net.md_5.bungee.api.ChatColor color = getHexFromString(str);
      if (color != null) {
        if (isValidStealColor(color.getColor())) {
          validTargetStats.put(loreIndex, str);
        }
        continue;
      }
      if (str.startsWith(ChatColor.GREEN + "") || str.startsWith(ChatColor.YELLOW + "")) {
        validTargetStats.put(loreIndex, str);
      }
    }

    if (validTargetStats.isEmpty()) {
      sendMessage(player, TextUtils.color(plugin.getSettings()
          .getString("language.tinker.no-valid-stats", "No valid stats to be tinkered!")));
      return;
    }

    List<String> itemLore = new ArrayList<>(TextUtils.getLore(targetItem));
    List<Integer> keysAsArray = new ArrayList<>(validTargetStats.keySet());
    int selectedIndex = keysAsArray.get(random.nextInt(keysAsArray.size()));
    itemLore.set(selectedIndex, FaceColor.CYAN + PreCraftListener.ESSENCE_SLOT_TEXT);
    TextUtils.setLore(targetItem, itemLore);

    player.playSound(player.getEyeLocation(), Sound.BLOCK_PISTON_EXTEND, 1F, 1.5F);
    plugin.getStrifePlugin().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.CRAFTING, 200, false, false);
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
