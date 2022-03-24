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
import static info.faceland.loot.utils.InventoryUtil.stripColor;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.ItemGenerationReason;
import info.faceland.loot.data.BuiltItem;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.events.LootCraftEvent;
import info.faceland.loot.listeners.DeconstructListener;
import info.faceland.loot.math.LootRandom;
import info.faceland.loot.recipe.EquipmentRecipeBuilder;
import info.faceland.loot.tier.Tier;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

public final class CraftingListener implements Listener {

  private final LootPlugin plugin;

  public static String ESSENCE_SLOT_TEXT;

  private final String BAD_INFUSE_NAME;
  private final double CRAFT_EXP;
  private final double CRAFT_LEVEL_MULT;
  private final double CRAFT_QUALITY_MULT;
  private final double CRAFT_MASTER_MULT;
  private final double MAX_QUALITY;
  private final double MAX_SLOTS;
  private final double MAX_SOCKETS;

  private final ItemMeta dupeStatMeta;
  private final ItemMeta powerfulEssenceMeta;
  private final ItemMeta lowLevelMeta;
  private final ItemMeta wrongTypeMeta;
  private final ItemMeta noSlotsMeta;

  private static final Pattern pattern = Pattern.compile("[^A-za-z]");

  private final LootRandom random;

  public CraftingListener(LootPlugin plugin) {
    this.plugin = plugin;
    this.random = new LootRandom();

    CRAFT_EXP = plugin.getSettings().getDouble("config.crafting.base-craft-exp", 1);
    CRAFT_LEVEL_MULT = plugin.getSettings()
        .getDouble("config.crafting.craft-item-level-mult", 0.01);
    CRAFT_QUALITY_MULT = plugin.getSettings().getDouble("config.crafting.craft-quality-mult", 0.1);
    CRAFT_MASTER_MULT = plugin.getSettings().getDouble("config.crafting.craft-master-mult", 2.5);
    MAX_QUALITY = plugin.getSettings().getDouble("config.crafting.craft-max-quality", 5);
    MAX_SLOTS = plugin.getSettings().getDouble("config.crafting.craft-max-craft-slots", 5);
    MAX_SOCKETS = plugin.getSettings().getDouble("config.crafting.craft-max-sockets", 2);
    ESSENCE_SLOT_TEXT = StringExtensionsKt
        .chatColorize(
            plugin.getSettings().getString("config.crafting.essence-text", "&b(Essence Slot)"));

    BAD_INFUSE_NAME = StringExtensionsKt
        .chatColorize(plugin.getSettings()
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
  public void onCraftItemEvent(CraftItemEvent event) {
    if (event.isCancelled()) {
      return;
    }
    for (ItemStack is : event.getInventory().getMatrix()) {
      // Keep this == null, javadocs lies
      if (is == null || is.getType() == Material.AIR) {
        continue;
      }
      ItemStack materialStack = new ItemStack(is);
      Material material = event.getInventory().getResult().getType();
      // TODO: Configurable material type restriction
      if (material == Material.DIAMOND_BLOCK || material == Material.IRON_BLOCK ||
          material == Material.GOLD_BLOCK || material == Material.EMERALD_BLOCK) {
        for (String str : TextUtils.getLore(materialStack)) {
          if (ChatColor.stripColor(str).equals("[ Crafting Component ]")) {
            sendMessage(event.getWhoClicked(),
                plugin.getSettings().getString("language.craft.nope", ""));
            event.setCancelled(true);
            return;
          }
        }
      }
      String name = ItemStackExtensionsKt.getDisplayName(materialStack);
      if (isUncraftableByName(name)) {
        event.setCancelled(true);
        return;
      }
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onSpecialCraftEquipment(CraftItemEvent event) {
    ItemStack resultStack = event.getCurrentItem();
    if (!plugin.getCraftBaseManager().getCraftBases().containsKey(resultStack.getType())) {
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

    if (!plugin.getCraftBaseManager().getCraftBases().containsKey(resultStack.getType())) {
      return;
    }
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
      String tierId = plugin.getCraftBaseManager().getCraftBases().get(resultStack.getType());
      tier = plugin.getTierManager().getTier(tierId);
    }

    if (tier == null) {
      Bukkit.getLogger()
          .warning("Attempted to craft item with unknown tier... " + resultStack.getType());
      return;
    }

    int craftingLevel = PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.CRAFTING);
    double effectiveCraftLevel = PlayerDataUtil.getEffectiveLifeSkill(player,
        LifeSkillType.CRAFTING, true);

    int numMaterials = 0;
    double totalQuality = 0;
    double totalItemLevel = 0;

    for (ItemStack is : event.getInventory().getMatrix()) {
      if (is == null || is.getType() == Material.AIR || is.getType() == resultStack.getType()) {
        continue;
      }
      ItemStack loopItem = new ItemStack(is);
      if (MaterialUtil.hasItemLevel(loopItem)) {
        int iLevel = NumberUtils.toInt(CharMatcher.digit().or(CharMatcher.is('-')).negate()
            .collapseFrom(ChatColor.stripColor(TextUtils.getLore(loopItem).get(0)), ' ')
            .trim());
        totalItemLevel += iLevel;
      } else {
        totalItemLevel += 0.5;
      }
      numMaterials++;
      if (MaterialUtil.hasQuality(loopItem)) {
        long count = TextUtils.getLore(loopItem).get(1).chars().filter(ch -> ch == '✪')
            .count();
        totalQuality += count;
      }
    }

    double rawItemLevel = totalItemLevel / numMaterials;
    double levelAdvantage = DeconstructListener.getLevelAdvantage(craftingLevel,
        (int) rawItemLevel);

    if (levelAdvantage < 0) {
      sendMessage(player, plugin.getSettings().getString("language.craft.low-level-craft", ""));
      player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 0.7F, 0.5F);
      return;
    }

    double quality = Math.floor(totalQuality / numMaterials) - 2;
    quality = Math.min(MAX_QUALITY, Math.max(0, quality));

    int itemLevel = (int) Math.max(1, Math.min(100, rawItemLevel - random.nextInt(4)));
    float effectiveLevelAdvantage = (float) Math.max(0, effectiveCraftLevel - rawItemLevel);
    float openSlotChance = 0.1f + Math.min(0.8f, effectiveLevelAdvantage / 100);

    BuiltItem builtItem = plugin.getNewItemBuilder()
        .withTier(tier)
        .withRarity(plugin.getRarityManager().getRandomRarityWithMinimum(quality))
        .withSlotScore(openSlotChance)
        .withLevel(itemLevel)
        .withCreator(player)
        .withItemGenerationReason(ItemGenerationReason.CRAFTING)
        .build();

    ItemStack newResult = builtItem.getStack();

    /*
    List<Material> mats = new ArrayList<>(tier.getAllowedMaterials());
    ItemStack newResult = new ItemStack(mats.get(0));
    ItemStackExtensionsKt.setDisplayName(newResult, ChatColor.AQUA +
        plugin.getNameManager().getRandomPrefix() + " " + plugin.getNameManager()
        .getRandomSuffix());
    List<String> lore = new ArrayList<>();

    lore.add(ChatColor.WHITE + "Level Requirement: " + itemLevel);
    lore.add(ChatColor.WHITE + "Tier: " + ChatColor.AQUA + "Crafted " + tier.getName());

    lore.add(plugin.getStatManager().getFinalStat(tier.getPrimaryStat(), itemLevel, quality, false)
        .getStatString());
    lore.add(plugin.getStatManager().getFinalStat(
            tier.getSecondaryStats().get(random.nextInt(tier.getSecondaryStats().size())), itemLevel,
            quality, false)
        .getStatString());

    List<ItemStat> stats = new ArrayList<>(tier.getBonusStats());

    boolean masterwork =
        craftedSlotScore / MAX_SLOTS > 0.85 && craftedSocketScore / MAX_SOCKETS > 0.85;

    if (masterwork) {
      craftedSlotScore = Math.ceil(craftedSlotScore);
      craftedSocketScore = Math.ceil(craftedSocketScore);
    }

    double openSlotChance = (skillMultiplier - 1) * 0.7;
    while (craftedSlotScore >= 1) {
      if (random.nextDouble() < openSlotChance) {
        lore.add(ESSENCE_SLOT_TEXT);
      } else {
        ItemStat stat = stats.get(random.nextInt(stats.size()));
        lore.add(ChatColor.AQUA + ChatColor
            .stripColor(plugin.getStatManager().getFinalStat(stat, itemLevel, quality, false)
                .getStatString()));
        stats.remove(stat);
      }
      craftedSlotScore--;
    }

    lore.add(ChatColor.BLUE + "(Enchantable)");

    while (craftedSocketScore >= 1) {
      lore.add(ChatColor.GOLD + "(Socket)");
      craftedSocketScore--;
    }
    if (masterwork) {
      lore.add(TextUtils.color("&8&oCrafted By: " + player.getName()));
      lore.add(TextUtils.color("&8&o[ Flavor Text Slot ]"));
    }

    TextUtils.setLore(newResult, lore);
    newResult.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE);

    MaterialUtil.applyTierLevelData(newResult, tier, itemLevel);

    // This section exists to clear existing item attributes and enforce
    // no stacking on equipment items
    ItemMeta meta = newResult.getItemMeta();
    double serialValue = Math.random() * 0.0001;
    AttributeModifier serial = new AttributeModifier("SERIAL", serialValue, Operation.ADD_NUMBER);
    meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, serial);
    newResult.setItemMeta(meta);

    MaterialUtil.applyTierLevelData(newResult, tier, itemLevel);
    */

    event.setCurrentItem(newResult);
    event.setCancelled(false);

    double exp = CRAFT_EXP * (numMaterials * 0.25);
    exp *= 1 + (itemLevel * CRAFT_LEVEL_MULT);
    exp *= 1 + (quality * CRAFT_QUALITY_MULT);
    if (craftingLevel - 8 > rawItemLevel) {
      exp *= rawItemLevel / craftingLevel;
    }

    LootCraftEvent craftEvent = new LootCraftEvent(player, newResult);
    Bukkit.getPluginManager().callEvent(craftEvent);

    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.CRAFTING, exp, false, false);

    player.playSound(player.getEyeLocation(), Sound.ITEM_ARMOR_EQUIP_CHAIN, 1F, 1F);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_ANVIL_FALL, 0.5F, 1F);
  }

  @EventHandler
  public void onEssenceInfuse(PrepareItemCraftEvent event) {
    updateCraftInvy(event.getInventory(), event.getRecipe(), (Player) event.getViewers().get(0));
  }

  private void updateCraftInvy(CraftingInventory craftingInventory, Recipe recipe, Player player) {
    if (recipe == null || recipe.getResult().getType() != Material.END_CRYSTAL) {
      return;
    }

    ItemStack equipmentItem = null;
    ItemStack essenceStack = null;

    for (ItemStack is : craftingInventory.getMatrix()) {
      if (!(essenceStack == null || equipmentItem == null)) {
        break;
      }
      if (is == null || is.getType() == Material.AIR) {
        continue;
      }
      ItemStack loopItem = new ItemStack(is);
      if (MaterialUtil.isEssence(loopItem)) {
        essenceStack = loopItem;
        continue;
      }
      if (EquipmentRecipeBuilder.MATERIAL_LIST.contains(is.getType())) {
        equipmentItem = loopItem;
      }
    }

    if (essenceStack == null || equipmentItem == null) {
      return;
    }

    if (!MaterialUtil.isEssenceTypeAny(essenceStack) && MaterialUtil.getEssenceTier(essenceStack) !=
        MaterialUtil.getTierFromStack(equipmentItem)) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(wrongTypeMeta);
      return;
    }

    List<String> lore = TextUtils.getLore(equipmentItem);
    List<String> strippedLore = stripColor(lore);

    if (!lore.contains(CraftingListener.ESSENCE_SLOT_TEXT)) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(noSlotsMeta);
      return;
    }

    int itemLevel = NumberUtils.toInt(CharMatcher.digit().or(CharMatcher.is('-')).negate()
        .collapseFrom(ChatColor.stripColor(strippedLore.get(0)), ' ').trim());

    int essenceLevel = MaterialUtil.getEssenceLevel(essenceStack);

    int craftingLevel = PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.CRAFTING);
    double levelAdvantage = DeconstructListener.getLevelAdvantage(craftingLevel, itemLevel);
    if (levelAdvantage < 0) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(lowLevelMeta);
      return;
    }

    if (essenceLevel > itemLevel) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(powerfulEssenceMeta);
      return;
    }

    List<String> existingCraftStatStrings = MaterialUtil.getValidEssenceStats(lore);
    String essenceStat = MaterialUtil.getEssenceStat(essenceStack);
    String matcherString = pattern.matcher(ChatColor.stripColor(essenceStat)).replaceAll("");

    if (existingCraftStatStrings.contains(matcherString)) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(dupeStatMeta);
      return;
    }

    int slotIndex = lore.indexOf(CraftingListener.ESSENCE_SLOT_TEXT);
    lore.set(slotIndex, ChatColor.AQUA + ChatColor.stripColor(essenceStat));
    TextUtils.setLore(equipmentItem, lore);

    craftingInventory.getResult().setType(equipmentItem.getType());
    craftingInventory.getResult().setItemMeta(equipmentItem.getItemMeta());
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
    if (!EquipmentRecipeBuilder.INFUSE_NAME.equals(ItemStackExtensionsKt.getDisplayName(event.getRecipe().getResult()))) {
      return;
    }
    plugin.getStrifePlugin().getSkillExperienceManager()
        .addExperience((Player) event.getWhoClicked(),
            LifeSkillType.CRAFTING, 25, false, false);
    updateCraftInvy(event.getInventory(), event.getRecipe(), (Player) event.getViewers().get(0));
  }

  private boolean isUncraftableByName(String name) {
    if (StringUtils.isBlank(name)) {
      return false;
    }
    return name.equals(ChatColor.DARK_AQUA + "Socket Extender") ||
        name.startsWith(ChatColor.BLUE + "Enchantment Tome - ") ||
        name.startsWith(ChatColor.GOLD + "Socket Gem -") ||
        name.startsWith(ChatColor.DARK_AQUA + "Scroll Augment -");
  }

  private boolean isDyeEvent(Material ingredient, Material result) {
    return ingredient == result && (ingredient == Material.LEATHER_HELMET
        || ingredient == Material.LEATHER_CHESTPLATE || ingredient == Material.LEATHER_LEGGINGS
        || ingredient == Material.LEATHER_BOOTS);
  }
}
