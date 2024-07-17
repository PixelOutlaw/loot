package info.faceland.loot.managers;

import com.google.common.base.Strings;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang.StringUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang.WordUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.items.UpgradeScroll;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ScrollManager {

  private double totalWeight = -1;

  public Collection<UpgradeScroll> getScrolls() {
    return UpgradeScroll.getScrolls().values();
  }

  @Deprecated
  public UpgradeScroll getScroll(ItemStack stack) {
    if (stack == null || stack.getType() != Material.PAPER) {
      return null;
    }
    String name = ChatColor.stripColor(ItemStackExtensionsKt.getDisplayName(stack).replace(" Upgrade Scroll", ""));
    return UpgradeScroll.get(name.toLowerCase());
  }

  @Deprecated
  public UpgradeScroll getScroll(String name) {
    return UpgradeScroll.get(name.toLowerCase());
  }

  public double getTotalWeight() {
    if (totalWeight != -1) {
      return totalWeight;
    }
    totalWeight = 0;
    for (UpgradeScroll scroll : UpgradeScroll.getScrolls().values()) {
      double d = scroll.getWeight();
      if (d > 0D) {
        totalWeight += d;
      }
    }
    return totalWeight;
  }

  public UpgradeScroll getRandomScroll() {
    double selectedWeight = LootPlugin.RNG.nextFloat() * getTotalWeight();
    double currentWeight = 0;
    for (var scroll : UpgradeScroll.getScrolls().entrySet()) {
      double calcWeight = scroll.getValue().getWeight();
      if (calcWeight > 0) {
        currentWeight += calcWeight;
        if (currentWeight >= selectedWeight) {
          return scroll.getValue();
        }
      }
    }
    return null;
  }

  @Deprecated
  public Set<String> getScrollIds() {
    return UpgradeScroll.getScrolls().keySet().stream().map(value -> value.replace(" ", "_")).collect(Collectors.toSet());
  }
}
