/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.listeners.crafting;

import static info.faceland.loot.items.Tier.TIER_BY_RESULT;

import com.google.common.base.Strings;
import com.sentropic.guiapi.gui.GUI;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.CraftResultData;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.Tier;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

public final class PreCraftEquipmentListener implements Listener {

  private final LootPlugin plugin;
  private final ItemMeta invalidItemMeta;

  private static final String STAR = "峰";
  private static final String HALF_STAR = "峱";
  private static final String EMPTY_STAR = "峲";
  private static final String ANTISTAR = "峵";
  private static final String HALF_ANTISTAR_LEFT = "島";
  private static final String HALF_ANTISTAR_RIGHT = "峷";
  private static final String RARITY_IND = "峻";

  private static final String STARS_0_POINT = Strings.repeat(STAR, 0) +
      Strings.repeat(HALF_STAR, 0) + Strings.repeat(EMPTY_STAR, 5);
  private static final String STARS_1_POINT = Strings.repeat(STAR, 0) +
      Strings.repeat(HALF_STAR, 1) + Strings.repeat(EMPTY_STAR, 4);
  private static final String STARS_2_POINT = Strings.repeat(STAR, 1) +
      Strings.repeat(HALF_STAR, 0) + Strings.repeat(EMPTY_STAR, 4);
  private static final String STARS_3_POINT = Strings.repeat(STAR, 1) +
      Strings.repeat(HALF_STAR, 1) + Strings.repeat(EMPTY_STAR, 3);
  private static final String STARS_4_POINT = Strings.repeat(STAR, 2) +
      Strings.repeat(HALF_STAR, 0) + Strings.repeat(EMPTY_STAR, 3);
  private static final String STARS_5_POINT = Strings.repeat(STAR, 2) +
      Strings.repeat(HALF_STAR, 1) + Strings.repeat(EMPTY_STAR, 2);
  private static final String STARS_6_POINT = Strings.repeat(STAR, 3) +
      Strings.repeat(HALF_STAR, 0) + Strings.repeat(EMPTY_STAR, 2);
  private static final String STARS_7_POINT = Strings.repeat(STAR, 3) +
      Strings.repeat(HALF_STAR, 1) + Strings.repeat(EMPTY_STAR, 1);
  private static final String STARS_8_POINT = Strings.repeat(STAR, 4) +
      Strings.repeat(HALF_STAR, 0) + Strings.repeat(EMPTY_STAR, 1);
  private static final String STARS_9_POINT = Strings.repeat(STAR, 4) +
      Strings.repeat(HALF_STAR, 1) + Strings.repeat(EMPTY_STAR, 0);
  private static final String STARS_10_POINT = Strings.repeat(STAR, 5) +
      Strings.repeat(HALF_STAR, 0) + Strings.repeat(EMPTY_STAR, 0);

  private final Map<Integer, String> totalStarBar = buildStars();
  private final Map<Integer, String> qualityStarBar = buildQualityStars();
  private final Map<Integer, String> negStars = buildNegStars();

  public PreCraftEquipmentListener(LootPlugin plugin) {
    this.plugin = plugin;

    ItemStack failStack = new ItemStack(Material.BARRIER);
    ItemStackExtensionsKt.setDisplayName(failStack, FaceColor.RED + "Invalid Item!");

    invalidItemMeta = failStack.getItemMeta().clone();
    invalidItemMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.menu.invalid-item")));
  }

  @EventHandler
  public void onPreCraftEquipment(PrepareItemCraftEvent event) {
    if (event.getRecipe() != null) {
      String name = ItemStackExtensionsKt.getDisplayName(event.getRecipe().getResult());
      if (StringUtils.isNotBlank(name) && ChatColor.stripColor(name).contains("Equipment Crafting")) {
        updateEquipmentCraftInvy(event.getInventory(), event.getRecipe(), (Player) event.getViewers().getFirst());
      }
    }
  }

  private void updateEquipmentCraftInvy(CraftingInventory craftingInventory, Recipe recipe, Player player) {

    for (ItemStack stack : craftingInventory.getMatrix()) {
      if (stack == null) {
        continue;
      }
      String name = ItemStackExtensionsKt.getDisplayName(stack);
      if (StringUtils.isBlank(name)) {
        continue;
      }
      if (SocketGem.get(stack) != null || EnchantmentTome.get(stack) != null || UpgradeScroll.get(stack) != null) {
        craftingInventory.getResult().setType(Material.BARRIER);
        craftingInventory.getResult().setItemMeta(invalidItemMeta);
        return;
      }
    }

    ItemStack result = recipe.getResult().clone();

    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.CRAFTING, true);
    CraftResultData crData = new CraftResultData(craftingInventory.getMatrix());
    int itemLevel = crData.getItemLevel() == -1 ? player.getLevel() : (int) crData.getItemLevel();

    // TODO: more betterer
    Tier tier = null;
    for (var entry : TIER_BY_RESULT.entrySet()) {
      if (entry.getKey().isSimilar(recipe.getResult())) {
        tier = entry.getValue();
        break;
      }
    }
    if (tier == null) {
      craftingInventory.getResult().setType(Material.BARRIER);
      //ItemStackExtensionsKt.setCustomModelData(craftingInventory.getResult(), 150);
      ItemStackExtensionsKt.setDisplayName(craftingInventory.getResult(),
          FaceColor.RED + FaceColor.BOLD.s() + FaceColor.UNDERLINE.s() + "Something Went Wrong!");
      TextUtils.setLore(craftingInventory.getResult(), List.of(
          FaceColor.GRAY + "The server doesn't know how",
          FaceColor.GRAY + "to craft this item! Please",
          FaceColor.GRAY + "report this to staff!"
      ), false);
      return;
    }

    int minItemLevel = Math.max(1, itemLevel - 3);
    int maxItemLevel = Math.max(1, Math.min(100, itemLevel));

    formatCraftOutput(
        craftingInventory.getResult(),
        data.getLevel(),
        data.getLevelWithBonus(),
        minItemLevel,
        maxItemLevel,
        (int) (crData.getRarity() * 2.0f)
    );
  }

  public void formatCraftOutput(ItemStack stack, int craftLevel, int craftLevelWithBonus, int minLevel, int maxLevel, int qualityStars) {
    int stars;
    int difference = craftLevelWithBonus - minLevel;
    int craftBonusStars = difference / 5;
    if (craftBonusStars >= 0) {
      craftBonusStars += SkillRank.check(SkillRank.JOURNEYMAN, craftLevel) ? 1 : 0;
      craftBonusStars += SkillRank.check(SkillRank.MASTER, craftLevel) ? 1 : 0;
      craftBonusStars = Math.min(10, craftBonusStars);
      stars = Math.min(20, qualityStars + craftBonusStars);
    } else {
      stars = qualityStars;
    }
    craftBonusStars = Math.clamp(craftBonusStars, -19, 10);
    int minRarity, maxRarity;
    String minRarityArrow, maxRarityArrow;
    if (craftBonusStars >= 0) {
      if (stars <= 4) {
        minRarity = 1;
      } else if (stars <= 8) {
        minRarity = 2;
      } else if (stars <= 16) {
        minRarity = 3;
      } else {
        minRarity = 4;
      }
      maxRarity = 4;
      minRarityArrow = getRaritySpacingArrow(20);
      maxRarityArrow = getRaritySpacingArrow(stars);
    } else {
      if (qualityStars <= 4) {
        minRarity = 1;
      } else if (qualityStars <= 8) {
        minRarity = 2;
      } else if (qualityStars <= 16) {
        minRarity = 3;
      } else {
        minRarity = 4;
      }
      if (craftBonusStars <= -16) {
        maxRarity = 1;
      } else if (craftBonusStars <= -12) {
        maxRarity = 2;
      } else if (craftBonusStars <= -4) {
        maxRarity = 3;
      } else {
        maxRarity = 4;
      }
      if (minRarity > maxRarity) {
        minRarity = maxRarity;
        minRarityArrow = getRaritySpacingArrow(20 + craftBonusStars);
        maxRarityArrow = getRaritySpacingArrow(20 + craftBonusStars);
      } else {
        minRarityArrow = getRaritySpacingArrow(qualityStars);
        maxRarityArrow = getRaritySpacingArrow(20 + craftBonusStars);
      }
    }

    String minRarityStr = switch (minRarity) {
      case 1 -> FaceColor.TRUE_WHITE + MaterialUtil.TAG_COMMON;
      case 2 -> FaceColor.TRUE_WHITE + MaterialUtil.TAG_UNCOMMON;
      case 3 -> FaceColor.TRUE_WHITE + MaterialUtil.TAG_RARE;
      default -> FaceColor.TRUE_WHITE + MaterialUtil.TAG_EPIC;
    };
    String maxRarityStr = switch (maxRarity) {
      case 1 -> FaceColor.TRUE_WHITE + MaterialUtil.TAG_COMMON;
      case 2 -> FaceColor.TRUE_WHITE + MaterialUtil.TAG_UNCOMMON;
      case 3 -> FaceColor.TRUE_WHITE + MaterialUtil.TAG_RARE;
      default -> FaceColor.TRUE_WHITE + MaterialUtil.TAG_EPIC;
    };

    List<String> lore = new ArrayList<>();
    int diff = craftLevelWithBonus - craftLevel;
    String bonus = (diff == 0) ? "" : FaceColor.LIGHT_GREEN + "(+" + diff + ")";

    lore.add("");
    lore.add(FaceColor.WHITE + "Item Level: " + minLevel + "~" + maxLevel);
    lore.add(FaceColor.WHITE + "Crafting Skill: " + craftLevelWithBonus + bonus);
    lore.add("");
    lore.add(FaceColor.YELLOW + "Average Material Quality:");
    lore.add(FaceColor.TRUE_WHITE + qualityStarBar.get(Math.min(10, qualityStars)));
    if (craftBonusStars != 0) {
      lore.add("");
      lore.add((craftBonusStars < 0 ? FaceColor.RED + "Underleveled Penalty:" : FaceColor.YELLOW + "Overleveled Bonus:"));
      lore.add(FaceColor.TRUE_WHITE + (craftBonusStars < 0 ? totalStarBar.get(craftBonusStars) : qualityStarBar.get(Math.min(10, craftBonusStars))));
    }
    lore.add("");
    lore.add(FaceColor.YELLOW + "Final Craft Quality:");
    lore.add(FaceColor.TRUE_WHITE + totalStarBar.get(stars) + (craftBonusStars < 0 ? negStars.get(-craftBonusStars) : "") + GUI.spacesOf(-2));
    lore.add(FaceColor.TRUE_WHITE + GUI.spacesOf(-1) + "峺" + minRarityArrow + maxRarityArrow + GUI.spacesOf(-2));
    lore.add("");
    if (minRarity == maxRarity) {
      lore.add(FaceColor.WHITE + "Rarity: " + maxRarityStr);
    } else {
      lore.add(FaceColor.WHITE + "Rarity: " + minRarityStr + FaceColor.LIGHT_GRAY + " to " + maxRarityStr);
    }
    Tier t = TIER_BY_RESULT.get(stack);
    if (t != null) {
      lore.add("");
      if (t.getPrimaryStats().size() > 1) {
        lore.add(FaceColor.LIGHT_GRAY + "Core Stat: [Random Element] Damage");
      } else {
        lore.add(FaceColor.LIGHT_GRAY + "Core Stat: " + t.getPrimaryStats().getFirst().getStat().getName());
      }
      t.getSecondaryStats().forEach(s -> lore.add(FaceColor.GRAY + " and " + s.getStat().getName() + "?"));
    }

    if (!SkillRank.check(SkillRank.APPRENTICE, craftLevel)) {
      float essMult = SkillRank.check(SkillRank.EXPERT, craftLevel) ? 2.0f : 1.0f;
      lore.add(" ");
      float emptyStatSlot = 0.04f;
      emptyStatSlot += 0.002f * qualityStars;
      if (craftBonusStars >= 0) {
        emptyStatSlot += (0.02f / 10f) * craftBonusStars * essMult;
      } else {
        emptyStatSlot /= -craftBonusStars;
      }
      emptyStatSlot = Math.max(0, emptyStatSlot);
      lore.add(FaceColor.CYAN + "Empty Stat Slot Chance: " + (int) (emptyStatSlot * 100) + "%");
      lore.add(FaceColor.LIGHT_GRAY + "( Rolled for every stat )");
    } else {
      lore.add(" ");
      lore.add(FaceColor.LIGHT_GRAY + "Quantity of stats and");
      lore.add(FaceColor.LIGHT_GRAY + "sockets is determined by");
      lore.add(FaceColor.LIGHT_GRAY + "the final item's rarity!");
    }
    if (craftBonusStars <= -4) {
      lore.add(" ");
      lore.add(FaceColor.CRIMSON + "Warning! Your craft level is");
      lore.add(FaceColor.CRIMSON + "too low! The quality of this");
      lore.add(FaceColor.CRIMSON + "craft is reduced!");
      lore.add(" ");
      lore.add(FaceColor.YELLOW + ">>      Click To Craft!      <<");
    } else {
      lore.add(" ");
      lore.add(FaceColor.LIGHT_GREEN + ">>      Click To Craft!      <<");
    }
    stack.editMeta(m -> {
      m.setDisplayName(FaceColor.ORANGE + FaceColor.BOLD.s() + FaceColor.UNDERLINE + "Equipment Crafting");
      m.setLore(lore);
    });
  }


  private static Map<Integer, String> buildStars() {
    Map<Integer, String> map = new HashMap<>();
    map.put(-20, Strings.repeat(ANTISTAR, 10) + Strings.repeat(HALF_ANTISTAR_LEFT, 0));
    map.put(-19, Strings.repeat(ANTISTAR, 9) + Strings.repeat(HALF_ANTISTAR_LEFT, 1));
    map.put(-18, Strings.repeat(ANTISTAR, 9) + Strings.repeat(HALF_ANTISTAR_LEFT, 0));
    map.put(-17, Strings.repeat(ANTISTAR, 8) + Strings.repeat(HALF_ANTISTAR_LEFT, 1));
    map.put(-16, Strings.repeat(ANTISTAR, 8) + Strings.repeat(HALF_ANTISTAR_LEFT, 0));
    map.put(-15, Strings.repeat(ANTISTAR, 7) + Strings.repeat(HALF_ANTISTAR_LEFT, 1));
    map.put(-14, Strings.repeat(ANTISTAR, 7) + Strings.repeat(HALF_ANTISTAR_LEFT, 0));
    map.put(-13, Strings.repeat(ANTISTAR, 6) + Strings.repeat(HALF_ANTISTAR_LEFT, 1));
    map.put(-12, Strings.repeat(ANTISTAR, 6) + Strings.repeat(HALF_ANTISTAR_LEFT, 0));
    map.put(-11, Strings.repeat(ANTISTAR, 5) + Strings.repeat(HALF_ANTISTAR_LEFT, 1));
    map.put(-10, Strings.repeat(ANTISTAR, 5) + Strings.repeat(HALF_ANTISTAR_LEFT, 0));
    map.put(-9, Strings.repeat(ANTISTAR, 4) + Strings.repeat(HALF_ANTISTAR_LEFT, 1));
    map.put(-8, Strings.repeat(ANTISTAR, 4) + Strings.repeat(HALF_ANTISTAR_LEFT, 0));
    map.put(-7, Strings.repeat(ANTISTAR, 3) + Strings.repeat(HALF_ANTISTAR_LEFT, 1));
    map.put(-6, Strings.repeat(ANTISTAR, 3) + Strings.repeat(HALF_ANTISTAR_LEFT, 0));
    map.put(-5, Strings.repeat(ANTISTAR, 2) + Strings.repeat(HALF_ANTISTAR_LEFT, 1));
    map.put(-4, Strings.repeat(ANTISTAR, 2) + Strings.repeat(HALF_ANTISTAR_LEFT, 0));
    map.put(-3, Strings.repeat(ANTISTAR, 1) + Strings.repeat(HALF_ANTISTAR_LEFT, 1));
    map.put(-2, Strings.repeat(ANTISTAR, 1) + Strings.repeat(HALF_ANTISTAR_LEFT, 0));
    map.put(-1, Strings.repeat(ANTISTAR, 0) + Strings.repeat(HALF_ANTISTAR_LEFT, 1));
    map.put(0, STARS_0_POINT + STARS_0_POINT);
    map.put(1, STARS_1_POINT + STARS_0_POINT);
    map.put(2, STARS_2_POINT + STARS_0_POINT);
    map.put(3, STARS_3_POINT + STARS_0_POINT);
    map.put(4, STARS_4_POINT + STARS_0_POINT);
    map.put(5, STARS_5_POINT + STARS_0_POINT);
    map.put(6, STARS_6_POINT + STARS_0_POINT);
    map.put(7, STARS_7_POINT + STARS_0_POINT);
    map.put(8, STARS_8_POINT + STARS_0_POINT);
    map.put(9, STARS_9_POINT + STARS_0_POINT);
    map.put(10, STARS_10_POINT + STARS_0_POINT);
    map.put(11, STARS_10_POINT + STARS_1_POINT);
    map.put(12, STARS_10_POINT + STARS_2_POINT);
    map.put(13, STARS_10_POINT + STARS_3_POINT);
    map.put(14, STARS_10_POINT + STARS_4_POINT);
    map.put(15, STARS_10_POINT + STARS_5_POINT);
    map.put(16, STARS_10_POINT + STARS_6_POINT);
    map.put(17, STARS_10_POINT + STARS_7_POINT);
    map.put(18, STARS_10_POINT + STARS_8_POINT);
    map.put(19, STARS_10_POINT + STARS_9_POINT);
    map.put(20, STARS_10_POINT + STARS_10_POINT);
    return map;
  }

  private static Map<Integer, String> buildQualityStars() {
    Map<Integer, String> map = new HashMap<>();
    map.put(0, STARS_0_POINT);
    map.put(1, STARS_1_POINT);
    map.put(2, STARS_2_POINT);
    map.put(3, STARS_3_POINT);
    map.put(4, STARS_4_POINT);
    map.put(5, STARS_5_POINT);
    map.put(6, STARS_6_POINT);
    map.put(7, STARS_7_POINT);
    map.put(8, STARS_8_POINT);
    map.put(9, STARS_9_POINT);
    map.put(10, STARS_10_POINT);
    return map;
  }

  private static Map<Integer, String> buildNegStars() {
    Map<Integer, String> map = new HashMap<>();
    map.put(1, GUI.spacesOf(-14) + Strings.repeat(HALF_ANTISTAR_RIGHT, 1) + Strings.repeat(ANTISTAR, 0));
    map.put(2, GUI.spacesOf(-14) + Strings.repeat(HALF_ANTISTAR_RIGHT, 0) + Strings.repeat(ANTISTAR, 1));
    map.put(3, GUI.spacesOf(-28) + Strings.repeat(HALF_ANTISTAR_RIGHT, 1) + Strings.repeat(ANTISTAR, 1));
    map.put(4, GUI.spacesOf(-28) + Strings.repeat(HALF_ANTISTAR_RIGHT, 0) + Strings.repeat(ANTISTAR, 2));
    map.put(5, GUI.spacesOf(-42) + Strings.repeat(HALF_ANTISTAR_RIGHT, 1) + Strings.repeat(ANTISTAR, 2));
    map.put(6, GUI.spacesOf(-42) + Strings.repeat(HALF_ANTISTAR_RIGHT, 0) + Strings.repeat(ANTISTAR, 3));
    map.put(7, GUI.spacesOf(-56) + Strings.repeat(HALF_ANTISTAR_RIGHT, 1) + Strings.repeat(ANTISTAR, 3));
    map.put(8, GUI.spacesOf(-56) + Strings.repeat(HALF_ANTISTAR_RIGHT, 0) + Strings.repeat(ANTISTAR, 4));
    map.put(9, GUI.spacesOf(-70) + Strings.repeat(HALF_ANTISTAR_RIGHT, 1) + Strings.repeat(ANTISTAR, 4));
    map.put(10, GUI.spacesOf(-70) + Strings.repeat(HALF_ANTISTAR_RIGHT, 0) + Strings.repeat(ANTISTAR, 5));
    map.put(11, GUI.spacesOf(-84) + Strings.repeat(HALF_ANTISTAR_RIGHT, 1) + Strings.repeat(ANTISTAR, 5));
    map.put(12, GUI.spacesOf(-84) + Strings.repeat(HALF_ANTISTAR_RIGHT, 0) + Strings.repeat(ANTISTAR, 6));
    map.put(13, GUI.spacesOf(-98) + Strings.repeat(HALF_ANTISTAR_RIGHT, 1) + Strings.repeat(ANTISTAR, 6));
    map.put(14, GUI.spacesOf(-98) + Strings.repeat(HALF_ANTISTAR_RIGHT, 0) + Strings.repeat(ANTISTAR, 7));
    map.put(15, GUI.spacesOf(-112) + Strings.repeat(HALF_ANTISTAR_RIGHT, 1) + Strings.repeat(ANTISTAR, 7));
    map.put(16, GUI.spacesOf(-112) + Strings.repeat(HALF_ANTISTAR_RIGHT, 0) + Strings.repeat(ANTISTAR, 8));
    map.put(17, GUI.spacesOf(-126) + Strings.repeat(HALF_ANTISTAR_RIGHT, 1) + Strings.repeat(ANTISTAR, 8));
    map.put(18, GUI.spacesOf(-126) + Strings.repeat(HALF_ANTISTAR_RIGHT, 0) + Strings.repeat(ANTISTAR, 9));
    map.put(19, GUI.spacesOf(-140) + Strings.repeat(HALF_ANTISTAR_RIGHT, 1) + Strings.repeat(ANTISTAR, 9));
    map.put(20, GUI.spacesOf(-140) + Strings.repeat(HALF_ANTISTAR_RIGHT, 0) + Strings.repeat(ANTISTAR, 10));
    return map;
  }

  private static String getRaritySpacingArrow(int placement) {
    int baseSpacing = -11;
    int spaceVal = (20 - placement) / 2;
    int totalOffset = baseSpacing - spaceVal * 14;
    return GUI.spacesOf(totalOffset) + RARITY_IND + GUI.spacesOf(-totalOffset - 6);
  }
}
