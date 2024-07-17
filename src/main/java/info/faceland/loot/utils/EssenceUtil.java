package info.faceland.loot.utils;

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.StoredStat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EssenceUtil {

  public static ItemStack createEssence(String statId, String originTier, int level, double statRoll, double rarityPower) {
    StoredStat stat = new StoredStat(statId, statRoll);
    return fromStoredStat(stat, originTier, level, rarityPower);
  }

  public static boolean canExtractStat(ItemStack stack, boolean allowMultiple) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    return data != null && data.canExtractStat(allowMultiple);
  }

  public static boolean extract(Player p, ItemStack stack, boolean destroyItem, boolean allowMultiple,
      boolean popOffStat) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data == null) {
      return false;
    }
    StoredStat storedStat = data.extractBonusStat(allowMultiple);
    if (storedStat == null) {
      return false;
    }
    if (destroyItem) {
      stack.setAmount(0);
    } else {
      PersistHelper.setEquipmentData(stack, data);
    }
    if (p != null && popOffStat) {
      ItemStack essence = fromStoredStat(storedStat, data.getTier().getId(), data.getLevel(),
          data.getItemRarity().getPower());
      ItemUtils.giveOrDrop(p, essence);
    }
    return true;
  }

  private static ItemStack fromStoredStat(StoredStat stat, String originTier, int level, double rarityPower) {
    ItemStack essenceItem = new ItemStack(Material.GHAST_TEAR);
    stat.setOriginTier(originTier);
    stat.setCrafted(true);
    stat.setLevel(level);
    stat.setRarityPower(rarityPower);
    PersistHelper.storeEssenceStat(essenceItem, stat);
    ItemRenderer.renderEssence(essenceItem, stat);
    return essenceItem;
  }

}
