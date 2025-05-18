package info.faceland.loot.utils;

import static info.faceland.loot.LootPlugin.ITEM_DATA_CLASS;
import static info.faceland.loot.LootPlugin.ITEM_DATA_KEY;
import static info.faceland.loot.LootPlugin.ITEM_EXTRA_KEY;
import static info.faceland.loot.LootPlugin.ITEM_TYPE_KEY;
import static info.faceland.loot.LootPlugin.ITEM_VERSION;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.items.CraftMaterial;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.SoulGem;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.items.prefabs.TinkerersGear;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import land.face.foods.FoodsPlugin;
import land.face.foods.objects.RpgFood;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ConversionUtil {

  private static final List<String> OLD_UNTRADES = List
      .of("Soulbound", "Untradeable", "[Untradeable]", "[ Untradeable ]", "傝", "俎");

  protected static void attemptToAbsorbItem(ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      return;
    }
    String type = PersistHelper.getItemType(stack);
    if (!type.isEmpty() && !"unknown".equals(type)) {
      //System.out.println("-- WEIRD UPDATE SCENARIO ABORTING AAAA");
      return;
    }
    try {
      boolean locked = stack.getItemMeta().getLore().stream().anyMatch(s -> {
        for (var ss : OLD_UNTRADES) {
          if (s.contains(ss)) {
            return true;
          }
        }
        return false;
      });
      if (locked) {
        PersistHelper.setTradeLocked(stack, true);
      }
    } catch (Exception ignored) {

    }
    SocketGem gem = LootPlugin.getInstance().getSocketGemManager().getSocketGem(stack);
    if (gem != null) {
      //System.out.println("-- Detected gem!");
      stack.setType(Material.EMERALD);
      stack.setItemMeta(gem.meta());
      return;
    }
    EnchantmentTome tome = EnchantmentTome.getEnchantmentItem(stack);
    if (tome != null) {
      //System.out.println("-- Detected tome!");
      stack.setType(stack.getType());
      stack.setItemMeta(tome.meta());
      return;
    }
    UpgradeScroll scroll = LootPlugin.getInstance().getScrollManager().getScroll(stack);
    if (scroll != null) {
      //System.out.println("-- Detected scroll!");
      stack.setType(stack.getType());
      stack.setItemMeta(scroll.meta());
      return;
    }
    if (SocketExtender.isSimilar(stack)) {
      //System.out.println("-- Detected extender!");
      SocketExtender.update(stack);
      return;
    }
    if (TinkerersGear.isSimilar(stack)) {
      //System.out.println("-- Detected tinker!");
      TinkerersGear.update(stack);
      return;
    }
    if (PurifyingScroll.isSimilar(stack)) {
      //System.out.println("-- Detected purity!");
      PurifyingScroll.update(stack);
      return;
    }
    CraftMaterial cr = covertMaterial(stack);
    if (cr != null) {
      cr.sync(stack);
      return;
    }
    SoulGem soulGem = SoulGem.getSimilar(stack);
    if (soulGem != null) {
      int amount = stack.getAmount();
      stack = soulGem.item();
      stack.setAmount(amount);
      return;
    }
    RpgFood food = FoodsPlugin.getInstance().getFoodsManager().getFoodFromItem(stack);
    if (food != null) {
      RpgFood.syncStack(stack, food);
      return;
    }
    String name = ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack));
    if (StringUtils.isNotBlank(name) && !name.contains("Potion") && !name.contains(" Key")) {
      for (var nci : LootPlugin.getInstance().getCustomItemManager().getCustomItems()) {
        if (nci.getItemName().equals(name)) {
          //System.out.println("-- Detected outdated unique!");
          if (nci.getTier() != null) {
            ItemMeta meta = stack.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
            pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "equipment");
            pdc.set(ITEM_EXTRA_KEY, PersistentDataType.STRING, nci.getId());
            pdc.set(ITEM_DATA_KEY, ITEM_DATA_CLASS, new EquipmentData());
            stack.setItemMeta(meta);
          } else {
            ItemMeta meta = stack.getItemMeta();
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
            pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "unique");
            pdc.set(ITEM_EXTRA_KEY, PersistentDataType.STRING, nci.getId());
            stack.setItemMeta(meta);
          }
          ItemRenderer.renderItem(stack, true);
          return;
        }
      }
    }
    ItemMeta meta = stack.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, "unknown");
    stack.setItemMeta(meta);
    //System.out.println("-- Unknown item! Tagging as unknown until version change!");
  }

  private static CraftMaterial covertMaterial(ItemStack stack) {
    try {
      if (!CraftMaterial.VALID_MATS.contains(stack.getType())) {
        return null;
      }
      List<String> lore = TextUtils.getLore(stack);
      if (lore.size() < 2) {
        return null;
      }
      String levelString = ChatColor.stripColor(lore.get(0));
      if (!levelString.contains("Item Level: ")) {
        return null;
      }
      int level = Integer.parseInt(levelString.replace("Item Level: ", ""));
      int quality = 1;
      if (lore.get(1).contains(MaterialUtil.TAG_COMMON)) {
        quality = 1;
      } else if (lore.get(1).contains(MaterialUtil.TAG_UNCOMMON)) {
        quality = 2;
      } else if (lore.get(1).contains(MaterialUtil.TAG_RARE)) {
        quality = 3;
      } else if (lore.get(1).contains(MaterialUtil.TAG_EPIC)) {
        quality = 4;
      }
      return CraftMaterial.get(stack.getType(), level, quality);
    } catch (Exception ignored) {
      return null;
    }
  }
}
