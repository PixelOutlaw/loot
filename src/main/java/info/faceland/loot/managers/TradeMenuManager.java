package info.faceland.loot.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.data.Trade;
import info.faceland.loot.data.TradeMerchant;
import info.faceland.loot.utils.ItemRenderer;
import info.faceland.loot.utils.LootUtil;
import info.faceland.loot.utils.PersistHelper;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TradeMenuManager {

  private final LootPlugin plugin;
  private final Map<String, TradeMerchant> storedTrades = new HashMap<>();

  public TradeMenuManager(LootPlugin plugin) {
    this.plugin = plugin;
  }

  public void startTrade(Player player, String tradeId, String menuName) {
    if (!storedTrades.containsKey(tradeId)) {
      Bukkit.getLogger().warning("[Loot] Trade " + tradeId + " doesn't exist! Can't open for " + player.getName());
      return;
    }
    storedTrades.get(tradeId).open(player, PaletteUtil.culturallyEnrich(menuName));
  }

  public void loadVillagerTrades(ConfigurationSection trade) {
    for (String nameSection : trade.getKeys(false)) {
      ConfigurationSection section = trade.getConfigurationSection(nameSection);
      TradeMerchant merchantMenu = new TradeMerchant();
      for (String key : section.getKeys(false)) {
        try {
          String optionOne = section.getString(key + ".item-one");
          if (optionOne == null) {
            Bukkit.getLogger().warning("[Loot] Failed to load trade " + key + " NO TRADE ONE");
            continue;
          }
          int quantityOne = section.getInt(key + ".quantity-one", 1);
          ItemStack stack1 = LootUtil.getItem(plugin, optionOne);
          if (stack1 == null) {
            Bukkit.getLogger().warning("[Loot] Failed to load trade " + key + " INVALID TRADE ONE");
            continue;
          }
          stack1.setAmount(quantityOne);
          PersistHelper.setTradeLocked(stack1, section.getBoolean(key + ".trade-locked-one", false));
          ItemRenderer.renderItem(stack1, true);

          String optionTwo = section.getString(key + ".item-two");
          int quantityTwo = section.getInt(key + ".quantity-two", 0);
          ItemStack stack2 = null;
          if (optionTwo != null && quantityTwo >= 1) {
            stack2 = LootUtil.getItem(plugin, optionTwo);
            if (stack2 != null) {
              stack2.setAmount(quantityTwo);
              PersistHelper.setTradeLocked(stack2, section.getBoolean(key + ".trade-locked-two", false));
              ItemRenderer.renderItem(stack2, true);
            } else {
              Bukkit.getLogger().warning("[Loot] Failed to load trade " + key + " INVALID TRADE TWO");
              continue;
            }
          }

          String resultId = section.getString(key + ".result");
          if (resultId == null) {
            Bukkit.getLogger().warning("[Loot] Failed to load trade " + key + " INVALID RESULT");
            continue;
          }
          int resultQuantity = section.getInt(key + ".result-quantity", 0);
          ItemStack resultItem = LootUtil.getItem(plugin, resultId);
          if (resultItem == null) {
            Bukkit.getLogger().warning("[Loot] Failed to load trade " + key + " INVALID RESULT");
            continue;
          }
          resultItem.setAmount(resultQuantity);
          PersistHelper.setTradeLocked(resultItem, section.getBoolean(key + ".result-trade-locked", false));
          ItemRenderer.renderItem(resultItem, true);

          String permission = section.getString(key + ".permission", "");

          merchantMenu.addTrade(new Trade(stack1, stack2, resultItem, permission));
        } catch (Exception e) {
          Bukkit.getLogger().warning("[Loot] Failed to load trade " + key + " for section " + nameSection);
          e.printStackTrace();
        }
      }
      storedTrades.put(nameSection, merchantMenu);
    }
  }
}
