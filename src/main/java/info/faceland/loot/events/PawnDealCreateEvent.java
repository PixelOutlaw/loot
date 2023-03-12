package info.faceland.loot.events;

import info.faceland.loot.api.events.LootCancellableEvent;
import info.faceland.loot.data.PawnDeal;
import lombok.Getter;
import lombok.Setter;

public class PawnDealCreateEvent extends LootCancellableEvent {

  @Getter
  private final String shopId;
  @Getter
  private final int dealSlot;
  @Getter @Setter
  private PawnDeal result;

  public PawnDealCreateEvent(String shopId, int dealSlot) {
    this.shopId = shopId;
    this.dealSlot = dealSlot;
  }

}
