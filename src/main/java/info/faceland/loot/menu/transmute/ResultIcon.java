/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.menu.transmute;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.listeners.sockets.CombinerListener;
import info.faceland.loot.sockets.SocketGem;
import info.faceland.loot.utils.InventoryUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import ninja.amp.ampmenus.events.ItemClickEvent;
import ninja.amp.ampmenus.items.MenuItem;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ResultIcon extends MenuItem {

  private final TransmuteMenu menu;
  @Getter @Setter
  private ItemStack stack;
  private final ItemStack ready;

  ResultIcon(TransmuteMenu menu) {
    super("", new ItemStack(Material.AIR));
    this.menu = menu;
    ready = new ItemStack(Material.NETHER_STAR);
    ItemStackExtensionsKt.setDisplayName(ready, TextUtils.color("&3&lClick To Transmute!"));
    TextUtils.setLore(ready, TextUtils.color(List.of(
        "&7Transmute these &ffour &7gems into",
        "&7a random &6II+ &7gem!"
    )));
  }

  @Override
  public ItemStack getFinalIcon(Player player) {
    if (stack != null) {
      return stack;
    }
    if (menu.isReady()) {
      return ready;
    }
    return new ItemStack(Material.AIR);
  }

  @Override
  public void onItemClick(ItemClickEvent event) {
    super.onItemClick(event);

    if (stack != null) {
      menu.ejectResult(event.getPlayer());
      event.setWillUpdate(true);
      return;
    }

    if (menu.isReady()) {
      if (!menu.takeGems(event.getPlayer())) {
        MessageUtils.sendMessage(event.getPlayer(),
            "&e[!] Transmute failed! Make sure you still have the gems you've selected!");
        event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(),
            Sound.BLOCK_GLASS_BREAK, 1.0f, 0.7f);
        event.setWillUpdate(false);
        return;
      }
      SocketGem gem = LootPlugin.getInstance().getSocketGemManager().getRandomSocketGemByBonus();
      ItemStack gemItem = gem.toItemStack(1);
      if (gem.isBroadcast()) {
        InventoryUtil.sendToDiscord(event.getPlayer(), gemItem, CombinerListener.transmuteFormat);
      }
      stack = gemItem;
      playTransmuteEffects(event.getPlayer());
      event.setWillUpdate(true);
      return;
    }
    event.setWillUpdate(false);
  }

  private void playTransmuteEffects(Player player) {
    MessageUtils.sendMessage(player,
        LootPlugin.getInstance().getSettings().getString("language.socket.transmute-success", ""));
    player.getWorld()
        .spawnParticle(Particle.ENCHANTMENT_TABLE, player.getLocation().clone().add(0, 1, 0), 30, 5,
            5, 5);
    player.getWorld().playSound(player.getLocation().clone(), Sound.BLOCK_GLASS_BREAK, 1.0f, 1.0f);
    player.getWorld()
        .playSound(player.getLocation().clone(), Sound.BLOCK_END_GATEWAY_SPAWN, 1.0f, 2f);
  }

}
