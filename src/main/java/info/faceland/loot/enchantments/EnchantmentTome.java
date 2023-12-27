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
package info.faceland.loot.enchantments;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

@Getter @Setter
public class EnchantmentTome {

  private final String name;
  private double weight;
  private double bonusWeight;
  private boolean broadcast;
  private List<ItemGroup> itemGroups;
  private List<String> lore;
  private String stat;
  private boolean bar;
  private double sellPrice;
  private double enchantXp;
  private String description;

  public EnchantmentTome(String name) {
    this.name = name;
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

    EnchantmentTome that = (EnchantmentTome) o;

    return !(name != null ? !name.equals(that.name) : that.name != null);
  }

  public static List<String> UNCOLORED_TOME_DESC = Arrays.asList(
      "",
      "|lgray|Use this at an |purple|Enchantment Table",
      "|lgray|on an " + MaterialUtil.ENCHANTABLE_TAG_BASE + " |lgray|item!",
      "",
      "|cyan|Bonuses Applied:"
  );

  public ItemStack toItemStack(int amount) {
    ItemStack is = new ItemStack(Material.BOOK);
    is.setAmount(amount);
    ItemStackExtensionsKt.setDisplayName(is, FaceColor.BLUE + "Enchantment Tome - " + getName());
    List<String> lore = new ArrayList<>();
    lore.add(FaceColor.WHITE + itemGroupsToString() + "Ս");
    lore.addAll(UNCOLORED_TOME_DESC);

    if (description != null && !description.isEmpty()) {
      lore.add(description);
    }
    TextUtils.setLore(is, PaletteUtil.color(lore));
    is.setDurability((short) 11);
    return is;
  }

  private String itemGroupsToString() {
    StringBuilder sb = new StringBuilder();
    for (ItemGroup ig : getItemGroups()) {
      sb.append(ig.getTag());
    }
    return sb.toString();
  }

  public List<ItemGroup> getItemGroups() {
    return new ArrayList<>(itemGroups);
  }

}
