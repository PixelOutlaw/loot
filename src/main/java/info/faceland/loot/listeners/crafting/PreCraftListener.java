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

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.ShaderStyle;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.CraftResultData;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.StoredStat;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.LevelModItem;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.Tier;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.utils.CraftingUtil;
import info.faceland.loot.utils.EssenceUtil;
import info.faceland.loot.utils.MaterialUtil;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.SkillRank;
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
  private final String BAD_LEVEL_MOD;
  private final float MAX_QUALITY;

  private final ItemMeta dupeStatMeta, unstableStatMeta, powerfulEssenceMeta, lowLevelMeta, invalidItemMeta,
      wrongTypeMeta, noSlotsMeta, wrongLevelModType, alreadyAppliedMod, wrongTierMod;

  public PreCraftListener(LootPlugin plugin) {
    this.plugin = plugin;

    MAX_QUALITY = (float) plugin.getSettings().getDouble("config.crafting.craft-max-rarity", 5);
    ESSENCE_SLOT_TEXT = plugin.getSettings()
        .getString("config.crafting.essence-text", "(Essence Slot)");

    BAD_INFUSE_NAME = StringExtensionsKt.chatColorize(plugin.getSettings()
        .getString("language.essence.invalid-title", "&cCannot Use Essence!"));
    BAD_LEVEL_MOD = PaletteUtil.color(plugin.getSettings()
        .getString("language.level-mod.invalid-title", "|red|Invalid Target Equipment!"));

    ItemStack failStack = new ItemStack(Material.BARRIER);
    ItemStackExtensionsKt.setDisplayName(failStack,
        StringExtensionsKt.chatColorize(BAD_INFUSE_NAME));

    ItemStack failStack2 = new ItemStack(Material.BARRIER);
    ItemStackExtensionsKt.setDisplayName(failStack2,
        StringExtensionsKt.chatColorize(BAD_LEVEL_MOD));

    dupeStatMeta = failStack.getItemMeta().clone();
    dupeStatMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.duplicate-stats")));
    unstableStatMeta = failStack.getItemMeta().clone();
    unstableStatMeta.setLore(ListExtensionsKt.chatColorize(
        plugin.getSettings().getStringList("language.essence.unstable-stat")));
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

    wrongLevelModType = failStack2.getItemMeta().clone();
    wrongLevelModType.setLore(PaletteUtil.color(
        plugin.getSettings().getStringList("language.level-mod.wrong-type")));
    alreadyAppliedMod = failStack2.getItemMeta().clone();
    alreadyAppliedMod.setLore(PaletteUtil.color(
        plugin.getSettings().getStringList("language.level-mod.already-applied")));
    wrongTierMod = failStack2.getItemMeta().clone();
    wrongTierMod.setLore(PaletteUtil.color(
        plugin.getSettings().getStringList("language.level-mod.wrong-tier")));
  }

  @EventHandler
  public void onEssenceInfuse(PrepareItemCraftEvent event) {
    if (event.getRecipe() != null && event.getRecipe().getResult().getType() == Material.END_CRYSTAL) {
      updateInfuseInvy(event.getInventory(), (Player) event.getViewers().getFirst());
    }
  }

  @EventHandler
  public void onnLevelModCraft(PrepareItemCraftEvent event) {
    if (event.getRecipe() != null && event.getRecipe().getResult().getType() == Material.SMITHING_TABLE) {
      updateLevelModInvy(event.getInventory(), (Player) event.getViewers().getFirst());
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
      if ("essence".equals(PersistHelper.getItemType(loopItem))) {
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
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    StoredStat stat = PersistHelper.getEssenceStat(essenceStack);
    if (data == null || stat == null) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(wrongTypeMeta);
      return;
    }
    if (!data.getTier().getId().equals(stat.getOriginTier())) {
      if (!data.getTier().getEssenceGroup().contains(stat.getOriginTier())) {
        craftingInventory.getResult().setType(Material.BARRIER);
        craftingInventory.getResult().setItemMeta(wrongTypeMeta);
        return;
      }
    }

    if (!data.hasCraftSlot()) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(noSlotsMeta);
      return;
    }

    int itemLevel = data.getCustomLevel();
    int essenceLevel = stat.getEssenceLevel(stat.getLevel());
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

    List<StoredStat> existingStats = data.getNormalStats();

    // TODO: use tier limits on stat combos
    for (var s : existingStats) {
      if (s.getStat() == stat.getStat()) {
        craftingInventory.getResult().setType(Material.BARRIER);
        craftingInventory.getResult().setItemMeta(dupeStatMeta);
        return;
      }
      if (s.getStat().getStat() == stat.getStat().getStat()) {
        craftingInventory.getResult().setType(Material.BARRIER);
        craftingInventory.getResult().setItemMeta(dupeStatMeta);
        return;
      }
    }

    // TODO: THIS IS LIKELY INEFFICIENT
    ItemStack newResult = equipmentItem.clone();
    EquipmentData newData = PersistHelper.getEquipmentData(newResult);

    if (!EssenceUtil.canItemAcceptStat(stat, newData)) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(unstableStatMeta);
      return;
    }

    EssenceUtil.applyEssence(newData, stat);
    PersistHelper.setEquipmentData(newResult, newData);

    craftingInventory.setResult(newResult);
  }

  private void updateLevelModInvy(CraftingInventory craftingInventory, Player player) {
    if (craftingInventory.getResult() == null) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(wrongLevelModType);
      return;
    }
    ItemStack equipmentItem = null;
    ItemStack levelModStack = null;
    for (ItemStack is : craftingInventory.getMatrix()) {
      if (!(levelModStack == null || equipmentItem == null)) {
        break;
      }
      if (is == null || is.getType() == Material.AIR) {
        continue;
      }
      ItemStack loopItem = new ItemStack(is);
      if (LevelModItem.TYPE_KEY.equals(PersistHelper.getItemType(loopItem))) {
        levelModStack = loopItem;
        continue;
      }
      equipmentItem = loopItem;
    }

    if (levelModStack == null || equipmentItem == null) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(wrongLevelModType);
      return;
    }
    EquipmentData data = PersistHelper.getEquipmentData(equipmentItem);
    LevelModItem levelModItem = LevelModItem.get(levelModStack);
    if (data == null || levelModItem == null) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(wrongLevelModType);
      return;
    }
    if (!levelModItem.getValidTiers().contains(data.getTier().getId())) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(wrongTierMod);
      return;
    }
    if (data.getLevelMod() == levelModItem.getModificationAmount()) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(alreadyAppliedMod);
      return;
    }

    ItemStack newResult = equipmentItem.clone();
    EquipmentData newData = PersistHelper.getEquipmentData(newResult);
    newData.setLevelMod(levelModItem.getModificationAmount());

    PersistHelper.setEquipmentData(newResult, newData);

    craftingInventory.setResult(newResult);
  }

  private void updateEquipmentCraftInvy(CraftingInventory craftingInventory, Recipe recipe, Player player) {

    for (ItemStack stack : craftingInventory.getMatrix()) {
      if (stack == null) {
        continue;
      }
      String name = ItemStackExtensionsKt.getDisplayName(stack);
      if (StringUtils.isBlank(name)) {
        continue;
      }
      if (SocketGem.get(stack) != null || EnchantmentTome.get(stack) != null || UpgradeScroll.get(stack) != null) {
        craftingInventory.getResult().setType(Material.BARRIER);
        craftingInventory.getResult().setItemMeta(invalidItemMeta);
        return;
      }
    }

    ItemStack result = recipe.getResult().clone();

    SkillLevelData data = PlayerDataUtil.getSkillLevels(player, LifeSkillType.CRAFTING, true);
    SkillRank rank = SkillRank.getRank(plugin.getStrifePlugin().getChampionManager().getChampion(player),
        LifeSkillType.CRAFTING);
    int craftingLevel = data.getLevel();
    double effectiveCraftLevel = data.getLevelWithBonus();
    CraftResultData crData = new CraftResultData(craftingInventory.getMatrix(), result);
    int maxCraftLevel = CraftingUtil.getMaxActionLevel(rank);

    if (maxCraftLevel < (int) crData.getItemLevel()) {
      craftingInventory.getResult().setType(Material.BARRIER);
      ItemStackExtensionsKt.setCustomModelData(craftingInventory.getResult(), 150);
      ItemStackExtensionsKt.setDisplayName(craftingInventory.getResult(),
          FaceColor.RED + FaceColor.BOLD.s() + FaceColor.UNDERLINE.s() + "Crafting Level Too Low!");
      TextUtils.setLore(craftingInventory.getResult(), List.of(
          FaceColor.WHITE + FaceColor.BOLD.s() + "Base Item Level: " + (int) crData.getItemLevel(),
          "",
          FaceColor.GRAY + "An item's level is determined by the",
          FaceColor.GRAY + "average level of materials used to",
          FaceColor.GRAY + "craft it. Use weaker materials or",
          FaceColor.GRAY + "raise your craft level to do this.",
          "",
          FaceColor.YELLOW + "Check /skillS for more info!"
      ), false);
      return;
    }

    Tier tier = Tier.determine(result);
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

    float craftBonus = data.getLevelWithBonus() - data.getLevel();
    float levelAdvantage = maxCraftLevel - (int) crData.getItemLevel();
    float effectiveLevelAdvantage = levelAdvantage + craftBonus;

    float minRarity = CraftingUtil.getMinimumRarity(crData.getRarity(), rank, effectiveLevelAdvantage);

    int minSockets = MaterialUtil.getMinSockets(rank);
    int maxSockets = MaterialUtil.getMaxSockets(minRarity) + tier.getExtraSockets();
    int extendChance = (int) (100 * MaterialUtil.getExtendChance(rank));
    float slotScore = crData.openSlotChance(Math.max(0, effectiveLevelAdvantage));

    List<String> newLore = new ArrayList<>();
    newLore.add(FaceColor.WHITE + "Item Type: " + tier.getPlainName());
    newLore.add(FaceColor.WHITE + "Item Level: " + minItemLevel + "⇒" + maxItemLevel);
    newLore.add(FaceColor.WHITE + getRarityTag(minRarity) + "⇒" + "\uD86D\uDFE9");
    newLore.add("");
    newLore.add(FaceColor.LIGHT_GRAY + "+X Primary Stat");
    for (ItemStat s : tier.getPrimaryStats()) {
      newLore.add(FaceColor.GRAY + " +X" + (s.isPercentage() ? "% " : " ") + s.getStat().getName() + "?");
    }
    newLore.add(FaceColor.LIGHT_GRAY + "+X Secondary Stat");
    for (ItemStat s : tier.getSecondaryStats()) {
      newLore.add(FaceColor.GRAY + " +X" + (s.isPercentage() ? "% " : " ") + s.getStat().getName() + "?");
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


    if (minSockets > 0) {
      newLore.add(FaceColor.TRUE_WHITE + "哀 " + FaceColor.ORANGE + "Socket (100%)");
    } else {
      newLore.add(FaceColor.TRUE_WHITE + "哀 " + FaceColor.ORANGE + "Socket (50%)");
    }

    if (maxSockets > 1 || minSockets > 1) {
      newLore.add(FaceColor.TRUE_WHITE + "哀 " + FaceColor.ORANGE + "Socket (100%)");
    } else {
      newLore.add(FaceColor.TRUE_WHITE + "哀 " + FaceColor.ORANGE + "Socket (12%)");
    }

    if (extendChance > 0) {
      newLore.add(FaceColor.TRUE_WHITE + "品 " + FaceColor.TEAL + "Socket (" + extendChance + "%)");
    }

    newLore.add("");

    newLore.add(FaceColor.YELLOW.shaded(ShaderStyle.BOUNCE) + "Use /skills for more info!");

    TextUtils.setLore(craftingInventory.getResult(), newLore, false);
  }

  private String getRarityTag(float amount) {
    if (amount < 1) {
      return MaterialUtil.TAG_COMMON;
    }
    if (amount < 2) {
      return MaterialUtil.TAG_UNCOMMON;
    }
    if (amount < 3) {
      return MaterialUtil.TAG_RARE;
    }
    return MaterialUtil.TAG_EPIC;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onCraftBarrier(CraftItemEvent event) {
    if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER
        || event.getCursor() != null && event.getCursor().getType() == Material.BARRIER) {
      event.setCancelled(true);
    }
  }
}
