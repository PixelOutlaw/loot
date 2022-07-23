package info.faceland.loot.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import org.bukkit.ChatColor;

public class ItemRarity {

  private boolean broadcast;
  private double power;
  private FaceColor color;
  private String name;
  private double weight;
  private double idWeight;
  private int minimumBonusStats;
  private int maximumBonusStats;
  private int enchantments;
  private int minimumSockets;
  private int maximumSockets;
  private int extenderSlots;
  private int livedTicks;

  public boolean isBroadcast() {
    return broadcast;
  }

  public void setBroadcast(boolean broadcast) {
    this.broadcast = broadcast;
  }

  public int getMinimumBonusStats() {
    return minimumBonusStats;
  }

  public void setMinimumBonusStats(int minimumBonusStats) {
    this.minimumBonusStats = minimumBonusStats;
  }

  public int getMaximumBonusStats() {
    return maximumBonusStats;
  }

  public void setMaximumBonusStats(int maximumBonusStats) {
    this.maximumBonusStats = maximumBonusStats;
  }

  public int getEnchantments() {
    return enchantments;
  }

  public void setEnchantments(int enchantments) {
    this.enchantments = enchantments;
  }

  public int getMinimumSockets() {
    return minimumSockets;
  }

  public void setMinimumSockets(int minimumSockets) {
    this.minimumSockets = minimumSockets;
  }

  public int getMaximumSockets() {
    return maximumSockets;
  }

  public void setMaximumSockets(int maximumSockets) {
    this.maximumSockets = maximumSockets;
  }

  public int getExtenderSlots() {
    return extenderSlots;
  }

  public void setExtenderSlots(int extenderSlots) {
    this.extenderSlots = extenderSlots;
  }

  public int getLivedTicks() {
    return livedTicks;
  }

  public void setLivedTicks(int livedTicks) {
    this.livedTicks = livedTicks;
  }

  public double getPower() {
    return power;
  }

  public void setPower(double power) {
    this.power = power;
  }

  public FaceColor getColor() {
    return color;
  }

  public void setColor(FaceColor color) {
    this.color = color;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double weight) {
    this.weight = weight;
  }

  public double getIdWeight() {
    return idWeight;
  }

  public void setIdWeight(double idWeight) {
    this.idWeight = idWeight;
  }
}
