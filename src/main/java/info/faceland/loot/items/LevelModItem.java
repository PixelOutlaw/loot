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

import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.DARK_ORANGE;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.GREEN;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.color;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.fakeNoShadow;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.noShadow;
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
public final class LevelModItem {

  public static final String TYPE_KEY = "level-mod";

  private static Map<String, LevelModItem> MOD_ITEMS = new HashMap<>();

  private final String id;
  private String name;
  private double weight;
  private int modelData;
  private List<String> description;
  private List<String> modelTags;
  private int modificationAmount;
  private List<String> validTiers;

  @Getter(value=AccessLevel.NONE)
  @Setter(value=AccessLevel.NONE)
  private ItemStack builtItem;

  public LevelModItem(String id) {
    this.id = id;
  }

  public static LevelModItem get(String id) {
    return MOD_ITEMS.getOrDefault(id, null);
  }

  public static LevelModItem get(ItemStack stack) {
    if (!PersistHelper.getItemType(stack).equals(TYPE_KEY)) {
      return null;
    }
    return MOD_ITEMS.getOrDefault(PersistHelper.getItemId(stack), null);
  }

  public static Set<String> getModsList() {
    return MOD_ITEMS.keySet();
  }

  public ItemStack item() {
    return builtItem.clone();
  }

  public ItemMeta meta() {
    return builtItem.getItemMeta();
  }

  public static void update(ItemStack stack) {
    LevelModItem s = get(stack);
    if (s != null) {
      int amount = stack.getAmount();
      ItemRenderer.replace(stack, s.builtItem.clone(), false);
      stack.setAmount(amount);
    }
  }

  public static boolean matches(ItemStack stack) {
    return PersistHelper.getItemType(stack).equals(TYPE_KEY) && MOD_ITEMS.containsKey(PersistHelper.getItemId(stack));
  }

  private void build() {
    builtItem = new ItemStack(Material.SHULKER_SHELL);
    ItemMeta meta = builtItem.getItemMeta();
    meta.displayName((color(Component.text(name), DARK_ORANGE)));
    meta.setCustomModelData(modelData);
    List<TextComponent> lore = new ArrayList<>();
    lore.add(fakeNoShadow(Component.text("\uF801\uF808俋\uF808\uF802")));
    lore.add(ItemStyleUtil.smallNameLoreLine(name));
    for (String s : description) {
      lore.add(centerSmallText(PaletteUtil.convert(s)).append(JUMP_BACK));
    }
    meta.lore(lore);
    meta.setTooltipStyle(NamespacedKey.fromString("item_uncommon"));
    meta.setMaxStackSize(99);

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, TYPE_KEY);
    pdc.set(ITEM_EXTRA_KEY, PersistentDataType.STRING, id);

    builtItem.setItemMeta(meta);
    ItemRenderer.applyCommonFlags(builtItem);
  }

  public static void clear() {
    MOD_ITEMS.clear();
  }

  public static LevelModItem getRandom() {
    double selectedWeight = LootPlugin.RNG.nextFloat() * weight();
    double currentWeight = 0D;
    for (LevelModItem sg : MOD_ITEMS.values()) {
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
    for (LevelModItem sg : MOD_ITEMS.values()) {
      totalWeight += sg.getWeight();
    }
    return totalWeight;
  }

  public static LevelModItem loadFromConfig(String id, ConfigurationSection section) {
    var result = new LevelModItem(id);
    result.setName(section.getString("name", "CHANGEME"));
    result.setModelData(section.getInt("model-data", 0));
    result.setModificationAmount(section.getInt("level-mod-amount", 0));
    result.setDescription(PaletteUtil.color(section.getStringList("description")));
    result.setModelTags(section.getStringList("model-tags"));
    result.setWeight(section.getDouble("weight", 0));
    result.setValidTiers(section.getStringList("valid-tiers"));
    return result;
  }

  public static void loadFromFile(SmartYamlConfiguration file) {
    for (String key : file.getKeys(false)) {
      try {
        if (!file.isConfigurationSection(key)) {
          continue;
        }
        ConfigurationSection cs = file.getConfigurationSection(key);
        LevelModItem modItem = LevelModItem.loadFromConfig(key, cs);
        modItem.build();
        MOD_ITEMS.put(key.toLowerCase(), modItem);
      } catch (Exception e) {
        Bukkit.getLogger().warning("[Loot] Failed to load level mod item named " + key);
        e.printStackTrace();
      }
    }
    Bukkit.getLogger().info("[Loot] Loaded " + MOD_ITEMS.size() + " level mod items");
  }
}
