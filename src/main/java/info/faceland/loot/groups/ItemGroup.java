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
package info.faceland.loot.groups;

import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.items.Tier;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public final class ItemGroup {

  public static final Map<String, ItemGroup> GROUPS = new HashMap<>();

  private final String id;
  private final String name;
  private final List<String> validTiers;

  public ItemGroup(String id, String name, List<String> validTiers) {
    this.id = id;
    this.name = name;
    this.validTiers = validTiers;
  }

  public static ItemGroup get(String id) {
    return GROUPS.getOrDefault(id, null);
  }

  public boolean matches(EquipmentData data) {
    if (data == null || data.getTier() == null) {
      System.out.println("null groop");
      return false;
    }
    return matches(data.getTier());
  }

  public boolean matches(ItemStack stack) {
    return matches(PersistHelper.getEquipmentData(stack));
  }

  public boolean matches(Tier t) {
    for (var tier : validTiers) {
      System.out.println(tier + " " + t.getId());
      if (tier.equals(t.getId())) {
        return true;
      }
    }
    return false;
  }

  public static void loadFromFile(SmartYamlConfiguration file) {
    GROUPS.clear();
    for (String key : file.getKeys(false)) {
      if (!file.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = file.getConfigurationSection(key);
      ItemGroup group = new ItemGroup(
          key,
          cs.getString("name", "changeme"),
          cs.getStringList("valid-tiers")
      );
      GROUPS.put(key, group);
    }
  }
}
