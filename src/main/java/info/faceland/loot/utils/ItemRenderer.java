package info.faceland.loot.utils;

import static info.faceland.loot.LootPlugin.ITEM_VERSION;
import static info.faceland.loot.LootPlugin.ITEM_VERSION_KEY;
import static info.faceland.loot.utils.ItemStyleUtil.CORRUPTION;
import static info.faceland.loot.utils.ItemStyleUtil.ENCHANT_CHARS;
import static info.faceland.loot.utils.ItemStyleUtil.HALFWAY;
import static info.faceland.loot.utils.ItemStyleUtil.UNTRADEABLE_NORMAL;
import static info.faceland.loot.utils.ItemStyleUtil.UNTRADEABLE_SMALL;
import static info.faceland.loot.utils.ItemStyleUtil.centerLargeText;
import static info.faceland.loot.utils.ItemStyleUtil.centerSmallText;
import static info.faceland.loot.utils.ItemStyleUtil.countSubtitleSize;
import static info.faceland.loot.utils.ItemStyleUtil.countTitleSize;
import static info.faceland.loot.utils.ItemStyleUtil.doNameThing;
import static info.faceland.loot.utils.ItemStyleUtil.doPrefix;
import static info.faceland.loot.utils.ItemStyleUtil.doSuffix;

import com.sentropic.guiapi.gui.GUI;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.data.ItemStat;
import info.faceland.loot.data.StatResponse;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.StoredStat;
import info.faceland.loot.events.ExternalRenderEvent;
import info.faceland.loot.items.CraftMaterial;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.NewCustomItem;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.SoulGem;
import info.faceland.loot.items.Tier;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.AwakeningStone;
import info.faceland.loot.items.prefabs.DivineHammer;
import info.faceland.loot.items.prefabs.ManaCrystal;
import info.faceland.loot.items.prefabs.MissingStat;
import info.faceland.loot.items.prefabs.NormalItem;
import info.faceland.loot.items.prefabs.OrbOfManyFaces;
import info.faceland.loot.items.prefabs.PerfectSphere;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.items.prefabs.TinkerersGear;
import info.faceland.loot.items.prefabs.TooltipPaint;
import info.faceland.loot.items.prefabs.UnidEnchant;
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class ItemRenderer {

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
      case "socket" -> SocketGem.update(stack);
      case "tome" -> EnchantmentTome.update(stack);
      case "scroll" -> UpgradeScroll.update(stack);
      case "material" -> CraftMaterial.update(stack);
      case "purity" -> PurifyingScroll.update(stack);
      case ManaCrystal.TYPE_KEY -> ManaCrystal.update(stack);
      case "pet" -> { /* do nothing, for now */ }
      case "food" -> FoodsPlugin.getInstance().getFoodsManager().updateItem(stack);
      case "extender" -> SocketExtender.update(stack);
      case "essence" -> renderEssence(stack, null);
      case "enhancer" -> ArcaneEnhancer.update(stack);
      case "tinker" -> TinkerersGear.update(stack);
      case "reroll" -> OrbOfManyFaces.update(stack);
      case "reroll2" -> PerfectSphere.update(stack);
      case "reroll3" -> DivineHammer.update(stack);
      case "normal" -> NormalItem.update(stack);
      case "soulgem" -> SoulGem.update(stack);
      case TooltipPaint.TYPE_KEY -> TooltipPaint.update(stack);
      case AwakeningStone.TYPE_KEY -> AwakeningStone.update(stack);
      case MissingStat.TYPE_KEY -> MissingStat.update(stack);
      default -> ConversionUtil.attemptToAbsorbItem(stack);
    }
    ExternalRenderEvent craftEvent = new ExternalRenderEvent(stack);
    Bukkit.getPluginManager().callEvent(craftEvent);
    applyCommonFlags(stack);
  }

  public static void applyCommonFlags(ItemStack stack) {
    stack.removeItemFlags(stack.getItemFlags().toArray(new ItemFlag[0]));
    stack.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ITEM_SPECIFICS, ItemFlag.HIDE_ARMOR_TRIM,
        ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_DYE);
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

    String viewThing = SkillRank.check(rank, stat.getLevel()) ?
        LoreglassMenu.getPercentText((float) stat.getStatRoll()) : "";

    String statText;
    if (sr == null) {
      statText = centerSmallText(FaceColor.CRIMSON + "Missing stat :(");
    } else if (stat.isDistorted()) {
      statText = centerSmallText(sr.getStatString() + ItemStyleUtil.getCorruptAddon(), viewThing);
    } else {
      statText = centerSmallText(sr.getStatString(), viewThing);
    }

    int modelId = 100000;
    modelId += 1000 * tier.getEssenceId();
    modelId += Math.min(100, stat.getEssenceLevel(stat.getLevel()));

    ItemMeta meta = stack.getItemMeta();
    meta.setDisplayName(FaceColor.CYAN + "Item Essence (" + tier.getPlainName() + ")");
    meta.setLore(List.of(
        PaletteUtil.color("|ns|\uF808万\uF808\uF801"),
        FaceColor.WHITE + centerSmallText("Item Type: " + FaceColor.CYAN + tier.getPlainName()),
        FaceColor.WHITE + centerSmallText("Essence Level: " + FaceColor.LIGHT_GREEN + stat.getEssenceLevel(stat.getLevel())),
        "",
        statText,
        "",
        FaceColor.LIGHT_GRAY + centerSmallText("Craft this together with"),
        FaceColor.LIGHT_GRAY + centerSmallText("an unfinished item with"),
        FaceColor.LIGHT_GRAY + centerSmallText("an empty stat slot!")
    ));
    meta.setCustomModelData(modelId);
    PersistentDataContainer pdc = meta.getPersistentDataContainer();
    pdc.set(ITEM_VERSION_KEY, PersistentDataType.INTEGER, ITEM_VERSION);
    stack.setItemMeta(meta);
  }

  public static void renderEquipmentItem(LootPlugin plugin, ItemStack stack, EquipmentData data, SkillRank viewRank) {
    List<String> lore = new ArrayList<>();

    NewCustomItem unique = LootPlugin.getInstance().getCustomItemManager().getUnique(PersistHelper.getItemId(stack));
    ItemRarity r = data.getItemRarity();
    String plate, rarityName;
    Tier tier;
    FaceColor color;
    if (unique == null) {
      if (StringUtils.isNotBlank(data.getString("craftedBy"))) {
        color = FaceColor.YELLOW;
        plate = "俌";
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
      rarityName = "Unique";
    }

    stack.setType(tier.getMaterial());
    if (tier.getMaterial() == Material.BOW) {
      stack.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 11);
    }
    if (!tier.getDestroyKeys().isEmpty()) {
      stack.editMeta(im -> im.setDestroyableKeys(new HashSet<>(tier.getDestroyKeys())));
    }

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
    lore.add(FaceColor.NO_SHADOW + "\uF808" + plate + "\uF80C\uF80B\uF828" + plus);
    lore.add(HALFWAY + GUI.spacesOf(1 + (-countTitleSize(name) / 2)) + FaceColor.BLACK + doNameThing(name) + "\uF80C");
    String subtitle = rarityName + " " + tier.getPlainName();
    lore.add(HALFWAY + GUI.spacesOf(-countSubtitleSize(subtitle) / 2) + color + doSuffix(subtitle) + "\uF80C");
    String distortion = "";
    if (distorted) {
      distortion = FaceColor.NO_SHADOW + GUI.spacesOf(-22) + CORRUPTION + GUI.spacesOf(-31) + FaceColor.GROW + "俽"
          + GUI.spacesOf(-162);
    }
    if (tier.isSkillRequirement()) {
      String reqString = "Skill Requirement: " + data.getInt("level");
      lore.add(distortion + FaceColor.WHITE + centerLargeText(reqString));
    } else {
      String reqString = "Level Requirement: " + data.getInt("level");
      lore.add(distortion + FaceColor.WHITE + centerLargeText(reqString));
    }
    lore.add("");
    if (unique == null) {
      ItemStackExtensionsKt.setCustomModelData(stack,
          tier.getCustomDataStart() + data.getLevel() / tier.getCustomDataInterval());
    } else {
      ItemStackExtensionsKt.setCustomModelData(stack, unique.getCustomDataNumber());
    }

    if (!data.getCoreStats().isEmpty()) {
      int numStats = 1;
      for (StoredStat stat : data.getCoreStats()) {
        StatResponse sr = buildStatResponse(plugin, data.getLevel(), data.getUpgradeLevel(), r.getPower(), stat,
            numStats);
        if (sr != null && !stat.getStat().isHide()) {
          lore.add(centerLargeText(sr.getStatString()));
        }
        numStats++;
      }
    } else {
      lore.add(FaceColor.GRAY + "No core stats...");
    }
    lore.add("\uF82C\uF829\uF825");
    String statPrefix = "";
    if (!data.getBonusStats().isEmpty()) {
      data.updateUniqueMultipliers(unique);
      Map<StoredStat, StatResponse> tempRollMap = new HashMap<>();
      for (StoredStat stat : data.getBonusStats()) {
        StatResponse sr = buildStatResponse(plugin, data.getLevel(), data.getUpgradeLevel(), r.getPower(), stat, 0);
        if (sr != null && !stat.getStat().isHide()) {
          tempRollMap.put(stat, sr);
        }
      }
      data.sortBonusStats();
      if (unique == null && !data.getBonusStats().isEmpty()) {
        ItemStat stat = data.getBonusStats().get(0).getStat();
        if (data.getBonusStats().get(0).getStatRoll() >= 0.85 && !stat.getNamePrefixes().isEmpty()
            && !data.getBonusStats().get(0).isEssenceSlot()) {
          statPrefix = stat.getNamePrefixes().get(0);
        }
      }
      for (StoredStat stat : data.getBonusStats()) {
        StatResponse sr = tempRollMap.get(stat);
        String corruptStr = distorted ? "" : (stat.isDistorted() ? ItemStyleUtil.getCorruptAddon() : "");
        if (stat.isInverted()) {
          String viewThing = SkillRank.check(viewRank, data.getLevel()) ?
              LoreglassMenu.getPercentText((float) stat.getStatRoll()) : "";
          lore.add(centerLargeText(FaceColor.RED +
              ChatColor.stripColor(sr.getStatString()).replace("+", "-") + corruptStr, viewThing));
        } else if (stat.isCrafted()) {
          String viewThing = SkillRank.check(viewRank, data.getLevel()) ?
              LoreglassMenu.getPercentText((float) stat.getStatRoll()) : "";
          lore.add(centerLargeText(FaceColor.CYAN + ChatColor.stripColor(sr.getStatString()) + corruptStr, viewThing));
          if (!stat.getOriginUnique().isBlank() && SkillRank.isRank(viewRank, SkillRank.EXPERT)) {
            var originUnique = plugin.getCustomItemManager().getUnique(stat.getOriginUnique());
            if (originUnique != null) {
              lore.add(centerLargeText(FaceColor.GRAY + "From: " + originUnique.getItemName()));
            }
          }
        } else if (stat.isEssenceSlot()) {
          lore.add(centerLargeText(FaceColor.GRAY + "< Empty Stat Slot >"));
        } else {
          String viewThing = SkillRank.check(viewRank, data.getLevel()) ?
              LoreglassMenu.getPercentText((float) stat.getStatRoll()) : "";
          lore.add(centerLargeText(sr.getStatString() + corruptStr, viewThing));
        }
      }
      if (unique != null) {
        for (String s : unique.getJokeStats()) {
          lore.add(centerLargeText(s));
        }
      }
      if (!data.isEnchanted()) {
        lore.add("");
      }
    }
    if (data.hasEnchantmentSlot()) {
      if (data.getEnchantment() == null) {
        lore.add(FaceColor.NO_SHADOW + "\uF805严\uF80A");
        lore.add("");
      } else {
        boolean enhanced = data.isEnhanced();
        lore.add((enhanced ? FaceColor.RED : FaceColor.WHITE) + "\uF805" + selectEnchantChar(data) + "\uF80A");
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
        String viewThing = SkillRank.check(viewRank, enchantmentStat.getLevel()) ?
            LoreglassMenu.getPercentText((float) enchantmentStat.getStatRoll()) + FaceColor.BLUE +
                " (" + enchantmentStat.getLevel() + ")": "";
        lore.add(centerLargeText(sr.getStatString(), viewThing));
        lore.add("");
      }
    }

    boolean hasTraits = renderEquipmentTraits(data, lore);
    if (hasTraits) {
      lore.add("");
    }

    String gemPrefix = "";
    int totalGemSlots = data.getSockets().size() + data.getEmptyGemSlots() + data.getEmptyExtendSlots();
    if (totalGemSlots > 0) {
      if (!data.getSocketGems().isEmpty()) {
        int gemPrefixData = 0;
        for (String socketStr : data.getSockets()) {
          SocketGem gem = plugin.getSocketGemManager().getSocketGem(socketStr);
          //System.out.println("detected gem: " + gem.getId());
          if (gem.getCustomModelData() > gemPrefixData && !StringUtils.isBlank(gem.getPrefix())) {
            gemPrefix = gem.getPrefix();
            gemPrefixData = gem.getCustomModelData();
          }
          for (Entry<StrifeStat, Float> entry : gem.getStats().entrySet()) {
            if (entry.getValue() > 0) {
              lore.add(centerLargeText(FaceColor.ORANGE +
                  "+" + Math.round(entry.getValue()) + " " + entry.getKey().getName()));
            } else {
              lore.add(centerLargeText(FaceColor.RED.s() +
                  Math.round(entry.getValue()) + " " + entry.getKey().getName()));
            }
            //System.out.println("detected flat stat " + entry.getKey());
          }
          for (Entry<StrifeStat, Float> entry : gem.getPercentStats().entrySet()) {
            if (entry.getValue() > 0) {
              lore.add(centerLargeText(FaceColor.ORANGE +
                  "+" + Math.round(entry.getValue()) + "% " + entry.getKey().getName()));
            } else {
              lore.add(centerLargeText(FaceColor.RED.s() +
                  Math.round(entry.getValue()) + "% " + entry.getKey().getName()));
            }
          }
          for (LoreAbility loreAbility : gem.getLoreAbilities()) {
            //System.out.println("detected la: " + loreAbility.getId());
            for (String s : loreAbility.getDescription()) {
              lore.add(centerLargeText(s));
            }
          }
          for (StrifeTrait strifeTrait : gem.getTraits()) {
            for (String s : strifeTrait.getAdditionalText()) {
              lore.add(centerLargeText(s));
            }
          }
        }
      } else {
        lore.add(FaceColor.ORANGE + centerLargeText("- Socket Slots -"));
      }
      lore.add(FaceColor.TRUE_WHITE +
          ItemStyleUtil.generateGemString(totalGemSlots, data.getEmptyExtendSlots(), data.getSocketGems()));
      lore.add("");
    }

    if (unique != null && !unique.getFlavorText().isEmpty()) {
      lore.addAll(unique.getFlavorText());
    } else if (StringUtils.isNotBlank(data.getString("craftedBy"))) {
      lore.add(centerLargeText(FaceColor.GRAY + FaceColor.ITALIC.s() + "Crafter: " + data.getString("craftedBy")));
    }
    if (SkillRank.isRank(viewRank, SkillRank.NOVICE)) {
      if (StringUtils.isBlank(data.getString("craftedBy"))) {
        lore.add(centerLargeText(FaceColor.GRAY + FaceColor.ITALIC.s() + "Looter: " + data.getDiscoveredBy()));
      }
      lore.add(centerLargeText(FaceColor.GRAY + FaceColor.ITALIC.s() + "Date: " + data.getCreationDate()));
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
      lore.set(4, HALFWAY + GUI.spacesOf(-countSubtitleSize(title) / 2) + color + doPrefix(title) + "\uF80C");
    }

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
    }
    ItemStackExtensionsKt.setDisplayName(stack, color + fullName);

    applyUntradeable(stack, lore, true);

    TextUtils.setLore(stack, lore, false);

    if (data.getUpgradeLevel() * tier.getUpgradeScale() >= 10) {
      stack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
    } else {
      stack.removeEnchantment(Enchantment.DURABILITY);
    }

    PersistHelper.setEquipmentData(stack, data, false);
  }

  private static boolean renderEquipmentTraits(EquipmentData data, List<String> lore) {
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
        for (var s : loreAbility.getDescription()) {
          lore.add(centerLargeText(s));
        }
      }
      for (StrifeTrait strifeTrait : traits) {
        for (var s : strifeTrait.getAdditionalText()) {
          lore.add(centerLargeText(s));
        }
      }
    }
    return !loreAbilities.isEmpty() || !traits.isEmpty();
  }

  private static StatResponse buildStatResponse(LootPlugin plugin, int level, int upgradeLevel,
      double backupRarityPower, StoredStat stat, int statOrder) {
    ItemStat itemStat = stat.getStat();
    if (itemStat == null) {
      return null;
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
    stat.setCachedValue(sr.getValue());
    stat.setSortOrder(sr.getValue() > 0 ? sr.getStatRoll() : -1);
    return sr;
  }

  private static void applyUntradeable(ItemStack stack, List<String> lore, boolean large) {
    if (!PersistHelper.isTradeLocked(stack) || lore.isEmpty()) {
      return;
    }
    String originalString = lore.get(lore.size() - 1);
    lore.set(lore.size() - 1, (large ? UNTRADEABLE_NORMAL : UNTRADEABLE_SMALL) + originalString);
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
      var lore = stack.getItemMeta().getLore();
      ItemRenderer.applyUntradeable(stack, lore, large);
      TextUtils.setLore(stack, lore, false);
    }
  }

}
