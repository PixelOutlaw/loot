package info.faceland.loot.data;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.TextComponent;

@Getter
@Setter
public class StatResponse {

  private TextComponent statString;
  private String statPrefix;
  private float statRoll;
  private boolean inverted = false;
  private boolean crafted = false;
  private int value;
}
