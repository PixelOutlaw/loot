package info.faceland.loot.utils;

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import info.faceland.loot.LootPlugin;
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

  public static boolean applyEssence(EquipmentData data, StoredStat stat) {
    if (data.getBonusStats().isEmpty()) {
      return false;
    }
    int index = -1;
    for (int i = 0; i < data.getBonusStats().size(); i++) {
      if (data.getBonusStats().get(i).isEssenceSlot()) {
        index = i;
        break;
      }
    }
    if (index == -1) {
      return false;
    }
    data.getBonusStats().get(index).copyFrom(stat);
    return true;
  }

  private static ItemStack fromStoredStat(StoredStat stat, String originTier, int level, double rarityPower) {
    ItemStack essenceItem = new ItemStack(Material.GHAST_TEAR);
    stat.setOriginTier(originTier);
    stat.setCrafted(true);
    stat.setLevel(Math.max(1, level));
    stat.setEssenceLevel(Math.max(1, level - 10 + LootPlugin.RNG.nextInt(8)));
    stat.setRarityPower(rarityPower);
    stat.setStatRoll((0.5 + Math.random() * 0.5) * stat.getStatRoll());
    PersistHelper.storeEssenceStat(essenceItem, stat);
    ItemRenderer.renderEssence(essenceItem, stat);
    return essenceItem;
  }

}
