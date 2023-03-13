package info.faceland.loot.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class LootDeconstructEvent extends Event {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  public static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  private ItemStack cursorItem;
  private ItemStack targetItem;
  private Player player;
  private boolean cancelled;

  @Override
  public HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  public ItemStack getCursorItem() {
    return cursorItem;
  }

  public void setCursorItem(ItemStack cursorItem) {
    this.cursorItem = cursorItem;
  }

  public ItemStack getTargetItem() {
    return targetItem;
  }

  public void setTargetItem(ItemStack targetItem) {
    this.targetItem = targetItem;
  }

  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public boolean isCancelled() {
    return cancelled;
  }

  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }
}
