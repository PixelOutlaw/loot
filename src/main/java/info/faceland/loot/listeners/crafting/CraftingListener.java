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
import info.faceland.loot.tier.Tier;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.regex.Pattern;
import land.face.strife.data.champion.LifeSkillType;
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
import org.bukkit.inventory.meta.ItemMeta;

public final class CraftingListener implements Listener {

  private final LootPlugin plugin;

  public static String ESSENCE_SLOT_TEXT;

  private final String BAD_INFUSE_NAME;
  private final double CRAFT_EXP;
  private final double CRAFT_LEVEL_MULT;
  private final double CRAFT_QUALITY_MULT;
  private final float MAX_QUALITY;

  private final ItemMeta dupeStatMeta;
  private final ItemMeta powerfulEssenceMeta;
  private final ItemMeta lowLevelMeta;
  private final ItemMeta wrongTypeMeta;
  private final ItemMeta noSlotsMeta;

  private static final Pattern pattern = Pattern.compile("[^A-za-z]");

  public CraftingListener(LootPlugin plugin) {
    this.plugin = plugin;

    CRAFT_EXP = plugin.getSettings().getDouble("config.crafting.base-craft-exp", 1);
    CRAFT_LEVEL_MULT = plugin.getSettings()
        .getDouble("config.crafting.craft-item-level-mult", 0.01);
    CRAFT_QUALITY_MULT = plugin.getSettings().getDouble("config.crafting.craft-quality-mult", 0.1);
    MAX_QUALITY = (float) plugin.getSettings().getDouble("config.crafting.craft-max-quality", 5);
    ESSENCE_SLOT_TEXT = plugin.getSettings()
        .getString("config.crafting.essence-text", "(Essence Slot)");

    BAD_INFUSE_NAME = StringExtensionsKt.chatColorize(plugin.getSettings()
        .getString("language.essence.invalid-title", "&cCannot Use Essence!"));

    ItemStack failStack = new ItemStack(Material.BARRIER);
    ItemStackExtensionsKt.setDisplayName(failStack,
        StringExtensionsKt.chatColorize(BAD_INFUSE_NAME));

    dupeStatMeta = failStack.getItemMeta().clone();
    dupeStatMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.duplicate-stats")));
    powerfulEssenceMeta = failStack.getItemMeta().clone();
    powerfulEssenceMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.essence-strength")));
    lowLevelMeta = failStack.getItemMeta().clone();
    lowLevelMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.low-craft-level")));
    wrongTypeMeta = failStack.getItemMeta().clone();
    wrongTypeMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.wrong-type")));
    noSlotsMeta = failStack.getItemMeta().clone();
    noSlotsMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.no-slots")));
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onSpecialCraftEquipment(CraftItemEvent event) {
    ItemStack resultStack = event.getCurrentItem();
    if (MaterialUtil.getTierFromStack(resultStack) == null) {
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
    for (ItemStack is : event.getInventory().getMatrix()) {
      if (is == null) {
        continue;
      }
      if (is.getType() == Material.PRISMARINE_SHARD || isDyeEvent(is.getType(),
          resultStack.getType())) {
        return;
      }
    }

    event.setCancelled(true);

    Player player = (Player) event.getWhoClicked();

    Tier tier;
    tier = MaterialUtil.getTierFromStack(resultStack);
    if (tier == null) {
      Bukkit.getLogger().warning(
          "[Loot] Attempted to craft item with unknown tier... " + resultStack.getType());
      return;
    }

    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.CRAFTING, true);
    int craftingLevel = data.getLevel();
    double effectiveCraftLevel = data.getLevelWithBonus();

    CraftResultData crData = new CraftResultData(event.getInventory().getMatrix(), resultStack);

    double levelAdvantage = craftingLevel + 10 - (int) crData.getItemLevel();
    double bonusLevelAdvantage = data.getLevelWithBonus() + 10 - (int) crData.getItemLevel();

    if (levelAdvantage < 0) {
      sendMessage(player, plugin.getSettings().getString("language.craft.low-level-craft", ""));
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 0.7F, 0.5F);
      return;
    }

    int itemLevel = (int) Math.max(1, Math.min(100, crData.getItemLevel() - LootPlugin.RNG.nextInt(4)));
    float effectiveLevelAdvantage = (float) Math.max(-8, effectiveCraftLevel - crData.getItemLevel());

    float minRarityFromLevel = Math.min(3, (8 + effectiveLevelAdvantage) / 8);
    float minRarityFromQuality = Math.max(0, Math.min(3, crData.getQuality() - 1));
    float minRarity = (minRarityFromLevel + minRarityFromQuality) / 2;
    minRarity = Math.min(MAX_QUALITY, Math.max(0f, minRarity));

    BuiltItem builtItem = plugin.getNewItemBuilder()
        .withTier(tier)
        .withRarity(plugin.getRarityManager().getRandomRarity(1, minRarity))
        .withSlotScore(crData.openSlotChance(Math.max(0, effectiveLevelAdvantage)))
        .withEnchantable(craftingLevel >= 10 || LootPlugin.RNG.nextFloat() < 0.15)
        .withExtendSlots(LootPlugin.RNG.nextFloat() < MaterialUtil.getExtendChance(craftingLevel) ? 1 : 0)
        .withAlwaysEssence(craftingLevel > 70)
        .withCraftBonusStats((craftingLevel >= 45 && bonusLevelAdvantage >= 20) ? 0 : -1)
        .withSockets(MaterialUtil.getSockets(minRarity, craftingLevel))
        .withLevel(itemLevel)
        .withCreator(player)
        .withItemGenerationReason(ItemGenerationReason.CRAFTING)
        .build();

    ItemStack newResult = builtItem.getStack();

    event.setCurrentItem(newResult);
    event.setCancelled(false);

    double exp = CRAFT_EXP * crData.getNumMaterials() * 0.25;
    exp *= 1 + (itemLevel * CRAFT_LEVEL_MULT);
    exp *= 1 + (crData.getQuality() * CRAFT_QUALITY_MULT);
    if (craftingLevel - 8 > crData.getItemLevel()) {
      exp *= crData.getItemLevel() / craftingLevel;
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
  public void onEssenceInfuse(CraftItemEvent event) {
    if (event.isCancelled()) {
      return;
    }
    for (ItemStack is : event.getInventory().getMatrix()) {
      if (is == null) {
        continue;
      }
      if (is.getType() == Material.PRISMARINE_SHARD) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getStrifePlugin()
            .getSkillExperienceManager().addExperience((Player) event.getWhoClicked(),
                LifeSkillType.CRAFTING, 25, false, false), 0L);
        return;
      }
    }
  }

  private boolean isDyeEvent(Material ingredient, Material result) {
    return ingredient == result && (ingredient == Material.LEATHER_HELMET
        || ingredient == Material.LEATHER_CHESTPLATE || ingredient == Material.LEATHER_LEGGINGS
        || ingredient == Material.LEATHER_BOOTS);
  }
}
