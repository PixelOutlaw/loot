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
import info.faceland.loot.items.ItemBuilder;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Arrays;
import land.face.strife.util.ItemUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class SocketExtender {

  public static String name = "Socket Extender";
  public static ItemStack EXTENDER = build();

  public static ItemStack build() {
    ItemStack stack = new ItemStack(Material.NETHER_STAR);
    ItemStackExtensionsKt.setDisplayName(stack, FaceColor.PURPLE + name);
    TextUtils.setLore(stack, PaletteUtil.color(Arrays.asList(
        "|white|\uD86D\uDFE8Ս",
        "",
        "|lgray|An upgrade item that can",
        "|lgray|be used on equipment with",
        "|lgray|a locked " + ItemBuilder.EXTEND + "|lgray| to add",
        "|lgray|an extra " + ItemBuilder.SOCKET)
    ));
    ItemStackExtensionsKt.setCustomModelData(stack, 10);
    stack.setDurability((short) 11);
    return stack;
  }

  public static boolean isSimilar(ItemStack stack) {
    if (stack.isSimilar(EXTENDER)) {
      return true;
    }
    if (stack.getType() != Material.NETHER_STAR) {
      return false;
    }
    if (!name.equals(ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack)))) {
      return false;
    }
    return stack.getDurability() == (short) 11;
  }
}
