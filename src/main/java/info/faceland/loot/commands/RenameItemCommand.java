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
package info.faceland.loot.commands;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandCompletion;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Default;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang.WordUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.utils.PersistHelper;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import java.util.regex.Pattern;
import ninja.amp.ampmenus.menus.common.ConfirmationMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("rename-item")
public class RenameItemCommand extends BaseCommand {

  private final LootPlugin plugin;

  private final String pattern = "^[a-zA-Z ']{3,22}$";
  private final Pattern regex = Pattern.compile(pattern);

  public RenameItemCommand(LootPlugin plugin) {
    this.plugin = plugin;
  }

  @Default
  @CommandCompletion("Snazzy Item")
  public void renamePet(Player player, @Default("") String name) {
    if (name == null || name.isEmpty()) {
      PaletteUtil.sendMessage(player, "|yellow|Failed! Usage: /rename-item <item name>");
      return;
    }
    if (!player.getInventory().contains(Material.NAME_TAG)) {
      PaletteUtil.sendMessage(player, "|yellow|Failed! You must have an item tag in your inventory!");
      return;
    }
    var selectedItem = player.getEquipment().getItemInMainHand();
    if (selectedItem == null || selectedItem.getType() == Material.AIR) {
      PaletteUtil.sendMessage(player, "|yellow|Failed! You must be holding the item you want to rename!");
      return;
    }
    var data = PersistHelper.getEquipmentData(selectedItem);
    if (data == null) {
      PaletteUtil.sendMessage(player, "|yellow|Failed! You can only rename equipment items!");
      return;
    }
    name = name.trim().replaceAll("\\s+", " ");
    if (!regex.matcher(name).matches() || StringUtils.countMatches(name, '\'') > 1) {
      PaletteUtil.sendMessage(player,
          "|yellow|Failed! Item name must be:\n- Between 3 and 22 characters\n- Only contain characters a-z, apostrophe, and space\n- Contain at most one apostrophe");
      return;
    }
    name = WordUtils.capitalizeFully(name);
    String finalName = name;

    ConfirmationMenu confirmationMenu = new ConfirmationMenu(
        "Rename Held Item?",
        FaceColor.GREEN + "Rename Held Item To " + name + "?",
        List.of(FaceColor.WHITE + "Click to confirm. This will consume", FaceColor.WHITE + "1x Item Tag."),
        FaceColor.YELLOW + "Cancel",
        List.of(FaceColor.WHITE + "Click to cancel"),
        true,
        true,
        () -> {
          ItemStack matchItem = null;
          for (var i : player.getInventory().getContents()) {
            if (i != null && i.getType() == Material.NAME_TAG) {
              var str = ItemStackExtensionsKt.getDisplayName(i);
              if (str != null && str.contains("Item Tag")) {
                matchItem = i;
                break;
              }
            }
          }
          if (matchItem == null) {
            PaletteUtil.sendMessage(player, "|yellow|Failed! You must have an item tag in your inventory!");
            return;
          }
          var selectedItem2 = player.getEquipment().getItemInMainHand();
          if (selectedItem2 == null || selectedItem2.getType() == Material.AIR) {
            PaletteUtil.sendMessage(player, "|yellow|Failed! You must be holding the item you want to rename!");
            return;
          }
          var data2 = PersistHelper.getEquipmentData(selectedItem2);
          if (data2 == null) {
            PaletteUtil.sendMessage(player, "|yellow|Failed! You can only rename equipment items!");
            return;
          }
          data2.setString("name", finalName);
          data2.setNameEdited(true);
          PersistHelper.setEquipmentData(selectedItem2, data2);
          matchItem.setAmount(matchItem.getAmount() - 1);
          MessageUtils.sendMessage(player, "&aItem renamed successfully!");
        },
        null
    );
    confirmationMenu.open(player);
  }
}