package info.faceland.loot.utils;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.ItemGenerationReason;
import info.faceland.loot.data.BuiltItem;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.LootResponse;
import info.faceland.loot.items.CraftMaterial;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.NewCustomItem;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.Tier;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.AspectOfPerfection;
import info.faceland.loot.items.prefabs.DivineHammer;
import info.faceland.loot.items.prefabs.ManaCrystal;
import info.faceland.loot.items.prefabs.OrbOfManyFaces;
import info.faceland.loot.items.prefabs.PerfectSphere;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.SocketExtender;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public class LootUtil {

  public static LootResponse getRandomItem(ItemType itemType, String extraData, int itemLevel, float rarityMultiplier) {
    switch (itemType) {
      case TIER_ITEM -> {
        ItemRarity rarity = LootPlugin.getInstance().getRarityManager().getRandomRarity(rarityMultiplier, -1);
        BuiltItem builtItem = LootPlugin.getInstance().getNewItemBuilder()
            .withTier(Tier.getRandomTier())
            .withRarity(rarity)
            .withLevel(Math.max(1, Math.min(itemLevel, 100)))
            .withItemGenerationReason(ItemGenerationReason.EXTERNAL)
            .build();
        return new LootResponse(builtItem.getStack(), rarity.isBroadcast());
      }
      case SOCKET_GEM -> {
        SocketGem gem;
        if (StringUtils.isBlank(extraData)) {
          gem = LootPlugin.getInstance().getSocketGemManager().getRandomSocketGemByLevel(itemLevel);
        } else {
          gem = SocketGem.get(extraData);
        }
        return new LootResponse(gem.item(), gem.isBroadcast());
      }
      case CUSTOM_ITEM -> {
        NewCustomItem ci;
        if (StringUtils.isBlank(extraData)) {
          ci = LootPlugin.getInstance().getCustomItemManager().getRandomByLevel(itemLevel);
        } else {
          ci = LootPlugin.getInstance().getCustomItemManager().getUnique(extraData);
        }
        return new LootResponse(ci.item(), ci.isBroadcast());
      }
      case PURITY_SCROLL -> {
        return new LootResponse(PurifyingScroll.get(), false);
      }
      case UPGRADE_SCROLL -> {
        UpgradeScroll us = LootPlugin.getInstance().getScrollManager().getRandomScroll();
        ItemStack scrollStack = us.item();
        return new LootResponse(scrollStack, us.isBroadcast());
      }
      case ARCANE_ENHANCER -> {
        return new LootResponse(ArcaneEnhancer.get(), true);
      }
      case SOCKET_EXTENDER -> {
        return new LootResponse(SocketExtender.item(), true);
      }
      case MANA_CRYSTAL -> {
        return new LootResponse(ManaCrystal.item(), true);
      }
      case ORB_OF_FACES -> {
        return new LootResponse(OrbOfManyFaces.item(), true);
      }
      case PERFECT_SPHERE -> {
        return new LootResponse(PerfectSphere.item(), true);
      }
      case DIVINE_HAMMER -> {
        return new LootResponse(DivineHammer.item(), true);
      }
      case ASPECT_OF_PERFECTION -> {
        return new LootResponse(AspectOfPerfection.item(), true);
      }
      case ENCHANTMENT_TOME -> {
        EnchantmentTome e;
        if (StringUtils.isBlank(extraData)) {
          e = LootPlugin.getInstance().getEnchantTomeManager().getRandomEnchantTome();
        } else {
          e = EnchantmentTome.get(extraData);
        }
        return new LootResponse(e.item(), e.isBroadcast());
      }
      case MATERIAL -> {
        CraftMaterial cr = CraftMaterial.get(extraData);
        return new LootResponse(cr.item(), false);
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
    ARCANE_ENHANCER,
    MANA_CRYSTAL,
    ORB_OF_FACES,
    PERFECT_SPHERE,
    DIVINE_HAMMER,
    ASPECT_OF_PERFECTION,
    MATERIAL
  }

}
