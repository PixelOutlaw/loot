package info.faceland.loot.data;

import lombok.Getter;
import lombok.Setter;

public class StatResponse {

  private String statString;
  private String statPrefix;
  private float statRoll;
  @Getter @Setter
  private boolean inverted = false;
  @Getter @Setter
  private boolean crafted = false;

  public String getStatString() {
    return statString;
  }

  public void setStatString(String statString) {
    this.statString = statString;
  }

  public String getStatPrefix() {
    return statPrefix;
  }

  public void setStatPrefix(String statPrefix) {
    this.statPrefix = statPrefix;
  }

  public float getStatRoll() {
    return statRoll;
  }

  public void setStatRoll(float statRoll) {
    this.statRoll = statRoll;
  }
}
