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

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.DeconstructData;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.enchantments.EnchantmentTome;
import info.faceland.loot.events.LootEnchantEvent;
import info.faceland.loot.items.prefabs.ShardOfFailure;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.math.LootRandom;
import info.faceland.loot.menu.upgrade.EnchantMenu;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.tier.Tier;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.PlayerDataUtil;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public final class MaterialUtil {

  private static final double BONUS_ESS_MULT = 0.3;
  private static final double TOOL_QUALITY_ESS_MULT = 0.08;
  private static final double MIN_BONUS_ESS_MULT = 0.25;
  private static final double BASE_ESSENCE_MULT = 0.3;
  private static final LootRandom random = new LootRandom();

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
    success += PlayerDataUtil.getEffectiveLifeSkill(player, LifeSkillType.ENCHANTING, true) * 0.001;
    if (success <= 1) {
      success += (1 - success) * getFailureMod(getFailureBonus(scrollStack));
    }
    return success;
  }

  public static int getFailureBonus(ItemStack scrollStack) {
    for (String s : ItemStackExtensionsKt.getLore(scrollStack)) {
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
    return random.nextDouble() * getMaxFailureDamagePercent(scroll, itemPlus);
  }

  public static double getMaxFailureDamagePercent(UpgradeScroll scroll, int itemPlus) {
    return (0.25 + itemPlus * 0.11) * scroll.getItemDamageMultiplier();
  }

  public static boolean canBeExtended(List<String> lore) {
    List<String> stripColor = InventoryUtil.stripColor(lore);
    return stripColor.contains("(+)");
  }

  public static void extendItem(Player player, ItemStack stack, ItemStack extender) {
    List<String> lore = new ArrayList<>(ItemStackExtensionsKt.getLore(stack));
    List<String> strippedLore = InventoryUtil.stripColor(lore);
    if (!canBeExtended(strippedLore)) {
      sendMessage(player, extendFailMsg);
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
      return;
    }
    int index = strippedLore.indexOf("(+)");
    lore.set(index, ChatColor.GOLD + "(Socket)");
    ItemStackExtensionsKt.setLore(stack, lore);

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
    String targetName = stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    UpgradeScroll scroll = LootPlugin.getInstance().getScrollManager().getScroll(scrollStack);

    int itemUpgradeLevel = MaterialUtil.getUpgradeLevel(targetName);
    int targetLevel = itemUpgradeLevel;
    int shards = getFailureBonus(scrollStack);
    if (stack.getType() == Material.BOOK || stack.getType() == Material.ARROW) {
      targetLevel += 3;
    } else {
      targetLevel += 1;
    }
    targetLevel = Math.min(targetLevel, 15);

    double successChance = getSuccessChance(player, targetLevel, scrollStack, scroll);

    scrollStack.setAmount(scrollStack.getAmount() - 1);

    // SUCCESS!
    if (successChance >= random.nextDouble()) {
      bumpItemPlus(stack, itemUpgradeLevel, 1, targetLevel - itemUpgradeLevel);

      double exp = 0.5f + (float) Math.pow(1.4, targetLevel);
      LootPlugin.getInstance().getStrifePlugin().getSkillExperienceManager()
          .addExperience(player, LifeSkillType.ENCHANTING, exp, false, false);

      sendMessage(player, upgradeSuccessMsg);
      player.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 2F);

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
          shards += itemUpgradeLevel / 4 + random.nextIntRange(0, itemUpgradeLevel - 5);
        }
        distributeShards(player, shards);
        return;
      }
      damage = Short.MAX_VALUE;
    } else {
      double totalPercentage = damagePercentage + currentDamagePercentage;
      damage = (short) (Math.floor(totalPercentage * (double) stack.getType().getMaxDurability()));
    }

    if (damage >= stack.getType().getMaxDurability()) {
      player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1F);
      sendMessage(player, upgradeItemDestroyMsg);
      InventoryUtil.sendToDiscord(player, stack.clone(), upgradeItemDestroyBroadcast);
      stack.setAmount(0);
      if (itemUpgradeLevel > 5) {
        shards += itemUpgradeLevel / 4 + random.nextIntRange(0, itemUpgradeLevel - 5);
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

  public static void bumpItemPlus(ItemStack stack, int currentLevel, int statAmount,
      int plusAmount) {
    String itemName = ItemStackExtensionsKt.getDisplayName(stack);
    if (StringUtils.isBlank(itemName)) {
      itemName = stack.getType().toString().replaceAll("_", " ");
      itemName = WordUtils.capitalizeFully(itemName);
    }
    int newLevel = Math.max(Math.min(currentLevel + plusAmount, 15), 0);
    if (currentLevel == 0) {
      String colorPrefix;
      ChatColor color = InventoryUtil.getFirstColor(itemName);
      if (color == ChatColor.RESET || color == ChatColor.WHITE) {
        colorPrefix = TextUtils.color("&f&7&f");
      } else {
        colorPrefix = "" + color;
      }
      itemName = colorPrefix + ("+" + newLevel) + " " + itemName;
    } else {
      itemName = itemName.replace("+" + currentLevel, "+" + newLevel);
    }
    ItemStackExtensionsKt.setDisplayName(stack, itemName);
    if (newLevel >= 10 && stack.getEnchantments().isEmpty()) {
      stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
    }
    ItemStackExtensionsKt.addItemFlags(stack, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES,
        ItemFlag.HIDE_UNBREAKABLE);
    List<String> lore = ItemStackExtensionsKt.getLore(stack);
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
    ItemStackExtensionsKt.setLore(stack, lore);
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
    List<String> lore = ItemStackExtensionsKt.getLore(stack);
    for (String s : lore) {
      if ("< Cannot be upgraded >".equals(stripColor(s))) {
        return false;
      }
    }
    return isEquipmentItem(stack);
  }

  public static boolean isEquipmentItem(ItemStack stack) {
    if (stack.getType().getMaxDurability() > 5) {
      return true;
    }
    List<String> strip = InventoryUtil.stripColor(ItemStackExtensionsKt.getLore(stack));
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

  public static boolean hasEnchantmentTag(ItemStack stack) {
    List<String> lore = new ArrayList<>(ItemStackExtensionsKt.getLore(stack));
    List<String> strippedLore = InventoryUtil.stripColor(lore);
    return strippedLore.contains("(Enchantable)");
  }

  public static boolean isMatchingGroup(EnchantmentTome tome, Material material) {
    return LootPlugin.getInstance().getItemGroupManager().getMatchingItemGroups(material)
        .containsAll(tome.getItemGroups());
  }

  public static void purifyItem(Player player, ItemStack item, ItemStack purifyScroll) {
    if (!isEnchanted(item)) {
      MessageUtils.sendMessage(player,
          "&eYou can only remove enchantments from items that have an enchantment ya ding dong");
      return;
    }
    MaterialUtil.removeEnchantment(item);
    purifyScroll.setAmount(purifyScroll.getAmount() - 1);
    StrifePlugin.getInstance().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.ENCHANTING, 68, false, false);
    player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1, 1f);
  }

  public static void enhanceEnchantment(Player player, ItemStack item, ItemStack enhancer) {
    if (!MaterialUtil.isEnchanted(item) || MaterialUtil.isArcaneEnchanted(item)) {
      MessageUtils.sendMessage(player, "&eThis item cannot be enhanced.");
      return;
    }

    int itemLevel = getItemLevel(item);
    double rawLevel = PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.ENCHANTING);

    if (EnchantMenu.getEnhanceRequirement(itemLevel) > rawLevel) {
      MessageUtils.sendMessage(player, "&eYour enchanting level is too low!");
      return;
    }

    int index = -1;
    for (int i = 0; i < ItemStackExtensionsKt.getLore(item).size(); i++) {
      String string = ItemStackExtensionsKt.getLore(item).get(i);
      if (!isEnchantBar(string)) {
        continue;
      }
      index = i;
      break;
    }
    if (index == -1) {
      return;
    }

    degradeItemEnchantment(item, player);

    List<String> lore = new ArrayList<>(ItemStackExtensionsKt.getLore(item));
    String enchantmentStatString = ChatColor.stripColor(lore.get(index - 1));

    int statValue = NumberUtils
        .toInt(CharMatcher.digit().or(CharMatcher.is('-')).retainFrom(enchantmentStatString));

    itemLevel = Math.max(1, Math.min(100, itemLevel));
    double enchantingLevel = PlayerDataUtil.getEffectiveLifeSkill(player,
        LifeSkillType.ENCHANTING, true);

    double enchantingBonus = Math.min(2.5, Math.max(1, enchantingLevel / itemLevel));
    float enhanceRoll = 0.1f + 0.2f * (float) Math.pow(Math.random(), 1.25);

    int newValue = statValue + (int) (statValue * enhanceRoll * enchantingBonus);
    newValue++;

    lore.set(index - 1,
        ChatColor.BLUE + enchantmentStatString
            .replace(Integer.toString(statValue), Integer.toString(newValue)));
    lore.set(index, lore.get(index).replace(ChatColor.BLACK + "", ChatColor.DARK_RED + ""));
    ItemStackExtensionsKt.setLore(item, lore);
    enhancer.setAmount(enhancer.getAmount() - 1);
    StrifePlugin.getInstance().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.ENCHANTING, 400, false, false);
    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 0.8f);
  }


  public static void removeEnchantment(ItemStack item) {
    if (!MaterialUtil.isEnchanted(item)) {
      return;
    }
    List<String> lore = new ArrayList<>();
    for (int i = 0; i < ItemStackExtensionsKt.getLore(item).size(); i++) {
      String string = ItemStackExtensionsKt.getLore(item).get(i);
      if (!string.startsWith(ChatColor.BLUE + "[")) {
        lore.add(string);
        continue;
      }
      lore.remove(lore.size() - 1);
      lore.add(ChatColor.BLUE + "(Enchantable)");
    }
    ItemStackExtensionsKt.setLore(item, lore);
  }

  public static boolean canBeEnchanted(Player player, ItemStack tomeStack, ItemStack stack) {
    EnchantmentTome tome = getEnchantmentItem(tomeStack);
    if (tome == null) {
      return false;
    }
    if (!isMatchingGroup(tome, stack.getType())) {
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

  public static boolean isEnchantBar(String string) {
    String stripped = ChatColor.stripColor(string);
    return stripped.startsWith("[") && stripped.endsWith("]") && stripped.contains("||");
  }

  public static boolean isEnchanted(ItemStack stack) {
    for (String string : ItemStackExtensionsKt.getLore(stack)) {
      String s = net.md_5.bungee.api.ChatColor.stripColor(string);
      if (!(s.startsWith("[") && string.endsWith("]") && s.contains("||"))) {
        return true;
      }
    }
    return false;
  }

  public static boolean isArcaneEnchanted(ItemStack stack) {
    if (!isEnchanted(stack)) {
      return false;
    }
    for (String string : ItemStackExtensionsKt.getLore(stack)) {
      if (isEnchantBar(string) && string.contains("" + ChatColor.DARK_RED)) {
        return true;
      }
    }
    return false;
  }

  public static void degradeItemEnchantment(ItemStack item, Player player) {
    if (!MaterialUtil.isEnchanted(item)) {
      return;
    }
    List<String> lore = new ArrayList<>();
    for (int i = 0; i < ItemStackExtensionsKt.getLore(item).size(); i++) {
      String barString = ItemStackExtensionsKt.getLore(item).get(i);
      if (!isEnchantBar(barString)) {
        lore.add(barString);
        continue;
      }
      barString = barString.replace("" + ChatColor.BLUE, "");
      ChatColor incompleteBarColor;
      int index;
      if (barString.contains("" + ChatColor.DARK_RED)) {
        index = barString.indexOf("" + ChatColor.DARK_RED);
        incompleteBarColor = ChatColor.DARK_RED;
      } else if (barString.contains("" + ChatColor.BLACK)) {
        index = barString.indexOf("" + ChatColor.BLACK);
        incompleteBarColor = ChatColor.BLACK;
      } else {
        index = barString.indexOf("]");
        incompleteBarColor = ChatColor.BLACK;
      }
      if (index <= 2) {
        lore.remove(lore.size() - 1);
        lore.add(ChatColor.BLUE + "(Enchantable)");
        sendMessage(player,
            LootPlugin.getInstance().getSettings().getString("language.enchant.degrade", ""));
        continue;
      } else if (index <= 5) {
        sendMessage(player,
            LootPlugin.getInstance().getSettings().getString("language.enchant.bar-low", ""));
      }
      barString = barString.replace("" + incompleteBarColor, "");
      barString = new StringBuilder(barString).insert(index - 1, incompleteBarColor + "")
          .toString();
      barString = barString.replace("[", ChatColor.BLUE + "[").replace("]", ChatColor.BLUE + "]");
      lore.add(barString);
    }
    ItemStackExtensionsKt.setLore(item, lore);
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

    List<String> lore = new ArrayList<>(ItemStackExtensionsKt.getLore(targetItem));
    List<String> strippedLore = InventoryUtil.stripColor(lore);

    int index = strippedLore.indexOf("(Enchantable)");
    lore.remove(index);

    double enchantSkill = PlayerDataUtil
        .getEffectiveLifeSkill(player, LifeSkillType.ENCHANTING, true);

    List<String> added = new ArrayList<>();
    if (!tome.getLore().isEmpty()) {
      added.addAll(ListExtensionsKt.chatColorize(tome.getLore()));
    }

    if (!StringUtils.isBlank(tome.getStat())) {
      int itemLevel = MaterialUtil.getLevelRequirement(targetItem);
      double eLevel = Math.max(1, Math.min(enchantSkill, itemLevel));
      double rarity = MaterialUtil.getBaseEnchantBonus(enchantSkill);

      ItemStat stat = LootPlugin.getInstance().getStatManager().getStat(tome.getStat());
      added.add(LootPlugin.getInstance().getStatManager().getFinalStat(stat, eLevel, rarity, false)
          .getStatString());
    }

    if (tome.getBar()) {
      double skillRatio = Math.min(1, enchantSkill / 100);
      double roll = skillRatio * Math.random() + (1 - skillRatio) * Math.pow(Math.random(), 2.5);
      double size = 8 + 22 * roll;
      String bars = IntStream.range(0, (int) size).mapToObj(i -> "|")
          .collect(Collectors.joining(""));
      added.add(ChatColor.BLUE + "[" + bars + "]");
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
    ItemStackExtensionsKt.setLore(targetItem, lore);

    float weightDivisor = tome.getWeight() == 0 ? 2000 : (float) tome.getWeight();
    float exp = 12 + 8 * (2000 / weightDivisor);
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
    if (!name.startsWith(ChatColor.BLUE + "Enchantment Tome - ")) {
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
    newMeta.setLore(new ArrayList<>(ItemStackExtensionsKt.getLore(helmet)));
    helmet.setType(Material.PLAYER_HEAD);
    helmet.setItemMeta(newMeta);
    helmet.setDurability(dura);
    ItemStackExtensionsKt.setCustomModelData(helmet, 200000 + MaterialUtil.getCustomData(helmet));
    head.setAmount(head.getAmount() - 1);
  }

  public static boolean isExtender(ItemStack stack) {
    return ItemStackExtensionsKt.getDisplayName(SocketExtender.EXTENDER)
        .equals(ItemStackExtensionsKt.getDisplayName(stack));
  }

  public static ItemStack buildMaterial(Material m, String name, int level, int quality) {
    ItemStack his = new ItemStack(m);
    ChatColor color;
    String prefix;
    switch (quality) {
      case 2:
        prefix = "Quality";
        color = ChatColor.BLUE;
        break;
      case 3:
        prefix = "Rare";
        color = ChatColor.DARK_PURPLE;
        break;
      case 4:
        prefix = "Grand";
        color = ChatColor.RED;
        break;
      case 5:
        prefix = "Perfect";
        color = ChatColor.GOLD;
        break;
      default:
        prefix = "Old";
        color = ChatColor.WHITE;
    }
    ItemStackExtensionsKt.setDisplayName(his, color + prefix + " " + name);
    List<String> lore = new ArrayList<>();
    lore.add(ChatColor.WHITE + "Item Level: " + Math.min(100, Math.max(1, 3 * (level / 3))));
    lore.add(ChatColor.WHITE + "Quality: " + color + IntStream.range(0, quality).mapToObj(i -> "✪")
        .collect(Collectors.joining("")));
    lore.add(ChatColor.YELLOW + "[ Crafting Component ]");
    ItemStackExtensionsKt.setLore(his, lore);
    his.setDurability((short) 11);
    return his;
  }

  public static int getQuality(ItemStack stack) {
    for (String line : ItemStackExtensionsKt.getLore(stack)) {
      if (ChatColor.stripColor(line).startsWith("Quality:")) {
        return (int) line.chars().filter(ch -> ch == '✪').count();
      }
    }
    return 0;
  }

  public static ItemStack buildEssence(Tier tier, double itemLevel, double craftLevelAdvantage,
      int toolQuality,
      List<String> possibleStats, boolean lucky) {

    int essLevel = Math.max(1, (int) (Math.floor(itemLevel) / 10) * 10);

    String statString = ChatColor
        .stripColor(possibleStats.get(random.nextInt(possibleStats.size())));
    int statVal = getDigit(statString);

    craftLevelAdvantage = Math.min(craftLevelAdvantage, 200);
    double toolQualityBonus = TOOL_QUALITY_ESS_MULT * toolQuality;
    double baseCraftBonus = MIN_BONUS_ESS_MULT * (craftLevelAdvantage / 200);
    double bonusCraftBonus = (BONUS_ESS_MULT * (craftLevelAdvantage / 200)) * rollMult(lucky, 1.3);

    double essMult = BASE_ESSENCE_MULT + baseCraftBonus + bonusCraftBonus + toolQualityBonus;

    String newStatString = statString.replace(String.valueOf(statVal),
        String.valueOf((int) Math.max(1, statVal * essMult)));

    ItemStack shard = new ItemStack(Material.PRISMARINE_SHARD);
    ItemStackExtensionsKt.setDisplayName(shard, ChatColor.YELLOW + "Item Essence");

    List<String> esslore = new ArrayList<>();
    esslore.add("&fItem Level Requirement: " + essLevel);
    esslore.add("&fItem Type: " + tier.getName());
    esslore.add("&e" + newStatString);
    esslore.add("&7&oCraft this together with an");
    esslore.add("&7&ounfinished item to have a");
    esslore.add("&7&ochance of applying this stat!");
    esslore.add("&e[ Crafting Component ]");
    ItemStackExtensionsKt.setLore(shard, ListExtensionsKt.chatColorize(esslore));
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
      if (!strippedString.startsWith("+")) {
        continue;
      }
      if (str.startsWith(ChatColor.BLUE + "+") || str.startsWith(ChatColor.GRAY + "+")
          || str.startsWith(ChatColor.DARK_PURPLE + "+") || str.startsWith(ChatColor.RED + "+")) {
        continue;
      }
      str = CharMatcher.javaLetter().or(CharMatcher.is(' ')).retainFrom(strippedString.trim());
      existingCraftStatStrings.add(str);
    }
    return existingCraftStatStrings;
  }

  public static int getLevelRequirement(ItemStack stack) {
    if (stack.getItemMeta() == null) {
      return -1;
    }
    if (ItemStackExtensionsKt.getLore(stack).get(0) == null) {
      return -1;
    }
    String lvlReqString = ChatColor.stripColor(ItemStackExtensionsKt.getLore(stack).get(0));
    if (!lvlReqString.startsWith("Level Requirement:")) {
      return -1;
    }
    return getDigit(ItemStackExtensionsKt.getLore(stack).get(0));
  }

  public static Tier getTierFromStack(ItemStack stack) {
    String strTier = "";
    int data = MaterialUtil.getCustomData(stack);
    for (DeconstructData dd : LootPlugin.getInstance().getCraftMatManager()
        .getDeconstructDataSet()) {
      if (StringUtils.isBlank(dd.getTierName())) {
        continue;
      }
      if (dd.getMaterial() == stack.getType()) {
        if (data >= dd.getMinCustomData() && data <= dd.getMaxCustomData()) {
          strTier = dd.getTierName();
          break;
        }
      }
    }
    if (StringUtils.isBlank(strTier)) {
      strTier = LootPlugin.getInstance().getCraftBaseManager().getCraftBases().get(stack.getType());
    }
    return LootPlugin.getInstance().getTierManager().getTier(strTier);
  }

  public static int getItemRarity(ItemStack stack) {
    if (stack.getItemMeta() == null) {
      return 0;
    }
    if (ItemStackExtensionsKt.getLore(stack).size() < 2) {
      return 0;
    }
    String tierString = ChatColor.stripColor(ItemStackExtensionsKt.getLore(stack).get(1));
    if (!tierString.startsWith("Tier:")) {
      return 0;
    }
    if (tierString.contains("Common")) {
      return 1;
    }
    if (tierString.contains("Uncommon")) {
      return 2;
    }
    if (tierString.contains("Rare")) {
      return 3;
    }
    if (tierString.contains("Epic")) {
      return 4;
    }
    if (tierString.contains("Unique")) {
      return 4;
    }
    return 1;
  }

  public static int getItemLevel(ItemStack stack) {
    if (stack.getItemMeta() == null) {
      return 0;
    }
    if (ItemStackExtensionsKt.getLore(stack).get(0) == null) {
      return 0;
    }
    String lvlReqString = ChatColor.stripColor(ItemStackExtensionsKt.getLore(stack).get(0));
    if (!lvlReqString.startsWith("Item Level:")) {
      return 0;
    }
    return getDigit(ItemStackExtensionsKt.getLore(stack).get(0));
  }

  public static int getToolLevel(ItemStack stack) {
    if (stack.getItemMeta() == null) {
      return -1;
    }
    if (ItemStackExtensionsKt.getLore(stack).get(0) == null) {
      return -1;
    }
    String lvlReqString = ChatColor.stripColor(ItemStackExtensionsKt.getLore(stack).get(0));
    if (!lvlReqString.startsWith("Craft Skill Requirement:")) {
      return -1;
    }
    return getDigit(ItemStackExtensionsKt.getLore(stack).get(0));
  }

  public static int getDigit(String string) {
    String lev = CharMatcher.digit().or(CharMatcher.is('-')).negate()
        .collapseFrom(ChatColor.stripColor(string), ' ').trim();
    return NumberUtils.toInt(lev.split(" ")[0], 0);
  }

  private static double rollMult(boolean lucky) {
    return rollMult(lucky, 2);
  }

  private static double rollMult(boolean lucky, double exponent) {
    return lucky ? random.nextDouble() : Math.pow(random.nextDouble(), exponent);
  }

  public static void applyTierLevelData(ItemStack stack, Tier tier, int level) {
    if (tier.getCustomDataStart() != -1) {
      int customModel = tier.getCustomDataStart() + level / tier.getCustomDataInterval();
      ItemStackExtensionsKt.setCustomModelData(stack, customModel);
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
    switch (item.getType()) {
      case EMERALD:
      case PAPER:
      case NETHER_STAR:
      case DIAMOND:
      case GHAST_TEAR:
      case ENCHANTED_BOOK:
      case NAME_TAG:
      case QUARTZ:
        return true;
      default:
        return false;
    }
  }
}
