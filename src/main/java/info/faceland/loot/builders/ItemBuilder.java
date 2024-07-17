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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class ItemBuilder {

  public static final String SOCKET_BASE = "|none|哀\uF822|orange|Socket";
  public static final String SOCKET = PaletteUtil.color(SOCKET_BASE);
  public static final String SOCKET_S = ChatColor.stripColor(SOCKET);
  public static final String EXTEND_BASE = "|none|品\uF822|teal|Socket";
  public static final String EXTEND = PaletteUtil.color(EXTEND_BASE);
  public static final String EXTEND_S = ChatColor.stripColor(EXTEND);

  @Getter
  private boolean built = false;
  private Tier tier;
  private ItemRarity rarity;
  private int level;
  private int plus;
  private Player creator;
  private float slotScore;
  private boolean distorted = false;
  private boolean soulbound = false;
  private boolean enchantable = true;
  private boolean alwaysEssence = false;
  private int sockets = -1;
  private int extendSlots = -1;
  private int craftBonusStats = 0;
  private ItemGenerationReason itemGenerationReason = ItemGenerationReason.MONSTER;

  public ItemBuilder withRarity(ItemRarity r) {
    rarity = r;
    return this;
  }

  public ItemBuilder withSlotScore(float l) {
    slotScore = l;
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

  public ItemBuilder withSockets(int l) {
    sockets = l;
    return this;
  }

  public ItemBuilder withExtendSlots(int l) {
    extendSlots = l;
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

  public ItemBuilder withTier(Tier t) {
    tier = t;
    return this;
  }

  public ItemBuilder withItemGenerationReason(ItemGenerationReason reason) {
    itemGenerationReason = reason;
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
    boolean crafted = itemGenerationReason == ItemGenerationReason.CRAFTING;

    EquipmentData data = new EquipmentData();
    data.setTier(tier);
    data.setRarity(rarity.getId());
    data.setLevel(level);
    data.setUpgradeLevel(plus / tier.getUpgradeScale());

    data.setUpgradeLevel(0);

    ItemStat primaryStat = getRandomPrimaryStat();
    ItemStat secondaryStat = getRandomSecondaryStat();
    StoredStat primaryStoredStat = new StoredStat(primaryStat.getId(), 1);
    StoredStat secondaryStoredStat = new StoredStat(secondaryStat.getId(), 1);
    data.addCoreStat(primaryStoredStat);
    data.addCoreStat(secondaryStoredStat);

    List<ItemStat> bonusStatList = new ArrayList<>(tier.getBonusStats());
    bonusStatList.removeIf(stat -> level < stat.getMinimumItemLevel());

    int bonusStats = LootPlugin.RNG.nextInt(rarity.getMinimumBonusStats(), rarity.getMaximumBonusStats());
    if (itemGenerationReason == ItemGenerationReason.CRAFTING) {
      bonusStats += craftBonusStats;
    }

    bonusStats = Math.max(1, bonusStats);

    boolean alwaysEssence = this.alwaysEssence;
    Map<String, Integer> categoryCount = new HashMap<>();
    int craftedStatSlots = 0;
    for (int i = 0; i < bonusStats; i++) {
      if (crafted && (alwaysEssence || LootPlugin.RNG.nextFloat() < slotScore / 5)) {
        craftedStatSlots++;
        alwaysEssence = false;
      }
      ItemStat stat = bonusStatList.get(LootPlugin.RNG.nextInt(bonusStatList.size()));
      float roll = (float) Math.pow(LootPlugin.RNG.nextFloat(), 2.85);
      data.addBonusStat(new StoredStat(stat.getId(), roll));
      bonusStatList.remove(stat);

      if (stat.getCategory() != null) {
        categoryCount.put(stat.getCategory(), categoryCount.getOrDefault(stat.getCategory(), 0) + 1);
        if (categoryCount.get(stat.getCategory()) >= tier.getStatCategoryLimits().getOrDefault(stat.getCategory(), 100)) {
          bonusStatList.removeIf(bs -> stat.getCategory().equals(bs.getCategory()));
          categoryCount.remove(stat.getCategory());
        }
      }
    }
    while (craftedStatSlots > 0) {
      data.getBonusStats().get(craftedStatSlots - 1).setEssenceSlot();
      craftedStatSlots--;
    }
    data.sortBonusStats();

    data.setEnchantable(enchantable);

    int sockets = this.sockets == -1 ? rarity.getMinimumSockets() : this.sockets;
    sockets = Math.min(sockets, tier.getMaximumSockets());
    int maxSockets = Math.min(rarity.getMaximumSockets(), tier.getMaximumSockets());
    while (sockets < maxSockets && LootPlugin.RNG.nextFloat() < rarity.getSocketChance()) {
      sockets++;
    }

    int minExtenders = this.extendSlots == -1 ? tier.getMinimumExtendSlots() : this.extendSlots;
    int extenders = Math.min(minExtenders, tier.getMaximumExtendSlots());
    while (extenders < tier.getMaximumExtendSlots() && LootPlugin.RNG.nextFloat() < rarity.getExtenderChance()) {
      extenders++;
    }

    data.setEmptyGemSlots(sockets);
    data.setEmptyExtendSlots(extenders);

    if (crafted && creator != null) {
      data.setString("craftedBy", creator.getName());
    }

    if (distorted) {
      data.distort();
    }

    String prefix = LootPlugin.getInstance().getNameManager().getRandomPrefix(rarity);
    String suffix;
    if (LootPlugin.RNG.nextFloat() < 0.3f) {
      suffix = LootPlugin.getInstance().getNameManager().getRandomSuffix(rarity);
    } else {
      suffix = tier.getItemSuffixes(rarity).get(LootPlugin.RNG.nextInt(tier.getItemSuffixes(rarity).size()));
    }

    data.setString("name", prefix + " " + suffix);

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
