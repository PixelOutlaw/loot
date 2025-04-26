package info.faceland.loot.data;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.data.champion.LifeSkillType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class Trade {

  public Trade(ItemStack cost1, ItemStack cost2, ItemStack reward, String permission) {
    this.cost1 = cost1;
    this.cost2 = cost2;
    this.reward = reward;
    this.permission = permission;
  }

  private ItemStack cost1;
  private ItemStack cost2;
  private ItemStack reward;
  private String permission;
  private Map<LifeSkillType, Integer> skillReqs = new HashMap<>();

}
