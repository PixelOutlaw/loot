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
package info.faceland.loot.commands;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static info.faceland.loot.utils.ItemRenderer.buildStatResponse;

import com.destroystokyo.paper.Namespaced;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandCompletion;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandPermission;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Default;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import com.tealcube.minecraft.bukkit.shade.acf.bukkit.contexts.OnlinePlayer;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.google.gson.Gson;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.ItemGenerationReason;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.StatResponse;
import info.faceland.loot.data.export.ExportEntry;
import info.faceland.loot.data.pdc.StoredStat;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.items.CosmeticHead;
import info.faceland.loot.items.CraftMaterial;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.LevelModItem;
import info.faceland.loot.items.NewCustomItem;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.SoulGem;
import info.faceland.loot.items.Tier;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.items.prefabs.*;
import info.faceland.loot.menu.gemcutter.GemcutterMenu;
import info.faceland.loot.menu.gemsmasher.GemSmashMenu;
import info.faceland.loot.menu.pawn.PawnMenu;
import info.faceland.loot.menu.transmute.TransmuteMenu;
import info.faceland.loot.menu.upgrade.EnchantMenu;
import info.faceland.loot.utils.DropUtil;
import info.faceland.loot.utils.EssenceUtil;
import info.faceland.loot.utils.InventoryUtil;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.dinvy.entity.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

@CommandAlias("loot")
public class LootCommand extends BaseCommand {

  private final LootPlugin plugin;
  private String awardFormat;
  private String awardFormatSelf;

  private final GemcutterMenu gemcutterMenu;
  private final GemSmashMenu gemsmasherMenu;

  private final Gson gson = new Gson();

  public LootCommand(LootPlugin plugin) {
    this.plugin = plugin;
    gemcutterMenu = new GemcutterMenu(plugin);
    gemsmasherMenu = new GemSmashMenu(plugin);
    awardFormat = plugin.getSettings().getString("language.broadcast.reward-item", "");
    awardFormatSelf = plugin.getSettings().getString("language.broadcast.reward-item-self", "");
  }

  @Subcommand("deco")
  @CommandPermission("loot.deco")
  public void decoCreate(Player sender) {
    ItemStack stack = sender.getEquipment().getItemInMainHand();
    if (stack == null || stack.getType() == Material.AIR) {
      sendMessage(sender, "&eNo valid item in hand");
      return;
    }
    Item item = sender.getWorld().dropItem(sender.getLocation(), stack);
    item.setInvulnerable(true);
    item.setGravity(false);
    item.setCanMobPickup(false);
    item.setCanPlayerPickup(false);
    item.setUnlimitedLifetime(true);
    item.setWillAge(false);
    item.setVelocity(new Vector(0, 0, 0));
    sendMessage(sender, "&aSuccess!");
  }

  @Subcommand("deco delete")
  @CommandPermission("loot.deco")
  public void decoDelete(Player sender) {
    RayTraceResult result = sender.getWorld().rayTraceEntities(sender.getEyeLocation(),
        sender.getLocation().getDirection(), 20, e -> e.getType() == EntityType.ITEM);
    if (result == null || result.getHitEntity() == null) {
      MessageUtils.sendMessage(sender, "&eNo target found...");
      return;
    }
    Item item = (Item) result.getHitEntity();
    item.remove();
    sendMessage(sender, "&aSuccess!");
  }

  @Subcommand("name-frame")
  @CommandPermission("loot.name-frame")
  public void memeCommand(Player sender, String text) {
    RayTraceResult result = sender.getWorld().rayTraceEntities(sender.getEyeLocation(),
        sender.getLocation().getDirection(), 20, e -> e.getType() == EntityType.ITEM_FRAME);
    if (result == null || result.getHitEntity() == null) {
      MessageUtils.sendMessage(sender, "&eNo target found...");
      return;
    }
    ItemFrame frame = (ItemFrame) result.getHitEntity();
    if (frame.getItem() == null || frame.getItem().getType() == Material.AIR) {
      MessageUtils.sendMessage(sender, "&eItem frame is empty...");
      return;
    }
    text = FaceColor.YELLOW + FaceColor.ITALIC.s() + text;
    ItemStack frameStack = frame.getItem().clone();
    ItemStackExtensionsKt.setDisplayName(frameStack, text);

    frame.setItem(frameStack, true);
    MessageUtils.sendMessage(sender, "&aSuccess!");
  }

  @Subcommand("mortar")
  @CommandPermission("loot.admin")
  public void gemdestroy(CommandSender sender, OnlinePlayer player) {
    plugin.getMortarMenu().open(player.getPlayer());
  }

  @Subcommand("loreglass")
  @CommandPermission("loot.admin")
  public void lorgrass(CommandSender sender, OnlinePlayer player) {
    plugin.getLoreglassMenu().open(player.getPlayer());
  }

  @Subcommand("trade")
  @CommandPermission("loot.admin")
  public void tradeOpen(CommandSender sender, OnlinePlayer player, String tradeId, String menuName) {
    plugin.getTradeMenuManager().startTrade(player.getPlayer(), tradeId, menuName);
  }

  @Subcommand("export")
  @CommandPermission("loot.export")
  public void exportCommand(CommandSender sender) {

    List<ExportEntry> exportGems = new ArrayList<>();
    for (SocketGem gem : SocketGem.getGems().values()) {
      try {
        ExportEntry exportGem = new ExportEntry();
        exportGem.setTitle("Socket Gem");
        exportGem.setName("|lime|" + gem.getName());
        exportGem.setDescription(SocketGem.getExportLore(gem));
        exportGem.getGroupNames().add(gem.getItemGroup().getName());
        exportGem.getGroupNames().add("Upgrade");
        if (gem.getWeight() == 0) {
          exportGem.setSpecialFlag(gem.getBonusWeight() > 0 ? "transmute" : "event");
          exportGem.setRarity(
              gem.getBonusWeight() > 500 ? "Common" :
                  gem.getBonusWeight() > 50 ? "Uncommon" :
                      gem.getBonusWeight() > 5 ? "Rare" : "Epic");
        } else {
          exportGem.setRarity(
              gem.getWeight() > 500 ? "Common" :
                  gem.getWeight() > 50 ? "Uncommon" :
                      gem.getWeight() > 5 ? "Rare" : "Epic");
        }
        exportGems.add(exportGem);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    List<ExportEntry> exportTomes = new ArrayList<>();
    for (EnchantmentTome tome : plugin.getEnchantTomeManager().getEnchantmentTomes()) {
      try {
        if (tome.getWeight() == 0) {
          continue;
        }
        ExportEntry exportTome = new ExportEntry();
        exportTome.setTitle("Enchantment Tome");
        exportTome.setName("|blue|" + tome.getName());
        List<String> lore = new ArrayList<>();
        lore.add(tome.getDescription());
        exportTome.setDescription(lore);
        exportTome.getGroupNames().add(tome.getItemGroup().getName());
        exportTome.getGroupNames().add("Upgrade");
        exportTome.getGroupNames().add("Enchanting");
        exportTome.setRarity(
            tome.getWeight() > 500 ? "Common" :
                tome.getWeight() > 200 ? "Uncommon" : "Rare"
        );
        exportTomes.add(exportTome);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    List<ExportEntry> exportUniques = new ArrayList<>();
    for (var unique : plugin.getCustomItemManager().getCustomItems()) {
      try {
        if (unique.getWeight() > 10000 || !unique.isExport() || unique.getCoreStats().isEmpty()) {
          continue;
        }
        ExportEntry exportUnique = new ExportEntry();
        exportUnique.setTitle("Unique Item");
        exportUnique.setName("|orange|" + unique.getItemName());
        exportUnique.setTier(unique.getTier().getPlainName());
        List<String> lore = new ArrayList<>();
        if (unique.getTier().isSkillRequirement()) {
          lore.add("|white|Skill Requirement: " + unique.getItemLevel());
        } else {
          lore.add("|white|Level Requirement: " + unique.getItemLevel());
        }
        lore.add("");
        for (var stat : unique.getCoreStats()) {
          if (stat.getStat() == null || stat.getStat().isHide()) {
            continue;
          }
          StoredStat stored1 = new StoredStat(stat.getStat().getId(), 1f);
          stored1.setUniqueMult(stat.getValueModifier(), unique.getId());
          int lvl = unique.getItemLevel();
          StatResponse sr1 = buildStatResponse(plugin, lvl, 0, 2, stored1, 0);
          if (sr1 == null) {
            continue;
          }
          var str = ChatColor.stripColor(sr1.getStatString());
          lore.add("|lgray|" + str);
        }
        lore.add("");
        for (var stat : unique.getBonusStats()) {
          if (stat.getStat() == null || stat.getStat().isHide()) {
            continue;
          }
          StoredStat stored1 = new StoredStat(stat.getStat().getId(), 0f);
          stored1.setUniqueMult(stat.getValueModifier(), unique.getId());
          StoredStat stored2 = new StoredStat(stat.getStat().getId(), 1f);
          stored2.setUniqueMult(stat.getValueModifier(), unique.getId());
          int lvl = unique.getItemLevel();
          StatResponse sr1 = buildStatResponse(plugin, lvl, 0, 2, stored1, 0);
          StatResponse sr2 = buildStatResponse(plugin, lvl, 0, 2, stored2, 0);
          if (sr1 == null || sr2 == null) {
            continue;
          }
          var str = ChatColor.stripColor(sr1.getStatString());
          int val1 = stored1.getCachedValue();
          int val2 = stored2.getCachedValue();
          if (val1 == val2) {
            if (val1 < 0) {
              lore.add("|red|" + str);
            } else {
              lore.add("|yellow|" + str);
            }
          } else {
            str = str.replace(Integer.toString(val1), "{}");
            str = str.replace("{}", "(" + Math.abs(val1) + " to " + Math.abs(val2) + ")");
            if (val1 < 0) {
              lore.add("|red| -" + str);
            } else {
              lore.add("|yellow|" + str);
            }
          }

        }
        lore.add("");
        exportUnique.setEnchantable(unique.isEnchantable());
        exportUnique.setGemSlots(unique.getSocketSlots());
        exportUnique.setExtendSlots(unique.getExtendSlots());
        exportUnique.setDescription(lore);

        List<String> passives = new ArrayList<>();
        unique.getPassives().forEach(passiveStr -> passives.addAll(
            plugin.getStrifePlugin().getLoreAbilityManager().getLoreAbilityFromId(passiveStr).getRawDescription()));
        unique.getTraits().forEach(traitStr -> passives.addAll(StrifeTrait.valueOf(traitStr).getAdditionalTextRaw()));
        exportUnique.setPassives(passives);

        List<String> flavor = new ArrayList<>();
        unique.getRawFlavorText().forEach(s -> flavor.add("|gray||i|" + ChatColor.stripColor(s)));
        exportUnique.setFlavorText(flavor);
        exportUnique.setRarity(switch (unique.getTooltip().getKey()) {
          case "equipment_common" -> "Common";
          case "equipment_uncommon" -> "Uncommon";
          case "equipment_epic" -> "Epic";
          default -> "Rare";
        });
        var gs = new ArrayList<String>();
        for (var entry : ItemGroup.GROUPS.entrySet()) {
          if (entry.getValue().getValidTiers().contains(unique.getTier().getId())) {
            gs.add(entry.getValue().getName());
          }
        }
        exportUnique.setGroupNames(gs);
        exportUniques.add(exportUnique);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    List<ExportEntry> exportScrolls = new ArrayList<>();
    for (UpgradeScroll scroll : plugin.getScrollManager().getScrolls()) {
      try {
        ExportEntry exportScroll = new ExportEntry();
        exportScroll.setTitle("Upgrade Scroll");
        exportScroll.setName("|green|" + scroll.getPrefix() + " Upgrade Scroll");
        List<String> lore = new ArrayList<>(scroll.getLore());
        lore.removeFirst();
        lore.removeFirst();
        if (StringUtils.isBlank(lore.getFirst())) {
          lore.removeFirst();
        }
        exportScroll.setDescription(lore);
        exportScroll.setRarity(
            scroll.getWeight() > 8500 ? "Common" :
                scroll.getWeight() > 2500 ? "Uncommon" :
                    scroll.getWeight() > 150 ? "Rare" : "Epic");
        exportScroll.setGroupNames(List.of("Upgrade", "Enchanting"));
        exportScrolls.add(exportScroll);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    List<String> tags = new ArrayList<>();
    for (var tag : ItemGroup.GROUPS.values()) {
      tags.add(tag.getName());
    }
    tags.add("Upgrade");
    tags.add("Enchanting");

    try (FileWriter writer = new FileWriter(plugin.getDataFolder() + "/export/gems.json")) {
      gson.toJson(exportGems, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try (FileWriter writer = new FileWriter(plugin.getDataFolder() + "/export/tomes.json")) {
      gson.toJson(exportTomes, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try (FileWriter writer = new FileWriter(plugin.getDataFolder() + "/export/uniques.json")) {
      gson.toJson(exportUniques, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try (FileWriter writer = new FileWriter(plugin.getDataFolder() + "/export/scrolls.json")) {
      gson.toJson(exportScrolls, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try (FileWriter writer = new FileWriter(plugin.getDataFolder() + "/export/tags.json")) {
      gson.toJson(tags, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Subcommand("reward")
  @CommandCompletion("@players @range:1-100 @range:1-100")
  @CommandPermission("loot.reward")
  public void memeCommand(CommandSender sender, OnlinePlayer player, int minLevel, int maxLevel,
      String rarity) {
    Tier t = DropUtil.getTier(player.getPlayer(), maxLevel);
    if (t == null) {
      sendMessage(sender,
          plugin.getSettings().getString("language.commands.spawn.other-failure", ""));
      return;
    }

    int level = minLevel + (int) ((maxLevel - minLevel) * LootPlugin.RNG.nextFloat());

    ItemRarity itemRarity = plugin.getRarityManager().getRarity(rarity);

    ItemStack item = plugin.getNewItemBuilder()
        .withTier(t)
        .withLevel(level)
        .withRarity(itemRarity)
        .withItemGenerationReason(ItemGenerationReason.COMMAND)
        .build().getStack();

    player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
    player.getPlayer()
        .playSound(player.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
    player.getPlayer().getInventory().addItem(item);
    if (itemRarity.isBroadcast()) {
      InventoryUtil.sendToDiscord(player.getPlayer(), item, awardFormat, true);
    } else {
      InventoryUtil.sendToDiscord(player.getPlayer(), item, awardFormatSelf, false);
    }
    sendMessage(sender, "Rewarded " + player.getPlayer().getName() + " successfully!");
  }

  @Subcommand("give")
  @CommandPermission("loot.give")
  public class GiveCommand extends BaseCommand {

    @Subcommand("essence")
    @CommandCompletion("@players @tiers @rarities itemLevel statId percentRoll")
    public void giveEssence(CommandSender sender, OnlinePlayer player, @Default("sword") String tier,
        @Default("RARE") String rarity, @Default("1") int level, String stat, double percentRoll) {
      Tier t = Tier.get(tier);
      if (t == null) {
        sender.sendMessage("Invalid Tier");
        return;
      }
      ItemRarity ir = plugin.getRarityManager().getRarity(rarity);
      if (ir == null) {
        sender.sendMessage("Invalid Rarity");
        return;
      }
      if (t.getEssenceId() == -1) {
        sender.sendMessage("Tier cannot have an essence");
        return;
      }
      ItemStack itemEss = EssenceUtil.createEssence(stat, tier, level, percentRoll, ir.getPower());
      player.getPlayer().getInventory().addItem(itemEss);
      sendMessage(sender, "Success!");
    }

    @Subcommand("material")
    @CommandCompletion("@players @materials")
    public void giveEssence(CommandSender sender, OnlinePlayer player, String id) {
      var mat = CraftMaterial.get(id);
      if (mat != null) {
        ItemUtils.giveOrDrop(player.getPlayer(), mat.item());
        sendMessage(sender, "Success!");
      } else {
        sendMessage(sender, "Not a real material!!!");
      }
    }

    @Subcommand("level-mod")
    @CommandCompletion("@players @levelmoditems")
    public void giveLevelModItem(CommandSender sender, OnlinePlayer player, String id) {
      var mat = LevelModItem.get(id);
      if (mat != null) {
        ItemUtils.giveOrDrop(player.getPlayer(), mat.item());
        sendMessage(sender, "Success!");
      } else {
        sendMessage(sender, "Not a real level mod item!!!");
      }
    }

    @Subcommand("unidTome")
    @CommandCompletion("@players")
    public void giveUnidTome(CommandSender sender, OnlinePlayer player) {
      ItemUtils.giveOrDrop(player.getPlayer(), UnidEnchant.item());
      sendMessage(sender, "Success!");
    }

    @Subcommand("soulgem")
    @CommandCompletion("@players @soulgems")
    public void getSoulGem(CommandSender sender, OnlinePlayer player, String material) {
      var gemb = SoulGem.get(material);
      if (gemb != null) {
        ItemUtils.giveOrDrop(player.getPlayer(), gemb.item());
        sendMessage(sender, "Success!");
      } else {
        sendMessage(sender, "Not a real gemb!!!");
      }
    }

    @Subcommand("head")
    @CommandCompletion("@players @heads")
    public void getHead(CommandSender sender, OnlinePlayer player, String material) {
      var gemb = CosmeticHead.get(material);
      if (gemb != null) {
        ItemUtils.giveOrDrop(player.getPlayer(), gemb.item());
        sendMessage(sender, "Success!");
      } else {
        sendMessage(sender, "Not a real head!!!");
      }
    }

    @Subcommand("tier")
    @CommandCompletion("@players @tiers @rarities amount level distorted")
    public void giveTier(CommandSender sender, OnlinePlayer player,
        String tier,
        String rarity,
        @Default("1") int amount,
        @Default("-1") int level,
        @Default("false") boolean distorted,
        @Default("false") boolean soulbound
    ) {
      Tier t = tier.equalsIgnoreCase("random") ? Tier.getRandomTier(level) : Tier.get(tier);
      ItemRarity r = rarity.equalsIgnoreCase("random") ? null : plugin.getRarityManager().getRarity(rarity);
      if (r == null) {
        r = plugin.getRarityManager().getRandomRarity(1, -1);
      }
      for (int i = 0; i < amount; i++) {
        ItemStack stack = plugin.getNewItemBuilder().withItemGenerationReason(ItemGenerationReason.COMMAND)
            .withTier(t)
            .withRarity(r)
            .withLevel(level == -1 ? 1 + LootPlugin.RNG.nextInt(100) : level)
            .withDistortion(distorted)
            .withCreator(player.getPlayer())
            .withSoulbound(soulbound)
            .build()
            .getStack();
        ItemUtils.giveOrDrop(player.getPlayer(), stack);
      }
      sendMessage(sender,
          plugin.getSettings().getString("language.commands.spawn.other-success", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("tome")
    @CommandCompletion("@players @tomes @range:1-100")
    public void giveTome(CommandSender sender, OnlinePlayer player, String id,
        @Default("1") int amount) {
      if (id.equalsIgnoreCase("random")) {
        for (int i = 0; i < amount; i++) {
          player.getPlayer().getInventory().addItem(
              plugin.getEnchantTomeManager().getRandomEnchantTome().toItemStack(1));
        }
        sendMessage(sender,
            plugin.getSettings().getString("language.commands.spawn.stone-success", ""),
            new String[][]{{"%amount%", amount + ""}});
        return;
      }
      EnchantmentTome es = plugin.getEnchantTomeManager().getEnchantTome(id);
      if (es == null) {
        sendMessage(sender,
            plugin.getSettings().getString("language.commands.spawn.stone-failure", ""));
        return;
      }
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(es.toItemStack(1));
      }
      sendMessage(sender,
          plugin.getSettings().getString("language.commands.spawn.stone-success", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("gem")
    @CommandCompletion("@players @gems @range:1-100")
    public void giveGem(CommandSender sender, OnlinePlayer player, String id, @Default("1") int amount) {
      if (id.equalsIgnoreCase("random")) {
        for (int i = 0; i < amount; i++) {
          giveItem(player.getPlayer(), plugin.getSocketGemManager().getRandomSocketGem(true).toItemStack(1));
        }
        sendMessage(sender,
            plugin.getSettings().getString("language.commands.spawn.gem-success", ""),
            new String[][]{{"%amount%", amount + ""}});
        return;
      }
      SocketGem sg = plugin.getSocketGemManager().getSocketGem(id);
      if (sg == null) {
        sendMessage(sender,
            plugin.getSettings().getString("language.commands.spawn.gem-failure", ""));
        return;
      }
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(sg.toItemStack(1));
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.gem-success", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("unique")
    @CommandCompletion("@players @uniques @range:1-10")
    public void giveUnique(
        CommandSender sender,
        OnlinePlayer player,
        String id,
        @Default("1") int amount,
        @Default("false") boolean soulbound
    ) {
      if (id.equalsIgnoreCase("random")) {
        for (int i = 0; i < amount; i++) {
          ItemStack item = plugin.getCustomItemManager().getRandomByLevel(player.getPlayer().getLevel()).item();
          if (soulbound) {
            PersistHelper.setTradeLocked(item, true);
          }
          ItemUtils.giveOrDrop(player.getPlayer(), item);
        }
        sendMessage(sender,
            plugin.getSettings().getString("language.commands.spawn.custom-success", ""),
            new String[][]{{"%amount%", amount + ""}});
        return;
      }
      NewCustomItem ci = plugin.getCustomItemManager().getCustomItem(id);
      if (ci == null) {
        sendMessage(sender,
            plugin.getSettings().getString("language.commands.spawn.custom-failure", ""));
        return;
      }
      for (int i = 0; i < amount; i++) {
        ItemStack stack = ci.item();
        if (soulbound) {
          PersistHelper.setTradeLocked(stack, true);
        }
        ItemUtils.giveOrDrop(player.getPlayer(), stack);
      }
      sendMessage(sender,
          plugin.getSettings().getString("language.commands.spawn.custom-success", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("unique-equip")
    @CommandCompletion("@players @uniques")
    public void giveUnique(CommandSender sender, OnlinePlayer player, String id, String dSlot) {
      NewCustomItem ci = plugin.getCustomItemManager().getCustomItem(id);
      if (ci == null) {
        sendMessage(sender,
            plugin.getSettings().getString("language.commands.spawn.custom-failure", ""));
        return;
      }
      DeluxeSlot slot;
      try {
        slot = DeluxeSlot.valueOf(dSlot);
      } catch (Exception e) {
        sendMessage(sender, "[Loot] No unique-equip slot " + dSlot);
        return;
      }
      ItemStack stack = ci.item();
      PlayerData playerData = DeluxeInvyPlugin.getInstance()
          .getPlayerManager().getPlayerData(player.getPlayer());
      if (playerData.getEquipmentItem(slot) == null) {
        playerData.setEquipmentItem(slot, stack);
      } else {
        giveItem(player.getPlayer(), stack);
      }
      sendMessage(sender, plugin.getSettings().getString(
          "language.commands.spawn.custom-success", ""), new String[][]{{"%amount%", 1 + ""}});
    }

    @Subcommand("scroll")
    @CommandCompletion("@players @scrolls @range:1-100")
    public void giveScroll(CommandSender sender, OnlinePlayer player, String id,
        @Default("1") int amount) {
      if (id.equalsIgnoreCase("random")) {
        for (int i = 0; i < amount; i++) {
          player.getPlayer().getInventory().addItem(plugin.getScrollManager().getRandomScroll().item());
        }
        sendMessage(sender, plugin.getSettings()
                .getString("language.commands.spawn.upgrade-scroll", ""),
            new String[][]{{"%amount%", amount + ""}});
        return;
      }
      UpgradeScroll scroll = UpgradeScroll.get(id);
      if (scroll == null) {
        sendMessage(sender, plugin.getSettings()
            .getString("language.commands.spawn.other-failure", ""));
        return;
      }
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(scroll.item());
      }
      sendMessage(sender, plugin.getSettings()
          .getString("language.commands.spawn.upgrade-scroll", ""));
    }

    @Subcommand("extender")
    @CommandCompletion("@players @range:1-100")
    public void giveExtender(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(SocketExtender.EXTENDER.clone());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("reroll")
    @CommandCompletion("@players @range:1-100")
    public void giveFaceOrb(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(OrbOfManyFaces.item());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("reroll2")
    @CommandCompletion("@players @range:1-100")
    public void giveHyperCube(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(PerfectSphere.item());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("reroll3")
    @CommandCompletion("@players @range:1-100")
    public void giveDivineHammer(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(DivineHammer.item());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("tongs")
    @CommandCompletion("@players @range:1-100")
    public void giveTongs(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(WarpedTongs.item());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("flip")
    @CommandCompletion("@players @range:1-100")
    public void giveFlip(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(OrbOfOscillation.item());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("trash")
    @CommandCompletion("@players @range:1-100")
    public void giveTrash(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(TraceOfTrash.item());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("normal")
    @CommandCompletion("@players @range:1-100")
    public void giveNormalItem(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(NormalItem.item());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("refill|manacrystal")
    @CommandCompletion("@players @range:1-100")
    public void manananananaa(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(ManaCrystal.item());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("perfection")
    @CommandCompletion("@players @range:1-100")
    public void perfection(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(AspectOfPerfection.item());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("distortcell")
    @CommandCompletion("@players @range:1-100")
    public void distortion(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(DistortionCell.item());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("awaken")
    @CommandCompletion("@players @range:1-100")
    public void awkenerrr(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(AwakeningStone.item());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("missing")
    @CommandCompletion("@players @range:1-100")
    public void missin(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(MissingStat.item());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("raritypaint")
    @CommandCompletion("@players @range:1-100")
    public void painer(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(TooltipPaint.item());
      }
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("purity|purify")
    @CommandCompletion("@players @range:1-100")
    public void givePurity(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(PurifyingScroll.get());
      }
      sendMessage(sender,
          plugin.getSettings().getString("language.commands.spawn.purify-scroll", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("tinker")
    @CommandCompletion("@players @range:1-100")
    public void giveTinker(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(TinkerersGear.get());
      }
      sendMessage(sender,
          plugin.getSettings().getString("language.commands.spawn.tinker-gear", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("enhancer")
    @CommandCompletion("@players @range:1-100")
    public void giveEnhancer(CommandSender sender, OnlinePlayer player, @Default("1") int amount) {
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(ArcaneEnhancer.get());
      }
      sendMessage(sender,
          plugin.getSettings().getString("language.commands.spawn.arcane-enhancer", ""),
          new String[][]{{"%amount%", amount + ""}});
    }
  }

  @Subcommand("reload")
  @CommandPermission("loot.reload")
  public void reloadSubcommand(CommandSender sender) {
    plugin.disable();
    plugin.enable();
    sendMessage(sender,
        plugin.getSettings().getString("language.command.reload", "&aLoot reloaded!"));
  }

  @Subcommand("upgrade")
  @CommandCompletion("@players")
  @CommandPermission("loot.upgrade")
  public void upgradeSubcommand(CommandSender sender, OnlinePlayer target) {
    if (target == null || !target.getPlayer().isValid()) {
      return;
    }
    EnchantMenu menu = new EnchantMenu(plugin);
    menu.open(target.getPlayer());
  }

  @Subcommand("salvage")
  @CommandCompletion("@players")
  @CommandPermission("loot.upgrade")
  public void salvageItem(CommandSender sender, OnlinePlayer target, int levelReq) {
    if (target == null || !target.getPlayer().isValid()) {
      return;
    }
    if (levelReq > PlayerDataUtil.getLifeSkillLevel(target.getPlayer(), LifeSkillType.CRAFTING)) {
      PaletteUtil.sendMessage(target.getPlayer(), "|yellow|Your crafting level is too low to use this!");
      return;
    }
    plugin.getSalvageMenu().setEquipment(target.getPlayer(), null);
    plugin.getSalvageMenu().setTool(target.getPlayer(), null);
    plugin.getSalvageMenu().setResult(target.getPlayer(), null);
    /*
    if (target.getPlayer().getEquipment().getItemInMainHand().getType() == Material.NETHER_BRICK) {
      plugin.getSalvageMenu().setTool(target.getPlayer(), target.getPlayer().getEquipment().getItemInMainHand());
    }
    */
    plugin.getSalvageMenu().open(target.getPlayer());
  }

  @Subcommand("dumb")
  @CommandPermission("loot.upgrade")
  public void dumb(Player sender, String shit) {
    ItemStackExtensionsKt.setDisplayName(sender.getEquipment().getItemInMainHand(), PaletteUtil.color(shit));
  }

  @Subcommand("pawn")
  @CommandCompletion("@players")
  @CommandPermission("loot.pawn")
  public void pawnSubcommand(CommandSender sender, OnlinePlayer target, @Default("") String dealId) {
    if (target == null || !target.getPlayer().isValid()) {
      return;
    }
    PawnMenu menu = plugin.getPawnManager().getPawnMenu(dealId);
    if (menu == null) {
      sender.sendMessage("[Loot] No pawn menu found for ID " + dealId);
      return;
    }
    menu.open(target.getPlayer());
  }

  @Subcommand("inspectBreakKeys")
  @CommandPermission("loot.inspect")
  public void inspectKeys(Player sender) {
    ItemStack stack = sender.getEquipment().getItemInMainHand();
    for (Namespaced k : stack.getItemMeta().getDestroyableKeys()) {
      sendMessage(sender, "key: " + k.getKey());
      sendMessage(sender, "namespace: " + k.getNamespace());
    }
  }

  @Subcommand("gemcutter")
  @CommandPermission("loot.cut")
  public void inspectKeys(CommandSender sender, OnlinePlayer target) {
    gemcutterMenu.setSelectedItem(target.getPlayer(), null);
    gemcutterMenu.open(target.getPlayer());
  }

  @Subcommand("gemsmasher")
  @CommandPermission("loot.cut")
  public void openSmash(CommandSender sender, OnlinePlayer target) {
    gemsmasherMenu.setData(target.getPlayer(), null);
    gemsmasherMenu.open(target.getPlayer());
  }

  @Subcommand("transmute")
  @CommandPermission("loot.transmute")
  public void openTransmuator(CommandSender sender, OnlinePlayer target) {
    TransmuteMenu menu = new TransmuteMenu(plugin);
    menu.open(target.player);
  }

  @Subcommand("rename")
  @CommandCompletion("@players")
  @CommandPermission("loot.rename")
  public void renameSubcommand(Player sender, String newLore) {
    ItemStack heldItem = new ItemStack(sender.getEquipment().getItemInMainHand());
    if (heldItem.getType() != Material.NAME_TAG) {
      sendMessage(sender, plugin.getSettings().getString("language.command.renamefail", ""));
      return;
    }
    if (!(ItemStackExtensionsKt.getDisplayName(heldItem)
        .equals(ChatColor.WHITE + "Item Rename Tag"))) {
      sendMessage(sender, plugin.getSettings().getString("language.command.renamefail", ""));
      return;
    }
    if (isIllegalName(ChatColor.stripColor(newLore))) {
      sendMessage(sender, plugin.getSettings().getString("language.command.invalidname", ""));
      return;
    }
    if (newLore.length() > 20 || newLore.startsWith("+") || Character.isDigit(newLore.charAt(0)) ||
        Character.isDigit(newLore.charAt(1))) {
      sendMessage(sender, plugin.getSettings().getString("language.command.invalidname", ""));
      return;
    }
    List<String> lore = TextUtils.getLore(heldItem);
    lore.set(3, ChatColor.WHITE + ChatColor.stripColor(TextUtils.color(newLore)));
    TextUtils.setLore(heldItem, lore);
    sender.getEquipment().setItemInMainHand(heldItem);
    sender.updateInventory();
    sendMessage(sender, plugin.getSettings().getString("language.command.renamesuccess", ""));
  }

  private void giveItem(Player player, ItemStack itemStack) {
    player.getInventory().addItem(itemStack);
  }

  private boolean isIllegalName(String name) {
    return name.equals("Socket Extender") || name.startsWith("Enchantment Tome") ||
        name.startsWith("Socket Gem") || name.startsWith("Scroll Augment") ||
        name.endsWith("Upgrade Scroll") || name.equals("Faceguy's Tears") ||
        name.equals("Identity Tome") || name.equals("Unidentified Item") ||
        name.equals("Item Rename Tag") || name.equals("Magic Crystal") ||
        name.equals("Soul Stone");
  }
}
