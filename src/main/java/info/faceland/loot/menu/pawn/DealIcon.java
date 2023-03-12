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
import info.faceland.loot.data.PawnDeal;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DealIcon extends MenuItem {

  private final PawnMenu menu;
  private final int slot;

  private static final DecimalFormat DF = new DecimalFormat("###.##");

  DealIcon(PawnMenu menu, int slot) {
    super(TextUtils.color("&cuhhhhhhhhhhhhhh erm error"), new ItemStack(Material.GOLD_INGOT));
    this.menu = menu;
    this.slot = slot;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    PawnDeal deal = switch (slot) {
      case 1 -> menu.getDealOne();
      case 2 -> menu.getDealTwo();
      case 3 -> menu.getDealThree();
      default -> null;
    };
    if (deal == null) {
      return getIcon();
    }
    ItemStack icon = new ItemStack(deal.getMaterial());
    ItemStackExtensionsKt.setDisplayName(icon, deal.getName());

    List<String> lore = new ArrayList<>();
    lore.add(FaceColor.WHITE + "Price Multiplier: " + DF.format(deal.getMultiplier()) + "x");
    lore.add(FaceColor.GREEN + "Trading Experience: " + DF.format(deal.getTradeXp()) + "XP");
    lore.add("");
    lore.add(FaceColor.LIGHT_GRAY + "This item is in demand! Sell it");
    lore.add(FaceColor.LIGHT_GRAY + "for extra money and some");
    lore.add(FaceColor.LIGHT_GRAY + "Trading XP!");
    lore.add("");
    lore.add(FaceColor.CYAN + "Offer ends in: " + deal.getMinutesRemaining() + " minute(s)!");
    TextUtils.setLore(icon, lore, false);

    if (deal.getModelData() > 0) {
      ItemStackExtensionsKt.setCustomModelData(icon, deal.getModelData());
    }
    return icon;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    event.setWillUpdate(false);
    event.setWillClose(false);
  }
}
