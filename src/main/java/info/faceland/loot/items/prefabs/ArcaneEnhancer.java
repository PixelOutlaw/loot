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

import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.PURPLE;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.RED;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.color;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.fakeNoShadow;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.noShadow;
import static info.faceland.loot.LootPlugin.ITEM_TYPE_KEY;
import static info.faceland.loot.LootPlugin.ITEM_VERSION;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;
import static info.faceland.loot.utils.ItemStyleUtil.HALFWAY_SMALL;
import static info.faceland.loot.utils.ItemStyleUtil.centerSmallText;
import static info.faceland.loot.utils.ItemStyleUtil.countTitleSize;
import static info.faceland.loot.utils.ItemStyleUtil.doSmallNameThing;

import com.sentropic.guiapi.gui.GUI;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.ItemStyleUtil;
import info.faceland.loot.utils.PersistHelper;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class ArcaneEnhancer {

  public static final String TYPE_KEY = "enhancer";

  private static ItemStack item;

  public static void rebuild() {
    item = new ItemStack(Material.MAGMA_CREAM);
    ItemMeta meta = item.getItemMeta();
    String name = "Arcane Enhancer";
    meta.displayName((color(Component.text(name), PURPLE)));
    meta.setCustomModelData(79);
    meta.setMaxStackSize(99);
    meta.lore(List.of(
        fakeNoShadow(Component.text("\uF801\uF808丏\uF808\uF802")),
        ItemStyleUtil.smallNameLoreLine(name),
        centerSmallText(PaletteUtil.convert(PaletteUtil.color("|lgray|Use this item to improve"))),
        centerSmallText(PaletteUtil.convert(PaletteUtil.color("|lgray|enchanted equipment!"))),
        Component.empty(),
        centerSmallText(PaletteUtil.convert(PaletteUtil.color("|lgray|Once used, the equipment"))),
        centerSmallText(PaletteUtil.convert(PaletteUtil.color("|lgray|can't have its enchant"))),
        centerSmallText(PaletteUtil.convert(PaletteUtil.color("|lgray|refilled, and will slowly"))),
        centerSmallText(PaletteUtil.convert(PaletteUtil.color("|lgray|revert to |blue|Enchantable")))
    ));
    meta.setTooltipStyle(NamespacedKey.fromString("item_rare"));

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, TYPE_KEY);

    item.setItemMeta(meta);
    ItemRenderer.applyCommonFlags(item);
  }

  public static ItemStack get() {
    return item.clone();
  }

  public static ItemMeta meta() {
    return item.getItemMeta();
  }

  public static void update(ItemStack stack) {
    int amount = stack.getAmount();
    ItemRenderer.replace(stack, item.clone(), false);
    stack.setAmount(amount);
  }

  public static boolean matches(ItemStack stack) {
    return PersistHelper.getItemType(stack).equals(TYPE_KEY);
  }
}
