package info.faceland.loot.listeners;

import info.faceland.loot.events.LootDropEvent;
import info.faceland.loot.utils.DropUtil;
import land.face.containers.data.Prefab;
import land.face.containers.events.ContainerOpenEvent;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ContainerOpenListener implements Listener {

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onContainerOpenEvent(ContainerOpenEvent event) {

    Prefab prefab = event.getData().getPrefab();
    StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(event.getPlayer());
    float bonusAmount = Math.max(0, mob.getStat(StrifeStat.ITEM_DISCOVERY) / 100);
    float bonusQuality = Math.max(0, mob.getStat(StrifeStat.ITEM_RARITY) / 100);

    switch (prefab.getId()) {
      case "generic-chest" -> {
        LootDropEvent lootEvent = new LootDropEvent();
        lootEvent.setLocation(event.getLocation());
        lootEvent.setLooterUUID(event.getPlayer().getUniqueId());
        lootEvent.setMonsterLevel(event.getData().getLevel());
        lootEvent.setRarityBonus(4.0 + bonusAmount * 2);
        lootEvent.setAmountBonus(6.0 + bonusQuality * 2);
        lootEvent.setDistance(1);
        lootEvent.setLooterUUID(event.getPlayer() == null ? null : event.getPlayer().getUniqueId());
        lootEvent.setEntity(null);

        DropUtil.dropLoot(lootEvent);
      }
      default -> {
        LootDropEvent lootEvent = new LootDropEvent();
        lootEvent.setLocation(event.getLocation());
        lootEvent.setLooterUUID(event.getPlayer().getUniqueId());
        lootEvent.setMonsterLevel(event.getData().getLevel());
        lootEvent.setRarityBonus(0.5 + bonusQuality / 3);
        lootEvent.setAmountBonus(0.75 + bonusAmount / 3);
        lootEvent.setDistance(1);
        lootEvent.setLooterUUID(event.getPlayer() == null ? null : event.getPlayer().getUniqueId());
        lootEvent.setEntity(null);

        DropUtil.dropLoot(lootEvent);
      }
    }
  }
}
