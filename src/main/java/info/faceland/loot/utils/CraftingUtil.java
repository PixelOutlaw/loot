package info.faceland.loot.utils;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import info.faceland.loot.data.CraftToolData;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import land.face.strife.data.champion.SkillRank;
import org.bukkit.ChatColor;

public class CraftingUtil {

  private static final Pattern hexPattern = Pattern.compile("§x(§[A-Fa-f0-9]){6}");

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

  public static float getRarityUpChance(double levelAdvantage, CraftToolData craftToolData) {
    float chance = (float) (levelAdvantage / 100);
    float rarityStep = (1 - chance) / 10;
    chance += rarityStep * craftToolData.getRarity();
    return chance;
  }

  public static boolean isValidStealColor(Color color) {
    float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    return hsb[0] >= 0.11 && hsb[0] <= 0.34 && hsb[1] >= 0.7 && hsb[1] <= 0.83 && hsb[2] > 0.905 && hsb[2] < 1.001;
  }

  public static net.md_5.bungee.api.ChatColor getHexFromString(String message) {
    Matcher matcher = hexPattern.matcher(message);
    if (matcher.find()) {
      String str = "#" + matcher.group().replace("§x", "").replace("§", "");
      return net.md_5.bungee.api.ChatColor.of(str);
    }
    return null;
  }

  public static List<String> getPossibleStats(List<String> lore) {
    List<String> possibleStats = new ArrayList<>();
    for (String s : lore) {
      if (!ChatColor.stripColor(s).startsWith("+")) {
        continue;
      }
      net.md_5.bungee.api.ChatColor color = CraftingUtil.getHexFromString(s);
      if (color != null) {
        if (CraftingUtil.isValidStealColor(color.getColor())) {
          possibleStats.add(s);
        }
        continue;
      }
      if (s.startsWith(ChatColor.GREEN + "") || s.startsWith(ChatColor.YELLOW + "")) {
        possibleStats.add(s);
      } else if (s.startsWith(FaceColor.LIGHT_GREEN.s()) || s.startsWith(FaceColor.YELLOW.s())) {
        possibleStats.add(s);
      }
    }
    return possibleStats;
  }

}
