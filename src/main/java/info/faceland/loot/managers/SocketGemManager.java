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
import info.faceland.loot.math.LootRandom;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

  private static final double DISTANCE = 1000;
  private static final double DISTANCE_SQUARED = Math.pow(DISTANCE, 2);

  private final LootPlugin plugin;
  private final Map<String, SocketGem> gemMap;
  private final LootRandom random;

  // split on "" ?
  public static final String GEM_1_PREFIX = PaletteUtil.color("|ns|\uF808\uF802咰\uF804\uF824");
  public static final String GEM_2_PREFIX = PaletteUtil.color("|ns|\uF808\uF802咱\uF804\uF824");
  public static final String GEM_3_PREFIX = PaletteUtil.color("|ns|\uF808\uF802咲\uF804\uF824");
  public static final String GEM_4_PREFIX = PaletteUtil.color("|ns|\uF808\uF802咳\uF804\uF824");
  public static final String GEM_SPECIAL_PREFIX = PaletteUtil.color("|ns|\uF808\uF802咴\uF804\uF824");

  public SocketGemManager(LootPlugin plugin) {
    this.plugin = plugin;
    this.gemMap = new HashMap<>();
    this.random = new LootRandom();
  }

  public List<SocketGem> getSocketGems() {
    return new ArrayList<>(gemMap.values());
  }

  public List<String> getGemNames() {
    return new ArrayList<>(gemMap.keySet());
  }

  public Set<String> getGemIds() {
    return gemMap.keySet().stream().map(value -> value.replace(" ", "_")).collect(Collectors.toSet());
  }

  public List<SocketGem> getSortedGems() {
    List<SocketGem> gems = getSocketGems();
    Collections.sort(gems);
    return gems;
  }

  public List<String> getSortedGemNames() {
    List<String> l = getGemNames();
    Collections.sort(l);
    return l;
  }

  public SocketGem getSocketGem(String name) {
    if (gemMap.containsKey(name.toLowerCase())) {
      return gemMap.get(name.toLowerCase());
    }
    if (gemMap.containsKey(name.toLowerCase().replace(" ", "_"))) {
      return gemMap.get(name.toLowerCase().replace(" ", "_"));
    }
    if (gemMap.containsKey(name.toLowerCase().replace("_", " "))) {
      return gemMap.get(name.toLowerCase().replace("_", " "));
    }
    return null;
  }

  public SocketGem getSocketGem(ItemStack stack) {
    if (stack == null || stack.getType() != Material.EMERALD) {
      return null;
    }
    String name = ItemStackExtensionsKt.getDisplayName(stack);
    if (StringUtils.isBlank(name)) {
      return null;
    }
    return getSocketGem(ChatColor.stripColor(name.replace("Socket Gem - ", "")));
  }

  public void addSocketGem(SocketGem gem) {
    if (gem != null) {
      gemMap.put(gem.getName().toLowerCase(), gem);
    }
  }

  public void removeSocketGem(String name) {
    if (name != null) {
      gemMap.remove(name.toLowerCase());
    }
  }

  public SocketGem getRandomSocketGem() {
    return getRandomSocketGem(false);
  }

  public SocketGem getRandomSocketGem(boolean withChance) {
    return getRandomSocketGem(withChance, 0D);
  }

  public SocketGem getRandomSocketGem(boolean withChance, double distance) {
    return getRandomSocketGem(withChance, distance, new HashMap<SocketGem, Double>());
  }

  public SocketGem getRandomSocketGem(boolean withChance, double distance,
      Map<SocketGem, Double> map) {
    if (!withChance) {
      List<SocketGem> gems = getSocketGems();
      SocketGem[] array = gems.toArray(new SocketGem[gems.size()]);
      return array[random.nextInt(array.length)];
    }
    double selectedWeight = random.nextDouble() * getTotalWeight(distance, map);
    double currentWeight = 0D;
    List<SocketGem> gems = getSocketGems();
    for (SocketGem sg : gems) {
      double calcWeight = sg.getWeight() + ((distance / DISTANCE_SQUARED) * sg.getDistanceWeight());
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
    double chosenWeight = random.nextDouble() * totalWeight;
    double currentWeight = 0;
    for (SocketGem sg : getSocketGems()) {
      currentWeight += sg.getBonusWeight();
      if (currentWeight >= chosenWeight) {
        return sg;
      }
    }
    return null;
  }

  public SocketGem getRandomSocketGemByLevel(int level) {
    double totalWeight = getTotalLevelWeight(level);
    double chosenWeight = random.nextDouble() * totalWeight;
    double currentWeight = 0;
    for (SocketGem sg : getSocketGems()) {
      currentWeight += sg.getWeight() + sg.getWeightPerLevel() * level;
      if (currentWeight >= chosenWeight) {
        return sg;
      }
    }
    return null;
  }

  public double getTotalWeight() {
    return getTotalWeight(0);
  }

  public double getTotalWeight(double distance) {
    return getTotalWeight(distance, new HashMap<SocketGem, Double>());
  }

  public double getTotalWeight(double distance, Map<SocketGem, Double> map) {
    double totalWeight = 0;
    List<SocketGem> gems = getSocketGems();
    for (SocketGem sg : gems) {
      double calcWeight = sg.getWeight() + ((distance / DISTANCE_SQUARED) * sg.getDistanceWeight());
      if (map.containsKey(sg)) {
        calcWeight *= map.get(sg);
      }
      totalWeight += calcWeight;
    }
    return totalWeight;
  }

  public double getTotalLevelWeight(int level) {
    double totalWeight = 0;
    List<SocketGem> gems = getSocketGems();
    for (SocketGem sg : gems) {
      double calcWeight = sg.getWeight() + sg.getWeightPerLevel() * level;
      totalWeight += calcWeight;
    }
    return totalWeight;
  }

  public double getTotalBonusWeight() {
    double totalWeight = 0;
    List<SocketGem> gems = getSocketGems();
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
    SocketGem gem = getSocketGem(gemStack);
    if (gem == null) {
      return false;
    }
    if (!MaterialUtil.matchesGroups(equipmentItem, gem.getItemGroups())) {
      sendMessage(player, plugin.getSettings().getString("language.socket.failure", ""));
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
      return false;
    }

    List<String> lore = TextUtils.getLore(equipmentItem);
    int index = getOpenSocketIndex(equipmentItem);
    if (index == -1) {
      sendMessage(player, plugin.getSettings().getString("language.socket.needs-sockets", ""));
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
      return false;
    }

    if (gem.getLore().get(0).contains(" - ")) {
      String firstLine = ChatColor.stripColor(gem.getLore().get(0));
      for (String l : lore) {
        if (l.contains(firstLine)) {
          sendMessage(player, plugin.getSettings().getString("language.socket.dupe-effect", ""));
          player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1F, 0.5F);
          return false;
        }
      }
    }

    List<String> addLore = TextUtils.color(gem.getLore());
    switch (gem.getCustomModelData()) {
      case 2005, 2004 -> addLore.set(0, SocketGemManager.GEM_SPECIAL_PREFIX + addLore.get(0));
      case 2003 -> addLore.set(0, SocketGemManager.GEM_4_PREFIX + addLore.get(0));
      case 2002 -> addLore.set(0, SocketGemManager.GEM_3_PREFIX + addLore.get(0));
      case 2001 -> addLore.set(0, SocketGemManager.GEM_2_PREFIX + addLore.get(0));
      default -> addLore.set(0, SocketGemManager.GEM_1_PREFIX + addLore.get(0));
    }

    lore.remove(index);
    lore.addAll(index, addLore);
    TextUtils.setLore(equipmentItem, lore);

    String equipmentItemName = ItemStackExtensionsKt.getDisplayName(equipmentItem);
    String strippedName = ChatColor.stripColor(equipmentItemName);
    int level = MaterialUtil.getUpgradeLevel(equipmentItemName);
    String rawName = strippedName.replace("+" + level + " ", "");

    String prefix = "";
    String suffix = "";
    if (!gem.getPrefix().isEmpty()) {
      if (!equipmentItemName.contains(gem.getPrefix())) {
        if (rawName.startsWith("The ")) {
          rawName = rawName.replace("The ", "");
          prefix = "The " + gem.getPrefix() + " ";
        } else {
          prefix = gem.getPrefix() + " ";
        }
      }
    }
    if (!gem.getSuffix().isEmpty()) {
      if (!strippedName.contains(gem.getSuffix())) {
        suffix = " " + gem.getSuffix();
      }
    }
    String newName = prefix + rawName + suffix;
    if (level > 0) {
      newName = "+" + level + " " + newName;
    }
    newName = equipmentItemName.replace(strippedName, newName);
    ItemStackExtensionsKt.setDisplayName(equipmentItem, newName);

    sendMessage(player, plugin.getSettings().getString("language.socket.success", ""));
    player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1L, 2.0F);
    gemStack.setAmount(gemStack.getAmount() - 1);
    return true;
  }

  public Set<SocketGem> getGems(ItemStack itemStack) {
    if (itemStack == null || itemStack.getType() == Material.AIR) {
      return new HashSet<>();
    }
    Set<SocketGem> gems = new HashSet<>();
    List<String> lore = TextUtils.getLore(itemStack);
    List<String> strippedLore = new ArrayList<>();
    for (String s : lore) {
      strippedLore.add(ChatColor.stripColor(s));
    }
    for (String key : strippedLore) {
      SocketGem gem = getSocketGem(key);
      if (gem == null) {
        for (SocketGem g : getSocketGems()) {
          if (!g.isTriggerable()) {
            continue;
          }
          if (key.equals(ChatColor.stripColor(TextUtils.color(
              g.getTriggerText() != null ? g.getTriggerText() : "")))) {
            gem = g;
            break;
          }
        }
        if (gem == null) {
          continue;
        }
      }
      gems.add(gem);
    }
    return gems;
  }


}
