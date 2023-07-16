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

public class LootDropEvent extends LootCancellableEvent {

  @Getter @Setter
  private double amountBonus = 1;
  @Getter @Setter
  private double rarityBonus = 1;
  @Getter
  private final List<ItemRarity> bonusTierDrops = new ArrayList<>();
  private Location location;
  private double distance;
  private UUID looterUUID;
  private int monsterLevel = 1;
  private LivingEntity entity;
  private String uniqueEntity;

  public UUID getLooterUUID() {
    return looterUUID;
  }

  public void setLooterUUID(UUID looterUUID) {
    this.looterUUID = looterUUID;
  }

  public double getDistance() {
    return distance;
  }

  public void setDistance(double distance) {
    this.distance = distance;
  }

  public int getMonsterLevel() {
    return monsterLevel;
  }

  public void setMonsterLevel(int monsterLevel) {
    this.monsterLevel = monsterLevel;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public LivingEntity getEntity() {
    return entity;
  }

  public void setEntity(LivingEntity entity) {
    this.entity = entity;
  }

  public String getUniqueEntity() {
    return uniqueEntity;
  }

  public void setUniqueEntity(String uniqueEntity) {
    this.uniqueEntity = uniqueEntity;
  }

}
