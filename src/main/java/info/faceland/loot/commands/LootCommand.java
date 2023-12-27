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

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.destroystokyo.paper.Namespaced;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
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
import info.faceland.loot.api.items.CustomItem;
import info.faceland.loot.api.items.ItemGenerationReason;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.data.export.ExportEntry;
import info.faceland.loot.enchantments.EnchantmentTome;
import info.faceland.loot.groups.ItemGroup;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.items.prefabs.TinkerersGear;
import info.faceland.loot.menu.gemcutter.GemcutterMenu;
import info.faceland.loot.menu.gemsmasher.GemSmashMenu;
import info.faceland.loot.menu.pawn.PawnMenu;
import info.faceland.loot.menu.transmute.TransmuteMenu;
import info.faceland.loot.menu.upgrade.EnchantMenu;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.tier.Tier;
import info.faceland.loot.utils.DropUtil;
import info.faceland.loot.utils.InventoryUtil;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.dinvy.entity.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
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
        sender.getLocation().getDirection(), 20, e -> e.getType() == EntityType.DROPPED_ITEM);
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

  @Subcommand("gemdestroy")
  @CommandPermission("loot.admin")
  public void gemdestroy(Player sender, int slot) {
    boolean success = MaterialUtil.destroyGem(sender.getEquipment().getItemInMainHand(), slot) != null;
    sendMessage(sender, success ? "success" : "fail");
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
    for (SocketGem gem : plugin.getSocketGemManager().getSocketGems()) {
      ExportEntry exportGem = new ExportEntry();
      exportGem.setTitle("Socket Gem");
      exportGem.setName("|lime|" + gem.getName());
      exportGem.setStrippedName(ChatColor.stripColor(PaletteUtil.color(exportGem.getName())));
      List<String> lore = new ArrayList<>(gem.getLore());
      lore.removeIf(str -> str.contains("Ս") || str.contains("tag_unique"));
      exportGem.setDescription(lore);
      exportGem.setGroupNames(buildItemTags(gem.getItemGroups()));
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
    }

    List<ExportEntry> exportTomes = new ArrayList<>();
    for (EnchantmentTome tome : plugin.getEnchantTomeManager().getEnchantmentTomes()) {
      if (tome.getWeight() == 0) {
        continue;
      }
      ExportEntry exportTome = new ExportEntry();
      exportTome.setTitle("Enchantment Tome");
      exportTome.setName("|blue|" + tome.getName());
      exportTome.setStrippedName(ChatColor.stripColor(PaletteUtil.color(exportTome.getName())));
      List<String> lore = new ArrayList<>(EnchantmentTome.UNCOLORED_TOME_DESC);
      lore.add(tome.getDescription());
      lore.removeIf(str -> str.contains("Ս") || str.contains("tag_upgrade"));
      exportTome.setDescription(lore);
      exportTome.setGroupNames(buildItemTags(tome.getItemGroups()));
      exportTome.getGroupNames().add("Upgrade");
      exportTome.getGroupNames().add("Enchanting");
      exportTome.setRarity(
          tome.getWeight() > 500 ? "Common" :
              tome.getWeight() > 200 ? "Uncommon" : "Rare");
      exportTomes.add(exportTome);
    }

    List<ExportEntry> exportUniques = new ArrayList<>();
    for (CustomItem customItem : plugin.getCustomItemManager().getCustomItems()) {
      if (customItem.getWeight() > 10000 || !customItem.isExport()) {
        continue;
      }
      ExportEntry exportUnique = new ExportEntry();
      exportUnique.setTitle("Unique Item");
      exportUnique.setName(customItem.getDisplayName());
      exportUnique.setStrippedName(ChatColor.stripColor(PaletteUtil.color(exportUnique.getName())));
      List<String> lore = new ArrayList<>(customItem.getLore());
      lore.removeIf(str -> str.contains("\uD86D\uDFEA") || str.contains("tag_unique"));
      exportUnique.setDescription(lore);
      exportUnique.setRarity(
          customItem.getWeight() > 1000 ? "Common" :
              customItem.getWeight() > 100 ? "Uncommon" :
                  customItem.getWeight() > 10 ? "Rare" : "Epic");
      Tier t = plugin.getItemGroupManager().getTierFromStack(customItem.toItemStack(1));
      if (t == null) {
        exportUnique.setGroupNames(List.of("Misc"));
      } else {
        exportUnique.setGroupNames(buildItemTags(t.getItemGroups()));
      }
      exportUniques.add(exportUnique);
    }

    List<ExportEntry> exportScrolls = new ArrayList<>();
    for (UpgradeScroll scroll : plugin.getScrollManager().getScrolls()) {
      ExportEntry exportScroll = new ExportEntry();
      exportScroll.setTitle("Upgrade Scroll");
      exportScroll.setName("|green|" + scroll.getPrefix() + " Upgrade Scroll");
      exportScroll.setStrippedName(ChatColor.stripColor(PaletteUtil.color(exportScroll.getName())));
      List<String> lore = new ArrayList<>(LootPlugin.staticAbuse.get(scroll.getId()));
      lore.removeIf(str -> str.contains("Ս") || str.contains("tag_upgrade"));
      if (StringUtils.isBlank(lore.get(0))) {
        lore.remove(0);
      }
      exportScroll.setDescription(lore);
      exportScroll.setRarity(
          scroll.getWeight() > 1000 ? "Common" :
              scroll.getWeight() > 250 ? "Uncommon" :
                  scroll.getWeight() > 50 ? "Rare" : "Epic");
      exportScroll.setGroupNames(List.of("Upgrade", "Enchanting"));
      exportScrolls.add(exportScroll);
    }

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
  }

  private static List<String> buildItemTags(Collection<ItemGroup> groups) {
    Set<String> tags = new HashSet<>();
    for (ItemGroup group : groups) {
      if (group.isInverse()) {
        continue;
      }
      tags.add(group.getName());
    }
    return new ArrayList<>(tags);
  }

  @Subcommand("reward")
  @CommandCompletion("@players @range:1-100 @range:1-100")
  @CommandPermission("loot.reward")
  public void memeCommand(CommandSender sender, OnlinePlayer player, int minLevel, int maxLevel,
      String rarity) {
    Tier t = DropUtil.getTier(player.getPlayer());
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

  @Subcommand("rgb")
  @CommandPermission("loot.rgb")
  public void memeCommand(Player player, @Default("0") int number) {
    if (number > 0) {
      Color color = Color.fromRGB(number);
      MessageUtils.sendMessage(player,
          "r:" + color.getRed() + " g:" + color.getGreen() + " b:" + color.getBlue());
      return;
    }
    ItemStack stack = player.getEquipment().getItemInMainHand();
    if (stack.getItemMeta() instanceof LeatherArmorMeta) {
      Color color = ((LeatherArmorMeta) stack.getItemMeta()).getColor();
      MessageUtils.sendMessage(player,
          "r:" + color.getRed() + " g:" + color.getGreen() + " b:" + color.getBlue());
    }
  }

  @Subcommand("give")
  @CommandPermission("loot.give")
  public class GiveCommand extends BaseCommand {

    @Subcommand("essence")
    @CommandCompletion("@players @tiers level name")
    public void giveEssence(CommandSender sender, OnlinePlayer player, String tier,
        @Default("1") int level, String stat) {

      String typeString;
      if (tier.equalsIgnoreCase("ANY")) {
        typeString = "慏";
      } else {
        Tier t = tier.equalsIgnoreCase("random") ? null : plugin.getTierManager().getTier(tier);
        if (t == null) {
          sendMessage(sender, "Invalid tier...");
          return;
        }
        typeString = t.getName();
      }
      ItemStack itemEss = MaterialUtil.createEssence(typeString, level, stat);
      player.getPlayer().getInventory().addItem(itemEss);
      sendMessage(sender, "Success!");
    }

    @Subcommand("tier")
    @CommandCompletion("@players @tiers @rarities amount level distorted")
    public void giveTier(CommandSender sender, OnlinePlayer player, String tier, String rarity,
        @Default("1") int amount, @Default("-1") int level, @Default("false") boolean distorted) {
      Tier t = tier.equalsIgnoreCase("random") ?
          null : plugin.getTierManager().getTier(tier);
      if (t == null) {
        t = plugin.getTierManager().getRandomTier();
      }
      ItemRarity r = rarity.equalsIgnoreCase("random") ?
          null : plugin.getRarityManager().getRarity(rarity);
      if (r == null) {
        r = plugin.getRarityManager().getRandomRarity(1, -1);
      }
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(
            plugin.getNewItemBuilder().withItemGenerationReason(ItemGenerationReason.COMMAND)
                .withTier(t)
                .withRarity(r)
                .withLevel(level == -1 ? 1 + LootPlugin.RNG.nextInt(100) : level)
                .withDistortion(distorted)
                .withCreator(player.getPlayer())
                .build()
                .getStack());
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
    public void giveGem(CommandSender sender, OnlinePlayer player, String id,
        @Default("1") int amount) {
      if (id.equalsIgnoreCase("random")) {
        for (int i = 0; i < amount; i++) {
          giveItem(player.getPlayer(),
              plugin.getSocketGemManager().getRandomSocketGem(true).toItemStack(1));
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
    @CommandCompletion("@players @uniques @range:1-100")
    public void giveUnique(CommandSender sender, OnlinePlayer player, String id,
        @Default("1") int amount) {
      if (id.equalsIgnoreCase("random")) {
        for (int i = 0; i < amount; i++) {
          giveItem(player.getPlayer(),
              plugin.getCustomItemManager().getRandomCustomItem(true).toItemStack(1));
        }
        sendMessage(sender,
            plugin.getSettings().getString("language.commands.spawn.custom-success", ""),
            new String[][]{{"%amount%", amount + ""}});
        return;
      }
      CustomItem ci = plugin.getCustomItemManager().getCustomItem(id);
      if (ci == null) {
        sendMessage(sender,
            plugin.getSettings().getString("language.commands.spawn.custom-failure", ""));
        return;
      }
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(ci.toItemStack(1));
      }
      sendMessage(sender,
          plugin.getSettings().getString("language.commands.spawn.custom-success", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("unique-equip")
    @CommandCompletion("@players @uniques")
    public void giveUnique(CommandSender sender, OnlinePlayer player, String id, String dSlot) {
      CustomItem ci = plugin.getCustomItemManager().getCustomItem(id);
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
      ItemStack stack = ci.toItemStack(1);
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
          player.getPlayer().getInventory().addItem(plugin.getScrollManager()
              .buildItemStack(plugin.getScrollManager().getRandomScroll()));
        }
        sendMessage(sender, plugin.getSettings()
                .getString("language.commands.spawn.upgrade-scroll", ""),
            new String[][]{{"%amount%", amount + ""}});
        return;
      }
      UpgradeScroll scroll = plugin.getScrollManager().getScroll(id);
      if (scroll == null) {
        sendMessage(sender, plugin.getSettings()
            .getString("language.commands.spawn.other-failure", ""));
        return;
      }
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(plugin.getScrollManager().buildItemStack(scroll));
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
      sendMessage(sender, plugin.getSettings().getString("language.commands.spawn.socket-extender", ""), new String[][]{{"%amount%", amount + ""}});
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

  @Subcommand("materials")
  @CommandCompletion("@players @range:1-100 @range:1-100")
  @CommandPermission("loot.give")
  public void materials(CommandSender sender, Player target, int itemLevel, int quality) {

    quality = Math.min(5, Math.max(quality, 1));

    for (Material m : plugin.getCraftMaterialManager().getCraftMaterials().keySet()) {
      ItemStack itemStack = MaterialUtil.buildMaterial(m,
          plugin.getCraftMaterialManager().getCraftMaterials().get(m), itemLevel, quality);
      target.getInventory().addItem(itemStack);
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
    gemsmasherMenu.setData(target.getPlayer(), null, null);
    gemsmasherMenu.open(target.getPlayer());
  }

  // TODO: handle this with a real menu
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
