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

import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.noShadow;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.StatResponse;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.LevelModItem;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.AspectOfPerfection;
import info.faceland.loot.items.prefabs.AwakeningStone;
import info.faceland.loot.items.prefabs.DistortionCell;
import info.faceland.loot.items.prefabs.DivineHammer;
import info.faceland.loot.items.prefabs.ManaCrystal;
import info.faceland.loot.items.prefabs.MissingStat;
import info.faceland.loot.items.prefabs.NormalItem;
import info.faceland.loot.items.prefabs.OrbOfManyFaces;
import info.faceland.loot.items.prefabs.OrbOfOscillation;
import info.faceland.loot.items.prefabs.PerfectSphere;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.items.prefabs.TinkerersGear;
import info.faceland.loot.items.prefabs.TooltipPaint;
import info.faceland.loot.items.prefabs.TraceOfTrash;
import info.faceland.loot.items.prefabs.WarpedTongs;
import info.faceland.loot.managers.StatManager.RollStyle;
import info.faceland.loot.utils.EnchantUtil;
import info.faceland.loot.utils.MaterialUtil;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.util.PlayerDataUtil;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class EnchantMenu extends ItemMenu {

  private final LootPlugin plugin;

  private ItemStack selectedEquipment;
  private ItemStack selectedUpgradeItem;

  private final ConfirmIcon confirmIcon;
  private final EquipmentIcon equipmentIcon;
  private final UpgradeItemIcon upgradeItemIcon;

  private final String validEnchant, invalidEnchant, validUpgrade, invalidUpgrade, validExtend,
      invalidExtend, itemNotEnchanted, alreadyEnhanced, noEnhanceLevel, validEnhance, validPurity,
      cannotRefill, validRefill, invalidSocket, validSocket, validTinker, invalidTinker;
  private final List<String> noEquipmentLore, noUpgradeItemLore, validEnchantLore, noEnchantTagLore,
      badTomeTypeLore, invalidUpgradeLore, badScrollRangeLore, validUpgradeLore, validExtendLore,
      invalidExtendLore, itemNotEnchantedLore, alreadyEnhancedLore, noEnhanceLevelLore,
      validEnhanceLore, validPurityLore, breakWarning, arcaneEnchanted, fullEnchantment,
      validRefillLore, noSocketSlotsLore, invalidSocketTypeLore, validSocketLore, hasCraftStatLore,
      noTinkerStatsLore, validTinkerLore;

  private final ItemStack blankItem;

  private static final DecimalFormat DF = new DecimalFormat("###.#");

  public EnchantMenu(LootPlugin plugin) {
    super(TextUtils.color(plugin.getSettings().getString("language.menu.menu-name",
        "&0&lUpgrade Items!")), Size.fit(27), plugin);

    this.plugin = plugin;

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
    invalidSocket = TextUtils.color(plugin.getSettings()
        .getString("language.menu.invalid-socket", "aaaa"));
    validSocket = TextUtils.color(plugin.getSettings()
        .getString("language.menu.valid-socket", "aaaa"));
    validTinker = TextUtils.color(plugin.getSettings()
        .getString("language.menu.valid-tinker", "aaaa"));
    invalidTinker = TextUtils.color(plugin.getSettings()
        .getString("language.menu.invalid-tinker", "aaaa"));

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
    noSocketSlotsLore = ListExtensionsKt.chatColorize(plugin.getSettings()
        .getStringList("language.menu.no-socket-slot"));
    invalidSocketTypeLore = ListExtensionsKt.chatColorize(plugin.getSettings()
        .getStringList("language.menu.invalid-socket-type"));
    validSocketLore = ListExtensionsKt.chatColorize(plugin.getSettings()
        .getStringList("language.menu.valid-socket-lore"));
    hasCraftStatLore = ListExtensionsKt.chatColorize(plugin.getSettings()
        .getStringList("language.menu.no-craft-stats"));
    noTinkerStatsLore = ListExtensionsKt.chatColorize(plugin.getSettings()
        .getStringList("language.menu.no-tinker-stats"));
    validTinkerLore = ListExtensionsKt.chatColorize(plugin.getSettings()
        .getStringList("language.menu.valid-tinker-lore"));

    blankItem = new ItemStack(Material.AIR);

    confirmIcon = new ConfirmIcon(this);
    equipmentIcon = new EquipmentIcon(this);
    upgradeItemIcon = new UpgradeItemIcon(this);

    setItem(3, equipmentIcon);
    setItem(5, upgradeItemIcon);
    setItem(22, confirmIcon);
  }

  public void setSelectedEquipment(Player player, ItemStack selectedEquipment) {
    this.selectedEquipment = selectedEquipment;
    equipmentIcon.getIcon().setAmount(selectedEquipment.getAmount());
    equipmentIcon.getIcon().setType(selectedEquipment.getType());
    equipmentIcon.getIcon().setItemMeta(selectedEquipment.getItemMeta());
    equipmentIcon.getIcon().editMeta(m -> m.displayName(selectedEquipment.getItemMeta().customName()));
    updateConfirmIcon(player);
  }

  public void setSelectedUpgradeItem(Player player, ItemStack selectedUpgradeItem) {
    this.selectedUpgradeItem = selectedUpgradeItem;
    upgradeItemIcon.getIcon().setAmount(1);
    upgradeItemIcon.getIcon().setType(selectedUpgradeItem.getType());
    upgradeItemIcon.getIcon().setItemMeta(selectedUpgradeItem.getItemMeta());
    upgradeItemIcon.getIcon().editMeta(m -> m.displayName(selectedUpgradeItem.getItemMeta().customName()));
    updateConfirmIcon(player);
  }

  private void updateConfirmIcon(Player player) {
    ItemUtils.setItemModel(confirmIcon.getIcon(), "faceland:icons/enchant_invalid");
    List<String> lore = new ArrayList<>();
    if (selectedEquipment == null || selectedEquipment.getType() == Material.AIR) {
      updateConfirmDisplay(StringExtensionsKt.chatColorize("&eNo Equipment Item..."), noEquipmentLore, "faceland:icons/enchant_incomplete");
      return;
    }
    if (selectedUpgradeItem == null || selectedUpgradeItem.getType() == Material.AIR) {
      updateConfirmDisplay(StringExtensionsKt.chatColorize("&eNo Upgrade Item..."), noUpgradeItemLore, "faceland:icons/enchant_incomplete");
      return;
    }
    switch (PersistHelper.getItemType(selectedUpgradeItem)) {
      case SocketGem.TYPE_KEY -> updateSocketDisplay(lore);
      case EnchantmentTome.TYPE_KEY -> updateEnchantDisplay(player, lore);
      case SocketExtender.TYPE_KEY -> updateExtenderDisplay();
      case TinkerersGear.TYPE_KEY -> updateTinkerDisplay();
      case OrbOfManyFaces.TYPE_KEY, DivineHammer.TYPE_KEY, PerfectSphere.TYPE_KEY -> updateRerollDisplay();
      case AspectOfPerfection.TYPE_KEY -> updatePolishDisplay();
      case UpgradeScroll.TYPE_KEY -> updateScrollDisplay(UpgradeScroll.get(selectedUpgradeItem), player, lore);
      case ArcaneEnhancer.TYPE_KEY -> updateArcaneDisplay(player);
      case PurifyingScroll.TYPE_KEY -> updatePurityDisplay();
      case ManaCrystal.TYPE_KEY -> updateRefillDisplay(lore);
      case DistortionCell.TYPE_KEY -> updateDistortDisplay();
      case NormalItem.TYPE_KEY -> updateNormalDisplay();
      case AwakeningStone.TYPE_KEY -> updateAwakenDisplay();
      case MissingStat.TYPE_KEY -> updateAwakenDisplay(); // TODO: this is generic make it better
      case TooltipPaint.TYPE_KEY -> updateRarityUpDisplay();
      case OrbOfOscillation.TYPE_KEY -> updateFlipDisplay();
      case TraceOfTrash.TYPE_KEY -> updateTrashDisplay();
      case WarpedTongs.TYPE_KEY -> updateWarpedDisplay();
      default -> {
        // NOTHING
      }
    }
    updateConfirmDisplay(validUpgrade, lore, "faceland:icons/enchant_ready");
  }

  private void updateEnchantDisplay(Player player, List<String> lore) {
    EnchantmentTome tome = EnchantmentTome.get(selectedUpgradeItem);
    if (!EnchantUtil.canBeEnchanted(player, tome, selectedEquipment)) {
      updateConfirmDisplay(invalidEnchant, noEnchantTagLore, "faceland:icons/enchant_invalid");
      return;
    }
    if (!MaterialUtil.isInGroup(selectedEquipment, tome.getItemGroup())) {
      updateConfirmDisplay(invalidEnchant, badTomeTypeLore, "faceland:icons/enchant_invalid");
      return;
    }
    if (StringUtils.isNotBlank(tome.getStat())) {
      int itemLevel = MaterialUtil.getLevelRequirement(selectedEquipment);
      SkillLevelData enchantSkill = PlayerDataUtil.getSkillLevels(player, LifeSkillType.ENCHANTING, true);
      ItemStat stat = LootPlugin.getInstance().getStatManager().getStat(tome.getStat());

      float enchantPower = Math.max(1, Math.min(enchantSkill.getLevelWithBonus(), itemLevel));
      float rarity = (float) EnchantUtil.getBaseEnchantBonus(enchantSkill.getLevelWithBonus());

      StatResponse srMin = plugin.getStatManager().getFinalStat(stat, enchantPower, rarity, RollStyle.MIN, 1, 0);
      StatResponse srMax = plugin.getStatManager().getFinalStat(stat, enchantPower, rarity, RollStyle.MAX, 1, 0);
      String minStat = srMin.getStatString().content();
      String maxStat = srMax.getStatString().content();

      for (String s : validEnchantLore) {
        lore.add(s.replace("{min}", minStat).replace("{max}", maxStat));
      }
      lore.add("");
      lore.add(FaceColor.CYAN + "Enchantment Power: " + FaceColor.TRUE_WHITE + (int) enchantPower);
      lore.add(FaceColor.LIGHT_GRAY + " Determines max/min enchant");
      lore.add(FaceColor.LIGHT_GRAY + " values! This number is the");
      lore.add(FaceColor.LIGHT_GRAY + " lowest between your enchant");
      lore.add(FaceColor.LIGHT_GRAY + " skill and the item's level.");
      lore.add("");
      lore.add(FaceColor.PURPLE + "Enchantment Bonus: " + FaceColor.TRUE_WHITE + "+" + DF.format(rarity * 100) + "%");
      lore.add(FaceColor.LIGHT_GRAY + " Extra power based on your");
      lore.add(FaceColor.LIGHT_GRAY + " enchanting skill, applied");
      lore.add(FaceColor.LIGHT_GRAY + " after all other numbers!");
    }
    updateConfirmDisplay(validEnchant, ListExtensionsKt.chatColorize(lore), "faceland:icons/enchant_ready");
  }

  private void updateExtenderDisplay() {
    if (MaterialUtil.indexOfExtend(TextUtils.getLore(selectedEquipment)) == -1) {
      updateConfirmDisplay(invalidExtend, invalidExtendLore, "faceland:icons/enchant_invalid");
      return;
    }
    updateConfirmDisplay(validExtend, validExtendLore, "faceland:icons/enchant_ready");
  }

  private void updateArcaneDisplay(Player player) {
    if (!PersistHelper.isEnchanted(selectedUpgradeItem)) {
      updateConfirmDisplay(itemNotEnchanted, itemNotEnchantedLore, "faceland:icons/enchant_invalid");
      return;
    }
    if (PersistHelper.isEnhanced(selectedUpgradeItem)) {
      updateConfirmDisplay(alreadyEnhanced, alreadyEnhancedLore, "faceland:icons/enchant_invalid");
      return;
    }
    Champion champion = LootPlugin.getInstance().getStrifePlugin().getChampionManager().getChampion(player);
    if (MaterialUtil.getLevelRequirement(selectedEquipment) > EnchantMenu.getEnhanceRequirement(
        SkillRank.getRank(champion, LifeSkillType.ENCHANTING))) {
      updateConfirmDisplay(noEnhanceLevel, noEnhanceLevelLore, "faceland:icons/enchant_invalid");
      return;
    }
    updateConfirmDisplay(validEnhance, validEnhanceLore, "faceland:icons/enchant_ready");
  }

  private void updatePurityDisplay() {
    if (!PersistHelper.isEnchanted(selectedEquipment)) {
      updateConfirmDisplay(itemNotEnchanted, itemNotEnchantedLore, "faceland:icons/enchant_invalid");
      return;
    }
    updateConfirmDisplay(validPurity, validPurityLore, "faceland:icons/enchant_ready");
  }

  private void updateTinkerDisplay() {
    EquipmentData equipmentData = PersistHelper.getEquipmentData(selectedEquipment);
    if (equipmentData == null) {
      updateConfirmDisplay(invalidTinker, hasCraftStatLore, "faceland:icons/enchant_invalid");
      return;
    }
    boolean canExtract = equipmentData.canExtractStat(false);
    if (!canExtract) {
      updateConfirmDisplay(invalidTinker, noTinkerStatsLore, "faceland:icons/enchant_invalid");
      return;
    }
    var stats = equipmentData.getExtractableStats();
    if (stats.isEmpty()) {
      updateConfirmDisplay(invalidTinker, noTinkerStatsLore, "faceland:icons/enchant_invalid");
      return;
    }
    updateConfirmDisplay(validTinker, validTinkerLore, "faceland:icons/enchant_ready");
  }

  private void updateRerollDisplay() {
    updateConfirmDisplay(
        PaletteUtil.color("|green|Reroll Item's Stats"),
        PaletteUtil.color(List.of(
            "",
            "|lgray|This will make stats in the random",
            "|lgray|section into a new value between",
            "|lgray|their minimum and maximum!",
            "",
            "|lime|Good luck!"
        )),
        "faceland:icons/enchant_ready"
    );
  }

  private void updateDistortDisplay() {
    updateConfirmDisplay(
        PaletteUtil.color("|crimson|Distort Item"),
        PaletteUtil.color(List.of(
            "",
            "|lgray|This will make stats in the random",
            "|lgray|section stronger, but flip one",
            "|lgray|to a negative value!"
        )),
        "faceland:icons/enchant_ready"
    );
  }

  private void updateAwakenDisplay() {
    updateConfirmDisplay(
        PaletteUtil.color("|crimson|Awaken Item"),
        PaletteUtil.color(List.of(
            "",
            "|lgray|This will add a stat to a",
            "|lgray|non-unique item!"
        )),
        "faceland:icons/enchant_ready"
    );
  }

  private void updateFlipDisplay() {
    updateConfirmDisplay(
        PaletteUtil.color("|pink|Flip Item"),
        PaletteUtil.color(List.of(
            "",
            "|lgray|This will flip the rolls",
            "|lgray|on randomized stats!"
        )),
        "faceland:icons/enchant_ready"
    );
  }

  private void updateTrashDisplay() {
    updateConfirmDisplay(
        PaletteUtil.color("|brown|Worsen Item"),
        PaletteUtil.color(List.of(
            "",
            "|lgray|This will lower the randomized",
            "|lgray|rolls on your item??"
        )),
        "faceland:icons/enchant_ready"
    );
  }

  private void updateWarpedDisplay() {
    EquipmentData data = PersistHelper.getEquipmentData(selectedEquipment);
    if (data == null) {
      updateConfirmDisplay(PaletteUtil.color("|red|Not Equipment"),
          PaletteUtil.color(List.of("",
              "|lgray|The selected item is not normal equipment"
          )), "faceland:icons/enchant_invalid"
      );
      return;
    }
    boolean isUnique = PersistHelper.isUnique(data, selectedEquipment);
    if (isUnique) {
      updateConfirmDisplay(PaletteUtil.color("|red|Invalid Equipment!"),
          PaletteUtil.color(List.of("",
              "|lgray|The tongs cannot be used on",
              "|lgray|unique items, as unique items",
              "|lgray|always have the same secondary",
              "|lgray|stat"
          )), "faceland:icons/enchant_invalid"
      );
      return;
    }
    if (data.getTier().getSecondaryStats().size() <= 1) {
      updateConfirmDisplay(PaletteUtil.color("|red|Invalid Equipment!"),
          PaletteUtil.color(List.of("",
              "|lgray|This equipment item only has",
              "|lgray|one possible option for its",
              "|lgray|secondary stat!"
          )), "faceland:icons/enchant_invalid"
      );
      return;
    }
    var secondaryStats = new ArrayList<>(data.getTier().getSecondaryStats());
    for (var ss : data.getCoreStats()) {
      secondaryStats.removeIf(s -> s.getStat() == ss.getStat().getStat());
    }
    List<String> lore = new ArrayList<>(PaletteUtil.color(List.of(
        "",
        "|lgray|This will change the secondary",
        "|lgray|stat on normal equipment to one",
        "|lgray|of the other possible options!",
        "",
        "|white|Possible Options:"
    )));
    for (var ss : secondaryStats) {
      lore.add(FaceColor.LIGHT_GRAY + "- " + ss.getStat().getName());
    }
    updateConfirmDisplay(
        PaletteUtil.color("|teal|Warp Item"),
        lore,
        "faceland:icons/enchant_ready"
    );
  }

  private void updateRarityUpDisplay() {
    updateConfirmDisplay(
        PaletteUtil.color("|rainbow|Paint Item"),
        PaletteUtil.color(List.of(
            "",
            "|lgray|This increases the rarity of",
            "|lgray|a non-unique item!"
        )), "faceland:icons/enchant_ready"
    );
  }

  private void updatePolishDisplay() {
    updateConfirmDisplay(
        PaletteUtil.color("|rainbow|Perfect An Item's Stats!"),
        PaletteUtil.color(List.of(
            "",
            "|lgray|This will improve stats in the",
            "|lgray|random section! It improves the",
            "|lgray|stat rolls, and brings them closer",
            "|lgray|to a |rainbow|perfect roll|lgray|!",
            "",
            "|lime|Note: The improvement is a random",
            "|lime|percentage between the current",
            "|lime|value and the maximum value, so",
            "|lime|this is more effective on items",
            "|lime|with lower rolls!"
        )), "faceland:icons/enchant_ready"
    );
  }

  private void updateNormalDisplay() {
    updateConfirmDisplay(
        PaletteUtil.color("|green|Normalize Item"),
        PaletteUtil.color(List.of(
            "",
            "|lgray|Makes an item more normal by",
            "|lgray|making all random rolls exactly",
            "|lgray|50% between min/max!"
        )), "faceland:icons/enchant_ready"
    );
  }

  private void updateScrollDisplay(UpgradeScroll scroll, Player player, List<String> lore) {
    if (!MaterialUtil.isUpgradePossible(selectedEquipment)) {
      updateConfirmDisplay(invalidUpgrade, invalidUpgradeLore, "faceland:icons/enchant_invalid");
      return;
    }
    if (!MaterialUtil.canBeUpgraded(selectedUpgradeItem, selectedEquipment)) {
      updateConfirmDisplay(
          invalidUpgrade,
          PaletteUtil.color(List.of(
              "|lgray|This scroll can only upgrade",
              "|lgray|items up to |cyan|+" + scroll.getMaxLevel()
          )),
          "faceland:icons/enchant_invalid"
      );
    }

    EquipmentData data = PersistHelper.getEquipmentData(selectedEquipment);

    if (data == null || data.getTier() == null) {
      updateConfirmDisplay(invalidUpgrade, invalidUpgradeLore, "faceland:icons/enchant_invalid");
      return;
    }

    if (data.getTier().getUpgradeScale() > 15) {
      updateConfirmDisplay(invalidUpgrade, invalidUpgradeLore, "faceland:icons/enchant_invalid");
      return;
    }

    int itemPlus = data.getUpgradeLevel() * data.getTier().getUpgradeScale();

    if (!MaterialUtil.meetsUpgradeRange(scroll, itemPlus)) {
      updateConfirmDisplay(invalidUpgrade, badScrollRangeLore, "faceland:icons/enchant_invalid");
      return;
    }

    int newPlus = itemPlus + data.getTier().getUpgradeScale();

    Champion champion = plugin.getStrifePlugin().getChampionManager().getChampion(player);
    double successChance = Math.min(100, 100 * MaterialUtil.getSuccessChance(
        champion,
        newPlus,
        selectedUpgradeItem,
        scroll)
    );
    double maxDura = ((Damageable) selectedEquipment.getItemMeta()).getMaxDamage();
    double damage;
    if (maxDura <= 1) {
      maxDura = 100;
      damage = 0;
    } else {
      damage = ((Damageable) selectedEquipment.getItemMeta()).getDamage();
    }
    int itemLevel = MaterialUtil.getLevelRequirement(selectedEquipment);
    double maxPercent = MaterialUtil.getMaxFailureDamagePercent(scroll, itemLevel, newPlus);
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
  }

  private void updateRefillDisplay(List<String> lore) {
    if (!PersistHelper.isEnchanted(selectedEquipment)) {
      confirmIcon.setDisplayName(itemNotEnchanted);
      lore.addAll(itemNotEnchantedLore);
      ItemUtils.setItemModel(confirmIcon.getIcon(), "faceland:icons/enchant_invalid");
      TextUtils.setLore(confirmIcon.getIcon(), lore);
      return;
    }
    if (PersistHelper.isEnhanced(selectedEquipment)) {
      confirmIcon.setDisplayName(cannotRefill);
      lore.addAll(arcaneEnchanted);
      ItemUtils.setItemModel(confirmIcon.getIcon(), "faceland:icons/enchant_invalid");
      TextUtils.setLore(confirmIcon.getIcon(), lore);
      return;
    }
    if (PersistHelper.getMissingEnchantPower(selectedEquipment) < 1) {
      confirmIcon.setDisplayName(cannotRefill);
      lore.addAll(fullEnchantment);
      ItemUtils.setItemModel(confirmIcon.getIcon(), "faceland:icons/enchant_invalid");
      TextUtils.setLore(confirmIcon.getIcon(), lore);
      return;
    }
    confirmIcon.setDisplayName(validRefill);
    ItemUtils.setItemModel(confirmIcon.getIcon(), "faceland:icons/enchant_ready");
    lore.addAll(validRefillLore);
  }

  private void updateSocketDisplay(List<String> lore) {
    SocketGem socketGem = SocketGem.get(selectedUpgradeItem);
    if (socketGem != null) {
      if (plugin.getSocketGemManager().getOpenSocketIndex(selectedEquipment) == -1) {
        updateConfirmDisplay(invalidSocket, noSocketSlotsLore, "faceland:icons/enchant_invalid");
        return;
      }
      if (!MaterialUtil.isInGroup(selectedEquipment, socketGem.getItemGroup())) {
        updateConfirmDisplay(invalidSocket, invalidSocketTypeLore, "faceland:icons/enchant_invalid");
        return;
      }
      updateConfirmDisplay(validSocket, validSocketLore, "faceland:icons/enchant_ready");
      return;
    }
    updateConfirmDisplay(invalidSocket, invalidSocketTypeLore, "faceland:icons/enchant_ready");
  }

  boolean doUpgrade(Player player) {
    if (selectedEquipment == null || selectedEquipment.getType() == Material.AIR ||
        selectedEquipment.getAmount() > 1) {
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return false;
    }
    if (selectedUpgradeItem == null || selectedUpgradeItem.getType() == Material.AIR ||
        selectedUpgradeItem.getAmount() < 1) {
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return false;
    }

    switch (PersistHelper.getItemType(selectedUpgradeItem)) {
      case SocketGem.TYPE_KEY -> {
        plugin.getSocketGemManager().applySocketGem(player, selectedUpgradeItem, selectedEquipment);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case EnchantmentTome.TYPE_KEY -> {
        EnchantUtil.enchantItem(player, selectedUpgradeItem, selectedEquipment);
        setSelectedUpgradeItem(player, new ItemStack(Material.AIR));
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case SocketExtender.TYPE_KEY -> {
        MaterialUtil.extendItem(player, selectedEquipment, selectedUpgradeItem);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case TinkerersGear.TYPE_KEY -> {
        MaterialUtil.doTinkerEffects(plugin, selectedUpgradeItem, selectedEquipment, player);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case OrbOfManyFaces.TYPE_KEY -> {
        MaterialUtil.doFaceOrbEffects(plugin, selectedUpgradeItem, selectedEquipment, player);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case PerfectSphere.TYPE_KEY -> {
        MaterialUtil.doPerfectSphereEffects(plugin, selectedUpgradeItem, selectedEquipment, player);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case DivineHammer.TYPE_KEY -> {
        MaterialUtil.doDivineHammerEffects(plugin, selectedUpgradeItem, selectedEquipment, player);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case UpgradeScroll.TYPE_KEY -> {
        MaterialUtil.upgradeItem(player, selectedUpgradeItem, selectedEquipment);
        setSelectedUpgradeItem(player, new ItemStack(Material.AIR));
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case ArcaneEnhancer.TYPE_KEY -> {
        EnchantUtil.enhanceEnchantment(player, selectedEquipment, selectedUpgradeItem);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case PurifyingScroll.TYPE_KEY -> {
        EnchantUtil.purifyItem(player, selectedEquipment, selectedUpgradeItem);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case ManaCrystal.TYPE_KEY -> {
        EnchantUtil.refillEnchantment(player, selectedEquipment, selectedUpgradeItem);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case AspectOfPerfection.TYPE_KEY -> {
        MaterialUtil.doPolishEffects(plugin, selectedUpgradeItem, selectedEquipment, player);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case DistortionCell.TYPE_KEY -> {
        MaterialUtil.doDistortEffects(plugin, selectedUpgradeItem, selectedEquipment, player);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case NormalItem.TYPE_KEY -> {
        MaterialUtil.doNormalEffects(plugin, selectedEquipment, selectedUpgradeItem, player);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case AwakeningStone.TYPE_KEY -> {
        MaterialUtil.doAwakeningEffects(plugin, selectedEquipment, selectedUpgradeItem, player);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case MissingStat.TYPE_KEY -> {
        MaterialUtil.doAddStat(plugin, selectedEquipment, selectedUpgradeItem, player);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case TooltipPaint.TYPE_KEY -> {
        MaterialUtil.doRarityIncrease(plugin, selectedEquipment, selectedUpgradeItem, player);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case OrbOfOscillation.TYPE_KEY -> {
        MaterialUtil.doFlipEffects(plugin, selectedEquipment, selectedUpgradeItem, player);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case TraceOfTrash.TYPE_KEY -> {
        MaterialUtil.doWorsenEffects(plugin, selectedEquipment, selectedUpgradeItem, player);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }
      case WarpedTongs.TYPE_KEY -> {
        MaterialUtil.doWarpEffects(plugin, selectedEquipment, selectedUpgradeItem, player);
        setSelectedUpgradeItem(player, selectedUpgradeItem);
        setSelectedEquipment(player, selectedEquipment);
        return true;
      }

      default -> {
        // do nothing
      }
    }
    setSelectedEquipment(player, selectedEquipment);
    return true;
  }

  ItemStack getBlankItem() {
    return blankItem;
  }

  public static int getEnhanceRequirement(SkillRank rank) {
    return switch (rank) {
      case NOVICE -> 35;
      case APPRENTICE -> 45;
      case JOURNEYMAN -> 65;
      case EXPERT -> 85;
      case MASTER -> 100;
    };
  }

  private void updateConfirmDisplay(String displayName, List<String> lore, String model) {
    confirmIcon.setDisplayName(displayName);
    ItemUtils.setItemModel(confirmIcon.getIcon(), model);
    TextUtils.setLore(confirmIcon.getIcon(), lore);
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
