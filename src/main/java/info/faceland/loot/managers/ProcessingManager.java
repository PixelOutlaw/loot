package info.faceland.loot.managers;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.menu.processing.ProcessingMenu;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;

public class ProcessingManager {

  private final LootPlugin plugin;
  private final Map<String, ProcessingMenu> processingGUIMap = new HashMap<>();

  private ProcessingManager(LootPlugin plugin) {
    this.plugin = plugin;
  }

  public void open(Player player, String menu) {
    if (!processingGUIMap.containsKey(menu)) {
      return;
    }
    processingGUIMap.get(menu).open(player);
  }

  /*
  public loadProcessingMenus(SmartYamlConfiguration configuration) {
    for (String key : configuration.getKeys(false)) {
      ConfigurationSection optionSection = configuration.getConfigurationSection(key);
      for (String optionName : optionSection.getKeys(false)) {
        ConfigurationSection itemSection = optionSection.getConfigurationSection(optionName);

        itemSection.getString("material");
        itemSection.getInt("custom-model-data");

      }
      ProcessingMenu menu = new ProcessingMenu(plugin)
    }
  }
  */
}
