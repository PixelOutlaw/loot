package info.faceland.loot.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.PawnShopType;
import info.faceland.loot.data.PriceData;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.enchantments.EnchantmentTome;
import info.faceland.loot.menu.pawn.PawnMenu;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import land.face.strife.util.ItemUtil;
import lombok.Getter;
import org.apache.commons.lang3.text.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PawnManager {

  private LootPlugin plugin;

  private final double baseEquipmentPrice;
  private final double baseGemPrice;
  private final double baseTomePrice;
  private final double baseScrollPrice;
  private final double equipPricePerLevel;
  private final double gemWeightHalf;
  private final double tomeWeightHalf;
  private final double scrollWeightHalf;

  @Getter
  private final Map<String, PawnShopType> pawnTypes = new HashMap<>();
  private final Map<Material, Double> materialPrices = new HashMap<>();
  private final Map<String, Double> namedPrices = new HashMap<>();

  Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

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

    pawnTypes.put("fishmonger", new PawnShopType("fishmonger"));
  }

  public PriceData getPrice(ItemStack stack) {
    int amount = stack.getAmount();
    int price;
    String itemName = ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    if (namedPrices.containsKey(itemName)) {
      return new PriceData(amount * namedPrices.get(itemName), namedPrices.get(itemName) > 500 &&
          !(itemName.contains("Silver Bar") || itemName.contains("Gold Bar")));
    }
    SocketGem socketGem = plugin.getSocketGemManager().getSocketGem(stack);
    if (socketGem != null) {
      double divisor = (gemWeightHalf + socketGem.getWeight()) / gemWeightHalf;
      price = (int) (baseGemPrice * 2 * Math.pow(0.5, divisor));
      price = Math.max(8, price);
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
      boolean cool = false;
      List<String> lore = TextUtils.getLore(stack);
      if (lore.size() > 1 && (lore.get(1).contains("\uD86D\uDFEA") ||
          lore.get(1).contains("\uD86D\uDFE9") ||
          lore.get(1).contains("\uD86D\uDFE8"))) {
        cool = true;
      }
      double itemLevel = MaterialUtil.getLevelRequirement(stack);
      int itemPlus = MaterialUtil.getUpgradeLevel(ItemStackExtensionsKt.getDisplayName(stack));
      price = (int) (baseEquipmentPrice + itemLevel * equipPricePerLevel);
      price *= Math.pow(1.1, itemPlus);
      return new PriceData(amount * price, itemPlus > 4 || cool);
    }
    if (stack.getType() == Material.KELP) {
      int rarity = MaterialUtil.getItemRarity(stack);
      float rarityMult = 0.5f * rarity;
      String stripped = net.md_5.bungee.api.ChatColor.stripColor(
          ItemStackExtensionsKt.getDisplayName(stack));
      Matcher matcher = pattern.matcher(stripped);
      while (matcher.find()) {
        double size = Double.parseDouble(matcher.group());
        return new PriceData((int) (2 + size * 0.2 * rarityMult), rarity > 2);
      }
      return new PriceData(1, false);
    }
    double quality = MaterialUtil.getQuality(stack);
    double itemPrice = materialPrices.getOrDefault(stack.getType(), -1D);
    double itemLevel = MaterialUtil.getItemLevel(stack);
    if (itemLevel >= 1) {
      itemPrice = Math.max(itemPrice, 1);
      itemPrice *= 1 + (itemLevel / 25);
    }
    switch ((int) quality) {
      case 1 -> itemPrice = Math.max(0, itemPrice);
      case 2 -> itemPrice += 0.35;
      case 3 -> {
        itemPrice *= 1.75;
        itemPrice += 3;
      }
      case 4 -> {
        itemPrice *= 3;
        itemPrice += 5;
      }
    }
    return new PriceData(Math.max(-1, itemPrice * amount), false);
  }

  public PawnMenu getPawnMenu(String dealId) {
    if (StringUtils.isBlank(dealId)) {
      return new PawnMenu(plugin);
    }
    return new PawnMenu(plugin, dealId);
  }

  public void checkAll() {
    for (PawnShopType type : pawnTypes.values()) {
      type.checkDealChange();
    }
  }

  public void loadMaterialPrices() {
    materialPrices.clear();
    ConfigurationSection cs = plugin.getConfigYAML().getConfigurationSection("selling.material-prices");
    for (String material : cs.getKeys(false)) {
      Material matty;
      try {
        matty = Material.valueOf(material);
      } catch (Exception e) {
        Bukkit.getLogger().warning("[Loot] Unknown material for material price! " + material);
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
