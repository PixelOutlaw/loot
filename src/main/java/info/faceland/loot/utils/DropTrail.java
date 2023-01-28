package info.faceland.loot.utils;

import info.faceland.loot.LootPlugin;
import java.awt.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DropTrail extends BukkitRunnable {

  private final Player owner;
  private final Item item;
  private final DustOptions dustOptions;

  public DropTrail(Item item, Player owner, Color dropRgb) {
    this.item = item;
    this.owner = owner;
    dustOptions = new DustOptions(org.bukkit.Color.fromRGB(
        dropRgb.getRed(), dropRgb.getGreen(), dropRgb.getBlue()), 2);
    this.runTaskTimer(LootPlugin.getInstance(), 0L, 1L);
  }

  @Override
  public void run() {
    if (!item.isValid() || (item.getVelocity().getY() < 0.01 && item.isOnGround())) {
      cancel();
      return;
    }
    Location loc = item.getLocation().clone().add(item.getVelocity()).add(0, 0.5, 0);
    owner.spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, dustOptions);
  }
}
