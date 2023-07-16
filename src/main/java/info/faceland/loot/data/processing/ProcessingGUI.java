package info.faceland.loot.data.processing;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.bukkit.Material;

@Data
public class ProcessingGUI {
  private String id;
  private List<ProcessResults> processResults = new ArrayList<>();

  public ProcessResults getProcessMatch(Material material, int modelData) {
    for (ProcessResults pr : processResults) {
      if (pr.getMaterial() == material && pr.getCustomModelData() == modelData) {
        return pr;
      }
    }
    return null;
  }
}
