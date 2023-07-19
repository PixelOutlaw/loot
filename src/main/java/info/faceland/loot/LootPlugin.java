/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot;

import com.tealcube.minecraft.bukkit.facecore.logging.PluginLogger;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.acf.PaperCommandManager;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.api.enchantments.EnchantmentTomeBuilder;
import info.faceland.loot.api.managers.*;
import info.faceland.loot.api.managers.UniqueDropsManager;
import info.faceland.loot.api.sockets.effects.SocketEffect;
import info.faceland.loot.commands.LootCommand;
import info.faceland.loot.commands.UpdateItemCommand;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.MatchMaterial;
import info.faceland.loot.data.MobLootTable;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.enchantments.EnchantmentTome;
import info.faceland.loot.enchantments.LootEnchantmentTomeBuilder;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.items.ItemBuilder;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.ShardOfFailure;
import info.faceland.loot.items.prefabs.TinkerersGear;
import info.faceland.loot.listeners.*;
import info.faceland.loot.listeners.sockets.*;
import info.faceland.loot.listeners.crafting.*;
import info.faceland.loot.listeners.anticheat.*;
import info.faceland.loot.managers.*;
import info.faceland.loot.menu.gemcutter.GemcutterMenu;
import info.faceland.loot.menu.salvage.SalvageMenu;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.sockets.SocketGemBuilder;
import info.faceland.loot.sockets.effects.LootSocketPotionEffect;
import info.faceland.loot.tier.Tier;
import info.faceland.loot.tier.TierBuilder;
import info.faceland.loot.utils.DropUtil;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import land.face.market.data.PlayerMarketState.FilterFlagA;
import land.face.strife.StrifePlugin;
import lombok.Getter;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class LootPlugin extends FacePlugin {

  private static LootPlugin instance;

  private PluginLogger debugPrinter;

  private MasterConfiguration settings;
  private VersionedSmartYamlConfiguration itemsYAML;
  private VersionedSmartYamlConfiguration materialsYAML;
  private VersionedSmartYamlConfiguration rarityYAML;
  private VersionedSmartYamlConfiguration tierYAML;
  private VersionedSmartYamlConfiguration socketGemsYAML;
  private VersionedSmartYamlConfiguration scrollsYAML;
  private VersionedSmartYamlConfiguration languageYAML;
  private VersionedSmartYamlConfiguration randomAffixes;
  private VersionedSmartYamlConfiguration configYAML;
  private VersionedSmartYamlConfiguration enchantmentTomesYAML;
  private VersionedSmartYamlConfiguration craftMaterialsYAML;
  private VersionedSmartYamlConfiguration uniqueDropsYAML;

  @Getter private ItemGroupManager itemGroupManager;
  @Getter private TierManager tierManager;
  @Getter private StatManager statManager;
  @Getter private RarityManager rarityManager;
  @Getter private LootNameManager nameManager;
  @Getter private CustomItemManager customItemManager;
  @Getter private SocketGemManager socketGemManager;
  @Getter private PawnManager pawnManager;
  @Getter private EnchantTomeManager enchantTomeManager;
  @Getter private AnticheatManager anticheatManager;
  @Getter private GemCacheManager gemCacheManager;
  @Getter private CraftMaterialManager craftMaterialManager;
  @Getter private UniqueDropsManager uniqueDropsManager;
  @Getter private SalvageManager salvageManager;
  @Getter private ScrollManager scrollManager;
  @Getter private TradeMenuManager tradeMenuManager;

  private StrifePlugin strifePlugin;

  private PlayerPointsAPI playerPointsAPI;

  private BukkitTask checkDealsTask;

  @Getter
  private GemcutterMenu gemcutterMenu;
  @Getter
  private SalvageMenu salvageMenu;

  public static LootPlugin getInstance() {
    return instance;
  }

  public LootPlugin() {
    instance = this;
  }

  @Override
  public void enable() {
    instance = this;
    debugPrinter = new PluginLogger(this);

    configYAML = defaultLoadConfig("config.yml");
    itemsYAML = defaultLoadConfig("items.yml");
    materialsYAML = defaultLoadConfig("material-to-tier.yml");
    rarityYAML = defaultLoadConfig("rarity.yml");
    tierYAML = defaultLoadConfig("tier.yml");
    socketGemsYAML = defaultLoadConfig("socketGems.yml");
    scrollsYAML = defaultLoadConfig("scrolls.yml");
    languageYAML = defaultLoadConfig("language.yml");
    randomAffixes = defaultLoadConfig("random-affixes.yml");
    enchantmentTomesYAML = defaultLoadConfig("enchantmentTomes.yml");
    craftMaterialsYAML = defaultLoadConfig("craftMaterials.yml");
    uniqueDropsYAML = defaultLoadConfig("strifeUniqueDrops.yml");

    settings = MasterConfiguration.loadFromFiles(languageYAML, configYAML);

    boolean potionTriggersEnabled = configYAML.getBoolean("socket-gems.use-potion-triggers", true);

    itemGroupManager = new LootItemGroupManager();
    tierManager = new TierManager();
    statManager = new StatManager();
    rarityManager = new RarityManager();
    nameManager = new LootNameManager();
    customItemManager = new CustomItemManager();
    socketGemManager = new SocketGemManager(this);
    pawnManager = new PawnManager(this);
    enchantTomeManager = new EnchantTomeManager();
    anticheatManager = new AnticheatManager();
    if (potionTriggersEnabled) {
      gemCacheManager = new LootGemCacheManager(this);
    }
    craftMaterialManager = new CraftMaterialManager();
    uniqueDropsManager = new LootUniqueDropsManager();
    salvageManager = new SalvageManager(this);
    scrollManager = new ScrollManager();

    gemcutterMenu = new GemcutterMenu(this);
    salvageMenu = new SalvageMenu(this);
    tradeMenuManager = new TradeMenuManager(this);

    setupPlayerPoints();

    loadItemGroups();
    statManager.loadFromFiles(fetchStats());
    loadRarities();
    loadTiers();
    // Load material to tier AFTER tiers or this will not work...
    loadCraftMaterials();
    loadMaterialToTierMapping();
    loadNames();
    customItemManager.loadFromFiles(fetchUniques());
    loadSocketGems();
    loadEnchantmentStones();
    loadUniqueDrops();
    loadScrolls();
    tradeMenuManager.loadVillagerTrades(defaultLoadConfig("trades.yml").getConfigurationSection("trades"));

    MaterialUtil.refreshConfig();
    DropUtil.refresh();

    ArcaneEnhancer.rebuild();
    TinkerersGear.rebuild();
    PurifyingScroll.rebuild();
    ShardOfFailure.rebuild();

    checkDealsTask = Bukkit.getScheduler().runTaskTimer(this, () ->
        getPawnManager().checkAll(), 200L, 60 * 20L);

    strifePlugin = (StrifePlugin) Bukkit.getPluginManager().getPlugin("Strife");

    PaperCommandManager commandManager = new PaperCommandManager(this);
    commandManager.registerCommand(new LootCommand(this));
    commandManager.registerCommand(new UpdateItemCommand(this));

    commandManager.getCommandCompletions()
        .registerCompletion("gems", c -> socketGemManager.getGemIds());
    commandManager.getCommandCompletions()
        .registerCompletion("tomes", c -> enchantTomeManager.getTomeIds());
    commandManager.getCommandCompletions()
        .registerCompletion("tiers", c -> tierManager.getTierIds());
    commandManager.getCommandCompletions()
        .registerCompletion("rarities", c -> rarityManager.getRarityIds());
    commandManager.getCommandCompletions()
        .registerCompletion("scrolls", c -> scrollManager.getScrollIds());
    commandManager.getCommandCompletions()
        .registerCompletion("uniques", c -> customItemManager.listCustomItems());

    Bukkit.getPluginManager().registerEvents(new EntityDeathListener(this), this);
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
    Bukkit.getPluginManager().registerEvents(new SoulGemListener(), this);
    Bukkit.getPluginManager().registerEvents(new PawnMenuListener(this), this);
    Bukkit.getPluginManager().registerEvents(new SalvageMenuListener(this), this);
    Bukkit.getPluginManager().registerEvents(new ItemSpawnListener(), this);
    if (potionTriggersEnabled) {
      Bukkit.getPluginManager().registerEvents(new SocketsListener(gemCacheManager), this);
    }
    if (strifePlugin != null) {
      if (potionTriggersEnabled) {
        Bukkit.getPluginManager().registerEvents(new StrifeListener(this), this);
      }
      VagabondEquipListener vagabondEquipListener = new VagabondEquipListener(this);
      vagabondEquipListener.loadData(configYAML.getConfigurationSection("vagabonds"));
      Bukkit.getPluginManager().registerEvents(vagabondEquipListener, this);
    }
    Bukkit.getPluginManager().registerEvents(new ContainerOpenListener(), this);
    debug("v" + getDescription().getVersion() + " enabled");
  }

  @Override
  public void disable() {
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

  public Map<EntityType, Double> fetchSpecialStatEntities() {
    Map<EntityType, Double> entityChanceMap = new HashMap<>();
    ConfigurationSection entities = configYAML.getConfigurationSection("special-stats.entities");
    for (String key : entities.getKeys(false)) {
      EntityType entityType;
      try {
        entityType = EntityType.valueOf(key);
      } catch (Exception e) {
        continue;
      }
      entityChanceMap.put(entityType, entities.getDouble(key, 0D));
    }
    return entityChanceMap;
  }

  public Map<String, Double> fetchSpecialStatWorlds() {
    Map<String, Double> worldChanceMap = new HashMap<>();
    ConfigurationSection entities = configYAML.getConfigurationSection("special-stats.worlds");
    for (String key : entities.getKeys(false)) {
      worldChanceMap.put(key, entities.getDouble(key, 0D));
    }
    return worldChanceMap;
  }

  private void loadEnchantmentStones() {
    Set<EnchantmentTome> tomes = new HashSet<>();
    List<String> loadedTomes = new ArrayList<>();
    for (String key : enchantmentTomesYAML.getKeys(false)) {
      if (!enchantmentTomesYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = enchantmentTomesYAML.getConfigurationSection(key);
      EnchantmentTomeBuilder builder = getNewEnchantmentStoneBuilder(key);
      builder.withDescription(cs.getString("description"));
      builder.withWeight(cs.getDouble("weight"));
      builder.withBonusWeight(cs.getDouble("bonus-weight"));
      builder.withLore(cs.getStringList("lore"));
      builder.withStat(cs.getString("stat", ""));
      builder.withBar(cs.getBoolean("enable-bar", true));
      builder.withSellPrice(cs.getDouble("sell-price", -1));
      builder.withBroadcast(cs.getBoolean("broadcast", false));
      List<ItemGroup> groups = new ArrayList<>();
      for (String groop : cs.getStringList("item-groups")) {
        ItemGroup g = itemGroupManager.getItemGroup(groop);
        if (g == null) {
          continue;
        }
        groups.add(g);
      }
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
      builder.withEnchantments(enchantments);
      builder.withItemGroups(groups);
      EnchantmentTome stone = builder.build();
      tomes.add(stone);
      loadedTomes.add(stone.getName());
    }
    for (EnchantmentTome es : tomes) {
      getEnchantTomeManager().addEnchantTome(es);
    }
    debug("Loaded enchantment tomes: " + loadedTomes);
  }

  public static Map<String, List<String>> staticAbuse = new HashMap<>();

  private void loadScrolls() {
    for (String key : scrollsYAML.getKeys(false)) {
      if (!scrollsYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = scrollsYAML.getConfigurationSection(key);
      UpgradeScroll scroll = new UpgradeScroll();
      scroll.setId(key);
      scroll.setPrefix(cs.getString("prefix", "CONFIGURE PREFIX FOR SCROLL" + key));
      scroll.setLore(PaletteUtil.color(cs.getStringList("lore")));
      staticAbuse.put(key, cs.getStringList("lore"));
      scroll.setBaseSuccess(cs.getDouble("base-success", 1.0));
      scroll.setFlatDecay(cs.getDouble("flat-decay", 0.01));
      scroll.setPercentDecay(cs.getDouble("percent-decay", 0.01));
      scroll.setItemDamageMultiplier(cs.getDouble("item-damage-modifier", 1.0));
      scroll.setExponent(cs.getDouble("exponent", 1.1));
      scroll.setWeight(cs.getDouble("weight", 100));
      scroll.setMinLevel(cs.getInt("min-level", 0));
      scroll.setMaxLevel(cs.getInt("max-level", 14));
      scroll.setCustomData(cs.getInt("custom-data", 100));
      scroll.setBroadcast(cs.getBoolean("broadcast", false));
      scrollManager.addScroll(key, scroll);
    }
    scrollManager.rebuildTotalWeight();
  }

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

  private void loadSocketGems() {
    for (SocketGem sg : getSocketGemManager().getSocketGems()) {
      getSocketGemManager().removeSocketGem(sg.getName());
    }
    Set<SocketGem> gems = new HashSet<>();
    List<String> loadedSocketGems = new ArrayList<>();
    for (String key : socketGemsYAML.getKeys(false)) {
      if (!socketGemsYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = socketGemsYAML.getConfigurationSection(key);
      SocketGemBuilder builder = getNewSocketGemBuilder(key);
      builder.withPrefix(cs.getString("prefix"));
      builder.withSuffix(cs.getString("suffix"));
      builder.withLore(PaletteUtil.color(cs.getStringList("lore")));
      builder.withWeight(cs.getDouble("weight"));
      builder.withDistanceWeight(cs.getDouble("distance-weight"));
      builder.withBonusWeight(cs.getDouble("bonus-weight"));
      builder.withWeightPerLevel(cs.getDouble("weight-per-level"));
      builder.withCustomModelData(cs.getInt("custom-model-data", 2000));
      List<SocketEffect> effects = new ArrayList<>();
      for (String eff : cs.getStringList("effects")) {
        effects.add(LootSocketPotionEffect.parseString(eff));
      }
      builder.withSocketEffects(effects);
      List<ItemGroup> groups = new ArrayList<>();
      for (String groop : cs.getStringList("item-groups")) {
        ItemGroup g = itemGroupManager.getItemGroup(groop);
        if (g == null) {
          continue;
        }
        groups.add(g);
      }
      builder.withItemGroups(groups);
      builder.withBroadcast(cs.getBoolean("broadcast"));
      builder.withTriggerable(cs.getBoolean("triggerable"));
      builder.withTriggerText(cs.getString("trigger-text"));
      builder.withGemType(SocketGem.GemType.fromName(cs.getString("gem-type")));

      String loreAbilityId = cs.getString("lore-ability-id");
      if (StringUtils.isNotBlank(loreAbilityId)) {
        builder.withLoreAbilityId(loreAbilityId);
      }

      SocketGem gem = builder.build();
      gems.add(gem);
      loadedSocketGems.add(gem.getName());
    }
    for (SocketGem sg : gems) {
      getSocketGemManager().addSocketGem(sg);
    }
    debug("Loaded socket gems: " + loadedSocketGems.toString());
  }

  private List<SmartYamlConfiguration> fetchUniques() {
    List<SmartYamlConfiguration> uniques = new ArrayList<>();
    File folder = new File(getDataFolder(), "uniques");
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

  private void loadItemGroups() {
    for (ItemGroup ig : getItemGroupManager().getItemGroups()) {
      getItemGroupManager().removeItemGroup(ig.getName());
    }
    Set<ItemGroup> itemGroups = new HashSet<>();
    List<String> loadedItemGroups = new ArrayList<>();
    for (String groupName : itemsYAML.getKeys(false)) {
      if ("version".equalsIgnoreCase(groupName)) {
        continue;
      }
      ItemGroup ig = new ItemGroup(groupName, false);
      int min = itemsYAML.getConfigurationSection(groupName).getInt("min-custom-data", -1);
      int max = itemsYAML.getConfigurationSection(groupName).getInt("max-custom-data", -1);
      ig.setMinimumCustomData(min);
      ig.setMaximumCustomData(max);
      ig.setTag(itemsYAML.getConfigurationSection(groupName).getString("tag", ""));
      ConfigurationSection materialsSection = itemsYAML.getConfigurationSection(groupName);
      for (String material : materialsSection.getStringList("materials")) {
        Material m = Material.getMaterial(material);
        if (m == Material.AIR) {
          continue;
        }
        ig.addMaterial(m);
      }
      itemGroups.add(ig);
      loadedItemGroups.add(groupName);
    }
    for (ItemGroup ig : itemGroups) {
      getItemGroupManager().addItemGroup(ig);
    }
    debug("Loaded item groups: " + loadedItemGroups.toString());
  }

  private void loadMaterialToTierMapping() {
    getItemGroupManager().getMatchMaterials().clear();
    for (String materialKey : materialsYAML.getKeys(false)) {
      Material material;
      try {
        material = Material.valueOf(materialKey);
      } catch (Exception e) {
        Bukkit.getLogger().warning("[Loot] Material to tier " + materialKey + " is invalid.");
        continue;
      }
      ConfigurationSection matSection = materialsYAML.getConfigurationSection(materialKey);
      for (String item : matSection.getKeys(false)) {
        ConfigurationSection cs = matSection.getConfigurationSection(item);
        MatchMaterial matchMaterial = new MatchMaterial();
        matchMaterial.setMaterial(material);
        matchMaterial.setMinCustomData(cs.getInt("min-model-data", -1));
        matchMaterial.setMaxCustomData(cs.getInt("max-model-data", -1));
        String tierString = cs.getString("tier-id");
        Tier tier = getTierManager().getTier(tierString);
        if (tier == null) {
          Bukkit.getLogger()
              .warning("[Loot] Material " + materialKey + " has invalid tier " + tierString);
          continue;
        }
        matchMaterial.setTier(tier);
        getItemGroupManager().addMatchMaterial(matchMaterial);
      }
    }
  }

  private void loadCraftMaterials() {
    ConfigurationSection validSection = craftMaterialsYAML
        .getConfigurationSection("valid-materials");
    for (String key : validSection.getKeys(false)) {
      String name = validSection.getString(key);
      Material material = Material.valueOf(key);
      getCraftMaterialManager().addCraftMaterial(material, name);
    }
    ConfigurationSection deconstructSection = craftMaterialsYAML
        .getConfigurationSection("deconstruct-overrides");
    for (String deconSection : deconstructSection.getKeys(false)) {
      ConfigurationSection section = deconstructSection.getConfigurationSection(deconSection);
      for (String key : section.getKeys(true)) {
        MatchMaterial data = new MatchMaterial();
        data.setMaterial(Material.valueOf(section.getString("material")));
        data.setMinCustomData(section.getInt("min-custom-data", -1));
        String tierString = section.getString("tier-name", "");
        Tier tier = getTierManager().getTier(tierString);
        data.setTier(tier);
        data.setMaxCustomData(section.getInt("max-custom-data", -1));
        for (String mat : section.getStringList("results")) {
          MatchMaterial.addResult(data, Material.valueOf(mat));
        }
        getCraftMaterialManager().addDeconstructData(data);
      }
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
      rarity.setColor(FaceColor.valueOf(cs.getString("color")));
      rarity.setGlowColor(ChatColor.valueOf(cs.getString("glow-color", "WHITE")));
      rarity.setAlwaysGlow(cs.getBoolean("always-glow", false));
      rarity.setAlwaysTrail(cs.getBoolean("always-trail", false));
      rarity.setWeight(cs.getDouble("weight"));
      rarity.setPower(cs.getDouble("power"));
      rarity.setMinimumBonusStats(cs.getInt("min-bonus-stats"));
      rarity.setMaximumBonusStats(cs.getInt("max-bonus-stats") + 1);
      rarity.setEnchantments(cs.getInt("enchantments"));
      rarity.setSocketChance(cs.getDouble("socket-chance"));
      rarity.setExtenderChance(cs.getDouble("extend-chance"));
      rarity.setMinimumSockets(cs.getInt("minimum-sockets"));
      rarity.setMaximumSockets(cs.getInt("maximum-sockets"));
      rarity.setLivedTicks(cs.getInt("base-ticks-lived", 0));
      getRarityManager().addRarity(key, rarity);
    }
    getRarityManager().setLowestRarityWeight();
    debug("Loaded rarities: " + getRarityManager().getLoadedRarities().toString());
  }

  private void loadTiers() {
    for (Tier t : getTierManager().getLoadedTiers()) {
      getTierManager().removeTier(t.getName());
    }
    Set<Tier> tiers = new HashSet<>();
    List<String> loadedTiers = new ArrayList<>();
    for (String key : tierYAML.getKeys(false)) {
      if (!tierYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = tierYAML.getConfigurationSection(key);
      TierBuilder builder = getNewTierBuilder(key);
      builder.withName(PaletteUtil.color(cs.getString("tier-name")));
      builder.withSkillRequirement(cs.getBoolean("skill-req", false));
      builder.withPrimaryStat(getStatManager().getLoadedStats().get(cs.getString("primary-stat")));

      List<ItemStat> secondaryStats = new ArrayList<>();
      for (String statName : cs.getStringList("secondary-stats")) {
        secondaryStats.add(getStatManager().getStat(statName));
      }
      builder.withSecondaryStats(secondaryStats);

      List<ItemStat> bonusStats = new ArrayList<>();
      for (String statName : cs.getStringList("bonus-stats")) {
        bonusStats.add(getStatManager().getStat(statName));
      }
      builder.withBonusStats(bonusStats);

      List<ItemStat> specialStats = new ArrayList<>();
      for (String statName : cs.getStringList("special-stats")) {
        specialStats.add(getStatManager().getStat(statName));
      }
      builder.withSpecialStats(specialStats);

      builder.withSpawnWeight(cs.getDouble("spawn-weight"));
      builder.withIdentifyWeight(cs.getDouble("identify-weight"));
      builder.withStartingCustomData(cs.getInt("base-custom-data", -1));
      builder.withCustomDataInterval(cs.getInt("custom-data-level-interval", 200));
      builder.withMinimumSockets(cs.getInt("min-sockets", 0));
      builder.withMaximumSockets(cs.getInt("max-sockets", 1));
      builder.withMinimumExtends(cs.getInt("min-extends", 0));
      builder.withMaximumExtends(cs.getInt("max-extends", 1));
      List<String> sl = cs.getStringList("item-groups");
      Set<ItemGroup> itemGroups = new HashSet<>();
      for (String s : sl) {
        ItemGroup ig;
        if (s.startsWith("-")) {
          ig = getItemGroupManager().getItemGroup(s.substring(1));
          if (ig == null) {
            continue;
          }
          itemGroups.add(!ig.isInverse() ? ig.getInverse() : ig);
        } else {
          ig = getItemGroupManager().getItemGroup(s);
          if (ig == null) {
            continue;
          }
          itemGroups.add(!ig.isInverse() ? ig : ig.getInverse());
        }
      }
      builder.withItemGroups(itemGroups);
      Tier t = builder.build();
      List<String> suffixes = cs.getStringList("suffixes.generic");
      for (ItemRarity r : rarityManager.getLoadedRarities().values()) {
        List<String> combined = new ArrayList<>(suffixes);
        combined.addAll(cs.getStringList("suffixes." + r.getId()));
        t.addItemSuffixes(r, combined);
      }
      loadedTiers.add(t.getId());

      String marketFilterFlag = cs.getString("filter-flag", "ALL");
      t.setFilterFlag(FilterFlagA.valueOf(marketFilterFlag));

      tiers.add(t);
    }
    debug("Loaded tiers: " + loadedTiers);
    for (Tier t : tiers) {
      getTierManager().addTier(t);
    }
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

  public TierBuilder getNewTierBuilder(String id) {
    return new TierBuilder(id);
  }

  public ItemBuilder getNewItemBuilder() {
    return new ItemBuilder(this);
  }

  public SocketGemBuilder getNewSocketGemBuilder(String name) {
    return new SocketGemBuilder(name);
  }

  public EnchantmentTomeBuilder getNewEnchantmentStoneBuilder(String name) {
    return new LootEnchantmentTomeBuilder(name);
  }

  public MasterConfiguration getSettings() {
    return settings;
  }

  public VersionedSmartYamlConfiguration getConfigYAML() {
    return configYAML;
  }

  public PlayerPointsAPI getPlayerPointsAPI() {
    return playerPointsAPI;
  }

  public StrifePlugin getStrifePlugin() {
    return strifePlugin;
  }
}
