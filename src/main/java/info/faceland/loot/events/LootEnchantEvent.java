package info.faceland.loot.events;

import info.faceland.loot.api.events.LootCancellableEvent;
import info.faceland.loot.items.EnchantmentTome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class LootEnchantEvent extends LootCancellableEvent {

  private final EnchantmentTome tome;
  private final ItemStack targetItem;
  private final Player player;

  public LootEnchantEvent(Player player, ItemStack targetItem, EnchantmentTome tome) {
    this.player = player;
    this.targetItem = targetItem;
    this.tome = tome;
  }

  public EnchantmentTome getTome() {
    return tome;
  }

  public ItemStack getTargetItem() {
    return targetItem;
  }

  public Player getPlayer() {
    return player;
  }

}
