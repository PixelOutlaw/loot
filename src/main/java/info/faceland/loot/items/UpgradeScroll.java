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
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang.WordUtils;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@Getter
@Setter
public final class UpgradeScroll {

  public static final String TYPE_KEY = "scroll";
  private static Map<String, UpgradeScroll> scrolls = new HashMap<>();

  private String id;
  private String prefix;
  private int customData;
  private List<String> lore;
  private double baseSuccess;
  private float itemDamageMultiplier;
  private double flatDecay;
  private double percentDecay;
  private double exponent;

  private boolean broadcast;
  private double weight;
  private int minLevel;
  private int maxLevel;

  @Getter(value = AccessLevel.NONE)
  @Setter(value = AccessLevel.NONE)
  private ItemStack stack;

  public static UpgradeScroll get(String id) {
    return scrolls.getOrDefault(id.toLowerCase(), null);
  }

  public static UpgradeScroll get(ItemStack stack) {
    if (!PersistHelper.getItemType(stack).equals(TYPE_KEY)) {
      return null;
    }
    return scrolls.getOrDefault(PersistHelper.getItemId(stack).toLowerCase(), null);
  }

  public static Map<String, UpgradeScroll> getScrolls() {
    return new HashMap<>(scrolls);
  }

  public ItemStack item() {
    return stack.clone();
  }

  public ItemMeta meta() {
    return stack.getItemMeta();
  }

  public static void update(ItemStack stack) {
    UpgradeScroll item = UpgradeScroll.get(stack);
    if (item != null) {
      int amount = stack.getAmount();
      ItemRenderer.replace(stack, item.stack.clone());
      stack.setAmount(amount);
    }
  }

  public static boolean matches(ItemStack stack) {
    return PersistHelper.getItemType(stack).equals(TYPE_KEY) && scrolls.containsKey(PersistHelper.getItemId(stack));
  }

  public static void loadFromFile(SmartYamlConfiguration file) {
    for (String key : file.getKeys(false)) {
      if (!file.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = file.getConfigurationSection(key);
      UpgradeScroll scroll = new UpgradeScroll();
      scroll.setId(key.toLowerCase());
      scroll.setPrefix(cs.getString("prefix", "CONFIGURE PREFIX FOR SCROLL" + key));
      scroll.setLore(PaletteUtil.color(cs.getStringList("lore")));
      scroll.setBaseSuccess(cs.getDouble("base-success", 1.0));
      scroll.setFlatDecay(cs.getDouble("flat-decay", 0.01));
      scroll.setPercentDecay(cs.getDouble("percent-decay", 0.01));
      scroll.setItemDamageMultiplier((float) cs.getDouble("item-damage-modifier", 1.0));
      scroll.setExponent(cs.getDouble("exponent", 1.1));
      scroll.setWeight(cs.getDouble("weight", 100));
      scroll.setMinLevel(cs.getInt("min-level", 0));
      scroll.setMaxLevel(cs.getInt("max-level", 14));
      scroll.setCustomData(cs.getInt("custom-data", 100));
      scroll.setBroadcast(cs.getBoolean("broadcast", false));

      scroll.buildItemStack();

      scrolls.put(key, scroll);
    }
  }

  public void buildItemStack() {
    stack = new ItemStack(Material.PAPER);
    ItemMeta meta = stack.getItemMeta();
    String name = ChatColor.stripColor(WordUtils.capitalize(id) + " Scroll");
    List<String> lore = new ArrayList<>();
    lore.add(PaletteUtil.color("|ns|\uF808上\uF808\uF801"));
    lore.add(
        HALFWAY_SMALL + GUI.spacesOf(-countTitleSize(name) / 2) + FaceColor.BLACK + doSmallNameThing(name) + "\uF80C");
    stack.setType(Material.PAPER);
    for (String s : this.lore) {
      lore.add(centerSmallText(s));
    }
    lore.remove(2);
    lore.remove(2);
    meta.setDisplayName(net.md_5.bungee.api.ChatColor.of(new Color(122, 101, 215)) + name);
    meta.setCustomModelData(customData);
    meta.setLore(lore);

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, TYPE_KEY);
    pdc.set(ITEM_EXTRA_KEY, PersistentDataType.STRING, id);

    stack.setItemMeta(meta);
    ItemRenderer.applyCommonFlags(stack);
  }
}
