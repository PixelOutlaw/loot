/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.loot.menu.tomereader;

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import info.faceland.loot.LootPlugin;
import info.faceland.loot.items.EnchantmentTome;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.SkillRank;
import ninja.amp.ampmenus.menus.ItemMenu;
import org.bukkit.entity.Player;

public class ReaderMenu extends ItemMenu {

  private final LootPlugin plugin;
  private static ReaderMenu instance;
  public static final Map<UUID, List<EnchantmentTome>> TOME_OPTIONS = new HashMap<>();

  /*
  00 01 02 03 04 05 06 07 08
  09 10 11 12 13 14 15 16 17
  18 19 20 21 22 23 24 25 26
  27 28 29 30 31 32 33 34 35
  36 37 38 39 40 41 42 43 44
  45 46 47 48 49 50 51 52 53
  */

  public ReaderMenu(LootPlugin plugin) {
    super(TextUtils.color(plugin.getSettings().getString("language.menu.reader-name","&0&lMortar")), Size.THREE_LINE, plugin);
    this.plugin = plugin;

    setItem(10, new TomeOptionIcon(this, 0));
    setItem(11, new TomeOptionIcon(this, 1));
    setItem(12, new TomeOptionIcon(this, 2));
    setItem(13, new TomeOptionIcon(this, 3));
    setItem(14, new TomeOptionIcon(this, 4));
    setItem(15, new TomeOptionIcon(this, 5));
    setItem(16, new TomeOptionIcon(this, 6));

    instance = this;
  }

  @Override
  public void open(Player player) {
    super.open(player);
  }

  public static void show(Player player) {
    instance.open(player);
  }

  public static void generateOptions(LootPlugin plugin, Player player) {
    SkillRank rank = SkillRank.getRank(plugin.getStrifePlugin().getChampionManager().getChampion(player), LifeSkillType.LOREMASTER);
    int minTomes = 1;
    int maxTomes = 3;
    double increaseOdds = 0.28;
    switch (rank) {
      case APPRENTICE -> {
        // maxTomes = 3
        // minTomes = 1;
        increaseOdds = 0.4;
      }
      case JOURNEYMAN -> {
        // minTomes = 1;
        maxTomes = 4;
        increaseOdds = 0.4;
      }
      case EXPERT -> {
        minTomes = 2;
        maxTomes = 4;
        increaseOdds = 0.4;
      }
      case MASTER -> {
        minTomes = 2;
        maxTomes = 4;
        increaseOdds = 0.6;
      }
    }
    int amount = 0;
    List<EnchantmentTome> tomes = new ArrayList<>();
    while (amount < minTomes || (amount < maxTomes && LootPlugin.RNG.nextDouble() < increaseOdds)) {
      tomes.add(plugin.getEnchantTomeManager().getRandomEnchantTome());
      amount++;
    }
    TOME_OPTIONS.put(player.getUniqueId(), tomes);
  }

  public void rewardOptions(Player player, int pick) {
    if (pick == -1) {
      var tome = TOME_OPTIONS.get(player.getUniqueId())
          .get(LootPlugin.RNG.nextInt(TOME_OPTIONS.get(player.getUniqueId()).size()));
      TOME_OPTIONS.remove(player.getUniqueId());
      ItemUtils.giveOrDrop(player, tome.item());
      plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
          LifeSkillType.LOREMASTER, 8, false, false);
      return;
    }
    var tome = TOME_OPTIONS.get(player.getUniqueId()).get(pick);
    ItemUtils.giveOrDrop(player, tome.item());
    plugin.getStrifePlugin().getSkillExperienceManager().addExperience(player,
        LifeSkillType.LOREMASTER, 8, false, false);
    TOME_OPTIONS.remove(player.getUniqueId());
  }

}
