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
package info.faceland.loot.menu.salvage;

import static info.faceland.loot.utils.MaterialUtil.getLevelRequirement;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.data.CraftToolData;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.PlayerDataUtil;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SalvageConfirmIcon extends MenuItem {

  private final SalvageMenu menu;

  SalvageConfirmIcon(SalvageMenu menu) {
    super(TextUtils.color("&cClick To Destroy!"), new ItemStack(Material.BARRIER));
    ItemStackExtensionsKt.setCustomModelData(getIcon(), 50);
    this.menu = menu;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    ItemStack stack = getIcon().clone();
    if (menu.getEquipment(player) == null || menu.getEquipment(player).getAmount() < 1 ||
        menu.getTool(player) == null || menu.getTool(player).getAmount() < 1) {
      ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color("&7&lSelect Tools And Equipment"));
      return stack;
    }

    int itemLevel = getLevelRequirement(menu.getEquipment(player));
    int craftingLevel = PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.CRAFTING);

    List<String> lore = new ArrayList<>();
    if (craftingLevel + 10 < itemLevel) {
      ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color("&e&lCraft Level Too Low!"));
      lore.add(FaceColor.LIGHT_GRAY + "This item requires Lvl " + (itemLevel - 10));
      TextUtils.setLore(stack, lore, false);
      return stack;
    }

    CraftToolData craftToolData = menu.getPlugin().getSalvageManager()
        .getToolData(menu.getTool(player));
    if (craftingLevel < craftToolData.getLevel()) {
      ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color("&e&lCraft Level Too Low!"));
      lore.add(FaceColor.LIGHT_GRAY + "This tool requires Lvl " + craftToolData.getLevel());
      TextUtils.setLore(stack, lore, false);
      return stack;
    }

    Material material = menu.getPlugin().getCraftMaterialManager().getMaterial(menu.getEquipment(player));
    if (material == null) {
      ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color("&e&lNo Known Materials"));
      lore.add(FaceColor.LIGHT_GRAY + "It appears as if this can't be salvaged...");
      TextUtils.setLore(stack, lore, false);
      return stack;
    }

    ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color("&a&lClick To Salvage!"));
    lore.add(FaceColor.LIGHT_GRAY + "This will destroy the selected");
    lore.add(FaceColor.LIGHT_GRAY + "equipment item and return some");
    lore.add(FaceColor.LIGHT_GRAY + "materials. You might also capture");
    lore.add(FaceColor.LIGHT_GRAY + "a stat from it an an Essence!");
    TextUtils.setLore(stack, lore, false);
    return stack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    if (event.getClickType() == ClickType.DOUBLE_CLICK) {
      event.setWillUpdate(false);
      event.setWillClose(false);
      return;
    }
    menu.doDestroy(event.getPlayer());
    event.setWillUpdate(true);
    event.setWillClose(false);
  }
}
