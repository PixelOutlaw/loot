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

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.api.sockets.effects.SocketEffect;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.items.ItemBuilder;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter @Setter
public final class SocketGem implements Comparable<SocketGem> {

  private final String name;
  private String strifeLoreAbility = null;
  private double weight;
  private double distanceWeight;
  private double weightPerLevel;
  private double bonusWeight;
  private int customModelData;
  private String prefix;
  private String suffix;
  private List<String> lore;
  private List<SocketEffect> socketEffects;
  private List<ItemGroup> itemGroups;
  private String typeDesc;
  private boolean broadcast;
  private boolean triggerable;
  private String triggerText;
  private GemType gemType;
  private LoreAbility loreAbility;

  private ItemStack builtItem;

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

    return Objects.equals(name, that.name);
  }

  @Override
  public int compareTo(SocketGem o) {
    if (o == null) {
      return 1;
    }
    int compareTo = getName().compareTo(o.getName());
    return Math.min(1, Math.max(compareTo, -1));
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

  public ItemStack toItemStack(int amount) {
    ItemStack newStack = builtItem.clone();
    newStack.setAmount(amount);
    return newStack;
  }

  public void buildStack() {
    ItemStack itemStack = new ItemStack(Material.EMERALD);
    ItemStackExtensionsKt.setDisplayName(itemStack, FaceColor.LIME + "Socket Gem - " + getName());
    itemStack.setAmount(1);
    ItemStackExtensionsKt.setCustomModelData(itemStack, customModelData);
    List<String> itemLore = new ArrayList<>();
    itemLore.addAll(List.of(
        "|white|" + itemGroupsToString(itemGroups) + "Ս",
        "",
        "|lgray|" + "Place this gem on an item with an",
        "|lgray|" + "open " + ItemBuilder.SOCKET_BASE + "|lgray| to upgrade it!",
        "",
        "|yellow|Bonuses Applied:"
    ));
    itemLore.addAll(getLore());
    TextUtils.setLore(itemStack, PaletteUtil.color(itemLore), false);
    itemStack.setDurability((short) 11);
    builtItem = itemStack;
  }

  private String itemGroupsToString(List<ItemGroup> groups) {
    StringBuilder sb = new StringBuilder();
    for (ItemGroup ig : groups) {
      sb.append(ig.getTag());
    }
    return sb.toString().trim();
  }

  public void setStrifeLoreAbility(String strifeLoreAbility) {
    if (StrifePlugin.getInstance().getLoreAbilityManager() != null) {
      this.strifeLoreAbility = strifeLoreAbility;
      loreAbility = StrifePlugin.getInstance().getLoreAbilityManager().getLoreAbilityFromId(strifeLoreAbility);
    } else {
      Bukkit.getLogger().info("[Loot] Failed to set lore ability desc on gem??");
    }
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
