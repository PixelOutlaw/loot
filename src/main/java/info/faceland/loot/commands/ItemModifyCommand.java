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
package info.faceland.loot.commands;

import static info.faceland.loot.LootPlugin.ITEM_DATA_CLASS;
import static info.faceland.loot.LootPlugin.ITEM_DATA_KEY;
import static info.faceland.loot.LootPlugin.ITEM_EXTRA_KEY;
import static info.faceland.loot.LootPlugin.ITEM_TYPE_KEY;
import static info.faceland.loot.LootPlugin.ITEM_VERSION;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;

import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandCompletion;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandPermission;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Default;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Optional;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.ItemDetails;
import info.faceland.loot.data.pdc.StoredStat;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.utils.EssenceUtil;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.ModifyEquipmentUtil;
import info.faceland.loot.utils.PersistHelper;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.champion.SkillRank;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

@CommandAlias("ir")
public class ItemModifyCommand extends BaseCommand {

  private final LootPlugin plugin;

  public ItemModifyCommand(LootPlugin plugin) {
    this.plugin = plugin;
  }

  @Default
  public void updateItem(Player player) {
    player.sendMessage("aaaaaaaaaAAAAAAAAAAAAAAAAAAAAA");
  }

  @Subcommand("render")
  @CommandPermission("loot.editor")
  public void render(Player player, @Default("false") boolean force) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    if (stack.getType() == Material.AIR) {
      player.sendMessage("hold an item");
      return;
    }
    ItemRenderer.renderItem(stack, force);
  }

  @Subcommand("read")
  @CommandPermission("loot.editor")
  public void read(Player player) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    if (stack.getType() == Material.AIR) {
      player.sendMessage("hold an item");
      return;
    }
    player.sendMessage("reeee: " + PersistHelper.getItemId(player.getItemInHand()));
    player.sendMessage("reeee2: " + PersistHelper.getItemType(player.getItemInHand()));
    ItemRenderer.renderItem(stack, false);
    StoredStat ss = PersistHelper.getEssenceStat(stack);
    if (ss != null) {
      player.sendMessage(ss.toString());
      return;
    }
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    ItemDetails details = data.buildItemDetails();
    for (var entry : details.getStats().entrySet()) {
      player.sendMessage(entry.getKey() + "/" + entry.getValue());
    }
    for (var tr : details.getTraits()) {
      player.sendMessage(tr.getName());
    }
    for (var la : details.getLoreAbilities()) {
      player.sendMessage(la.getId());
    }
  }

  @Subcommand("set")
  @CommandPermission("loot.editor")
  public void render(Player player, String itemType, @Optional String extraData) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    if (stack.getType() == Material.AIR) {
      player.sendMessage("hold an item");
      return;
    }
    ItemMeta meta = stack.getItemMeta();
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    pdc.set(ITEM_TYPE_KEY, PersistentDataType.STRING, itemType);
    if (extraData != null && !extraData.isBlank()) {
      pdc.set(ITEM_EXTRA_KEY, PersistentDataType.STRING, extraData);
    }
    pdc.set(ITEM_DATA_KEY, ITEM_DATA_CLASS, new EquipmentData());
    stack.setItemMeta(meta);
    ItemRenderer.renderItem(stack, true);
  }

  @Subcommand("edit level")
  @CommandPermission("loot.editor")
  public void editlevel(Player player, int level) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    data.setLevel(level);
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit name-base")
  @CommandPermission("loot.editor")
  public void editNameBase(Player player, String text) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    data.setString("name", text);
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit upgrade-level")
  @CommandPermission("loot.editor")
  public void editUpgradeLevel(Player player, int level) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    data.setUpgradeLevel(level);
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit tier")
  @CommandPermission("loot.editor")
  public void render(Player player, String tier) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    data.setTier(tier);
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit rarity")
  @CommandPermission("loot.editor")
  public void setRarity(Player player, String rarity) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    data.setRarity(rarity);
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit add-core-stat")
  @CommandPermission("loot.editor")
  public void render(Player player, String coreStat, float roll) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    ItemStat stat = LootPlugin.getInstance().getStatManager().getStat(coreStat);
    if (stat == null) {
      player.sendMessage("No such stat: " + coreStat);
      return;
    }
    StoredStat storedStat = new StoredStat(coreStat, roll);
    data.addCoreStat(storedStat);
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit remove-core-stat")
  @CommandPermission("loot.editor")
  public void render(Player player, int index) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    if (data.getCoreStats().size() < index + 1) {
      player.sendMessage("stat index does not exist!");
      return;
    }
    data.getCoreStats().remove(index);
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit add-bonus-stat")
  @CommandPermission("loot.editor")
  public void removeBonus(Player player, String bonusStat, float roll, @Default("false") boolean crafted) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    ItemStat stat = LootPlugin.getInstance().getStatManager().getStat(bonusStat);
    if (stat == null) {
      player.sendMessage("No such stat: " + bonusStat);
      return;
    }
    StoredStat storedStat = new StoredStat(bonusStat, roll);
    //storedStat.setCrafted(crafted);
    data.addBonusStat(storedStat);
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit remove-bonus-stat")
  @CommandPermission("loot.editor")
  public void removebonus(Player player, int index) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    if (data.getBonusStats().size() < index + 1) {
      player.sendMessage("stat index does not exist!");
      return;
    }
    data.getBonusStats().remove(index);
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit enchantable")
  @CommandPermission("loot.editor")
  public void removeBonus(Player player, @Default("true") boolean enchantable) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    data.setEnchantable(enchantable);
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit enchant apply")
  @CommandCompletion("@tomes")
  @CommandPermission("loot.editor")
  public void enchantItem(Player player, String tomeId) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    ModifyEquipmentUtil.enchant(stack, EnchantmentTome.get(tomeId), player.getLevel(), 0.1f, SkillRank.MASTER);
  }

  @Subcommand("edit enchant refill")
  @CommandPermission("loot.editor")
  public void refillEnchant(Player player, int amount) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    ModifyEquipmentUtil.refillEnchantment(stack, amount);
  }

  @Subcommand("edit enchant degrade")
  public void degardeEnchant(Player player, int amount) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    ModifyEquipmentUtil.tickEnchantment(stack, amount);
  }

  @Subcommand("edit unenchant")
  @CommandPermission("loot.editor")
  public void enchantItem(Player player) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    ModifyEquipmentUtil.unenchant(stack);
  }

  @Subcommand("edit enhance")
  @CommandPermission("loot.editor")
  public void enhanceItem(Player player, boolean e) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    ModifyEquipmentUtil.enhance(stack);
  }

  @Subcommand("edit reroll")
  @CommandPermission("loot.editor")
  public void reroll(Player player) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    ModifyEquipmentUtil.reroll(stack);
  }

  @Subcommand("edit distort")
  @CommandPermission("loot.editor")
  public void distort(Player player, boolean dist) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    if (dist) {
      ModifyEquipmentUtil.distort(stack);
    } else {
      ModifyEquipmentUtil.undistort(stack);
    }
  }

  @Subcommand("edit socket add")
  @CommandPermission("loot.editor")
  public void removeBonus(Player player) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    data.setEmptyGemSlots(data.getEmptyGemSlots() + 1);
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit socket remove")
  @CommandPermission("loot.editor")
  public void removeSockets(Player player) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    data.setEmptyGemSlots(Math.max(0, data.getEmptyGemSlots() - 1));
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit socket apply")
  @CommandCompletion("@gems")
  @CommandPermission("loot.editor")
  public void removeBonus(Player player, String gemId) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    ModifyEquipmentUtil.applySocket(stack, gemId);
  }

  @Subcommand("edit socket extract")
  //@CommandCompletion("@gems")
  @CommandPermission("loot.editor")
  public void extractGem(Player player, int index) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    ModifyEquipmentUtil.removeSocket(stack, index);
  }

  @Subcommand("edit extender add")
  @CommandPermission("loot.editor")
  public void removeexter(Player player) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    data.setEmptyExtendSlots(data.getEmptyExtendSlots() + 1);
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit extender remove")
  @CommandPermission("loot.editor")
  public void removeext(Player player) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    data.setEmptyExtendSlots(Math.max(0, data.getEmptyExtendSlots() - 1));
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit extender apply")
  @CommandPermission("loot.editor")
  public void extend(Player player) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    ModifyEquipmentUtil.applyExtender(stack);
  }

  @Subcommand("edit craft by")
  @CommandPermission("loot.editor")
  public void serCraftedBy(Player player, String s) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EquipmentData data = validateItemData(player, stack);
    if (data == null) {
      return;
    }
    data.setString("craftedBy", s);
    PersistHelper.setEquipmentData(stack, data);
  }

  @Subcommand("edit craft extract")
  @CommandPermission("loot.editor")
  public void extract(Player player, @Default("false") boolean allowMultiple) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    EssenceUtil.extract(player, stack, false, allowMultiple, true);
  }

  @Subcommand("edit soulbound")
  @CommandPermission("loot.editor")
  public void soulbound(Player player, @Default("false") boolean soulBound) {
    ItemStack stack = player.getEquipment().getItemInMainHand();
    PersistHelper.setTradeLocked(stack, soulBound);
  }
  
  private EquipmentData validateItemData(Player player, ItemStack stack) {
    if (stack.getType() == Material.AIR) {
      player.sendMessage("hold an item");
      return null;
    }
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data == null) {
      player.sendMessage("null data no update");
      return null;
    }
    return data;
  }
}