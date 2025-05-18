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
package info.faceland.loot.utils;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static org.bukkit.ChatColor.stripColor;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.builders.ItemBuilder;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.StoredStat;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.items.LevelModItem;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.items.prefabs.ShardOfFailure;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.SkillRank;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound.Source;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.StringUtil;
import org.codehaus.plexus.util.StringUtils;

public final class MaterialUtil {

  private static String upgradeFailureMsg;
  private static String upgradeItemDestroyMsg;
  private static String upgradeItemDamageMsg;
  private static String upgradeSuccessMsg;

  public static final String TAG_COMMON = "\uD86D\uDFE6";
  public static final String TAG_UNCOMMON = "\uD86D\uDFE7";
  public static final String TAG_RARE = "\uD86D\uDFE8";
  public static final String TAG_EPIC = "\uD86D\uDFE9";
  public static final String TAG_UNIQUE = "\uD86D\uDFEA";

  private static final net.kyori.adventure.sound.Sound UPGRADE_ITEM = net.kyori.adventure.sound.Sound.sound(
      Key.key("minecraft:custom.sfx.item_upgrade"), Source.MASTER, 1f, 1f);

  public static void refreshConfig() {
    upgradeFailureMsg = LootPlugin.getInstance().getSettings()
        .getString("language.upgrade.failure", "");
    upgradeItemDestroyMsg = LootPlugin.getInstance().getSettings()
        .getString("language.upgrade.destroyed", "");
    upgradeItemDamageMsg = LootPlugin.getInstance().getSettings()
        .getString("language.upgrade.damaged", "");
    upgradeSuccessMsg = LootPlugin.getInstance().getSettings()
        .getString("language.upgrade.success", "");
  }

  public static double getSuccessChance(Champion champion, int targetPlus, UpgradeScroll scroll, int upgradeScale) {
    double success = scroll.getBaseSuccess();
    success -= scroll.getFlatDecay() * targetPlus;
    success *= 1 - (scroll.getPercentDecay() * targetPlus);
    success = Math.pow(success, scroll.getExponent());
    success *= switch (upgradeScale) {
      case 3 -> 0.45f;
      case 2 -> 0.79f;
      default -> 1.0f;
    };
    success += SkillRank.isRank(champion, LifeSkillType.ENCHANTING, SkillRank.EXPERT) ? 0.02 : 0;
    return success;
  }

  public static double getUpgradeFailureDamagePercent(UpgradeScroll scroll, int itemLevel, int itemPlus) {
    return LootPlugin.RNG.nextFloat() * getMaxFailureDamagePercent(scroll, itemLevel, itemPlus);
  }

  public static float getMaxFailureDamagePercent(UpgradeScroll scroll, float itemLevel, float itemPlus) {
    // scales penalty from 0 at lvl 15 to 2 at level 70
    float difficultyFromLevel = Math.max(0, Math.min(2, (itemLevel - 15) / 27.5f));
    float itemDifficulty = itemPlus + difficultyFromLevel;
    return (0.25f + itemDifficulty * 0.11f) * scroll.getItemDamageMultiplier();
  }

  public static void upgradeItem(Player player, ItemStack scrollStack, ItemStack stack) {
    if (!canBeUpgraded(scrollStack, stack)) {
      sendMessage(player, upgradeFailureMsg);
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
      return;
    }
    UpgradeScroll scroll = UpgradeScroll.get(scrollStack);

    EquipmentData data = PersistHelper.getEquipmentData(stack);
    int itemUpgradeLevel = data.getUpgradeLevel() * data.getTier().getUpgradeScale();
    int targetLevel = itemUpgradeLevel;
    targetLevel += data.getTier().getUpgradeScale();
    targetLevel = Math.min(targetLevel, 15);

    Champion champion = LootPlugin.getInstance().getStrifePlugin().getChampionManager().getChampion(player);
    double successChance = getSuccessChance(champion, targetLevel, scroll, data.getTier().getUpgradeScale());

    scrollStack.setAmount(scrollStack.getAmount() - 1);

    // SUCCESS!
    if (successChance >= LootPlugin.RNG.nextFloat()) {
      data.upgrade();

      double exp = 4f + (float) Math.pow(1.45, targetLevel);
      LootPlugin.getInstance().getStrifePlugin().getSkillExperienceManager()
          .addExperience(player, LifeSkillType.ENCHANTING, exp, false, false);

      sendMessage(player, upgradeSuccessMsg);
      Audience audience = Audience.audience(player);
      audience.playSound(UPGRADE_ITEM);

      PersistHelper.setEquipmentData(stack, data);
      return;
    }

    // DAMAGED
    int itemLevel = MaterialUtil.getLevelRequirement(stack);
    double damagePercentage = getUpgradeFailureDamagePercent(scroll, itemLevel, targetLevel);
    int maxDamage = ((Damageable) stack.getItemMeta()).getMaxDamage();
    int currentDamage = ((Damageable) stack.getItemMeta()).getDamage();
    double currentDamagePercentage = ((double) currentDamage / maxDamage);
    int damage;

    double totalPercentage = damagePercentage + currentDamagePercentage;
    double rawDamage = Math.floor(totalPercentage * (double) maxDamage);
    // TODO: reimpl
    /*
    double repairMod = 100;
    rawDamage /= repairMod / 100;
    */
    damage = (int) rawDamage;

    if (damage >= maxDamage) {
      if (SkillRank.isRank(champion, LifeSkillType.ENCHANTING, SkillRank.MASTER) && LootPlugin.RNG.nextFloat() < 0.1) {
        damage = (short) (maxDamage - 1);
        player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1.5F);
        sendMessage(player, upgradeItemDestroyMsg);
        sendMessage(player, FaceColor.YELLOW + FaceColor.ITALIC.s() + "Except not rlly???");
      } else {
        player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
        sendMessage(player, upgradeItemDestroyMsg);
        stack.setAmount(0);
        return;
      }
    }
    int finalDamage = damage;
    stack.editMeta(m -> ((Damageable) m).setDamage(finalDamage));
    sendMessage(player, upgradeItemDamageMsg);
  }

  private static int rollShards(float itemLevel, float upgradeLevel, float itemRarity) {
    float newShards = upgradeLevel / 3.5f - 0.4f;
    newShards *= 1 + itemRarity / 4f;
    newShards *= 1 + itemLevel / 70f;
    return (int) Math.max(0, newShards);
  }

  public static boolean meetsUpgradeRange(UpgradeScroll type, int itemPlus) {
    return itemPlus >= type.getMinLevel() && itemPlus <= type.getMaxLevel();
  }

  public static boolean isUpgradePossible(ItemStack stack) {
    return isEquipmentItem(stack) && ModifyEquipmentUtil.canUpgrade(stack);
  }

  public static boolean isEquipmentItem(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    return data != null;
  }

  public static boolean canBeUpgraded(ItemStack scrollStack, ItemStack stack) {
    if (!isUpgradePossible(stack)) {
      return false;
    }
    UpgradeScroll scroll = UpgradeScroll.get(scrollStack);
    if (scroll == null) {
      return false;
    }
    return meetsUpgradeRange(scroll, PersistHelper.getItemPlus(stack));
  }

  public static boolean isInGroup(ItemStack stack, ItemGroup group) {
    if (stack == null) {
      return false;
    }
    if (group == null) {
      return true;
    }
    return group.matches(stack);
  }

  public static boolean isInGroup(EquipmentData data, ItemGroup group) {
    if (data == null) {
      return false;
    }
    if (group == null) {
      return true;
    }
    return group.matches(data);
  }


  public static int indexOfSocket(List<String> lore) {
    List<String> strippedLore = InventoryUtil.stripColor(lore);
    return strippedLore.indexOf(ItemBuilder.SOCKET_S);
  }

  public static int indexOfExtend(List<String> lore) {
    List<String> strippedLore = InventoryUtil.stripColor(lore);
    return strippedLore.indexOf(ItemBuilder.EXTEND_S);
  }

  public static boolean extendItem(Player player, ItemStack equipmentStack, ItemStack extenderStack) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentStack);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Not an equipment item!");
      return false;
    }
    if (data.getEmptyExtendSlots() < 1) {
      PaletteUtil.sendMessage(player, "|yellow|Item has no extend slots!");
      return false;
    }
    boolean success = data.extend();
    if (!success) {
      PaletteUtil.sendMessage(player, "|yellow|Failed for some reason?!");
      return false;
    }
    extenderStack.setAmount(extenderStack.getAmount() - 1);
    PersistHelper.setEquipmentData(equipmentStack, data);
    return true;
  }

  public static float getSocketChanceBonus(SkillRank skillRank) {
    if (SkillRank.isRank(skillRank, SkillRank.EXPERT)) {
      return 0.2f;
    }
    if (SkillRank.isRank(skillRank, SkillRank.APPRENTICE)) {
      return 0.1f;
    }
    return 0;
  }

  public static int getLevelRequirement(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data == null) {
      return 0;
    }
    //return data.getCustomLevel() + data.getLevelMod();
    return data.getCustomLevel();
  }

  public static boolean updateItem(ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      return false;
    }
    ItemRenderer.renderItem(stack, true);
    return true;
  }

  public static void doTinkerEffects(LootPlugin plugin, ItemStack tinkerGear, ItemStack targetItem, Player player) {
    EquipmentData equipmentData = PersistHelper.getEquipmentData(targetItem);
    if (equipmentData == null) {
      sendMessage(player, TextUtils.color(plugin.getSettings().getString(
          "language.tinker.only-one-crafted-stat", "Only one stat can be tinkered!")));
      return;
    }
    boolean canExtract = equipmentData.canExtractStat(false);
    if (!canExtract) {
      sendMessage(player, TextUtils.color(plugin.getSettings().getString(
          "language.tinker.only-one-crafted-stat", "Only one stat can be tinkered!")));
      return;
    }
    var stats = equipmentData.getExtractableStats();
    if (stats.isEmpty()) {
      sendMessage(player, TextUtils.color(plugin.getSettings().getString(
          "language.tinker.no-valid-stats", "No valid stats to be tinkered!")));
      return;
    }

    boolean extracted = EssenceUtil.extract(player, targetItem, false, false, true);

    player.playSound(player.getEyeLocation(), Sound.BLOCK_PISTON_EXTEND, 1F, 1.5F);
    plugin.getStrifePlugin().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.CRAFTING, 200, false, false);
    tinkerGear.setAmount(tinkerGear.getAmount() - 1);
  }

  public static void doFaceOrbEffects(LootPlugin plugin, ItemStack rerollOrb, ItemStack equipmentItem, Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment can be rerolled!");
      return;
    }
    data.reroll(0.12, 2.45);
    PersistHelper.setEquipmentData(equipmentItem, data);
    player.playSound(player.getEyeLocation(), Sound.ENTITY_EVOKER_PREPARE_WOLOLO, 1F, 1F);
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 200, false, false);
    rerollOrb.setAmount(rerollOrb.getAmount() - 1);
  }

  public static void doPerfectSphereEffects(LootPlugin plugin, ItemStack rerollOrb, ItemStack equipmentItem,
      Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment can be rerolled!");
      return;
    }
    data.reroll(0.5, 2.35);
    PersistHelper.setEquipmentData(equipmentItem, data);
    player.playSound(player.getEyeLocation(), Sound.ENTITY_EVOKER_PREPARE_WOLOLO, 1F, 1F);
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 200, false, false);
    rerollOrb.setAmount(rerollOrb.getAmount() - 1);
  }

  public static void doDivineHammerEffects(LootPlugin plugin, ItemStack rerollOrb, ItemStack equipmentItem,
      Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment can be rerolled!");
      return;
    }
    data.reroll(0.2, 2.55);
    PersistHelper.setEquipmentData(equipmentItem, data);
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 200, false, false);
    if (LootPlugin.RNG.nextFloat() > 0.8) {
      player.playSound(player.getEyeLocation(), Sound.BLOCK_ANVIL_BREAK, 1F, 1F);
      rerollOrb.setAmount(rerollOrb.getAmount() - 1);
    } else {
      player.playSound(player.getEyeLocation(), Sound.BLOCK_ANVIL_HIT, 1F, 1F);
    }
  }

  public static void doWarpEffects(LootPlugin plugin, ItemStack equipmentItem, ItemStack tongs, Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment can be tong'd!");
      return;
    }
    boolean isUnique = PersistHelper.isUnique(data, equipmentItem);
    if (isUnique) {
      PaletteUtil.sendMessage(player, "|yellow|This upgrade cannot be used on uniques!");
      return;
    }
    if (data.getTier().getSecondaryStats().size() <= 1) {
      PaletteUtil.sendMessage(player, "|yellow|This equipment has no other secondary stat options!");
      return;
    }
    var secondaryStats = new ArrayList<>(data.getTier().getSecondaryStats());
    for (var ss : data.getCoreStats()) {
      secondaryStats.removeIf(s -> s.getStat() == ss.getStat().getStat());
    }
    Collections.shuffle(secondaryStats);
    StoredStat newSecondary = new StoredStat(secondaryStats.getFirst().getId(), 1);
    data.getCoreStats().set(1, newSecondary);

    PersistHelper.setEquipmentData(equipmentItem, data);
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 200, false, false);
    if (LootPlugin.RNG.nextFloat() > 0.35) {
      player.playSound(player.getEyeLocation(), Sound.BLOCK_CREAKING_HEART_FALL, 1F, 1F);
      tongs.setAmount(tongs.getAmount() - 1);
    } else {
      player.playSound(player.getEyeLocation(), Sound.BLOCK_CREAKING_HEART_FALL, 1F, 1F);
    }
  }

  public static void doPolishEffects(LootPlugin plugin, ItemStack polishItem, ItemStack equipmentItem, Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment can be improved!");
      return;
    }
    data.polish(0.1, 2);
    PersistHelper.setEquipmentData(equipmentItem, data);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_GRINDSTONE_USE, 1F, 2F);
    player.playSound(player.getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 1F, 0.5F);
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 200, false, false);
    polishItem.setAmount(polishItem.getAmount() - 1);
  }

  public static void doWorsenEffects(LootPlugin plugin, ItemStack equipmentItem, ItemStack shitItem, Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment can be worsened!");
      return;
    }
    data.shitify();
    PersistHelper.setEquipmentData(equipmentItem, data);
    player.playSound(player.getEyeLocation(), Sound.ENTITY_LLAMA_SPIT, 1F, 1F);
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 200, false, false);
    shitItem.setAmount(shitItem.getAmount() - 1);
  }

  public static void doFlipEffects(LootPlugin plugin, ItemStack equipmentItem, ItemStack flipItem, Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment can have stats flipped!");
      return;
    }
    data.invertRandomRolls();
    PersistHelper.setEquipmentData(equipmentItem, data);
    player.playSound(player.getEyeLocation(), Sound.ITEM_BUNDLE_DROP_CONTENTS, 1F, 1F);
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 700, false, false);
    flipItem.setAmount(flipItem.getAmount() - 1);
  }

  public static void doNormalEffects(LootPlugin plugin, ItemStack equipmentItem, ItemStack rerollOrb, Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment can be normalized!");
      return;
    }
    data.undistort();
    data.forceRoll(0.5);
    PersistHelper.setEquipmentData(equipmentItem, data);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1F, 1F);
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 200, false, false);
    rerollOrb.setAmount(rerollOrb.getAmount() - 1);
  }

  public static void doAwakeningEffects(LootPlugin plugin, ItemStack equipmentItem, ItemStack rarityUpgrade,
      Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment items can be awakened!");
      return;
    }
    String id = PersistHelper.getItemId(equipmentItem);
    if (StringUtils.isNotBlank(id)) {
      PaletteUtil.sendMessage(player, "|yellow|This item cannot be used on uniques!");
      return;
    }
    if (!data.canAddBonusStat(1)) {
      PaletteUtil.sendMessage(player, "|yellow|This equipment already has the max number of stats!");
      return;
    }
    data.addBonusStat();
    PersistHelper.setEquipmentData(equipmentItem, data);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1F, 1F);
    plugin.getStrifePlugin().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.ENCHANTING, 200, false, false);
    rarityUpgrade.setAmount(rarityUpgrade.getAmount() - 1);
  }

  public static void doAddStat(LootPlugin plugin, ItemStack equipmentItem, ItemStack rarityUpgrade, Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment items have a stat added!");
      return;
    }
    String id = PersistHelper.getItemId(equipmentItem);
    if (StringUtils.isNotBlank(id)) {
      PaletteUtil.sendMessage(player, "|yellow|This item cannot be used on uniques!");
      return;
    }
    if (!data.canAddBonusStat(0)) {
      PaletteUtil.sendMessage(player, "|yellow|This equipment already has the max number of stats!");
      return;
    }
    data.addBonusStat();
    PersistHelper.setEquipmentData(equipmentItem, data);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1F, 1F);
    plugin.getStrifePlugin().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.ENCHANTING, 200, false, false);
    rarityUpgrade.setAmount(rarityUpgrade.getAmount() - 1);
  }

  public static void doRarityIncrease(LootPlugin plugin, ItemStack equipmentItem, ItemStack rarityUpgrade,
      Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment can have their rarity increased!");
      return;
    }
    boolean isUnique = PersistHelper.isUnique(data, equipmentItem);
    if (isUnique) {
      PaletteUtil.sendMessage(player, "|yellow|This upgrade cannot be used on uniques!");
      return;
    }
    ItemRarity rarity = data.getItemRarity();
    switch (rarity.getId()) {
      case "COMMON" -> data.setRarity("UNCOMMON");
      case "UNCOMMON" -> data.setRarity("RARE");
      case "RARE" -> data.setRarity("EPIC");
      case "EPIC" -> {
        PaletteUtil.sendMessage(player, "|yellow|This item is as rare as it gets!!");
        return;
      }
    }
    if (data.getBonusStats().size() < data.getItemRarity().getMinimumBonusStats()) {
      // TODO: could maybe be more??
      data.addBonusStat();
    }
    if (data.getSockets().size() + data.getEmptyGemSlots() + data.getEmptyExtendSlots()
        < data.getTier().getMinSockets(rarity.getId())) {
      data.setEmptyExtendSlots(data.getEmptyExtendSlots() + 1);
    }
    PersistHelper.setEquipmentData(equipmentItem, data);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1F, 1F);
    plugin.getStrifePlugin().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.ENCHANTING, 200, false, false);
    rarityUpgrade.setAmount(rarityUpgrade.getAmount() - 1);
  }

  public static void doDistortEffects(LootPlugin plugin, ItemStack distortItem, ItemStack equipmentItem,
      Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment can be distorted!");
      return;
    }
    if (data.isDistorted()) {
      data.undistort();
    }
    data.distort();
    PersistHelper.setEquipmentData(equipmentItem, data);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_SCULK_VEIN_HIT, 1F, 0.5F);
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 200, false, false);
    distortItem.setAmount(distortItem.getAmount() - 1);
  }

  public static ItemRarity getRarity(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data == null) {
      return null;
    }
    return data.getItemRarity();
  }

  public static int getDigit(String input) {
    input = ChatColor.stripColor(input);
    String num = input.replaceAll("[^\\d-]", "");
    return Integer.parseInt(num);
  }

  private static double rollMult(boolean lucky) {
    return rollMult(lucky, 2);
  }

  private static double rollMult(boolean lucky, double exponent) {
    return lucky ? LootPlugin.RNG.nextFloat() : Math.pow(LootPlugin.RNG.nextFloat(), exponent);
  }

  public static int getCustomData(ItemStack stack) {
    if (stack.getItemMeta() == null || !stack.getItemMeta().hasCustomModelData()) {
      return -1;
    }
    return stack.getItemMeta().getCustomModelData();
  }
}
