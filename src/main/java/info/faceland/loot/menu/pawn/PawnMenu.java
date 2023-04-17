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
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.PawnDeal;
import info.faceland.loot.data.PriceData;
import info.faceland.loot.data.SaleRewards;
import info.faceland.loot.events.PawnDealCreateEvent;
import info.faceland.loot.menu.TransparentIcon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PawnMenu extends ItemMenu {

  private final LootPlugin plugin;

  private static PawnMenu mainPawnMenu;
  private static final Map<String, PawnMenu> pawnMenuPool = new HashMap<>();

  private final List<SaleIcon> saleIcons = new ArrayList<>();
  @Getter
  private String dealId;
  @Getter @Setter
  private PawnDeal dealOne;
  @Getter @Setter
  private PawnDeal dealTwo;
  @Getter @Setter
  private PawnDeal dealThree;

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
    mainPawnMenu = this;
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
    pawnMenuPool.put(dealId, this);
    checkDealChange();
  }

  public static void checkAll() {
    for (PawnMenu menu : pawnMenuPool.values()) {
      menu.checkDealChange();
    }
  }

  public void checkDealChange() {
    boolean update = false;
    if (dealOne == null || dealOne.getMinutesRemaining() == 0) {
      PawnDealCreateEvent event = new PawnDealCreateEvent(dealId, 1);
      Bukkit.getPluginManager().callEvent(event);
      if (event.getResult() != null) {
        dealOne = event.getResult();
        dealOne.setMinutesRemaining(4 + (int) (Math.random() * 3.5));
        update = true;
      }
    }
    else {
      dealOne.setMinutesRemaining(dealOne.getMinutesRemaining() - 1);
    }
    if (dealTwo == null || dealTwo.getMinutesRemaining() == 0) {
      PawnDealCreateEvent event = new PawnDealCreateEvent(dealId, 2);
      Bukkit.getPluginManager().callEvent(event);
      if (event.getResult() != null) {
        dealTwo = event.getResult();
        dealTwo.setMinutesRemaining(8 + (int) (Math.random() * 4.5));
        update = true;
      }
    } else {
      dealTwo.setMinutesRemaining(dealTwo.getMinutesRemaining() - 1);
    }
    if (dealThree == null || dealThree.getMinutesRemaining() == 0) {
      PawnDealCreateEvent event = new PawnDealCreateEvent(dealId, 3);
      Bukkit.getPluginManager().callEvent(event);
      if (event.getResult() != null) {
        dealThree = event.getResult();
        dealThree.setMinutesRemaining(17 + (int) (Math.random() * 7.5));
        update = true;
      }
    } else {
      dealThree.setMinutesRemaining(dealThree.getMinutesRemaining() - 1);
    }
    if (update) {
      for (Player p : Bukkit.getOnlinePlayers()) {
        update(p);
      }
    }
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
        if (dealOne.matches(saleIcon.getTargetStack())) {
          amount *= dealOne.getMultiplier();
        } else if (dealTwo.matches(saleIcon.getTargetStack())) {
          amount *= dealTwo.getMultiplier();
        } else if (dealThree.matches(saleIcon.getTargetStack())) {
          amount *= dealThree.getMultiplier();
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
      if (dealOne.matches(saleIcon.getTargetStack())) {
        amount *= dealOne.getMultiplier();
        tradeXp += dealOne.getTradeXp() * stackQuantity;
      } else if (dealTwo.matches(saleIcon.getTargetStack())) {
        amount *= dealTwo.getMultiplier();
        tradeXp += dealTwo.getTradeXp() * stackQuantity;
      } else if (dealThree.matches(saleIcon.getTargetStack())) {
        amount *= dealThree.getMultiplier();
        tradeXp += dealThree.getTradeXp() * stackQuantity;
      }
    }
    saleIcon.setPrice(0);
    saleIcon.setTargetStack(null);
    saleIcon.setCheckRare(false);
    return new SaleRewards(amount, tradeXp);
  }

  public static PawnMenu getPawnMenu(String dealId) {
    if (StringUtils.isBlank(dealId)) {
      return mainPawnMenu;
    }
    return pawnMenuPool.get(dealId);
  }

  public static void clearPool() {
    mainPawnMenu = null;
    pawnMenuPool.clear();
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
