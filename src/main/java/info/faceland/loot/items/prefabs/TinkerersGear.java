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
package info.faceland.loot.items.prefabs;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class TinkerersGear {

  private static ItemStack item;
  private static String TINKER_NAME = ChatColor.RED + "Tinkerer's Gear";

  public static void rebuild() {
    ItemStack stack = new ItemStack(Material.BRICK);
    ItemStackExtensionsKt.setDisplayName(stack, ChatColor.RED + "Tinkerer's Gear");
    ItemStackExtensionsKt.setLore(stack, TextUtils.color(Arrays.asList(
        "&7This perplexing gear can be",
        "&7used on an equipment item to",
        "&7turn one random &agreen&7/&eyellow",
        "&7stat into an &bEssence Slot&7!",
        "&8&oA strange item that seems to",
        "&8&obe more science than magic..."
    )));
    stack.setDurability((short) 12);
    ItemStackExtensionsKt.setCustomModelData(stack, 3000);
    item = stack;
  }

  public static boolean isSimilar(ItemStack stack) {
    return stack.getType() == item.getType() && MaterialUtil.getCustomData(stack) == 3000 && TINKER_NAME
        .equals(ItemStackExtensionsKt.getDisplayName(stack));
  }

  public static ItemStack get() {
    return item.clone();
  }
}
