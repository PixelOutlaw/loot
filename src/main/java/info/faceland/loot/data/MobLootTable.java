package info.faceland.loot.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public class MobLootTable {

  @Getter @Setter
  private double amountMultiplier;
  @Getter @Setter
  private double rarityMultiplier;
  @Getter
  private final List<ItemRarity> bonusTierItems = new ArrayList<>();
  @Getter
  private final Map<String, Double> gemMap = new HashMap<>();
  @Getter
  private final Map<String, Double> tomeMap = new HashMap<>();
  @Getter
  private final Map<String, Map<String, Double>> customItemMap = new HashMap<>();
}
