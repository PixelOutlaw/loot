/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.menu.pawn;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.data.PawnShopType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nunnerycode.mint.MintPlugin;

public class SaleIcon extends MenuItem {

  private final PawnMenu menu;
  private final Integer slot;
  private double price;
  private ItemStack targetStack;
  private boolean checkRare = false;

  SaleIcon(PawnMenu menu, Integer slot) {
    super("", new ItemStack(Material.AIR));
    this.menu = menu;
    this.slot = slot;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    if (targetStack == null) {
      return getIcon();
    }
    ItemStack finalIcon = targetStack.clone();
    List<String> newLore = new ArrayList<>(TextUtils.getLore(finalIcon));
    newLore.add("");
    double displayPrice = price;
    boolean bonus = false;
    if (menu.getDealId() != null) {
      PawnShopType type = menu.getPlugin().getPawnManager().getPawnTypes().get(menu.getDealId());
      if (type.getDealOne().matches(targetStack)) {
        displayPrice *= type.getDealOne().getMultiplier();
        bonus = true;
      } else if (type.getDealTwo().matches(targetStack)) {
        displayPrice *= type.getDealTwo().getMultiplier();
        bonus = true;
      } else if (type.getDealThree().matches(targetStack)) {
        displayPrice *= type.getDealThree().getMultiplier();
        bonus = true;
      }
    }
    newLore.add(FaceColor.ORANGE + "Sale Price: " + FaceColor.YELLOW +
        MintPlugin.getInstance().getEconomy().format(displayPrice));
    if (bonus) {
      newLore.add(FaceColor.PINK + FaceColor.ITALIC.s() + FaceColor.BOLD.s() + "SALE BONUS!!");
    }
    TextUtils.setLore(finalIcon, newLore);
    return finalIcon;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    price = 0;
    checkRare = false;
    if (targetStack != null) {
      HashMap<Integer, ItemStack> overflow = event.getPlayer().getInventory().addItem(targetStack);
      for (ItemStack stack : overflow.values()) {
        Item item = event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), stack);
        item.setOwner(event.getPlayer().getUniqueId());
      }
      targetStack = null;
    }
    event.setWillUpdate(true);
  }

  public Integer getSlot() {
    return slot;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public ItemStack getTargetStack() {
    return targetStack;
  }

  public void setTargetStack(ItemStack targetStack) {
    this.targetStack = targetStack;
  }

  public boolean isCheckRare() {
    return checkRare;
  }

  public void setCheckRare(boolean checkRare) {
    this.checkRare = checkRare;
  }
}
