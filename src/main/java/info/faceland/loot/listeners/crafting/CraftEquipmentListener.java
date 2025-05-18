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
package info.faceland.loot.listeners.crafting;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static info.faceland.loot.items.Tier.TIER_BY_RESULT;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.ItemGenerationReason;
import info.faceland.loot.data.BuiltItem;
import info.faceland.loot.data.CraftResultData;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.events.LootCraftEvent;
import info.faceland.loot.items.Tier;
import info.faceland.loot.utils.CraftingUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.ItemStack;

public final class CraftEquipmentListener implements Listener {

  private final LootPlugin plugin;

  private final double CRAFT_EXP;
  private final double CRAFT_LEVEL_MULT;
  private final double CRAFT_QUALITY_MULT;

  public CraftEquipmentListener(LootPlugin plugin) {
    this.plugin = plugin;
    CRAFT_EXP = plugin.getSettings().getDouble("config.crafting.base-craft-exp", 1);
    CRAFT_LEVEL_MULT = plugin.getSettings().getDouble("config.crafting.craft-item-level-mult", 0.01);
    CRAFT_QUALITY_MULT = plugin.getSettings().getDouble("config.crafting.craft-rarity-mult", 0.1);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onSpecialCraftEquipment(CraftItemEvent event) {
    Tier tier = null;
    for (var entry : TIER_BY_RESULT.entrySet()) {
      if (entry.getKey().isSimilar(event.getRecipe().getResult())) {
        tier = entry.getValue();
        break;
      }
    }
    if (tier == null) {
      return;
    }
    Player player = (Player) event.getWhoClicked();
    if (player.getItemOnCursor().getType() != Material.AIR) {
      //sendMessage(player, plugin.getSettings().getString("language.craft.no-weird", ""));
      sendMessage(player, plugin.getSettings().getString("language.craft.no-cursor", ""));
      event.setCancelled(true);
      return;
    }
    if (event.getAction() == InventoryAction.NOTHING) {
      sendMessage(player, plugin.getSettings().getString("language.craft.no-weird", ""));
      event.setCancelled(true);
      return;
    }
    if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT ||
        event.getClick() == ClickType.NUMBER_KEY || event.isShiftClick()) {
      sendMessage(player, plugin.getSettings().getString("language.craft.no-shift", ""));
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onCraftEquipmentBlocked(InventoryClickEvent event) {
    if (event.getRawSlot() == 0 && event.getSlot() == 0 && event.getSlotType() == SlotType.RESULT) {
      if (event.getClick() == ClickType.CONTROL_DROP || event.isShiftClick()) {
        MessageUtils.sendMessage(event.getWhoClicked(), "&e&oSorry, this crafting operation is blocked! Try again with a normal click!");
        event.setCancelled(true);
        return;
      }
      if (event.getAction() == InventoryAction.DROP_ONE_SLOT && event.getCursor().getType() != Material.AIR) {
        MessageUtils.sendMessage(event.getWhoClicked(), "&e&oSorry, this crafting operation is blocked! Try again with a normal click!");
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onCraftEquipment(CraftItemEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (event.isShiftClick()) {
      event.setCancelled(true);
      return;
    }
    ItemStack resultStack = event.getCurrentItem();
    if (resultStack != null && resultStack.getType() == Material.EMERALD) {
      return;
    }
    for (ItemStack is : event.getInventory().getMatrix()) {
      if (is == null) {
        continue;
      }
      if (is.getType() == Material.GHAST_TEAR || is.getType() == Material.SHULKER_SHELL ||
          isDyeEvent(is.getType(), resultStack.getType())) {
        return;
      }
    }
    event.setCancelled(true);

    Player player = (Player) event.getWhoClicked();

    // TODO: IMPROVE, INEFFICIENT
    Tier tier = null;
    for (var entry : TIER_BY_RESULT.entrySet()) {
      if (entry.getKey().isSimilar(event.getRecipe().getResult())) {
        tier = entry.getValue();
        break;
      }
    }
    if (tier == null) {
      Bukkit.getLogger().warning("[Loot] Attempted to craft item with unknown tier... " + resultStack.getType());
      return;
    }

    String name = ItemStackExtensionsKt.getDisplayName(event.getRecipe().getResult());
    if (StringUtils.isBlank(name) || !org.bukkit.ChatColor.stripColor(name).contains("Equipment Crafting")) {
      Bukkit.getLogger().warning("[Loot] Attempted to craft tiered item with wrong craft title... " + resultStack.getType());
      return;
    }

    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.CRAFTING, true);
    CraftResultData crData = new CraftResultData(event.getInventory().getMatrix());
    int itemLevel = (int) (crData.getItemLevel() == -1 ? player.getLevel() : crData.getItemLevel());

    itemLevel = Math.max(1, Math.min(100, itemLevel - LootPlugin.RNG.nextInt(4)));

    int stars;
    int difference = data.getLevelWithBonus() - itemLevel;
    int craftBonusStars = difference / 5;
    int qualityStars = (int) crData.getRarity() * 2;
    if (craftBonusStars >= 0) {
      craftBonusStars += SkillRank.check(SkillRank.JOURNEYMAN, data.getLevel()) ? 1 : 0;
      craftBonusStars += SkillRank.check(SkillRank.MASTER, data.getLevel()) ? 1 : 0;
      craftBonusStars = Math.min(10, craftBonusStars);
      stars = Math.min(20, qualityStars + craftBonusStars);
    } else {
      stars = qualityStars;
    }
    craftBonusStars = Math.clamp(craftBonusStars, -19, 10);
    int minRarity, maxRarity;
    if (craftBonusStars >= 0) {
      if (stars <= 4) {
        minRarity = 0;
      } else if (stars <= 8) {
        minRarity = 1;
      } else if (stars <= 16) {
        minRarity = 2;
      } else {
        minRarity = 3;
      }
      maxRarity = 3;
    } else {
      if (qualityStars <= 4) {
        minRarity = 0;
      } else if (qualityStars <= 8) {
        minRarity = 1;
      } else if (qualityStars <= 16) {
        minRarity = 2;
      } else {
        minRarity = 3;
      }
      if (craftBonusStars <= -16) {
        maxRarity = 0;
      } else if (craftBonusStars <= -12) {
        maxRarity = 1;
      } else if (craftBonusStars <= -4) {
        maxRarity = 2;
      } else {
        maxRarity = 3;
      }
      if (minRarity > maxRarity) {
        minRarity = maxRarity;
      }
    }

    float bonus = craftBonusStars >= 0 ?
        1.25f + craftBonusStars * 0.03f + qualityStars * 0.06f :
        1.25f - craftBonusStars * 0.01f;

    ItemRarity rarity = plugin.getRarityManager().getRandomRarity(bonus, minRarity);
    if (rarity.getPower() > maxRarity) {
      rarity = plugin.getRarityManager().getRarityByPower(maxRarity);
    }

    float emptyStatSlot = 0;
    if (!SkillRank.check(SkillRank.APPRENTICE, data.getLevel())) {
      float essMult = SkillRank.check(SkillRank.EXPERT, data.getLevel()) ? 2.0f : 1.0f;
      emptyStatSlot = 0.04f;
      emptyStatSlot += 0.002f * qualityStars;
      if (craftBonusStars >= 0) {
        emptyStatSlot += (0.02f / 10f) * craftBonusStars * essMult;
      } else {
        emptyStatSlot /= -craftBonusStars;
      }
      emptyStatSlot = Math.max(0, emptyStatSlot);
    }

    BuiltItem builtItem = plugin.getNewItemBuilder()
        .withItemGenerationReason(ItemGenerationReason.CRAFTING)
        .withTier(tier)
        .withRarity(rarity)
        .withPreferredStats(crData.getPreferredStats())
        .withLevel(itemLevel)
        .withEmptyStatChance(emptyStatSlot)
        .withCreator(player)
        .build();

    ItemStack newResult = builtItem.getStack();

    event.setCurrentItem(newResult);
    event.setCancelled(false);

    int cappedItemLevel = Math.min(itemLevel, data.getLevel() + 12);
    double exp = CRAFT_EXP * crData.getNumMaterials() * 0.25;
    exp *= 1 + (cappedItemLevel * CRAFT_LEVEL_MULT);
    exp *= 1 + (crData.getRarity() * CRAFT_QUALITY_MULT);
    exp *= CraftingUtil.levelPenaltyMult(data.getLevel(), crData.getItemLevel());

    LootCraftEvent craftEvent = new LootCraftEvent(player, newResult);
    Bukkit.getPluginManager().callEvent(craftEvent);

    double finalExp = exp;
    Bukkit.getScheduler().runTaskLater(plugin, () ->
        plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
            LifeSkillType.CRAFTING, finalExp, false, false), 0L);

    player.playSound(player.getEyeLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_ANVIL_FALL, 0.5F, 1F);
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void CRAFT_SECURITY(CraftItemEvent event) {
    if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER
        || event.getCursor().getType() == Material.BARRIER) {
      event.setCancelled(true);
    }
    Player player = (Player) event.getWhoClicked();
    if (player.getItemOnCursor().getType() != Material.AIR) {
      if (!player.getItemOnCursor().isSimilar(event.getRecipe().getResult())) {
        sendMessage(player, plugin.getSettings().getString("language.craft.no-cursor", ""));
        event.setCancelled(true);
      }
    }
  }

  private boolean isDyeEvent(Material ingredient, Material result) {
    return ingredient == result && (ingredient == Material.LEATHER_HELMET
        || ingredient == Material.LEATHER_CHESTPLATE || ingredient == Material.LEATHER_LEGGINGS
        || ingredient == Material.LEATHER_BOOTS);
  }
}
