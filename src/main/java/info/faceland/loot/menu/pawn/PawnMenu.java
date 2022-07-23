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

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.PriceData;
import info.faceland.loot.menu.TransparentIcon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ninja.amp.ampmenus.menus.ItemMenu;
import ninja.amp.ampmenus.menus.MenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PawnMenu extends ItemMenu {

  private static final Set<PawnMenu> pawnMenuPool = new HashSet<>();
  private final List<SaleIcon> saleIcons = new ArrayList<>();

  private PawnMenu(LootPlugin plugin) {
    super(TextUtils.color(plugin.getSettings().getString("language.menu.pawn.name",
        "&8Sell Items!")), Size.fit(35), plugin);
    for (int i = 0; i <= 26; i++) {
      SaleIcon icon = new SaleIcon(this, i);
      saleIcons.add(icon);
      setItem(i, icon);
    }
    setItem(30, new SellIcon(this));
    setItem(31, new SellIcon(this));
    setItem(32, new SellIcon(this));
    fillEmptySlots(new TransparentIcon());
  }

  public void open(Player player) {
    super.open(player);
  }

  public List<ItemStack> getReturnStacks() {
    List<ItemStack> returnItems = new ArrayList<>();
    for (SaleIcon icon : saleIcons) {
      if (icon.getTargetStack() != null && icon.getTargetStack().getType() != Material.AIR) {
        returnItems.add(icon.getTargetStack());
      }
      icon.setPrice(0);
      icon.setTargetStack(null);
      icon.setCheckRare(false);
    }
    return returnItems;
  }

  public static PawnMenu getPawnMenu(LootPlugin plugin) {
    for (PawnMenu menu : pawnMenuPool) {
      for (Player p : Bukkit.getOnlinePlayers()) {
        if (!(p.getInventory().getHolder() instanceof MenuHolder)) {
          continue;
        }
        if (((MenuHolder) p.getInventory().getHolder()).getMenu() == menu) {
          continue;
        }
        return menu;
      }
    }
    PawnMenu menu = new PawnMenu(plugin);
    pawnMenuPool.add(menu);
    return menu;
  }

  public static void clearPool() {
    pawnMenuPool.clear();
  }

  public boolean addItem(Player player, ItemStack stack, PriceData data) {
    for (SaleIcon icon : saleIcons) {
      if (icon.getTargetStack() == null) {
        icon.setTargetStack(stack);
        icon.setPrice(data.getPrice());
        icon.setCheckRare(data.isRare());
        update(player);
        return true;
      }
    }
    return false;
  }

  int getTotal() {
    int total = 0;
    for (SaleIcon saleIcon : saleIcons) {
      if (saleIcon.getTargetStack() == null) {
        continue;
      }
      total += saleIcon.getPrice();
    }
    return total;
  }

  int sellItems() {
    int total = 0;
    for (SaleIcon saleIcon : saleIcons) {
      if (saleIcon.getTargetStack() == null) {
        continue;
      }
      total += sellItem(saleIcon);
    }
    return total;
  }

  private int sellItem(SaleIcon saleIcon) {
    if (saleIcon.isCheckRare()) {
      saleIcon.setCheckRare(false);
      return 0;
    }
    int amount = saleIcon.getPrice();
    saleIcon.setPrice(0);
    saleIcon.setTargetStack(null);
    saleIcon.setCheckRare(false);
    return amount;
  }
}

/*
00 01 02 03 04 05 06 07 08
09 10 11 12 13 14 15 16 17
18 19 20 21 22 23 24 25 26
27 28 29 30 31 32 33 34 35
36 37 38 39 40 41 42 43 44
45 46 47 48 49 50 51 52 53
*/
