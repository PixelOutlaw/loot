package info.faceland.loot.utils;

import info.faceland.loot.LootPlugin;
import land.face.strife.data.champion.SkillRank;

public class CraftingUtil {

  public static int getMaxActionLevel(SkillRank rank) {
    return switch (rank) {
      case NOVICE -> 25;
      case APPRENTICE -> 45;
      case JOURNEYMAN -> 65;
      case EXPERT -> 85;
      case MASTER -> 100;
    };
  }

  public static float getMinimumRarity(float avgCraftRarity, SkillRank rank, float levelAdvantage) {
    float minRarity = 0;
    if (SkillRank.isRank(rank, SkillRank.JOURNEYMAN) || levelAdvantage >= 30) {
      minRarity++;
    }
    return Math.max(minRarity, Math.round(avgCraftRarity) - 2);
  }

  public static int getStarBonus(double levelAdvantage, int resultStars, int toolStars) {
    int bonus = (LootPlugin.RNG.nextDouble() < levelAdvantage / 200f) ? 1 : 0;
    int divisor = resultStars + 1;
    return switch (toolStars) {
      case 1 -> bonus + (LootPlugin.RNG.nextDouble() < (0.15f / divisor) ? 1 : 0);
      case 2 -> bonus + (LootPlugin.RNG.nextDouble() < (0.25f / divisor) ? 1 : 0);
      case 3 -> bonus + (LootPlugin.RNG.nextDouble() < (0.35f / divisor) ? 1 : 0);
      case 4 -> {
        if (LootPlugin.RNG.nextDouble() < 0.25f) {
          bonus++;
        }
        yield bonus + (LootPlugin.RNG.nextDouble() < (0.45f / divisor) ? 1 : 0);
      }
      case 5 -> bonus + 1 + (LootPlugin.RNG.nextDouble() < (0.25f / divisor) ? 1 : 0);
      default -> bonus;
    };
  }

}
