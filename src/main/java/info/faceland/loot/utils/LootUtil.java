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
import info.faceland.loot.items.SoulGem;
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
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("unused")
public class LootUtil {

  private static LootPlugin plugin;

  public static void refresh(LootPlugin lootPlugin) {
    plugin = lootPlugin;
  }

  public static ItemStack getItem(Plugin requester, String id) {
    var split = id.split("_", 2);
    if (split.length < 2) {
      // SINGLE PARAM FLOW
      ItemStack stack = null;
      return switch (split[0]) {
        case "reroll" ->  OrbOfManyFaces.item();
        case "reroll2", "hypercube" -> PerfectSphere.item();
        case "enhancer", "arcaneenhancer" -> ArcaneEnhancer.get();
        case "extender", "socketextender" -> SocketExtender.item();
        default -> {
          var result = plugin.getCustomItemManager().getUnique(split[0]);
          if (result != null) {
            yield result.item();
          }
          CraftMaterial cr = CraftMaterial.get(id);
          if (cr != null) {
            yield cr.item();
          }
          SocketGem sg = SocketGem.get(id);
          if (sg != null) {
            yield sg.item();
          }
          EnchantmentTome et = EnchantmentTome.get(id);
          if (et != null) {
            yield et.item();
          }
          UpgradeScroll us = UpgradeScroll.get(id);
          if (us != null) {
            yield us.item();
          }
          Bukkit.getLogger().warning("[Loot] Plugin " + requester.getName() + " requested invalid item with id " + id);
          yield null;
        }
      };
    }
    ItemStack stack = null;
    switch (split[0]) {
      case "unique" -> {
        var result = plugin.getCustomItemManager().getUnique(split[1]);
        if (result != null) {
          stack = result.item();
        }
      }
      case "soulgem" -> {
        if ("random".equals(split[1])) {
          return SoulGem.getRandom().item();
        }
        var result = SoulGem.get(split[1]);
        if (result != null) {
          stack = result.item();
        }
      }
      case "material" -> {
        var result = CraftMaterial.get(split[1]);
        if (result != null) {
          stack = result.item();
        }
      }
      case "socketgem", "gem" -> {
        if ("random".equals(split[1])) {
          return plugin.getSocketGemManager().getRandomSocketGem().item();
        }
        var result = SocketGem.get(split[1]);
        if (result != null) {
          stack = result.item();
        }
      }
      case "tome", "enchanttome", "enchant" -> {
        if ("random".equals(split[1])) {
          return plugin.getEnchantTomeManager().getRandomEnchantTome().item();
        }
        var result = EnchantmentTome.get(split[1]);
        if (result != null) {
          stack = result.item();
        }
      }
      case "scroll", "upgradescroll"  -> {
        if ("random".equals(split[1])) {
          return plugin.getScrollManager().getRandomScroll().item();
        }
        var result = UpgradeScroll.get(split[1]);
        if (result != null) {
          stack = result.item();
        }
      }
    }
    if (stack == null) {
      Bukkit.getLogger().warning("[Loot] Plugin " + requester.getName() + " requested invalid item with id " + id);
    }
    return stack;
  }

  public static LootResponse getRandomItem(ItemType itemType, String extraData, int itemLevel, float rarityMultiplier) {
    switch (itemType) {
      case TIER_ITEM -> {
        ItemRarity rarity = plugin.getRarityManager().getRandomRarity(rarityMultiplier, -1);
        BuiltItem builtItem = plugin.getNewItemBuilder()
            .withTier(Tier.getRandomTier(itemLevel))
            .withRarity(rarity)
            .withLevel(Math.max(1, Math.min(itemLevel, 100)))
            .withItemGenerationReason(ItemGenerationReason.EXTERNAL)
            .build();
        return new LootResponse(builtItem.getStack(), rarity.isBroadcast());
      }
      case SOCKET_GEM -> {
        SocketGem gem;
        if (StringUtils.isBlank(extraData)) {
          gem = plugin.getSocketGemManager().getRandomSocketGemByLevel(itemLevel);
        } else {
          gem = SocketGem.get(extraData);
        }
        return new LootResponse(gem.item(), gem.isBroadcast());
      }
      case CUSTOM_ITEM -> {
        NewCustomItem ci;
        if (StringUtils.isBlank(extraData)) {
          ci = plugin.getCustomItemManager().getRandomByLevel(itemLevel);
        } else {
          ci = plugin.getCustomItemManager().getUnique(extraData);
        }
        return new LootResponse(ci.item(), ci.isBroadcast());
      }
      case PURITY_SCROLL -> {
        return new LootResponse(PurifyingScroll.get(), false);
      }
      case UPGRADE_SCROLL -> {
        UpgradeScroll us = plugin.getScrollManager().getRandomScroll();
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
          e = plugin.getEnchantTomeManager().getRandomEnchantTome();
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
