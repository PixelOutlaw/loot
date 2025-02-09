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

import static info.faceland.loot.LootPlugin.ITEM_TYPE_KEY;
import static info.faceland.loot.LootPlugin.ITEM_VERSION;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;
import static info.faceland.loot.utils.ItemStyleUtil.HALFWAY_SMALL;
import static info.faceland.loot.utils.ItemStyleUtil.centerSmallText;
import static info.faceland.loot.utils.ItemStyleUtil.countTitleSize;
import static info.faceland.loot.utils.ItemStyleUtil.doSmallNameThing;

import com.sentropic.guiapi.gui.GUI;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class SocketExtender {

  public static final String TYPE_KEY = "extender";

  public static ItemStack EXTENDER = rebuild();

  public static ItemStack item() {
    return EXTENDER.clone();
  }

  public static ItemMeta meta() {
    return EXTENDER.getItemMeta();
  }

  public static void update(ItemStack stack) {
    int amount = stack.getAmount();
    ItemRenderer.replace(stack, EXTENDER.clone());
    stack.setAmount(amount);
  }

  public static boolean matches(ItemStack stack) {
    return PersistHelper.getItemType(stack).equals(TYPE_KEY);
  }

  public static ItemStack rebuild() {
    EXTENDER = new ItemStack(Material.NETHER_STAR);
    ItemMeta meta = EXTENDER.getItemMeta();
    String name = "Socket Extender";
    meta.setDisplayName(FaceColor.YELLOW + name);
    List<String> lore = List.of(
        PaletteUtil.color("|ns|\uF801\uF808三\uF808\uF802"),
        HALFWAY_SMALL + GUI.spacesOf(-countTitleSize(name) / 2) + FaceColor.TRUE_WHITE + doSmallNameThing(name) + "\uF80C",
        centerSmallText(PaletteUtil.color("|lgray|This |red|powerful |lgray|item can be")),
        centerSmallText(PaletteUtil.color("|lgray|used on equipment to turn")),
        centerSmallText(PaletteUtil.color("|lgray|a |yellow|locked socket |lgray|into a")),
        centerSmallText(PaletteUtil.color("|lgray|normal |orange|socket|lgray|!"))
    );
    meta.setLore(lore);
    meta.setCustomModelData(10);
    meta.setMaxStackSize(99);
    meta.setTooltipStyle(NamespacedKey.fromString("item_epic"));

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, TYPE_KEY);

    EXTENDER.setItemMeta(meta);
    ItemRenderer.applyCommonFlags(EXTENDER);
    return EXTENDER;
  }

  @Deprecated
  public static boolean isSimilar(ItemStack stack) {
    if (stack == null) {
      return false;
    }
    if (stack.isSimilar(EXTENDER)) {
      return true;
    }
    if (stack.getType() != Material.NETHER_STAR) {
      return false;
    }
    if (!"Socket Extender".equals(ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack)))) {
      return false;
    }
    return stack.getDurability() == (short) 11;
  }
}
