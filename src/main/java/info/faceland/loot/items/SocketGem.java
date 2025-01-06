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
import com.sk89q.worldedit.util.kyori.Key;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
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
  private int customModelData;
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

  public SocketGem(String id) {
    this.id = id;
    this.lore = new ArrayList<>();
  }

  public static SocketGem get(String id) {
    return gems.getOrDefault(id.toLowerCase().replace(" ", "_"), null);
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

    List<String> lore = new ArrayList<>();
    lore.add(PaletteUtil.color("|ns|\uF801\uF808丈\uF808\uF802"));
    // Todo: remove manual character replace
    lore.add(HALFWAY_SMALL + GUI.spacesOf(-countTitleSize(name) / 2) + FaceColor.BLACK +
        doSmallNameThing(name.replace("â", "a")) + "\uF80C");
    lore.add(centerSmallText(FaceColor.WHITE + "Usable On: " + FaceColor.CYAN + itemGroup.getName()));
    lore.add("");
    for (var stuff : percentStats.entrySet()) {
      int val = Math.round(stuff.getValue());
      if (val >= 0) {
        lore.add(centerSmallText(FaceColor.ORANGE + "+" + val + "% " + stuff.getKey().getName()));
      } else {
        lore.add(centerSmallText(FaceColor.RED.s() + (val + "% " + stuff.getKey().getName())));
      }
    }
    for (var stuff : stats.entrySet()) {
      int val = Math.round(stuff.getValue());
      if (val >= 0) {
        lore.add(centerSmallText(FaceColor.ORANGE + "+" + val + " " + stuff.getKey().getName()));
      } else {
        lore.add(centerSmallText(FaceColor.RED.s() + val + " " + stuff.getKey().getName()));
      }
    }
    for (var stuff : traits) {
      for (String s : stuff.getAdditionalText()) {
        lore.add(centerSmallText(s));
      }
    }
    for (var stuff : loreAbilities) {
      for (String s : stuff.getDescription()) {
        lore.add(centerSmallText(s));
      }
    }
    for (String s : this.lore) {
      lore.add(centerSmallText(s));
    }
    lore.add("");
    lore.addAll(List.of(
        FaceColor.LIGHT_GRAY + centerSmallText("Visit an enchanting table"),
        FaceColor.LIGHT_GRAY + centerSmallText("to use this gem on items"),
        FaceColor.LIGHT_GRAY + centerSmallText("with empty socket slots!")
    ));

    meta.setDisplayName(FaceColor.LIME + name);
    meta.setCustomModelData(customModelData);
    meta.setMaxStackSize(99);
    meta.setLore(lore);
    if (weight < 10) {
      meta.setTooltipStyle(NamespacedKey.fromString("item_epic"));
    } else if (weight < 200) {
      meta.setTooltipStyle(NamespacedKey.fromString("item_rare"));
    } else if (weight < 1000) {
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
        gem.setCustomModelData(cs.getInt("custom-model-data", 2000));
        gem.setItemGroup(ItemGroup.get(cs.getString("item-group")));
        gem.setLore(PaletteUtil.color(cs.getStringList("lore")));
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
