package info.faceland.loot.data.pdc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.LoreAbility;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemDetails {

  private Map<StrifeStat, Float> stats = new HashMap<>();
  private Set<LoreAbility> loreAbilities = new HashSet<>();
  private Set<StrifeTrait> traits = new HashSet<>();

  @Override
  public int hashCode() {
    return Math.abs(stats.hashCode() + traits.hashCode() + loreAbilities.hashCode());
  }
}
