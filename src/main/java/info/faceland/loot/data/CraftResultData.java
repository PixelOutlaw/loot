package info.faceland.loot.data;

import info.faceland.loot.items.CraftMaterial;
import info.faceland.loot.utils.MaterialUtil;
import info.faceland.loot.utils.ModifyEquipmentUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class CraftResultData {

  private final int numMaterials;
  private final float totalRarity;
  private final float totalItemLevel;
  private final float itemLevel;
  private final float rarity;

  public CraftResultData(ItemStack[] craftMatrix, ItemStack resultStack) {
    int numMaterials = 0;
    float totalStars = 0;
    float totalItemLevel = 0;
    for (ItemStack is : craftMatrix) {
      if (is == null || is.getType() == Material.AIR || is.getType() == resultStack.getType()) {
        continue;
      }
      ItemStack loopItem = new ItemStack(is);
      if (loopItem == null || loopItem.getType() == Material.AIR) {
        totalItemLevel += 1;
        totalStars += 1;
      } else {
        CraftMaterial cr = CraftMaterial.get(loopItem);
        if (cr == null) {
          totalItemLevel += 1;
          totalStars += 1;
        } else {
          totalItemLevel += cr.getItemLevel();
          totalStars += cr.getQuality();
        }
      }
      numMaterials++;
    }

    this.numMaterials = numMaterials;
    this.totalRarity = totalStars;
    this.totalItemLevel = totalItemLevel;
    this.itemLevel = totalItemLevel / numMaterials;
    this.rarity = totalStars / numMaterials;
  }

  public float openSlotChance(float levelAdvantage) {
    return (rarity * 0.065f) + Math.min(0.8f, levelAdvantage / 100);
  }
}
