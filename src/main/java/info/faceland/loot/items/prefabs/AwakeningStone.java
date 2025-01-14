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
import java.util.List;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class AwakeningStone {

  public static final String TYPE_KEY = "awakening";

  private static ItemStack item;

  public static void rebuild() {
    item = new ItemStack(Material.NETHER_STAR);
    ItemMeta meta = item.getItemMeta();
    String name = "Stone Of Awakening";
    meta.setDisplayName(FaceColor.RED + name);
    meta.setCustomModelData(16);
    meta.setMaxStackSize(99);
    List<String> lore = List.of(
        PaletteUtil.color("|ns|\uF801\uF808丌\uF808\uF802"),
        HALFWAY_SMALL + GUI.spacesOf(-countTitleSize(name) / 2) + FaceColor.TRUE_WHITE + doSmallNameThing(name) + "\uF80C",
        centerSmallText(PaletteUtil.color("|lgray|This item will unleash")),
        centerSmallText(PaletteUtil.color("|lgray|the power of non-unique")),
        centerSmallText(PaletteUtil.color("|lgray|equipment items!")),
        "",
        centerSmallText(PaletteUtil.color("|yellow|Adds an extra stat to an")),
        centerSmallText(PaletteUtil.color("|yellow|item, up to one greater")),
        centerSmallText(PaletteUtil.color("|yellow|than the normal maximum")),
        centerSmallText(PaletteUtil.color("|yellow|for that rarity!")),
        "",
        centerSmallText(PaletteUtil.color("|gray||i|All weapons, armor, and")),
        centerSmallText(PaletteUtil.color("|gray||i|tools possess a latent")),
        centerSmallText(PaletteUtil.color("|gray||i|power... it's just really")),
        centerSmallText(PaletteUtil.color("|gray||i|really sleepy"))
    );
    meta.setLore(lore);
    meta.setTooltipStyle(NamespacedKey.fromString("item_epic"));

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, TYPE_KEY);

    item.setItemMeta(meta);
    ItemRenderer.applyCommonFlags(item);
  }

  public static ItemStack item() {
    return item.clone();
  }

  public static ItemMeta meta() {
    return item.getItemMeta();
  }

  public static void update(ItemStack stack) {
    int amount = stack.getAmount();
    ItemRenderer.replace(stack, item.clone());
    stack.setAmount(amount);
  }

  public static boolean matches(ItemStack stack) {
    return PersistHelper.getItemType(stack).equals(TYPE_KEY);
  }
}
