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
package info.faceland.loot.menu.mortar;

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.PulverizeResult;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class MortarMenu extends ItemMenu {

  private final LootPlugin plugin;

  private final Map<Player, ItemStack> selectedStacks = new WeakHashMap<>();
  private final Map<String, PulverizeResult> results = new HashMap<>();

  public MortarMenu(LootPlugin plugin) {
    super(TextUtils.color(
        plugin.getSettings().getString("language.menu.mortar-name","&0&lMortar")), Size.FOUR_LINE, plugin);
    this.plugin = plugin;

    setItem(4, new SelectedItemIcon(this));

    setItem(30, new GrindConfirmIcon(this));
    setItem(31, new GrindConfirmIcon(this));
    setItem(32, new GrindConfirmIcon(this));
  }

  @Override
  public void open(Player player) {
    setSelectedItem(player, null);
    super.open(player);
  }

  public ItemStack getSelectedStack(Player player) {
    return selectedStacks.get(player);
  }

  public void setSelectedItem(Player player, ItemStack item) {
    if (item == null || item.getType() == Material.AIR) {
      selectedStacks.put(player, null);
      return;
    }
    String key = item.getType().toString() + ItemUtils.getModelData(item);
    if (!results.containsKey(key)) {
      selectedStacks.put(player, null);
      update(player);
      return;
    }
    selectedStacks.put(player, item);
    update(player);
  }

  void doPulverize(Player player, boolean fullStack) {
    ItemStack selectedStack = selectedStacks.get(player);
    if (selectedStack == null || selectedStack.getAmount() < 1) {
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return;
    }
    PulverizeResult pr = fetchResult(selectedStack);
    if (pr == null) {
      player.playSound(player.getLocation(), Sound.ENTITY_SHULKER_HURT, 1, 0.8f);
      return;
    }
    int amount = fullStack ? selectedStack.getAmount() : 1;
    if (pr.getSkillType() != null && pr.getExperience() != 0) {
      StrifePlugin.getInstance().getSkillExperienceManager()
          .addExperience(player, pr.getSkillType(), pr.getExperience() * amount, false, false);
    }
    int newAmount = selectedStack.getAmount() - amount;
    selectedStack.setAmount(newAmount);
    if (newAmount == 0) {
      setSelectedItem(player, null);
    }
    ItemStack rewardDrop = pr.getStack().clone();
    rewardDrop.setAmount(amount);
    ItemUtils.giveOrDrop(player, rewardDrop);
    update(player);
    player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1, 0.6f);
  }

  public PulverizeResult fetchResult(ItemStack stack) {
    int data = ItemUtils.getModelData(stack);
    if (data == -1) {
      return results.getOrDefault(stack.getType().toString(), null);
    }
    String key = stack.getType().toString() + ItemUtils.getModelData(stack);
    return results.getOrDefault(key, null);
  }

  public void importResult(Material material, int modelData, ItemStack result, LifeSkillType skill, float xp) {
    if (modelData == -1) {
      results.put(material.toString(), new PulverizeResult(result, skill, xp));
      return;
    }
    String key = material.toString() + modelData;
    results.put(key, new PulverizeResult(result, skill, xp));
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
