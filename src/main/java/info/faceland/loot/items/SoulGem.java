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
package info.faceland.loot.items;

import static info.faceland.loot.LootPlugin.ITEM_EXTRA_KEY;
import static info.faceland.loot.LootPlugin.ITEM_TYPE_KEY;
import static info.faceland.loot.LootPlugin.ITEM_VERSION;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;
import static info.faceland.loot.utils.ItemStyleUtil.HALFWAY_SMALL;
import static info.faceland.loot.utils.ItemStyleUtil.centerSmallText;
import static info.faceland.loot.utils.ItemStyleUtil.countTitleSize;
import static info.faceland.loot.utils.ItemStyleUtil.doSmallNameThing;

import com.sentropic.guiapi.gui.GUI;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.LoreAbility;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.StatUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@Getter
@Setter
public final class SoulGem  {

  public static final String TYPE_KEY = "soulgem";

  private static Map<String, SoulGem> gems = new HashMap<>();

  private final String id;
  private String name;
  private double weight;
  private int modelData;
  private float breakChance;
  private List<String> description;

  private boolean hideStats;

  private Set<StrifeTrait> traits = new HashSet<>();
  private Set<LoreAbility> loreAbilities = new HashSet<>();
  private Map<StrifeStat, Float> stats = new HashMap<>();
  private Map<StrifeStat, Float> percentStats = new HashMap<>();

  @Getter(value=AccessLevel.NONE)
  @Setter(value=AccessLevel.NONE)
  private ItemStack builtItem;

  public SoulGem(String id) {
    this.id = id;
  }

  public static SoulGem get(String id) {
    return gems.getOrDefault(id, null);
  }

  public static SoulGem get(ItemStack stack) {
    if (!PersistHelper.getItemType(stack).equals(TYPE_KEY)) {
      return null;
    }
    return gems.getOrDefault(PersistHelper.getItemId(stack), null);
  }

  public static Set<String> getGemList() {
    return gems.keySet();
  }

  public ItemStack item() {
    return builtItem.clone();
  }

  public ItemMeta meta() {
    return builtItem.getItemMeta();
  }

  public static void update(ItemStack stack) {
    SoulGem s = get(stack);
    if (s != null) {
      int amount = stack.getAmount();
      ItemRenderer.replace(stack, s.builtItem.clone());
      stack.setAmount(amount);
    }
  }

  public static SoulGem getSimilar(ItemStack stack) {
    String id = ItemStackExtensionsKt.getDisplayName(stack);
    if (id == null || id.isBlank()) {
      return null;
    }
    id = ChatColor.stripColor(id).replace("Soul Gem - ", "").toLowerCase();
    return SoulGem.get(id);
  }

  public static boolean matches(ItemStack stack) {
    return PersistHelper.getItemType(stack).equals(TYPE_KEY) && gems.containsKey(PersistHelper.getItemId(stack));
  }

  private void build() {
    builtItem = new ItemStack(Material.AMETHYST_SHARD);
    ItemMeta meta = builtItem.getItemMeta();
    meta.setDisplayName(FaceColor.PINK + name);
    meta.setCustomModelData(modelData);
    List<String> lore = new ArrayList<>();
    lore.add(PaletteUtil.color("|ns|\uF808俊\uF808\uF801"));
    lore.add(HALFWAY_SMALL + GUI.spacesOf(-countTitleSize(name) / 2) + FaceColor.BLACK + doSmallNameThing(name) + "\uF80C");
    if (breakChance < 0.2) {
      lore.add(centerSmallText(FaceColor.WHITE + "Break Chance: " + FaceColor.GREEN + "Very Low"));
    } else if (breakChance < 0.4) {
      lore.add(centerSmallText(FaceColor.WHITE + "Break Chance: " + FaceColor.LIME + "Low"));
    } else if (breakChance < 0.6) {
      lore.add(centerSmallText(FaceColor.WHITE + "Break Chance: " + FaceColor.YELLOW + "Average"));
    } else if (breakChance < 0.8) {
      lore.add(centerSmallText(FaceColor.WHITE + "Break Chance: " + FaceColor.ORANGE + "High"));
    } else {
      lore.add(centerSmallText(FaceColor.WHITE + "Break Chance: " + FaceColor.RED + "Extremely High"));
    }
    lore.add("");
    if (!hideStats && !(stats.isEmpty() && percentStats.isEmpty())) {
      for (var e : stats.entrySet()) {
        lore.add(centerSmallText(
            FaceColor.PINK + "+" + e.getValue().intValue() + " " + e.getKey().getName()) + "\uF80B");
      }
      for (var e : percentStats.entrySet()) {
        lore.add(centerSmallText(
            FaceColor.PINK + "+" + e.getValue().intValue() + "% " + e.getKey().getName()) + "\uF80B");
      }
      lore.add("");
    }
    for (String s : description) {
      lore.add(centerSmallText(s) + "\uF80B");
    }
    meta.setLore(lore);

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, TYPE_KEY);
    pdc.set(ITEM_EXTRA_KEY, PersistentDataType.STRING, id);

    builtItem.setItemMeta(meta);
    ItemRenderer.applyCommonFlags(builtItem);
  }

  public static void clear() {
    gems.clear();
  }

  public static SoulGem getRandom() {
    double selectedWeight = LootPlugin.RNG.nextFloat() * weight();
    double currentWeight = 0D;
    for (SoulGem sg : gems.values()) {
      double calcWeight = sg.getWeight();
      currentWeight += calcWeight;
      if (currentWeight >= selectedWeight) {
        return sg;
      }
    }
    return null;
  }

  private static double weight() {
    double totalWeight = 0;
    for (SoulGem sg : gems.values()) {
      totalWeight += sg.getWeight();
    }
    return totalWeight;
  }

  public static SoulGem loadFromConfig(String id, ConfigurationSection section) {
    var result = new SoulGem(id);
    result.setName(section.getString("name", "CHANGEME"));
    result.setModelData(section.getInt("model-data", 0));
    result.setHideStats(section.getBoolean("hide-stats", false));
    result.setBreakChance((float) section.getDouble("break-chance", 0.2));
    result.setDescription(PaletteUtil.color(section.getStringList("description")));
    result.setStats(StatUtil.getStatMapFromSection(section.getConfigurationSection("stats")));
    result.setPercentStats(StatUtil.getStatMapFromSection(section.getConfigurationSection("percent-stats")));
    result.setWeight(section.getDouble("weight", 0));
    return result;
  }

  public static void loadFromFile(SmartYamlConfiguration file) {
    for (String key : file.getKeys(false)) {
      try {
        if (!file.isConfigurationSection(key)) {
          continue;
        }
        ConfigurationSection cs = file.getConfigurationSection(key);
        SoulGem gem = SoulGem.loadFromConfig(key, cs);
        gem.build();
        gems.put(key.toLowerCase(), gem);
      } catch (Exception e) {
        Bukkit.getLogger().warning("[Loot] Failed to load soul gem named " + key);
        e.printStackTrace();
      }
    }
    Bukkit.getLogger().info("[Loot] Loaded " + gems.size() + " soul gems");
  }
}
