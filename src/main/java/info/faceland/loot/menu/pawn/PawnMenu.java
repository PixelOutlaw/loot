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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ninja.amp.ampmenus.menus.ItemMenu;
import ninja.amp.ampmenus.menus.MenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PawnMenu extends ItemMenu {

  private static Set<PawnMenu> pawnMenuPool = new HashSet<>();

  private Set<Integer> sellSlots = new HashSet<>();
  private List<SaleIcon> saleIcons = new ArrayList<>();

  private PawnMenu(LootPlugin plugin) {
    super(TextUtils.color(plugin.getSettings().getString("language.menu.pawn.name",
        "&8Sell Items!")), Size.fit(35), plugin);
    for (int i = 0; i <= 26; i++) {
      SaleIcon icon = new SaleIcon(this, i);
      saleIcons.add(icon);
      setItem(i, icon);
    }
    fillEmptySlots();
    setItem(31, new SellIcon(this));
  }

  public void open(Player player) {
    sellSlots.clear();
    super.open(player);
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

  public void removeSlot(Integer slot) {
    sellSlots.remove(slot);
  }

  public void addItem(Player player, ItemStack stack, PriceData data, Integer slot) {
    if (sellSlots.contains(slot)) {
      return;
    }
    SaleIcon firstNull = null;
    for (SaleIcon icon : saleIcons) {
      if (icon.getTargetStack() == null) {
        firstNull = icon;
        break;
      }
    }
    if (firstNull != null) {
      sellSlots.add(slot);
      firstNull.setTargetStack(stack);
      firstNull.setPrice(data.getPrice());
      firstNull.setCheckRare(data.isRare());
      update(player);
    }
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

  int sellItems(Player player) {
    int total = 0;
    for (SaleIcon saleIcon : saleIcons) {
      if (saleIcon.getTargetStack() == null) {
        continue;
      }
      total += sellItem(player, saleIcon);
    }
    return total;
  }

  private int sellItem(Player player, SaleIcon saleIcon) {
    if (saleIcon.isCheckRare()) {
      saleIcon.setCheckRare(false);
      return 0;
    }
    if (player.getInventory().containsAtLeast(saleIcon.getTargetStack(), saleIcon.getTargetStack().getAmount())) {
      player.getInventory().removeItem(saleIcon.getTargetStack());
      int amount = saleIcon.getPrice();
      saleIcon.setPrice(0);
      saleIcon.setTargetStack(null);
      saleIcon.setCheckRare(false);
      sellSlots.remove(saleIcon.getSlot());
      return amount;
    }
    Bukkit.getLogger().warning(player.getDisplayName() + " tried to sell non-existing item...");
    return 0;
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
