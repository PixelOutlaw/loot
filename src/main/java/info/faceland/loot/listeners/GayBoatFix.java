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
package info.faceland.loot.listeners;

import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public final class GayBoatFix implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onSpawnBoat(EntitySpawnEvent event) {
    if (event.getEntityType() == EntityType.BOAT) {
      if (!event.getEntity().hasMetadata("NPC")) {
        event.setCancelled(true);
        event.getEntity().remove();
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onSpawnBoat(VehicleCreateEvent event) {
    if (event.getVehicle().getType() == EntityType.BOAT) {
      if (!event.getVehicle().hasMetadata("NPC")) {
        event.setCancelled(true);
        event.getVehicle().remove();
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void playerRightClickBoat(PlayerInteractAtEntityEvent e) {
    if (e.getRightClicked() instanceof Boat) {
      e.setCancelled(true);
      if (!e.getRightClicked().hasMetadata("NPC")) {
        e.getRightClicked().remove();
      }
    }
  }

  @EventHandler
  public void playerTryEnterBoat(VehicleEnterEvent e) {
    if (e.getEntered() instanceof Boat) {
      e.setCancelled(true);
      if (!e.getEntered().hasMetadata("NPC")) {
        e.getEntered().remove();
      }
    }
  }
}