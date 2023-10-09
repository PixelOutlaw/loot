package info.faceland.loot.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import info.faceland.loot.LootPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import teammt.villagerguiapi.classes.VillagerInventory;
import teammt.villagerguiapi.classes.VillagerTrade;

public class TradeMenuManager {

  private final LootPlugin plugin;
  private final Map<String, List<VillagerTrade>> storedTrades = new HashMap<>();

  public TradeMenuManager(LootPlugin plugin) {
    this.plugin = plugin;
  }

  public void startTrade(Player player, String tradeId, String menuName) {
    if (!storedTrades.containsKey(tradeId)) {
      Bukkit.getLogger().warning("[Loot] Trade " + tradeId + " doesn't exist! Can't open for " + player.getName());
      return;
    }
    VillagerInventory cr = new VillagerInventory(new ArrayList<>(storedTrades.get(tradeId)), player);
    cr.setName(PaletteUtil.culturallyEnrich(menuName));
    cr.open();
  }

  public void loadVillagerTrades(ConfigurationSection trade) {
    for (String nameSection : trade.getKeys(false)) {
      List<VillagerTrade> trades = new ArrayList<>(); // Create a new list of items
      ConfigurationSection section = trade.getConfigurationSection(nameSection);
      for (String key : section.getKeys(false)) {
        try {
          String optionOne = section.getString(key + ".item-one");
          int quantityOne = section.getInt(key + ".quantity-one", 1);
          ItemStack stack1 = plugin.getCustomItemManager().getCustomItem(optionOne).toItemStack(quantityOne);

          String optionTwo = section.getString(key + ".item-two");
          int quantityTwo = section.getInt(key + ".quantity-two", 0);
          ItemStack stack2 = null;
          if (optionTwo != null && quantityTwo >= 1) {
            stack2 = plugin.getCustomItemManager().getCustomItem(optionOne).toItemStack(quantityOne);
          }

          String result = section.getString(key + ".result");
          int resultQuantity = section.getInt(key + ".result-quantity", 0);
          ItemStack resulttem = plugin.getCustomItemManager().getCustomItem(result).toItemStack(resultQuantity);
          if (stack2 == null) {
            trades.add(new VillagerTrade(stack1, resulttem, 9999));
          } else {
            trades.add(new VillagerTrade(stack1, stack2, resulttem, 9999));
          }
        } catch (Exception e) {
          Bukkit.getLogger().warning("[Loot] Failed to load trade " + key + " for section " + nameSection);
          e.printStackTrace();
        }
      }
      storedTrades.put(nameSection, trades);
    }
  }
}
