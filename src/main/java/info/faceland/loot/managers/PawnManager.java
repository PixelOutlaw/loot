package info.faceland.loot.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.PriceData;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.enchantments.EnchantmentTome;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import land.face.strife.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class PawnManager {

  private LootPlugin plugin;

  private double baseEquipmentPrice;
  private double baseGemPrice;
  private double baseTomePrice;
  private double baseScrollPrice;
  private double equipPricePerLevel;
  private double gemWeightHalf;
  private double tomeWeightHalf;
  private double scrollWeightHalf;

  private Map<Material, Double> materialPrices = new HashMap<>();
  private Map<String, Double> namedPrices = new HashMap<>();

  public PawnManager(LootPlugin plugin) {
    this.plugin = plugin;
    baseEquipmentPrice = plugin.getSettings()
        .getDouble("config.selling.base-equipment-price", 5);
    baseGemPrice = plugin.getSettings()
        .getDouble("config.selling.base-gem-price", 5);
    baseTomePrice = plugin.getSettings()
        .getDouble("config.selling.base-tome-price", 5);
    baseScrollPrice = plugin.getSettings()
        .getDouble("config.selling.base-scroll-price", 5);
    equipPricePerLevel = plugin.getSettings()
        .getDouble("config.selling.equipment-price-per-level", 0.1);
    gemWeightHalf = plugin.getSettings()
        .getDouble("config.selling.weight-per-half-gem-price", 5);
    tomeWeightHalf = plugin.getSettings()
        .getDouble("config.selling.weight-per-half-tome-price", 5);
    scrollWeightHalf = plugin.getSettings()
        .getDouble("config.selling.weight-per-half-scroll-price", 5);

    loadMaterialPrices();
    loadNamePrices();
  }

  public PriceData getPrice(ItemStack stack) {
    int amount = stack.getAmount();
    int price;
    String itemName = ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    if (namedPrices.containsKey(itemName)) {
      return new PriceData(amount * namedPrices.get(itemName).intValue(), false);
    }
    SocketGem socketGem = plugin.getSocketGemManager().getSocketGem(stack);
    if (socketGem != null) {
      double divisor = (gemWeightHalf + socketGem.getWeight()) / gemWeightHalf;
      price = (int) (baseGemPrice * 2 * Math.pow(0.5, divisor));
      return new PriceData(amount * price, socketGem.getWeight() < 100);
    }
    UpgradeScroll scroll = plugin.getScrollManager().getScroll(stack);
    if (scroll != null) {
      double divisor = (scrollWeightHalf + scroll.getWeight()) / scrollWeightHalf;
      price = (int) (baseScrollPrice * 2 * Math.pow(0.5, divisor));
      return new PriceData(amount * price, scroll.getWeight() < 100);
    }
    EnchantmentTome tome = MaterialUtil.getEnchantmentItem(stack);
    if (tome != null) {
      boolean rare;
      if (tome.getSellPrice() > 0) {
        price = (int) tome.getSellPrice();
        rare = tome.getSellPrice() >= 1000;
      } else {
        double divisor = (tomeWeightHalf + tome.getWeight()) / tomeWeightHalf;
        price = (int) (baseTomePrice * 2 * Math.pow(0.5, divisor));
        rare = tome.getWeight() < 100;
      }
      return new PriceData(amount * price, rare);
    }
    if (MaterialUtil.isEssence(stack)) {
      price = 2;
      price += (double) MaterialUtil.getEssenceLevel(stack) * 0.15;
      return new PriceData(amount * price, false);
    }
    if (MaterialUtil.getTierFromStack(stack) != null) {
      boolean unique = TextUtils.getLore(stack).get(1).contains("Unique");
      double itemLevel = MaterialUtil.getLevelRequirement(stack);
      int itemPlus = MaterialUtil.getUpgradeLevel(ItemStackExtensionsKt.getDisplayName(stack));
      price = (int) (baseEquipmentPrice + itemLevel * equipPricePerLevel);
      price *= Math.pow(1.1, itemPlus);
      return new PriceData(amount * price, itemPlus > 4 || unique);
    }
    double quality = MaterialUtil.getQuality(stack);
    if (quality > 0) {
      double itemLevel = MaterialUtil.getItemLevel(stack);
      double priceMult = 1 + (itemLevel / 100);
      price = (int) (1D * priceMult * quality);
      return new PriceData(amount * price, quality > 3);
    }
    if (materialPrices.containsKey(stack.getType())) {
      return new PriceData(amount * materialPrices.get(stack.getType()).intValue(), false);
    }
    return new PriceData(-1, false);
  }

  public void loadMaterialPrices() {
    materialPrices.clear();
    ConfigurationSection cs = plugin.getConfigYAML().getConfigurationSection("selling.material-prices");
    for (String material : cs.getKeys(false)) {
      Material matty;
      try {
        matty = Material.valueOf(material);
      } catch (Exception e) {
        Bukkit.getLogger().warning("Unknown material for material price! " + material);
        continue;
      }
      materialPrices.put(matty, cs.getDouble(material));
    }
  }

  public void loadNamePrices() {
    namedPrices.clear();
    ConfigurationSection cs = plugin.getConfigYAML().getConfigurationSection("selling.name-prices");
    for (String name : cs.getKeys(false)) {
      namedPrices.put(name, cs.getDouble(name));
    }
  }
}
