/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.loot.items.prefabs;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class UnidentifiedItem extends ItemStack {

    public UnidentifiedItem(Material material, int level) {
        super(material);
        ItemStackExtensionsKt.setDisplayName(this, ChatColor.LIGHT_PURPLE + "Unidentified Item");
        if (level > -1) {
            TextUtils.setLore(this, Arrays.asList(
                    ChatColor.WHITE + "Item Level: " + level,
                    ChatColor.GRAY + "Find an " + ChatColor.DARK_PURPLE + "Identity Tome" + ChatColor.GRAY + " to",
                    ChatColor.GRAY + "reveal this item's stats!"));
        } else {
            TextUtils.setLore(this, Arrays.asList(
                    ChatColor.GRAY + "Find an " + ChatColor.DARK_PURPLE + "Identity Tome" + ChatColor.GRAY + " to",
                    ChatColor.GRAY + "reveal this item's stats!"));
        }
        this.setDurability((short) 11);
    }

}
