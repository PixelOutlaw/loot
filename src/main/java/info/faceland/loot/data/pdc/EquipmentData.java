package info.faceland.loot.data.pdc;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.ItemRarity;
import info.faceland.loot.items.NewCustomItem;
import info.faceland.loot.items.SocketGem;
import info.faceland.loot.items.Tier;
import info.faceland.loot.utils.EssenceUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

@Getter
@Setter
public class EquipmentData implements Serializable {

  //@Serial
  //private static final long serialVersionUID = 2L;

  private Map<String, String> stringData = new HashMap<>();
  private Map<String, Integer> intData = new HashMap<>();
  private Map<String, Double> doubleData = new HashMap<>();
  private Map<String, Boolean> booleanData = new HashMap<>();

  private List<StoredStat> coreStats = new ArrayList<>();
  private List<StoredStat> bonusStats = new ArrayList<>();

  private List<String> passives = new ArrayList<>();
  private List<String> sockets = new ArrayList<>();

  private StoredStat enchantment = null;

  @Getter(value=AccessLevel.NONE)
  @Setter(value=AccessLevel.NONE)
  private transient List<SocketGem> socketGems = null;
  @Getter(value=AccessLevel.NONE)
  @Setter(value=AccessLevel.NONE)
  private transient ItemRarity itemRarity = null;
  @Getter(value= AccessLevel.NONE)
  @Setter(value=AccessLevel.NONE)
  private transient Tier itemTier = null;

  public int rebuild(int oldHash) {
    if (oldHash == intData.getOrDefault("hash", 0)) {
      return oldHash;
    }
    ItemDetails itemDetails = buildItemDetails();
    intData.put("hash", itemDetails.hashCode());
    return intData.get("hash");
  }

  public ItemDetails buildItemDetails() {
    ItemDetails itemDetails = new ItemDetails();
    for (StoredStat s : coreStats) {
      StrifeStat stat = s.getStat().getStat();
      itemDetails.getStats().put(stat, itemDetails.getStats().getOrDefault(stat, 0f) + s.getCachedValue());
    }
    for (StoredStat s : bonusStats) {
      if (s.isEssenceSlot()) {
        continue;
      }
      if (s.getStat() == null) {
        Bukkit.getLogger().severe("[Loot] Unknown stat on item!");
        continue;
      }
      StrifeStat stat = s.getStat().getStat();
      itemDetails.getStats().put(stat, itemDetails.getStats().getOrDefault(stat, 0f) + s.getCachedValue());
    }
    if (enchantment != null) {
      StrifeStat stat = enchantment.getStat().getStat();
      itemDetails.getStats().put(stat, itemDetails.getStats().getOrDefault(stat, 0f) + enchantment.getCachedValue());
    }
    if (getTier() != null) {
      for (var stat : getTier().getHiddenStats().entrySet()) {
        itemDetails.getStats().put(stat.getKey(),
            itemDetails.getStats().getOrDefault(stat.getKey(), 0f) + stat.getValue().intValue());
      }
    }
    for (String s : passives) {
      LoreAbility la = StrifePlugin.getInstance().getLoreAbilityManager().getLoreAbilityFromId(s);
      if (la != null) {
        itemDetails.getLoreAbilities().add(la);
        continue;
      }
      try {
        itemDetails.getTraits().add(StrifeTrait.valueOf(s));
        continue;
      } catch (Exception ignored) {
        // Nothing
      }
      Bukkit.getLogger().warning("[Loot] Bad passive load" + s + " attempted");
    }
    for (SocketGem gem : getSocketGems()) {
      for (var gemStats : gem.getStats().entrySet()) {
        itemDetails.getStats().put(gemStats.getKey(),
            itemDetails.getStats().getOrDefault(gemStats.getKey(), 0f) + gemStats.getValue().intValue());
      }
      for (var gemStats : gem.getPercentStats().entrySet()) {
        itemDetails.getStats().put(gemStats.getKey(),
            itemDetails.getStats().getOrDefault(gemStats.getKey(), 0f) + gemStats.getValue().intValue());
      }
      itemDetails.getTraits().addAll(gem.getTraits());
      itemDetails.getLoreAbilities().addAll(gem.getLoreAbilities());
    }
    return itemDetails;
  }

  public void setLevel(int level) {
    intData.put("level", level);
  }

  public int getLevel() {
    return intData.getOrDefault("level", 0);
  }

  public void setCreationDate(String creation) {
    stringData.put("creation-date", creation);
  }

  public String getCreationDate() {
    return stringData.getOrDefault("creation-date", "The Before Times");
  }

  public void setDiscoveredBy(String c) {
    stringData.put("creator", c);
  }

  public String getDiscoveredBy() {
    return stringData.getOrDefault("creator", "The Gods");
  }

  public void upgrade() {
    if (getUpgradeLevel() * getTier().getUpgradeScale() < 15) {
      setUpgradeLevel(getUpgradeLevel() + 1);
    }
  }

  public void setUpgradeLevel(int level) {
    intData.put("upgradeLevel", level);
  }

  public int getUpgradeLevel() {
    return intData.getOrDefault("upgradeLevel", 0);
  }

  public void setTier(String tier) {
    stringData.put("tier", tier);
    itemTier = Tier.get(tier);
  }

  public void setTier(Tier tier) {
    stringData.put("tier", tier.getId());
    itemTier = tier;
  }

  public Tier getTier() {
    if (itemTier == null) {
      itemTier = Tier.get(stringData.getOrDefault("tier", "sword"));
    }
    return itemTier;
  }

  public void setRarity(String rarity) {
    stringData.put("rarity", rarity);
    itemRarity = LootPlugin.getInstance().getRarityManager().getRarity(rarity);
  }

  public ItemRarity getItemRarity() {
    if (itemRarity == null) {
      itemRarity = LootPlugin.getInstance().getRarityManager().getRarity(stringData.getOrDefault("rarity", "COMMON"));
    }
    return itemRarity;
  }

  public void addCoreStat(StoredStat storedStat) {
    coreStats.add(storedStat);
  }

  public void addBonusStat(StoredStat storedStat) {
    addBonusStat(storedStat, true);
  }

  public void addBonusStat(StoredStat storedStat, boolean sort) {
    bonusStats.add(storedStat);
    if (sort) {
      sortBonusStats();
    }
  }

  public boolean canExtractStat(boolean allowMultipleEssenceSlots) {
    if (bonusStats.isEmpty()) {
      return false;
    }
    List<StoredStat> eligibleStats = getExtractableStats();
    if (eligibleStats.isEmpty()) {
      return false;
    }
    if (getTier().getEssenceId() == -1) {
      return false;
    }
    if (!allowMultipleEssenceSlots && hasExistingCraftedStat()) {
      //System.out.println("not eligibile");
      return false;
    }
    return true;
  }

  public boolean hasCraftSlot() {
    for (StoredStat stat : bonusStats) {
      if (stat.isEssenceSlot()) {
        return true;
      }
    }
    return false;
  }

  public StoredStat extractBonusStat(boolean allowMultipleEssenceSlots) {
    if (bonusStats.isEmpty()) {
      return null;
    }
    List<StoredStat> eligibleStats = getExtractableStats();
    if (eligibleStats.isEmpty()) {
      return null;
    }
    if (!allowMultipleEssenceSlots && hasExistingCraftedStat()) {
      //System.out.println("not eligibile");
      return null;
    }
    StoredStat stat = eligibleStats.get(LootPlugin.RNG.nextInt(eligibleStats.size()));
    StoredStat extractedStat = new StoredStat(stat.getStat().getId(), stat.getStatRoll());
    extractedStat.copyFrom(stat);
    stat.setEssenceSlot(true);

    extractedStat.setCrafted(true);
    extractedStat.setRarityPower(getItemRarity().getPower());
    extractedStat.setLevel(getLevel());

    return extractedStat;
  }

  public boolean hasExistingCraftedStat() {
    if (bonusStats.isEmpty()) {
      return false;
    }
    for (StoredStat s : bonusStats) {
      if (s.isEssenceSlot() || s.isCrafted()) {
        return true;
      }
    }
    return false;
  }

  public List<StoredStat> getExtractableStats() {
    if (bonusStats.isEmpty()) {
      return new ArrayList<>();
    }
    List<StoredStat> eligibleStats = new ArrayList<>();
    for (StoredStat s : bonusStats) {
      if (s.isEssenceSlot() || s.isInverted() || s.isCrafted() || s.getCachedValue() < 0) {
        continue;
      }
      eligibleStats.add(s);
    }
    return eligibleStats;
  }

  // For use in crafting to ensure no dupes
  public List<StoredStat> getNormalStats() {
    if (bonusStats.isEmpty()) {
      return new ArrayList<>();
    }
    List<StoredStat> eligibleStats = new ArrayList<>();
    for (StoredStat s : bonusStats) {
      // Skip because it'll be overwritten
      if (s.isEssenceSlot()) {
        continue;
      }
      eligibleStats.add(s);
    }
    return eligibleStats;
  }

  public List<SocketGem> getSocketGems() {
    if (socketGems != null) {
      return socketGems;
    }
    socketGems = new ArrayList<>();
    for (String s : sockets) {
      if (s == null) {
        continue;
      }
      SocketGem gem = LootPlugin.getInstance().getSocketGemManager().getSocketGem(s);
      if (gem != null) {
        socketGems.add(gem);
      }
    }
    return socketGems;
  }

  public boolean canSocket() {
    return getInt("sockets") > 0;
  }

  public boolean socket(SocketGem gem) {
    if (gem != null && canSocket()) {
      getSocketGems();
      sockets.add(gem.getId());
      socketGems.add(gem);
      setInt("sockets", getInt("sockets") - 1);
      return true;
    }
    return false;
  }

  public SocketGem extractGem(int index) {
    getSocketGems();
    if (index > sockets.size() - 1) {
      return null;
    }
    String returned = sockets.remove(index);
    setEmptyGemSlots(getEmptyGemSlots() + 1);
    return LootPlugin.getInstance().getSocketGemManager().getSocketGem(returned);
  }

  public boolean canExtend() {
    return getEmptyExtendSlots() > 0;
  }

  public boolean extend() {
    if (canExtend()) {
      setEmptyGemSlots(getEmptyGemSlots() + 1);
      setEmptyExtendSlots(getEmptyExtendSlots() - 1);
      return true;
    }
    return false;
  }

  public boolean enhance() {
    if (isEnchanted() && !isEnhanced()) {
      doubleData.put("enhancement", 1 + 0.1 + 0.2 * Math.pow(Math.random(), 2));
      return true;
    }
    return false;
  }

  public void reroll(double minimum, double exponent) {
    for (StoredStat b : bonusStats) {
      if (b.isCrafted()) {
        continue;
      }
      b.setStatRoll(Math.max(minimum, Math.pow(LootPlugin.RNG.nextFloat(), exponent)));
    }
    sortBonusStats();
  }

  public void reroll() {
    reroll(0, 2.75);
  }

  public void forceRoll(double amount) {
    for (StoredStat b : bonusStats) {
      if (b.isCrafted()) {
        continue;
      }
      b.setStatRoll(amount);
    }
    sortBonusStats();
  }

  public void polish(double minimum, double exponent) {
    for (StoredStat b : bonusStats) {
      if (b.isCrafted()) {
        continue;
      }
      double percentImprovement = Math.max(minimum, Math.pow(LootPlugin.RNG.nextFloat(), exponent));
      double improvementRemaining = 1.0 - b.getStatRoll();
      b.setStatRoll(b.getStatRoll() + (improvementRemaining * percentImprovement));
    }
    sortBonusStats();
  }

  public void shitify() {
    for (StoredStat b : bonusStats) {
      if (b.isCrafted()) {
        continue;
      }
      double newValue = b.getStatRoll() * (0.4 + LootPlugin.RNG.nextDouble() * 0.4);
      b.setStatRoll(newValue);
    }
    sortBonusStats();
  }

  public void invertRandomRolls() {
    for (StoredStat b : bonusStats) {
      if (b.isCrafted()) {
        continue;
      }
      double newValue = 1.0 - b.getStatRoll();
      b.setStatRoll(newValue);
    }
    sortBonusStats();
  }

  public boolean canAddBonusStat(int leeway) {
    return bonusStats.size() < getItemRarity().getMaximumBonusStats() + leeway;
  }

  public boolean addBonusStat() {
    var tierStats = EssenceUtil.generateValidStatList(getTier(), bonusStats);
    if (tierStats.isEmpty()) {
      return false;
    }
    Collections.shuffle(tierStats);
    var selectedStat = tierStats.get(0);
    StoredStat ss = new StoredStat(selectedStat.getId(), Math.pow(Math.random(), 2.2));
    if (isDistorted()) {
      ss.setDistorted(true);
    }
    bonusStats.add(ss);
    sortBonusStats();
    return true;
  }

  public boolean isDistorted() {
    return booleanData.getOrDefault("distorted", false);
  }

  public boolean distort() {
    booleanData.put("distorted", true);
    List<StoredStat> eligibileStats = new ArrayList<>();
    for (StoredStat b : bonusStats) {
      b.setInverted(false);
      if (b.isDistorted() || b.isEssenceSlot()) {
        continue;
      }
      b.setDistorted(true);
      if (b.getCachedValue() < 0) {
        continue;
      }
      eligibileStats.add(b);
    }
    if (eligibileStats.isEmpty()) {
      return false;
    }
    eligibileStats.get(LootPlugin.RNG.nextInt(eligibileStats.size())).setInverted(true);
    sortBonusStats();
    return true;
  }

  public boolean undistort() {
    booleanData.remove("distorted");
    for (StoredStat b : bonusStats) {
      b.getBooleanData().remove("distorted");
      b.getBooleanData().remove("inverted");
    }
    sortBonusStats();
    return true;
  }

  // Return true if the item should be updated
  public boolean tickEnchant(int amount) {
    if (!isEnchanted()) {
      return false;
    }
    int currentPoints = intData.get("enchantPoints");
    if (currentPoints - amount < 0) {
      unenchant();
      return true;
    }
    int oldState = (int) ((9f * currentPoints) / intData.getOrDefault("maxEnchantPoints", 1));
    currentPoints -= amount;
    int newState = (int) ((9f * currentPoints) / intData.getOrDefault("maxEnchantPoints", 1));
    intData.put("enchantPoints", currentPoints);
    return newState != oldState;
  }

  public void refillEnchantment(int amount) {
    if (!isEnchanted()) {
      return;
    }
    int currentPoints = intData.get("enchantPoints");
    intData.put("enchantPoints", Math.min(currentPoints + amount, intData.get("maxEnchantPoints")));
  }

  public boolean unenchant() {
    if (isEnchanted()) {
      enchantment = null;
      intData.remove("maxEnchantPoints");
      intData.remove("enchantPoints");
      doubleData.remove("enhancement");
      return true;
    }
    return false;
  }

  public boolean enchant(StoredStat storedStat, int maxEnchantment) {
    if (!canBeEnchanted()) {
      return false;
    }
    enchantment = storedStat;
    intData.put("maxEnchantPoints", maxEnchantment);
    intData.put("enchantPoints", maxEnchantment);
    doubleData.remove("enhancement");
    return true;
  }

  public boolean isEnchanted() {
    return booleanData.getOrDefault("enchantable", true) && enchantment != null;
  }

  public boolean isEnhanced() {
    return enchantment != null && doubleData.getOrDefault("enhancement", 0D) > 0;
  }

  public boolean canBeEnchanted() {
    return booleanData.getOrDefault("enchantable", false) && enchantment == null;
  }

  public boolean hasEnchantmentSlot() {
    return booleanData.getOrDefault("enchantable", false);
  }

  public void setEnchantable(boolean enchantable) {
    if (enchantable) {
      booleanData.put("enchantable", true);
    } else {
      booleanData.put("enchantable", false);
      enchantment = null;
      intData.remove("maxEnchantPoints");
      intData.remove("enchantPoints");
      doubleData.remove("enhancement");
    }
  }

  public int getMissingEnchantPower() {
    if (!isEnchanted()) {
      return -1;
    }
    return intData.get("maxEnchantPoints") - intData.get("enchantPoints");
  }

  public void sortBonusStats() {
    getBonusStats().sort(Comparator
        .comparing((StoredStat s) -> s.isInverted() ? 0 : s.isEssenceSlot() ? 0 : s.getSortOrder())
        .thenComparing(StoredStat::getStatRoll));
    Collections.reverse(getBonusStats());
  }

  public void updateUniqueMultipliers(NewCustomItem parentUnique) {
    for (StoredStat newStat : getBonusStats()) {
      if (newStat.getOriginUnique().isBlank()) {
        continue;
      }
      NewCustomItem originUnique;
      if (parentUnique != null && newStat.getOriginUnique().equals(parentUnique.getId())) {
        originUnique = parentUnique;
      } else {
        originUnique = LootPlugin.getInstance().getCustomItemManager().getUnique(newStat.getOriginUnique());
      }
      if (originUnique == null) {
        continue;
      }
      for (var loopStat : originUnique.getBonusStats()) {
        if (newStat.getStat().getId().equals(loopStat.getStat().getId())) {
          newStat.setUniqueMult(loopStat.getValueModifier(), originUnique.getId());
          break;
        }
      }
    }
  }

  public int getEmptyGemSlots() {
    return intData.getOrDefault("sockets", 0);
  }

  public void setEmptyGemSlots(int i) {
    intData.put("sockets", i);
    //System.out.println("Current gem slots: " + getEmptyGemSlots());
  }

  public int getEmptyExtendSlots() {
    return intData.getOrDefault("lockedSockets", 0);
  }

  public void setEmptyExtendSlots(int i) {
    intData.put("lockedSockets", i);
    //System.out.println("Current expend slots: " + getEmptyExtendSlots());
  }

  public String getString(String key) {
    return stringData.getOrDefault(key, "");
  }

  public String getString(String key, String fallback) {
    return stringData.getOrDefault(key, fallback);
  }

  public void setString(String key, String str) {
    stringData.put(key, str);
  }

  public int getInt(String key) {
    return intData.getOrDefault(key, 0);
  }

  public int getInt(String key, int fallback) {
    return intData.getOrDefault(key, fallback);
  }

  public void setInt(String key, int val) {
    intData.put(key, val);
  }

  public boolean getBoolean(String key) {
    return booleanData.getOrDefault(key, false);
  }

  public boolean getBoolean(String key, boolean fallback) {
    return booleanData.getOrDefault(key, fallback);
  }

  public void setBoolean(String key, boolean val) {
    booleanData.put(key, val);
  }

}
