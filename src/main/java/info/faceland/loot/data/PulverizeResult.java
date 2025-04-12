package info.faceland.loot.data;

import land.face.strife.data.champion.LifeSkillType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class PulverizeResult {

  private final LifeSkillType skillType;
  private final float experience;
  private final ItemStack stack;

  public PulverizeResult(ItemStack stack, LifeSkillType skillType, float xpAmount) {
    this.stack = stack;
    this.skillType = skillType;
    this.experience = xpAmount;
  }

}
