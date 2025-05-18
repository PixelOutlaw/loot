package info.faceland.loot.data;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.items.CraftMaterial;
import java.util.HashSet;
import java.util.Set;
import land.face.strife.data.champion.SkillRank;
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
  private final Set<String> preferredStats;

  public CraftResultData(ItemStack[] craftMatrix) {
    int numMaterials = 0;
    int xLevelMats = 0;
    float totalStars = 0;
    float totalItemLevel = 0;
    this.preferredStats = new HashSet<>();
    for (ItemStack is : craftMatrix) {
      if (is == null || is.getType() == Material.AIR) {
        continue;
      }
      CraftMaterial cr = CraftMaterial.get(is);
      if (cr == null) {
        totalItemLevel += 1;
        totalStars += 1;
      } else {
        if (cr.getItemLevel() == -1) {
          xLevelMats++;
        } else {
          totalItemLevel += cr.getItemLevel();
        }
        totalStars += cr.getQuality();
        preferredStats.addAll(cr.getPreferredStats());
      }
      numMaterials++;
    }
    if (xLevelMats == numMaterials) {
      this.numMaterials = numMaterials;
      this.totalRarity = totalStars;
      this.totalItemLevel = -1;
      this.itemLevel = -1;
      this.rarity = totalStars / numMaterials;
    } else {
      float avgItemLevel = totalItemLevel / (numMaterials - xLevelMats);
      this.numMaterials = numMaterials;
      this.totalRarity = totalStars;
      this.totalItemLevel = totalItemLevel + (avgItemLevel * xLevelMats);
      this.itemLevel = avgItemLevel;
      this.rarity = totalStars / numMaterials;
    }
  }

  public int highRateEmptySlots(double rarityScore, float levelAdvantage, boolean estimate) {
    int amount = 0;
    if (estimate) {
      if (levelAdvantage > 75) {
        amount++;
      }
      if (levelAdvantage > 35) {
        amount++;
      }
    } else {
      if (levelAdvantage / 100 > LootPlugin.RNG.nextDouble()) {
        amount++;
      }
      if (levelAdvantage / 50 > LootPlugin.RNG.nextDouble()) {
        amount++;
      }
    }
    if (rarityScore >= 2) {
      amount++;
    }
    if (rarityScore >= 3) {
      amount++;
    }
    return amount;
  }
}
