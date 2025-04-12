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

import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.noShadow;
import static info.faceland.loot.LootPlugin.ITEM_EXTRA_KEY;
import static info.faceland.loot.LootPlugin.ITEM_TYPE_KEY;
import static info.faceland.loot.LootPlugin.ITEM_VERSION;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;
import static info.faceland.loot.utils.ItemStyleUtil.HALFWAY_SMALL;
import static info.faceland.loot.utils.ItemStyleUtil.centerSmallText;
import static info.faceland.loot.utils.ItemStyleUtil.countTitleSize;
import static info.faceland.loot.utils.ItemStyleUtil.doSmallNameThing;
import static org.bukkit.ChatColor.stripColor;

import com.sentropic.guiapi.gui.GUI;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.ItemStyleUtil;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.security.PermitAll;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@Getter
@Setter
public class EnchantmentTome {

  public static final String TYPE_KEY = "tome";
  private static Map<String, EnchantmentTome> tomes = new HashMap<>();

  private final String id;
  private String name;

  private double weight;
  private double bonusWeight;
  private boolean broadcast;
  private ItemGroup itemGroup;
  private List<String> lore;
  private String stat;
  private boolean bar;
  private double sellPrice;
  private double enchantXp;
  private String description;

  private ItemStack builtItem;

  public EnchantmentTome(String id) {
    this.id = id;
  }

  @Deprecated
  public ItemStack toItemStack(int amount) {
    ItemStack is = builtItem.clone();
    is.setAmount(amount);
    return is;
  }

  public static EnchantmentTome get(String name) {
    return tomes.getOrDefault(name.toLowerCase().replace(" ", "_"), null);
  }

  public static EnchantmentTome get(ItemStack stack) {
    if (!PersistHelper.getItemType(stack).equals(TYPE_KEY)) {
      return null;
    }
    return get(PersistHelper.getItemId(stack));
  }

  public ItemStack item() {
    return builtItem.clone();
  }

  public ItemMeta meta() {
    return builtItem.getItemMeta();
  }

  public static Map<String, EnchantmentTome> getTomes() {
    return new HashMap<>(tomes);
  }

  @Deprecated
  public static EnchantmentTome getEnchantmentItem(ItemStack stack) {
    if (stack.getType() != Material.BOOK) {
      return null;
    }
    String name = ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    String id = name.replace("Enchantment Tome - ", "").replace(" ", "_").toLowerCase();
    return EnchantmentTome.get(id);
  }

  public static void update(ItemStack stack) {
    EnchantmentTome item = EnchantmentTome.get(stack);
    if (item != null) {
      int amount = stack.getAmount();
      ItemRenderer.replace(stack, item.builtItem.clone(), false);
      stack.setAmount(amount);
    }
  }

  public static boolean matches(ItemStack stack) {
    return PersistHelper.getItemType(stack).equals(TYPE_KEY) && tomes.containsKey(PersistHelper.getItemId(stack));
  }

  private void build() {
    builtItem = new ItemStack(Material.BOOK);
    String name = this.name;
    ItemMeta meta = builtItem.getItemMeta();
    ItemStat is = LootPlugin.getInstance().getStatManager().getStat(stat);
    List<TextComponent> lore = List.of(
        noShadow(Component.text("\uF801\uF808出\uF808\uF802")),
        ItemStyleUtil.smallNameLoreLine(name),
        centerSmallText(PaletteUtil.convert(FaceColor.WHITE + "Used On: " + FaceColor.CYAN + itemGroup.getName())),
        centerSmallText(PaletteUtil.convert(FaceColor.WHITE + "Adds: " + FaceColor.BLUE + is.getStat().getName() + "\uF80C")),
        Component.empty(),
        centerSmallText(PaletteUtil.convert(FaceColor.LIGHT_GRAY + "Use this magical book at")),
        centerSmallText(PaletteUtil.convert(FaceColor.LIGHT_GRAY + "an enchanting table to")),
        centerSmallText(PaletteUtil.convert(FaceColor.LIGHT_GRAY + "empower your equipment!"))
    );
    meta.setDisplayName(FaceColor.BLUE + name);
    meta.lore(lore);
    meta.setMaxStackSize(99);
    if (weight == 0) {
      meta.setTooltipStyle(NamespacedKey.fromString("item_common"));
    } else if (weight <= 30) {
      meta.setTooltipStyle(NamespacedKey.fromString("item_epic"));
    } else if (weight <= 200) {
      meta.setTooltipStyle(NamespacedKey.fromString("item_rare"));
    } else if (weight <= 1000) {
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
  }

  public static void loadFromFile(LootPlugin plugin, SmartYamlConfiguration file) {
    for (String key : file.getKeys(false)) {
      if (!file.isConfigurationSection(key)) {
        continue;
      }
      try {
        ConfigurationSection cs = file.getConfigurationSection(key);
        String trueId = key.toLowerCase().replace(" ", "_");
        EnchantmentTome tome = new EnchantmentTome(trueId);
        tome.setName(key);
        tome.setDescription(cs.getString("description"));
        tome.setWeight(cs.getDouble("weight"));
        tome.setBonusWeight(cs.getDouble("bonus-weight"));
        tome.setLore(cs.getStringList("lore"));
        tome.setStat(cs.getString("stat", ""));
        tome.setBar(cs.getBoolean("enable-bar", true));
        tome.setSellPrice(cs.getDouble("sell-price", -1));
        tome.setEnchantXp(cs.getDouble("enchant-xp", -1));
        tome.setBroadcast(cs.getBoolean("broadcast", false));
        tome.setItemGroup(ItemGroup.get(cs.getString("item-group")));
        Map<Enchantment, Integer> enchantments = new HashMap<>();
        if (cs.isConfigurationSection("enchantments")) {
          ConfigurationSection enchCS = cs.getConfigurationSection("enchantments");
          for (String eKey : enchCS.getKeys(false)) {
            Enchantment ench = Enchantment.getByName(eKey);
            if (ench == null) {
              continue;
            }
            int i = enchCS.getInt(eKey);
            enchantments.put(ench, i);
          }
        }
        tome.build();
        tomes.put(trueId, tome);
      } catch (Exception e) {
        Bukkit.getLogger().info("[Loot] Skipped enchantment tomes " + key);
        e.printStackTrace();
      }
    }
    Bukkit.getLogger().info("[Loot] Loaded enchantment tomes: " + tomes.size());
  }
}
