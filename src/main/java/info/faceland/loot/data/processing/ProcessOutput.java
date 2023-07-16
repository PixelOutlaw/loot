package info.faceland.loot.data.processing;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
public class ProcessOutput {
  private int cost;
  private ItemStack reward;
}
