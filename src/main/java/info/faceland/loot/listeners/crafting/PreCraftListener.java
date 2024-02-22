/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.listeners.crafting;

import static info.faceland.loot.utils.InventoryUtil.stripColor;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.ShaderStyle;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.CraftResultData;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.tier.Tier;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;

public final class PreCraftListener implements Listener {

  private final LootPlugin plugin;

  public static String ESSENCE_SLOT_TEXT;

  private final String BAD_INFUSE_NAME;
  private final float MAX_QUALITY;

  private final ItemMeta dupeStatMeta;
  private final ItemMeta powerfulEssenceMeta;
  private final ItemMeta lowLevelMeta;
  private final ItemMeta invalidItemMeta;
  private final ItemMeta wrongTypeMeta;
  private final ItemMeta noSlotsMeta;

  public PreCraftListener(LootPlugin plugin) {
    this.plugin = plugin;

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
    invalidItemMeta = failStack.getItemMeta().clone();
    invalidItemMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.menu.invalid-item")));
    wrongTypeMeta = failStack.getItemMeta().clone();
    wrongTypeMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.wrong-type")));
    noSlotsMeta = failStack.getItemMeta().clone();
    noSlotsMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.no-slots")));
  }

  @EventHandler
  public void onEssenceInfuse(PrepareItemCraftEvent event) {
    if (event.getRecipe() != null && event.getRecipe().getResult().getType() == Material.END_CRYSTAL) {
      updateInfuseInvy(event.getInventory(), (Player) event.getViewers().get(0));
    }
  }

  @EventHandler
  public void onCraftEquipment(PrepareItemCraftEvent event) {
    if (event.getRecipe() != null) {
      String name = ItemStackExtensionsKt.getDisplayName(event.getRecipe().getResult());
      if (StringUtils.isNotBlank(name) && ChatColor.stripColor(name).contains("Equipment Crafting")) {
        updateEquipmentCraftInvy(event.getInventory(), event.getRecipe(), (Player) event.getViewers().get(0));
      }
    }
  }

  private void updateInfuseInvy(CraftingInventory craftingInventory, Player player) {

    if (craftingInventory.getResult() == null) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(wrongTypeMeta);
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
      equipmentItem = loopItem;
    }

    if (essenceStack == null || equipmentItem == null) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(wrongTypeMeta);
      return;
    }

    String tag = MaterialUtil.getEssenceTag(essenceStack);
    if (!"慏".equals(tag)) {
      if (StringUtils.isBlank(tag) || !MaterialUtil.getEssenceTiers(tag)
          .contains(MaterialUtil.getTierFromStack(equipmentItem))) {
        craftingInventory.getResult().setType(Material.BARRIER);
        craftingInventory.getResult().setItemMeta(wrongTypeMeta);
        return;
      }
    }

    List<String> lore = TextUtils.getLore(equipmentItem);
    List<String> strippedLore = stripColor(lore);

    int slotIndex = strippedLore.indexOf(PreCraftListener.ESSENCE_SLOT_TEXT);
    if (slotIndex == -1) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(noSlotsMeta);
      return;
    }

    int itemLevel = NumberUtils.toInt(CharMatcher.digit().or(CharMatcher.is('-')).negate()
        .collapseFrom(ChatColor.stripColor(strippedLore.get(0)), ' ').trim());
    int essenceLevel = MaterialUtil.getEssenceLevel(essenceStack);
    int craftingLevel = PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.CRAFTING);
    double levelAdvantage = craftingLevel + 10 - itemLevel;
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
    String essenceStat = FaceColor.CYAN +
        ChatColor.stripColor(MaterialUtil.getEssenceStat(essenceStack));
    String rawEssenceText = MaterialUtil.ONLY_LETTERS
        .matcher(ChatColor.stripColor(essenceStat)).replaceAll("");

    if (existingCraftStatStrings.contains(rawEssenceText)) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(dupeStatMeta);
      return;
    }

    lore.set(slotIndex, FaceColor.CYAN + ChatColor.stripColor(essenceStat));

    ItemStack newResult = equipmentItem.clone();
    TextUtils.setLore(newResult, lore, false);

    craftingInventory.setResult(newResult);
  }

  private void updateEquipmentCraftInvy(CraftingInventory craftingInventory, Recipe recipe,
      Player player) {

    for (ItemStack stack : craftingInventory.getMatrix()) {
      if (stack == null) {
        continue;
      }
      String name = ItemStackExtensionsKt.getDisplayName(stack);
      if (StringUtils.isBlank(name)) {
        continue;
      }
      name = ChatColor.stripColor(name);
      if (name.startsWith("Socket Gem") || name.startsWith("Enchantment Tome")
          || name.endsWith("Scroll")) {
        craftingInventory.getResult().setType(Material.BARRIER);
        craftingInventory.getResult().setItemMeta(invalidItemMeta);
        return;
      }
    }

    ItemStack result = recipe.getResult().clone();

    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.CRAFTING, true);
    int craftingLevel = data.getLevel();
    double effectiveCraftLevel = data.getLevelWithBonus();
    CraftResultData crData = new CraftResultData(craftingInventory.getMatrix(), result);
    int requiredLevel = (int) crData.getItemLevel() - 10;

    if (craftingLevel < requiredLevel) {
      craftingInventory.getResult().setType(Material.BARRIER);
      ItemStackExtensionsKt.setCustomModelData(craftingInventory.getResult(), 150);
      ItemStackExtensionsKt.setDisplayName(craftingInventory.getResult(),
          FaceColor.RED + FaceColor.BOLD.s() + FaceColor.UNDERLINE.s() + "Crafting Level Too Low!");
      TextUtils.setLore(craftingInventory.getResult(), List.of(
          FaceColor.WHITE + FaceColor.BOLD.s() + "Base Item Level: " + (int) crData.getItemLevel(),
          FaceColor.RED + FaceColor.BOLD.s() + "Required Crafting Level: " + FaceColor.UNDERLINE + requiredLevel,
          FaceColor.YELLOW + FaceColor.BOLD.s() + "Your Crafting Level: " + craftingLevel,
          FaceColor.GRAY + "An item's level is determined by the",
          FaceColor.GRAY + "average level of materials used to",
          FaceColor.GRAY + "craft it. Use weaker materials or",
          FaceColor.GRAY + "raise your craft level to do this."
      ), false);
      return;
    }

    Tier tier = plugin.getItemGroupManager().getTierFromStack(result);
    if (tier == null) {
      craftingInventory.getResult().setType(Material.BARRIER);
      ItemStackExtensionsKt.setCustomModelData(craftingInventory.getResult(), 150);
      ItemStackExtensionsKt.setDisplayName(craftingInventory.getResult(),
          FaceColor.RED + FaceColor.BOLD.s() + FaceColor.UNDERLINE.s() + "Something Went Wrong!");
      TextUtils.setLore(craftingInventory.getResult(), List.of(
          FaceColor.GRAY + "The server doesn't know how",
          FaceColor.GRAY + "to craft this item! Please",
          FaceColor.GRAY + "report this to staff!"
      ), false);
      return;
    }

    int minItemLevel = (int) Math.max(1, crData.getItemLevel() - 3);
    int maxItemLevel = (int) Math.max(1, Math.min(100, crData.getItemLevel()));

    float effectiveLevelAdvantage = (float) Math.max(-8,
        effectiveCraftLevel - crData.getItemLevel());

    float minRarityFromLevel = Math.min(3, (effectiveLevelAdvantage + 8) / 8);
    float minRarityFromQuality = Math.max(0, Math.min(3, crData.getQuality() - 1));
    float minRarity = Math.min(2.9f, (minRarityFromLevel + minRarityFromQuality) / 2);
    minRarity = Math.min(MAX_QUALITY, Math.max(0f, minRarity));
    int minSockets = MaterialUtil.getMinSockets(craftingLevel);
    int maxSockets = MaterialUtil.getMaxSockets(minRarityFromQuality);
    int extendChance = (int) (100 * MaterialUtil.getExtendChance(craftingLevel));
    float slotScore = crData.openSlotChance(Math.max(0, effectiveLevelAdvantage));

    List<String> newLore = new ArrayList<>();
    newLore.add(FaceColor.WHITE + "Item Level: " + minItemLevel + "⇒" + maxItemLevel);
    newLore.add(FaceColor.WHITE + getRarityTag(minRarity) + "⇒" + "\uD86D\uDFE9" + tier.getName());
    newLore.add("");
    newLore.add(FaceColor.LIGHT_GRAY + "+X Primary Stat");
    for (ItemStat s : tier.getPrimaryStats()) {
      newLore.add(FaceColor.GRAY + " " + s.getStatString()
          .replace("+{}% ", "").replace("+{} ", "") + "?");
    }
    newLore.add(FaceColor.LIGHT_GRAY + "+X Secondary Stat");
    for (ItemStat s : tier.getSecondaryStats()) {
      newLore.add(FaceColor.GRAY + " " + s.getStatString()
          .replace("+{}% ", "").replace("+{} ", "") + "?");
    }
    newLore.add("");
    newLore.add(FaceColor.RAINBOW + "Stat⇒Essence Slot: (" + Math.round(100 * slotScore * 0.2) + "%)");
    int certainStats = 3;
    int maybeStats = 1;
    if (craftingLevel > 45 && (effectiveCraftLevel - 20 >= minItemLevel)) {
      certainStats++;
    }
    if (minRarity < 1) {
      certainStats--;
      maybeStats++;
    }
    if (minRarity < 2) {
      certainStats--;
      maybeStats++;
    }
    if (minRarity < 3) {
      certainStats--;
      maybeStats++;
    }
    certainStats = Math.max(1, certainStats);
    while (certainStats > 0) {
      newLore.add(FaceColor.CYAN + "+X Random Stat");
      certainStats--;
    }
    while (maybeStats > 0) {
      newLore.add(FaceColor.CYAN + "+X Random Stat " + FaceColor.LIGHT_GRAY.shaded(ShaderStyle.WAVE) + "(?)");
      maybeStats--;
    }
    newLore.add("");
    newLore.add(FaceColor.TRUE_WHITE + "傜" + FaceColor.BLUE +
        (craftingLevel >= 10 ? " (100%)" : " (15%)"));
    newLore.add("");
    if (tier.getMaximumSockets() > 0) {
      if (minSockets > 0) {
        newLore.add(FaceColor.TRUE_WHITE + "哀 " + FaceColor.ORANGE + "Socket (100%)");
      } else {
        newLore.add(FaceColor.TRUE_WHITE + "哀 " + FaceColor.ORANGE + "Socket (50%)");
      }
    }
    if (tier.getMaximumSockets() > 1) {
      if (maxSockets > 1 || minSockets > 1) {
        newLore.add(FaceColor.TRUE_WHITE + "哀 " + FaceColor.ORANGE + "Socket (100%)");
      } else {
        newLore.add(FaceColor.TRUE_WHITE + "哀 " + FaceColor.ORANGE + "Socket (12%)");
      }
    }
    if (tier.getMaximumExtendSlots() > 0) {
      newLore.add(FaceColor.TRUE_WHITE + "品 " + FaceColor.TEAL + "Socket (" + extendChance + "%)");
    }
    if (tier.getMaximumSockets() > 0 || tier.getMaximumExtendSlots() > 0) {
      newLore.add("");
    }
    newLore.add(FaceColor.YELLOW.shaded(ShaderStyle.BOUNCE) + "Use /skills for more info!");

    TextUtils.setLore(craftingInventory.getResult(), newLore, false);
  }

  private String getRarityTag(float amount) {
    if (amount < 1) {
      return "\uD86D\uDFE6";
    }
    if (amount < 2) {
      return "\uD86D\uDFE7";
    }
    if (amount < 3) {
      return "\uD86D\uDFE8";
    }
    return "\uD86D\uDFE9";
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onCraftBarrier(CraftItemEvent event) {
    if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER
        || event.getCursor() != null && event.getCursor().getType() == Material.BARRIER) {
      event.setCancelled(true);
    }
  }
}
