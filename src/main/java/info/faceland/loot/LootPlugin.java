/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.logging.PluginLogger;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.shade.acf.PaperCommandManager;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.api.creatures.CreatureModBuilder;
import info.faceland.loot.api.creatures.MobInfo;
import info.faceland.loot.api.enchantments.EnchantmentTomeBuilder;
import info.faceland.loot.api.groups.ItemGroup;
import info.faceland.loot.api.items.CustomItem;
import info.faceland.loot.api.items.CustomItemBuilder;
import info.faceland.loot.api.items.ItemBuilder;
import info.faceland.loot.api.managers.CreatureModManager;
import info.faceland.loot.api.managers.CustomItemManager;
import info.faceland.loot.api.managers.GemCacheManager;
import info.faceland.loot.api.managers.ItemGroupManager;
import info.faceland.loot.api.managers.NameManager;
import info.faceland.loot.api.managers.RarityManager;
import info.faceland.loot.api.managers.UniqueDropsManager;
import info.faceland.loot.api.sockets.SocketGemBuilder;
import info.faceland.loot.api.sockets.effects.SocketEffect;
import info.faceland.loot.api.tier.TierBuilder;
import info.faceland.loot.commands.LootCommand;
import info.faceland.loot.creatures.LootCreatureModBuilder;
import info.faceland.loot.data.DeconstructData;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.JunkItemData;
import info.faceland.loot.data.UniqueLoot;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.enchantments.EnchantmentTome;
import info.faceland.loot.enchantments.LootEnchantmentTomeBuilder;
import info.faceland.loot.groups.LootItemGroup;
import info.faceland.loot.io.SmartTextFile;
import info.faceland.loot.items.LootCustomItemBuilder;
import info.faceland.loot.items.LootItemBuilder;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.ShardOfFailure;
import info.faceland.loot.items.prefabs.TinkerersGear;
import info.faceland.loot.listeners.DeconstructListener;
import info.faceland.loot.listeners.EnchantDegradeListener;
import info.faceland.loot.listeners.EnchantMenuListener;
import info.faceland.loot.listeners.EntityDeathListener;
import info.faceland.loot.listeners.HeadHelmetsListener;
import info.faceland.loot.listeners.InteractListener;
import info.faceland.loot.listeners.ItemListListener;
import info.faceland.loot.listeners.ItemSpawnListener;
import info.faceland.loot.listeners.PawnMenuListener;
import info.faceland.loot.listeners.StrifeListener;
import info.faceland.loot.listeners.anticheat.AnticheatListener;
import info.faceland.loot.listeners.crafting.CraftingListener;
import info.faceland.loot.listeners.sockets.CombinerListener;
import info.faceland.loot.listeners.sockets.SocketsListener;
import info.faceland.loot.managers.AnticheatManager;
import info.faceland.loot.managers.EnchantTomeManager;
import info.faceland.loot.managers.LootCraftBaseManager;
import info.faceland.loot.managers.LootCraftMatManager;
import info.faceland.loot.managers.LootCreatureModManager;
import info.faceland.loot.managers.LootCustomItemManager;
import info.faceland.loot.managers.LootGemCacheManager;
import info.faceland.loot.managers.LootItemGroupManager;
import info.faceland.loot.managers.LootNameManager;
import info.faceland.loot.managers.LootRarityManager;
import info.faceland.loot.managers.LootUniqueDropsManager;
import info.faceland.loot.managers.PawnManager;
import info.faceland.loot.managers.ScrollManager;
import info.faceland.loot.managers.SocketGemManager;
import info.faceland.loot.managers.StatManager;
import info.faceland.loot.managers.TierManager;
import info.faceland.loot.menu.pawn.PawnMenu;
import info.faceland.loot.recipe.EquipmentRecipeBuilder;
import info.faceland.loot.sockets.LootSocketGemBuilder;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.sockets.effects.LootSocketPotionEffect;
import info.faceland.loot.tier.LootTierBuilder;
import info.faceland.loot.tier.Tier;
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
import java.util.Set;
import java.util.logging.Level;
import land.face.market.data.PlayerMarketState.FilterFlagA;
import land.face.strife.StrifePlugin;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class LootPlugin extends FacePlugin {

  private static LootPlugin instance;

  private PluginLogger debugPrinter;
  private EquipmentRecipeBuilder recipeBuilder;
  private VersionedSmartYamlConfiguration itemsYAML;
  private VersionedSmartYamlConfiguration materialsYAML;
  private VersionedSmartYamlConfiguration statsYAML;
  private VersionedSmartYamlConfiguration rarityYAML;
  private VersionedSmartYamlConfiguration tierYAML;
  private VersionedSmartYamlConfiguration corestatsYAML;
  private VersionedSmartYamlConfiguration customItemsYAML;
  private VersionedSmartYamlConfiguration socketGemsYAML;
  private VersionedSmartYamlConfiguration scrollsYAML;
  private VersionedSmartYamlConfiguration languageYAML;
  private VersionedSmartYamlConfiguration configYAML;
  private VersionedSmartYamlConfiguration creaturesYAML;
  private VersionedSmartYamlConfiguration identifyingYAML;
  private VersionedSmartYamlConfiguration enchantmentTomesYAML;
  private VersionedSmartYamlConfiguration craftBasesYAML;
  private VersionedSmartYamlConfiguration craftMaterialsYAML;
  private VersionedSmartYamlConfiguration uniqueDropsYAML;
  private SmartYamlConfiguration chestsYAML;
  private MasterConfiguration settings;
  private ItemGroupManager itemGroupManager;
  private TierManager tierManager;
  private StatManager statManager;
  private RarityManager rarityManager;
  private NameManager nameManager;
  private CustomItemManager customItemManager;
  private SocketGemManager socketGemManager;
  private PawnManager pawnManager;
  private CreatureModManager creatureModManager;
  private EnchantTomeManager enchantTomeManager;
  private AnticheatManager anticheatManager;
  private GemCacheManager gemCacheManager;
  private LootCraftBaseManager lootCraftBaseManager;
  private LootCraftMatManager lootCraftMatManager;
  private UniqueDropsManager uniqueDropsManager;
  private ScrollManager scrollManager;
  private StrifePlugin strifePlugin;

  private Economy economy;
  private PlayerPointsAPI playerPointsAPI;

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

    recipeBuilder = new EquipmentRecipeBuilder(this);

    recipeBuilder.setupAllRecipes();
    configYAML = defaultLoadConfig("config.yml");
    itemsYAML = defaultLoadConfig("items.yml");
    materialsYAML = defaultLoadConfig("material-to-tier.yml");
    statsYAML = defaultLoadConfig("stats.yml");
    rarityYAML = defaultLoadConfig("rarity.yml");
    tierYAML = defaultLoadConfig("tier.yml");
    corestatsYAML = defaultLoadConfig("corestats.yml");
    customItemsYAML = defaultLoadConfig("customItems.yml");
    socketGemsYAML = defaultLoadConfig("socketGems.yml");
    scrollsYAML = defaultLoadConfig("scrolls.yml");
    languageYAML = defaultLoadConfig("language.yml");
    creaturesYAML = defaultLoadConfig("creatures.yml");
    identifyingYAML = defaultLoadConfig("identifying.yml");
    enchantmentTomesYAML = defaultLoadConfig("enchantmentTomes.yml");
    craftBasesYAML = defaultLoadConfig("craftBases.yml");
    craftMaterialsYAML = defaultLoadConfig("craftMaterials.yml");
    uniqueDropsYAML = defaultLoadConfig("strifeUniqueDrops.yml");
    chestsYAML = new SmartYamlConfiguration(new File(getDataFolder(), "chests.yml"));
    chestsYAML.load();

    settings = MasterConfiguration
        .loadFromFiles(corestatsYAML, languageYAML, configYAML, identifyingYAML);

    boolean potionTriggersEnabled = configYAML.getBoolean("socket-gems.use-potion-triggers", true);

    itemGroupManager = new LootItemGroupManager();
    tierManager = new TierManager();
    statManager = new StatManager();
    rarityManager = new LootRarityManager();
    nameManager = new LootNameManager();
    customItemManager = new LootCustomItemManager();
    socketGemManager = new SocketGemManager();
    pawnManager = new PawnManager(this);
    creatureModManager = new LootCreatureModManager();
    enchantTomeManager = new EnchantTomeManager();
    anticheatManager = new AnticheatManager();
    if (potionTriggersEnabled) {
      gemCacheManager = new LootGemCacheManager(this);
    }
    lootCraftBaseManager = new LootCraftBaseManager();
    lootCraftMatManager = new LootCraftMatManager();
    uniqueDropsManager = new LootUniqueDropsManager();
    scrollManager = new ScrollManager();

    setupEconomy();
    setupPlayerPoints();

    loadItemGroups();
    loadCraftBases();
    loadCraftMaterials();
    loadStats();
    loadRarities();
    loadTiers();
    // Load material to tier AFTER tiers or this will not work...
    loadMaterialToTierMapping();
    loadNames();
    loadCustomItems();
    loadSocketGems();
    loadEnchantmentStones();
    loadCreatureMods();
    loadUniqueDrops();
    loadScrolls();

    MaterialUtil.refreshConfig();
    DropUtil.refresh();

    ArcaneEnhancer.rebuild();
    TinkerersGear.rebuild();
    PurifyingScroll.rebuild();
    ShardOfFailure.rebuild();

    PawnMenu.clearPool();

    strifePlugin = (StrifePlugin) Bukkit.getPluginManager().getPlugin("Strife");

    PaperCommandManager commandManager = new PaperCommandManager(this);
    commandManager.registerCommand(new LootCommand(this));

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
    Bukkit.getPluginManager().registerEvents(new DeconstructListener(this), this);
    Bukkit.getPluginManager().registerEvents(new CraftingListener(this), this);
    Bukkit.getPluginManager().registerEvents(new AnticheatListener(this), this);
    Bukkit.getPluginManager().registerEvents(new EnchantDegradeListener(this), this);
    Bukkit.getPluginManager().registerEvents(new EnchantMenuListener(), this);
    Bukkit.getPluginManager().registerEvents(new ItemListListener(this), this);
    Bukkit.getPluginManager().registerEvents(new HeadHelmetsListener(), this);
    Bukkit.getPluginManager().registerEvents(new PawnMenuListener(this), this);
    Bukkit.getPluginManager().registerEvents(new ItemSpawnListener(), this);
    if (potionTriggersEnabled) {
      Bukkit.getPluginManager().registerEvents(new SocketsListener(gemCacheManager), this);
    }
    if (strifePlugin != null && potionTriggersEnabled) {
      Bukkit.getPluginManager().registerEvents(new StrifeListener(this), this);
    }
    debug("v" + getDescription().getVersion() + " enabled");
  }

  @Override
  public void disable() {
    HandlerList.unregisterAll(this);
    Bukkit.getScheduler().cancelTasks(this);

    playerPointsAPI = null;
    economy = null;
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

  private boolean setupEconomy() {
    if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
      return false;
    }
    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return false;
    }
    economy = rsp.getProvider();
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
    debug("Loaded enchantment tomes: " + loadedTomes.toString());
  }

  private void loadScrolls() {
    for (String key : scrollsYAML.getKeys(false)) {
      if (!scrollsYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = scrollsYAML.getConfigurationSection(key);
      UpgradeScroll scroll = new UpgradeScroll();
      scroll.setId(key);
      scroll.setPrefix(cs.getString("prefix", "CONFIGURE PREFIX FOR SCROLL" + key));
      scroll.setLore(TextUtils.color(cs.getStringList("lore")));
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
      UniqueLoot uniqueLoot = new UniqueLoot();
      uniqueLoot.setQuantityMultiplier(cs.getDouble("quantity-multiplier", 1D));
      uniqueLoot.setQualityMultiplier(cs.getDouble("rarity-multiplier", 1D));
      if (cs.getConfigurationSection("gem-drops") != null) {
        for (String g : cs.getConfigurationSection("gem-drops").getKeys(false)) {
          uniqueLoot.getGemMap().put(g, cs.getConfigurationSection("gem-drops").getDouble(g));
        }
      }
      List<String> extraEquipment = cs.getStringList("extra-equipment");
      for (String s : extraEquipment) {
        uniqueLoot.getBonusEquipment().add(rarityManager.getRarity(s));
      }
      if (cs.getConfigurationSection("tome-drops") != null) {
        for (String t : cs.getConfigurationSection("tome-drops").getKeys(false)) {
          uniqueLoot.getTomeMap().put(t, cs.getConfigurationSection("tome-drops").getDouble(t));
        }
      }
      ConfigurationSection cds = cs.getConfigurationSection("custom-drops");
      if (cds != null) {
        for (String tableName : cds.getKeys(false)) {
          Map<String, Double> tableMap = new HashMap<>();
          for (String customName : cds.getConfigurationSection(tableName).getKeys(false)) {
            tableMap.put(customName, cds.getConfigurationSection(tableName).getDouble(customName));
          }
          uniqueLoot.getCustomItemMap().put(tableName, tableMap);
        }
      }
      uniqueDropsManager.addData(key, uniqueLoot);
    }
  }

  private void loadCreatureMods() {
    getMobInfoManager().getMobInfo().clear();
    Set<MobInfo> mods = new HashSet<>();
    List<String> loadedMods = new ArrayList<>();
    for (String key : creaturesYAML.getKeys(false)) {
      if (!creaturesYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = creaturesYAML.getConfigurationSection(key);
      CreatureModBuilder builder = getNewCreatureModBuilder(EntityType.valueOf(key));
      for (String fieldKey : cs.getKeys(false)) {
        if (fieldKey.equals("custom-items")) {
          Map<CustomItem, Double> map = new HashMap<>();
          for (String k : cs.getConfigurationSection("custom-items").getKeys(false)) {
            if (!cs.isConfigurationSection("custom-items." + k)) {
              continue;
            }
            CustomItem ci = customItemManager.getCustomItem(k);
            if (ci == null) {
              continue;
            }
            map.put(ci, cs.getDouble("custom-items." + k));
          }
          builder.withCustomItemMults(map);
        } else if (fieldKey.equals("socket-gems")) {
          Map<SocketGem, Double> map = new HashMap<>();
          for (String k : cs.getConfigurationSection("socket-gems").getKeys(false)) {
            if (!cs.isConfigurationSection("socket-gems." + k)) {
              continue;
            }
            SocketGem sg = socketGemManager.getSocketGem(k);
            if (sg == null) {
              continue;
            }
            map.put(sg, cs.getDouble("socket-gems." + k));
          }
          builder.withSocketGemMults(map);
        } else if (fieldKey.equals("tiers")) {
          Map<Tier, Double> map = new HashMap<>();
          for (String k : cs.getConfigurationSection("tiers").getKeys(false)) {
            if (!cs.isConfigurationSection("tiers." + k)) {
              continue;
            }
            Tier t = tierManager.getTier(k);
            if (t == null) {
              continue;
            }
            map.put(t, cs.getDouble("tiers." + k));
          }
          builder.withTierMults(map);
        } else if (fieldKey.equals("enchantment-stone")) {
          Map<EnchantmentTome, Double> map = new HashMap<>();
          for (String k : cs.getConfigurationSection("enchantment-stones").getKeys(false)) {
            if (!cs.isConfigurationSection("enchantment-stones." + k)) {
              continue;
            }
            EnchantmentTome es = enchantTomeManager.getEnchantTome(k);
            if (es == null) {
              continue;
            }
            map.put(es, cs.getDouble("enchantment-stones." + k));
          }
          builder.withEnchantTomeMults(map);
        } else if (fieldKey.equals("drops")) {
          Map<String, Map<JunkItemData, Double>> map = new HashMap<>();
          ConfigurationSection worldSection = cs.getConfigurationSection("drops");
          if (worldSection == null) {
            continue;
          }
          for (String worldKey : worldSection.getKeys(false)) {
            ConfigurationSection dropSection = worldSection.getConfigurationSection(worldKey);
            if (dropSection == null) {
              map.put(worldKey, new HashMap<>());
              continue;
            }
            for (String dropKey : dropSection.getKeys(false)) {
              String matStr = dropSection.getString(dropKey + ".material");
              Material material;
              try {
                material = Material.valueOf(matStr);
              } catch (Exception e) {
                getLogger().warning("Invalid material " + matStr + " in " + key + " junk drops!");
                continue;
              }
              int min = dropSection.getInt(dropKey + ".min-amount");
              int max = dropSection.getInt(dropKey + ".max-amount");
              double chance = dropSection.getDouble(dropKey + ".chance");

              JunkItemData jid = new JunkItemData(material, min, max);

              map.putIfAbsent(worldKey, new HashMap<>());
              map.get(worldKey).put(jid, chance);
            }
          }
          builder.withJunkMap(map);
        }
      }
      MobInfo mod = builder.build();
      mods.add(mod);
      loadedMods.add(mod.getEntityType().name());
    }
    for (MobInfo cm : mods) {
      creatureModManager.addMobInfo(cm);
    }
    System.out.println("Loaded creature mods: " + loadedMods.toString());
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
      builder.withLore(cs.getStringList("lore"));
      builder.withWeight(cs.getDouble("weight"));
      builder.withDistanceWeight(cs.getDouble("distance-weight"));
      builder.withBonusWeight(cs.getDouble("bonus-weight"));
      builder.withWeightPerLevel(cs.getDouble("weight-per-level"));
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

  private void loadCustomItems() {
    for (CustomItem ci : getCustomItemManager().getCustomItems()) {
      getCustomItemManager().removeCustomItem(ci.getName());
    }
    Set<CustomItem> customItems = new HashSet<>();
    List<String> loaded = new ArrayList<>();
    for (String key : customItemsYAML.getKeys(false)) {
      if (!customItemsYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = customItemsYAML.getConfigurationSection(key);
      String matString = cs.getString("material");
      Material material;
      try {
        material = Material.valueOf(matString);
      } catch (Exception e) {
        Bukkit.getLogger().warning(
            "Invalid material " + matString + " for item " + key + "! Skipping...");
        continue;
      }
      CustomItemBuilder builder = getNewCustomItemBuilder(key, material);
      builder.withDisplayName(cs.getString("display-name"));
      builder.withLore(cs.getStringList("lore"));
      builder.withWeight(cs.getDouble("weight"));
      builder.withDistanceWeight(cs.getDouble("distance-weight"));
      builder.withLevelBase(cs.getInt("level-base"));
      builder.withLevelRange(cs.getInt("level-range"));
      builder.withCustomData(cs.getInt("custom-data-value", -1));
      builder.withBroadcast(cs.getBoolean("broadcast"));
      builder.withQuality(cs.getBoolean("can-be-quality-enhanced"));
      List<String> flags = cs.getStringList("flags");
      Set<ItemFlag> itemFlags = new HashSet<>();
      for (String s : flags) {
        itemFlags.add(ItemFlag.valueOf(s));
      }
      builder.withFlags(itemFlags);
      builder.withCanBreak(new HashSet<>(cs.getStringList("can-break")));
      CustomItem ci = builder.build();
      customItems.add(ci);
      loaded.add(ci.getName());
    }
    for (CustomItem ci : customItems) {
      getCustomItemManager().addCustomItem(ci);
    }
    debug("Loaded custom items: " + loaded.toString());
  }

  private void loadNames() {
    getNameManager().getPrefixes().clear();
    getNameManager().getSuffixes().clear();

    File prefixFile = new File(getDataFolder(), "prefix.txt");
    File suffixFile = new File(getDataFolder(), "suffix.txt");

    SmartTextFile.writeToFile(getResource("prefix.txt"), prefixFile, true);
    SmartTextFile.writeToFile(getResource("suffix.txt"), suffixFile, true);

    SmartTextFile smartPrefixFile = new SmartTextFile(prefixFile);
    SmartTextFile smartSuffixFile = new SmartTextFile(suffixFile);

    for (String s : smartPrefixFile.read()) {
      getNameManager().addPrefix(s);
    }
    for (String s : smartSuffixFile.read()) {
      getNameManager().addSuffix(s);
    }

    debug("Loaded prefixes: " + getNameManager().getPrefixes().size(),
        "Loaded suffixes: " + getNameManager()
            .getSuffixes().size());
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
      ItemGroup ig = new LootItemGroup(groupName, false);
      int min = itemsYAML.getConfigurationSection(groupName).getInt("min-custom-data", -1);
      int max = itemsYAML.getConfigurationSection(groupName).getInt("max-custom-data", -1);
      ig.setMinimumCustomData(min);
      ig.setMaximumCustomData(max);
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
    getItemGroupManager().getMaterialGroups().clear();
    for (String key : materialsYAML.getKeys(false)) {
      if (!materialsYAML.isList(key)) {
        continue;
      }
      Material m = Material.getMaterial(key);
      if (m == null || m == Material.AIR) {
        continue;
      }
      Set<Tier> tiers = new HashSet<>();
      for (String s : materialsYAML.getStringList(key)) {
        Tier tier = getTierManager().getTier(s);
        if (tier != null) {
          tiers.add(tier);
        }
      }
      getItemGroupManager().addMaterialGroup(m, tiers);
    }
  }

  private void loadCraftBases() {
    Map<Material, String> craftBases = new HashMap<>();
    for (String key : craftBasesYAML.getKeys(false)) {
      if (!craftBasesYAML.isString(key)) {
        continue;
      }
      String string = craftBasesYAML.getString(key);
      craftBases.put(Material.valueOf(key), string);
    }
    for (Material mat : craftBases.keySet()) {
      getCraftBaseManager().addCraftBase(mat, craftBases.get(mat));
    }
    debug("Loaded item groups: " + getCraftBaseManager().getCraftBases());
  }

  private void loadCraftMaterials() {
    ConfigurationSection validSection = craftMaterialsYAML
        .getConfigurationSection("valid-materials");
    for (String key : validSection.getKeys(false)) {
      String name = validSection.getString(key);
      Material material = Material.valueOf(key);
      getCraftMatManager().addCraftMaterial(material, name);
    }
    ConfigurationSection deconstructSection = craftMaterialsYAML
        .getConfigurationSection("deconstruct-overrides");
    for (String deconSection : deconstructSection.getKeys(false)) {
      ConfigurationSection section = deconstructSection.getConfigurationSection(deconSection);
      for (String key : section.getKeys(true)) {
        DeconstructData data = new DeconstructData();
        data.setMaterial(Material.valueOf(section.getString("material")));
        data.setMinCustomData(section.getInt("min-custom-data", -1));
        data.setTierName(section.getString("tier-name", ""));
        data.setMaxCustomData(section.getInt("max-custom-data", -1));
        for (String mat : section.getStringList("results")) {
          DeconstructData.addResult(data, Material.valueOf(mat));
        }
        getCraftMatManager().addDeconstructData(data);
      }
    }

    debug("Loaded item groups: " + getCraftBaseManager().getCraftBases());
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
      ItemRarity rarity = new ItemRarity();
      rarity.setBroadcast(cs.getBoolean("broadcast"));
      rarity.setName(cs.getString("name"));
      rarity.setColor(ChatColor.valueOf(cs.getString("color")));
      rarity.setWeight(cs.getDouble("weight"));
      rarity.setIdWeight(cs.getDouble("id-weight"));
      rarity.setPower(cs.getDouble("power"));
      rarity.setMinimumBonusStats(cs.getInt("min-bonus-stats"));
      rarity.setMaximumBonusStats(cs.getInt("max-bonus-stats") + 1);
      rarity.setEnchantments(cs.getInt("enchantments"));
      rarity.setMinimumSockets(cs.getInt("min-sockets"));
      rarity.setMaximumSockets(cs.getInt("max-sockets") + 1);
      rarity.setExtenderSlots(cs.getInt("extend-slots"));
      rarity.setLivedTicks(cs.getInt("base-ticks-lived", 0));
      getRarityManager().addRarity(key, rarity);
    }
    debug("Loaded rarities: " + getRarityManager().getLoadedRarities().toString());
  }

  private void loadStats() {
    for (String statName : getStatManager().getLoadedStats().keySet()) {
      getStatManager().removeStat(statName);
    }
    for (String key : statsYAML.getKeys(false)) {
      if (!statsYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = statsYAML.getConfigurationSection(key);
      ItemStat stat = new ItemStat();
      stat.setMinBaseValue(cs.getDouble("min-base-value"));
      stat.setMaxBaseValue(cs.getDouble("max-base-value"));
      stat.setPerLevelIncrease(cs.getDouble("per-level-increase"));
      stat.setPerLevelMultiplier(cs.getDouble("per-level-multiplier"));
      stat.setPerRarityIncrease(cs.getDouble("per-rarity-increase"));
      stat.setPerRarityMultiplier(cs.getDouble("per-rarity-multiplier"));
      stat.setStatString(cs.getString("stat-string"));
      stat.setStatPrefix(cs.getString("stat-prefix"));
      stat.setPerfectStatPrefix(cs.getString("perfect-stat-prefix", stat.getStatPrefix()));
      stat.setSpecialStatPrefix(cs.getString("special-stat-prefix", stat.getStatPrefix()));
      stat.setMinHue((float) cs.getDouble("min-hue", 0));
      stat.setMaxHue((float) cs.getDouble("max-hue", 0));
      stat.setMinSaturation((float) cs.getDouble("min-saturation", 0.83));
      stat.setMaxSaturation((float) cs.getDouble("max-saturation", 0.83));
      stat.setMinBrightness((float) cs.getDouble("min-brightness", 1));
      stat.setMaxBrightness((float) cs.getDouble("max-brightness", 1));
      stat.getNamePrefixes().addAll(cs.getStringList("name-prefixes"));
      getStatManager().addStat(key, stat);
    }
    debug("Loaded stats: " + getStatManager().getLoadedStats().keySet().toString());
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
      builder.withName(cs.getString("tier-name"));
      builder.withLevelRequirement(cs.getBoolean("level-req"));
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
      t.getItemSuffixes().addAll(cs.getStringList("name-suffixes"));
      loadedTiers.add(t.getName());

      String marketFilterFlag = cs.getString("filter-flag", "ALL");
      t.setFilterFlag(FilterFlagA.valueOf(marketFilterFlag));

      tiers.add(t);
    }
    debug("Loaded tiers: " + loadedTiers.toString());
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
    return new LootTierBuilder(id);
  }

  public ItemBuilder getNewItemBuilder() {
    return new LootItemBuilder(this);
  }

  public CustomItemBuilder getNewCustomItemBuilder(String name, Material material) {
    return new LootCustomItemBuilder(name, material);
  }

  public SocketGemBuilder getNewSocketGemBuilder(String name) {
    return new LootSocketGemBuilder(name);
  }

  public CreatureModBuilder getNewCreatureModBuilder(EntityType entityType) {
    return new LootCreatureModBuilder(entityType);
  }

  public EnchantmentTomeBuilder getNewEnchantmentStoneBuilder(String name) {
    return new LootEnchantmentTomeBuilder(name);
  }

  public TierManager getTierManager() {
    return tierManager;
  }

  public StatManager getStatManager() {
    return statManager;
  }

  public RarityManager getRarityManager() {
    return rarityManager;
  }

  public ItemGroupManager getItemGroupManager() {
    return itemGroupManager;
  }

  public NameManager getNameManager() {
    return nameManager;
  }

  public MasterConfiguration getSettings() {
    return settings;
  }

  public VersionedSmartYamlConfiguration getConfigYAML() {
    return configYAML;
  }

  public CustomItemManager getCustomItemManager() {
    return customItemManager;
  }

  public SocketGemManager getSocketGemManager() {
    return socketGemManager;
  }

  public PawnManager getPawnManager() {
    return pawnManager;
  }

  public CreatureModManager getMobInfoManager() {
    return creatureModManager;
  }

  public EnchantTomeManager getEnchantTomeManager() {
    return enchantTomeManager;
  }

  public AnticheatManager getAnticheatManager() {
    return anticheatManager;
  }

  public GemCacheManager getGemCacheManager() {
    return gemCacheManager;
  }

  public LootCraftBaseManager getCraftBaseManager() {
    return lootCraftBaseManager;
  }

  public LootCraftMatManager getCraftMatManager() {
    return lootCraftMatManager;
  }

  public UniqueDropsManager getUniqueDropsManager() {
    return uniqueDropsManager;
  }

  public ScrollManager getScrollManager() {
    return scrollManager;
  }

  public Economy getEconomy() {
    return economy;
  }

  public PlayerPointsAPI getPlayerPointsAPI() {
    return playerPointsAPI;
  }

  public StrifePlugin getStrifePlugin() {
    return strifePlugin;
  }
}
