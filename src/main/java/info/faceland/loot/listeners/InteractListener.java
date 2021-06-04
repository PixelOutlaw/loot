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
import static info.faceland.loot.utils.InventoryUtil.getLastColor;
import static info.faceland.loot.utils.MaterialUtil.FAILURE_BONUS;
import static org.bukkit.ChatColor.stripColor;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.data.GemCacheData;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.items.prefabs.ShardOfFailure;
import info.faceland.loot.items.prefabs.TinkerersGear;
import info.faceland.loot.math.LootRandom;
import info.faceland.loot.menu.upgrade.EnchantMenu;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.tier.Tier;
import info.faceland.loot.utils.InventoryUtil;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.PlayerDataUtil;
import org.apache.commons.lang.WordUtils;
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
  private LootRandom random;

  private boolean customEnchantingEnabled;
  private String noDropMessage;
  private Set<UUID> dropFromInvySet = new HashSet<>();

  public InteractListener(LootPlugin plugin) {
    this.plugin = plugin;
    this.random = new LootRandom();
    customEnchantingEnabled = plugin.getSettings().getBoolean("config.custom-enchanting", true);
    noDropMessage = TextUtils
        .color(plugin.getSettings().getString("language.generic.no-drop", "aaaa"));
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

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerDropItem(InventoryClickEvent e) {
    if (e.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
      return;
    }
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
    e.setCancelled(true);
    MessageUtils.sendMessage(e.getPlayer(), noDropMessage);
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
        .getWhoClicked() instanceof Player)) {
      return;
    }

    Player player = (Player) event.getWhoClicked();
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
    List<String> lore = ItemStackExtensionsKt.getLore(targetItem);
    int failureBonus = MaterialUtil.getFailureBonus(targetItem);
    if (failureBonus > 0) {
      lore.set(0, StringExtensionsKt.chatColorize(FAILURE_BONUS + " +" + (failureBonus + amount)));
    } else {
      lore.add(0, StringExtensionsKt.chatColorize(FAILURE_BONUS + " +" + amount));
    }
    ItemStackExtensionsKt.setLore(targetItem, lore);
    event.setCurrentItem(targetItem);
    event.getCursor().setAmount(0);
    event.setCursor(null);
    event.setCancelled(true);
    event.setResult(Event.Result.DENY);
    player.updateInventory();

    player.playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1L, 2F);

    plugin.getStrifePlugin().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.ENCHANTING, amount * 22, false, false);
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
        !(event.getWhoClicked() instanceof Player)) {
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
        InventoryUtil.sendToDiscord(player, targetItem,
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
    } else if (ShardOfFailure.isSimilar(cursor)) {
      UpgradeScroll scroll = plugin.getScrollManager().getScroll(targetItem);
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
      plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
          LifeSkillType.ENCHANTING, 22, false, false);
    } else if (cursorName.equals(ChatColor.WHITE + "Item Rename Tag")) {
      doItemRenameEffects(targetItem, cursor, targetItemName, player, event);
    } else if (cursorName.startsWith(ChatColor.DARK_PURPLE + "Magic Crystal")) {
      doRechargeEffects(targetItem, player, event);
    } else if (TinkerersGear.isSimilar(cursor)) {
      doTinkerEffects(targetItem, player, event);
    }
  }

  private void doItemRenameEffects(ItemStack targetItem, ItemStack cursor, String targetItemName,
      Player player, InventoryClickEvent event) {
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
  }

  private void doRechargeEffects(ItemStack targetItem, Player player, InventoryClickEvent event) {
    List<String> lore = ItemStackExtensionsKt.getLore(targetItem);
    boolean valid = false;
    int index = 0;
    int addAmount = 0;
    for (String str : ItemStackExtensionsKt.getLore(targetItem)) {
      if (str.startsWith(ChatColor.BLUE + "[")) {
        if (str.contains("" + ChatColor.BLACK)) {
          valid = true;
          int barIndex = str.indexOf("" + ChatColor.BLACK);
          if (barIndex == str.length() - 5) {
            sendMessage(player, plugin.getSettings().getString("language.enchant.full", ""));
            return;
          }
          double enchantLevel = PlayerDataUtil
              .getLifeSkillLevel(player, LifeSkillType.ENCHANTING);
          double itemLevel = MaterialUtil.getLevelRequirement(targetItem);
          addAmount =
              2 + (int) (random.nextDouble() * (2 + Math
                  .max(0, (enchantLevel - itemLevel) * 0.2)));
          str = str.replace("" + ChatColor.BLACK, "");
          str = new StringBuilder(str)
              .insert(Math.min(str.length() - 3, barIndex + addAmount), ChatColor.BLACK + "")
              .toString();
          lore.set(index, str);
        } else if (str.contains("" + ChatColor.DARK_RED)) {
          sendMessage(player, TextUtils.color(plugin.getSettings().getString(
              "language.enchant.no-refill-enhanced", "you cant refill this enchant")));
          return;
        }
      }
      index++;
    }
    if (valid) {
      ItemStackExtensionsKt.setLore(targetItem, lore);
      plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
          LifeSkillType.ENCHANTING, 10f + addAmount, false, false);
      sendMessage(player, plugin.getSettings().getString("language.enchant.refill", ""));
      player.playSound(player.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1F, 1.2F);
      player.playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1F, 1F);
      updateItem(event, targetItem);
    }
  }

  private void doTinkerEffects(ItemStack targetItem, Player player, InventoryClickEvent event) {
    List<String> lore = ItemStackExtensionsKt.getLore(targetItem);
    Map<Integer, String> validTargetStats = new HashMap<>();
    int loreIndex = -1;
    for (String str : lore) {
      loreIndex++;
      if (str.startsWith(ChatColor.AQUA + "")) {
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

    String essenceText = StringExtensionsKt.chatColorize(
        plugin.getSettings().getString("config.crafting.essence-text", "&b(Essence Slot)"));

    List<String> itemLore = new ArrayList<>(ItemStackExtensionsKt.getLore(targetItem));
    List<Integer> keysAsArray = new ArrayList<>(validTargetStats.keySet());
    int selectedIndex = keysAsArray.get(random.nextInt(keysAsArray.size()));
    itemLore.set(selectedIndex, essenceText);
    ItemStackExtensionsKt.setLore(targetItem, itemLore);

    player.playSound(player.getEyeLocation(), Sound.BLOCK_PISTON_EXTEND, 1F, 1.5F);
    plugin.getStrifePlugin().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.CRAFTING, 200, false, false);
    updateItem(event, targetItem);
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
