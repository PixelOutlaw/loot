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
package info.faceland.loot.menu.transmute;

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TransmuteMenu extends ItemMenu {

  private final SelectedGem selectedGem1;
  private final SelectedGem selectedGem2;
  private final SelectedGem selectedGem3;
  private final SelectedGem selectedGem4;
  private final ResultIcon resultIcon;

/*
00 01 02 03 04 05 06 07 08
09 10 11 12 13 14 15 16 17
18 19 20 21 22 23 24 25 26
27 28 29 30 31 32 33 34 35
36 37 38 39 40 41 42 43 44
45 46 47 48 49 50 51 52 53
*/

  public TransmuteMenu(LootPlugin plugin) {
    super(TextUtils.color("&f\uF808玕"), Size.fit(36), plugin);

    selectedGem1 = new SelectedGem(this);
    selectedGem2 = new SelectedGem(this);
    selectedGem3 = new SelectedGem(this);
    selectedGem4 = new SelectedGem(this);

    resultIcon = new ResultIcon(this);

    setItem(1, selectedGem1);
    setItem(3, selectedGem2);
    setItem(5, selectedGem3);
    setItem(7, selectedGem4);

    setItem(31, resultIcon);

    //fillEmptySlots(new TransparentIcon());
  }

  public void attemptGemEntry(Player player, ItemStack stack, int slot) {
    if (!isSocketGem(stack)) {
      MessageUtils.sendMessage(player, LootPlugin.getInstance()
          .getSettings().getString("language.socket.must-be-gem", ""));
      return;
    }
    boolean change = false;

    int slots = 0;
    if (selectedGem1.getSlot() == slot) {
      slots++;
    }
    if (selectedGem2.getSlot() == slot) {
      slots++;
    }
    if (selectedGem3.getSlot() == slot) {
      slots++;
    }
    if (selectedGem4.getSlot() == slot) {
      slots++;
    }

    if (selectedGem1.getStack() == null) {
      if (slots >= stack.getAmount()) {
        return;
      }
      ItemStack newStack = stack.clone();
      newStack.setAmount(1);
      selectedGem1.update(newStack, slot);
      player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1L, 1.5F);
      change = true;
    } else if (selectedGem2.getStack() == null) {
      if (slots >= stack.getAmount()) {
        return;
      }
      ItemStack newStack = stack.clone();
      newStack.setAmount(1);
      selectedGem2.update(newStack, slot);
      player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1L, 1.6F);
      change = true;
    } else if (selectedGem3.getStack() == null) {
      if (slots >= stack.getAmount()) {
        return;
      }
      ItemStack newStack = stack.clone();
      newStack.setAmount(1);
      selectedGem3.update(newStack, slot);
      player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1L, 1.7F);
      change = true;
    } else if (selectedGem4.getStack() == null) {
      if (slots >= stack.getAmount()) {
        return;
      }
      ItemStack newStack = stack.clone();
      newStack.setAmount(1);
      selectedGem4.update(newStack, slot);
      player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1L, 1.8F);
      change = true;
    }
    if (change) {
      validateResult(player);
    }
    update(player);
  }

  private boolean isSocketGem(ItemStack item) {
    if (item == null || item.getType() != Material.EMERALD) {
      return false;
    }
    String name = ItemStackExtensionsKt.getDisplayName(item);
    return StringUtils.isNotBlank(name) &&
        net.md_5.bungee.api.ChatColor.stripColor(name).startsWith("Socket Gem - ");
  }

  public void validateResult(Player player) {
    if (isReady()) {
      player.playSound(player.getEyeLocation(), Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1L, 2.0F);
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1L, 1.0F);
    }
  }

  public boolean isReady() {
    return selectedGem1.getStack() != null && selectedGem2.getStack() != null &&
        selectedGem3.getStack() != null && selectedGem4.getStack() != null;
  }

  public boolean takeGems(Player player) {
    Map<ItemStack, Integer> removeItems = new HashMap<>();
    removeItems.put(selectedGem1.getStack(), 1);

    if (player.getInventory().containsAtLeast(selectedGem1.getStack(), 1) &&
        player.getInventory().containsAtLeast(selectedGem2.getStack(), 1) &&
        player.getInventory().containsAtLeast(selectedGem3.getStack(), 1) &&
        player.getInventory().containsAtLeast(selectedGem4.getStack(), 1)) {

      List<ItemStack> refundItems = new ArrayList<>();

      if (!removeItemStrict(selectedGem1.getStack(), player, refundItems) ||
          !removeItemStrict(selectedGem2.getStack(), player, refundItems) ||
          !removeItemStrict(selectedGem3.getStack(), player, refundItems) ||
          !removeItemStrict(selectedGem4.getStack(), player, refundItems)) {
        for (ItemStack is : refundItems) {
          player.getInventory().addItem(is);
        }
        return false;
      }

      selectedGem1.clear();
      selectedGem2.clear();
      selectedGem3.clear();
      selectedGem4.clear();

      return true;
    }
    return false;
  }

  // Never use this for stacks of items with a size above 1
  private boolean removeItemStrict(ItemStack stack, Player player, List<ItemStack> refundItems) {
    HashMap<Integer, ItemStack> r;
    r = player.getInventory().removeItem(stack);
    if (r.isEmpty()) {
      refundItems.add(stack);
      return true;
    }
    return false;
  }

  public void ejectResult(Player player) {
    if (resultIcon.getStack() != null) {
      ItemUtils.giveOrDrop(player, resultIcon.getStack());
      resultIcon.setStack(null);
    }
  }
}
