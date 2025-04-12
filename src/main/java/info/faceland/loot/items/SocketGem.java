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

import com.sentropic.guiapi.gui.GUI;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.ItemStyleUtil;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.StrifePlugin;
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
public final class SocketGem {

  public static final String TYPE_KEY = "socket";
  private static Map<String, SocketGem> gems = new HashMap<>();

  private final String id;
  private String name;

  private boolean broadcast;
  private double weight;
  private double weightPerLevel;
  private double bonusWeight;
  private String rarity;
  private NamespacedKey tooltip;
  private String prefix = "";
  private String suffix = "";
  private List<String> lore;
  private ItemGroup itemGroup;
  private String typeDesc;

  private Set<StrifeTrait> traits = new HashSet<>();
  private Set<LoreAbility> loreAbilities = new HashSet<>();
  private Map<StrifeStat, Float> stats = new HashMap<>();
  private Map<StrifeStat, Float> percentStats = new HashMap<>();

  @Getter(value = AccessLevel.NONE)
  @Setter(value = AccessLevel.NONE)
  private ItemStack builtItem;

  private static Map<String, String> CONVERTZ = Map.ofEntries(
      Map.entry("deflect_i", "parry_i"),
      Map.entry("deflect_ii", "parry_ii"),
      Map.entry("deflect_iii", "parry_iii"),
      Map.entry("deflect_iv", "parry_iv"),
      Map.entry("discovery_i", "discovery"),
      Map.entry("discovery_ii", "discovery"),
      Map.entry("discovery_iii", "discovery"),
      Map.entry("discovery_iv", "discovery"),
      Map.entry("fortune_i", "fortune"),
      Map.entry("fortune_ii", "fortune"),
      Map.entry("fortune_iii", "fortune"),
      Map.entry("fortune_iv", "fortune"),
      Map.entry("sharpen_i", "impact_i"),
      Map.entry("sharpen_ii", "impact_ii"),
      Map.entry("sharpen_iii", "impact_iii"),
      Map.entry("sharpen_iv", "impact_iv"),
      Map.entry("shoot_i", "impact_i"),
      Map.entry("shoot_ii", "impact_ii"),
      Map.entry("shoot_iii", "impact_iii"),
      Map.entry("shoot_iv", "impact_iv"),
      Map.entry("imbue", "elemental_energy"),
      Map.entry("insight", "muscle_memory")
  );

  public SocketGem(String id) {
    this.id = id;
    this.lore = new ArrayList<>();
  }

  public static SocketGem get(String id) {
    var trueId = id.toLowerCase().replace(" ", "_");
    trueId = CONVERTZ.getOrDefault(trueId, trueId);
    return gems.getOrDefault(trueId, null);
  }

  public static SocketGem get(ItemStack stack) {
    if (!PersistHelper.getItemType(stack).equals(TYPE_KEY)) {
      return null;
    }
    return gems.getOrDefault(PersistHelper.getItemId(stack), null);
  }

  public static Map<String, SocketGem> getGems() {
    return new HashMap<>(gems);
  }

  public List<String> getLore() {
    return new ArrayList<>(lore);
  }

  @Deprecated
  public ItemStack toItemStack(int amount) {
    ItemStack newStack = builtItem.clone();
    newStack.setAmount(amount);
    return newStack;
  }

  public ItemStack item() {
    return builtItem.clone();
  }

  public ItemMeta meta() {
    return builtItem.getItemMeta();
  }

  public static void update(ItemStack stack) {
    SocketGem gemb = LootPlugin.getInstance().getSocketGemManager().getSocketGem(PersistHelper.getItemId(stack));
    if (gemb != null) {
      int amount = stack.getAmount();
      ItemRenderer.replace(stack, gemb.builtItem.clone(), false);
      stack.setAmount(amount);
    }
  }

  public static boolean matches(ItemStack stack) {
    return PersistHelper.getItemType(stack).equals(TYPE_KEY) && gems.containsKey(PersistHelper.getItemId(stack));
  }

  private void buildStack() {
    builtItem = new ItemStack(Material.EMERALD);
    ItemMeta meta = builtItem.getItemMeta();
    String name = ChatColor.stripColor(this.name);

    List<TextComponent> lore = new ArrayList<>();
    lore.add(noShadow(Component.text("\uF801\uF808丈\uF808\uF802")));
    // Todo: remove manual character replace
    var tempName = name.replace("â", "a");
    lore.add(ItemStyleUtil.smallNameLoreLine(tempName));
    lore.add(centerSmallText(PaletteUtil.convert(FaceColor.WHITE + "Used On: " + FaceColor.CYAN + itemGroup.getName())));
    lore.add(Component.empty());
    for (var stuff : percentStats.entrySet()) {
      int val = Math.round(stuff.getValue());
      if (val >= 0) {
        lore.add(centerSmallText(PaletteUtil.convert(FaceColor.ORANGE + "+" + val + "% " + stuff.getKey().getName())));
      } else {
        lore.add(centerSmallText(PaletteUtil.convert(FaceColor.RED.s() + (val + "% " + stuff.getKey().getName()))));
      }
    }
    for (var stuff : stats.entrySet()) {
      int val = Math.round(stuff.getValue());
      if (val >= 0) {
        lore.add(centerSmallText(PaletteUtil.convert(FaceColor.ORANGE + "+" + val + " " + stuff.getKey().getName())));
      } else {
        lore.add(centerSmallText(PaletteUtil.convert(FaceColor.RED.s() + val + " " + stuff.getKey().getName())));
      }
    }
    for (var stuff : traits) {
      for (String s : stuff.getAdditionalText()) {
        lore.add(centerSmallText(PaletteUtil.convert(s)));
      }
    }
    for (var stuff : loreAbilities) {
      for (String s : stuff.getDescription()) {
        lore.add(centerSmallText(PaletteUtil.convert(s)));
      }
    }
    for (String s : this.lore) {
      lore.add(centerSmallText(PaletteUtil.convert(PaletteUtil.color(s))));
    }
    lore.add(Component.empty());
    lore.addAll(List.of(
        centerSmallText(PaletteUtil.convert(FaceColor.LIGHT_GRAY + "Visit an enchanting table")),
        centerSmallText(PaletteUtil.convert(FaceColor.LIGHT_GRAY + "to use this gem on items")),
        centerSmallText(PaletteUtil.convert(FaceColor.LIGHT_GRAY + "with empty socket slots!"))
    ));

    meta.setDisplayName(FaceColor.LIME + name);
    meta.setMaxStackSize(99);
    meta.lore(lore);
    meta.setTooltipStyle(tooltip);

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, TYPE_KEY);
    pdc.set(ITEM_EXTRA_KEY, PersistentDataType.STRING, id);

    builtItem.setItemMeta(meta);
    ItemRenderer.applyCommonFlags(builtItem);
  }

  public static List<String> getExportLore(SocketGem gem) {
    List<String> lore = new ArrayList<>();
    for (var stuff : gem.percentStats.entrySet()) {
      int val = Math.round(stuff.getValue());
      if (val >= 0) {
        lore.add("|orange|+" + val + "% " + stuff.getKey().getName());
      } else {
        lore.add("|red|" + val + "% " + stuff.getKey().getName());
      }
    }
    for (var stuff : gem.stats.entrySet()) {
      int val = Math.round(stuff.getValue());
      if (val >= 0) {
        lore.add("|orange|+" + val + " " + stuff.getKey().getName());
      } else {
        lore.add("|red|" + val + " " + stuff.getKey().getName());
      }
    }
    for (var stuff : gem.traits) {
      lore.addAll(stuff.getAdditionalTextRaw());
    }
    for (var stuff : gem.loreAbilities) {
      lore.addAll(stuff.getRawDescription());
    }
    lore.addAll(gem.lore);
    return lore;
  }

  public static void loadFromFile(LootPlugin plugin, SmartYamlConfiguration file) {
    gems.clear();
    for (String key : file.getKeys(false)) {
      if (!file.isConfigurationSection(key)) {
        continue;
      }
      try {
        String trueId = key.toLowerCase().replace(" ", "_");
        ConfigurationSection cs = file.getConfigurationSection(key);
        SocketGem gem = new SocketGem(trueId);
        gem.setName(key);
        gem.setPrefix(cs.getString("prefix", ""));
        gem.setSuffix(cs.getString("suffix", ""));
        gem.setWeight(cs.getDouble("weight"));
        gem.setBonusWeight(cs.getDouble("bonus-weight"));
        gem.setWeightPerLevel(cs.getDouble("weight-per-level"));
        var rarityWeight = Math.max(gem.getWeight(), gem.getBonusWeight());
        var unique = !(key.endsWith(" I") || key.endsWith(" II") || key.endsWith(" III") || key.endsWith(" IV"));
        if (rarityWeight <= 10) {
          gem.setTooltip(NamespacedKey.fromString("item_epic"));
          gem.setRarity(unique ? "unique_epic" : "epic");
        } else if (rarityWeight <= 50) {
          gem.setTooltip(NamespacedKey.fromString("item_rare"));
          gem.setRarity(unique ? "unique" : "rare");
        } else if (rarityWeight <= 3000) {
          gem.setTooltip(NamespacedKey.fromString("item_uncommon"));
          gem.setRarity(unique ? "unique" : "uncommon");
        } else {
          gem.setTooltip(NamespacedKey.fromString("item_common"));
          gem.setRarity(unique ? "unique" : "common");
        }
        gem.setItemGroup(ItemGroup.get(cs.getString("item-group")));
        gem.setLore(cs.getStringList("lore"));
        gem.setStats(StatUtil.getStatMapFromSection(cs.getConfigurationSection("stats")));
        gem.setPercentStats(StatUtil.getStatMapFromSection(cs.getConfigurationSection("percentage-stats")));
        gem.setTraits(getTraits(trueId, cs.getStringList("traits")));
        gem.setLoreAbilities(getPassives(trueId, cs.getStringList("passives")));
        gem.setBroadcast(cs.getBoolean("broadcast"));

        gem.buildStack();

        gems.put(trueId, gem);
      } catch (Exception e) {
        Bukkit.getLogger().warning("[Loot] Failed to load gem " + key);
        e.printStackTrace();
      }
    }
    Bukkit.getLogger().info("[Loot] Loaded " + gems.size() + " socket gems!");
  }

  private static Set<StrifeTrait> getTraits(String id, List<String> strs) {
    Set<StrifeTrait> traits = new HashSet<>();
    for (String s : strs) {
      try {
        traits.add(StrifeTrait.valueOf(s));
      } catch (Exception e) {
        Bukkit.getLogger().warning("[Loot] Invalid trait " + s + " used for gem " + id);
      }
    }
    return traits;
  }

  private static Set<LoreAbility> getPassives(String id, List<String> strs) {
    Set<LoreAbility> passives = new HashSet<>();
    for (String s : strs) {
      var la = StrifePlugin.getInstance().getLoreAbilityManager().getLoreAbilityFromId(s);
      if (la == null) {
        Bukkit.getLogger().warning("[Loot] Invalid la " + s + " used for gem " + id);
        continue;
      }
      passives.add(la);
    }
    return passives;
  }
}
