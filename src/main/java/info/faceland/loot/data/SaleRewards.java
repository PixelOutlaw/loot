package info.faceland.loot.data;

import lombok.Data;

@Data
public class SaleRewards {

  private double money;
  private double tradeXp;

  public SaleRewards(double money, double tradeXp) {
    this.money = money;
    this.tradeXp = tradeXp;
  }

}
