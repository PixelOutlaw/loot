package info.faceland.loot.items;

import info.faceland.loot.data.ItemStat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UniqueItemStat {

  private ItemStat stat;
  private float valueModifier;

}
