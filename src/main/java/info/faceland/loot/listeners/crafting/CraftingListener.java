/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.listeners.crafting;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.loot.LootPlugin;
import land.face.strife.data.champion.LifeSkillType;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public final class CraftingListener implements Listener {

  private final LootPlugin plugin;

  public static Recipe TRANSMUTE_RECIPE = null;

  public CraftingListener(LootPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void CRAFT_SECURITY(CraftItemEvent event) {
    if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER
        || event.getCursor().getType() == Material.BARRIER) {
      event.setCancelled(true);
    }
    Player player = (Player) event.getWhoClicked();
    if (player.getItemOnCursor().getType() != Material.AIR) {
      if (!player.getItemOnCursor().isSimilar(event.getRecipe().getResult())) {
        sendMessage(player, plugin.getSettings().getString("language.craft.no-cursor", ""));
        event.setCancelled(true);
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onCraftTransmute(CraftItemEvent event) {
    if (event.isCancelled()) {
      return;
    }
    if (event.getRecipe().getResult().getType() == Material.EMERALD) {
      Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getStrifePlugin()
          .getSkillExperienceManager().addExperience((Player) event.getWhoClicked(),
              LifeSkillType.ALCHEMY, 0.75, false, false), 0L);
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEssenceInfuse(CraftItemEvent event) {
    if (event.isCancelled()) {
      return;
    }
    for (ItemStack is : event.getInventory().getMatrix()) {
      if (is == null) {
        continue;
      }
      if (is.getType() == Material.GHAST_TEAR) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getStrifePlugin()
            .getSkillExperienceManager().addExperience((Player) event.getWhoClicked(),
                LifeSkillType.CRAFTING, 25, false, false), 0L);
        return;
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onEssenceInfuseFailsafe(CraftItemEvent event) {
    if (event.isCancelled()) {
      return;
    }
    var result = event.getInventory().getResult();
    if (result == null || result.getType() == Material.END_CRYSTAL) {
      MessageUtils.sendMessage(event.getWhoClicked(), ChatColor.RED + "Crafting failed!");
      event.setCancelled(true);
    }
  }

  private boolean isDyeEvent(Material ingredient, Material result) {
    return ingredient == result && (ingredient == Material.LEATHER_HELMET
        || ingredient == Material.LEATHER_CHESTPLATE || ingredient == Material.LEATHER_LEGGINGS
        || ingredient == Material.LEATHER_BOOTS);
  }
}
