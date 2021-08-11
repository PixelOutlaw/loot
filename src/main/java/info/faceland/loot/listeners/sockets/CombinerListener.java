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
package info.faceland.loot.listeners.sockets;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.utils.InventoryUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public final class CombinerListener implements Listener {

  private LootPlugin plugin;
  private ItemStack clickToCombineButton;
  private final String transmuteFormat;

  public CombinerListener(LootPlugin plugin) {
    this.plugin = plugin;
    this.clickToCombineButton = SetupClickToCombineButton();
    this.transmuteFormat = plugin.getSettings().getString("language.broadcast.transmute-gem", "");
  }

  @EventHandler
  public void onGemAddToCombiner(InventoryClickEvent event) {
    if (!ChatColor.stripColor(event.getView().getTitle()).equals("Socket Gem Combiner")) {
      return;
    }
    event.setResult(Event.Result.DENY);
    if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
      return;
    }
    if (!(event.getClickedInventory().getHolder() instanceof HumanEntity)) {
      return;
    }

    Player player = (Player) event.getWhoClicked();
    Inventory invy = event.getInventory();
    Inventory playerInvy = event.getWhoClicked().getInventory();

    ItemStack clickedItem = new ItemStack(event.getCurrentItem());
    ItemStack resultSlotItem = new ItemStack(Material.BARRIER);
    if (invy.getItem(31) != null) {
      resultSlotItem = new ItemStack(invy.getItem(31));
    }

    if (!isSocketGem(clickedItem)) {
      MessageUtils
          .sendMessage(player, plugin.getSettings().getString("language.socket.must-be-gem", ""));
      return;
    }

    if (isSocketGem(resultSlotItem)) {
      MessageUtils.sendMessage(player,
          plugin.getSettings().getString("language.socket.pls-claim-first", ""));
      return;
    }

    int firstEmptyCombinerSlot = getEmptySocketCombinerSlot(invy);

    if (firstEmptyCombinerSlot != -1) {
      ItemStack newGem = clickedItem.clone();
      ItemStack oldGem = clickedItem.clone();

      newGem.setAmount(1);
      oldGem.setAmount(oldGem.getAmount() - 1);

      invy.setItem(firstEmptyCombinerSlot, newGem);
      playerInvy.setItem(event.getSlot(), oldGem);

      player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1L, 1.8F);

      if (getEmptySocketCombinerSlot(invy) == -1) {
        player.playSound(player.getEyeLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1L, 2.0F);
        player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1L, 1.0F);
        invy.setItem(31, clickToCombineButton);
      }
    }
  }

  @EventHandler
  public void onGemRemoveFromCombiner(InventoryClickEvent event) {
    if (!ChatColor.stripColor(event.getView().getTitle()).equals("Socket Gem Combiner")) {
      return;
    }
    event.setResult(Event.Result.DENY);
    if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
      return;
    }
    if (event.getClickedInventory().getHolder() instanceof HumanEntity) {
      return;
    }

    Player player = (Player) event.getWhoClicked();
    Inventory invy = event.getInventory();
    Inventory playerInvy = player.getInventory();

    ItemStack his = new ItemStack(event.getCurrentItem());

    if (!isSocketGem(his) && !isCombineButton(his)) {
      return;
    }

    if (event.getSlot() == 31) {
      if (isCombineButton(his) && getEmptySocketCombinerSlot(invy) == -1) {
        ItemStack gem = RandomTransmutedGem((Player) event.getWhoClicked());
        playTransmuteEffects((Player) event.getWhoClicked());
        invy.setItem(10, null);
        invy.setItem(12, null);
        invy.setItem(14, null);
        invy.setItem(16, null);
        invy.setItem(event.getSlot(), gem);
        return;
      }
      if (isSocketGem(his)) {
        player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1L, 1.8F);
        invy.setItem(event.getSlot(), null);
        if (playerInvy.firstEmpty() == -1) {
          event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), his);
          return;
        } else {
          playerInvy.addItem(his);
          return;
        }
      }
      invy.setItem(event.getSlot(), null);
    }

    if (event.getSlot() == 10 || event.getSlot() == 12 || event.getSlot() == 14
        || event.getSlot() == 16) {
      if (playerInvy.firstEmpty() != -1) {
        playerInvy.addItem(his);
      } else {
        event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), his);
      }
      invy.setItem(event.getSlot(), null);
      if (invy.getItem(31) != null) {
        player.playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1L, 1.2F);
        invy.setItem(31, null);
      }
    }
  }

  @EventHandler
  public void onInventoryOpen(InventoryOpenEvent event) {
    Inventory inventory = event.getInventory();
    InventoryHolder holder = inventory.getHolder();
    if (!(holder instanceof Chest)) {
      return;
    }
    /*
    00 01 02 03 04 05 06 07 08
    09 10 11 12 13 14 15 16 17
    18 19 20 21 22 23 24 25 26
    27 28 29 30 31 32 33 34 35
    36 37 38 39 40 41 42 43 44
    45 46 47 48 49 50 51 52 53
    */

    //ItemStack stack = inventory.getItem(13);
    //if (stack == null || stack.getType() != Material.NETHER_STAR || !"Transmutation Core"
    //    .equals(ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack)))) {
    //  return;
    //}

    String chestName = ((Chest) holder).getCustomName();
    if (StringUtils.isBlank(chestName)) {
      return;
    }
    if (!chestName.equals(ChatColor.DARK_PURPLE + "GEM_CHEST")) {
      return;
    }

    event.setCancelled(true);
    Inventory toShow = Bukkit.createInventory(null, 45, TextUtils.color("&5&lSocket Gem Combiner"));
    ItemStack buffer = new ItemStack(Material.IRON_BARS);
    ItemStackExtensionsKt
        .setDisplayName(buffer, TextUtils.color("&aClick a &6Socket Gem &ato begin!"));
    for (int slot = 0; slot < toShow.getSize(); slot++) {
      if (slot == 10 || slot == 12 || slot == 14 || slot == 16 || slot == 31) {
        continue;
      }
      toShow.setItem(slot, buffer);
    }
    toShow.setMaxStackSize(1);
    event.getPlayer().openInventory(toShow);
  }

  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    if (!ChatColor.stripColor(event.getView().getTitle()).equals("Socket Gem Combiner")) {
      return;
    }
    List<ItemStack> contents = new ArrayList<>();

    contents.add(event.getInventory().getItem(10));
    contents.add(event.getInventory().getItem(12));
    contents.add(event.getInventory().getItem(14));
    contents.add(event.getInventory().getItem(16));
    contents.add(event.getInventory().getItem(31));

    HumanEntity c = event.getPlayer();

    for (ItemStack content : contents) {
      if (content == null || content.getType() != Material.EMERALD) {
        continue;
      }
      if (c.getInventory().firstEmpty() != -1) {
        c.getInventory().addItem(content);
      } else {
        c.getWorld().dropItem(c.getLocation(), content);
      }
    }
  }

  private int getEmptySocketCombinerSlot(Inventory invy) {
    if (invy.getItem(10) == null || invy.getItem(10).getType() == Material.AIR) {
      return 10;
    }
    if (invy.getItem(12) == null || invy.getItem(12).getType() == Material.AIR) {
      return 12;
    }
    if (invy.getItem(14) == null || invy.getItem(14).getType() == Material.AIR) {
      return 14;
    }
    if (invy.getItem(16) == null || invy.getItem(16).getType() == Material.AIR) {
      return 16;
    }
    return -1;
  }

  private ItemStack SetupClickToCombineButton() {
    ItemStack clickToCombineButton = new ItemStack(Material.NETHER_STAR);
    clickToCombineButton.setAmount(1);
    ItemStackExtensionsKt
        .setDisplayName(clickToCombineButton, TextUtils.color("&e&lClick to combine!"));
    return clickToCombineButton;
  }

  private ItemStack RandomTransmutedGem(Player player) {
    SocketGem gem = plugin.getSocketGemManager().getRandomSocketGemByBonus();
    ItemStack gemItem = gem.toItemStack(1);
    if (gem.isBroadcast()) {
      InventoryUtil.sendToDiscord(player, gemItem, transmuteFormat);
    }
    return gemItem;
  }

  private void playTransmuteEffects(Player player) {
    MessageUtils.sendMessage(player,
        plugin.getSettings().getString("language.socket.transmute-success", ""));
    player.getWorld()
        .spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().clone().add(0, 1, 0), 30, 5,
            5, 5);
    player.getWorld().playSound(player.getLocation().clone(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
    player.getWorld()
        .playSound(player.getLocation().clone(), Sound.BLOCK_END_GATEWAY_SPAWN, 1.0f, 2f);
  }

  private boolean isSocketGem(ItemStack item) {
    if (item == null || item.getType() != Material.EMERALD) {
      return false;
    }
    String name = ItemStackExtensionsKt.getDisplayName(item);
    return StringUtils.isNotBlank(name) && name.startsWith(ChatColor.GOLD + "Socket Gem - ");
  }

  private boolean isCombineButton(ItemStack item) {
    return item.isSimilar(clickToCombineButton);
  }
}
