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
package info.faceland.loot.items;

import com.destroystokyo.paper.Namespaced;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.CustomItem;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public final class LootCustomItem implements CustomItem {

  private final String name;
  private String displayName;
  private List<String> lore;
  private Material material;
  private double weight;
  private double distanceWeight;
  private int levelBase;
  private int levelRange;
  private int customDataNumber;
  private boolean broadcast;
  private boolean export;
  private Set<ItemFlag> flags;
  private Set<String> canBreak;

  public LootCustomItem(String name, Material material) {
    this.name = name;
    this.material = material;
    this.lore = new ArrayList<>();
  }

  public String getName() {
    return name;
  }

  public String getDisplayName() {
    return displayName;
  }

  void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public List<String> getLore() {
    return lore;
  }

  void setLore(List<String> lore) {
    this.lore = lore;
  }

  public Material getMaterial() {
    return material;
  }

  @Override
  public ItemStack toItemStack(int amount) {
    ItemStack itemStack = new ItemStack(material);
    if (itemStack.getType() == Material.AIR) {
      return itemStack;
    }
    itemStack.setAmount(amount);
    ItemStackExtensionsKt.setDisplayName(itemStack, PaletteUtil.color(this.displayName));
    TextUtils.setLore(itemStack, PaletteUtil.color(lore));
    itemStack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

    if (material.getMaxStackSize() == 1 && customDataNumber > 8000) {
      // This section exists to clear existing item attributes and enforce
      // no stacking on equipment items
      ItemMeta meta = itemStack.getItemMeta();
      double serialValue = LootPlugin.RNG.nextFloat() * 0.0001;
      AttributeModifier serial = new AttributeModifier("SERIAL", serialValue, Operation.ADD_NUMBER);
      meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, serial);
      itemStack.setItemMeta(meta);
    }

    if (customDataNumber != -1) {
      ItemStackExtensionsKt.setCustomModelData(itemStack, customDataNumber);
      ItemMeta iMeta = itemStack.getItemMeta();
      iMeta.addItemFlags(ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_DYE);
      if (customDataNumber != -1 && iMeta instanceof LeatherArmorMeta) {
        ((LeatherArmorMeta) iMeta).setColor(Color.fromRGB(customDataNumber));
      }
      itemStack.setItemMeta(iMeta);
    }
    if (!canBreak.isEmpty()) {
      ItemMeta iMeta = itemStack.getItemMeta();
      Set<Namespaced> breakSpaces = new HashSet<>();
      for (String s : canBreak) {
        breakSpaces.add(NamespacedKey.minecraft(s));
      }
      iMeta.setDestroyableKeys(breakSpaces);
      iMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
      itemStack.setItemMeta(iMeta);
    }
    if (!flags.isEmpty()) {
      ItemMeta iMeta = itemStack.getItemMeta();
      for (ItemFlag flag : flags) {
        iMeta.addItemFlags(flag);
      }
      itemStack.setItemMeta(iMeta);
    }
    return itemStack;
  }

  @Override
  public double getWeight() {
    return weight;
  }

  void setWeight(double weight) {
    this.weight = weight;
  }

  @Override
  public double getDistanceWeight() {
    return distanceWeight;
  }

  @Override
  public int getLevelBase() {
    return levelBase;
  }

  @Override
  public int getLevelRange() {
    return levelRange;
  }

  void setDistanceWeight(double distanceWeight) {
    this.distanceWeight = distanceWeight;
  }

  void setLevelBase(int levelBase) {
    this.levelBase = levelBase;
  }

  void setLevelRange(int levelRange) {
    this.levelRange = levelRange;
  }

  @Override
  public int getCustomDataNumber() {
    return customDataNumber;
  }

  public void setCustomDataNumber(int customDataNumber) {
    this.customDataNumber = customDataNumber;
  }

  @Override
  public boolean isBroadcast() {
    return broadcast;
  }

  @Override
  public boolean isExport() {
    return export;
  }

  public Set<ItemFlag> getFlags() {
    return flags;
  }

  public void setFlags(Set<ItemFlag> flags) {
    this.flags = flags;
  }

  public Set<String> getCanBreak() {
    return canBreak;
  }

  public void setCanBreak(Set<String> canBreak) {
    this.canBreak = canBreak;
  }

  void setBroadcast(boolean broadcast) {
    this.broadcast = broadcast;
  }

  void setExport(boolean export) {
    this.export = export;
  }

  void setMaterial(Material material) {
    this.material = material;
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

    LootCustomItem that = (LootCustomItem) o;

    return !(name != null ? !name.equals(that.name) : that.name != null);
  }

}
