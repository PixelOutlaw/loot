/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.CustomItem;
import info.faceland.loot.api.items.ItemGenerationReason;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.tier.Tier;
import info.faceland.loot.utils.DropUtil;
import info.faceland.loot.utils.InventoryUtil;
import info.faceland.loot.utils.MaterialUtil;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.events.VagabondEquipEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class VagabondEquipListener implements Listener {

  private final LootPlugin plugin;
  private final String itemFoundFormat;
  private final Map<String, VagabondData> vagabondData = new HashMap<>();
  private final Map<LivingEntity, Boolean> vagabonds = new WeakHashMap<>();
  private final float vagabondUniqueChance;

  private final Random random = new Random();

  public VagabondEquipListener(LootPlugin plugin) {
    this.plugin = plugin;
    itemFoundFormat = plugin.getSettings().getString("language.broadcast.found-item", "");
    vagabondUniqueChance = (float) plugin.getSettings()
        .getDouble("config.vagabond-unique-chance", 0);
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onVagabondEquip(VagabondEquipEvent event) {
    if (!vagabondData.containsKey(event.getCombatClass())) {
      return;
    }
    VagabondData data = vagabondData.get(event.getCombatClass());
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      int upgradeLevel = 3;
      while (upgradeLevel < 15 && random.nextFloat() < 0.5) {
        upgradeLevel++;
      }
      ItemStack item = null;
      if (random.nextFloat() < vagabondUniqueChance) {
        List<CustomItem> potentialItems = buildPotentialItems(data, slot, event.getLevel());
        if (potentialItems.size() > 1) {
          CustomItem customItem = selectItemByWeight(potentialItems);
          item = customItem.toItemStack(1);
        }
      }
      if (item == null) {
        ItemRarity rarity = plugin.getRarityManager().getRandomRarity(1, 2.0);
        // TODO: this is ugly
        String selectedTier = data.getTierList().get(slot)
            .get(random.nextInt(data.getTierList().get(slot).size()));
        Tier tier = plugin.getTierManager().getTier(selectedTier);
        item = plugin.getNewItemBuilder()
            .withItemGenerationReason(ItemGenerationReason.MONSTER)
            .withTier(tier)
            .withRarity(rarity)
            .withLevel(event.getLevel())
            .withDistortion(Math.random() < 0.05)
            .withCreator(null)
            .build()
            .getStack();
      }
      DropUtil.upgradeItem(item, upgradeLevel);
      ItemMeta m = item.getItemMeta();
      m.setUnbreakable(true);
      item.setItemMeta(m);
      event.getLivingEntity().getEquipment().setItem(slot, item);
      event.getLivingEntity().getEquipment().setDropChance(slot, 0f);
    }
    vagabonds.put(event.getLivingEntity(), true);
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onVagabondDeath(EntityDeathEvent event) {
    if (!vagabonds.containsKey(event.getEntity())) {
      return;
    }
    StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager()
        .getStatMob(event.getEntity());
    Player killer = mob.getTopDamager();
    if (killer == null) {
      killer = event.getEntity().getKiller();
      if (killer == null) {
        event.getDrops().clear();
        return;
      }
    }

    for (EquipmentSlot slot : EquipmentSlot.values()) {
      if (Math.random() > 0.3) {
        continue;
      }
      ItemStack stack = event.getEntity().getEquipment().getItem(slot);
      if (stack == null || stack.getType() == Material.AIR) {
        continue;
      }
      ItemMeta m = stack.getItemMeta();
      m.setUnbreakable(false);
      stack.setItemMeta(m);

      int rarity = MaterialUtil.getItemRarity(stack);
      boolean announce = MaterialUtil.getUpgradeLevel(stack) > 6 || rarity > 3;
      Color dropRgb = rarity == 4 ? FaceColor.RED.getRawColor() : FaceColor.PURPLE.getRawColor();
      ChatColor glow = rarity == 4 ? ChatColor.RED : ChatColor.DARK_PURPLE;

      ItemUtils.dropItem(event.getEntity().getLocation(), stack, killer, 0, dropRgb, glow, true);
      if (announce) {
        InventoryUtil.sendToDiscord(killer, stack, itemFoundFormat);
      }
    }
  }

  public void loadData(ConfigurationSection section) {
    vagabondData.clear();
    for (String classKey : section.getKeys(false)) {
      VagabondData data = new VagabondData(classKey);
      ConfigurationSection tierSection = section.getConfigurationSection(classKey + ".tiers");
      if (tierSection == null) {
        Bukkit.getLogger().warning("[Loot] No tier section for vagabond class " + classKey);
        Bukkit.getLogger().warning("[Loot] Skipping...");
        continue;
      }
      ConfigurationSection uniqueSection = section.getConfigurationSection(classKey + ".uniques");
      if (uniqueSection == null) {
        Bukkit.getLogger().warning("[Loot] No unique section for vagabond class " + classKey);
        Bukkit.getLogger().warning("[Loot] Skipping...");
        continue;
      }
      for (String slotKey : tierSection.getKeys(false)) {
        EquipmentSlot slot = EquipmentSlot.valueOf(slotKey);
        data.getTierList().put(slot, tierSection.getStringList(slotKey));
      }
      for (String slotKey : uniqueSection.getKeys(false)) {
        EquipmentSlot slot = EquipmentSlot.valueOf(slotKey);
        data.getUniquesList().put(slot, uniqueSection.getStringList(slotKey));
      }
      vagabondData.put(classKey, data);
    }
  }

  private List<CustomItem> buildPotentialItems(VagabondData data, EquipmentSlot slot, int level) {
    List<CustomItem> options = new ArrayList<>();
    for (String s : data.getUniquesList().get(slot)) {
      CustomItem ci = plugin.getCustomItemManager().getCustomItem(s);
      if (level > ci.getLevelBase() + ci.getLevelRange() ||
          level < ci.getLevelBase() - ci.getLevelRange()) {
        continue;
      }
      options.add(ci);
    }
    return options;
  }

  private CustomItem selectItemByWeight(List<CustomItem> options) {
    float totalWeight = 0;
    for (CustomItem ci : options) {
      totalWeight += ci.getWeight();
    }
    float selectorValue = random.nextFloat() * totalWeight;
    float currentWeight = 0;
    for (CustomItem ci : options) {
      currentWeight += ci.getWeight();
      if (currentWeight >= selectorValue) {
        return ci;
      }
    }
    Bukkit.getLogger().warning("[Loot] Somehow, unique weight for vagabonds has failed.");
    return null;
  }

  public static class VagabondData {

    @Getter
    private final String id;
    @Getter
    private final Map<EquipmentSlot, List<String>> tierList = new HashMap<>();
    @Getter
    private final Map<EquipmentSlot, List<String>> uniquesList = new HashMap<>();

    public VagabondData(String id) {
      this.id = id;
    }
  }
}
