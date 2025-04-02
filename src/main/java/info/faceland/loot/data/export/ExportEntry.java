package info.faceland.loot.data.export;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;

@Getter
@Setter
public class ExportEntry {

  private String title;
  private String name;
  private String strippedName;
  private String tier;
  private List<String> description = new ArrayList<>();
  private List<String> passives = new ArrayList<>();
  private List<String> flavorText = new ArrayList<>();
  private List<String> groupNames = new ArrayList<>();
  private String rarity;
  private String specialFlag;
  private String type;
  private boolean enchantable = false;
  private int gemSlots = 0;
  private int extendSlots = 0;

  public void setName(String string) {
    name = string;
    strippedName = ChatColor.stripColor(PaletteUtil.color(string));
  }

}
