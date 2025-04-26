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

import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.*;
import static info.faceland.loot.LootPlugin.ITEM_EXTRA_KEY;
import static info.faceland.loot.LootPlugin.ITEM_TYPE_KEY;
import static info.faceland.loot.LootPlugin.ITEM_VERSION;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;
import static info.faceland.loot.utils.ItemStyleUtil.HALFWAY_SMALL;
import static info.faceland.loot.utils.ItemStyleUtil.JUMP_BACK;
import static info.faceland.loot.utils.ItemStyleUtil.centerSmallText;
import static info.faceland.loot.utils.ItemStyleUtil.countTitleSize;
import static info.faceland.loot.utils.ItemStyleUtil.doSmallNameThing;

import com.sentropic.guiapi.gui.GUI;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.ItemStyleUtil;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
  private List<TextComponent> description;

  private boolean hideStats;

  private Set<StrifeTrait> traits = new HashSet<>();
  private Set<LoreAbility> loreAbilities = new HashSet<>();
  private Map<StrifeStat, Float> stats = new HashMap<>();
  private Map<StrifeStat, Float> percentStats = new HashMap<>();

  private TextComponent BREAK_CHANCE = color(Component.text("Break Chance: "), WHITE);
  private TextComponent BREAK_LEVEL1 = color(Component.text("Very Low"), GREEN);
  private TextComponent BREAK_LEVEL2 = color(Component.text("Low"), LIME);
  private TextComponent BREAK_LEVEL3 = color(Component.text("Average"), YELLOW);
  private TextComponent BREAK_LEVEL4 = color(Component.text("High"), ORANGE);
  private TextComponent BREAK_LEVEL5 = color(Component.text("Extremely High"), RED);

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
      ItemRenderer.replace(stack, s.builtItem.clone(), false);
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
    meta.displayName(color(Component.text(name), PINK));
    meta.setCustomModelData(modelData);
    List<TextComponent> lore = new ArrayList<>();
    lore.add(fakeNoShadow(Component.text("\uF801\uF808俊\uF808\uF802")));
    lore.add(ItemStyleUtil.smallNameLoreLine(name));
    if (breakChance < 0.2) {
      lore.add(centerSmallText(BREAK_CHANCE.append(BREAK_LEVEL1)));
    } else if (breakChance < 0.4) {
      lore.add(centerSmallText(BREAK_CHANCE.append(BREAK_LEVEL2)));
    } else if (breakChance < 0.6) {
      lore.add(centerSmallText(BREAK_CHANCE.append(BREAK_LEVEL3)));
    } else if (breakChance < 0.8) {
      lore.add(centerSmallText(BREAK_CHANCE.append(BREAK_LEVEL4)));
    } else {
      lore.add(centerSmallText(BREAK_CHANCE.append(BREAK_LEVEL5)));
    }
    lore.add(Component.empty());
    if (!hideStats && !(stats.isEmpty() && percentStats.isEmpty())) {
      for (var e : stats.entrySet()) {
        lore.add(centerSmallText(color(Component.text(
            "+" + e.getValue().intValue() + " " + e.getKey().getName()), PINK).append(JUMP_BACK)));
      }
      for (var e : percentStats.entrySet()) {
        lore.add(centerSmallText(color(Component.text(
            "+" + e.getValue().intValue() + "% " + e.getKey().getName()), PINK).append(JUMP_BACK)));
      }
      lore.add(Component.empty());
    }
    description.forEach(s -> lore.add(centerSmallText(s).append(JUMP_BACK)));
    meta.lore(lore);
    meta.setMaxStackSize(99);
    String rarityStyle;
    if (weight <= 500) {
      meta.setTooltipStyle(NamespacedKey.fromString("item_epic"));
      rarityStyle = "epic";
    } else if (weight <= 2000) {
      meta.setTooltipStyle(NamespacedKey.fromString("item_rare"));
      rarityStyle = "rare";
    } else if (weight <= 6000) {
      meta.setTooltipStyle(NamespacedKey.fromString("item_uncommon"));
      rarityStyle = "uncommon";
    } else {
      meta.setTooltipStyle(NamespacedKey.fromString("item_common"));
      rarityStyle = "common";
    }

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, TYPE_KEY);
    pdc.set(ITEM_EXTRA_KEY, PersistentDataType.STRING, id);

    builtItem.setItemMeta(meta);
    ItemRenderer.applyCommonFlags(builtItem);
    ItemUtils.setCustomData(builtItem, List.of(TYPE_KEY, rarityStyle, id));
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
    result.setDescription(PaletteUtil.loadComponents(section, "description"));
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
