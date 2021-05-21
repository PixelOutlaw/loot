package info.faceland.loot.listeners;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;
import static info.faceland.loot.utils.MaterialUtil.buildEssence;
import static info.faceland.loot.utils.MaterialUtil.getDigit;
import static info.faceland.loot.utils.MaterialUtil.getLevelRequirement;
import static info.faceland.loot.utils.MaterialUtil.getToolLevel;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.events.LootDeconstructEvent;
import info.faceland.loot.events.LootDeconstructEvent.DeconstructType;
import info.faceland.loot.items.prefabs.ShardOfFailure;
import info.faceland.loot.math.LootRandom;
import info.faceland.loot.tier.Tier;
import info.faceland.loot.utils.MaterialUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

public class DeconstructListener implements Listener {

  private final LootPlugin plugin;
  private final LootRandom random;

  public DeconstructListener(LootPlugin plugin) {
    this.plugin = plugin;
    this.random = new LootRandom();
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onInventoryClick(InventoryClickEvent event) {
    if (event.isCancelled() || !(event.getClickedInventory() instanceof PlayerInventory)) {
      return;
    }
    if (event.getClick() != ClickType.RIGHT) {
      return;
    }
    if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) {
      return;
    }
    if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR ||
        !(event.getWhoClicked() instanceof Player)) {
      return;
    }
    Player player = (Player) event.getWhoClicked();
    ItemStack currentItem = new ItemStack(event.getCurrentItem());
    ItemStack cursor = new ItemStack(event.getCursor());

    String curName = ItemStackExtensionsKt.getDisplayName(cursor);
    if (StringUtils.isBlank(curName)) {
      return;
    }

    DeconstructType type;
    if (curName.endsWith("Craftsman's Tools")) {
      type = DeconstructType.CRAFTING;
    } else if (curName.endsWith("Enchanter's Arcana")) {
      type = DeconstructType.ENCHANTING;
    } else {
      return;
    }

    int itemLevel = getLevelRequirement(currentItem);
    if (itemLevel == -1) {
      MessageUtils.sendMessage(
          player, plugin.getSettings().getString("language.craft.no-level", ""));
      return;
    }

    // NO GOING BACK NOW BOYOS
    event.setCancelled(true);

    if (event.getCurrentItem().getAmount() > 1) {
      MessageUtils.sendMessage(
          player, plugin.getSettings().getString("language.craft.big-stack", ""));
      return;
    }

    if (event.getCursor().getAmount() > 1) {
      MessageUtils.sendMessage(
          player, plugin.getSettings().getString("language.craft.big-cursor", ""));
      return;
    }

    LootDeconstructEvent deconstructEvent = new LootDeconstructEvent();
    deconstructEvent.setDeconstructType(type);
    deconstructEvent.setCursorItem(cursor);
    deconstructEvent.setTargetItem(currentItem);
    deconstructEvent.setPlayer(player);
    Bukkit.getPluginManager().callEvent(deconstructEvent);

    if (deconstructEvent.isCancelled()) {
      return;
    }
    event.setCurrentItem(deconstructEvent.getTargetItem());
    event.setCursor(deconstructEvent.getCursorItem());
  }

  @EventHandler
  public void onDeconstruct(LootDeconstructEvent event) {
    if (event.getDeconstructType() == DeconstructType.CRAFTING) {
      doCraftDeconstruct(event);
    } else if (event.getDeconstructType() == DeconstructType.ENCHANTING) {
      doEnchantDeconstruct(event);
    }
  }

  private void doCraftDeconstruct(LootDeconstructEvent event) {
    Player player = event.getPlayer();
    ItemStack targetItem = event.getTargetItem();
    ItemStack cursorItem = event.getCursorItem();

    int itemLevel = getLevelRequirement(targetItem);

    int craftingLevel = PlayerDataUtil.getLifeSkillLevel(player, LifeSkillType.CRAFTING);
    double effectiveCraftLevel = PlayerDataUtil.getEffectiveLifeSkill(player, LifeSkillType.CRAFTING, true);

    int toolQuality = 1;
    if (cursorItem.hasItemMeta()) {
      toolQuality = (int) ItemStackExtensionsKt.getLore(cursorItem).get(1).chars().filter(ch -> ch == '✪').count();
    }

    double levelAdvantage = getLevelAdvantage(craftingLevel, itemLevel);
    if (levelAdvantage < 0) {
      sendMessage(player, plugin.getSettings().getString("language.craft.low-level", ""));
      return;
    }
    if (craftingLevel < getToolLevel(cursorItem)) {
      sendMessage(player, plugin.getSettings().getString("language.craft.low-level-tool", ""));
      return;
    }

    double effectiveLevelAdvantage = getLevelAdvantage((int) effectiveCraftLevel, itemLevel);
    List<String> lore = ItemStackExtensionsKt.getLore(targetItem);
    List<String> possibleStats = new ArrayList<>();
    for (String str : lore) {
      if (!ChatColor.stripColor(str).startsWith("+")) {
        continue;
      }
      net.md_5.bungee.api.ChatColor color = getHexFromString(str);
      if (color != null) {
        if (isValidStealColor(color.getColor())) {
          possibleStats.add(str);
        }
        continue;
      }
      if (str.startsWith(ChatColor.GREEN + "") || str.startsWith(ChatColor.YELLOW + "")) {
        possibleStats.add(str);
      }
    }

    ItemStack materialDetectionItem = targetItem.clone();
    materialDetectionItem.setDurability((short) 0);
    Material material = plugin.getCraftMatManager().getMaterial(materialDetectionItem);
    if (material == null) {
      sendMessage(player, plugin.getSettings().getString("language.craft.no-materials", ""));
      return;
    }
    float quality = 1;
    while (random.nextDouble() <= plugin.getSettings().getDouble("config.drops.material-quality-up", 0.1D)
        && quality < 5) {
      quality++;
    }
    quality += Math.max(0, effectiveCraftLevel - itemLevel) / 100;
    quality += (toolQuality - 1) * 0.1;
    quality = Math.floor(quality) + Math.random() <= quality % 1 ? 1 : 0;

    quality = Math.max(MaterialUtil.getItemRarity(targetItem) - 1, quality);
    quality = Math.min(5, Math.max(1, quality));

    ItemStack craftMaterial = MaterialUtil.buildMaterial(material, plugin.getCraftMatManager()
        .getCraftMaterials().get(material), itemLevel, (int) quality);

    player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 0.8F);
    event.setTargetItem(null);
    if (player.getInventory().firstEmpty() != -1) {
      player.getInventory().addItem(craftMaterial);
    } else {
      Item item = player.getWorld().dropItem(player.getLocation(), craftMaterial);
      item.setOwner(player.getUniqueId());
    }

    if (possibleStats.size() > 0) {
      player.playSound(player.getEyeLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4F, 2F);
      Tier tier = MaterialUtil.getTierFromStack(targetItem);

      ItemStack essence = buildEssence(tier, itemLevel, effectiveLevelAdvantage, toolQuality, possibleStats,
          player.hasPotionEffect(PotionEffectType.LUCK));

      if (player.getInventory().firstEmpty() != -1) {
        player.getInventory().addItem(essence);
      } else {
        Item item = player.getWorld().dropItem(player.getLocation(), essence);
        item.setOwner(player.getUniqueId());
      }
    }

    List<String> toolLore = ItemStackExtensionsKt.getLore(cursorItem);
    if (ChatColor.stripColor(toolLore.get(toolLore.size() - 1)).startsWith("Remaining Uses: ")) {
      int uses = getDigit(ChatColor.stripColor(toolLore.get(toolLore.size() - 1)));
      if (uses == 1) {
        sendMessage(player, plugin.getSettings().getString("language.craft.tool-decay", ""));
        event.setCursorItem(null);
      } else {
        uses--;
        toolLore.set(toolLore.size() - 1, ChatColor.WHITE + "Remaining Uses: " + uses);
        ItemStackExtensionsKt.setLore(cursorItem, toolLore);
        event.setCursorItem(cursorItem);
      }
    }
    double exp = 5 + itemLevel * 1.1;
    plugin.getStrifePlugin().getSkillExperienceManager()
        .addExperience(player, LifeSkillType.CRAFTING, exp, false, false);
  }

  private void doEnchantDeconstruct(LootDeconstructEvent event) {
    Player player = event.getPlayer();
    ItemStack targetItem = event.getTargetItem();

    double itemPlus = MaterialUtil.getUpgradeLevel(targetItem);
    if (itemPlus <= 3) {
      sendMessage(player, plugin.getSettings().getString("language.enchant.too-low-to-deconstruct", ""));
      event.setCancelled(true);
      return;
    }

    event.setTargetItem(null);
    player.playSound(player.getEyeLocation(), Sound.ENTITY_ITEM_BREAK, 1F, 1.5F);
    player.playSound(player.getEyeLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1F, 1F);

    double shards = itemPlus + Math.pow(itemPlus, 1 + Math.random() * (itemPlus / 15 * 0.7));
    ItemStack shardOfFailure = ShardOfFailure.build(player.getName());
    shardOfFailure.setAmount((int) shards);
    player.getInventory().addItem(shardOfFailure);
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.ENCHANTING, 20 + Math.pow(itemPlus, 2.2), false, false);
  }

  public static int getLevelAdvantage(int craftLevel, int itemLevel) {
    int lvlReq = 20 + (int) Math.floor((double) craftLevel / 10) * 14;
    return lvlReq - itemLevel;
  }

  private boolean isValidStealColor(Color color) {
    float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
    return hsb[0] >= 0.14 && hsb[0] <= 0.34 && hsb[1] >= 0.7 && hsb[1] <= 0.72 && hsb[2] >= 0.99 && hsb[2] <= 1.01;
  }

  private final Pattern hexPattern = Pattern.compile("§x(§[A-Fa-f0-9]){6}");

  public net.md_5.bungee.api.ChatColor getHexFromString(String message) {
    Matcher matcher = hexPattern.matcher(message);
    if (matcher.find()) {
      String str = "#" + matcher.group().replace("§x", "").replace("§", "");
      Bukkit.getLogger().warning(str);
      return net.md_5.bungee.api.ChatColor.of(str);
    }
    return null;
  }
}