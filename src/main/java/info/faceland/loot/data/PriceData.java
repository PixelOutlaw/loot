package info.faceland.loot.data;

public class PriceData {

  private double price;
  private boolean rare;

  public PriceData (double price, boolean rare) {
    this.price = price;
    this.rare = rare;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public boolean isRare() {
    return rare;
  }

  public void setRare(boolean rare) {
    this.rare = rare;
  }

}
