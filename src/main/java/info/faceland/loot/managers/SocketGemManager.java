/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.utils.MaterialUtil;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class SocketGemManager {

  private final LootPlugin plugin;

  public SocketGemManager(LootPlugin plugin) {
    this.plugin = plugin;
  }

  @Deprecated
  public SocketGem getSocketGem(String name) {
    SocketGem gem = SocketGem.get(name);
    if (gem != null) {
      return gem;
    }
    name = ChatColor.stripColor(name).replace("Socket Gem - ", "").replace(" ", "_");
    return SocketGem.get(name.toLowerCase());
  }

  @Deprecated
  public SocketGem getSocketGem(ItemStack stack) {
    if (stack == null || stack.getType() != Material.EMERALD) {
      return null;
    }
    String name = ItemStackExtensionsKt.getDisplayName(stack);
    if (StringUtils.isBlank(name)) {
      return null;
    }
    return getSocketGem(ChatColor.stripColor(name.replace("Socket Gem - ", "").replace(" ", "_").toLowerCase()));
  }

  public SocketGem getRandomSocketGem() {
    return getRandomSocketGem(false);
  }

  public SocketGem getRandomSocketGem(boolean withChance) {
    Map<SocketGem, Double> map = new HashMap<>();
    if (!withChance) {
      Collection<SocketGem> gems = SocketGem.getGems().values();
      SocketGem[] array = gems.toArray(new SocketGem[0]);
      return array[LootPlugin.RNG.nextInt(array.length)];
    }
    double selectedWeight = LootPlugin.RNG.nextFloat() * getTotalWeight(map);
    double currentWeight = 0D;
    Collection<SocketGem> gems = SocketGem.getGems().values();
    for (SocketGem sg : gems) {
      double calcWeight = sg.getWeight();
      if (map.containsKey(sg)) {
        calcWeight *= map.get(sg);
      }
      currentWeight += calcWeight;
      if (currentWeight >= selectedWeight) {
        return sg;
      }
    }
    return null;
  }

  public SocketGem getRandomSocketGemByBonus() {
    double totalWeight = getTotalBonusWeight();
    double chosenWeight = LootPlugin.RNG.nextFloat() * totalWeight;
    double currentWeight = 0;
    for (SocketGem sg : SocketGem.getGems().values()) {
      currentWeight += sg.getBonusWeight();
      if (currentWeight >= chosenWeight) {
        return sg;
      }
    }
    return null;
  }

  public SocketGem getRandomSocketGemByLevel(int level) {
    double totalWeight = getTotalLevelWeight(level);
    double chosenWeight = LootPlugin.RNG.nextFloat() * totalWeight;
    double currentWeight = 0;
    for (SocketGem sg : SocketGem.getGems().values()) {
      currentWeight += sg.getWeight() + sg.getWeightPerLevel() * level;
      if (currentWeight >= chosenWeight) {
        return sg;
      }
    }
    return null;
  }

  public double getTotalWeight(Map<SocketGem, Double> map) {
    double totalWeight = 0;
    Collection<SocketGem> gems = SocketGem.getGems().values();
    for (SocketGem sg : gems) {
      double calcWeight = sg.getWeight();
      if (map.containsKey(sg)) {
        calcWeight *= map.get(sg);
      }
      totalWeight += calcWeight;
    }
    return totalWeight;
  }

  public double getTotalLevelWeight(int level) {
    double totalWeight = 0;
    Collection<SocketGem> gems = SocketGem.getGems().values();
    for (SocketGem sg : gems) {
      double calcWeight = sg.getWeight() + sg.getWeightPerLevel() * level;
      totalWeight += calcWeight;
    }
    return totalWeight;
  }

  public double getTotalBonusWeight() {
    double totalWeight = 0;
    Collection<SocketGem> gems = SocketGem.getGems().values();
    for (SocketGem sg : gems) {
      totalWeight += sg.getBonusWeight();
    }
    return totalWeight;
  }

  public int getOpenSocketIndex(ItemStack equipmentItem) {
    List<String> lore = TextUtils.getLore(equipmentItem);
    return MaterialUtil.indexOfSocket(lore);
  }

  public boolean applySocketGem(Player player, ItemStack gemStack, ItemStack equipmentItem) {
    SocketGem gem = SocketGem.get(gemStack);
    if (gem == null) {
      PaletteUtil.sendMessage(player, "|yellow|Hrmm... that gem doesn't seem valid!");
      return false;
    }
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    //System.out.println("data: " + data.getTier().getId());
    if (!MaterialUtil.isInGroup(data, gem.getItemGroup())) {
      sendMessage(player, plugin.getSettings().getString("language.socket.failure", ""));
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
      return false;
    }
    if (data.getEmptyGemSlots() < 1) {
      sendMessage(player, plugin.getSettings().getString("language.socket.needs-sockets", ""));
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
      return false;
    }

    if (data.socket(gem)) {
      PersistHelper.setEquipmentData(equipmentItem, data);
      sendMessage(player, plugin.getSettings().getString("language.socket.success", ""));
      player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1L, 2.0F);
      gemStack.setAmount(gemStack.getAmount() - 1);
      return true;
    } else {
      sendMessage(player, "somehow, palatine returned");
      return false;
    }
  }
}
