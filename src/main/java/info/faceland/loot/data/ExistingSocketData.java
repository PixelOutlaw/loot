package info.faceland.loot.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.data.effects.CreateWorldSpaceEntity;


public class ExistingSocketData {

  private final Map<Integer, List<Integer>> gemLores = new HashMap<>();

  public boolean hasIndexes() {
    if (gemLores.size() == 0) {
      return false;
    }
    for (List<Integer> l : gemLores.values()) {
      if (l != null) {
        return true;
      }
    }
    return false;
  }

  public void addGemData(int gemNumber, List<Integer> indexes) {
    gemLores.put(gemNumber, indexes);
  }

  public List<Integer> getIndexes(int gemNumber) {
    return gemLores.getOrDefault(gemNumber, null);
  }

}
