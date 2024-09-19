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
package info.faceland.loot.listeners;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.utils.ItemRenderer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.dinvy.entity.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class JoinListener implements Listener {

  private final LootPlugin plugin;
  private final Set<UUID> scannedPlayers = new HashSet<>();

  public JoinListener(LootPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (scannedPlayers.contains(event.getPlayer().getUniqueId())) {
      return;
    }
    final Player player = event.getPlayer();
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (!player.isOnline()) {
        return;
      }
      PlayerData playerData = DeluxeInvyPlugin.getInstance().getPlayerManager().getPlayerData(player);
      if (playerData == null) {
        return;
      }
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.MOUNT), false);
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.PET), false);
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.COSMETIC_HAT), false);

      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.EARRING_1), false);
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.EARRING_2), false);
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.NECKLACE), false);
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.RING_1), false);
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.RING_2), false);

      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.HELMET), false);
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.BODY), false);
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.LEGS), false);
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.BOOTS), false);
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.OFF_HAND), false);

      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.SOUL_GEM_1), false);
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.SOUL_GEM_2), false);
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.SOUL_GEM_3), false);
      ItemRenderer.renderItem(playerData.getEquipmentItem(DeluxeSlot.SOUL_GEM_4), false);

      for (int i = 4; i <= 35; i++) {
        ItemRenderer.renderItem(player.getInventory().getItem(i), false);
      }
      scannedPlayers.add(player.getUniqueId());
    }, 30L);
  }
}