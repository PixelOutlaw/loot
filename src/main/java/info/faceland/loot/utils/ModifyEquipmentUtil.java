package info.faceland.loot.utils;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.StoredStat;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.Tier;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import land.face.strife.data.champion.SkillRank;
import org.bukkit.inventory.ItemStack;

public class ModifyEquipmentUtil {

  public static boolean applyCraftedStat(ItemStack stack, StoredStat storedStat) {

    return false;
  }

  public static void upgrade(ItemStack stack, int amount) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      data.setUpgradeLevel(data.getUpgradeLevel() + amount);
      PersistHelper.setEquipmentData(stack, data);
    }
  }

  public static int getUpgradeLevel(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      return data.getUpgradeLevel();
    }
    return 0;
  }

  public static boolean canUpgrade(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data == null || !data.getBoolean("upgradeable", true)) {
      return false;
    }
    if (data.getTier() == null || data.getTier().getUpgradeScale() > 15) {
      return false;
    }
    return true;
  }

  public static void setCanUpgrade(ItemStack stack, boolean upgradeable) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      data.setBoolean("upgradeable", upgradeable);
    }
  }

  public static Tier getTier(@Nullable ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    return data == null ? null : data.getTier();
  }

  public static int getItemLevel(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    return data == null ? 0 : data.getCustomLevel();
  }

  public static void enhance(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      if (data.enhance()) {
        PersistHelper.setEquipmentData(stack, data);
      }
    }
  }

  public static void reroll(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      data.reroll();
      PersistHelper.setEquipmentData(stack, data);
    }
  }

  public static void distort(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      if (data.distort()) {
        PersistHelper.setEquipmentData(stack, data);
      }
    }
  }

  public static void undistort(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      if (data.undistort()) {
        PersistHelper.setEquipmentData(stack, data);
      }
    }
  }

  public static void tickEnchantment(ItemStack stack, int amount) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      boolean changeState = data.tickEnchant(amount);
      if (!changeState) {
        return;
      }
      PersistHelper.setEquipmentData(stack, data);
    }
  }

  public static void refillEnchantment(ItemStack stack, int amount) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      data.refillEnchantment(amount);
      PersistHelper.setEquipmentData(stack, data);
    }
  }

  public static void unenchant(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      if (data.unenchant()) {
        PersistHelper.setEquipmentData(stack, data);
      }
    }
  }

  public static boolean enchant(ItemStack stack, EnchantmentTome tome, int level, float rarityPower, SkillRank rank) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      if (tome == null) {
        return false;
      }
      ItemStat stat = LootPlugin.getInstance().getStatManager().getStat(tome.getStat());
      if (stat == null) {
        return false;
      }
      StoredStat storedStat = new StoredStat(tome.getStat(), Math.random());
      storedStat.setLevel(level);
      storedStat.setRarityPower(rarityPower);
      int enchantPoints = 10 + LootPlugin.RNG.nextInt(20);
      if (SkillRank.isRank(rank, SkillRank.APPRENTICE)) {
        enchantPoints += 20;
      }
      boolean success = data.enchant(storedStat, enchantPoints);
      if (!success) {
        return false;
      }
      PersistHelper.setEquipmentData(stack, data);
      return true;
    }
    return false;
  }

  public static float identify(ItemStack stack, SkillRank loremasterRank) {
    float idPoints = 0;
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      if (SkillRank.check(loremasterRank, data.getCustomLevel())) {
        for (var s : data.getBonusStats()) {
          if (!s.isIdentified()) {
            s.setIdentified(true);
            idPoints++;
            if (StringUtils.isNotBlank(s.getOriginUnique())) {
              idPoints += 0.5f;
              s.setShowOrigin(true);
            }
          }
        }
      }
      var enchant = data.getEnchantment();
      if (enchant != null && SkillRank.check(loremasterRank, enchant.getLevel() - 10)) {
        if (!enchant.isIdentified()) {
          enchant.setIdentified(true);
          idPoints++;
        }
      }
    }
    PersistHelper.setEquipmentData(stack, data, true);
    return idPoints;
  }

  public static void applyExtender(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      boolean success = data.extend();
      if (success) {
        PersistHelper.setEquipmentData(stack, data);
      }
    }
  }

  public static void applySocket(ItemStack stack, String socketId) {
    SocketGem gem = SocketGem.get(socketId);
    if (gem == null) {
      return;
    }
    applySocket(stack, gem);
  }

  public static void applySocket(ItemStack stack, SocketGem gem) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data == null) {
      return;
    }
    boolean success = data.socket(gem);
    if (success) {
      PersistHelper.setEquipmentData(stack, data);
    }
  }

  public static SocketGem removeSocket(ItemStack stack, int index) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      SocketGem gem = data.extractGem(index);
      PersistHelper.setEquipmentData(stack, data);
      return gem;
    }
    return null;
  }

  public static List<SocketGem> getSockets(ItemStack stack) {
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data == null) {
      return new ArrayList<>();
    }
    return data.getSocketGems();
  }
}
