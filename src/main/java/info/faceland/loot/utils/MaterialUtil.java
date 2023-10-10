/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.utils;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static org.bukkit.ChatColor.stripColor;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.ToastUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.UnicodeUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang.WordUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.tuple.Pair;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.CustomItem;
import info.faceland.loot.data.ExistingSocketData;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.enchantments.EnchantmentTome;
import info.faceland.loot.events.LootEnchantEvent;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.items.ItemBuilder;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.ShardOfFailure;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.listeners.crafting.PreCraftListener;
import info.faceland.loot.menu.upgrade.EnchantMenu;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.tier.Tier;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import land.face.dinvy.entity.PlayerData;
import land.face.dinvy.events.EquipmentUpdateEvent;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.PlayerDataUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound.Source;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

public final class MaterialUtil {

  private static final double BONUS_ESS_MULT = 0.3;
  private static final double TOOL_QUALITY_ESS_MULT = 0.08;
  private static final double MIN_BONUS_ESS_MULT = 0.25;
  private static final double BASE_ESSENCE_MULT = 0.3;

  public static final Pattern ONLY_LETTERS = Pattern.compile("[^A-za-z]");
  private static final Pattern DIGITS = Pattern.compile("[^0-9.-]");

  private static String upgradeFailureMsg;
  private static String noPointsForHelmetMsg;
  private static String upgradeItemDestroyMsg;
  private static String upgradeItemDestroyBroadcast;
  private static String upgradeItemDamageMsg;
  private static String upgradeItemNoDamageMsg;
  private static String upgradeSuccessMsg;
  private static String upgradeSuccessBroadcast;
  private static String enchantFailureMsg;
  private static String enchantTagNeededMsg;
  private static String enchantPointlessMsg;
  private static String enchantSuccessMsg;
  private static String extendSuccessMsg;
  private static String extendFailMsg;
  private static boolean enchantmentsStack;

  public static final String FAILURE_BONUS = ChatColor.RED + "Failure Bonus";
  public static final String ENCHANTABLE_TAG = FaceColor.TRUE_WHITE + "傜";
  public static final String ENCHANTABLE_TAG_S = ChatColor.stripColor(ENCHANTABLE_TAG);

  private static final String enchantBarStart = "τ\uF801";
  private static final String arcaneBarStart = "ψ\uF801";
  private static final String enchantBarEmpty = "φ\uF801";
  private static final String enchantBarFull = "υ\uF801";
  private static final String arcaneBarFull = "ω\uF801";
  private static final String enchantBarEnd = "χ";

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
    noPointsForHelmetMsg = LootPlugin.getInstance().getSettings()
        .getString("language.generic.no-points-for-head-merge", "");
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
    enchantFailureMsg = LootPlugin.getInstance().getSettings()
        .getString("language.enchant.failure", "");
    enchantTagNeededMsg = LootPlugin.getInstance().getSettings()
        .getString("language.enchant.needs-enchantable", "");
    enchantPointlessMsg = LootPlugin.getInstance().getSettings()
        .getString("language.enchant.pointless", "");
    enchantSuccessMsg = LootPlugin.getInstance().getSettings()
        .getString("language.enchant.success", "");
    enchantmentsStack = LootPlugin.getInstance().getSettings()
        .getBoolean("config.enchantments-stack", true);
    extendSuccessMsg = LootPlugin.getInstance().getSettings()
        .getString("language.extend.success", "");
    extendFailMsg = LootPlugin.getInstance().getSettings()
        .getString("language.extend.fail", "");
  }

  public static double getSuccessChance(Player player, int targetPlus, ItemStack scrollStack,
      UpgradeScroll scroll) {
    double success = scroll.getBaseSuccess();
    success -= scroll.getFlatDecay() * targetPlus;
    success *= 1 - (scroll.getPercentDecay() * targetPlus);
    success = Math.pow(success, scroll.getExponent());
    success += PlayerDataUtil.getSkillLevels(player, LifeSkillType.ENCHANTING, true).getLevelWithBonus() * 0.001;
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

  public static double getUpgradeFailureDamagePercent(UpgradeScroll scroll, int itemPlus) {
    return LootPlugin.RNG.nextFloat() * getMaxFailureDamagePercent(scroll, itemPlus);
  }

  public static double getMaxFailureDamagePercent(UpgradeScroll scroll, int itemPlus) {
    return (0.25 + itemPlus * 0.11) * scroll.getItemDamageMultiplier();
  }

  public static void extendItem(Player player, ItemStack stack, ItemStack extender) {
    List<String> lore = new ArrayList<>(TextUtils.getLore(stack));
    int index = indexOfExtend(lore);
    if (index == -1) {
      sendMessage(player, extendFailMsg);
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
      return;
    }
    lore.set(index, ItemBuilder.SOCKET);
    TextUtils.setLore(stack, lore);

    sendMessage(player, extendSuccessMsg);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_PORTAL_TRAVEL, 1L, 2.0F);
    extender.setAmount(extender.getAmount() - 1);
  }

  public static void upgradeItem(Player player, ItemStack scrollStack, ItemStack stack) {
    if (!canBeUpgraded(scrollStack, stack)) {
      sendMessage(player, upgradeFailureMsg);
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
      return;
    }
    String strippedName = stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    UpgradeScroll scroll = LootPlugin.getInstance().getScrollManager().getScroll(scrollStack);

    int itemUpgradeLevel = MaterialUtil.getUpgradeLevel(strippedName);
    int targetLevel = itemUpgradeLevel;
    int shards = getFailureBonus(scrollStack);
    if (stack.getType().getMaxDurability() < 5) {
      targetLevel += 3;
    } else {
      targetLevel += 1;
    }
    targetLevel = Math.min(targetLevel, 15);

    double successChance = getSuccessChance(player, targetLevel, scrollStack, scroll);

    scrollStack.setAmount(scrollStack.getAmount() - 1);

    // SUCCESS!
    if (successChance >= LootPlugin.RNG.nextFloat()) {
      bumpItemPlus(stack, itemUpgradeLevel, 1, targetLevel - itemUpgradeLevel);

      double exp = 4f + (float) Math.pow(1.45, targetLevel);
      LootPlugin.getInstance().getStrifePlugin().getSkillExperienceManager()
          .addExperience(player, LifeSkillType.ENCHANTING, exp, false, false);

      sendMessage(player, upgradeSuccessMsg);
      Audience audience = Audience.audience(player);
      audience.playSound(UPGRADE_ITEM);

      if (targetLevel >= 10) {
        InventoryUtil.sendToDiscord(player, stack, upgradeSuccessBroadcast);
      }
      return;
    }

    // DAMAGED
    double damagePercentage = getUpgradeFailureDamagePercent(scroll, targetLevel);
    double currentDamagePercentage = ((double) stack.getDurability()) / stack.getType().getMaxDurability();
    short damage;

    // Offhand Upgrade
    if (stack.getType().getMaxDurability() <= 1) {
      if (damagePercentage < 1) {
        sendMessage(player, upgradeItemNoDamageMsg);
        if (itemUpgradeLevel > 5) {
          shards += itemUpgradeLevel / 4 + LootPlugin.RNG.nextInt(0, itemUpgradeLevel - 5);
        }
        distributeShards(player, shards);
        return;
      }
      damage = Short.MAX_VALUE;
    } else {
      double totalPercentage = damagePercentage + currentDamagePercentage;
      double rawDamage = Math.floor(totalPercentage * (double) stack.getType().getMaxDurability());
      double repairMod = 100;
      for (String loreLine : TextUtils.getLore(stack)) {
        if (!loreLine.endsWith(" Increased Durability")) {
          continue;
        }
        String strippedLore = ChatColor.stripColor(loreLine);
        repairMod += Double.parseDouble(DIGITS.matcher(strippedLore).replaceAll(""));
      }
      rawDamage /= repairMod / 100;
      damage = (short) rawDamage;
    }

    if (damage >= stack.getType().getMaxDurability()) {
      player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
      sendMessage(player, upgradeItemDestroyMsg);
      if (itemUpgradeLevel >= 10) {
        InventoryUtil.sendToDiscord(player, stack.clone(), upgradeItemDestroyBroadcast);
      }
      stack.setAmount(0);
      if (itemUpgradeLevel > 5) {
        shards += itemUpgradeLevel / 4 + LootPlugin.RNG.nextInt(0, itemUpgradeLevel - 5);
      }
      distributeShards(player, shards);
      return;
    }

    distributeShards(player, shards);
    stack.setDurability(damage);
    sendMessage(player, upgradeItemDamageMsg);
  }

  private static void distributeShards(Player player, int amount) {
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
  }

  public static void bumpItemPlus(ItemStack stack, int currentLevel, int amount) {
    bumpItemPlus(stack, currentLevel, amount, amount);
  }

  public static void bumpItemPlus(ItemStack stack, int currentLevel, int statAmount, int plusAmount) {
    String itemName = ItemStackExtensionsKt.getDisplayName(stack);
    if (StringUtils.isBlank(itemName)) {
      itemName = stack.getType().toString().replaceAll("_", " ");
      itemName = WordUtils.capitalizeFully(itemName);
    }
    int newLevel = Math.max(Math.min(currentLevel + plusAmount, 15), 0);
    if (currentLevel > 0) {
      itemName = itemName.replace("+" + currentLevel, "+" + newLevel);
    } else {
      String strippedName = ChatColor.stripColor(itemName);
      itemName = itemName.replace(strippedName, "+" + newLevel + " " + strippedName);
    }
    ItemStackExtensionsKt.setDisplayName(stack, itemName);
    if (newLevel >= 10 && stack.getEnchantments().isEmpty()) {
      stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
    }
    stack.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
    List<String> lore = TextUtils.getLore(stack);
    for (int i = 0; i < lore.size(); i++) {
      String s = lore.get(i);
      String ss = stripColor(s);
      if (!ss.startsWith("+")) {
        continue;
      }
      String attributeText = CharMatcher.digit().or(CharMatcher.is('-')).retainFrom(ss);
      int attributeValue = NumberUtils.toInt(attributeText);
      lore.set(i, s.replace("+" + attributeValue, "+" + (attributeValue + statAmount)));
      break;
    }
    TextUtils.setLore(stack, lore);
  }

  public static boolean meetsUpgradeRange(UpgradeScroll type, int itemPlus) {
    return itemPlus >= type.getMinLevel() && itemPlus <= type.getMaxLevel();
  }

  public static boolean isUpgradePossible(ItemStack stack) {
    if (isBannedUpgradeMaterial(stack)) {
      return false;
    }
    String targetName = stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    if (LootPlugin.getInstance().getSettings().getStringList("config.cannot-be-upgraded",
        new ArrayList<>()).contains(targetName)) {
      return false;
    }
    List<String> lore = TextUtils.getLore(stack);
    for (String s : lore) {
      if ("< Cannot be upgraded >".equalsIgnoreCase(stripColor(s))) {
        return false;
      }
    }
    return isEquipmentItem(stack);
  }

  public static boolean isEquipmentItem(ItemStack stack) {
    if (stack.getType() == Material.WHEAT_SEEDS || stack.getType() == Material.SHEARS) {
      return false;
    }
    if (stack.getType().getMaxDurability() > 5) {
      return true;
    }
    if (MaterialUtil.isEssence(stack)) {
      return false;
    }
    List<String> strip = InventoryUtil.stripColor(TextUtils.getLore(stack));
    for (String s : strip) {
      if (s.startsWith("+")) {
        return true;
      }
    }
    return false;
  }

  public static boolean canBeUpgraded(ItemStack scrollStack, ItemStack stack) {
    if (!isUpgradePossible(stack)) {
      return false;
    }
    UpgradeScroll scroll = LootPlugin.getInstance().getScrollManager().getScroll(scrollStack);
    if (scroll == null) {
      return false;
    }
    return meetsUpgradeRange(scroll, getUpgradeLevel(ItemStackExtensionsKt.getDisplayName(stack)));
  }

  public static boolean matchesGroups(ItemStack stack, List<ItemGroup> groups) {
    int modelData = ItemUtil.getCustomData(stack);
    Material material = stack.getType();
    for (ItemGroup ig : groups) {
      if (ig.isInverse()) {
        if (ig.getMaterials().contains(material)) {
          return false;
        }
        if (ig.getMinimumCustomData() != -1 && modelData >= ig.getMinimumCustomData()) {
          return false;
        }
        if (ig.getMaximumCustomData() != -1 && modelData <= ig.getMaximumCustomData()) {
          return false;
        }
      } else {
        if (!ig.getMaterials().contains(material)) {
          return false;
        }
        if (ig.getMinimumCustomData() != -1 && modelData < ig.getMinimumCustomData()) {
          return false;
        }
        if (ig.getMaximumCustomData() != -1 && modelData > ig.getMaximumCustomData()) {
          return false;
        }
      }
    }
    return true;
  }

  public static boolean hasEnchantmentTag(ItemStack stack) {
    List<String> lore = new ArrayList<>(TextUtils.getLore(stack));
    List<String> strippedLore = InventoryUtil.stripColor(lore);
    return strippedLore.contains(MaterialUtil.ENCHANTABLE_TAG_S);
  }

  public static String getSocketString(ItemStack stack) {
    List<String> lore = new ArrayList<>(TextUtils.getLore(stack));
    List<String> strippedLore = InventoryUtil.stripColor(lore);
    int sockets = 0;
    int extenders = 0;
    for (String s : strippedLore) {
      if (s.equals(ItemBuilder.SOCKET_S)) {
        sockets++;
      } else if (s.equals(ItemBuilder.EXTEND_S)) {
        extenders++;
      }
    }
    return "-" + sockets + "-" + extenders + "-";
  }

  public static void purifyItem(Player player, ItemStack item, ItemStack purifyScroll) {
    Pair<String, Integer> enchantBar = getEnchantBar(item);
    if (enchantBar == null) {
      MessageUtils.sendMessage(player, "&eYou can only remove enchantments from items that have an enchantment ya ding dong");
      return;
    }

    List<String> lore = TextUtils.getLore(item);
    lore.set(enchantBar.getRight(), MaterialUtil.ENCHANTABLE_TAG);
    lore.remove(enchantBar.getRight() - 1);
    TextUtils.setLore(item, lore);

    purifyScroll.setAmount(purifyScroll.getAmount() - 1);
    StrifePlugin.getInstance().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.ENCHANTING, 70, false, false);
    player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1, 1f);
  }

  public static void enhanceEnchantment(Player player, ItemStack item, ItemStack enhancer) {
    Pair<String, Integer> enchantBar = getEnchantBar(item);
    if (enchantBar == null || enchantBar.getLeft().contains(arcaneBarStart)) {
      MessageUtils.sendMessage(player, "&eThis item cannot be enhanced.");
      return;
    }

    int itemLevel = getItemLevel(item);
    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.ENCHANTING, true);
    double rawLevel = data.getLevel();

    if (EnchantMenu.getEnhanceRequirement(itemLevel) > rawLevel) {
      MessageUtils.sendMessage(player, "&eYour enchanting level is too low!");
      return;
    }

    List<String> lore = TextUtils.getLore(item);
    lore.set(enchantBar.getRight(),enchantBar.getLeft()
        .replace(enchantBarStart, arcaneBarStart).replaceAll(enchantBarFull, arcaneBarFull));
    String enchantmentStatString = ChatColor.stripColor(lore.get(enchantBar.getRight() - 1));

    int statValue = NumberUtils.toInt(CharMatcher.digit()
        .or(CharMatcher.is('-')).retainFrom(enchantmentStatString));

    itemLevel = Math.max(1, Math.min(100, itemLevel));
    double enchantingLevel = data.getLevelWithBonus();

    double enchantingBonus = Math.min(2.5, Math.max(1, enchantingLevel / itemLevel));
    float enhanceRoll = 0.1f + 0.2f * (float) Math.pow(LootPlugin.RNG.nextFloat(), 1.25);

    int newValue = statValue + (int) (statValue * enhanceRoll * enchantingBonus);
    newValue++;

    lore.set(enchantBar.getRight() - 1, FaceColor.BLUE + enchantmentStatString
            .replace(Integer.toString(statValue), Integer.toString(newValue)));
    TextUtils.setLore(item, lore);

    enhancer.setAmount(enhancer.getAmount() - 1);
    StrifePlugin.getInstance().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.ENCHANTING, 450, false, false);
    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 0.8f);
  }

  public static boolean canBeEnchanted(Player player, ItemStack tomeStack, ItemStack stack) {
    EnchantmentTome tome = getEnchantmentItem(tomeStack);
    if (tome == null) {
      return false;
    }
    if (!matchesGroups(stack, tome.getItemGroups())) {
      sendMessage(player, enchantFailureMsg);
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
      return false;
    }

    if (!hasEnchantmentTag(stack)) {
      sendMessage(player, enchantTagNeededMsg);
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
      return false;
    }
    return true;
  }

  public static int indexOfSocket(List<String> lore) {
    List<String> strippedLore = InventoryUtil.stripColor(lore);
    return strippedLore.indexOf(ItemBuilder.SOCKET_S);
  }

  public static int indexOfExtend(List<String> lore) {
    List<String> strippedLore = InventoryUtil.stripColor(lore);
    return strippedLore.indexOf(ItemBuilder.EXTEND_S);
  }

  public static int getMissingEnchantmentPower(ItemStack stack) {
    if (!isEnchanted(stack)) {
      return 0;
    }
    Pair<String, Integer> bar = getEnchantBar(stack);
    String stripped = ChatColor.stripColor(bar.getKey());
    if (stripped.startsWith("[|")) {
      return 1;
    } else if (stripped.startsWith(enchantBarStart)) {
      int current = StringUtils.countMatches(stripped, "υ");
      int max = current + StringUtils.countMatches(stripped, "φ");
      return max - current;
    } else if (stripped.startsWith(arcaneBarStart)) {
      int current = StringUtils.countMatches(stripped, "ω");
      int max = current +  StringUtils.countMatches(stripped, "φ");
      return max - current;
    } else {
      return 0;
    }
  }

  public static boolean isEnchanted(ItemStack stack) {
    return getEnchantBar(stack) != null;
  }

  public static boolean isArcaneEnchanted(ItemStack stack) {
    Pair<String, Integer> bar = getEnchantBar(stack);
    return bar != null && isArcaneEnchanted(bar.getKey());
  }

  public static boolean isArcaneEnchanted(String enchantBarString) {
    return enchantBarString.contains(arcaneBarStart);
  }

  public static void refillEnchantment(Player player, ItemStack equipment, ItemStack crystal) {
    if (!isEnchanted(equipment) || isArcaneEnchanted(equipment)) {
      return;
    }
    if (MaterialUtil.getMissingEnchantmentPower(equipment) < 1) {
      return;
    }
    Pair<String, Integer> enchantBar = getEnchantBar(equipment);
    double enchantLevel = PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.ENCHANTING);
    double itemLevel = MaterialUtil.getLevelRequirement(equipment);
    int addAmount = 2 + (int) (LootPlugin.RNG.nextFloat() *
        (2 + Math.max(0, (enchantLevel - itemLevel) * 0.2)));
    Pair<String, Integer> result = refillEnchantBar(enchantBar.getLeft(), addAmount);
    List<String> lore = TextUtils.getLore(equipment);
    lore.set(enchantBar.getRight(), result.getLeft());
    TextUtils.setLore(equipment, lore);

    StrifePlugin.getInstance().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 8f + 4 * addAmount, false, false);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1F, 1.2F);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1F, 1F);
    crystal.setAmount(crystal.getAmount() - 1);
  }

  public static Pair<String, Integer> refillEnchantBar(String string, int amount) {
    String stripped = ChatColor.stripColor(string);
    if (stripped.startsWith("[|")) {
      int current = StringUtils.countMatches(stripped, "|");
      if (string.contains("" + ChatColor.DARK_RED)) {
        return buildArcaneBar(current, current);
      } else {
        return buildEnchantmentBar(current, current);
      }
    } else if (stripped.startsWith(enchantBarStart)) {
      int current = StringUtils.countMatches(stripped, "υ");
      int max = current + StringUtils.countMatches(stripped, "φ");
      current = Math.min(current + amount, max);
      return buildEnchantmentBar(current, max);
    } else if (stripped.startsWith(arcaneBarStart)) {
      int current = StringUtils.countMatches(stripped, "ω");
      int max = current +  StringUtils.countMatches(stripped, "φ");
      current = Math.min(current + amount, max);
      return buildArcaneBar(current, max);
    } else {
      return null;
    }
  }

  public static Pair<String, Integer> depleteEnchantBar(String string) {
    String stripped = ChatColor.stripColor(string);
    if (stripped.startsWith("[|")) {
      int current = StringUtils.countMatches(stripped, "|");
      int max = current;
      current--;
      if (string.contains("" + ChatColor.DARK_RED)) {
        return buildArcaneBar(current, max);
      } else {
        return buildEnchantmentBar(current, max);
      }
    } else if (stripped.startsWith(enchantBarStart)) {
      int current = StringUtils.countMatches(stripped, "υ");
      int max = current + StringUtils.countMatches(stripped, "φ");
      current--;
      return buildEnchantmentBar(current, max);
    } else if (stripped.startsWith(arcaneBarStart)) {
      int current = StringUtils.countMatches(stripped, "ω");
      int max = current +  StringUtils.countMatches(stripped, "φ");
      current--;
      return buildArcaneBar(current, max);
    } else {
      return null;
    }
  }

  public static Pair<String, Integer> buildEnchantmentBar(int current, int max) {
    if (current == 0) {
      return Pair.of(MaterialUtil.ENCHANTABLE_TAG, 0);
    }
    String bar = ChatColor.WHITE + enchantBarStart;
    bar += StringUtils.repeat(enchantBarFull, current);
    bar += StringUtils.repeat(enchantBarEmpty, max - current);
    bar += enchantBarEnd;
    return Pair.of(bar, current);
  }

  public static Pair<String, Integer> buildArcaneBar(int current, int max) {
    if (current == 0) {
      return Pair.of(MaterialUtil.ENCHANTABLE_TAG, 0);
    }
    String bar = ChatColor.WHITE + arcaneBarStart;
    bar += StringUtils.repeat(arcaneBarFull, current);
    bar += StringUtils.repeat(enchantBarEmpty, max - current);
    bar += enchantBarEnd;
    return Pair.of(bar, current);
  }

  // Left is the bar, right is the lore line index
  public static Pair<String, Integer> getEnchantBar(ItemStack stack) {
    int index = 0;
    for (String string : TextUtils.getLore(stack)) {
      index++;
      String s = net.md_5.bungee.api.ChatColor.stripColor(string);
      if (s.startsWith("[") && string.endsWith("]") && s.contains("||")) {
        return Pair.of(string, index - 1);
      }
      if (s.startsWith(enchantBarStart) || s.startsWith(arcaneBarStart)) {
        return Pair.of(string, index - 1);
      }
    }
    return null;
  }

  public static void depleteEnchantment(ItemStack stack, Player player) {
    depleteEnchantment(stack, player, null);
  }

  public static void depleteEnchantment(ItemStack stack, Player player, PlayerData data) {
    Pair<String, Integer> enchantBar = getEnchantBar(stack);
    if (enchantBar == null) {
      return;
    }
    List<String> lore = TextUtils.getLore(stack);
    Pair<String, Integer> depletionResult = depleteEnchantBar(enchantBar.getLeft());
    if (depletionResult.getRight() == 0) {
      sendMessage(player, LootPlugin.getInstance().getSettings().getString("language.enchant.degrade", ""));
      lore.set(enchantBar.getRight(), depletionResult.getLeft());
      lore.remove(enchantBar.getRight() - 1);
      TextUtils.setLore(stack, lore);
    } else if (depletionResult.getRight() < 5) {
      ToastUtils.sendToast(player, FaceColor.NO_SHADOW +
          UnicodeUtil.unicodePlacehold("<toast_low_enchant>"), ItemUtils.BLANK);
      //sendMessage(player, LootPlugin.getInstance().getSettings().getString("language.enchant.bar-low", ""));
      lore.set(enchantBar.getRight(), depletionResult.getLeft());
      TextUtils.setLore(stack, lore);
    } else {
      lore.set(enchantBar.getRight(), depletionResult.getLeft());
      TextUtils.setLore(stack, lore);
    }
    if (data != null) {
      EquipmentUpdateEvent e = new EquipmentUpdateEvent(player, data, new HashSet<>(), false);
      Bukkit.getServer().getPluginManager().callEvent(e);
    }
  }

  public static boolean enchantItem(Player player, ItemStack tomeStack, ItemStack targetItem) {
    if (!canBeEnchanted(player, tomeStack, targetItem)) {
      return false;
    }
    EnchantmentTome tome = getEnchantmentItem(tomeStack);

    if (tome == null) {
      return false;
    }

    LootEnchantEvent enchantEvent = new LootEnchantEvent(player, targetItem, tome);
    Bukkit.getPluginManager().callEvent(enchantEvent);

    if (enchantEvent.isCancelled()) {
      return false;
    }

    List<String> lore = new ArrayList<>(TextUtils.getLore(targetItem));
    List<String> strippedLore = InventoryUtil.stripColor(lore);

    int index = strippedLore.indexOf(MaterialUtil.ENCHANTABLE_TAG_S);
    lore.remove(index);

    SkillLevelData enchantSkill = PlayerDataUtil.getSkillLevels(player, LifeSkillType.CRAFTING, true);

    List<String> added = new ArrayList<>();
    if (!tome.getLore().isEmpty()) {
      added.addAll(ListExtensionsKt.chatColorize(tome.getLore()));
    }

    if (!StringUtils.isBlank(tome.getStat())) {
      int itemLevel = MaterialUtil.getLevelRequirement(targetItem);
      float enchantPower = Math.max(1, Math.min(enchantSkill.getLevelWithBonus(), itemLevel));
      float rarity = (float) MaterialUtil.getBaseEnchantBonus(enchantSkill.getLevelWithBonus());

      ItemStat stat = LootPlugin.getInstance().getStatManager().getStat(tome.getStat());
      added.add(LootPlugin.getInstance().getStatManager().getFinalStat(stat, enchantPower, rarity).getStatString());
    }

    if (tome.getBar()) {
      double skillRatio = Math.min(1, enchantSkill.getLevelWithBonus() / 100);
      double roll = skillRatio * LootPlugin.RNG.nextFloat() + (1 - skillRatio) * Math.pow(LootPlugin.RNG.nextFloat(), 2.5);
      double size = 14 + 20 * roll;
      added.add(buildEnchantmentBar((int) size, (int) size).getLeft());
    }

    lore.addAll(index, added);

    if (enchantmentsStack) {
      for (Map.Entry<Enchantment, Integer> entry : tome.getEnchantments().entrySet()) {
        if (targetItem.containsEnchantment(entry.getKey())) {
          int previousLevel = targetItem.getEnchantmentLevel(entry.getKey());
          int newLevel = previousLevel + entry.getValue();
          targetItem.removeEnchantment(entry.getKey());
          targetItem.addUnsafeEnchantment(entry.getKey(), newLevel);
        } else {
          targetItem.addUnsafeEnchantment(entry.getKey(), entry.getValue());
        }
      }
    } else {
      boolean fail = true;
      for (Map.Entry<Enchantment, Integer> entry : tome.getEnchantments().entrySet()) {
        if (targetItem.containsEnchantment(entry.getKey())) {
          if (targetItem.getEnchantmentLevel(entry.getKey()) < entry.getValue()) {
            targetItem.removeEnchantment(entry.getKey());
            targetItem.addUnsafeEnchantment(entry.getKey(), entry.getValue());
            fail = false;
          }
        } else {
          targetItem.addUnsafeEnchantment(entry.getKey(), entry.getValue());
          fail = false;
        }
      }
      if (fail) {
        sendMessage(player, enchantPointlessMsg);
        return false;
      }
    }

    tomeStack.setAmount(tomeStack.getAmount() - 1);
    TextUtils.setLore(targetItem, lore);

    float weightDivisor = tome.getWeight() == 0 ? 2000 : (float) tome.getWeight();
    float exp = 10 + 17 * (2000 / weightDivisor);
    LootPlugin.getInstance().getStrifePlugin().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.ENCHANTING, exp, false, false);
    sendMessage(player, enchantSuccessMsg);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.4f, 2.0f);
    return true;
  }

  public static EnchantmentTome getEnchantmentItem(String name) {
    String stoneName = stripColor(name.replace("Enchantment Tome - ", ""));
    return LootPlugin.getInstance().getEnchantTomeManager().getEnchantTome(stoneName);
  }

  public static SocketGem getSocketGem(ItemStack stack) {
    if (stack.getType() != Material.EMERALD) {
      return null;
    }
    return LootPlugin.getInstance().getSocketGemManager().getSocketGem(stack);
  }

  public static EnchantmentTome getEnchantmentItem(ItemStack stack) {
    if (stack == null) {
      return null;
    }
    String name = ItemStackExtensionsKt.getDisplayName(stack);
    if (StringUtils.isBlank(name)) {
      return null;
    }
    if (!net.md_5.bungee.api.ChatColor.stripColor(name).startsWith("Enchantment Tome - ")) {
      return null;
    }
    return getEnchantmentItem(name);
  }

  public static boolean isEnchantmentItem(ItemStack stack) {
    return getEnchantmentItem(stack) != null;
  }

  public static boolean isHelmet(ItemStack stack) {
    return stack.getType() == Material.LEATHER_HELMET || stack.getType() == Material.IRON_HELMET
        || stack.getType() == Material.GOLDEN_HELMET || stack.getType() == Material.DIAMOND_HELMET
        || stack.getType() == Material.CHAINMAIL_HELMET;
  }

  public static boolean isNormalHead(ItemStack stack) {
    if (stack.getType() == Material.PLAYER_HEAD) {
      return MaterialUtil.getCustomData(stack) < 2000;
    }
    return false;
  }

  public static boolean isHelmetHead(ItemStack stack) {
    if (stack.getType() == Material.PLAYER_HEAD) {
      return MaterialUtil.getCustomData(stack) >= 2000;
    }
    return false;
  }

  public static void convertToHead(Player player, ItemStack head, ItemStack helmet) {
    if (LootPlugin.getInstance().getPlayerPointsAPI().look(player.getUniqueId()) < 600) {
      player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.7f);
      MessageUtils.sendMessage(player, noPointsForHelmetMsg);
      return;
    }
    player.playSound(player.getLocation(), Sound.ENTITY_MOOSHROOM_CONVERT, 1, 1f);
    LootPlugin.getInstance().getPlayerPointsAPI().take(player.getUniqueId(), 500);
    SkullMeta newMeta = ((SkullMeta) head.getItemMeta()).clone();
    short dura = helmet.getDurability();
    newMeta.setDisplayName(ItemStackExtensionsKt.getDisplayName(helmet));
    newMeta.setLore(new ArrayList<>(TextUtils.getLore(helmet)));
    helmet.setType(Material.PLAYER_HEAD);
    helmet.setItemMeta(newMeta);
    helmet.setDurability(dura);
    ItemStackExtensionsKt.setCustomModelData(helmet, 200000 + MaterialUtil.getCustomData(helmet));
    head.setAmount(head.getAmount() - 1);
  }

  public static ItemStack buildMaterial(Material m, String name, int level, int quality) {
    ItemStack his = new ItemStack(m);
    FaceColor color;
    String prefix;
    String tag;
    switch (quality) {
      case 2 -> {
        prefix = "";
        color = FaceColor.BLUE;
        tag = TAG_UNCOMMON;
      }
      case 3 -> {
        prefix = "Quality ";
        color = FaceColor.PURPLE;
        tag = TAG_RARE;
      }
      case 4 -> {
        prefix = "Superb ";
        color = FaceColor.RED;
        tag = TAG_EPIC;
      }
      default -> {
        prefix = "Crappy ";
        color = FaceColor.WHITE;
        tag = TAG_COMMON;
      }
    }
    ItemStackExtensionsKt.setDisplayName(his, color + prefix + name);
    List<String> lore = new ArrayList<>();
    lore.add(FaceColor.WHITE + "Item Level: " + Math.min(100, Math.max(1, 4 * (level / 4))));
    lore.add(FaceColor.WHITE + tag + "\uD86D\uDFF5");
    lore.add("");
    lore.add(FaceColor.LIGHT_GRAY + "Materials for crafting");
    TextUtils.setLore(his, lore, false);
    his.setDurability((short) 11);
    return his;
  }

  public static int getQuality(ItemStack stack) {
    if (!stack.hasItemMeta()) {
      return 1;
    }
    for (String line : TextUtils.getLore(stack)) {
      if (line.contains(TAG_COMMON)) {
        return 1;
      }
      if (line.contains(TAG_UNCOMMON)) {
        return 2;
      }
      if (line.contains(TAG_RARE)) {
        return 3;
      }
      if (line.contains(TAG_EPIC)) {
        return 4;
      }
      if (line.contains(TAG_UNIQUE)) {
        return 4;
      }
    }
    return 0;
  }

  public static ItemStack buildEssence(Tier tier, double itemLevel, List<String> possibleStats) {

    int essLevel = Math.max(1, (int) (Math.floor(itemLevel) / 10) * 10);

    String statString = ChatColor
        .stripColor(possibleStats.get(LootPlugin.RNG.nextInt(possibleStats.size())));
    int statVal = getDigit(statString);

    String newStatString = statString.replace(String.valueOf(statVal),
        String.valueOf((int) Math.max(1, statVal * (0.8 + LootPlugin.RNG.nextFloat() * 0.2))));

    return createEssence(tier.getName(), essLevel, ChatColor.stripColor(newStatString));
  }

  public static ItemStack createEssence(String typeName, int level, String stat) {
    ItemStack shard = new ItemStack(Material.PRISMARINE_SHARD);
    ItemStackExtensionsKt.setDisplayName(shard, FaceColor.CYAN + "Item Essence");

    List<String> esslore = new ArrayList<>();
    esslore.add(FaceColor.WHITE + "Item Level Requirement: " + level);
    esslore.add(FaceColor.WHITE + typeName + "\u0588");
    esslore.add("");
    esslore.add(FaceColor.CYAN + stat);
    esslore.add("");
    esslore.add(FaceColor.LIGHT_GRAY + "Craft this together with");
    esslore.add(FaceColor.LIGHT_GRAY + "an unfinished item to fill");
    esslore.add(FaceColor.LIGHT_GRAY + "an " + FaceColor.CYAN +
        "Essence Slot" + FaceColor.LIGHT_GRAY + "!");
    TextUtils.setLore(shard, esslore, false);
    ItemStackExtensionsKt.setCustomModelData(shard, 501);

    return shard;
  }

  public static int getUpgradeLevel(ItemStack stack) {
    return getUpgradeLevel(ItemStackExtensionsKt.getDisplayName(stack));
  }

  public static int getUpgradeLevel(String name) {
    name = stripColor(name);
    String lev = CharMatcher.digit().or(CharMatcher.is('-')).negate().collapseFrom(name, ' ')
        .trim();
    return NumberUtils.toInt(lev.split(" ")[0], 0);
  }

  public static List<String> getValidEssenceStats(List<String> lore) {
    List<String> existingCraftStatStrings = new ArrayList<>();
    for (String str : lore) {
      String strippedString = ChatColor.stripColor(str);
      if (!(strippedString.startsWith("+") || strippedString.startsWith("-"))) {
        continue;
      }
      if (FaceColor.PURPLE.isStartOf(str) || FaceColor.RED.isStartOf(str) ||
          FaceColor.ORANGE.isStartOf(str) || FaceColor.BLUE.isStartOf(str) ||
          FaceColor.LIGHT_GRAY.isStartOf(str)) {
        continue;
      }
      existingCraftStatStrings.add(ONLY_LETTERS.matcher(strippedString).replaceAll(""));
    }
    return existingCraftStatStrings;
  }

  public static boolean isEssence(ItemStack itemStack) {
    if (itemStack.getType() != Material.PRISMARINE_SHARD || StringUtils.isBlank(ItemStackExtensionsKt.getDisplayName(itemStack))) {
      return false;
    }
    if (!ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(itemStack)).equals("Item Essence")) {
      return false;
    }
    List<String> lore = TextUtils.getLore(itemStack);
    List<String> strippedLore = InventoryUtil.stripColor(lore);
    if (strippedLore.get(0) == null || !strippedLore.get(0).startsWith("Item Level Requirement")) {
      return false;
    }
    return true;
  }

  public static String getEssenceTag(ItemStack stack) {
    return ChatColor.stripColor(TextUtils.getLore(stack).get(1)).replace("\u0588", "");
  }

  public static List<Tier> getEssenceTiers(String tag) {
    return LootPlugin.getInstance().getTierManager().getTiersFromName(tag);
  }

  public static String getEssenceStat(ItemStack itemStack) {
    return TextUtils.getLore(itemStack).get(3);
  }

  public static boolean hasItemLevel(ItemStack h) {
    return !StringUtils.isBlank(ItemStackExtensionsKt.getDisplayName(h)) && h.hasItemMeta()
            && TextUtils.getLore(h).get(0) != null &&
            ChatColor.stripColor(TextUtils.getLore(h).get(0)).startsWith("Item Level: ");
  }

  public static int getEssenceLevel(ItemStack h) {
    return NumberUtils.toInt(CharMatcher.digit().or(CharMatcher.is('-')).negate().collapseFrom(
            ChatColor.stripColor(TextUtils.getLore(h).get(0)), ' ').trim());
  }

  public static float getExtendChance(int craftingLevel) {
    float extendChance = 0f;
    if (craftingLevel >= 20) {
      extendChance += 0.25;
    }
    if (craftingLevel >= 55) {
      extendChance += 0.25;
    }
    if (craftingLevel >= 85) {
      extendChance += 0.5;
    }
    return extendChance;
  }

  public static int getSockets(float quality, int craftingLevel) {
    int minSockets = getMinSockets(craftingLevel);
    int maxSockets = getMaxSockets(quality);
    if (minSockets >= maxSockets) {
      return minSockets;
    }
    int result = minSockets;
    while (result < maxSockets && LootPlugin.RNG.nextFloat() > 0.5f) {
      result++;
    }
    return result;
  }

  public static int getMinSockets(int craftingLevel) {
    return craftingLevel >= 60 ? 2 : craftingLevel >= 25 ? 1 : 0;
  }

  public static int getMaxSockets(float quality) {
    return quality > 2.1 ? 2 : 1;
  }

  public static int getLevelRequirement(ItemStack stack) {
    if (stack.getItemMeta() == null || !stack.getItemMeta().hasLore()) {
      return -1;
    }
    if (TextUtils.getLore(stack).get(0) == null) {
      return -1;
    }
    String lvlReqString = ChatColor.stripColor(TextUtils.getLore(stack).get(0));
    if (lvlReqString.startsWith("Level Requirement:") ||
        lvlReqString.startsWith("Skill Requirement:")) {
      return getDigit(TextUtils.getLore(stack).get(0));
    }
    return -1;
  }

  public static boolean updateItem(ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      return false;
    }

    EnchantmentTome tome = MaterialUtil.getEnchantmentItem(stack);
    if (tome != null) {
      ItemStack newStack = tome.toItemStack(1);
      stack.setType(newStack.getType());
      stack.setItemMeta(newStack.getItemMeta());
      return true;
    }

    UpgradeScroll scroll = LootPlugin.getInstance().getScrollManager().getScroll(stack);
    if (scroll != null) {
      ItemStack newStack = LootPlugin.getInstance().getScrollManager().buildItemStack(scroll);
      stack.setType(newStack.getType());
      stack.setItemMeta(newStack.getItemMeta());
      return true;
    }

    SocketGem gem = MaterialUtil.getSocketGem(stack);
    if (gem != null && !gem.getName().contains("ransmute")) {
      ItemStack newStack = gem.toItemStack(stack.getAmount());
      stack.setType(newStack.getType());
      stack.setItemMeta(newStack.getItemMeta());
      return true;
    }

    if (stack.getType() == Material.PAPER &&
        "Scroll Of Purity".equals(ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack)))) {
      ItemStack newStack = PurifyingScroll.get();
      stack.setType(newStack.getType());
      stack.setItemMeta(newStack.getItemMeta());
      return true;
    }

    if (SocketExtender.isSimilar(stack)) {
      ItemStack newStack = SocketExtender.build();
      stack.setType(newStack.getType());
      stack.setItemMeta(newStack.getItemMeta());
      return true;
    }

    if (ArcaneEnhancer.isSimilar(stack)) {
      ItemStack newStack = ArcaneEnhancer.get();
      stack.setType(newStack.getType());
      stack.setItemMeta(newStack.getItemMeta());
      return true;
    }

    boolean updated = false;
    List<String> lore = TextUtils.getLore(stack);
    List<Integer> replaceSockets = new ArrayList<>();
    List<Integer> replaceExtend = new ArrayList<>();
    List<Integer> replaceEnchant = new ArrayList<>();
    int i = 0;
    for (String s : lore) {
      String stripped = ChatColor.stripColor(s);
      if (stripped.equals("(Socket)")) {
        replaceSockets.add(i);
      } else if (stripped.equals("(+)")) {
        replaceExtend.add(i);
      } else if (stripped.equals("(Enchantable)")) {
        replaceEnchant.add(i);
      }
      i++;
    }
    if (!replaceSockets.isEmpty() || !replaceExtend.isEmpty() || !replaceEnchant.isEmpty()) {
      for (int index : replaceSockets) {
        lore.set(index, ItemBuilder.SOCKET);
      }
      for (int index : replaceExtend) {
        lore.set(index, ItemBuilder.EXTEND);
      }
      for (int index : replaceEnchant) {
        lore.set(index, MaterialUtil.ENCHANTABLE_TAG);
      }
      TextUtils.setLore(stack, lore, false);
      updated = true;
    }

    CustomItem ci = LootPlugin.getInstance().getCustomItemManager().getCustomItemFromStack(stack);
    if (ci != null) {
      ItemStack newStack = ci.toItemStack(1);
      stack.setType(newStack.getType());
      stack.setItemMeta(newStack.getItemMeta());
      return true;
    }

    return updated;
  }

  public static Tier getTierFromStack(ItemStack stack) {
    return LootPlugin.getInstance().getItemGroupManager().getTierFromStack(stack);
  }

  public static Map<Integer, String> getTinkerableStats(List<String> lore) {
    Map<Integer, String> validTargetStats = new HashMap<>();
    int loreIndex = -1;
    for (String str : lore) {
      loreIndex++;
      if (FaceColor.CYAN.isStartOf(str)) {
        return null;
      }
      if (!ChatColor.stripColor(str).startsWith("+")) {
        continue;
      }
      net.md_5.bungee.api.ChatColor color = CraftingUtil.getHexFromString(str);
      if (color != null) {
        if (CraftingUtil.isValidStealColor(color.getColor())) {
          validTargetStats.put(loreIndex, str);
        }
        continue;
      }
      if (str.startsWith(ChatColor.GREEN + "") || str.startsWith(ChatColor.YELLOW + "")) {
        validTargetStats.put(loreIndex, str);
      }
    }
    return validTargetStats;
  }

  public static void doTinkerEffects(LootPlugin plugin, ItemStack tinkerGear,
      ItemStack targetItem, Player player) {
    List<String> itemLore = new ArrayList<>(TextUtils.getLore(targetItem));
    Map<Integer, String> validTargetStats = getTinkerableStats(itemLore);

    if (validTargetStats == null) {
      sendMessage(player, TextUtils.color(plugin.getSettings().getString(
          "language.tinker.only-one-crafted-stat", "Only one stat can be tinkered!")));
      return;
    }
    if (validTargetStats.isEmpty()) {
      sendMessage(player, TextUtils.color(plugin.getSettings().getString(
          "language.tinker.no-valid-stats", "No valid stats to be tinkered!")));
      return;
    }

    List<Integer> keysAsArray = new ArrayList<>(validTargetStats.keySet());
    int selectedIndex = keysAsArray.get(LootPlugin.RNG.nextInt(keysAsArray.size()));
    itemLore.set(selectedIndex, FaceColor.CYAN + PreCraftListener.ESSENCE_SLOT_TEXT);
    TextUtils.setLore(targetItem, itemLore);

    player.playSound(player.getEyeLocation(), Sound.BLOCK_PISTON_EXTEND, 1F, 1.5F);
    plugin.getStrifePlugin().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.CRAFTING, 200, false, false);
    tinkerGear.setAmount(tinkerGear.getAmount() - 1);
  }

  public static ExistingSocketData buildSocketData(ItemStack stack) {
    ExistingSocketData data = new ExistingSocketData();
    if (stack == null || stack.getType() == Material.AIR) {
      return data;
    }
    List<String> lore = TextUtils.getLore(stack);
    if (lore.isEmpty()) {
      return data;
    }

    int gemNumber = 0;
    int lineNumber = -1;
    List<Integer> gemLoreIndexes = new ArrayList<>();

    for (String line : lore) {
      lineNumber++;
      String stripped = ChatColor.stripColor(line);
      if (stripped.equals(ItemBuilder.SOCKET_S)) {
        if (!gemLoreIndexes.isEmpty()) {
          data.addGemData(gemNumber, gemLoreIndexes);
          gemNumber++;
        }
        data.addGemData(gemNumber, null);
        gemLoreIndexes = new ArrayList<>();
        gemNumber++;
        continue;
      }
      if (line.contains("\uF804\uF824")) {
        if (!gemLoreIndexes.isEmpty()) {
          data.addGemData(gemNumber, gemLoreIndexes);
          gemNumber++;
        }
        gemLoreIndexes = new ArrayList<>();
        gemLoreIndexes.add(lineNumber);
        continue;
      }
      if (!gemLoreIndexes.isEmpty()) {
        // Must check " - " in case items have a passive below
        // Todo: not that ^
        if (StringUtils.isBlank(stripped) || stripped.contains(" - ") ||
            stripped.equals(ItemBuilder.EXTEND_S)) {
          data.addGemData(gemNumber, gemLoreIndexes);
          gemNumber++;
          gemLoreIndexes = new ArrayList<>();
        } else {
          gemLoreIndexes.add(lineNumber);
        }
      }
    }
    if (!gemLoreIndexes.isEmpty()) {
      data.addGemData(gemNumber, gemLoreIndexes);
    }
    return data;
  }

  public static ExistingSocketData destroyGem(ItemStack stack, int index) {
    ExistingSocketData data = buildSocketData(stack);
    if (data.getIndexes(index) == null) {
      return data;
    }
    List<String> lore = TextUtils.getLore(stack);
    List<Integer> loreIndexes = new ArrayList<>(data.getIndexes(index));
    Collections.sort(loreIndexes);
    int lowestIndex = loreIndexes.get(0);
    Collections.reverse(loreIndexes);
    try {
      for (int line : loreIndexes) {
        lore.remove(line);
      }
    } catch (Exception e) {
      return null;
    }
    lore.add(lowestIndex, ItemBuilder.SOCKET);
    TextUtils.setLore(stack, lore);
    data = buildSocketData(stack);
    return data;
  }

  public static int getItemRarity(ItemStack stack) {
    if (stack.getItemMeta() == null) {
      return 0;
    }
    if (TextUtils.getLore(stack).size() < 2) {
      return 0;
    }
    int rarity = checkRarity(TextUtils.getLore(stack).get(0));
    if (rarity != -1) {
      return rarity;
    }
    rarity = checkRarity(TextUtils.getLore(stack).get(1));
    return rarity != -1 ? rarity : 1;
  }

  private static int checkRarity(String s) {
    if (s.contains(TAG_COMMON)) {
      return 1;
    }
    if (s.contains(TAG_UNCOMMON)) {
      return 2;
    }
    if (s.contains(TAG_RARE)) {
      return 3;
    }
    if (s.contains(TAG_EPIC)) {
      return 4;
    }
    if (s.contains(TAG_UNIQUE)) {
      return 4;
    }
    return -1;
  }

  public static int getItemLevel(ItemStack stack) {
    if (stack.getItemMeta() == null) {
      return 0;
    }
    List<String> lore = TextUtils.getLore(stack);
    if (lore.isEmpty() || StringUtils.isEmpty(TextUtils.getLore(stack).get(0))) {
      return 0;
    }
    String strippedString = ChatColor.stripColor(TextUtils.getLore(stack).get(0));
    if (strippedString.startsWith("Item Level:")) {
      return getDigit(strippedString);
    }
    return 0;
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

  public static void applyTierLevelData(ItemStack stack, Tier tier, int level) {
    if (tier.getCustomDataStart() != -1) {
      int customModel = tier.getCustomDataStart() + level / tier.getCustomDataInterval();
      ItemStackExtensionsKt.setCustomModelData(stack, customModel);
      if (stack.getItemMeta() instanceof LeatherArmorMeta meta) {
        meta.setColor(Color.fromRGB(customModel));
        stack.setItemMeta(meta);
      }
    }
  }

  public static int getCustomData(ItemStack stack) {
    if (stack.getItemMeta() == null || !stack.getItemMeta().hasCustomModelData()) {
      return -1;
    }
    return stack.getItemMeta().getCustomModelData();
  }

  public static double getBaseEnchantBonus(double enchantSkill) {
    return enchantSkill * 0.0015;
  }

  private static boolean isBannedUpgradeMaterial(ItemStack item) {
    return switch (item.getType()) {
      case EMERALD, PAPER, NETHER_STAR, DIAMOND, GHAST_TEAR, AMETHYST_SHARD, PRISMARINE_SHARD,
          ENCHANTED_BOOK, NAME_TAG, QUARTZ, TNT_MINECART, SHEARS, WHEAT_SEEDS -> true;
      default -> false;
    };
  }
}
