package info.faceland.loot.managers;

import info.faceland.loot.api.managers.UniqueDropsManager;
import info.faceland.loot.data.MobLootTable;
import java.util.HashMap;
import java.util.Map;

public class LootUniqueDropsManager implements UniqueDropsManager {

  private Map<String, MobLootTable> lootMap;

  public LootUniqueDropsManager() {
    this.lootMap = new HashMap<>();
  }

  @Override
  public MobLootTable getData(String name) {
    return lootMap.getOrDefault(name, null);
  }

  @Override
  public void addData(String name, MobLootTable loot) {
    lootMap.put(name, loot);
  }
}
