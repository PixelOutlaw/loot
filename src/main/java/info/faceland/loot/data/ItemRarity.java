package info.faceland.loot.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import lombok.Data;
import org.bukkit.ChatColor;

@Data
public class ItemRarity {

  private final String id;
  private boolean broadcast;
  private boolean alwaysTrail;
  private boolean alwaysGlow;
  private double power;
  private FaceColor color;
  private ChatColor glowColor;
  private String name;
  private double weight;
  private double idWeight;
  private int minimumBonusStats;
  private int maximumBonusStats;
  private int enchantments;
  private int minimumSockets;
  private int maximumSockets;
  private double socketChance;
  private double extenderChance;
  private int livedTicks;

  public ItemRarity(String id) {
    this.id = id;
  }

}
