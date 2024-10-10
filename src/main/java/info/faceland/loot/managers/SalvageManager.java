package info.faceland.loot.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static info.faceland.loot.utils.MaterialUtil.getLevelRequirement;

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.items.CraftMaterial;
import info.faceland.loot.items.NewCustomItem;
import info.faceland.loot.items.Tier;
import info.faceland.loot.items.prefabs.DistortionCell;
import info.faceland.loot.utils.CraftingUtil;
import info.faceland.loot.utils.EssenceUtil;
import info.faceland.loot.utils.MaterialUtil;
import info.faceland.loot.utils.PersistHelper;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SalvageManager {

  private final LootPlugin plugin;

  public SalvageManager(LootPlugin plugin) {
    this.plugin = plugin;
  }

  public ItemStack destroy(Player player, ItemStack selectedStack, ItemStack toolStack) {
    if (selectedStack == null || toolStack == null || selectedStack.getAmount() < 1 || toolStack.getAmount() < 1) {
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return null;
    }
    int itemLevel = getLevelRequirement(selectedStack);
    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.CRAFTING, true);

    SkillRank rank = SkillRank.getRank(plugin.getStrifePlugin().getChampionManager().getChampion(player), LifeSkillType.CRAFTING);
    int maxSalvageLevel = CraftingUtil.getMaxActionLevel(rank);
    int craftingLevel = data.getLevel();

    if (itemLevel > maxSalvageLevel) {
      sendMessage(player, plugin.getSettings().getString("language.craft.low-level", ""));
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return null;
    }

    NewCustomItem tool = PersistHelper.getNonEquipmentUniqueItem(toolStack);
    if (tool == null) {
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return null;
    }
    if (craftingLevel < tool.getItemLevel()) {
      sendMessage(player, plugin.getSettings().getString("language.craft.low-level-tool", ""));
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return null;
    }

    float effectiveCraftLevel = data.getLevelWithBonus();
    // double levelAdvantage = Math.max(0, craftingLevel - itemLevel);
    float effectiveLevelAdvantage = Math.max(0f, effectiveCraftLevel - itemLevel);

    ItemStack materialDetectionItem = selectedStack.clone();
    materialDetectionItem.setDurability((short) 0);
    var equipmentData = PersistHelper.getEquipmentData(selectedStack);
    var tier = equipmentData.getTier();
    Material material = tier.getCraftMaterials().get(LootPlugin.RNG.nextInt(tier.getCraftMaterials().size()));
    if (material == null) {
      sendMessage(player, plugin.getSettings().getString("language.craft.no-materials", ""));
      return null;
    }

    double exp = 5;
    int rarity = (int) MaterialUtil.getRarity(selectedStack).getPower();
    float rarityUpChance = CraftingUtil.getRarityUpChance(effectiveLevelAdvantage, tool.getQuality());
    while (LootPlugin.RNG.nextFloat() < rarityUpChance && rarity < 3) {
      rarity++;
    }

    if (equipmentData.isDistorted() && LootPlugin.RNG.nextDouble() < 0.005) {
      try {
        player.playSound(player.getEyeLocation(), Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1F, 2.0F);
        exp += 1000;
        ItemUtils.giveOrDrop(player, DistortionCell.item());
      } catch (Exception ignored) {}
    }

    ItemStack craftMaterial = CraftMaterial.get(material, itemLevel, rarity).item();
    player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 0.8F);

    float essenceChance = (effectiveLevelAdvantage / 100) + (tool.getItemLevel() - 1) * 0.2f;
    if (LootPlugin.RNG.nextFloat() < essenceChance) {
      if (EssenceUtil.canExtractStat(selectedStack, true)) {
        player.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4F, 2F);
        EssenceUtil.extract(player, selectedStack, false, true, true);
      }
    }

    selectedStack.setAmount(selectedStack.getAmount() - 1);
    toolStack.setAmount(toolStack.getAmount() - 1);

    ItemUtils.giveOrDrop(player, craftMaterial);

    exp += itemLevel * 1.1;

    plugin.getStrifePlugin().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.CRAFTING, exp, false, false);
    return craftMaterial.clone();
  }
  
}
