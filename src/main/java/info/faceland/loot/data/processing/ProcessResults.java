package info.faceland.loot.data.processing;

import lombok.Data;
import org.bukkit.Material;

@Data
public class ProcessResults {
  private Material material;
  private int customModelData;
  private ProcessOutput outputOne;
  private ProcessOutput outputTwo;
  private ProcessOutput outputThree;
}
