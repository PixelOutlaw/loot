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

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public final class TinkerersGear {

  private static ItemStack item;
  private static String TINKER_NAME = "Tinkerer's Gear";

  public static void rebuild() {
    ItemStack stack = new ItemStack(Material.BRICK);
    ItemStackExtensionsKt.setDisplayName(stack, FaceColor.RED + TINKER_NAME);
    TextUtils.setLore(stack, PaletteUtil.color(Arrays.asList(
        "|white|\uD86D\uDFE9Ս",
        "",
        "|lgray|This perplexing gear can be",
        "|lgray|used on an equipment item to",
        "|lgray|turn one random |lgreen|green|lgray|/|yellow|yellow",
        "|lgray|stat into an |cyan|Essence Slot|lgray|!",
        "",
        "|dgray||i|A strange item that seems to",
        "|dgray||i|be more science than magic..."
    )));
    stack.setDurability((short) 12);
    ItemStackExtensionsKt.setCustomModelData(stack, 3000);
    stack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
    item = stack;
  }

  public static boolean isSimilar(ItemStack stack) {
    return stack != null && stack.getType() == item.getType() && MaterialUtil.getCustomData(stack) == 3000 && TINKER_NAME
        .equals(ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack)));
  }

  public static ItemStack get() {
    return item.clone();
  }
}
