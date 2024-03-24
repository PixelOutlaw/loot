package info.faceland.loot.utils;

import static info.faceland.loot.LootPlugin.ITEM_LEVEL_KEY;
import static info.faceland.loot.LootPlugin.ITEM_TRADEABLE_KEY;
import static info.faceland.loot.LootPlugin.ITEM_TYPE_KEY;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;

import info.faceland.loot.LootPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class PersistHelper {

  public static void checkItemVersion(ItemStack stack) {
    if (stack == null) {
      return;
    }
    ItemMeta meta = stack.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    int currentVersion = pdc.getOrDefault(ITEM_VERSION_KEY, PersistentDataType.INTEGER, 0);
    if (currentVersion < LootPlugin.itemVersion) {
      //forceUpdate(stack);
    }
  }

  public static String getItemType(ItemStack stack) {
    if (stack == null || !stack.hasItemMeta()) {
      return "";
    }
    PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
    return pdc.getOrDefault(ITEM_TYPE_KEY, PersistentDataType.STRING, "");
  }

  public static int getItemLevel(ItemStack stack) {
    if (stack == null || !stack.hasItemMeta()) {
      return 0;
    }
    PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
    return pdc.getOrDefault(ITEM_LEVEL_KEY, PersistentDataType.INTEGER, 0);
  }

  public static boolean getItemTradeable(ItemStack stack) {
    if (stack == null || !stack.hasItemMeta()) {
      return true;
    }
    PersistentDataContainer pdc = stack.getItemMeta().getPersistentDataContainer();
    return pdc.getOrDefault(ITEM_TRADEABLE_KEY, PersistentDataType.BOOLEAN, true);
  }

  public static void setItemType(ItemStack stack, String type) {
    if (stack == null) {
      return;
    }
    ItemMeta meta = stack.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, type);
    stack.setItemMeta(meta);
  }

  public static void setItemLevel(ItemStack stack, int level) {
    if (stack == null) {
      return;
    }
    ItemMeta meta = stack.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.INTEGER, level);
    stack.setItemMeta(meta);
  }

  public static void setItemTradeableKey(ItemStack stack, boolean canTrade) {
    if (stack == null) {
      return;
    }
    ItemMeta meta = stack.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.BOOLEAN, canTrade);
    stack.setItemMeta(meta);
  }

}
