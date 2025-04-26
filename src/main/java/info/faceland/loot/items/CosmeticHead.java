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

import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.BLUE;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.GREEN;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.color;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.fakeNoShadow;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.noShadow;
import static info.faceland.loot.LootPlugin.ITEM_EXTRA_KEY;
import static info.faceland.loot.LootPlugin.ITEM_TYPE_KEY;
import static info.faceland.loot.LootPlugin.ITEM_VERSION;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;
import static info.faceland.loot.utils.ItemStyleUtil.JUMP_BACK;
import static info.faceland.loot.utils.ItemStyleUtil.centerSmallText;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.ItemStyleUtil;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@Getter
@Setter
public final class CosmeticHead {

  public static final String TYPE_KEY = "headhat";
  private static Map<String, CosmeticHead> heads = new HashMap<>();

  private static HeadDatabaseAPI HEAD_API = new HeadDatabaseAPI();
  public static Boolean DB_LOADED = false;

  private final String id;
  private final int hdbId;
  private String name;
  private double weight;
  private int modelData;
  private List<String> description;

  @Getter(value=AccessLevel.NONE)
  @Setter(value=AccessLevel.NONE)
  private ItemStack builtItem;

  public CosmeticHead(String id, int hdbId) {
    this.id = id;
    this.hdbId = hdbId;
  }

  public static CosmeticHead get(String id) {
    return heads.getOrDefault(id, null);
  }

  public static CosmeticHead get(ItemStack stack) {
    if (!PersistHelper.getItemType(stack).equals(TYPE_KEY)) {
      return null;
    }
    return heads.getOrDefault(PersistHelper.getItemId(stack), null);
  }

  public static Set<String> getHeadList() {
    return heads.keySet();
  }

  public ItemStack item() {
    return builtItem.clone();
  }

  public ItemMeta meta() {
    return builtItem.getItemMeta();
  }

  public static void update(ItemStack stack) {
    CosmeticHead s = get(stack);
    if (s != null) {
      int amount = stack.getAmount();
      ItemRenderer.replace(stack, s.builtItem.clone(), false);
      stack.setAmount(amount);
    }
  }

  public static boolean matches(ItemStack stack) {
    return PersistHelper.getItemType(stack).equals(TYPE_KEY) && heads.containsKey(PersistHelper.getItemId(stack));
  }

  public static void rebuild(boolean bdLoaded) {
    for (var h : heads.values()) {
      h.build(bdLoaded);
    }
  }

  private void build(boolean hbdLoaded) {
    builtItem = new ItemStack(Material.PLAYER_HEAD);
    ItemMeta meta = hbdLoaded ? HEAD_API.getItemHead(String.valueOf(hdbId)).getItemMeta() : builtItem.getItemMeta();
    meta.displayName((color(Component.text(name), BLUE)));
    meta.setCustomModelData(modelData);

    List<TextComponent> lore = new ArrayList<>();
    lore.add(fakeNoShadow(Component.text("\uF801\uF808侽\uF808\uF802")));
    lore.add(ItemStyleUtil.smallNameLoreLine(name));
    for (String s : description) {
      lore.add(centerSmallText(PaletteUtil.convert(s)).append(JUMP_BACK));
    }
    meta.lore(lore);
    meta.setMaxStackSize(99);
    if (weight <= 500) {
      meta.setTooltipStyle(NamespacedKey.fromString("item_epic"));
    } else if (weight <= 2000) {
      meta.setTooltipStyle(NamespacedKey.fromString("item_rare"));
    } else if (weight <= 6000) {
      meta.setTooltipStyle(NamespacedKey.fromString("item_uncommon"));
    } else {
      meta.setTooltipStyle(NamespacedKey.fromString("item_common"));
    }

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, TYPE_KEY);
    pdc.set(ITEM_EXTRA_KEY, PersistentDataType.STRING, id);

    builtItem.setItemMeta(meta);
    ItemRenderer.applyCommonFlags(builtItem);
    ItemUtils.setCustomData(builtItem, List.of(TYPE_KEY));
  }

  public static void clear() {
    heads.clear();
  }

  public static CosmeticHead getRandom() {
    double selectedWeight = LootPlugin.RNG.nextFloat() * weight();
    double currentWeight = 0D;
    for (CosmeticHead sg : heads.values()) {
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
    for (CosmeticHead sg : heads.values()) {
      totalWeight += sg.getWeight();
    }
    return totalWeight;
  }

  public static CosmeticHead loadFromConfig(String id, ConfigurationSection section) {
    var headDbId = section.getInt("head-id", 1);
    var result = new CosmeticHead(id, headDbId);
    result.setName(section.getString("name", "CHANGEME"));
    result.setModelData(section.getInt("model-data", 0));
    result.setDescription(PaletteUtil.color(section.getStringList("description")));
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
        CosmeticHead head = CosmeticHead.loadFromConfig(key, cs);
        head.build(false);
        heads.put(key.toLowerCase(), head);
      } catch (Exception e) {
        Bukkit.getLogger().warning("[Loot] Failed to load head named " + key);
        e.printStackTrace();
      }
    }
    Bukkit.getLogger().info("[Loot] Loaded " + heads.size() + " cosmetic heads");
  }
}
