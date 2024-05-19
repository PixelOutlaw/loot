package info.faceland.loot.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.loot.utils.MaterialUtil;
import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Data
public class CraftResultData {

  private final int numMaterials;
  private final float totalRarity;
  private final float totalItemLevel;
  private final float itemLevel;
  private final float rarity;

  public CraftResultData(ItemStack[] craftMatrix, ItemStack resultStack) {
    int numMaterials = 0;
    float totalRarity = 0;
    float totalItemLevel = 0;
    for (ItemStack is : craftMatrix) {
      if (is == null || is.getType() == Material.AIR || is.getType() == resultStack.getType()) {
        continue;
      }
      ItemStack loopItem = new ItemStack(is);
      if (MaterialUtil.hasItemLevel(loopItem)) {
        int iLevel = NumberUtils.toInt(CharMatcher.digit().or(CharMatcher.is('-')).negate()
            .collapseFrom(ChatColor.stripColor(TextUtils.getLore(loopItem).get(0)), ' ')
            .trim());
        totalItemLevel += iLevel;
      } else {
        totalItemLevel += 0.5F;
      }
      numMaterials++;
      totalRarity += MaterialUtil.getRarity(loopItem);
    }

    this.numMaterials = numMaterials;
    this.totalRarity = totalRarity;
    this.totalItemLevel = totalItemLevel;
    this.itemLevel = totalItemLevel / numMaterials;
    this.rarity = totalRarity / numMaterials;
  }

  public float openSlotChance(float levelAdvantage) {
    return (rarity * 0.065f) + Math.min(0.8f, levelAdvantage / 100);
  }
}
