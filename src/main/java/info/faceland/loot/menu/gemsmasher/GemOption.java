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
package info.faceland.loot.menu.gemsmasher;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ExistingSocketData;
import info.faceland.loot.items.ItemBuilder;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.nunnerycode.mint.MintPlugin;

public class GemOption extends MenuItem {

  private final GemSmashMenu menu;
  private final int index;

  GemOption(GemSmashMenu menu, int index) {
    super(TextUtils.color("&eClick Gems To Begin!"), new ItemStack(Material.BARRIER));
    this.menu = menu;
    this.index = index;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    ExistingSocketData data = menu.getData(player);
    if (data == null || data.getIndexes(index) == null) {
      return new ItemStack(Material.AIR);
    }
    ItemStack gemb = new ItemStack(Material.EMERALD);
    ItemStackExtensionsKt.setCustomModelData(gemb, 2000);
    ItemStackExtensionsKt.setDisplayName(gemb, PaletteUtil.color("|red||b|WOO! SMASH IT!"));
    List<String> lore = new ArrayList<>();
    lore.add("");
    lore.add(PaletteUtil.color("|white|Clicking this will destroy a gem and"));
    lore.add(PaletteUtil.color("|white|restore the " + ItemBuilder.SOCKET));
    lore.add("");
    ItemStack stack = menu.getStack(player);
    List<String> handLore = TextUtils.getLore(stack);
    for (int i : data.getIndexes(index)) {
      lore.add(handLore.get(i));
    }
    lore.add("");
    lore.add(PaletteUtil.color("|red|Click to destroy this gem!"));
    lore.add(PaletteUtil.color("|orange|Cost: |yellow|" +
        StrifePlugin.INT_FORMAT.format(menu.getCost(player)) + "◎"));
    lore.add("");
    lore.add(ChatColor.WHITE + "哄");
    lore.add("");
    lore.add("");
    TextUtils.setLore(gemb, lore, false);
    return gemb;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    ExistingSocketData data = menu.getData(event.getPlayer());
    if (data == null || data.getIndexes((index)) == null) {
      event.setWillUpdate(false);
      event.setWillClose(false);
      return;
    }
    int cost = menu.getCost(event.getPlayer());
    EconomyResponse response = MintPlugin.getInstance().getEconomy()
        .withdrawPlayer(event.getPlayer(), cost);

    if (response.type == ResponseType.FAILURE) {
      event.setWillUpdate(false);
      event.setWillClose(false);
      PaletteUtil.sendMessage(event.getPlayer(), "|orange|You need |yellow|" +
          StrifePlugin.INT_FORMAT.format(cost) + "◎ |orange|to do this!");
      return;
    }
    PaletteUtil.sendMessage(event.getPlayer(), "|lgreen|Socket gem removed!");
    ExistingSocketData newData = MaterialUtil.destroyGem(menu.getStack(event.getPlayer()), index);
    menu.setData(event.getPlayer(), newData, menu.getStack(event.getPlayer()));
    event.getPlayer().playSound(event.getPlayer().getLocation(),
        "minecraft:custom.container.ceramic_break", SoundCategory.MASTER, 1, 1.7f);
    event.setWillUpdate(true);
  }
}
