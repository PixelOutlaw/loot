/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.menu.salvage;

import static info.faceland.loot.utils.MaterialUtil.getLevelRequirement;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.items.NewCustomItem;
import info.faceland.loot.items.Tier;
import info.faceland.loot.utils.CraftingUtil;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.util.PlayerDataUtil;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class SalvageConfirmIcon extends MenuItem {

  private final SalvageMenu menu;

  SalvageConfirmIcon(SalvageMenu menu) {
    super(TextUtils.color("&cClick To Destroy!"), ItemUtils.BLANK.clone());
    this.menu = menu;
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    ItemStack stack = getIcon().clone();
    if (menu.getEquipment(player) == null || menu.getEquipment(player).getAmount() < 1 ||
        menu.getTool(player) == null || menu.getTool(player).getAmount() < 1) {
      ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color("&6Select BOTH a tool and equipment!"));
      return stack;
    }

    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.CRAFTING, true);
    int itemLevel = getLevelRequirement(menu.getEquipment(player));
    int craftingLevel = data.getLevel();
    float effectiveCraftLevel = data.getLevelWithBonus();
    // double levelAdvantage = Math.max(0, craftingLevel - itemLevel);
    float effectiveLevelAdvantage = Math.max(0f, effectiveCraftLevel - itemLevel);

    SkillRank rank = SkillRank.getRank(LootPlugin.getInstance().getStrifePlugin()
        .getChampionManager().getChampion(player), LifeSkillType.CRAFTING);
    int maxSalvageLevel = CraftingUtil.getMaxActionLevel(rank);

    List<String> lore = new ArrayList<>();
    if (itemLevel > maxSalvageLevel) {
      ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color("&e&lCraft Level Too Low!"));
      lore.add(FaceColor.LIGHT_GRAY + "You can salvage items up to level " + maxSalvageLevel);
      TextUtils.setLore(stack, lore, false);
      return stack;
    }

    NewCustomItem tool = PersistHelper.getNonEquipmentUniqueItem(menu.getTool(player));
    if (tool == null || !tool.getId().contains("crafttool")) {
      ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color("&e&lInvalid tool!?"));
      lore.add(FaceColor.LIGHT_GRAY + "How did you get that in there, huh??");
      TextUtils.setLore(stack, lore, false);
      return stack;
    }

    Tier tier = PersistHelper.getEquipmentData(menu.getEquipment(player)).getTier();
    var mats = new ArrayList<>(tier.getCraftMaterials());
    Collections.shuffle(mats);
    Material material = mats.getFirst();
    if (material == null) {
      ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color("&e&lNo Known Materials"));
      lore.add(FaceColor.LIGHT_GRAY + "It appears as if this can't be salvaged...");
      TextUtils.setLore(stack, lore, false);
      return stack;
    }

    float essenceChance = (effectiveLevelAdvantage / 100) + (tool.getQuality() - 1) * 0.2f;

    ItemStackExtensionsKt.setDisplayName(stack, TextUtils.color("&a&lClick To Salvage!"));

    int levelDiff = (int) (effectiveCraftLevel - craftingLevel);
    if (levelDiff > 0) {
      lore.add(FaceColor.WHITE + "Crafting Skill: " + (int) effectiveCraftLevel +
          FaceColor.YELLOW + " (+" + levelDiff + ")");
    } else {
      lore.add(FaceColor.WHITE + "Crafting Skill: " + craftingLevel);
    }
    lore.add(FaceColor.WHITE + "Material Level: ~" + itemLevel);
    lore.add("");
    lore.add(FaceColor.LIGHT_GRAY + "Destroy this item to get raw");
    lore.add(FaceColor.LIGHT_GRAY + "materials and maybe an essence!");
    lore.add("");
    EquipmentData equipmentData = PersistHelper.getEquipmentData(menu.getEquipment(player));
    var stats = equipmentData.getExtractableStats();
    if (stats.isEmpty()) {
      lore.add(FaceColor.CYAN + "Essence Chance: 0%");
      lore.add(FaceColor.PINK + "(No Valid Essence Stats)");
    } else {
      lore.add(FaceColor.CYAN + "Essence Chance: " + (int) (essenceChance * 100) + "%");
      lore.add(FaceColor.CYAN + "Possible Essence Stats:");
      for (var s : stats) {
        lore.add(FaceColor.TEAL + " " +
            s.getCachedValue() + (s.getStat().isPercentage() ? "% " : " " + s.getStat().getStat().getName()));
      }
    }
    lore.add("");
    lore.add(FaceColor.LIGHT_GRAY + FaceColor.ITALIC.s() + "Higher craft level and better");
    lore.add(FaceColor.LIGHT_GRAY + FaceColor.ITALIC.s() + " tools yields rarer materials");
    lore.add(FaceColor.LIGHT_GRAY + FaceColor.ITALIC.s() + " and a higher essence chance");
    lore.add(FaceColor.LIGHT_GRAY + FaceColor.ITALIC.s() + "Rarer equipment affects minimum");
    lore.add(FaceColor.LIGHT_GRAY + FaceColor.ITALIC.s() + " rarity of materials");
    TextUtils.setLore(stack, lore, false);
    return stack;
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);
    if (event.getClickType() == ClickType.DOUBLE_CLICK) {
      event.setWillUpdate(false);
      event.setWillClose(false);
      return;
    }
    menu.doDestroy(event.getPlayer());
    event.setWillUpdate(true);
    event.setWillClose(false);
  }
}
