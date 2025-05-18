package info.faceland.loot.listeners;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.component.ComponentType;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Server;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import org.bukkit.entity.Player;

public class PacketCleaner implements PacketListener {

  @Override
  public void onPacketSend(PacketSendEvent event) {
    if (event.getPacketType().equals(Server.SET_SLOT)) {
      var player = event.getPlayer();
      if (player instanceof Player p && p.hasPermission("bukkit.command.gamemode")) {
        return;
      }
      WrapperPlayServerSetSlot setSlotPacket = new WrapperPlayServerSetSlot(event);
      setSlotPacket.setItem(sanitize(event.getUser(), setSlotPacket.getItem()));
    } else if (event.getPacketType().equals(Server.WINDOW_ITEMS)) {
      var player = event.getPlayer();
      if (player instanceof Player p && p.hasPermission("bukkit.command.gamemode")) {
        return;
      }
      User user = event.getUser();
      WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);
      packet.getCarriedItem().ifPresent(is -> sanitize(user, packet.getCarriedItem().get()));
      packet.getItems().forEach(is -> sanitize(user, is));
    } else if (event.getPacketType().equals(Server.ENTITY_METADATA)) {
      WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(event);
      if (packet.getEntityMetadata() == null || packet.getEntityMetadata().isEmpty()) {
        return;
      }
      var entityData = packet.getEntityMetadata().stream()
          .filter(e -> e.getType() == EntityDataTypes.ITEMSTACK).findFirst();
      entityData.ifPresent(item -> {
        var player = event.getPlayer();
        if (player instanceof Player p && p.hasPermission("bukkit.command.gamemode")) {
          return;
        }
        var stack = (ItemStack) item.getValue();
        item.setValue(sanitize(event.getUser(), stack));
      });
    } else if (event.getPacketType().equals(Server.ENTITY_EQUIPMENT)) {
      User user = event.getUser();
      WrapperPlayServerEntityEquipment packet = new WrapperPlayServerEntityEquipment(event);
      packet.getEquipment().forEach(eq -> {
        if (eq.getItem() != null) {
          sanitize(user, eq.getItem());
        }
      });
    }
  }

  private ItemStack sanitize(User user, ItemStack stack) {
    if (user == null) {
      return stack;
    }
    stack.unsetComponent(ComponentTypes.CUSTOM_DATA);
    var enchants = stack.getEnchantments(user.getClientVersion());
    if (enchants != null && !enchants.isEmpty()) {
      enchants.removeIf(e -> e.getLevel() == 11);
      stack.setEnchantments(enchants, user.getClientVersion());
    }
    return stack;
  }
}