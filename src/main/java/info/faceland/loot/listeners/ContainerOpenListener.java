package info.faceland.loot.listeners;

import info.faceland.loot.events.LootDropEvent;
import info.faceland.loot.utils.DropUtil;
import land.face.containers.events.ContainerOpenEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ContainerOpenListener implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onContainerOpenEvent(ContainerOpenEvent event) {
    LootDropEvent lootEvent = new LootDropEvent();
    lootEvent.setLocation(event.getLocation());
    lootEvent.setLooterUUID(event.getPlayer().getUniqueId());
    lootEvent.setMonsterLevel(event.getData().getLevel());
    lootEvent.setQualityMultiplier(10.0);
    lootEvent.setQuantityMultiplier(10.0);
    lootEvent.setDistance(1);
    lootEvent.setLooterUUID(event.getPlayer() == null ? null : event.getPlayer().getUniqueId());
    lootEvent.setEntity(null);

    DropUtil.dropLoot(lootEvent);
  }
}
