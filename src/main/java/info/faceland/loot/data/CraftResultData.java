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
  private final double totalQuality;
  private final double totalItemLevel;
  private final double itemLevel;
  private final float quality;

  public CraftResultData(ItemStack[] craftMatrix, ItemStack resultStack) {
    int numMaterials = 0;
    double totalQuality = 0;
    double totalItemLevel = 0;
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
        totalItemLevel += 0.5;
      }
      numMaterials++;
      totalQuality += MaterialUtil.getQuality(loopItem);
    }

    this.numMaterials = numMaterials;
    this.totalQuality = totalQuality;
    this.totalItemLevel = totalItemLevel;
    this.itemLevel = totalItemLevel / numMaterials;
    this.quality = (float) (totalQuality / numMaterials);
  }

  public float openSlotChance(float levelAdvantage) {
    return (quality * 0.05f) + Math.min(0.8f, levelAdvantage / 100);
  }
}
