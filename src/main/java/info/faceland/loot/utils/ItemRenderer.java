package info.faceland.loot.utils;

import static info.faceland.loot.LootPlugin.ITEM_VERSION;
import static info.faceland.loot.utils.ItemStyleUtil.CORRUPTION;
import static info.faceland.loot.utils.ItemStyleUtil.ENCHANT_CHARS;
import static info.faceland.loot.utils.ItemStyleUtil.HALFWAY;
import static info.faceland.loot.utils.ItemStyleUtil.HALFWAY_SMALL;
import static info.faceland.loot.utils.ItemStyleUtil.UNTRADEABLE_NORMAL;
import static info.faceland.loot.utils.ItemStyleUtil.UNTRADEABLE_SMALL;
import static info.faceland.loot.utils.ItemStyleUtil.centerLargeText;
import static info.faceland.loot.utils.ItemStyleUtil.centerSmallText;
import static info.faceland.loot.utils.ItemStyleUtil.centerText;
import static info.faceland.loot.utils.ItemStyleUtil.countSubtitleSize;
import static info.faceland.loot.utils.ItemStyleUtil.countTitleSize;
import static info.faceland.loot.utils.ItemStyleUtil.doNameThing;
import static info.faceland.loot.utils.ItemStyleUtil.doPrefix;
import static info.faceland.loot.utils.ItemStyleUtil.doSmallNameThing;
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
import info.faceland.loot.items.CraftMaterial;
import info.faceland.loot.items.EnchantmentTome;
import info.faceland.loot.items.NewCustomItem;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.SoulGem;
import info.faceland.loot.items.UpgradeScroll;
import info.faceland.loot.items.prefabs.ArcaneEnhancer;
import info.faceland.loot.items.prefabs.DivineHammer;
import info.faceland.loot.items.prefabs.OrbOfManyFaces;
import info.faceland.loot.items.prefabs.PerfectSphere;
import info.faceland.loot.items.prefabs.PurifyingScroll;
import info.faceland.loot.items.prefabs.SocketExtender;
import info.faceland.loot.items.prefabs.TinkerersGear;
import info.faceland.loot.items.Tier;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemRenderer {

  public static void renderItem(ItemStack stack, boolean force) {
    System.out.println("ITEM RENDER START");
    String type = PersistHelper.getItemType(stack);
    if (type.isBlank()) {
      System.out.println("- No type... attempting to absorb item...");
      ConversionUtil.attemptToAbsorbItem(stack);
      return;
    }
    boolean versionChange = PersistHelper.getItemVersion(stack) < ITEM_VERSION;
    if (!force && !versionChange) {
      System.out.println("- Item already on current version! Aborting!");
      return;
    }
    System.out.println("- Proceeding with update...");
    System.out.println("-- Type: " + type);
    switch (type) {
      case "equipment" -> renderEquipmentItem(LootPlugin.getInstance(), stack, true);
      case "unique" -> renderUniqueItem(LootPlugin.getInstance(), stack);
      case "socket" -> SocketGem.update(stack);
      case "tome" -> EnchantmentTome.update(stack);
      case "scroll" -> UpgradeScroll.update(stack);
      case "extender" -> SocketExtender.update(stack);
      case "tinker" -> TinkerersGear.update(stack);
      case "reroll" -> OrbOfManyFaces.update(stack);
      case "reroll2" -> PerfectSphere.update(stack);
      case "reroll3" -> DivineHammer.update(stack);
      case "enhancer" -> ArcaneEnhancer.update(stack);
      case "purity" -> PurifyingScroll.update(stack);
      case "material" -> CraftMaterial.update(stack);
      case "soulgem" -> SoulGem.update(stack);
      case "essence" -> renderEssence(stack, null);
      default -> ConversionUtil.attemptToAbsorbItem(stack);
    }
  }

  private static void renderEquipmentItem(LootPlugin plugin, ItemStack stack, boolean versionChange) {
    String extraContext = PersistHelper.getItemId(stack);
    if (extraContext.isBlank()) {
      EquipmentData data = PersistHelper.getEquipmentData(stack);
      if (data == null) {
        return;
      }
      renderEquipmentItem(plugin, stack, data);
      return;
    }
    NewCustomItem ci = plugin.getCustomItemManager().getUnique(extraContext);
    if (ci == null) {
      System.out.println("Item renderer found item with no custom data");
      return;
    }
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data == null) {
      System.out.println("NO DATA");
      return;
    }
    if (versionChange) {
      PersistHelper.syncUniqueEquipment(stack, data, ci);
    }
    renderEquipmentItem(plugin, stack, data);
  }

  public static void renderUniqueItem(LootPlugin plugin, ItemStack stack) {
    NewCustomItem customItem = plugin.getCustomItemManager().getUnique(PersistHelper.getItemId(stack));
    if (customItem == null) {
      System.out.println("unique not found aborting");
      return;
    }
    String name = ChatColor.stripColor(customItem.getItemName());
    List<String> lore = new ArrayList<>();
    if (StringUtils.isNotBlank(customItem.getPlateString())) {
      lore.add(PaletteUtil.color(customItem.getPlateString()));
    } else {
      lore.add(PaletteUtil.color("|ns|\uF808丄\uF808\uF801"));
    }
    if (customItem.isPrintName()) {
      if (customItem.isLarge()) {
        lore.add(HALFWAY + GUI.spacesOf(-countTitleSize(name) / 2) + FaceColor.BLACK + doNameThing(name) + "\uF80C");
      } else {
        lore.add(HALFWAY_SMALL + GUI.spacesOf(-countTitleSize(name) / 2) + FaceColor.BLACK + doSmallNameThing(name)
            + "\uF80C");
      }
    } else {
      lore.add("");
    }
    for (String s : customItem.getFlavorText()) {
      lore.add(centerText(s, customItem.isLarge()));
    }
    applyUntradeable(stack, lore, customItem.isLarge());

    if (stack.getType() != customItem.getMaterial()) {
      stack.setType(customItem.getMaterial());
    }
    ItemMeta meta = stack.getItemMeta();
    meta.setDisplayName(FaceColor.ORANGE + customItem.getItemName());
    meta.setCustomModelData(customItem.getCustomDataNumber());
    meta.removeItemFlags(meta.getItemFlags().toArray(new ItemFlag[0]));
    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ITEM_SPECIFICS, ItemFlag.HIDE_ARMOR_TRIM,
        ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_DYE);
    meta.setLore(lore);
    stack.setItemMeta(meta);
  }

  protected static void renderEssence(ItemStack stack, StoredStat stat) {
    if (stat == null) {
      stat = PersistHelper.getEssenceStat(stack);
      if (stat == null) {
        return;
      }
    }
    Tier tier = Tier.get(stat.getOriginTier());
    StatResponse sr = buildStatResponse(
        LootPlugin.getInstance(), stat.getLevel(), 0, 0, stat, 0);

    ItemStackExtensionsKt.setDisplayName(stack, FaceColor.CYAN + "Item Essence (" + tier.getPlainName() + ")");
    List<String> lore = List.of(
        PaletteUtil.color("|ns|\uF808万\uF808\uF801"),
        FaceColor.WHITE + centerSmallText("Level: " + stat.getLevel() + "  Type: " + tier.getPlainName()),
        "",
        centerSmallText(sr.getStatString()),
        "",
        FaceColor.LIGHT_GRAY + centerSmallText("Craft this together with"),
        FaceColor.LIGHT_GRAY + centerSmallText("an unfinished item with"),
        FaceColor.LIGHT_GRAY + centerSmallText("an empty stat slot!")
    );
    ItemStackExtensionsKt.setCustomModelData(stack, 200);
    TextUtils.setLore(stack, lore, false);
  }

  protected static void renderEquipmentItem(LootPlugin plugin, ItemStack stack, EquipmentData data) {
    List<String> lore = new ArrayList<>();

    NewCustomItem nci = LootPlugin.getInstance().getCustomItemManager().getUnique(PersistHelper.getItemId(stack));
    ItemRarity r = data.getItemRarity();
    String plate, rarityName;
    Tier tier;
    FaceColor color;
    if (nci == null) {
      if (StringUtils.isNotBlank(data.getString("craftedBy"))) {
        color = FaceColor.YELLOW;
        plate = "俌";
        tier = data.getTier();
        rarityName = r.getPlainName();
      } else {
        color = r.getColor();
        plate = r.getTooltipPlate();
        tier = data.getTier();
        rarityName = r.getPlainName();
      }
    } else {
      color = nci.getColor();
      plate = "乞";
      tier = nci.getTier();
      rarityName = "Unique";
    }

    stack.setType(tier.getMaterial());

    data.getBoolean("distorted");

    String name = data.getString("name", "Item");
    String plus = "";
    if (data.getUpgradeLevel() > 0) {
      int scaledPlus = data.getUpgradeLevel() * tier.getUpgradeScale();
      scaledPlus = Math.min(15, scaledPlus);
      plus = ItemStyleUtil.upgradeNumbers.get(scaledPlus - 1);
    }
    if (nci != null && StringUtils.isNotBlank(nci.getPlateString())) {
      lore.add(PaletteUtil.color(nci.getPlateString()));
    } else {
      lore.add(FaceColor.NO_SHADOW + "\uF808" + plate + "\uF80C\uF80B\uF828" + plus);
    }
    lore.add(HALFWAY + GUI.spacesOf(1 + (-countTitleSize(name) / 2)) + FaceColor.BLACK + doNameThing(name) + "\uF80C");
    String subtitle = rarityName + " " + tier.getPlainName();
    lore.add(HALFWAY + GUI.spacesOf(-countSubtitleSize(subtitle) / 2) + color + doSuffix(subtitle) + "\uF80C");
    String distortion = "";
    if (data.getBoolean("distorted")) {
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
    if (nci == null) {
      ItemStackExtensionsKt.setCustomModelData(stack,
          tier.getCustomDataStart() + data.getLevel() / tier.getCustomDataInterval());
    } else {
      ItemStackExtensionsKt.setCustomModelData(stack, nci.getCustomDataNumber());
    }

    StringBuilder nameBuilder = new StringBuilder();
    if (data.getUpgradeLevel() > 0) {
      nameBuilder.append("+").append(data.getUpgradeLevel()).append(" ");
    }
    if (!data.getString("prefix").isBlank()) {
      nameBuilder.append(data.getString("prefix")).append(" ");
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

    if (!data.getCoreStats().isEmpty()) {
      int numStats = 1;
      for (StoredStat stat : data.getCoreStats()) {
        StatResponse sr = buildStatResponse(plugin,
            data.getLevel(), data.getUpgradeLevel(), r.getPower(), stat, numStats);
        if (sr != null) {
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
      Map<StoredStat, StatResponse> tempRollMap = new HashMap<>();
      for (StoredStat stat : data.getBonusStats()) {
        StatResponse sr = buildStatResponse(plugin, data.getLevel(), data.getUpgradeLevel(), r.getPower(), stat, 0);
        if (sr != null) {
          tempRollMap.put(stat, sr);
        }
      }
      data.sortBonusStats();
      if (nci == null && !data.getBonusStats().isEmpty()) {
        ItemStat stat = data.getBonusStats().get(0).getStat();
        if (data.getBonusStats().get(0).getStatRoll() >= 0.9 && !stat.getNamePrefixes().isEmpty()) {
          statPrefix = stat.getNamePrefixes().get(0);
        }
      }
      for (StoredStat stat : data.getBonusStats()) {
        StatResponse sr = tempRollMap.get(stat);
        if (stat.isInverted()) {
          lore.add(centerLargeText(FaceColor.RED + ChatColor.stripColor(sr.getStatString()).replace("+", "-")));
        } else if (stat.isCrafted()) {
          lore.add(centerLargeText(FaceColor.CYAN + ChatColor.stripColor(sr.getStatString())));
        } else if (stat.isEssenceSlot()) {
          lore.add(centerLargeText(FaceColor.DARK_GRAY + "< Empty Stat Slot >"));
        } else {
          lore.add(centerLargeText(sr.getStatString()));
        }
      }
    } else {
      // debug only
      //lore.add(FaceColor.GRAY + "No bonus stats...");
    }
    if (data.getBoolean("enchantable")) {
      if (data.getEnchantment() == null) {
        lore.add("");
        lore.add(FaceColor.NO_SHADOW + "\uF805严\uF80A");
      } else {
        lore.add((data.getBoolean("enhanced") ? FaceColor.RED : FaceColor.WHITE) +
            "\uF805" + selectEnchantChar(data) + "\uF80A");
        StoredStat enchantmentStat = data.getEnchantment();
        //System.out.println("a:" + enchantmentStat.toString());
        //System.out.println("b:" + enchantmentStat.getStat());
        //System.out.println("c:" + enchantmentStat.getLevel());
        //System.out.println("d:" + enchantmentStat.getRarityPower());
        //System.out.println("d:" + enchantmentStat.getRarityPower());
        StatResponse sr = plugin.getStatManager().getFinalStat(
            enchantmentStat.getStat(),
            enchantmentStat.getLevel(),
            (float) enchantmentStat.getRarityPower(),
            (float) enchantmentStat.getStatRoll(),
            1, 0
        );
        //System.out.println(sr.getStatString() + " " + sr.getValue());
        enchantmentStat.setCachedValue(sr.getValue());
        lore.add(centerLargeText(sr.getStatString()));
      }
    }

    Set<LoreAbility> loreAbilities = new HashSet<>();
    Set<StrifeTrait> traits = new HashSet<>();
    for (String s : data.getPassives()) {
      LoreAbility la = StrifePlugin.getInstance().getLoreAbilityManager().getLoreAbilityFromId(s);
      if (la != null) {
        loreAbilities.add(la);
        continue;
      }
      StrifeTrait trait = StrifeTrait.fromName(s);
      if (trait != null) {
        traits.add(trait);
      }
    }
    if (!(loreAbilities.isEmpty() && traits.isEmpty())) {
      lore.add("");
      for (LoreAbility loreAbility : loreAbilities) {
        lore.addAll(loreAbility.getDescription());
      }
      for (StrifeTrait strifeTrait : traits) {
        lore.addAll(strifeTrait.getAdditionalText());
      }
    }

    String gemPrefix = "";
    int totalGemSlots = data.getSockets().size() + data.getEmptyGemSlots() + data.getEmptyExtendSlots();
    if (totalGemSlots > 0) {
      lore.add("");
      if (!data.getSocketGems().isEmpty()) {
        int gemPrefixData = 0;
        for (String socketStr : data.getSockets()) {
          SocketGem gem = plugin.getSocketGemManager().getSocketGem(socketStr);
          System.out.println("detected gem: " + gem.getId());
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
            System.out.println("detected flat stat " + entry.getKey());
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
            System.out.println("detected la: " + loreAbility.getId());
            lore.addAll(loreAbility.getDescription());
          }
          for (StrifeTrait strifeTrait : gem.getTraits()) {
            lore.addAll(strifeTrait.getAdditionalText());
          }
        }
      } else {
        lore.add(FaceColor.ORANGE + centerLargeText("- Socket Slots -"));
      }
      lore.add(FaceColor.TRUE_WHITE +
          ItemStyleUtil.generateGemString(totalGemSlots, data.getEmptyExtendSlots(), data.getSocketGems()));
    }

    if (nci != null && !nci.getFlavorText().isEmpty()) {
      if (totalGemSlots > 0) {
        lore.add(FaceColor.TRUE_WHITE + "両");
      }
      lore.add("");
      for (String s : nci.getFlavorText()) {
        lore.add(centerLargeText(s));
      }
    } else if (StringUtils.isNotBlank(data.getString("craftedBy"))) {
      if (totalGemSlots > 0) {
        lore.add(FaceColor.TRUE_WHITE + "両");
      }
      lore.add("");
      lore.add(centerLargeText(FaceColor.GRAY + "Crafted By: " + data.getString("craftedBy")));
    } else {
      lore.add("");
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
    if (!prefixThings.isEmpty()) {
      String title = StringUtils.join(prefixThings, " ");
      lore.set(4, HALFWAY + GUI.spacesOf(-countSubtitleSize(title) / 2) + color + doPrefix(title) + "\uF80C");
    }

    applyUntradeable(stack, lore, true);

    TextUtils.setLore(stack, lore, false);

    if (data.getUpgradeLevel() >= 10) {
      stack.addEnchantment(Enchantment.DURABILITY, 1);
    } else {
      stack.removeEnchantment(Enchantment.DURABILITY);
    }

    stack.removeItemFlags(stack.getItemFlags().toArray(new ItemFlag[0]));
    stack.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ITEM_SPECIFICS,
        ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_DYE);

    PersistHelper.setEquipmentData(stack, data, false);
  }

  private static StatResponse buildStatResponse(LootPlugin plugin, int level, int upgradeLevel,
      double backupRarityPower, StoredStat stat, int statOrder) {
    ItemStat itemStat = stat.getStat();
    if (itemStat == null || itemStat.isHide()) {
      return null;
    }
    int statLevel = stat.getLevel(level);
    if (stat.isDistorted()) {
      statLevel += 10;
    }
    double rarityPower = stat.getRarityPower(backupRarityPower);
    float flatBonus = 0;
    float multiplier = 1;
    //System.out.println("stat: " + stat.getStat().getId());
    //System.out.println("stat order " + statOrder);
    if (statOrder == 1) {
      flatBonus = upgradeLevel;
      multiplier += 0.00667f * upgradeLevel;
    } else if (statOrder == 2) {
      flatBonus = upgradeLevel / 5;
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
    System.out.println(sr.getStatString() + " " + sr.getValue());
    stat.setCachedValue(sr.getValue());
    stat.setSortOrder(sr.getStatRoll());
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

}
