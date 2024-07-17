package info.faceland.loot.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatResponse {

  private String statString;
  private String statPrefix;
  private float statRoll;
  private boolean inverted = false;
  private boolean crafted = false;
  private int value;
}
