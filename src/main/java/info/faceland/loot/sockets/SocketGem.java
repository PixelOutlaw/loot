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
package info.faceland.loot.sockets;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.text.WordUtils;
import info.faceland.loot.api.groups.ItemGroup;
import info.faceland.loot.api.sockets.effects.SocketEffect;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class SocketGem implements Comparable<SocketGem> {

  private final String name;
  private String strifeLoreAbility = null;
  private double weight;
  private double distanceWeight;
  private double weightPerLevel;
  private double bonusWeight;
  @Getter @Setter
  private int customModelData;
  private String prefix;
  private String suffix;
  private List<String> lore;
  private List<SocketEffect> socketEffects;
  private List<ItemGroup> itemGroups;
  private boolean broadcast;
  private boolean triggerable;
  private String triggerText;
  private GemType gemType;
  private LoreAbility loreAbility;

  public SocketGem(String name) {
    this.name = name;
    this.lore = new ArrayList<>();
    this.socketEffects = new ArrayList<>();
    this.itemGroups = new ArrayList<>();
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    SocketGem that = (SocketGem) o;

    return !(name != null ? !name.equals(that.name) : that.name != null);
  }

  @Override
  public int compareTo(SocketGem o) {
    if (o == null) {
      return 1;
    }
    int compareTo = getName().compareTo(o.getName());
    return Math.min(1, Math.max(compareTo, -1));
  }

  public String getName() {
    return name;
  }

  public double getWeight() {
    return weight;
  }

  public String getLoreAbilityString() {
    return strifeLoreAbility;
  }

  public String getPrefix() {
    return prefix != null ? prefix : "";
  }

  public String getSuffix() {
    return suffix != null ? suffix : "";
  }

  public List<String> getLore() {
    if (StringUtils.isNotBlank(strifeLoreAbility)) {
      List<String> loreAbilityText = new ArrayList<>();
      loreAbilityText.add(loreAbility.getTriggerText());
      loreAbilityText.addAll(loreAbility.getDescription());
      return loreAbilityText;
    } else {
      return new ArrayList<>(lore);
    }
  }

  public List<SocketEffect> getSocketEffects() {
    return new ArrayList<>(socketEffects);
  }

  public List<ItemGroup> getItemGroups() {
    return itemGroups;
  }

  public ItemStack toItemStack(int amount) {
    ItemStack itemStack = new ItemStack(Material.EMERALD);
    ItemStackExtensionsKt.setDisplayName(itemStack, ChatColor.GOLD + "Socket Gem - " + getName());
    itemStack.setAmount(amount);
    List<String> lore = new ArrayList<>();
    Collections.addAll(lore, ChatColor.WHITE + "Type: " + (!itemGroups.isEmpty() ? itemGroupsToString() : "Any"),
        ChatColor.GRAY + "Place this gem on an item with an",
        ChatColor.GRAY + "open " + ChatColor.GOLD + "(Socket) " + ChatColor.GRAY + "to upgrade it!",
        ChatColor.WHITE + "Bonuses Applied:");
    lore.addAll(getLore());
    ItemStackExtensionsKt.setCustomModelData(itemStack, customModelData);
    TextUtils.setLore(itemStack, TextUtils.color(lore));
    itemStack.setDurability((short) 11);
    return itemStack;
  }

  private String itemGroupsToString() {
    StringBuilder sb = new StringBuilder();
    for (ItemGroup ig : getItemGroups()) {
      sb.append(ig.getName()).append(" ");
    }
    return WordUtils.capitalizeFully(sb.toString().trim());
  }

  public double getDistanceWeight() {
    return distanceWeight;
  }

  public double getWeightPerLevel() {
    return weightPerLevel;
  }

  void setWeightPerLevel(double weightPerLevel) {
    this.weightPerLevel = weightPerLevel;
  }

  void setDistanceWeight(double distanceWeight) {
    this.distanceWeight = distanceWeight;
  }

  public boolean isBroadcast() {
    return broadcast;
  }

  public boolean isTriggerable() {
    return triggerable;
  }

  void setTriggerable(boolean triggerable) {
    this.triggerable = triggerable;
  }

  void setBroadcast(boolean broadcast) {
    this.broadcast = broadcast;
  }

  void setItemGroups(List<ItemGroup> itemGroups) {
    this.itemGroups = itemGroups;
  }

  void setSocketEffects(List<SocketEffect> socketEffects) {
    this.socketEffects = socketEffects;
  }

  void setLore(List<String> lore) {
    this.lore = lore;
  }

  void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  void setWeight(double weight) {
    this.weight = weight;
  }

  public void setStrifeLoreAbility(String strifeLoreAbility) {
    this.strifeLoreAbility = strifeLoreAbility;
    loreAbility = StrifePlugin.getInstance().getLoreAbilityManager().getLoreAbilityFromId(strifeLoreAbility);
  }

  public String getTriggerText() {
    return triggerText;
  }

  void setTriggerText(String triggerText) {
    this.triggerText = triggerText;
  }

  public double getBonusWeight() {
    return bonusWeight;
  }

  void setBonusWeight(double bonusWeight) {
    this.bonusWeight = bonusWeight;
  }

  public GemType getGemType() {
    return gemType;
  }

  void setGemType(GemType gemType) {
    this.gemType = gemType;
  }

  public enum GemType {
    ON_HIT, ON_KILL, WHEN_HIT, ON_SNEAK, ON_CRIT, ON_EVADE;

    public static GemType fromName(String name) {
      for (GemType gemType : values()) {
        if (gemType.name().equals(name)) {
          return gemType;
        }
      }
      return ON_HIT;
    }
  }
}
