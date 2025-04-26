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
package info.faceland.loot.menu.gemcutter;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.items.CraftMaterial;
import info.faceland.loot.menu.TransparentIcon;
import info.faceland.loot.utils.CraftingUtil;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.util.PlayerDataUtil;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GemcutterMenu extends ItemMenu {

  private final Map<Player, ItemStack> selectedStacks = new WeakHashMap<>();

  public GemcutterMenu(LootPlugin plugin) {
    super(TextUtils.color(plugin.getSettings().getString("language.menu.gemcutter-name",
        "&0&lGemcutter")), Size.fit(18), plugin);

    setItem(2, new SelectedItemIcon(this));
    setItem(6, new ResultIcon(this));

    setItem(12, new CutConfirmIcon(this));
    setItem(13, new CutConfirmIcon(this));
    setItem(14, new CutConfirmIcon(this));

    setItem(16, new TransparentIcon(FaceColor.BROWN + "I made a crazy risk, a gamble, and it's about to pay off"));
  }

  public ItemStack getSelectedStack(Player player) {
    return selectedStacks.get(player);
  }

  public void setSelectedItem(Player player, ItemStack item) {
    if (item == null || item.getType() != Material.CHARCOAL) {
      selectedStacks.put(player, null);
      update(player);
      return;
    }
    selectedStacks.put(player, item);
    update(player);
  }

  void doCut(Player player) {
    ItemStack selectedStack = selectedStacks.get(player);
    if (selectedStack == null || selectedStack.getAmount() < 1) {
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return;
    }
    SkillRank rank = SkillRank.getRank(LootPlugin.getInstance()
        .getStrifePlugin().getChampionManager().getChampion(player), LifeSkillType.CRAFTING);
    int maxLvl = CraftingUtil.getMaxActionLevel(rank);
    CraftMaterial selectedMaterial = CraftMaterial.get(selectedStack);
    if (selectedMaterial == null || selectedMaterial.getGemResult() == null) {
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return;
    }
    int itemLevel = selectedMaterial.getItemLevel();
    if (itemLevel > maxLvl) {
      MessageUtils.sendMessage(player, "&e[!] You need &fCrafting " + maxLvl + " &eto do this!");
      player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1, 0.8f);
      return;
    }
    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.CRAFTING, true);
    float effectiveCraftLevel = data.getLevelWithBonus();
    
    Material material = switch (selectedMaterial.getGemResult()) {
      case "ruby" -> Material.RED_DYE;
      case "emerald" -> Material.EMERALD;
      case "sapphire" -> Material.LAPIS_LAZULI;
      case "topaz" -> Material.ORANGE_DYE;
      case "onyx" -> Material.BLACK_DYE;
      case "amethyst" -> Material.PURPLE_DYE;
      case "opal" -> Material.PINK_DYE;
      default -> Material.DIAMOND;
    };

    float upgradeChance = 0.15f * ((effectiveCraftLevel + 100) / (itemLevel + 100));
    int stars = 1;
    while (stars < 4 && LootPlugin.RNG.nextFloat() < upgradeChance) {
      stars++;
    }
    ItemStack result = CraftMaterial.get(material, itemLevel, stars).item();
    ItemUtils.giveOrDrop(player, result);
    selectedStack.setAmount(selectedStack.getAmount() - 1);
    StrifePlugin.getInstance().getSkillExperienceManager().addExperience(player,
        LifeSkillType.CRAFTING, 10 + itemLevel * 3, false, false);
    update(player);
    player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1, 1.4f);
  }

}

/*
00 01 02 03 04 05 06 07 08
09 10 11 12 13 14 15 16 17
18 19 20 21 22 23 24 25 26
27 28 29 30 31 32 33 34 35
36 37 38 39 40 41 42 43 44
45 46 47 48 49 50 51 52 53
*/
