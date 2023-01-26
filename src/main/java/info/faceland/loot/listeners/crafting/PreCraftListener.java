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

import static info.faceland.loot.utils.InventoryUtil.stripColor;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.CraftResultData;
import info.faceland.loot.listeners.DeconstructListener;
import info.faceland.loot.math.LootRandom;
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

  private final LootRandom random;

  public PreCraftListener(LootPlugin plugin) {
    this.plugin = plugin;
    this.random = new LootRandom();

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
    if (event.getRecipe() != null && net.md_5.bungee.api.ChatColor.stripColor(ItemStackExtensionsKt
        .getDisplayName(event.getRecipe().getResult())).contains("Equipment Crafting")) {
      updateEquipmentCraftInvy(event.getInventory(), event.getRecipe(),
          (Player) event.getViewers().get(0));
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
    if (StringUtils.isBlank(tag) || (!"慏".equals(tag) && (MaterialUtil.getEssenceTier(tag)
        != MaterialUtil.getTierFromStack(equipmentItem)))) {
      craftingInventory.getResult().setType(Material.BARRIER);
      craftingInventory.getResult().setItemMeta(wrongTypeMeta);
      return;
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

    int craftingLevel = PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.CRAFTING);
    double effectiveCraftLevel = PlayerDataUtil.getEffectiveLifeSkill(player,
        LifeSkillType.CRAFTING, true);

    CraftResultData crData = new CraftResultData(craftingInventory.getMatrix(), result);

    double levelAdvantage = DeconstructListener.getLevelAdvantage(craftingLevel,
        (int) crData.getItemLevel());

    if (levelAdvantage < 0) {
      craftingInventory.getResult().setType(Material.BARRIER);
      ItemStackExtensionsKt.setCustomModelData(craftingInventory.getResult(), 150);
      ItemStackExtensionsKt.setDisplayName(craftingInventory.getResult(),
          FaceColor.RED + FaceColor.BOLD.s() + FaceColor.UNDERLINE.s() + "Crafting Level Too Low!");
      TextUtils.setLore(craftingInventory.getResult(), List.of(
          FaceColor.WHITE + FaceColor.BOLD.s() +
              "Base Item Level: " + (int) crData.getItemLevel(),
          FaceColor.RED + FaceColor.BOLD.s() +
              "Required Crafting Level: " + FaceColor.UNDERLINE + (int) (craftingLevel - levelAdvantage),
          FaceColor.YELLOW + FaceColor.BOLD.s() +
              "Your Crafting Level: " + craftingLevel,
          FaceColor.GRAY +
              "An item's level is determined by the",
          FaceColor.GRAY +
              "average level of materials used to",
          FaceColor.GRAY +
              "craft it. Use weaker materials or",
          FaceColor.GRAY +
              "raise your craft level to do this."
      ), false);
      return;
    }

    int minItemLevel = (int) Math.max(1, crData.getItemLevel() - 3);
    int maxItemLevel = (int) Math.max(1, Math.min(100, crData.getItemLevel()));

    float effectiveLevelAdvantage = (float) Math.max(0,
        effectiveCraftLevel - crData.getItemLevel());

    float minRarityFromLevel = Math.min(3, effectiveLevelAdvantage / 20);
    float minRarityFromQuality = Math.max(0, Math.min(3, crData.getQuality() - 1));
    float minRarity = Math.min(2.9f, (minRarityFromLevel + minRarityFromQuality) / 2);
    minRarity = Math.min(MAX_QUALITY, Math.max(0f, minRarity));
    int minSockets = MaterialUtil.getMinSockets(craftingLevel);
    int maxSockets = MaterialUtil.getMaxSockets(minRarityFromQuality);
    int extendChance = (int) (100 * MaterialUtil.getExtendChance(craftingLevel));
    float slotScore = crData.openSlotChance(effectiveLevelAdvantage);

    List<String> newLore = new ArrayList<>();
    newLore.add(FaceColor.WHITE + FaceColor.BOLD.s() +
        "Item Level: " + minItemLevel + " ~ " + maxItemLevel);
    newLore.add(FaceColor.WHITE + FaceColor.BOLD.s() +
        "Crafting Level Requirement: " + (int) Math.max(1, craftingLevel - levelAdvantage));
    newLore.add(FaceColor.GRAY + FaceColor.ITALIC.s() +
        " Based on average material level");
    newLore.add(FaceColor.WHITE + FaceColor.BOLD.s() +
        "Minimum Rarity: " + getRarityTag(minRarity));
    newLore.add(FaceColor.WHITE + FaceColor.BOLD.s() +
        "Maximum Rarity: " + "\uD86D\uDFE9");
    newLore.add(FaceColor.GRAY + FaceColor.ITALIC.s() +
        " Based on material rarity and");
    newLore.add(FaceColor.GRAY + FaceColor.ITALIC.s() +
        " Crafting level");

    if (craftingLevel >= 10) {
      newLore.add(FaceColor.CYAN + FaceColor.BOLD.s() +
          "Essence Slot Chance: " + Math.round(100 * slotScore * 0.2) + "%");
      newLore.add(FaceColor.GRAY + FaceColor.ITALIC.s() +
          " Crafted stats will be essence slots");
      newLore.add(FaceColor.GRAY + FaceColor.ITALIC.s() +
          " instead with a higher Crafting Level");
    }

    if (craftingLevel < 11) {
      newLore.add(FaceColor.BLUE + FaceColor.BOLD.s() +
          "Enchantable Chance: " + (craftingLevel >= 10 ? "100%" : "15%"));
      newLore.add(FaceColor.GRAY + FaceColor.ITALIC.s() +
          " 15% Base Chance");
      newLore.add(FaceColor.GRAY + FaceColor.ITALIC.s() +
          " 100% Chance at Crafting Lv10");
    }

    if (craftingLevel >= 5) {
      newLore.add(FaceColor.ORANGE + FaceColor.BOLD.s() +
          "Minimum Sockets: " + minSockets);
      newLore.add(FaceColor.ORANGE + FaceColor.BOLD.s() +
          "Maximum Sockets: " + Math.max(minSockets, maxSockets));
      newLore.add(FaceColor.GRAY + FaceColor.ITALIC.s() +
          " Max. Sockets is based on quality.");
      newLore.add(FaceColor.GRAY + FaceColor.ITALIC.s() +
          " +1 Min. Sockets at Crafting Lv25");
      newLore.add(FaceColor.GRAY + FaceColor.ITALIC.s() +
          " +1 Min. Sockets at Crafting Lv60");
    }

    if (craftingLevel >= 20) {
      newLore.add(FaceColor.TEAL + FaceColor.BOLD.s() +
          "Extendable Chance: " + extendChance + "%");
      newLore.add(FaceColor.GRAY + FaceColor.ITALIC.s() +
          " +25% Chance at Crafting Lv20");
      newLore.add(FaceColor.GRAY + FaceColor.ITALIC.s() +
          " +25% Chance at Crafting Lv55");
      newLore.add(FaceColor.GRAY + FaceColor.ITALIC.s() +
          " +50% Chance at Crafting Lv85");
    }

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
