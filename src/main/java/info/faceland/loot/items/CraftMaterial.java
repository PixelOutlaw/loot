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
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@Getter
@Setter
public class CraftMaterial {

  public static final String TYPE_KEY = "material";

  private static Map<String, CraftMaterial> materials = new HashMap<>();
  public static List<Material> VALID_MATS = List.of(
      Material.STICK, Material.LEATHER, Material.IRON_INGOT, Material.GOLD_INGOT, Material.DIAMOND, Material.CLAY_BALL,
      Material.PAPER,Material.STRING,Material.ARROW
  );

  private String id;

  private String name;
  private Material material = Material.STICK;
  private int itemLevel = 1;
  private int quality = 1;
  private int modelData = 1;
  private List<String> description = new ArrayList<>();
  private Set<String> preferredStats = new HashSet<>();

  @Getter(value=AccessLevel.NONE)
  @Setter(value=AccessLevel.NONE)
  private ItemStack cachedStack;

  public CraftMaterial(String id) {
    this.id = id;
  }

  public static CraftMaterial get(String id) {
    return materials.getOrDefault(id.toLowerCase(), null);
  }

  public static CraftMaterial get(ItemStack stack) {
    if (!PersistHelper.getItemType(stack).equals(TYPE_KEY)) {
      return null;
    }
    return materials.getOrDefault(PersistHelper.getItemId(stack).toLowerCase(), null);
  }

  public static CraftMaterial get(Material type, int level, int quality) {
    quality = Math.max(1, Math.min(quality, 5));
    level = Math.max(1, level - (level % 5));
    String key = "generic_" + type + "_" + level + "_" + quality;
    return materials.getOrDefault(key.toLowerCase(), null);
  }

  public static Map<String, CraftMaterial> getMaterials() {
    return new HashMap<>(materials);
  }

  public ItemStack item() {
    return cachedStack.clone();
  }

  public ItemMeta meta() {
    return cachedStack.getItemMeta();
  }

  public static void update(ItemStack stack) {
    CraftMaterial mat = get(stack);
    if (mat != null) {
      int amount = stack.getAmount();
      ItemRenderer.replace(stack, mat.cachedStack.clone(), false);
      stack.setAmount(amount);
    }
  }

  public void sync(ItemStack stack) {
    stack.setType(item().getType());
    stack.setItemMeta(item().getItemMeta());
  }

  public static boolean matches(ItemStack stack) {
    return PersistHelper.getItemType(stack).equals(TYPE_KEY) &&
        materials.containsKey(PersistHelper.getItemId(stack).toLowerCase());
  }

  public static void buildGeneric() {
    buildGenericMaterial(Material.STICK, "Wood");
    buildGenericMaterial(Material.LEATHER, "Leather");
    buildGenericMaterial(Material.IRON_INGOT, "Iron");
    buildGenericMaterial(Material.GOLD_INGOT, "Gold");
    buildGenericMaterial(Material.DIAMOND, "Crystal");
    buildGenericMaterial(Material.CLAY_BALL, "Stone");
    buildGenericMaterial(Material.PAPER, "Paper");
    buildGenericMaterial(Material.STRING, "String");
    buildGenericMaterial(Material.ARROW, "Arrow");
    buildGenericMaterial(Material.EMERALD, "Emerald");
    buildGenericMaterial(Material.RED_DYE, "Ruby");
    buildGenericMaterial(Material.LAPIS_LAZULI, "Sapphire");
    buildGenericMaterial(Material.ORANGE_DYE, "Topaz");
    buildGenericMaterial(Material.PURPLE_DYE, "Amethyst");
    buildGenericMaterial(Material.BLACK_DYE, "Obsidian");
    buildGenericMaterial(Material.PINK_DYE, "Opal");
  }

  private static void buildGenericMaterial(Material material, String baseName) {
    for (int itemLevel = 0; itemLevel <= 120; itemLevel += 5) {
      int level = Math.max(itemLevel, 1);
      for (int quality = 1; quality <= 5; quality++) {
        String id = "generic_" + material.name().toLowerCase() + "_" + level + "_" + quality;
        var mat = new CraftMaterial(id);
        var name = switch (quality) {
          case 2 -> "Pretty Okay " + baseName;
          case 3 -> "Quality " + baseName;
          case 4 -> "Excellent " + baseName;
          case 5 -> "Exceptional " + baseName;
          default -> "Crappy " + baseName;
        };
        mat.setMaterial(material);
        mat.setName(name);
        mat.setModelData(2000 * quality + level);
        mat.setQuality(quality);
        mat.setItemLevel(level);
        mat.setDescription(List.of(PaletteUtil.color("|lgray|Normal crafting material")));
        mat.buildStack();
        materials.put(id.toLowerCase(), mat);
      }
    }
  }

  public static void loadFromFile(SmartYamlConfiguration file) {
    for (String key : file.getKeys(false)) {
      try {
        if (!file.isConfigurationSection(key)) {
          continue;
        }
        ConfigurationSection cs = file.getConfigurationSection(key);
        assert cs != null;
        CraftMaterial mat = CraftMaterial.loadFromConfig(key, cs);
        mat.buildStack();
        materials.put(key.toLowerCase(), mat);
      } catch (Exception e) {
        Bukkit.getLogger().warning("[Loot] Failed to load material named " + key);
        e.printStackTrace();
      }
    }
    Bukkit.getLogger().info("[Loot] Loaded " + materials.size() + " materials");
  }

  public static CraftMaterial loadFromConfig(String id, ConfigurationSection section) {
    var result = new CraftMaterial(id);
    result.setMaterial(Material.valueOf(section.getString("material", "STICK")));
    result.setName(section.getString("name", "CHANGEME"));
    result.setItemLevel(section.getInt("item-level", 1));
    result.setQuality(section.getInt("quality", 1));
    result.setModelData(section.getInt("model-data", 0));
    result.setDescription(PaletteUtil.color(section.getStringList("description")));
    result.setPreferredStats(new HashSet<>(section.getStringList("preferred-stats")));
    return result;
  }

  public static void clear() {
    materials.clear();
  }

  private void buildStack() {
    cachedStack = new ItemStack(material);
    ItemMeta meta = cachedStack.getItemMeta();
    meta.setDisplayName(FaceColor.YELLOW + name);
    meta.setCustomModelData(modelData);
    List<String> lore = new ArrayList<>();
    String symbol = switch (quality) {
      case 0, 1 -> "丁";
      case 2 -> "丂";
      case 3 -> "七";
      case 4 -> "丄";
      default -> "丅";
    };
    lore.add(PaletteUtil.color("|ns|\uF808" + symbol + "\uF808\uF801"));
    lore.add(HALFWAY_SMALL + GUI.spacesOf(-countTitleSize(name) / 2) + FaceColor.BLACK + doSmallNameThing(name) + "\uF80C");
    lore.add("");
    if (itemLevel > 0) {
      lore.add(centerSmallText(FaceColor.WHITE + "Material Level: " + itemLevel));
      lore.add("");
    } else if (itemLevel < 0 ) {
      lore.add(centerSmallText(FaceColor.WHITE + "Material Level: X"));
      lore.add("");
    }
    for (String s : description) {
      lore.add(centerSmallText(s));
    }
    meta.setLore(lore);
    meta.setMaxStackSize(99);

    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, TYPE_KEY);
    pdc.set(ITEM_EXTRA_KEY, PersistentDataType.STRING, id);
    cachedStack.setItemMeta(meta);
    ItemRenderer.applyCommonFlags(cachedStack);
  }
}
