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

import static info.faceland.loot.listeners.sockets.SocketsListener.applyEffects;

import info.faceland.loot.LootPlugin;
import info.faceland.loot.api.data.GemCacheData;
import info.faceland.loot.api.sockets.effects.SocketEffect;
import info.faceland.loot.sockets.SocketGem.GemType;
import java.util.HashSet;
import java.util.Set;
import land.face.strife.events.CriticalEvent;
import land.face.strife.events.EvadeEvent;
import land.face.strife.events.UniqueSpawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class StrifeListener implements Listener {

  private final LootPlugin plugin;

  public StrifeListener(LootPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onCriticalHit(CriticalEvent event) {
    if (event.getAttacker() instanceof Player) {
      Set<SocketEffect> attackerEffects = new HashSet<>();
      Player p = (Player) event.getAttacker();
      GemCacheData attackerData = plugin.getGemCacheManager().getGemCacheData(p.getUniqueId());
      attackerEffects.addAll(attackerData.getWeaponCache(GemType.ON_CRIT));
      applyEffects(attackerEffects, p, event.getVictim().getEntity());
    }
  }

  @EventHandler
  public void onEvade(EvadeEvent event) {
    if (event.getEvader() instanceof Player) {
      Set<SocketEffect> defenderEffects = new HashSet<>();
      Player p = (Player) event.getEvader();
      GemCacheData defenderData = plugin.getGemCacheManager().getGemCacheData(p.getUniqueId());
      defenderEffects.addAll(defenderData.getArmorCache(GemType.ON_EVADE));
      applyEffects(defenderEffects, p, event.getAttacker().getEntity());
    }
  }

  @EventHandler
  public void onEvade(UniqueSpawnEvent event) {
    if (!"INTENRAL_ROGUE".equals(event.getStrifeMob().getUniqueEntityId())) {
      return;
    }
    event.getStrifeMob().setUseEquipment(true);
  }
}