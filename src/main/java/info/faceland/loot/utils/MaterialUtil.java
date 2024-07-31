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
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.items.prefabs.ShardOfFailure;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
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

public final class MaterialUtil {

  public static final Pattern ONLY_LETTERS = Pattern.compile("[^A-za-z]");
  private static final Pattern DIGITS = Pattern.compile("[^0-9.-]");

  private static String upgradeFailureMsg;
  private static String upgradeItemDestroyMsg;
  private static String upgradeItemDestroyBroadcast;
  private static String upgradeItemDamageMsg;
  private static String upgradeItemNoDamageMsg;
  private static String upgradeSuccessMsg;
  private static String upgradeSuccessBroadcast;

  public static final String FAILURE_BONUS = ChatColor.RED + "Failure Bonus";

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
    upgradeItemDestroyBroadcast = LootPlugin.getInstance().getSettings()
        .getString("language.broadcast.destroyed-item");
    upgradeItemDamageMsg = LootPlugin.getInstance().getSettings()
        .getString("language.upgrade.damaged", "");
    upgradeItemNoDamageMsg = LootPlugin.getInstance().getSettings()
        .getString("language.upgrade.not-damaged", "");
    upgradeSuccessMsg = LootPlugin.getInstance().getSettings()
        .getString("language.upgrade.success", "");
    upgradeSuccessBroadcast = LootPlugin.getInstance().getSettings()
        .getString("language.broadcast.upgraded-item");
  }

  public static double getSuccessChance(Champion champion, int targetPlus, ItemStack scrollStack, UpgradeScroll scroll) {
    double success = scroll.getBaseSuccess();
    success -= scroll.getFlatDecay() * targetPlus;
    success *= 1 - (scroll.getPercentDecay() * targetPlus);
    success = Math.pow(success, scroll.getExponent());
    success += SkillRank.isRank(champion, LifeSkillType.ENCHANTING, SkillRank.JOURNEYMAN) ? 0.03 : 0;
    if (success <= 1) {
      success += (1 - success) * getFailureMod(getFailureBonus(scrollStack));
    }
    return success;
  }

  public static int getFailureBonus(ItemStack scrollStack) {
    for (String s : TextUtils.getLore(scrollStack)) {
      if (s.startsWith(FAILURE_BONUS)) {
        return MaterialUtil.getDigit(ChatColor.stripColor(s));
      }
    }
    return 0;
  }

  public static double getFailureMod(double failBonus) {
    return 1 - (300D / (300D + failBonus));
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
    String strippedName = stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    UpgradeScroll scroll = UpgradeScroll.get(scrollStack);

    EquipmentData data = PersistHelper.getEquipmentData(stack);
    int itemUpgradeLevel = data.getUpgradeLevel() * data.getTier().getUpgradeScale();
    int targetLevel = itemUpgradeLevel;
    int scrollShards = getFailureBonus(scrollStack);
    if (stack.getType().getMaxDurability() < 5) {
      targetLevel += 3;
    } else {
      targetLevel += 1;
    }
    targetLevel = Math.min(targetLevel, 15);

    Champion champion = LootPlugin.getInstance().getStrifePlugin().getChampionManager().getChampion(player);
    double successChance = getSuccessChance(champion, targetLevel, scrollStack, scroll);

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
      if (targetLevel >= 10 && !strippedName.contains("Starter")) {
        InventoryUtil.sendToDiscord(player, stack, upgradeSuccessBroadcast);
      }
      return;
    }

    // DAMAGED
    int itemLevel = MaterialUtil.getLevelRequirement(stack);
    double damagePercentage = getUpgradeFailureDamagePercent(scroll, itemLevel, targetLevel);
    double currentDamagePercentage = ((double) stack.getDurability()) / stack.getType().getMaxDurability();
    short damage;

    // Offhand Upgrade
    if (stack.getType().getMaxDurability() <= 1) {
      if (damagePercentage < 1) {
        sendMessage(player, upgradeItemNoDamageMsg);
        int newShards = (int) (rollShards(itemLevel, itemUpgradeLevel, (float) MaterialUtil.getRarity(stack).getPower()) / 2f);
        distributeShards(player, scrollShards + newShards);
        return;
      }
      damage = Short.MAX_VALUE;
    } else {
      double totalPercentage = damagePercentage + currentDamagePercentage;
      double rawDamage = Math.floor(totalPercentage * (double) stack.getType().getMaxDurability());
      double repairMod = 100;
      rawDamage /= repairMod / 100;
      damage = (short) rawDamage;
    }

    if (damage >= stack.getType().getMaxDurability()) {
      if (SkillRank.isRank(champion, LifeSkillType.ENCHANTING, SkillRank.MASTER) && LootPlugin.RNG.nextFloat() < 0.1) {
        damage = (short) (stack.getType().getMaxDurability() - 1);
        player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1.5F);
        sendMessage(player, upgradeItemDestroyMsg);
        sendMessage(player, FaceColor.YELLOW + FaceColor.ITALIC.s() + "Except not rlly???");
      } else {
        player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
        sendMessage(player, upgradeItemDestroyMsg);
        if (itemUpgradeLevel >= 10 && !strippedName.contains("Starter")) {
          InventoryUtil.sendToDiscord(player, stack.clone(), upgradeItemDestroyBroadcast);
        }
        int newShards = rollShards(itemLevel, itemUpgradeLevel, (float) MaterialUtil.getRarity(stack).getPower());
        stack.setAmount(0);
        distributeShards(player, scrollShards + newShards);
        return;
      }
    }
    int newShards = rollShards(itemLevel, itemUpgradeLevel, (float) MaterialUtil.getRarity(stack).getPower());
    distributeShards(player, scrollShards + newShards);
    stack.setDurability(damage);
    sendMessage(player, upgradeItemDamageMsg);
  }

  private static int rollShards(float itemLevel, float upgradeLevel, float itemRarity) {
    float newShards = upgradeLevel / 3.5f - 0.4f;
    newShards *= 1 + itemRarity / 4f;
    newShards *= 1 + itemLevel / 70f;
    return (int) Math.max(0, newShards);
  }

  private static void distributeShards(Player player, int amount) {
    return;
    /*
    if (amount > 0) {
      ItemStack shard = ShardOfFailure.build(player.getName());
      while (amount > 0) {
        ItemStack loopShard = shard.clone();
        loopShard.setAmount(Math.min(amount, 64));
        if (player.getInventory().firstEmpty() != -1) {
          player.getInventory().addItem(loopShard);
        } else {
          Item item = player.getWorld().dropItem(player.getLocation(), loopShard);
          item.setOwner(player.getUniqueId());
        }
        amount -= 64;
      }
    }
     */
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

  public static float getExtendChance(SkillRank rank) {
    return switch (rank) {
      case NOVICE -> 0;
      case APPRENTICE -> 0.25f;
      case JOURNEYMAN, EXPERT -> 0.5f;
      case MASTER -> 1.0f;
    };
  }

  public static int getSockets(float rarity, SkillRank skillRank) {
    int minSockets = getMinSockets(skillRank);
    int maxSockets = getMaxSockets(rarity);
    if (minSockets >= maxSockets) {
      return minSockets;
    }
    int result = minSockets;
    while (result < maxSockets && LootPlugin.RNG.nextFloat() > 0.5f) {
      result++;
    }
    return result;
  }

  public static int getMinSockets(SkillRank skillRank) {
    int amount = 0;
    if (SkillRank.isRank(skillRank, SkillRank.APPRENTICE)) {
      amount++;
    }
    if (SkillRank.isRank(skillRank, SkillRank.EXPERT)) {
      amount++;
    }
    return amount;
  }

  public static int getMaxSockets(float rarity) {
    return rarity > 1.1 ? 2 : 1;
  }

  public static int getLevelRequirement(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    return data == null ? 0 : data.getLevel();
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
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player, LifeSkillType.CRAFTING, 200, false, false);
    tinkerGear.setAmount(tinkerGear.getAmount() - 1);
  }

  public static void doFaceOrbEffects(LootPlugin plugin, ItemStack rerollOrb, ItemStack equipmentItem, Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment can be rerolled!");
      return;
    }
    data.reroll(0.12);
    PersistHelper.setEquipmentData(equipmentItem, data);
    player.playSound(player.getEyeLocation(), Sound.ENTITY_EVOKER_PREPARE_WOLOLO, 1F, 1F);
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 200, false, false);
    rerollOrb.setAmount(rerollOrb.getAmount() - 1);
  }

  public static void doPerfectSphereEffects(LootPlugin plugin, ItemStack rerollOrb, ItemStack equipmentItem, Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment can be rerolled!");
      return;
    }
    data.reroll(0.5);
    PersistHelper.setEquipmentData(equipmentItem, data);
    player.playSound(player.getEyeLocation(), Sound.ENTITY_EVOKER_PREPARE_WOLOLO, 1F, 1F);
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 200, false, false);
    rerollOrb.setAmount(rerollOrb.getAmount() - 1);
  }

  public static void doDivineHammerEffects(LootPlugin plugin, ItemStack rerollOrb, ItemStack equipmentItem, Player player) {
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Only equipment can be rerolled!");
      return;
    }
    data.reroll(0.2);
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

  public static void doDistortEffects(LootPlugin plugin, ItemStack distortItem, ItemStack equipmentItem, Player player) {
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
