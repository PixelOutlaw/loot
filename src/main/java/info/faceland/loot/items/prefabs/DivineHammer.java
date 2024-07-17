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
import info.faceland.loot.utils.PersistHelper;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class DivineHammer {

  public static final String TYPE_KEY = "reroll3";

  private static ItemStack item;

  public static void rebuild() {
    item = new ItemStack(Material.NETHER_STAR);
    ItemMeta meta = item.getItemMeta();
    String name = "Divine Smith's Hammer";
    meta.setDisplayName(FaceColor.RED + name);
    meta.setCustomModelData(13);
    List<String> lore = List.of(
        PaletteUtil.color("|ns|\uF808丌\uF808\uF801"),
        HALFWAY_SMALL + GUI.spacesOf(-countTitleSize(name) / 2) + FaceColor.BLACK + doSmallNameThing(name) + "\uF80C",
        centerSmallText(PaletteUtil.color("|lgray|This hammer can be used at")),
        centerSmallText(PaletteUtil.color("|lgray|an enchantment table to")),
        centerSmallText(PaletteUtil.color("|green|reroll |lgray|an item's stats!")),
        "",
        centerSmallText(PaletteUtil.color("|lgreen|Stats rerolled with this")),
        centerSmallText(PaletteUtil.color("|lgreen|item are always at least")),
        centerSmallText(PaletteUtil.color("|lgreen|20% between the minimum")),
        centerSmallText(PaletteUtil.color("|lgreen|and maximum value!")),
        "",
        centerSmallText(PaletteUtil.color("|yellow|Has a 80% to not be")),
        centerSmallText(PaletteUtil.color("|yellow|consumed on use!")),
        "",
        centerSmallText(PaletteUtil.color("|gray||i|This hammer of the Gods")),
        centerSmallText(PaletteUtil.color("|gray||i|fell from the heavens,")),
        centerSmallText(PaletteUtil.color("|gray||i|blessing all but the")),
        centerSmallText(PaletteUtil.color("|gray||i|person it struck, who")),
        centerSmallText(PaletteUtil.color("|gray||i|was instantly killed"))
    );
    meta.setLore(lore);

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, TYPE_KEY);

    item.setItemMeta(meta);
  }

  public static ItemStack item() {
    return item.clone();
  }

  public static ItemMeta meta() {
    return item.getItemMeta();
  }

  public static void update(ItemStack stack) {
    stack.setType(item.getType());
    stack.setItemMeta(meta());
  }

  public static boolean matches(ItemStack stack) {
    return PersistHelper.getItemType(stack).equals(TYPE_KEY);
  }
}
