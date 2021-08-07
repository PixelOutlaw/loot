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
import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandCompletion;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandPermission;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Default;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import com.tealcube.minecraft.bukkit.shade.acf.bukkit.contexts.OnlinePlayer;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.items.CustomItem;
import info.faceland.loot.api.items.ItemGenerationReason;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.enchantments.EnchantmentTome;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.items.prefabs.TinkerersGear;
import info.faceland.loot.menu.pawn.PawnMenu;
import info.faceland.loot.menu.upgrade.EnchantMenu;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.tier.Tier;
import info.faceland.loot.utils.DropUtil;
import info.faceland.loot.utils.InventoryUtil;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("loot")
public class LootCommand extends BaseCommand {

  private final LootPlugin plugin;
  private final Random random;
  private String awardFormat;
  private String awardFormatSelf;

  public LootCommand(LootPlugin plugin) {
    this.plugin = plugin;
    this.random = new Random(System.currentTimeMillis());
    awardFormat = plugin.getSettings().getString("language.broadcast.reward-item", "");
    awardFormatSelf = plugin.getSettings().getString("language.broadcast.reward-item-self", "");
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

    int level = minLevel + (int) ((maxLevel - minLevel) * Math.random());

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

    @Subcommand("tier")
    @CommandCompletion("@players @tiers @rarities @range:1-100")
    public void giveTier(CommandSender sender, OnlinePlayer player, String tier, String rarity, @Default("1") int amount, @Default("-1") int level) {
      Tier t = tier.equalsIgnoreCase("random") ? null : plugin.getTierManager().getTier(tier);
      ItemRarity r = rarity.equalsIgnoreCase("random") ? null : plugin.getRarityManager().getRarity(rarity);
      for (int i = 0; i < amount; i++) {
        player.getPlayer().getInventory().addItem(
            plugin.getNewItemBuilder().withItemGenerationReason(ItemGenerationReason.COMMAND)
                .withTier(t == null ? plugin.getTierManager().getRandomTier() : t)
                .withRarity(r == null ? plugin.getRarityManager().getRandomRarity() : r)
                .withLevel(level == -1 ? 1 + random.nextInt(100) : level)
                .build()
                .getStack());
      }
      sendMessage(sender,
          plugin.getSettings().getString("language.commands.spawn.other-success", ""),
          new String[][]{{"%amount%", amount + ""}});
    }

    @Subcommand("tome")
    @CommandCompletion("@players @tomes @range:1-100")
    public void giveTome(CommandSender sender, OnlinePlayer player, String id, @Default("1") int amount) {
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
    public void giveUnique(CommandSender sender, OnlinePlayer player, String id, @Default("1") int amount) {
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

    @Subcommand("scroll")
    @CommandCompletion("@players @scrolls @range:1-100")
    public void giveScroll(CommandSender sender, OnlinePlayer player, String id, @Default("1") int amount) {
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
        player.getPlayer().getInventory().addItem(new SocketExtender());
      }
      sendMessage(sender,
          plugin.getSettings().getString("language.commands.spawn.socket-extender", ""),
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

  @Subcommand("materials")
  @CommandCompletion("@players @range:1-100 @range:1-100")
  @CommandPermission("loot.give")
  public void materials(CommandSender sender, Player target, int itemLevel, int quality) {

    quality = Math.min(5, Math.max(quality, 1));

    for (Material m : plugin.getCraftMatManager().getCraftMaterials().keySet()) {
      ItemStack itemStack = MaterialUtil.buildMaterial(m,
          plugin.getCraftMatManager().getCraftMaterials().get(m), itemLevel, quality);
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

  @Subcommand("pawn")
  @CommandCompletion("@players")
  @CommandPermission("loot.pawn")
  public void pawnSubcommand(CommandSender sender, OnlinePlayer target) {
    if (target == null || !target.getPlayer().isValid()) {
      return;
    }
    PawnMenu menu = PawnMenu.getPawnMenu(plugin);
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
    List<String> lore = ItemStackExtensionsKt.getLore(heldItem);
    lore.set(3, ChatColor.WHITE + ChatColor.stripColor(TextUtils.color(newLore)));
    ItemStackExtensionsKt.setLore(heldItem, lore);
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
