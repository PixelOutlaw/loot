package info.faceland.loot.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;

@Getter
@Setter
public class ItemRarity {

  private final String id;
  private boolean broadcast;
  private boolean alwaysTrail;
  private boolean alwaysGlow;
  private double power;
  private FaceColor color;
  private ChatColor glowColor;
  private String name;
  private String plainName;
  private String tooltipPlate;
  private String tooltipPlateCrafted;
  private NamespacedKey tooltip;
  private double weight;
  private int minimumBonusStats;
  private int maximumBonusStats;
  private int enchantments;
  private double socketChance;
  private double extenderChance;
  private int livedTicks;

  public ItemRarity(String id) {
    this.id = id;
  }

}
