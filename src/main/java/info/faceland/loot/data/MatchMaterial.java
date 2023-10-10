package info.faceland.loot.data;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.tier.Tier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class MatchMaterial {

  private Material material;
  @Getter @Setter
  private Tier tier;
  private int minCustomData;
  private int maxCustomData;
  private final List<Material> results = new ArrayList<>();

  public Material getMaterial() {
    return material;
  }

  public void setMaterial(Material material) {
    this.material = material;
  }

  public int getMinCustomData() {
    return minCustomData;
  }

  public void setMinCustomData(int minCustomData) {
    this.minCustomData = minCustomData;
  }

  public int getMaxCustomData() {
    return maxCustomData;
  }

  public void setMaxCustomData(int maxCustomData) {
    this.maxCustomData = maxCustomData;
  }

  public static void addResult(MatchMaterial data, Material material) {
    data.results.add(material);
  }

  public static Material getResultMaterial(MatchMaterial data, Set<Material> validMats) {
    Material material = data.results.get(LootPlugin.RNG.nextInt(data.results.size()));
    if (validMats.contains(material)) {
      return material;
    }
    Bukkit.getLogger().warning(
        "[Loot] Deconstruct data for " + data.getMaterial() + " has invalid material" + material);
    return null;
  }
}
