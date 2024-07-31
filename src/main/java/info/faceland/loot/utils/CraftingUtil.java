package info.faceland.loot.utils;

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
    return Math.max(minRarity, Math.round(avgCraftRarity) - 1);
  }

  public static float getRarityUpChance(double levelAdvantage, int quality) {
    float chance = (float) (levelAdvantage / 100);
    float rarityStep = (1 - chance) / 10;
    chance += rarityStep * (quality - 1);
    return chance;
  }

}
