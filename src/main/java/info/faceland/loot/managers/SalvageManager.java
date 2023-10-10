package info.faceland.loot.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static info.faceland.loot.utils.MaterialUtil.buildEssence;
import static info.faceland.loot.utils.MaterialUtil.getLevelRequirement;

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.CraftToolData;
import info.faceland.loot.tier.Tier;
import info.faceland.loot.utils.CraftingUtil;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SalvageManager {

  private LootPlugin plugin;

  private double baseEquipmentPrice;
  private double baseGemPrice;
  private double baseTomePrice;
  private double baseScrollPrice;
  private double equipPricePerLevel;
  private double gemWeightHalf;
  private double tomeWeightHalf;
  private double scrollWeightHalf;


  public SalvageManager(LootPlugin plugin) {
    this.plugin = plugin;
    baseEquipmentPrice = plugin.getSettings()
        .getDouble("config.selling.base-equipment-price", 5);
    baseGemPrice = plugin.getSettings()
        .getDouble("config.selling.base-gem-price", 5);
    baseTomePrice = plugin.getSettings()
        .getDouble("config.selling.base-tome-price", 5);
    baseScrollPrice = plugin.getSettings()
        .getDouble("config.selling.base-scroll-price", 5);
    equipPricePerLevel = plugin.getSettings()
        .getDouble("config.selling.equipment-price-per-level", 0.1);
    gemWeightHalf = plugin.getSettings()
        .getDouble("config.selling.weight-per-half-gem-price", 5);
    tomeWeightHalf = plugin.getSettings()
        .getDouble("config.selling.weight-per-half-tome-price", 5);
    scrollWeightHalf = plugin.getSettings()
        .getDouble("config.selling.weight-per-half-scroll-price", 5);
  }

  public void destroy(Player player, ItemStack selectedStack, ItemStack toolStack) {
    if (selectedStack == null || toolStack == null ||
        selectedStack.getAmount() < 1 || toolStack.getAmount() < 1) {
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return;
    }
    int itemLevel = getLevelRequirement(selectedStack);
    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.CRAFTING, true);
    int craftingLevel = data.getLevel();

    if (craftingLevel + 10 < itemLevel) {
      sendMessage(player, plugin.getSettings().getString("language.craft.low-level", ""));
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return;
    }

    CraftToolData craftToolData = getToolData(toolStack);
    if (craftingLevel < craftToolData.getLevel()) {
      sendMessage(player, plugin.getSettings().getString("language.craft.low-level-tool", ""));
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return;
    }

    float effectiveCraftLevel = data.getLevelWithBonus();
    double levelAdvantage = Math.max(0, craftingLevel - itemLevel);
    float effectiveLevelAdvantage = Math.max(0f, effectiveCraftLevel - itemLevel);

    List<String> lore = TextUtils.getLore(selectedStack);
    List<String> possibleStats = CraftingUtil.getPossibleStats(lore);

    ItemStack materialDetectionItem = selectedStack.clone();
    materialDetectionItem.setDurability((short) 0);
    Material material = plugin.getCraftMaterialManager().getMaterial(materialDetectionItem);
    if (material == null) {
      sendMessage(player, plugin.getSettings().getString("language.craft.no-materials", ""));
      return;
    }

    int equipmentQuality = MaterialUtil.getQuality(selectedStack);
    int quality = Math.max(1, equipmentQuality - 1);

    float qualityUpChance = CraftingUtil.getQualityUpChance(effectiveLevelAdvantage, craftToolData);
    while (LootPlugin.RNG.nextFloat() < qualityUpChance && quality < 4) {
      quality++;
    }

    ItemStack craftMaterial = MaterialUtil.buildMaterial(material, plugin.getCraftMaterialManager()
        .getCraftMaterials().get(material), itemLevel, quality);

    player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 0.8F);

    ItemStack essence = null;
    float essenceChance = (effectiveLevelAdvantage / 100) + craftToolData.getQuality() * 0.2f;
    if (LootPlugin.RNG.nextFloat() < essenceChance) {
      if (!possibleStats.isEmpty()) {
        player.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4F, 2F);
        Tier tier = MaterialUtil.getTierFromStack(selectedStack);
        essence = buildEssence(tier, itemLevel, possibleStats);
      }
    }

    selectedStack.setAmount(selectedStack.getAmount() - 1);
    toolStack.setAmount(toolStack.getAmount() - 1);

    ItemUtils.giveOrDrop(player, craftMaterial);
    if (essence != null) {
      ItemUtils.giveOrDrop(player, essence);
    }

    double exp = 5 + itemLevel * 1.1;
    plugin.getStrifePlugin().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.CRAFTING, exp, false, false);
  }

  public CraftToolData getToolData(ItemStack stack) {
    if (stack == null) {
      return null;
    }
    String stackName = ItemStackExtensionsKt.getDisplayName(stack);
    if (stackName == null) {
      return null;
    }
    if (!stackName.endsWith("Craftsman's Tools") && !stackName.endsWith("Crafting Tools")) {
      return null;
    }
    CraftToolData data = new CraftToolData();
    data.setLevel(getToolLevel(stack));
    data.setQuality(MaterialUtil.getQuality(stack));

    return data;
  }

  public int getToolLevel(ItemStack stack) {
    if (stack.getItemMeta() == null) {
      return 0;
    }
    if (TextUtils.getLore(stack).get(0) == null) {
      return 0;
    }
    String lvlReqString = ChatColor.stripColor(TextUtils.getLore(stack).get(0));
    if (!lvlReqString.startsWith("Craft Skill Requirement:")) {
      return 0;
    }
    return MaterialUtil.getDigit(TextUtils.getLore(stack).get(0));
  }



}
