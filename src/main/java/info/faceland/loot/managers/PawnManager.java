package info.faceland.loot.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.PawnShopType;
import info.faceland.loot.data.PriceData;
import info.faceland.loot.data.pdc.StoredStat;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.Tier;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.menu.pawn.PawnMenu;
import info.faceland.loot.utils.MaterialUtil;
import info.faceland.loot.utils.ModifyEquipmentUtil;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class PawnManager {

  private final LootPlugin plugin;

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
    float price;
    String itemName = ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    if (namedPrices.containsKey(itemName)) {
      return new PriceData(amount * namedPrices.get(itemName), namedPrices.get(itemName) > 500 &&
          !(itemName.contains("Silver Bar") || itemName.contains("Gold Bar")));
    }
    SocketGem socketGem = SocketGem.get(stack);
    if (socketGem != null) {
      double divisor = (gemWeightHalf + socketGem.getWeight()) / gemWeightHalf;
      price = (int) (baseGemPrice * 2 * Math.pow(0.5, divisor));
      price = Math.max(8, price);
      return new PriceData(amount * price, socketGem.getWeight() < 100);
    }
    UpgradeScroll scroll = UpgradeScroll.get(stack);
    if (scroll != null) {
      double divisor = (scrollWeightHalf + scroll.getWeight()) / scrollWeightHalf;
      price = (int) (baseScrollPrice * 2 * Math.pow(0.5, divisor));
      return new PriceData(amount * price, scroll.getWeight() < 100);
    }
    EnchantmentTome tome = EnchantmentTome.get(stack);
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
    if ("essence".equals(PersistHelper.getItemType(stack))) {
      StoredStat stat = PersistHelper.getEssenceStat(stack);
      price = 2;
      price += (stat == null ? 1 : stat.getLevel(1)) * 0.15f;
      return new PriceData(amount * price, false);
    }
    if (Tier.get(stack) != null) {
      boolean cool = false;
      List<String> lore = TextUtils.getLore(stack);
      if (lore.size() > 1 && (lore.get(1).contains("\uD86D\uDFEA") ||
          lore.get(1).contains("\uD86D\uDFE9") ||
          lore.get(1).contains("\uD86D\uDFE8"))) {
        cool = true;
      }
      double itemLevel = MaterialUtil.getLevelRequirement(stack);
      int itemPlus = ModifyEquipmentUtil.getUpgradeLevel(stack);
      price = (int) (baseEquipmentPrice + itemLevel * equipPricePerLevel);
      price *= (float) Math.pow(1.1, itemPlus);
      return new PriceData(amount * price, itemPlus > 4 || cool);
    }
    // TODO: food logic
    if (stack.getType() == Material.KELP) {
      int rarity = 1; // (int) MaterialUtil.getRarity(stack).getPower();
      float rarityMult = 0.5f * rarity;
      String stripped = net.md_5.bungee.api.ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack));
      Matcher matcher = pattern.matcher(stripped);
      while (matcher.find()) {
        double size = Double.parseDouble(matcher.group());
        return new PriceData((int) (2 + size * 0.2 * rarityMult), rarity > 2);
      }
      return new PriceData(1, false);
    }
    ItemRarity ir = MaterialUtil.getRarity(stack);
    double rarity = 1 + ((ir == null) ? 0 : ir.getPower());
    double itemPrice = materialPrices.getOrDefault(stack.getType(), -1D);
    double itemLevel = ModifyEquipmentUtil.getItemLevel(stack);
    if (itemLevel >= 1) {
      itemPrice = Math.max(itemPrice, 1);
      itemPrice *= 1 + (itemLevel / 25);
    }
    switch ((int) rarity) {
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
