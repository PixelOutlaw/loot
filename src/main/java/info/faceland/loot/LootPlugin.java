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

import com.tealcube.minecraft.bukkit.facecore.logging.PluginLogger;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.shade.acf.PaperCommandManager;
import info.faceland.loot.api.managers.UniqueDropsManager;
import info.faceland.loot.builders.ItemBuilder;
import info.faceland.loot.commands.ItemModifyCommand;
import info.faceland.loot.commands.LootCommand;
import info.faceland.loot.commands.UpdateItemCommand;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.MobLootTable;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.EquipmentDetailsType;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.items.CraftMaterial;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.SoulGem;
import info.faceland.loot.items.Tier;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.AspectOfPerfection;
import info.faceland.loot.items.prefabs.DistortionCell;
import info.faceland.loot.items.prefabs.DivineHammer;
import info.faceland.loot.items.prefabs.ManaCrystal;
import info.faceland.loot.items.prefabs.OrbOfManyFaces;
import info.faceland.loot.items.prefabs.PerfectSphere;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.ShardOfFailure;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.items.prefabs.TinkerersGear;
import info.faceland.loot.listeners.EnchantDegradeListener;
import info.faceland.loot.listeners.EnchantMenuListener;
import info.faceland.loot.listeners.EntityDeathListener;
import info.faceland.loot.listeners.GayBoatFix;
import info.faceland.loot.listeners.GemSmashMenuListener;
import info.faceland.loot.listeners.GemcutterListener;
import info.faceland.loot.listeners.HeadHelmetsListener;
import info.faceland.loot.listeners.InteractListener;
import info.faceland.loot.listeners.ItemListListener;
import info.faceland.loot.listeners.ItemSpawnListener;
import info.faceland.loot.listeners.MortarListener;
import info.faceland.loot.listeners.PawnMenuListener;
import info.faceland.loot.listeners.SalvageMenuListener;
import info.faceland.loot.listeners.SoulGemListener;
import info.faceland.loot.listeners.StrifeListener;
import info.faceland.loot.listeners.VagabondEquipListener;
import info.faceland.loot.listeners.anticheat.AnticheatListener;
import info.faceland.loot.listeners.crafting.CraftingListener;
import info.faceland.loot.listeners.crafting.PreCraftListener;
import info.faceland.loot.listeners.sockets.CombinerListener;
import info.faceland.loot.managers.AnticheatManager;
import info.faceland.loot.managers.CustomItemManager;
import info.faceland.loot.managers.EnchantTomeManager;
import info.faceland.loot.managers.LootNameManager;
import info.faceland.loot.managers.LootUniqueDropsManager;
import info.faceland.loot.managers.PawnManager;
import info.faceland.loot.managers.RarityManager;
import info.faceland.loot.managers.SalvageManager;
import info.faceland.loot.managers.ScrollManager;
import info.faceland.loot.managers.SocketGemManager;
import info.faceland.loot.managers.StatManager;
import info.faceland.loot.managers.TradeMenuManager;
import info.faceland.loot.menu.gemcutter.GemcutterMenu;
import info.faceland.loot.menu.mortar.MortarMenu;
import info.faceland.loot.menu.salvage.SalvageMenu;
import info.faceland.loot.utils.DropUtil;
import info.faceland.loot.utils.EnchantUtil;
import info.faceland.loot.utils.EssenceUtil;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import land.face.strife.StrifePlugin;
import land.face.strife.data.champion.LifeSkillType;
import lombok.Getter;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public final class LootPlugin extends FacePlugin {

  private static LootPlugin instance;
  public static SecureRandom RNG;
  private PluginLogger debugPrinter;

  public static int ITEM_VERSION = 2;

  public static NamespacedKey ITEM_TYPE_KEY;
  public static NamespacedKey ITEM_EXTRA_KEY;
  public static NamespacedKey ITEM_DATA_KEY;
  public static NamespacedKey ITEM_VERSION_KEY;
  public static NamespacedKey ITEM_NO_TRADE;

  public static EquipmentDetailsType ITEM_DATA_CLASS = new EquipmentDetailsType();

  @Getter
  private MasterConfiguration settings;
  @Getter
  private VersionedSmartYamlConfiguration itemGroupsYml, rarityYAML, tierYML, socketGemsYAML, scrollsYAML,
      languageYAML, randomAffixes, configYAML, enchantmentTomesYAML, uniqueDropsYAML, materialsYml, soulGemsYml,
      mortarYAML;

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
    ITEM_DATA_KEY = new NamespacedKey(this, "loot.item_data");
    ITEM_TYPE_KEY = new NamespacedKey(this, "loot.type");
    ITEM_EXTRA_KEY = new NamespacedKey(this, "loot.item_id");
    ITEM_VERSION_KEY = new NamespacedKey(this, "loot.item_version");
    ITEM_NO_TRADE = new NamespacedKey(this, "loot.no_trade");

    RNG = new SecureRandom();
    debugPrinter = new PluginLogger(this);

    configYAML = defaultLoadConfig("config.yml");
    itemGroupsYml = defaultLoadConfig("item-groups.yml");
    rarityYAML = defaultLoadConfig("rarity.yml");
    tierYML = defaultLoadConfig("tier.yml");
    socketGemsYAML = defaultLoadConfig("socketGems.yml");
    scrollsYAML = defaultLoadConfig("scrolls.yml");
    languageYAML = defaultLoadConfig("language.yml");
    randomAffixes = defaultLoadConfig("random-affixes.yml");
    enchantmentTomesYAML = defaultLoadConfig("enchantmentTomes.yml");
    uniqueDropsYAML = defaultLoadConfig("unique-tables.yml");
    mortarYAML = defaultLoadConfig("pulverize.yml");
    materialsYml = defaultLoadConfig("materials.yml");
    soulGemsYml = defaultLoadConfig("soul-gems.yml");

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
    salvageMenu = new SalvageMenu(this);
    tradeMenuManager = new TradeMenuManager(this);

    strifePlugin = (StrifePlugin) Bukkit.getPluginManager().getPlugin("Strife");

    setupPlayerPoints();

    ItemGroup.loadFromFile(itemGroupsYml);
    statManager.loadFromFiles(fetchStats());
    loadRarities();
    Tier.loadFromFile(this, tierYML);
    loadNames();

    customItemManager.loadNewUniques(fetchNewUniques());
    loadUniqueDrops();

    tradeMenuManager.loadVillagerTrades(defaultLoadConfig("trades.yml").getConfigurationSection("trades"));
    loadMortarResults();

    SocketGem.loadFromFile(this, socketGemsYAML);
    UpgradeScroll.loadFromFile(scrollsYAML);
    CraftMaterial.buildGeneric();
    CraftMaterial.loadFromFile(materialsYml);
    SoulGem.loadFromFile(soulGemsYml);
    EnchantmentTome.loadFromFile(this, enchantmentTomesYAML);

    MaterialUtil.refreshConfig();
    EnchantUtil.refreshConfig();
    DropUtil.refresh();

    ArcaneEnhancer.rebuild();
    TinkerersGear.rebuild();
    PurifyingScroll.rebuild();
    ShardOfFailure.rebuild();
    OrbOfManyFaces.rebuild();
    SocketExtender.rebuild();
    ManaCrystal.rebuild();
    DistortionCell.rebuild();
    DivineHammer.rebuild();
    PerfectSphere.rebuild();
    AspectOfPerfection.rebuild();

    checkDealsTask = Bukkit.getScheduler().runTaskTimer(this, () ->
        getPawnManager().checkAll(), 200L, 60 * 20L);

    PaperCommandManager commandManager = new PaperCommandManager(this);
    commandManager.registerCommand(new LootCommand(this));
    commandManager.registerCommand(new ItemModifyCommand(this));
    commandManager.registerCommand(new UpdateItemCommand(this));

    commandManager.getCommandCompletions().registerCompletion("gems", c -> SocketGem.getGems().keySet());
    commandManager.getCommandCompletions().registerCompletion("tomes", c -> enchantTomeManager.getTomeIds());
    commandManager.getCommandCompletions().registerCompletion("tiers", c -> Tier.getIds());
    commandManager.getCommandCompletions().registerCompletion("rarities", c -> rarityManager.getRarityIds());
    commandManager.getCommandCompletions().registerCompletion("scrolls", c -> UpgradeScroll.getScrolls().keySet());
    commandManager.getCommandCompletions().registerCompletion("uniques", c -> customItemManager.listCustomItems());
    commandManager.getCommandCompletions().registerCompletion("materials", c -> CraftMaterial.getMaterials().keySet());
    commandManager.getCommandCompletions().registerCompletion("soulgems", c -> SoulGem.getGemList());

    Bukkit.getPluginManager().registerEvents(new EntityDeathListener(this), this);
    Bukkit.getPluginManager().registerEvents(new GayBoatFix(), this);
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
    Bukkit.getPluginManager().registerEvents(new MortarListener(this), this);
    if (strifePlugin != null) {
      Bukkit.getPluginManager().registerEvents(new StrifeListener(this), this);
      VagabondEquipListener vagabondEquipListener = new VagabondEquipListener(this);
      vagabondEquipListener.loadData(configYAML.getConfigurationSection("vagabonds"));
      Bukkit.getPluginManager().registerEvents(vagabondEquipListener, this);
    }
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
      int modelData;
      if (cm == null) {
        try {
          material = Material.valueOf(cs.getString("material", "APPLE"));
          modelData = cs.getInt("model-data", -1);
        } catch (Exception e) {
          Bukkit.getLogger().info("[Loot] Mortar bad material " + key);
          continue;
        }
      } else {
        material = cm.getMaterial();
        modelData = cm.getModelData();
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

      mortarMenu.importResult(material, modelData, rcm.item(), skillType, xp);
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

}
