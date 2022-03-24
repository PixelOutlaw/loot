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
package info.faceland.loot.listeners;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.enchantments.EnchantmentTome;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.items.prefabs.TinkerersGear;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.tier.Tier;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import land.face.market.data.PlayerMarketState.Category;
import land.face.market.data.PlayerMarketState.FilterFlagA;
import land.face.market.data.PlayerMarketState.FilterFlagB;
import land.face.market.events.ListItemEvent;
import land.face.market.managers.MarketManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public final class ItemListListener implements Listener {

  private final LootPlugin plugin;

  public ItemListListener(LootPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onMarketList(ListItemEvent event) {
    ItemStack stack = event.getListing().getItemStack();
    if (stack.getType() == Material.TRIPWIRE_HOOK && stack.hasItemMeta()) {
      event.getListing().setCategory(Category.CATEGORY_3);
      event.getListing().setFlagA(FilterFlagA.ALL);
      event.getListing().setFlagB(FilterFlagB.ALL);
      return;
    }
    SocketGem gem = plugin.getSocketGemManager().getSocketGem(stack);
    if (gem != null) {
      event.getListing().setCategory(Category.CATEGORY_2);
      event.getListing().setFlagA(FilterFlagA.FLAG_2);
      event.getListing().setFlagB(FilterFlagB.ALL);
      return;
    }
    if (PurifyingScroll.get().isSimilar(stack)) {
      event.getListing().setCategory(Category.CATEGORY_2);
      event.getListing().setFlagA(FilterFlagA.FLAG_5);
      event.getListing().setFlagB(FilterFlagB.ALL);
      return;
    }
    if (ArcaneEnhancer.isSimilar(stack)) {
      event.getListing().setCategory(Category.CATEGORY_2);
      event.getListing().setFlagA(FilterFlagA.FLAG_6);
      event.getListing().setFlagB(FilterFlagB.ALL);
      return;
    }
    if (TinkerersGear.get().isSimilar(stack)) {
      event.getListing().setCategory(Category.CATEGORY_2);
      event.getListing().setFlagA(FilterFlagA.FLAG_6);
      event.getListing().setFlagB(FilterFlagB.ALL);
      return;
    }
    if (SocketExtender.EXTENDER.isSimilar(stack)) {
      event.getListing().setCategory(Category.CATEGORY_2);
      event.getListing().setFlagA(FilterFlagA.FLAG_4);
      event.getListing().setFlagB(FilterFlagB.ALL);
      return;
    }
    UpgradeScroll scroll = plugin.getScrollManager().getScroll(stack);
    if (scroll != null) {
      event.getListing().setCategory(Category.CATEGORY_2);
      event.getListing().setFlagA(FilterFlagA.FLAG_1);
      event.getListing().setFlagB(FilterFlagB.ALL);
      return;
    }
    EnchantmentTome tome = MaterialUtil.getEnchantmentItem(stack);
    if (tome != null) {
      event.getListing().setCategory(Category.CATEGORY_2);
      event.getListing().setFlagA(FilterFlagA.FLAG_3);
      //TODO IMPROVE FLAG B LOGIC
      event.getListing().setFlagB(FilterFlagB.ALL);
      return;
    }
    Tier tier = MaterialUtil.getTierFromStack(stack);
    if (tier != null) {
      event.getListing().setCategory(Category.CATEGORY_1);
      event.getListing().setFlagA(tier.getFilterFlag());
      int index = 1 + (MaterialUtil.getLevelRequirement(stack) - 1) / 10;
      for (FilterFlagB b : MarketManager.FILTER_BS) {
        if (b.ordinal() == index) {
          event.getListing().setFlagB(b);
          break;
        }
      }
      return;
    }
    if (stack.getType() == Material.TRIPWIRE_HOOK && ItemStackExtensionsKt
        .getDisplayName(stack).contains("Key")) {
      event.getListing().setCategory(Category.CATEGORY_3);
      event.getListing().setFlagA(FilterFlagA.FLAG_3);
      event.getListing().setFlagB(FilterFlagB.ALL);
      return;
    }
    if (plugin.getCraftMatManager().getCraftMaterials().containsKey(stack.getType()) && stack
        .hasItemMeta()) {
      event.getListing().setCategory(Category.CATEGORY_4);
      event.getListing().setFlagA(FilterFlagA.FLAG_1);
      int index = 1 + (MaterialUtil.getItemLevel(stack) - 1) / 10;
      for (FilterFlagB b : MarketManager.FILTER_BS) {
        if (b.ordinal() == index) {
          event.getListing().setFlagB(b);
          break;
        }
      }
      return;
    }
    if (MaterialUtil.isEssence(stack)) {
      event.getListing().setCategory(Category.CATEGORY_4);
      event.getListing().setFlagA(FilterFlagA.FLAG_1);
      int index = 1 + (MaterialUtil.getEssenceLevel(stack) - 1) / 10;
      for (FilterFlagB b : MarketManager.FILTER_BS) {
        if (b.ordinal() == index) {
          event.getListing().setFlagB(b);
          break;
        }
      }
      return;
    }
  }
}