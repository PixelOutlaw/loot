package info.faceland.loot.utils;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.ToastUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.UnicodeUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.tuple.Pair;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.events.LootEnchantEvent;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.menu.upgrade.EnchantMenu;
import java.util.HashSet;
import java.util.List;
import land.face.dinvy.entity.PlayerData;
import land.face.dinvy.events.EquipmentUpdateEvent;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EnchantUtil {

  private static String enchantFailureMsg;
  private static String enchantTagNeededMsg;
  private static String enchantSuccessMsg;

  public static void refreshConfig() {
    enchantFailureMsg = LootPlugin.getInstance().getSettings()
        .getString("language.enchant.failure", "");
    enchantTagNeededMsg = LootPlugin.getInstance().getSettings()
        .getString("language.enchant.needs-enchantable", "");
    enchantSuccessMsg = LootPlugin.getInstance().getSettings()
        .getString("language.enchant.success", "");
  }

  public static void purifyItem(Player player, ItemStack item, ItemStack purifyScroll) {
    EquipmentData data = PersistHelper.getEquipmentData(item);
    if (data == null || !data.isEnchanted()) {
      MessageUtils.sendMessage(player,
          "&eYou can only remove enchantments from items that have an enchantment ya ding dong");
      return;
    }

    data.unenchant();

    purifyScroll.setAmount(purifyScroll.getAmount() - 1);
    StrifePlugin.getInstance().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.ENCHANTING, 70, false, false);
    player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1, 1f);
    PersistHelper.setEquipmentData(item, data);
  }

  public static boolean enhanceEnchantment(Player player, ItemStack item, ItemStack enhancer) {
    EquipmentData data = PersistHelper.getEquipmentData(item);
    if (data == null || data.isEnhanced()) {
      MessageUtils.sendMessage(player, "&eThis item cannot be enhanced.");
      return false;
    }
    int itemLevel = MaterialUtil.getLevelRequirement(item);
    Champion champion = LootPlugin.getInstance().getStrifePlugin().getChampionManager().getChampion(player);
    if (itemLevel > EnchantMenu.getEnhanceRequirement(SkillRank.getRank(champion, LifeSkillType.ENCHANTING))) {
      MessageUtils.sendMessage(player, "&eYour enchanting level is too low!");
      return false;
    }
    if (!data.enhance()) {
      return false;
    }
    PersistHelper.setEquipmentData(item, data);
    enhancer.setAmount(enhancer.getAmount() - 1);

    StrifePlugin.getInstance().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 450, false, false);
    player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1, 0.8f);
    return true;
  }

  public static boolean canBeEnchanted(Player player, EnchantmentTome tome, ItemStack stack) {
    if (tome == null) {
      return false;
    }
    if (!MaterialUtil.isInGroup(stack, tome.getItemGroup())) {
      sendMessage(player, enchantFailureMsg);
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
      return false;
    }

    if (!PersistHelper.isEnchantable(stack)) {
      sendMessage(player, enchantTagNeededMsg);
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
      return false;
    }
    return true;
  }

  public static void refillEnchantment(Player player, ItemStack equipment, ItemStack crystal) {
    EquipmentData data = PersistHelper.getEquipmentData(equipment);
    if (data == null || !data.isEnchanted() || data.isEnhanced()) {
      return;
    }
    if (data.getMissingEnchantPower() < 1) {
      return;
    }
    double enchantLevel = PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.ENCHANTING);
    double itemLevel = data.getLevel();
    int addAmount = 2 + (int) (LootPlugin.RNG.nextFloat() * (2 + Math.max(0, (enchantLevel - itemLevel) * 0.2)));

    data.refillEnchantment(Math.min(addAmount, data.getMissingEnchantPower()));
    PersistHelper.setEquipmentData(equipment, data, true);

    StrifePlugin.getInstance().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 8f + 4 * addAmount, false, false);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_GLASS_BREAK, 1F, 1.2F);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1F, 1F);
    crystal.setAmount(crystal.getAmount() - 1);
  }

  public static void depleteEnchantment(ItemStack stack, Player player, PlayerData data) {
    EquipmentData equipmentData = PersistHelper.getEquipmentData(stack);
    if (equipmentData == null || !equipmentData.isEnchanted()) {
      return;
    }
    boolean stateChange = equipmentData.tickEnchant(1);
    if (stateChange) {
      if (!equipmentData.isEnchanted()) {
        sendMessage(player, LootPlugin.getInstance().getSettings().getString("language.enchant.degrade", ""));
      } else if (equipmentData.getMissingEnchantPower() > 80) {
        sendMessage(player, LootPlugin.getInstance().getSettings().getString("language.enchant.bar-low", ""));
        ToastUtils.sendToast(player, FaceColor.NO_SHADOW + UnicodeUtil.unicodePlacehold("<toast_low_enchant>"), ItemUtils.BLANK);
      }
      PersistHelper.setEquipmentData(stack, equipmentData);
      EquipmentUpdateEvent e = new EquipmentUpdateEvent(player, data, new HashSet<>(), false);
      Bukkit.getServer().getPluginManager().callEvent(e);
    }
  }

  public static boolean enchantItem(Player player, ItemStack tomeStack, ItemStack targetItem) {
    EnchantmentTome tome = EnchantmentTome.get(tomeStack);
    if (!canBeEnchanted(player, tome, targetItem)) {
      return false;
    }

    LootEnchantEvent enchantEvent = new LootEnchantEvent(player, targetItem, tome);
    Bukkit.getPluginManager().callEvent(enchantEvent);

    if (enchantEvent.isCancelled()) {
      return false;
    }

    SkillLevelData enchantSkill = PlayerDataUtil.getSkillLevels(player, LifeSkillType.ENCHANTING, true);

    int itemLevel = MaterialUtil.getLevelRequirement(targetItem);
    //System.out.println("enchanted item levle " + itemLevel);
    //System.out.println("enchantedlvl " + enchantSkill.getLevelWithBonus());
    float enchantPower = Math.max(1, Math.min(enchantSkill.getLevelWithBonus(), itemLevel));
    float rarity = (float) getBaseEnchantBonus(enchantSkill.getLevelWithBonus());
    //System.out.println("rarity " + rarity);

    boolean success = ModifyEquipmentUtil.enchant(targetItem, tome, (int) enchantPower, rarity);

    if (!success) {
      return false;
    }

    tomeStack.setAmount(tomeStack.getAmount() - 1);

    float exp;
    if (tome.getEnchantXp() > 0) {
      exp = (float) tome.getEnchantXp();
    } else {
      float weightMult = Math.min(1, Math.max(0, (2000f - (float) tome.getWeight()) / 2000f));
      exp = 25 + 80 * weightMult;
    }
    exp *= 1 + (enchantPower / 50);
    LootPlugin.getInstance().getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, exp, false, false);
    sendMessage(player, enchantSuccessMsg);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.4f, 2.0f);
    return true;
  }

  public static double getBaseEnchantBonus(double enchantSkill) {
    return enchantSkill * 0.0015;
  }
}
