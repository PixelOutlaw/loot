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
package info.faceland.loot.items.prefabs;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class ShardOfFailure {

  private static ItemStack item;
  public static String FAILURE_NAME = "Shard of Failure";

  public static ItemStack build(String name) {
    List<String> lore = new ArrayList<>();
    for (String s : TextUtils.getLore(item)) {
      s = s.replace("{name}", name);
      lore.add(s);
    }
    ItemStack shard = item.clone();
    TextUtils.setLore(shard, lore);
    return shard;
  }

  public static void rebuild() {
    ItemStack stack = new ItemStack(Material.PRISMARINE_SHARD);
    String name = FaceColor.BROWN + FAILURE_NAME;
    ItemStackExtensionsKt.setDisplayName(stack, name);
    TextUtils.setLore(stack, PaletteUtil.color(Arrays.asList(
        "|white|\uD86D\uDFE6Ս",
        "",
        "|lgray|Use this item on any",
        "|green|Upgrade Scroll |lgray|to raise",
        "|lgray|its success chance!",
        "",
        "|dgray||i|This failure is special",
        "|dgray||i|because it was made by",
        "|dgray||i|{name} :)"
    )));
    ItemStackExtensionsKt.setCustomModelData(stack, 500);
    item = stack;
  }

  public static boolean isSimilar(ItemStack stack) {
    return stack != null && stack.getType() == item.getType() && MaterialUtil.getCustomData(stack) == 500 &&
        FAILURE_NAME.equals(ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack)));
  }
}
