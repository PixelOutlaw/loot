/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.menu.tomereader;

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.items.prefabs.UnidEnchant;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class TomeOptionIcon extends MenuItem {

  private final LootPlugin plugin;
  private final ReaderMenu menu;
  private final int index;

  TomeOptionIcon(LootPlugin plugin, ReaderMenu menu, int index) {
    super(TextUtils.color("&cClick to cut a gem!"), ItemUtils.BLANK.clone());
    this.menu = menu;
    this.index = index;
    this.plugin = plugin;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    var tomeOptions = ReaderMenu.TOME_OPTIONS.get(player.getUniqueId());
    switch (index) {
      case 0 -> {
        return tomeOptions.size() == 4 ? tomeOptions.getFirst().item() : new ItemStack(Material.AIR);
      }
      case 1 -> {
        return tomeOptions.size() == 3 ? tomeOptions.getFirst().item() : new ItemStack(Material.AIR);
      }
      case 2 -> {
        if (tomeOptions.size() == 2) {
          return tomeOptions.getFirst().item();
        }
        if (tomeOptions.size() == 4) {
          return tomeOptions.get(1).item();
        }
        return new ItemStack(Material.AIR);
      }
      case 3 -> {
        if (tomeOptions.size() == 1) {
          return tomeOptions.getFirst().item();
        }
        if (tomeOptions.size() == 3) {
          return tomeOptions.get(1).item();
        }
        return new ItemStack(Material.AIR);
      }
      case 4 -> {
        if (tomeOptions.size() == 2) {
          return tomeOptions.get(1).item();
        }
        if (tomeOptions.size() == 4) {
          return tomeOptions.get(2).item();
        }
        return new ItemStack(Material.AIR);
      }
      case 5 -> {
        return tomeOptions.size() == 3 ? tomeOptions.get(2).item() : new ItemStack(Material.AIR);
      }
      case 6 -> {
        return tomeOptions.size() == 4 ? tomeOptions.get(3).item() : new ItemStack(Material.AIR);
      }
      default -> new ItemStack(Material.AIR);
    }
    return new ItemStack(Material.AIR);
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    if (event.getClickType() == ClickType.DOUBLE_CLICK) {
      event.setWillUpdate(false);
      event.setWillClose(false);
      return;
    }
    event.setWillUpdate(false);
    event.setWillClose(true);
    var tomeOptions = ReaderMenu.TOME_OPTIONS.get(event.getPlayer().getUniqueId());
    switch (index) {
      case 0 -> {
        if (tomeOptions.size() == 4) {
          menu.rewardOptions(event.getPlayer(), 0);
        }
      }
      case 1 -> {
        if (tomeOptions.size() == 3) {
          menu.rewardOptions(event.getPlayer(), 0);
        }
      }
      case 2 -> {
        if (tomeOptions.size() == 2) {
          menu.rewardOptions(event.getPlayer(), 0);
        } else if (tomeOptions.size() == 4) {
          menu.rewardOptions(event.getPlayer(), 1);
        }
      }
      case 3 -> {
        if (tomeOptions.size() == 1) {
          menu.rewardOptions(event.getPlayer(), 0);
        }
        if (tomeOptions.size() == 3) {
          menu.rewardOptions(event.getPlayer(), 1);
        }
      }
      case 4 -> {
        if (tomeOptions.size() == 2) {
          menu.rewardOptions(event.getPlayer(), 1);
        } else if (tomeOptions.size() == 4) {
          menu.rewardOptions(event.getPlayer(), 2);
        }
      }
      case 5 -> {
        if (tomeOptions.size() == 3) {
          menu.rewardOptions(event.getPlayer(), 2);
        }
      }
      case 6 -> {
        if (tomeOptions.size() == 4) {
          menu.rewardOptions(event.getPlayer(), 3);
        }
      }
    }
    var stack = event.getPlayer().getEquipment().getItemInMainHand();
    if (stack.getAmount() > 1 && event.getClickType().isRightClick() &&
        UnidEnchant.matches(event.getPlayer().getEquipment().getItemInMainHand())) {
      event.setWillClose(false);
      event.setWillUpdate(true);
      stack.setAmount(stack.getAmount() - 1);
      ReaderMenu.generateOptions(plugin, event.getPlayer());
      //ReaderMenu.show(event.getPlayer());
    }
  }
}
