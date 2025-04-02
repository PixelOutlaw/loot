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

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.ItemGenerationReason;
import info.faceland.loot.data.BuiltItem;
import info.faceland.loot.data.CraftResultData;
import info.faceland.loot.events.LootCraftEvent;
import info.faceland.loot.items.Tier;
import info.faceland.loot.utils.CraftingUtil;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.regex.Pattern;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.util.PlayerDataUtil;
import net.md_5.bungee.api.ChatColor;
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
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

public final class CraftingListener implements Listener {

  private final LootPlugin plugin;

  public static String ESSENCE_SLOT_TEXT;

  private final double CRAFT_EXP;
  private final double CRAFT_LEVEL_MULT;
  private final double CRAFT_QUALITY_MULT;

  public static Recipe TRANSMUTE_RECIPE = null;

  private static final Pattern pattern = Pattern.compile("[^A-za-z]");

  public CraftingListener(LootPlugin plugin) {
    this.plugin = plugin;

    CRAFT_EXP = plugin.getSettings().getDouble("config.crafting.base-craft-exp", 1);
    CRAFT_LEVEL_MULT = plugin.getSettings()
        .getDouble("config.crafting.craft-item-level-mult", 0.01);
    CRAFT_QUALITY_MULT = plugin.getSettings().getDouble("config.crafting.craft-rarity-mult", 0.1);
    ESSENCE_SLOT_TEXT = plugin.getSettings().getString("config.crafting.essence-text", "(Essence Slot)");

    String badInfuseName = StringExtensionsKt.chatColorize(plugin.getSettings()
        .getString("language.essence.invalid-title", "&cCannot Use Essence!"));

    ItemStack failStack = new ItemStack(Material.BARRIER);
    ItemStackExtensionsKt.setDisplayName(failStack,
        StringExtensionsKt.chatColorize(badInfuseName));

    ItemMeta dupeStatMeta = failStack.getItemMeta().clone();
    dupeStatMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.duplicate-stats")));
    ItemMeta powerfulEssenceMeta = failStack.getItemMeta().clone();
    powerfulEssenceMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.essence-strength")));
    ItemMeta lowLevelMeta = failStack.getItemMeta().clone();
    lowLevelMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.low-craft-level")));
    ItemMeta wrongTypeMeta = failStack.getItemMeta().clone();
    wrongTypeMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.wrong-type")));
    ItemMeta noSlotsMeta = failStack.getItemMeta().clone();
    noSlotsMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.no-slots")));
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onSpecialCraftEquipment(CraftItemEvent event) {
    ItemStack resultStack = event.getCurrentItem();
    if (Tier.determine(resultStack) == null) {
      return;
    }

    Player player = (Player) event.getWhoClicked();

    if (event.getAction() == InventoryAction.NOTHING) {
      sendMessage(player, plugin.getSettings().getString("language.craft.no-weird", ""));
      event.setCancelled(true);
    } else if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT
        || event.getClick() == ClickType.NUMBER_KEY) {
      sendMessage(player, plugin.getSettings().getString("language.craft.no-shift", ""));
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
  public void onCraftEquipment(InventoryClickEvent event) {
    if (event.getRawSlot() == 0 && event.getSlot() == 0 && event.getSlotType() == SlotType.RESULT) {
      if (event.getClick() == ClickType.CONTROL_DROP) {
        MessageUtils.sendMessage(event.getWhoClicked(),
            "&e&oSorry, this crafting operation is blocked due to bugs! No items have been consumed, even if your game client may say they were until you close the crafting grid...");
        event.setCancelled(true);
        return;
      }
      if (event.getAction() == InventoryAction.DROP_ONE_SLOT && (event.getCursor() != null
          && event.getCursor().getType() != Material.AIR)) {
        MessageUtils.sendMessage(event.getWhoClicked(),
            "&e&oSorry, this crafting operation is blocked due to bugs! No items have been consumed, even if your game client may say they were until you close the crafting grid...");
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onCraftEquipment(CraftItemEvent event) {
    if (event.isCancelled()) {
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

    Tier tier = Tier.determine(resultStack);
    if (tier == null) {
      Bukkit.getLogger().warning("[Loot] Attempted to craft item with unknown tier... " + resultStack.getType());
      return;
    }
    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.CRAFTING, true);
    SkillRank rank = SkillRank.getRank(plugin.getStrifePlugin().getChampionManager().getChampion(player), LifeSkillType.CRAFTING);
    CraftResultData crData = new CraftResultData(event.getInventory().getMatrix(), resultStack);
    int itemLevel = (int) (crData.getItemLevel() == -1 ? player.getLevel() : crData.getItemLevel());
    int maxCraftLevel = CraftingUtil.getMaxActionLevel(rank);
    float craftBonus = data.getLevelWithBonus() - data.getLevel();
    float levelAdvantage = maxCraftLevel - itemLevel;
    float effectiveLevelAdvantage = levelAdvantage + craftBonus;

    if (levelAdvantage < 0) {
      sendMessage(player, plugin.getSettings().getString("language.craft.low-level-craft", ""));
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 0.7F, 0.5F);
      return;
    }

    itemLevel = Math.max(1, Math.min(100, itemLevel - LootPlugin.RNG.nextInt(4)));
    float minRarity = CraftingUtil.getMinimumRarity(crData.getRarity(), rank, effectiveLevelAdvantage);
    float rarityBonus = 1 + (craftBonus / 50) + (crData.getRarity() / 10);
    var rarity = plugin.getRarityManager().getRandomRarity(rarityBonus, minRarity);
    var enchantable = true;
    if (!SkillRank.isRank(rank, SkillRank.APPRENTICE)) {
      enchantable = LootPlugin.RNG.nextFloat() < 0.2 + 0.03 * data.getLevelWithBonus();
    }
    BuiltItem builtItem = plugin.getNewItemBuilder()
        .withItemGenerationReason(ItemGenerationReason.CRAFTING)
        .withTier(tier)
        .withRarity(rarity)
        .withHighRateEmptySlots(crData.highRateEmptySlots(rarity.getPower(), Math.max(0, effectiveLevelAdvantage), false))
        .withEnchantable(enchantable)
        .withAlwaysEssence(SkillRank.isRank(rank, SkillRank.MASTER))
        .withAlwaysExtend(SkillRank.isRank(rank, SkillRank.MASTER))
        .withCraftBonusStats(SkillRank.isRank(rank, SkillRank.EXPERT)? 0 : -1)
        .socketChanceBonus(MaterialUtil.getSocketChanceBonus(rank))
        .withPreferredStats(crData.getPreferredStats())
        .withLevel(itemLevel)
        .withCreator(player)
        .build();

    ItemStack newResult = builtItem.getStack();

    event.setCurrentItem(newResult);
    event.setCancelled(false);

    double exp = CRAFT_EXP * crData.getNumMaterials() * 0.25;
    exp *= 1 + (itemLevel * CRAFT_LEVEL_MULT);
    exp *= 1 + (crData.getRarity() * CRAFT_QUALITY_MULT);
    if (maxCraftLevel > 25) {
      exp *= (float) itemLevel / maxCraftLevel;
    }

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
  public void onCraftBarrier(CraftItemEvent event) {
    if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER
        || event.getCursor() != null && event.getCursor().getType() == Material.BARRIER) {
      event.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onCraftTransmute(CraftItemEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (TRANSMUTE_RECIPE == event.getRecipe()) {
      Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getStrifePlugin().getSkillExperienceManager()
          .addExperience((Player) event.getWhoClicked(), LifeSkillType.ALCHEMY,
              event.getInventory().getResult().getAmount() * 0.7f, false, false), 0L);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEssenceInfuse(CraftItemEvent event) {
    if (event.isCancelled()) {
      return;
    }
    for (ItemStack is : event.getInventory().getMatrix()) {
      if (is == null) {
        continue;
      }
      if (is.getType() == Material.GHAST_TEAR) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getStrifePlugin()
            .getSkillExperienceManager().addExperience((Player) event.getWhoClicked(),
                LifeSkillType.CRAFTING, 25, false, false), 0L);
        return;
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEssenceInfuseFailsafe(CraftItemEvent event) {
    if (event.isCancelled()) {
      return;
    }
    var result = event.getInventory().getResult();
    if (result == null || result.getType() == Material.END_CRYSTAL) {
      MessageUtils.sendMessage(event.getWhoClicked(), ChatColor.RED + "Crafting failed!");
      event.setCancelled(true);
    }
  }

  private boolean isDyeEvent(Material ingredient, Material result) {
    return ingredient == result && (ingredient == Material.LEATHER_HELMET
        || ingredient == Material.LEATHER_CHESTPLATE || ingredient == Material.LEATHER_LEGGINGS
        || ingredient == Material.LEATHER_BOOTS);
  }
}
