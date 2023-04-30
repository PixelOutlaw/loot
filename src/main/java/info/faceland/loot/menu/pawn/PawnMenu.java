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
import info.faceland.loot.data.PawnShopType;
import info.faceland.loot.data.PriceData;
import info.faceland.loot.data.SaleRewards;
import info.faceland.loot.menu.TransparentIcon;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PawnMenu extends ItemMenu {

  @Getter
  private final LootPlugin plugin;
  @Getter
  private String dealId = null;

  private final List<SaleIcon> saleIcons = new ArrayList<>();

  public PawnMenu(LootPlugin plugin) {
    super(TextUtils.color(plugin.getSettings().getString("language.menu.pawn.name")), Size.fit(35), plugin);
    this.plugin = plugin;
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

  public PawnMenu(LootPlugin plugin, String dealId) {
    super(TextUtils.color(plugin.getSettings().getString("language.menu.pawn.deals")), Size.fit(35), plugin);
    this.plugin = plugin;
    this.dealId = dealId;
    for (int i = 0; i <= 26; i++) {
      // For deal icon slots
      if (i == 8 || i == 17 || i == 26) {
        continue;
      }
      SaleIcon icon = new SaleIcon(this, i);
      saleIcons.add(icon);
      setItem(i, icon);
    }
    setItem(30, new SellIcon(this));
    setItem(31, new SellIcon(this));
    setItem(32, new SellIcon(this));

    setItem(8, new DealIcon(this, 1));
    setItem(17, new DealIcon(this, 2));
    setItem(26, new DealIcon(this, 3));

    fillEmptySlots(new TransparentIcon());
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

  double getTotal() {
    double total = 0;
    for (SaleIcon saleIcon : saleIcons) {
      if (saleIcon.getTargetStack() == null) {
        continue;
      }
      double amount = saleIcon.getPrice();
      if (dealId != null) {
        PawnShopType type = plugin.getPawnManager().getPawnTypes().get(dealId);
        if (type.getDealOne().matches(saleIcon.getTargetStack())) {
          amount *= type.getDealOne().getMultiplier();
        } else if (type.getDealTwo().matches(saleIcon.getTargetStack())) {
          amount *= type.getDealTwo().getMultiplier();
        } else if (type.getDealThree().matches(saleIcon.getTargetStack())) {
          amount *= type.getDealThree().getMultiplier();
        }
      }
      total += amount;
    }
    return total;
  }

  SaleRewards sellItems(Player player) {
    SaleRewards rewards = new SaleRewards(0, 0);
    for (SaleIcon saleIcon : saleIcons) {
      if (saleIcon.getTargetStack() == null) {
        continue;
      }
      SaleRewards loop = sellItem(saleIcon, player);
      rewards.setMoney(rewards.getMoney() + loop.getMoney());
      rewards.setTradeXp(rewards.getTradeXp() + loop.getTradeXp());
    }
    return rewards;
  }

  private SaleRewards sellItem(SaleIcon saleIcon, Player player) {
    if (saleIcon.isCheckRare()) {
      saleIcon.setCheckRare(false);
      return new SaleRewards(0, 0);
    }
    double amount = saleIcon.getPrice();
    double tradeXp = 0;
    int stackQuantity = saleIcon.getTargetStack().getAmount();
    if (dealId != null) {
      PawnShopType type = plugin.getPawnManager().getPawnTypes().get(dealId);
      if (type.getDealOne().matches(saleIcon.getTargetStack())) {
        amount *= type.getDealOne().getMultiplier();
        tradeXp += type.getDealOne().getTradeXp() * stackQuantity;
      } else if (type.getDealTwo().matches(saleIcon.getTargetStack())) {
        amount *= type.getDealTwo().getMultiplier();
        tradeXp += type.getDealTwo().getTradeXp() * stackQuantity;
      } else if (type.getDealThree().matches(saleIcon.getTargetStack())) {
        amount *= type.getDealThree().getMultiplier();
        tradeXp += type.getDealThree().getTradeXp() * stackQuantity;
      }
    }
    saleIcon.setPrice(0);
    saleIcon.setTargetStack(null);
    saleIcon.setCheckRare(false);
    return new SaleRewards(amount, tradeXp);
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
