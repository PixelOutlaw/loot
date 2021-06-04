package info.faceland.loot.managers;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.data.UpgradeScroll;
import info.faceland.loot.math.LootRandom;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ScrollManager {

  private Map<String, UpgradeScroll> scrolls;
  private Map<UpgradeScroll, ItemStack> cachedScrollStacks;
  private LootRandom random;
  private Material scrollMaterial;
  private double totalWeight;

  public ScrollManager() {
    scrolls = new HashMap<>();
    cachedScrollStacks = new HashMap<>();
    random = new LootRandom(System.currentTimeMillis());
    scrollMaterial = Material.PAPER;
  }

  public ItemStack buildItemStack(UpgradeScroll upgradeScroll) {
    if (!cachedScrollStacks.containsKey(upgradeScroll)) {
      ItemStack stack = new ItemStack(scrollMaterial);
      ItemStackExtensionsKt.setDisplayName(stack,
          ChatColor.DARK_GREEN + upgradeScroll.getPrefix() + " Upgrade Scroll");
      ItemStackExtensionsKt.setLore(stack, upgradeScroll.getLore());
      stack.setDurability((short) 11);
      ItemStackExtensionsKt.setCustomModelData(stack, upgradeScroll.getCustomData());
      cachedScrollStacks.put(upgradeScroll, stack);
    }
    return cachedScrollStacks.get(upgradeScroll).clone();
  }

  public UpgradeScroll getScroll(ItemStack stack) {
    if (stack == null || stack.getType() != scrollMaterial) {
      return null;
    }
    String name = ItemStackExtensionsKt.getDisplayName(stack);
    if (StringUtils.isBlank(name) || !name.endsWith("Upgrade Scroll")) {
      return null;
    }
    name = ChatColor.stripColor(name);
    for (UpgradeScroll scroll : scrolls.values()) {
      if (name.startsWith(scroll.getPrefix())) {
        return scroll;
      }
    }
    return null;
  }

  public UpgradeScroll getScroll(String name) {
    return scrolls.get(name);
  }

  public void addScroll(String name, UpgradeScroll scroll) {
    scrolls.put(name, scroll);
  }

  public void rebuildTotalWeight() {
    totalWeight = getTotalWeight();
  }

  public UpgradeScroll getRandomScroll() {
    double selectedWeight = random.nextDouble() * totalWeight;
    double currentWeight = 0;
    for (UpgradeScroll scroll : scrolls.values()) {
      double calcWeight = scroll.getWeight();
      if (calcWeight > 0) {
        currentWeight += calcWeight;
        if (currentWeight >= selectedWeight) {
          return scroll;
        }
      }
    }
    return null;
  }

  public Set<String> getScrollIds() {
    return scrolls.keySet().stream().map(value -> value.replace(" ", "_")).collect(Collectors.toSet());
  }

  private double getTotalWeight() {
    double weight = 0;
    for (UpgradeScroll scroll : scrolls.values()) {
      double d = scroll.getWeight();
      if (d > 0D) {
        weight += d;
      }
    }
    return weight;
  }
}
