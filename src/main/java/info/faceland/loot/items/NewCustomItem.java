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

import static info.faceland.loot.LootPlugin.ITEM_DATA_CLASS;
import static info.faceland.loot.LootPlugin.ITEM_DATA_KEY;
import static info.faceland.loot.LootPlugin.ITEM_EXTRA_KEY;
import static info.faceland.loot.LootPlugin.ITEM_TYPE_KEY;
import static info.faceland.loot.LootPlugin.ITEM_VERSION;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.utils.ItemRenderer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import land.face.strife.data.LoreAbility;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.StatUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@Getter
@Setter
@EqualsAndHashCode
public class NewCustomItem {

  private final String id;

  private Material material;
  private int customDataNumber;

  private String itemName;
  private String plateString;
  private FaceColor color;
  private boolean printName;
  private boolean large;
  private Tier tier;
  private int itemLevel;
  private List<UniqueItemStat> coreStats = new ArrayList<>();
  private List<UniqueItemStat> bonusStats = new ArrayList<>();

  private boolean enchantable = false;

  private Set<String> passives = new HashSet<>();
  private Set<String> traits = new HashSet<>();

  private int socketSlots = 0;
  private int extendSlots = 0;

  private List<String> styling = new ArrayList<>();
  private List<String> flavorText = new ArrayList<>();

  private Set<ItemFlag> flags;

  // Drop data
  private boolean broadcast;
  private boolean export;
  private double weight;
  private int dropLevel;
  private int dropRange;

  public NewCustomItem(String id, Material material) {
    this.id = id;
    this.material = material;
  }

  public ItemStack item() {
    ItemStack stack = new ItemStack(material);
    ItemMeta meta = stack.getItemMeta();
    if (tier == null) {
      PersistentDataContainer pdc = meta.getPersistentDataContainer();
      pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
      pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "unique");
      pdc.set(ITEM_EXTRA_KEY, PersistentDataType.STRING, id);
    } else {
      PersistentDataContainer pdc = meta.getPersistentDataContainer();
      pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
      pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "equipment");
      pdc.set(ITEM_EXTRA_KEY, PersistentDataType.STRING, id);
      pdc.set(ITEM_DATA_KEY, ITEM_DATA_CLASS, new EquipmentData());
    }
    stack.setItemMeta(meta);
    ItemRenderer.renderItem(stack, true);
    return stack;
  }

  public static NewCustomItem fromConfigSection(LootPlugin plugin, String key, ConfigurationSection section) {
    Material mat = Material.BRICK;
    try {
      mat = Material.valueOf(section.getString("material"));
    } catch (Exception e) {
      return new NewCustomItem(key, mat);
    }
    NewCustomItem ci = new NewCustomItem(key, mat);

    // item construction
    ci.setItemName(PaletteUtil.color(section.getString("name", "changeme")));
    ci.setCustomDataNumber(section.getInt("model-data", 0));
    ci.setItemLevel(section.getInt("item-level", 1));
    ci.setTier(Tier.get(section.getString("tier")));
    ci.setPlateString(section.getString("nameplate", ""));
    ci.setColor(FaceColor.valueOf(section.getString("color", "ORANGE")));
    ci.setPrintName(section.getBoolean("print-name", true));
    ci.setLarge(section.getBoolean("large", false));
    ci.setEnchantable(section.getBoolean("enchantable"));
    ci.setTraits(new HashSet<>(section.getStringList("traits")));
    ci.setPassives(new HashSet<>(section.getStringList("passives")));
    ci.setSocketSlots(section.getInt("socket-slots"));
    ci.setExtendSlots(section.getInt("extend-slots"));
    ci.setStyling(PaletteUtil.color(section.getStringList("styling")));
    ci.setFlavorText(PaletteUtil.color(section.getStringList("flavor-text")));
    ci.getCoreStats().clear();
    for (String s : section.getStringList("core-stats")) {
      ci.getCoreStats().add(getOrCreateStat(plugin, key, s, true));
    }
    for (String s : section.getStringList("bonus-stats")) {
      ci.getBonusStats().add(getOrCreateStat(plugin, key, s, false));
    }

    // Drop stuff
    ci.setWeight(section.getInt("weight", 0));
    ci.setDropLevel(section.getInt("drop-level", 0));
    ci.setDropRange(section.getInt("drop-range", 1));
    ci.setBroadcast(section.getBoolean("broadcast", false));
    ci.setExport(section.getBoolean("export", ci.getWeight() > 0));

    return ci;
  }

  private static UniqueItemStat getOrCreateStat(LootPlugin plugin, String uniqueId, String statId, boolean core) {
    List<String> strs = new ArrayList<>(List.of(statId.split("\\|")));
    if (statId.startsWith("unique|")) {
      boolean percent = strs.contains("p");
      strs.remove("p");
      boolean hide = strs.contains("h");
      strs.remove("h");
      strs.remove("c");
      StrifeStat stat = StrifeStat.valueOf(strs.get(1));
      float min = Float.parseFloat(strs.get(2));
      float max = strs.size() == 3 ? min : Float.parseFloat(strs.get(3));

      String trueStatId = "unique|" + uniqueId + "|" + stat + (core ? "|core" : "|bonus");

      ItemStat itemStat = new ItemStat(trueStatId);
      itemStat.setHide(hide);
      itemStat.setMinBaseValue(min);
      itemStat.setMaxBaseValue(max);
      itemStat.setMinHue(core ? 0.05f : 0.3083f);
      itemStat.setMaxHue(core ? 0.05f : 0.1278f);
      itemStat.setMinSaturation(core ?  0.09827f : 0.82906f);
      itemStat.setMaxSaturation(core ?  0.09827f : 0.74118f);
      itemStat.setMinBrightness(core ?  0.67843f : 0.91765f);
      itemStat.setMaxBrightness(core ?  0.67843f : 1.0f);
      itemStat.setStat(stat);
      itemStat.setPercentage(percent);
      itemStat.setHide(hide);
      itemStat.calcColorRange();
      if (StringUtils.isBlank(stat.getName())) {
        itemStat.setStatString("+{} Unknown Stat");
      } else {
        itemStat.setStatString("+{}" + (percent ? "% " : " ") + stat.getName());
      }
      plugin.getStatManager().addStat(trueStatId, itemStat);
      return new UniqueItemStat(itemStat, 1f);
    } else {
      float multiplier = strs.size() > 1 ? Float.parseFloat(strs.get(1)) : 1;
      return new UniqueItemStat(plugin.getStatManager().getStat(strs.get(0)), multiplier);
    }
  }
}
