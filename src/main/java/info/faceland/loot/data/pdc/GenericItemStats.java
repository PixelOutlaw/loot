package info.faceland.loot.data.pdc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import land.face.strife.data.LoreAbility;
import land.face.strife.stats.StrifeTrait;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenericItemStats implements Serializable {

  private List<StoredStat> stats = new ArrayList<>();
  private Set<LoreAbility> loreAbilities = new HashSet<>();
  private Set<StrifeTrait> traits = new HashSet<>();

}
