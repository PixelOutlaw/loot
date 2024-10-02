package info.faceland.loot.utils;

import com.tealcube.minecraft.bukkit.facecore.pojo.RandomSound;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.ItemGenerationReason;
import info.faceland.loot.builders.ItemBuilder;
import info.faceland.loot.data.BuiltItem;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.MobLootTable;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.items.CraftMaterial;
import info.faceland.loot.items.NewCustomItem;
import info.faceland.loot.items.SoulGem;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.events.LootDropEvent;
import info.faceland.loot.items.prefabs.*;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.Tier;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import land.face.learnin.LearninBooksPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class DropUtil implements Listener {

  private static LootPlugin plugin;

  private static String itemFoundFormat;

  private static double customizedTierChance, normalDropChance, scrollDropChance, socketDropChance, tomeDropChance,
      enhancerDropChance, tinkerGearDropChance, faceOrbDropChance, hypercubeDropChance, purityDropChance,
      normalItemDropChance, distortDropChance, soulGemDropChance, rarityPaintDropChance, awakenerDropChance,
      missingStatDropChance, unidTomeDropChance;

  private static final RandomSound distortedDropSound =
      new RandomSound(SoundCategory.MASTER, Sound.ENTITY_ENDERMAN_HURT, 4f, 1f, 0.05f);
  private static final RandomSound tieredDropSound =
      new RandomSound(SoundCategory.MASTER, Sound.ITEM_ARMOR_EQUIP_CHAIN, 4f, 1f, 0.05f);
  private static final RandomSound gemDropSound =
      new RandomSound(SoundCategory.MASTER, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 4, 2f, 0f);
  private static final RandomSound tomeDropSound =
      new RandomSound(SoundCategory.MASTER, Sound.ITEM_BOOK_PAGE_TURN, 4, 1f, 0f);
  private static final RandomSound scrollDropSound =
      new RandomSound(SoundCategory.MASTER, Sound.ITEM_BOOK_PAGE_TURN, 4, 1.0f, 0f);
  private static final RandomSound extenderDropSound =
      new RandomSound(SoundCategory.MASTER, Sound.ENTITY_WARDEN_SONIC_BOOM, 4, 2f, 0f);
  private static final RandomSound arcaneDropSound =
      new RandomSound(SoundCategory.MASTER, Sound.ENTITY_WARDEN_SONIC_BOOM, 4, 2f, 0f);
  private static final RandomSound tinkerDropSound =
      new RandomSound(SoundCategory.MASTER, Sound.ENTITY_WARDEN_SONIC_BOOM, 4, 2f, 0f);
  private static final RandomSound purifyDropSound =
      new RandomSound(SoundCategory.MASTER, Sound.BLOCK_CHISELED_BOOKSHELF_INSERT, 4, 1.0f, 0f);
  private static final RandomSound uniqueDropSound =
      new RandomSound(SoundCategory.MASTER, Sound.ITEM_BUNDLE_DROP_CONTENTS, 4, 1.0f, 0f);


  public static void refresh() {
    plugin = LootPlugin.getInstance();
    itemFoundFormat = plugin.getSettings().getString("language.broadcast.found-item", "");

    customizedTierChance = plugin.getSettings()
        .getDouble("config.drops.customized-tier-chance", 0D);

    normalDropChance = plugin.getSettings().getDouble("config.drops.normal-drop", 0D);
    scrollDropChance = plugin.getSettings().getDouble("config.drops.upgrade-scroll", 0D);
    socketDropChance = plugin.getSettings().getDouble("config.drops.socket-gem", 0D);
    tomeDropChance = plugin.getSettings().getDouble("config.drops.enchant-gem", 0D);
    unidTomeDropChance = plugin.getSettings().getDouble("config.drops.unid-enchant-gem", 0D);

    enhancerDropChance = plugin.getSettings().getDouble("config.drops.arcane-enhancer", 0D);

    tinkerGearDropChance = plugin.getSettings().getDouble("config.drops.tinker-gear", 0D);
    faceOrbDropChance = plugin.getSettings().getDouble("config.drops.face-orb", 0D);
    hypercubeDropChance = plugin.getSettings().getDouble("config.drops.hypercube", 0D);
    distortDropChance = plugin.getSettings().getDouble("config.drops.distort", 0D);
    awakenerDropChance = plugin.getSettings().getDouble("config.drops.awakener", 0D);
    missingStatDropChance = plugin.getSettings().getDouble("config.drops.missing-stat", 0D);
    rarityPaintDropChance = plugin.getSettings().getDouble("config.drops.raritypaint", 0D);

    purityDropChance = plugin.getSettings().getDouble("config.drops.purity-scroll", 0D);
    normalItemDropChance = plugin.getSettings().getDouble("config.drops.normal-item", 0D);
    soulGemDropChance = plugin.getSettings().getDouble("config.drops.soul-gem", 0.0001D);
  }

  public static void dropLoot(LootDropEvent event) {
    Player killer = null;
    if (event.getLooterUUID() != null) {
      killer = Bukkit.getPlayer(event.getLooterUUID());
      if (killer == null) {
        return;
      }
    }
    double amountBonus = event.getAmountBonus();
    double rarityBonus = event.getRarityBonus();
    float mobLevel = event.getMonsterLevel();
    float itemLevel = (mobLevel + 2f) - (LootPlugin.RNG.nextFloat() * 6f);
    itemLevel = Math.min(100, Math.max(1, Math.round(itemLevel)));

    List<ItemRarity> bonusDrops = new ArrayList<>(event.getBonusTierDrops());

    if (StringUtils.isNotBlank(event.getUniqueEntity())) {
      MobLootTable loot = plugin.getUniqueDropsManager().getData(event.getUniqueEntity());
      if (loot != null) {
        amountBonus *= loot.getAmountMultiplier();
        rarityBonus *= loot.getRarityMultiplier();
        doUniqueDrops(loot, event.getLocation(), killer);
        bonusDrops.addAll(loot.getBonusTierItems());
      }
      for (String fac : event.getFactions()) {
        MobLootTable factionLoot = plugin.getUniqueDropsManager().getData(fac);
        if (factionLoot == null) {
          continue;
        }
        //amountBonus *= factionLoot.getAmountMultiplier();
        //rarityBonus *= factionLoot.getRarityMultiplier();
        doUniqueDrops(factionLoot, event.getLocation(), killer);
        bonusDrops.addAll(factionLoot.getBonusTierItems());
      }
    }

    if (LearninBooksPlugin.instance.getKnowledgeManager().getKnowledgeLevel(killer, event.getUniqueEntity()) > 2) {
      amountBonus += 0.1;
    }

    float normalBonus = (float) amountBonus;
    float bonusWithCheesePenalty = normalBonus * event.getCheesePenaltyMult();
    float bonusWithCheeseAndLevelPenalty = bonusWithCheesePenalty * event.getLevelPenaltyMult();

    boolean normalDrop = bonusWithCheeseAndLevelPenalty * normalDropChance > LootPlugin.RNG.nextFloat();
    while (!bonusDrops.isEmpty() || normalDrop) {
      Tier tier = getTier(killer);
      ItemRarity rarity = plugin.getRarityManager().getRandomRarity(rarityBonus, -1);
      if (!bonusDrops.isEmpty()) {
        ItemRarity dropRarity = bonusDrops.get(LootPlugin.RNG.nextInt(0, bonusDrops.size()));
        if (dropRarity.getPower() > rarity.getPower()) {
          rarity = dropRarity;
        }
        bonusDrops.remove(dropRarity);
      } else {
        normalDrop = false;
      }

      boolean distort = LootPlugin.RNG.nextFloat() < 0.01;
      int upgradeBonus = 0;
      double upgradeChance = plugin.getSettings().getDouble("config.random-upgrade-chance", 0.1);
      double multiUpgradeChance = plugin.getSettings()
          .getDouble("config.multi-upgrade-chance", 0.1);
      if (LootPlugin.RNG.nextFloat() <= upgradeChance) {
        while (LootPlugin.RNG.nextFloat() <= multiUpgradeChance && upgradeBonus < 15) {
          upgradeBonus++;
        }
      }

      BuiltItem builtItem = plugin.getNewItemBuilder()
          .withTier(tier)
          .withRarity(rarity)
          .withLevel((int) itemLevel)
          .withPlus(upgradeBonus)
          .withDistortion(distort)
          .withItemGenerationReason(ItemGenerationReason.MONSTER)
          .withCreator(killer)
          .build();

      ItemStack tierItem = builtItem.getStack();

      boolean broadcast = rarity.isBroadcast() || (int) rarity.getPower() + (upgradeBonus / tier.getUpgradeScale()) > 7;
      Color trailColor = broadcast || rarity.isAlwaysTrail() ? rarity.getColor().getRawColor() : null;
      ChatColor glowColor = broadcast || rarity.isAlwaysGlow() ? rarity.getGlowColor() : null;
      RandomSound randomSound = null;
      if (distort && rarity.getPower() > 1.5) {
        randomSound = distortedDropSound;
      } else if (broadcast) {
        randomSound = tieredDropSound;
      }
      dropItem(event.getLocation(), tierItem, killer, builtItem.getTicksLived(), broadcast, trailColor, glowColor,
          randomSound);
    }

    if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * socketDropChance) {
      SocketGem sg = plugin.getSocketGemManager().getRandomSocketGemByLevel((int) mobLevel);
      assert sg != null;
      ItemStack his = sg.toItemStack(1);
      RandomSound rs = sg.isBroadcast() ? gemDropSound : null;
      dropItem(event.getLocation(), his, killer, 0, sg.isBroadcast(),
          sg.isBroadcast() ? FaceColor.LIGHT_GREEN.getRawColor() : null,
          sg.isBroadcast() ? ChatColor.GREEN : null, rs);
    }
    if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * tomeDropChance) {
      EnchantmentTome es = plugin.getEnchantTomeManager().getRandomEnchantTome(rarityBonus);
      assert es != null;
      RandomSound rs = es.isBroadcast() ? tomeDropSound : null;
      ItemStack his = es.toItemStack(1);
      dropItem(event.getLocation(), his, killer, 0, es.isBroadcast(),
          es.isBroadcast() ? FaceColor.BLUE.getRawColor() : null,
          es.isBroadcast() ? ChatColor.BLUE : null, rs);
    }
    if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * unidTomeDropChance) {
      ItemStack his = UnidEnchant.item();
      dropItem(event.getLocation(), his, killer, 0, false, null, null, null);
    }
    if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * purityDropChance) {
      dropItem(event.getLocation(), PurifyingScroll.get(), killer, 0, false,
          FaceColor.WHITE.getRawColor(), ChatColor.WHITE, purifyDropSound);
    }
    if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * soulGemDropChance) {
      if (killer != null && killer.getLevel() > 25) {
        SoulGem gemb = SoulGem.getRandom();
        if (gemb != null) {
          dropItem(event.getLocation(), SoulGem.getRandom().item(), killer, 0, false,
              FaceColor.PINK.getRawColor(), ChatColor.LIGHT_PURPLE, gemDropSound);
        }
      }
    }
    if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * scrollDropChance) {
      UpgradeScroll us = plugin.getScrollManager().getRandomScroll();
      ItemStack stack = us.item();
      RandomSound rs = us.isBroadcast() ? scrollDropSound : null;
      dropItem(event.getLocation(), stack, killer, 0, us.isBroadcast(),
          us.isBroadcast() ? FaceColor.GREEN.getRawColor() : null,
          us.isBroadcast() ? ChatColor.DARK_GREEN : null, rs);
    }
    if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * plugin.getSettings()
        .getDouble("config.drops.custom-item", 0D)) {
      NewCustomItem ci = plugin.getCustomItemManager().getRandomByLevel((int) mobLevel);
      try {
        assert ci != null;
        ItemStack stack = ci.item();

        EquipmentData data = PersistHelper.getEquipmentData(stack);
        if (data != null) {
          data.setCreationDate(ItemBuilder.simpleFormat.format(new Date()));
          data.setDiscoveredBy(killer.getName());
          PersistHelper.setEquipmentData(stack, data);
        }

        boolean broadcast = ci.isBroadcast();
        RandomSound rs = broadcast ? uniqueDropSound : null;
        dropItem(event.getLocation(), stack, killer, 0, broadcast,
            broadcast ? FaceColor.ORANGE.getRawColor() : null,
            broadcast ? ChatColor.GOLD : null, rs);
      } catch (Exception e) {
        Bukkit.getLogger().warning("[Loot] Failed to drop item " + ci.getId());
        e.printStackTrace();
      }
    }


    // RARE LOOT TABLE
    if (LootPlugin.RNG.nextDouble() < 0.01) {
      if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * enhancerDropChance) {
        dropItem(event.getLocation(), ArcaneEnhancer.get(), killer, 0, true,
            FaceColor.RED.getRawColor(), ChatColor.DARK_PURPLE, arcaneDropSound);
      }
      if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * tinkerGearDropChance) {
        dropItem(event.getLocation(), TinkerersGear.get(), killer, 0, true,
            FaceColor.RED.getRawColor(), ChatColor.RED, tinkerDropSound);
      }
      if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * faceOrbDropChance) {
        dropItem(event.getLocation(), OrbOfManyFaces.item(), killer, 0, true,
            FaceColor.RED.getRawColor(), ChatColor.RED, tinkerDropSound);
      }
      if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * hypercubeDropChance) {
        dropItem(event.getLocation(), PerfectSphere.item(), killer, 0, true,
            FaceColor.RED.getRawColor(), ChatColor.RED, tinkerDropSound);
      }
      if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * normalItemDropChance) {
        dropItem(event.getLocation(), NormalItem.item(), killer, 0, false,
            FaceColor.BLUE.getRawColor(), ChatColor.BLUE, purifyDropSound);
      }
      if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * distortDropChance) {
        dropItem(event.getLocation(), DistortionCell.item(), killer, 0, true,
            FaceColor.BLACK.getRawColor(), ChatColor.BLACK, tinkerDropSound);
      }
      if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * awakenerDropChance) {
        dropItem(event.getLocation(), AwakeningStone.item(), killer, 0, true,
            FaceColor.RED.getRawColor(), ChatColor.RED, tinkerDropSound);
      }
      if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * missingStatDropChance) {
        dropItem(event.getLocation(), MissingStat.item(), killer, 0, true,
            FaceColor.RED.getRawColor(), ChatColor.RED, tinkerDropSound);
      }
      if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * rarityPaintDropChance) {
        dropItem(event.getLocation(), TooltipPaint.item(), killer, 0, true,
            FaceColor.RED.getRawColor(), ChatColor.RED, tinkerDropSound);
      }
      if (LootPlugin.RNG.nextFloat() < bonusWithCheesePenalty * plugin.getSettings()
          .getDouble("config.drops.socket-extender", 0D)) {
        ItemStack his = SocketExtender.EXTENDER.clone();
        dropItem(event.getLocation(), his, killer, 0, true,
            FaceColor.TEAL.getRawColor(), ChatColor.AQUA, extenderDropSound);
      }
    }
  }

  private static void doUniqueDrops(MobLootTable mobLootTable, Location location, Player killer) {
    for (String gemString : mobLootTable.getGemMap().keySet()) {
      if (mobLootTable.getGemMap().get(gemString) > LootPlugin.RNG.nextFloat()) {
        SocketGem gem = plugin.getSocketGemManager().getSocketGem(gemString);
        if (gem == null) {
          continue;
        }
        ItemStack his = gem.toItemStack(1);
        RandomSound rs = gem.isBroadcast() ? gemDropSound : null;
        dropItem(location, his, killer, 0, gem.isBroadcast(),
            gem.isBroadcast() ? FaceColor.LIGHT_GREEN.getRawColor() : null,
            gem.isBroadcast() ? ChatColor.GREEN : null, rs);
      }
    }
    for (String str : mobLootTable.getMaterialMap().keySet()) {
      if (mobLootTable.getMaterialMap().get(str) > LootPlugin.RNG.nextFloat()) {
        CraftMaterial mar = CraftMaterial.get(str);
        if (mar == null) {
          continue;
        }
        ItemStack his = mar.item();
        dropItem(location, his, killer, 0, false, null, null, null);
      }
    }
    for (String tomeString : mobLootTable.getTomeMap().keySet()) {
      if (mobLootTable.getTomeMap().get(tomeString) > LootPlugin.RNG.nextFloat()) {
        EnchantmentTome tome = plugin.getEnchantTomeManager().getEnchantTome(tomeString);
        if (tome == null) {
          continue;
        }
        ItemStack his = tome.toItemStack(1);
        RandomSound rs = tome.isBroadcast() ? tomeDropSound : null;
        dropItem(location, his, killer, 0, tome.isBroadcast(),
            tome.isBroadcast() ? FaceColor.BLUE.getRawColor() : null,
            tome.isBroadcast() ? ChatColor.BLUE : null, rs);
      }
    }
    for (String tableName : mobLootTable.getCustomItemMap().keySet()) {
      double totalWeight = 0;
      for (double weight : mobLootTable.getCustomItemMap().get(tableName).values()) {
        totalWeight += weight;
      }
      totalWeight *= LootPlugin.RNG.nextFloat();
      double currentWeight = 0;
      for (String customName : mobLootTable.getCustomItemMap().get(tableName).keySet()) {
        currentWeight += mobLootTable.getCustomItemMap().get(tableName).get(customName);
        if (currentWeight >= totalWeight) {
          if ("NO_DROP_WEIGHT".equalsIgnoreCase(customName)) {
            break;
          }
          NewCustomItem ci = plugin.getCustomItemManager().getCustomItem(customName);
          if (ci == null) {
            break;
          }
          ItemStack his = ci.item();
          RandomSound rs = ci.isBroadcast() ? tieredDropSound : null;
          dropItem(location, his, killer, 0, ci.isBroadcast(),
              ci.isBroadcast() ? FaceColor.ORANGE.getRawColor() : null,
              ci.isBroadcast() ? ChatColor.GOLD : null, rs);
          break;
        }
      }
    }
  }

  private static void dropItem(Location loc, ItemStack itemStack, Player looter, int ticksLived,
      boolean broadcast, Color dropRgb, ChatColor glowColor, RandomSound randomSound) {
    ItemUtils.dropItem(loc, itemStack, looter, ticksLived, dropRgb, glowColor, randomSound, dropRgb != null);
    if (broadcast) {
      InventoryUtil.sendToDiscord(looter, itemStack, itemFoundFormat);
    }
  }

  public static Tier getTier(Player killer) {
    if (killer == null || customizedTierChance < LootPlugin.RNG.nextFloat()) {
      return Tier.getRandomTier();
    }
    List<ItemStack> itemStacks = getWornItems(killer);
    List<Tier> wornTiers = new ArrayList<>();
    for (ItemStack i : itemStacks) {
      EquipmentData data = PersistHelper.getEquipmentData(i);
      if (data == null || data.getTier() == null) {
        continue;
      }
      wornTiers.add(data.getTier());
    }
    if (wornTiers.isEmpty()) {
      return Tier.getRandomTier();
    }
    return wornTiers.get(LootPlugin.RNG.nextInt(0, wornTiers.size()));
  }

  private static List<ItemStack> getWornItems(Player player) {
    List<ItemStack> wornItems = new ArrayList<>();
    for (ItemStack stack : player.getEquipment().getArmorContents()) {
      if (stack == null || stack.getType() == Material.AIR) {
        continue;
      }
      wornItems.add(stack);
    }
    ItemStack handItem = player.getEquipment().getItemInMainHand();
    if (handItem.getType() != Material.AIR) {
      wornItems.add(handItem);
    }
    ItemStack offItem = player.getEquipment().getItemInOffHand();
    if (offItem.getType() != Material.AIR) {
      wornItems.add(offItem);
    }
    return wornItems;
  }
}
