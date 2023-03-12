package info.faceland.loot.data;

import info.faceland.loot.utils.MaterialUtil;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Data
public class PawnDeal {

  private final String name;
  private final Material material;
  private final int modelData;
  private final float multiplier;
  private final float tradeXp;

  private int minutesRemaining = 10;

  public PawnDeal(String name, Material material, int modelData, int durationMinutes, float multiplier, float tradeXp) {
    this.name = name;
    this.material = material;
    this.modelData = modelData;
    this.multiplier = multiplier;
    this.tradeXp = tradeXp;

    this.minutesRemaining = durationMinutes;
  }

  public boolean matches(ItemStack stack) {
    return stack.getType() == material && MaterialUtil.getCustomData(stack) == modelData;
  }
}
