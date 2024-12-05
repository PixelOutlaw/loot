package info.faceland.loot.utils;

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.StoredStat;
import info.faceland.loot.items.Tier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class EssenceUtil {

  public static ItemStack createEssence(String statId, String originTier, int level, double statRoll,
      double rarityPower) {
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
    if (p != null) {
      //p.sendMessage("Testresmoved: " + storedStat.toString());
      if (popOffStat) {
        ItemStack essence = fromStoredStat(storedStat, data.getTier().getId(), data.getCustomLevel(),
            data.getItemRarity().getPower());
        ItemUtils.giveOrDrop(p, essence);
      }
    }
    return true;
  }

  public static List<ItemStat> generateValidStatList(Tier tier, List<StoredStat> currentBonusStats) {
    List<ItemStat> validStats = new ArrayList<>(tier.getBonusStats());

    Map<String, Integer> categoryCount = new HashMap<>();
    List<ItemStat> currentStats = new ArrayList<>();
    for (StoredStat ss : currentBonusStats) {
      if (ss.isEssenceSlot()) {
        continue;
      }
      currentStats.add(ss.getStat());
    }
    //noinspection Convert2MethodRef
    validStats.removeIf(vs -> currentStats.contains(vs));
    for (var ts : currentStats) {
      if (!ItemStat.LIMIT_CATEGORY_IDS.containsKey(ts.getStat())) {
        continue;
      }
      for (var statId : ItemStat.LIMIT_CATEGORY_IDS.get(ts.getStat())) {
        categoryCount.put(statId, categoryCount.getOrDefault(statId, 0) + 1);
        if (categoryCount.get(statId) >= tier.getStatCategoryLimits().getOrDefault(statId, 100)) {
          validStats.removeIf(vs -> ItemStat.LIMIT_CATEGORY_STATS.get(statId).contains(vs.getStat()));
        }
      }
    }
    return validStats;
  }

  public static boolean canItemAcceptStat(StoredStat ss, EquipmentData equipmentData) {
    Map<String, Integer> categoryCount = new HashMap<>();
    if (!ItemStat.LIMIT_CATEGORY_IDS.containsKey(ss.getStat().getStat())) {
      return true;
    }
    for (var loopStat : equipmentData.getBonusStats()) {
      if (loopStat.isEssenceSlot() || !ItemStat.LIMIT_CATEGORY_IDS.containsKey(loopStat.getStat().getStat())) {
        continue;
      }
      for (var category : ItemStat.LIMIT_CATEGORY_IDS.get(loopStat.getStat().getStat())) {
        categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
      }
    }
    for (var id : ItemStat.LIMIT_CATEGORY_IDS.get(ss.getStat().getStat())) {
      if (categoryCount.getOrDefault(id, 0) + 1 >= equipmentData.getTier().getStatCategoryLimits().getOrDefault(id, 100)) {
        return false;
      }
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
    //System.out.println("roll1: " + stat.getStatRoll());
    stat.setStatRoll((0.5 + Math.random() * 0.5) * stat.getStatRoll());
    //System.out.println("roll2: " + stat.getStatRoll());
    PersistHelper.storeEssenceStat(essenceItem, stat);
    ItemRenderer.renderEssence(essenceItem, stat);
    return essenceItem;
  }

}
