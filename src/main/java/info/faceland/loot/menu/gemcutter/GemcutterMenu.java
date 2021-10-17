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

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.menu.BlankIcon;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.PlayerDataUtil;
import lombok.Getter;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.apache.commons.lang.StringUtils;
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
        "&0&lGemcutter")), Size.fit(45), plugin);

    SelectedItemIcon selectedItemIcon = new SelectedItemIcon(this);
    CutConfirmIcon cutConfirmIcon = new CutConfirmIcon(this);

    setItem(13, selectedItemIcon);
    setItem(31, cutConfirmIcon);

    fillEmptySlots(new BlankIcon());
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
    int craftingLevel = PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.CRAFTING);
    if (craftingLevel + 10 < itemLevel) {
      MessageUtils.sendMessage(player, "&e[!] You need &fCrafting " + (itemLevel - 10) +
          " &eto do this!");
      player.playSound(player.getLocation(), Sound.BLOCK_GRINDSTONE_USE, 1, 0.8f);
      return;
    }
    float effectiveCraftLevel = (float) PlayerDataUtil.getEffectiveLifeSkill(player,
        LifeSkillType.CRAFTING, false);
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
    }
    ItemStack result = new ItemStack(material);
    float upgradeChance = 0.3f * ((effectiveCraftLevel + 100) / (itemLevel + 100));
    int quality = 1;
    while (Math.random() < upgradeChance && quality < 6) {
      quality++;
    }
    ChatColor color = switch (quality) {
      case 1 -> ChatColor.WHITE;
      case 2 -> ChatColor.BLUE;
      case 3 -> ChatColor.DARK_PURPLE;
      case 4 -> ChatColor.RED;
      case 5 -> ChatColor.GOLD;
      default -> throw new IllegalStateException("Unexpected value: " + quality);
    };
    ItemStackExtensionsKt.setDisplayName(result, color + name);
    List<String> lore = new ArrayList<>();
    lore.add(ChatColor.WHITE + "Item Level: " + itemLevel);
    lore.add(ChatColor.WHITE + "Quality: " + color + StringUtils.repeat("✪", quality));
    lore.add(ChatColor.GRAY + "A very pretty jewel!");
    lore.add(ChatColor.YELLOW + "[ Crafting Component ]");
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
