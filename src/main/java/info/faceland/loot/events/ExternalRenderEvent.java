package info.faceland.loot.events;

import info.faceland.loot.api.events.LootEvent;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

public class ExternalRenderEvent extends LootEvent {

  @Getter
  private final ItemStack item;

  public ExternalRenderEvent(ItemStack item) {
    this.item = item;
  }

}
