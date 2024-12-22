/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.builders;

import static info.faceland.loot.LootPlugin.ITEM_DATA_CLASS;
import static info.faceland.loot.LootPlugin.ITEM_DATA_KEY;
import static info.faceland.loot.LootPlugin.ITEM_TYPE_KEY;
import static info.faceland.loot.LootPlugin.ITEM_VERSION;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.ItemGenerationReason;
import info.faceland.loot.data.BuiltItem;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.StoredStat;
import info.faceland.loot.items.Tier;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.PersistHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@Getter
public final class ItemBuilder {

  public static final String SOCKET_BASE = "|none|哀\uF822|orange|Socket";
  public static final String SOCKET = PaletteUtil.color(SOCKET_BASE);
  public static final String SOCKET_S = ChatColor.stripColor(SOCKET);
  public static final String EXTEND_BASE = "|none|品\uF822|teal|Socket";
  public static final String EXTEND = PaletteUtil.color(EXTEND_BASE);
  public static final String EXTEND_S = ChatColor.stripColor(EXTEND);
  public static final SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");

  private Tier tier;
  private ItemRarity rarity;
  private int level;
  private int plus;
  private Player creator;
  private int highRateEmptySlots;
  private boolean distorted = false;
  private boolean soulbound = false;
  private boolean enchantable = true;
  private boolean alwaysEssence = false;
  private boolean alwaysExtend = false;
  private int craftBonusStats = 0;
  private float socketChanceBonus = 0;
  private ItemGenerationReason generationReason = ItemGenerationReason.MONSTER;
  private Set<String> preferredStats = new HashSet<>();
  private boolean built = false;

  public ItemBuilder withRarity(ItemRarity r) {
    rarity = r;
    return this;
  }

  public ItemBuilder withHighRateEmptySlots(int l) {
    highRateEmptySlots = l;
    return this;
  }

  public ItemBuilder withLevel(int l) {
    level = l;
    return this;
  }

  public ItemBuilder withPlus(int l) {
    plus = l;
    return this;
  }

  public ItemBuilder withCraftBonusStats(int l) {
    craftBonusStats = l;
    return this;
  }

  public ItemBuilder withCreator(Player l) {
    creator = l;
    return this;
  }

  public ItemBuilder withSoulbound(boolean b) {
    soulbound = b;
    return this;
  }

  public ItemBuilder withDistortion(boolean b) {
    distorted = b;
    return this;
  }

  public ItemBuilder withEnchantable(boolean b) {
    enchantable = b;
    return this;
  }

  public ItemBuilder withAlwaysEssence(boolean b) {
    alwaysEssence = b;
    return this;
  }

  public ItemBuilder withAlwaysExtend(boolean b) {
    alwaysExtend = b;
    return this;
  }

  public ItemBuilder withTier(Tier t) {
    tier = t;
    return this;
  }

  public ItemBuilder socketChanceBonus(float l) {
    socketChanceBonus = l;
    return this;
  }

  public ItemBuilder withItemGenerationReason(ItemGenerationReason reason) {
    generationReason = reason;
    return this;
  }

  public ItemBuilder withPreferredStats(Set<String> preferredStats) {
    this.preferredStats = preferredStats;
    return this;
  }

  private ItemStat getRandomSecondaryStat() {
    return tier.getSecondaryStats().get(LootPlugin.RNG.nextInt(tier.getSecondaryStats().size()));
  }

  private ItemStat getRandomPrimaryStat() {
    return tier.getPrimaryStats().get(LootPlugin.RNG.nextInt(tier.getPrimaryStats().size()));
  }

  public BuiltItem build() {
    if (isBuilt()) {
      throw new IllegalStateException("already built");
    }
    ItemStack stack = new ItemStack(tier.getMaterial());
    boolean crafted = generationReason == ItemGenerationReason.CRAFTING;

    EquipmentData data = new EquipmentData();
    data.setTier(tier);
    data.setRarity(rarity.getId());
    data.setLevel(level);
    data.setUpgradeLevel(plus / tier.getUpgradeScale());

    ItemStat primaryStat = getRandomPrimaryStat();
    ItemStat secondaryStat = getRandomSecondaryStat();
    StoredStat primaryStoredStat = new StoredStat(primaryStat.getId(), 1);
    StoredStat secondaryStoredStat = new StoredStat(secondaryStat.getId(), 1);
    data.addCoreStat(primaryStoredStat);
    data.addCoreStat(secondaryStoredStat);

    List<ItemStat> bonusStatList = new ArrayList<>(tier.getBonusStats());
    List<ItemStat> preferredStatList = new ArrayList<>();
    bonusStatList.forEach(bs -> {
      if (preferredStats.contains(bs.getId())) {
        preferredStatList.add(bs);
      }
    });
    bonusStatList.removeIf(stat -> preferredStatList.contains(stat) || level < stat.getMinimumItemLevel());

    int bonusStats = LootPlugin.RNG.nextInt(rarity.getMinimumBonusStats(), rarity.getMaximumBonusStats() + 1);
    if (generationReason == ItemGenerationReason.CRAFTING) {
      bonusStats += craftBonusStats;
    }

    bonusStats = Math.max(1, bonusStats);

    int craftedStatSlots = 0;
    if (crafted) {
      int highRateTries = this.highRateEmptySlots;
      for (int i = 0; i <= bonusStats; i++) {
        if (highRateTries > 0) {
          craftedStatSlots += LootPlugin.RNG.nextDouble() < 0.25 ? 1 : 0;
          highRateTries--;
        } else {
          craftedStatSlots += LootPlugin.RNG.nextDouble() < 0.04 ? 1 : 0;
        }
      }
      if (craftedStatSlots == 0 && alwaysEssence) {
        craftedStatSlots = 1;
      }
      craftedStatSlots = Math.min(craftedStatSlots, bonusStats);
    }

    Map<String, Integer> categoryCount = new HashMap<>();
    for (int i = 0; i < bonusStats; i++) {
      ItemStat stat;
      if (!preferredStatList.isEmpty()) {
        stat = preferredStatList.get(LootPlugin.RNG.nextInt(preferredStatList.size()));
        float roll = (float) Math.pow(LootPlugin.RNG.nextFloat(), 2.85);
        data.addBonusStat(new StoredStat(stat.getId(), roll));
        preferredStatList.remove(stat);
      } else {
        stat = bonusStatList.get(LootPlugin.RNG.nextInt(bonusStatList.size()));
        float roll = (float) Math.pow(LootPlugin.RNG.nextFloat(), 2.85);
        data.addBonusStat(new StoredStat(stat.getId(), roll));
        bonusStatList.remove(stat);
      }

      if (ItemStat.LIMIT_CATEGORY_IDS.containsKey(stat.getStat())) {
        for (var category : ItemStat.LIMIT_CATEGORY_IDS.get(stat.getStat())) {
          categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
          if (categoryCount.get(category) >= tier.getStatCategoryLimits().getOrDefault(category, 100)) {
            preferredStatList.removeIf(bs -> ItemStat.LIMIT_CATEGORY_STATS.get(category).contains(bs.getStat()));
            bonusStatList.removeIf(bs -> ItemStat.LIMIT_CATEGORY_STATS.get(category).contains(bs.getStat()));
            categoryCount.remove(category);
          }
        }
      }
    }
    while (craftedStatSlots > 0) {
      data.getBonusStats().get(craftedStatSlots - 1).setEssenceSlot(true);
      craftedStatSlots--;
    }
    data.sortBonusStats();

    data.setEnchantable(enchantable);

    int extenders = 0;
    int sockets = 0;
    int maxSockets = tier.getMaxSockets(rarity.getId());
    if (maxSockets > 0) {
      sockets = tier.getMinSockets(rarity.getId());
      double socketUpChance = crafted ? rarity.getSocketChance() / 2f : rarity.getSocketChance();
      socketUpChance += socketChanceBonus;
      int failures = 0;
      while (failures + sockets + extenders < maxSockets) {
        if (LootPlugin.RNG.nextFloat() < socketUpChance) {
          if (LootPlugin.RNG.nextFloat() < rarity.getExtenderChance()) {
            extenders++;
          } else {
            sockets++;
          }
        } else {
          failures++;
        }
      }
    }

    if (alwaysExtend && extenders < 1 && sockets + extenders < maxSockets) {
      extenders++;
    }

    data.setEmptyGemSlots(Math.max(0, sockets));
    data.setEmptyExtendSlots(Math.max(0, extenders));

    if (crafted && creator != null) {
      data.setString("craftedBy", creator.getName());
    }
    if (creator != null) {
      data.setDiscoveredBy(creator.getName());
    }
    data.setCreationDate(simpleFormat.format(new Date()));

    if (distorted) {
      data.distort();
    }

    String suffix;
    if (LootPlugin.RNG.nextFloat() < 0.3f) {
      suffix = LootPlugin.getInstance().getNameManager().getRandomSuffix(rarity);
    } else {
      suffix = tier.getItemSuffixes(rarity).get(LootPlugin.RNG.nextInt(tier.getItemSuffixes(rarity).size()));
    }
    String prefix = LootPlugin.getInstance().getNameManager().getRandomPrefix(rarity, 21 - suffix.length());

    data.setString("name", prefix != null ? prefix + " " + suffix : suffix);

    ItemMeta meta = stack.getItemMeta();

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "equipment");
    pdc.set(ITEM_DATA_KEY, ITEM_DATA_CLASS, data);

    stack.setItemMeta(meta);

    if (soulbound) {
      PersistHelper.setTradeLocked(stack, true);
    }

    ItemRenderer.renderItem(stack, true);
    built = true;
    return new BuiltItem(stack, rarity.getLivedTicks());
  }
}
