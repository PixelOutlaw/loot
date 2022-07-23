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
package info.faceland.loot.menu.upgrade;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang.WordUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.tuple.Pair;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.enchantments.EnchantmentTome;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.managers.StatManager.RollStyle;
import info.faceland.loot.utils.MaterialUtil;
import info.faceland.loot.utils.NumberUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.PlayerDataUtil;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantMenu extends ItemMenu {

  private LootPlugin plugin;

  private static int baseEnhanceRequirement;
  private static int enhanceReqPerTwenty;

  private int playerPointHelmetMergeCost;

  private ItemStack selectedEquipment;
  private ItemStack selectedUpgradeItem;

  private ConfirmIcon confirmIcon;
  private EquipmentIcon equipmentIcon;
  private UpgradeItemIcon upgradeItemIcon;

  private final String validEnchant, invalidEnchant, validUpgrade, invalidUpgrade, validExtend,
      invalidExtend, itemNotEnchanted, alreadyEnhanced, noEnhanceLevel, validEnhance,
      validPurity, cannotRefill, validRefill;
  private final List<String> noEquipmentLore, noUpgradeItemLore, validEnchantLore, noEnchantTagLore,
      badTomeTypeLore, invalidUpgradeLore, badScrollRangeLore, validUpgradeLore, validExtendLore,
      invalidExtendLore, itemNotEnchantedLore, alreadyEnhancedLore, noEnhanceLevelLore,
      validEnhanceLore, validPurityLore, breakWarning, arcaneEnchanted, fullEnchantment,
      validRefillLore;

  private ItemStack blankItem;

  private static final DecimalFormat DF = new DecimalFormat("###.#");

  public EnchantMenu(LootPlugin plugin) {
    super(TextUtils.color(plugin.getSettings().getString("language.menu.menu-name",
        "&0&lUpgrade Items!")), Size.fit(27), plugin);

    this.plugin = plugin;

    baseEnhanceRequirement = plugin.getSettings().getInt("config.enhancement.base-level-req", 6);
    playerPointHelmetMergeCost = plugin.getSettings().getInt("config.helmet-merge-cost", 320);
    enhanceReqPerTwenty = plugin.getSettings()
        .getInt("config.enhancement.level-req-per-ten-levels", 8);

    validEnchant = TextUtils.color(plugin.getSettings()
        .getString("language.menu.valid-enchant-name", "aaaa"));
    validUpgrade = TextUtils.color(plugin.getSettings()
        .getString("language.menu.valid-upgrade-name", "aaaa"));
    validExtend = TextUtils.color(plugin.getSettings()
        .getString("language.menu.valid-extend-name", "aaaa"));
    invalidEnchant = TextUtils.color(plugin.getSettings()
        .getString("language.menu.invalid-enchant-name", "aaaa"));
    invalidUpgrade = TextUtils.color(plugin.getSettings()
        .getString("language.menu.invalid-upgrade-name", "aaaa"));
    invalidExtend = TextUtils.color(plugin.getSettings()
        .getString("language.menu.invalid-extend-name", "aaaa"));
    itemNotEnchanted = TextUtils.color(plugin.getSettings()
        .getString("language.menu.not-enchanted-name", "aaaa"));
    alreadyEnhanced = TextUtils.color(plugin.getSettings()
        .getString("language.menu.already-enhanced-name", "aaaa"));
    noEnhanceLevel = TextUtils.color(plugin.getSettings()
        .getString("language.menu.no-enhance-level-name", "aaaa"));
    validEnhance = TextUtils.color(plugin.getSettings()
        .getString("language.menu.valid-enhance-name", "aaaa"));
    validPurity = TextUtils.color(plugin.getSettings()
        .getString("language.menu.valid-purity-name", "aaaa"));
    cannotRefill = TextUtils.color(plugin.getSettings()
        .getString("language.menu.cannot-refill", "aaaa"));
    validRefill = TextUtils.color(plugin.getSettings()
        .getString("language.menu.valid-refill", "aaaa"));

    noEquipmentLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.no-equipment"));
    noUpgradeItemLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.no-upgrade"));
    validEnchantLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.valid-enchant-lore"));
    noEnchantTagLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.no-tag-lore"));
    badTomeTypeLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.type-mismatch-lore"));
    invalidUpgradeLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.invalid-upgrade-lore"));
    badScrollRangeLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.invalid-plus-range-lore"));
    validUpgradeLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.valid-upgrade-lore"));
    validExtendLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.valid-extend-lore"));
    invalidExtendLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.invalid-extend-lore"));
    itemNotEnchantedLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.not-enchanted-lore"));
    alreadyEnhancedLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.already-enhanced-lore"));
    noEnhanceLevelLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.no-enhance-level-lore"));
    validEnhanceLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.valid-enhance-lore"));
    validPurityLore = TextUtils.color(plugin.getSettings()
        .getStringList("language.menu.valid-purity-lore"));
    breakWarning = ListExtensionsKt.chatColorize(plugin.getSettings()
        .getStringList("language.menu.break-warning"));
    arcaneEnchanted = ListExtensionsKt.chatColorize(plugin.getSettings()
        .getStringList("language.menu.arcane-enchanted"));
    fullEnchantment = ListExtensionsKt.chatColorize(plugin.getSettings()
        .getStringList("language.menu.full-enchantment"));
    validRefillLore = ListExtensionsKt.chatColorize(plugin.getSettings()
        .getStringList("language.menu.valid-refill-lore"));

    blankItem = new ItemStack(Material.AIR);

    confirmIcon = new ConfirmIcon(this);
    equipmentIcon = new EquipmentIcon(this);
    upgradeItemIcon = new UpgradeItemIcon(this);

    setItem(3, equipmentIcon);
    setItem(5, upgradeItemIcon);
    setItem(22, confirmIcon);
  }

  public void setSelectedEquipment(Player player, ItemStack selectedEquipment) {
    if (MaterialUtil.isEssence(selectedEquipment)) {
      return;
    }
    this.selectedEquipment = selectedEquipment;
    equipmentIcon.getIcon().setAmount(selectedEquipment.getAmount());
    equipmentIcon.getIcon().setType(selectedEquipment.getType());
    equipmentIcon.getIcon().setItemMeta(selectedEquipment.getItemMeta());
    equipmentIcon.setDisplayName(ItemStackExtensionsKt.getDisplayName(selectedEquipment));
    updateConfirmIcon(player);
  }

  public void setSelectedUpgradeItem(Player player, ItemStack selectedUpgradeItem) {
    this.selectedUpgradeItem = selectedUpgradeItem;
    upgradeItemIcon.getIcon().setAmount(1);
    upgradeItemIcon.getIcon().setType(selectedUpgradeItem.getType());
    upgradeItemIcon.getIcon().setItemMeta(selectedUpgradeItem.getItemMeta());
    upgradeItemIcon.setDisplayName(ItemStackExtensionsKt.getDisplayName(selectedUpgradeItem));
    updateConfirmIcon(player);
  }

  private void updateConfirmIcon(Player player) {
    ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 61);
    List<String> lore = new ArrayList<>();
    if (selectedEquipment == null || selectedEquipment.getType() == Material.AIR) {
      confirmIcon.setDisplayName(StringExtensionsKt.chatColorize("&eNo Equipment Item..."));
      lore.addAll(noEquipmentLore);
      TextUtils.setLore(confirmIcon.getIcon(), lore);
      return;
    }
    if (selectedUpgradeItem == null || selectedUpgradeItem.getType() == Material.AIR) {
      confirmIcon.setDisplayName(StringExtensionsKt.chatColorize("&eNo Upgrade Item..."));
      lore.addAll(noUpgradeItemLore);
      TextUtils.setLore(confirmIcon.getIcon(), lore);
      return;
    }
    if (MaterialUtil.isEnchantmentItem(selectedUpgradeItem)) {
      if (!MaterialUtil.hasEnchantmentTag(selectedEquipment)) {
        confirmIcon.setDisplayName(invalidEnchant);
        lore.addAll(noEnchantTagLore);
        ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 62);
        TextUtils.setLore(confirmIcon.getIcon(), lore);
        return;
      }
      EnchantmentTome tome = MaterialUtil.getEnchantmentItem(selectedUpgradeItem);
      if (!MaterialUtil.matchesGroups(selectedEquipment, tome.getItemGroups())) {
        confirmIcon.setDisplayName(invalidEnchant);
        lore.addAll(badTomeTypeLore);
        ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 62);
        TextUtils.setLore(confirmIcon.getIcon(), lore);
        return;
      }
      confirmIcon.setDisplayName(validEnchant);
      ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 60);

      if (StringUtils.isNotBlank(tome.getStat())) {
        int itemLevel = MaterialUtil.getLevelRequirement(selectedEquipment);
        double enchantLevel = PlayerDataUtil.getEffectiveLifeSkill(player, LifeSkillType.ENCHANTING, true);
        double effectiveLevel = Math.max(1, Math.min(enchantLevel, itemLevel));
        ItemStat stat = LootPlugin.getInstance().getStatManager().getStat(tome.getStat());
        double rarityBonus = MaterialUtil.getBaseEnchantBonus(enchantLevel);

        String minStat = ChatColor.stripColor(
            (plugin.getStatManager().getFinalStat(stat, effectiveLevel, rarityBonus, false, RollStyle.MIN))
                .getStatString());
        String maxStat = ChatColor.stripColor(
            (plugin.getStatManager().getFinalStat(stat, effectiveLevel, rarityBonus, false, RollStyle.MAX))
                .getStatString());

        for (String s : validEnchantLore) {
          lore.add(s.replace("{min}", minStat).replace("{max}", maxStat));
        }

        lore.add("");
        lore.add("&bEnchantment Power: &f" + Math.min(itemLevel, (int) effectiveLevel));
        lore.add("&7 Determines max/min enchant");
        lore.add("&7 values! This number is the");
        lore.add("&7 lowest between your enchant");
        lore.add("&7 skill and the item's level.");
        lore.add("");
        lore.add("&dEnchantment Bonus: &f+" + rarityBonus * 100 + "%");
        lore.add("&7 Extra power based on your");
        lore.add("&7 enchanting skill, applied");
        lore.add("&7 after all other numbers!");
      }

      if (!tome.getEnchantments().isEmpty()) {
        lore.add("&fApplied Enchantments:");
        for (Enchantment enchantment : tome.getEnchantments().keySet()) {
          lore.add(" &9" + WordUtils.capitalizeFully(enchantment.getKey().getKey()) + " " + NumberUtil
              .toRoman(tome.getEnchantments().get(enchantment)));
        }
      }

      TextUtils.setLore(confirmIcon.getIcon(), ListExtensionsKt.chatColorize(lore));
      return;
    }
    if (SocketExtender.isSimilar(selectedUpgradeItem)) {
      if (!MaterialUtil.canBeExtended(new ArrayList<>(TextUtils.getLore(selectedEquipment)))) {
        confirmIcon.setDisplayName(invalidExtend);
        lore.addAll(invalidExtendLore);
        ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 62);
        TextUtils.setLore(confirmIcon.getIcon(), lore);
        return;
      }
      confirmIcon.setDisplayName(validExtend);
      ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 60);
      lore.addAll(validExtendLore);

      TextUtils.setLore(confirmIcon.getIcon(), lore);
      return;
    }
    if (ArcaneEnhancer.isSimilar(selectedUpgradeItem)) {
      Pair<String, Integer> enchantBar = MaterialUtil.getEnchantBar(selectedEquipment);
      if (MaterialUtil.getEnchantBar(selectedEquipment) == null) {
        confirmIcon.setDisplayName(itemNotEnchanted);
        lore.addAll(itemNotEnchantedLore);
        ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 62);
        TextUtils.setLore(confirmIcon.getIcon(), lore);
        return;
      }
      if (MaterialUtil.isArcaneEnchanted(enchantBar.getLeft())) {
        confirmIcon.setDisplayName(alreadyEnhanced);
        lore.addAll(alreadyEnhancedLore);
        ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 62);
        TextUtils.setLore(confirmIcon.getIcon(), lore);
        return;
      }

      int enchantingLevel = plugin.getStrifePlugin().getChampionManager().getChampion(player)
          .getLifeSkillLevel(LifeSkillType.ENCHANTING);
      if (enchantingLevel < getEnhanceRequirement(MaterialUtil.getItemLevel(selectedEquipment))) {
        confirmIcon.setDisplayName(noEnhanceLevel);
        lore.addAll(noEnhanceLevelLore);
        ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 62);
        TextUtils.setLore(confirmIcon.getIcon(), lore);
        return;
      }
      confirmIcon.setDisplayName(validEnhance);
      ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 60);
      lore.addAll(validEnhanceLore);

      TextUtils.setLore(confirmIcon.getIcon(), lore);
      return;
    }
    if (PurifyingScroll.isSimilar(selectedUpgradeItem)) {
      Pair<String, Integer> enchantBar = MaterialUtil.getEnchantBar(selectedEquipment);
      if (enchantBar == null) {
        confirmIcon.setDisplayName(itemNotEnchanted);
        lore.addAll(itemNotEnchantedLore);
        ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 62);
        TextUtils.setLore(confirmIcon.getIcon(), lore);
        return;
      }
      confirmIcon.setDisplayName(validPurity);
      ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 60);
      lore.addAll(validPurityLore);

      TextUtils.setLore(confirmIcon.getIcon(), lore);
      return;
    }

    UpgradeScroll scroll = plugin.getScrollManager().getScroll(selectedUpgradeItem);
    if (scroll != null) {
      if (!MaterialUtil.isUpgradePossible(selectedEquipment)) {
        confirmIcon.setDisplayName(invalidUpgrade);
        lore.addAll(invalidUpgradeLore);
        ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 62);
        TextUtils.setLore(confirmIcon.getIcon(), lore);
        return;
      }
      String equipName = ItemStackExtensionsKt.getDisplayName(selectedEquipment);
      int itemPlus = MaterialUtil.getUpgradeLevel(equipName);
      if (!MaterialUtil.meetsUpgradeRange(scroll, itemPlus)) {
        confirmIcon.setDisplayName(invalidUpgrade);
        lore.addAll(badScrollRangeLore);
        ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 62);
        TextUtils.setLore(confirmIcon.getIcon(), lore);
        return;
      }
      confirmIcon.setDisplayName(validUpgrade);
      ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 60);
      if (selectedEquipment.getType().getMaxDurability() < 5) {
        itemPlus += 3;
      } else {
        itemPlus += 1;
      }
      itemPlus = Math.min(itemPlus, 15);
      double successChance = Math
          .min(100, 100 * MaterialUtil.getSuccessChance(player, itemPlus, selectedUpgradeItem, scroll));
      double maxDura = selectedEquipment.getType().getMaxDurability();
      double damage;
      if (maxDura <= 1) {
        maxDura = 100;
        damage = 0;
      } else {
        damage = selectedEquipment.getDurability();

      }
      double maxPercent = MaterialUtil.getMaxFailureDamagePercent(scroll, itemPlus);
      double maxDamage = maxDura * maxPercent;
      double killChance = 0;
      double damageChance = 0;
      if (successChance < 99.9) {
        double failChance = 100 - successChance;
        killChance = failChance * Math.max(0, ((damage + maxDamage) - maxDura) / maxDamage);
        damageChance = failChance - killChance;
      }

      for (String s : validUpgradeLore) {
        lore.add(s.replace("{succ}", DF.format(successChance))
            .replace("{dam}", DF.format(damageChance))
            .replace("{ded}", DF.format(killChance)));
      }

      if (selectedEquipment.getDurability() > 0 && killChance > 0) {
        lore.addAll(breakWarning);
      }

      TextUtils.setLore(confirmIcon.getIcon(), lore);
    }

    if ("Magic Crystal".equals(ChatColor.stripColor(
        ItemStackExtensionsKt.getDisplayName(selectedUpgradeItem)))) {
      if (!MaterialUtil.isEnchanted(selectedEquipment)) {
        confirmIcon.setDisplayName(itemNotEnchanted);
        lore.addAll(itemNotEnchantedLore);
        ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 62);
        TextUtils.setLore(confirmIcon.getIcon(), lore);
        return;
      }
      if (MaterialUtil.isArcaneEnchanted(selectedEquipment)) {
        confirmIcon.setDisplayName(cannotRefill);
        lore.addAll(arcaneEnchanted);
        ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 62);
        TextUtils.setLore(confirmIcon.getIcon(), lore);
        return;
      }
      if (MaterialUtil.getMissingEnchantmentPower(selectedEquipment) < 1) {
        confirmIcon.setDisplayName(cannotRefill);
        lore.addAll(fullEnchantment);
        ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 62);
        TextUtils.setLore(confirmIcon.getIcon(), lore);
        return;
      }
      confirmIcon.setDisplayName(validRefill);
      ItemStackExtensionsKt.setCustomModelData(confirmIcon.getIcon(), 60);
      lore.addAll(validRefillLore);

      TextUtils.setLore(confirmIcon.getIcon(), lore);
    }
  }

  boolean doUpgrade(Player player) {
    if (selectedEquipment == null || selectedEquipment.getType() == Material.AIR ||
        selectedEquipment.getAmount() > 1) {
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return false;
    }
    if (MaterialUtil.isEnchantmentItem(selectedUpgradeItem)) {
      MaterialUtil.enchantItem(player, selectedUpgradeItem, selectedEquipment);
      setSelectedUpgradeItem(player, new ItemStack(Material.AIR));
    } else if (plugin.getScrollManager().getScroll(selectedUpgradeItem) != null) {
      MaterialUtil.upgradeItem(player, selectedUpgradeItem, selectedEquipment);
      setSelectedUpgradeItem(player, new ItemStack(Material.AIR));
    } else if (SocketExtender.isSimilar(selectedUpgradeItem)) {
      MaterialUtil.extendItem(player, selectedEquipment, selectedUpgradeItem);
      setSelectedUpgradeItem(player, selectedUpgradeItem);
    } else if (ArcaneEnhancer.isSimilar(selectedUpgradeItem)) {
      MaterialUtil.enhanceEnchantment(player, selectedEquipment, selectedUpgradeItem);
      setSelectedUpgradeItem(player, selectedUpgradeItem);
    } else if (PurifyingScroll.isSimilar(selectedUpgradeItem)) {
      MaterialUtil.purifyItem(player, selectedEquipment, selectedUpgradeItem);
      setSelectedUpgradeItem(player, selectedUpgradeItem);
    } else if ("Magic Crystal".equals(ChatColor
        .stripColor(ItemStackExtensionsKt.getDisplayName(selectedUpgradeItem)))) {
      MaterialUtil.refillEnchantment(player, selectedEquipment, selectedUpgradeItem);
      setSelectedUpgradeItem(player, selectedUpgradeItem);
    } else {
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      setSelectedUpgradeItem(player, selectedUpgradeItem);
      return false;
    }
    setSelectedEquipment(player, selectedEquipment);
    return true;
  }

  ItemStack getBlankItem() {
    return blankItem;
  }

  public static int getEnhanceRequirement(int itemLevel) {
    int eLevel = itemLevel / 20;
    return baseEnhanceRequirement + eLevel * enhanceReqPerTwenty;
  }

}

/*
00 01 02 03 04 05 06 07 08
09 10 11 12 13 14 15 16 17
18 19 20 21 22 23 24 25 26
27 28 29 30 31 32 33 34 35
36 37 38 39 40 41 42 43 44
45 46 47 48 49 50 51 52 53
*/
