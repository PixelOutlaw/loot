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

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public final class ArcaneEnhancer {

  private static ItemStack item;

  public static void rebuild() {
    ItemStack stack = new ItemStack(Material.MAGMA_CREAM);
    ItemStackExtensionsKt.setDisplayName(stack, ChatColor.RED + "Arcane Enhancer");
    TextUtils.setLore(stack, TextUtils.color(Arrays.asList(
        "&7Use this at an enchantment",
        "&7table to &cenhance &7the power",
        "&7of an item's &9enchantment&7!",
        "",
        "&8&oEnhanced items cannot have",
        "&8&otheir enchantments refilled",
        "&8&oand will eventually revert",
        "&8&oto being (Enchantable)"
    )));
    stack.setDurability((short) 11);
    ItemStackExtensionsKt.setCustomModelData(stack, 79);
    stack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
    item = stack;
  }

  public static ItemStack get() {
    return item.clone();
  }
}
