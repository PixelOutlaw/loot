package info.faceland.loot.data.export;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class ExportEntry {

  @Getter @Setter
  private String title;
  @Getter @Setter
  private String name;
  @Getter @Setter
  private String strippedName;
  @Getter @Setter
  private List<String> description;
  @Getter @Setter
  private List<String> groupNames;
  @Getter @Setter
  private String rarity;
  @Getter @Setter
  private String specialFlag;
  @Getter @Setter
  private String type;

}
