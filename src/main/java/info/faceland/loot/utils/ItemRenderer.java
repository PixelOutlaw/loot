package info.faceland.loot.utils;

import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.BLUE;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.CRIMSON;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.CYAN;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.GRAY;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.GREEN;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.GROW;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.LIGHT_GRAY;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.LIGHT_GREEN;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.ORANGE;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.RED;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.TRUE_WHITE;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.WHITE;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.YELLOW;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.color;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.i;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.noShadow;
import static info.faceland.loot.LootPlugin.ITEM_VERSION;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;
import static info.faceland.loot.utils.ItemStyleUtil.CORRUPTION;
import static info.faceland.loot.utils.ItemStyleUtil.ENCHANT_CHARS;
import static info.faceland.loot.utils.ItemStyleUtil.HALFWAY;
import static info.faceland.loot.utils.ItemStyleUtil.JUMP_BACK;
import static info.faceland.loot.utils.ItemStyleUtil.UNTRADEABLE_NORMAL;
import static info.faceland.loot.utils.ItemStyleUtil.UNTRADEABLE_SMALL;
import static info.faceland.loot.utils.ItemStyleUtil.centerLargeText;
import static info.faceland.loot.utils.ItemStyleUtil.centerSmallText;
import static info.faceland.loot.utils.ItemStyleUtil.countSubtitleSize;
import static info.faceland.loot.utils.ItemStyleUtil.doPrefix;
import static info.faceland.loot.utils.ItemStyleUtil.doSuffix;

import com.google.common.collect.Multimap;
import com.sentropic.guiapi.gui.GUI;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.StatResponse;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.StoredStat;
import info.faceland.loot.events.ExternalRenderEvent;
import info.faceland.loot.items.CosmeticHead;
import info.faceland.loot.items.CraftMaterial;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.LevelModItem;
import info.faceland.loot.items.NewCustomItem;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.SoulGem;
import info.faceland.loot.items.Tier;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.AspectOfPerfection;
import info.faceland.loot.items.prefabs.AwakeningStone;
import info.faceland.loot.items.prefabs.DistortionCell;
import info.faceland.loot.items.prefabs.DivineHammer;
import info.faceland.loot.items.prefabs.ManaCrystal;
import info.faceland.loot.items.prefabs.MissingStat;
import info.faceland.loot.items.prefabs.NormalItem;
import info.faceland.loot.items.prefabs.OrbOfManyFaces;
import info.faceland.loot.items.prefabs.OrbOfOscillation;
import info.faceland.loot.items.prefabs.PerfectSphere;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.items.prefabs.TinkerersGear;
import info.faceland.loot.items.prefabs.TooltipPaint;
import info.faceland.loot.items.prefabs.TraceOfTrash;
import info.faceland.loot.items.prefabs.UnidEnchant;
import info.faceland.loot.items.prefabs.WarpedTongs;
import info.faceland.loot.menu.loreglass.LoreglassMenu;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import land.face.foods.FoodsPlugin;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.champion.SkillRank;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import me.elsiff.morefish.pojo.CustomFish;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemRenderer {

  private static Multimap<Attribute, AttributeModifier> BS_MAP = excreteBullshit();
  private static TextComponent FIXED_WIDTH_BUFFER = Component.text("\uF82C\uF829\uF824");
  private static TextComponent ENCHANT_THINGY = noShadow(color(Component.text("\uF806严\uF80A"), TRUE_WHITE));
  //private static NamespacedKey aa = NamespacedKey.minecraft()new NamespacedKey(this, "loot.item_data");

  public static void renderItem(ItemStack stack, boolean force) {
    //System.out.println("ITEM RENDER START");
    String type = PersistHelper.getItemType(stack);
    if (type.isBlank()) {
      //System.out.println("- No type... attempting to absorb item...");
      ConversionUtil.attemptToAbsorbItem(stack);
      return;
    }
    boolean versionChange = PersistHelper.getItemVersion(stack) < ITEM_VERSION;
    if (!force && !versionChange) {
      //System.out.println("- Item already on current version! Aborting!");
      return;
    }
    //System.out.println("- Proceeding with update...");
    //System.out.println("-- Type: " + type);
    switch (type) {
      case "equipment" -> renderEquipmentItem(LootPlugin.getInstance(), stack, true);
      case "unique" -> renderUniqueItem(LootPlugin.getInstance(), stack);
      case UnidEnchant.TYPE_KEY -> UnidEnchant.update(stack);
      case SocketGem.TYPE_KEY -> SocketGem.update(stack);
      case EnchantmentTome.TYPE_KEY -> EnchantmentTome.update(stack);
      case UpgradeScroll.TYPE_KEY -> UpgradeScroll.update(stack);
      case CraftMaterial.TYPE_KEY -> CraftMaterial.update(stack);
      case PurifyingScroll.TYPE_KEY -> PurifyingScroll.update(stack);
      case ManaCrystal.TYPE_KEY -> ManaCrystal.update(stack);
      case "pet" -> { /* do nothing, for now */ }
      case "food" -> FoodsPlugin.getInstance().getFoodsManager().updateItem(stack);
      case "fish" -> CustomFish.update(stack);
      case SocketExtender.TYPE_KEY -> SocketExtender.update(stack);
      case "essence" -> renderEssence(stack, null);
      case ArcaneEnhancer.TYPE_KEY -> ArcaneEnhancer.update(stack);
      case TinkerersGear.TYPE_KEY -> TinkerersGear.update(stack);
      case CosmeticHead.TYPE_KEY -> CosmeticHead.update(stack);
      case OrbOfManyFaces.TYPE_KEY -> OrbOfManyFaces.update(stack);
      case PerfectSphere.TYPE_KEY -> PerfectSphere.update(stack);
      case DivineHammer.TYPE_KEY -> DivineHammer.update(stack);
      case WarpedTongs.TYPE_KEY -> WarpedTongs.update(stack);
      case NormalItem.TYPE_KEY -> NormalItem.update(stack);
      case SoulGem.TYPE_KEY -> SoulGem.update(stack);
      case LevelModItem.TYPE_KEY -> LevelModItem.update(stack);
      case TooltipPaint.TYPE_KEY -> TooltipPaint.update(stack);
      case AwakeningStone.TYPE_KEY -> AwakeningStone.update(stack);
      case AspectOfPerfection.TYPE_KEY -> AspectOfPerfection.update(stack);
      case TraceOfTrash.TYPE_KEY -> TraceOfTrash.update(stack);
      case OrbOfOscillation.TYPE_KEY -> OrbOfOscillation.update(stack);
      case DistortionCell.TYPE_KEY -> DistortionCell.update(stack);
      case MissingStat.TYPE_KEY -> MissingStat.update(stack);
      default -> ConversionUtil.attemptToAbsorbItem(stack);
    }
    ExternalRenderEvent craftEvent = new ExternalRenderEvent(stack);
    Bukkit.getPluginManager().callEvent(craftEvent);
    applyCommonFlags(stack);
  }

  private static Multimap<Attribute, AttributeModifier> excreteBullshit() {
    ItemStack stack = new ItemStack(Material.WOODEN_SWORD);
    stack.editMeta(m -> {
      m.addAttributeModifier(Attribute.ATTACK_DAMAGE,
          new AttributeModifier(new NamespacedKey(LootPlugin.getInstance(), "ATTACK_DAMAGE"), 0.002,
              AttributeModifier.Operation.ADD_NUMBER));
      m.addAttributeModifier(Attribute.ARMOR_TOUGHNESS,
          new AttributeModifier(new NamespacedKey(LootPlugin.getInstance(), "ARMOR_TOUGHNESS"), 0.002,
              AttributeModifier.Operation.ADD_NUMBER));
    });
    return stack.getItemMeta().getAttributeModifiers();
  }

  private static void applyBullshit(ItemStack stack) {
    stack.editMeta(itemMeta -> {
      itemMeta.removeItemFlags(stack.getItemFlags().toArray(new ItemFlag[0]));
      itemMeta.setAttributeModifiers(BS_MAP);
      itemMeta.addItemFlags(
          ItemFlag.HIDE_ITEM_SPECIFICS,
          ItemFlag.HIDE_ARMOR_TRIM,
          ItemFlag.HIDE_ATTRIBUTES,
          ItemFlag.HIDE_DESTROYS,
          ItemFlag.HIDE_DYE,
          ItemFlag.HIDE_ENCHANTS,
          ItemFlag.HIDE_UNBREAKABLE,
          ItemFlag.HIDE_ADDITIONAL_TOOLTIP
      );
      //itemMeta.setTooltipStyle();
    });
  }

  public static void applyCommonFlags(ItemStack stack) {
    applyBullshit(stack);
  }

  private static void renderEquipmentItem(LootPlugin plugin, ItemStack stack, boolean versionChange) {
    String extraContext = PersistHelper.getItemId(stack);
    if (extraContext.isBlank()) {
      EquipmentData data = PersistHelper.getEquipmentData(stack);
      if (data == null) {
        return;
      }
      renderEquipmentItem(plugin, stack, data, null);
      return;
    }
    NewCustomItem ci = plugin.getCustomItemManager().getUnique(extraContext);
    if (ci == null) {
      //System.out.println("Item renderer found item with no custom data");
      return;
    }
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data == null) {
      //System.out.println("NO DATA");
      return;
    }
    if (versionChange) {
      PersistHelper.syncUniqueEquipment(stack, data, ci);
    }
    renderEquipmentItem(plugin, stack, data, null);
  }

  public static void renderUniqueItem(LootPlugin plugin, ItemStack stack) {
    NewCustomItem customItem = plugin.getCustomItemManager().getUnique(PersistHelper.getItemId(stack));
    if (customItem == null) {
      //System.out.println("unique not found aborting");
      return;
    }
    int amount = stack.getAmount();
    replace(stack, customItem.item(), customItem.isLarge());
    stack.setAmount(amount);
  }

  public static void renderEssence(ItemStack stack, StoredStat stat) {
    renderEssence(stack, stat, null);
  }

  public static void renderEssence(ItemStack stack, StoredStat stat, SkillRank rank) {
    if (stat == null) {
      stat = PersistHelper.getEssenceStat(stack);
      if (stat == null) {
        return;
      }
    }
    Tier tier = Tier.get(stat.getOriginTier());
    StatResponse sr = buildStatResponse(
        LootPlugin.getInstance(),
        stat.getLevel(),
        0,
        0,
        stat,
        0
    );

    TextComponent viewThing = SkillRank.check(rank, stat.getLevel()) ?
        LoreglassMenu.getPercentText((float) stat.getStatRoll()) : Component.empty();

    TextComponent statText;
    if (sr == null) {
      statText = centerSmallText(color(Component.text("Missing stat :("), CRIMSON));
    } else if (stat.isDistorted()) {
      statText = centerSmallText(sr.getStatString(), false).append(ItemStyleUtil.getCorruptAddon()).append(viewThing).append(JUMP_BACK);
    } else {
      statText = centerSmallText(sr.getStatString(), false).append(viewThing).append(JUMP_BACK);
    }

    int modelId = 100000;
    modelId += 1000 * tier.getEssenceId();
    modelId += Math.min(100, stat.getEssenceLevel(stat.getLevel()));

    ItemMeta meta = stack.getItemMeta();
    meta.displayName((color(Component.text("Item Essence (" + tier.getPlainName() + ")"), CYAN)));
    meta.lore(List.of(
        noShadow(Component.text("\uF808万\uF808\uF801")),
        centerSmallText(color(Component.text("Item Type: "), WHITE)
            .append(color(Component.text(tier.getPlainName()), CYAN))),
        centerSmallText(color(Component.text("Essence Level: "), WHITE)
            .append(color(Component.text(stat.getEssenceLevel(stat.getLevel())), LIGHT_GREEN))),
        Component.empty(),
        statText,
        Component.empty(),
        centerSmallText(color(Component.text("Craft this together with"), LIGHT_GRAY)),
        centerSmallText(color(Component.text("an unfinished item with"), LIGHT_GRAY)),
        centerSmallText(color(Component.text("an empty stat slot!"), LIGHT_GRAY))
    ));
    meta.setCustomModelData(modelId);
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    stack.setItemMeta(meta);
  }

  public static void renderEquipmentItem(LootPlugin plugin, ItemStack stack, EquipmentData data, SkillRank viewRank) {
    List<Component> lore = new ArrayList<>();

    NewCustomItem unique = LootPlugin.getInstance().getCustomItemManager().getUnique(PersistHelper.getItemId(stack));
    ItemRarity r = data.getItemRarity();
    String plate, rarityName;
    Tier tier;
    FaceColor color;
    NamespacedKey tooltip;
    if (unique == null) {
      tooltip = r.getTooltip();
      if (StringUtils.isNotBlank(data.getString("craftedBy"))) {
        color = FaceColor.YELLOW;
        plate = r.getTooltipPlateCrafted();
      } else {
        color = r.getColor();
        plate = r.getTooltipPlate();
      }
      tier = data.getTier();
      rarityName = r.getPlainName();
    } else {
      color = unique.getColor();
      plate = StringUtils.isBlank(unique.getPlateString()) ? "乞" : unique.getPlateString();
      tier = unique.getTier();
      tooltip = unique.getTooltip();
      rarityName = "Unique";
    }

    stack.setType(tier.getMaterial());
    if (tier.getMaterial() == Material.BOW) {
      stack.addUnsafeEnchantment(Enchantment.INFINITY, 11);
    }
    if (!tier.getDestroyKeys().isEmpty()) {
      stack.editMeta(im -> im.setDestroyableKeys(new HashSet<>(tier.getDestroyKeys())));
    }
    stack.editMeta(m -> {
      m.setMaxStackSize(1);
      ((Damageable) m).setMaxDamage(1000);
      m.setTooltipStyle(tooltip);
      //m.get
    });

    boolean distorted = data.getBoolean("distorted");

    String name = data.getString("name", "Item");
    String plus = "";
    int scaledPlus = 0;
    if (data.getUpgradeLevel() > 0) {
      scaledPlus = data.getUpgradeLevel() * tier.getUpgradeScale();
      scaledPlus = Math.min(15, scaledPlus);
      plus = ItemStyleUtil.upgradeNumbers.get(scaledPlus - 1);
    }
    // MAYBE CHECK FOR SMALL VS LARGE? Should always be large though
    lore.add(noShadow(Component.text(plate + plus)));
    lore.add(ItemStyleUtil.nameLoreLine(name, data.isNameEdited()));
    String subtitle = rarityName + " " + tier.getPlainName();

    lore.add(HALFWAY.append(Component.text(GUI.spacesOf(-countSubtitleSize(subtitle) / 2))
        .append(noShadow(color(Component.text(doSuffix(subtitle) + "\uF80C"), color)))));
    TextComponent distortion = Component.empty();
    if (distorted) {
      var start = noShadow(color(Component.text(GUI.spacesOf(-22) + CORRUPTION + GUI.spacesOf(-31)), TRUE_WHITE));
      var grow = noShadow(color(Component.text("俽" + GUI.spacesOf(-162)), GROW));
      distortion = start.append(grow);
    }
    TextComponent modified = Component.empty();
    if (data.getLevelMod() != 0) {
      modified = switch (data.getLevelMod()) {
        case -5 -> color(Component.text("↓"), RED).decoration(TextDecoration.ITALIC, false);
        case -3 -> color(Component.text("↓"), YELLOW).decoration(TextDecoration.ITALIC, false);
        case 5 -> color(Component.text("↑"), LIGHT_GREEN).decoration(TextDecoration.ITALIC, false);
        default -> Component.text("*").decoration(TextDecoration.ITALIC, false);
      };
    }
    int iLevel = data.getCustomLevel() - data.getUpgradeLevel() / 3;
    iLevel += data.getLevelMod();
    iLevel = Math.max(1, iLevel);
    if (tier.isSkillRequirement()) {
      TextComponent reqString = color(Component.text("Skill Requirement: " + iLevel), WHITE).append(modified);
      // TODO: ADD DISTORT
      lore.add(centerLargeText(reqString));
    } else {
      TextComponent reqString = color(Component.text("Level Requirement: " + iLevel), WHITE).append(modified);
      // TODO: ADD DISTORT
      lore.add(centerLargeText(reqString));
    }
    lore.add(Component.text(" "));
    int tierNum = -1;
    if (unique == null) {
      tierNum = Math.min(5, 1 + data.getCustomLevel() / 20);
      var tierString = "t" + tierNum;
      ItemUtils.setCustomData(stack, List.of(tier.getId(), tierString + "-" + r.getId().toLowerCase()));
    } else {
      ItemUtils.setCustomData(stack, List.of(tier.getId(), unique.getId()));
    }

    if (!data.getCoreStats().isEmpty()) {
      int numStats = 1;
      for (StoredStat stat : data.getCoreStats()) {
        int lvl = data.getCustomLevel() + data.getLevelMod();
        StatResponse sr = buildStatResponse(plugin, lvl, data.getUpgradeLevel(), r.getPower(), stat, numStats);
        if (sr != null && !stat.getStat().isHide()) {
          lore.add(centerLargeText(sr.getStatString()));
        }
        numStats++;
      }
    } else {
      lore.add(color(Component.text("No core stats..."), RED));
    }
    String statPrefix = "";
    if (!data.getBonusStats().isEmpty()) {
      lore.add(FIXED_WIDTH_BUFFER);
      PersistHelper.validateCraftStats(data);
      data.updateUniqueMultipliers(unique);
      Map<StoredStat, StatResponse> tempRollMap = new HashMap<>();
      for (StoredStat stat : data.getBonusStats()) {
        int lvl = data.getCustomLevel() + data.getLevelMod();
        StatResponse sr = buildStatResponse(plugin, lvl, data.getUpgradeLevel(), r.getPower(), stat, 0);
        if (sr != null && !stat.getStat().isHide()) {
          tempRollMap.put(stat, sr);
        }
      }
      data.sortBonusStats();
      if (unique == null && !data.getBonusStats().isEmpty()) {
        ItemStat stat = data.getBonusStats().getFirst().getStat();
        if (data.getBonusStats().getFirst().getStatRoll() >= 0.85 && !stat.getNamePrefixes().isEmpty()
            && !data.getBonusStats().getFirst().isEssenceSlot()) {
          statPrefix = stat.getNamePrefixes().getFirst();
        }
      }
      for (StoredStat stat : data.getBonusStats()) {
        StatResponse sr = tempRollMap.get(stat);
        TextComponent corruptStr = distorted ? Component.empty() :
            (stat.isDistorted() ? ItemStyleUtil.getCorruptAddon() : Component.empty());
        if (stat.isInverted()) {
          TextComponent viewThing = viewRank != null && SkillRank.check(viewRank, data.getCustomLevel()) ?
              LoreglassMenu.getPercentText((float) stat.getStatRoll()) : Component.empty();
          var content = color(sr.getStatString().content(sr.getStatString().content().replace("+", "-")), RED);
          lore.add(centerLargeText(content, false).append(corruptStr).append(viewThing).append(JUMP_BACK));
        } else if (stat.isCrafted()) {
          TextComponent viewThing = viewRank != null && SkillRank.check(viewRank, data.getCustomLevel()) ?
              LoreglassMenu.getPercentText((float) stat.getStatRoll()) : Component.empty();
          TextComponent statContent = color(sr.getStatString(), CYAN);
          lore.add(centerLargeText(statContent, false).append(corruptStr).append(viewThing).append(JUMP_BACK));
          if (!stat.getOriginUnique().isBlank() && viewRank != null && SkillRank.isRank(viewRank, SkillRank.EXPERT)) {
            var originUnique = plugin.getCustomItemManager().getUnique(stat.getOriginUnique());
            if (originUnique != null) {
              lore.add(centerLargeText(color(Component.text("From: " + originUnique.getItemName()), GRAY)));
            }
          }
        } else if (stat.isEssenceSlot()) {
          lore.add(centerLargeText(color(Component.text("< Empty Stat Slot >"), GRAY)));
        } else {
          TextComponent viewThing = viewRank != null && SkillRank.check(viewRank, data.getCustomLevel()) ?
              LoreglassMenu.getPercentText((float) stat.getStatRoll()) : Component.empty();
          lore.add(centerLargeText(sr.getStatString(), false).append(corruptStr).append(viewThing).append(JUMP_BACK));
        }
      }
      if (unique != null) {
        lore.addAll(unique.getJokeStats());
      }
    }
    if (data.hasEnchantmentSlot()) {
      if (data.getEnchantment() == null) {
        lore.add(FIXED_WIDTH_BUFFER);
        lore.add(ENCHANT_THINGY);
        lore.add(Component.empty());
      } else {
        boolean enhanced = data.isEnhanced();
        var barComp = Component.text("\uF805" + selectEnchantChar(data) + "\uF80A");
        lore.add(color(barComp, enhanced ? RED : FaceColor.WHITE));
        StoredStat enchantmentStat = data.getEnchantment();
        StatResponse sr = plugin.getStatManager().getFinalStat(
            enchantmentStat.getStat(),
            enchantmentStat.getLevel(),
            (float) enchantmentStat.getRarityPower(),
            (float) enchantmentStat.getStatRoll(),
            data.getDoubleData().getOrDefault("enhancement", 1D).floatValue(),
            enhanced ? 1 : 0
        );
        enchantmentStat.setCachedValue(sr.getValue());
        if (SkillRank.check(viewRank, enchantmentStat.getLevel())) {
          var comp = LoreglassMenu.getPercentText((float) enchantmentStat.getStatRoll());
          comp = comp.append(color(Component.text(" (" + enchantmentStat.getLevel() + ")"), BLUE));
          lore.add(centerLargeText(sr.getStatString(), false).append(comp).append(JUMP_BACK));
        } else {
          var comp = LoreglassMenu.getPercentText((float) enchantmentStat.getStatRoll());
          lore.add(centerLargeText(sr.getStatString(), false).append(comp).append(JUMP_BACK));
        }
        lore.add(FIXED_WIDTH_BUFFER);
      }
    } else {
      lore.add(FIXED_WIDTH_BUFFER);
    }

    boolean hasTraits = renderEquipmentTraits(data, lore);
    if (hasTraits) {
      lore.add(Component.empty());
    }

    String gemPrefix = "";
    int totalGemSlots = data.getSockets().size() + data.getEmptyGemSlots() + data.getEmptyExtendSlots();
    if (totalGemSlots > 0) {
      if (!data.getSocketGems().isEmpty()) {
        int lowestPrefixWeight = 1000;
        for (String socketStr : data.getSockets()) {
          SocketGem gem = plugin.getSocketGemManager().getSocketGem(socketStr);
          if (gem == null) {
            Bukkit.getLogger().warning("[Loot] Outdated gem detected... " + socketStr);
            lore.add(centerLargeText(color(Component.text("< Outdated gem >"), RED)));
            continue;
          }
          //System.out.println("detected gem: " + gem.getId());
          if (gem.getWeight() < lowestPrefixWeight && !StringUtils.isBlank(gem.getPrefix())) {
            gemPrefix = gem.getPrefix();
            lowestPrefixWeight = (int) gem.getWeight();
          }
          for (Entry<StrifeStat, Float> entry : gem.getStats().entrySet()) {
            if (entry.getValue() > 0) {
              lore.add(centerLargeText(color(Component.text(
                  "+" + Math.round(entry.getValue()) + " " + entry.getKey().getName()), ORANGE)));
            } else {
              lore.add(centerLargeText(color(Component.text(
                  Math.round(entry.getValue()) + " " + entry.getKey().getName()), RED)));
            }
            //System.out.println("detected flat stat " + entry.getKey());
          }
          for (Entry<StrifeStat, Float> entry : gem.getPercentStats().entrySet()) {
            if (entry.getValue() > 0) {
              lore.add(centerLargeText(color(Component.text(
                  "+" + Math.round(entry.getValue()) + "% " + entry.getKey().getName()), ORANGE)));
            } else {
              lore.add(centerLargeText(color(Component.text(
                  Math.round(entry.getValue()) + "% " + entry.getKey().getName()), RED)));
            }
          }
          for (LoreAbility loreAbility : gem.getLoreAbilities()) {
            //System.out.println("detected la: " + loreAbility.getId());
            for (var s : loreAbility.getDescAsComponents()) {
              lore.add(centerLargeText(s));
            }
          }
          for (StrifeTrait strifeTrait : gem.getTraits()) {
            for (var s : strifeTrait.getDescAsComponents()) {
              lore.add(centerLargeText(s));
            }
          }
        }
      } else {
        lore.add(centerLargeText(color(Component.text("- Socket Slots -"), ORANGE)));
      }
      lore.add(ItemStyleUtil.generateGemString(totalGemSlots, data.getEmptyExtendSlots(), data.getSocketGems()));
      lore.add(Component.empty());
    }

    if (unique != null && !unique.getFlavorText().isEmpty()) {
      lore.addAll(unique.getFlavorText());
    } else if (StringUtils.isNotBlank(data.getString("craftedBy"))) {
      lore.add(centerLargeText(color(i(Component.text("Crafter: " + data.getString("craftedBy"))), GRAY)));
    }
    if (SkillRank.isRank(viewRank, SkillRank.NOVICE)) {
      if (StringUtils.isBlank(data.getString("craftedBy"))) {
        lore.add(centerLargeText(color(i(Component.text("Looter: " + data.getDiscoveredBy())), GRAY)));
      }
      lore.add(centerLargeText(color(i(Component.text("Date: " + data.getCreationDate())), GRAY)));
    }
    List<String> prefixThings = new ArrayList<>();
    if (!gemPrefix.isBlank()) {
      prefixThings.add(gemPrefix);
    }
    if (!statPrefix.isBlank()) {
      prefixThings.add(statPrefix);
    }
    if (data.getBoolean("distorted") && statPrefix.length() + gemPrefix.length() < 17) {
      prefixThings.add("Distorted");
    }
    String title = "";
    if (!prefixThings.isEmpty()) {
      title = StringUtils.join(prefixThings, " ");
      var spacing = Component.text(GUI.spacesOf(-countSubtitleSize(title) / 2));
      lore.set(4, HALFWAY.append(spacing).append(
          noShadow(color(Component.text(doPrefix(title)), color))).append(JUMP_BACK));
    }

    applyUntradeable(stack, lore, true);

    StringBuilder nameBuilder = new StringBuilder();
    if (scaledPlus > 0) {
      nameBuilder.append("+").append(scaledPlus).append(" ");
    }
    if (!title.isBlank()) {
      nameBuilder.append(title).append(" ");
    }
    if (!prefixThings.isEmpty()) {
      nameBuilder.append(data.getString("prefix"));
    }
    nameBuilder.append(name);
    if (!data.getString("suffix").isBlank()) {
      nameBuilder.append(" ").append(data.getString("suffix"));
    }
    String fullName = nameBuilder.toString();
    if (fullName.length() > 26) {
      fullName = fullName.substring(0, 24) + "...";
    } else {
      fullName = nameBuilder.toString();
    }
    String finalName = fullName;

    stack.editMeta(m -> {
      m.displayName((color(Component.text(finalName), color)));
      m.lore(lore);

    });

    if (tier.getTrim() != null && tierNum != -1) {
      ItemUtils.applyCustomTrim(stack, tier.getTrim() + "_t" + tierNum);
    }
    if (unique != null && unique.getTrim() != null) {
      ItemUtils.applyCustomTrim(stack, unique.getTrim());
    }

    if (data.getUpgradeLevel() * tier.getUpgradeScale() >= 10) {
      stack.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
    } else {
      stack.removeEnchantment(Enchantment.UNBREAKING);
    }

    PersistHelper.setEquipmentData(stack, data, false);
  }

  private static boolean renderEquipmentTraits(EquipmentData data, List<Component> lore) {
    Set<LoreAbility> loreAbilities = new HashSet<>();
    Set<StrifeTrait> traits = new HashSet<>();
    for (String s : data.getPassives()) {
      LoreAbility la = StrifePlugin.getInstance().getLoreAbilityManager().getLoreAbilityFromId(s);
      if (la != null) {
        loreAbilities.add(la);
        continue;
      }
      // Note: get your traits enums right dipshit. Unsafe but FAST
      traits.add(StrifeTrait.valueOf(s));
    }
    if (!(loreAbilities.isEmpty() && traits.isEmpty())) {
      for (LoreAbility loreAbility : loreAbilities) {
        for (var s : loreAbility.getDescAsComponents()) {
          lore.add(centerLargeText(s));
        }
      }
      for (StrifeTrait strifeTrait : traits) {
        for (var s : strifeTrait.getDescAsComponents()) {
          lore.add(centerLargeText(s));
        }
      }
    }
    return !loreAbilities.isEmpty() || !traits.isEmpty();
  }

  public static StatResponse buildStatResponse(LootPlugin plugin, int level, int upgradeLevel,
      double backupRarityPower, StoredStat stat, int statOrder) {
    ItemStat itemStat = stat.getStat();
    if (itemStat == null) {
      return null;
    }
    if (level < 1) {
      level = 1;
    }
    float flatBonus = 0;
    float multiplier = 1;
    int statLevel = stat.getLevel(level);
    if (stat.isDistorted()) {
      statLevel += 10;
      flatBonus += 1;
      multiplier += 0.05f;
    }
    double rarityPower = stat.getRarityPower(backupRarityPower);

    //System.out.println("stat: " + stat.getStat().getId());
    //System.out.println("stat order " + statOrder);
    if (statOrder == 1) {
      flatBonus += upgradeLevel;
      multiplier += 0.00667f * upgradeLevel;
    } else if (statOrder == 2) {
      flatBonus += upgradeLevel / 5;
    }
    multiplier += (float) (stat.getUniqueMult(1) - 1f);
    StatResponse sr = plugin.getStatManager().getFinalStat(
        itemStat,
        statLevel,
        (float) rarityPower,
        (float) stat.getStatRoll(),
        multiplier,
        flatBonus
    );
    //System.out.println(sr.getStatString() + " " + sr.getValue());
    stat.setCachedValue(stat.isInverted() ? -sr.getValue() : sr.getValue());
    stat.setSortOrder(sr.getValue() > 0 ? sr.getStatRoll() : -1);
    return sr;
  }

  public static void applyUntradeable(ItemStack stack, List<Component> lore, boolean large) {
    if (!PersistHelper.isTradeLocked(stack) || lore.isEmpty()) {
      return;
    }
    var originalString = lore.getLast();
    lore.set(lore.size() - 1, (large ? UNTRADEABLE_NORMAL : UNTRADEABLE_SMALL).append(originalString));
  }

  private static String selectEnchantChar(EquipmentData data) {
    float fullness = (float) data.getInt("enchantPoints", 0) / data.getInt("maxEnchantPoints", 1);
    return ENCHANT_CHARS.get((int) ((ENCHANT_CHARS.size() - 1f) * fullness));
  }

  public static void replace(ItemStack stack, ItemStack template) {
    replace(stack, template, false);
  }

  public static void replace(ItemStack stack, ItemStack template, boolean large) {
    boolean isTradeLocked = PersistHelper.isTradeLocked(stack);
    stack.setType(template.getType());
    stack.setData(template.getData());
    stack.setDurability(template.getDurability());
    stack.setItemMeta(template.getItemMeta());
    if (isTradeLocked) {
      PersistHelper.setTradeLocked(stack, true);
      var lore = stack.getItemMeta().lore();
      ItemRenderer.applyUntradeable(stack, lore, large);
      stack.editMeta(m -> m.lore(lore));
    }
  }

}
