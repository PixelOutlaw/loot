package info.faceland.loot.events;

import info.faceland.loot.api.events.LootCancellableEvent;
import info.faceland.loot.data.ItemRarity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

@Getter
@Setter
public class LootDropEvent extends LootCancellableEvent {

  private double amountBonus = 1;
  private double rarityBonus = 1;
  private float cheesePenaltyMult = 1;
  private float levelPenaltyMult = 1;

  private final List<ItemRarity> bonusTierDrops = new ArrayList<>();
  private Location location;
  private double distance;
  private UUID looterUUID;
  private int monsterLevel = 1;
  private LivingEntity entity;
  private String uniqueEntity;

}
