package info.faceland.loot.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PriceData {

  private double price;
  private boolean rare;

  public PriceData (double price, boolean rare) {
    this.price = price;
    this.rare = rare;
  }

}
