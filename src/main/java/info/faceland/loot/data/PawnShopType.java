package info.faceland.loot.data;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.events.PawnDealCreateEvent;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

@Data
public class PawnShopType {

  @Getter
  private String dealId;
  @Getter @Setter
  private PawnDeal dealOne;
  @Getter @Setter
  private PawnDeal dealTwo;
  @Getter @Setter
  private PawnDeal dealThree;

  public PawnShopType(String dealId) {
    this.dealId = dealId;
    checkDealChange();
  }

  public boolean checkDealChange() {
    boolean update = false;
    if (dealOne == null || dealOne.getMinutesRemaining() == 0) {
      PawnDealCreateEvent event = new PawnDealCreateEvent(dealId, 1);
      Bukkit.getPluginManager().callEvent(event);
      if (event.getResult() != null) {
        dealOne = event.getResult();
        dealOne.setMinutesRemaining(4 + (int) (LootPlugin.RNG.nextFloat() * 3.5));
        update = true;
      }
    }
    else {
      dealOne.setMinutesRemaining(dealOne.getMinutesRemaining() - 1);
    }
    if (dealTwo == null || dealTwo.getMinutesRemaining() == 0) {
      PawnDealCreateEvent event = new PawnDealCreateEvent(dealId, 2);
      Bukkit.getPluginManager().callEvent(event);
      if (event.getResult() != null) {
        dealTwo = event.getResult();
        dealTwo.setMinutesRemaining(8 + (int) (LootPlugin.RNG.nextFloat() * 4.5));
        update = true;
      }
    } else {
      dealTwo.setMinutesRemaining(dealTwo.getMinutesRemaining() - 1);
    }
    if (dealThree == null || dealThree.getMinutesRemaining() == 0) {
      PawnDealCreateEvent event = new PawnDealCreateEvent(dealId, 3);
      Bukkit.getPluginManager().callEvent(event);
      if (event.getResult() != null) {
        dealThree = event.getResult();
        dealThree.setMinutesRemaining(17 + (int) (LootPlugin.RNG.nextFloat() * 7.5));
        update = true;
      }
    } else {
      dealThree.setMinutesRemaining(dealThree.getMinutesRemaining() - 1);
    }
    return update;
  }
}
