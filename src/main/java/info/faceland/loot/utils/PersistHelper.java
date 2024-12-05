package info.faceland.loot.utils;

import static info.faceland.loot.LootPlugin.ITEM_DATA_CLASS;
import static info.faceland.loot.LootPlugin.ITEM_DATA_KEY;
import static info.faceland.loot.LootPlugin.ITEM_EXTRA_KEY;
import static info.faceland.loot.LootPlugin.ITEM_GENERIC_STATS_KEY;
import static info.faceland.loot.LootPlugin.ITEM_NO_TRADE;
import static info.faceland.loot.LootPlugin.ITEM_STATS_CLASS;
import static info.faceland.loot.LootPlugin.ITEM_TYPE_KEY;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;

import com.tealcube.minecraft.bukkit.shade.google.gson.Gson;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.GenericItemStats;
import info.faceland.loot.data.pdc.StoredStat;
import info.faceland.loot.items.NewCustomItem;
import info.faceland.loot.items.UniqueItemStat;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class PersistHelper {

  private static final Gson gson = new Gson();

  public static NewCustomItem getNonEquipmentUniqueItem(ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      //System.out.println("invalid material for itemId");
      return null;
    }
    var pdc = stack.getItemMeta().getPersistentDataContainer();
    if (!"unique".equals(pdc.getOrDefault(ITEM_TYPE_KEY, PersistentDataType.STRING, ""))) {
      return null;
    }
    String id = pdc.getOrDefault(ITEM_EXTRA_KEY, PersistentDataType.STRING, "");
    return LootPlugin.getInstance().getCustomItemManager().getUnique(id);
  }

  @NotNull
  public static String getItemId(ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      //System.out.println("invalid material for itemId");
      return "";
    }
    return stack.getItemMeta().getPersistentDataContainer().getOrDefault(ITEM_EXTRA_KEY, PersistentDataType.STRING, "");
  }

  @NotNull
  public static String getItemType(ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      //System.out.println("[Loot] Invalid material for itemType...");
      return "";
    }
    return stack.getItemMeta().getPersistentDataContainer().getOrDefault(ITEM_TYPE_KEY, PersistentDataType.STRING, "");
  }

  @NotNull
  public static int getItemVersion(ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      //System.out.println("invalid material for version");
      return Integer.MAX_VALUE;
    }
    return stack.getItemMeta().getPersistentDataContainer()
        .getOrDefault(ITEM_VERSION_KEY, PersistentDataType.INTEGER, 0);
  }

  @NotNull
  public static boolean isTradeLocked(ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      return false;
    }
    return stack.getItemMeta().getPersistentDataContainer().has(ITEM_NO_TRADE);
  }

  @NotNull
  public static void setTradeLocked(ItemStack stack, boolean locked) {
    if (locked) {
      if (!stack.getItemMeta().getPersistentDataContainer().has(ITEM_NO_TRADE)) {
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().set(ITEM_NO_TRADE, PersistentDataType.BOOLEAN, true);
        stack.setItemMeta(meta);
      }
    } else {
      if (stack.getItemMeta().getPersistentDataContainer().has(ITEM_NO_TRADE)) {
        ItemMeta meta = stack.getItemMeta();
        meta.getPersistentDataContainer().remove(ITEM_NO_TRADE);
        stack.setItemMeta(meta);
      }
    }
  }

  public static StoredStat getEssenceStat(ItemStack stack) {
    if ("essence".equals(getItemType(stack))) {
      try {
        return gson.fromJson(getItemId(stack), StoredStat.class);
      } catch (Exception e) {
        return null;
      }
    } else {
      return null;
    }
  }

  public static void storeEssenceStat(ItemStack stack, StoredStat storedStat) {
    try {
      ItemMeta meta = stack.getItemMeta();
      PersistentDataContainer pdc = meta.getPersistentDataContainer();
      pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "essence");
      pdc.set(ITEM_EXTRA_KEY, PersistentDataType.STRING, gson.toJson(storedStat));
      stack.setItemMeta(meta);
    } catch (Exception ignored) {

    }
  }

  public static EquipmentData getEquipmentData(ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      //System.out.println("invalid material for data");
      return null;
    }
    ItemMeta meta = stack.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    if (!pdc.has(ITEM_DATA_KEY)) {
      return null;
    }
    return pdc.get(ITEM_DATA_KEY, ITEM_DATA_CLASS);
  }

  public static void setEquipmentData(ItemStack stack, EquipmentData data) {
    setEquipmentData(stack, data, true);
  }

  public static void setEquipmentData(ItemStack stack, EquipmentData data, boolean render) {
    if (stack == null || stack.getType() == Material.AIR) {
      return;
    }
    ItemMeta meta = stack.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_DATA_KEY, ITEM_DATA_CLASS, data);
    stack.setItemMeta(meta);
    if (render) {
      ItemRenderer.renderItem(stack, true);
    }
  }

  public static GenericItemStats getGenericStats(ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      //System.out.println("invalid material for data");
      return null;
    }
    ItemMeta meta = stack.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    if (!pdc.has(ITEM_GENERIC_STATS_KEY)) {
      return null;
    }
    return pdc.get(ITEM_GENERIC_STATS_KEY, ITEM_STATS_CLASS);
  }

  public static void syncUniqueEquipment(ItemStack stack, EquipmentData data, NewCustomItem ci) {
    stack.setType(ci.getMaterial());
    ItemStackExtensionsKt.setCustomModelData(stack, ci.getCustomDataNumber());
    data.setString("name", ci.getItemName());
    data.setInt("modelData", ci.getCustomDataNumber());
    data.setLevel(ci.getItemLevel());
    data.setTier(ci.getTier());
    data.setRarity("RARE");

    data.getCoreStats().clear();
    for (UniqueItemStat stat : ci.getCoreStats()) {
      if (stat.getStat() == null) {
        continue;
      }
      StoredStat storedStat = new StoredStat(stat.getStat().getId(), 1f);
      storedStat.setUniqueMult(stat.getValueModifier(), ci.getId());
      data.getCoreStats().add(storedStat);
    }

    // Store old stats
    List<StoredStat> oldStats = new ArrayList<>(data.getBonusStats());
    data.getBonusStats().clear();

    // Remove excess stats and regenerate missing ones
    for (UniqueItemStat stat : ci.getBonusStats()) {
      if (stat.getStat() == null) {
        continue;
      }
      StoredStat storedStat = new StoredStat(stat.getStat().getId(), Math.pow(Math.random(), 2.5));
      storedStat.setUniqueMult(stat.getValueModifier(), ci.getId());
      data.getBonusStats().add(storedStat);
    }
    // restore crafted stats
    for (StoredStat oldStat : oldStats) {
      for (StoredStat newStat : data.getBonusStats()) {
        if (oldStat.getOriginalStatId().equals(newStat.getOriginalStatId())) {
          newStat.copyFrom(oldStat);
        }
      }
    }
    data.updateUniqueMultipliers(ci);

    data.sortBonusStats();

    data.setEnchantable(ci.isEnchantable());

    data.getPassives().clear();
    data.getPassives().addAll(ci.getPassives());
    data.getPassives().addAll(ci.getTraits());

    int targetSockets = ci.getSocketSlots();
    int targetExtendSlots = ci.getExtendSlots();

    int usedTotal = data.getEmptyGemSlots() + data.getSockets().size() + data.getEmptyExtendSlots();
    int targetTotal = targetSockets + targetExtendSlots;

    if (usedTotal != targetTotal) {
      if (usedTotal > targetTotal) {
        while (data.getEmptyExtendSlots() > 0 && usedTotal != targetTotal) {
          data.setEmptyExtendSlots(data.getEmptyExtendSlots() - 1);
          usedTotal = data.getEmptyGemSlots() + data.getSockets().size() + data.getEmptyExtendSlots();
        }
        while (data.getEmptyGemSlots() > 0 && usedTotal != targetTotal) {
          data.setEmptyGemSlots(data.getEmptyGemSlots() - 1);
          usedTotal = data.getEmptyGemSlots() + data.getSockets().size() + data.getEmptyExtendSlots();
        }
      } else {
        while (data.getEmptyGemSlots() + data.getSockets().size() < targetSockets) {
          data.setEmptyGemSlots(data.getEmptyGemSlots() + 1);
        }
        while (data.getEmptyGemSlots() + data.getSockets().size() + data.getEmptyExtendSlots() < targetTotal) {
          data.setEmptyExtendSlots(data.getEmptyExtendSlots() + 1);
        }
      }
    }

    ItemMeta meta = stack.getItemMeta();
    meta.getPersistentDataContainer().set(ITEM_DATA_KEY, ITEM_DATA_CLASS, data);
    stack.setItemMeta(meta);
  }

  public static boolean isEnchanted(ItemStack stack) {
    EquipmentData data = getEquipmentData(stack);
    return data != null && data.isEnchanted();
  }

  public static boolean isEnchantable(ItemStack stack) {
    EquipmentData data = getEquipmentData(stack);
    return data != null && data.canBeEnchanted();
  }

  public static int getMissingEnchantPower(ItemStack stack) {
    EquipmentData data = getEquipmentData(stack);
    return data == null ? -1 : data.getMissingEnchantPower();
  }

  public static boolean isEnhanced(ItemStack stack) {
    EquipmentData data = getEquipmentData(stack);
    return data != null && data.isEnhanced();
  }

  /*
  public static int getUpgradeLevel(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    return data == null ? 0 : data.getUpgradeLevel();
  }
  */

  public static int getItemPlus(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    return data == null ? 0 : data.getUpgradeLevel() * data.getTier().getUpgradeScale();
  }

}
