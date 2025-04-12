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
package info.faceland.loot;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.tealcube.minecraft.bukkit.facecore.logging.PluginLogger;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.acf.PaperCommandManager;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.api.managers.UniqueDropsManager;
import info.faceland.loot.builders.ItemBuilder;
import info.faceland.loot.commands.ItemModifyCommand;
import info.faceland.loot.commands.LootCommand;
import info.faceland.loot.commands.RenameItemCommand;
import info.faceland.loot.commands.UpdateItemCommand;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.MobLootTable;
import info.faceland.loot.data.pdc.EquipmentDetailsType;
import info.faceland.loot.data.pdc.GenericStatsType;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.items.CosmeticHead;
import info.faceland.loot.items.CraftMaterial;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.LevelModItem;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.SoulGem;
import info.faceland.loot.items.Tier;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.items.prefabs.*;
import info.faceland.loot.listeners.*;
import info.faceland.loot.listeners.anticheat.AnticheatListener;
import info.faceland.loot.listeners.crafting.CraftingListener;
import info.faceland.loot.listeners.crafting.PreCraftListener;
import info.faceland.loot.listeners.sockets.CombinerListener;
import info.faceland.loot.managers.*;
import info.faceland.loot.menu.gemcutter.GemcutterMenu;
import info.faceland.loot.menu.loreglass.LoreglassMenu;
import info.faceland.loot.menu.mortar.MortarMenu;
import info.faceland.loot.menu.salvage.SalvageMenu;
import info.faceland.loot.menu.tomereader.ReaderMenu;
import info.faceland.loot.utils.DropUtil;
import info.faceland.loot.utils.EnchantUtil;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.LootUtil;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class LootPlugin extends FacePlugin {

  @Getter
  private static LootPlugin instance;
  public static SecureRandom RNG;
  private PluginLogger debugPrinter;

  public static int ITEM_VERSION = 2;

  public static NamespacedKey ITEM_TYPE_KEY;
  public static NamespacedKey ITEM_EXTRA_KEY;
  public static NamespacedKey ITEM_GENERIC_STATS_KEY;
  public static NamespacedKey ITEM_DATA_KEY;
  public static NamespacedKey ITEM_VERSION_KEY;
  public static NamespacedKey ITEM_NO_TRADE;

  public static EquipmentDetailsType ITEM_DATA_CLASS = new EquipmentDetailsType();
  public static GenericStatsType ITEM_STATS_CLASS = new GenericStatsType();

  @Getter
  private MasterConfiguration settings;
  @Getter
  private VersionedSmartYamlConfiguration itemGroupsYml, rarityYAML, socketGemsYAML, scrollsYAML, languageYAML,
      randomAffixes, configYAML, enchantmentTomesYAML, uniqueDropsYAML, materialsYml, soulGemsYml, mortarYAML,
      levelModItemsYml, cosmeticHeadYml;

  @Getter
  private StatManager statManager;
  @Getter
  private RarityManager rarityManager;
  @Getter
  private LootNameManager nameManager;
  @Getter
  private CustomItemManager customItemManager;
  @Getter
  private SocketGemManager socketGemManager;
  @Getter
  private PawnManager pawnManager;
  @Getter
  private EnchantTomeManager enchantTomeManager;
  @Getter
  private AnticheatManager anticheatManager;
  @Getter
  private UniqueDropsManager uniqueDropsManager;
  @Getter
  private SalvageManager salvageManager;
  @Getter
  private ScrollManager scrollManager;
  @Getter
  private TradeMenuManager tradeMenuManager;

  @Getter
  private StrifePlugin strifePlugin;

  @Getter
  private PlayerPointsAPI playerPointsAPI;

  private BukkitTask checkDealsTask;

  @Getter
  private GemcutterMenu gemcutterMenu;
  @Getter
  private MortarMenu mortarMenu;
  @Getter
  private LoreglassMenu loreglassMenu;
  @Getter
  private SalvageMenu salvageMenu;

  private PacketListenerCommon stackSanitizeListener;

  public LootPlugin() {
    instance = this;
  }

  @Override
  public void onLoad() {
    instance = this;
  }

  @Override
  public void enable() {
    instance = this;
    ITEM_DATA_KEY = new NamespacedKey(this, "loot.item_data");
    ITEM_TYPE_KEY = new NamespacedKey(this, "loot.type");
    ITEM_EXTRA_KEY = new NamespacedKey(this, "loot.item_id");
    ITEM_GENERIC_STATS_KEY = new NamespacedKey(this, "loot.generic_stats");
    ITEM_VERSION_KEY = new NamespacedKey(this, "loot.item_version");
    ITEM_NO_TRADE = new NamespacedKey(this, "loot.no_trade");

    RNG = new SecureRandom();
    debugPrinter = new PluginLogger(this);

    configYAML = defaultLoadConfig("config.yml");
    itemGroupsYml = defaultLoadConfig("item-groups.yml");
    rarityYAML = defaultLoadConfig("rarity.yml");
    socketGemsYAML = defaultLoadConfig("socketGems.yml");
    scrollsYAML = defaultLoadConfig("scrolls.yml");
    languageYAML = defaultLoadConfig("language.yml");
    randomAffixes = defaultLoadConfig("random-affixes.yml");
    enchantmentTomesYAML = defaultLoadConfig("enchantmentTomes.yml");
    uniqueDropsYAML = defaultLoadConfig("unique-tables.yml");
    mortarYAML = defaultLoadConfig("pulverize.yml");
    materialsYml = defaultLoadConfig("materials.yml");
    soulGemsYml = defaultLoadConfig("soul-gems.yml");
    cosmeticHeadYml = defaultLoadConfig("cosmetic-heads.yml");
    levelModItemsYml = defaultLoadConfig("level-mod-items.yml");

    settings = MasterConfiguration.loadFromFiles(languageYAML, configYAML);

    ITEM_VERSION = settings.getInt("config.item-version", 2);

    statManager = new StatManager();
    rarityManager = new RarityManager();
    nameManager = new LootNameManager();
    customItemManager = new CustomItemManager(this);
    socketGemManager = new SocketGemManager(this);
    pawnManager = new PawnManager(this);
    enchantTomeManager = new EnchantTomeManager();
    anticheatManager = new AnticheatManager();
    uniqueDropsManager = new LootUniqueDropsManager();
    salvageManager = new SalvageManager(this);
    scrollManager = new ScrollManager();

    gemcutterMenu = new GemcutterMenu(this);
    mortarMenu = new MortarMenu(this);
    loreglassMenu = new LoreglassMenu(this);
    salvageMenu = new SalvageMenu(this);
    tradeMenuManager = new TradeMenuManager(this);
    new ReaderMenu(this);

    strifePlugin = (StrifePlugin) Bukkit.getPluginManager().getPlugin("Strife");

    ItemStat.LIMIT_CATEGORY_STATS.clear();
    ItemStat.LIMIT_CATEGORY_IDS.clear();
    var cs = configYAML.getConfigurationSection("stat-categories");
    for (String key : cs.getKeys(false)) {
      Set<StrifeStat> stats = new HashSet<>();
      for (String s : cs.getStringList(key)) {
        try {
          stats.add(StrifeStat.valueOf(s));
        } catch (Exception e) {
          Bukkit.getLogger().warning("[Loot] Invalid category stat config'd! " + s);
        }
      }
      ItemStat.LIMIT_CATEGORY_STATS.put(key, stats);
      for (var stat : stats) {
        ItemStat.LIMIT_CATEGORY_IDS.putIfAbsent(stat, new HashSet<>());
        ItemStat.LIMIT_CATEGORY_IDS.get(stat).add(key);
      }
    }
    Bukkit.getLogger().info("[Loot] Category debug output:");
    for (var li : ItemStat.LIMIT_CATEGORY_STATS.entrySet()) {
      Bukkit.getLogger().info("[Loot] Stats: " + li.getKey() + " - " + li.getValue().toString());
    }
    for (var li : ItemStat.LIMIT_CATEGORY_IDS.entrySet()) {
      Bukkit.getLogger().info("[Loot] Ids: " + li.getKey() + " - " + li.getValue().toString());
    }

    setupPlayerPoints();

    ItemGroup.loadFromFile(itemGroupsYml);
    statManager.loadFromFiles(fetchStats());
    loadRarities();
    Tier.loadFromFile(this, getDataFolder());
    loadNames();

    Bukkit.getOnlinePlayers().forEach(p ->
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "recipe take " + p.getName() + " *"));
    Bukkit.clearRecipes();

    Tier.RECIPES.forEach((r, t) -> Bukkit.addRecipe(r));
    try {
      Bukkit.addRecipe(constructInfusionRecipe());
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      Bukkit.addRecipe(constructLevelModRecipe());
    } catch (Exception e) {
      e.printStackTrace();
    }

    customItemManager.loadNewUniques(fetchNewUniques());
    loadUniqueDrops();

    loadMortarResults();

    SocketGem.loadFromFile(this, socketGemsYAML);
    UpgradeScroll.loadFromFile(scrollsYAML);
    CraftMaterial.buildGeneric();
    CraftMaterial.loadFromFile(materialsYml);
    SoulGem.loadFromFile(soulGemsYml);
    CosmeticHead.loadFromFile(cosmeticHeadYml);
    LevelModItem.loadFromFile(levelModItemsYml);
    EnchantmentTome.loadFromFile(this, enchantmentTomesYAML);

    Tier.generateRandomUpgradeOptions(configYAML);

    MaterialUtil.refreshConfig();
    EnchantUtil.refreshConfig();
    DropUtil.refresh();

    ArcaneEnhancer.rebuild();
    TinkerersGear.rebuild();
    PurifyingScroll.rebuild();
    ShardOfFailure.rebuild();
    OrbOfManyFaces.rebuild();
    NormalItem.rebuild();
    SocketExtender.rebuild();
    ManaCrystal.rebuild();
    DistortionCell.rebuild();
    DivineHammer.rebuild();
    WarpedTongs.rebuild();
    PerfectSphere.rebuild();
    AspectOfPerfection.rebuild();
    TraceOfTrash.rebuild();
    OrbOfOscillation.rebuild();
    TooltipPaint.rebuild();
    UnidEnchant.rebuild();
    AwakeningStone.rebuild();
    MissingStat.rebuild();

    tradeMenuManager.loadVillagerTrades(defaultLoadConfig("trades.yml").getConfigurationSection("trades"));

    checkDealsTask = Bukkit.getScheduler().runTaskTimer(this, () ->
        getPawnManager().checkAll(), 200L, 60 * 20L);

    PaperCommandManager commandManager = new PaperCommandManager(this);
    commandManager.registerCommand(new LootCommand(this));
    commandManager.registerCommand(new ItemModifyCommand(this));
    commandManager.registerCommand(new RenameItemCommand(this));
    commandManager.registerCommand(new UpdateItemCommand(this));

    commandManager.getCommandCompletions().registerCompletion("gems", c -> SocketGem.getGems().keySet());
    commandManager.getCommandCompletions().registerCompletion("tomes", c -> enchantTomeManager.getTomeIds());
    commandManager.getCommandCompletions().registerCompletion("tiers", c -> Tier.getIds());
    commandManager.getCommandCompletions().registerCompletion("rarities", c -> rarityManager.getRarityIds());
    commandManager.getCommandCompletions().registerCompletion("scrolls", c -> UpgradeScroll.getScrolls().keySet());
    commandManager.getCommandCompletions().registerCompletion("uniques", c -> customItemManager.listCustomItems());
    commandManager.getCommandCompletions().registerCompletion("materials", c -> CraftMaterial.getMaterials().keySet());
    commandManager.getCommandCompletions().registerCompletion("soulgems", c -> SoulGem.getGemList());
    commandManager.getCommandCompletions().registerCompletion("heads", c -> CosmeticHead.getHeadList());
    commandManager.getCommandCompletions().registerCompletion("levelmoditems", c -> LevelModItem.getModsList());

    Bukkit.getPluginManager().registerEvents(new EntityDeathListener(this), this);
    Bukkit.getPluginManager().registerEvents(new GayBoatFix(), this);
    Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
    Bukkit.getPluginManager().registerEvents(new CombinerListener(this), this);
    Bukkit.getPluginManager().registerEvents(new InteractListener(this), this);
    Bukkit.getPluginManager().registerEvents(new CraftingListener(this), this);
    Bukkit.getPluginManager().registerEvents(new PreCraftListener(this), this);
    Bukkit.getPluginManager().registerEvents(new AnticheatListener(this), this);
    Bukkit.getPluginManager().registerEvents(new EnchantDegradeListener(this), this);
    Bukkit.getPluginManager().registerEvents(new GemcutterListener(this), this);
    Bukkit.getPluginManager().registerEvents(new GemSmashMenuListener(), this);
    Bukkit.getPluginManager().registerEvents(new EnchantMenuListener(), this);
    Bukkit.getPluginManager().registerEvents(new ItemListListener(this), this);
    Bukkit.getPluginManager().registerEvents(new HeadHelmetsListener(), this);
    if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
      Bukkit.getPluginManager().registerEvents(new HeadLoadListener(), this);
    }
    Bukkit.getPluginManager().registerEvents(new SoulGemListener(), this);
    Bukkit.getPluginManager().registerEvents(new PawnMenuListener(this), this);
    Bukkit.getPluginManager().registerEvents(new SalvageMenuListener(this), this);
    Bukkit.getPluginManager().registerEvents(new ItemSpawnListener(), this);
    Bukkit.getPluginManager().registerEvents(new MortarListener(this), this);
    Bukkit.getPluginManager().registerEvents(new LoreglassListener(this), this);

    // Must be after gems load
    try {
      Bukkit.addRecipe(constructTransmuteRecipe());
    } catch (Exception e) {
      e.printStackTrace();
    }

    Bukkit.getOnlinePlayers().forEach(p ->
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "recipe give " + p.getName() + " *"));

    if (strifePlugin != null) {
      Bukkit.getPluginManager().registerEvents(new StrifeListener(this), this);
      VagabondEquipListener vagabondEquipListener = new VagabondEquipListener(this);
      vagabondEquipListener.loadData(configYAML.getConfigurationSection("vagabonds"));
      Bukkit.getPluginManager().registerEvents(vagabondEquipListener, this);
    }
    if (CosmeticHead.DB_LOADED) {
      CosmeticHead.rebuild(true);
    }

    stackSanitizeListener = PacketEvents.getAPI().getEventManager()
        .registerListener(new PacketCleaner(), PacketListenerPriority.NORMAL);

    LootUtil.refresh(this);

    debug("v" + getDescription().getVersion() + " enabled");
  }

  @Override
  public void disable() {
    PacketEvents.getAPI().getEventManager().unregisterListener(stackSanitizeListener);
    checkDealsTask.cancel();
    HandlerList.unregisterAll(this);
    Bukkit.getScheduler().cancelTasks(this);
    playerPointsAPI = null;
  }

  private boolean setupPlayerPoints() {
    Plugin ppplugin = Bukkit.getPluginManager().getPlugin("PlayerPoints");
    if (ppplugin == null) {
      playerPointsAPI = null;
      return false;
    }
    playerPointsAPI = ((PlayerPoints) ppplugin).getAPI();
    return true;
  }

  public void debug(String... messages) {
    debug(Level.INFO, messages);
  }

  public void debug(Level level, String... messages) {
    if (debugPrinter != null && (settings == null || settings.getBoolean("config.debug", false))) {
      debugPrinter.log(level, Arrays.asList(messages));
    }
  }

  public static Map<String, List<String>> staticAbuse = new HashMap<>();

  private void loadUniqueDrops() {
    for (String key : uniqueDropsYAML.getKeys(false)) {
      if (!uniqueDropsYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = uniqueDropsYAML.getConfigurationSection(key);
      MobLootTable mobLootTable = new MobLootTable();
      mobLootTable.setAmountMultiplier(cs.getDouble("quantity-multiplier", 1D));
      mobLootTable.setRarityMultiplier(cs.getDouble("rarity-multiplier", 1D));
      if (cs.getConfigurationSection("gem-drops") != null) {
        for (String g : cs.getConfigurationSection("gem-drops").getKeys(false)) {
          mobLootTable.getGemMap().put(g, cs.getConfigurationSection("gem-drops").getDouble(g));
        }
      }
      if (cs.getConfigurationSection("materials") != null) {
        for (String g : cs.getConfigurationSection("materials").getKeys(false)) {
          mobLootTable.getMaterialMap().put(g, cs.getConfigurationSection("materials").getDouble(g));
        }
      }
      List<String> extraEquipment = cs.getStringList("extra-equipment");
      for (String s : extraEquipment) {
        mobLootTable.getBonusTierItems().add(rarityManager.getRarity(s));
      }
      if (cs.getConfigurationSection("tome-drops") != null) {
        for (String t : cs.getConfigurationSection("tome-drops").getKeys(false)) {
          mobLootTable.getTomeMap().put(t, cs.getConfigurationSection("tome-drops").getDouble(t));
        }
      }
      ConfigurationSection cds = cs.getConfigurationSection("custom-drops");
      if (cds != null) {
        for (String tableName : cds.getKeys(false)) {
          Map<String, Double> tableMap = new HashMap<>();
          for (String customName : cds.getConfigurationSection(tableName).getKeys(false)) {
            tableMap.put(customName, cds.getConfigurationSection(tableName).getDouble(customName));
          }
          mobLootTable.getCustomItemMap().put(tableName, tableMap);
        }
      }
      uniqueDropsManager.addData(key, mobLootTable);
    }
  }

  private void loadMortarResults() {
    for (String key : mortarYAML.getKeys(false)) {
      if (!mortarYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = mortarYAML.getConfigurationSection(key);
      CraftMaterial cm = CraftMaterial.get(cs.getString("unique", "fard"));
      Material material;
      String itemId = null;
      if (cm == null) {
        try {
          material = Material.valueOf(cs.getString("material", "APPLE"));
        } catch (Exception e) {
          Bukkit.getLogger().info("[Loot] Mortar bad material " + key);
          continue;
        }
      } else {
        material = cm.getMaterial();
        itemId = cm.getId();
      }
      CraftMaterial rcm = CraftMaterial.get(cs.getString("result", "fard"));
      if (rcm == null) {
        Bukkit.getLogger().info("[Loot] Mortar result " + cs.getString("result") + " not found!");
        continue;
      }
      String skill = cs.getString("skill", "sneed");
      float xp = (float) cs.getDouble("xp", 0);

      LifeSkillType skillType = null;
      try {
        skillType = LifeSkillType.valueOf(skill);
      } catch (Exception e) {
        Bukkit.getLogger().info("[Loot] Mortar recipe " + key + " has an invalid skill xp!");
      }

      mortarMenu.importResult(material, itemId, rcm.item(), skillType, xp);
    }
  }

  private List<SmartYamlConfiguration> fetchNewUniques() {
    List<SmartYamlConfiguration> uniques = new ArrayList<>();
    File folder = new File(getDataFolder(), "unique_items");
    File[] listOfFiles = folder.listFiles();
    for (File f : Objects.requireNonNull(listOfFiles)) {
      uniques.add(new SmartYamlConfiguration(f));
    }
    return uniques;
  }

  private List<SmartYamlConfiguration> fetchStats() {
    List<SmartYamlConfiguration> stats = new ArrayList<>();
    File folder = new File(getDataFolder(), "stats");
    File[] listOfFiles = folder.listFiles();
    for (File f : Objects.requireNonNull(listOfFiles)) {
      stats.add(new SmartYamlConfiguration(f));
    }
    return stats;
  }

  private void loadNames() {
    for (ItemRarity r : rarityManager.getLoadedRarities().values()) {
      nameManager.setPrefixes(r, randomAffixes.getStringList("prefix." + r.getId()));
      nameManager.setSuffixes(r, randomAffixes.getStringList("suffix." + r.getId()));
    }
  }

  private void loadRarities() {
    for (String rarityName : getRarityManager().getLoadedRarities().keySet()) {
      getStatManager().removeStat(rarityName);
    }
    for (String key : rarityYAML.getKeys(false)) {
      if (!rarityYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = rarityYAML.getConfigurationSection(key);
      ItemRarity rarity = new ItemRarity(key);
      rarity.setBroadcast(cs.getBoolean("broadcast"));
      rarity.setName(cs.getString("name"));
      rarity.setPlainName(cs.getString("plain-name"));
      rarity.setTooltipPlate(cs.getString("tooltip-plate"));
      var craftedPlate = cs.getString("tooltip-plate-crafted");
      rarity.setTooltipPlateCrafted(StringUtils.isNotBlank(craftedPlate) ? craftedPlate : rarity.getTooltipPlate());
      var tt = cs.getString("tooltip-id", null);
      rarity.setTooltip(tt == null ? null : NamespacedKey.fromString(tt));
      rarity.setColor(FaceColor.valueOf(cs.getString("color")));
      rarity.setGlowColor(ChatColor.valueOf(cs.getString("glow-color", "WHITE")));
      rarity.setAlwaysGlow(cs.getBoolean("always-glow", false));
      rarity.setAlwaysTrail(cs.getBoolean("always-trail", false));
      rarity.setWeight(cs.getDouble("weight"));
      rarity.setPower(cs.getDouble("power"));
      rarity.setMinimumBonusStats(cs.getInt("min-bonus-stats"));
      rarity.setMaximumBonusStats(cs.getInt("max-bonus-stats"));
      rarity.setEnchantments(cs.getInt("enchantments"));
      rarity.setSocketChance(cs.getDouble("socket-chance"));
      rarity.setExtenderChance(cs.getDouble("extend-chance"));
      rarity.setLivedTicks(cs.getInt("base-ticks-lived", 0));
      getRarityManager().addRarity(key, rarity);
    }
    getRarityManager().setLowestRarityWeight();
    debug("Loaded rarities: " + getRarityManager().getLoadedRarities().toString());
  }

  private VersionedSmartYamlConfiguration defaultLoadConfig(String fileName) {
    VersionedSmartYamlConfiguration config = new VersionedSmartYamlConfiguration(
        new File(getDataFolder(), fileName), getResource(fileName),
        VersionedConfiguration.VersionUpdateType.BACKUP_NO_UPDATE);
    if (config.update()) {
      getLogger().info("Updating " + fileName);
      debug("Updating " + fileName);
    }
    return config;
  }

  public ItemBuilder getNewItemBuilder() {
    return new ItemBuilder();
  }

  private Recipe constructInfusionRecipe() {
    NamespacedKey key = new NamespacedKey(this, "infusion_guide");

    var result = new ItemStack(Material.END_CRYSTAL);
    result.editMeta(m -> {
      m.setDisplayName(FaceColor.CYAN + "Item Essence Infusion");
      m.setLore(PaletteUtil.color(List.of(
          "|lgray|Use an |cyan|Item Essence |lgray|on a",
          "|yellow|crafted item |lgray|with an empty stat",
          "|lgray|slot to complete it!"
      )));
    });
    ItemRenderer.applyCommonFlags(result);

    ShapelessRecipe recipe = new ShapelessRecipe(key, result);

    recipe.addIngredient(Material.GHAST_TEAR);
    recipe.addIngredient(new MaterialChoice(Tier.EVERY_MAT.toArray(new Material[0])));

    return recipe;
  }

  private Recipe constructTransmuteRecipe() {
    NamespacedKey key = new NamespacedKey(this, "transmute");
    var gemb = SocketGem.get("transmute");
    if (gemb == null) {
      CraftingListener.TRANSMUTE_RECIPE = null;
      return null;
    }
    ShapelessRecipe recipe = new ShapelessRecipe(key, gemb.item());
    recipe.addIngredient(Material.EMERALD);

    CraftingListener.TRANSMUTE_RECIPE = recipe;
    return recipe;
  }

  private Recipe constructLevelModRecipe() {
    NamespacedKey key = new NamespacedKey(this, "level_mod_guide");

    var result = new ItemStack(Material.SMITHING_TABLE);
    result.editMeta(m -> {
      m.setDisplayName(FaceColor.BLUE + "Item Level Alteration");
      m.setLore(PaletteUtil.color(List.of(
          "|lgray|Using certain items, you can modify",
          "|lgray|the level of various equipment items!"
      )));
    });
    ItemRenderer.applyCommonFlags(result);

    ShapelessRecipe recipe = new ShapelessRecipe(key, result);

    recipe.addIngredient(Material.SHULKER_SHELL);
    recipe.addIngredient(new MaterialChoice(Tier.EVERY_MAT.toArray(new Material[0])));

    return recipe;
  }

}
