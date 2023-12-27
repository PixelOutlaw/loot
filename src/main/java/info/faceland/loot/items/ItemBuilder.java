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
package info.faceland.loot.items;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.ItemGenerationReason;
import info.faceland.loot.data.BuiltItem;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.StatResponse;
import info.faceland.loot.listeners.crafting.CraftingListener;
import info.faceland.loot.managers.LootNameManager;
import info.faceland.loot.managers.StatManager;
import info.faceland.loot.tier.Tier;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemBuilder {

  private final StatManager statManager;
  private final LootNameManager nameManager;

  public static final String SOCKET_BASE = "|none|哀\uF822|orange|Socket";
  public static final String SOCKET = PaletteUtil.color(SOCKET_BASE);
  public static final String SOCKET_S = ChatColor.stripColor(SOCKET);
  public static final String EXTEND_BASE = "|none|品\uF822|teal|Socket";
  public static final String EXTEND = PaletteUtil.color(EXTEND_BASE);
  public static final String EXTEND_S = ChatColor.stripColor(EXTEND);

  private boolean built = false;
  private Tier tier;
  private ItemRarity rarity;
  private int level;
  private Player creator;
  private float slotScore;
  private Material material;
  private boolean distorted = false;
  private boolean enchantable = true;
  private boolean alwaysEssence = false;
  private int sockets = -1;
  private int extendSlots = -1;
  private int craftBonusStats = 0;
  private ItemGenerationReason itemGenerationReason = ItemGenerationReason.MONSTER;

  private double specialStatChance;

  public ItemBuilder(LootPlugin plugin) {
    statManager = plugin.getStatManager();
    nameManager = plugin.getNameManager();
  }

  public boolean isBuilt() {
    return built;
  }

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

  public ItemBuilder withMaterial(Material m) {
    material = m;
    return this;
  }

  public ItemBuilder withItemGenerationReason(ItemGenerationReason reason) {
    itemGenerationReason = reason;
    return this;
  }

  private ItemStat getRandomSecondaryStat() {
    return tier.getSecondaryStats().get(LootPlugin.RNG.nextInt(tier.getSecondaryStats().size()));
  }

  public BuiltItem build() {
    if (isBuilt()) {
      throw new IllegalStateException("already built");
    }
    built = true;
    ItemStack stack;
    if (material == null) {
      Set<Material> set = tier.getAllowedMaterials();
      Material[] array = set.toArray(new Material[0]);
      if (set.isEmpty()) {
        throw new RuntimeException("array length is 0 for tier: " + tier.getName());
      }
      material = array[LootPlugin.RNG.nextInt(array.length)];
    }
    stack = new ItemStack(material);
    List<String> lore = new ArrayList<>();
    float rarityPower = (float) rarity.getPower();

    boolean crafted = itemGenerationReason == ItemGenerationReason.CRAFTING;
    FaceColor color = crafted ? FaceColor.CYAN : rarity.getColor();

    if (tier.isSkillRequirement()) {
      lore.add(FaceColor.WHITE + "Skill Requirement: " + level);
    } else {
      lore.add(FaceColor.WHITE + "Level Requirement: " + level);
    }
    if (distorted && !Bukkit.getOnlinePlayers().isEmpty()) {
      while (creator == null) {
        for (Player p : Bukkit.getOnlinePlayers()) {
          creator = p;
        }
      }
      //String buf = Integer.toHexString(color.getRawColor().getRGB());
      //String hex = "#"+buf.substring(buf.length()-6);
      lore.add(FaceColor.WHITE + "և" + rarity.getName() + tier.getName());
    } else {
      lore.add(FaceColor.WHITE + rarity.getName() + tier.getName());
    }

    lore.add("");

    lore.add(statManager.getFinalStat(tier.getPrimaryStat(), level, rarityPower).getStatString());
    lore.add(statManager.getFinalStat(getRandomSecondaryStat(), level, rarityPower).getStatString());

    lore.add("");

    List<ItemStat> bonusStatList = new ArrayList<>(tier.getBonusStats());
    bonusStatList.removeIf(stat -> level < stat.getMinimumItemLevel());

    int bonusStats = LootPlugin.RNG.nextInt(rarity.getMinimumBonusStats(), rarity.getMaximumBonusStats());
    if (itemGenerationReason == ItemGenerationReason.CRAFTING) {
      bonusStats += craftBonusStats;
    }

    bonusStats = Math.max(1, bonusStats);

    int invertedIndex = -1;
    int distortionBonus = 0;
    if (distorted) {
      distortionBonus = Math.max(10, level / 3);
      rarityPower += 1;
      level += distortionBonus;
      invertedIndex = LootPlugin.RNG.nextInt(bonusStats);
    }

    boolean alwaysEssence = this.alwaysEssence;
    List<StatResponse> responseList = new ArrayList<>();
    Map<String, Integer> categories = new HashMap<>();
    int essenceSlots = 0;
    for (int i = 0; i < bonusStats; i++) {
      if (crafted && (alwaysEssence || LootPlugin.RNG.nextFloat() < slotScore / 5)) {
        essenceSlots++;
        alwaysEssence = false;
        continue;
      }
      ItemStat stat = bonusStatList.get(LootPlugin.RNG.nextInt(bonusStatList.size()));
      StatResponse rStat = statManager.getFinalStat(stat, level, rarityPower);
      if (invertedIndex == i) {
        rStat.setInverted(true);
      } else if (crafted) {
        rStat.setCrafted(true);
      }
      responseList.add(rStat);
      bonusStatList.remove(stat);
      if (stat.getCategory() != null) {
        categories.put(stat.getCategory(), categories.getOrDefault(stat.getCategory(), 0) + 1);
        if (categories.get(stat.getCategory()) >= tier.getStatCategoryLimits().getOrDefault(stat.getCategory(), 100)) {
          bonusStatList.removeIf(bs -> stat.getCategory().equals(bs.getCategory()));
          categories.remove(stat.getCategory());
        }
      }
    }

    if (distorted) {
      level -= distortionBonus;
    }

    String prefix = nameManager.getRandomPrefix(rarity);
    List<String> goodStatPrefixes = new ArrayList<>();
    responseList.sort((b, a) -> Float.compare(a.getStatRoll(), b.getStatRoll()));

    for (StatResponse r : responseList) {
      if (r.isInverted()) {
        continue;
      }
      if (r.getStatRoll() > 0.85 && StringUtils.isNotBlank(r.getStatPrefix())) {
        goodStatPrefixes.add(r.getStatPrefix());
      }
    }

    if (goodStatPrefixes.size() > 1) {
      prefix = goodStatPrefixes.get(0) + " " + goodStatPrefixes.get(1);
    } else if (goodStatPrefixes.size() == 1) {
      prefix = goodStatPrefixes.get(0);
    }

    for (StatResponse r : responseList) {
      if (r.isInverted()) {
        lore.add(FaceColor.RED + ChatColor.stripColor(r.getStatString()).replace("+", "-"));
      } else {
        lore.add(crafted ?
            FaceColor.CYAN + ChatColor.stripColor(r.getStatString()) : r.getStatString());
      }
    }

    while (essenceSlots > 0) {
      lore.add(FaceColor.CYAN + CraftingListener.ESSENCE_SLOT_TEXT);
      essenceSlots--;
    }

    if (enchantable) {
      lore.add("");
      for (int i = 0; i < rarity.getEnchantments(); i++) {
        lore.add(MaterialUtil.ENCHANTABLE_TAG);
      }
    }

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

    if (extenders > 0 || sockets > 0) {
      lore.add("");
    }
    for (int i = 0; i < sockets; i++) {
      lore.add(SOCKET);
    }

    for (int i = 0; i < extenders; i++) {
      lore.add(ItemBuilder.EXTEND);
    }

    if (crafted && creator != null) {
      lore.add("");
      lore.add(PaletteUtil.color("|dgray||i|[By: " + creator.getName()) + "]");
    }

    String suffix;
    if (LootPlugin.RNG.nextFloat() < 0.3f) {
      suffix = nameManager.getRandomSuffix(rarity);
    } else {
      suffix = tier.getItemSuffixes(rarity).get(LootPlugin.RNG.nextInt(tier.getItemSuffixes(rarity).size()));
    }

    ItemStackExtensionsKt.setDisplayName(stack, color + prefix + " " + suffix);
    TextUtils.setLore(stack, lore);
    stack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);

    // This section exists to clear existing item attributes and enforce
    // no stacking on equipment items
    ItemMeta iMeta = stack.getItemMeta();
    double serialValue = LootPlugin.RNG.nextFloat() * 0.0001;
    AttributeModifier serial = new AttributeModifier("SERIAL", serialValue, Operation.ADD_NUMBER);
    iMeta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, serial);
    stack.setItemMeta(iMeta);

    MaterialUtil.applyTierLevelData(stack, tier, level);

    return new BuiltItem(stack, rarity.getLivedTicks());
  }
}
