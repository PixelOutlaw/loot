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
package info.faceland.loot.menu.gemcutter;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.PlayerDataUtil;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ResultIcon extends MenuItem {

  private final GemcutterMenu menu;

  ResultIcon(GemcutterMenu menu) {
    super(TextUtils.color("&eClick Gems To Begin!"), new ItemStack(Material.BARRIER));
    this.menu = menu;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    if (menu.getSelectedStack(player) == null || menu.getSelectedStack(player).getAmount() < 1) {
      ItemStack stack = getIcon().clone();
      ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color("&7No gem selected"));
      return stack;
    }
    int levelReq = Integer.parseInt(ChatColor.stripColor(
        TextUtils.getLore(menu.getSelectedStack(player)).get(0)).replace("Item Level: ", "")) - 10;
    if (levelReq > PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.CRAFTING)) {
      ItemStack stack = getIcon().clone();
      ItemStackExtensionsKt.setDisplayName(stack,
          TextUtils.color("&cRequires: &fCrafting " + levelReq));
      return stack;
    }
    ItemStack stack = new ItemStack(Material.NETHER_STAR);
    ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color("&aReady to cut!"));
    return stack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    event.setWillUpdate(true);
    event.setWillClose(false);
  }
}
