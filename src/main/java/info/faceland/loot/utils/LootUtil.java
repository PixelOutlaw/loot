package info.faceland.loot.utils;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.CustomItem;
import info.faceland.loot.api.items.ItemGenerationReason;
import info.faceland.loot.data.BuiltItem;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.LootResponse;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.enchantments.EnchantmentTome;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.tier.Tier;
import org.bukkit.inventory.ItemStack;

public class LootUtil {

  public static LootResponse getRandomItem(ItemType itemType) {
    return getRandomItem(itemType, 1, 1);
  }

  public static LootResponse getRandomItem(ItemType itemType, int itemLevel) {
    return getRandomItem(itemType, itemLevel, 1);
  }

  public static LootResponse getRandomItem(ItemType itemType, int itemLevel, float rarityMultiplier) {
    switch (itemType) {
      case TIER_ITEM -> {
        Tier tier = LootPlugin.getInstance().getTierManager().getRandomTier();
        ItemRarity rarity = LootPlugin.getInstance().getRarityManager()
            .getRandomRarityWithBonus(1 - rarityMultiplier);
        BuiltItem builtItem = LootPlugin.getInstance().getNewItemBuilder()
            .withTier(tier)
            .withRarity(rarity)
            .withLevel(Math.max(1, Math.min(itemLevel, 100)))
            .withItemGenerationReason(ItemGenerationReason.EXTERNAL)
            .withSpecialStat(false)
            .build();
        return new LootResponse(builtItem.getStack(), rarity.isBroadcast());
      }
      case SOCKET_GEM -> {
        SocketGem gem = LootPlugin.getInstance().getSocketGemManager()
            .getRandomSocketGemByLevel(itemLevel);
        return new LootResponse(gem.toItemStack(1), gem.isBroadcast());
      }
      case CUSTOM_ITEM -> {
        CustomItem ci = LootPlugin.getInstance().getCustomItemManager()
            .getRandomCustomItemByLevel(itemLevel);
        return new LootResponse(ci.toItemStack(1), ci.isBroadcast());
      }
      case PURITY_SCROLL -> {
        return new LootResponse(PurifyingScroll.get(), false);
      }
      case UPGRADE_SCROLL -> {
        UpgradeScroll us = LootPlugin.getInstance().getScrollManager().getRandomScroll();
        ItemStack scrollStack = LootPlugin.getInstance().getScrollManager().buildItemStack(us);
        return new LootResponse(scrollStack, us.isBroadcast());
      }
      case ARCANE_ENHANCER -> {
        return new LootResponse(ArcaneEnhancer.get(), true);
      }
      case SOCKET_EXTENDER -> {
        return new LootResponse(SocketExtender.EXTENDER, true);
      }
      case ENCHANTMENT_TOME -> {
        EnchantmentTome e = LootPlugin.getInstance().getEnchantTomeManager()
            .getRandomEnchantTome(rarityMultiplier);
        return new LootResponse(e.toItemStack(1), e.isBroadcast());
      }
    }
    return null;
  }

  public enum ItemType {
    TIER_ITEM,
    SOCKET_GEM,
    UPGRADE_SCROLL,
    PURITY_SCROLL,
    CUSTOM_ITEM,
    SOCKET_EXTENDER,
    ENCHANTMENT_TOME,
    ARCANE_ENHANCER
  }

}
