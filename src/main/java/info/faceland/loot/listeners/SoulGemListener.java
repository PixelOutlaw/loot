package info.faceland.loot.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.items.prefabs.SocketExtender;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import land.face.dinvy.events.GemSlotUpgradeEvent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class SoulGemListener implements Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  public void onGemUpgradeAttempt(GemSlotUpgradeEvent event) {
    if (event.getData().getSlots() > 4) {
      return;
    }
    int amount = (int) Math.pow(4, event.getData().getSlots());
    if (event.getData().getPlayer().getInventory().containsAtLeast(SocketExtender.EXTENDER, amount)) {
      MessageUtils.sendMessage(event.getData().getPlayer(),
          "&d&lYour Soul Grows Stronger! &dSoul Gem slot unlocked!");
      event.getData().setSlots(event.getData().getSlots() + 1);
      for (ItemStack stack : event.getData().getPlayer().getInventory().getContents()) {
        if (stack != null && SocketExtender.isSimilar(stack)) {
          if (stack.getAmount() > amount) {
            stack.setAmount(stack.getAmount() - amount);
            break;
          } else {
            amount -= stack.getAmount();
            stack.setAmount(0);
          }
        }
      }
    } else {
      MessageUtils.sendMessage(event.getData().getPlayer(),
          "&e&oYou don't have enough &3Socket Extenders &e&oto do this!");
      event.setCancelled(true);
    }
  }
}
