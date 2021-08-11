package info.faceland.loot.utils;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.managers.SocketGemManager;
import info.faceland.loot.sockets.SocketGem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class GemUtil {

  private GemUtil() {
    // do nothing
  }

  public static Set<SocketGem> getGems(SocketGemManager manager, ItemStack itemStack) {
    if (itemStack == null || itemStack.getType() == Material.AIR) {
      return new HashSet<>();
    }
    Set<SocketGem> gems = new HashSet<>();
    ItemStack item = new ItemStack(itemStack);
    List<String> lore = TextUtils.getLore(item);
    List<String> strippedLore = new ArrayList<>();
    for (String s : lore) {
      strippedLore.add(ChatColor.stripColor(s));
    }
    for (String key : strippedLore) {
      SocketGem gem = manager.getSocketGem(key);
      if (gem == null) {
        for (SocketGem g : manager.getSocketGems()) {
          if (!g.isTriggerable()) {
            continue;
          }
          if (key.equals(ChatColor.stripColor(TextUtils.color(
              g.getTriggerText() != null ? g.getTriggerText() : "")))) {
            gem = g;
            break;
          }
        }
        if (gem == null) {
          continue;
        }
      }
      gems.add(gem);
    }
    return gems;
  }

}
