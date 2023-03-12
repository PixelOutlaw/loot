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

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.SaleRewards;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.PlayerDataUtil;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nunnerycode.mint.MintPlugin;

public class SellIcon extends MenuItem {

  private final PawnMenu menu;
  private final List<String> lore = new ArrayList<>();

  private static final DecimalFormat DF = new DecimalFormat("###.#");
  private static final ItemStack defaultStack = buildStack();

  SellIcon(PawnMenu menu) {
    super(TextUtils.color("&e&lSell Items"), new ItemStack(Material.GOLD_INGOT));
    this.menu = menu;
    lore.add(TextUtils.color("&7Click to sell the items"));
    lore.add(TextUtils.color("&7selected above!"));
    lore.add("");
    lore.add(TextUtils.color("&6Total: &e{total}"));
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    ItemStack stack = defaultStack.clone();
    ItemStackExtensionsKt.setDisplayName(stack, getDisplayName());
    List<String> newLore = new ArrayList<>();
    int tradeLevel = PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.TRADING);
    int total = (int) menu.getTotal();
    int modifiedTotal = getModifiedTotal(total, tradeLevel);
    String priceString;
    if (tradeLevel < 5 || modifiedTotal == total) {
      priceString = MintPlugin.getInstance().getEconomy().format(total);
    } else {
      priceString = TextUtils.color("&7&m" + total + "&r &e&l" + MintPlugin.getInstance().getEconomy().format(modifiedTotal));
    }
    for (String s : lore) {
      newLore.add(s.replace("{total}", priceString));
    }
    if (PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.TRADING) >= 5) {
      newLore.add(
          TextUtils.color("&eTrade Level Bonus: " + DF.format(((double) tradeLevel) / 5) + "%"));
    }
    TextUtils.setLore(stack, newLore);
    return stack;
  }

  private int getModifiedTotal(double total, double tradeLevel) {
    if (tradeLevel >= 5) {
      total = total * (1 + 0.002 * tradeLevel);
      total = Math.floor(total);
    }
    return (int) total;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    event.getPlayer()
        .playSound(event.getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 1, 1.2f);
    int tradeLevel = PlayerDataUtil.getLifeSkillLevel(event.getPlayer(), LifeSkillType.TRADING);
    SaleRewards rewards = menu.sellItems(event.getPlayer());
    int total = (int) rewards.getMoney();
    double tradeXp = rewards.getTradeXp();
    total = getModifiedTotal(total, tradeLevel);
    menu.update(event.getPlayer());
    if (total != 0) {
      MintPlugin.getInstance().getEconomy().depositPlayer(event.getPlayer(), total);
      event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.BLOCK_CHAIN_PLACE, 1.0F, 1.3F);
      MessageUtils.sendMessage(event.getPlayer(), "&e  +" + MintPlugin.getInstance().getEconomy().format(total));
    }
    if (tradeXp > 0.5) {
      LootPlugin.getInstance().getStrifePlugin().getSkillExperienceManager()
          .addExperience(event.getPlayer(), LifeSkillType.TRADING, tradeXp, false, false);
    }
    if (menu.getTotal() > 0) {
      MessageUtils.sendMessage(event.getPlayer(),
          "&e&l[!] &eHigh rarity items found! Press sell again to confirm the sale of these items.");
      event.getPlayer()
          .playSound(event.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_BIT, 1, 0.5f);
    }
    event.setWillUpdate(true);
  }

  private static ItemStack buildStack() {
    ItemStack stack = new ItemStack(Material.BARRIER);
    ItemStackExtensionsKt.setCustomModelData(stack, 50);
    return stack;
  }
}
