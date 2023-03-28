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
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.menu.TransparentIcon;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.PlayerDataUtil;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
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
    int itemLevel = Integer.parseInt(ChatColor.stripColor(TextUtils.getLore(selectedStack).get(0))
        .replace("Item Level: ", ""));
    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.CRAFTING, true);
    int craftingLevel = data.getLevel();
    if (craftingLevel + 10 < itemLevel) {
      MessageUtils.sendMessage(player, "&e[!] You need &fCrafting " + (itemLevel - 10) +
          " &eto do this!");
      player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1, 0.8f);
      return;
    }
    float effectiveCraftLevel = data.getLevelWithBonus();
    int modelData = ItemUtil.getCustomData(selectedStack);
    Material material = Material.DIAMOND;
    String name = "Diamond";
    if (modelData >= 1000 && modelData < 2000) {
      material = Material.RED_DYE;
      name = "Ruby";
    } else if (modelData < 3000) {
      material = Material.EMERALD;
      name = "Emerald";
    } else if (modelData < 4000) {
      material = Material.LAPIS_LAZULI;
      name = "Sapphire";
    } else if (modelData < 5000) {
      material = Material.ORANGE_DYE;
      name = "Topaz";
    } else if (modelData < 6000) {
      material = Material.AMETHYST_SHARD;
      name = "Amethyst";
    } else if (modelData < 7000) {
      material = Material.PINK_DYE;
      name = "Opal";
    }
    ItemStack result = new ItemStack(material);
    float upgradeChance = 0.3f * ((effectiveCraftLevel + 100) / (itemLevel + 100));
    int quality = 1;
    while ( quality < 4 && Math.random() < upgradeChance) {
      quality++;
    }
    FaceColor color = switch (quality) {
      case 1 -> FaceColor.WHITE;
      case 2 -> FaceColor.BLUE;
      case 3 -> FaceColor.PURPLE;
      case 4 -> FaceColor.RED;
      case 5 -> FaceColor.ORANGE;
      default -> throw new IllegalStateException("Unexpected value: " + quality);
    };
    String tag = switch (quality) {
      case 1 -> MaterialUtil.TAG_COMMON;
      case 2 -> MaterialUtil.TAG_UNCOMMON;
      case 3 -> MaterialUtil.TAG_RARE;
      case 4 -> MaterialUtil.TAG_EPIC;
      case 5 -> MaterialUtil.TAG_UNIQUE;
      default -> throw new IllegalStateException("Unexpected value: " + quality);
    };
    ItemStackExtensionsKt.setDisplayName(result, color + name);
    List<String> lore = new ArrayList<>();
    lore.add(FaceColor.WHITE + "Item Level: " + itemLevel);
    lore.add(FaceColor.WHITE + tag + "\uD86D\uDFF5");
    lore.add("");
    lore.add(FaceColor.GRAY + "A very pretty jewel!");
    TextUtils.setLore(result, lore);
    HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(result);
    for (ItemStack leftover : leftovers.values()) {
      Item item = player.getWorld().dropItemNaturally(player.getLocation(), leftover);
      item.setOwner(player.getUniqueId());
    }
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
