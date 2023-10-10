/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.anticheat;


import info.faceland.loot.LootPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public final class AnticheatTag {

  private final UUID uuid;
  private final Location entityLocation;
  private final Map<UUID, Location> taggerLocations;
  private final Map<UUID, Double> taggerDamage;

  public AnticheatTag(LivingEntity entity) {
    this(entity.getUniqueId(), entity.getLocation());
  }

  public AnticheatTag(UUID uuid, Location entityLocation) {
    this.uuid = uuid;
    this.entityLocation = entityLocation;
    this.taggerLocations = new HashMap<>();
    this.taggerDamage = new HashMap<>();
  }

  public UUID getUniqueId() {
    return uuid;
  }

  public Location getEntityLocation() {
    return entityLocation;
  }

  public Location getTaggerLocation(UUID uuid) {
    if (taggerLocations.containsKey(uuid)) {
      return taggerLocations.get(uuid);
    }
    return null;
  }

  public void setTaggerLocation(UUID uuid, Location location) {
    taggerLocations.put(uuid, location);
  }

  public double getTaggerDamage(UUID uuid) {
    return taggerDamage.containsKey(uuid) ? taggerDamage.get(uuid) : 0D;
  }

  public void setTaggerDamage(UUID uuid, double damage) {
    if (taggerDamage.containsKey(uuid)) {
      taggerDamage.put(uuid, damage + taggerDamage.get(uuid));
      return;
    }
    taggerDamage.put(uuid, damage);
  }

  public UUID getRandomWeightedLooter() {
    if (LootPlugin.RNG.nextFloat() < 0.5f) {
      List<UUID> damagers = new ArrayList<>(taggerDamage.keySet());
      return damagers.get(LootPlugin.RNG.nextInt(damagers.size()));
    }

    UUID tagger = null;
    double highestDamage = 0D;

    for (Map.Entry<UUID, Double> entry : taggerDamage.entrySet()) {
      if (entry.getKey() == null || entry.getValue() == null) {
        continue;
      }
      if (entry.getValue() > highestDamage) {
        highestDamage = entry.getValue();
        tagger = entry.getKey();
      }
    }
    return tagger;
  }
}
